package com.aladdin.wobbrik

import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException

/** Approved decoration purchase panel only; purchasing never invokes placement. */
@Composable
internal fun DecorationShopScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onBack: () -> Unit) {
    val context = LocalContext.current
    val art = remember(context) {
        val image = context.assets.open("city/decor-isometric.png").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        // Explicit art regions, not collision footprints or an equal-cell atlas.
        val regions = listOf(
            intArrayOf(0, 0, 300, 380), intArrayOf(300, 0, 345, 380), intArrayOf(645, 0, 441, 390),
            intArrayOf(0, 410, 300, 320), intArrayOf(300, 410, 345, 320), intArrayOf(645, 390, 441, 350),
            intArrayOf(0, 770, 300, 280), intArrayOf(300, 770, 320, 280), intArrayOf(620, 770, 466, 280),
            intArrayOf(0, 1160, 310, 180), intArrayOf(310, 1050, 305, 370), intArrayOf(615, 1160, 471, 180))
        listOf("tree", "bench", "streetlight", "road").flatMap { group -> (1..3).map { "decor_${group}_$it" } }
            .zip(regions).associate { (id, r) -> id to CosmeticSprite(image, IntOffset(r[0], r[1]), IntSize(r[2], r[3])) }
    }
    val purchases = remember(configured) { CityPurchases(configured.recovery) }
    var category by remember { mutableStateOf("tree") }
    var selected by remember { mutableStateOf<CatalogItem?>(null) }
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
    val back = { if (selected != null) selected = null else onBack() }
    BackHandler(onBack = back)
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            TextButton(onClick = back, modifier = Modifier.heightIn(min = GameTokens.Target)) { Text("‹ Geri", color = GameTokens.Text) }
            Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.weight(1f).height(GameTokens.Target))
            Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
            Text(ready.snapshot.coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
        }
        Text("Dekor · ${ready.catalog?.items?.count { it.method == "copy" } ?: 0}", color = GameTokens.Text,
            fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            listOf("tree" to "Ağac", "bench" to "Skamya", "streetlight" to "İşıq", "road" to "Yol").forEach { (key, label) ->
                Button(onClick = { category = key; selected = null }, modifier = Modifier.heightIn(min = GameTokens.Target),
                    shape = RoundedCornerShape(GameTokens.ControlRadius), colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == key) GameTokens.Accent else GameTokens.Raised,
                        contentColor = if (category == key) GameTokens.Background else GameTokens.Text)) { Text(label, softWrap = false) }
            }
        }
        val items = ready.catalog?.items.orEmpty().filter { it.method == "copy" && it.id.startsWith("decor_${category}_") }
        LazyVerticalGrid(GridCells.Adaptive(GameTokens.PanelWidth / 3 - GameTokens.Small), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            items(items, key = { it.id }) { item ->
                Surface(onClick = { selected = item }, color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius),
                    border = BorderStroke(GameTokens.Hairline, if (selected?.id == item.id) GameTokens.Accent else GameTokens.Raised)) {
                    Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Canvas(Modifier.fillMaxWidth().height(GameTokens.Target * 2)) { sprite(art[item.id], 0f, 0f, size.width, size.height) }
                        Text(item.name, color = GameTokens.Text, fontWeight = FontWeight.Bold)
                        Text("${item.price ?: "—"} sikkə", color = GameTokens.Danger)
                        copies?.let { owned -> Text("Nüsxə: ${owned.count { it.itemId == item.id }}", color = GameTokens.Muted) }
                    }
                }
            }
        }
        if (failed) TextButton(onClick = { retry++ }) { Text("Dekor inventarı yüklənmədi · Yenidən cəhd et", color = GameTokens.Danger) }
        if (ready.catalog == null) Text("Kataloq yüklənmədi", color = GameTokens.Danger)
        selected?.let { item ->
            Column(Modifier.heightIn(max = GameTokens.PanelWidth).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Text("${item.name} · ${item.price} sikkə", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                Text("Bu dekorun bir nüsxəsini almaq istəyirsiniz?", color = GameTokens.Muted)
                Text("XP, gəlir və bina dəyəri bonusu vermir.", color = GameTokens.Muted)
                if (item.price?.let { it > ready.snapshot.coins } == true) Text("Sikkə kifayət deyil", color = GameTokens.Danger)
            }
            HomeButton("✓ Al", primary = true, enabled = CityPurchases.eligible(ready.catalog, ready.snapshot, item.id),
                onClick = { purchases.buy(ready, item.id) })
            HomeButton("Ləğv et", compact = true, onClick = { selected = null })
        }
        HomeButton("İnventara qayıt", compact = true, onClick = onBack)
    }
}
