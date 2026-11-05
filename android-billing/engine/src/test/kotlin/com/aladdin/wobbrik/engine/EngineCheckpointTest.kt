package com.aladdin.wobbrik.engine

import org.junit.Assert.*
import org.junit.Test

class EngineCheckpointTest {
    private fun roundtrip(engine: PracticeEngine) {
        val original = engine.checkpoint()
        val decoded = EngineCheckpointCodec.decode(EngineCheckpointCodec.encode(original))
        assertEquals(original, decoded)
        val restored = PracticeEngine(); restored.restore(decoded)
        repeat(80) { step ->
            if (step % 20 == 0) { engine.drop(); restored.drop() }
            engine.advance(17_123_456); restored.advance(17_123_456)
            assertEquals(engine.checkpoint(), restored.checkpoint())
        }
    }
    @Test fun deterministicAcrossSwingFallPauseAndFinish() {
        roundtrip(PracticeEngine().also { it.advance(123_456_789) })
        roundtrip(PracticeEngine().also { it.drop(); it.advance(100_000_000) })
        roundtrip(PracticeEngine().also { it.drop(); it.advance(100_000_000); it.pause() })
        val landed = PracticeEngine(); landed.drop(); repeat(40) { landed.advance(16_666_667) }
        assertEquals(1, landed.state.floors.size)
        roundtrip(landed)
        val missed = PracticeEngine(); repeat(100) { missed.advance(16_666_667) }; missed.drop()
        repeat(40) { missed.advance(16_666_667) }; assertEquals(Phase.FINISHED, missed.state.phase)
        roundtrip(missed)
    }
    @Test fun legacyCheckpointKeepsItsOriginalTrajectory() {
        val engine = PracticeEngine()
        engine.restore(engine.checkpoint().copy(rules = PracticeRulesV1.LEGACY_VERSION))
        engine.advance(100_000_000)
        assertEquals(9.0, engine.state.craneX, 0.0)
        assertEquals(PracticeRulesV1.LEGACY_VERSION, engine.checkpoint().rules)
        roundtrip(engine)
    }
    @Test fun pendulumSlowsAtTurningPointAndReplaysThere() {
        val engine = PracticeEngine()
        engine.advance(16_666_667)
        val centerStep = engine.state.craneX
        repeat(123) { engine.advance(16_666_667) }
        val before = engine.state.craneX
        engine.advance(16_666_667)
        assertTrue(kotlin.math.abs(engine.state.craneX - before) < centerStep * 0.1)
        roundtrip(engine)
    }
    @Test fun rejectsCorruptionAndDoesNotMutateLiveEngine() {
        val engine = PracticeEngine(); val before = engine.checkpoint()
        val bad = listOf(before.copy(version = "future"), before.copy(rules = "future"), before.copy(direction = 0),
            before.copy(tickUnits = 1_000_000_000), before.copy(fallVelocity = Double.NaN),
            before.copy(state = before.state.copy(phase = Phase.FALLING)),
            before.copy(state = before.state.copy(floors = listOf(Floor(0.0, 9.0)))))
        bad.forEach { assertThrows(IllegalArgumentException::class.java) { engine.restore(it) }; assertEquals(before, engine.checkpoint()) }
        val json = EngineCheckpointCodec.encode(before)
        assertThrows(IllegalArgumentException::class.java) { EngineCheckpointCodec.decode(json.replace("practice-checkpoint-v1", "future")) }
        assertThrows(IllegalArgumentException::class.java) { EngineCheckpointCodec.decodeAccepted(json, 1, 0, 0) }
        assertThrows(IllegalArgumentException::class.java) { EngineCheckpointCodec.decode(" ".repeat(16385)) }
    }
}
