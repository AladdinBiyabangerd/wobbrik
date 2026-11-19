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
        loadDecorationArt(context)
    }
}

/** Inventory decodes off the UI dispatcher, never per card. */
@Composable
internal fun rememberOwnedDecorationArt(): Map<String, CosmeticSprite> {
    val context = LocalContext.current
    val result = androidx.compose.runtime.produceState(emptyMap<String, CosmeticSprite>(), context) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            loadDecorationArt(context) + loadDecorationArt(context, "owned-decor-art-v1.json")
        }
    }
    return result.value
}

internal fun loadDecorationArt(context: android.content.Context, metadataFile: String = "purchase-art.json"): Map<String, CosmeticSprite> {
        val metadata = JSONObject(context.assets.open("city/$metadataFile").bufferedReader().use { it.readText() })
        val image = context.assets.open("city/${metadata.getString("source")}").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        val sprites = metadata.getJSONArray("sprites")
        return (0 until sprites.length()).associate { i ->
            val entry = sprites.getJSONObject(i)
            entry.getString("id") to CosmeticSprite(image, IntOffset(entry.getInt("x"), entry.getInt("y")),
                IntSize(entry.getInt("width"), entry.getInt("height")))
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

/** Legacy thumbnails retain their pixel cap; dedicated inventory art fits the native card. */
@Composable
internal fun PurchaseArt(art: CosmeticSprite?, modifier: Modifier, allowUpscale: Boolean = false) {
    Canvas(modifier) {
        if (art != null) {
            val scale = minOf(if (allowUpscale) Float.MAX_VALUE else 1f, size.width / art.size.width, size.height / art.size.height)
            val width = (art.size.width * scale).roundToInt().coerceAtLeast(1)
            val height = (art.size.height * scale).roundToInt().coerceAtLeast(1)
            drawImage(art.image, srcOffset = art.origin, srcSize = art.size,
                dstOffset = IntOffset(((size.width - width) / 2).roundToInt(), ((size.height - height) / 2).roundToInt()),
                dstSize = IntSize(width, height))
        }
    }
}
