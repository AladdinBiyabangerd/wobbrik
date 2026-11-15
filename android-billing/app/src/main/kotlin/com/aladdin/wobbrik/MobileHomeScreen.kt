package com.aladdin.wobbrik

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
        if (!LocalMobileMenuShell.current) MobileMenuHeader(snapshot?.coins)
        BoxWithConstraints(Modifier.weight(1f)) {
            val available = maxHeight
            Column(Modifier.fillMaxSize().then(if (largeFont || !accepted) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(fontFamily = GameTypeface, text = "Öz şəhərini qur", color = GameTokens.Text, fontSize = GameTokens.Title,
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
                    Text(fontFamily = GameTypeface, text = message, color = GameTokens.Text, fontSize = GameTokens.Hint, fontWeight = FontWeight.Bold)
                    if (message == "Hesab və cəhd bərpa olunur…") CircularProgressIndicator(color = GameTokens.Accent)
                    if (journalBlocked) Text(fontFamily = GameTypeface, text = JournalRecoveryCopy.Description, color = GameTokens.Muted, fontSize = GameTokens.Body)
                    HomeButton(if (journalBlocked) JournalRecoveryCopy.Retry else "Yenilə", primary = true,
                        compact = true, onClick = onRetry)
                }
            }
        }
        if (!LocalMobileMenuShell.current) MobileMenuNavbar(page = "home", active = active, enabled = !journalBlocked && onPlay != null,
            onPlay = { onPlay?.invoke() }, onPage = onPage)
    }
}

/** Shared menu header: profile is reached through the dock, balance stays right aligned. */
@Composable
internal fun MobileMenuHeader(coins: Long?) {
    val largeBalance = (coins?.toString()?.length ?: 1) > 6
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.weight(1f).height(GameTokens.Target),
                contentScale = ContentScale.Fit, alignment = Alignment.CenterStart)
            if (!largeBalance) MobileHomeBalance(coins)
        }
        if (largeBalance) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { MobileHomeBalance(coins) }
    }
}

@Composable
private fun MobileHomeBalance(coins: Long?) {
    Row(Modifier.background(GameTokens.Background, RoundedCornerShape(GameTokens.CardRadius)).padding(GameTokens.Small),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        Image(painterResource(R.drawable.ui_coin), null, Modifier.size(GameTokens.Inset))
        Text(fontFamily = GameTypeface, text = coins?.toString() ?: "—", modifier = Modifier.weight(1f, fill = false), color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
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
                Text(fontFamily = GameTypeface, text = "$earned / $required XP", modifier = Modifier.align(Alignment.CenterHorizontally),
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
        Text(fontFamily = GameTypeface, text = label, color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.SemiBold)
    }
}
