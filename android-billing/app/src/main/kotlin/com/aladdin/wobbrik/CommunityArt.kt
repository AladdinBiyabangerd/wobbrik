package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/** One decoded approved board per frame, shared by all rows and avatar choices. */
internal val LocalCommunityBoard = staticCompositionLocalOf<ImageBitmap> { error("CommunityFrame is required") }

@Composable
internal fun CommunityBackdrop(modifier: Modifier = Modifier) {
    val board = LocalCommunityBoard.current
    val colors = remember(board) {
        val art = GameTokens.Leaderboard
        List(art.BackgroundSamples) { index -> Color(board.asAndroidBitmap().getPixel(art.BackgroundX, art.BackgroundY + index * art.BackgroundStep)) }
    }
    Canvas(modifier) {
        // Smooth samples from the empty source margin, without stretching compression noise.
        drawRect(Brush.verticalGradient(colors))
    }
}

@Composable
internal fun CommunityBackArt(modifier: Modifier = Modifier) {
    val board = LocalCommunityBoard.current
    Canvas(modifier) {
        val art = GameTokens.Leaderboard
        clipPath(Path().apply { addOval(Rect(0f, 0f, size.width, size.height)) }) {
            drawImage(board, srcOffset = IntOffset(art.BackX, art.BackY), srcSize = IntSize(art.BackSize, art.BackSize),
                dstSize = IntSize(size.width.toInt(), size.height.toInt()))
        }
    }
}

@Composable
internal fun CommunityFloorsArt(modifier: Modifier = Modifier) {
    val board = LocalCommunityBoard.current
    val foreground = remember(board) { with(GameTokens.Leaderboard) {
        sourceForeground(board, FloorsX, FloorsY, FloorsWidth, FloorsHeight)
    } }
    Canvas(modifier) {
        val art = GameTokens.Leaderboard
        val scale = minOf(size.width / art.FloorsWidth, size.height / art.FloorsHeight)
        withTransform({ scale(scale, scale, pivot = Offset.Zero); translate(-art.FloorsX.toFloat(), -art.FloorsY.toFloat()) }) {
            clipPath(foreground) { drawImage(board) }
        }
    }
}

/**
 * A drawing clip, computed once from the source border's navy backdrop. Flooding only
 * border-connected pixels keeps enclosed dark eyes/hair/details. Foreground pixels are
 * drawn directly from the unchanged approved bitmap, never regenerated or repainted.
 */
internal fun sourceForeground(board: ImageBitmap, x: Int, y: Int, width: Int, height: Int): Path {
    val pixels = IntArray(width * height)
    board.asAndroidBitmap().getPixels(pixels, 0, width, x, y, width, height)
    val removed = BooleanArray(pixels.size)
    val queue = IntArray(pixels.size)
    var head = 0
    var tail = 0
    fun channel(pixel: Int, shift: Int) = (pixel ushr shift) and 255
    fun offer(index: Int) {
        if (removed[index]) return
        val row = index / width
        val left = pixels[row * width]
        val right = pixels[row * width + width - 1]
        val pixel = pixels[index]
        // Tolerances describe source compression/gradient noise, not a new UI colour.
        val navy = channel(pixel, 16) <= maxOf(channel(left, 16), channel(right, 16)) + 8 &&
            channel(pixel, 8) <= maxOf(channel(left, 8), channel(right, 8)) + 8 &&
            channel(pixel, 0) <= maxOf(channel(left, 0), channel(right, 0)) + 12
        if (navy) { removed[index] = true; queue[tail++] = index }
    }
    for (column in 0 until width) { offer(column); offer((height - 1) * width + column) }
    for (row in 0 until height) { offer(row * width); offer(row * width + width - 1) }
    while (head < tail) {
        val index = queue[head++]
        val column = index % width
        if (column > 0) offer(index - 1)
        if (column + 1 < width) offer(index + 1)
        if (index >= width) offer(index - width)
        if (index + width < pixels.size) offer(index + width)
    }
    return Path().apply {
        for (row in 0 until height) {
            var column = 0
            while (column < width) {
                while (column < width && removed[row * width + column]) column++
                val start = column
                while (column < width && !removed[row * width + column]) column++
                if (start < column) addRect(Rect((x + start).toFloat(), (y + row).toFloat(),
                    (x + column).toFloat(), (y + row + 1).toFloat()))
            }
        }
    }
}

@Composable
internal fun ApprovedAccountHero(modifier: Modifier = Modifier) {
    val board = ImageBitmap.imageResource(R.drawable.account_approved_board)
    val foreground = remember(board) { with(GameTokens.AccountArt) {
        sourceForeground(board, HeroX, HeroY, HeroWidth, HeroHeight)
    } }
    Canvas(modifier) {
        val art = GameTokens.AccountArt
        val scale = minOf(size.width / art.HeroWidth, size.height / art.HeroHeight)
        withTransform({
            translate((size.width - art.HeroWidth * scale) / 2, (size.height - art.HeroHeight * scale) / 2)
            scale(scale, scale, pivot = Offset.Zero)
            translate(-art.HeroX.toFloat(), -art.HeroY.toFloat())
        }) { clipPath(foreground) { drawImage(board) } }
    }
}

@Composable
internal fun CommunityCalendarArt(selected: Boolean, modifier: Modifier = Modifier) {
    val board = LocalCommunityBoard.current
    val ink = remember(board) {
        val art = GameTokens.Leaderboard
        val pixels = IntArray(art.CalendarWidth * art.CalendarHeight)
        board.asAndroidBitmap().getPixels(pixels, 0, art.CalendarWidth, art.CalendarX, art.CalendarY, art.CalendarWidth, art.CalendarHeight)
        // The approved calendar is dark ink on a cyan selected tab. Keep its exact silhouette.
        Path().apply {
            for(y in 0 until art.CalendarHeight) for(x in 0 until art.CalendarWidth) {
                val pixel = pixels[y * art.CalendarWidth + x]
                if (((pixel ushr 16) and 255) < 64 && ((pixel ushr 8) and 255) < 96 && (pixel and 255) < 128)
                    addRect(Rect(x.toFloat(), y.toFloat(), x + 1f, y + 1f))
            }
        }
    }
    Canvas(modifier) {
        val art = GameTokens.Leaderboard
        val scale = minOf(size.width / art.CalendarWidth, size.height / art.CalendarHeight)
        withTransform({ scale(scale, scale, pivot = Offset.Zero) }) {
            drawPath(ink, if (selected) GameTokens.Background else GameTokens.Text)
        }
    }
}

@Composable
internal fun CommunityRefreshArt(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = GameTokens.Hairline.toPx() * 2
        drawArc(GameTokens.Text, 45f, 285f, false, topLeft = Offset(stroke, stroke),
            size = androidx.compose.ui.geometry.Size(size.width - stroke * 2, size.height - stroke * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        drawPath(Path().apply {
            moveTo(size.width - stroke, 0f); lineTo(size.width - stroke, size.height / 3)
            lineTo(size.width - size.width / 3, size.height / 3)
        }, GameTokens.Text, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    }
}
