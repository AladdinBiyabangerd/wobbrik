package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.billing.Sessions
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Wire bodies are immutable and retained verbatim, including operation ID and revision. */
data class AttemptCommand(val operationId: String, val expectedRevision: Long, val route: Route, val envelope: String) {
    enum class Route { START, CHECKPOINT, FINISH, ABANDON, SHOP_BUY, BUILDING_EDIT, BUILDING_DELETE }
}
data class ServerAttempt(val id: String, val active: Boolean, val sequence: Long, val checkpointJson: String?, val floors: Long = 0, val precise: Long = 0, val rescues: Long = 0, val continued: Boolean = false, val rulesVersion: String = "wobbrik-v1", val engineVersion: String = "wobbrik-structural-v1", val geometryVersion: String? = null, val acceptedGeometry: AcceptedCityGeometry? = null, val abandoned: Boolean = false)
data class GameSnapshot(val rulesVersion: String, val revision: Long, val coins: Long, val xp: Long, val attempt: ServerAttempt?, val account: AccountRecovery? = null) {
    /** Exact validated `/sync` body this snapshot was parsed from; not part of equality or copies. */
    var wire: String? = null
        internal set
}

interface AttemptBackend {
    suspend fun catalog(session: BillingSession): RecoveryCatalog? = null
    suspend fun attempt(session: BillingSession, id: String): GameSnapshot? = null
    suspend fun receipt(session: BillingSession, id: String, route: AttemptCommand.Route): AttemptReceipt? = null
    suspend fun sync(session: BillingSession): GameSnapshot
    /** Return only after a committed receipt; HTTP/network errors must throw. */
    suspend fun submit(session: BillingSession, command: AttemptCommand)
}

interface AttemptJournal {
    fun pending(uid: String): AttemptCommand?
    /** Durable, atomic, account-scoped write before network I/O; never stores credentials. */
    fun save(uid: String, command: AttemptCommand)
    fun remove(uid: String, command: AttemptCommand)
}

/** A documented rejection of a parts mutation; never an ambiguous network outcome. */
class MutationRejected(val code: String) : IllegalStateException(code)

/** Only a definitive revision_conflict; never an automatic rebase. */
class RevisionConflict : IllegalStateException("Sync required before a new operation")

/** Serializes this host's mutations; server revisions handle other devices. No local grants. */
class AttemptCoordinator(
    private val sessions: Sessions,
    private val backend: AttemptBackend,
    private val journal: AttemptJournal,
    // Persist domain intent acknowledgement before the sole wire journal can be cleared.
    private val onCommitted: (BillingSession, AttemptCommand) -> Unit = { _, _ -> },
) {
    private val mutex = Mutex()
    @Volatile private var accepted: Pair<BillingSession, GameSnapshot>? = null

    fun snapshot(): GameSnapshot? {
        val value = accepted ?: return null
        return value.second.takeIf { sessions.current() == value.first }
    }

    /** False after failure/account switch and until any pending command has been recovered. */
    fun ready(): Boolean {
        val session = sessions.current() ?: return false
        return accepted?.first == session && journal.pending(session.uid) == null && sessions.current() == session
    }

    suspend fun recover(expectedSession: BillingSession = current()): GameSnapshot = mutex.withLock {
        val session = current()
        if (session != expectedSession) throw AccountChanged()
        accepted = null
        journal.pending(session.uid)?.let { drain(session, it) } ?: refresh(session)
    }

    suspend fun execute(command: AttemptCommand, expectedSession: BillingSession = current()): GameSnapshot = mutex.withLock {
        val session = current()
        if (session != expectedSession) throw AccountChanged()
        val state = accepted?.takeIf { it.first == session }?.second
        check(state != null && journal.pending(session.uid) == null) { "Recover before issuing a command" }
        require(command.operationId.isNotBlank() && command.envelope.isNotBlank())
        require(command.expectedRevision == state.revision) { "Stale command" }
        stillCurrent(session)
        accepted = null // Block input even if durable persistence fails.
        journal.save(session.uid, command)
        stillCurrent(session)
        drain(session, command)
    }

    /** Historical reads never become the current account state or reopen an attempt. */
    suspend fun reconcileFinished(
        session: BillingSession, attemptId: String, operationId: String, floors: Long,
        onConfirmed: () -> Unit,
    ): GameSnapshot? = mutex.withLock {
        stillCurrent(session)
        check(accepted?.first == session && journal.pending(session.uid) == null) { "Recover before reconciliation" }
        val historical = backend.attempt(session, attemptId)
        stillCurrent(session)
        val attempt = historical?.attempt ?: return@withLock null
        require(attempt.id == attemptId)
        if (attempt.active || attempt.abandoned) return@withLock null
        val receipt = backend.receipt(session, operationId, AttemptCommand.Route.FINISH)
        stillCurrent(session)
        if (receipt == null) return@withLock null
        val finish = receipt.result as? AttemptResult.Finished ?: error("Not a finish receipt")
        require(finish.attemptId == attemptId && finish.floors == floors && attempt.floors == floors)
        accepted = null
        val current = backend.sync(session)
        stillCurrent(session)
        require(current.rulesVersion == "wobbrik-v1" && current.revision >= historical.revision && current.revision >= receipt.revision)
        onConfirmed() // Durable acknowledgement before publication; disk failure retains the intent.
        stillCurrent(session)
        accepted = session to current
        current
    }

    private suspend fun drain(session: BillingSession, command: AttemptCommand): GameSnapshot {
        stillCurrent(session)
        try {
            backend.submit(session, command)
        } catch (rejected: MutationRejected) {
            stillCurrent(session)
            journal.remove(session.uid, command)
            refresh(session)
            throw rejected
        } catch (conflict: RevisionConflict) {
            stillCurrent(session)
            journal.remove(session.uid, command)
            refresh(session)
            throw conflict // Caller must make a new decision; never silently rebase a mutation.
        }
        stillCurrent(session)
        onCommitted(session, command)
        stillCurrent(session)
        // A receipt may be historical. Always sync, including at an equal revision (billing).
        val result = backend.sync(session)
        stillCurrent(session)
        check(result.rulesVersion == "wobbrik-v1") { "Unsupported server rules" }
        journal.remove(session.uid, command)
        stillCurrent(session)
        accepted = session to result
        return result
    }

    private suspend fun refresh(session: BillingSession): GameSnapshot {
        val result = backend.sync(session)
        stillCurrent(session)
        check(result.rulesVersion == "wobbrik-v1") { "Unsupported server rules" }
        accepted = session to result
        return result
    }

    private fun current() = sessions.current() ?: error("Authenticated gameplay session required")
    private suspend fun stillCurrent(session: BillingSession) {
        currentCoroutineContext().ensureActive()
        if (sessions.current() != session) throw AccountChanged()
    }
}
