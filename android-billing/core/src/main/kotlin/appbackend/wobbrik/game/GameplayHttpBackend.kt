package appbackend.wobbrik.game

import appbackend.wobbrik.billing.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/** Injectable boundary for deterministic lost-response and authentication tests. */
fun interface GameplayTransport {
    suspend fun request(path: String, bearer: String, body: String?): GameplayResponse
}
data class GameplayResponse(val status: Int, val body: String)
class GameplayHttpError(val status: Int, val code: String?) : IOException("Gameplay request failed: $status ${code.orEmpty()}")

fun interface GameplayReads {
    suspend fun read(session: BillingSession, path: String): String
}

class GameplayHttpBackend(private val sessions: Sessions, private val transport: GameplayTransport) : AttemptBackend, GameplayReads, ProfileAccess {
    override suspend fun profile(session: BillingSession): PublicProfile = PublicProfiles.parse(request(session, "profile", null))
    override suspend fun replaceProfile(session: BillingSession, replacement: ProfileReplacement): PublicProfile =
        PublicProfiles.parse(request(session, "profile", replacement.json())).also {
            require(it.revision > 0 && it.displayName == replacement.displayName && it.avatarId == replacement.avatarId)
        }
    override suspend fun catalog(session: BillingSession): RecoveryCatalog = RecoveryDtos.catalog(request(session, "catalog", null))
    override suspend fun sync(session: BillingSession): GameSnapshot = GameplayWire.snapshot(request(session, "sync", null))
    override suspend fun attempt(session: BillingSession, id: String): GameSnapshot? {
        InventoryReads.id(id)
        val body = lookup(session, "attempts/$id") ?: return null
        return RecoveryDtos.snapshot(body, allowAbandoned = true).also { require(it.attempt?.id == id) }
    }
    override suspend fun receipt(session: BillingSession, id: String, route: AttemptCommand.Route): AttemptReceipt? {
        InventoryReads.id(id)
        return lookup(session, "operations/$id")?.let { RecoveryDtos.attemptReceipt(it, route) }
    }
    private suspend fun lookup(session: BillingSession, path: String): String? = try { request(session, path, null) }
        catch (failure: GameplayHttpError) {
            if (failure.status == 404 && failure.code == "not_found") null else throw failure
        }
    override suspend fun submit(session: BillingSession, command: AttemptCommand) {
        GameplayWire.validate(command)
        val path = when (command.route) {
            AttemptCommand.Route.MARKET_LIST -> "market/list"
            AttemptCommand.Route.MARKET_WITHDRAW -> "market/withdraw"
            AttemptCommand.Route.MARKET_BUY -> "market/buy"
            AttemptCommand.Route.SHOP_BUY -> "shop/buy"
            AttemptCommand.Route.BUILDING_EDIT -> "buildings/edit"
            AttemptCommand.Route.BUILDING_DELETE -> "buildings/delete"
            else -> "attempts/" + command.route.name.lowercase(java.util.Locale.ROOT)
        }
        val body = try { request(session, path, command.envelope) }
        catch (failure: GameplayHttpError) {
            if (command.route in MarketMutations.routes + setOf(AttemptCommand.Route.SHOP_BUY, AttemptCommand.Route.BUILDING_EDIT, AttemptCommand.Route.BUILDING_DELETE) &&
                ((failure.status == 409 && failure.code in setOf("shop_conflict", "insufficient_balance", "building_conflict", "listing_conflict")) ||
                 (failure.status == 404 && failure.code == "not_found") ||
                 (failure.status == 400 && failure.code == "invalid_request"))) throw MutationRejected(requireNotNull(failure.code))
            throw failure
        }
        when (command.route) {
            AttemptCommand.Route.MARKET_LIST, AttemptCommand.Route.MARKET_WITHDRAW, AttemptCommand.Route.MARKET_BUY -> MarketMutations.receipt(body, command)
            AttemptCommand.Route.SHOP_BUY, AttemptCommand.Route.BUILDING_EDIT -> PartsMutations.receipt(body, command)
            AttemptCommand.Route.BUILDING_DELETE -> BuildingDeletion.receipt(body, command)
            else -> RecoveryDtos.attemptReceipt(body, command.route)
        }
    }
    override suspend fun read(session: BillingSession, path: String): String = request(session, path, null)
    private suspend fun request(session: BillingSession, path: String, body: String?): String {
        current(session)
        val token = sessions.idToken(session)
        current(session)
        require(token.isNotBlank())
        val response = transport.request(path, token, body)
        current(session)
        if (response.status !in 200..299) {
            val code = GameplayWire.error(response.body)
            if (response.status == 409 && code == "revision_conflict") throw RevisionConflict()
            throw GameplayHttpError(response.status, code)
        }
        return response.body
    }
    private fun current(session: BillingSession) { if (sessions.current() != session) throw AccountChanged() }
}

/** HTTPS origin only; redirects disabled so credentials never follow a redirect. */
class UrlConnectionGameplayTransport(origin: String) : GameplayTransport {
    private val base = URI(origin).also {
        require(it.scheme == "https" && !it.host.isNullOrBlank() && it.userInfo == null && it.query == null && it.fragment == null)
        require(it.path.isNullOrEmpty() || it.path == "/")
    }.toString().trimEnd('/')
    @OptIn(InternalCoroutinesApi::class)
    override suspend fun request(path: String, bearer: String, body: String?): GameplayResponse = withContext(Dispatchers.IO) {
        require(allowedReadPath(path) || path in setOf("sync", "catalog", "market/list", "market/withdraw", "market/buy", "shop/buy", "buildings/edit", "buildings/delete", "attempts/start", "attempts/checkpoint", "attempts/finish", "attempts/abandon"))
        val suffix = if (path == "catalog") "catalog?rules_version=wobbrik-v1" else path
        val responseLimit = if (path.startsWith("inventory?") || path.startsWith("market/")) 1_048_576 else 65_536
        val connection = URI("$base/v1/apps/wobbrik/game/$suffix").toURL().openConnection() as HttpURLConnection
        // Close the underlying socket, rather than disconnecting while the HTTPS
        // implementation may hold its connection/SSL read lock. Ownership is per request.
        val sockets = RequestSockets((connection as javax.net.ssl.HttpsURLConnection).sslSocketFactory)
        connection.sslSocketFactory = sockets
        val context = currentCoroutineContext()
        val cancellation = context[Job]!!.invokeOnCompletion(onCancelling = true, invokeImmediately = true) { cause ->
            if (cause is CancellationException) sockets.cancel()
        }
        try {
            context.ensureActive()
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 8000; connection.readTimeout = 8000
            connection.setRequestProperty("Authorization", "Bearer $bearer")
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                val bytes = body.toByteArray(Charsets.UTF_8); require(bytes.size <= 32768)
                connection.requestMethod = if (path == "profile") "PUT" else "POST"; connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val bytes = stream?.use { input ->
                val out = ByteArrayOutputStream(); val buffer = ByteArray(4096)
                while (out.size() <= responseLimit) {
                    val n = input.read(buffer, 0, minOf(buffer.size, responseLimit + 1 - out.size()))
                    if (n < 0) break
                    out.write(buffer, 0, n)
                }
                out.toByteArray()
            } ?: byteArrayOf()
            if (bytes.size > responseLimit) throw IOException("Gameplay response too large")
            GameplayResponse(status, String(bytes, Charsets.UTF_8))
        } catch (failure: IOException) {
            context.ensureActive()
            throw failure
        } finally {
            cancellation.dispose()
            connection.disconnect()
        }
    }
}

/** Keep authenticated GET paths constrained; IDs/cursors cannot escape the game API. */
internal fun allowedReadPath(path: String): Boolean =
    Regex("city\\?limit=100(?:&cursor=[A-Za-z0-9_-]{1,128})?").matches(path) ||
    Regex("buildings/[A-Za-z0-9_-]{1,128}\\?view=display").matches(path) ||
    Regex("city\\?limit=20(?:&cursor=[A-Za-z0-9_-]{1,128})?&include=details").matches(path) ||
    Regex("inventory\\?limit=20(?:&cursor=[A-Za-z0-9_-]{1,128})?&view=display").matches(path) ||
    path == "profile" || LeaderboardReads.allowedPath(path) || MarketReads.allowedPath(path) ||
    Regex("(?:inventory|city|decorations)\\?limit=20(?:&cursor=[A-Za-z0-9_-]{1,128})?").matches(path) ||
        Regex("(?:buildings|attempts|operations)/[A-Za-z0-9_-]{1,128}").matches(path)

/** Tracks only this connection's sockets, including the raw socket under TLS. */
private class RequestSockets(private val delegate: javax.net.ssl.SSLSocketFactory) : javax.net.ssl.SSLSocketFactory() {
    private val sockets = mutableListOf<java.net.Socket>()
    private var cancelled = false
    private fun track(socket: java.net.Socket): java.net.Socket {
        val close = synchronized(this) {
            if (cancelled) true else { sockets.add(socket); false }
        }
        if (close) {
            runCatching { socket.close() }
            throw IOException("Gameplay request cancelled")
        }
        return socket
    }
    fun cancel() {
        val owned = synchronized(this) { cancelled = true; sockets.toList() }
        // Raw sockets are registered before layered TLS sockets.
        owned.forEach { runCatching { it.close() } }
    }
    override fun getDefaultCipherSuites() = delegate.defaultCipherSuites
    override fun getSupportedCipherSuites() = delegate.supportedCipherSuites
    override fun createSocket(socket: java.net.Socket, host: String, port: Int, autoClose: Boolean): java.net.Socket {
        track(socket)
        return track(delegate.createSocket(socket, host, port, autoClose))
    }
    override fun createSocket(): java.net.Socket = track(delegate.createSocket())
    override fun createSocket(host: String, port: Int): java.net.Socket = track(delegate.createSocket(host, port))
    override fun createSocket(host: String, port: Int, local: java.net.InetAddress, localPort: Int): java.net.Socket =
        track(delegate.createSocket(host, port, local, localPort))
    override fun createSocket(host: java.net.InetAddress, port: Int): java.net.Socket = track(delegate.createSocket(host, port))
    override fun createSocket(host: java.net.InetAddress, port: Int, local: java.net.InetAddress, localPort: Int): java.net.Socket =
        track(delegate.createSocket(host, port, local, localPort))
}
