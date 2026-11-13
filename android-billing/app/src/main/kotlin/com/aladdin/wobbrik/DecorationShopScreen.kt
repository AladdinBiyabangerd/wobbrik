package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException

/** Approved decoration purchase panel only; purchasing never invokes placement. */
@Composable
internal fun DecorationShopScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onBack: () -> Unit) {
    val purchases = remember(configured) { CityPurchases(configured.recovery) }
    var copies by remember { mutableStateOf<List<DecorationCopy>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(ready, retry) {
        copies = null; failed = false
        try {
            val session = requireNotNull(configured.sessions.current())
            check(session.uid == ready.accountKey && configured.recovery.state.value === ready)
            val repository = configured.decorations
            var cursor: String? = null
            val result = linkedMapOf<String, DecorationCopy>()
            val seen = mutableSetOf<String>()
            do {
                val page = repository.page(session, cursor)
                check(configured.sessions.current() == session && configured.recovery.state.value === ready)
                check(page.copies.none { it.id in result })
                page.copies.forEach { result[it.id] = it }
                cursor = page.next
                check(cursor == null || seen.add(cursor))
            } while (cursor != null)
            copies = result.values.toList()
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
    }
    DecorationShopContent(ready, copies, failed, { retry++ }, { purchases.buy(ready, it) }, onBack)
}

@Composable
internal fun DecorationShopContent(ready: AuthenticatedRecoveryOwner.State.Ready,
    copies: List<DecorationCopy>?, failed: Boolean, onRetry: () -> Unit, onBuy: (String) -> Unit,
    onBack: () -> Unit) {
    val art = rememberDecorationArt()
    var category by remember { mutableStateOf("tree") }
    var selected by remember { mutableStateOf<CatalogItem?>(null) }
    val back = { if (selected != null) selected = null else onBack() }
    BackHandler(onBack = back)
    Column(Modifier.fillMaxSize().background(GameTokens.Background)
        .safeDrawingPadding().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        PurchaseHeader("Dekor · ${ready.catalog?.items?.count { it.method == "copy" } ?: 0}", ready.snapshot.coins, back)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            listOf(Triple("tree", "Ağac", R.drawable.city_decor_tree), Triple("bench", "Skamya", R.drawable.city_decor_bench),
                Triple("streetlight", "İşıq", R.drawable.city_decor_light), Triple("road", "Yol", R.drawable.city_decor_road)).forEach { (key, label, icon) ->
                Button(onClick = { category = key; selected = null }, modifier = Modifier.heightIn(min = GameTokens.Target),
                    shape = RoundedCornerShape(GameTokens.CardRadius), contentPadding = PaddingValues(GameTokens.Small),
                    border = BorderStroke(GameTokens.Hairline, if (category == key) GameTokens.Text else GameTokens.Raised),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == key) GameTokens.Accent else GameTokens.Raised,
                        contentColor = if (category == key) GameTokens.Background else GameTokens.Text)) {
                    Icon(painterResource(icon), null, Modifier.size(GameTokens.Inset))
                    Spacer(Modifier.width(GameTokens.Quarter))
                    Text(label, softWrap = false, fontWeight = FontWeight.Bold, fontSize = GameTokens.Caption)
                }
            }
        }
        val items = ready.catalog?.items.orEmpty().filter { it.method == "copy" && it.id.startsWith("decor_${category}_") }
        LazyVerticalGrid(GridCells.Fixed(if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1f) 1 else 3), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            items(items, key = { it.id }) { item ->
                PurchaseCard(selected?.id == item.id, onClick = { selected = item }) {
                    Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        PurchaseArt(art[item.id], Modifier.fillMaxWidth().height(mobileUiSizing().cardArt))
                        Text(item.name, color = GameTokens.Text, fontWeight = FontWeight.Bold, fontSize = GameTokens.Body, minLines = 2)
                        PurchasePrice(item.price, Modifier.fillMaxWidth())
                        copies?.let { owned -> Text("Nüsxə: ${owned.count { it.itemId == item.id }}", color = GameTokens.Muted, fontSize = GameTokens.Caption) }
                    }
                }
            }
        }
        if (failed) TextButton(onClick = { onRetry() }) { Text("Dekor inventarı yüklənmədi · Yenidən cəhd et", color = GameTokens.Danger) }
        if (ready.catalog == null) Text("Kataloq yüklənmədi", color = GameTokens.Danger)
        selected?.let { item ->
            CityPurchasePanel(enabled = CityPurchases.eligible(ready.catalog, ready.snapshot, item.id),
                onBuy = { onBuy(item.id) }, onCancel = { selected = null }) {
                PurchaseSummary(art[item.id]) {
                    Text("${item.name} · ${item.price} sikkə", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    Text("Bu dekorun bir nüsxəsini almaq istəyirsiniz?", color = GameTokens.Muted)
                    Text("XP, gəlir və bina dəyəri bonusu vermir.", color = GameTokens.Muted)
                    if (item.price?.let { it > ready.snapshot.coins } == true) Text("Sikkə kifayət deyil", color = GameTokens.Danger)
                }
            }
        }
        HomeButton("İnventara qayıt", compact = true, onClick = onBack)
    }
}
