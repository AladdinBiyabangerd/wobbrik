package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

/** Seven approved destinations. Play remains an action, never a synthetic game route. */
@Composable
internal fun MobileMenuNavbar(page: String, active: Boolean, enabled: Boolean,
    onPlay: () -> Unit, onPage: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Seven 48dp targets cannot fit in a 320dp window. Wrap each side while keeping Play centered.
        val wrapped = maxWidth < GameTokens.Target * 6 + GameTokens.MobileHome.PlayDiameter
        val sideWidth = (maxWidth - GameTokens.MobileHome.PlayDiameter) / 2
        val labels = !wrapped && LocalDensity.current.fontScale <= 1f
        val compactLabels = maxWidth < GameTokens.PanelWidth + GameTokens.Target
        MenuRelief(kind = MenuSkin.Navbar) {
            Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.MobileHome.NavbarHeight),
                verticalAlignment = Alignment.CenterVertically) {
                MenuSide(listOf(
                    MenuDestination("Ana", "Ana ekran", "home", MobileIcon.Home),
                    MenuDestination("Şəhər", "Şəhərim", "city", MobileIcon.City),
                    MenuDestination("İnventar", "İnventar", "inventory", MobileIcon.Inventory)),
                    page, labels, wrapped, compactLabels, Modifier.width(sideWidth), onPage)
                Box(Modifier.size(GameTokens.MobileHome.PlayDiameter)
                    .shadow(GameTokens.Small, CircleShape, ambientColor = GameTokens.MobileHome.PlayBottom, spotColor = GameTokens.Background)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom)))
                    .border(GameTokens.Hairline, GameTokens.MobileHome.RimLight, CircleShape)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onPlay)
                    .semantics { contentDescription = if (active) "Cəhdə qayıt" else "Oyunu başlat" }
                    .then(if (!enabled) Modifier.background(GameTokens.Raised) else Modifier), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.matchParentSize()) {
                        val inset = GameTokens.Quarter.toPx()
                        drawArc(GameTokens.MobileHome.PanelBottom.copy(alpha = 1f - GameTokens.DimOpacity), 10f, 150f, false,
                            topLeft = Offset(inset / 2, inset / 2), size = Size(size.width - inset, size.height - inset),
                            style = Stroke(GameTokens.Hairline.toPx() * 2, cap = StrokeCap.Round))
                        drawArc(GameTokens.MobileHome.RimLight, 190f, 150f, false,
                            topLeft = Offset(inset / 2, inset / 2), size = Size(size.width - inset, size.height - inset),
                            style = Stroke(GameTokens.Hairline.toPx()))
                        drawArc(GameTokens.Text, 210f, 40f, false,
                            topLeft = Offset(inset, inset), size = Size(size.width - inset * 2, size.height - inset * 2),
                            style = Stroke(GameTokens.Hairline.toPx() * 2, cap = StrokeCap.Round))
                    }
                    if (!enabled) Box(Modifier.matchParentSize().background(GameTokens.Background.copy(alpha = GameTokens.DimOpacity)))
                    MobileMenuIcon(MobileIcon.Play, Modifier.size(GameTokens.Target - GameTokens.Small))
                }
                MenuSide(listOf(
                    MenuDestination("Bazar", "Bazar", "market", MobileIcon.Market),
                    MenuDestination("Uğurlar", "Nailiyyətlər", "achievements", MobileIcon.Trophy),
                    MenuDestination("Profil", "Profil", "account", MobileIcon.Account)),
                    page, labels, wrapped, compactLabels, Modifier.width(sideWidth), onPage)
            }
        }
    }
}

private data class MenuDestination(val label: String, val description: String, val page: String, val icon: MobileIcon)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuSide(items: List<MenuDestination>, page: String, labels: Boolean, wrapped: Boolean, compactLabels: Boolean,
    modifier: Modifier, onPage: (String) -> Unit) {
    FlowRow(modifier, maxItemsInEachRow = if (wrapped) 2 else 3,
        horizontalArrangement = Arrangement.SpaceEvenly, verticalArrangement = Arrangement.Center) {
        items.forEach { item ->
            Box((if (wrapped) Modifier.width(GameTokens.Target) else Modifier.weight(1f))
                .heightIn(min = GameTokens.Target).clip(RoundedCornerShape(GameTokens.ControlRadius))
                .clickable(role = Role.Tab, onClick = { onPage(item.page) })
                .semantics { contentDescription = item.description; selected = page == item.page }) {
                if (page == item.page) MenuRelief(Modifier.matchParentSize(), shape = RoundedCornerShape(GameTokens.ControlRadius), kind = MenuSkin.Selected) {}
                Column(Modifier.fillMaxWidth().padding(vertical = GameTokens.Quarter), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterVertically)) {
                    MobileMenuIcon(item.icon, Modifier.size(GameTokens.MobileHome.NavIcon))
                    if (labels) Text(fontFamily = GameTypeface, text = item.label, color = GameTokens.Text, fontSize = if (compactLabels) GameTokens.MobileHome.CompactNavLabel else GameTokens.Caption,
                        fontWeight = FontWeight.Bold, softWrap = false)
                }
            }
        }
    }
}
