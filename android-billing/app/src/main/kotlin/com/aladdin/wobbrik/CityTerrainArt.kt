package com.aladdin.wobbrik

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import appbackend.wobbrik.game.CitySceneDecoration

internal data class CityTerrainArt(val grass: Brush, val cliff: Brush)

@Composable
internal fun rememberCityTerrainArt(): CityTerrainArt {
    val context = LocalContext.current
    return remember(context) {
        val sheet = context.assets.open("city/terrain-material-v1.png").use { BitmapFactory.decodeStream(it) }
        fun material(y: Int, scale: Float): Brush {
            // Material sheet boundary has blended pixels; exclude its four-pixel atlas gutter.
            val gutter = 4
            val image = Bitmap.createBitmap(sheet, gutter, y + gutter, sheet.width - 2*gutter, sheet.height / 2 - 2*gutter).asImageBitmap()
            return ShaderBrush(ImageShader(image, TileMode.Mirror, TileMode.Mirror).apply {
                setLocalMatrix(Matrix().apply { setScale(scale, scale) })
            })
        }
        CityTerrainArt(material(0, 0.25f), material(sheet.height / 2, 0.08f))
    }
}

/** Boolean union suppresses internal parcel/L-shape seams. World geometry is never rounded. */
internal fun citySurface(contours: List<List<CityBuildingProjection.Point>>): Path =
    contours.fold(Path()) { union, points ->
        val polygon = Path().apply {
            points.forEachIndexed { index, p -> if (index == 0) moveTo(p.x.toFloat(), p.y.toFloat()) else lineTo(p.x.toFloat(), p.y.toFloat()) }
            close()
        }
        Path.combine(PathOperation.Union, union, polygon)
    }

internal fun cityCliff(contours: List<List<CityBuildingProjection.Point>>): Path {
    val depth = CityTerrainGeometry.CliffDepth
    val faces = contours.flatMap { points ->
        points.indices.map { index ->
            val a = points[index]; val b = points[(index + 1) % points.size]
            listOf(a, b, b.copy(y = b.y + depth), a.copy(y = a.y + depth))
        }
    }
    return citySurface(contours + faces)
}

internal fun DrawScope.cityTerrain(surface: Path, cliff: Path, view: CityViewport, art: CityTerrainArt) {
    if (surface.isEmpty) return
    withTransform({ translate(view.x.toFloat(), view.y.toFloat()); scale(view.scale.toFloat(), view.scale.toFloat(), Offset.Zero) }) {
        drawPath(cliff, art.cliff)
        drawPath(surface, art.grass)
    }
}

internal data class CityDecorationDrawing(val source: CitySceneDecoration, val art: CosmeticSprite,
    val anchor: CityBuildingProjection.Point, val width: Double, val height: Double,
    val bounds: CityBuildingProjection.Bounds, val road: Boolean)

internal fun cityDecoration(source: CitySceneDecoration, art: Map<String, CosmeticSprite>): CityDecorationDrawing? {
    val sprite = art[source.copy.itemId] ?: return null
    val (w, h) = CityTerrainGeometry.decorationSize(source.copy.itemId) ?: return null
    val anchor = CityTerrainGeometry.point(source.position, 0.0, 0.0)
    val road = source.copy.itemId.startsWith("decor_road_")
    val points = if (road) listOf(-w/2 to -h/2, w/2 to -h/2, w/2 to h/2, -w/2 to h/2)
        .map { (x, y) -> CityTerrainGeometry.point(source.position, x, y) } else emptyList()
    // Upright atlas billboards keep the explicitly approved fixed view, not a fake bitmap rotation.
    val width = w + h
    val height = width * sprite.size.height / sprite.size.width
    val bounds = if (road) CityBuildingProjection.Bounds(points.minOf { it.x }, points.minOf { it.y }, points.maxOf { it.x }, points.maxOf { it.y })
        else CityBuildingProjection.Bounds(anchor.x - width/2, anchor.y - height, anchor.x + width/2, anchor.y)
    return CityDecorationDrawing(source, sprite, anchor, width, height, bounds, road)
}

internal fun DrawScope.cityDecoration(drawing: CityDecorationDrawing, view: CityViewport) {
    if (!drawing.road) {
        sprite(drawing.art, (view.x + drawing.bounds.left * view.scale).toFloat(),
            (view.y + drawing.bounds.top * view.scale).toFloat(), (drawing.width * view.scale).toFloat(), (drawing.height * view.scale).toFloat())
        return
    }
    val (width, height) = requireNotNull(CityTerrainGeometry.decorationSize(drawing.source.copy.itemId))
    val origin = CityTerrainGeometry.point(drawing.source.position, -width / 2, height / 2)
    val u = CityTerrainGeometry.point(drawing.source.position, width / 2, height / 2)
    val v = CityTerrainGeometry.point(drawing.source.position, -width / 2, -height / 2)
    val matrix = Matrix().apply { setValues(floatArrayOf(
        ((u.x-origin.x)*view.scale/width).toFloat(), ((v.x-origin.x)*view.scale/height).toFloat(), (view.x+origin.x*view.scale).toFloat(),
        ((u.y-origin.y)*view.scale/width).toFloat(), ((v.y-origin.y)*view.scale/height).toFloat(), (view.y+origin.y*view.scale).toFloat(), 0f,0f,1f)) }
    val a = drawing.art
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.apply {
            save(); concat(matrix)
            drawBitmap(a.image.asAndroidBitmap(), Rect(a.origin.x,a.origin.y,a.origin.x+a.size.width,a.origin.y+a.size.height),
                RectF(0f,0f,width.toFloat(),height.toFloat()), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            restore()
        }
    }
}

/** Conservative affine sampling cap: no source pixel is enlarged by the road projection. */
internal fun cityDecorationArtScale(drawing: CityDecorationDrawing): Double {
    if (!drawing.road) return drawing.art.size.width / drawing.width
    val (w, h) = requireNotNull(CityTerrainGeometry.decorationSize(drawing.source.copy.itemId))
    val u = w / drawing.art.size.width
    val v = h / drawing.art.size.height
    return 1.0 / kotlin.math.sqrt(1.25 * (u*u + v*v))
}
