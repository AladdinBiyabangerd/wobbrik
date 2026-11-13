package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
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
    onHome: () -> Unit, onInventory: () -> Unit, lifeTimeSeconds: Double? = null) {
    var selected by remember(scene) { mutableStateOf<String?>(null) }
    var focused by remember(scene) { mutableStateOf(false) }
    BackHandler(focused) { focused = false }
    val atlases = rememberGameAtlases()
    val art = rememberCosmeticArt() + rememberGameWindowArt()
    val decorArt = rememberDecorationArt()
    val terrainArt = rememberCityTerrainArt()
    val gardenArt = rememberCityGardenArt()
    val lifeArt = rememberCityLifeArt()
    val routes = remember(scene) { scene?.let(CityLifeRoutes::build).orEmpty() }
    val lifeClock = if (lifeTimeSeconds == null) rememberCityLifeClock(scene != null && !focused, scene)
        else rememberUpdatedState(lifeTimeSeconds)
    val contours = remember(scene) { CityTerrainGeometry.contours(scene?.parcels.orEmpty()) }
    val terrainBounds = remember(contours) { CityTerrainGeometry.bounds(contours) }
    val surface = remember(contours) { citySurface(contours) }
    val cliff = remember(contours) { cityCliff(contours) }
    val shore = remember(surface) { cityShore(surface) }
    val decorations = remember(scene, decorArt) { scene?.decorations.orEmpty().mapNotNull { cityDecoration(it, decorArt) } }
    val buildings = remember(scene, art) {
        CityBuildingProjection.ordered(scene?.buildings.orEmpty().mapNotNull { entry ->
            if (FinishCosmetics.categories.any { entry.building.parts[it] !in art }) null
            else CityBuildingProjection.building(entry)
        })
    }
    val chosen = buildings.firstOrNull { it.source.building.id == selected }
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap)) {
        // The reference groups the scene and its action panel into one board. In a
        // tall portrait, cap the scene at its square illustration area and centre
        // the board instead of stretching empty Canvas space between those elements.
        // Compact windows and enlarged type retain all available scene height.
        val tallPortrait = maxHeight > maxWidth * 2 && LocalDensity.current.fontScale <= 1f
        val sceneHeight = if (tallPortrait) maxWidth else maxHeight
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.CenterVertically)) {
            CityHeader(scene?.membership?.population?.toString(), onHome)
            BoxWithConstraints(Modifier.weight(1f, fill = !tallPortrait).fillMaxWidth().heightIn(max = sceneHeight)) {
                val visible = if (focused && chosen != null) listOf(chosen) else buildings
                val sceneBounds = if (focused && chosen != null) emptyList() else
                    listOfNotNull(terrainBounds) + decorations.map {
                        // Sweep envelope for the approved subtle tree motion, with the fixed camera unchanged.
                        if (it.source.copy.itemId.startsWith("decor_tree_")) it.bounds.copy(left=it.bounds.left-2, right=it.bounds.right+2)
                        else it.bounds
                    }
                val maxArtScale = minOf(cityArtScale(visible, art),
                    atlases.getValue("modular").floor.size.width / CityBuildingProjection.ArtWidth,
                    (if (focused) emptyList() else decorations).minOfOrNull { cityDecorationArtScale(it) } ?: Double.POSITIVE_INFINITY)
                Canvas(Modifier.fillMaxSize().semantics {
                    val buildingsDescription = if (buildings.isEmpty()) "Şəhər səhnəsi" else
                        buildings.joinToString("; ") { "Bina ${it.source.building.id}: ${it.floors.size} mərtəbə" }
                    contentDescription = buildingsDescription + "; ${scene?.parcels?.size ?: 0} torpaq sahəsi; ${decorations.size} yerləşdirilmiş dekor"
                    customActions = buildings.map { building -> CustomAccessibilityAction("Bina ${building.source.building.id}: bax") {
                        selected = building.source.building.id; focused = true; true
                    } }
                }.pointerInput(visible, sceneBounds, maxArtScale, maxWidth, maxHeight) {
                    detectTapGestures { point ->
                        val view = cityViewport(visible, size.width.toDouble(), size.height.toDouble(), maxArtScale, sceneBounds) ?: return@detectTapGestures
                        selected = visible.asReversed().firstOrNull {
                            val b = it.bounds
                            val x = (point.x - view.x) / view.scale
                            val y = (point.y - view.y) / view.scale
                            x >= b.left && x <= b.right && y >= b.top && y <= b.bottom
                        }?.source?.building?.id
                        if (selected == null) focused = false
                    }
                }) {
                    val view = cityViewport(visible, size.width.toDouble(), size.height.toDouble(), maxArtScale, sceneBounds) ?: return@Canvas
                    if (!focused) {
                        cityTerrain(surface, cliff, view, terrainArt)
                        cityGardenShore(surface, cliff, shore, view, gardenArt)
                    }
                    if (!focused) decorations.filter { it.road }.sortedBy { it.source.copy.id }.forEach { cityDecoration(it, view) }
                    val seconds = if (focused) 0.0 else lifeClock.value
                    val moving = seconds > 0.0
                    val actors = if (focused) emptyList() else cityLifeActors(routes, seconds)
                    // Roads lie below all upright objects. Buildings and upright decorations share painter depth.
                    val layers = visible.map { Triple(it.foundation.y, it.foundation.x, it.source.building.id) to it } +
                        (if (focused) emptyList() else decorations.filter { !it.road }.map { Triple(it.anchor.y, it.anchor.x, it.source.copy.id) to it }) +
                        actors.map { Triple(it.position.y, it.position.x, it.route.id) to it }
                    layers.sortedWith(compareBy<Pair<Triple<Double, Double, String>, Any>> { it.first.first }
                        .thenBy { it.first.second }.thenBy { it.first.third }).forEach { layer ->
                        val projected = layer.second
                        if (projected is CityDecorationDrawing) {
                            cityLivingDecoration(projected, view, seconds, moving)
                            return@forEach
                        }
                        if (projected is CityLifeActor) {
                            cityLifeActor(projected, view, lifeArt, moving)
                            return@forEach
                        }
                        projected as CityBuildingProjection.Building
                        val parts = projected.source.building.parts
                        projected.floors.forEachIndexed { index, point ->
                            val scale = view.scale.toFloat()
                            val x = (view.x + (point.x - CityBuildingProjection.ArtWidth / 2) * view.scale).toFloat()
                            val base = (view.y + (point.y + CityBuildingProjection.FoundationFront) * view.scale).toFloat()
                            cityBuildingFloor(atlases, art, parts, x, base, scale, index == 0, index == projected.floors.lastIndex,
                                windowAlpha = if (moving) cityWindowAlpha(projected.source.building.id,index,seconds) else 1f)
                            cityFacadeGarden(gardenArt,x,base,CityBuildingProjection.ArtWidth.toFloat()*scale,index == 0,parts["window"] == "coins_window_2")
                        }
                    }
                }
            }
            when {
                failed -> { Text("Şəhəri oxumaq mümkün olmadı", color = GameTokens.Danger); HomeButton(label = "Yenilə", onClick = onRetry) }
                scene == null -> Text("Şəhər yüklənir…", color = GameTokens.Muted)
                else -> {
                    if (scene.buildings.isEmpty()) Text("Şəhərdə bina yoxdur", color = GameTokens.Muted)
                    val unavailableDecor = scene.decorations.size - decorations.size
                    if (unavailableDecor > 0) Text("$unavailableDecor dekorun qrafikası mövcud deyil", color = GameTokens.Muted)
                    val unavailable = scene.buildings.size - buildings.size
                    if (unavailable > 0) Text("$unavailable binanın mövqeyi, həndəsəsi və ya qrafikası mövcud deyil", color = GameTokens.Muted)
                }
            }
            CityViewPanel(chosen?.source?.building, focused, buildings.isNotEmpty()) {
                if (selected == null) selected = buildings.lastOrNull()?.source?.building?.id
                focused = !focused
            }
            CityNavigation(onInventory)
        }
    }
}

internal data class CityViewport(val scale: Double, val x: Double, val y: Double)
internal fun cityViewport(buildings: List<CityBuildingProjection.Building>, width: Double, height: Double, maxScale: Double = Double.POSITIVE_INFINITY, extraBounds: List<CityBuildingProjection.Bounds> = emptyList()): CityViewport? {
    val bounds = buildings.map { it.bounds } + extraBounds
    if (bounds.isEmpty() || width <= 0 || height <= 0) return null
    val left = bounds.minOf { it.left }; val right = bounds.maxOf { it.right }
    val top = bounds.minOf { it.top }; val bottom = bounds.maxOf { it.bottom }
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
        maxOf(sprite.size.width / width, sprite.size.height / height) * 100.0 / CityBuildingProjection.ArtWidth
    } ?: Double.POSITIVE_INFINITY
