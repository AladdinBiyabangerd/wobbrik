package com.aladdin.wobbrik.engine

import org.junit.Assert.*
import org.junit.Test

class PracticeEngineTest {
    private fun settle(engine: PracticeEngine) {
        repeat(40) { engine.advance(20_000_000) }
    }

    @Test fun oneTapDropsOneFloorAndRejectsRepeatedInput() {
        val engine = PracticeEngine()
        assertTrue(engine.drop())
        assertFalse(engine.drop())
        settle(engine)
        assertEquals(1, engine.state.floors.size)
        assertEquals(Floor(0.0, 0.0), engine.state.floors.single())
        assertEquals(Phase.SWINGING, engine.state.phase)
    }

    @Test fun pauseFreezesFallAndResumeDoesNotDrop() {
        val engine = PracticeEngine()
        engine.drop()
        engine.advance(100_000_000)
        engine.pause()
        val paused = engine.state
        engine.advance(Long.MAX_VALUE)
        assertFalse(engine.drop())
        assertEquals(paused, engine.state)
        engine.resume()
        assertEquals(paused.copy(paused = false), engine.state)
        settle(engine)
        assertEquals(1, engine.state.floors.size)
    }

    @Test fun framePartitionsProduceSameSimulation() {
        fun run(frame: Long): PracticeState {
            val engine = PracticeEngine()
            repeat((200_000_000L / frame).toInt()) { engine.advance(frame) }
            engine.drop()
            repeat((1_000_000_000L / frame).toInt()) { engine.advance(frame) }
            return engine.state
        }
        assertEquals(run(10_000_000), run(20_000_000))
        assertEquals(run(20_000_000), run(40_000_000))
    }

    @Test fun missedFloorEndsAttemptAndRestartClearsIt() {
        val engine = PracticeEngine()
        repeat(14) { engine.advance(100_000_000) }
        assertTrue(engine.state.craneX > PracticeRulesV1.FLOOR_WIDTH)
        engine.drop()
        settle(engine)
        assertEquals(Phase.FINISHED, engine.state.phase)
        assertFalse(engine.drop())
        engine.advance(1_000_000_000)
        assertTrue(engine.state.floors.isEmpty())
        engine.restart()
        assertEquals(PracticeState(), engine.state)
        assertTrue(engine.drop())
    }

    @Test fun stallIsBoundedAndWaitingCannotLose() {
        val engine = PracticeEngine()
        engine.advance(Long.MAX_VALUE)
        val eightTicks = PracticeEngine()
        repeat(8) { eightTicks.advance(16_666_667) }
        assertEquals(eightTicks.state, engine.state)
        repeat(10_000) { engine.advance(100_000_000) }
        assertEquals(Phase.SWINGING, engine.state.phase)
        assertTrue(engine.state.floors.isEmpty())
    }
}
