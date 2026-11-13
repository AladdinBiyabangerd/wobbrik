package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException

/** Server-only read destination. Leaving the exact Ready epoch destroys its scene and selection. */
@Composable
internal fun CityScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onHome: () -> Unit, onInventory: () -> Unit) {
    var scene by remember { mutableStateOf<CityScene?>(null) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(retry) {
        scene = null; failed = false
        try {
            val session = requireNotNull(configured.sessions.current())
            check(session.uid == ready.accountKey && configured.recovery.state.value === ready)
            val loaded = configured.cityScene.load(session)
            check(configured.sessions.current() == session && configured.recovery.state.value === ready)
            scene = loaded
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
    }
    BackHandler(onBack = onHome)
    CityContent(scene, failed, { retry++ }, onHome, onInventory)
}

@Composable
internal fun CityContent(scene: CityScene?, failed: Boolean, onRetry: () -> Unit,
    onHome: () -> Unit, onInventory: () -> Unit) {
    var selected by remember(scene) { mutableStateOf<String?>(null) }
    var focused by remember(scene) { mutableStateOf(false) }
    BackHandler(focused) { focused = false }
    val art = rememberCosmeticArt()
    val buildings = remember(scene, art) {
        CityBuildingProjection.ordered(scene?.buildings.orEmpty().mapNotNull { entry ->
            if (FinishCosmetics.categories.any { entry.building.parts[it] !in art }) null
            else CityBuildingProjection.building(entry)
        })
    }
    val chosen = buildings.firstOrNull { it.source.building.id == selected }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Text("Şəhərim", color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
        Text("Şəhər görünüşü", color = GameTokens.Accent, fontSize = GameTokens.Body)
        if (scene != null) Text("Əhali: ${scene.membership.population}", color = GameTokens.Muted)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val visible = if (focused && chosen != null) listOf(chosen) else buildings
            Canvas(Modifier.fillMaxSize().semantics {
                contentDescription = if (buildings.isEmpty()) "Şəhər səhnəsi" else
                    buildings.joinToString("; ") { "Bina ${it.source.building.id}: ${it.floors.size} mərtəbə" }
                customActions = buildings.map { building -> CustomAccessibilityAction("Bina ${building.source.building.id}: bax") {
                    selected = building.source.building.id; focused = true; true
                } }
            }.pointerInput(visible, maxWidth, maxHeight) {
                detectTapGestures { point ->
                    val view = cityViewport(visible, size.width.toDouble(), size.height.toDouble(), cityArtScale(visible, art)) ?: return@detectTapGestures
                    selected = visible.asReversed().firstOrNull {
                        val b = it.bounds
                        val x = (point.x - view.x) / view.scale
                        val y = (point.y - view.y) / view.scale
                        x >= b.left && x <= b.right && y >= b.top && y <= b.bottom
                    }?.source?.building?.id
                    if (selected == null) focused = false
                }
            }) {
                val view = cityViewport(visible, size.width.toDouble(), size.height.toDouble(), cityArtScale(visible, art)) ?: return@Canvas
                visible.forEach { projected ->
                    val parts = projected.source.building.parts
                    projected.floors.forEachIndexed { index, point ->
                        val scale = view.scale.toFloat()
                        val x = (view.x + (point.x - 50) * view.scale).toFloat()
                        val base = (view.y + point.y * view.scale).toFloat()
                        buildingFloor(art, parts, x, base, scale, index == 0, index == projected.floors.lastIndex)
                    }
                }
            }
        }
        when {
            failed -> { Text("Şəhəri oxumaq mümkün olmadı", color = GameTokens.Danger); HomeButton(label = "Yenilə", onClick = onRetry) }
            scene == null -> Text("Şəhər yüklənir…", color = GameTokens.Muted)
            scene.buildings.isEmpty() -> Text("Şəhərdə bina yoxdur", color = GameTokens.Muted)
            else -> {
                val unavailable = scene.buildings.size - buildings.size
                if (unavailable > 0) Text("$unavailable binanın mövqeyi, həndəsəsi və ya qrafikası mövcud deyil", color = GameTokens.Muted)
                if (chosen != null) Text("${chosen.source.building.floors} mərtəbə · ${chosen.source.building.residents} sakin", color = GameTokens.Text)
                HomeButton(label = if (focused) "Şəhər görünüşü" else "Binaya bax", enabled = buildings.isNotEmpty(),
                    primary = true, compact = true, onClick = {
                        if (selected == null) selected = buildings.lastOrNull()?.source?.building?.id
                        focused = !focused
                    })
            }
        }
        HomeButton(label = "İnventar", compact = true, onClick = onInventory)
        HomeButton(label = "Ana ekran", compact = true, onClick = onHome)
    }
}

internal data class CityViewport(val scale: Double, val x: Double, val y: Double)
internal fun cityViewport(buildings: List<CityBuildingProjection.Building>, width: Double, height: Double, maxScale: Double = Double.POSITIVE_INFINITY): CityViewport? {
    if (buildings.isEmpty() || width <= 0 || height <= 0) return null
    val left = buildings.minOf { it.bounds.left }; val right = buildings.maxOf { it.bounds.right }
    val top = buildings.minOf { it.bounds.top }; val bottom = buildings.maxOf { it.bounds.bottom }
    val scale = minOf(width / (right - left), height / (bottom - top), maxScale)
    return CityViewport(scale, (width - (right + left) * scale) / 2, (height - (bottom + top) * scale) / 2)
}

/** Never enlarge cropped atlas pixels, including in focused building mode. */
private fun cityArtScale(buildings: List<CityBuildingProjection.Building>, art: Map<String, CosmeticSprite>): Double =
    buildings.flatMap { it.source.building.parts.entries }.minOfOrNull { (category, id) ->
        val sprite = requireNotNull(art[id])
        val (width, height) = when (category) {
            "wall" -> 100.0 to 100.0 * 597 / 1064
            "roof" -> 100.0 to 85.0
            "window" -> 40.0 to 35.0
            "entrance" -> 40.0 to 40.0
            "lighting" -> 15.0 to 25.0
            else -> error("Unknown building category")
        }
        maxOf(sprite.size.width / width, sprite.size.height / height)
    } ?: 1.0
