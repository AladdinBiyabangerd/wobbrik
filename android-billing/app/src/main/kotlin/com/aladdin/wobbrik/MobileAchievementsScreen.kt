package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import appbackend.wobbrik.game.GameSnapshot

/** Read-only accepted progress; threshold completion never grants an award locally. */
@Composable
internal fun MobileAchievementsScreen(snapshot: GameSnapshot, onHome: () -> Unit) {
    BackHandler(onBack = onHome)
    val (art, retry) = rememberResourceArt(R.drawable.achievements_collection_art)
    val rows = remember(snapshot) {
        val awards = snapshot.account?.achievements.orEmpty().toSet()
        val progress = snapshot.account?.achievementProgress.orEmpty()
        val groups = progress.groupBy { it.category }.values.toList()
        val ordered = (0 until (groups.maxOfOrNull { it.size } ?: 0)).flatMap { tier -> groups.mapNotNull { it.getOrNull(tier) } }
        val known = progress.mapTo(HashSet()) { it.id }
        ordered.map { AchievementRow(it.id, it.category, it.threshold, it.current, it.id in awards) } +
            awards.filter { it !in known }.map { AchievementRow(it, it.substringBeforeLast('_'), null, null, true) }
    }
    val large = LocalDensity.current.fontScale > 1f
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < GameTokens.MobileUi.CompactHeight
        CompositionLocalProvider(LocalAchievementArt provides art.images?.singleOrNull()) {
            LazyColumn(Modifier.fillMaxSize().testTag("awards-list").padding(horizontal = GameTokens.InventoryCollection.ScreenInset),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small),
                contentPadding = PaddingValues(top = GameTokens.Small, bottom = LocalCollectionDockClearance.current)) {
                item("hero") {
                    Box(Modifier.fillMaxWidth().height(if(large) GameTokens.InventoryCollection.LargeTypeArtHeight else
                        if(compact) GameTokens.InventoryCollection.CompactArtHeight else GameTokens.InventoryCollection.ArtHeight),
                        contentAlignment = Alignment.Center) {
                        if(art.images == null) ApprovedArtStatus(if(art.failed) ArtPreparation.Failed else ArtPreparation.Loading, retry)
                        else AchievementIllustration(0, Modifier.fillMaxSize())
                    }
                }
                item("summary") { AchievementSummary(snapshot, large) }
                item("heading") { Text("Nailiyyətlər", color = GameTokens.Text, fontFamily = GameTypeface,
                    fontSize = GameTokens.OwnedDetail.Title, fontWeight = FontWeight.ExtraBold) }
                if(snapshot.account?.achievementProgress == null) item("unavailable") {
                    Text("Pillələrin irəliləyişi bu hesab məlumatında yoxdur. Yenilənmiş server məlumatı tələb olunur.",
                        color = GameTokens.Muted, fontSize = GameTokens.Label)
                }
                if(rows.isEmpty()) item("empty") { AchievementPanel(Modifier.fillMaxWidth()) {
                    Text("Hələ açılmış mükafat yoxdur. Oyunu tamamlayaraq nailiyyətlər qazan.",
                        Modifier.padding(GameTokens.Gap), color = GameTokens.Muted, fontSize = GameTokens.Body)
                } }
                items(rows, key = { it.id }) { AchievementCard(it, large) }
            }
        }
    }
}

private val LocalAchievementArt = staticCompositionLocalOf<ImageBitmap?> { null }

@Composable
private fun AchievementIllustration(cell: Int, modifier: Modifier) {
    val sheet = LocalAchievementArt.current ?: return
    val w = sheet.width / 2; val h = sheet.height / 3
    val painter = remember(sheet, cell) {
        if(cell == 0) BitmapPainter(sheet, IntOffset(w / 20, h / 7), IntSize(w * 9 / 10, h * 3 / 4))
        else BitmapPainter(sheet, IntOffset(cell % 2 * w, cell / 2 * h), IntSize(w,h))
    }
    Image(painter, null, modifier)
}

@Composable
private fun AchievementSummary(snapshot: GameSnapshot, large: Boolean) {
    AchievementPanel(Modifier.fillMaxWidth()) {
        val label: @Composable () -> Unit = {
            Text("${snapshot.account?.achievements?.size ?: 0} nailiyyət açılıb", color = GameTokens.Text,
                fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.ExtraBold)
        }
        if(large) Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            label()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Box(Modifier.size(GameTokens.Target), contentAlignment = Alignment.Center) {
                    AchievementIllustration(1, Modifier.fillMaxSize())
                }
                Text("Səviyyə ${snapshot.account?.level ?: "—"}", Modifier.weight(1f), color = GameTokens.Text, fontSize = GameTokens.Label)
            }
            Row {
                Text("Rekord ${snapshot.account?.bestFloors ?: "—"}", color = GameTokens.InventoryCollection.Metadata, fontSize = GameTokens.Label)
            }
        } else Row(Modifier.padding(horizontal = GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Box(Modifier.weight(1f)) { label() }
            Box(Modifier.size(GameTokens.Target), contentAlignment = Alignment.Center) {
                AchievementIllustration(1, Modifier.fillMaxSize())
                Text(snapshot.account?.level?.toString() ?: "—", color = GameTokens.Text, fontFamily = GameTypeface,
                    fontSize = GameTokens.Hint, fontWeight = FontWeight.ExtraBold)
            }
            Text("Rekord ${snapshot.account?.bestFloors ?: "—"}", Modifier.width(GameTokens.Target * 2 + GameTokens.Small),
                color = GameTokens.InventoryCollection.Metadata, fontSize = GameTokens.Label, fontWeight = FontWeight.Bold)
        }
    }
}

private data class AchievementRow(val id: String, val category: String, val target: Long?, val current: Long?, val completed: Boolean)

@Composable
private fun AchievementCard(row: AchievementRow, large: Boolean) {
    AchievementPanel(Modifier.fillMaxWidth().testTag("award-${row.id}")) {
        Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            AchievementIllustration(when { row.id == "height_10" -> 5; row.category == "height" -> 2;
                row.category == "precise" -> 3; else -> 4 },
                Modifier.size(if(large) GameTokens.Target else GameTokens.Target + GameTokens.Inset))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Text(when { row.id == "height_10" -> "İlk bina"; row.category == "height" -> "Usta inşaatçı";
                    row.category == "precise" -> "Dəqiq yerləşdirmə"; row.category in setOf("rescue", "rescues") -> "Yıxılmayan şəhər";
                    else -> achievementTitle(row.id) }, color = GameTokens.Text, fontFamily = GameTypeface,
                    fontSize = GameTokens.Body, fontWeight = FontWeight.ExtraBold)
                if(row.completed) Text("✓ Tamamlandı", color = GameTokens.Success, fontSize = GameTokens.Label, fontWeight = FontWeight.Bold)
                else if(row.target != null && row.current != null) Text("${row.current} / ${row.target}" + when(row.category) {
                    "height" -> " mərtəbə"; "rescue", "rescues" -> " xilasetmə"; else -> ""
                }, color = GameTokens.InventoryCollection.Metadata, fontSize = GameTokens.Label)
                if(!row.completed && row.target != null && row.current != null) {
                    val fraction = (row.current.toDouble() / row.target).toFloat().coerceIn(0f,1f)
                    val shape = RoundedCornerShape(GameTokens.Small)
                    Canvas(Modifier.fillMaxWidth().height(GameTokens.Achievements.ProgressHeight).clip(shape)
                        .background(GameTokens.InventoryCollection.Background)
                        .border(GameTokens.InventoryCollection.OutlineWidth, GameTokens.InventoryCollection.Metadata, shape)
                        .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) }) {
                        drawRoundRect(Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent,
                            GameTokens.MobileHome.PlayBottom)), size = Size(size.width * fraction, size.height), cornerRadius = CornerRadius(size.height))
                    }
                }
            }
        }
    }
}

// Unknown additive server awards keep a readable fallback title.
private fun achievementTitle(id: String): String {
    if (id == "height_10") return "İlk bina"
    val target = id.substringAfterLast('_').toLongOrNull() ?: return "Yeni nailiyyət"
    return when (id.substringBeforeLast('_')) {
        "height" -> "Usta inşaatçı · $target mərtəbə"
        "precise" -> "$target dəqiq yerləşdirmə"
        "rescue", "rescues" -> "Yıxılmayan şəhər · $target xilasetmə"
        else -> "Yeni nailiyyət"
    }
}

/** Native shield and live level label; the number is never baked into artwork. */
@Composable
internal fun AchievementLevelBadge(level: Long?) {
    Box(Modifier.width(GameTokens.Target).heightIn(min = GameTokens.Target + GameTokens.Small),
        contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val shield = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * .5f, size.height * .04f)
                lineTo(size.width * .94f, size.height * .18f)
                quadraticBezierTo(size.width * .94f, size.height * .72f, size.width * .5f, size.height * .96f)
                quadraticBezierTo(size.width * .06f, size.height * .72f, size.width * .06f, size.height * .18f)
                close()
            }
            drawPath(shield, Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent,GameTokens.MobileHome.PlayBottom)))
            drawPath(shield, GameTokens.Text, style = androidx.compose.ui.graphics.drawscope.Stroke(GameTokens.Hairline.toPx()))
        }
        Text(level?.toString() ?: "—", Modifier.padding(GameTokens.Small), color = GameTokens.Background,
            fontFamily = GameTypeface, fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold)
    }
}
