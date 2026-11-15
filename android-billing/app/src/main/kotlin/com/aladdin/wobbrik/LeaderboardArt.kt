package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import appbackend.wobbrik.game.LeaderboardEntry

private fun portraitOrigin(id: String): IntOffset = with(GameTokens.Leaderboard) {
    when (id) {
        "builder" -> IntOffset(BuilderPortraitX, BuilderPortraitY)
        "pilot" -> IntOffset(PilotPortraitX, PilotPortraitY)
        else -> IntOffset(ArchitectPortraitX, ArchitectPortraitY)
    }
}

@Composable
internal fun PublicAvatar(id: String, modifier: Modifier = Modifier) {
    val board = LocalCommunityBoard.current
    Canvas(modifier.clipToBounds().semantics { contentDescription = avatarLabel(id) + " avatarı" }) {
        clipPath(Path().apply { addOval(Rect(0f, 0f, size.width, size.height)) }) {
            val art = GameTokens.Leaderboard
            val rowY = when(id) { "builder" -> art.BuilderRowY; "pilot" -> art.PilotRowY; else -> art.ArchitectRowY }
            drawImage(board, srcOffset = IntOffset(art.RowAvatarX, rowY),
                srcSize = IntSize(art.RowAvatarSize, art.RowAvatarSize),
                dstSize = IntSize(size.width.toInt(), size.height.toInt()))
        }
    }
}

internal fun leaderboardRankLabel(rank: Long): String = if (rank > 9999L) "99999+" else rank.toString()

@Composable
internal fun LeaderboardRank(rank: Long) {
    if (rank !in 1L..3L) {
        Text(leaderboardRankLabel(rank), color = GameTokens.Text, fontWeight = FontWeight.Bold)
        return
    }
    val board = LocalCommunityBoard.current
    val foreground = remember(board, rank) { with(GameTokens.Leaderboard) {
        sourceForeground(board, RankX, RankY + (rank.toInt() - 1) * RankStep, RankWidth, RankHeight)
    } }
    Canvas(Modifier.size(GameTokens.Target).semantics { contentDescription = "$rank-ci yer" }) {
        val art = GameTokens.Leaderboard
        val scale = size.width / art.RankWidth
        withTransform({
            translate(0f, (size.height - art.RankHeight * scale) / 2)
            scale(scale, scale, pivot = Offset.Zero)
            translate(-art.RankX.toFloat(), -(art.RankY + (rank.toInt() - 1) * art.RankStep).toFloat())
        }) { clipPath(foreground) { drawImage(board) } }
    }
}

@Composable
internal fun LeaderboardPodium(entries: List<LeaderboardEntry>) {
    val board = LocalCommunityBoard.current
    val foreground = remember(board) { with(GameTokens.Leaderboard) {
        sourceForeground(board, ArtX, ArtY, ArtWidth, ArtHeight)
    } }
    Canvas(Modifier.fillMaxWidth().aspectRatio(GameTokens.Leaderboard.ArtWidth.toFloat() / GameTokens.Leaderboard.ArtHeight)
        .semantics { contentDescription = entries.joinToString { "${it.rank}. ${it.displayName}: ${it.floors} mərtəbə" } }) {
        val art = GameTokens.Leaderboard
        val scale = size.width / art.ArtWidth
        withTransform({ scale(scale, scale, pivot = Offset.Zero); translate(-art.ArtX.toFloat(), -art.ArtY.toFloat()) }) {
            // Clip only the empty outer margin of the approved source. No regenerated art,
            // stretched podium or rectangular backdrop; the source pixels stay at one scale.
            clipPath(foreground) { drawImage(board) }
            fun portrait(entry: LeaderboardEntry, original: String, x: Int, y: Int, width: Int, height: Int, bottom: Int) {
                if (entry.avatarId == original) return
                val side = if (entry.avatarId == "builder") art.PortraitSize else art.SidePortraitSize
                clipPath(Path().apply { addRect(Rect(x.toFloat(), y.toFloat(), (x + width).toFloat(), bottom.toFloat())) }) {
                    clipPath(Path().apply { addOval(Rect(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())) }) {
                        drawImage(board, srcOffset = portraitOrigin(entry.avatarId), srcSize = IntSize(side, side),
                            dstOffset = IntOffset(x, y), dstSize = IntSize(width, height))
                    }
                }
            }
            fun score(entry: LeaderboardEntry, original: Long, x: Int, y: Int, width: Int, height: Int) {
                // Preserve the exact approved pixels when the accepted score matches them.
                if (entry.floors == original) return
                drawRoundRect(GameTokens.Background, Offset(x.toFloat(), y.toFloat()),
                    Size(width.toFloat(), height.toFloat()), CornerRadius(height / 2f))
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = GameTokens.Text.toArgb(); textSize = art.ScoreTextSize.toFloat()
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val label = entry.floors.toString()
                paint.textSize *= minOf(1f, width / paint.measureText(label))
                drawContext.canvas.nativeCanvas.drawText(label, x + width / 2f,
                    y + height / 2f - (paint.ascent() + paint.descent()) / 2, paint)
            }
            portrait(entries[0], "builder", art.FirstAvatarX, art.FirstAvatarY, art.FirstAvatarWidth, art.FirstAvatarHeight, 325)
            portrait(entries[1], "pilot", art.SecondAvatarX, art.SecondAvatarY, art.SecondAvatarWidth, art.SecondAvatarHeight, 344)
            portrait(entries[2], "architect", art.ThirdAvatarX, art.ThirdAvatarY, art.ThirdAvatarWidth, art.ThirdAvatarHeight, 352)
            score(entries[0], 42, art.FirstScoreX, art.FirstScoreY, art.FirstScoreWidth, art.FirstScoreHeight)
            score(entries[1], 38, art.SecondScoreX, art.SecondScoreY, art.SecondScoreWidth, art.SecondScoreHeight)
            score(entries[2], 35, art.ThirdScoreX, art.ThirdScoreY, art.ThirdScoreWidth, art.ThirdScoreHeight)
        }
    }
}
