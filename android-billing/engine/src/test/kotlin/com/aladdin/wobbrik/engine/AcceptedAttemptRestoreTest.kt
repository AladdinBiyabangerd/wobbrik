package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.GameSnapshot
import appbackend.wobbrik.game.ServerAttempt
import appbackend.wobbrik.game.AcceptedCityGeometry
import appbackend.wobbrik.game.CityFloorGeometry
import appbackend.wobbrik.game.RecoveryDtos
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

    @Test fun typedRestoreRejectsMissingOrMismatchedGeometry() {
        val provenance = AcceptedCityGeometry(CityFloorGeometry(listOf(0L)), "attempt", 1, "cp",
            "wobbrik-v1", "wobbrik-structural-v1")
        val typed = attempt().copy(geometryVersion = CityFloorGeometry.VERSION, acceptedGeometry = provenance)
        assertTrue(restore(typed) is AcceptedAttemptRestore.Result.Paused)
        blocked(typed.copy(acceptedGeometry = null))
        blocked(typed.copy(acceptedGeometry = provenance.copy(sequence = 2)))
        blocked(typed.copy(acceptedGeometry = provenance.copy(geometry = CityFloorGeometry(listOf(1L)))))
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

    @Test fun initialServerJsonWithEmptySnapshotRestoresSamePausedAttempt() {
        val snapshot = RecoveryDtos.snapshot("""{
            "rules_version":"wobbrik-v1","revision":1,"coins":0,"xp":0,
            "server_time":"2026-11-08T00:00:00Z","first_online_at":"2026-11-08T00:00:00Z",
            "grace_until":"2026-11-09T00:00:00Z","level":1,"rescues":0,
            "achievements":[],"unlocks":[],"land":[],"part_progress":[],
            "attempt":{"attempt_id":"existing","status":"active","sequence":0,
                "snapshot":{},"floors":0,"precise":0,"rescues":0,"continued":false,
                "rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1",
                "geometry_version":"city-geometry-v1","accepted_geometry":null}
        }""")
        val result = AcceptedAttemptRestore.from(AuthenticatedRecoveryOwner.State.Ready(snapshot))
            as AcceptedAttemptRestore.Result.Paused
        assertEquals("existing", result.attemptId)
        assertEquals(0L, result.sequence)
        assertEquals(restore(ServerAttempt("existing", true, 0, null)), result)
    }

    @Test fun initialEmptySnapshotDoesNotRelaxCountersVersionsOrSnapshotShape() {
        val fresh = ServerAttempt("existing", true, 0, "{}")
        listOf(fresh.copy(floors = 1), fresh.copy(precise = 1), fresh.copy(rescues = 1),
            fresh.copy(checkpointJson = "[]"), fresh.copy(checkpointJson = "null"),
            fresh.copy(checkpointJson = "{"), fresh.copy(checkpointJson = "{\"unknown\":0}"))
            .forEach { blocked(it) }
        blocked(fresh.copy(engineVersion = "future"), AcceptedAttemptRestore.Reason.UNSUPPORTED_VERSION)
        blocked(fresh.copy(continued = true), AcceptedAttemptRestore.Reason.CONTINUATION_UNRESOLVED)
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
