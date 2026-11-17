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
import appbackend.wobbrik.game.*

@Composable
internal fun MarketModeTabs(own: Boolean, onOwn: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        CommerceAction("Alış", Modifier.weight(1f), primary = !own, icon = MobileIcon.Cart, compact = true) { onOwn(false) }
        CommerceAction("Mənim elanlarım", Modifier.weight(1f), primary = own, icon = MobileIcon.Tag, compact = true) { onOwn(true) }
    }
}

@Composable
internal fun MobileDecorationShop(ready: AuthenticatedRecoveryOwner.State.Ready, copies: List<DecorationCopy>?, failed: Boolean,
    onRetry: () -> Unit, onBuy: (String) -> Unit, onBack: () -> Unit, onListings: () -> Unit) {
    var selected by remember(ready) { mutableStateOf<CatalogItem?>(null) }
    val art = rememberDecorationArt()
    val large = LocalDensity.current.fontScale > 1f
    BackHandler { if(selected != null) selected = null else onBack() }
    Column(Modifier.fillMaxSize().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        MarketModeTabs(false) { if(it) onListings() }
        LazyVerticalGrid(GridCells.Fixed(if(large) 1 else 2), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            items(ready.catalog?.items.orEmpty().filter { it.method == "copy" }, key = { it.id }) { item ->
                PurchaseCard(selected?.id == item.id, { selected = item },
                    shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                    Column(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        PurchaseArt(art[item.id], Modifier.fillMaxWidth().height(mobileUiSizing().cardArt))
                        Text(item.name, Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body,
                            fontWeight = FontWeight.Bold, softWrap = false)
                        copies?.let { owned ->
                            val count = owned.count { it.itemId == item.id }
                            if(count > 0) Surface(color = GameTokens.Success, shape = RoundedCornerShape(GameTokens.Target)) {
                                Text("✓ Sahibsən", Modifier.padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter),
                                    color = GameTokens.Background, fontFamily = GameTypeface, fontSize = GameTokens.Caption,
                                    fontWeight = FontWeight.Bold)
                            }
                            Text("Nüsxə: $count", color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Caption)
                        }
                        if (large) {
                            item.price?.let { CommercePrice(it, Modifier.fillMaxWidth()) }
                            CommerceAction("Al", primary = true, icon = MobileIcon.Cart) { selected = item }
                        } else {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                                Box(Modifier.weight(1f)) { item.price?.let { CommercePrice(it, Modifier.fillMaxWidth()) } }
                                CommerceAction("Al", Modifier.width(GameTokens.Target + GameTokens.Inset),
                                    primary = true, icon = MobileIcon.Cart, compact = true) { selected = item }
                            }
                        }
                    }
                }
            }
            if(ready.catalog == null) item(span = { GridItemSpan(maxLineSpan) }) { Text("Kataloq yüklənmədi", color = GameTokens.Danger) }
            if(copies == null && !failed) item(span = { GridItemSpan(maxLineSpan) }) { CircularProgressIndicator(Modifier.size(GameTokens.Inset), color = GameTokens.Accent) }
            if(failed) item(span = { GridItemSpan(maxLineSpan) }) {
                Text("Dekor inventarı yüklənmədi", color = GameTokens.Danger)
                CommerceAction("Yenidən cəhd et", onClick = onRetry)
            }
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
