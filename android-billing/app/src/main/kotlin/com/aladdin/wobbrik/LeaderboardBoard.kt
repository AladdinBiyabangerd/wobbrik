package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import appbackend.wobbrik.game.*

@Composable
internal fun LeaderboardBoard(page: LeaderboardPage?, modifier: Modifier = Modifier, onFit: (Int) -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    val repeatPodiumRows = LocalMobileMenuShell.current
    var shown by remember { mutableIntStateOf(0) }
    SubcomposeLayout(modifier.testTag("records-board").semantics { contentDescription = "Göstərilən oyunçular: $shown" }) { constraints ->
        val entries = page?.entries.orEmpty().take(LeaderboardWindowPolicy.MaxRows)
        val loose = constraints.copy(minWidth = constraints.maxWidth, minHeight = 0, maxHeight = Constraints.Infinity)
        val gap = GameTokens.Small.roundToPx()
        val dots = subcompose("dots") {
            Canvas(Modifier.fillMaxWidth().height(GameTokens.Inset).testTag("records-omitted")) {
                for (i in -1..1) drawCircle(GameTokens.Muted, GameTokens.Hairline.toPx() * 2,
                    Offset(size.width / 2 + i * GameTokens.Gap.toPx(), size.height / 2))
            }
        }.first().measure(loose)
        val sample = subcompose("size-probe") { Box { LeaderboardRowContent(4, 20, false, "Oyunçu", "builder") } }.first().measure(loose)
        val canShowPodium = !large && entries.take(3).map { it.rank } == listOf(1L, 2L, 3L) &&
            entries.take(3).all { it.floors.toString().length <= GameTokens.Leaderboard.MaxPodiumDigits }
        val candidate = when {
            canShowPodium -> subcompose("podium") { LeaderboardPodiumWithNames(entries.take(3)) }.first().measure(loose)
            page == null && !large -> subcompose("podium-size-probe") {
                Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    Spacer(Modifier.fillMaxWidth().aspectRatio(GameTokens.Leaderboard.ArtWidth.toFloat() / GameTokens.Leaderboard.ArtHeight))
                    Text("M", fontSize = GameTokens.Caption, fontWeight = FontWeight.Bold)
                }
            }.first().measure(loose)
            else -> null
        }
        val head = candidate?.takeIf { it.height + dots.height + gap <= constraints.maxHeight }
        val base = if (head != null && !repeatPodiumRows) 3 else 0
        val limit = constraints.maxHeight - dots.height - gap
        var y = head?.let { it.height + gap } ?: 0
        val placed = ArrayList<Pair<LeaderboardEntry, Placeable>>()
        var blocked = false
        for (entry in entries.drop(if (canShowPodium && head != null && !repeatPodiumRows) 3 else 0)) {
            if (base + placed.size >= LeaderboardWindowPolicy.MaxRows) break
            var row = subcompose("player-${entry.rank}") { LeaderboardRow(entry, modifier = Modifier.testTag("records-player-${entry.rank}")) }.first().measure(loose)
            // Only the first oversized row may use the existing compact token step.
            if (placed.isEmpty() && row.height > limit - y) {
                row = subcompose("compact-player-${entry.rank}") { LeaderboardRow(entry, compact = true, modifier = Modifier.testTag("records-compact-player-${entry.rank}")) }.first().measure(loose)
            }
            if (y + row.height > limit) { blocked = true; break }
            placed += entry to row
            y += row.height + gap
        }
        // The mobile reference repeats podium players in the list; count unique ranks for reads.
        val visible = if (canShowPodium && head != null && repeatPodiumRows) maxOf(3, placed.size)
            else (if (canShowPodium && head != null) 3 else 0) + placed.size
        if (shown != visible) shown = visible
        val mayNeedRows = page == null || (page.next != null && !blocked)
        val extra = if (mayNeedRows && sample.height > 0) ((limit - y + gap).coerceAtLeast(0) / (sample.height + gap)) else 0
        val capacity = if (page == null) base + extra else visible + extra
        onFit(LeaderboardWindowPolicy.requestSize(capacity))
        layout(constraints.maxWidth, constraints.maxHeight) {
            var top = 0
            if (canShowPodium && head != null) { head.place(0, top); top += head.height + gap }
            placed.forEach { (_, row) -> row.place(0, top); top += row.height + gap }
            if (entries.size > visible || page?.next != null) {
                if (top + dots.height <= constraints.maxHeight) dots.place(0, top)
            }
        }
    }
}

@Composable
private fun LeaderboardPodiumWithNames(entries: List<LeaderboardEntry>) {
    if (LocalMobileMenuShell.current) {
        MobileRecordsPodium(entries)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        LeaderboardPodium(entries)
        Row(Modifier.fillMaxWidth()) {
            listOf(entries[1], entries[0], entries[2]).forEach { entry ->
                Text(entry.displayName + if (entry.isSelf) " · Sən" else "", Modifier.weight(1f), color = GameTokens.Text,
                    fontSize = GameTokens.Caption, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun LeaderboardRow(entry: LeaderboardEntry, compact: Boolean = false, modifier: Modifier = Modifier) {
    LeaderboardRowContent(entry.rank, entry.floors, entry.isSelf, entry.displayName, entry.avatarId, compact, modifier)
}

/** Shared visual measurement uses primitives; no fabricated domain record or API26 date. */
@Composable
private fun LeaderboardRowContent(rank: Long, floors: Long, isSelf: Boolean, displayName: String,
    avatarId: String, compact: Boolean = false, modifier: Modifier = Modifier) {
    if (LocalMobileMenuShell.current) {
        MobileRecordsRow(rank, floors, isSelf, displayName, avatarId, compact, modifier)
        return
    }
    Surface(modifier = modifier, color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.ControlRadius),
        border = BorderStroke(GameTokens.Hairline, if (isSelf) GameTokens.Accent else GameTokens.Raised)) {
        if (compact) {
            Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface)))
                .padding(horizontal = GameTokens.Quarter, vertical = GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Text((if (isSelf) "Sən · " else "") + "Yer ${leaderboardRankLabel(rank)}", color = GameTokens.Muted,
                    fontSize = GameTokens.Caption, lineHeight = GameTokens.Label)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    PublicAvatar(avatarId, Modifier.size(GameTokens.Inset))
                    Text(displayName, Modifier.weight(1f), color = GameTokens.Text, fontWeight = FontWeight.Bold,
                        fontSize = GameTokens.Body, lineHeight = GameTokens.Hint)
                }
                Text("${floors} mərtəbə", color = GameTokens.Muted, fontSize = GameTokens.Caption, lineHeight = GameTokens.Label)
            }
            return@Surface
        }
        val longRank = rank.toString().length > GameTokens.Leaderboard.MaxPodiumDigits
        val stackedScore = LocalDensity.current.fontScale > 1f ||
            floors.toString().length > GameTokens.Leaderboard.MaxPodiumDigits
        Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface)))
            .padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            if (longRank) Text("Yer ${leaderboardRankLabel(rank)}", color = GameTokens.Text, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if (!longRank) LeaderboardRank(rank)
                PublicAvatar(avatarId, Modifier.size(GameTokens.Target))
                Column(Modifier.weight(1f)) {
                    Text(displayName, color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    if (isSelf) Text("Sən", color = GameTokens.Accent, fontSize = GameTokens.Caption)
                    if (stackedScore) Text("${floors} mərtəbə", color = GameTokens.Muted)
                }
                if (!stackedScore) {
                    CommunityFloorsArt(Modifier.size(GameTokens.Inset))
                    Text(floors.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { contentDescription = "${floors} mərtəbə" })
                }
            }
        }
    }
}
