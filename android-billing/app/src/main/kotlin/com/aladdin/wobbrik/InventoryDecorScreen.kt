package com.aladdin.wobbrik

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*

private val decorFilters = listOf("all" to "Hamısı", "tree" to "Ağac", "bench" to "Skamya", "streetlight" to "İşıq", "road" to "Yol")

/** Filters only accepted loaded copies; server pagination remains bounded and reachable. */
internal fun decorMatches(copy: DecorationCopy, category: String) =
    category == "all" || copy.itemId.startsWith("decor_${category}_")

@Composable
internal fun InventoryDecorContent(catalog: RecoveryCatalog?, copies: List<DecorationCopy>, loading: Boolean,
    failed: Boolean, cached: Boolean, hasMore: Boolean, onMore: () -> Unit, onRefresh: () -> Unit, onMarket: () -> Unit) {
    var category by rememberSaveable { mutableStateOf("all") }
    val art = rememberOwnedDecorationArt()
    val names = remember(catalog) { catalog?.items.orEmpty().associate { it.id to it.name } }
    val visible = remember(copies, category) { copies.filter { decorMatches(it, category) } }
    val state = rememberLazyGridState()
    val large = LocalDensity.current.fontScale > 1f
    LaunchedEffect(category) { state.scrollToItem(0) }
    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = GameTokens.InventoryCollection.ScreenInset)) {
        val height = if (large) GameTokens.InventoryCollection.LargeTypeArtHeight else
            ((maxWidth - GameTokens.Small) / 2) * GameTokens.DecorCollection.ArtHeightFraction
        Column(Modifier.fillMaxSize().padding(bottom = LocalCollectionDockClearance.current)) {
            LazyVerticalGrid(GridCells.Fixed(if (large) 1 else 2), state = state,
                modifier = Modifier.weight(1f).semantics { contentDescription = "Dekor nüsxələri" },
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small),
                contentPadding = PaddingValues(vertical = GameTokens.Small)) {
                item(key = "filters", span = { GridItemSpan(maxLineSpan) }) { DecorFilters(category) { category = it } }
                item(key = "heading", span = { GridItemSpan(maxLineSpan) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Dekorlarım", Modifier.weight(1f), color = GameTokens.Text,
                            fontSize = GameTokens.Body, fontWeight = FontWeight.ExtraBold)
                        CollectionRefresh(!loading, onRefresh)
                    }
                }
                if (cached) item(key = "cache", span = { GridItemSpan(maxLineSpan) }) {
                    Text("Saxlanmış məlumat · yalnız baxış", color = GameTokens.InventoryCollection.Metadata, fontSize = GameTokens.Caption)
                }
                items(visible, key = { it.id }, contentType = { "decor-copy" }) { copy ->
                    val shape = RoundedCornerShape(GameTokens.ControlRadius)
                    Column(Modifier.fillMaxWidth().clip(shape)
                        .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))
                        .border(GameTokens.InventoryCollection.OutlineWidth, GameTokens.InventoryCollection.Outline, shape)
                        .semantics(mergeDescendants = true) {}) {
                        PurchaseArt(art[copy.itemId], Modifier.fillMaxWidth().height(height).padding(GameTokens.Quarter), allowUpscale = true)
                        Column(Modifier.padding(start = GameTokens.InventoryCollection.CardInset, end = GameTokens.Small,
                            bottom = GameTokens.InventoryCollection.CardInset), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                            Text(names[copy.itemId] ?: copy.itemId, color = GameTokens.Text, fontSize = GameTokens.Body, lineHeight = GameTokens.Body, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                                if (copy.position != null) Text("✓", color = GameTokens.AppearanceCollection.Unlocked, fontSize = GameTokens.Body, lineHeight = GameTokens.Body, fontWeight = FontWeight.Bold)
                                else MobileMenuIcon(MobileIcon.Inventory, Modifier.size(GameTokens.Gap), GameTokens.InventoryCollection.Metadata)
                                Text(if (copy.position != null) "Şəhərdə" else "Saxlanılır",
                                    color = if (copy.position != null) GameTokens.AppearanceCollection.Unlocked else GameTokens.InventoryCollection.Metadata,
                                    fontSize = GameTokens.Label, lineHeight = GameTokens.Body, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                if (!loading && !failed && visible.isEmpty()) item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    Text(if (copies.isEmpty()) "Hələ dekor nüsxən yoxdur." else "Yüklənmiş nüsxələrdə bu filtrə uyğun dekor yoxdur.",
                        color = GameTokens.InventoryCollection.Metadata, fontSize = GameTokens.Body)
                }
                if (loading) item(key = "loading", span = { GridItemSpan(maxLineSpan) }) {
                    CircularProgressIndicator(Modifier.size(GameTokens.Inset).semantics { contentDescription = "Dekor yüklənir" }, color = GameTokens.Accent)
                }
                if (failed) item(key = "error", span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text("İnventar yenilənmədi.", color = GameTokens.Danger)
                        CommerceAction("Yenidən cəhd et", onClick = onRefresh)
                    }
                }
                if (hasMore && !loading && !failed) item(key = "more", span = { GridItemSpan(maxLineSpan) }) {
                    CommerceAction("Daha çox", onClick = onMore)
                }
            }
            DecorMarketAction(onMarket)
        }
    }
}

@Composable
private fun DecorFilters(category: String, onCategory: (String) -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    Row(Modifier.fillMaxWidth().selectableGroup().then(if (large) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        decorFilters.forEach { (key, label) ->
            val active = category == key
            val interaction = remember { MutableInteractionSource() }
            val shape = RoundedCornerShape(GameTokens.AppearanceCollection.CategoryRadius)
            Surface(onClick = { onCategory(key) }, interactionSource = interaction, color = Color.Transparent,
                modifier = (if (large) Modifier.widthIn(min = GameTokens.Target * 2) else Modifier.weight(1f))
                    .collectionPress(interaction).semantics { selected = active; role = Role.Tab }) {
                Column(Modifier.heightIn(min = GameTokens.AppearanceCollection.CategoryHeight).clip(shape)
                    .background(Brush.verticalGradient(if (active) listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom)
                        else listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))
                    .border(GameTokens.InventoryCollection.OutlineWidth, if (active) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape)
                    .padding(GameTokens.Quarter), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    val tint = if (active) GameTokens.Background else GameTokens.Text
                    if (key == "all") DecorAllIcon(tint)
                    else Icon(painterResource(when(key) {
                        "tree" -> R.drawable.inventory_decor_tree; "bench" -> R.drawable.inventory_decor_bench
                        "streetlight" -> R.drawable.inventory_decor_light; else -> R.drawable.inventory_decor_road
                    }), null, Modifier.size(GameTokens.AppearanceCollection.CategoryIcon), tint = tint)
                    Text(label, color = tint, fontSize = GameTokens.Caption, fontWeight = FontWeight.Bold, softWrap = false)
                }
            }
        }
    }
}

@Composable
private fun DecorAllIcon(tint: Color) {
    Canvas(Modifier.size(GameTokens.AppearanceCollection.CategoryIcon)) {
        val gap = GameTokens.Quarter.toPx()
        val cell = (size.width - gap) / 2
        for (x in 0..1) for (y in 0..1) drawRoundRect(tint,
            androidx.compose.ui.geometry.Offset(x * (cell + gap), y * (cell + gap)),
            androidx.compose.ui.geometry.Size(cell, cell), androidx.compose.ui.geometry.CornerRadius(gap / 2))
    }
}

@Composable
private fun DecorMarketAction(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(GameTokens.CardRadius)
    val large = LocalDensity.current.fontScale > 1f
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(onClick = onClick, interactionSource = interaction, color = Color.Transparent,
            modifier = Modifier.fillMaxWidth(if (large) 1f else GameTokens.DecorCollection.MarketWidthFraction).collectionPress(interaction)) {
            Box(Modifier.heightIn(min = GameTokens.Target).clip(shape)
                .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))
                .border(GameTokens.InventoryCollection.OutlineWidth, GameTokens.Accent, shape)
                .padding(horizontal = GameTokens.Small, vertical = GameTokens.Small), contentAlignment = Alignment.Center) {
                Text("Dekor üçün Bazara keç", color = GameTokens.Text, fontSize = GameTokens.Label,
                    fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
