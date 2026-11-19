package com.aladdin.wobbrik

import android.graphics.Region
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import kotlin.math.*

/** Bounded presentation routing, not placement validation or a domain road network. */
internal data class CityParkPaths(val paths: List<Path>, val cells: Int, val edges: Int)
internal fun cityParkPaths(surface: Path, buildings: List<CityBuildingProjection.Building>,
    decorations: List<CityDecorationDrawing>): CityParkPaths {
    if (surface.isEmpty || buildings.isEmpty()) return CityParkPaths(emptyList(), 0, 0)
    val bounds = surface.getBounds()
    // Keep both memory and preparation bounded even for widely separated parcels.
    var step = 8f
    fun columns() = ceil(bounds.width / step).toInt().coerceAtLeast(1)
    fun rows() = ceil(bounds.height / step).toInt().coerceAtLeast(1)
    while (columns().toLong() * rows() > 65536) step *= 2
    // Do not let a coarse cell jump over a small footprint or gap. Extremely sparse
    // extents omit incidental paving rather than inventing a bridge through geometry.
    if (step > 16f) return CityParkPaths(emptyList(), 0, 0)
    val width = columns(); val height = rows(); val count = width * height
    val region = Region().apply { setPath(surface.asAndroidPath(), Region(
        floor(bounds.left).toInt(), floor(bounds.top).toInt(), ceil(bounds.right).toInt(), ceil(bounds.bottom).toInt())) }
    fun point(i: Int) = Offset(bounds.left + (i % width + .5f) * step, bounds.top + (i / width + .5f) * step)
    val open = BooleanArray(count) { i ->
        val p = point(i)
        // Entire paving width remains on land; smooth corners stay within this clearance.
        var inside = true
        for (dx in -1..1) for (dy in -1..1) {
            if (!region.contains((p.x+dx*12f).toInt(), (p.y+dy*12f).toInt())) inside = false
        }
        inside
    }
    fun exclude(left: Float, top: Float, right: Float, bottom: Float, contains: (Offset) -> Boolean) {
        val x0 = floor((left-bounds.left)/step).toInt().coerceIn(0,width-1)
        val x1 = ceil((right-bounds.left)/step).toInt().coerceIn(0,width-1)
        val y0 = floor((top-bounds.top)/step).toInt().coerceIn(0,height-1)
        val y1 = ceil((bottom-bounds.top)/step).toInt().coerceIn(0,height-1)
        for (y in y0..y1) for (x in x0..x1) if (contains(point(y*width+x))) open[y*width+x] = false
    }
    buildings.forEach { b ->
        val x=b.foundation.x.toFloat(); val y=b.foundation.y.toFloat()
        exclude(x-120,y-60,x+120,y+60) { abs(it.x-x)/120f + abs(it.y-y)/60f < 1f }
    }
    decorations.filterNot { it.road }.forEach { d ->
        val size = CityTerrainGeometry.decorationSize(d.source.copy.itemId) ?: return@forEach
        val x=d.anchor.x.toFloat(); val y=d.anchor.y.toFloat()
        val w=((size.first+size.second)/2+20).toFloat(); val h=w/2+10
        exclude(x-w,y-h,x+w,y+h) { abs(it.x-x)/w + abs(it.y-y)/h < 1f }
    }
    val parent=IntArray(count) { -1 }; val linked=BooleanArray(count); val queue=IntArray(count)
    val paths=mutableListOf<Path>(); var edges=0
    fun neighbours(i:Int): List<Int> = listOfNotNull(
        if(i%width>0) i-1 else null, if(i%width<width-1) i+1 else null,
        if(i>=width) i-width else null, if(i+width<count) i+width else null)
    // The first entrance in each connected component roots one shared shortest-path tree.
    // Each grid edge is emitted at most once: no all-pairs links or per-building full search.
    buildings.forEach { b ->
        val door=Offset(b.foundation.x.toFloat(), b.foundation.y.toFloat()+60f)
        val cx=floor((door.x-bounds.left)/step).toInt(); val cy=floor((door.y-bounds.top)/step).toInt()
        var start=-1; var distance=Float.POSITIVE_INFINITY
        for(y in max(0,cy-2)..min(height-1,cy+2)) for(x in max(0,cx-2)..min(width-1,cx+2)) {
            val i=y*width+x; val d=(point(i)-door).getDistance()
            if(open[i] && d<distance) { start=i; distance=d }
        }
        if(start<0 || distance>step*2) return@forEach
        if(parent[start]<0) {
            var head=0; var tail=0; queue[tail++]=start; parent[start]=start
            while(head<tail) {
                val i=queue[head++]
                neighbours(i).forEach { n -> if(open[n] && parent[n]<0) { parent[n]=i; queue[tail++]=n } }
            }
        }
        val points=mutableListOf<Offset>(); var i=start
        points+=point(i)
        while(!linked[i]) {
            linked[i]=true
            val next=parent[i]
            if(next==i) break
            i=next; points+=point(i); edges++
        }
        // A disconnected single entrance still has a short path at its real foundation.
        val path=Path().apply {
            moveTo(door.x, b.foundation.y.toFloat()+50f)
            points.forEachIndexed { index,p ->
                val next=points.getOrNull(index+1)
                if(next==null) lineTo(p.x,p.y)
                else { val mid=(p+next)/2f; quadraticTo(p.x,p.y,mid.x,mid.y) }
            }
        }
        paths+=path
    }
    return CityParkPaths(paths,count,edges)
}
