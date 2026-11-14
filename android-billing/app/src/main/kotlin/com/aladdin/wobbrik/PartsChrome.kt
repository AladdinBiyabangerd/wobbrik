package com.aladdin.wobbrik

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource

/** Outline symbols from the approved boards; adjacent labels carry semantics. */
internal fun partCategoryIcon(category: String): Int = when (category) {
    "wall" -> R.drawable.detail_wall
    "window" -> R.drawable.detail_window
    "entrance" -> R.drawable.detail_entrance
    "roof" -> R.drawable.detail_roof
    else -> R.drawable.detail_lighting
}

@Composable
internal fun DetailIcon(resource: Int, active: Boolean = false) {
    if (mobileUiSizing().portrait) Icon(painterResource(resource), null,
        Modifier.size(GameTokens.Inset), tint = if (active) GameTokens.Background else GameTokens.Muted)
}

@Composable
internal fun Modifier.partsFrame(): Modifier = if (!mobileUiSizing().portrait) this else
    background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface)))

/** Approved soft selected edge, using the same token-derived strokes as city chrome. */
@Composable
internal fun Modifier.partsEdge(active: Boolean): Modifier {
    if (!active || !mobileUiSizing().portrait) return this
    return drawBehind {
        val layers = (GameTokens.Small / GameTokens.Hairline).toInt()
        for (layer in layers downTo 1) drawRoundRect(
            GameTokens.Accent.copy(alpha = (1f - GameTokens.DimOpacity) / layer),
            cornerRadius = CornerRadius(GameTokens.ControlRadius.toPx()),
            style = Stroke(GameTokens.Hairline.toPx() + GameTokens.Small.toPx() * layer / layers))
    }
}

@Composable
internal fun Modifier.partsTabFill(active: Boolean): Modifier {
    if (!mobileUiSizing().portrait) return this
    return background(Brush.verticalGradient(if (active)
        listOf(androidx.compose.ui.graphics.lerp(GameTokens.Accent, GameTokens.Text, 1f - GameTokens.DimOpacity), GameTokens.Accent)
        else listOf(GameTokens.Raised, GameTokens.Surface)),
        androidx.compose.foundation.shape.RoundedCornerShape(GameTokens.ControlRadius))
}

@Composable
internal fun partsBorder(active: Boolean = false) = androidx.compose.foundation.BorderStroke(GameTokens.Hairline,
    if (active) GameTokens.Accent else if (!mobileUiSizing().portrait) GameTokens.Raised else
        androidx.compose.ui.graphics.lerp(GameTokens.Raised, GameTokens.Accent, 1f - GameTokens.DimOpacity))
