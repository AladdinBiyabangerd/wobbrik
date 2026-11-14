package com.aladdin.wobbrik

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.json.JSONObject
import kotlin.math.ceil

/** Presentation ornaments from city-view, never catalog items or placed/owned decor. */
internal data class CityGardenArt(val sprites: Map<String, CosmeticSprite>)

@Composable
internal fun rememberCityGardenArt(): CityGardenArt {
    val context = LocalContext.current
    return remember(context) {
        CityGardenArt(listOf("garden-v1", "balcony-v1", "terrace-v1").flatMap { name ->
            val data = JSONObject(context.assets.open("city/$name.json").bufferedReader().use { it.readText() })
            val image = context.assets.open("city/$name.png").use { requireNotNull(BitmapFactory.decodeStream(it)) }.asImageBitmap()
            val cells = data.getJSONObject("sprites")
            cells.keys().asSequence().map { key ->
                val c = cells.getJSONArray(key)
                key to CosmeticSprite(image, IntOffset(c.getInt(0), c.getInt(1)), IntSize(c.getInt(2), c.getInt(3)))
            }.toList()
        }.toMap())
    }
}

/** Samples the boolean union, so adjacent parcels and the two rectangles of an L
 * never acquire an interior shoreline. Every tuft is inset into actual owned land. */
internal data class CityShore(val strips: List<Pair<Offset, Offset>>, val tufts: List<Offset>)
internal fun cityShore(surface: Path): CityShore {
    if (surface.isEmpty) return CityShore(emptyList(), emptyList())
    val bounds = surface.getBounds()
    val region = Region().apply { setPath(surface.asAndroidPath(), Region(
        kotlin.math.floor(bounds.left).toInt(), kotlin.math.floor(bounds.top).toInt(),
        kotlin.math.ceil(bounds.right).toInt(), kotlin.math.ceil(bounds.bottom).toInt())) }
    val measure = PathMeasure(surface.asAndroidPath(), true)
    val strips = mutableListOf<Pair<Offset,Offset>>()
    val tufts = mutableListOf<Offset>()
    val p = FloatArray(2); val tangent = FloatArray(2)
    do {
        // Subdivide for texture repetition. PathMeasure works on minSdk24;
        // Path.approximate would require API26. Half-unit sampling is presentation
        // only and remains clipped to the exact owned surface/cliff paths.
        val count = ceil(measure.length / 48f).toInt().coerceAtLeast(1)
        for (index in 0 until count) {
            val from = measure.length * index / count
            val to = measure.length * (index + 1) / count
            val samples = ceil((to-from)/0.5f).toInt().coerceAtLeast(1)
            val points = mutableListOf<Offset>()
            for (sample in 0..samples) {
                measure.getPosTan(from+(to-from)*sample/samples,p,null)
                val next = Offset(p[0],p[1])
                if (points.size >= 2) {
                    val a = points[points.lastIndex]-points[points.lastIndex-1]
                    val b = next-points.last()
                    if (kotlin.math.abs(a.x*b.y-a.y*b.x) < 0.001f && a.x*b.x+a.y*b.y >= 0f)
                        points.removeAt(points.lastIndex)
                }
                points += next
            }
            points.zipWithNext().forEach { (a,b) ->
                if ((b-a).getDistance() > 0.01f) strips += a to b
            }
            measure.getPosTan((from+to)/2, p, tangent)
            val a = Offset(p[0]-tangent[1]*12f, p[1]+tangent[0]*12f)
            val b = Offset(p[0]+tangent[1]*12f, p[1]-tangent[0]*12f)
            val inside = listOf(a,b).firstOrNull { region.contains(it.x.toInt(),it.y.toInt()) }
            if (inside != null) tufts += inside
        }
    } while (measure.nextContour())
    return CityShore(strips, tufts)
}

internal fun DrawScope.cityGardenShore(surface: Path, cliff: Path, shore: CityShore, view: CityViewport, art: CityGardenArt) {
    withTransform({ translate(view.x.toFloat(),view.y.toFloat()); scale(view.scale.toFloat(),view.scale.toFloat(),Offset.Zero) }) {
        val strip = art.sprites.getValue("shore")
        val source = Rect(strip.origin.x,strip.origin.y,strip.origin.x+strip.size.width,strip.origin.y+strip.size.height)
        clipPath(cliff) {
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val w = strip.size.width.toFloat(); val h = strip.size.height.toFloat()
                shore.strips.forEach { (a,b) ->
                    val matrix = Matrix().apply { setPolyToPoly(floatArrayOf(0f,0f,w,0f,0f,h),0,
                        floatArrayOf(a.x,a.y-4f,b.x,b.y-4f,a.x,a.y+CityTerrainGeometry.CliffDepth.toFloat()),0,3) }
                    native.save(); native.concat(matrix)
                    native.drawBitmap(strip.image.asAndroidBitmap(),source,RectF(0f,0f,w,h),paint)
                    native.restore()
                }
            }
        }
        clipPath(surface) {
            shore.tufts.forEachIndexed { index, point ->
                val item = art.sprites.getValue(if (index % 4 == 0) "flowers" else "tuft")
                val width = if (index % 4 == 0) 22f else 15f
                val height = width * item.size.height / item.size.width
                sprite(item, point.x-width/2,point.y-height,width,height)
            }
        }
    }
}

/** Fixed-camera facade ornaments; cafe furniture is presentation, never an owned catalog item. */
internal fun DrawScope.cityFacadeGarden(art: CityGardenArt, x: Float, base: Float, width: Float, ground: Boolean, cornerWindow: Boolean, cafe: Boolean = false) {
    if (ground) return // Ground door, selected lighting and accepted footprint stay unobscured.
    // Keep the narrow selected corner-window kit unobscured by parasols.
    val front = art.sprites.getValue(if (cafe && !cornerWindow) "cafe_front" else "balcony_front")
    val side = art.sprites.getValue("balcony_side")
    // Coordinates refer to the same approved 464x288 shell as GameFacade, not world geometry.
    val upper = base - width*288f/464f
    fun plant(item: CosmeticSprite, left: Float, top: Float, w: Float) {
        sprite(item,x+width*left,upper+width*top,width*w,width*w*item.size.height/item.size.width)
    }
    // The front sample is horizontal. Shear it to the measured shell front
    // (16px fall across 326px), rather than putting a level sticker on that plane.
    val left = if (cornerWindow) .47f else .08f
    val span = if (cornerWindow) .25f else if (cafe) .62f else .46f
    val w = width * span
    val h = w * front.size.height / front.size.width
    val top = upper + width * (if (cafe && !cornerWindow) .25f else .36f)
    drawIntoCanvas { canvas ->
        val matrix = Matrix().apply { setPolyToPoly(
            floatArrayOf(0f,0f,front.size.width.toFloat(),0f,0f,front.size.height.toFloat()),0,
            floatArrayOf(x+width*left,top,x+width*left+w,top+w*16f/326f,x+width*left,top+h),0,3) }
        val native = canvas.nativeCanvas
        native.save(); native.concat(matrix)
        native.drawBitmap(front.image.asAndroidBitmap(),
            Rect(front.origin.x,front.origin.y,front.origin.x+front.size.width,front.origin.y+front.size.height),
            RectF(0f,0f,front.size.width.toFloat(),front.size.height.toFloat()),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        native.restore()
    }
    plant(side,.74f,.34f,.24f)
}
