package com.aladdin.wobbrik

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/** Read failure has no scene or camera controls. Retry uses the existing account-bound read. */
@Composable
internal fun CityErrorContent(onRetry: () -> Unit, onHome: () -> Unit, onInventory: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Column(Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
            CommerceTitle("Şəhərim", onHome)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CityErrorMode("Plan", MobileIcon.Layers, false, Modifier.weight(1f))
                CityErrorMode("Görünüş", MobileIcon.City, true, Modifier.weight(1f))
            }
        }
        CityErrorCard(onRetry, Modifier.weight(1f, fill = false).heightIn(max = GameTokens.Target * 8))
        CityNavigation(onInventory)
    }
}

@Composable
private fun CityErrorMode(label: String, icon: MobileIcon, active: Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(GameTokens.CardRadius)
    Surface(modifier = modifier.semantics { if (active) selected = true else disabled() },
        color = Color.Transparent, shape = shape) {
        MenuRelief(shape = shape, kind = if (active) MenuSkin.Selected else MenuSkin.Panel) {
            Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)
                .then(if (active) Modifier.background(Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop,
                    GameTokens.Accent, GameTokens.MobileHome.PlayBottom))) else Modifier).padding(GameTokens.Small),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterHorizontally)) {
                if (LocalDensity.current.fontScale <= 1f)
                    MobileMenuIcon(icon, Modifier.size(GameTokens.Inset), if (active) GameTokens.Background else GameTokens.Muted)
                Text(label, Modifier.weight(1f, fill = false).horizontalScroll(rememberScrollState()), softWrap = false, color = if (active) GameTokens.Background else GameTokens.Muted,
                    fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CityErrorCard(onRetry: () -> Unit, modifier: Modifier) {
    val largeFont = LocalDensity.current.fontScale > 1f
    MenuRelief(modifier = modifier, shape = RoundedCornerShape(GameTokens.CardRadius), kind = MenuSkin.Panel) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val gap = if (maxHeight < GameTokens.Target * 6) GameTokens.Small else GameTokens.Gap
            Column(Modifier.fillMaxWidth().padding(gap), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(gap)) {
                Column(Modifier.weight(1f).fillMaxWidth()
                    .then(if (largeFont) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(gap)) {
                    // Large type keeps a readable illustration and scrolls supporting content;
                    // the primary retry stays pinned. Normal type fits the whole composition.
                    Box((if (largeFont) Modifier.height(GameTokens.Target * 2) else Modifier.weight(1f))
                        .fillMaxWidth().heightIn(max = GameTokens.Target * 4).clipToBounds()) {
                        Image(painterResource(R.drawable.city_error_illustration_v1), null,
                            Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                    Text("Şəhər yüklənmədi", color = GameTokens.Text, fontFamily = GameTypeface,
                        fontSize = if (largeFont) GameTokens.Body else GameTokens.Title,
                        fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                }
                HomeButton("Yenilə", primary = true, compact = true, leading = {
                    MobileMenuIcon(MobileIcon.Refresh, Modifier.size(GameTokens.Inset), GameTokens.Background)
                }, onClick = onRetry)
            }
        }
    }
}
