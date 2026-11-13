package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.billing.BillingSession
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One application-owned coordinator/journal for recovery and all attempt mutations. */
class AuthenticatedRecoveryOwner(
    private val sessions: AccountSessions,
    private val backend: AttemptBackend,
    journal: AttemptJournal,
    private val scope: CoroutineScope,
    // Called under the owner lock before publication; must be synchronous and non-reentrant.
    private val onStateChanged: (State) -> Unit = {},
    // Display-only; never consulted for Ready, submit or the journal.
    private val cache: SnapshotCache? = null,
    private val readCache: ReadOnlyGameCache? = null,
    onCommitted: (BillingSession, AttemptCommand) -> Unit = { _, _ -> },
    private val reconcile: suspend (BillingSession, AttemptCoordinator, GameSnapshot) -> GameSnapshot = { _, _, snapshot -> snapshot },
) {
    sealed interface State {
        data object SignedOut : State
        data object Recovering : State
        data class Ready(val snapshot: GameSnapshot, val catalog: RecoveryCatalog? = null, val accountKey: String? = null) : State
        data class Error(val cause: Exception) : State
    }

    private val coordinator = AttemptCoordinator(sessions, backend, journal, onCommitted)
    private val lock = Any()
    private val mutableState = MutableStateFlow<State>(State.SignedOut)
    val state: StateFlow<State> = mutableState.asStateFlow()
    private val mutableCached = MutableStateFlow<GameSnapshot?>(null)
    /** Last accepted snapshot of the current account for display while not Ready; null otherwise unknown. */
    val cached: StateFlow<GameSnapshot?> = mutableCached.asStateFlow()
    private val mutableReadOnly = MutableStateFlow<ReadOnlyAccount?>(null)
    val readOnly: StateFlow<ReadOnlyAccount?> = mutableReadOnly.asStateFlow()
    private var work: Job? = null
    private var generation = 0L

    /** The sole auth-listener entry point, including same-UID re-login. Never deletes journals. */
    fun onAuthChanged(uid: String?) = synchronized(lock) {
        generation++
        work?.cancel()
        work = null
        sessions.onAuthChanged(uid)
        mutableCached.value = null
        mutableReadOnly.value = null
        publishLocked(if (uid == null) State.SignedOut else State.Recovering)
        if (uid != null) {
            loadCachedLocked(uid)
            recoverLocked()
        }
    }

    private fun loadCachedLocked(uid: String) {
        if (cache == null && readCache == null) return
        val epoch = generation
        scope.launch {
            val value = try { cache?.lastKnown(uid) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { null }
            val account = try { readCache?.account(uid) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { null }
            synchronized(lock) {
                if (epoch == generation && sessions.current()?.uid == uid) {
                    if (mutableCached.value == null) mutableCached.value = value ?: account?.snapshot
                    if (mutableReadOnly.value == null) mutableReadOnly.value = account
                }
            }
        }
    }

    /**
     * Foreground is a lifecycle event, not a reason to destroy locally accumulated physics.
     * An accepted active attempt needs no network until its next mutation. Auth changes
     * still revoke it synchronously; pending/error recovery is never skipped.
     */
    fun onForeground() = synchronized(lock) {
        val ready = mutableState.value as? State.Ready
        if (ready?.snapshot?.attempt?.active == true && coordinator.ready()) return@synchronized
        if (sessions.current() != null && work?.isCompleted != false) recoverLocked()
    }

    private fun publishLocked(state: State) {
        onStateChanged(state)
        mutableState.value = state
    }

    /**
     * Accept only the exact published Ready instance, not an equal snapshot from another
     * account/login/recovery. Build once before invalidation; retries use the journal.
     * True means dispatched, never committed. Invalid proposals throw before dispatch.
     */
    fun submit(expected: State.Ready, build: (GameSnapshot) -> AttemptCommand): Boolean = synchronized(lock) {
        if (mutableState.value !== expected || work?.isCompleted == false || !coordinator.ready()) return false
        val command = build(expected.snapshot)
        GameplayWire.validate(command)
        require(command.expectedRevision == expected.snapshot.revision)
        launchLocked { session -> coordinator.execute(command, session) }
        true
    }

    private fun recoverLocked() = launchLocked { session ->
        reconcile(session, coordinator, coordinator.recover(session))
    }

    private fun launchLocked(action: suspend (BillingSession) -> GameSnapshot) {
        val session = sessions.current() ?: return
        val epoch = generation
        publishLocked(State.Recovering)
        work = scope.launch(start = CoroutineStart.LAZY) {
            try {
                val snapshot = action(session)
                val catalog = backend.catalog(session)
                ensureActive()
                val published = synchronized(lock) {
                    if (epoch == generation && sessions.current() == session && coordinator.ready()) {
                        work = null // Ready observers may dispatch the next durable command.
                        mutableCached.value = snapshot
                        mutableReadOnly.value = catalog?.let { ReadOnlyAccount(session.uid, snapshot, it) }
                        publishLocked(State.Ready(snapshot, catalog, session.uid))
                        true
                    } else false
                }
                // Cache failures never affect gameplay; only accepted, published snapshots are written.
                if (published && catalog != null) try { readCache?.accepted(ReadOnlyAccount(session.uid, snapshot, catalog)) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { }
                if (published) try { cache?.accepted(session.uid, snapshot) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                synchronized(lock) {
                    if (epoch == generation && sessions.current() == session) {
                        publishLocked(State.Error(failure))
                    }
                }
            }
        }.also { it.start() }
    }
}
