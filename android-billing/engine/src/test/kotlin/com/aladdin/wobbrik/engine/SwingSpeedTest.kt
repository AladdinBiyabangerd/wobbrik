package com.aladdin.wobbrik.engine

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.asin
import kotlin.math.abs

class SwingSpeedTest {
    private fun atLevel(level: Int, rules: String = PracticeRulesV1.VERSION): PracticeEngine =
        PracticeEngine().also { engine ->
            val floors = List(level - 1) { Floor(0.0, it * PracticeRulesV1.FLOOR_HEIGHT) }
            engine.restore(engine.checkpoint().copy(rules = rules, state = PracticeState(floors = floors)))
        }

    private fun measuredSpeed(engine: PracticeEngine): Double {
        engine.advance(16_666_667)
        return asin(engine.state.craneX / PracticeRulesV1.CRANE_RANGE) * PracticeRulesV1.CRANE_RANGE
    }

    @Test fun speedIncreasesEveryLevelUntilPlayableCap() {
        var previous = 0.0
        for (level in 1..26) {
            val speed = measuredSpeed(atLevel(level))
            assertTrue("level $level is faster", speed > previous)
            previous = speed
        }
        assertEquals(PracticeRulesV1.CRANE_SPEED, measuredSpeed(atLevel(1)), 1e-10)
        assertEquals(1.8 * PracticeRulesV1.CRANE_SPEED, measuredSpeed(atLevel(11)), 1e-10)
        assertEquals(3.0 * PracticeRulesV1.CRANE_SPEED, measuredSpeed(atLevel(26)), 1e-10)
        assertEquals(previous, measuredSpeed(atLevel(101)), 1e-10)
        assertEquals(3.0, PracticeRulesV1.swingMultiplier(Int.MAX_VALUE), 0.0)
        assertThrows(IllegalArgumentException::class.java) { PracticeRulesV1.swingMultiplier(0) }
    }

    @Test fun actualLandingAdvancesSpeedButPauseAndRestartDoNot() {
        val engine = PracticeEngine()
        engine.drop()
        repeat(40) { engine.advance(16_666_667) }
        assertEquals(1, engine.state.floors.size)
        // Measure from the same phase without changing the landed level.
        engine.restore(engine.checkpoint().let { it.copy(state = it.state.copy(craneX = 0.0)) })
        assertEquals(1.08 * PracticeRulesV1.CRANE_SPEED, measuredSpeed(engine), 1e-10)
        engine.pause()
        val paused = engine.checkpoint()
        engine.advance(1_000_000_000)
        assertEquals(paused, engine.checkpoint())
        engine.restart()
        assertEquals(PracticeRulesV1.CRANE_SPEED, measuredSpeed(engine), 1e-10)
    }

    @Test fun fastSwingStaysInRangeAndReplaysAcrossTurnsAndDrop() {
        val original = atLevel(26)
        repeat(35) { original.advance(16_666_667) }
        val restored = PracticeEngine().also {
            it.restore(EngineCheckpointCodec.decode(EngineCheckpointCodec.encode(original.checkpoint())))
        }
        repeat(500) { tick ->
            if (tick == 300) { original.drop(); restored.drop() }
            original.advance(16_666_667); restored.advance(16_666_667)
            assertEquals(original.checkpoint(), restored.checkpoint())
            assertTrue(abs(original.state.craneX) <= PracticeRulesV1.CRANE_RANGE)
        }
    }

    @Test fun previousRulesRetainTheirExactSpeedAndCameraBehavior() {
        for (rules in listOf(PracticeRulesV1.LEGACY_VERSION, PracticeRulesV1.PENDULUM_VERSION, PracticeRulesV1.CAMERA_VERSION)) {
            val original = atLevel(11, rules)
            val expected = BalanceRulesV1.difficulty(11) * PracticeRulesV1.CRANE_SPEED
            if (rules == PracticeRulesV1.LEGACY_VERSION) {
                original.advance(16_666_667)
                assertEquals(expected, original.state.craneX, 0.0)
            } else {
                assertEquals(expected, measuredSpeed(original), 1e-10)
            }
            val restored = PracticeEngine().also {
                it.restore(EngineCheckpointCodec.decode(EngineCheckpointCodec.encode(original.checkpoint())))
            }
            repeat(150) {
                original.advance(16_666_667); restored.advance(16_666_667)
                assertEquals(original.checkpoint(), restored.checkpoint())
            }
            assertEquals(rules, restored.checkpoint().rules)
        }
        val oldCamera = atLevel(21, PracticeRulesV1.CAMERA_VERSION)
        oldCamera.restore(oldCamera.checkpoint().let {
            it.copy(state = it.state.copy(floors = it.state.floors.mapIndexed { index, f -> f.copy(centerX = index * 8.0) }))
        })
        assertEquals(152.0, oldCamera.cameraX, 0.0)
        oldCamera.drop()
        assertEquals(152.0, oldCamera.state.falling!!.centerX, 0.0)
    }
}
