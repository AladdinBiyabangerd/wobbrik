package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    ready: AuthenticatedRecoveryOwner.State.Ready, onHome: () -> Unit, onParts: (String?) -> Unit,
    onDecorations: () -> Unit, onLand: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    var deleteCandidate by remember(ready, selected) { mutableStateOf<OwnedBuilding?>(null) }
    val deletion = remember(configured) { BuildingDeletion(configured.recovery) }
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
                Surface(onClick = back, shape = CircleShape, color = GameTokens.Raised,
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Muted), modifier = Modifier.size(GameTokens.Target)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.semantics { contentDescription = "Geri" }) {
                        Text("‹", color = GameTokens.Text, fontSize = GameTokens.Score)
                    }
                }
                Text(if (selected == null) "İnventar" else "Bina detalları", Modifier.weight(1f), color = GameTokens.Text,
                    fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius),
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                    Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        Image(painterResource(R.drawable.ui_coin), contentDescription = "Sikkə", Modifier.size(GameTokens.Inset))
                        Text(ready.snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (selected == null) {
                InventoryButton("Hissələr · Mağaza") { onParts(null) }
                InventoryButton("Dekor") { onDecorations() }
                InventoryButton("Ərazi alışı") { onLand() }
                Row(Modifier.fillMaxWidth().background(GameTokens.Raised, RoundedCornerShape(GameTokens.CardRadius))
                    .padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    Image(painterResource(R.drawable.ui_inventory), null, Modifier.size(GameTokens.Inset))
                    Text("Binalar", color = GameTokens.Accent, fontWeight = FontWeight.Bold)
                }
                LazyVerticalGrid(GridCells.Adaptive(GameTokens.PanelWidth / 2 - GameTokens.Gap), Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    items(buildings, key = { it.id }) { building ->
                        Surface(onClick = { selected = building.id }, color = GameTokens.Surface,
                            shape = RoundedCornerShape(GameTokens.CardRadius), border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                                Surface(color = GameTokens.Background, shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                                    BuildingPreview(building, Modifier.fillMaxWidth().height(GameTokens.PanelWidth / 2))
                                }
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
                InventoryButton("Bina görünüşü") { onParts(detail?.id) }
                TextButton(onClick = { deleteCandidate = detail }, modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)) {
                    Text("Binanı sil", color = GameTokens.Danger)
                }
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
    deleteCandidate?.let { candidate ->
        BuildingDeleteConfirmation(onDismiss = { deleteCandidate = null }, onConfirm = {
            deleteCandidate = null
            if (deletion.confirmedDelete(ready, candidate)) {
                // Ready is revoked during submission. The next Ready remount reads server inventory.
                selected = null; cursor = null
            }
        })
    }
}

@Composable
private fun BuildingDeleteConfirmation(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val largeFont = LocalDensity.current.fontScale > 1f
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.padding(GameTokens.Gap).widthIn(max = GameTokens.PanelWidth),
            color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius)) {
            Column(Modifier.padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Gap),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Bina silinsin?", color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                Text("Bina inventardan və şəhərdən silinəcək. Sikkə qaytarılmır. Bu əməliyyatı geri qaytarmaq mümkün deyil.",
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    color = GameTokens.Muted, fontSize = GameTokens.Body, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                if (largeFont) Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    DeleteDialogButton("Ləğv et", false, Modifier.fillMaxWidth(), onDismiss)
                    DeleteDialogButton("Binanı sil", true, Modifier.fillMaxWidth(), onConfirm)
                } else Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
                    DeleteDialogButton("Ləğv et", false, Modifier.weight(1f), onDismiss)
                    DeleteDialogButton("Binanı sil", true, Modifier.weight(1f), onConfirm)
                }
            }
        }
    }
}

@Composable
private fun DeleteDialogButton(label: String, destructive: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = GameTokens.Target),
        shape = RoundedCornerShape(GameTokens.ControlRadius),
        colors = ButtonDefaults.buttonColors(containerColor = if (destructive) GameTokens.Danger else GameTokens.Raised,
            contentColor = if (destructive) GameTokens.Background else GameTokens.Text)) {
        Text(label, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
    }
}

private fun buildingName(building: OwnedBuilding, catalog: RecoveryCatalog?): String =
    catalog?.items?.firstOrNull { it.id == building.parts["wall"] }?.name ?: "Bina"

@Composable
private fun BuildingDetail(building: OwnedBuilding, city: CityMembership?, catalog: RecoveryCatalog?, modifier: Modifier) {
    val art = rememberCosmeticArt()
    val value = remember(building, catalog) { BuildingValueBreakdown.from(building, catalog) }
    val largeFont = LocalDensity.current.fontScale > 1.3f
    var tab by rememberSaveable(building.id) { mutableIntStateOf(0) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(Modifier.fillMaxWidth().then(if (largeFont) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            listOf("Sakin", "Dəyər", "Hissələr").forEachIndexed { index, title ->
                Button(onClick = { tab = index }, modifier = (if (largeFont) Modifier.widthIn(min = GameTokens.Target * 2) else Modifier.weight(1f)).heightIn(min = GameTokens.Target),
                    contentPadding = PaddingValues(GameTokens.Quarter), shape = RoundedCornerShape(GameTokens.ControlRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = if (tab == index) GameTokens.Accent else GameTokens.Raised,
                        contentColor = if (tab == index) GameTokens.Background else GameTokens.Text)) { Text(title, softWrap = false,
                        fontSize = if (LocalDensity.current.fontScale > 1f) GameTokens.Caption else GameTokens.Body) }
            }
        }
        Surface(Modifier.weight(1f), color = GameTokens.Surface,
            shape = RoundedCornerShape(GameTokens.CardRadius),
            border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
            if (largeFont) {
                Column(Modifier.fillMaxSize().padding(GameTokens.Small)) {
                    BuildingPreview(building, Modifier.fillMaxWidth().height(GameTokens.Target * 3))
                    BuildingDetailContent(building, city, catalog, art, value, tab, Modifier.weight(1f))
                }
            } else {
                Row(Modifier.fillMaxSize().padding(GameTokens.Small), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    BuildingPreview(building, Modifier.weight(1f).fillMaxHeight())
                    BuildingDetailContent(building, city, catalog, art, value, tab, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BuildingDetailContent(building: OwnedBuilding, city: CityMembership?, catalog: RecoveryCatalog?,
    art: Map<String, CosmeticSprite>, value: BuildingValueBreakdown?, tab: Int, modifier: Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
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
                DetailRow("Hər mərtəbəyə", if (building.residents.toBigInteger() == building.floors.toBigInteger() * java.math.BigInteger.valueOf(4)) "4 sakin" else "—")
                DetailRow("Passiv gəlir", "Yaratmır")
            }
            1 -> {
                if (value != null) {
                    DetailRow("Tikinti", value.construction.toString())
                    DetailRow("Sakin tutumu", value.capacity.toString())
                    DetailRow("Hissələr", value.parts.toString())
                }
                DetailRow("Baza dəyəri", building.baseValue.toString())
                DetailRow("Maksimum satış", building.maxPrice.toString())
                if (building.traded) Text("Alınmış bina yenidən satılmır", color = GameTokens.Muted)
                else if (value != null) {
                    Text("Maksimum qiymətlə satışda", color = GameTokens.Muted, fontSize = GameTokens.Caption)
                    DetailRow("Komissiya", value.commissionAtMaximum.toString())
                    DetailRow("Satıcıya", value.sellerAtMaximum.toString())
                }
                if (value == null) Text("Qiymət bölgüsü mövcud kataloqla təsdiqlənmir", color = GameTokens.Muted)
            }
            2 -> FinishCosmetics.categories.forEach { category ->
                val item = catalog?.items?.firstOrNull { it.id == building.parts[category] }
                Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.ControlRadius),
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                    Row(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Canvas(Modifier.size(GameTokens.Target)) {
                            sprite(art[building.parts[category]], 0f, 0f, size.width, size.height)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                            Text(when(category) { "wall" -> "Divar"; "window" -> "Pəncərə"; "entrance" -> "Giriş"; "roof" -> "Dam"; else -> "İşıq" },
                                color = GameTokens.Muted, fontSize = GameTokens.Caption)
                            Text(item?.name ?: building.parts.getValue(category), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.ControlRadius),
        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Row(Modifier.fillMaxWidth().padding(GameTokens.Small), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Text(label, Modifier.weight(1f), color = GameTokens.Muted)
            Text(value, Modifier.weight(1f), color = GameTokens.Text, fontWeight = FontWeight.Bold)
        }
    }
}

/** Uses accepted coordinates without inventing a straight footprint for legacy buildings. */
@Composable
internal fun BuildingPreview(building: OwnedBuilding, modifier: Modifier) {
    val art = rememberCosmeticArt()
    val gardenImage = ImageBitmap.imageResource(R.drawable.inventory_garden)
    val garden = remember(gardenImage) { CosmeticSprite(gardenImage, androidx.compose.ui.unit.IntOffset.Zero,
        androidx.compose.ui.unit.IntSize(gardenImage.width, gardenImage.height)) }
    val centers = building.geometry?.geometry?.centers
    val missing = building.parts.values.any { it !in art }
    if (centers == null || missing) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(if (missing) "Bina qrafikası mövcud deyil" else "Bu binanın həndəsəsi mövcud deyil", color = GameTokens.Muted)
        }
        return
    }
    Canvas(modifier.semantics { contentDescription = "${building.floors} mərtəbəli bina" }) {
        // Use the same facade/eave anchors as GameScene; centers remain server coordinates.
        // Fit the entire silhouette, including the garden, instead of clipping leaning towers.
        val min = minOf(-90f, centers.min() / 1000f - 50f)
        val max = maxOf(90f, centers.max() / 1000f + 50f)
        val top = (centers.lastIndex * 30f) + 100f
        val below = 55f
        val height = top + below
        val scale = minOf(size.width / (max - min), size.height / height)
        val left = (size.width - (max - min) * scale) / 2
        val bottom = (size.height - height * scale) / 2 + top * scale
        sprite(garden, left + (-90f - min) * scale, bottom - 65f * scale, 180f * scale, 120f * scale)
        centers.forEachIndexed { index, center ->
            val x = left + (center / 1000f - 50f - min) * scale
            val base = bottom - index * 30f * scale
            buildingFloor(art, building.parts, x, base, scale, index == 0, index == centers.lastIndex)
        }
    }
}

@Composable
private fun InventoryButton(label: String, onClick: () -> Unit) {
    HomeButton(label = label, primary = true, compact = true, onClick = onClick)
}
