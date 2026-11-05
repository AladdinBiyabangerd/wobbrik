package com.aladdin.wobbrik

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.aladdin.wobbrik.engine.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal data class SceneArt(val floor: ImageBitmap, val crane: ImageBitmap,
    val island: ImageBitmap, val roof: ImageBitmap, val cloud: ImageBitmap)

/** Sprite coordinates are art anchors, not UI spacing or authoritative building geometry.
 * No bitmap allocation/decoding in the frame loop. Physics/collision units stay unchanged.
 */
internal fun DrawScope.drawGameScene(state: PracticeState, art: SceneArt, motion: SceneMotion) {
    if (size.width <= 0f || size.height <= 0f) return
    val floorWidth = PracticeRulesV1.FLOOR_WIDTH.toFloat()
    val floorHeight = PracticeRulesV1.FLOOR_HEIGHT.toFloat()
    val top = state.floors.size * floorHeight
    val camera = motion.cameraUnits
    // Reserve the full crane travel + floor width, and island through hook vertically.
    val travelWidth = 2 * PracticeRulesV1.CRANE_RANGE.toFloat() + floorWidth + 50f
    val scale = min(size.width / travelWidth, size.height / 520f)
    val ground = (size.height + 350f * scale) / 2f
    // Compress empty air above the stack, continuously through landing. Physics is unchanged.
    fun visualY(y: Float) = if (y > top) top + (y - top) * .5f else y
    fun point(x: Double, y: Double) = Offset(size.width / 2f + (x.toFloat() - motion.cameraXUnits) * scale,
        ground - (visualY(y.toFloat()) - camera) * scale)
    fun sprite(bitmap: ImageBitmap, sx: Int, sy: Int, sw: Int, sh: Int,
        left: Float, upper: Float, width: Float) {
        drawImage(bitmap, srcOffset = IntOffset(sx, sy), srcSize = IntSize(sw, sh),
            dstOffset = IntOffset(left.roundToInt(), upper.roundToInt()),
            dstSize = IntSize(max(1, width.roundToInt()), max(1, (width * sh / sw).roundToInt())))
    }
    if (size.width > size.height) {
        sprite(art.cloud, 160, 150, 1470, 620, size.width * .08f, size.height * .25f, size.width * .2f)
        sprite(art.cloud, 160, 150, 1470, 620, size.width * .73f, size.height * .16f, size.width * .2f)
        sprite(art.cloud, 160, 150, 1470, 620, size.width * .22f, size.height * .03f, size.width * .14f)
    }
    val width = floorWidth * scale
    // Visible brick silhouette crop; original alpha and generated artwork are preserved.
    fun floor(x: Double, y: Double, degrees: Float = 0f, position: Offset = point(x, y)) {
        val p = position
        val upper = p.y - width * 597 / 1064
        // Test the actual projected sprite, never a fixed suffix of the floor list.
        // Keep every visible connecting floor, including ones below the camera anchor.
        if (p.y >= -width && upper <= size.height + width) {
            rotate(degrees, p) {
                sprite(art.floor, 233, 258, 1064, 597, p.x - width / 2, upper, width)
            }
        }
    }
    val island = point(0.0, 0.0)
    val islandWidth = width * 1259 / 720
    sprite(art.island, 123, 129, 1259, 778, island.x - islandWidth / 2,
        island.y - width * 306 / 720, islandWidth)
    // Rotate the connected stack as one body about its foundation. No per-floor sliding.
    rotate(motion.towerDegrees, island) {
        state.floors.forEach { f -> floor(f.centerX, f.baseY) }
    }
    if (state.phase != Phase.FINISHED) {
        val target = point(state.floors.lastOrNull()?.centerX ?: 0.0,
            (state.floors.lastOrNull()?.baseY?.plus(4.0) ?: -30.0))
        // The guide sits just above the open top; it is never a collision rectangle.
        val guide = Path().apply {
            moveTo(target.x - width / 2, target.y - width * .36f)
            lineTo(target.x - width * .22f, target.y - width * .5f)
            lineTo(target.x + width / 2, target.y - width * .47f)
            lineTo(target.x + width * .24f, target.y - width * .33f)
            close()
        }
        rotate(motion.towerDegrees, island) {
            drawPath(guide, if (state.balance.dangerEpisode) GameTokens.Danger else GameTokens.Accent,
                style = Stroke(2 * scale, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5 * scale, 3 * scale))))
        }
        val hangingY = top + PracticeRulesV1.DROP_HEIGHT.toFloat()
        // Crane body and upper suspension point are fixed in window coordinates.
        // The world pans to follow the top floor; only the cable's lower endpoints
        // follow the hanging module. The hook stays at the screen center.
        val hook = Offset(size.width / 2f,
            ground - (4 * floorHeight + PracticeRulesV1.DROP_HEIGHT.toFloat() * .5f + 185f) * scale)
        val craneWidth = width * 1.8f
        // The ring center in the cropped crane art is (174, 410).
        sprite(art.crane, 142, 134, 1858, 483, hook.x - craneWidth * 174 / 1858,
            hook.y - craneWidth * 410 / 1858, craneWidth)
        val module = state.falling ?: Floor(state.craneX + motion.cameraXUnits, hangingY.toDouble())
        // Center suspension has one fixed length, including while the camera settles.
        // Map the falling floor continuously from its release arc to the landing plane.
        val rope = SceneMotion.SUSPENSION_LENGTH * scale
        val x = (module.centerX.toFloat() - motion.cameraXUnits) * scale
        val suspendedY = hook.y + sqrt(max(0f, rope * rope - x * x)) + width * .48f
        val fallFraction = ((module.baseY - top) / PracticeRulesV1.DROP_HEIGHT).toFloat().coerceIn(0f, 1f)
        val landing = point(module.centerX, top.toDouble())
        val p = Offset(landing.x, landing.y + (suspendedY - landing.y) * fallFraction)
        if (state.phase == Phase.SWINGING) {
            val angle = Math.toRadians(motion.loadDegrees.toDouble()).toFloat()
            for ((dx, dy) in listOf(-.48f to -.43f, -.22f to -.55f, .24f to -.4f, .48f to -.51f)) {
                val anchor = Offset(width * dx, width * dy)
                val rotated = Offset(anchor.x * cos(angle) - anchor.y * sin(angle),
                    anchor.x * sin(angle) + anchor.y * cos(angle))
                drawLine(GameTokens.Muted, hook, p + rotated, 1.5f * scale)
            }
        }
        floor(module.centerX, module.baseY, motion.loadDegrees, p)
    } else if (state.floors.isNotEmpty()) {
        val last = state.floors.last()
        val p = point(last.centerX, last.baseY)
        rotate(motion.towerDegrees, island) {
            sprite(art.roof, 229, 121, 1308, 641, p.x - width / 2,
                p.y - width * .36f - width * 641 / 1308, width)
        }
    }
}
