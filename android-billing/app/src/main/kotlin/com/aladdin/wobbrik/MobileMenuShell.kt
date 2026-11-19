package com.aladdin.wobbrik

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush

/** Menu content owns contextual actions; the authenticated host owns the dock and Play capability. */
internal val LocalMobileMenuShell = staticCompositionLocalOf { false }

/** Shared only by a mounted mobile shell, never retained by a process singleton. */
internal val LocalMenuLogo = staticCompositionLocalOf<Pair<ResourceArt, () -> Unit>?> { null }

internal fun mobileMenuDestination(page: String): String = when (page) {
    "parts", "collection" -> "inventory"
    "owned-decorations", "owned-land" -> "inventory"
    "decorations", "market-own" -> "market"
    "leaderboard" -> "achievements"
    "settings" -> "account"
    else -> page
}

@Composable
internal fun MobileMenuShell(page: String, coins: Long?, onPlay: (() -> Unit)?,
    onPage: (String) -> Unit, content: @Composable () -> Unit) {
    val logo = rememberResourceArt(R.drawable.wobbrik_logo)
    CompositionLocalProvider(LocalMenuLogo provides logo) {
        MobileMenuShellContent(page, coins, onPlay, onPage, content)
    }
}

@Composable
private fun MobileMenuShellContent(page: String, coins: Long?, onPlay: (() -> Unit)?,
    onPage: (String) -> Unit, content: @Composable () -> Unit) {
    if (page == "home") {
        // Home owns its frameless header/body and bottom safe-area dock.
        CompositionLocalProvider(LocalMobileMenuShell provides false) { content() }
        return
    }
    if (page == "city") {
        CityCollectionShell(coins, onPlay, onPage, content)
        return
    }
    var commerceDetail by remember(page) { mutableStateOf(false) }
    val reportDetail = remember(page) { { active: Boolean -> commerceDetail = active } }
    if (page == "inventory" || page == "collection" || page == "owned-decorations" || mobileMenuDestination(page) in setOf("market", "achievements")) {
        CollectionInventoryShell(coins, onPlay, onPage, commerceDetail, reportDetail, page, content)
        return
    }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().consumeWindowInsets(WindowInsets.safeDrawing).imePadding()) {
        Box(Modifier.padding(horizontal = GameTokens.Gap, vertical = GameTokens.Small)) { MobileMenuHeader(coins) }
        if (!commerceDetail && page in inventoryTabs.map { it.route }) {
            Column(Modifier.padding(horizontal = GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                CommerceTabs(inventoryTabs, page, onPage)
            }
        }
        if (!commerceDetail && (page in marketTabs.map { it.route } || page == "market-own")) {
            Column(Modifier.padding(horizontal = GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                CommerceTabs(marketTabs, if(page == "market-own") "market" else page, onPage)
            }
        }
        if (page in setOf("achievements", "leaderboard")) {
            val communityTabs = page in setOf("achievements", "leaderboard")
            val tabs = if (communityTabs)
                listOf("Nailiyyətlər" to "achievements", "Rekordlar" to "leaderboard")
            else listOf("Binalar" to "inventory", "Görünüş" to "collection")
            val tab: @Composable (String, String, Modifier) -> Unit = { label, route, modifier ->
                Surface(onClick = { onPage(route) }, modifier = modifier.heightIn(min = GameTokens.Target)
                    .semantics { selected = page == route }, shape = RoundedCornerShape(GameTokens.ControlRadius),
                    color = if (communityTabs) androidx.compose.ui.graphics.Color.Transparent else if (page == route) GameTokens.Raised else GameTokens.Surface) {
                    if (communityTabs) MenuRelief(Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(GameTokens.ControlRadius),
                        kind = if(page == route) MenuSkin.Selected else MenuSkin.Panel) {
                        Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target).background(Brush.verticalGradient(
                            if(page == route) listOf(GameTokens.MobileHome.PlayTop,GameTokens.Accent,GameTokens.MobileHome.PlayBottom)
                            else listOf(GameTokens.Raised,GameTokens.MobileHome.PanelBottom)))
                            .padding(GameTokens.Small), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter, androidx.compose.ui.Alignment.CenterHorizontally)) {
                            MobileMenuIcon(if(route == "achievements") MobileIcon.Trophy else MobileIcon.Records,
                                Modifier.size(GameTokens.Inset), tint = if(page == route) GameTokens.Background else GameTokens.Text)
                            Text(label, color = if(page == route) GameTokens.Background else GameTokens.Text,
                                fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
                        }
                    } else Box(Modifier.fillMaxWidth().padding(GameTokens.Small), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(label, color = GameTokens.Text, fontFamily = GameTypeface,
                            fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
                    }
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = GameTokens.Gap)) {
                val measurer = rememberTextMeasurer()
                val density = LocalDensity.current
                val widths = tabs.map { (label, _) ->
                    val measured = measurer.measure(label, LocalTextStyle.current.copy(fontFamily = GameTypeface,
                        fontSize = GameTokens.Body, fontWeight = FontWeight.Bold))
                    with(density) { measured.size.width.toDp() } + GameTokens.Small * 2 +
                        (if(communityTabs) GameTokens.Inset + GameTokens.Quarter else GameTokens.Quarter)
                }
                val scrolling = density.fontScale > 1f || widths.fold(GameTokens.Small * (tabs.size - 1)) { sum, width -> sum + width } > maxWidth
                val tabScroll = rememberScrollState()
                LaunchedEffect(page, scrolling, tabScroll.maxValue) {
                    if (scrolling) tabScroll.scrollTo(if (page == tabs.last().second) tabScroll.maxValue else 0)
                }
                Row(Modifier.fillMaxWidth().then(if(scrolling) Modifier.horizontalScroll(tabScroll) else Modifier),
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    tabs.forEachIndexed { index, (label, route) ->
                        tab(label, route, if(scrolling) Modifier.width(widths[index]) else Modifier.weight(1f))
                    }
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            CompositionLocalProvider(LocalMobileMenuShell provides true, LocalCommerceDetail provides reportDetail,
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = GameTypeface)) { content() }
        }
        Box(Modifier.padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter)) {
            MobileMenuNavbar(mobileMenuDestination(page), false, onPlay != null, { onPlay?.invoke() }, onPage)
        }
    }
}
