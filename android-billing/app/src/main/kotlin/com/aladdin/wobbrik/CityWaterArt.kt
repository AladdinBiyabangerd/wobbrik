package com.aladdin.wobbrik

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*

/** Immutable native brushes, prepared with the accepted shoreline, never per ambient frame. */
internal data class CityWaterReflection(val midpoint: Offset, val blue: Brush, val warm: Brush?)
internal fun cityWaterReflections(surface: Path, shore: CityShore): List<CityWaterReflection> {
    val bounds=surface.getBounds()
    val region=android.graphics.Region().apply { setPath(surface.asAndroidPath(),android.graphics.Region(
        kotlin.math.floor(bounds.left).toInt(),kotlin.math.floor(bounds.top).toInt(),
        kotlin.math.ceil(bounds.right).toInt(),kotlin.math.ceil(bounds.bottom).toInt())) }
    return shore.strips.mapIndexedNotNull { index, (a,b) ->
        val mid=(a+b)/2f
        // Contour direction is not stable across boolean unions or disconnected parcels.
        if(region.contains(mid.x.toInt(),(mid.y+3f).toInt())) null else {
            val center=Offset(mid.x,mid.y+CityIslandStyle.BankDepth.toFloat()+24f)
            CityWaterReflection(mid,
                Brush.radialGradient(listOf(GameTokens.MobileHome.RimLight.copy(alpha=.12f),
                    GameTokens.MobileHome.RimLight.copy(alpha=0f)),center,62f),
                if(index%3!=0) null else Brush.radialGradient(listOf(GameTokens.MobileHome.Gold.copy(alpha=.19f),
                    GameTokens.MobileHome.Gold.copy(alpha=0f)),center,34f))
        }
    }
}

/** Static shoreline reflections in world space; no new lights, clock or bitmap layer. */
internal fun DrawScope.cityWaterReflections(surface: Path, reflections: List<CityWaterReflection>) {
    clipPath(surface, ClipOp.Difference) {
        reflections.forEach { reflection ->
            val mid=reflection.midpoint
            val center=Offset(mid.x,mid.y+CityIslandStyle.BankDepth.toFloat()+24f)
            drawOval(reflection.blue,Offset(center.x-62f,center.y-20f),Size(124f,40f))
            reflection.warm?.let { warm ->
                drawOval(warm,Offset(center.x-34f,center.y-26f),Size(68f,52f))
                repeat(7) { row ->
                    val spread=5f+row*1.4f
                    val drift=if(row%2==0) 2f else -2f
                    val y=mid.y+CityIslandStyle.BankDepth.toFloat()+8f+row*5f
                    drawLine(GameTokens.MobileHome.GoldLight.copy(alpha=(.65f-row*.075f)),
                        Offset(mid.x-spread+drift,y),Offset(mid.x+spread+drift,y),1.6f,StrokeCap.Round)
                }
            }
            drawArc(GameTokens.InventoryCollection.Metadata.copy(alpha=.28f), 12f,145f,false,
                Offset(mid.x-24f,mid.y+CityIslandStyle.BankDepth.toFloat()+12f),Size(48f,10f),style=Stroke(1.2f))
        }
    }
}
