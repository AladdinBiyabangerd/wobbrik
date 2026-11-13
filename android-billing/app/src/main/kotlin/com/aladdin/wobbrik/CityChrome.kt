package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.OwnedBuilding

@Composable
internal fun CityHeader(population: String?, onHome: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        IconButton(onClick = onHome, modifier = Modifier.size(GameTokens.Target)) {
            Image(painterResource(R.drawable.ui_home), "Ana ekran", Modifier.size(GameTokens.Target - GameTokens.Gap))
        }
        CityFrame(Modifier.weight(1f)) {
            Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Image(painterResource(R.drawable.ui_city), null, Modifier.size(GameTokens.Target - GameTokens.Gap))
                Text("Şəhərim", color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
            }
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
            border = BorderStroke(GameTokens.Hairline, GameTokens.Raised), modifier = Modifier.semantics { disabled() }) {
            Text("Plan", Modifier.padding(GameTokens.Small), color = GameTokens.Muted, fontSize = GameTokens.Body)
        }
        Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Accent,
            border = BorderStroke(GameTokens.Hairline, GameTokens.Text), modifier = Modifier.weight(1f).semantics { selected = true }) {
            Text("Şəhər görünüşü", Modifier.padding(GameTokens.Small), color = GameTokens.Background,
                fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
    if (population != null) Text("Əhali: $population", color = GameTokens.Muted, fontSize = GameTokens.Body)
}

@Composable
internal fun CityViewPanel(building: OwnedBuilding?, focused: Boolean, enabled: Boolean, onView: () -> Unit) {
    val largeFont = LocalDensity.current.fontScale > 1f
    CityFrame(Modifier.fillMaxWidth()) {
        if (largeFont) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    CityCameraArt()
                    Column(Modifier.weight(1f)) { CityViewSummary(building, focused) }
                }
                HomeButton(if (focused) "Geri" else "Bax", primary = true, compact = true, showPlayIcon = !focused,
                    enabled = enabled, onClick = onView)
            }
        } else {
            Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                CityCameraArt()
                Column(Modifier.weight(1f)) { CityViewSummary(building, focused) }
                HomeButton(if (focused) "Geri" else "Bax", primary = true, compact = true, showPlayIcon = !focused,
                    modifier = Modifier.widthIn(min = GameTokens.Target * 2), enabled = enabled, onClick = onView)
            }
        }
    }
}

@Composable
private fun CityCameraArt() {
    Image(painterResource(R.drawable.city_view_camera_v1), null,
        Modifier.size(width = GameTokens.Target + GameTokens.Gap, height = GameTokens.Target))
}

@Composable
private fun CityViewSummary(building: OwnedBuilding?, focused: Boolean) {
    Text(if (focused) "Bina görünüşü" else "Binaya bax", color = GameTokens.Text,
        fontSize = GameTokens.Hint, fontWeight = FontWeight.Bold)
    Text(building?.let { "${it.floors} mərtəbə · ${it.residents} sakin" } ?: "Sabit kamera",
        color = GameTokens.Muted, fontSize = GameTokens.Caption)
}

@Composable
internal fun CityNavigation(onInventory: () -> Unit) {
    val largeFont = LocalDensity.current.fontScale > 1f
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Only the narrow, enlarged-font window needs a wider inventory destination.
        val inventoryWeight = if (largeFont && maxWidth < GameTokens.PanelWidth) 1.4f else 1f
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            CityNavItem("Şəhər", R.drawable.ui_city, true, {}, Modifier.weight(1f))
            CityNavItem("İnventar", R.drawable.ui_inventory, false, onInventory, Modifier.weight(inventoryWeight))
            CityNavItem("Bazar", R.drawable.ui_market, false, {}, Modifier.weight(1f), enabled = false)
        }
    }
}

@Composable
private fun CityNavItem(label: String, icon: Int, selected: Boolean, onClick: () -> Unit,
    modifier: Modifier, enabled: Boolean = true) {
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = GameTokens.Target).semantics { this.selected = selected },
        shape = RoundedCornerShape(GameTokens.ControlRadius), color = if (selected) GameTokens.Raised else GameTokens.Surface,
        border = BorderStroke(GameTokens.Hairline, if (selected) GameTokens.Accent else GameTokens.Raised)) {
        Column(Modifier.padding(GameTokens.Quarter), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(icon), null, Modifier.size(GameTokens.Target - GameTokens.Gap))
            Text(label, color = if (enabled) GameTokens.Text else GameTokens.Muted,
                fontSize = if (LocalDensity.current.fontScale > 1f) GameTokens.Caption else GameTokens.Body, fontWeight = FontWeight.Bold, maxLines = 1,
                softWrap = false)
        }
    }
}

@Composable
private fun CityFrame(modifier: Modifier, content: @Composable () -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(GameTokens.CardRadius), color = androidx.compose.ui.graphics.Color.Transparent,
        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Box(Modifier.background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface)))) { content() }
    }
}
