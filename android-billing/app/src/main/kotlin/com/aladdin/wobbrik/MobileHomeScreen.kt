package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import appbackend.wobbrik.game.GameSnapshot
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

/** Portrait Home: accepted server stats and existing routes; artwork is decorative. */
@Composable
internal fun MobileHomeScreen(snapshot: GameSnapshot?, active: Boolean, onPlay: (() -> Unit)?,
    onPage: (String) -> Unit, status: String?, onRetry: () -> Unit, journalBlocked: Boolean) {
    val largeFont = LocalDensity.current.fontScale > 1f
    val accepted = snapshot != null && status == null && !journalBlocked
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        MobileHomeHeader(snapshot?.coins, onPage)
        BoxWithConstraints(Modifier.weight(1f)) {
            val available = maxHeight
            Column(Modifier.fillMaxSize().then(if (largeFont || !accepted) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Öz şəhərini qur", color = GameTokens.Text, fontSize = GameTokens.Title,
                    fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = GameTokens.Small))
                if (accepted) {
                    if (!largeFont) Image(painterResource(R.drawable.home_city_cutout_v2), null,
                        Modifier.fillMaxWidth().weight(1f).heightIn(max = GameTokens.MobileHome.HeroMax), contentScale = ContentScale.Fit)
                    else if (available > GameTokens.PanelWidth) Image(painterResource(R.drawable.home_city_cutout_v2), null,
                        Modifier.fillMaxWidth().height(GameTokens.MobileHome.LargeFontHero), contentScale = ContentScale.Fit)
                    MobileHomeStats(requireNotNull(snapshot), largeFont)
                } else {
                    val message = status ?: "Hesab və cəhd bərpa olunur…"
                    Text(message, color = GameTokens.Text, fontSize = GameTokens.Hint, fontWeight = FontWeight.Bold)
                    if (message == "Hesab və cəhd bərpa olunur…") CircularProgressIndicator(color = GameTokens.Accent)
                    if (journalBlocked) Text(JournalRecoveryCopy.Description, color = GameTokens.Muted, fontSize = GameTokens.Body)
                    HomeButton(if (journalBlocked) JournalRecoveryCopy.Retry else "Yenilə", primary = true,
                        compact = true, onClick = onRetry)
                }
            }
        }
        MobileHomeNavbar(active = active, enabled = !journalBlocked && onPlay != null,
            onPlay = { onPlay?.invoke() }, onPage = onPage)
    }
}

@Composable
private fun MobileHomeHeader(coins: Long?, onPage: (String) -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        val largeBalance = (coins?.toString()?.length ?: 1) > 6
        // Long real balances retain all digits without crowding the account target or logo.
        Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.weight(1f).height(GameTokens.Target),
                    contentScale = ContentScale.Fit, alignment = Alignment.CenterStart)
                if (!largeBalance) MobileHomeBalance(coins)
                Surface(onClick = { onPage("account") }, modifier = Modifier.size(GameTokens.Target),
                    color = GameTokens.Raised, shape = CircleShape) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.ui_account), "Hesab", Modifier.size(GameTokens.Inset))
                    }
                }
            }
            if (largeBalance) MobileHomeBalance(coins)
        }
    }
}

@Composable
private fun MobileHomeBalance(coins: Long?) {
    Row(Modifier.background(GameTokens.Background, RoundedCornerShape(GameTokens.CardRadius)).padding(GameTokens.Small),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        Image(painterResource(R.drawable.ui_coin), null, Modifier.size(GameTokens.Inset))
        Text(coins?.toString() ?: "—", Modifier.weight(1f, fill = false), color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MobileHomeStats(snapshot: GameSnapshot, largeFont: Boolean) {
    Surface(color = GameTokens.Background.copy(alpha = GameTokens.DimOpacity), shape = RoundedCornerShape(GameTokens.ControlRadius)) {
        Column(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            if (largeFont) {
                MobileHomeStat(R.drawable.ui_level, "Səviyyə ${snapshot.account?.level ?: "—"}")
                MobileHomeStat(R.drawable.ui_crown, "Rekord ${snapshot.account?.bestFloors ?: "—"}")
            } else FlowStats(snapshot)
            snapshot.account?.xpInterval?.let { interval ->
                val earned = BigInteger.valueOf(snapshot.xp).subtract(interval.start)
                val required = interval.next.subtract(interval.start)
                Text("$earned / $required XP", Modifier.align(Alignment.CenterHorizontally),
                    color = GameTokens.Muted, fontSize = GameTokens.Caption)
                LinearProgressIndicator(progress = { BigDecimal(earned).divide(BigDecimal(required), MathContext.DECIMAL64).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(), color = GameTokens.Accent, trackColor = GameTokens.Background, drawStopIndicator = {})
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowStats(snapshot: GameSnapshot) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        MobileHomeStat(R.drawable.ui_level, "Səviyyə ${snapshot.account?.level ?: "—"}")
        Box(Modifier.width(GameTokens.Hairline).height(GameTokens.Inset).background(GameTokens.Raised))
        MobileHomeStat(R.drawable.ui_crown, "Rekord ${snapshot.account?.bestFloors ?: "—"}")
    }
}

@Composable
private fun MobileHomeStat(icon: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Image(painterResource(icon), null, Modifier.size(GameTokens.Inset))
        Text(label, color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MobileHomeNavbar(active: Boolean, enabled: Boolean, onPlay: () -> Unit, onPage: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val largeFont = LocalDensity.current.fontScale > 1f
        val labels = !largeFont && maxWidth >= GameTokens.MobileHome.NavbarLabelWidth
        val barHeight = if (largeFont) GameTokens.MobileHome.LargeFontNavbarHeight else GameTokens.MobileHome.NavbarHeight
        Box(Modifier.fillMaxWidth().height(barHeight + GameTokens.Gap)) {
            Surface(Modifier.fillMaxWidth().height(barHeight).align(Alignment.BottomCenter),
                shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
                border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                Row(Modifier.fillMaxSize().padding(horizontal = GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
                    MobileHomeNavItem("Şəhər", "Şəhərim", R.drawable.ui_city, labels, Modifier.weight(1f)) { onPage("city") }
                    MobileHomeNavItem("İnventar", "İnventar", R.drawable.ui_inventory, labels, Modifier.weight(1f)) { onPage("inventory") }
                    Spacer(Modifier.width(GameTokens.MobileHome.PlaySlot))
                    MobileHomeNavItem("Kolleksiya", "Kolleksiya", R.drawable.ui_medal, labels, Modifier.weight(1f)) { onPage("collection") }
                    MobileHomeNavItem("Uğurlar", "Nailiyyətlər", R.drawable.ui_trophy, labels, Modifier.weight(1f)) { onPage("achievements") }
                }
            }
            Column(Modifier.width(GameTokens.MobileHome.PlaySlot).align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                val foreground = if (enabled) GameTokens.Background else GameTokens.Muted
                val background = if (enabled) GameTokens.Accent else GameTokens.Raised
                Surface(onClick = onPlay, enabled = enabled,
                    modifier = Modifier.size(GameTokens.MobileHome.PlayDiameter).semantics { contentDescription = if (active) "Cəhdə qayıt" else "Oyunu başlat" },
                    shape = CircleShape, color = background,
                    border = BorderStroke(GameTokens.Hairline * 2, if (enabled) GameTokens.Text else GameTokens.Raised), shadowElevation = GameTokens.Small) {
                    Box(Modifier.background(Brush.verticalGradient(listOf(if (enabled) lerp(GameTokens.Accent, GameTokens.Text,
                        1f - GameTokens.DimOpacity) else background, background))), contentAlignment = Alignment.Center) {
                        Text("▶", fontSize = GameTokens.Title, color = foreground, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Oyna", fontSize = GameTokens.Caption, color = if (enabled) GameTokens.Text else GameTokens.Muted,
                    fontWeight = FontWeight.Bold, softWrap = false)
            }
        }
    }
}

@Composable
private fun MobileHomeNavItem(label: String, description: String, icon: Int, showLabel: Boolean,
    modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = GameTokens.Target).semantics { contentDescription = description },
        shape = RoundedCornerShape(GameTokens.ControlRadius), color = Color.Transparent) {
        Column(Modifier.padding(vertical = GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterVertically)) {
            Image(painterResource(icon), null, Modifier.size(GameTokens.Inset))
            if (showLabel) Text(label, fontSize = GameTokens.Caption, color = GameTokens.Muted, softWrap = false)
        }
    }
}
