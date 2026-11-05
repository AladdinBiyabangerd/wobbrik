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

class GameplayHttpBackend(private val sessions: Sessions, private val transport: GameplayTransport) : AttemptBackend {
    override suspend fun sync(session: BillingSession): GameSnapshot = GameplayWire.snapshot(request(session, "sync", null))
    override suspend fun submit(session: BillingSession, command: AttemptCommand) {
        GameplayWire.validate(command)
        val path = "attempts/" + command.route.name.lowercase(java.util.Locale.ROOT)
        RecoveryDtos.attemptReceipt(request(session, path, command.envelope), command.route)
    }
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
        require(path in setOf("sync", "attempts/start", "attempts/checkpoint", "attempts/finish"))
        val connection = URI("$base/v1/apps/wobbrik/game/$path").toURL().openConnection() as HttpURLConnection
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
                while (out.size() <= 65536) {
                    val n = input.read(buffer, 0, minOf(buffer.size, 65537 - out.size()))
                    if (n < 0) break
                    out.write(buffer, 0, n)
                }
                out.toByteArray()
            } ?: byteArrayOf()
            if (bytes.size > 65536) throw IOException("Gameplay response too large")
            GameplayResponse(status, String(bytes, Charsets.UTF_8))
        } finally { connection.disconnect() }
    }
}
