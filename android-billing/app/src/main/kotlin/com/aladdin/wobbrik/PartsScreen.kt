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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException

/** Approved parts flow: catalog → permanent unlock; owned building → draft → explicit apply. */
@Composable
internal fun PartsScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, buildingId: String?, onBack: () -> Unit) {
    val art = rememberCosmeticArt()
    val mutations = remember(configured) { PartsMutations(configured.recovery) }
    val catalog = ready.catalog
    var category by remember { mutableStateOf("wall") }
    var choosing by remember { mutableStateOf(buildingId == null) }
    var openedOnly by remember { mutableStateOf(false) }
    var achievementOnly by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<CatalogItem?>(null) }
    var building by remember { mutableStateOf<OwnedBuilding?>(null) }
    var draft by remember { mutableStateOf(emptyMap<String, String>()) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(ready, buildingId, retry) {
        if (buildingId == null) return@LaunchedEffect
        building = null; failed = false
        try {
            val session = requireNotNull(configured.sessions.current())
            check(session.uid == ready.accountKey && configured.recovery.state.value === ready)
            val value = configured.inventory.building(session, buildingId)
            check(configured.sessions.current() == session && configured.recovery.state.value === ready)
            building = value; draft = value.parts
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
    }
    val back = { if (selected != null) selected = null else if (buildingId != null && choosing) choosing = false else onBack() }
    BackHandler(onBack = back)
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            TextButton(onClick = back, modifier = Modifier.heightIn(min = GameTokens.Target)) { Text("‹ Geri", color = GameTokens.Text) }
            Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.weight(1f).height(GameTokens.Target))
            Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
            Text(ready.snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
        }
        val item = selected
        if (item != null) {
            val available = FinishCosmetics.available(item, ready.snapshot)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
                Text(item.name, color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                Canvas(Modifier.fillMaxWidth().height(GameTokens.PanelWidth)) { sprite(art[item.id], 0f, 0f, size.width, size.height) }
                Text("Bina dəyərinə +${item.weight}", color = GameTokens.Accent, fontSize = GameTokens.Title)
                Text(partAccess(item, ready), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                if (item.method == "coins") Text("Hesabda daimi açılır", color = GameTokens.Muted)
                if (item.id !in art) Text("Hissənin qrafikası mövcud deyil", color = GameTokens.Danger)
            }
            HomeButton(label = if (available) "✓ Açılıb" else "Aç", primary = true,
                enabled = !available && item.method == "coins" && item.price?.let { it <= ready.snapshot.coins } == true,
                onClick = { mutations.buy(ready, item.id) })
        } else {
            Text(if (buildingId == null) "Hissələr · ${catalog?.items?.count { it.category in FinishCosmetics.categories } ?: 0}" else "Bina görünüşü",
                color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
            if (choosing) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                FinishCosmetics.categories.forEach { key ->
                    Button(onClick = { category = key }, modifier = Modifier.heightIn(min = GameTokens.Target),
                        shape = RoundedCornerShape(GameTokens.ControlRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = if (category == key) GameTokens.Accent else GameTokens.Raised,
                            contentColor = if (category == key) GameTokens.Background else GameTokens.Text)) {
                        Text("${partCategory(key)} ${catalog?.items?.count { it.category == key } ?: 0}", softWrap = false)
                    }
                }
            }
            if (buildingId != null && !choosing) {
                val current = building
                if (current != null) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    Row(Modifier.fillMaxWidth().height(GameTokens.PanelWidth), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Column(Modifier.widthIn(max = GameTokens.PanelWidth / 3).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                            FinishCosmetics.categories.forEach { key ->
                                Button(onClick = { category = key; choosing = true },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target),
                                    shape = RoundedCornerShape(GameTokens.ControlRadius),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (category == key) GameTokens.Accent else GameTokens.Raised,
                                        contentColor = if (category == key) GameTokens.Background else GameTokens.Text)) {
                                    Text(partCategory(key), softWrap = false)
                                }
                            }
                        }
                        BuildingPreview(current.copy(parts = draft), Modifier.weight(1f).fillMaxHeight())
                    }
                    Text("${current.floors} mərtəbə · ${current.residents} sakin", color = GameTokens.Text)
                    val old = catalog?.items?.firstOrNull { it.id == current.parts[category] }?.name ?: current.parts[category].orEmpty()
                    val new = catalog?.items?.firstOrNull { it.id == draft[category] }?.name ?: draft[category].orEmpty()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        listOf("Hazırkı" to current.parts[category], "Seçilmiş" to draft[category]).forEach { (label, id) ->
                            Surface(onClick = { choosing = true }, modifier = Modifier.weight(1f), color = GameTokens.Surface,
                                shape = RoundedCornerShape(GameTokens.ControlRadius),
                                border = BorderStroke(GameTokens.Hairline, if (label == "Seçilmiş") GameTokens.Accent else GameTokens.Raised)) {
                                Column(Modifier.padding(GameTokens.Small)) {
                                    Canvas(Modifier.fillMaxWidth().height(GameTokens.Target * 2)) { sprite(art[id], 0f, 0f, size.width, size.height) }
                                    Text(label, color = GameTokens.Muted)
                                    Text(if (label == "Hazırkı") old else new, color = GameTokens.Text, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                    Text(if (failed) "Bina yüklənmədi" else "Bina yüklənir…", color = GameTokens.Muted)
                    if (failed) TextButton(onClick = { retry++ }) { Text("Yenidən cəhd et") }
                }
            }
            if (choosing) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                FilterChip(selected = openedOnly, onClick = { openedOnly = !openedOnly }, label = { Text("Açıq") })
                FilterChip(selected = achievementOnly, onClick = { achievementOnly = !achievementOnly }, label = { Text("Nailiyyət") })
            }
            val items = catalog?.items.orEmpty().filter { it.category == category &&
                (!openedOnly || FinishCosmetics.available(it, ready.snapshot)) && (!achievementOnly || it.method == "achievement") }
            if (choosing) LazyVerticalGrid(GridCells.Adaptive(GameTokens.PanelWidth / 2 - GameTokens.Gap), Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                items(items, key = { it.id }) { choice ->
                    val available = FinishCosmetics.available(choice, ready.snapshot)
                    val active = draft[category] == choice.id
                    Surface(onClick = {
                        if (building != null && available && choice.id in art) { draft = draft + (category to choice.id); choosing = false }
                        else selected = choice
                    }, color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.ControlRadius),
                        border = BorderStroke(GameTokens.Hairline, if (active) GameTokens.Accent else GameTokens.Raised)) {
                        Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                            Canvas(Modifier.fillMaxWidth().height(GameTokens.Target * 2)) { sprite(art[choice.id], 0f, 0f, size.width, size.height) }
                            Text(choice.name, color = GameTokens.Text, fontWeight = FontWeight.Bold)
                            Text(if (active) "✓ Seçilib" else partAccess(choice, ready), color = if (available) GameTokens.Success else GameTokens.Muted)
                        }
                    }
                }
            }
            if (choosing && items.isEmpty()) Text("Bu filtrdə hissə yoxdur", color = GameTokens.Muted)
            if (catalog == null) Text("Kataloq yüklənmədi", color = GameTokens.Danger)
            if (buildingId != null && !choosing) HomeButton(label = "✓ Tətbiq et", primary = true,
                enabled = building != null && draft != building?.parts && catalog != null &&
                    FinishCosmetics.valid(catalog, ready.snapshot, draft) && draft.values.all { it in art },
                onClick = { building?.let { mutations.apply(ready, it, draft) } })
        }
        HomeButton(label = if (item != null || (buildingId != null && choosing)) "Hissələrə qayıt" else "İnventara qayıt", compact = true, onClick = back)
    }
}

private fun partCategory(category: String) = when (category) {
    "wall" -> "Divar"; "window" -> "Pəncərə"; "entrance" -> "Giriş"; "roof" -> "Dam"; else -> "İşıq"
}
private fun partAccess(item: CatalogItem, ready: AuthenticatedRecoveryOwner.State.Ready): String = when {
    item.method == "free" -> "Pulsuz"
    FinishCosmetics.available(item, ready.snapshot) -> "✓ Açılıb"
    item.method == "coins" -> "${item.price ?: "—"} sikkə"
    item.method == "reward" -> "Reklam ${ready.snapshot.account?.partProgress?.firstOrNull { it.itemId == item.id }?.completed ?: 0}/${item.views ?: "—"} · Hazır deyil"
    item.method == "achievement" -> "Nailiyyətlə açılır"
    else -> "Daimi alış · Hazır deyil"
}
