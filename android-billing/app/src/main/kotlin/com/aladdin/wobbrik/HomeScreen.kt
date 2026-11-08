package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import appbackend.wobbrik.game.GameSnapshot
import java.math.BigInteger
import java.math.BigDecimal
import java.math.MathContext

/** Decorative city artwork never substitutes for the player's server city. */
@Composable
internal fun HomeScreen(snapshot: GameSnapshot, active: Boolean, onPlay: (() -> Unit)?, onPage: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap)) {
        val tablet = maxWidth >= GameTokens.MediumWidth
        val wide = maxWidth > maxHeight
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
            if (tablet) HomeRail(onPage)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", (if (tablet) Modifier.width(GameTokens.PanelWidth / 2) else Modifier.weight(1f))
                        .height(GameTokens.Target + GameTokens.Small), contentScale = ContentScale.Fit, alignment = Alignment.CenterStart)
                    Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Background,
                        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                        Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                            HomeIcon(R.drawable.ui_coin)
                            Text(snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (tablet) Spacer(Modifier.weight(1f))
                    if (!tablet) IconButton(onClick = { onPage("account") }, modifier = Modifier.size(GameTokens.Target)) {
                        HomeIcon(R.drawable.ui_account, "Hesab")
                    }
                }
                if (wide) Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap),
                    verticalAlignment = Alignment.CenterVertically) {
                    HomeArt(Modifier.weight(1f).fillMaxHeight())
                    HomeControls(snapshot, active, onPlay, onPage, tablet, true,
                        (if (tablet) Modifier.width(GameTokens.PanelWidth + GameTokens.Target) else Modifier.weight(1f)).verticalScroll(rememberScrollState()))
                } else Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    if (!tablet) HomeHeading()
                    HomeArt(Modifier.fillMaxWidth().aspectRatio(1f))
                    HomeControls(snapshot, active, onPlay, onPage, tablet, tablet, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun HomeArt(modifier: Modifier) = Image(painterResource(R.drawable.home_city), null, modifier, contentScale = ContentScale.Fit)

@Composable
private fun HomeHeading() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.CenterHorizontally)) {
        Text("›", color = GameTokens.Accent, fontSize = GameTokens.Score, fontWeight = FontWeight.Black)
        Text("Öz şəhərini qur", Modifier.weight(1f, fill = false), color = GameTokens.Text,
            fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text("‹", color = GameTokens.Accent, fontSize = GameTokens.Score, fontWeight = FontWeight.Black)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeControls(snapshot: GameSnapshot, active: Boolean, onPlay: (() -> Unit)?, onPage: (String) -> Unit,
                         tablet: Boolean, heading: Boolean, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
        if (heading) HomeHeading()
        Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
            border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
            Column(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    HomeStat(R.drawable.ui_level, "Səviyyə ${snapshot.account?.level ?: "—"}")
                    HomeStat(R.drawable.ui_crown, "Rekord ${snapshot.account?.bestFloors ?: "—"}")
                }
                val interval = snapshot.account?.xpInterval
                if (interval != null) {
                    val earned = BigInteger.valueOf(snapshot.xp).subtract(interval.start)
                    val required = interval.next.subtract(interval.start)
                    Text("$earned / $required XP", Modifier.align(Alignment.CenterHorizontally), color = GameTokens.Muted)
                    LinearProgressIndicator(progress = { BigDecimal(earned).divide(BigDecimal(required), MathContext.DECIMAL64).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(), color = GameTokens.Accent, trackColor = GameTokens.Background)
                } else Text("XP —", color = GameTokens.Muted)
            }
        }
        HomeButton(if (active) "Cəhdə qayıt" else "Oyna", primary = true, enabled = onPlay != null, onClick = { onPlay?.invoke() })
        if (tablet) HomeButton("Şəhərim", R.drawable.ui_level, enabled = false, onClick = { onPage("city") })
        if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1f) {
            HomeButton("Kolleksiya", R.drawable.ui_medal, onClick = { onPage("collection") })
            HomeButton("Nailiyyətlər", R.drawable.ui_trophy, onClick = { onPage("achievements") })
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            HomeButton("Kolleksiya", R.drawable.ui_medal, modifier = Modifier.weight(1f), onClick = { onPage("collection") })
            HomeButton("Nailiyyətlər", R.drawable.ui_trophy, modifier = Modifier.weight(1f), onClick = { onPage("achievements") })
        }
    }
}

@Composable
private fun HomeStat(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        HomeIcon(icon)
        Text(text, color = GameTokens.Text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeIcon(icon: Int, description: String? = null) = Image(painterResource(icon), description,
    Modifier.size(GameTokens.Gap * 2), contentScale = ContentScale.Fit)

@Composable
private fun HomeButton(label: String, icon: Int? = null, primary: Boolean = false, enabled: Boolean = true,
                       modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    Button(onClick, modifier = modifier.heightIn(min = if (primary) GameTokens.Target + GameTokens.Gap else GameTokens.Target)
            .background(Brush.verticalGradient(if (primary) listOf(GameTokens.Accent, GameTokens.Accent) else listOf(GameTokens.Raised, GameTokens.Surface)), RoundedCornerShape(GameTokens.CardRadius)),
        enabled = enabled, shape = RoundedCornerShape(GameTokens.CardRadius),
        border = BorderStroke(GameTokens.Hairline, if (primary) GameTokens.Text else GameTokens.Raised),
        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = if (primary) GameTokens.Background else GameTokens.Text), contentPadding = PaddingValues(GameTokens.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.CenterHorizontally)) {
            if (icon != null) HomeIcon(icon)
            if (primary) Text("▶", fontSize = GameTokens.Title)
            Text(label, fontSize = if (primary) GameTokens.Score else GameTokens.Body, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeRail(onPage: (String) -> Unit) {
    Surface(Modifier.width(GameTokens.Target + GameTokens.Gap).padding(top = GameTokens.Target),
        shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            listOf(Triple(R.drawable.ui_home, "Ana ekran", "home"), Triple(R.drawable.ui_city, "Şəhərim", "city"),
                Triple(R.drawable.ui_inventory, "İnventar", "inventory"), Triple(R.drawable.ui_market, "Bazar", "market"),
                Triple(R.drawable.ui_trophy, "Nailiyyətlər", "achievements"), Triple(R.drawable.ui_account, "Hesab", "account"))
                .forEach { (icon, label, page) ->
                    IconButton(onClick = { onPage(page) }, enabled = page != "inventory" && page != "market" && page != "city",
                        modifier = Modifier.size(GameTokens.Target).background(if (page == "home") GameTokens.Accent else GameTokens.Surface,
                            RoundedCornerShape(GameTokens.ControlRadius))) { HomeIcon(icon, label) }
                }
        }
    }
}
