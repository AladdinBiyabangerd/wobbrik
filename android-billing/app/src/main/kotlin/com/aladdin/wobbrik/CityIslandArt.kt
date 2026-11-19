package com.aladdin.wobbrik

import android.graphics.Paint
import androidx.compose.ui.graphics.*

/** Scene world units, not UI sizing or placement rules. Server contour remains exact. */
internal object CityIslandStyle {
    const val BankDepth = 48.0
    const val ParkWidth = 34f
    const val PromenadeInset = 30f
    const val TreeWidth = 160f
    const val TreeSpacing = 128f
    const val PavingScale = .24f
}

/** Build the paving once: broad shared walks and a coast-following park loop.
 * Exact land and real object footprints cut the result; no parcel seam is a coast.
 * Narrow places naturally taper instead of moving a building or widening land. */
internal fun cityParkSurface(surface: Path, walks: List<Path>,
    buildings: List<CityBuildingProjection.Building>, decorations: List<CityDecorationDrawing>): Path {
    if(surface.isEmpty) return Path()
    fun stroke(path: Path, width: Float): Path {
        val outline=android.graphics.Path()
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style=Paint.Style.STROKE;strokeWidth=width
            strokeCap=Paint.Cap.ROUND;strokeJoin=Paint.Join.ROUND
        }.getFillPath(path.asAndroidPath(),outline)
        return outline.asComposePath()
    }
    // Erode the whole union, then stroke that inset contour. Concave/rotated parcel
    // extensions reshape the loop without a hard-coded island or per-parcel road.
    val inset=Path.combine(PathOperation.Difference,surface,
        stroke(surface,CityIslandStyle.PromenadeInset*2))
    val branches=Path()
    walks.forEach { branches.addPath(stroke(it,CityIslandStyle.ParkWidth)) }
    val paving=Path.combine(PathOperation.Union,branches,stroke(inset,CityIslandStyle.ParkWidth))
    val obstacles=Path()
    fun diamond(x:Float,y:Float,w:Float,h:Float) {
        obstacles.moveTo(x-w,y);obstacles.lineTo(x,y-h);obstacles.lineTo(x+w,y)
        obstacles.lineTo(x,y+h);obstacles.close()
    }
    buildings.forEach { diamond(it.foundation.x.toFloat(),it.foundation.y.toFloat(),104f,52f) }
    decorations.filterNot { it.road }.forEach { d ->
        val size=CityTerrainGeometry.decorationSize(d.source.copy.itemId) ?: return@forEach
        val w=((size.first+size.second)/2).toFloat()+4f
        diamond(d.anchor.x.toFloat(),d.anchor.y.toFloat(),w,w/2)
    }
    return Path.combine(PathOperation.Difference,
        Path.combine(PathOperation.Intersect,surface,paving),obstacles)
}
