package com.aladdin.wobbrik

import androidx.compose.ui.graphics.drawscope.DrawScope

/** The same selected modular layers as the accepted game, at the fixed city camera.
 * City floor centers/rotation remain server-derived; only vertical art pitch is projected.
 * Inventory's older presentation is intentionally outside this screen's change.
 */
internal fun DrawScope.cityBuildingFloor(atlases: Map<String, GameAtlas>, art: Map<String, CosmeticSprite>,
    parts: Map<String, String>, x: Float, base: Float, scale: Float, ground: Boolean, top: Boolean,
    windowAlpha: Float = 1f) {
    val atlas = atlases.getValue(gameAtlasStyle(parts))
    val width = CityBuildingProjection.ArtWidth.toFloat() * scale
    val height = width * atlas.floor.size.height / atlas.floor.size.width
    drawModularFloor(atlas.floor, art, atlases, parts, x, base - height, width, ground, windowAlpha)
    if (!top) return
    val eave = base - height + width * atlas.depth / atlas.floor.size.width
    // Keep the city's selected catalog roof, including pitched base roofs from city-view.
    // The game-only flat-cap replacement does not change this ownership presentation.
    val id = parts["roof"]
    drawModularRoof(art[id], id, x, eave, width)
}
