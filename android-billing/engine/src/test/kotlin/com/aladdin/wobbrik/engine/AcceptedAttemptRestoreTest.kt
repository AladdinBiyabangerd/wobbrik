package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.GameSnapshot
import appbackend.wobbrik.game.ServerAttempt
import org.junit.Assert.*
import org.junit.Test

class AcceptedAttemptRestoreTest {
    private fun restore(attempt: ServerAttempt?) = AcceptedAttemptRestore.from(
        AuthenticatedRecoveryOwner.State.Ready(GameSnapshot("wobbrik-v1", 4, 90, 20, attempt)))
    private fun checkpoint(phase: Phase = Phase.SWINGING): EngineCheckpoint = PracticeEngine().checkpoint().let {
        it.copy(tickUnits = 999_999_999, state = it.state.copy(phase = phase,
            floors = listOf(Floor(0.0, 0.0)), perfectLandings = 1))
    }
    private fun attempt(checkpoint: EngineCheckpoint = checkpoint()) = ServerAttempt("attempt", true, 1,
        EngineCheckpointCodec.encode(checkpoint), 1, 1)
    private fun blocked(attempt: ServerAttempt, reason: AcceptedAttemptRestore.Reason = AcceptedAttemptRestore.Reason.INVALID_CHECKPOINT) {
        assertEquals(AcceptedAttemptRestore.Result.Blocked(reason), restore(attempt))
    }

    @Test fun onlySuccessfulRecoveryExposesAttempt() {
        listOf(AuthenticatedRecoveryOwner.State.SignedOut, AuthenticatedRecoveryOwner.State.Recovering,
            AuthenticatedRecoveryOwner.State.Error(IllegalStateException("offline"))).forEach {
            assertEquals(AcceptedAttemptRestore.Result.Unavailable, AcceptedAttemptRestore.from(it))
        }
        assertEquals(AcceptedAttemptRestore.Result.NoAttempt, restore(null))
    }

    @Test fun freshServerAttemptRestoresPausedWithoutCreatingCheckpointCommand() {
        val result = restore(ServerAttempt("new", true, 0, null)) as AcceptedAttemptRestore.Result.Paused
        assertEquals(0L, result.sequence)
        assertTrue(result.checkpoint.state.paused)
        assertTrue(result.checkpoint.state.floors.isEmpty())
        assertFalse(result.terminal)
    }

    @Test fun acceptedPhysicsPreservedButTimingAndPauseReset() {
        val original = checkpoint()
        val result = restore(attempt(original)) as AcceptedAttemptRestore.Result.Paused
        assertEquals(original.copy(tickUnits = 0, state = original.state.copy(paused = true)), result.checkpoint)
        val engine = PracticeEngine(); engine.restore(result.checkpoint)
        val before = engine.checkpoint()
        assertFalse(engine.drop()); engine.advance(1_000_000_000)
        assertEquals(before, engine.checkpoint())
    }

    @Test fun terminalActiveSnapshotCannotBecomePlayableAndFinishedDoesNotDecode() {
        val result = restore(attempt(checkpoint(Phase.FINISHED))) as AcceptedAttemptRestore.Result.Paused
        assertTrue(result.terminal)
        assertTrue(result.checkpoint.state.paused)
        assertEquals(AcceptedAttemptRestore.Result.Finished("attempt"), restore(attempt().copy(active = false, checkpointJson = "invalid")))
    }

    @Test fun continuedAttemptStaysBlockedEvenWithNonterminalPhysics() {
        blocked(attempt().copy(continued = true), AcceptedAttemptRestore.Reason.CONTINUATION_UNRESOLVED)
    }

    @Test fun invalidCountersSequenceAndMissingOrCorruptSnapshotsFailClosed() {
        val valid = attempt()
        listOf(valid.copy(floors = 2), valid.copy(precise = 0), valid.copy(rescues = 1),
            valid.copy(sequence = -1), valid.copy(sequence = 2), valid.copy(checkpointJson = null),
            valid.copy(checkpointJson = "{}"), valid.copy(checkpointJson = "["), valid.copy(checkpointJson = "[]"),
            valid.copy(sequence = 0), valid.copy(id = ""),
            ServerAttempt("new", true, 0, null, precise = 1),
            ServerAttempt("new", true, 0, EngineCheckpointCodec.encode(PracticeEngine().checkpoint()))).forEach { blocked(it) }
    }

    @Test fun unsupportedEngineAndRulesFailClosed() {
        blocked(attempt().copy(engineVersion = "future"), AcceptedAttemptRestore.Reason.UNSUPPORTED_VERSION)
        blocked(attempt().copy(rulesVersion = "future"), AcceptedAttemptRestore.Reason.UNSUPPORTED_VERSION)
    }
}
