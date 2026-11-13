package com.aladdin.wobbrik

import androidx.compose.ui.graphics.drawscope.DrawScope

/** Shared GameScene facade/eave anchors for inventory and the fixed city camera. */
internal fun DrawScope.buildingFloor(art: Map<String, CosmeticSprite>, parts: Map<String, String>,
    x: Float, base: Float, scale: Float, ground: Boolean, top: Boolean, windowAlpha: Float = 1f) {
    val upper = base - 100f * 597f / 1064f * scale
    sprite(art[parts["wall"]], x, upper, 100f * scale, 100f * 597f / 1064f * scale)
    if (!ground) sprite(art[parts["window"]], x + 30f * scale, upper + 12f * scale, 40f * scale, 35f * scale, alpha = windowAlpha)
    if (ground) {
        sprite(art[parts["entrance"]], x + 30f * scale, base - 40f * scale, 40f * scale, 40f * scale)
        sprite(art[parts["lighting"]], x + 75f * scale, base - 35f * scale, 15f * scale, 25f * scale)
    }
    if (top) roof(art[parts["roof"]], x, base - 36f * scale, 100f * scale)
}
