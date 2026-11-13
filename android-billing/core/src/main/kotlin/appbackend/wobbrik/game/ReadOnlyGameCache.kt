package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*
import java.security.MessageDigest

/** Accepted account data for presentation/local play only. Deliberately carries no Ready capability. */
data class ReadOnlyAccount(val uid: String, val snapshot: GameSnapshot, val catalog: RecoveryCatalog)

/** Atomic, account-scoped cache. A view is a complete captured read, never a union of live and cached pages. */
class ReadOnlyGameCache(private val files: (String) -> JournalFile) {
    private fun file(uid: String, view: String): JournalFile {
        require(uid.isNotBlank())
        val key = MessageDigest.getInstance("SHA-256").digest("${uid.length}:$uid:$view".toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return files(key)
    }
    @Synchronized fun accepted(account: ReadOnlyAccount) {
        val snapshot = account.snapshot.wire ?: return
        val catalog = account.catalog.wire ?: return
        // Reparse at both boundaries; malformed or unsupported data never becomes an offline model.
        RecoveryDtos.snapshot(snapshot); RecoveryDtos.catalog(catalog)
        val previous = runCatching { account(account.uid) }.getOrNull()
        if (previous != null && previous.snapshot.revision > account.snapshot.revision) return
        write(account.uid, "account", buildJsonObject { put("snapshot", snapshot); put("catalog", catalog) })
    }
    @Synchronized fun account(uid: String): ReadOnlyAccount? {
        val data = read(uid, "account") ?: return null
        return ReadOnlyAccount(uid, RecoveryDtos.snapshot(data.getValue("snapshot").jsonPrimitive.content),
            RecoveryDtos.catalog(data.getValue("catalog").jsonPrimitive.content))
    }
    /** Cache persistence is optional; successful live reads must survive a cache disk failure. */
    suspend fun <T> capture(session: BillingSession, account: ReadOnlyAccount, view: String,
        backend: GameplayHttpBackend, block: suspend (GameplayReads) -> T): T {
        require(session.uid == account.uid)
        val bodies = linkedMapOf<String, String>()
        val reader = GameplayReads { expected, path ->
            require(expected == session && allowedReadPath(path))
            backend.read(expected, path).also { body ->
                val previous = bodies.put(path, body)
                require(previous == null || previous == body) { "View changed during read" }
            }
        }
        val value = block(reader)
        // Bracket the captured view by the accepted account revision. Never publish a racing bundle.
        val after = backend.sync(session)
        require(after.revision == account.snapshot.revision) { "Account changed; refresh required" }
        try {
            synchronized(this) {
                write(session.uid, view, buildJsonObject {
                    put("revision", after.revision)
                    put("bodies", buildJsonObject { bodies.forEach { (path, body) -> put(path, body) } })
                })
            }
        } catch (_: Exception) { /* Read cache cannot revoke a successful server view. */ }
        return value
    }
    suspend fun <T> replay(session: BillingSession, account: ReadOnlyAccount, view: String,
        block: suspend (GameplayReads) -> T): T {
        require(session.uid == account.uid)
        val data = synchronized(this) { requireNotNull(read(session.uid, view)) { "View not cached" } }
        require(data.getValue("revision").jsonPrimitive.long == account.snapshot.revision) { "Cached view is stale" }
        val bodies = data.getValue("bodies").jsonObject
        return block(GameplayReads { expected, path ->
            require(expected == session && allowedReadPath(path))
            bodies.getValue(path).jsonPrimitive.content
        })
    }
    private fun read(uid: String, view: String): JsonObject? {
        val bytes = file(uid, view).read() ?: return null
        require(bytes.size <= DurableJournalFile.MaxBytes)
        val root = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
        require(root.getValue("version").jsonPrimitive.int == 1 && root.getValue("uid").jsonPrimitive.content == uid &&
            root.getValue("view").jsonPrimitive.content == view)
        return root.getValue("data").jsonObject
    }
    private fun write(uid: String, view: String, data: JsonObject) {
        val body = buildJsonObject { put("version", 1); put("uid", uid); put("view", view); put("data", data) }
            .toString().toByteArray(Charsets.UTF_8)
        require(body.size <= DurableJournalFile.MaxBytes)
        file(uid, view).replace(body)
    }
}
