package com.aladdin.wobbrik

import com.aladdin.wobbrik.engine.*
import org.junit.Assert.*
import org.junit.Test

class SceneMotionTest {
    @Test fun fixedViewAndClearanceAcrossLandingsAndRestore() {
        val motion = SceneMotion()
        for (count in listOf(0, 1, 4, 60, 61, 0, 10)) {
            val state = PracticeState(floors = List(count) { Floor(if (it % 2 == 0) 40.0 else -20.0, it * 30.0) })
            motion.advance(state, 1f / 60, 40.0)
            assertEquals(0f, motion.cameraXUnits, 0f)
            assertEquals(0f, motion.towerDegrees, 0f)
            assertEquals(40f, motion.craneOriginXUnits, 0f)
            assertEquals(120f, count * 30f - motion.cameraUnits, 0f)
        }
    }
    @Test fun resetAndZeroTimeRestoreKeepCameraStable() {
        val motion = SceneMotion()
        val state = PracticeState(floors = List(60) { Floor(40.0, it * 30.0) })
        motion.advance(state, 0f, 40.0)
        val camera = motion.cameraUnits
        repeat(100) { motion.advance(state, 1f / 60, 40.0) }
        assertEquals(camera, motion.cameraUnits, 0f)
        motion.reset()
        motion.advance(state, 0f, 40.0)
        assertEquals(camera, motion.cameraUnits, 0f)
        assertEquals(0f, motion.cameraXUnits, 0f)
    }
}
