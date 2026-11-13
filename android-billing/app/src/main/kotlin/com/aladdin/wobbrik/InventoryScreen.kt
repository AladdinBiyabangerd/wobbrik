package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException

/** Equal snapshots from a new login must still discard the old destination state. */
internal class InventoryReadKey(private val ready: AuthenticatedRecoveryOwner.State.Ready) {
    override fun equals(other: Any?) = other is InventoryReadKey && ready === other.ready
    override fun hashCode() = System.identityHashCode(ready)
}

/** Destination-local reads are cancelled and discarded whenever the exact Ready epoch changes. */
@Composable
internal fun InventoryScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onHome: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    var cursor by remember { mutableStateOf<String?>(null) }
    var buildings by remember(ready) { mutableStateOf(emptyList<OwnedBuilding>()) }
    var next by remember(ready) { mutableStateOf<String?>(null) }
    var city by remember(ready) { mutableStateOf<CityMembership?>(null) }
    var detail by remember(ready, selected) { mutableStateOf<OwnedBuilding?>(null) }
    var loading by remember(ready, selected) { mutableStateOf(true) }
    var failed by remember(ready, selected) { mutableStateOf(false) }
    LaunchedEffect(ready, selected, cursor, retry) {
        loading = true; failed = false
        try {
            val session = requireNotNull(configured.sessions.current())
            check(session.uid == ready.accountKey && configured.recovery.state.value === ready)
            if (selected == null) {
                val page = configured.inventory.page(session, cursor)
                check(configured.sessions.current() == session && configured.recovery.state.value === ready)
                buildings = if (cursor == null) page.buildings else (buildings + page.buildings).distinctBy { it.id }
                next = page.next
            } else {
                detail = null; city = null
                val building = configured.inventory.building(session, requireNotNull(selected))
                val membership = configured.inventory.city(session)
                check(configured.sessions.current() == session && configured.recovery.state.value === ready)
                detail = building; city = membership
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    val back = { if (selected != null) selected = null else onHome() }
    BackHandler(onBack = back)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                TextButton(onClick = back, modifier = Modifier.sizeIn(minWidth = GameTokens.Target, minHeight = GameTokens.Target)) { Text("←", color = GameTokens.Text) }
                Text(if (selected == null) "İnventar" else "Bina detalları", Modifier.weight(1f), color = GameTokens.Text,
                    fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                Text("${ready.snapshot.coins} ●", color = GameTokens.Text, fontWeight = FontWeight.Bold)
            }
            if (selected == null) {
                Text("Binalar", Modifier.fillMaxWidth().background(GameTokens.Raised, RoundedCornerShape(GameTokens.ControlRadius))
                    .padding(GameTokens.Small), color = GameTokens.Accent, fontWeight = FontWeight.Bold)
                LazyVerticalGrid(GridCells.Adaptive(GameTokens.PanelWidth / 2 - GameTokens.Gap), Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    items(buildings, key = { it.id }) { building ->
                        Surface(onClick = { selected = building.id }, color = GameTokens.Surface,
                            shape = RoundedCornerShape(GameTokens.CardRadius), border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                                BuildingPreview(building, Modifier.fillMaxWidth().height(GameTokens.PanelWidth / 2))
                                Text(buildingName(building, ready.catalog), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                                Text("${building.floors} mərtəbə  ›", color = GameTokens.Muted)
                            }
                        }
                    }
                }
                if (buildings.isEmpty() && !loading && !failed) Text("Hələ bina yoxdur. Ən azı 10 mərtəbəli cəhdi tamamla.", color = GameTokens.Muted)
                if (next != null && !loading && !failed) InventoryButton("Daha çox") { cursor = next }
                if (!loading) TextButton(onClick = { cursor = null; buildings = emptyList(); next = null; retry++ }) { Text("Yenilə", color = GameTokens.Accent) }
            } else if (detail != null) {
                BuildingDetail(requireNotNull(detail), city, ready.catalog, Modifier.weight(1f))
            } else Spacer(Modifier.weight(1f))
            if (loading) Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(GameTokens.Inset), color = GameTokens.Accent)
                Text("Yüklənir…", color = GameTokens.Muted)
            }
            if (failed) {
                Text("Məlumat yenilənmədi. Bağlantını yoxla və yenidən cəhd et.", color = GameTokens.Danger)
                InventoryButton("Yenidən cəhd et") { retry++ }
            }
            InventoryButton(if (selected == null) "Ana ekran" else "İnventara qayıt", back)
        }
    }
}

private fun buildingName(building: OwnedBuilding, catalog: RecoveryCatalog?): String =
    catalog?.items?.firstOrNull { it.id == building.parts["wall"] }?.name ?: "Bina"

@Composable
private fun BuildingDetail(building: OwnedBuilding, city: CityMembership?, catalog: RecoveryCatalog?, modifier: Modifier) {
    val art = rememberCosmeticArt()
    var tab by rememberSaveable(building.id) { mutableIntStateOf(0) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            listOf("Sakin", "Dəyər", "Hissələr").forEachIndexed { index, title ->
                Button(onClick = { tab = index }, modifier = Modifier.weight(1f).heightIn(min = GameTokens.Target),
                    contentPadding = PaddingValues(GameTokens.Quarter), shape = RoundedCornerShape(GameTokens.ControlRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = if (tab == index) GameTokens.Accent else GameTokens.Raised,
                        contentColor = if (tab == index) GameTokens.Background else GameTokens.Text)) { Text(title, softWrap = false,
                        fontSize = if (LocalDensity.current.fontScale > 1f) GameTokens.Caption else GameTokens.Body) }
            }
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            BuildingPreview(building, Modifier.weight(1f).fillMaxHeight())
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Text(buildingName(building, catalog), color = GameTokens.Text, fontSize = GameTokens.Hint, fontWeight = FontWeight.Bold)
            when (tab) {
                0 -> {
                    DetailRow("Mərtəbə", building.floors.toString()); DetailRow("Sakin", building.residents.toString())
                    Text(when {
                        city == null -> "Şəhərə aidlik yüklənməyib"
                        city.buildings[building.id] == building.revision -> "✓ Şəhərdə yerləşdirilib"
                        building.id in city.buildings -> "Şəhər məlumatı dəyişib. Yenilə."
                        else -> "İnventardadır"
                    }, color = GameTokens.Muted)
                    Text("Passiv gəlir yaratmır", color = GameTokens.Muted)
                }
                1 -> { DetailRow("Baza dəyəri", building.baseValue.toString()); DetailRow("Maksimum satış", building.maxPrice.toString()) }
                2 -> FinishCosmetics.categories.forEach { category ->
                    val item = catalog?.items?.firstOrNull { it.id == building.parts[category] }
                    Canvas(Modifier.fillMaxWidth().height(GameTokens.Target)) {
                        sprite(art[building.parts[category]], 0f, 0f, size.width, size.height)
                    }
                    DetailRow(when(category) { "wall" -> "Divar"; "window" -> "Pəncərə"; "entrance" -> "Giriş"; "roof" -> "Dam"; else -> "İşıq" }, item?.name ?: building.parts.getValue(category))
                }
            }
        }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.ControlRadius)) {
        Row(Modifier.fillMaxWidth().padding(GameTokens.Small), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Text(label, Modifier.weight(1f), color = GameTokens.Muted)
            Text(value, Modifier.weight(1f), color = GameTokens.Text, fontWeight = FontWeight.Bold)
        }
    }
}

/** Uses accepted coordinates without inventing a straight footprint for legacy buildings. */
@Composable
private fun BuildingPreview(building: OwnedBuilding, modifier: Modifier) {
    val art = rememberCosmeticArt()
    val centers = building.geometry?.geometry?.centers
    val missing = building.parts.values.any { it !in art }
    if (centers == null || missing) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(if (missing) "Bina qrafikası mövcud deyil" else "Bu binanın həndəsəsi mövcud deyil", color = GameTokens.Muted)
        }
        return
    }
    Canvas(modifier.semantics { contentDescription = "${building.floors} mərtəbəli bina" }) {
        val min = minOf(0L, centers.min()) / 1000f - 50f
        val max = maxOf(0L, centers.max()) / 1000f + 50f
        val height = centers.size * 30f + 120f
        val scale = minOf(size.width / (max - min), size.height / height)
        val left = (size.width - (max - min) * scale) / 2
        val bottom = (size.height + height * scale) / 2 - 40f * scale
        centers.forEachIndexed { index, center ->
            val x = left + (center / 1000f - 50f - min) * scale
            val y = bottom - (index + 1) * 30f * scale
            sprite(art[building.parts["wall"]], x, y, 100f * scale, 70f * scale)
            if (index > 0) sprite(art[building.parts["window"]], x + 25f * scale, y + 15f * scale, 30f * scale, 25f * scale)
            if (index == 0) {
                sprite(art[building.parts["entrance"]], x + 30f * scale, y + 15f * scale, 40f * scale, 50f * scale)
                sprite(art[building.parts["lighting"]], x + 70f * scale, y + 20f * scale, 18f * scale, 30f * scale)
            }
            if (index == centers.lastIndex) roof(art[building.parts["roof"]], x, y + 10f * scale, 100f * scale)
        }
    }
}

@Composable
private fun InventoryButton(label: String, onClick: () -> Unit) {
    Button(onClick, Modifier.fillMaxWidth().heightIn(min = GameTokens.Target), shape = RoundedCornerShape(GameTokens.ControlRadius),
        colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Accent, contentColor = GameTokens.Background)) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}
