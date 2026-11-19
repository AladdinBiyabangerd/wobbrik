package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
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
import com.aladdin.wobbrik.engine.CosmeticDefaultsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Global collection, not a building draft. Buying still uses the sole server journal. */
@Composable
internal fun InventoryAppearanceScreen(configured: WobbrikApplication.Bootstrap.Configured,
    view: AccountView, defaults: CosmeticDefaultsStore, onBack: () -> Unit) {
    val ready = view.ready
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var category by rememberSaveable { mutableStateOf("wall") }
    var openedOnly by rememberSaveable { mutableStateOf(false) }
    var achievementOnly by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyGridState()
    val cosmetic = rememberCosmeticArt()
    val windows = rememberGameWindowArt()
    val art = remember(cosmetic, windows) { cosmetic + windows }
    LaunchedEffect(category, openedOnly, achievementOnly) { listState.scrollToItem(0) }
    CommerceDetailChrome(selected != null)
    BackHandler { if (selected != null && !saving) selected = null else if (!saving) onBack() }
    val selectedId = selected
    if (selectedId != null && ready != null) {
        PartsScreen(configured, ready, null, { if (!saving) { selected = null; saveFailed = false } },
            initialItemId = selectedId, useEnabled = !saving, useError = saveFailed,
            onUse = { item ->
                if (!saving && configured.recovery.state.value === ready) {
                    saving = true; saveFailed = false
                    scope.launch {
                        try {
                            val uid = requireNotNull(ready.accountKey)
                            val catalog = requireNotNull(ready.catalog)
                            val session = requireNotNull(configured.sessions.current())
                            check(session.uid == uid && configured.recovery.state.value === ready)
                            val saved = withContext(Dispatchers.IO) { defaults.read(uid) }
                            check(configured.sessions.current() == session && configured.recovery.state.value === ready)
                            val parts = FinishCosmetics.selection(catalog, ready.snapshot, saved) + (item.category to item.id)
                            check(FinishCosmetics.valid(catalog, ready.snapshot, parts))
                            check(parts.values.all { it in art })
                            // Account-scoped display preference, never a wallet or building command.
                            withContext(Dispatchers.IO) {
                                check(configured.sessions.current() == session && configured.recovery.state.value === ready)
                                defaults.write(uid, parts)
                            }
                            if (configured.sessions.current() == session && configured.recovery.state.value === ready) selected = null
                        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                        catch (_: Exception) { saveFailed = true }
                        finally { saving = false }
                    }
                }
            })
        return
    }
    val purple = rememberPurpleBuildingArt()
    val catalog = view.catalog
    // One linear catalog pass per accepted revision/filter, no card reads or collection scan.
    val items = remember(catalog, view.snapshot, category, openedOnly, achievementOnly) {
        catalog.items.filter { it.category == category &&
            (!openedOnly || FinishCosmetics.available(it, view.snapshot)) &&
            (!achievementOnly || it.method == "achievement") }
    }
    val parts = remember(catalog, view.snapshot) {
        FinishCosmetics.selection(catalog, view.snapshot, emptyMap())
    }
    AppearanceCollectionContent(category, openedOnly, achievementOnly, items, view.snapshot,
        onCategory = { category = it }, onOpened = { openedOnly = !openedOnly },
        onAchievement = { achievementOnly = !achievementOnly }, onRefresh = { configured.recovery.onForeground() },
        onItem = { selected = it.id }, readOnly = ready == null, listState = listState) { item, modifier ->
        if (item.id in art) Canvas(modifier) {
            appearancePartPreview(purple, art, parts + (item.category to item.id), item.category)
        } else Box(modifier, contentAlignment = Alignment.Center) {
            Text("Qrafika mövcud deyil", color = GameTokens.Muted, fontSize = GameTokens.Caption)
        }
    }
}

/** Pure native presentation: all text, controls, gradients and card surfaces remain Compose. */
@Composable
internal fun AppearanceCollectionContent(category: String, openedOnly: Boolean, achievementOnly: Boolean,
    items: List<CatalogItem>, snapshot: GameSnapshot, onCategory: (String) -> Unit,
    onOpened: () -> Unit, onAchievement: () -> Unit, onRefresh: () -> Unit, onItem: (CatalogItem) -> Unit,
    catalogMissing: Boolean = false, readOnly: Boolean = false,
    listState: LazyGridState = rememberLazyGridState(), preview: @Composable (CatalogItem, Modifier) -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = GameTokens.InventoryCollection.ScreenInset)) {
        val compact = maxHeight < GameTokens.MobileUi.CompactHeight - GameTokens.Target * 3
        val artHeight = if (large) GameTokens.AppearanceCollection.LargeTypeArtHeight else if (compact)
            GameTokens.AppearanceCollection.CompactArtHeight else GameTokens.AppearanceCollection.ArtHeight
        LazyVerticalGrid(GridCells.Fixed(if (large) 1 else 2), state = listState,
            modifier = Modifier.fillMaxSize().semantics { contentDescription = "Görünüş hissələri" },
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Small),
            contentPadding = PaddingValues(top = GameTokens.Small, bottom = LocalCollectionDockClearance.current)) {
            item(key = "categories", span = { GridItemSpan(maxLineSpan) }) {
                AppearanceCategories(category, onCategory)
            }
            if (readOnly) item(key = "read-only", span = { GridItemSpan(maxLineSpan) }) {
                Text("Saxlanmış məlumat · yalnız baxış", color = GameTokens.Muted, fontSize = GameTokens.Caption)
            }
            item(key = "filters", span = { GridItemSpan(maxLineSpan) }) {
                if (large) Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(appearanceCategoryName(category), Modifier.weight(1f), color = GameTokens.Text,
                            fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                        CollectionRefresh(true, onRefresh)
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        AppearanceFilter("Açıq", openedOnly, onOpened)
                        AppearanceFilter("Nailiyyət", achievementOnly, onAchievement)
                    }
                } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    Text(appearanceCategoryName(category), Modifier.weight(1f), color = GameTokens.Text,
                        fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                    AppearanceFilter("Açıq", openedOnly, onOpened)
                    AppearanceFilter("Nailiyyət", achievementOnly, onAchievement)
                    CollectionRefresh(true, onRefresh)
                }
            }
            if (catalogMissing || items.isEmpty()) item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                Text(if (catalogMissing) "Kataloq yüklənmədi. Yenidən cəhd et." else "Bu filtrdə hissə yoxdur",
                    color = GameTokens.Muted, fontSize = GameTokens.Body, modifier = Modifier.padding(GameTokens.Gap))
            }
            items(items, key = { it.id }, contentType = { "appearance" }) { item ->
                AppearanceCard(item, FinishCosmetics.available(item, snapshot), snapshot, artHeight,
                    enabled = !readOnly, onClick = { onItem(item) }, preview = { modifier -> preview(item, modifier) })
            }
        }
    }
}

internal fun appearanceCategoryName(category: String) = when (category) {
    "wall" -> "Divar"; "window" -> "Pəncərə"; "entrance" -> "Giriş"; "roof" -> "Dam"; else -> "İşıq"
}

@Composable
private fun AppearanceCategories(category: String, onCategory: (String) -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    Row(Modifier.fillMaxWidth().selectableGroup().then(if (large) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        FinishCosmetics.categories.forEach { key ->
            val active = key == category
            val interaction = remember { MutableInteractionSource() }
            val shape = RoundedCornerShape(GameTokens.AppearanceCollection.CategoryRadius)
            Surface(onClick = { onCategory(key) }, interactionSource = interaction, color = Color.Transparent,
                modifier = (if (large) Modifier.widthIn(min = GameTokens.Target * 2) else Modifier.weight(1f))
                    .collectionPress(interaction).semantics { selected = active; role = Role.Tab }) {
                Column(Modifier.heightIn(min = GameTokens.AppearanceCollection.CategoryHeight).clip(shape)
                    .background(Brush.verticalGradient(if (active) listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom)
                        else listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))
                    .border(GameTokens.InventoryCollection.OutlineWidth, if (active) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape)
                    .padding(horizontal = GameTokens.Quarter, vertical = GameTokens.Quarter),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterVertically)) {
                    Icon(painterResource(appearanceCategoryIcon(key)), null, Modifier.size(GameTokens.AppearanceCollection.CategoryIcon),
                        tint = if (active) GameTokens.Background else GameTokens.Text)
                    Text(appearanceCategoryName(key), color = if (active) GameTokens.Background else GameTokens.Text,
                        fontSize = GameTokens.Caption, fontWeight = FontWeight.Bold, softWrap = false)
                }
            }
        }
    }
}

private fun appearanceCategoryIcon(category: String): Int = when (category) {
    "wall" -> R.drawable.appearance_wall
    "entrance" -> R.drawable.appearance_entrance
    "roof" -> R.drawable.appearance_roof
    else -> partCategoryIcon(category)
}

@Composable
private fun AppearanceFilter(label: String, active: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(onClick = onClick, interactionSource = interaction, color = Color.Transparent,
        modifier = Modifier.heightIn(min = GameTokens.Target).collectionPress(interaction).semantics { selected = active; role = Role.Checkbox }) {
        Box(contentAlignment = Alignment.Center) {
            val shape = RoundedCornerShape(GameTokens.AppearanceCollection.FilterRadius)
            Text(label, Modifier.clip(shape).background(if (active) GameTokens.Accent else GameTokens.InventoryCollection.PanelTop)
                .border(GameTokens.InventoryCollection.OutlineWidth, if(active) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape)
                .padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter),
                color = if (active) GameTokens.Background else GameTokens.InventoryCollection.Metadata,
                fontSize = GameTokens.Caption, fontWeight = FontWeight.SemiBold, softWrap = false)
        }
    }
}

@Composable
private fun AppearanceCard(item: CatalogItem, available: Boolean, snapshot: GameSnapshot,
    artHeight: androidx.compose.ui.unit.Dp, enabled: Boolean, onClick: () -> Unit, preview: @Composable (Modifier) -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Surface(onClick = onClick, enabled = enabled, interactionSource = interaction, color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().collectionPress(interaction, enabled).semantics { contentDescription = item.name }) {
        Column(Modifier.clip(shape).background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop,
            GameTokens.InventoryCollection.PanelBottom))).border(GameTokens.InventoryCollection.OutlineWidth,
            GameTokens.InventoryCollection.Outline, shape)) {
            Box(Modifier.fillMaxWidth().height(artHeight)) {
                preview(Modifier.fillMaxSize().padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter))
            }
            Column(Modifier.fillMaxWidth().padding(start = GameTokens.InventoryCollection.CardInset,
                end = GameTokens.Small, bottom = GameTokens.InventoryCollection.CardInset),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Text(item.name, color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    if (available) Text("✓", color = GameTokens.AppearanceCollection.Unlocked, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                    else if (item.method in setOf("coins", "achievement")) MobileMenuIcon(
                        if (item.method == "coins") MobileIcon.Coin else MobileIcon.Trophy, Modifier.size(GameTokens.Gap))
                    Text(appearanceAccess(item, snapshot), color = if (available) GameTokens.AppearanceCollection.Unlocked else GameTokens.InventoryCollection.Metadata,
                        fontSize = GameTokens.Label, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

internal fun appearanceAccess(item: CatalogItem, snapshot: GameSnapshot): String =
    if (FinishCosmetics.available(item, snapshot)) "Açılıb" else partAccess(item, snapshot)
