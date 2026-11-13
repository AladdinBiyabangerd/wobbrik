package com.aladdin.wobbrik

import appbackend.wobbrik.billing.BillingSession
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

class AccountViews(private val backend: GameplayHttpBackend, private val cache: ReadOnlyGameCache) {
    suspend fun <T> read(view: AccountView, session: BillingSession, key: String, onCached: () -> Unit = {}, block: suspend (GameplayReads) -> T): T {
        if (view.ready == null) { onCached(); return cache.replay(session, view.account, key, block) }
        return try { cache.capture(session, view.account, key, backend, block) }
        catch (failure: java.io.IOException) {
            // Authentication/server rejections must never be disguised as offline data.
            if (failure is GameplayHttpError && failure.status !in 500..599 && failure.status !in setOf(408, 429)) throw failure
            val value = cache.replay(session, view.account, key, block)
            onCached()
            value
        }
    }
}
