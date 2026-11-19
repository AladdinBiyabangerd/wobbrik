package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException

/** Ownership reads only. Shop commands and placement capability never originate here. */
@Composable
internal fun OwnedCityItemsScreen(configured: WobbrikApplication.Bootstrap.Configured, view: AccountView,
    land: Boolean, onBack: () -> Unit, onMarket: () -> Unit) {
    var page by remember(view, land) { mutableStateOf<DecorationPage?>(null) }
    var copies by remember(view, land) { mutableStateOf(emptyList<DecorationCopy>()) }
    var cursor by remember(view, land) { mutableStateOf<String?>(null) }
    var failed by remember(view, land) { mutableStateOf(false) }
    var loading by remember(view, land) { mutableStateOf(!land) }
    var cached by remember(view, land) { mutableStateOf(view.ready == null) }
    var retry by remember(view, land) { mutableIntStateOf(0) }
    BackHandler(onBack = onBack)
    LaunchedEffect(view, land, cursor, retry) {
        if (land) return@LaunchedEffect
        loading = true; failed = false
        try {
            val session = requireNotNull(configured.sessions.current())
            check(view.current(configured, session))
            val requestedCursor = cursor
            val prefix = copies
            fun publish(value: DecorationPage) {
                check(view.current(configured, session))
                page = value
                copies = if (requestedCursor == null) value.copies else (prefix + value.copies).distinctBy { it.id }
            }
            val value = configured.views?.read(view, session, "owned-decorations-${cursor.orEmpty()}",
                onCached = { cached = true }, refresh = retry > 0,
                onFresh = { cached = false }, onLocal = { value: DecorationPage -> publish(value) }) {
                DecorationRepository(it).page(session, requestedCursor)
            } ?: configured.decorations.page(session, requestedCursor)
            publish(value)
        } catch(cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    OwnedCityItemsContent(view.snapshot, view.catalog, land, copies, loading, failed, cached,
        page?.next != null, { cursor = page?.next }, { cursor = null; retry++ }, onMarket)
}

@Composable
internal fun OwnedCityItemsContent(snapshot: GameSnapshot, catalog: RecoveryCatalog?, land: Boolean,
    copies: List<DecorationCopy>, loading: Boolean, failed: Boolean, cached: Boolean, hasMore: Boolean,
    onMore: () -> Unit, onRetry: () -> Unit, onMarket: () -> Unit) {
    if (!land && LocalCollectionLayout.current) {
        InventoryDecorContent(catalog, copies, loading, failed, cached, hasMore, onMore, onRetry, onMarket)
        return
    }
    val art = if(land) emptyMap() else rememberDecorationArt()
    val illustration = if(land) rememberLandArt() else null
    val largeFont = LocalDensity.current.fontScale > 1f
    val sizing = mobileUiSizing()
    Column(Modifier.fillMaxSize().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            if (land) {
                item { PurchaseArt(illustration, Modifier.fillMaxWidth().height(if(largeFont) GameTokens.Target * 2 else GameTokens.Target * 3)) }
                if(snapshot.account == null) item { Text("Ərazi hüquqları yüklənməyib", color = GameTokens.Muted) }
                else items(listOf("base") + requireNotNull(snapshot.account).land, key = { it }) { id ->
                    MenuRelief(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                            Text(if(id == "base") "Başlanğıc sahə" else "Sahə ${id.removePrefix("land_")}", color = GameTokens.Text,
                                fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                            Text("✓ Alınıb · Daimi hüquq", color = GameTokens.Accent, fontFamily = GameTypeface)
                        }
                    }
                }
            } else {
                items(copies, key = { it.id }) { copy ->
                    MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius), kind = if(copy.position != null) MenuSkin.Selected else MenuSkin.Commerce) {
                        val details: @Composable ColumnScope.() -> Unit = {
                            Text(catalog?.items?.firstOrNull { it.id == copy.itemId }?.name ?: copy.itemId,
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), softWrap = false,
                                color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                            if(copy.position != null) MenuRelief(Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(GameTokens.Target), kind = MenuSkin.Track) {
                                Box(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target).padding(GameTokens.Small),
                                    contentAlignment = Alignment.Center) {
                                    Text("✓ Şəhərdə", Modifier.horizontalScroll(rememberScrollState()),
                                        color = GameTokens.Accent, fontFamily = GameTypeface,
                                        fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
                                }
                            }
                            else Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                                MobileMenuIcon(MobileIcon.Inventory, Modifier.size(GameTokens.Inset))
                                Text("Saxlanılır", Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                                    color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body,
                                    fontWeight = FontWeight.Medium, softWrap = false)
                            }
                            if(copy.position == null) CommerceAction("Yerləşdir", enabled = false) {}
                        }
                        if(largeFont) Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                            PurchaseArt(art[copy.itemId], Modifier.fillMaxWidth().height(GameTokens.Target + GameTokens.Inset))
                            details()
                        } else Row(Modifier.heightIn(min = sizing.cardArt + GameTokens.Target).padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                            PurchaseArt(art[copy.itemId], Modifier.size(sizing.cardArt + GameTokens.Inset))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = details)
                        }
                    }
                }
                if(!loading && !failed && copies.isEmpty()) item { Text("Hələ dekor nüsxən yoxdur.", color = GameTokens.Muted) }
                if(copies.any { it.position == null }) item { Text("Yerləşdirmə hazırda bağlıdır.", color = GameTokens.Muted, fontSize = GameTokens.Caption) }
            }
            if(cached) item { Text("Keş · yalnız baxış", color = GameTokens.Muted, fontSize = GameTokens.Caption) }
            if(loading) item { CircularProgressIndicator(Modifier.size(GameTokens.Inset), color = GameTokens.Accent) }
            if(failed) item {
                Text("İnventar yenilənmədi.", color = GameTokens.Danger)
                CommerceAction("Yenidən cəhd et", onClick = onRetry)
            }
            if(hasMore && !loading && !failed) item { CommerceAction("Daha çox", onClick = onMore) }
        }
        if(!land) CommerceAction("Dekor üçün Bazara keç", icon = MobileIcon.Market, onClick = onMarket)
    }
}
