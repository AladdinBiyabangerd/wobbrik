package com.aladdin.wobbrik

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable
internal fun rememberDecorationArt(): Map<String, CosmeticSprite> {
    val context = LocalContext.current
    return remember(context) {
        val metadata = JSONObject(context.assets.open("city/purchase-art.json").bufferedReader().use { it.readText() })
        val image = context.assets.open("city/${metadata.getString("source")}").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        val sprites = metadata.getJSONArray("sprites")
        (0 until sprites.length()).associate { i ->
            val entry = sprites.getJSONObject(i)
            entry.getString("id") to CosmeticSprite(image, IntOffset(entry.getInt("x"), entry.getInt("y")),
                IntSize(entry.getInt("width"), entry.getInt("height")))
        }
    }
}

@Composable
internal fun rememberLandArt(): CosmeticSprite {
    val context = LocalContext.current
    return remember(context) {
        val entry = JSONObject(context.assets.open("city/purchase-art.json").bufferedReader().use { it.readText() }).getJSONObject("land")
        val image = context.assets.open("city/${entry.getString("source")}").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        // User-approved transparent art only; never the parcel's collision contour.
        CosmeticSprite(image, IntOffset(entry.getInt("x"), entry.getInt("y")),
            IntSize(entry.getInt("width"), entry.getInt("height")))
    }
}

/** No bitmap upsampling: dp × density may never exceed the source crop's pixels. */
@Composable
internal fun PurchaseArt(art: CosmeticSprite?, modifier: Modifier) {
    Canvas(modifier) {
        if (art != null) {
            val scale = minOf(1f, size.width / art.size.width, size.height / art.size.height)
            val width = (art.size.width * scale).roundToInt().coerceAtLeast(1)
            val height = (art.size.height * scale).roundToInt().coerceAtLeast(1)
            drawImage(art.image, srcOffset = art.origin, srcSize = art.size,
                dstOffset = IntOffset(((size.width - width) / 2).roundToInt(), ((size.height - height) / 2).roundToInt()),
                dstSize = IntSize(width, height))
        }
    }
}
