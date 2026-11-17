package com.aladdin.wobbrik

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.contentDescription
import appbackend.wobbrik.game.OwnedBuilding

@Composable
internal fun CityHeader(population: String?, onHome: () -> Unit, cached: Boolean = false) {
    if (LocalMobileMenuShell.current) {
        Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            CommerceTitle("Şəhərim", onHome)
            Row(Modifier.fillMaxWidth()) {
                CityMode("Plan", MobileIcon.Layers, false, Modifier.weight(1f))
                CityMode("Görünüş", MobileIcon.City, true, Modifier.weight(1f))
            }
            if (population != null) Text("Əhali: $population" + if (cached) " · Keş" else "",
                color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Caption)
        }
        return
    }
    val largeFont = LocalDensity.current.fontScale > 1f
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        if (!LocalMobileMenuShell.current) Image(painterResource(R.drawable.wobbrik_logo), null,
            Modifier.size(width = if (largeFont) GameTokens.Target else GameTokens.Target * 2,
                height = GameTokens.Target).clickable(onClick = onHome)
                .semantics { contentDescription = "Ana ekran" })
        CityFrame(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                if (!largeFont) Image(painterResource(R.drawable.city_view_skyline), null,
                    Modifier.size(GameTokens.Target - GameTokens.Gap), colorFilter = ColorFilter.tint(GameTokens.Accent))
                Text("Şəhərim", color = GameTokens.Text,
                    fontSize = if (largeFont) GameTokens.Body else GameTokens.Title,
                    fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
        }
    }
    // One joined rail, with the approved active pill above it. Large type keeps its
    // existing text-only adaptation instead of squeezing an icon into the label.
    Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(4f).heightIn(min = GameTokens.Target).semantics { disabled() },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (!largeFont) Image(painterResource(R.drawable.city_plan_layers), null, Modifier.size(GameTokens.Inset),
                    colorFilter = ColorFilter.tint(GameTokens.Muted))
                Spacer(Modifier.width(GameTokens.Small))
                Text("Plan", color = GameTokens.Muted, fontSize = GameTokens.Body)
            }
            Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Accent,
                border = BorderStroke(GameTokens.Hairline, GameTokens.Text),
                modifier = Modifier.weight(5f).cityActiveGlow(GameTokens.CardRadius).semantics { selected = true }) {
                Row(Modifier.background(Brush.horizontalGradient(listOf(GameTokens.Accent,
                    lerp(GameTokens.Accent, GameTokens.Text, 1f - GameTokens.DimOpacity))))
                    .heightIn(min = GameTokens.Target).padding(GameTokens.Small),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    if (!largeFont) {
                        Image(painterResource(R.drawable.city_view_skyline), null,
                            Modifier.size(GameTokens.Inset), colorFilter = ColorFilter.tint(GameTokens.Background))
                        Spacer(Modifier.width(GameTokens.Quarter))
                    }
                    Text("Şəhər görünüşü", color = GameTokens.Background,
                        fontSize = GameTokens.Body, fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
    if (population != null) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Text("Əhali: $population", color = GameTokens.Muted, fontSize = GameTokens.Body)
        if (cached) Text("Keş", color = GameTokens.Muted, fontSize = GameTokens.Caption,
            modifier = Modifier.semantics { contentDescription = "Saxlanmış məlumat · yalnız baxış" })
    }
}

@Composable
internal fun CityViewPanel(building: OwnedBuilding?, focused: Boolean, enabled: Boolean, onView: () -> Unit) {
    val largeFont = LocalDensity.current.fontScale > 1f
    if (LocalMobileMenuShell.current) {
        MenuRelief(Modifier.fillMaxWidth(), kind = MenuSkin.Commerce) {
            Row(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if (!largeFont) {
                    if (building != null) BuildingPreview(building, Modifier.size(GameTokens.Target + GameTokens.Gap))
                    else CityCameraArt()
                }
                Column(Modifier.weight(1f)) { CityViewSummary(building, focused) }
                HomeButton(if (focused) "Geri" else "Bax", primary = true, compact = true,
                    showPlayIcon = !focused, enabled = enabled,
                    modifier = Modifier.widthIn(min = GameTokens.Target * 2), onClick = onView)
            }
        }
        return
    }
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
    if (LocalMobileMenuShell.current) return
    val largeFont = LocalDensity.current.fontScale > 1f
    CityFrame(Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // Only the narrow, enlarged-font window needs a wider inventory destination.
            val inventoryWeight = if (largeFont && maxWidth < GameTokens.PanelWidth) 1.4f else 1f
            Row(Modifier.fillMaxWidth().padding(GameTokens.Quarter), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                CityNavItem("Şəhər", R.drawable.city_view_skyline, true, {}, Modifier.weight(1f))
                CityNavItem("İnventar", R.drawable.ui_inventory, false, onInventory, Modifier.weight(inventoryWeight))
                CityNavItem("Bazar", R.drawable.ui_market, false, {}, Modifier.weight(1f), enabled = false)
            }
        }
    }
}

@Composable
private fun CityNavItem(label: String, icon: Int, selected: Boolean, onClick: () -> Unit,
    modifier: Modifier, enabled: Boolean = true) {
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = GameTokens.Target)
        .then(if (selected) Modifier.cityActiveGlow(GameTokens.ControlRadius) else Modifier)
        .semantics { this.selected = selected },
        shape = RoundedCornerShape(GameTokens.ControlRadius), color = if (selected) GameTokens.Raised else GameTokens.Surface,
        border = BorderStroke(if (selected) GameTokens.Hairline * 2 else GameTokens.Hairline,
            if (selected) GameTokens.Accent else GameTokens.Raised)) {
        Column(Modifier.background(Brush.verticalGradient(if (selected) listOf(lerp(GameTokens.Surface, GameTokens.Accent, 1f - GameTokens.DimOpacity), GameTokens.Surface)
            else listOf(GameTokens.Surface, GameTokens.Background))).padding(GameTokens.Quarter), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(icon), null, Modifier.size(GameTokens.Target - GameTokens.Gap),
                colorFilter = if (selected) ColorFilter.tint(GameTokens.Accent) else null)
            Text(label, color = if (enabled) GameTokens.Text else GameTokens.Muted,
                fontSize = if (LocalDensity.current.fontScale > 1f) GameTokens.Caption else GameTokens.Body, fontWeight = FontWeight.Bold, maxLines = 1,
                softWrap = false)
        }
    }
}

/** Soft cyan edge from city-view, using concentric strokes on every supported API.
 * Drawing behind the Surface leaves text, hit targets and layout bounds unchanged.
 * No platform blur/shadow support or per-frame bitmap allocation is required. */
private fun Modifier.cityActiveGlow(radius: androidx.compose.ui.unit.Dp): Modifier = drawBehind {
    val edge = GameTokens.Hairline.toPx()
    val reach = GameTokens.Small.toPx()
    val layers = (GameTokens.Small / GameTokens.Hairline).toInt()
    for (layer in layers downTo 1) {
        drawRoundRect(GameTokens.Accent.copy(alpha = (1f - GameTokens.DimOpacity) / layer),
            cornerRadius = CornerRadius(radius.toPx()),
            style = Stroke(edge + reach * layer / layers))
    }
}

@Composable
private fun CityFrame(modifier: Modifier, content: @Composable () -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(GameTokens.CardRadius), color = androidx.compose.ui.graphics.Color.Transparent,
        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Box(Modifier.background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface)))) { content() }
    }
}

/** Native camera controls; fit returns to the complete scene, focus uses the selected real building. */
@Composable
internal fun CityCameraControls(focused: Boolean, enabled: Boolean, onFit: () -> Unit, onFocus: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.End)) {
        listOf(false, true).forEach { expand ->
            Surface(onClick = if (expand) onFocus else onFit, enabled = enabled,
                modifier = Modifier.size(GameTokens.Target).semantics {
                    contentDescription = if (expand) { if (focused) "Şəhər görünüşünə qayıt" else "Binanı böyüt" } else "Kameranı sıfırla"
                }, color = androidx.compose.ui.graphics.Color.Transparent, shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                MenuRelief(shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                    androidx.compose.foundation.Canvas(Modifier.align(Alignment.Center).size(GameTokens.Inset)) {
                        val stroke = GameTokens.Hairline.toPx() * 2
                        val ink = if (enabled) GameTokens.Text else GameTokens.Muted
                        val w = size.width; val h = size.height
                        if (expand) {
                            val arm = w / 3
                            listOf(0f to 0f, w to 0f, 0f to h, w to h).forEach { (x, y) ->
                                drawLine(ink, androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Offset(if (x == 0f) arm else w - arm, y), stroke)
                                drawLine(ink, androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Offset(x, if (y == 0f) arm else h - arm), stroke)
                            }
                        } else {
                            drawRoundRect(ink, topLeft = androidx.compose.ui.geometry.Offset(0f, h / 4),
                                size = androidx.compose.ui.geometry.Size(w, h * 3 / 4), cornerRadius = CornerRadius(stroke), style = Stroke(stroke))
                            drawCircle(ink, w / 5, androidx.compose.ui.geometry.Offset(w / 2, h * 5 / 8), style = Stroke(stroke))
                            drawLine(ink, androidx.compose.ui.geometry.Offset(w / 3, h / 4), androidx.compose.ui.geometry.Offset(w / 3, 0f), stroke)
                            drawLine(ink, androidx.compose.ui.geometry.Offset(w / 3, 0f), androidx.compose.ui.geometry.Offset(w * 2 / 3, 0f), stroke)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CityMode(label: String, icon: MobileIcon, active: Boolean, modifier: Modifier) {
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
