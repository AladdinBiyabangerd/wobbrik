package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

/** Purchase explanation scrolls independently of the explicit confirmation controls. */
@Composable
internal fun CityPurchasePanel(enabled: Boolean, onBuy: () -> Unit, onCancel: () -> Unit,
    content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = Color.Transparent,
        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Column(Modifier.background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background))).padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Column(Modifier.heightIn(max = GameTokens.Target * 3).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = content)
            if (LocalDensity.current.fontScale > 1f) {
                PurchaseAction(primary = true, enabled = enabled, onClick = onBuy)
                PurchaseAction(onClick = onCancel)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    PurchaseAction(modifier = Modifier.weight(1f), onClick = onCancel)
                    PurchaseAction(modifier = Modifier.weight(1f), primary = true,
                        enabled = enabled, onClick = onBuy)
                }
            }
        }
    }
}

@Composable
internal fun PurchaseSummary(art: CosmeticSprite?, content: @Composable ColumnScope.() -> Unit) {
    if (LocalDensity.current.fontScale > 1f) {
        Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            PurchaseArt(art, Modifier.size(GameTokens.Target + GameTokens.Inset))
            content()
        }
    } else {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            PurchaseArt(art, Modifier.size(GameTokens.Target * 2))
            Box(Modifier.width(GameTokens.Hairline).height(GameTokens.Target * 2).background(GameTokens.Muted))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = content)
        }
    }
}
