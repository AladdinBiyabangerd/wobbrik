package com.aladdin.wobbrik

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/** Two-storey material swatch from the shipped modular layers, not a synthetic owned building. */
internal fun DrawScope.appearancePartPreview(purple: Map<String, CosmeticSprite>, art: Map<String, CosmeticSprite>,
    parts: Map<String, String>, category: String) {
    if (category != "wall") {
        // Show the selected category itself, without unrelated building layers obscuring it.
        val id = parts[category]
        val cell = when (category) { "window" -> "arch"; "entrance" -> "entrance"; "roof" -> "roof"; else -> "lamp" }
        val item = if (id == "base_1_$category") purple[cell] else art[id]
        sprite(item, size.width * .12f, size.height * .08f, size.width * .76f, size.height * .84f)
        return
    }
    val shell = purple.getValue("floor")
    val wallId = parts["wall"]
    val wall = art[wallId] ?: return
    val width = GameTokens.AppearanceCollection.SampleWidth
    val height = GameTokens.AppearanceCollection.SampleHeight
    val scale = minOf(size.width / width, size.height / height)
    withTransform({ translate((size.width-width*scale)/2, (size.height-height*scale)/2); scale(scale, scale, Offset.Zero) }) {
        sprite(purple["garden"], 0f, 263f, width, 77f)
        val sample = wall.copy(origin=IntOffset(wall.origin.x+(wall.size.width*.14f).toInt(),
            wall.origin.y+(wall.size.height*.29f).toInt()),
            size=IntSize((wall.size.width*.64f).toInt(),(wall.size.height*.48f).toInt()))
        repeat(2) { floor ->
            val y = 20f+floor*139f
            purplePlane(sample, listOf(Offset(45f,y),Offset(229f,y-11f),Offset(229f,y+128f),Offset(45f,y+139f)))
            // Only the stone rim is sampled; the selected material owns the entire face.
            for (x in listOf(45f,217f)) purpleSurface(shell,
                listOf(80f,72f,90f,74f,90f,80f,80f,78f),
                listOf(Offset(x,y),Offset(x+12f,y-1f),Offset(x+12f,y+138f),Offset(x,y+139f)))
            // Trim is sampled from the shell's existing stone cornice, rather than a new skin.
            purpleSurface(shell, listOf(16f,63f,241f,90f,241f,100f,16f,73f),
                listOf(Offset(38f,y-9f),Offset(236f,y-21f),Offset(236f,y-7f),Offset(38f,y+5f)))
            val window = if (parts["window"] == "base_1_window") purple["arch"] else art[parts["window"]]
            sprite(window, 99f, y+22f, 77f, 94f)
        }
        purpleSurface(shell, listOf(16f,63f,241f,90f,241f,100f,16f,73f),
            listOf(Offset(38f,291f),Offset(236f,279f),Offset(236f,293f),Offset(38f,305f)))
        sprite(purple["balcony"], 73f, 108f, 104f, 79f)
    }
}
