package com.aladdin.wobbrik

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

/** Reprojects the selected roof's front and side independently to the same eave.
 * Roof art (including its own dormers) remains owned by the selected roof ID. */
internal fun DrawScope.drawModularRoof(item: CosmeticSprite?, id: String?, left: Float, baseline: Float, width: Float) {
    if (item == null) return
    // Legacy roof crops include neighbouring sprites in three cells. Tighten only
    // the modular renderer's source sample; picker catalog metadata and IDs stay intact.
    val w = when (id) { "reward_roof_1" -> 280f; "coins_roof_1" -> 303f; "reward_roof_2" -> 305f
        else -> item.size.width.toFloat() }
    val h = if (id == "reward_roof_1") 154f else item.size.height.toFloat()
    val seam = w * .86f
    // The old sprite already contains the back-to-front depth. Do not add that
    // depth a second time as vertical roof rise when expanding its narrow side.
    val rise = width * h / w * .65f
    val sourceDepth = w * .24f
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        for (side in listOf(false, true)) {
            val x0 = if (side) seam else 0f
            val x1 = if (side) w else seam
            val a = if (side) left + width * .73f else left
            val b = if (side) left + width else left + width * .73f
            val ya = if (side) baseline else baseline - width * .04f
            val yb = if (side) baseline - width * .16f else baseline
            val matrix = Matrix()
            matrix.setPolyToPoly(floatArrayOf(x0,h,x1,if (side) h-sourceDepth else h,x0,0f),0,
                floatArrayOf(a,ya,b,yb,a,ya-rise),0,3)
            native.save()
            native.concat(matrix)
            native.clipRect(x0,0f,x1,h)
            native.drawBitmap(item.image.asAndroidBitmap(),
                Rect(item.origin.x,item.origin.y,item.origin.x+w.toInt(),item.origin.y+h.toInt()),
                RectF(0f,0f,w,h),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            native.restore()
        }
    }
}

/** Measured art coordinates in the approved 464 × 288 cell; no engine geometry.
 * The clean shell has no windows, entrance or lamps. Each selected ID owns one layer.
 * Material samples exclude the old panel's frame and silhouette entirely.
 */
internal fun DrawScope.drawModularFloor(shell: CosmeticSprite, art: Map<String, CosmeticSprite>, atlases: Map<String, GameAtlas>,
    parts: Map<String, String>, left: Float, upper: Float, width: Float, ground: Boolean, windowAlpha: Float = 1f) {
    translate(left, upper) {
        scale(width / 464f, width / 464f, androidx.compose.ui.geometry.Offset.Zero) {
            val contour = Path().apply {
                val points = listOf(7f to 75f, 129f to 4f, 146f to 4f, 454f to 19f,
                    459f to 23f, 459f to 43f, 452f to 51f, 452f to 179f,
                    458f to 181f, 461f to 186f, 460f to 203f, 345f to 281f,
                    338f to 283f, 7f to 264f, 4f to 260f, 4f to 240f,
                    14f to 231f, 14f to 105f, 8f to 102f)
                points.forEachIndexed { i, p -> if (i == 0) moveTo(p.first,p.second) else lineTo(p.first,p.second) }
                close()
            }
            clipPath(contour) {
                sprite(shell, 0f, 0f, 464f, 288f)
                // Front and right side share the approved hull; never stretch a panel hull.
                fun plane(item: CosmeticSprite?, source: RectF, quad: FloatArray, alpha: Float = 1f) {
                    if (item == null) return
                    drawIntoCanvas { canvas ->
                        val matrix = Matrix()
                        val src = floatArrayOf(source.left,source.top,source.right,source.top,
                            source.right,source.bottom,source.left,source.bottom)
                        matrix.setPolyToPoly(src,0,quad,0,4)
                        val native = canvas.nativeCanvas
                        native.save()
                        native.concat(matrix)
                        native.drawBitmap(item.image.asAndroidBitmap(),
                            Rect(item.origin.x + source.left.toInt(), item.origin.y + source.top.toInt(),
                                item.origin.x + source.right.toInt(), item.origin.y + source.bottom.toInt()),
                            source, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply { this.alpha = (alpha.coerceIn(0f, 1f) * 255).toInt() })
                        native.restore()
                    }
                }
                val wall = art[parts["wall"]]
                // Keep purple's clean original brick; other wall IDs supply their own material.
                if (parts["wall"] !in setOf("base_1_wall", "base_2_wall") && wall != null) {
                    val sample = RectF(wall.size.width * .14f, wall.size.height * .29f,
                        wall.size.width * .78f, wall.size.height * .77f)
                    plane(wall, sample, floatArrayOf(14f,105f,340f,121f,340f,251f,14f,231f))
                    plane(wall, sample, floatArrayOf(340f,121f,452f,51f,452f,179f,340f,251f))
                }
                // Affine surface coordinates preserve the front slope and deep right face.
                fun decal(id: String?, side: Boolean, u: Float, v: Float, w: Float, h: Float, sourceStart: Float = 0f, sourceEnd: Float = 1f) {
                    val approved = when {
                        id?.startsWith("base_1_") == true && id != "base_1_window" -> atlases["purple"]
                        id?.startsWith("base_2_") == true && id != "base_2_window" -> atlases["teal"]
                        else -> null
                    }
                    val item = approved?.cells?.get(id?.substringAfterLast("_")) ?: art[id] ?: return
                    fun point(x: Float,y: Float): Pair<Float,Float> = if (side)
                        (340f + 112f*x) to (121f - 70f*x + 130f*y)
                    else (14f + 326f*x) to (105f + 16f*x + 126f*y)
                    val corners = listOf(point(u,v),point(u+w,v),point(u+w,v+h),point(u,v+h))
                    plane(item, RectF(item.size.width*sourceStart,0f,item.size.width*sourceEnd,item.size.height.toFloat()),
                        corners.flatMap { listOf(it.first,it.second) }.toFloatArray(),
                        if (id == parts["window"]) windowAlpha else 1f)
                }
                if (ground) {
                    decal(parts["entrance"],false,.31f,.02f,.38f,1.02f)
                    decal(parts["lighting"],false,.1f,.14f,.16f,.62f)
                } else if (parts["window"] == "coins_window_2") {
                    // A corner kit has two panes, not three duplicated complete corners.
                    decal(parts["window"],false,.68f,.16f,.32f,.68f,0f,.5f)
                } else {
                    decal(parts["window"],false,.12f,.16f,.27f,.68f)
                    decal(parts["window"],false,.6f,.16f,.27f,.68f)
                }
                if (parts["window"] == "coins_window_2")
                    decal(parts["window"],true,0f,.16f,.6f,.68f,.5f,1f)
                else decal(parts["window"],true,.23f,.16f,.55f,.68f)
            }
        }
    }
}
