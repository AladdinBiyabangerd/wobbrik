package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
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
    val (art, retryArt) = rememberResourceArt(R.drawable.home_city_cutout_v3)
    val city = art.images?.single()
    val cityPainter = remember(city) { city?.let { BitmapPainter(it,
        IntOffset(GameTokens.MobileHome.HeroLeft, 0), IntSize(GameTokens.MobileHome.HeroWidth, it.height)) } }
    val accepted = snapshot != null && status == null && !journalBlocked
    val scenery = rememberResourceArt(R.drawable.inventory_collection_header).first.images?.singleOrNull()
    Column(Modifier.fillMaxSize().background(GameTokens.InventoryCollection.Background)
        .safeDrawingPadding().consumeWindowInsets(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally) {
        CollectionHeader(snapshot?.coins, scenery)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().clipToBounds()
            .padding(horizontal = GameTokens.InventoryCollection.ScreenInset)) {
            val compact = maxHeight < GameTokens.MobileUi.CompactHeight
            Column(Modifier.fillMaxSize().then(if (largeFont || !accepted) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Öz şəhərini qur", fontFamily = GameTypeface, color = GameTokens.Text,
                    fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = GameTokens.Inset))
                if (accepted) {
                    BoxWithConstraints(if (largeFont) Modifier.fillMaxWidth().height(GameTokens.MobileHome.LargeFontHero)
                        else Modifier.fillMaxWidth().weight(1f).clipToBounds(), contentAlignment = Alignment.Center) {
                        val heroWidth = minOf(maxWidth, maxHeight * GameTokens.MobileHome.HeroAspectRatio)
                        if (cityPainter != null) Image(cityPainter, null,
                            Modifier.size(heroWidth, heroWidth / GameTokens.MobileHome.HeroAspectRatio).testTag("home-city-hero"),
                            contentScale = ContentScale.FillBounds)
                    }
                    if (art.failed) TextButton(onClick = retryArt) { Text("Qrafikanı yenilə", color = GameTokens.Accent) }
                    HomeCollectionStats(requireNotNull(snapshot), largeFont)
                    if (!largeFont && !compact) Spacer(Modifier.height(GameTokens.MobileHome.StatBottomSpace + GameTokens.Inset + GameTokens.Small))
                } else {
                    val message = status ?: "Hesab və cəhd bərpa olunur…"
                    Text(message, fontFamily = GameTypeface, color = GameTokens.Text, fontSize = GameTokens.Hint, fontWeight = FontWeight.Bold)
                    if (message == "Hesab və cəhd bərpa olunur…") CircularProgressIndicator(color = GameTokens.Accent)
                    if (journalBlocked) Text(JournalRecoveryCopy.Description, fontFamily = GameTypeface, color = GameTokens.Muted, fontSize = GameTokens.Body)
                    NativeProfileAction(if (journalBlocked) JournalRecoveryCopy.Retry else "Yenilə", enabled = true, modifier = Modifier.fillMaxWidth(), onClick = onRetry)
                }
            }
        }
        Box(Modifier.padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter)) {
            CollectionNavbar(if (!journalBlocked) onPlay else null, onPage, "home", active)
        }
    }
}

/** Home-only collection panel; shared achievement stats keep their existing presentation. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeCollectionStats(snapshot: GameSnapshot, largeFont: Boolean) {
    ProfileCollectionPanel {
        Column(Modifier.padding(GameTokens.InventoryCollection.CardInset),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally) {
            if (largeFont) Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                MobileHomeStat(MobileIcon.City, "Səviyyə ${snapshot.account?.level ?: "—"}")
                MobileHomeStat(MobileIcon.Crown, "Rekord ${snapshot.account?.bestFloors ?: "—"}")
            } else FlowStats(snapshot)
            val interval = snapshot.account?.xpInterval
            if (interval == null) Text("XP —", color = GameTokens.Muted, fontFamily = GameTypeface)
            else {
                val earned = BigInteger.valueOf(snapshot.xp).subtract(interval.start)
                val required = interval.next.subtract(interval.start)
                val remaining = interval.next.subtract(BigInteger.valueOf(snapshot.xp)).max(BigInteger.ZERO)
                val progress = if (required.signum() > 0) BigDecimal(earned).divide(BigDecimal(required), MathContext.DECIMAL64).toFloat().coerceIn(0f, 1f) else 0f
                val label = "$earned / $required XP"
                val shape = RoundedCornerShape(GameTokens.CardRadius)
                val style = TextStyle(fontFamily = GameTypeface, fontSize = GameTokens.Label, fontWeight = FontWeight.ExtraBold)
                val labelPixels = rememberTextMeasurer().measure(label, style).size.width
                val labelWidth = with(LocalDensity.current) { labelPixels.toDp() } + GameTokens.Small * 2
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val insideFill = maxWidth * progress >= labelWidth
                    Box(Modifier.fillMaxWidth().clip(shape).background(GameTokens.InventoryCollection.PanelBottom)
                        .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }
                        .testTag("home-xp-progress"), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.matchParentSize()) {
                            drawRoundRect(Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom)),
                                size = Size(size.width * progress, size.height), cornerRadius = CornerRadius(GameTokens.CardRadius.toPx()))
                        }
                        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(if (insideFill) progress else 1f), contentAlignment = Alignment.Center) {
                            Text(label, Modifier.padding(GameTokens.Quarter)
                                .then(if (insideFill) Modifier else Modifier.background(GameTokens.InventoryCollection.PanelBottom, shape))
                                .padding(horizontal = GameTokens.Small).horizontalScroll(rememberScrollState()),
                                color = if (insideFill) GameTokens.Background else GameTokens.Text,
                                style = style, softWrap = false)
                        }
                    }
                }
                Text("Növbəti səviyyəyə $remaining XP", Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Label,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, softWrap = false)
            }
        }
    }
}

/** Shared menu header: profile is reached through the dock, balance stays right aligned. */
@Composable
internal fun MobileMenuHeader(coins: Long?, collectionTrial: Boolean = false) {
    val (logoArt, retryLogo) = LocalMenuLogo.current ?: rememberResourceArt(R.drawable.wobbrik_logo)
    val logo = logoArt.images?.singleOrNull()
    val largeBalance = (coins?.toString()?.length ?: 1) > 6
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Box(Modifier.weight(1f).height(GameTokens.Target), contentAlignment = Alignment.CenterStart) {
                if (logo != null) Image(logo, "Wobbrik", Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit, alignment = Alignment.CenterStart)
                else if (logoArt.failed) TextButton(onClick = retryLogo) {
                    Text("Wobbrik", color = GameTokens.Text, fontFamily = GameTypeface)
                }
            }
            if (!largeBalance) MobileHomeBalance(coins, collectionTrial)
        }
        if (largeBalance) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { MobileHomeBalance(coins, collectionTrial) }
    }
}

@Composable
private fun MobileHomeBalance(coins: Long?, collectionTrial: Boolean = false) {
    if (collectionTrial) {
        androidx.compose.material3.Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius),
            border = androidx.compose.foundation.BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
            Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Inset))
                Text(coins?.toString() ?: "—", Modifier.weight(1f, fill = false).horizontalScroll(rememberScrollState()),
                    color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body,
                    fontWeight = FontWeight.Bold, softWrap = false)
            }
        }
        return
    }
    MenuRelief(shape = RoundedCornerShape(GameTokens.CardRadius), kind = MenuSkin.Wallet) {
    Row(Modifier.padding(GameTokens.Small),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Inset))
        Text(fontFamily = GameTypeface, text = coins?.toString() ?: "—", modifier = Modifier.weight(1f, fill = false).horizontalScroll(rememberScrollState()), softWrap = false, color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
    }
    }
}

@Composable
internal fun MobileHomeStats(snapshot: GameSnapshot, largeFont: Boolean, achievementBadge: Boolean = false) {
    MenuRelief(shape = RoundedCornerShape(GameTokens.ControlRadius), kind = MenuSkin.Stats) {
        Row(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        if (achievementBadge) AchievementLevelBadge(snapshot.account?.level)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            if (largeFont) {
                MobileHomeStat(if(achievementBadge) null else MobileIcon.City, "Səviyyə ${snapshot.account?.level ?: "—"}")
                MobileHomeStat(MobileIcon.Crown, "Rekord ${snapshot.account?.bestFloors ?: "—"}")
            } else FlowStats(snapshot, achievementBadge)
            snapshot.account?.xpInterval?.let { interval ->
                val earned = BigInteger.valueOf(snapshot.xp).subtract(interval.start)
                val required = interval.next.subtract(interval.start)
                val progress = BigDecimal(earned).divide(BigDecimal(required), MathContext.DECIMAL64).toFloat().coerceIn(0f, 1f)
                val label = "$earned / $required XP"
                val style = TextStyle(fontFamily = GameTypeface, fontSize = GameTokens.Label, fontWeight = FontWeight.ExtraBold)
                val measured = rememberTextMeasurer().measure(label, style).size.width
                val labelWidth = with(LocalDensity.current) { measured.toDp() } + GameTokens.Small * 2
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val insideFill = maxWidth * progress >= labelWidth
                    MenuRelief(Modifier.fillMaxWidth().heightIn(min = GameTokens.Inset)
                        .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
                        shape = RoundedCornerShape(GameTokens.CardRadius), kind = MenuSkin.Track) {
                        Canvas(Modifier.matchParentSize()) {
                            drawRoundRect(Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom)),
                                size = Size(size.width * progress, size.height), cornerRadius = CornerRadius(GameTokens.CardRadius.toPx()))
                        }
                        Box(Modifier.fillMaxWidth(if (insideFill) progress else 1f), contentAlignment = if (insideFill) Alignment.CenterEnd else Alignment.Center) {
                            Text(label, modifier = Modifier.then(if (insideFill) Modifier else Modifier.background(GameTokens.MobileHome.PanelBottom,
                                RoundedCornerShape(GameTokens.CardRadius))).padding(horizontal = GameTokens.Small),
                                color = if (insideFill) GameTokens.Background else GameTokens.Text, style = style)
                        }
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowStats(snapshot: GameSnapshot, achievementBadge: Boolean = false) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if(achievementBadge) GameTokens.Small else GameTokens.Gap, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        MobileHomeStat(if(achievementBadge) null else MobileIcon.City, "Səviyyə ${snapshot.account?.level ?: "—"}")
        Box(Modifier.width(GameTokens.Hairline).height(GameTokens.Inset).background(GameTokens.Raised))
        MobileHomeStat(MobileIcon.Crown, "Rekord ${snapshot.account?.bestFloors ?: "—"}")
    }
}

@Composable
private fun MobileHomeStat(icon: MobileIcon?, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        if(icon != null) MobileMenuIcon(icon, Modifier.size(GameTokens.Inset))
        Text(fontFamily = GameTypeface, text = label, color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.SemiBold)
    }
}
