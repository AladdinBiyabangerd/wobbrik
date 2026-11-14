package com.aladdin.wobbrik

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform

/** Game's modular shell, selected windows and roof; never the legacy baked wall panel. */
internal fun DrawScope.buildingFloor(atlases: Map<String, GameAtlas>, art: Map<String, CosmeticSprite>, parts: Map<String, String>,
    x: Float, base: Float, scale: Float, ground: Boolean, top: Boolean) {
    val atlas = atlases.getValue(gameAtlasStyle(parts))
    val width = 100f * scale
    val height = width * atlas.floor.size.height / atlas.floor.size.width
    drawModularFloor(atlas.floor, art, atlases, parts, x, base - height, width, ground)
    if (top) {
        val eave = base - height + width * atlas.depth / atlas.floor.size.width
        previewRoof(atlases, art, parts["roof"], x, eave, width)
    }
}

private fun DrawScope.previewRoof(atlases: Map<String, GameAtlas>, art: Map<String, CosmeticSprite>,
    id: String?, x: Float, eave: Float, width: Float) {
    val cap = when (id) {
        "base_1_roof" -> atlases["purple"]?.roof
        "base_2_roof" -> atlases["teal"]?.roof
        else -> null
    }
    if (cap != null) sprite(cap, x, eave - width * cap.size.height / cap.size.width,
        width, width * cap.size.height / cap.size.width)
    else drawModularRoof(art[id], id, x, eave, width)
}

/** Decorative owned-building presentation; accepted geometry remains untouched. */
internal fun buildingPresentationCenters(centers: List<Long>): List<Long> {
    val count = minOf(centers.size, GameTokens.BuildingPreview.MaxFloors)
    if (count < 2) return centers.toList()
    val extent = centers.max().toDouble() - centers.min().toDouble()
    val retouch = if (extent == 0.0) 1.0 else
        minOf(1.0, GameTokens.BuildingPreview.MaxOffsetTicks / extent)
    return List(count) { index ->
        val source = index.toLong() * centers.lastIndex / (count - 1)
        ((centers[source.toInt()].toDouble() - centers.first().toDouble()) * retouch).toLong()
    }
}

/** Picker retains its geometry; owned previews use the approved representative floors. */
internal fun DrawScope.buildingPreview(atlases: Map<String, GameAtlas>, art: Map<String, CosmeticSprite>,
    parts: Map<String, String>, centers: List<Long>, garden: CosmeticSprite? = null) {
    if (centers.isEmpty() || size.width <= 0 || size.height <= 0) return
    val atlas = atlases.getValue(gameAtlasStyle(parts))
    val displayCenters = if (garden == null) centers else buildingPresentationCenters(centers)
    val pitch = 100f * atlas.pitch / atlas.floor.size.width
    val gardenWidth = if (garden == null) 180f else GameTokens.BuildingPreview.GardenWidth
    val min = minOf(-gardenWidth / 2, displayCenters.min() / 1000f - 50f)
    val max = maxOf(gardenWidth / 2, displayCenters.max() / 1000f + 50f)
    val top = displayCenters.lastIndex * pitch + 121f
    val gardenScale = gardenWidth / 180f
    val below = if (garden == null) 0f else 55f * gardenScale
    val height = top + below
    val scale = minOf(size.width / (max - min), size.height / height, atlas.floor.size.width / 100f)
    val left = (size.width - (max - min) * scale) / 2
    val bottom = (size.height - height * scale) / 2 + top * scale
    withTransform({
        translate(left, bottom)
        scale(scale, scale, Offset.Zero)
    }) {
        if (garden != null) sprite(garden, -gardenWidth / 2 - min, -65f * gardenScale, gardenWidth, 120f * gardenScale)
        displayCenters.forEachIndexed { index, center ->
            val x = center / 1000f - 50f - min
            buildingFloor(atlases, art, parts, x, -index * pitch, 1f, index == 0, index == displayCenters.lastIndex)
        }
    }
}

/** Category thumbnails use the same selected layers as their building preview. */
internal fun DrawScope.partPreview(atlases: Map<String, GameAtlas>, art: Map<String, CosmeticSprite>,
    parts: Map<String, String>, category: String) {
    val atlas = atlases.getValue(gameAtlasStyle(parts))
    val width = minOf(size.width, size.height * atlas.floor.size.width / atlas.floor.size.height,
        atlas.floor.size.width.toFloat())
    val height = width * atlas.floor.size.height / atlas.floor.size.width
    val x = (size.width - width) / 2
    val y = (size.height - height) / 2
    if (category == "roof") previewRoof(atlases, art, parts["roof"], x, y + height, width)
    else drawModularFloor(atlas.floor, art, atlases, parts, x, y, width, category in listOf("entrance", "lighting"))
}
