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
    val island: ImageBitmap, val roof: ImageBitmap, val cloud: ImageBitmap,
    val atlases: Map<String, GameAtlas> = emptyMap())

/** Sprite coordinates are art anchors, not UI spacing or authoritative building geometry.
 * No bitmap allocation/decoding in the frame loop. Physics/collision units stay unchanged.
 */
internal fun DrawScope.drawGameScene(state: PracticeState, art: SceneArt, motion: SceneMotion, cosmetics: Map<String, CosmeticSprite> = emptyMap(), parts: Map<String, String> = emptyMap()) {
    if (size.width <= 0f || size.height <= 0f) return
    val floorWidth = PracticeRulesV1.FLOOR_WIDTH.toFloat()
    val floorHeight = PracticeRulesV1.FLOOR_HEIGHT.toFloat()
    val atlas = art.atlases[gameAtlasStyle(parts)]
    val wall = cosmetics[parts["wall"]]
    // Full native aspect and measured eave depth, specific to each atlas.
    // Presentation pitch only; accepted floor bases and collision remain 30 engine units.
    val artHeight = atlas?.floor?.let { it.size.height.toFloat() / it.size.width }
        ?: wall?.let { it.size.height.toFloat() / it.size.width } ?: (597f / 1064f)
    val depth = atlas?.let { it.depth / it.floor.size.width }
        ?: if (wall != null) 38f / 270f else 207f / 1064f
    val pitch = floorWidth * (atlas?.let { it.pitch / it.floor.size.width } ?: (artHeight - depth))
    val projection = pitch / floorHeight
    val top = state.floors.size * floorHeight
    val camera = motion.cameraUnits
    // Keep the hanging floor inside the window at max swing, but drop the old
    // 50u gutter so the tower reads closer to game-tap-v2 hero scale.
    val travelWidth = 2 * PracticeRulesV1.CRANE_RANGE.toFloat() + floorWidth
    // Tighter vertical budget: island through crane fills more of the portrait frame.
    val scale = min(size.width / travelWidth, size.height / (4f * pitch + 370f))
    val ground = size.height - 60f * scale
    // Compress empty air above the stack, continuously through landing. Physics is unchanged.
    fun visualY(y: Float) = if (y > top) top * projection + (y - top) * .5f else y * projection
    fun point(x: Double, y: Double) = Offset(size.width / 2f + (x.toFloat() - motion.cameraXUnits) * scale,
        ground - (visualY(y.toFloat()) - camera * projection) * scale)
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
    fun floor(x: Double, y: Double, degrees: Float = 0f, groundFloor: Boolean = false, position: Offset = point(x, y)) {
        val p = position
        val upper = p.y - width * artHeight
        // Test the actual projected sprite, never a fixed suffix of the floor list.
        // Keep every visible connecting floor, including ones below the camera anchor.
        if (p.y >= -width && upper <= size.height + width) {
            rotate(degrees, p) {
                if (atlas != null) {
                    val cell = if (groundFloor) atlas.ground else atlas.floor
                    sprite(cell.image, cell.origin.x, cell.origin.y, cell.size.width, cell.size.height,
                        p.x - width / 2, upper, width)
                } else if (wall == null) sprite(art.floor, 233, 258, 1064, 597, p.x - width / 2, upper, width)
                else {
                    drawImage(wall.image, srcOffset = wall.origin, srcSize = wall.size,
                        dstOffset = IntOffset((p.x - width / 2).roundToInt(), upper.roundToInt()),
                        dstSize = IntSize(width.roundToInt().coerceAtLeast(1), (width * artHeight).roundToInt().coerceAtLeast(1)))
                    if (!groundFloor) sprite(cosmetics[parts["window"]], p.x - width * .2f, upper + width * .12f, width * .4f, width * .35f)
                    if (groundFloor) {
                        sprite(cosmetics[parts["entrance"]], p.x - width * .2f, p.y - width * .4f, width * .4f, width * .4f)
                        sprite(cosmetics[parts["lighting"]], p.x + width * .25f, p.y - width * .35f, width * .15f, width * .25f)
                    }
                }
            }
        }
    }
    val island = point(0.0, 0.0)
    val islandWidth = width * 1259 / 720
    // Binasiz garden: the first accepted floor owns the only entrance/base.
    sprite(art.island, 0, 0, art.island.width, art.island.height, island.x - islandWidth / 2,
        island.y - islandWidth * 65f / 180f, islandWidth)
    // Rotate the connected stack as one body about its foundation. No per-floor sliding.
    rotate(motion.towerDegrees, island) {
        state.floors.forEach { f -> floor(f.centerX, f.baseY, groundFloor = f.baseY == 0.0) }
    }
    if (state.phase != Phase.FINISHED) {
        val target = point(state.floors.lastOrNull()?.centerX ?: 0.0,
            (state.floors.lastOrNull()?.baseY?.plus(4.0) ?: -30.0))
        // The guide sits just above the open top; it is never a collision rectangle.
        val guide = Path().apply {
            if (atlas != null) {
                atlas.topAnchors.forEachIndexed { index, anchor ->
                    val x = target.x - width / 2 + width * anchor.x / atlas.floor.size.width
                    val y = target.y - width * artHeight + width * anchor.y / atlas.floor.size.width
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            } else {
                moveTo(target.x - width / 2, target.y - width * (artHeight - depth))
                lineTo(target.x - width * .22f, target.y - width * artHeight)
                lineTo(target.x + width / 2, target.y - width * artHeight)
                lineTo(target.x + width * .24f, target.y - width * (artHeight - depth))
            }
            close()
        }
        val guideColor = if (state.balance.dangerEpisode) GameTokens.Danger else GameTokens.Accent
        val dash = PathEffect.dashPathEffect(floatArrayOf(6 * scale, 4 * scale))
        rotate(motion.towerDegrees, island) {
            // Soft outer glow, then a crisp dashed core — concept-readable without new art.
            drawPath(guide, guideColor.copy(alpha = 0.22f),
                style = Stroke(7 * scale, pathEffect = dash))
            drawPath(guide, guideColor.copy(alpha = 0.45f),
                style = Stroke(4 * scale, pathEffect = dash))
            drawPath(guide, guideColor,
                style = Stroke(2.25f * scale, pathEffect = dash))
        }
        val hangingY = top + PracticeRulesV1.DROP_HEIGHT.toFloat()
        // Crane body and upper suspension point are fixed in window coordinates.
        // The world pans to follow the top floor; only the cable's lower endpoints
        // follow the hanging module. The hook stays at the screen center.
        val hook = Offset(size.width / 2f,
            ground - (4 * pitch + PracticeRulesV1.DROP_HEIGHT.toFloat() * .5f + 175f) * scale)
        val craneWidth = width * 2.05f
        // The ring center in the cropped crane art is (174, 410).
        sprite(art.crane, 142, 134, 1858, 483, hook.x - craneWidth * 174 / 1858,
            hook.y - craneWidth * 410 / 1858, craneWidth)
        val module = state.falling ?: Floor(state.craneX + motion.craneOriginXUnits, hangingY.toDouble())
        // Center suspension has one fixed length, including while the camera settles.
        // Map the falling floor continuously from its release arc to the landing plane.
        val rope = SceneMotion.SUSPENSION_LENGTH * scale
        val x = (module.centerX.toFloat() - motion.cameraXUnits) * scale
        val suspendedY = hook.y + sqrt(max(0f, rope * rope - x * x)) + width * artHeight
        val fallFraction = ((module.baseY - top) / PracticeRulesV1.DROP_HEIGHT).toFloat().coerceIn(0f, 1f)
        val landing = point(module.centerX, top.toDouble())
        val p = Offset(landing.x, landing.y + (suspendedY - landing.y) * fallFraction)
        if (state.phase == Phase.SWINGING) {
            val angle = Math.toRadians(motion.loadDegrees.toDouble()).toFloat()
            val anchors = if (atlas != null) atlas.topAnchors.map {
                (it.x / atlas.floor.size.width - .5f) to (-artHeight + it.y / atlas.floor.size.width)
            }
                else if (wall == null) listOf(-.48f to -.43f, -.22f to -.55f, .24f to -.4f, .48f to -.51f)
                else listOf(-.46f to (-artHeight + .10f), -.38f to (-artHeight + .04f),
                    .34f to (-artHeight + .18f), .47f to (-artHeight + .11f))
            for ((dx, dy) in anchors) {
                val anchor = Offset(width * dx, width * dy)
                val rotated = Offset(anchor.x * cos(angle) - anchor.y * sin(angle),
                    anchor.x * sin(angle) + anchor.y * cos(angle))
                val end = p + rotated
                drawLine(GameTokens.Text.copy(alpha = 0.35f), hook, end, 3.2f * scale)
                drawLine(GameTokens.Muted, hook, end, 1.8f * scale)
            }
        }
        floor(module.centerX, module.baseY, motion.loadDegrees, position = p)
    } else if (state.floors.isNotEmpty()) {
        val last = state.floors.last()
        val p = point(last.centerX, last.baseY)
        rotate(motion.towerDegrees, island) {
            if (atlas != null) {
                val cell = atlas.roof
                // Cap replaces the exposed top plane, sharing its front eave baseline.
                sprite(cell.image, cell.origin.x, cell.origin.y, cell.size.width, cell.size.height,
                    p.x - width / 2, p.y - width * (artHeight - depth) - width * cell.size.height / cell.size.width, width)
            } else if (cosmetics[parts["roof"]] != null) roof(cosmetics[parts["roof"]], p.x - width / 2, p.y - width * (artHeight - depth), width)
            else sprite(art.roof, 229, 121, 1308, 641, p.x - width / 2,
                p.y - width * (artHeight - depth) - width * 641 / 1308, width)
        }
    }
}
