package com.aladdin.wobbrik.engine

import org.junit.Assert.*
import org.junit.Test

class CameraFollowTest {
    private fun release(engine: PracticeEngine, localX: Double) {
        engine.restore(engine.checkpoint().let { it.copy(state = it.state.copy(craneX = localX)) })
        assertTrue(engine.drop())
    }
    private fun land(engine: PracticeEngine, localX: Double) {
        release(engine, localX)
        repeat(40) { engine.advance(16_666_667) }
    }
    @Test fun cameraTracksBothDirectionsBeyondOldWorldBoundary() {
        for (sign in listOf(-1, 1)) {
            val engine = PracticeEngine()
            repeat(4) { index ->
                land(engine, sign * 40.0)
                assertEquals(Phase.SWINGING, engine.state.phase)
                assertEquals(sign * (index + 1) * 40.0, engine.cameraX, 0.0)
                assertEquals(engine.cameraX, engine.state.floors.last().centerX, 0.0)
            }
            release(engine, 0.0)
            assertEquals(engine.cameraX, engine.state.falling!!.centerX, 0.0)
            repeat(40) { engine.advance(16_666_667) }
            assertEquals(Phase.SWINGING, engine.state.phase)
            assertEquals(5, engine.state.floors.size)
        }
    }
    @Test fun followingCameraDoesNotPreventMissOrBalanceLoss() {
        for (sign in listOf(-1, 1)) {
            val missed = PracticeEngine()
            land(missed, sign * 40.0)
            land(missed, sign * 40.0)
            land(missed, sign * 120.0)
            assertEquals(Phase.FINISHED, missed.state.phase)
            assertEquals(2, missed.state.floors.size)
            val toppled = PracticeEngine()
            repeat(6) { land(toppled, sign * 40.0) }
            assertEquals(Phase.FINISHED, toppled.state.phase)
            assertTrue(kotlin.math.abs(toppled.state.balance.lean) >= BalanceRulesV1.TOPPLE)
            assertFalse(toppled.drop())
        }
    }
    @Test fun shiftedFallRoundtripsAndPauseRestartKeepCameraConsistent() {
        val engine = PracticeEngine()
        repeat(4) { land(engine, 40.0) }
        release(engine, -30.0)
        engine.advance(100_000_000)
        engine.pause()
        val checkpoint = EngineCheckpointCodec.decode(EngineCheckpointCodec.encode(engine.checkpoint()))
        val restored = PracticeEngine().also { it.restore(checkpoint) }
        assertEquals(160.0, restored.cameraX, 0.0)
        restored.advance(1_000_000_000)
        assertEquals(checkpoint, restored.checkpoint())
        engine.resume(); restored.resume()
        repeat(100) {
            engine.advance(16_666_667); restored.advance(16_666_667)
            assertEquals(engine.checkpoint(), restored.checkpoint())
            assertEquals(engine.cameraX, restored.cameraX, 0.0)
        }
        restored.restart()
        assertEquals(0.0, restored.cameraX, 0.0)
        assertEquals(PracticeState(), restored.state)
    }
    @Test fun oldPendulumSnapshotRetainsFixedOriginAndRejectsNewWorldCoordinates() {
        val engine = PracticeEngine()
        engine.restore(engine.checkpoint().copy(rules = PracticeRulesV1.PENDULUM_VERSION))
        land(engine, 40.0)
        assertEquals(0.0, engine.cameraX, 0.0)
        release(engine, 0.0)
        assertEquals(0.0, engine.state.falling!!.centerX, 0.0)
        val decoded = EngineCheckpointCodec.decode(EngineCheckpointCodec.encode(engine.checkpoint()))
        assertEquals(PracticeRulesV1.PENDULUM_VERSION, decoded.rules)
        val shifted = PracticeEngine()
        repeat(4) { land(shifted, 40.0) }
        assertThrows(IllegalArgumentException::class.java) {
            shifted.checkpoint().copy(rules = PracticeRulesV1.PENDULUM_VERSION).validate()
        }
        engine.restart()
        assertEquals(PracticeRulesV1.VERSION, engine.checkpoint().rules)
    }
}
