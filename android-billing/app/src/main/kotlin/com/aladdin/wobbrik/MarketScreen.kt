package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
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
    ready: AuthenticatedRecoveryOwner.State.Ready, onHome: () -> Unit, onInventory: () -> Unit, onCity: () -> Unit, initialBuilding: String? = null, onInitialConsumed: () -> Unit = {}, initialOwn: Boolean = false) {
    val repository = requireNotNull(configured.market)
    val mutations = remember(configured) { MarketMutations(configured.recovery) }
    var own by remember { mutableStateOf(initialBuilding != null || initialOwn) }
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
    var withdrawAfterRead by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf(false) }
    fun refresh() { withdrawAfterRead = false; cursor = null; next = null; listings = emptyList(); previews = emptyMap(); retry++ }
    fun back() { withdrawAfterRead = false; when { candidate != null -> candidate = null; detailId != null -> { detailId = null; detail = null }; picker -> { picker = false; candidate = null }; else -> onHome() } }
    BackHandler { back() }
    LaunchedEffect(ready, own, filter, cursor, detailId, picker, inventoryCursor, retry) {
        loading = true; failed = false; localError = false
        try {
            val session = requireNotNull(configured.sessions.current())
            fun current() { check(configured.recovery.state.value === ready && configured.sessions.current() == session) }
            current()
            when {
                detailId != null -> { detail = null; val value = repository.detail(session, requireNotNull(detailId)); current(); detail = value; if(withdrawAfterRead) { withdrawAfterRead = false; confirming = true } }
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
    if(LocalMobileMenuShell.current) CommerceDetailChrome(detailId != null || picker)
    val nativeMenu = LocalMobileMenuShell.current
    val sizing = mobileUiSizing()
    val separateBalance = LocalDensity.current.fontScale > 1f || ready.snapshot.coins.toString().length > 8
    if (nativeMenu && candidate != null) {
        val building = requireNotNull(candidate)
        MarketSalePage(building, ready.catalog, { candidate = null }) { price ->
            candidate = null
            if (!mutations.list(ready, building, price)) localError = true
        }
        return
    }
    Box(Modifier.fillMaxSize().then(if(LocalMobileMenuShell.current) Modifier else Modifier.background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))).then(if(LocalMobileMenuShell.current) Modifier else Modifier.safeDrawingPadding()).padding(GameTokens.Gap)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            if(LocalMobileMenuShell.current && (detailId != null || picker)) CommerceTitle(if(picker) "Yeni elan" else "Bina detalı") { back() }
            if(!LocalMobileMenuShell.current) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                MarketAction("‹", modifier = Modifier.width(GameTokens.Target).semantics { contentDescription = "Geri" }, onClick = { back() })
                Text("Bazar", Modifier.weight(1f), color = GameTokens.Text, fontSize = sizing.heading, fontWeight = FontWeight.Bold)
                if (!LocalMobileMenuShell.current && !separateBalance) {
                    Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
                    Text(ready.snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                }
            }
            if (!LocalMobileMenuShell.current && separateBalance) Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
                Text(ready.snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
            }
            if (detailId == null && !picker) {
                Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    MarketTab("Alış", !own, Modifier.weight(1f)) { own = false; refresh() }
                    MarketTab(if(nativeMenu) "Mənim elanlarım" else "Elanlarım", own, Modifier.weight(1f)) { own = true; if(nativeMenu) filter = MarketFilter(); refresh() }
                }
                if(LocalMobileMenuShell.current && own) MarketAction("+ Yeni elan", primary = true) { picker = true; inventoryCursor = null }

            }
            val nativeGrid = LocalMobileMenuShell.current && !own && LocalDensity.current.fontScale <= 1f
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if(detailId == null && !picker && (!nativeMenu || !own)) item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    MarketAction("Süzgəc", modifier = Modifier.widthIn(min = GameTokens.Target)) { filtersOpen = true }
                    MarketAction("Yenilə", modifier = Modifier.widthIn(min = GameTokens.Target), enabled = !loading) { refresh() }
                    if (filter != MarketFilter()) MarketAction("Hamısı", modifier = Modifier.widthIn(min = GameTokens.Target)) { filter = MarketFilter(); refresh() }
                }
                }
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
                    if (inventoryNext != null && !loading) item { MarketAction("Daha çox") { inventoryCursor = inventoryNext } }
                    if (inventory.none { !it.traded } && !loading && !failed) item { Text("Bu səhifədə satıla bilən bina yoxdur. Satış üçün ən azı 10 mərtəbəli öz binanı qur.", color = GameTokens.Muted) }
                } else {
                    if(nativeGrid) {
                        items(listings.chunked(2), key = { it.first().id }) { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                                row.forEach { listing -> Box(Modifier.weight(1f)) {
                                    val preview = previews[listing.id]
                                    if(preview != null) MarketBuildingCard(preview.building, ready.catalog, "${listing.price} sikkə", "Bax", !loading,
                                        grid = true) { detailId = listing.id }
                                    else MarketUnavailableCard(listing) { detailId = listing.id }
                                } }
                                if(row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    } else items(listings, key = { it.id }) { listing ->
                        val preview = previews[listing.id]
                        if (preview != null) MarketBuildingCard(preview.building, ready.catalog, "${listing.price} sikkə",
                            if(own && nativeMenu) "Elanı geri götür" else "Bax", !loading, listed = own && nativeMenu, purchase = nativeMenu && !own) { detailId = listing.id; withdrawAfterRead = own && nativeMenu }
                        else MarketUnavailableCard(listing) { detailId = listing.id }
                    }
                    if (next != null && !loading && !failed) item { MarketAction("Daha çox") { cursor = next } }
                    if (listings.isEmpty() && !loading && !failed) item { Text(if (own) "Hələ elanın yoxdur." else "Bu süzgəcdə elan yoxdur.", color = GameTokens.Muted) }
                }
                if (loading) item { CircularProgressIndicator(color = GameTokens.Accent, modifier = Modifier.size(GameTokens.Inset).semantics { contentDescription = "Bazar yüklənir" }) }
                if (failed || localError) item {
                    Text("Bazar yenilənmədi. Bağlantını yoxla və yenidən cəhd et.", color = GameTokens.Danger)
                    MarketAction("Yenidən cəhd et") { retry++ }
                }
            }
            val value = detail
            if (detailId != null && value != null && !failed) MarketAction(if (own) "Elanı geri götür" else "Al · ${value.listing.price}", primary = true,
                enabled = !loading && (own || value.listing.price <= ready.snapshot.coins)) { confirming = true }
            else if (own && !picker && detailId == null && !LocalMobileMenuShell.current) MarketAction("Bazara çıxar", icon = R.drawable.ui_market, primary = true) { picker = true; inventoryCursor = null }
            val marketHome = { detailId = null; picker = false; refresh() }
            if (!LocalMobileMenuShell.current) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                MarketNavigation("Şəhər", R.drawable.ui_city, false, Modifier.weight(1f), onCity)
                MarketNavigation("İnventar", R.drawable.ui_inventory, false, Modifier.weight(1f), onInventory)
                if (!separateBalance) MarketNavigation("Bazar", R.drawable.ui_market, true, Modifier.weight(1f), marketHome)
            }
            if (!LocalMobileMenuShell.current && separateBalance) MarketTab("Bazar", true, Modifier.fillMaxWidth(), marketHome)

        }
    }
    if (filtersOpen) MarketFilterDialog(filter, ready.catalog, { filtersOpen = false }) { filter = it; filtersOpen = false; refresh() }
    candidate?.let { building -> MarketSaleDialog(building, ready.catalog, { candidate = null }) { price ->
        candidate = null; if (!mutations.list(ready, building, price)) localError = true
    } }
    if (confirming && detail != null) {
        val value = requireNotNull(detail)
        val confirm = {
            confirming = false
            val accepted = if (own) mutations.withdraw(ready, value.listing) else mutations.buy(ready, value)
            if (!accepted) localError = true
        }
        if(LocalMobileMenuShell.current) MarketDialog(if(own) "Elan geri götürülsün?" else "Alış təsdiqi", { confirming = false },
            compact = true, bottomSheet = !own, compactHeight = GameTokens.Market.SaleDialogHeight + GameTokens.Target * 2,
            footer = { CommerceAction(if(own) "Elanı geri götür" else "Al", primary = true,
                icon = if(own) MobileIcon.Refresh else MobileIcon.Cart, onClick = confirm) }) {
            MarketConfirmationSummary(value.building, ready.catalog, value.listing.price, ready.snapshot.coins)
            Text(if(own) "Bina şəhərində və inventarında qalacaq." else "Bina inventarına keçəcək; təkrar satıla bilməz.", color = GameTokens.Muted)
        } else AlertDialog(onDismissRequest = { confirming = false }, containerColor = GameTokens.Surface,
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
internal fun MarketBuildingCard(building: OwnedBuilding, catalog: RecoveryCatalog?, price: String, action: String, enabled: Boolean, grid: Boolean = false, listed: Boolean = false, purchase: Boolean = grid, onClick: () -> Unit) {
    if(!LocalMobileMenuShell.current) {
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
        return
    }
    if (listed) {
        MarketOwnListingCard(building, catalog, price, enabled, onClick)
        return
    }
    val largeFont = LocalDensity.current.fontScale > 1f
    val sizing = mobileUiSizing()
    MarketBuildingRelief(purchase) {
        val details: @Composable ColumnScope.() -> Unit = {
            Text(buildingName(building, catalog), Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Text,
                fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
            Text("${building.floors} mərtəbə", Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Muted,
                fontSize = GameTokens.Caption, softWrap = false)
            if(listed) CommerceStatus("✓ Satışdadır", filled = true)
            val amount = price.removeSuffix(" sikkə").toLongOrNull()
            if(grid && amount != null) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                CommercePrice(amount, Modifier.weight(1f))
                CommerceAction(action, Modifier.width(GameTokens.Target + GameTokens.Inset), primary = true, enabled = enabled, icon = MobileIcon.Play, compact = true, onClick = onClick)
            } else {
                if(amount != null) CommercePrice(amount)
                else Text(price, Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Text, fontFamily = GameTypeface,
                    fontWeight = FontWeight.Bold, softWrap = false)
                MarketAction(action, primary = !listed, compact = true, enabled = enabled, onClick = onClick)
            }
        }
        if(largeFont || grid) Column(Modifier.padding(GameTokens.Small),
            verticalArrangement = Arrangement.spacedBy(if(grid) GameTokens.Quarter else GameTokens.Small)) {
            BuildingPreview(building, Modifier.fillMaxWidth().height(if(grid) sizing.cardArt else GameTokens.Target))
            details()
        } else Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            BuildingPreview(building, Modifier.width(sizing.listPreview).height(sizing.listPreview))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = details)
        }
    }
}

/** Active own listings keep price/status together, with a full-width withdrawal action. */
@Composable
private fun MarketOwnListingCard(building: OwnedBuilding, catalog: RecoveryCatalog?, price: String,
    enabled: Boolean, onClick: () -> Unit) {
    val largeFont = LocalDensity.current.fontScale > 1f
    val sizing = mobileUiSizing()
    MarketBuildingRelief(purchase = true) {
        val details: @Composable ColumnScope.() -> Unit = {
            Text(buildingName(building, catalog), Modifier.horizontalScroll(rememberScrollState()),
                color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body,
                fontWeight = FontWeight.Bold, softWrap = false)
            Text("${building.floors} mərtəbə", Modifier.horizontalScroll(rememberScrollState()),
                color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Label, softWrap = false)
            val amount = price.removeSuffix(" sikkə").toLongOrNull()
            val money: @Composable () -> Unit = {
                if(amount != null) CommercePrice(amount)
                else Text(price, Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Text,
                    fontFamily = GameTypeface, fontWeight = FontWeight.Bold, softWrap = false)
            }
            if(largeFont) {
                money()
                CommerceStatus("✓ Satışdadır", filled = true)
            } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Box(Modifier.weight(1f)) { money() }
                CommerceStatus("✓ Satışdadır", filled = true)
            }
            CommerceAction("Elanı geri götür", enabled = enabled, icon = MobileIcon.Refresh, compact = true, onClick = onClick)
        }
        if(largeFont) Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            BuildingPreview(building, Modifier.fillMaxWidth().height(GameTokens.Target * 2))
            details()
        } else Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            BuildingPreview(building, Modifier.width(sizing.listPreview - GameTokens.Inset).height(sizing.listPreview + GameTokens.Inset))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter), content = details)
        }
    }
}

/** Purchase cards have a fine rim and light falling from the upper left, as in 02-market. */
@Composable
private fun MarketBuildingRelief(purchase: Boolean, content: @Composable BoxScope.() -> Unit) {
    if (!purchase) {
        MenuRelief(Modifier.fillMaxWidth(), content = content)
        return
    }
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Box(Modifier.fillMaxWidth()
        .shadow(GameTokens.Quarter, shape, ambientColor = GameTokens.Background, spotColor = GameTokens.Background)
        .clip(shape)
        .background(Brush.verticalGradient(listOf(GameTokens.MobileHome.PanelTop, GameTokens.MobileHome.PanelBottom)))
        .drawWithCache {
            val light = Brush.radialGradient(
                listOf(GameTokens.MobileHome.Rim.copy(alpha = 1f - GameTokens.DimOpacity),
                    GameTokens.MobileHome.Rim.copy(alpha = 0f)),
                center = Offset.Zero, radius = size.maxDimension)
            onDrawBehind { drawRect(light) }
        }
        .border(GameTokens.Hairline, Brush.verticalGradient(listOf(
            GameTokens.MobileHome.Rim, GameTokens.MobileHome.PanelTop)), shape), content = content)
}

@Composable
private fun MarketUnavailableCard(listing: MarketListing, onClick: () -> Unit) {
    MenuRelief(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            CommercePrice(listing.price)
            Text(when(listing.status) { "sold" -> "Satılıb"; "withdrawn" -> "Geri götürülüb"; else -> "Bina görünüşünü yenilə" }, color = GameTokens.Muted)
            if(listing.status == "active") MarketAction("Bax", onClick = onClick)
        }
    }
}

/** Compact native summary shared by the buy and withdrawal confirmation. */
@Composable
private fun MarketConfirmationSummary(building: OwnedBuilding, catalog: RecoveryCatalog?, price: Long, balance: Long) {
    val largeFont = LocalDensity.current.fontScale > 1f
    MarketBuildingSummary(building, catalog)
    MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius)) {
        Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            listOf(Triple("Qiymət", MobileIcon.Tag, price), Triple("Balans", MobileIcon.Layers, balance)).forEach { (label, icon, amount) ->
                val heading: @Composable () -> Unit = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        MobileMenuIcon(icon, Modifier.size(GameTokens.Inset))
                        Text(label, color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body)
                    }
                }
                if(largeFont) Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    heading()
                    CommercePrice(amount, Modifier.fillMaxWidth())
                } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    Box(Modifier.weight(1f)) { heading() }
                    CommercePrice(amount, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MarketBuildingSummary(building: OwnedBuilding, catalog: RecoveryCatalog?) {
    MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius)) {
        Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            BuildingPreview(building, Modifier.width(GameTokens.Target + GameTokens.Inset).height(GameTokens.Target * 2))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Text(buildingName(building, catalog), Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Text,
                    fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
                Text("${building.floors} mərtəbə", Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Muted,
                    fontFamily = GameTypeface, fontSize = GameTokens.Label, softWrap = false)
            }
        }
    }
}

/** Native route content: shell header and navbar remain visible while only the body scrolls. */
@Composable
private fun MarketSalePage(building: OwnedBuilding, catalog: RecoveryCatalog?, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var price by remember(building.id) { mutableStateOf("") }
    val value = price.toLongOrNull()?.takeIf { it in 1..building.maxPrice }
    val fee = value?.let(MarketMutations::fee)
    Column(Modifier.fillMaxSize().imePadding().padding(GameTokens.Gap),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        CommerceTitle("Yeni elan", onDismiss)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Text("Bazar · Mənim elanlarım", color = GameTokens.Muted, fontSize = GameTokens.Label)
            MarketBuildingSummary(building, catalog)
            MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    Text("Qiymət", color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                    OutlinedTextField(price, { text ->
                        if (text.length <= 19 && text.all { it in '0'..'9' }) price = text
                    }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Qiymət" }, singleLine = true,
                        leadingIcon = { MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Inset)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(GameTokens.ControlRadius),
                        isError = price.isNotEmpty() && value == null,
                        placeholder = { Text("0", color = GameTokens.Muted) },
                        textStyle = LocalTextStyle.current.copy(color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = GameTokens.Text,
                            unfocusedTextColor = GameTokens.Text, cursorColor = GameTokens.Accent,
                            focusedBorderColor = GameTokens.Accent, unfocusedBorderColor = GameTokens.Muted,
                            errorBorderColor = GameTokens.Danger, errorTextColor = GameTokens.Text))
                    Text("Maksimum ${building.maxPrice} sikkə", color = if(price.isNotEmpty() && value == null) GameTokens.Danger else GameTokens.Muted,
                        fontSize = GameTokens.Label)
                }
            }
            MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    MarketSaleAmount("Komissiya (5%)", MobileIcon.Tag, fee, negative = true)
                    HorizontalDivider(color = GameTokens.Raised)
                    MarketSaleAmount("Alacağın", MobileIcon.Layers, value?.let { it - requireNotNull(fee) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Text("ⓘ", color = GameTokens.Muted, fontSize = GameTokens.Title)
                Text("Elan yaradıldıqdan sonra Bazar bölməsində alıcılara görünəcək.", Modifier.weight(1f),
                    color = GameTokens.Text, fontSize = GameTokens.Label)
            }
            Text("Eyni anda 5 aktiv elan. Bina şəhərində qalır.", color = GameTokens.Muted, fontSize = GameTokens.Label)
        }
        CommerceAction("Elanı dərc et", primary = true, enabled = value != null, icon = MobileIcon.Tag) {
            onConfirm(requireNotNull(value))
        }
        CommerceAction("Ləğv et", onClick = onDismiss)
    }
}

@Composable
private fun MarketSaleAmount(label: String, icon: MobileIcon, amount: Long?, negative: Boolean = false) {
    val heading: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            MobileMenuIcon(icon, Modifier.size(GameTokens.Inset))
            Text(label, color = GameTokens.Text, fontSize = GameTokens.Body,
                fontWeight = if(negative) FontWeight.Normal else FontWeight.Bold)
        }
    }
    val money: @Composable () -> Unit = {
        Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            Text(if(amount == null) "—" else (if(negative) "−" else "") + amount,
                color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
            MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Inset))
        }
    }
    if(LocalDensity.current.fontScale > 1f) Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        heading(); money()
    } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Box(Modifier.weight(1f)) { heading() }
        money()
    }
}

@Composable
private fun MarketSaleDialog(building: OwnedBuilding, catalog: RecoveryCatalog?, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var price by remember(building.id) { mutableStateOf("") }
    val value = price.toLongOrNull()?.takeIf { it in 1..building.maxPrice }
    MarketDialog(if(LocalMobileMenuShell.current) "Yeni elan" else "Bazara çıxar", onDismiss, compact = true, footer = { MarketAction("Elanı dərc et", primary = true, enabled = value != null) { onConfirm(requireNotNull(value)) } }) {
        if(LocalMobileMenuShell.current) MarketBuildingSummary(building, catalog)
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
    MarketDialog("Bazar süzgəci", onDismiss, footer = { MarketAction("Tətbiq et", primary = true, enabled = filter != null) { onApply(requireNotNull(filter)) } }) {
        MarketNumber("Minimum mərtəbə", minFloors) { minFloors = it }; MarketNumber("Maksimum mərtəbə", maxFloors) { maxFloors = it }
        MarketNumber("Minimum qiymət", minPrice) { minPrice = it }; MarketNumber("Maksimum qiymət", maxPrice) { maxPrice = it }
        Text("Görünüş", color = GameTokens.Text, fontWeight = FontWeight.Bold)
        MarketAction("Hamısı", primary = part == null) { part = null }
        catalog?.items?.filter { it.category in FinishCosmetics.categories }?.forEach { item ->
            MarketAction(item.name, primary = part == item.id, compact = true) { part = item.id }
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
internal fun MarketDialog(title: String, onDismiss: () -> Unit, compact: Boolean = false,
    compactHeight: Dp = GameTokens.Market.SaleDialogHeight, bottomSheet: Boolean = false,
    footer: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val parentDensity = LocalDensity.current
    val configuration = LocalConfiguration.current
    val wrappedDock = configuration.screenWidthDp.dp - GameTokens.Quarter * 2 <
        GameTokens.Target * 6 + GameTokens.MobileHome.PlayDiameter
    val dockHeight = (if(wrappedDock) GameTokens.Target * 2 else GameTokens.MobileHome.NavbarHeight) + GameTokens.Small * 2
    val availableHeight = LocalConfiguration.current.screenHeightDp.dp -
        if(bottomSheet) dockHeight + GameTokens.Gap * 2 else GameTokens.Target * 2
    val height = if (compact) minOf(availableHeight, compactHeight * LocalDensity.current.fontScale) else availableHeight
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CompositionLocalProvider(LocalDensity provides parentDensity) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        val dockSpace = with(parentDensity) { dockHeight.roundToPx() }
        SideEffect {
            if (bottomSheet) {
                window?.setGravity(android.view.Gravity.BOTTOM)
                window?.setDimAmount(GameTokens.DimOpacity)
                window?.attributes = window?.attributes?.apply { y = dockSpace }
            }
        }
        MarketDialogPanel(Modifier.padding(horizontal = if(bottomSheet) GameTokens.Small else GameTokens.Gap)
            .padding(bottom = if(bottomSheet) GameTokens.Small else GameTokens.Gap,
                top = if(bottomSheet) GameTokens.Small else GameTokens.Gap)
            .widthIn(max = GameTokens.PanelWidth).fillMaxWidth().height(height), bottomSheet) {
            Column(Modifier.padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if(bottomSheet) Box(Modifier.align(Alignment.CenterHorizontally)
                    .size(width = GameTokens.Inset + GameTokens.Gap, height = GameTokens.Quarter)
                    .background(GameTokens.Muted, RoundedCornerShape(GameTokens.ControlRadius)))
                Text(title, modifier = if(bottomSheet) Modifier.align(Alignment.CenterHorizontally) else Modifier,
                    color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = content)
                footer()
                MarketAction("Ləğv et", onClick = onDismiss)
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
    if(LocalMobileMenuShell.current) {
        CommerceAction(label, modifier.semantics { this.selected = selected }, primary = selected, icon = if(label == "Alış") MobileIcon.Cart else MobileIcon.Tag, compact = true, onClick = onClick)
    } else {
        Surface(onClick = onClick, modifier = modifier.heightIn(min = GameTokens.Target), color = if(selected) GameTokens.Accent else GameTokens.Raised,
            shape = RoundedCornerShape(GameTokens.ControlRadius)) {
            Text(label, Modifier.padding(GameTokens.Small), color = if(selected) GameTokens.Background else GameTokens.Text)
        }
    }
}

@Composable
private fun MarketAction(label: String, modifier: Modifier = Modifier.fillMaxWidth(), primary: Boolean = false,
    compact: Boolean = false, enabled: Boolean = true, icon: Int? = null, onClick: () -> Unit) {
    if(LocalMobileMenuShell.current) CommerceAction(label, modifier, primary, enabled, icon = when { label == "Bax" -> MobileIcon.Play; label == "Elanı geri götür" -> MobileIcon.Refresh; label.startsWith("Al") -> MobileIcon.Cart; else -> null }, onClick = onClick)
    else HomeButton(label, modifier = modifier, primary = primary, compact = compact, enabled = enabled, icon = icon, onClick = onClick)
}

@Composable
private fun MarketDialogPanel(modifier: Modifier, bottomSheet: Boolean = false, content: @Composable () -> Unit) {
    if(bottomSheet) {
        val shape = RoundedCornerShape(GameTokens.CardRadius)
        Box(modifier.shadow(GameTokens.Small, shape).clip(shape)
            .background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.MobileHome.PanelTop, GameTokens.MobileHome.PanelBottom)))
            .border(GameTokens.Hairline, Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight, GameTokens.MobileHome.Rim)), shape)) { content() }
    } else if(LocalMobileMenuShell.current) MenuRelief(modifier) { content() }
    else Surface(modifier, color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius), content = content)
}
