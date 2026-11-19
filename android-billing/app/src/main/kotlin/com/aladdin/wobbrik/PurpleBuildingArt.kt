package com.aladdin.wobbrik

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
    return remember { ApprovedArt.purple(context.assets) }
}

internal fun loadPurpleBuildingArt(assets: AssetManager): Map<String, CosmeticSprite> {
    return run {
        val metadata = assets.open("building-purple/atlas.json").bufferedReader().use { JSONObject(it.readText()) }
        val image = assets.open("building-purple/atlas.png").use { requireNotNull(BitmapFactory.decodeStream(it)).asImageBitmap() }
        val cells = metadata.getJSONObject("cells")
        val primary = cells.keys().asSequence().associateWith { key ->
            val c = cells.getJSONArray(key)
            CosmeticSprite(image, IntOffset(c.getInt(0), c.getInt(1)), IntSize(c.getInt(2), c.getInt(3)))
        }
        val foundation = assets.open("building-purple/" + metadata.getString("foundation_image"))
            .use { requireNotNull(BitmapFactory.decodeStream(it)).asImageBitmap() }
        val foundationCells = metadata.getJSONObject("foundation_cells")
        val foreground = assets.open("building-purple/" + metadata.getString("foreground_image"))
            .use { requireNotNull(BitmapFactory.decodeStream(it)).asImageBitmap() }
        val foregroundCells = metadata.getJSONObject("foreground_cells")
        val groundFloor = assets.open("building-purple/" + metadata.getString("ground_floor_image"))
            .use { requireNotNull(BitmapFactory.decodeStream(it)).asImageBitmap() }
        val groundFloorCells = metadata.getJSONObject("ground_floor_cells")
        primary + foundationCells.keys().asSequence().associateWith { key ->
            val c = foundationCells.getJSONArray(key)
            CosmeticSprite(foundation,IntOffset(c.getInt(0),c.getInt(1)),IntSize(c.getInt(2),c.getInt(3)))
        } + foregroundCells.keys().asSequence().associateWith { key ->
            val c = foregroundCells.getJSONArray(key)
            CosmeticSprite(foreground,IntOffset(c.getInt(0),c.getInt(1)),IntSize(c.getInt(2),c.getInt(3)))
        } + groundFloorCells.keys().asSequence().associateWith { key ->
            val c = groundFloorCells.getJSONArray(key)
            CosmeticSprite(groundFloor,IntOffset(c.getInt(0),c.getInt(1)),IntSize(c.getInt(2),c.getInt(3)))
        }
    }
}

// A mixed selection retains this template. Complete other styles retain their current renderer.
internal fun usesPurpleTemplate(parts: Map<String, String>): Boolean =
    (2..6).none { style ->
        appbackend.wobbrik.game.FinishCosmetics.categories.all { category -> parts[category] == "base_${style}_$category" }
    }

/** Atlas-pixel anchors measured in the approved modular study; not UI tokens or accepted geometry. */
internal fun DrawScope.purpleBuilding(art: Map<String, CosmeticSprite>, catalog: Map<String, CosmeticSprite>,
    atlases: Map<String, GameAtlas>, parts: Map<String, String>, centers: List<Long>, marketMaterials: Boolean = false) {
    val representative = buildingPresentationCenters(centers)
    val count = minOf(representative.size, GameTokens.BuildingPreview.PurpleMaxFloors)
    val display = if (count < 2) representative else List(count) {
        representative[it * representative.lastIndex / (count - 1)]
    }
    if (display.isEmpty()) return
    val pitch = 180f
    val worldHeight = 440f + display.lastIndex * pitch + 260f
    val minOffset = display.min() / 1000f * 3.7f
    val maxOffset = display.max() / 1000f * 3.7f
    val worldWidth = 550f + maxOffset - minOffset
    val zoom = minOf(size.width / worldWidth, size.height / worldHeight)
    withTransform({
        translate((size.width-worldWidth*zoom)/2, (size.height-worldHeight*zoom)/2)
        scale(zoom, zoom, Offset.Zero)
    }) {
        val bottom = worldHeight - 180f
        // Keep the complete island inside the scene; its rear shrubs sit behind the masonry.
        val gardenX = 90f + display.first() / 1000f * 3.7f - minOffset - 90f
        val gardenY = bottom - 165f
        val garden = art.getValue("garden")
        sprite(garden, gardenX, gardenY, 550f, 290f)
        // Ink and warm bounce are sampled from the approved art, not a second UI palette.
        val lamp = art.getValue("lamp")
        val warm = Color(lamp.image.asAndroidBitmap().getPixel(lamp.origin.x + lamp.size.width / 2, lamp.origin.y + lamp.size.height / 2))
        fun glow(center: Offset, radius: Float, opacity: Float, color: Color = warm) {
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha=opacity),color.copy(alpha=0f)),center,radius),radius,center)
        }
        val baseX = gardenX + 150f
        // The shadow follows the masonry's actual ground footprint, rather than
        // a floating ellipse centred independently of its front/right faces.
        purpleContactShadow(listOf(
            Offset(baseX+16f,bottom-29f), Offset(baseX+240f,bottom+31f),
            Offset(baseX+354f,bottom-34f), Offset(baseX+130f,bottom-94f),
        ))
        // Low annexes share the same contact-shadow treatment as the core.
        // These contours follow their footing, not an enlarged floating ellipse.
        purpleContactShadow(listOf(
            Offset(baseX-60f,bottom-32f), Offset(baseX+53f,bottom-2f),
            Offset(baseX+94f,bottom-25f), Offset(baseX-19f,bottom-55f),
        ))
        purpleContactShadow(listOf(
            Offset(baseX+280f,bottom+40f), Offset(baseX+375f,bottom+7f),
            Offset(baseX+341f,bottom-18f), Offset(baseX+246f,bottom+15f),
        ))
        // The path continues from the lowest stair onto the same garden plane.
        sprite(art.getValue("path"),baseX+50f,bottom+26f,144f,84f)
        display.forEachIndexed { i, center ->
            val x = 150f + center / 1000f * 3.7f - minOffset
            val y = bottom - 330f - i*pitch
            // Sample masonry without the shell's bottom skirt. A single shared
            // cornice belongs to the lower storey's top, on both facade planes.
            val shell = art.getValue(if (i == 0) "ground" else "floor")
            val frontSource = if (i == 0) listOf(20f,77f,222f,101f,211f,280f,5f,251f)
                else listOf(16f,76f,241f,103f,220f,241f,34f,218f)
            val sideSource = if (i == 0) listOf(222f,101f,350f,40f,348f,220f,211f,280f)
                else listOf(241f,103f,353f,42f,333f,177f,220f,241f)
            val shellHeight = if (i == 0) 233f else pitch - if (marketMaterials) 6f else 14f
            val lowerX = if (i == 0) x else 150f + display[i-1] / 1000f * 3.7f - minOffset
            if (i == 0) purpleGroundShell(art,catalog,parts,x,y,atlases["teal"]?.roof,marketMaterials)
            else {
            purpleSurface(shell, frontSource, listOf(Offset(x+16f,y+68f),Offset(x+240f,y+128f),
                Offset(lowerX+240f,y+128f+shellHeight),Offset(lowerX+16f,y+68f+shellHeight)))
            purpleSurface(shell, sideSource, listOf(Offset(x+240f,y+128f),Offset(x+354f,y+63f),
                Offset(lowerX+354f,y+63f+shellHeight),Offset(lowerX+240f,y+128f+shellHeight)))
            }
            if (i < display.lastIndex) purpleJoint(art.getValue("floor"),x,y,marketMaterials)
            fun point(u:Float,v:Float,side:Boolean): Offset {
                val faceX = x + (lowerX-x) * (160f*v/shellHeight)
                return if(side) Offset(faceX+240f+114f*u,y+128f-65f*u+160f*v)
                    else Offset(faceX+16f+224f*u,y+68f+60f*u+160f*v)
            }
            fun plane(item:CosmeticSprite,side:Boolean,u:Float,v:Float,w:Float,h:Float) {
                purplePlane(item,listOf(point(u,v,side),point(u+w,v,side),point(u+w,v+h,side),point(u,v+h,side)))
            }
            if(i > 0 && (marketMaterials || parts["wall"] != "base_1_wall")) catalog[parts["wall"]]?.let { wall ->
                val sample = if (marketMaterials) marketWallSample(wall, parts["wall"]) else {
                    val origin = IntOffset(wall.origin.x+(wall.size.width*.14f).toInt(),wall.origin.y+(wall.size.height*.29f).toInt())
                    wall.copy(origin=origin,size=IntSize((wall.size.width*.64f).toInt(),(wall.size.height*.48f).toInt()))
                }
                // Material reaches the existing pilasters and shared cornices;
                // inset decals left exposed purple strips around other finishes.
                val frontInset = if (marketMaterials) 5f else 16f
                val sideInset = if (marketMaterials) 4f else 11f
                plane(sample,false,frontInset/224f,0f,(224f-frontInset*2)/224f,shellHeight/160f)
                plane(sample,true,sideInset/114f,0f,(114f-sideInset*2)/114f,shellHeight/160f)
            }
            if (marketMaterials && i > 0) {
                marketFacadeLight(listOf(point(0f,0f,false),point(1f,0f,false),
                    point(1f,shellHeight/160f,false),point(0f,shellHeight/160f,false)), false)
                marketFacadeLight(listOf(point(0f,0f,true),point(1f,0f,true),
                    point(1f,shellHeight/160f,true),point(0f,shellHeight/160f,true)), true)
            }
            fun selected(category:String,cell:String):CosmeticSprite? =
                if(parts[category] == "base_1_$category") art[cell] else catalog[parts[category]]
            fun window(side:Boolean) {
                var item = selected("window","arch") ?: return
                if(parts["window"] == "base_1_window") {
                    val face = Path().apply {
                        val corners = listOf(point(0f,0f,side),point(1f,0f,side),point(1f,1f,side),point(0f,1f,side))
                        moveTo(corners[0].x,corners[0].y)
                        corners.drop(1).forEach { lineTo(it.x,it.y) }; close()
                    }
                    clipPath(face) { glow(point(.5f,.48f,side),if(side) 68f else 93f,.22f) }
                }
                val corner = parts["window"] == "coins_window_2"
                if(corner) {
                    val half=item.size.width/2
                    item=item.copy(origin=IntOffset(item.origin.x+if(side) half else 0,item.origin.y),size=IntSize(half,item.size.height))
                }
                // Leave masonry around the selected frame; oversize frames hid the
                // brick courses and made every floor read as a repeated window tile.
                if (marketMaterials) plane(item,side,if(side).23f else .30f,.12f,if(side).54f else .38f,.70f)
                else plane(item,side,if(side).2f else .26f,.06f,if(side).6f else .45f,.82f)
            }
            if(i==0) {
                // Independent ground-facing stair: its top landing follows the
                // door threshold's slope; pots can occlude the masonry naturally.
                sprite(art.getValue("steps"),x-1f,bottom-87f,252f,160f)
                if(parts["lighting"] == "base_1_lighting") {
                    glow(point(.085f,.67f,false),78f,.42f)
                    glow(point(.905f,.67f,false),65f,.36f)
                    glow(Offset(x+127f,y+260f),90f,.18f)
                }
                selected("entrance","ground_entrance")?.let {
                    // The door is a facade decal; the separate garden owns the steps.
                    plane(purpleEntranceElevation(it,parts["entrance"]),false,.24f,.08f,.56f,1.28f)
                }
                selected("lighting","lamp")?.let {
                    plane(it,false,.02f,.43f,.13f,.48f)
                    plane(it,false,.84f,.43f,.13f,.48f)
                }
            } else window(false)
            if (i == 0) {
                selected("window","square")?.let { purpleGroundWindows(it,x,y,parts["window"] == "coins_window_2") }
            } else window(true)
            if(i>0 && i%2==1) {
                if (marketMaterials) {
                    // The rear of the selected template's balcony meets the sill;
                    // its rail and trailing flowers project beyond the front wall.
                    sprite(art.getValue("balcony"),x-63f,y+153f,252f,196f)
                } else sprite(art.getValue("balcony"),x-30f,y+164f,225f,175f)
            }
            if(i==display.lastIndex) {
                if(parts["roof"] == "base_1_roof") purpleRoof(art.getValue("roof"),x,y,marketMaterials)
                else {
                    // Teal's clean modular roof remains its own selected asset.
                    val selectedRoof = if(parts["roof"] == "base_2_roof") atlases["teal"]?.roof else catalog[parts["roof"]]
                    if(selectedRoof != null) {
                        if(parts["roof"] == "base_2_roof") purpleTealRoof(selectedRoof,x,y)
                        else purpleSelectedRoof(selectedRoof,parts["roof"],x,y)
                    }
                }
            }
        }
        // Independent transparent foliage overlaps the footing naturally. No
        // rectangular/diagonal garden redraw can cut through the stonework.
        sprite(art.getValue("front_left"),baseX-33f,bottom-29f,129f,82f)
        sprite(art.getValue("front_right"),baseX+289f,bottom-38f,106f,61f)
    }
}

/** One ledge per junction: its upper edge is also the upper wall's lower edge. */
private fun DrawScope.purpleJoint(shell: CosmeticSprite, x: Float, y: Float, marketMaterials: Boolean) {
    // A narrow lip keeps the floor joint legible without turning every storey
    // into a thick white frame. Wall anchors and floor pitch do not move.
    val lip = if (marketMaterials) 6f else 14f
    purpleSurface(shell,listOf(19f,57f,240f,84f,240f,101f,19f,74f),listOf(
        Offset(x+16f,y+68f-lip),Offset(x+240f,y+128f-lip),
        Offset(x+240f,y+128f),Offset(x+16f,y+68f),
    ))
    purpleSurface(shell,listOf(240f,84f,350f,25f,351f,40f,240f,101f),listOf(
        Offset(x+240f,y+128f-lip),Offset(x+354f,y+63f-lip),
        Offset(x+354f,y+63f),Offset(x+240f,y+128f),
    ))
}

private fun DrawScope.purpleContactShadow(footprint: List<Offset>) {
    drawIntoCanvas { canvas ->
        val path = android.graphics.Path().apply {
            moveTo(footprint.first().x,footprint.first().y)
            footprint.drop(1).forEach { lineTo(it.x,it.y) }
            close()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(170,
                (GameTokens.Background.red*255).toInt(),
                (GameTokens.Background.green*255).toInt(),
                (GameTokens.Background.blue*255).toInt())
            maskFilter = android.graphics.BlurMaskFilter(13f,android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawPath(path,paint)
    }
}

internal fun DrawScope.purplePlane(item:CosmeticSprite,points:List<Offset>) {
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

/** Copy a measured atlas quadrilateral into a continuous facade, excluding the
 * open top and redundant cornice. The source quad is clipped before drawing. */
internal fun DrawScope.purpleSurface(item: CosmeticSprite, source: List<Float>, target: List<Offset>) {
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val matrix = Matrix()
        val absolute = source.mapIndexed { i, v -> v + if (i % 2 == 0) item.origin.x else item.origin.y }.toFloatArray()
        if (!matrix.setPolyToPoly(absolute, 0, target.flatMap { listOf(it.x,it.y) }.toFloatArray(), 0, 4)) return@drawIntoCanvas
        val clip = android.graphics.Path().apply {
            moveTo(absolute[0],absolute[1])
            for (i in 1..3) lineTo(absolute[i*2],absolute[i*2+1])
            close()
        }
        native.save()
        native.concat(matrix)
        native.clipPath(clip)
        native.drawBitmap(item.image.asAndroidBitmap(),0f,0f,Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        native.restore()
    }
}

/** Three matching eave points put the roof on the very same facade planes. */
private fun DrawScope.purpleRoof(item: CosmeticSprite, x: Float, y: Float, marketMaterials: Boolean) {
    // Eaves project past the facade; the selected dormer/chimney remain in the
    // original roof sprite. This never adds another roof ID.
    val overhang = if (marketMaterials) 10f else 0f
    drawIntoCanvas { canvas ->
        val matrix = Matrix()
        matrix.setPolyToPoly(floatArrayOf(7f,194f,262f,263f,396f,140f),0,
            floatArrayOf(x+16f-overhang,y+68f,x+240f,y+128f+overhang/2,x+354f+overhang,y+63f),0,3)
        val native = canvas.nativeCanvas
        native.save()
        native.concat(matrix)
        native.drawBitmap(item.image.asAndroidBitmap(),
            Rect(item.origin.x,item.origin.y,item.origin.x+item.size.width,item.origin.y+item.size.height),
            RectF(0f,0f,item.size.width.toFloat(),item.size.height.toFloat()),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        native.restore()
    }
}
