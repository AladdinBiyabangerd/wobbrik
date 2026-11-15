package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
        val labels = !wrapped && LocalDensity.current.fontScale <= 1f && sideWidth >= (GameTokens.Target + GameTokens.Small) * 3
        Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
            border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
            Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.MobileHome.NavbarHeight),
                verticalAlignment = Alignment.CenterVertically) {
                MenuSide(listOf(
                    MenuDestination("Ana", "Ana ekran", "home", R.drawable.ui_home),
                    MenuDestination("Şəhər", "Şəhərim", "city", R.drawable.ui_city),
                    MenuDestination("İnventar", "İnventar", "inventory", R.drawable.ui_inventory)),
                    page, labels, wrapped, Modifier.width(sideWidth), onPage)
                Surface(onClick = onPlay, enabled = enabled,
                    modifier = Modifier.size(GameTokens.MobileHome.PlayDiameter).semantics {
                        contentDescription = if (active) "Cəhdə qayıt" else "Oyunu başlat"
                    }, shape = CircleShape, color = if (enabled) GameTokens.Accent else GameTokens.Raised,
                    border = BorderStroke(GameTokens.Hairline, if (enabled) GameTokens.Text else GameTokens.Muted)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(fontFamily = GameTypeface, text = "▶", color = GameTokens.Background, fontSize = GameTokens.Body)
                        if (labels) Text(fontFamily = GameTypeface, text = "Oyna", color = GameTokens.Background, fontSize = GameTokens.Caption, fontWeight = FontWeight.ExtraBold)
                    }
                }
                MenuSide(listOf(
                    MenuDestination("Bazar", "Bazar", "market", R.drawable.ui_market),
                    MenuDestination("Uğurlar", "Nailiyyətlər", "achievements", R.drawable.ui_trophy),
                    MenuDestination("Profil", "Profil", "account", R.drawable.ui_account)),
                    page, labels, wrapped, Modifier.width(sideWidth), onPage)
            }
        }
    }
}

private data class MenuDestination(val label: String, val description: String, val page: String, val icon: Int)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuSide(items: List<MenuDestination>, page: String, labels: Boolean, wrapped: Boolean,
    modifier: Modifier, onPage: (String) -> Unit) {
    FlowRow(modifier, maxItemsInEachRow = if (wrapped) 2 else 3,
        horizontalArrangement = Arrangement.SpaceEvenly, verticalArrangement = Arrangement.Center) {
        items.forEach { item ->
            Surface(onClick = { onPage(item.page) },
                modifier = (if (wrapped) Modifier.width(GameTokens.Target) else Modifier.weight(1f))
                    .heightIn(min = GameTokens.Target).semantics {
                        contentDescription = item.description
                        selected = page == item.page
                    }, shape = RoundedCornerShape(GameTokens.ControlRadius),
                color = if (page == item.page) GameTokens.Raised else GameTokens.Surface,
                border = if (page == item.page) BorderStroke(GameTokens.Hairline, GameTokens.Accent) else null) {
                Column(Modifier.padding(vertical = GameTokens.Quarter), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterVertically)) {
                    Image(painterResource(item.icon), null, Modifier.size(GameTokens.Inset))
                    if (labels) Text(fontFamily = GameTypeface, text = item.label, color = GameTokens.Text, fontSize = GameTokens.Caption,
                        fontWeight = FontWeight.Bold, softWrap = false)
                }
            }
        }
    }
}
