package com.aladdin.wobbrik

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*

@Composable
internal fun MobileOwnedBuildingCard(building: OwnedBuilding, catalog: RecoveryCatalog?, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, shape = RoundedCornerShape(GameTokens.ControlRadius)) {
        MenuRelief(Modifier.fillMaxWidth().border(GameTokens.Hairline,
            Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight, GameTokens.MobileHome.Rim)),
            RoundedCornerShape(GameTokens.ControlRadius)), shape = RoundedCornerShape(GameTokens.ControlRadius), kind = MenuSkin.Commerce) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                BuildingPreview(building, Modifier.fillMaxWidth().height(if(LocalDensity.current.fontScale > 1f)
                    GameTokens.Target else mobileUiSizing().cardArt))
                Text(buildingName(building, catalog), Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Text,
                    fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
                Text("${building.floors} mərtəbə  ›", Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Muted,
                    fontFamily = GameTypeface, fontSize = GameTokens.Caption, softWrap = false)
            }
        }
    }
}

@Composable
internal fun MobileOwnedBuildingDetail(building: OwnedBuilding, city: CityMembership?, catalog: RecoveryCatalog?, modifier: Modifier, canDelete: Boolean, onDelete: () -> Unit) {
    val largeFont = LocalDensity.current.fontScale > 1f
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        val summaryShape = RoundedCornerShape(GameTokens.ControlRadius)
        MenuRelief(Modifier.fillMaxWidth().border(GameTokens.Hairline,
            Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight, GameTokens.MobileHome.Rim)), summaryShape),
            shape = summaryShape, kind = MenuSkin.Commerce) {
            val labels: @Composable ColumnScope.() -> Unit = {
                Text(buildingName(building, catalog), Modifier.horizontalScroll(rememberScrollState()),
                    color = GameTokens.Text, fontFamily = GameTypeface, softWrap = false,
                    fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                Text("${building.floors} mərtəbə", color = GameTokens.Muted, fontFamily = GameTypeface)
                // Ownership is a read-only seal, not a disabled action.
                Surface(color = GameTokens.Success, shape = RoundedCornerShape(GameTokens.Target)) {
                    Text("✓ Sənindir", Modifier.padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter),
                        color = GameTokens.Background, fontFamily = GameTypeface, fontWeight = FontWeight.Bold)
                }
            }
            if(largeFont) Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                BuildingPreview(building, Modifier.fillMaxWidth().height(GameTokens.Target * 2))
                labels()
            } else Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                BuildingPreview(building, Modifier.weight(1f).height(mobileUiSizing().detailArt))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = labels)
            }
        }
        MenuRelief(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Text("Bina məlumatı", color = GameTokens.Text, fontFamily = GameTypeface, fontWeight = FontWeight.Bold)
                Text("${building.floors} mərtəbə · ${building.residents} sakin", color = GameTokens.Muted)
                Text(when {
                    city == null -> "Şəhərə aidlik yüklənməyib"
                    city.buildings[building.id] == building.revision -> "✓ Şəhərdə yerləşdirilib"
                    building.id in city.buildings -> "Şəhər məlumatı dəyişib. Yenilə."
                    else -> "İnventardadır"
                }, color = GameTokens.Accent)
                Text("Maksimum satış", color = GameTokens.Muted)
                CommercePrice(building.maxPrice)
                if(building.traded) Text("Alınmış bina yenidən satılmır", color = GameTokens.Muted)
                CommerceAction("Binanı sil", enabled = canDelete, onClick = onDelete)
            }
        }
    }
}
