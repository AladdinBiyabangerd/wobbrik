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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush

/** Menu content owns contextual actions; the authenticated host owns the dock and Play capability. */
internal val LocalMobileMenuShell = staticCompositionLocalOf { false }

internal fun mobileMenuDestination(page: String): String = when (page) {
    "parts", "collection" -> "inventory"
    "decorations", "land" -> "inventory" // Existing purchase entry points; ownership split is a later unit.
    "leaderboard" -> "achievements"
    "settings" -> "account"
    else -> page
}

@Composable
internal fun MobileMenuShell(page: String, coins: Long?, onPlay: (() -> Unit)?,
    onPage: (String) -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().consumeWindowInsets(WindowInsets.safeDrawing).imePadding()) {
        Box(Modifier.padding(horizontal = GameTokens.Gap, vertical = GameTokens.Small)) { MobileMenuHeader(coins) }
        if (page in setOf("achievements", "leaderboard", "inventory", "collection")) {
            val tabs = if (page in setOf("achievements", "leaderboard"))
                listOf("Nailiyyətlər" to "achievements", "Rekordlar" to "leaderboard")
            else listOf("Binalar" to "inventory", "Görünüş" to "collection")
            val tab: @Composable (String, String, Modifier) -> Unit = { label, route, modifier ->
                Surface(onClick = { onPage(route) }, modifier = modifier.heightIn(min = GameTokens.Target)
                    .semantics { selected = page == route }, shape = RoundedCornerShape(GameTokens.ControlRadius),
                    color = if (page == route) GameTokens.Raised else GameTokens.Surface) {
                    Box(Modifier.padding(GameTokens.Small), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(label, color = GameTokens.Text, fontFamily = GameTypeface,
                            fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
                    }
                }
            }
            if (LocalDensity.current.fontScale > 1f) Row(Modifier.padding(horizontal = GameTokens.Gap)
                .horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                tabs.forEach { (label, route) -> tab(label, route, Modifier.widthIn(min = GameTokens.Target * 3)) }
            } else Row(Modifier.fillMaxWidth().padding(horizontal = GameTokens.Gap),
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                tabs.forEach { (label, route) -> tab(label, route, Modifier.weight(1f)) }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            CompositionLocalProvider(LocalMobileMenuShell provides true,
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = GameTypeface)) { content() }
        }
        Box(Modifier.padding(horizontal = GameTokens.Gap, vertical = GameTokens.Small)) {
            MobileMenuNavbar(mobileMenuDestination(page), false, onPlay != null, { onPlay?.invoke() }, onPage)
        }
    }
}
