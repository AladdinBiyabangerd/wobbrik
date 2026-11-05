package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner.State
import appbackend.wobbrik.game.GameSnapshot
import appbackend.wobbrik.game.ServerAttempt
import org.junit.Assert.*
import org.junit.Test

class AcceptedEngineHostTest {
    private fun ready(attempt: ServerAttempt? = ServerAttempt("accepted", true, 0, null)) =
        State.Ready(GameSnapshot("wobbrik-v1", 0, 0, 0, attempt))

    @Test fun recoveryIsPausedAndAllLifecycleFramesAndInputStayLocked() {
        val host = AcceptedEngineHost()
        host.onRecoveryChanged(ready())
        val restored = host.snapshot().restore as AcceptedAttemptRestore.Result.Paused
        assertTrue(restored.checkpoint.state.paused)
        assertEquals(0L, restored.checkpoint.tickUnits)
        repeat(3) {
            host.onForeground()
            host.onFrame(1)
            host.onPause()
            host.onFrame(Long.MAX_VALUE)
            host.onForeground()
            assertFalse(host.resume())
            assertFalse(host.drop())
            assertFalse(host.snapshot().inputEnabled)
            assertNull(host.snapshot().previousFrameNanos)
            assertEquals(restored, host.snapshot().restore)
        }
    }

    @Test fun everyUnavailableTransitionClearsAcceptedAccountImmediately() {
        val host = AcceptedEngineHost()
        listOf(State.Recovering, State.SignedOut, State.Error(Exception("offline"))).forEach {
            host.onRecoveryChanged(ready())
            host.onRecoveryChanged(it)
            assertEquals(AcceptedAttemptRestore.Result.Unavailable, host.snapshot().restore)
            assertFalse(host.drop())
        }
        host.onRecoveryChanged(ready(ServerAttempt("other-account", true, 0, null)))
        assertEquals("other-account", (host.snapshot().restore as AcceptedAttemptRestore.Result.Paused).attemptId)
    }

    @Test fun absentFinishedAndBlockedAttemptsNeverRetainOldPhysics() {
        val host = AcceptedEngineHost()
        val cases = listOf(
            ready(null) to AcceptedAttemptRestore.Result.NoAttempt,
            ready(ServerAttempt("done", false, 0, null)) to AcceptedAttemptRestore.Result.Finished("done"),
            ready(ServerAttempt("continued", true, 0, null, continued = true)) to
                AcceptedAttemptRestore.Result.Blocked(AcceptedAttemptRestore.Reason.CONTINUATION_UNRESOLVED),
        )
        cases.forEach { (state, expected) ->
            host.onRecoveryChanged(ready())
            host.onRecoveryChanged(state)
            assertEquals(expected, host.snapshot().restore)
            assertFalse(host.resume())
        }
    }
}
