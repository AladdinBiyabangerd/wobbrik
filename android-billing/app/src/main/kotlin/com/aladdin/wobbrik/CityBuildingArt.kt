package com.aladdin.wobbrik

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*

/** City-only illustration. Accepted centres, floor count and selected part IDs stay authoritative.
 * The game and inventory renderers retain their own approved presentation. */
internal fun DrawScope.cityBuildingFloor(atlases: Map<String, GameAtlas>, art: Map<String, CosmeticSprite>,
    illustration: CityIllustrationArt?, parts: Map<String, String>, x: Float, base: Float, scale: Float,
    ground: Boolean, top: Boolean, windowAlpha: Float = 1f) {
    val width = CityBuildingProjection.ArtWidth.toFloat() * scale
    val height = width * 288f / 464f
    if (illustration == null) {
        val atlas = atlases.getValue(gameAtlasStyle(parts))
        drawModularFloor(atlas.floor, art, atlases, parts, x, base-height, width, ground, windowAlpha)
    } else withTransform({ translate(x,base-height); scale(width/464f,width/464f,androidx.compose.ui.geometry.Offset.Zero) }) {
        fun point(u: Float, v: Float, side: Boolean): Pair<Float,Float> = if (side)
            (340f+112f*u) to (105f-70f*u+176f*v)
        else (12f+328f*u) to (89f+16f*u+176f*v)
        fun plane(item: CosmeticSprite?, side: Boolean, u: Float=0f, v: Float=0f, w: Float=1f, h: Float=1f,
            start: Float=0f, end: Float=1f, alpha: Float=1f, material: Boolean=false) {
            if(item == null) return
            val src = if(material) RectF(item.size.width*.18f,item.size.height*.3f,item.size.width*.74f,item.size.height*.72f)
                else RectF(item.size.width*start,0f,item.size.width*end,item.size.height.toFloat())
            val quad = listOf(point(u,v,side),point(u+w,v,side),point(u+w,v+h,side),point(u,v+h,side))
                .flatMap { listOf(it.first,it.second) }.toFloatArray()
            drawIntoCanvas { canvas ->
                val matrix=Matrix().apply { setPolyToPoly(floatArrayOf(src.left,src.top,src.right,src.top,src.right,src.bottom,src.left,src.bottom),0,quad,0,4) }
                val native=canvas.nativeCanvas
                native.save();native.concat(matrix)
                native.drawBitmap(item.image.asAndroidBitmap(),Rect(item.origin.x+src.left.toInt(),item.origin.y+src.top.toInt(),
                    item.origin.x+src.right.toInt(),item.origin.y+src.bottom.toInt()),src,
                    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply { this.alpha=(alpha*255).toInt().coerceIn(0,255) })
                native.restore()
            }
        }
        val id=parts["wall"]
        val wall=when(id) {
            "base_2_wall" -> illustration.brick
            "base_3_wall" -> illustration.plaster
            else -> art[id]
        }
        val sampled=id !in setOf("base_2_wall","base_3_wall")
        fun face(side:Boolean,color:Color,top:Float,bottom:Float) {
            val path=Path().apply {
                listOf(point(0f,top,side),point(1f,top,side),point(1f,bottom,side),point(0f,bottom,side))
                    .forEachIndexed { i,p -> if(i==0) moveTo(p.first,p.second) else lineTo(p.first,p.second) };close()
            }
            drawPath(path,color)
        }
        // Generated materials retain painterly alpha; an opaque underpaint prevents facade gaps.
        face(false,GameTokens.Surface,0f,1f);face(true,GameTokens.Surface,0f,1f)
        plane(wall,false,material=sampled);plane(wall,true,material=sampled)
        face(true,GameTokens.Background.copy(alpha=.20f),0f,1f)
        for(side in listOf(false,true)) {
            face(side,GameTokens.Text.copy(alpha=.55f),.015f,.045f)
            face(side,GameTokens.Background.copy(alpha=.35f),.95f,.98f)
            face(side,GameTokens.Muted.copy(alpha=.7f),.98f,1f)
        }
        fun selected(category:String):CosmeticSprite? {
            val part=parts[category]
            val approved=when {
                part?.startsWith("base_1_")==true && category!="window" -> atlases["purple"]
                part?.startsWith("base_2_")==true && category!="window" -> atlases["teal"]
                else -> null
            }
            return approved?.cells?.get(category) ?: art[part]
        }
        if(ground) {
            plane(selected("entrance"),false,.30f,.03f,.43f,.97f)
            plane(selected("lighting"),false,.08f,.17f,.14f,.56f)
        } else if(parts["window"]=="coins_window_2") {
            plane(selected("window"),false,.65f,.15f,.35f,.70f,0f,.5f,windowAlpha)
        } else plane(selected("window"),false,.34f,.12f,.38f,.74f,alpha=windowAlpha)
        if(parts["window"]=="coins_window_2")
            plane(selected("window"),true,0f,.15f,.65f,.70f,.5f,1f,windowAlpha)
        else plane(selected("window"),true,.24f,.12f,.54f,.74f,alpha=windowAlpha)
    }
    if(top) {
        val id=parts["roof"]
        drawModularRoof(art[id],id,x,base-height+width*112f/464f,width)
    }
}
