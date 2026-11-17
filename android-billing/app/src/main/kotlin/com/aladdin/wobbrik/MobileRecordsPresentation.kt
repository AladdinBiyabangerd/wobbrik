package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import appbackend.wobbrik.game.LeaderboardEntry
import appbackend.wobbrik.game.LeaderboardScope

private val LocalRecordsPortraits = staticCompositionLocalOf<ImageBitmap?> { null }

/** The shared shell owns the header and dock; this body owns only records content. */
@Composable
internal fun MobileRecordsFrame(content: @Composable ColumnScope.() -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    val portraits = ImageBitmap.imageResource(R.drawable.records_portraits_v3)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val bodyHeight = if (large) maxHeight.coerceAtLeast(GameTokens.MobileUi.CompactHeight) else maxHeight
        Box(Modifier.fillMaxSize().then(if (large) Modifier.verticalScroll(rememberScrollState()) else Modifier)) {
            CompositionLocalProvider(LocalRecordsPortraits provides portraits,
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = GameTypeface)) {
                Column(Modifier.fillMaxWidth().height(bodyHeight).padding(GameTokens.Gap),
                    verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = content)
            }
        }
    }
}

@Composable
internal fun MobileRecordsControls(scope: LeaderboardScope, onScope: (LeaderboardScope) -> Unit,
    onRefresh: () -> Unit, refreshEnabled: Boolean, refreshing: Boolean) {
    val large = LocalDensity.current.fontScale > 1f
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small),
        verticalAlignment = Alignment.Top) {
        val choices: @Composable (Modifier) -> Unit = { modifier ->
            RecordsScopeButton("Həftəlik", true, scope == LeaderboardScope.WEEKLY, modifier) { onScope(LeaderboardScope.WEEKLY) }
            RecordsScopeButton("Ümumi", false, scope == LeaderboardScope.ALL, modifier) { onScope(LeaderboardScope.ALL) }
        }
        if (large) Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            choices(Modifier.fillMaxWidth())
        } else Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            choices(Modifier.weight(1f))
        }
        Surface(onClick = onRefresh, enabled = refreshEnabled, color = Color.Transparent,
            shape = RoundedCornerShape(GameTokens.ControlRadius),
            modifier = Modifier.size(GameTokens.Target).testTag("records-refresh").semantics { contentDescription = "Yenilə" }) {
            MenuRelief(Modifier.fillMaxSize(), shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                if (refreshing) CircularProgressIndicator(Modifier.size(GameTokens.Inset).align(Alignment.Center),
                    color = GameTokens.Accent, strokeWidth = GameTokens.Hairline * 2)
                else Canvas(Modifier.size(GameTokens.Inset).align(Alignment.Center)) {
                    val stroke = GameTokens.Hairline.toPx() * 2
                    drawArc(GameTokens.Text, -55f, 290f, false, Offset(size.width * .15f, size.height * .15f),
                        Size(size.width * .7f, size.height * .7f), style = Stroke(stroke))
                    drawLine(GameTokens.Text, Offset(size.width * .85f, size.height * .05f), Offset(size.width * .85f, size.height * .35f), stroke)
                    drawLine(GameTokens.Text, Offset(size.width * .55f, size.height * .35f), Offset(size.width * .85f, size.height * .35f), stroke)
                }
            }
        }
    }
}

@Composable
private fun RecordsScopeButton(label: String, weekly: Boolean, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, shape = RoundedCornerShape(GameTokens.ControlRadius),
        modifier = modifier.heightIn(min = GameTokens.Target).semantics { selected = active }) {
        MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius),
            kind = if (active) MenuSkin.Selected else MenuSkin.Panel) {
            Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)
                .then(if (active) Modifier.background(Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop,
                    GameTokens.Accent, GameTokens.MobileHome.PlayBottom))) else Modifier)
                .padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterHorizontally)) {
                val ink = if (active) GameTokens.Background else GameTokens.Text
                if (weekly) Canvas(Modifier.size(GameTokens.Inset)) {
                    val stroke = GameTokens.Hairline.toPx() * 2
                    drawRect(ink, Offset(size.width * .15f, size.height * .22f), Size(size.width * .7f, size.height * .65f), style = Stroke(stroke))
                    drawLine(ink, Offset(size.width * .15f, size.height * .42f), Offset(size.width * .85f, size.height * .42f), stroke)
                    for (x in listOf(.3f, .7f)) drawLine(ink, Offset(size.width * x, size.height * .1f), Offset(size.width * x, size.height * .3f), stroke)
                } else MobileMenuIcon(MobileIcon.Crown, Modifier.size(GameTokens.Inset), tint = ink)
                Text(label, color = ink, fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun MobileRecordsPodium(entries: List<LeaderboardEntry>) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        listOf(entries[1], entries[0], entries[2]).forEach { entry ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Box(Modifier.size(if (entry.rank == 1L) GameTokens.Target * 2 else GameTokens.Target + GameTokens.Inset)) {
                    Canvas(Modifier.matchParentSize()) { recordsLaurel(entry.rank == 1L) }
                    MobileRecordsAvatar(entry.avatarId, Modifier.fillMaxSize())
                }
                Box(Modifier.fillMaxWidth().height(GameTokens.Target),
                    contentAlignment = Alignment.Center) {
                    Canvas(Modifier.matchParentSize()) { recordsMetal(entry.rank, pedestal = true) }
                    Text(entry.rank.toString(), color = GameTokens.Background, fontFamily = GameTypeface,
                        fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold)
                }
                Text(entry.displayName, color = GameTokens.Text, fontSize = GameTokens.Label, fontFamily = GameTypeface,
                    fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    MobileMenuIcon(MobileIcon.Crown, Modifier.size(GameTokens.Inset))
                    Text(entry.floors.toString(), color = GameTokens.Text, fontSize = GameTokens.Label, fontFamily = GameTypeface)
                }
                if (entry.isSelf) Text("Sən", color = GameTokens.Accent, fontSize = GameTokens.Caption, fontFamily = GameTypeface)
            }
        }
    }
}

@Composable
internal fun MobileRecordsRow(rank: Long, floors: Long, isSelf: Boolean, name: String, avatar: String,
    compact: Boolean, modifier: Modifier) {
    val large = LocalDensity.current.fontScale > 1f
    val stacked = large || floors.toString().length > GameTokens.Leaderboard.MaxPodiumDigits
    val longRank = rank.toString().length > GameTokens.Leaderboard.MaxPodiumDigits
    val ink = if (isSelf) GameTokens.Background else GameTokens.Text
    MenuRelief(modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius),
        kind = if (isSelf) MenuSkin.Selected else MenuSkin.Panel) {
        Column(Modifier.fillMaxWidth().then(if (isSelf) Modifier.background(Brush.verticalGradient(
            listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom))) else Modifier)
            .padding(if (compact) GameTokens.Quarter else GameTokens.Small),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            if (longRank) Text("Yer ${leaderboardRankLabel(rank)}", color = ink, fontFamily = GameTypeface, fontSize = GameTokens.Label)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if (!longRank) Box(Modifier.size(GameTokens.Target), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.matchParentSize()) { recordsMetal(rank, pedestal = false) }
                    Text(rank.toString(), color = if (rank in 1L..3L) GameTokens.Background else GameTokens.Text,
                        fontFamily = GameTypeface, fontSize = GameTokens.Label, fontWeight = FontWeight.ExtraBold)
                }
                MobileRecordsAvatar(avatar, Modifier.size(if (compact) GameTokens.Inset else GameTokens.Target))
                Column(Modifier.weight(1f)) {
                    Text(name, Modifier.horizontalScroll(rememberScrollState()), softWrap = false,
                        color = ink, fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                    if (isSelf) Text("Sən", color = ink, fontFamily = GameTypeface, fontSize = GameTokens.Caption)
                }
                if (!stacked) {
                    MobileMenuIcon(MobileIcon.Crown, Modifier.size(GameTokens.Inset))
                    Text(floors.toString(), color = ink, fontFamily = GameTypeface, fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { contentDescription = "$floors mərtəbə" })
                }
            }
            if (stacked) Text("$floors mərtəbə", color = ink, fontFamily = GameTypeface, fontSize = GameTokens.Label)
        }
    }
}

/** Metallic surfaces remain native; highlights reuse the approved menu palette. */
private fun DrawScope.recordsMetal(rank: Long, pedestal: Boolean) {
    val light: Color
    val metal: Color
    val shade: Color
    when (rank) {
        1L -> { light = GameTokens.MobileHome.GoldLight; metal = GameTokens.MobileHome.Gold
            shade = lerp(metal, GameTokens.Background, .28f) }
        2L -> { light = GameTokens.Text; metal = GameTokens.Muted
            shade = GameTokens.MobileHome.Rim }
        3L -> { light = lerp(GameTokens.MobileHome.GoldLight, GameTokens.Danger, .55f)
            metal = lerp(GameTokens.MobileHome.Gold, GameTokens.Danger, .42f)
            shade = lerp(metal, GameTokens.Background, .38f) }
        else -> { light = GameTokens.MobileHome.RimLight; metal = GameTokens.Raised
            shade = GameTokens.MobileHome.PanelBottom }
    }
    val edge = GameTokens.Hairline.toPx()
    if (pedestal) {
        val cap = size.height * .22f
        val body = Path().apply {
            moveTo(edge, cap / 2)
            lineTo(size.width - edge, cap / 2)
            lineTo(size.width - edge, size.height - cap / 2)
            cubicTo(size.width - edge, size.height + cap / 6, edge, size.height + cap / 6, edge, size.height - cap / 2)
            close()
        }
        drawPath(body, Brush.horizontalGradient(listOf(shade, metal, light, metal, shade)))
        drawPath(body, shade, style = Stroke(edge))
        drawOval(Brush.verticalGradient(listOf(light, metal)), Offset(edge, edge), Size(size.width - edge * 2, cap))
        drawOval(light, Offset(edge, edge), Size(size.width - edge * 2, cap), style = Stroke(edge))
        for (x in listOf(.09f, .84f)) drawLine(light.copy(alpha = .55f),
            Offset(size.width * x, cap), Offset(size.width * x, size.height - cap), edge)
        drawCircle(shade, size.height * .35f, Offset(size.width / 2, size.height * .55f))
        drawCircle(Brush.verticalGradient(listOf(light, metal)), size.height * .31f,
            Offset(size.width / 2, size.height * .53f))
    } else {
        val radius = size.minDimension / 2 - edge
        drawCircle(Brush.verticalGradient(listOf(light, metal, shade)), radius)
        drawCircle(light, radius, style = Stroke(edge))
        drawCircle(shade, radius * .82f, style = Stroke(edge))
        drawArc(light.copy(alpha = .8f), 200f, 110f, false,
            Offset(size.width / 2 - radius * .88f, size.height / 2 - radius * .88f),
            Size(radius * 1.76f, radius * 1.76f), style = Stroke(edge))
    }
}

private fun DrawScope.recordsLaurel(winner: Boolean) {
    val edge = GameTokens.Hairline.toPx()
    val gold = GameTokens.MobileHome.Gold
    val leafBase = if (winner) gold else GameTokens.Success
    val leafLight = if (winner) GameTokens.MobileHome.GoldLight else lerp(GameTokens.Success, gold, .6f)
    if (winner) {
        drawCircle(Brush.verticalGradient(listOf(GameTokens.MobileHome.GoldLight, gold)),
            size.minDimension * .41f, style = Stroke(edge * 2))
    }
    for (side in listOf(-1f, 1f)) {
        val stem = Path().apply {
            moveTo(size.width * (.5f + side * .12f), size.height * .94f)
            quadraticTo(size.width * (.5f + side * .56f), size.height * .88f,
                size.width * (.5f + side * .42f), size.height * .58f)
        }
        drawPath(stem, leafBase, style = Stroke(edge))
        for (leaf in 0..5) {
            val t = leaf / 5f
            val x = size.width * (.5f + side * (.26f + .17f * kotlin.math.sin(t * 2.2f)))
            val y = size.height * (.92f - t * .30f)
            rotate(side * (48f - t * 65f), Offset(x, y)) {
                val shape = Path().apply {
                    moveTo(x, y + size.height * .055f)
                    cubicTo(x - size.width * .045f, y, x - size.width * .035f, y - size.height * .04f, x, y - size.height * .065f)
                    cubicTo(x + size.width * .035f, y - size.height * .04f, x + size.width * .045f, y, x, y + size.height * .055f)
                }
                drawPath(shape, Brush.horizontalGradient(listOf(leafBase, leafLight),
                    startX = x - size.width * .045f, endX = x + size.width * .045f))
                drawLine(leafLight, Offset(x, y + size.height * .04f), Offset(x, y - size.height * .045f), edge)
            }
        }
    }
}

@Composable
private fun MobileRecordsAvatar(id: String, modifier: Modifier) {
    val sheet = requireNotNull(LocalRecordsPortraits.current)
    val cell = GameTokens.Leaderboard.PortraitCell
    val index = when (id) { "pilot" -> 1; "architect" -> 2; else -> 0 }
    val painter = remember(sheet, index) { BitmapPainter(sheet, IntOffset(index * cell, 0), IntSize(cell, cell)) }
    Image(painter, "${avatarLabel(id)} avatarı", modifier)
}
