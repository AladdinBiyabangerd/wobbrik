package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner

/** Application-owned online physics boundary. Never exposes its mutable engine or issues commands. */
class AcceptedEngineHost {
    data class Snapshot(
        val restore: AcceptedAttemptRestore.Result = AcceptedAttemptRestore.Result.Unavailable,
        val foreground: Boolean = false,
        val previousFrameNanos: Long? = null,
    ) {
        // Durable commands exist; the online simulation/scene input loop is not bound yet.
        val inputEnabled: Boolean get() = false
    }

    private var engine: PracticeEngine? = null
    private var current = Snapshot()
    @Synchronized fun snapshot(): Snapshot = current

    /** Synchronous with recovery transitions, including same-UID auth epochs. */
    @Synchronized fun onRecoveryChanged(state: AuthenticatedRecoveryOwner.State) {
        engine = null
        current = current.copy(restore = AcceptedAttemptRestore.Result.Unavailable, previousFrameNanos = null)
        val restored = AcceptedAttemptRestore.from(state)
        if (restored is AcceptedAttemptRestore.Result.Paused) {
            engine = PracticeEngine().also { it.restore(restored.checkpoint); it.pause() }
        }
        current = current.copy(restore = restored)
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
        current = current.copy(previousFrameNanos = null)
    }

    /** Frame callbacks cannot accumulate background time while the online simulation/input boundary is locked. */
    @Synchronized fun onFrame(frameNanos: Long) {
        require(frameNanos >= 0)
        pauseAndReset()
    }

    @Synchronized fun resume(): Boolean = false
    @Synchronized fun drop(): Boolean = false
    // Restart/abandon intentionally have no binding: there is no approved server policy/API.
}
