package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*

/** Purchase rights only; map selection and placement remain outside this flow. */
@Composable
internal fun LandShopScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onBack: () -> Unit) {
    val purchases = remember(configured) { CityPurchases(configured.recovery) }
    LandShopContent(ready, { purchases.buy(ready, it) }, onBack)
}

@Composable
internal fun LandShopContent(ready: AuthenticatedRecoveryOwner.State.Ready, onBuy: (String) -> Unit,
    onBack: () -> Unit) {
    var selected by remember { mutableStateOf<LandOffer?>(null) }
    val illustration = rememberLandArt()
    val back = { if (selected != null) selected = null else onBack() }
    BackHandler(onBack = back)
    Column(Modifier.fillMaxSize().background(GameTokens.Background)
        .safeDrawingPadding().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        PurchaseHeader("Ərazi alışı", ready.snapshot.coins, back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Text("Şəhəri genişləndirmək üçün əlavə sahə alın.", color = GameTokens.Muted)
            ready.catalog?.landOffers.orEmpty().forEach { offer ->
                val owned = offer.id in ready.snapshot.account?.land.orEmpty()
                val unlocked = ready.snapshot.xp >= offer.requiredXp
                val expanded = selected?.id == offer.id || (selected == null && offer == ready.catalog?.landOffers?.firstOrNull())
                PurchaseCard(selected == offer, onClick = { selected = offer }) {
                    Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        PurchaseArt(illustration, Modifier.size(if (expanded) GameTokens.Target * 2 else GameTokens.Target + GameTokens.Inset))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                            Text("Sahə ${offer.id.removePrefix("land_")}", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                            Text("${offer.requiredXp} XP", color = GameTokens.Accent, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                                Image(painterResource(R.drawable.ui_coin), null, Modifier.size(GameTokens.Inset))
                                Text("${offer.price} sikkə", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                            }
                            val capacity = mapOf("land_1" to 4, "land_2" to 5, "land_3" to 6, "land_4" to 7, "land_5" to 8)[offer.id]
                            if (expanded) capacity?.let { Text("+$it standart bina ekvivalenti", color = GameTokens.Accent) }
                            Text(when { owned -> "✓ Alınıb"; !unlocked -> "Kilidli · ${ready.snapshot.xp}/${offer.requiredXp} XP"; else -> "Alış üçün açıqdır" },
                                color = if (owned) GameTokens.Success else GameTokens.Muted)
                        }
                    }
                }
            }
            if (ready.catalog == null) Text("Kataloq yüklənmədi", color = GameTokens.Danger)
            Text("Sahələri istənilən ardıcıllıqla ala bilərsiniz. Alınmış hüquq daimidir.", color = GameTokens.Muted)
            Text("Bina ekvivalenti sahənin tutumudur, sərt bina limiti deyil.", color = GameTokens.Muted)
        }
        selected?.let { offer ->
            CityPurchasePanel(enabled = CityPurchases.eligible(ready.catalog, ready.snapshot, offer.id),
                onBuy = { onBuy(offer.id) }, onCancel = { selected = null }) {
                PurchaseSummary(illustration) {
                    Text("Sahə ${offer.id.removePrefix("land_")} · ${offer.price} sikkə", color = GameTokens.Text,
                        fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                    Text("Balans: ${ready.snapshot.coins} sikkə", color = GameTokens.Text)
                    Text("Daimi genişlənmə", color = GameTokens.Accent)
                    Text("Alış üçün ${offer.requiredXp} XP tələb olunur. Alış XP vermir.", color = GameTokens.Muted)
                    if (offer.price > ready.snapshot.coins) Text("Sikkə kifayət deyil", color = GameTokens.Danger)
                }
            }
        }
        HomeButton("İnventara qayıt", compact = true, onClick = onBack)
    }
}
