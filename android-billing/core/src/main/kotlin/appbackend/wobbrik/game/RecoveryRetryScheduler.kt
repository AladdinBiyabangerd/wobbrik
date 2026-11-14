package appbackend.wobbrik.game

import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Process-owned wakeups only. The recovery owner remains the sole command/journal writer. */
class RecoveryRetryScheduler(
    private val owner: AuthenticatedRecoveryOwner,
    scope: CoroutineScope,
    private val wait: suspend (Long) -> Unit = { delay(it) },
) {
    private val foreground = MutableStateFlow(false)
    private val connected = MutableStateFlow(false)
    private val job = scope.launch {
        var retries = 0
        combine(owner.state, foreground, connected) { state, visible, online -> Triple(state, visible, online) }
            .collectLatest { (state, visible, online) ->
                if (state is AuthenticatedRecoveryOwner.State.Ready || state is AuthenticatedRecoveryOwner.State.SignedOut) retries = 0
                if (state !is AuthenticatedRecoveryOwner.State.Error || !visible || !online || !retryable(state.cause)) return@collectLatest
                // Repeated capability callbacks cannot bypass backoff or replace an in-flight recovery.
                wait(minOf(60_000L, 1_000L shl retries.coerceAtMost(6)))
                currentCoroutineContext().ensureActive()
                if (foreground.value && connected.value && owner.retryTransient(state)) retries = (retries + 1).coerceAtMost(6)
            }
    }

    fun foreground(value: Boolean) { foreground.value = value }
    fun connected(value: Boolean) { connected.value = value }
    fun close() { job.cancel() }

    companion object {
        internal fun retryable(failure: Exception): Boolean = when (failure) {
            is GameplayHttpError -> failure.status in setOf(408, 429) || failure.status in 500..599
            is IOException, is RevisionConflict -> true
            else -> false
        }
    }
}
