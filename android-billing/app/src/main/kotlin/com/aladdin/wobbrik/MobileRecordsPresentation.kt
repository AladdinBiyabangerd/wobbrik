package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.foundation.selection.selectableGroup
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
    val (portraits, retry) = rememberResourceArt(R.drawable.records_portraits_v3)
    CompositionLocalProvider(LocalRecordsPortraits provides portraits.images?.singleOrNull(),
        LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = GameTypeface)) {
        Column(Modifier.fillMaxSize().padding(horizontal = GameTokens.InventoryCollection.ScreenInset)
            .padding(top = GameTokens.Small, bottom = LocalCollectionDockClearance.current),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            // Optional illustration decode never gates leaderboard metadata or fitting.
            if(portraits.failed) TextButton(onClick = retry) { Text("Avatarları yenidən yüklə", color = GameTokens.Accent) }
            content()
        }
    }
}

@Composable
internal fun MobileRecordsControls(scope: LeaderboardScope, onScope: (LeaderboardScope) -> Unit,
    onRefresh: () -> Unit, refreshEnabled: Boolean, refreshing: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small),
        verticalAlignment = Alignment.CenterVertically) {
        val large = LocalDensity.current.fontScale > 1f
        val choices: @Composable (Modifier) -> Unit = { item ->
            RecordsScopeButton("Ümumi", scope == LeaderboardScope.ALL, item) { onScope(LeaderboardScope.ALL) }
            RecordsScopeButton("Həftəlik", scope == LeaderboardScope.WEEKLY, item) { onScope(LeaderboardScope.WEEKLY) }
        }
        val scroll = rememberScrollState()
        LaunchedEffect(scope, scroll.maxValue) { scroll.scrollTo(if(scope == LeaderboardScope.WEEKLY) scroll.maxValue else 0) }
        Row(Modifier.weight(1f).selectableGroup().then(if(large) Modifier.horizontalScroll(scroll) else Modifier)) {
            choices(if(large) Modifier.widthIn(min = GameTokens.Target + GameTokens.Inset) else Modifier.weight(1f))
        }

        Box(Modifier.testTag("records-refresh")) { CollectionRefresh(refreshEnabled, onRefresh) }
    }
}

@Composable
private fun RecordsScopeButton(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent,
        modifier = modifier.heightIn(min = GameTokens.Target).semantics { selected = active; role = Role.Tab }) {
        val shape = RoundedCornerShape(GameTokens.CardRadius)
        Box(Modifier.padding(vertical = GameTokens.Quarter).clip(shape)
            .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop,GameTokens.InventoryCollection.PanelBottom)))
            .border(if(active) GameTokens.Hairline else GameTokens.InventoryCollection.OutlineWidth,
                if(active) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape)
            .padding(horizontal = GameTokens.Small, vertical = GameTokens.Small), contentAlignment = Alignment.Center) {
            Text(label, color = if(active) GameTokens.Text else GameTokens.InventoryCollection.Metadata,
                fontFamily = GameTypeface, fontSize = GameTokens.Label, fontWeight = FontWeight.Bold, softWrap = false)
        }
    }
}

@Composable
internal fun MobileRecordsPodium(entries: List<LeaderboardEntry>) {
    Box(Modifier.fillMaxWidth().padding(top = GameTokens.Small)) {
        Canvas(Modifier.fillMaxWidth().height(GameTokens.Target + GameTokens.Inset).align(Alignment.BottomCenter)) { recordsGarden() }
        Row(Modifier.fillMaxWidth().padding(horizontal = GameTokens.Small).padding(bottom = GameTokens.RecordsPodium.GardenHeight), verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            listOf(entries[1], entries[0], entries[2]).forEach { entry ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    val winner = entry.rank == 1L
                    Box(Modifier.size(if(winner) GameTokens.Target + GameTokens.Inset else GameTokens.Target + GameTokens.Gap)) {
                        Canvas(Modifier.matchParentSize()) { recordsLaurel(winner) }
                        MobileRecordsAvatar(entry.avatarId, Modifier.fillMaxSize().padding(GameTokens.Quarter), if(winner) GameTokens.MobileHome.Gold else if(entry.rank == 2L) GameTokens.Muted else GameTokens.RecordsPodium.Bronze)
                    }
                    val metal = if(winner) GameTokens.MobileHome.Gold else if(entry.rank == 2L) GameTokens.Muted else GameTokens.RecordsPodium.Bronze
                    Box(Modifier.fillMaxWidth().padding(horizontal = GameTokens.Small).clip(RoundedCornerShape(GameTokens.RecordsPodium.BadgeRadius))
                        .background(Brush.verticalGradient(listOf(lerp(metal, GameTokens.Background, .55f), GameTokens.InventoryCollection.PanelBottom)))
                        .border(GameTokens.Hairline,metal,RoundedCornerShape(GameTokens.RecordsPodium.BadgeRadius))
                        .padding(horizontal = GameTokens.Quarter, vertical = GameTokens.Quarter), contentAlignment = Alignment.Center) {
                        Text(entry.displayName + if(entry.isSelf) " · Sən" else "", color = GameTokens.Text,
                            fontSize = GameTokens.Label, fontFamily = GameTypeface, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(Modifier.fillMaxWidth().padding(horizontal = GameTokens.Small).height(if(winner) GameTokens.Target * 2 else GameTokens.Target + GameTokens.Inset)) {
                        Canvas(Modifier.matchParentSize()) { recordsMetal(entry.rank, pedestal = true) }
                        Column(Modifier.fillMaxSize().padding(top = GameTokens.Small, bottom = GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.floors.toString(), color = GameTokens.Background, fontFamily = GameTypeface,
                                fontSize = GameTokens.Hint, fontWeight = FontWeight.ExtraBold)
                            Text(entry.rank.toString(), color = GameTokens.Background, fontFamily = GameTypeface,
                                fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
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
    AchievementPanel(modifier.fillMaxWidth(), selected = isSelf) {
        Column(Modifier.fillMaxWidth().padding(if(compact) GameTokens.Quarter else GameTokens.Small),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            if(longRank) Text("Yer ${leaderboardRankLabel(rank)}", color = GameTokens.Text, fontSize = GameTokens.Label)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if(!longRank) Text(leaderboardRankLabel(rank), Modifier.widthIn(min = GameTokens.Inset),
                    color = GameTokens.Text, fontSize = GameTokens.Hint, fontWeight = FontWeight.ExtraBold)
                MobileRecordsAvatar(avatar, Modifier.size(if(compact) GameTokens.Inset else GameTokens.Target - GameTokens.Small))
                Text(if(isSelf) "$name · Sən" else name, Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    softWrap = false, color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                if(!stacked) Text("$floors mərtəbə", color = GameTokens.InventoryCollection.Metadata,
                    fontSize = GameTokens.Label, fontWeight = FontWeight.Bold)
            }
            if(stacked) Text("$floors mərtəbə", Modifier.horizontalScroll(rememberScrollState()), softWrap = false,
                color = GameTokens.InventoryCollection.Metadata, fontSize = GameTokens.Label)
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
        3L -> { light = GameTokens.RecordsPodium.BronzeLight
            metal = GameTokens.RecordsPodium.Bronze
            shade = GameTokens.RecordsPodium.BronzeShade }
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
        drawOval(shade, Offset(edge, size.height - cap), Size(size.width - edge * 2, cap))
        drawOval(light.copy(alpha = .65f), Offset(edge * 2, size.height - cap), Size(size.width - edge * 4, cap * .62f), style = Stroke(edge))
        drawCircle(shade, size.height * .24f, Offset(size.width / 2, size.height * .69f))
        drawCircle(Brush.verticalGradient(listOf(light, metal)), size.height * .21f,
            Offset(size.width / 2, size.height * .69f))
        for(side in listOf(-1f, 1f)) for(leaf in 0..4) {
            val t = leaf / 4f
            val x = size.width * (.5f + side * (.28f + .08f * kotlin.math.sin(t * 2.2f)))
            val y = size.height * (.89f - t * .34f)
            rotate(side * (50f - t * 50f), Offset(x,y)) {
                val leafShape = Path().apply {
                    moveTo(x, y + size.height * .055f)
                    cubicTo(x - size.width * .065f, y, x - size.width * .04f, y - size.height * .06f, x, y - size.height * .065f)
                    cubicTo(x + size.width * .04f, y - size.height * .06f, x + size.width * .065f, y, x, y + size.height * .055f)
                    close()
                }
                drawPath(leafShape, Brush.horizontalGradient(listOf(metal, light), startX = x - size.width * .05f, endX = x + size.width * .05f))
                drawLine(shade.copy(alpha = .4f), Offset(x, y + size.height * .035f), Offset(x, y - size.height * .045f), edge)
            }
        }
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
        drawCircle(Brush.radialGradient(listOf(gold.copy(alpha = .48f), gold.copy(alpha = 0f))), size.minDimension * .5f)
        for (ray in 0..11) rotate(ray * 30f) {
            drawLine(gold.copy(alpha = .38f), Offset(size.width / 2, size.height * .02f),
                Offset(size.width / 2, size.height * .16f), edge)
        }
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
private fun MobileRecordsAvatar(id: String, modifier: Modifier, rim: Color = GameTokens.InventoryCollection.Metadata) {
    val sheet = LocalRecordsPortraits.current
    if(sheet == null) {
        Box(modifier, contentAlignment = Alignment.Center) { MobileMenuIcon(MobileIcon.Account, Modifier.size(GameTokens.Inset)) }
        return
    }
    val cell = GameTokens.Leaderboard.PortraitCell
    val index = when (id) { "pilot" -> 1; "architect" -> 2; else -> 0 }
    val painter = remember(sheet, index) { BitmapPainter(sheet, IntOffset(index * cell, 0), IntSize(cell, cell)) }
    Image(painter, "${avatarLabel(id)} avatarı", modifier.clip(CircleShape)
        .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.Background)))
        .border(GameTokens.Hairline, rim, CircleShape))
}

/** Bounded decorative garden: no bitmap allocations, reads or frame-driven state. */
private fun DrawScope.recordsGarden() {
    val p = GameTokens.RecordsPodium
    val edge = GameTokens.Hairline.toPx()
    val w = size.width
    val h = size.height
    drawOval(GameTokens.Background.copy(alpha = .65f), Offset(0f, h * .55f), Size(w, h * .45f))
    drawOval(Brush.verticalGradient(listOf(p.StoneLight, p.StoneShade)), Offset(0f, h * .48f), Size(w, h * .47f))
    drawOval(Brush.verticalGradient(listOf(p.Stone, p.StoneShade)), Offset(0f, h * .42f), Size(w, h * .40f))
    drawOval(p.StoneLight, Offset(edge, h * .43f), Size(w - edge * 2, h * .37f), style = Stroke(edge))
    for (i in 1..13) {
        val x = w * i / 14f
        drawLine(p.StoneShade, Offset(x, h * .67f), Offset(x - w * .018f, h * .85f), edge)
        drawLine(p.StoneLight.copy(alpha = .55f), Offset(x + edge, h * .69f), Offset(x - w * .018f + edge, h * .83f), edge)
    }
    for (side in listOf(-1f, 1f)) {
        val root = if (side < 0) w * .025f else w * .975f
        for (i in 0..9) {
            val x = root + side * w * ((i % 3) - 1) * .018f
            val y = h * (.58f - (i / 3) * .13f)
            val r = h * (.09f - (i % 3) * .009f)
            drawCircle(Brush.radialGradient(listOf(p.LeafLight, p.Leaf), center = Offset(x - r * .3f, y - r * .35f), radius = r * 1.5f), r, Offset(x, y))
        }
        for (i in 0..4) {
            val x = root + w * ((i % 3) - 1) * .022f
            val y = h * (.64f - (i / 3) * .13f)
            val r = h * .025f
            for (petal in 0..4) {
                val a = petal * kotlin.math.PI * 2 / 5
                drawCircle(if(i % 2 == 0) p.Flower else GameTokens.Text, r,
                    Offset(x + kotlin.math.cos(a).toFloat() * r * 1.4f, y + kotlin.math.sin(a).toFloat() * r * 1.4f))
            }
            drawCircle(GameTokens.MobileHome.Gold, r * .65f, Offset(x, y))
        }
    }
}
