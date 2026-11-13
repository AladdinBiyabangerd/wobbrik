package com.aladdin.wobbrik

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun PurchaseHeader(title: String, coins: Long, onBack: () -> Unit) {
    BoxWithConstraints {
        val inline = maxWidth >= GameTokens.PanelWidth - GameTokens.Gap * 2 &&
            androidx.compose.ui.platform.LocalDensity.current.fontScale <= 1f && coins.toString().length <= 4
        Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if (inline) Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik",
                    Modifier.width(GameTokens.Target + GameTokens.Inset).height(GameTokens.Target))
                Surface(onClick = onBack, shape = CircleShape, color = GameTokens.Raised,
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Muted), modifier = Modifier.size(GameTokens.Target)) {
                    Box(Modifier.semantics { contentDescription = "Geri" }, contentAlignment = Alignment.Center) {
                        Text("‹", color = GameTokens.Text, fontSize = GameTokens.Score)
                    }
                }
                if (inline) {
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(GameTokens.CardRadius),
                        color = GameTokens.Surface, border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                        Text(title, Modifier.padding(GameTokens.Small), color = GameTokens.Text,
                            fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
                    }
                } else Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.weight(1f).height(GameTokens.Target))
                Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                    Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
                        Text(coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (!inline) Text(title, color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun PurchaseCard(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier,
    content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(GameTokens.CardRadius)
    Surface(onClick = onClick, modifier = modifier.then(if (selected) Modifier.shadow(GameTokens.PanelElevation,
        shape, ambientColor = GameTokens.Accent, spotColor = GameTokens.Accent) else Modifier),
        shape = shape, color = Color.Transparent,
        border = BorderStroke(if (selected) GameTokens.Hairline * 2 else GameTokens.Hairline,
            if (selected) GameTokens.Accent else lerp(GameTokens.Raised, GameTokens.Muted, 1f - GameTokens.DimOpacity))) {
        Box(Modifier.background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background))),
            contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
internal fun PurchasePrice(price: Long?, modifier: Modifier = Modifier) {
    Row(modifier.background(Brush.verticalGradient(listOf(lerp(GameTokens.Danger, GameTokens.Text,
        1f - GameTokens.DimOpacity), GameTokens.Danger)), RoundedCornerShape(GameTokens.ControlRadius))
        .border(GameTokens.Hairline, GameTokens.Text, RoundedCornerShape(GameTokens.ControlRadius))
        .padding(GameTokens.Quarter), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterHorizontally)) {
        Image(painterResource(R.drawable.ui_coin), null, Modifier.size(GameTokens.Inset))
        Text(price?.toString() ?: "—", color = GameTokens.Background, fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { contentDescription = "${price ?: "—"} sikkə" })
    }
}

@Composable
internal fun PurchaseAction(modifier: Modifier = Modifier.fillMaxWidth(), primary: Boolean = false,
    enabled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(GameTokens.CardRadius)
    val background = if (primary && enabled) GameTokens.Accent else GameTokens.Surface
    val foreground = if (!enabled) GameTokens.Muted else if (primary) GameTokens.Background else GameTokens.Text
    Button(onClick, modifier = modifier.heightIn(min = GameTokens.Target + GameTokens.Gap)
        .background(Brush.verticalGradient(listOf(if (primary && enabled)
            lerp(GameTokens.Accent, GameTokens.Text, 1f - GameTokens.DimOpacity) else GameTokens.Raised, background)), shape),
        enabled = enabled, shape = shape,
        border = BorderStroke(GameTokens.Hairline, if (primary && enabled) GameTokens.Text else GameTokens.Raised),
        contentPadding = PaddingValues(GameTokens.Small),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = foreground,
            disabledContainerColor = Color.Transparent, disabledContentColor = foreground)) {
        Icon(painterResource(if (primary) R.drawable.city_purchase_check else R.drawable.city_purchase_cancel),
            null, Modifier.size(GameTokens.Inset))
        Spacer(Modifier.width(GameTokens.Small))
        Text(if (primary) "Al" else "Ləğv et", fontSize = if (primary) GameTokens.Title else GameTokens.Body,
            fontWeight = FontWeight.Bold, softWrap = false)
    }
}
