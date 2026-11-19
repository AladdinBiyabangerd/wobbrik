package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    CityScreen(configured, AccountView(ReadOnlyAccount(requireNotNull(ready.accountKey), ready.snapshot, requireNotNull(ready.catalog)), ready), onHome, onInventory)
}

@Composable
internal fun CityScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AccountView, onHome: () -> Unit, onInventory: () -> Unit, lifeTimeSeconds: Double? = null) {
    var fromCache by remember { mutableStateOf(ready.ready == null) }
    var scene by remember { mutableStateOf<CityScene?>(null) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(retry) {
        scene = null; failed = false; fromCache = ready.ready == null
        try {
            val session = requireNotNull(configured.sessions.current())
            check(ready.current(configured, session))
            val loaded = configured.views?.read(ready, session, "city",
                onCached = { fromCache = true }, refresh = retry > 0,
                onLocal = { local ->
                    check(ready.current(configured, session))
                    scene = local
                },
                onFresh = {
                    check(ready.current(configured, session))
                    fromCache = false
                }) { CitySceneRepository(it).load(session) } ?: configured.cityScene.load(session)
            check(ready.current(configured, session))
            scene = loaded
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) {
            android.util.Log.w("WobbrikCity", "City read failed", failure)
            scene = null
            failed = true
        }
    }
    BackHandler(onBack = onHome)
    CityContent(scene, failed, { retry++ }, onHome, onInventory, lifeTimeSeconds = lifeTimeSeconds, cached = fromCache, catalog = ready.catalog)
}

@Composable
internal fun CityContent(scene: CityScene?, failed: Boolean, onRetry: () -> Unit,
    onHome: () -> Unit, onInventory: () -> Unit, lifeTimeSeconds: Double? = null, cached: Boolean = false,
    catalog: RecoveryCatalog? = null) {
    if (failed && LocalMobileMenuShell.current && !LocalCityCollection.current) {
        CityErrorContent(onRetry, onHome, onInventory)
        return
    }
    var selected by remember(scene) { mutableStateOf<String?>(null) }
    var focused by remember(scene) { mutableStateOf(false) }
    BackHandler(focused) { focused = false }
    val atlases = rememberGameAtlases()
    val art = rememberCosmeticArt() + rememberGameWindowArt()
    val cityArt = rememberCitySceneArt(scene != null)
    val illustration = if (LocalCityCollection.current) cityArt.assets?.illustration else null
    val decorArt = remember(cityArt.assets, illustration) { cityArt.assets?.decorations.orEmpty() +
        (illustration?.let { mapOf("decor_tree_1" to it.tree) } ?: emptyMap()) }
    val terrainArt = cityArt.assets?.terrain
    val gardenArt = cityArt.assets?.garden
    val lifeArt = cityArt.assets?.life.orEmpty()
    val routes = remember(scene) { scene?.let(CityLifeRoutes::build).orEmpty() }
    val lifeClock = if (lifeTimeSeconds == null) rememberCityLifeClock(scene != null && !focused, scene)
        else rememberUpdatedState(lifeTimeSeconds)
    val contours = remember(scene) { CityTerrainGeometry.contours(scene?.parcels.orEmpty()) }
    val illustrated = LocalCityCollection.current
    val terrainBounds = remember(contours, illustrated) { CityTerrainGeometry.bounds(contours)?.let {
        if (illustrated) it.copy(bottom=it.bottom+CityIslandStyle.BankDepth+48-CityTerrainGeometry.CliffDepth) else it } }
    val surface = remember(contours) { citySurface(contours) }
    val cliff = remember(contours, illustrated) { cityCliff(contours, if (illustrated) CityIslandStyle.BankDepth else CityTerrainGeometry.CliffDepth) }
    val shore = remember(surface) { cityShore(surface) }
    val decorations = remember(scene, decorArt) { scene?.decorations.orEmpty().mapNotNull { cityDecoration(it, decorArt) } }
    val buildings = remember(scene, art) {
        CityBuildingProjection.ordered(scene?.buildings.orEmpty().mapNotNull { entry ->
            if (FinishCosmetics.categories.any { entry.building.parts[it] !in art }) null
            else CityBuildingProjection.building(entry)
        })
    }
    val chosen = buildings.firstOrNull { it.source.building.id == selected }
        ?: if (LocalMobileMenuShell.current) buildings.lastOrNull() else null
    val collectionMarker = LocalCityCollection.current
    val illustratedGarden = rememberCityIllustratedGarden(illustrated, buildings, shore, decorations, surface)
    val sceneContent: @Composable () -> Unit = {
        if (cityArt.assets == null) {
            ApprovedArtStatus(cityArt.status, cityArt.retry)
        } else BoxWithConstraints(Modifier.fillMaxSize()) {
            val visible = if (focused && chosen != null) listOf(chosen) else buildings
            val sceneBounds = if (focused && chosen != null) emptyList() else
                listOfNotNull(terrainBounds) + (if (illustration == null) emptyList() else illustratedGarden.trees.map {
                    val width=CityIslandStyle.TreeWidth.toDouble()
                    val height=width*illustration.tree.size.height/illustration.tree.size.width
                    CityBuildingProjection.Bounds(it.anchor.x-width/2,it.anchor.y-height,it.anchor.x+width/2,it.anchor.y.toDouble()) }) + decorations.map {
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
                this[CitySceneryPrepared] = illustration == null || illustratedGarden.prepared
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
                    cityTerrain(surface, cliff, view, requireNotNull(terrainArt), illustration != null)
                    if (illustration == null) cityGardenShore(surface, cliff, shore, view, requireNotNull(gardenArt))
                    else {
                        cityIllustratedGarden(surface, shore, illustratedGarden, view, illustration)
                        cityIllustratedShore(surface, cliff, shore, view, illustration)
                    }
                }
                if (!focused) decorations.filter { it.road }.sortedBy { it.source.copy.id }.forEach { cityDecoration(it, view) }
                if (collectionMarker && chosen != null && !focused) {
                    val point = chosen.foundation
                    val x = (view.x + point.x * view.scale).toFloat()
                    val y = (view.y + point.y * view.scale).toFloat()
                    val half = (CityBuildingProjection.ArtWidth * view.scale / 2).toFloat()
                    val ring = androidx.compose.ui.graphics.Path().apply {
                        moveTo(x-half,y); lineTo(x,y-half/2); lineTo(x+half,y)
                        lineTo(x,y+half/2); close()
                    }
                    drawPath(ring, GameTokens.Accent, style = androidx.compose.ui.graphics.drawscope.Stroke(GameTokens.Hairline.toPx()*2))
                }
                val seconds = if (focused) 0.0 else lifeClock.value
                val moving = seconds > 0.0
                val actors = if (focused) emptyList() else cityLifeActors(routes, seconds)
                // Roads lie below all upright objects. Buildings and upright decorations share painter depth.
                val layers = visible.map { Triple(it.foundation.y, it.foundation.x, it.source.building.id) to it } +
                    (if (focused) emptyList() else decorations.filter { !it.road }.map { Triple(it.anchor.y, it.anchor.x, it.source.copy.id) to it }) +
                    actors.map { Triple(it.position.y, it.position.x, it.route.id) to it } +
                    (if (focused || illustration == null) emptyList() else illustratedGarden.trees.mapIndexed { i,it ->
                        Triple(it.anchor.y.toDouble(),it.anchor.x.toDouble(),"garden-$i") to it })
                layers.sortedWith(compareBy<Pair<Triple<Double, Double, String>, Any>> { it.first.first }
                    .thenBy { it.first.second }.thenBy { it.first.third }).forEach { layer ->
                    val projected = layer.second
                    if (projected is CityGardenTree) {
                        cityGardenTree(projected,view,requireNotNull(illustration))
                        return@forEach
                    }
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
                        cityBuildingFloor(atlases, art, illustration, parts, x, base, scale, index == 0, index == projected.floors.lastIndex,
                            windowAlpha = if (moving) cityWindowAlpha(projected.source.building.id,index,seconds) else 1f)
                        cityFacadeGarden(requireNotNull(gardenArt),x,base,CityBuildingProjection.ArtWidth.toFloat()*scale,index == 0,parts["window"] == "coins_window_2", cafe = index == 1)
                    }
                }
            }
        }
    }
    if (LocalCityCollection.current) {
        CityCollectionContent(scene, failed, cached, chosen?.source?.building, catalog, focused,
            onRetry, onInventory,
            onFit = { focused = false; selected = null },
            onFocus = { if (selected == null) selected = chosen?.source?.building?.id; focused = !focused },
            sceneContent = sceneContent)
        return
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap)) {
        // The reference groups the scene and its action panel into one board. In a
        // tall portrait, cap the scene at its square illustration area and centre
        // the board instead of stretching empty Canvas space between those elements.
        // Enlarged type in the mobile shell uses a bounded scene in scrollable content;
        // its primary view/back action stays outside the scroll area.
        val tallPortrait = maxHeight > maxWidth * 2 && LocalDensity.current.fontScale <= 1f
        val sceneHeight = if (tallPortrait) maxWidth else maxHeight
        val scrollScene = LocalMobileMenuShell.current &&
            (LocalDensity.current.fontScale > 1f || maxHeight < GameTokens.CompactHeight)
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.CenterVertically)) {
            val body: @Composable ColumnScope.() -> Unit = {
                CityHeader(scene?.membership?.population?.toString(), onHome, cached = cached)
                if (!scrollScene || scene != null) Box((if (scrollScene) Modifier.height(GameTokens.Target * 3)
                    else Modifier.weight(1f, fill = !tallPortrait).heightIn(max = sceneHeight)).fillMaxWidth()) {
                    sceneContent()
                }
                when {
                    failed -> {
                        Text("Şəhəri oxumaq mümkün olmadı", color = GameTokens.Danger)
                        HomeButton(label = "Yenilə", onClick = onRetry)
                    }
                    scene == null -> Text("Şəhər yüklənir…", color = GameTokens.Muted)
                    else -> {
                        if (scene.buildings.isEmpty()) Text("Şəhərdə bina yoxdur", color = GameTokens.Muted)
                        val unavailableDecor = scene.decorations.size - decorations.size
                        if (unavailableDecor > 0) Text("$unavailableDecor dekorun qrafikası mövcud deyil", color = GameTokens.Muted)
                        val unavailable = scene.buildings.size - buildings.size
                        if (unavailable > 0) Text("$unavailable binanın mövqeyi, həndəsəsi və ya qrafikası mövcud deyil", color = GameTokens.Muted)
                    }
                }
                if (scrollScene) {
                    Text(if (focused) "Bina görünüşü" else "Binaya bax", color = GameTokens.Text, fontSize = GameTokens.Body)
                    chosen?.source?.building?.let { Text("${it.floors} mərtəbə · ${it.residents} sakin", color = GameTokens.Muted, fontSize = GameTokens.Caption) }
                }
            }
            if (scrollScene) Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = body)
            else body()
            val onView = {
                if (selected == null) selected = buildings.lastOrNull()?.source?.building?.id
                focused = !focused
            }
            if (LocalMobileMenuShell.current && scene != null) CityCameraControls(focused, buildings.isNotEmpty(),
                onFit = { focused = false; selected = null }, onFocus = {
                    if (selected == null) selected = chosen?.source?.building?.id
                    focused = !focused
                })
            if (scrollScene) HomeButton(if (focused) "Geri" else "Bax", primary = true, compact = true,
                showPlayIcon = !focused, enabled = buildings.isNotEmpty(), onClick = onView)
            else CityViewPanel(chosen?.source?.building, focused, buildings.isNotEmpty(), onView)
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
