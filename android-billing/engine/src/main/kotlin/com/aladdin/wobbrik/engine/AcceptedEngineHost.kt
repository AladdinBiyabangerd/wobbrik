package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.FinishCosmetics

/** Application-owned online physics boundary. Owns only accepted online physics; dispatch always occurs outside its monitor. */
class AcceptedEngineHost(private val drafts: FinishDraftStore? = null) {
    data class Snapshot(
        val restore: AcceptedAttemptRestore.Result = AcceptedAttemptRestore.Result.Unavailable,
        val foreground: Boolean = false,
        val previousFrameNanos: Long? = null,
        val scene: PracticeState? = null,
        val waiting: Boolean = false,
        val cameraX: Double = 0.0,
        val finishParts: Map<String, String> = emptyMap(),
        val choosingFinish: Boolean = false,
        val finishStorageError: Boolean = false,
        /** The player entered this attempt in this process; app-scoped, so it survives rotation but not process death. */
        val playing: Boolean = false,
    ) {
        val inputEnabled: Boolean get() = foreground && !waiting && scene?.paused == false && scene.phase != Phase.FINISHED
    }

    private var engine: PracticeEngine? = null
    private var ready: AuthenticatedRecoveryOwner.State.Ready? = null
    private var commands: AcceptedAttemptCommands? = null
    /** Last accepted open attempt (account key, attempt ID); survives Recovering for a leave. */
    private var lastOpen: Pair<String?, String>? = null
    /** Open attempt the player left; abandoned once a Ready confirms it is still open and not lost. */
    private var leaving: Pair<String?, String>? = null
    private var leaveTriedFor: AuthenticatedRecoveryOwner.State.Ready? = null

    @Synchronized fun bind(commands: AcceptedAttemptCommands) { this.commands = commands }
    private var current = Snapshot()
    @Synchronized fun snapshot(): Snapshot = current.copy(cameraX = engine?.cameraX ?: 0.0)

    /** Synchronous with recovery transitions, including same-UID auth epochs. */
    @Synchronized fun onRecoveryChanged(state: AuthenticatedRecoveryOwner.State) {
        engine = null
        ready = state as? AuthenticatedRecoveryOwner.State.Ready
        current = current.copy(restore = AcceptedAttemptRestore.Result.Unavailable, previousFrameNanos = null, scene = null, waiting = false, finishParts = emptyMap(), choosingFinish = false, finishStorageError = false)
        val restored = AcceptedAttemptRestore.from(state)
        if (restored is AcceptedAttemptRestore.Result.Paused) {
            engine = PracticeEngine().also { it.restore(restored.checkpoint); it.pause() }
        }
        current = current.copy(restore = restored, scene = engine?.state)
        if (restored is AcceptedAttemptRestore.Result.Paused) lastOpen = ready?.accountKey to restored.attemptId
        if (restored is AcceptedAttemptRestore.Result.Paused) {
            val expected = ready ?: return
            val draft = expected.accountKey?.let { drafts?.read(it, restored.attemptId) }
            // A local miss is reusable only against exactly the accepted progress it ended.
            val terminal = draft?.terminal?.takeIf { sameProgress(it, restored.checkpoint) }
                ?: restored.checkpoint.takeIf { restored.terminal }
            if (terminal != null) {
                engine = PracticeEngine().also { it.restore(terminal); it.pause() }
                val parts = expected.catalog?.let { FinishCosmetics.selection(it, expected.snapshot, draft?.parts.orEmpty()) }.orEmpty()
                current = current.copy(scene = engine?.state, choosingFinish = true, finishParts = parts)
            }
        }
    }

    /** Foreground never implicitly resumes an accepted attempt. */
    @Synchronized fun onForeground() {
        pauseAndReset()
        current = current.copy(foreground = true)
    }

    @Synchronized fun onPause() {
        pauseAndReset()
        current = current.copy(foreground = false)
    }

    /** User pause preserves foreground eligibility but always resets the frame clock. */
    @Synchronized fun pause() { pauseAndReset() }

    private fun pauseAndReset() {
        engine?.pause()
        current = current.copy(previousFrameNanos = null, scene = engine?.state)
    }

    /**
     * Stop at the first settled tick: a catch-up frame must never simulate past the
     * checkpoint boundary. The coordinator independently rejects stale Ready identity.
     */
    fun onFrame(frameNanos: Long, expected: AuthenticatedRecoveryOwner.State.Ready? = null) {
        require(frameNanos >= 0)
        val dispatch = synchronized(this) {
            if (expected == null || expected !== ready || !current.inputEnabled) return
            val previous = current.previousFrameNanos
            if (previous != null && frameNanos < previous) {
                pauseAndReset()
                return
            }
            current = current.copy(previousFrameNanos = frameNanos)
            val live = engine ?: return
            // One tick per advance allows us to observe every settled landing.
            var remaining = if (previous == null) 0L else (frameNanos - previous).coerceAtMost(133_333_334L)
            while (remaining > 0) {
                val step = remaining.coerceAtMost(16_666_666L)
                val before = live.state
                live.advance(step)
                remaining -= step
                if (before.phase == Phase.FALLING && live.state.phase != Phase.FALLING) {
                    live.pause()
                    current = current.copy(scene = live.state, waiting = true, previousFrameNanos = null)
                    val expectedReady = ready ?: return
                    val proposal = live.checkpoint()
                    if (proposal.state.phase == Phase.FINISHED) {
                        saveDraft(expectedReady, FinishDraft(proposal))
                        current = current.copy(choosingFinish = true, finishParts = expectedReady.catalog?.let {
                            FinishCosmetics.selection(it, expectedReady.snapshot, emptyMap())
                        }.orEmpty())
                    }
                    return@synchronized Triple(expectedReady, proposal, commands ?: return)
                }
            }
            current = current.copy(scene = live.state)
            null
        }
        dispatch?.let { (expected, proposal, sender) ->
            val accepted = expected.snapshot.attempt ?: return
            if (proposal.state.floors.size.toLong() > accepted.floors) sender.checkpoint(expected, proposal)
            else synchronized(this) {
                if (ready === expected) current = current.copy(waiting = false)
            }
        }
    }

    @Synchronized fun resume(expected: AuthenticatedRecoveryOwner.State.Ready? = null): Boolean {
        if (expected == null || expected !== ready || commands == null || !current.foreground || current.waiting) return false
        val restored = current.restore as? AcceptedAttemptRestore.Result.Paused ?: return false
        if (restored.terminal || current.choosingFinish) return false
        val live = engine ?: return false
        live.resume()
        current = current.copy(scene = live.state, previousFrameNanos = null)
        return true
    }

    @Synchronized fun drop(expected: AuthenticatedRecoveryOwner.State.Ready? = null): Boolean {
        if (expected == null || expected !== ready || !current.inputEnabled) return false
        return (engine?.drop() == true).also { current = current.copy(scene = engine?.state) }
    }

    @Synchronized fun selectPart(expected: AuthenticatedRecoveryOwner.State.Ready, category: String, id: String): Boolean {
        if (ready !== expected || !current.choosingFinish || current.waiting) return false
        val catalog = expected.catalog ?: return false
        val item = catalog.items.firstOrNull { it.id == id && it.category == category } ?: return false
        if (!FinishCosmetics.available(item, expected.snapshot)) return false
        val terminal = engine?.checkpoint() ?: return false
        val parts = current.finishParts + (category to id)
        if (!saveDraft(expected, FinishDraft(terminal, parts))) return false
        current = current.copy(finishParts = parts)
        return true
    }

    fun confirmFinish(expected: AuthenticatedRecoveryOwner.State.Ready): Boolean {
        val dispatch = synchronized(this) {
            if (ready !== expected || !current.choosingFinish || current.waiting) return false
            val catalog = expected.catalog ?: return false
            if (!FinishCosmetics.valid(catalog, expected.snapshot, current.finishParts)) return false
            val sender = commands ?: return false
            val terminal = engine?.checkpoint() ?: return false
            if (!saveDraft(expected, FinishDraft(terminal, current.finishParts))) return false
            current = current.copy(waiting = true)
            Triple(sender, terminal, current.finishParts.toMap())
        }
        val sent = dispatch.first.finish(expected, dispatch.second, dispatch.third)
        if (!sent) synchronized(this) {
            if (ready === expected) current = current.copy(waiting = false)
        }
        return sent
    }

    private fun saveDraft(expected: AuthenticatedRecoveryOwner.State.Ready, draft: FinishDraft): Boolean {
        val uid = expected.accountKey ?: return drafts == null
        val attempt = expected.snapshot.attempt ?: return false
        return try {
            drafts?.write(uid, attempt.id, draft)
            current = current.copy(finishStorageError = false)
            true
        } catch (_: Exception) {
            // Keep terminal physics paused and expose a retryable selection error; never send on failed confirm.
            current = current.copy(finishStorageError = true)
            false
        }
    }

    private fun sameProgress(a: EngineCheckpoint, b: EngineCheckpoint): Boolean =
        a.version == b.version && a.rules == b.rules && a.state.phase == Phase.FINISHED && a.state.floors == b.state.floors &&
            a.state.perfectLandings == b.state.perfectLandings && a.state.balance.rescues == b.state.balance.rescues

    /** The player opened the game page for the current attempt (Play / resume a loss picker). */
    @Synchronized fun enter() {
        leaving = null
        current = current.copy(playing = true)
    }

    /**
     * User policy (9 November 2026): leaving an open attempt mid-game (Home, Back, menu) deletes
     * it. A loss is kept: its cosmetic picker stays resumable and is never abandoned.
     * Returns true when an abandon was scheduled.
     */
    fun leave(expected: AuthenticatedRecoveryOwner.State.Ready? = null): Boolean {
        val scheduled = synchronized(this) {
            pauseAndReset()
            val wasPlaying = current.playing
            current = current.copy(playing = false)
            if (!wasPlaying || current.choosingFinish) return false
            leaving = when (val r = current.restore) {
                is AcceptedAttemptRestore.Result.Paused -> if (r.terminal) null else ready?.accountKey to r.attemptId
                // Recovering (e.g. a checkpoint in flight): decide on the next Ready for the same attempt.
                AcceptedAttemptRestore.Result.Unavailable -> lastOpen
                else -> null
            }
            leaveTriedFor = null
            leaving != null
        }
        if (scheduled) enforceLeave(expected)
        return scheduled
    }

    /**
     * Play while an open attempt from an earlier process (app closed mid-game) is still on the
     * server: abandon it first, never resume it. False when there is nothing to abandon.
     */
    fun discardOpen(expected: AuthenticatedRecoveryOwner.State.Ready): Boolean {
        val scheduled = synchronized(this) {
            if (ready !== expected || current.playing || current.choosingFinish) return false
            val r = current.restore as? AcceptedAttemptRestore.Result.Paused ?: return false
            if (r.terminal) return false
            leaving = expected.accountKey to r.attemptId
            leaveTriedFor = null
            true
        }
        enforceLeave(expected)
        return scheduled
    }

    /** True while a left attempt still awaits its abandon. */
    @Synchronized fun leavePending(): Boolean = leaving != null

    /**
     * Called every frame. Dispatches at most one abandon per Ready capability, outside the monitor,
     * and only for the same account/attempt that was left, still open, not lost and not re-entered.
     */
    fun enforceLeave(expected: AuthenticatedRecoveryOwner.State.Ready?) {
        val sender = synchronized(this) {
            val target = leaving ?: return
            if (expected == null || expected !== ready) return
            val r = current.restore
            if (r !is AcceptedAttemptRestore.Result.Paused || target != (expected.accountKey to r.attemptId) ||
                r.terminal || current.choosingFinish) {
                leaving = null // Abandoned, finished, lost, or another account/attempt: nothing to delete.
                return
            }
            if (current.playing || current.waiting || leaveTriedFor === expected) return
            val sender = commands ?: return
            leaveTriedFor = expected
            engine?.pause()
            current = current.copy(waiting = true, previousFrameNanos = null, scene = engine?.state)
            sender
        }
        // Runs in the frame loop: a refused or invalid dispatch must never crash the app; the next Ready retries.
        if (!runCatching { sender.abandon(expected!!) }.getOrDefault(false)) synchronized(this) {
            if (ready === expected) current = current.copy(waiting = false)
        }
    }
}
