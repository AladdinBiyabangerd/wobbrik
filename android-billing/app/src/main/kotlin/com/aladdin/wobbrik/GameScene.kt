package com.aladdin.wobbrik

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
internal fun DrawScope.drawGameScene(state: PracticeState, art: SceneArt) {
    if (size.width <= 0f || size.height <= 0f) return
    val floorWidth = PracticeRulesV1.FLOOR_WIDTH.toFloat()
    val floorHeight = PracticeRulesV1.FLOOR_HEIGHT.toFloat()
    val top = state.floors.size * floorHeight
    val camera = max(0f, top - 4 * floorHeight)
    // Reserve the full crane travel + floor width, and island through hook vertically.
    val travelWidth = 2 * PracticeRulesV1.CRANE_RANGE.toFloat() + floorWidth + 20f
    val scale = min(size.width / travelWidth, size.height / 405f)
    val ground = (size.height + 265f * scale) / 2f
    // Compress empty air above the stack, continuously through landing. Physics is unchanged.
    fun visualY(y: Float) = if (y > top) top + (y - top) * .5f else y
    fun point(x: Double, y: Double) = Offset(size.width / 2f + x.toFloat() * scale,
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
    fun floor(x: Double, y: Double) {
        val p = point(x, y)
        val upper = p.y - width * 597 / 1064
        // Test the actual projected sprite, never a fixed suffix of the floor list.
        // Keep every visible connecting floor, including ones below the camera anchor.
        if (p.y >= 0f && upper <= size.height) {
            sprite(art.floor, 233, 258, 1064, 597, p.x - width / 2, upper, width)
        }
    }
    val island = point(0.0, 0.0)
    val islandWidth = width * 1259 / 720
    sprite(art.island, 123, 129, 1259, 778, island.x - islandWidth / 2,
        island.y - width * 306 / 720, islandWidth)
    state.floors.forEachIndexed { index, f ->
        floor(f.centerX + state.sway * (index + 1), f.baseY)
    }
    if (state.phase != Phase.FINISHED) {
        val target = point(state.floors.lastOrNull()?.let { it.centerX + state.sway * state.floors.size } ?: 0.0,
            (state.floors.lastOrNull()?.baseY?.plus(4.0) ?: -30.0))
        // The guide sits just above the open top; it is never a collision rectangle.
        val guide = Path().apply {
            moveTo(target.x - width / 2, target.y - width * .36f)
            lineTo(target.x - width * .22f, target.y - width * .5f)
            lineTo(target.x + width / 2, target.y - width * .47f)
            lineTo(target.x + width * .24f, target.y - width * .33f)
            close()
        }
        drawPath(guide, if (state.balance.dangerEpisode) GameTokens.Danger else GameTokens.Accent,
            style = Stroke(2 * scale, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5 * scale, 3 * scale))))
        val hangingY = top + PracticeRulesV1.DROP_HEIGHT.toFloat()
        // Crane body and upper suspension point are fixed in window coordinates.
        // Only the cable's lower endpoints follow the hanging module. Neither the
        // crane nor the tower pans horizontally with the engine's historical craneX.
        val hook = Offset(size.width / 2f,
            ground - (4 * floorHeight + PracticeRulesV1.DROP_HEIGHT.toFloat() * .5f + 95f) * scale)
        val craneWidth = width * 1.8f
        // The ring center in the cropped crane art is (174, 410).
        sprite(art.crane, 142, 134, 1858, 483, hook.x - craneWidth * 174 / 1858,
            hook.y - craneWidth * 410 / 1858, craneWidth)
        val module = state.falling ?: Floor(state.craneX, hangingY.toDouble())
        if (state.phase == Phase.SWINGING) {
            val p = point(module.centerX, module.baseY)
            for ((dx, dy) in listOf(-.48f to -.43f, -.22f to -.55f, .24f to -.4f, .48f to -.51f)) {
                drawLine(GameTokens.Muted, hook, Offset(p.x + width * dx, p.y + width * dy), 1.5f * scale)
            }
        }
        floor(module.centerX, module.baseY)
    } else if (state.floors.isNotEmpty()) {
        val last = state.floors.last()
        val p = point(last.centerX + state.sway * state.floors.size, last.baseY)
        sprite(art.roof, 229, 121, 1308, 641, p.x - width / 2,
            p.y - width * .36f - width * 641 / 1308, width)
    }
}
