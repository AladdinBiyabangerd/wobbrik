package com.aladdin.wobbrik

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.FinishCosmetics
import com.aladdin.wobbrik.engine.AcceptedEngineHost
import org.json.JSONObject
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal data class CosmeticSprite(val image: ImageBitmap, val origin: IntOffset, val size: IntSize)

/** Atlas metadata locates art only; it never supplies catalog entries or ownership. */
@Composable
internal fun rememberCosmeticArt(): Map<String, CosmeticSprite> {
    val context = LocalContext.current
    return remember(context) {
        val metadata = context.assets.open("finish/catalog-map.json").bufferedReader().use { JSONObject(it.readText()) }
        val items = metadata.getJSONArray("items")
        val atlases = mutableMapOf<String, android.graphics.Bitmap>()
        buildMap {
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val bitmap = atlases.getOrPut(item.getString("atlas")) {
                    context.assets.open("finish/${item.getString("atlas")}").use { BitmapFactory.decodeStream(it) }
                }
                val crop = item.getJSONArray("crop")
                val x0 = crop.getInt(0); val y0 = crop.getInt(1)
                val width = crop.getInt(2); val height = crop.getInt(3)
                require(x0 >= 0 && y0 >= 0 && width > 0 && height > 0 &&
                    x0 + width <= bitmap.width && y0 + height <= bitmap.height)
                put(item.getString("id"), CosmeticSprite(bitmap.asImageBitmap(), IntOffset(x0, y0), IntSize(width, height)))
            }
        }
    }
}

internal fun DrawScope.sprite(sprite: CosmeticSprite?, left: Float, top: Float, width: Float, height: Float, alpha: Float = 1f) {
    if (sprite == null) return
    val scale = minOf(width / sprite.size.width, height / sprite.size.height)
    val w = (sprite.size.width * scale).roundToInt().coerceAtLeast(1)
    val h = (sprite.size.height * scale).roundToInt().coerceAtLeast(1)
    drawImage(sprite.image, srcOffset = sprite.origin, srcSize = sprite.size,
        dstOffset = IntOffset((left + (width - w) / 2).roundToInt(), (top + (height - h) / 2).roundToInt()),
        dstSize = IntSize(w, h), alpha = alpha)
}

/** Common facade eave anchor: changing roof silhouettes must not move the join. */
internal fun DrawScope.roof(sprite: CosmeticSprite?, left: Float, baseline: Float, width: Float) {
    if (sprite == null) return
    val scale = minOf(width / sprite.size.width, width * .85f / sprite.size.height)
    val w = (sprite.size.width * scale).roundToInt().coerceAtLeast(1)
    val h = (sprite.size.height * scale).roundToInt().coerceAtLeast(1)
    drawImage(sprite.image, srcOffset = sprite.origin, srcSize = sprite.size,
        dstOffset = IntOffset((left + (width - w) / 2).roundToInt(), (baseline - h).roundToInt()), dstSize = IntSize(w, h))
}

/** Thin category outlines from the approved picker, in token colours. */
@Composable
private fun CosmeticCategoryIcon(category: String, color: Color) {
    Canvas(Modifier.size(GameTokens.Inset)) {
        val w = size.width; val h = size.height
        val stroke = Stroke(GameTokens.Hairline.toPx() * 2)
        fun line(x0: Float, y0: Float, x1: Float, y1: Float) =
            drawLine(color, Offset(w * x0, h * y0), Offset(w * x1, h * y1), stroke.width)
        when (category) {
            "wall" -> {
                drawRect(color, Offset(w * .1f, h * .15f), Size(w * .8f, h * .7f), style = stroke)
                line(.1f, .5f, .9f, .5f); line(.5f, .15f, .5f, .5f)
                line(.3f, .5f, .3f, .85f); line(.7f, .5f, .7f, .85f)
            }
            "window", "entrance" -> {
                val path = Path().apply {
                    moveTo(w * .2f, h * .9f); lineTo(w * .2f, h * .4f)
                    cubicTo(w * .2f, h * .02f, w * .8f, h * .02f, w * .8f, h * .4f)
                    lineTo(w * .8f, h * .9f); close()
                }
                drawPath(path, color, style = stroke)
                if (category == "window") { line(.5f, .1f, .5f, .9f); line(.2f, .5f, .8f, .5f) }
                else line(.32f, .9f, .32f, .42f)
            }
            "roof" -> { line(.08f, .8f, .5f, .2f); line(.5f, .2f, .92f, .8f); line(.55f, .2f, .72f, .2f); line(.72f, .2f, .98f, .58f) }
            "lighting" -> {
                drawCircle(color, w * .25f, Offset(w * .5f, h * .35f), style = stroke)
                line(.35f, .58f, .4f, .8f); line(.65f, .58f, .6f, .8f)
                line(.4f, .8f, .6f, .8f); line(.42f, .92f, .58f, .92f)
            }
        }
    }
}

@Composable
internal fun FinishCosmeticScreen(view: AcceptedEngineHost.Snapshot, ready: AuthenticatedRecoveryOwner.State.Ready,
    onSelect: (String, String) -> Unit, onConfirm: () -> Unit, onBack: () -> Unit) {
    val art = rememberCosmeticArt() + rememberGameWindowArt()
    val atlases = rememberGameAtlases()
    var category by remember { mutableStateOf("wall") }
    val sizing = mobileUiSizing()
    val labels = listOf("Divar", "Pəncərə", "Giriş", "Dam", "İşıq")
    val catalog = ready.catalog
    val missingArt = catalog?.items?.any { it.category in FinishCosmetics.categories && it.id !in art } == true
    val valid = catalog != null && view.finishParts.values.all { it in art } &&
        FinishCosmetics.valid(catalog, ready.snapshot, view.finishParts)
    BoxWithConstraints(Modifier.fillMaxSize().background(GameTokens.Background).safeDrawingPadding().padding(GameTokens.Gap)) {
        val stackedActions = maxWidth < GameTokens.MediumWidth && androidx.compose.ui.platform.LocalDensity.current.fontScale > 1f
        val wide = maxWidth >= GameTokens.MediumWidth && maxWidth > maxHeight
        val gridColumns = if (maxWidth >= GameTokens.ExpandedWidth && wide) 3 else 2
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.width(GameTokens.Target * 2).height(GameTokens.Target), contentScale = ContentScale.Fit)
                Text("Bina görünüşü", color = GameTokens.Text, fontSize = sizing.heading, fontWeight = FontWeight.Bold)
            }
            val preview: @Composable (Modifier) -> Unit = { modifier ->
                Surface(modifier, color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(Modifier.fillMaxWidth().weight(1f).semantics { contentDescription = "Seçilmiş bina görünüşü" }) {
                            val centers = view.scene?.floors?.map { (it.centerX * 1000).roundToLong() } ?: listOf(0L)
                            buildingPreview(atlases, art, view.finishParts, centers)
                        }
                        Text(view.scene?.floors?.size?.let { "$it mərtəbə" } ?: "Standart görünüş", Modifier.padding(GameTokens.Small),
                            color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    }
                }
            }
            val picker: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        FinishCosmetics.categories.forEachIndexed { index, key ->
                            val active = category == key
                            Button(onClick = { category = key }, modifier = Modifier.heightIn(min = GameTokens.Target),
                                shape = RoundedCornerShape(GameTokens.ControlRadius),
                                contentPadding = PaddingValues(GameTokens.Small),
                                colors = ButtonDefaults.buttonColors(containerColor = if (active) GameTokens.Accent else GameTokens.Raised,
                                    contentColor = if (active) GameTokens.Background else GameTokens.Text)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CosmeticCategoryIcon(key, if (active) GameTokens.Background else GameTokens.Text)
                                    Text(labels[index])
                                }
                            }
                        }
                    }
                    LazyVerticalGrid(columns = GridCells.Fixed(gridColumns), modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        items(catalog?.items.orEmpty().filter { it.category == category && it.id in art }, key = { it.id }) { item ->
                            val available = FinishCosmetics.available(item, ready.snapshot)
                            val selected = view.finishParts[category] == item.id
                            Surface(onClick = { onSelect(category, item.id) }, enabled = available && !view.waiting && item.id in art,
                                modifier = Modifier.fillMaxWidth().semantics { this.selected = selected; contentDescription = item.name },
                                color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.ControlRadius),
                                border = BorderStroke(GameTokens.Hairline, if (selected) GameTokens.Accent else GameTokens.Raised)) {
                                Column(Modifier.padding(GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                                    Box {
                                        Canvas(Modifier.fillMaxWidth().height(if (sizing.portrait && sizing.compact) GameTokens.Target else GameTokens.Target + GameTokens.Inset)) {
                                            if (!available) drawContext.canvas.saveLayer(
                                                androidx.compose.ui.geometry.Rect(Offset.Zero, size),
                                                androidx.compose.ui.graphics.Paint().apply { alpha = GameTokens.DimOpacity })
                                            partPreview(atlases, art, view.finishParts + (category to item.id), category)
                                            if (!available) drawContext.canvas.restore()
                                        }
                                        if (selected) Text("✓", color = GameTokens.Background, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.TopEnd).background(GameTokens.Accent, CircleShape).padding(GameTokens.Quarter))
                                    }
                                    Text(if (!available) "Bağlı" else if (item.method == "free") "Pulsuz" else "Açılıb",
                                        modifier = Modifier.fillMaxWidth().background(if (available) GameTokens.Success else GameTokens.Raised,
                                            RoundedCornerShape(GameTokens.ControlRadius)).padding(GameTokens.Quarter),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        color = if (available) GameTokens.Background else GameTokens.Muted, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Text(when { view.finishStorageError -> "Seçim saxlanmadı. Yenidən təsdiqlə."; catalog == null -> "Kataloq yüklənmədi"; missingArt -> "Bəzi hissələrin qrafikası bu versiyada yoxdur";
                        else -> "Yalnız açıq hissələr seçilə bilər" }, color = GameTokens.Muted, fontSize = GameTokens.Caption)
                }
            }
            if (wide) Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
                preview(Modifier.weight(1f).fillMaxHeight())
                picker(Modifier.weight(2f).fillMaxHeight())
            } else Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                preview(Modifier.fillMaxWidth().weight(1f))
                picker(Modifier.fillMaxWidth().weight(2f))
            }
            val backAction: @Composable (Modifier) -> Unit = { modifier ->
                Button(onClick = onBack, shape = RoundedCornerShape(GameTokens.ControlRadius), modifier = modifier.heightIn(min = GameTokens.Target),
                    colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Raised, contentColor = GameTokens.Text)) { Text("← Geri", softWrap = false) }
            }
            val confirmAction: @Composable (Modifier) -> Unit = { modifier ->
                Button(onClick = onConfirm, shape = RoundedCornerShape(GameTokens.ControlRadius), enabled = valid && !view.waiting,
                    modifier = modifier.heightIn(min = GameTokens.Target),
                    colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Accent, contentColor = GameTokens.Background)) {
                    Text(if (view.waiting) "Saxlanılır…" else "✓ Təsdiqlə", fontWeight = FontWeight.Bold, softWrap = false)
                }
            }
            if (stackedActions) {
                Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    backAction(Modifier.fillMaxWidth())
                    confirmAction(Modifier.fillMaxWidth())
                }
            } else Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                backAction(Modifier.weight(1f))
                confirmAction(Modifier.weight(2f))
            }
        }
    }
}
