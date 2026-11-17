package com.aladdin.wobbrik

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

/** Native commerce controls use the established menu palette and dimension scale. */
@Composable
internal fun CommerceAction(label: String, modifier: Modifier = Modifier.fillMaxWidth(), primary: Boolean = false,
    enabled: Boolean = true, icon: MobileIcon? = null, compact: Boolean = false, destructive: Boolean = false, onClick: () -> Unit) {
    val feedback = LocalGameFeedback.current
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Surface(onClick = { feedback(); onClick() }, enabled = enabled, modifier = modifier.heightIn(min = GameTokens.Target),
        shape = shape, color = Color.Transparent) {
        MenuRelief(shape = shape, kind = if (primary && enabled) MenuSkin.Selected else MenuSkin.Panel) {
            BoxWithConstraints {
            val bounded = constraints.hasBoundedWidth
            Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target).then(if (primary && enabled) Modifier.background(Brush.verticalGradient(
                listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom))) else Modifier)
                .padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterHorizontally)) {
                val color = if (!enabled) GameTokens.Muted else if (primary) GameTokens.Background else if (destructive) GameTokens.Danger else GameTokens.Text
                icon?.let { MobileMenuIcon(it, Modifier.size(if(compact) GameTokens.Gap else GameTokens.Inset), color) }
                Text(label, Modifier.then(if (bounded) Modifier.weight(1f, fill = false) else Modifier).widthIn(max = GameTokens.MobileHome.NavbarLabelWidth * LocalDensity.current.fontScale).horizontalScroll(rememberScrollState()), color = color, fontFamily = GameTypeface,
                    fontSize = if(compact) GameTokens.Caption else GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
            }
            }
        }
    }
}

internal data class CommerceTab(val label: String, val route: String, val icon: MobileIcon)
internal val inventoryTabs = listOf(CommerceTab("Binalar", "inventory", MobileIcon.City),
    CommerceTab("Görünüş", "collection", MobileIcon.Palette), CommerceTab("Dekor", "owned-decorations", MobileIcon.Tree))
internal val marketTabs = listOf(CommerceTab("Binalar", "market", MobileIcon.City),
    CommerceTab("Dekor", "decorations", MobileIcon.Tree))

@Composable
internal fun CommerceTabs(tabs: List<CommerceTab>, page: String, onPage: (String) -> Unit) {
    val scroll = rememberScrollState()
    val font = LocalDensity.current.fontScale
    // All labels retain their natural width; compact phones scroll rather than split words.
    LaunchedEffect(page, scroll.maxValue) {
        val index = tabs.indexOfFirst { it.route == page }
        if (index >= 0 && scroll.maxValue > 0) scroll.scrollTo(scroll.maxValue * index / maxOf(1, tabs.size - 1))
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Share the row only when all tabs fit; large type keeps selected-tab scrolling.
        val minimum = (GameTokens.Target + GameTokens.Inset) * font
        val fits = font <= 1f && maxWidth >= minimum * tabs.size + GameTokens.Quarter * (tabs.size - 1)
        Row(Modifier.fillMaxWidth().then(if (fits) Modifier else Modifier.horizontalScroll(scroll)),
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            tabs.forEach { tab ->
                Box(if (fits) Modifier.weight(1f) else Modifier.widthIn(min = minimum)) {
                    CommerceAction(tab.label, Modifier.semantics { selected = page == tab.route }, page == tab.route, icon = tab.icon, compact = true) { onPage(tab.route) }
                }
            }
        }
    }
}

@Composable
internal fun CommerceTitle(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        CommerceAction("", Modifier.width(GameTokens.Target).semantics { contentDescription = "Geri" }, icon = MobileIcon.Back, onClick = onBack)
        if(title == "Bazar") MobileMenuIcon(MobileIcon.Market, Modifier.size(GameTokens.Inset))
        Text(title, Modifier.weight(1f).horizontalScroll(rememberScrollState()), color = GameTokens.Text,
            fontSize = mobileUiSizing().heading, fontFamily = GameTypeface, fontWeight = FontWeight.Bold, softWrap = false)
    }
}

@Composable
internal fun CommercePrice(price: Long, modifier: Modifier = Modifier) {
    Row(modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Inset))
        Text(price.toString(), color = GameTokens.Text, fontFamily = GameTypeface, fontWeight = FontWeight.Bold, softWrap = false)
    }
}

/** Detail routes own their contextual title; the shared wallet/dock stay mounted. */
internal val LocalCommerceDetail = staticCompositionLocalOf<(Boolean) -> Unit> { {} }
@Composable
internal fun CommerceDetailChrome(active: Boolean) {
    val report = LocalCommerceDetail.current
    DisposableEffect(active, report) {
        report(active)
        onDispose { report(false) }
    }
}

/** Read-only status seal: deliberately has no click semantics or action feedback. */
@Composable
internal fun CommerceStatus(label: String, filled: Boolean = false) {
    val shape = RoundedCornerShape(GameTokens.Target)
    MenuRelief(shape = shape, kind = if (filled) MenuSkin.Selected else MenuSkin.Track) {
        Text(label, Modifier.then(if (filled) Modifier.background(Brush.verticalGradient(listOf(
            GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom))) else Modifier)
            .padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter),
            color = if (filled) GameTokens.Background else GameTokens.Accent,
            fontFamily = GameTypeface, fontSize = GameTokens.Label, fontWeight = FontWeight.Bold)
    }
}
