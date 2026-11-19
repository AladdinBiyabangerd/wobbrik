package com.aladdin.wobbrik

import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/** APK illustration only. Nothing here adds a catalog item, owned tree or traversable road. */
internal data class CityIllustrationArt(val rocks: CosmeticSprite, val flowers: CosmeticSprite,
    val tree: CosmeticSprite, val brick: CosmeticSprite, val plaster: CosmeticSprite, val paving: Brush)

internal fun loadCityIllustrationArt(context: android.content.Context): CityIllustrationArt {
    val image = context.assets.open("city/collection-illustration-v1.png").use {
        requireNotNull(BitmapFactory.decodeStream(it)).asImageBitmap()
    }
    fun cell(x:Int,y:Int,w:Int,h:Int) = CosmeticSprite(image,IntOffset(x,y),IntSize(w,h))
    // Exact source rectangles are checked against the shipped alpha sheet in host QA.
    val paving = android.graphics.Bitmap.createBitmap(image.asAndroidBitmap(),1070,550,400,400).asImageBitmap()
    return CityIllustrationArt(cell(35,170,450,190),cell(550,110,440,300),cell(1050,10,450,480),
        cell(60,550,400,400),cell(565,550,400,400),
        ShaderBrush(ImageShader(paving,TileMode.Mirror,TileMode.Mirror).apply {
            setLocalMatrix(android.graphics.Matrix().apply { setScale(CityIslandStyle.PavingScale,CityIslandStyle.PavingScale/2) })
        }))
}

/** Static garden geometry is prepared once per accepted scene, never on an ambient frame. */
internal data class CityIllustratedGarden(val walks: List<Path>,val beds: List<Offset>, val trees: List<CityGardenTree>, val water: List<CityWaterReflection>,
    val park: Path, val grassShade: List<Pair<Offset, Brush>>, val prepared: Boolean = true)
internal val CitySceneryPrepared = androidx.compose.ui.semantics.SemanticsPropertyKey<Boolean>("CitySceneryPrepared")
internal data class CityGardenTree(val anchor: Offset)
internal fun cityIllustratedGarden(buildings: List<CityBuildingProjection.Building>, shore: CityShore, decorations: List<CityDecorationDrawing>, surface: Path): CityIllustratedGarden {
    val walks=cityParkPaths(surface, buildings, decorations).paths
    val park=cityParkSurface(surface,walks,buildings,decorations)
    // Spatial buckets keep landscape preparation linear in scene size; no shore × city scan.
    fun bucket(x:Double,y:Double)=kotlin.math.floor(x/128).toLong() to kotlin.math.floor(y/128).toLong()
    val occupied=HashMap<Pair<Long,Long>,MutableList<Offset>>()
    fun occupy(x:Double,y:Double) { occupied.getOrPut(bucket(x,y)) { mutableListOf() }.add(Offset(x.toFloat(),y.toFloat())) }
    buildings.forEach { occupy(it.foundation.x,it.foundation.y) }
    decorations.forEach { occupy(it.anchor.x,it.anchor.y) }
    // Interior planting follows accepted land, including future extensions. Sample
    // at most 64×64 cells even for sparse extents; no per-building land scan.
    val bounds=surface.getBounds()
    fun region(path:Path)=android.graphics.Region().apply {
        setPath(path.asAndroidPath(),android.graphics.Region(kotlin.math.floor(bounds.left).toInt(),
            kotlin.math.floor(bounds.top).toInt(),kotlin.math.ceil(bounds.right).toInt(),kotlin.math.ceil(bounds.bottom).toInt()))
    }
    val land=region(surface);val paving=region(park)
    val step=maxOf(CityIslandStyle.TreeSpacing,bounds.width/64f,bounds.height/64f)
    val interior=sequence {
        var y=bounds.top+step/2
        while(y<bounds.bottom) {
            var x=bounds.left+step/2
            while(x<bounds.right) { yield(Offset(x,y));x+=step }
            y+=step
        }
    }
    val candidates=shore.tufts.asSequence().filterIndexed { i,_ -> i%3==0 }+interior
    val trees=candidates.filter { p ->
        val margin=CityIslandStyle.ParkWidth/4
        val rooted=listOf(Offset.Zero,Offset(-margin,0f),Offset(margin,0f),Offset(0f,-margin),Offset(0f,margin)).all { d ->
            land.contains((p.x+d.x).toInt(),(p.y+d.y).toInt()) && !paving.contains((p.x+d.x).toInt(),(p.y+d.y).toInt())
        }
        if(!rooted) false else {
            val (x,y)=bucket(p.x.toDouble(),p.y.toDouble())
            (-1L..1L).all { dx -> (-1L..1L).all { dy -> occupied[x+dx to y+dy].orEmpty().none {
                kotlin.math.abs(it.x-p.x)<115 && kotlin.math.abs(it.y-p.y)<75 } } }
        }
    }.take(128).map {
        occupy(it.x.toDouble(),it.y.toDouble())
        CityGardenTree(it)
    }.toList()
    return CityIllustratedGarden(walks,buildings.flatMap { b -> listOf(
        Offset(b.foundation.x.toFloat()-65f,b.foundation.y.toFloat()+20f),
        Offset(b.foundation.x.toFloat()+64f,b.foundation.y.toFloat()+12f)) }, trees, cityWaterReflections(surface, shore), park,
        shore.tufts.filterIndexed { index,_ -> index%4==0 }.take(256).map { p ->
            p to Brush.radialGradient(listOf(GameTokens.Background.copy(alpha=.3f),
                GameTokens.Background.copy(alpha=0f)),p,85f)
        })
}

internal fun DrawScope.cityIllustratedGarden(surface:Path,shore:CityShore,garden:CityIllustratedGarden,
    view:CityViewport,art:CityIllustrationArt) {
    withTransform({ translate(view.x.toFloat(),view.y.toFloat());scale(view.scale.toFloat(),view.scale.toFloat(),Offset.Zero) }) {
        // Native water light is clipped away from owned land and its cliff.
        cityWaterReflections(surface, garden.water)
        clipPath(surface) {
            garden.grassShade.forEach { (p,brush) ->
                drawCircle(brush,85f,p)
            }
            drawPath(garden.park,GameTokens.Background.copy(alpha=.4f),style=Stroke(4f))
            drawPath(garden.park,art.paving)
            garden.beds.forEach { p -> sprite(art.flowers,p.x-20f,p.y-19f,40f,27f) }
        }
    }
}

internal fun DrawScope.cityIllustratedShore(surface:Path,cliff:Path,shore:CityShore,view:CityViewport,art:CityIllustrationArt) {
    withTransform({ translate(view.x.toFloat(),view.y.toFloat());scale(view.scale.toFloat(),view.scale.toFloat(),Offset.Zero) }) {
        clipPath(cliff) {
            // A large alpha boulder cluster every two shore segments, instead
            // of tiny repeated gravel. The silhouette stays within the exact bank.
            shore.strips.forEachIndexed { index,(a,b) ->
                if(index%2!=0) return@forEachIndexed
                val width=(b-a).getDistance()+112f
                val center=(a+b)/2f
                val h=width*.45f
                val top=center.y-18f
                if(index%4==0) sprite(art.rocks,center.x-width/2f,top,width,h)
                else withTransform({ scale(-1f,1f,center) }) {
                    sprite(art.rocks,center.x-width/2f,top,width,h)
                }
            }
        }
        clipPath(surface) {
            shore.tufts.forEachIndexed { index,p ->
                if(index%2==0) sprite(art.flowers,p.x-16f,p.y-17f,32f,22f)
            }
        }
    }
}

/** Incidental landscape foliage, like the flower beds; never counted as placed decor. */
internal fun DrawScope.cityGardenTree(tree:CityGardenTree, view:CityViewport, art:CityIllustrationArt) {
    val width=CityIslandStyle.TreeWidth*view.scale.toFloat()
    val height=width*art.tree.size.height/art.tree.size.width
    sprite(art.tree,(view.x+tree.anchor.x*view.scale).toFloat()-width/2,
        (view.y+tree.anchor.y*view.scale).toFloat()-height,width,height)
}

/** Optional scenery preparation never stalls the accepted buildings or runs grid work on UI. */
@androidx.compose.runtime.Composable
internal fun rememberCityIllustratedGarden(required: Boolean, buildings: List<CityBuildingProjection.Building>,
    shore: CityShore, decorations: List<CityDecorationDrawing>, surface: Path): CityIllustratedGarden {
    val result=androidx.compose.runtime.remember(required,buildings,shore,decorations,surface) {
        androidx.compose.runtime.mutableStateOf(CityIllustratedGarden(emptyList(),emptyList(),emptyList(),emptyList(),Path(),emptyList(),prepared=false))
    }
    androidx.compose.runtime.LaunchedEffect(result) {
        if(required) result.value=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            cityIllustratedGarden(buildings,shore,decorations,surface)
        }
    }
    return result.value
}
