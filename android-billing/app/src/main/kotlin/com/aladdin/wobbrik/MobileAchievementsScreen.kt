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

/** Read-only accepted progress. Completion is an explicit server award, never inferred. */
@Composable
internal fun MobileAchievementsScreen(snapshot: GameSnapshot, onHome: () -> Unit) {
    BackHandler(onBack = onHome)
    val largeFont = LocalDensity.current.fontScale > 1f
    val awards = snapshot.account?.achievements.orEmpty()
    val progress = snapshot.account?.achievementProgress
    val groups = progress.orEmpty().groupBy { it.category }.values.toList()
    // Keep categories visible together, then subsequent server-defined tiers below.
    val ordered = (0 until (groups.maxOfOrNull { it.size } ?: 0)).flatMap { tier -> groups.mapNotNull { it.getOrNull(tier) } }
    val rows = ordered.map { AchievementRow(it.id, it.category, it.threshold, it.current, it.id in awards) } +
        awards.filter { id -> progress.orEmpty().none { it.id == id } }.map { AchievementRow(it, it.substringBeforeLast('_'), null, null, true) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = GameTokens.Gap),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Small),
        contentPadding = PaddingValues(bottom = GameTokens.Gap)) {
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AchievementArt(GameTokens.Achievements.Trophy, Modifier.height(GameTokens.Achievements.HeroHeight)
                    .width(GameTokens.Achievements.HeroHeight * 2))
            }
        }
        item { MobileHomeStats(snapshot, largeFont, achievementBadge = true) }
        item {
            Text("${awards.size} mükafat açılıb", color = GameTokens.Text,
                fontFamily = GameTypeface, fontSize = GameTokens.Hint, fontWeight = FontWeight.Bold)
        }
        if (progress == null) item {
            Text("Pillələrin irəliləyişi bu hesab məlumatında yoxdur. Yenilənmiş server məlumatı tələb olunur.",
                color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Label)
        }
        if (rows.isEmpty()) item {
            MenuRelief(Modifier.fillMaxWidth()) {
                Text("Hələ açılmış mükafat yoxdur. Oyunu tamamlayaraq nailiyyətlər qazan.",
                    Modifier.padding(GameTokens.Gap), color = GameTokens.Muted, fontSize = GameTokens.Body)
            }
        }
        items(rows, key = { it.id }) { row -> AchievementCard(row, largeFont) }
    }
}

private data class AchievementRow(val id: String, val category: String, val target: Long?, val current: Long?, val completed: Boolean)

@Composable
private fun AchievementCard(row: AchievementRow, largeFont: Boolean) {
    MenuRelief(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            AchievementArt(when(row.category) {
                "height" -> GameTokens.Achievements.GoldMedal
                "precise" -> GameTokens.Achievements.BlueMedal
                else -> GameTokens.Achievements.PurpleMedal
            }, Modifier.size(GameTokens.Target))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Text(achievementTitle(row.id), color = GameTokens.Text, fontFamily = GameTypeface,
                    fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                if (row.completed) Text("✓ Tamamlandı", color = GameTokens.Success,
                    fontFamily = GameTypeface, fontSize = GameTokens.Label)
                val fraction = if (row.completed) 1f else if (row.target != null && row.current != null)
                    (row.current.toDouble() / row.target).toFloat().coerceIn(0f, 1f) else 0f
                val label = if (!row.completed && row.target != null) "${row.current} / ${row.target}" else null
                if (largeFont && label != null) Text(label, color = GameTokens.Text, fontSize = GameTokens.Label)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    MenuRelief(Modifier.weight(1f).height(GameTokens.Achievements.ProgressHeight)
                        .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) },
                        shape = RoundedCornerShape(GameTokens.CardRadius), kind = MenuSkin.Track) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawRoundRect(Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop,
                                if(row.completed) GameTokens.Success else GameTokens.Accent, GameTokens.MobileHome.PlayBottom)),
                                size = Size(size.width * fraction, size.height), cornerRadius = CornerRadius(size.height))
                        }
                    }
                    if (!largeFont && label != null) Text(label, color = GameTokens.Text,
                        fontFamily = GameTypeface, fontWeight = FontWeight.Bold, fontSize = GameTokens.Label)
                }
            }
        }
    }
}

@Composable
private fun AchievementArt(bounds: IntArray, modifier: Modifier) {
    val bitmap = ImageBitmap.imageResource(R.drawable.achievement_art_v3)
    val painter = remember(bitmap, bounds) { BitmapPainter(bitmap, IntOffset(bounds[0], bounds[1]), IntSize(bounds[2], bounds[3])) }
    Image(painter, null, modifier)
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
