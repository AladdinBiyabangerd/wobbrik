package com.aladdin.wobbrik

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.json.JSONObject

/** Approved preview template, using existing owned IDs, never a second catalog. */
@Composable
internal fun rememberPurpleBuildingArt(): Map<String, CosmeticSprite> {
    val context = LocalContext.current
    return remember(context) {
        val metadata = context.assets.open("building-purple/atlas.json").bufferedReader().use { JSONObject(it.readText()) }
        val image = context.assets.open("building-purple/atlas.png").use { requireNotNull(BitmapFactory.decodeStream(it)).asImageBitmap() }
        val cells = metadata.getJSONObject("cells")
        cells.keys().asSequence().associateWith { key ->
            val c = cells.getJSONArray(key)
            CosmeticSprite(image, IntOffset(c.getInt(0), c.getInt(1)), IntSize(c.getInt(2), c.getInt(3)))
        }
    }
}

// A mixed selection retains this template. Complete other styles retain their current renderer.
internal fun usesPurpleTemplate(parts: Map<String, String>) = parts.values.any { it.startsWith("base_1_") }

/** Atlas-pixel anchors measured in the approved modular study; not UI tokens or accepted geometry. */
internal fun DrawScope.purpleBuilding(art: Map<String, CosmeticSprite>, catalog: Map<String, CosmeticSprite>,
    atlases: Map<String, GameAtlas>, parts: Map<String, String>, centers: List<Long>) {
    val representative = buildingPresentationCenters(centers)
    val count = minOf(representative.size, GameTokens.BuildingPreview.PurpleMaxFloors)
    val display = if (count < 2) representative else List(count) {
        representative[it * representative.lastIndex / (count - 1)]
    }
    if (display.isEmpty()) return
    val pitch = 180f
    val worldHeight = 340f + display.lastIndex * pitch + 170f
    val minOffset = display.min() / 1000f * 3.7f
    val maxOffset = display.max() / 1000f * 3.7f
    val worldWidth = 550f + maxOffset - minOffset
    val zoom = minOf(size.width / worldWidth, size.height / worldHeight)
    withTransform({
        translate((size.width-worldWidth*zoom)/2, (size.height-worldHeight*zoom)/2)
        scale(zoom, zoom, Offset.Zero)
    }) {
        val bottom = worldHeight - 90f
        sprite(art.getValue("garden"), (worldWidth-550f)/2, bottom-100f, 550f, 268f)
        display.forEachIndexed { i, center ->
            val x = 90f + center / 1000f * 3.7f - minOffset
            val y = bottom - 268f - i*pitch
            sprite(art.getValue(if (i == 0) "ground" else "floor"), x,y,370f,if(i==0)287f else 268f)
            fun point(u:Float,v:Float,side:Boolean) = if(side)
                Offset(x+240f+104f*u,y+115f-65f*u+135f*v)
                else Offset(x+16f+224f*u,y+86f+29f*u+135f*v)
            fun plane(item:CosmeticSprite,side:Boolean,u:Float,v:Float,w:Float,h:Float) {
                purplePlane(item,listOf(point(u,v,side),point(u+w,v,side),point(u+w,v+h,side),point(u,v+h,side)))
            }
            if(parts["wall"] != "base_1_wall") catalog[parts["wall"]]?.let { wall ->
                val origin = IntOffset(wall.origin.x+(wall.size.width*.14f).toInt(),wall.origin.y+(wall.size.height*.29f).toInt())
                val sample = wall.copy(origin=origin,size=IntSize((wall.size.width*.64f).toInt(),(wall.size.height*.48f).toInt()))
                plane(sample,false,.13f,.05f,.74f,if(i==0) .68f else .9f)
                plane(sample,true,.08f,.05f,.82f,if(i==0) .68f else .9f)
            }
            fun selected(category:String,cell:String):CosmeticSprite? =
                if(parts[category] == "base_1_$category") art[cell] else catalog[parts[category]]
            fun window(side:Boolean) {
                var item = selected("window","arch") ?: return
                val corner = parts["window"] == "coins_window_2"
                if(corner) {
                    val half=item.size.width/2
                    item=item.copy(origin=IntOffset(item.origin.x+if(side) half else 0,item.origin.y),size=IntSize(half,item.size.height))
                }
                plane(item,side,if(side).2f else .26f,.06f,if(side).6f else .45f,.82f)
            }
            if(i==0) {
                selected("entrance","entrance")?.let { sprite(it,x+35f,y+95f,200f,195f) }
                selected("lighting","lamp")?.let {
                    sprite(it,x+10f,y+115f,35f,53f)
                    sprite(it,x+213f,y+130f,27f,41f)
                }
            } else window(false)
            window(true)
            if(i>0 && i%2==1) sprite(art.getValue("balcony"),x-30f,y+164f,225f,166f)
            if(i==display.lastIndex) {
                if(parts["roof"] == "base_1_roof") sprite(art.getValue("roof"),x-19f,y-137f,410f,270f)
                else {
                    val cap = if(parts["roof"] == "base_2_roof") atlases["teal"]?.roof else null
                    if(cap!=null) {
                        val height = 410f * cap.size.height / cap.size.width
                        sprite(cap,x-19f,y+115f-height,410f,height)
                    }
                    else drawModularRoof(catalog[parts["roof"]],parts["roof"],x-19f,y+115f,410f)
                }
            }
        }
    }
}

private fun DrawScope.purplePlane(item:CosmeticSprite,points:List<Offset>) {
 drawIntoCanvas { canvas ->
  val w=item.size.width.toFloat();val h=item.size.height.toFloat()
  val matrix=Matrix()
  matrix.setPolyToPoly(floatArrayOf(0f,0f,w,0f,w,h,0f,h),0,points.flatMap {listOf(it.x,it.y)}.toFloatArray(),0,4)
  val native=canvas.nativeCanvas
  native.save();native.concat(matrix)
  native.drawBitmap(item.image.asAndroidBitmap(),Rect(item.origin.x,item.origin.y,item.origin.x+item.size.width,item.origin.y+item.size.height),
   RectF(0f,0f,w,h),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
  native.restore()
 }
}
