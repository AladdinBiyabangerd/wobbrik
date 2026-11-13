package com.aladdin.wobbrik.engine

import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.game.FinishCosmetics
import appbackend.wobbrik.game.ReadOnlyAccount
import java.util.UUID

/** Local, unaccepted attempt. Never sends commands or grants wallet/ownership/Ready. */
class LocalGameHost(private val results: PendingGameResults, private val defaults: CosmeticDefaultsStore? = null,
    private val id: () -> String = { UUID.randomUUID().toString() }) {
    private data class Attempt(val uid: String, val id: String, val engine: PracticeEngine, val parts: Map<String, String>)
    private var session: BillingSession? = null
    private var attempt: Attempt? = null
    // A terminal result whose disk write failed must survive auth/lifecycle transitions in this process.
    private val unsaved = mutableMapOf<String, Attempt>()
    private var foreground = false
    private var previous: Long? = null
    private var storageError = false
    @Synchronized fun bind(session: BillingSession?) {
        if (this.session == session) return
        pause()
        attempt?.takeIf { it.engine.state.phase == Phase.FINISHED }?.let { unsaved[it.uid] = it }
        attempt = null
        this.session = session
        storageError = false
    }
    @Synchronized fun start(expected: BillingSession, account: ReadOnlyAccount): Boolean {
        if (session != expected || account.uid != expected.uid) return false
        if (attempt != null) return attempt?.uid == expected.uid && attempt?.engine?.state?.phase == Phase.FINISHED
        val waiting = unsaved[expected.uid]
        if (waiting != null) { attempt = waiting; storageError = true; return true }
        val parts = FinishCosmetics.selection(account.catalog, account.snapshot, defaults?.read(expected.uid).orEmpty())
        if (!FinishCosmetics.valid(account.catalog, account.snapshot, parts)) return false
        // Detect corrupt/full-unreadable queue before starting; never evict earlier results.
        results.pending(expected.uid)
        attempt = Attempt(expected.uid, id(), PracticeEngine().also { it.pause() }, parts.toMap())
        previous = null; storageError = false
        return true
    }
    @Synchronized fun snapshot(): AcceptedEngineHost.Snapshot = AcceptedEngineHost.Snapshot(
        foreground = foreground, scene = attempt?.engine?.state, cameraX = attempt?.engine?.cameraX ?: 0.0,
        playing = attempt != null, finishParts = attempt?.parts.orEmpty(), finishStorageError = storageError,
        choosingFinish = attempt?.engine?.state?.phase == Phase.FINISHED)
    @Synchronized fun foreground(value: Boolean) { pause(); foreground = value }
    @Synchronized fun pause() { attempt?.engine?.pause(); previous = null }
    @Synchronized fun resume(): Boolean {
        val a = attempt ?: return false
        if (!foreground || storageError || a.engine.state.phase == Phase.FINISHED || session?.uid != a.uid) return false
        a.engine.resume(); previous = null
        return true
    }
    @Synchronized fun drop(): Boolean = snapshot().inputEnabled && attempt?.engine?.drop() == true
    @Synchronized fun frame(now: Long) {
        require(now >= 0)
        if (!snapshot().inputEnabled) return
        val a = attempt ?: return
        val last = previous
        if (last != null && now < last) { pause(); return }
        previous = now
        var remaining = if (last == null) 0L else (now - last).coerceAtMost(133_333_334L)
        while (remaining > 0 && a.engine.state.phase != Phase.FINISHED) {
            val step = remaining.coerceAtMost(16_666_666L)
            a.engine.advance(step); remaining -= step
        }
        if (a.engine.state.phase == Phase.FINISHED) { pause(); persist() }
    }
    @Synchronized fun retryTerminal(): Boolean = persist()
    private fun persist(): Boolean {
        val a = attempt ?: return false
        if (session?.uid != a.uid || a.engine.state.phase != Phase.FINISHED) return false
        a.engine.pause()
        return try {
            results.enqueue(a.uid, PendingGameResult(a.id, a.engine.checkpoint(), a.parts))
            unsaved.remove(a.uid); attempt = null; previous = null; storageError = false
            true
        } catch (_: Exception) {
            unsaved[a.uid] = a; storageError = true
            false
        }
    }
    /** Existing leave policy: unfinished local play is discarded; terminal intent is retained. */
    @Synchronized fun leave() {
        pause()
        if (attempt?.engine?.state?.phase == Phase.FINISHED) persist() else attempt = null
    }
}
