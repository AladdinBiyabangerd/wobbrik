package com.aladdin.wobbrik

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

/** Existing catalog art only. Measured source crops exclude neighbouring cells.
 * Both eaves share the approved shell's three anchors, including its front slope.
 */
internal fun DrawScope.purpleSelectedRoof(item: CosmeticSprite, id: String?, x: Float, y: Float) {
    val w = when(id) { "reward_roof_1" -> 280f; "coins_roof_1" -> 303f; "reward_roof_2" -> 305f
        else -> item.size.width.toFloat() }
    val h = if(id == "reward_roof_1") 154f else item.size.height.toFloat()
    val seam = w * .86f
    val rise = 338f * h / w * .65f
    drawIntoCanvas { canvas ->
        val native=canvas.nativeCanvas
        for (side in listOf(false,true)) {
            val a=if(side) seam else 0f
            val b=if(side) w else seam
            val left=if(side) Offset(x+240f,y+128f) else Offset(x+16f,y+68f)
            val right=if(side) Offset(x+354f,y+63f) else Offset(x+240f,y+128f)
            val matrix=Matrix()
            if(!matrix.setPolyToPoly(floatArrayOf(a,h,b,if(side) h-w*.24f else h,a,0f),0,
                floatArrayOf(left.x,left.y,right.x,right.y,left.x,left.y-rise),0,3)) continue
            native.save(); native.concat(matrix); native.clipRect(a,0f,b,h)
            native.drawBitmap(item.image.asAndroidBitmap(),
                Rect(item.origin.x,item.origin.y,item.origin.x+w.toInt(),item.origin.y+h.toInt()),
                RectF(0f,0f,w,h),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            native.restore()
        }
    }
}

/** Sample the selected roof's visible covering, rather than shrinking a whole
 * roof (fascia, underside and transparent margins) onto the annex roof plane.
 * Coordinates are measured in existing catalog cells, never replacement art.
 */
internal fun DrawScope.purpleSelectedAnnexRoof(item: CosmeticSprite, id: String?, target: List<Offset>) {
    val source=when(id) {
        "base_2_roof" -> listOf(49f,79f,157f,28f,425f,46f,316f,110f)
        "base_3_roof" -> listOf(61f,105f,170f,105f,206f,116f,40f,116f)
        "base_4_roof" -> listOf(214f,110f,261f,104f,245f,139f,208f,139f)
        "base_5_roof" -> listOf(57f,63f,208f,63f,208f,121f,34f,121f)
        "base_6_roof" -> listOf(52f,51f,205f,51f,238f,148f,20f,148f)
        "coins_roof_1" -> listOf(58f,141f,145f,83f,244f,145f,150f,171f)
        "coins_roof_2" -> listOf(88f,20f,214f,20f,204f,35f,76f,35f)
        "coins_roof_3" -> listOf(30f,173f,231f,173f,245f,189f,15f,189f)
        "reward_roof_1" -> listOf(60f,33f,216f,33f,202f,64f,43f,64f)
        "reward_roof_2" -> listOf(53f,140f,144f,67f,244f,143f,150f,157f)
        "reward_roof_3" -> listOf(47f,78f,133f,13f,243f,74f,160f,125f)
        "paid_roof_1" -> listOf(45f,146f,104f,85f,186f,129f,114f,180f)
        "summit_crown" -> listOf(66f,147f,105f,115f,115f,145f,88f,166f)
        else -> return
    }
    purpleSurface(item,source,target)
}

internal fun DrawScope.purpleTealRoof(item: CosmeticSprite, x: Float, y: Float) {
    drawIntoCanvas { canvas ->
        val matrix=Matrix()
        matrix.setPolyToPoly(floatArrayOf(6f,120f,344f,147f,470f,56f),0,
            floatArrayOf(x+16f,y+68f,x+240f,y+128f,x+354f,y+63f),0,3)
        val native=canvas.nativeCanvas
        native.save();native.concat(matrix)
        native.drawBitmap(item.image.asAndroidBitmap(),
            Rect(item.origin.x,item.origin.y,item.origin.x+item.size.width,item.origin.y+item.size.height),
            RectF(0f,0f,item.size.width.toFloat(),item.size.height.toFloat()),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        native.restore()
    }
}
