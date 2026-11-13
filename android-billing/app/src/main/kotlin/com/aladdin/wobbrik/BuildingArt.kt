package com.aladdin.wobbrik

import androidx.compose.ui.graphics.drawscope.DrawScope

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

/** Fit the whole accepted silhouette; art pitch changes only vertical presentation. */
internal fun DrawScope.buildingPreview(atlases: Map<String, GameAtlas>, art: Map<String, CosmeticSprite>,
    parts: Map<String, String>, centers: List<Long>, garden: CosmeticSprite? = null) {
    if (centers.isEmpty() || size.width <= 0 || size.height <= 0) return
    val atlas = atlases.getValue(gameAtlasStyle(parts))
    val pitch = 100f * atlas.pitch / atlas.floor.size.width
    val min = minOf(-90f, centers.min() / 1000f - 50f)
    val max = maxOf(90f, centers.max() / 1000f + 50f)
    val top = centers.lastIndex * pitch + 121f
    val below = if (garden == null) 0f else 55f
    val height = top + below
    val scale = minOf(size.width / (max - min), size.height / height, atlas.floor.size.width / 100f)
    val left = (size.width - (max - min) * scale) / 2
    val bottom = (size.height - height * scale) / 2 + top * scale
    if (garden != null) sprite(garden, left + (-90f - min) * scale, bottom - 65f * scale, 180f * scale, 120f * scale)
    centers.forEachIndexed { index, center ->
        val x = left + (center / 1000f - 50f - min) * scale
        buildingFloor(atlases, art, parts, x, bottom - index * pitch * scale, scale, index == 0, index == centers.lastIndex)
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
