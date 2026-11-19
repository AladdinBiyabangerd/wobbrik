package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException

/** Equal snapshots from a new login must still discard the old destination state. */
internal class InventoryReadKey(private val ready: Any) {
    override fun equals(other: Any?) = other is InventoryReadKey && ready === other.ready
    override fun hashCode() = System.identityHashCode(ready)
}

/** A user refresh belongs to one read, not every later list/detail/page visit. */
internal class InventoryRefreshRequest {
    private var consumed = 0
    fun consume(generation: Int): Boolean = (generation != consumed).also { consumed = generation }
}

/** Destination-local reads are cancelled and discarded whenever the exact Ready epoch changes. */
@Composable
internal fun InventoryScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, selected: String?, onSelectBuilding: (String?) -> Unit, onHome: () -> Unit, onParts: (String?) -> Unit,
    onDecorations: () -> Unit, onLand: () -> Unit, onMarket: (() -> Unit)? = null) {
    InventoryScreen(configured, AccountView(appbackend.wobbrik.game.ReadOnlyAccount(requireNotNull(ready.accountKey), ready.snapshot, requireNotNull(ready.catalog)), ready), selected, onSelectBuilding, onHome, onParts, onDecorations, onLand, onMarket)
}

@Composable
internal fun InventoryScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AccountView, selected: String?, onSelectBuilding: (String?) -> Unit, onHome: () -> Unit, onParts: (String?) -> Unit,
    onDecorations: () -> Unit, onLand: () -> Unit, onMarket: (() -> Unit)? = null) {
    var fromCache by remember(ready) { mutableStateOf(ready.ready == null) }
    val canMutate = ready.ready != null && !fromCache
    val sizing = mobileUiSizing()
    val largeFont = LocalDensity.current.fontScale > 1f
    var retry by remember { mutableIntStateOf(0) }
    val refreshRequest = remember(ready) { InventoryRefreshRequest() }
    var deleteCandidate by remember(ready, selected) { mutableStateOf<OwnedBuilding?>(null) }
    val deletion = remember(configured) { BuildingDeletion(configured.recovery) }
    var cursor by remember { mutableStateOf<String?>(null) }
    var buildings by remember(ready) { mutableStateOf(emptyList<OwnedBuilding>()) }
    var next by remember(ready) { mutableStateOf<String?>(null) }
    val city: CityMembership? = null // Membership arrives with the selected building; no whole-city scan.
    var detail by remember(ready, selected) { mutableStateOf<OwnedBuilding?>(null) }
    var loading by remember(ready, selected) { mutableStateOf(true) }
    var failed by remember(ready, selected) { mutableStateOf(false) }
    LaunchedEffect(ready, selected, cursor, retry) {
        val refresh = refreshRequest.consume(retry)
        loading = true; failed = false; fromCache = ready.ready == null
        try {
            val session = requireNotNull(configured.sessions.current())
            check(ready.current(configured, session))
            if (selected == null) {
                fun present(page: InventoryPage) {
                    check(ready.current(configured, session))
                    buildings = if (cursor == null) page.buildings else (buildings + page.buildings).distinctBy { it.id }
                    next = page.next
                }
                val page = configured.views?.read(ready, session, "inventory-${cursor.orEmpty()}",
                    onCached = { fromCache = true }, refresh = refresh, onLocal = { present(it) }, onFresh = { fromCache = false }) {
                    InventoryRepository(it).page(session, cursor)
                } ?: configured.inventory.page(session, cursor)
                present(page)
            } else {
                detail = null
                fun present(building: OwnedBuilding) {
                    check(ready.current(configured, session))
                    detail = building
                }
                // Detail has its own accepted bundle: optional membership must not gate it.
                // Legacy detail bundles already contain this exact building path.
                val building = configured.views?.read(ready, session, "detail-$selected",
                    onCached = { fromCache = true }, refresh = refresh,
                    onLocal = { present(it) }, onFresh = { fromCache = false }) { reader ->
                    InventoryRepository(reader).building(session, requireNotNull(selected))
                } ?: configured.inventory.building(session, requireNotNull(selected))
                present(building)
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    if(LocalMobileMenuShell.current) CommerceDetailChrome(selected != null)
    val back = { if (selected != null) onSelectBuilding(null) else onHome() }
    BackHandler(onBack = back)
    val needsGarden = LocalBuildingArt.current.status == ArtPreparation.Ready &&
        (buildings.any { !usesPurpleTemplate(it.parts) } || detail?.let { !usesPurpleTemplate(it.parts) } == true)
    InventoryGardenArt(needsGarden) {
    Box(Modifier.fillMaxSize().then(if(LocalMobileMenuShell.current) Modifier else Modifier.background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background))))
        .then(if(LocalMobileMenuShell.current) Modifier else Modifier.safeDrawingPadding()).then(if(LocalCollectionLayout.current) Modifier.padding(horizontal = GameTokens.InventoryCollection.ScreenInset)
            else Modifier.padding(GameTokens.Gap))) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            if(LocalMobileMenuShell.current && selected != null) OwnedDetailTitle(back)
            if(!LocalMobileMenuShell.current) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Surface(onClick = back, shape = if (sizing.portrait) RoundedCornerShape(GameTokens.ControlRadius) else CircleShape, color = GameTokens.Raised,
                    border = if (sizing.portrait) null else BorderStroke(GameTokens.Hairline, GameTokens.Muted), modifier = Modifier.size(GameTokens.Target)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.semantics { contentDescription = "Geri" }) {
                        Text("‹", color = GameTokens.Text, fontSize = if (sizing.portrait) GameTokens.Title else GameTokens.Score)
                    }
                }
                if (sizing.portrait && largeFont) Spacer(Modifier.weight(1f))
                else Text(if (selected == null) "İnventar" else "Bina detalları", Modifier.weight(1f), color = GameTokens.Text,
                    fontSize = sizing.heading, fontWeight = FontWeight.Bold)
                if (selected == null && sizing.portrait) IconButton(onClick = {
                    cursor = null; buildings = emptyList(); next = null; retry++
                }, enabled = !loading, modifier = Modifier.size(GameTokens.Target).semantics { contentDescription = "Yenilə" }) {
                    Text("↻", color = GameTokens.Accent, fontSize = GameTokens.Title)
                }
                if (!LocalMobileMenuShell.current) Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius),
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                    Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        Image(painterResource(R.drawable.ui_coin), contentDescription = "Sikkə", Modifier.size(GameTokens.Inset))
                        Text(ready.snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (sizing.portrait && largeFont && !LocalMobileMenuShell.current) Text(if (selected == null) "İnventar" else "Bina detalları",
                color = GameTokens.Text, fontSize = sizing.heading, fontWeight = FontWeight.Bold)
            if (selected == null) {
                if (sizing.portrait) {
                    if(!LocalMobileMenuShell.current) InventoryToolbar(largeFont, { onParts(null) }, onDecorations, onLand, enabled = canMutate)
                    if (LocalMobileMenuShell.current) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${buildings.size}${if(next != null) "+" else ""} bina", Modifier.weight(1f), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                        CollectionRefresh(enabled = !loading) { cursor = null; buildings = emptyList(); next = null; retry++ }
                    }
                    else Text("Binalar", Modifier.padding(top = GameTokens.Small, bottom = GameTokens.Quarter),
                        color = GameTokens.Text, fontSize = GameTokens.Hint, fontWeight = FontWeight.Bold)
                } else {
                    InventoryButton("Hissələr · Mağaza", enabled = canMutate) { onParts(null) }
                    InventoryButton("Dekor", enabled = canMutate) { onDecorations() }
                    InventoryButton("Ərazi alışı", enabled = canMutate) { onLand() }
                    Row(Modifier.fillMaxWidth().background(GameTokens.Raised, RoundedCornerShape(GameTokens.CardRadius))
                        .padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Image(painterResource(R.drawable.ui_inventory), null, Modifier.size(GameTokens.Inset))
                        Text("Binalar", color = GameTokens.Accent, fontWeight = FontWeight.Bold)
                    }
                }
                InventoryBuildingGrid(buildings, sizing.portrait, largeFont, Modifier.weight(1f)) { building ->
                    if(LocalMobileMenuShell.current) MobileOwnedBuildingCard(building, ready.catalog, city) { onSelectBuilding(building.id) }
                    else Surface(onClick = { onSelectBuilding(building.id) }, color = GameTokens.Surface,
                        shape = RoundedCornerShape(if (sizing.portrait) GameTokens.ControlRadius else GameTokens.CardRadius), border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                        if (sizing.portrait && largeFont) Row(Modifier.padding(GameTokens.Small),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                            BuildingPreview(building, Modifier.size(GameTokens.Target))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                                Text(buildingName(building, ready.catalog), color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.SemiBold)
                                Text("${building.floors} mərtəbə  ›", color = GameTokens.Muted, fontSize = GameTokens.Caption)
                            }
                        } else Column(Modifier.background(Brush.verticalGradient(if (sizing.portrait)
                            listOf(GameTokens.Raised, GameTokens.Surface) else listOf(GameTokens.Surface, GameTokens.Surface)))
                            .padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                            if (sizing.portrait) BuildingPreview(building, Modifier.fillMaxWidth().height(sizing.listPreview))
                            else Surface(color = GameTokens.Background, shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                                BuildingPreview(building, Modifier.fillMaxWidth().height(sizing.listPreview))
                            }
                            Text(buildingName(building, ready.catalog), color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.SemiBold)
                            Text("${building.floors} mərtəbə  ›", color = GameTokens.Muted, fontSize = if (sizing.portrait) GameTokens.Caption else GameTokens.Body)
                        }
                    }
                }
                if (buildings.isEmpty() && !loading && !failed) Text("Hələ bina yoxdur. Ən azı 10 mərtəbəli cəhdi tamamla.", color = GameTokens.Muted)
                if (next != null && !loading && !failed) InventoryButton("Daha çox") { cursor = next }
                if (!loading && !sizing.portrait) TextButton(onClick = { cursor = null; buildings = emptyList(); next = null; retry++ }) { Text("Yenilə", color = GameTokens.Accent) }
            } else if (detail != null) {
                if(LocalMobileMenuShell.current) MobileOwnedBuildingDetail(requireNotNull(detail), city, ready.catalog,
                    Modifier.weight(1f), canMutate, { deleteCandidate = detail },
                    if(fromCache) "Yalnız baxış" else "Bina görünüşü", { onParts(detail?.id) }, onMarket)
                else {
                    BuildingDetail(requireNotNull(detail), city, ready.catalog, Modifier.weight(1f), canMutate) { deleteCandidate = detail }
                    InventoryButton(if (fromCache) "Yalnız baxış" else "Bina görünüşü", enabled = canMutate) { onParts(detail?.id) }
                    if(onMarket != null) HomeButton("Bazara çıxar", icon = R.drawable.ui_market,
                        enabled = canMutate && detail?.traded == false, onClick = onMarket)
                    TextButton(enabled = canMutate, onClick = { deleteCandidate = detail }, modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)) {
                        Text("Binanı sil", color = if(canMutate) GameTokens.Danger else GameTokens.Muted)
                    }
                }
            } else Spacer(Modifier.weight(1f))
            if (fromCache && selected == null) Text("Keş · yalnız baxış", color = GameTokens.Muted, fontSize = GameTokens.Caption,
                modifier = Modifier.semantics { contentDescription = "Saxlanmış məlumat · yalnız baxış" })
            if (loading) Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(GameTokens.Inset), color = GameTokens.Accent)
                Text("Yüklənir…", color = GameTokens.Muted)
            }
            if (failed) {
                Text("Məlumat yenilənmədi. Bağlantını yoxla və yenidən cəhd et.", color = GameTokens.Danger)
                InventoryButton("Yenidən cəhd et") { retry++ }
            }
            if(LocalCollectionLayout.current && selected == null && (loading || failed || buildings.isEmpty()))
                Spacer(Modifier.height(LocalCollectionDockClearance.current))
            if (!LocalMobileMenuShell.current && sizing.portrait) HomeButton(if (selected == null) "Ana ekran" else "İnventara qayıt",
                icon = if (selected == null) R.drawable.ui_home else null, onClick = back)
            else if (!LocalMobileMenuShell.current) InventoryButton(if (selected == null) "Ana ekran" else "İnventara qayıt", onClick = back)
        }
    }
    } // The list and detail share one composition-owned garden decode.
    deleteCandidate?.let { candidate ->
        BuildingDeleteConfirmation(onDismiss = { deleteCandidate = null }, onConfirm = {
            deleteCandidate = null
            if (canMutate && ready.ready?.let { deletion.confirmedDelete(it, candidate) } == true) {
                // Ready is revoked during submission. The next Ready remount reads server inventory.
                onSelectBuilding(null); cursor = null
            }
        })
    }
}

/** Mobile inventory has fixed equal columns, so lazily compose rows without the
 * grid's extra track/slot machinery. Tablet retains its existing grid. */
@Composable
private fun InventoryBuildingGrid(buildings: List<OwnedBuilding>, portrait: Boolean, largeFont: Boolean,
    modifier: Modifier, content: @Composable (OwnedBuilding) -> Unit) {
    if (!portrait) {
        LazyVerticalGrid(GridCells.Adaptive(GameTokens.PanelWidth / 2 - GameTokens.Gap), modifier,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            items(buildings, key = { it.id }) { content(it) }
        }
        return
    }
    val columns = if (largeFont) 1 else 2
    val rows = remember(buildings, columns) { buildings.chunked(columns) }
    LazyColumn(modifier.semantics { collectionInfo = CollectionInfo(rows.size, columns) },
        contentPadding = if(LocalCollectionLayout.current) PaddingValues(bottom = LocalCollectionDockClearance.current) else PaddingValues(),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        itemsIndexed(rows, key = { _, row -> row.first().id }) { rowIndex, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                row.forEachIndexed { column, building -> key(building.id) {
                    Box(Modifier.weight(1f).semantics {
                        collectionItemInfo = CollectionItemInfo(rowIndex, 1, column, 1)
                    }) { content(building) }
                } }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
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

internal fun buildingName(building: OwnedBuilding, catalog: RecoveryCatalog?): String =
    catalog?.items?.firstOrNull { it.id == building.parts["wall"] }?.name ?: "Bina"

@Composable
private fun BuildingDetail(building: OwnedBuilding, city: CityMembership?, catalog: RecoveryCatalog?, modifier: Modifier, canDelete: Boolean, onDelete: () -> Unit) {
    val art = if (LocalBuildingArt.current.status == ArtPreparation.Ready) rememberCosmeticArt() else emptyMap()
    val value = remember(building, catalog) { BuildingValueBreakdown.from(building, catalog) }
    val largeFont = LocalDensity.current.fontScale > 1.3f
    var tab by rememberSaveable(building.id) { mutableIntStateOf(0) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(Modifier.fillMaxWidth().then(if (largeFont) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            listOf("Sakin", "Dəyər", "Hissələr").forEachIndexed { index, title ->
                Button(onClick = { tab = index }, modifier = (if (largeFont) Modifier.widthIn(min = GameTokens.Target * 2) else Modifier.weight(1f)).heightIn(min = GameTokens.Target).partsEdge(tab == index).partsTabFill(tab == index),
                    border = if (mobileUiSizing().portrait) BorderStroke(GameTokens.Hairline, if (tab == index) GameTokens.Text else GameTokens.Raised) else null,
                    contentPadding = PaddingValues(GameTokens.Quarter), shape = RoundedCornerShape(GameTokens.ControlRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = if (mobileUiSizing().portrait) androidx.compose.ui.graphics.Color.Transparent else if (tab == index) GameTokens.Accent else GameTokens.Raised,
                        contentColor = if (tab == index) GameTokens.Background else GameTokens.Text)) {
                        DetailIcon(listOf(R.drawable.detail_residents, R.drawable.detail_coins, R.drawable.detail_parts)[index], tab == index)
                        if (mobileUiSizing().portrait) Spacer(Modifier.width(GameTokens.Quarter))
                        Text(title, softWrap = false,
                        fontSize = if (LocalDensity.current.fontScale > 1f) GameTokens.Caption else GameTokens.Body) }
            }
        }
        Surface(Modifier.weight(1f), color = GameTokens.Surface,
            shape = RoundedCornerShape(GameTokens.CardRadius),
            border = partsBorder()) {
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
                Text(building.cityStatus(city), color = GameTokens.Muted)
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
                    border = partsBorder()) {
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
        border = partsBorder()) {
        Row(Modifier.fillMaxWidth().partsFrame().padding(GameTokens.Small), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
            DetailIcon(when (label) {
                "Mərtəbə" -> R.drawable.city_plan_layers
                "Sakin", "Sakin tutumu" -> R.drawable.detail_residents
                "Tikinti" -> R.drawable.detail_construction
                "Hissələr" -> R.drawable.detail_parts
                "Baza dəyəri", "Maksimum satış", "Satıcıya" -> R.drawable.detail_coins
                else -> R.drawable.detail_info
            })
            BoxWithConstraints(Modifier.weight(1f)) {
                // The portrait detail panel shares its width with the building preview.
                // Keep whole labels readable when two equal text columns cannot fit.
                if (maxWidth < GameTokens.Target * 4 * LocalDensity.current.fontScale) {
                    Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        Text(label, color = GameTokens.Muted)
                        Text(value, color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    }
                } else Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    Text(label, Modifier.weight(1f), color = GameTokens.Muted)
                    Text(value, Modifier.weight(1f), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Share the nodpi inventory illustration across cards, without retaining Activity/account state
 * or decoding it for a purple-only inventory. Other preview destinations keep their own scope. */
internal val LocalInventoryGarden = staticCompositionLocalOf<Pair<ResourceArt, () -> Unit>?> { null }

@Composable
internal fun InventoryGardenArt(required: Boolean, content: @Composable () -> Unit) {
    // A refresh briefly clears the rows. Keep an already requested illustration
    // until this screen leaves, so list/detail and row replacement do not decode again.
    var requested by remember { mutableStateOf(false) }
    SideEffect { if (required) requested = true }
    val garden = if (required || requested) rememberResourceArt(R.drawable.inventory_garden) else null
    CompositionLocalProvider(LocalInventoryGarden provides garden, content = content)
}

/** Server metadata stays real; approved art is a decorative owned-building presentation. */
@Composable
internal fun BuildingPreview(building: OwnedBuilding, modifier: Modifier, marketMaterials: Boolean = false, marketWall: CosmeticSprite? = null) {
    val preparation = LocalBuildingArt.current
    if (preparation.status != ArtPreparation.Ready) {
        Box(modifier) { ApprovedArtStatus(preparation.status, preparation.retry) }
        return
    }
    val sourceArt = rememberCosmeticArt() + rememberGameWindowArt()
    val art = if (marketMaterials && marketWall != null) sourceArt + (requireNotNull(building.parts["wall"]) to marketWall) else sourceArt
    val atlases = rememberGameAtlases()
    val purple = rememberPurpleBuildingArt()
    val purpleTemplate = usesPurpleTemplate(building.parts)
    // Purple already carries its approved garden in the atlas. Other templates load
    // their decorative island on IO without withholding the building's metadata.
    val garden = if (purpleTemplate) null else {
        val (resource, retry) = LocalInventoryGarden.current ?: rememberResourceArt(R.drawable.inventory_garden)
        val image = resource.images?.singleOrNull()
        if (image == null) {
            Box(modifier) { ApprovedArtStatus(if (resource.failed) ArtPreparation.Failed else ArtPreparation.Loading, retry) }
            return
        }
        remember(image) { CosmeticSprite(image, androidx.compose.ui.unit.IntOffset.Zero,
            androidx.compose.ui.unit.IntSize(image.width, image.height)) }
    }
    val centers = building.geometry?.geometry?.centers
    val missing = building.parts.values.any { it !in art }
    if (centers == null || missing) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(if (missing) "Bina qrafikası mövcud deyil" else "Bu binanın həndəsəsi mövcud deyil", color = GameTokens.Muted)
        }
        return
    }
    Canvas(modifier.semantics { contentDescription = "${building.floors} mərtəbəli bina" }) {
        if (purpleTemplate) purpleBuilding(purple, art, atlases, building.parts, centers, marketMaterials)
        else buildingPreview(atlases, art, building.parts, centers, requireNotNull(garden), marketMaterials, if (marketMaterials) purple["balcony"] else null)
    }
}

@Composable
private fun InventoryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    if(LocalMobileMenuShell.current) CommerceAction(label, primary = true, enabled = enabled, onClick = onClick)
    else HomeButton(label = label, primary = true, compact = true, enabled = enabled, onClick = onClick)
}


@Composable
private fun InventoryToolbar(largeFont: Boolean, onParts: () -> Unit, onDecorations: () -> Unit, onLand: () -> Unit, enabled: Boolean = true) {
    if (largeFont) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        InventoryToolbarEntry("Hissələr", null, Modifier, enabled, onParts)
        InventoryToolbarEntry("Dekor", null, Modifier, enabled, onDecorations)
        InventoryToolbarEntry("Ərazi", null, Modifier, enabled, onLand)
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        InventoryToolbarEntry("Hissələr", R.drawable.ui_inventory, Modifier.weight(1f), enabled, onParts)
        InventoryToolbarEntry("Dekor", R.drawable.ui_city, Modifier.weight(1f), enabled, onDecorations)
        InventoryToolbarEntry("Ərazi", R.drawable.ui_home, Modifier.weight(1f), enabled, onLand)
    }
}

@Composable
private fun InventoryToolbarEntry(label: String, icon: Int?, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = GameTokens.Target),
        shape = RoundedCornerShape(GameTokens.ControlRadius), color = GameTokens.Raised) {
        Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterHorizontally)) {
            if (icon != null) Image(painterResource(icon), null, Modifier.size(GameTokens.Inset))
            Text(label, fontSize = GameTokens.Caption, color = GameTokens.Text, fontWeight = FontWeight.SemiBold, softWrap = false)
        }
    }
}
