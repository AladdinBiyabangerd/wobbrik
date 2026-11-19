package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import appbackend.wobbrik.game.*

@Composable
internal fun MarketModeTabs(own: Boolean, onOwn: (Boolean) -> Unit) {
    CollectionTabs(if(own) "own" else "buy", { onOwn(it == "own") }, listOf(
        CommerceTab("Alış", "buy", MobileIcon.Cart), CommerceTab("Mənim elanlarım", "own", MobileIcon.Tag)), compactLargeType = true)
}

@Composable
internal fun MobileDecorationShop(ready: AuthenticatedRecoveryOwner.State.Ready, onBuy: (String) -> Unit, onBack: () -> Unit, onListings: () -> Unit, onRefresh: () -> Unit) {
    var selected by remember(ready) { mutableStateOf<CatalogItem?>(null) }
    val art = rememberOwnedDecorationArt()
    var category by remember { mutableStateOf("all") }
    val visible = remember(ready.catalog, category) { ready.catalog?.items.orEmpty().filter { it.method == "copy" && (category == "all" || it.id.startsWith("decor_${category}_")) } }
    val large = LocalDensity.current.fontScale > 1f
    BackHandler { if(selected != null) selected = null else onBack() }
    Column(Modifier.fillMaxSize().padding(horizontal = GameTokens.InventoryCollection.ScreenInset).padding(bottom = LocalCollectionDockClearance.current), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        MarketModeTabs(false) { if(it) onListings() }
        LazyVerticalGrid(GridCells.Fixed(if(large) 1 else 2), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            item(span = { GridItemSpan(maxLineSpan) }) { DecorFilters(category, collectionMarket = true) { category = it; selected = null } }
            item(span = { GridItemSpan(maxLineSpan) }) { MarketCollectionHeading("Dekor al", ready.snapshot.attempt?.active == true, onRefresh) }
            items(visible, key = { it.id }) { item ->
                MarketCollectionPanel {
                    Column(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        PurchaseArt(art[item.id], Modifier.fillMaxWidth().height(if(large) GameTokens.InventoryCollection.LargeTypeArtHeight else if(mobileUiSizing().compact) mobileUiSizing().cardArt else GameTokens.InventoryCollection.LargeTypeArtHeight), allowUpscale = true)
                        Text(item.name, Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body,
                            fontWeight = FontWeight.Bold, softWrap = false)
                        item.price?.let { price ->
                            MarketCardPurchase(price, "Al", MobileIcon.Cart, true) { selected = item }
                        }
                    }
                }
            }
            if(ready.catalog == null) item(span = { GridItemSpan(maxLineSpan) }) { Text("Kataloq yüklənmədi", color = GameTokens.Danger) }

        }
        selected?.let { item ->
            MobileShopPurchasePanel(CityPurchases.eligible(ready.catalog, ready.snapshot, item.id), { onBuy(item.id) }, { selected = null }) {
                Text("${item.name} · bir nüsxə", color = GameTokens.Text, fontFamily = GameTypeface, fontWeight = FontWeight.Bold)
                item.price?.let { CommercePrice(it) }
                if(!CityPurchases.eligible(ready.catalog, ready.snapshot, item.id)) Text("Alış hazırda mümkün deyil", color = GameTokens.Muted)
            }
        }
    }
}

@Composable
internal fun MobileLandShop(ready: AuthenticatedRecoveryOwner.State.Ready, onBuy: (String) -> Unit,
    onBack: () -> Unit, onListings: () -> Unit) {
    var selected by remember(ready) { mutableStateOf<LandOffer?>(null) }
    val illustration = rememberLandArt()
    BackHandler { if(selected != null) selected = null else onBack() }
    Column(Modifier.fillMaxSize().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        MarketModeTabs(false) { if(it) onListings() }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            item { PurchaseArt(illustration, Modifier.fillMaxWidth().height(if(LocalDensity.current.fontScale > 1f) GameTokens.Target * 2 else GameTokens.Target * 3)) }
            items(ready.catalog?.landOffers.orEmpty(), key = { it.id }) { offer ->
                val owned = offer.id in ready.snapshot.account?.land.orEmpty()
                val unlocked = ready.snapshot.xp >= offer.requiredXp
                PurchaseCard(selected == offer, { selected = offer }) {
                    Column(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        Text("Sahə ${offer.id.removePrefix("land_")}", color = GameTokens.Text, fontFamily = GameTypeface, fontWeight = FontWeight.Bold)
                        CommercePrice(offer.price)
                        Text("${offer.requiredXp} XP tələb olunur", color = GameTokens.Muted, fontSize = GameTokens.Caption)
                        Text(when { owned -> "✓ Alınıb"; unlocked -> "Açıq"; else -> "Kilidli" },
                            color = if(owned) GameTokens.Accent else GameTokens.Muted, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if(ready.catalog == null) item { Text("Kataloq yüklənmədi", color = GameTokens.Danger) }
            item { Text("Daimi genişlənmə · alış XP vermir", color = GameTokens.Muted, fontSize = GameTokens.Caption) }
        }
        selected?.let { offer ->
            MobileShopPurchasePanel(CityPurchases.eligible(ready.catalog, ready.snapshot, offer.id), { onBuy(offer.id) }, { selected = null }) {
                Text("Sahə ${offer.id.removePrefix("land_")}", color = GameTokens.Text, fontFamily = GameTypeface, fontWeight = FontWeight.Bold)
                CommercePrice(offer.price)
                Text("${offer.requiredXp} XP tələb olunur", color = GameTokens.Muted, fontSize = GameTokens.Caption)
            }
        }
    }
}

/** Large text uses the approved confirmation modal so chrome cannot squeeze out buy/cancel. */
@Composable
private fun MobileShopPurchasePanel(enabled: Boolean, onBuy: () -> Unit, onCancel: () -> Unit,
    content: @Composable ColumnScope.() -> Unit) {
    if(LocalDensity.current.fontScale > 1f) {
        MarketDialog("Alış təsdiqi", onCancel, compact = true,
            footer = { CommerceAction("Al", primary = true, enabled = enabled, icon = MobileIcon.Cart, onClick = onBuy) }, content = content)
    } else CityPurchasePanel(enabled, onBuy, onCancel, content)
}
