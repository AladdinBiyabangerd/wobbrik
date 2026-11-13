package com.aladdin.wobbrik

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

/** Purchase rights only; map selection and placement remain outside this flow. */
@Composable
internal fun LandShopScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onBack: () -> Unit) {
    val purchases = remember(configured) { CityPurchases(configured.recovery) }
    var selected by remember { mutableStateOf<LandOffer?>(null) }
    val context = LocalContext.current
    val illustration = remember(context) {
        // Illustration crop only; text and controls are native, geometry is server-owned.
        val image = context.assets.open("city/land-purchase-source.png").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        CosmeticSprite(image, IntOffset(27, 136), IntSize(153, 141))
    }
    val back = { if (selected != null) selected = null else onBack() }
    BackHandler(onBack = back)
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = back, modifier = Modifier.heightIn(min = GameTokens.Target)) { Text("‹ Geri", color = GameTokens.Text) }
            Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.weight(1f).height(GameTokens.Target))
            Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
            Text(ready.snapshot.coins.toString(), color = GameTokens.Text)
        }
        Text("Ərazi alışı", color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Text("Şəhəri genişləndirmək üçün əlavə sahə alın.", color = GameTokens.Muted)
            ready.catalog?.landOffers.orEmpty().forEach { offer ->
                val owned = offer.id in ready.snapshot.account?.land.orEmpty()
                val unlocked = ready.snapshot.xp >= offer.requiredXp
                Surface(onClick = { selected = offer }, color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius),
                    border = BorderStroke(GameTokens.Hairline, if (selected == offer) GameTokens.Accent else GameTokens.Raised)) {
                    Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Canvas(Modifier.size(GameTokens.Target * 2)) { sprite(illustration, 0f, 0f, size.width, size.height) }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                            Text("Sahə ${offer.id.removePrefix("land_")}", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                            Text("${offer.requiredXp} XP · ${offer.price} sikkə", color = GameTokens.Text)
                            val capacity = mapOf("land_1" to 4, "land_2" to 5, "land_3" to 6, "land_4" to 7, "land_5" to 8)[offer.id]
                            capacity?.let { Text("+$it standart bina ekvivalenti", color = GameTokens.Accent) }
                            Text(when { owned -> "✓ Alınıb"; !unlocked -> "Kilidli · ${ready.snapshot.xp}/${offer.requiredXp} XP"; else -> "Alış üçün açıqdır" },
                                color = if (owned) GameTokens.Success else GameTokens.Muted)
                        }
                    }
                }
            }
            if (ready.catalog == null) Text("Kataloq yüklənmədi", color = GameTokens.Danger)
            Text("Sahələri istənilən ardıcıllıqla ala bilərsiniz. Alınmış hüquq daimidir.", color = GameTokens.Muted)
            Text("Bina ekvivalenti sahənin tutumudur, sərt bina limiti deyil.", color = GameTokens.Muted)
            selected?.let { offer ->
                Text("Sahə ${offer.id.removePrefix("land_")} · ${offer.price} sikkə", color = GameTokens.Text,
                    fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                Text("Balans: ${ready.snapshot.coins} sikkə", color = GameTokens.Text)
                Text("Alış üçün ${offer.requiredXp} XP tələb olunur. Alış XP vermir.", color = GameTokens.Muted)
                if (offer.price > ready.snapshot.coins) Text("Sikkə kifayət deyil", color = GameTokens.Danger)
            }
        }
        selected?.let { offer ->
            HomeButton("✓ Al", primary = true, enabled = CityPurchases.eligible(ready.catalog, ready.snapshot, offer.id),
                onClick = { purchases.buy(ready, offer.id) })
            HomeButton("Ləğv et", compact = true, onClick = { selected = null })
        }
        HomeButton("İnventara qayıt", compact = true, onClick = onBack)
    }
}
