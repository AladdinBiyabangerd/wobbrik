package com.aladdin.wobbrik

import com.aladdin.wobbrik.engine.Phase
import com.aladdin.wobbrik.engine.PracticeRulesV1
import com.aladdin.wobbrik.engine.PracticeState
import kotlin.math.asin
import kotlin.math.abs

/** Presentation inertia only. Never serialized, scored or used as collision geometry. */
internal class SceneMotion {
    companion object { const val SUSPENSION_LENGTH = 155f }
    var towerDegrees = 0f
        private set
    var loadDegrees = 0f
        private set
    var cameraXUnits = 0f
        private set
    var craneOriginXUnits = 0f
        private set
    var cameraUnits = 0f
        private set
    private var towerVelocity = 0f
    private var loadVelocity = 0f
    private var previous: PracticeState? = null

    val settled: Boolean get() {
        return abs(towerVelocity) < .01f && abs(loadVelocity) < .01f &&
            abs(towerDegrees) < .01f && abs(loadDegrees) < .01f &&
            abs(cameraXUnits) < .01f &&
            abs(cameraUnits - ((previous?.floors?.size ?: 0) - 4) *
                PracticeRulesV1.FLOOR_HEIGHT.toFloat()) < .01f
    }

    fun reset() {
        towerDegrees = 0f; towerVelocity = 0f; loadDegrees = 0f; loadVelocity = 0f
        previous = null
        cameraUnits = 0f
        cameraXUnits = 0f
        craneOriginXUnits = 0f
    }

    fun advance(state: PracticeState, seconds: Float, cameraX: Double = 0.0) {
        // The swing origin belongs to the engine; it is not the view camera.
        craneOriginXUnits = cameraX.toFloat()
        val old = previous
        val cameraTarget = (state.floors.size - 4) * PracticeRulesV1.FLOOR_HEIGHT.toFloat()
        if (old == null || state.floors.size < old.floors.size) {
            cameraUnits = cameraTarget
            cameraXUnits = 0f
        }
        if (old != null && state.floors.size < old.floors.size) {
            towerDegrees = 0f; towerVelocity = 0f; loadDegrees = 0f; loadVelocity = 0f
        }
        previous = state
        val dt = seconds.coerceIn(0f, .05f)
        // Follow height from the first floor. A delayed follow lets the landing plane
        // approach the hanging load during every transition, including rapid drops.
        cameraUnits = cameraTarget
        cameraXUnits = 0f
        // Collision uses unrotated world coordinates: the visible support must too.
        towerDegrees = 0f
        towerVelocity = 0f
        // Small substeps keep springs stable across 30/60/120 Hz and bounded stalls.
        repeat(6) {
            val h = dt / 6
            val cableAngle = Math.toDegrees(asin((state.craneX / SUSPENSION_LENGTH).coerceIn(-1.0, 1.0))).toFloat()
            val loadTarget = if (state.phase == Phase.SWINGING) -cableAngle * .22f else 0f
            // Suspension pulls the load; after release angular momentum decays slowly.
            val spring = if (state.phase == Phase.SWINGING) 24f else 4f
            val damping = if (state.phase == Phase.SWINGING) 4f else 1.2f
            loadVelocity += ((loadTarget - loadDegrees) * spring - damping * loadVelocity) * h
            loadDegrees += loadVelocity * h
        }
    }
}
