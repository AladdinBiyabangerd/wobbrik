package com.aladdin.wobbrik

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import appbackend.wobbrik.game.MarketDetail
import appbackend.wobbrik.game.RecoveryCatalog

/** Keep the approved price/action row when it fits; never hide digits behind the CTA. */
@Composable
internal fun MarketCardPurchase(price: Long, action: String, icon: MobileIcon?, enabled: Boolean, onClick: () -> Unit) {
    val measure = rememberTextMeasurer()
    val density = LocalDensity.current
    val text = measure.measure(price.toString(), LocalTextStyle.current.copy(fontFamily = GameTypeface, fontWeight = FontWeight.Bold), softWrap = false)
    val actionWidth = GameTokens.Target + GameTokens.Gap
    val priceWidth = with(density) { text.size.width.toDp() } + GameTokens.Inset + GameTokens.Quarter
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (density.fontScale <= 1f && maxWidth >= priceWidth + GameTokens.Quarter + actionWidth) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                CommercePrice(price, Modifier.weight(1f))
                MarketCollectionAction(action, Modifier.width(actionWidth), enabled = enabled,
                    icon = icon, onClick = onClick)
            }
        } else Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            CommercePrice(price, Modifier.fillMaxWidth())
            MarketCollectionAction(action, enabled = enabled, icon = icon, onClick = onClick)
        }
    }
}

/** The listing's fresh detail is presentation only; the footer still opens server-authoritative confirmation. */
@Composable
internal fun MarketPurchaseDetail(value: MarketDetail, catalog: RecoveryCatalog?, own: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Text(if(own) "Bazar · Mənim elanlarım" else "Bazar · Binalar", color = GameTokens.Muted,
            fontFamily = GameTypeface, fontSize = GameTokens.Label)
        MarketBuildingSummary(value.building, catalog)
        MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius)) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Text("Bina məlumatı", color = GameTokens.Text, fontFamily = GameTypeface,
                    fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                MarketPurchaseFact("Hündürlük", "${value.building.floors} mərtəbə")
                MarketPurchaseFact("Sakinlər", value.building.residents.toString())
                Text("Qiymət", color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Label)
                CommercePrice(value.listing.price, Modifier.fillMaxWidth())
                if(own) {
                    MarketPurchaseFact("Komissiya", value.listing.fee.toString())
                    MarketPurchaseFact("Alacağın", (value.listing.price - value.listing.fee).toString())
                }
            }
        }
        if (!own) Text("Bina inventarına keçəcək; təkrar satıla bilməz.", color = GameTokens.Muted,
            fontFamily = GameTypeface, fontSize = GameTokens.Label)
    }
}

@Composable
private fun MarketPurchaseFact(label: String, value: String) {
    if (LocalDensity.current.fontScale > 1f) Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        Text(label, color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Label)
        Text(value, color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Text(label, Modifier.weight(1f), color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Label)
        Text(value, Modifier.weight(1f), color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
    }
}
