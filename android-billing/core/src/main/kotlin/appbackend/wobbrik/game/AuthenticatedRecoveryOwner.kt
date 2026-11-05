package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Retain once in the application, never in an Activity. No gameplay mutations are exposed. */
class AuthenticatedRecoveryOwner(
    private val sessions: AccountSessions,
    backend: AttemptBackend,
    journal: AttemptJournal,
    private val scope: CoroutineScope,
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
        mutableState.value = if (uid == null) State.SignedOut else State.Recovering
        if (uid != null) recoverLocked()
    }

    /** Coalesces overlapping foreground events; a later event retries after any failure. */
    fun onForeground() = synchronized(lock) {
        if (sessions.current() != null && work?.isCompleted != false) recoverLocked()
    }

    private fun recoverLocked() {
        val session = sessions.current() ?: return
        val epoch = generation
        mutableState.value = State.Recovering
        work = scope.launch(start = CoroutineStart.LAZY) {
            try {
                val snapshot = coordinator.recover()
                ensureActive()
                synchronized(lock) {
                    if (epoch == generation && sessions.current() == session && coordinator.ready()) {
                        mutableState.value = State.Ready(snapshot)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                synchronized(lock) {
                    if (epoch == generation && sessions.current() == session) {
                        mutableState.value = State.Error(failure)
                    }
                }
            }
        }.also { it.start() }
    }
}
