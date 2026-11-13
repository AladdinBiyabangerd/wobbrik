package com.aladdin.wobbrik

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.json.JSONObject

/** Complete approved game modules. These do not create catalog IDs or ownership. */
internal data class GameAtlas(val cells: Map<String, CosmeticSprite>, val pitch: Float, val depth: Float,
    val topAnchors: List<Offset>) {
    val floor get() = cells.getValue("floor")
    val ground get() = cells.getValue("ground")
    val roof get() = cells.getValue("roof")
}

@Composable
internal fun rememberGameAtlases(): Map<String, GameAtlas> {
    val context = LocalContext.current
    return remember(context) {
        val metadata = context.assets.open("game-v3/atlas.json").bufferedReader().use { JSONObject(it.readText()) }
        val cells = metadata.getJSONObject("cells")
        listOf("purple", "teal").associateWith { style ->
            val image = context.assets.open("game-v3/$style.png").use {
                requireNotNull(BitmapFactory.decodeStream(it)).asImageBitmap()
            }
            val sprites = cells.keys().asSequence().associateWith { key ->
                val crop = cells.getJSONArray(key)
                val x = crop.getInt(0); val y = crop.getInt(1)
                val w = crop.getInt(2); val h = crop.getInt(3)
                require(x >= 0 && y >= 0 && w > 0 && h > 0 && x + w <= image.width && y + h <= image.height)
                CosmeticSprite(image, IntOffset(x, y), IntSize(w, h))
            }
            val anchors = metadata.getJSONArray("top_anchors_px")
            GameAtlas(sprites, metadata.getDouble("floor_pitch_px").toFloat(), metadata.getDouble("floor_depth_px").toFloat(),
                List(anchors.length()) { index ->
                    val point = anchors.getJSONArray(index)
                    Offset(point.getDouble(0).toFloat(), point.getDouble(1).toFloat())
                })
        }
    }
}

/** Only complete first/second base styles may use baked-in windows/doors/lamps.
 * Mixed and earned/purchased selections keep independent catalog art and IDs.
 */
internal fun gameAtlasStyle(parts: Map<String, String>): String? {
    if (parts.isEmpty()) return "purple"
    for ((index, style) in listOf("purple", "teal").withIndex()) {
        if (listOf("wall", "window", "entrance", "lighting", "roof").all {
                parts[it] == "base_${index + 1}_$it"
            }) return style
    }
    return null
}
