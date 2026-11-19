package com.aladdin.wobbrik

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/** Measured art-plane coordinates, not UI dimensions or accepted building geometry.
 * The central footing/threshold stays on the existing ground anchor. Lower bays
 * dress that footing; garden, path, steps and foreground remain owned by the host.
 */
internal fun DrawScope.purpleGroundShell(
    art: Map<String, CosmeticSprite>, catalog: Map<String, CosmeticSprite>,
    parts: Map<String, String>, x: Float, y: Float, tealRoof: CosmeticSprite?, marketMaterials: Boolean = false,
) {
    val masonry = art.getValue("ground_masonry")
    val wallSource = listOf(435f,179f,822f,199f,817f,472f,435f,451f)
    val stoneSource = listOf(441f,540f,820f,560f,820f,627f,441f,608f)
    val sideStoneSource = listOf(903f,527f,1039f,451f,1039f,527f,903f,604f)
    val trimSource = listOf(447f,499f,815f,519f,815f,539f,447f,519f)
    val capSource = listOf(855f,250f,875f,251f,875f,275f,855f,274f)
    val columnSource = listOf(852f,203f,883f,205f,878f,491f,846f,489f)
    val selectedWall = if (!marketMaterials && parts["wall"] == "base_1_wall") null else catalog[parts["wall"]]
    val material = selectedWall?.let { wall ->
        if (marketMaterials) marketWallSample(wall, parts["wall"]) else wall.copy(
            origin = IntOffset(wall.origin.x+(wall.size.width*.14f).toInt(),wall.origin.y+(wall.size.height*.29f).toInt()),
            size = IntSize((wall.size.width*.64f).toInt(),(wall.size.height*.48f).toInt()),
        )
    }
    fun quad(vararg coordinates: Float) = coordinates.toList().chunked(2).map { Offset(x+it[0],y+it[1]) }
    fun wall(points: List<Offset>) {
        if (material == null) purpleSurface(masonry,wallSource,points) else purplePlane(material,points)
    }
    // Crop a material instead of squeezing an entire facade into a narrow
    // return. Atlas texel density then matches the adjoining core masonry.
    fun sample(source: List<Float>, left: Float, top: Float, right: Float, bottom: Float): List<Float> {
        fun point(u: Float, v: Float): List<Float> = (0..1).map { axis ->
            val upper = source[axis]*(1-u)+source[2+axis]*u
            val lower = source[6+axis]*(1-u)+source[4+axis]*u
            upper*(1-v)+lower*v
        }
        return point(left,top)+point(right,top)+point(right,bottom)+point(left,bottom)
    }
    fun wallPatch(points: List<Offset>, left: Float, top: Float, right: Float, bottom: Float) {
        val sprite = material ?: masonry
        val source = if (material == null) wallSource else listOf(
            0f,0f,sprite.size.width.toFloat(),0f,
            sprite.size.width.toFloat(),sprite.size.height.toFloat(),0f,sprite.size.height.toFloat())
        purpleSurface(sprite,sample(source,left,top,right,bottom),points)
    }
    fun stone(points: List<Offset>, side: Boolean = false) =
        purpleSurface(masonry,if (side) sideStoneSource else stoneSource,points)
    fun trim(points: List<Offset>) = purpleSurface(masonry,trimSource,points)
    fun column(points: List<Offset>) = purpleSurface(masonry,columnSource,points)

    // Continuous core: its upper edge is the existing storey junction, while
    // the stone plinth reaches the unchanged front/right ground-contact edge.
    wall(quad(16f,68f,240f,128f,240f,293f,16f,233f))
    wall(quad(240f,128f,354f,63f,354f,228f,240f,293f))
    stone(quad(16f,233f,240f,293f,240f,361f,16f,301f))
    stone(quad(240f,293f,354f,228f,354f,296f,240f,361f),side=true)
    trim(quad(16f,227f,240f,287f,240f,301f,16f,241f))
    trim(quad(240f,287f,354f,222f,354f,236f,240f,301f))
    column(quad(16f,68f,32f,72f,32f,237f,16f,233f))
    column(quad(222f,123f,240f,128f,240f,293f,222f,288f))
    column(quad(240f,128f,251f,122f,251f,287f,240f,293f))
    column(quad(340f,71f,354f,63f,354f,228f,340f,236f))

    // Wider low bay with a continuous stone course and a closed cream cap.
    wall(quad(-60f,151f,53f,181f,53f,260f,-60f,230f))
    wall(quad(53f,181f,94f,158f,94f,237f,53f,260f))
    stone(quad(-60f,230f,53f,260f,53f,328f,-60f,298f))
    stone(quad(53f,260f,94f,237f,94f,305f,53f,328f),side=true)
    purpleSurface(masonry,capSource,quad(-65f,139f,-19f,113f,99f,145f,53f,171f))
    trim(quad(-65f,139f,53f,171f,53f,185f,-65f,153f))
    trim(quad(53f,171f,99f,145f,99f,159f,53f,185f))
    trim(quad(-63f,226f,53f,256f,53f,270f,-63f,240f))
    trim(quad(53f,256f,97f,231f,97f,245f,53f,270f))
    column(quad(-60f,153f,-50f,156f,-50f,233f,-60f,230f))
    column(quad(43f,182f,53f,185f,53f,260f,43f,257f))

    // Shared side plane keeps the selected window between eave and plinth.
    // Shallow annex facade matches the approved study; its outer footing sits
    // inside the garden instead of extending past the island silhouette.
    // Close the annex's exposed return into the core. Previously only its
    // window-facing plane was drawn: the near edge floated in front of the
    // core's side plinth. This return shares that edge all the way to ground.
    // The return starts at the core corner; no second cream seam is left
    // between the two volumes. Belt endpoints meet the core belt exactly.
    wallPatch(quad(240f,178f,280f,223f,280f,306f,240f,293f),.82f,.30f,1f,1f)
    purpleSurface(masonry,sample(stoneSource,.82f,0f,1f,1f),
        quad(240f,293f,280f,306f,280f,370f,240f,361f))
    purpleSurface(masonry,sample(trimSource,.82f,0f,1f,1f),
        quad(240f,287f,280f,300f,280f,314f,240f,301f))
    wallPatch(groundRightBay(x,y,0f,0f,1f,83f),0f,.50f,.42f,1f)
    stone(groundRightBay(x,y,0f,83f,1f,147f),side=true)
    trim(groundRightBay(x,y,0f,77f,1f,91f))
    column(groundRightBay(x,y,0f,0f,.085f,83f))
    column(groundRightBay(x,y,.91f,0f,1f,83f))
    if (parts["roof"] == "base_1_roof") {
        // Front eave follows the same facade edge instead of crossing the window.
        purpleGroundRoof(art.getValue("ground_side_roof"),
            quad(240f,178f,354f,113f,382f,190f,277f,226f))
    } else (if(parts["roof"] == "base_2_roof") tealRoof else catalog[parts["roof"]])?.let { roof ->
        purpleSelectedAnnexRoof(roof,parts["roof"],quad(240f,178f,354f,113f,382f,190f,277f,226f))
    }
    // The rear roof edge follows the core side facade (114 / -65), not the
    // shallower annex front. A narrow existing stone flashing closes the joint
    // against the wall and overlaps the tile edge without a floating gap.
    trim(quad(240f,172f,354f,107f,354f,115f,240f,180f))
}

internal fun DrawScope.purpleGroundWindows(window: CosmeticSprite, x: Float, y: Float, corner: Boolean) {
    // Ground windows belong to the lower bays; upper-storey windows are unchanged.
    fun face(side: Boolean): CosmeticSprite {
        if (!corner) return window
        val half = window.size.width / 2
        return window.copy(origin=IntOffset(window.origin.x+if(side) half else 0,window.origin.y),
            size=IntSize(half,window.size.height))
    }
    purplePlane(face(false),listOf(Offset(x-29f,y+171f),Offset(x+20f,y+184f),
        Offset(x+20f,y+245f),Offset(x-29f,y+232f)))
    purplePlane(face(true),groundRightBay(x,y,.25f,7f,.75f,72f))
}

/** Measured art-plane coordinates; no UI or accepted-geometry dimensions. */
private fun groundRightBay(x: Float, y: Float, left: Float, top: Float, right: Float, bottom: Float): List<Offset> {
    fun point(u: Float, v: Float) = Offset(x+280f+95f*u,y+223f-33f*u+v)
    return listOf(point(left,top),point(right,top),point(right,bottom),point(left,bottom))
}

private fun DrawScope.purpleGroundRoof(roof: CosmeticSprite, points: List<Offset>) {
    // The roof sprite's transparent margins are excluded by measured source quad.
    purpleSurface(roof,listOf(9f,110f,208f,12f,380f,136f,118f,237f),points)
}
