package appbackend.wobbrik.game

import appbackend.wobbrik.billing.*
import kotlinx.coroutines.Dispatchers
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

class GameplayHttpBackend(private val sessions: Sessions, private val transport: GameplayTransport) : AttemptBackend, GameplayReads {
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
            AttemptCommand.Route.SHOP_BUY -> "shop/buy"
            AttemptCommand.Route.BUILDING_EDIT -> "buildings/edit"
            AttemptCommand.Route.BUILDING_DELETE -> "buildings/delete"
            else -> "attempts/" + command.route.name.lowercase(java.util.Locale.ROOT)
        }
        val body = try { request(session, path, command.envelope) }
        catch (failure: GameplayHttpError) {
            if (command.route in setOf(AttemptCommand.Route.SHOP_BUY, AttemptCommand.Route.BUILDING_EDIT, AttemptCommand.Route.BUILDING_DELETE) &&
                ((failure.status == 409 && failure.code in setOf("shop_conflict", "insufficient_balance", "building_conflict", "listing_conflict")) ||
                 (failure.status == 404 && failure.code == "not_found") ||
                 (failure.status == 400 && failure.code == "invalid_request"))) throw MutationRejected(requireNotNull(failure.code))
            throw failure
        }
        when (command.route) {
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
    override suspend fun request(path: String, bearer: String, body: String?): GameplayResponse = withContext(Dispatchers.IO) {
        require(allowedReadPath(path) || path in setOf("sync", "catalog", "shop/buy", "buildings/edit", "buildings/delete", "attempts/start", "attempts/checkpoint", "attempts/finish", "attempts/abandon"))
        val suffix = if (path == "catalog") "catalog?rules_version=wobbrik-v1" else path
        val responseLimit = if (path.startsWith("inventory?")) 1_048_576 else 65_536
        val connection = URI("$base/v1/apps/wobbrik/game/$suffix").toURL().openConnection() as HttpURLConnection
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 8000; connection.readTimeout = 8000
            connection.setRequestProperty("Authorization", "Bearer $bearer")
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                val bytes = body.toByteArray(Charsets.UTF_8); require(bytes.size <= 32768)
                connection.requestMethod = "POST"; connection.doOutput = true
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
        } finally { connection.disconnect() }
    }
}

/** Keep authenticated GET paths constrained; IDs/cursors cannot escape the game API. */
internal fun allowedReadPath(path: String): Boolean =
    Regex("(?:inventory|city|decorations)\\?limit=20(?:&cursor=[A-Za-z0-9_-]{1,128})?").matches(path) ||
        Regex("(?:buildings|attempts|operations)/[A-Za-z0-9_-]{1,128}").matches(path)
