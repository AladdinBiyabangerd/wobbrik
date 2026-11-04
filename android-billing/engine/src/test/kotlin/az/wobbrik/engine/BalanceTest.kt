package az.wobbrik.engine

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class BalanceTest {
    @Test fun centeredLandingIsStableAndOppositeOffsetCanRescue() {
        assertEquals(Balance(), BalanceRulesV1.land(Balance(), 0.0, 1))
        val danger = BalanceRulesV1.land(Balance(0.4), 40.0, 6)
        assertTrue(danger.dangerEpisode)
        val rescued = BalanceRulesV1.land(danger, -70.0, 7)
        assertFalse(rescued.dangerEpisode)
        assertEquals(1, rescued.rescues)
        assertEquals(1, BalanceRulesV1.land(rescued, 0.0, 8).rescues)
        val dangerAgain = BalanceRulesV1.land(rescued, 70.0, 9)
        assertTrue(dangerAgain.dangerEpisode)
        assertEquals(2, BalanceRulesV1.land(dangerAgain, -70.0, 10).rescues)
    }

    @Test fun dangerUsesHysteresisAndSustainedOffsetsCanTopple() {
        val danger = BalanceRulesV1.land(Balance(0.6, true), -25.0, 6)
        assertTrue(danger.dangerEpisode)
        assertEquals(0, danger.rescues)
        val toppled = BalanceRulesV1.land(Balance(0.9, true), 50.0, 6)
        assertTrue(toppled.lean >= BalanceRulesV1.TOPPLE)
    }

    @Test fun firstFiveAreForgivingAndGrowthIsCapped() {
        (1..5).forEach { assertEquals(1.0, BalanceRulesV1.difficulty(it), 0.0) }
        assertTrue(BalanceRulesV1.difficulty(6) > 1.0)
        assertEquals(2.0, BalanceRulesV1.difficulty(Int.MAX_VALUE), 0.0)
        val early = BalanceRulesV1.land(Balance(), 50.0, 5)
        val late = BalanceRulesV1.land(Balance(), 50.0, 100)
        assertTrue(abs(early.lean) < abs(late.lean))
    }

    private fun landAt(engine: PracticeEngine, x: Double) {
        var ticks = 0
        while (abs(engine.state.craneX - x) > 1.5 && ticks++ < 1000) engine.advance(16_666_667)
        assertTrue("target reachable", ticks < 1000)
        assertTrue(engine.drop())
        while (engine.state.phase == Phase.FALLING) engine.advance(16_666_667)
    }

    @Test fun engineDangerDoesNotDisappearOrLoseWhileWaiting() {
        val engine = PracticeEngine()
        repeat(10) { landAt(engine, 0.0) }
        landAt(engine, 60.0)
        landAt(engine, 120.0)
        assertTrue(engine.state.balance.dangerEpisode)
        val balance = engine.state.balance
        repeat(3000) { engine.advance(16_666_667) }
        assertEquals(Phase.SWINGING, engine.state.phase)
        assertEquals(balance, engine.state.balance)
        assertEquals(0.0, engine.state.sway, 0.000001)
        landAt(engine, 30.0)
        assertEquals(1, engine.state.balance.rescues)
        engine.restart()
        assertEquals(PracticeState(), engine.state)
    }

    @Test fun engineTopplesOnLandingEvenWithOverlap() {
        val engine = PracticeEngine()
        repeat(45) { landAt(engine, 0.0) }
        assertEquals(45, engine.state.perfectLandings)
        landAt(engine, 90.0)
        assertEquals(Phase.SWINGING, engine.state.phase)
        landAt(engine, 120.0)
        assertEquals(Phase.FINISHED, engine.state.phase)
        assertTrue(engine.state.balance.lean >= BalanceRulesV1.TOPPLE)
        assertEquals(47, engine.state.floors.size)
        assertFalse(engine.drop())
    }

    @Test fun pausePreservesImpactVelocityAndRestartRemovesIt() {
        val uninterrupted = PracticeEngine()
        val paused = PracticeEngine()
        landAt(uninterrupted, 60.0)
        landAt(paused, 60.0)
        paused.pause()
        val snapshot = paused.state
        paused.advance(1_000_000_000)
        assertEquals(snapshot, paused.state)
        paused.resume()
        repeat(30) {
            uninterrupted.advance(16_666_667)
            paused.advance(16_666_667)
        }
        assertEquals(uninterrupted.state, paused.state)
        paused.restart()
        val fresh = PracticeEngine()
        paused.advance(16_666_667)
        fresh.advance(16_666_667)
        assertEquals(fresh.state, paused.state)
    }
}
