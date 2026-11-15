package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Mounted under an exact Ready key. No account-global cursor, preview cache or local grants. */
@Composable
internal fun MarketScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onHome: () -> Unit, onInventory: () -> Unit, onCity: () -> Unit, initialBuilding: String? = null, onInitialConsumed: () -> Unit = {}) {
    val repository = requireNotNull(configured.market)
    val mutations = remember(configured) { MarketMutations(configured.recovery) }
    var own by remember { mutableStateOf(initialBuilding != null) }
    var filter by remember { mutableStateOf(MarketFilter()) }
    var cursor by remember { mutableStateOf<String?>(null) }
    var next by remember { mutableStateOf<String?>(null) }
    var listings by remember { mutableStateOf(emptyList<MarketListing>()) }
    var previews by remember { mutableStateOf(emptyMap<String, MarketDetail>()) }
    var detailId by remember { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf<MarketDetail?>(null) }
    var candidate by remember { mutableStateOf<OwnedBuilding?>(null) }
    var picker by remember { mutableStateOf(initialBuilding != null) }
    var inventoryCursor by remember { mutableStateOf<String?>(null) }
    var inventoryNext by remember { mutableStateOf<String?>(null) }
    var inventory by remember { mutableStateOf(emptyList<OwnedBuilding>()) }
    var initialPending by remember { mutableStateOf(initialBuilding != null) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var filtersOpen by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf(false) }
    fun refresh() { cursor = null; next = null; listings = emptyList(); previews = emptyMap(); retry++ }
    fun back() { when { detailId != null -> { detailId = null; detail = null }; picker -> { picker = false; candidate = null }; else -> onHome() } }
    BackHandler { back() }
    LaunchedEffect(ready, own, filter, cursor, detailId, picker, inventoryCursor, retry) {
        loading = true; failed = false; localError = false
        try {
            val session = requireNotNull(configured.sessions.current())
            fun current() { check(configured.recovery.state.value === ready && configured.sessions.current() == session) }
            current()
            when {
                detailId != null -> { detail = null; val value = repository.detail(session, requireNotNull(detailId)); current(); detail = value }
                picker -> {
                    if (initialPending && initialBuilding != null) {
                        val value = configured.inventory.building(session, initialBuilding); current()
                        candidate = value.takeUnless { it.traded }; initialPending = false; onInitialConsumed()
                    }
                    val value = configured.inventory.page(session, inventoryCursor); current()
                    inventory = if (inventoryCursor == null) value.buildings else (inventory + value.buildings).distinctBy { it.id }
                    inventoryNext = value.next
                }
                else -> {
                    val value = repository.page(session, own, filter, cursor)
                    val gate = Semaphore(4)
                    val art = coroutineScope { value.listings.filter { it.status == "active" }.map { listing -> async {
                        gate.withPermit {
                            try { listing.id to repository.detail(session, listing.id) }
                            catch (cancelled: CancellationException) { throw cancelled }
                            catch (_: Exception) { null }
                        }
                    } }.awaitAll().filterNotNull().toMap() }
                    current()
                    listings = if (cursor == null) value.listings else (listings + value.listings).distinctBy { it.id }
                    previews = if (cursor == null) art else previews + art
                    next = value.next
                }
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    val sizing = mobileUiSizing()
    val separateBalance = LocalDensity.current.fontScale > 1f || ready.snapshot.coins.toString().length > 8
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background))).safeDrawingPadding().padding(GameTokens.Gap)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                HomeButton("‹", modifier = Modifier.width(GameTokens.Target).semantics { contentDescription = "Geri" }, onClick = { back() })
                Text("Bazar", Modifier.weight(1f), color = GameTokens.Text, fontSize = sizing.heading, fontWeight = FontWeight.Bold)
                if (!separateBalance) {
                    Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
                    Text(ready.snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                }
            }
            if (separateBalance) Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
                Text(ready.snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
            }
            if (detailId == null && !picker) {
                Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    MarketTab("Alış", !own, Modifier.weight(1f)) { own = false; refresh() }
                    MarketTab("Elanlarım", own, Modifier.weight(1f)) { own = true; refresh() }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    HomeButton("Süzgəc", modifier = Modifier.widthIn(min = GameTokens.Target)) { filtersOpen = true }
                    HomeButton("Yenilə", modifier = Modifier.widthIn(min = GameTokens.Target), enabled = !loading) { refresh() }
                    if (filter != MarketFilter()) HomeButton("Hamısı", modifier = Modifier.widthIn(min = GameTokens.Target)) { filter = MarketFilter(); refresh() }
                }
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if (detailId != null) {
                    detail?.let { value -> item { Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        BuildingPreview(value.building, Modifier.fillMaxWidth().height(sizing.detailArt))
                        Text(buildingName(value.building, ready.catalog), color = GameTokens.Text, fontSize = sizing.heading, fontWeight = FontWeight.Bold)
                        Text("${value.building.floors} mərtəbə · ${value.building.residents} sakin", color = GameTokens.Muted)
                        Text("${value.listing.price} sikkə", color = GameTokens.Text, fontSize = sizing.heading, fontWeight = FontWeight.Bold)
                        if (own) Text("Komissiya ${value.listing.fee} · Satıcıya ${value.listing.price - value.listing.fee}", color = GameTokens.Muted)
                    } } }
                } else if (picker) {
                    item { Text("Satılacaq binanı seç", color = GameTokens.Text, fontSize = sizing.heading) }
                    items(inventory.filter { !it.traded }, key = { it.id }) { building ->
                        MarketBuildingCard(building, ready.catalog, "Maksimum ${building.maxPrice}", "Seç", !loading) { candidate = building }
                    }
                    if (inventoryNext != null && !loading) item { HomeButton("Daha çox") { inventoryCursor = inventoryNext } }
                    if (inventory.none { !it.traded } && !loading && !failed) item { Text("Bu səhifədə satıla bilən bina yoxdur. Satış üçün ən azı 10 mərtəbəli öz binanı qur.", color = GameTokens.Muted) }
                } else {
                    items(listings, key = { it.id }) { listing ->
                        val preview = previews[listing.id]
                        if (preview != null) MarketBuildingCard(preview.building, ready.catalog, "${listing.price} sikkə", "Bax", !loading) { detailId = listing.id }
                        else Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius), border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                            Column(Modifier.padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                                Text("${listing.price} sikkə", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                                Text(when (listing.status) { "sold" -> "Satılıb"; "withdrawn" -> "Geri götürülüb"; else -> "Bina görünüşünü yenilə" }, color = GameTokens.Muted)
                                if (listing.status == "active") HomeButton("Bax") { detailId = listing.id }
                            }
                        }
                    }
                    if (next != null && !loading && !failed) item { HomeButton("Daha çox") { cursor = next } }
                    if (listings.isEmpty() && !loading && !failed) item { Text(if (own) "Hələ elanın yoxdur." else "Bu süzgəcdə elan yoxdur.", color = GameTokens.Muted) }
                }
                if (loading) item { CircularProgressIndicator(color = GameTokens.Accent, modifier = Modifier.size(GameTokens.Inset)) }
                if (failed || localError) item {
                    Text("Bazar yenilənmədi. Bağlantını yoxla və yenidən cəhd et.", color = GameTokens.Danger)
                    HomeButton("Yenidən cəhd et") { retry++ }
                }
            }
            val value = detail
            if (detailId != null && value != null && !failed) HomeButton(if (own) "Elanı geri götür" else "Al · ${value.listing.price}", primary = true,
                enabled = !loading && (own || value.listing.price <= ready.snapshot.coins)) { confirming = true }
            else if (own && !picker && detailId == null) HomeButton("Bazara çıxar", icon = R.drawable.ui_market, primary = true) { picker = true; inventoryCursor = null }
            val marketHome = { detailId = null; picker = false; refresh() }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                MarketNavigation("Şəhər", R.drawable.ui_city, false, Modifier.weight(1f), onCity)
                MarketNavigation("İnventar", R.drawable.ui_inventory, false, Modifier.weight(1f), onInventory)
                if (!separateBalance) MarketNavigation("Bazar", R.drawable.ui_market, true, Modifier.weight(1f), marketHome)
            }
            if (separateBalance) MarketTab("Bazar", true, Modifier.fillMaxWidth(), marketHome)

        }
    }
    if (filtersOpen) MarketFilterDialog(filter, ready.catalog, { filtersOpen = false }) { filter = it; filtersOpen = false; refresh() }
    candidate?.let { building -> MarketSaleDialog(building, { candidate = null }) { price ->
        candidate = null; if (!mutations.list(ready, building, price)) localError = true
    } }
    if (confirming && detail != null) {
        val value = requireNotNull(detail)
        AlertDialog(onDismissRequest = { confirming = false }, containerColor = GameTokens.Surface,
            title = { Text(if (own) "Elan geri götürülsün?" else "Bina alınsın?", color = GameTokens.Text) },
            text = { Text(if (own) "Bina şəhərində və inventarında qalacaq." else "${value.listing.price} sikkə xərclənəcək. Bina inventarına keçəcək; təkrar satıla bilməz.", color = GameTokens.Muted) },
            confirmButton = { TextButton(onClick = {
                confirming = false
                val accepted = if (own) mutations.withdraw(ready, value.listing) else mutations.buy(ready, value)
                if (!accepted) localError = true
            }) { Text("Təsdiq et", color = GameTokens.Accent) } },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text("Ləğv et", color = GameTokens.Text) } })
    }
}

@Composable
private fun MarketBuildingCard(building: OwnedBuilding, catalog: RecoveryCatalog?, price: String, action: String, enabled: Boolean, onClick: () -> Unit) {
    val largeFont = LocalDensity.current.fontScale > 1f
    val sizing = mobileUiSizing()
    Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius), border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Row(Modifier.background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface))).padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            BuildingPreview(building, Modifier.width(if (largeFont) GameTokens.Target else sizing.listPreview).height(sizing.listPreview))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Text(buildingName(building, catalog), color = GameTokens.Text, fontWeight = FontWeight.Bold, fontSize = GameTokens.Body)
                Text("${building.floors} mərtəbə", color = GameTokens.Muted, fontSize = GameTokens.Caption)
                HorizontalDivider(color = GameTokens.Raised)
                Text(price, color = GameTokens.Text, fontWeight = FontWeight.Bold)
                HomeButton(action, primary = true, compact = true, enabled = enabled, onClick = onClick)
            }
        }
    }
}

@Composable
private fun MarketSaleDialog(building: OwnedBuilding, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var price by remember(building.id) { mutableStateOf("") }
    val value = price.toLongOrNull()?.takeIf { it in 1..building.maxPrice }
    MarketDialog("Bazara çıxar", onDismiss, compact = true, footer = { HomeButton("Elanı dərc et", primary = true, enabled = value != null) { onConfirm(requireNotNull(value)) } }) {
        Text("Maksimum ${building.maxPrice} sikkə. Eyni anda 5 aktiv elan. Bina şəhərində qalır.", color = GameTokens.Muted)
        MarketNumber("Qiymət", price) { price = it }
        if (value != null) Text("Komissiya ${MarketMutations.fee(value)} · Satıcıya ${value - MarketMutations.fee(value)}", color = GameTokens.Text)
    }
}

@Composable
private fun MarketFilterDialog(initial: MarketFilter, catalog: RecoveryCatalog?, onDismiss: () -> Unit, onApply: (MarketFilter) -> Unit) {
    var minFloors by remember { mutableStateOf(initial.minFloors.takeIf { it > 0 }?.toString().orEmpty()) }
    var maxFloors by remember { mutableStateOf(initial.maxFloors.takeIf { it > 0 }?.toString().orEmpty()) }
    var minPrice by remember { mutableStateOf(initial.minPrice.takeIf { it > 0 }?.toString().orEmpty()) }
    var maxPrice by remember { mutableStateOf(initial.maxPrice.takeIf { it > 0 }?.toString().orEmpty()) }
    var part by remember { mutableStateOf(initial.partId) }
    fun number(value: String) = if (value.isEmpty()) 0L else value.toLongOrNull()
    val filter = runCatching { MarketFilter(requireNotNull(number(minFloors)), requireNotNull(number(maxFloors)), requireNotNull(number(minPrice)), requireNotNull(number(maxPrice)), part) }.getOrNull()
    MarketDialog("Bazar süzgəci", onDismiss, footer = { HomeButton("Tətbiq et", primary = true, enabled = filter != null) { onApply(requireNotNull(filter)) } }) {
        MarketNumber("Minimum mərtəbə", minFloors) { minFloors = it }; MarketNumber("Maksimum mərtəbə", maxFloors) { maxFloors = it }
        MarketNumber("Minimum qiymət", minPrice) { minPrice = it }; MarketNumber("Maksimum qiymət", maxPrice) { maxPrice = it }
        Text("Görünüş", color = GameTokens.Text, fontWeight = FontWeight.Bold)
        HomeButton("Hamısı", primary = part == null) { part = null }
        catalog?.items?.filter { it.category in FinishCosmetics.categories }?.forEach { item ->
            HomeButton(item.name, primary = part == item.id, compact = true) { part = item.id }
        }
    }
}

@Composable
private fun MarketNumber(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value, { text -> if (text.length <= 19 && text.all { it in '0'..'9' }) onChange(text) }, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = GameTokens.Text, unfocusedTextColor = GameTokens.Text,
            focusedLabelColor = GameTokens.Accent, unfocusedLabelColor = GameTokens.Muted, focusedBorderColor = GameTokens.Accent, unfocusedBorderColor = GameTokens.Muted))
}

@Composable
private fun MarketDialog(title: String, onDismiss: () -> Unit, compact: Boolean = false, footer: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val parentDensity = LocalDensity.current
    val availableHeight = LocalConfiguration.current.screenHeightDp.dp - GameTokens.Target * 2
    val height = if (compact) minOf(availableHeight, GameTokens.Market.SaleDialogHeight * LocalDensity.current.fontScale) else availableHeight
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CompositionLocalProvider(LocalDensity provides parentDensity) {
        Surface(modifier = Modifier.padding(GameTokens.Gap).widthIn(max = GameTokens.PanelWidth).fillMaxWidth().height(height), color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius)) {
            Column(Modifier.padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Text(title, color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = content)
                footer()
                HomeButton("Ləğv et", onClick = onDismiss)
            }
        }
        }
    }
}

@Composable
private fun MarketNavigation(label: String, icon: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = GameTokens.Target), color = GameTokens.Raised,
        shape = RoundedCornerShape(GameTokens.ControlRadius), border = BorderStroke(GameTokens.Hairline, if (selected) GameTokens.Accent else GameTokens.Raised)) {
        Column(Modifier.padding(GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            if (LocalDensity.current.fontScale <= 1f) Image(painterResource(icon), null, Modifier.size(GameTokens.Inset))
            Text(label, color = GameTokens.Text, fontSize = GameTokens.Caption, fontWeight = FontWeight.Bold, softWrap = false)
        }
    }
}

@Composable
private fun MarketTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val largeFont = LocalDensity.current.fontScale > 1f
    Surface(onClick = onClick, modifier = modifier.heightIn(min = GameTokens.Target),
        color = if (selected) GameTokens.Accent else GameTokens.Raised, shape = RoundedCornerShape(GameTokens.CardRadius),
        border = BorderStroke(GameTokens.Hairline, if (selected) GameTokens.Text else GameTokens.Raised)) {
        Box(Modifier.padding(GameTokens.Small), contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) GameTokens.Background else GameTokens.Text,
                fontSize = if (largeFont) GameTokens.Caption else GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
        }
    }
}
