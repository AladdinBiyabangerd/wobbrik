package com.aladdin.wobbrik

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape

internal enum class MenuSkin { Panel, Commerce, Wallet, Stats, Track, Navbar, Selected }

/** Scalable native surfaces. No bitmap, fixed source rectangle or baked label. */
@Composable
internal fun MenuRelief(modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(GameTokens.CardRadius), kind: MenuSkin = MenuSkin.Panel,
    content: @Composable BoxScope.() -> Unit) {
    val selected = kind == MenuSkin.Selected
    val fill = if (selected) listOf(GameTokens.Raised, GameTokens.MobileHome.PanelTop, GameTokens.MobileHome.PanelBottom)
        else if (kind == MenuSkin.Commerce) listOf(GameTokens.Raised, GameTokens.MobileHome.PanelTop, GameTokens.MobileHome.PanelBottom)
        else listOf(GameTokens.MobileHome.PanelTop, GameTokens.MobileHome.PanelBottom, GameTokens.MobileHome.PanelTop)
    val rim = if (selected) listOf(GameTokens.Text, GameTokens.Accent, GameTokens.Accent)
        else listOf(GameTokens.MobileHome.RimLight, GameTokens.MobileHome.Rim, GameTokens.MobileHome.Rim)
    Box(modifier.shadow(if (kind == MenuSkin.Panel || kind == MenuSkin.Commerce || selected) GameTokens.Small else GameTokens.Quarter, shape,
            ambientColor = GameTokens.Background, spotColor = GameTokens.Background)
        .clip(shape).background(Brush.verticalGradient(fill))
        .border(GameTokens.Hairline, Brush.verticalGradient(rim), shape)
        .border(GameTokens.Quarter, Brush.verticalGradient(listOf(
            GameTokens.MobileHome.Rim.copy(alpha = 1f - GameTokens.DimOpacity),
            GameTokens.Background.copy(alpha = 0f),
            GameTokens.MobileHome.Rim.copy(alpha = 1f - GameTokens.DimOpacity))), shape),
        content = content)
}
