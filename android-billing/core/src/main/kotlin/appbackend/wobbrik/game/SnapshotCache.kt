package appbackend.wobbrik.game

/** One stored row: the exact accepted server `/sync` body for one app + uid. */
data class CachedSnapshotRow(val appId: String, val uid: String, val rulesVersion: String, val revision: Long, val body: String)

/** Persistence port; the Room implementation lives in the android module. */
interface SnapshotCacheStore {
    suspend fun read(appId: String, uid: String): CachedSnapshotRow?
    suspend fun replace(row: CachedSnapshotRow)
    suspend fun delete(appId: String, uid: String)
}

/**
 * Display-only cache of accepted server snapshots (decision B, 9 November 2026). It is never a
 * command source, wallet authority, Ready capability or second journal; the durable file journal
 * stays the only outbox. Rows are re-validated with the wire parser on every read.
 */
class SnapshotCache(private val store: SnapshotCacheStore, private val appId: String = APP_ID) {
    /** Call only with a snapshot the coordinator accepted for [uid]. */
    suspend fun accepted(uid: String, snapshot: GameSnapshot) {
        require(uid.isNotBlank())
        val body = snapshot.wire ?: return
        if (snapshot.rulesVersion != RULES_VERSION) return
        val current = store.read(appId, uid)
        if (current != null && current.rulesVersion == RULES_VERSION && current.revision > snapshot.revision) return
        // Same revision with a different body is accepted: billing changes balances without a revision bump.
        if (current != null && current.revision == snapshot.revision && current.body == body) return
        store.replace(CachedSnapshotRow(appId, uid, snapshot.rulesVersion, snapshot.revision, body))
    }

    /** Last accepted snapshot for exactly [uid], or null; unusable rows are dropped. */
    suspend fun lastKnown(uid: String): GameSnapshot? {
        require(uid.isNotBlank())
        val row = store.read(appId, uid) ?: return null
        val parsed = if (row.appId == appId && row.uid == uid && row.rulesVersion == RULES_VERSION) {
            try { RecoveryDtos.snapshot(row.body).takeIf { it.revision == row.revision } } catch (_: IllegalArgumentException) { null }
                catch (_: IllegalStateException) { null } catch (_: NoSuchElementException) { null }
                catch (_: kotlinx.serialization.SerializationException) { null }
        } else null
        if (parsed == null) store.delete(appId, uid)
        return parsed
    }

    companion object {
        const val APP_ID = "wobbrik"
        const val RULES_VERSION = "wobbrik-v1"
    }
}
