package com.aladdin.wobbrik.engine

import org.junit.Assert.*
import org.junit.Test

class CityGeometryTest {
    @Test fun sharedRoundingVectorsAndBounds() {
        listOf(0.0005 to 1L, -0.0005 to -1L, 0.0004999999999999999 to 0L,
            -0.0004999999999999999 to 0L, 1.2345 to 1235L, -1.2345 to -1235L,
            -0.0 to 0L).forEach { (value, tick) -> assertEquals(tick, cityTick(value)) }
        listOf(Double.NaN, Double.POSITIVE_INFINITY, 1_000_000.1).forEach {
            assertThrows(IllegalArgumentException::class.java) { cityTick(it) }
        }
    }
    @Test fun projectionKeepsFoundationOffsetAndDoesNotMutatePhysics() {
        val original = PracticeEngine().checkpoint().let {
            it.copy(state = it.state.copy(floors = listOf(Floor(-1.2345, 0.0), Floor(2.0005, 30.0)), sway = 14.0))
        }
        assertEquals(listOf(-1235L, 2001L), original.cityGeometry().centers)
        assertEquals(-1.2345, original.state.floors.first().centerX, 0.0)
        assertEquals(14.0, original.state.sway, 0.0)
    }
    @Test fun roundingCannotTurnLostSupportIntoTrustedFootprint() {
        val value = PracticeEngine().checkpoint().let {
            it.copy(state = it.state.copy(floors = listOf(Floor(99.9999, 0.0))))
        }
        value.validate()
        assertThrows(IllegalArgumentException::class.java) { value.cityGeometry() }
    }
}
