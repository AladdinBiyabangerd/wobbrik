package com.aladdin.wobbrik

import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.billing.Sessions
import appbackend.wobbrik.billing.AccountChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import appbackend.wobbrik.game.*

/** The screen may hold either an exact online capability or display-only accepted data. */
data class AccountView(val account: ReadOnlyAccount, val ready: AuthenticatedRecoveryOwner.State.Ready?) {
    val snapshot get() = account.snapshot
    val catalog get() = account.catalog
    val accountKey get() = account.uid
    fun current(configured: WobbrikApplication.Bootstrap.Configured, session: BillingSession): Boolean =
        configured.sessions.current() == session && session.uid == accountKey &&
            if (ready != null) configured.recovery.state.value === ready
            else configured.recovery.readOnly.value === account && configured.recovery.state.value !is AuthenticatedRecoveryOwner.State.Ready
}

/** Reuse only reads completed in this exact login/capability epoch. Never grants commands. */
class AccountViews(private val backend: GameplayHttpBackend, private val cache: ReadOnlyGameCache,
    private val sessions: Sessions) {
    private var epoch: Any? = null
    private var login: BillingSession? = null
    private val completed = mutableSetOf<String>()

    suspend fun <T> read(view: AccountView, session: BillingSession, key: String, onCached: () -> Unit = {},
        refresh: Boolean = false, onLocal: ((T) -> Unit)? = null, onFresh: () -> Unit = {}, block: suspend (GameplayReads) -> T): T {
        fun current() { if (sessions.current() != session || session.uid != view.accountKey) throw AccountChanged() }
        current()
        val capability = view.ready ?: view.account
        val reuse = synchronized(this) {
            if (epoch !== capability || login != session) {
                epoch = capability; login = session; completed.clear()
            }
            !refresh && key in completed
        }
        // Publish matching accepted data before network work. It remains display-only until
        // the live read succeeds; replay never creates or extends a mutation capability.
        if (view.ready != null && onLocal != null && !reuse) {
            val local = withContext(Dispatchers.IO) {
                try { cache.replay(session, view.account, key, block) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { null }
            }
            current()
            if (local != null) { onCached(); onLocal(local) }
        }
        // Parsing, hashing, file reads and fsync must not run on the Compose/UI dispatcher.
        val result = withContext(Dispatchers.IO) {
            if (view.ready == null) {
                cache.replay(session, view.account, key, block) to true
            } else {
                val reused = if (reuse) try { cache.replay(session, view.account, key, block) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { null } else null
                if (reused != null) reused to false
                else try {
                    synchronized(this@AccountViews) {
                        if (epoch === capability && login == session) completed.remove(key)
                    }
                    cache.capture(session, view.account, key, backend, block).also {
                        synchronized(this@AccountViews) {
                            if (epoch === capability && login == session) completed.add(key)
                        }
                    } to false
                } catch (failure: java.io.IOException) {
                    // Blocking I/O can time out after its caller has already been cancelled.
                    // Preserve cancellation instead of starting an offline replay (or cache miss).
                    currentCoroutineContext().ensureActive()
                    // Authentication/server rejections must never be disguised as offline data.
                    if (failure is GameplayHttpError && failure.status !in 500..599 && failure.status !in setOf(408, 429)) throw failure
                    cache.replay(session, view.account, key, block) to true
                }
            }
        }
        current()
        if (result.second) onCached() else onFresh()
        if (view.ready == null) {
            // The sole replay is already the final display-only result. Publish it without
            // rereading/parsing the bundle, including nullable results and replay failures.
            currentCoroutineContext().ensureActive()
            current()
            onLocal?.invoke(result.first)
            currentCoroutineContext().ensureActive()
            current()
        }
        return result.first
    }
}
