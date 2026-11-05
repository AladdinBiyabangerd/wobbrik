package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner

/** Application-owned online physics boundary. Owns only accepted online physics; dispatch always occurs outside its monitor. */
class AcceptedEngineHost {
    data class Snapshot(
        val restore: AcceptedAttemptRestore.Result = AcceptedAttemptRestore.Result.Unavailable,
        val foreground: Boolean = false,
        val previousFrameNanos: Long? = null,
        val scene: PracticeState? = null,
        val waiting: Boolean = false,
    ) {
        val inputEnabled: Boolean get() = foreground && !waiting && scene?.paused == false && scene.phase != Phase.FINISHED
    }

    private var engine: PracticeEngine? = null
    private var ready: AuthenticatedRecoveryOwner.State.Ready? = null
    private var commands: AcceptedAttemptCommands? = null

    @Synchronized fun bind(commands: AcceptedAttemptCommands) { this.commands = commands }
    private var current = Snapshot()
    @Synchronized fun snapshot(): Snapshot = current

    /** Synchronous with recovery transitions, including same-UID auth epochs. */
    @Synchronized fun onRecoveryChanged(state: AuthenticatedRecoveryOwner.State) {
        engine = null
        ready = state as? AuthenticatedRecoveryOwner.State.Ready
        current = current.copy(restore = AcceptedAttemptRestore.Result.Unavailable, previousFrameNanos = null, scene = null, waiting = false)
        val restored = AcceptedAttemptRestore.from(state)
        if (restored is AcceptedAttemptRestore.Result.Paused) {
            engine = PracticeEngine().also { it.restore(restored.checkpoint); it.pause() }
        }
        current = current.copy(restore = restored, scene = engine?.state)
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
                    return@synchronized Triple(ready ?: return, live.checkpoint(), commands ?: return)
                }
            }
            current = current.copy(scene = live.state)
            null
        }
        dispatch?.let { (expected, proposal, sender) ->
            val accepted = expected.snapshot.attempt ?: return
            if (proposal.state.floors.size.toLong() > accepted.floors) sender.checkpoint(expected, proposal)
            else sender.finish(expected, proposal, DEFAULT_PARTS)
        }
    }

    @Synchronized fun resume(expected: AuthenticatedRecoveryOwner.State.Ready? = null): Boolean {
        if (expected == null || expected !== ready || commands == null || !current.foreground || current.waiting) return false
        val restored = current.restore as? AcceptedAttemptRestore.Result.Paused ?: return false
        if (restored.terminal) return false
        val live = engine ?: return false
        live.resume()
        current = current.copy(scene = live.state, previousFrameNanos = null)
        return true
    }

    @Synchronized fun drop(expected: AuthenticatedRecoveryOwner.State.Ready? = null): Boolean {
        if (expected == null || expected !== ready || !current.inputEnabled) return false
        return (engine?.drop() == true).also { current = current.copy(scene = engine?.state) }
    }

    /** Called after Ready publication, never from the synchronous invalidation callback.
     * FINISHED in the accepted checkpoint is the durable finish intent, including
     * process death between checkpoint and finish. Base parts are always free.
     */
    fun reconcileTerminal(): Boolean {
        val dispatch = synchronized(this) {
            val restored = current.restore as? AcceptedAttemptRestore.Result.Paused ?: return false
            if (!restored.terminal || current.waiting) return false
            val expected = ready ?: return false
            val sender = commands ?: return false
            current = current.copy(waiting = true)
            Triple(expected, restored.checkpoint, sender)
        }
        val sent = dispatch.third.finish(dispatch.first, dispatch.second, DEFAULT_PARTS)
        if (!sent) synchronized(this) {
            if (ready === dispatch.first) current = current.copy(waiting = false)
        }
        return sent
    }

    companion object {
        private val DEFAULT_PARTS = setOf("wall", "window", "entrance", "roof", "lighting")
            .associateWith { "base_6_$it" }
    }
    // Restart/abandon intentionally have no binding: there is no approved server policy/API.
}
