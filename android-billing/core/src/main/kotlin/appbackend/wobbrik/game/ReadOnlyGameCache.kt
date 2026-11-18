package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*
import java.security.MessageDigest
import kotlinx.coroutines.ensureActive

/** Accepted account data for presentation/local play only. Deliberately carries no Ready capability. */
data class ReadOnlyAccount(val uid: String, val snapshot: GameSnapshot, val catalog: RecoveryCatalog)

/** Atomic, account-scoped cache. A view is a complete captured read, never a union of live and cached pages. */
class ReadOnlyGameCache(private val files: (String) -> JournalFile) {
    private class FileLock(var users: Int = 0)
    private val locks = mutableMapOf<String, FileLock>()

    // DurableJournalFile.read resolves .new/.bak leftovers, so readers and writers of
    // the SAME file must serialize. An unrelated account/view fsync must not gate it.
    // Retain locks only while in use, including callers waiting to enter the monitor.
    private fun <T> withFile(uid: String, view: String, block: (JournalFile) -> T): T {
        require(uid.isNotBlank())
        val key = MessageDigest.getInstance("SHA-256").digest("${uid.length}:$uid:$view".toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val lock = synchronized(locks) { locks.getOrPut(key) { FileLock() }.also { it.users++ } }
        try { return synchronized(lock) { block(files(key)) } }
        finally { synchronized(locks) { if (--lock.users == 0) locks.remove(key) } }
    }
    fun accepted(account: ReadOnlyAccount) {
        val snapshot = account.snapshot.wire ?: return
        val catalog = account.catalog.wire ?: return
        // Reparse at both boundaries; malformed or unsupported data never becomes an offline model.
        RecoveryDtos.snapshot(snapshot); RecoveryDtos.catalog(catalog)
        withFile(account.uid, "account") { file ->
            val previous = runCatching { account(account.uid) }.getOrNull()
            if (previous != null && previous.snapshot.revision > account.snapshot.revision) return@withFile
            write(file, account.uid, "account", buildJsonObject { put("snapshot", snapshot); put("catalog", catalog) })
        }
    }
    fun account(uid: String): ReadOnlyAccount? {
        val data = withFile(uid, "account") { read(it, uid, "account") } ?: return null
        return ReadOnlyAccount(uid, RecoveryDtos.snapshot(data.getValue("snapshot").jsonPrimitive.content),
            RecoveryDtos.catalog(data.getValue("catalog").jsonPrimitive.content))
    }
    /** Cache persistence is optional; successful live reads must survive a cache disk failure. */
    suspend fun <T> capture(session: BillingSession, account: ReadOnlyAccount, view: String,
        backend: GameplayHttpBackend, onVerified: suspend (T) -> Unit = {},
        block: suspend (GameplayReads) -> T): T {
        require(session.uid == account.uid)
        val bodies = linkedMapOf<String, String>()
        val reader = GameplayReads { expected, path ->
            require(expected == session && allowedReadPath(path))
            backend.read(expected, path).let { body ->
                if (path.startsWith("inventory?")) InventoryReads.displayBody(body) else body
            }.also { body ->
                // Scene detail reads may complete on different IO workers.
                synchronized(bodies) {
                    val previous = bodies.put(path, body)
                    // City server_time is observational; the repository brackets the actual
                    // membership, geometry and revision. Legacy first/final bodies may
                    // differ only by that clock and still be the same captured view.
                    require(previous == null || previous == body ||
                        (path.startsWith("city?") && CitySceneReads.page(previous) == CitySceneReads.page(body))) {
                        "View changed during read"
                    }
                }
            }
        }
        val value = block(reader)
        // Bracket the captured view by the accepted account revision. Never publish a racing bundle.
        val after = backend.sync(session)
        require(after.revision == account.snapshot.revision) { "Account changed; refresh required" }
        // The complete view has passed the server revision bracket. Presentation
        // may proceed while optional persistence finishes; no callback is made for
        // a racing/failed read. Callback failures/cancellation must not be swallowed
        // by the optional disk-failure handler below.
        onVerified(value)
        kotlinx.coroutines.currentCoroutineContext().ensureActive()
        try {
            withFile(session.uid, view) { file ->
                write(file, session.uid, view, buildJsonObject {
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
        val data = withFile(session.uid, view) { requireNotNull(read(it, session.uid, view)) { "View not cached" } }
        require(data.getValue("revision").jsonPrimitive.long == account.snapshot.revision) { "Cached view is stale" }
        val bodies = data.getValue("bodies").jsonObject
        return block(GameplayReads { expected, path ->
            require(expected == session && allowedReadPath(path))
            // The additive market representation did not exist in older complete bundles.
            // Replay the identical legacy query, then let the repository use its captured
            // detail bodies. Never alias filters, scope, cursor or account revisions.
            val body = bodies[path] ?: if (path.endsWith("&include=previews") && MarketReads.allowedPath(path))
                bodies[path.removeSuffix("&include=previews")]
            else if (path.endsWith("&view=display") && allowedReadPath(path))
                bodies[path.removeSuffix("&view=display")]
            else if (path.endsWith("&include=details") && allowedReadPath(path))
                bodies[path.removeSuffix("&include=details")] else null
            requireNotNull(body) { "View path not cached" }.jsonPrimitive.content
        })
    }
    private fun read(file: JournalFile, uid: String, view: String): JsonObject? {
        val bytes = file.read() ?: return null
        require(bytes.size <= DurableJournalFile.MaxBytes)
        val root = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
        require(root.getValue("version").jsonPrimitive.int == 1 && root.getValue("uid").jsonPrimitive.content == uid &&
            root.getValue("view").jsonPrimitive.content == view)
        return root.getValue("data").jsonObject
    }
    private fun write(file: JournalFile, uid: String, view: String, data: JsonObject) {
        val body = buildJsonObject { put("version", 1); put("uid", uid); put("view", view); put("data", data) }
            .toString().toByteArray(Charsets.UTF_8)
        require(body.size <= DurableJournalFile.MaxBytes)
        file.replace(body)
    }
}
