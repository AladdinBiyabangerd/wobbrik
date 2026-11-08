package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
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
internal fun HomeScreen(snapshot: GameSnapshot?, active: Boolean, onPlay: (() -> Unit)?, onPage: (String) -> Unit,
                        status: String? = null, onRetry: () -> Unit = {}) {
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
                        .height(GameTokens.Target + GameTokens.Inset), contentScale = ContentScale.Fit, alignment = Alignment.CenterStart)
                    Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Background,
                        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                        Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                            HomeIcon(R.drawable.ui_coin)
                            Text(snapshot?.coins?.toString() ?: "—", color = GameTokens.Text, fontWeight = FontWeight.Bold)
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
                    HomeControls(snapshot, active, onPlay, onPage, status, onRetry, tablet, true, false,
                        Modifier.weight(1f).align(Alignment.Top).padding(top = GameTokens.Gap).verticalScroll(rememberScrollState()))
                } else Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    if (!tablet) HomeHeading()
                    HomeArt(Modifier.fillMaxWidth(), fitWidth = true)
                    HomeControls(snapshot, active, onPlay, onPage, status, onRetry, tablet, tablet, tablet, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun HomeArt(modifier: Modifier, fitWidth: Boolean = false) {
    val art = painterResource(R.drawable.home_city_cutout_v2)
    Image(art, null, if (fitWidth) modifier.aspectRatio(art.intrinsicSize.width / art.intrinsicSize.height) else modifier,
        contentScale = ContentScale.Fit)
}

@Composable
private fun HomeHeading(large: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.CenterHorizontally)) {
        HomeHeadingMark()
        Text("Öz şəhərini qur", Modifier.weight(1f, fill = false), color = GameTokens.Text,
            fontSize = if (large) GameTokens.Score else GameTokens.Title, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        HomeHeadingMark(mirrored = true)
    }
}

/** Separate rays preserve the approved ornament without depending on font glyphs. */
@Composable
private fun HomeHeadingMark(mirrored: Boolean = false) {
    val accent = GameTokens.Accent
    Canvas(Modifier.size(GameTokens.Gap, GameTokens.Inset)) {
        fun point(x: Float, y: Float) = Offset(size.width * (if (mirrored) 1f - x else x), size.height * y)
        drawLine(accent, point(0.1f, 0.15f), point(0.8f, 0.35f), GameTokens.Hairline.toPx() * 3, StrokeCap.Round)
        drawLine(accent, point(0.1f, 0.85f), point(0.8f, 0.65f), GameTokens.Hairline.toPx() * 3, StrokeCap.Round)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeControls(snapshot: GameSnapshot?, active: Boolean, onPlay: (() -> Unit)?, onPage: (String) -> Unit,
                         status: String?, onRetry: () -> Unit, tablet: Boolean, heading: Boolean, portraitTablet: Boolean, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
        if (heading) HomeHeading(large = tablet)
        if (status != null || snapshot == null) {
            Text(status ?: "Hesab və cəhd bərpa olunur…", color = GameTokens.Text,
                fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
            if (status == null || status == "Hesab və cəhd bərpa olunur…") {
                CircularProgressIndicator(color = GameTokens.Accent)
            }
            HomeButton(large = tablet, label = "Yenilə", primary = true, onClick = onRetry)
            return@Column
        }
        Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
            border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
            Column(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    HomeStat(R.drawable.ui_level, "Səviyyə ${snapshot.account?.level ?: "—"}", tablet)
                    HomeStat(R.drawable.ui_crown, "Rekord ${snapshot.account?.bestFloors ?: "—"}", tablet)
                }
                val interval = snapshot.account?.xpInterval
                if (interval != null) {
                    val earned = BigInteger.valueOf(snapshot.xp).subtract(interval.start)
                    val required = interval.next.subtract(interval.start)
                    Text("$earned / $required XP", Modifier.align(Alignment.CenterHorizontally), color = GameTokens.Muted, fontSize = GameTokens.Caption)
                    LinearProgressIndicator(progress = { BigDecimal(earned).divide(BigDecimal(required), MathContext.DECIMAL64).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(), color = GameTokens.Accent, trackColor = GameTokens.Background,
                        drawStopIndicator = {})
                } else Text("XP —", color = GameTokens.Muted)
            }
        }
        if (portraitTablet && androidx.compose.ui.platform.LocalDensity.current.fontScale <= 1f) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small),
                verticalAlignment = Alignment.CenterVertically) {
                HomeButton(large = tablet, label = if (active) "Cəhdə qayıt" else "Oyna", primary = true, showPlayIcon = true, enabled = onPlay != null,
                    modifier = Modifier.weight(1f), onClick = { onPlay?.invoke() })
                HomeButton(large = tablet, label = "Şəhərim", icon = R.drawable.ui_city, enabled = false,
                    modifier = Modifier.weight(1f).heightIn(min = GameTokens.Target * 2), onClick = { onPage("city") })
            }
        } else {
            HomeButton(large = tablet, label = if (active) "Cəhdə qayıt" else "Oyna", primary = true, showPlayIcon = true, enabled = onPlay != null, onClick = { onPlay?.invoke() })
            if (tablet) HomeButton(large = tablet, label = "Şəhərim", icon = R.drawable.ui_city, enabled = false, onClick = { onPage("city") })
        }
        if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1f) {
            HomeButton(large = tablet, label = "Kolleksiya", icon = R.drawable.ui_medal, onClick = { onPage("collection") })
            HomeButton(large = tablet, label = "Nailiyyətlər", icon = R.drawable.ui_trophy, onClick = { onPage("achievements") })
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            HomeButton(large = tablet, label = "Kolleksiya", icon = R.drawable.ui_medal, modifier = Modifier.weight(1f), onClick = { onPage("collection") })
            HomeButton(large = tablet, label = "Nailiyyətlər", icon = R.drawable.ui_trophy, modifier = Modifier.weight(1f), onClick = { onPage("achievements") })
        }
    }
}

@Composable
private fun HomeStat(icon: Int, text: String, large: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        HomeIcon(icon, large = large)
        Text(text, color = GameTokens.Text, fontSize = if (large) GameTokens.Title else GameTokens.Body, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeIcon(icon: Int, description: String? = null, large: Boolean = false) = Image(painterResource(icon), description,
    Modifier.size(if (large) GameTokens.Target else GameTokens.Gap * 2), contentScale = ContentScale.Fit)

@Composable
private fun HomeButton(label: String, icon: Int? = null, primary: Boolean = false, showPlayIcon: Boolean = false, enabled: Boolean = true, large: Boolean = false,
                       modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    Button(onClick, modifier = modifier.heightIn(min = if (large && primary) GameTokens.Target * 2 else if (large) GameTokens.Target + GameTokens.Inset else if (primary) GameTokens.Target + GameTokens.Gap else GameTokens.Target)
            .background(Brush.verticalGradient(if (primary) listOf(lerp(GameTokens.Accent, GameTokens.Text, 1f - GameTokens.DimOpacity), GameTokens.Accent) else listOf(GameTokens.Raised, GameTokens.Surface)), RoundedCornerShape(GameTokens.CardRadius))
            .drawWithContent {
                drawContent()
                if (primary && enabled) {
                    val inset = GameTokens.Hairline.toPx() * 3
                    drawRoundRect(brush = Brush.verticalGradient(listOf(GameTokens.Text, GameTokens.Accent)),
                        topLeft = Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(GameTokens.CardRadius.toPx() - inset),
                        style = Stroke(GameTokens.Hairline.toPx()))
                }
            },
        enabled = enabled, shape = RoundedCornerShape(GameTokens.CardRadius),
        border = BorderStroke(GameTokens.Hairline, if (primary) GameTokens.Text else GameTokens.Raised),
        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = if (primary) GameTokens.Background else GameTokens.Text), contentPadding = PaddingValues(GameTokens.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.CenterHorizontally)) {
            if (icon != null) HomeIcon(icon, large = large)
            if (showPlayIcon) Text("▶", fontSize = GameTokens.Title)
            Text(label, fontSize = if (primary) GameTokens.Score else if (large) GameTokens.Title else GameTokens.Body, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeRail(onPage: (String) -> Unit) {
    Surface(Modifier.width(GameTokens.Target + GameTokens.Inset).padding(top = GameTokens.Target),
        shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            listOf(Triple(R.drawable.ui_home, "Ana ekran", "home"), Triple(R.drawable.ui_city, "Şəhərim", "city"),
                Triple(R.drawable.ui_inventory, "İnventar", "inventory"), Triple(R.drawable.ui_market, "Bazar", "market"),
                Triple(R.drawable.ui_trophy, "Nailiyyətlər", "achievements"), Triple(R.drawable.ui_account, "Hesab", "account"))
                .forEach { (icon, label, page) ->
                    IconButton(onClick = { onPage(page) }, enabled = page != "inventory" && page != "market" && page != "city",
                        modifier = Modifier.size(GameTokens.Target + GameTokens.Small).background(if (page == "home") GameTokens.Accent else GameTokens.Surface,
                            RoundedCornerShape(GameTokens.ControlRadius))) { Image(painterResource(icon), label, Modifier.size(GameTokens.Target), contentScale = ContentScale.Fit) }
                }
        }
    }
}
