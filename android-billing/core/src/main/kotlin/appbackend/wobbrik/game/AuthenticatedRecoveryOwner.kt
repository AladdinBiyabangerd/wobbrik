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
    backend: AttemptBackend,
    journal: AttemptJournal,
    private val scope: CoroutineScope,
    // Called under the owner lock before publication; must be synchronous and non-reentrant.
    private val onStateChanged: (State) -> Unit = {},
) {
    sealed interface State {
        data object SignedOut : State
        data object Recovering : State
        data class Ready(val snapshot: GameSnapshot) : State
        data class Error(val cause: Exception) : State
    }

    private val coordinator = AttemptCoordinator(sessions, backend, journal)
    private val lock = Any()
    private val mutableState = MutableStateFlow<State>(State.SignedOut)
    val state: StateFlow<State> = mutableState.asStateFlow()
    private var work: Job? = null
    private var generation = 0L

    /** The sole auth-listener entry point, including same-UID re-login. Never deletes journals. */
    fun onAuthChanged(uid: String?) = synchronized(lock) {
        generation++
        work?.cancel()
        work = null
        sessions.onAuthChanged(uid)
        publishLocked(if (uid == null) State.SignedOut else State.Recovering)
        if (uid != null) recoverLocked()
    }

    /** Coalesces overlapping foreground events; a later event retries after any failure. */
    fun onForeground() = synchronized(lock) {
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

    private fun recoverLocked() = launchLocked { session -> coordinator.recover(session) }

    private fun launchLocked(action: suspend (BillingSession) -> GameSnapshot) {
        val session = sessions.current() ?: return
        val epoch = generation
        publishLocked(State.Recovering)
        work = scope.launch(start = CoroutineStart.LAZY) {
            try {
                val snapshot = action(session)
                ensureActive()
                synchronized(lock) {
                    if (epoch == generation && sessions.current() == session && coordinator.ready()) {
                        publishLocked(State.Ready(snapshot))
                    }
                }
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
