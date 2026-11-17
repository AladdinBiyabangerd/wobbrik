package com.aladdin.wobbrik

import androidx.compose.foundation.background
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
internal fun MobileOwnedBuildingCard(building: OwnedBuilding, catalog: RecoveryCatalog?, city: CityMembership?, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, shape = RoundedCornerShape(GameTokens.ControlRadius)) {
        MenuRelief(Modifier.fillMaxWidth().border(GameTokens.Hairline,
            Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight, GameTokens.MobileHome.Rim)),
            RoundedCornerShape(GameTokens.ControlRadius)), shape = RoundedCornerShape(GameTokens.ControlRadius), kind = MenuSkin.Commerce) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                val largeFont = LocalDensity.current.fontScale > 1f
                val inCity = city?.buildings?.get(building.id) == building.revision
                Box(Modifier.fillMaxWidth()) {
                    BuildingPreview(building, Modifier.fillMaxWidth()
                        .then(if(!largeFont) Modifier.padding(top = GameTokens.Inset + GameTokens.Small) else Modifier)
                        .height(if(largeFont) GameTokens.Target else mobileUiSizing().cardArt))
                    if(inCity && !largeFont) OwnedCitySeal(Modifier.align(Alignment.TopEnd))
                }
                // Large type gets its own natural-height row, keeping the art and label unobscured.
                if(inCity && largeFont) OwnedCitySeal()
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
                OwnedBuildingDetailArtwork(building, Modifier.fillMaxWidth().height(GameTokens.Target * 2))
                labels()
            } else Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                OwnedBuildingDetailArtwork(building, Modifier.weight(1f).height(mobileUiSizing().detailArt))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Small), content = labels)
            }
        }
        val informationShape = RoundedCornerShape(GameTokens.ControlRadius)
        MenuRelief(Modifier.fillMaxWidth().border(GameTokens.Hairline,
            Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight, GameTokens.MobileHome.Rim)), informationShape),
            shape = informationShape, kind = MenuSkin.Commerce) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    Surface(color = GameTokens.Text, shape = RoundedCornerShape(GameTokens.Target)) {
                        Box(Modifier.size(GameTokens.Inset * LocalDensity.current.fontScale), contentAlignment = Alignment.Center) {
                            Text("i", color = GameTokens.Background, fontFamily = GameTypeface, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Bina məlumatı", color = GameTokens.Text, fontFamily = GameTypeface,
                        fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                }
                MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.Small), kind = MenuSkin.Track) {
                    Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        // The contract has no building level. Reference sample numbers do not create one.
                        BuildingInformationRow(MobileIcon.Layers, "Hündürlük", "${building.floors} mərtəbə")
                        BuildingInformationRow(MobileIcon.City, "Bina növü", buildingName(building, catalog))
                        BuildingInformationRow(MobileIcon.Account, "Sakinlər", building.residents.toString())
                    }
                }
            }
        }
        val placed = city?.buildings?.get(building.id) == building.revision
        MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius), kind = MenuSkin.Track) {
            Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                MobileMenuIcon(MobileIcon.City, Modifier.size(GameTokens.Inset),
                    if (placed) GameTokens.Success else GameTokens.Muted)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    Text("Şəhər statusu", color = GameTokens.Muted, fontFamily = GameTypeface,
                        fontSize = GameTokens.Label)
                    Text(when {
                        city == null -> "Şəhərə aidlik yüklənməyib"
                        placed -> "✓ Şəhərdə yerləşdirilib"
                        building.id in city.buildings -> "Şəhər məlumatı dəyişib. Yenilə."
                        else -> "İnventardadır"
                    }, color = if (placed) GameTokens.Success else GameTokens.Text,
                        fontFamily = GameTypeface, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
                }
            }
        }
        MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius), kind = MenuSkin.Commerce) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                val priceLabel: @Composable () -> Unit = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        MobileMenuIcon(MobileIcon.Tag, Modifier.size(GameTokens.Inset), GameTokens.Muted)
                        Text("Maksimum satış", color = GameTokens.Text, fontFamily = GameTypeface,
                            fontSize = GameTokens.Label, fontWeight = FontWeight.Bold)
                    }
                }
                if (largeFont) {
                    priceLabel()
                    CommercePrice(building.maxPrice)
                } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    Box(Modifier.weight(1f)) { priceLabel() }
                    CommercePrice(building.maxPrice, Modifier.weight(1f))
                }
                if(building.traded) Text("Alınmış bina yenidən satılmır", color = GameTokens.Muted,
                    fontFamily = GameTypeface, fontSize = GameTokens.Label)
            }
        }
        CommerceAction("Binanı sil", enabled = canDelete, destructive = true, onClick = onDelete)

    }
}

/** A natural-height row; large type stacks the value rather than squeezing either column. */
@Composable
private fun BuildingInformationRow(icon: MobileIcon, label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        MobileMenuIcon(icon, Modifier.size(GameTokens.Inset), GameTokens.Muted)
        if (LocalDensity.current.fontScale > 1f) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Text(label, color = GameTokens.Muted, fontFamily = GameTypeface, fontSize = GameTokens.Label)
                Text(value, Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Text,
                    fontFamily = GameTypeface, fontSize = GameTokens.Body, softWrap = false)
            }
        } else {
            Text(label, Modifier.weight(1f), color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Label)
            Text(value, Modifier.weight(1f).horizontalScroll(rememberScrollState()), color = GameTokens.Text,
                fontFamily = GameTypeface, fontSize = GameTokens.Label, softWrap = false,
                textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
    }
}

/** Read-only membership seal: only an exact accepted city revision earns it. */
@Composable
private fun OwnedCitySeal(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = GameTokens.Success, shape = RoundedCornerShape(GameTokens.Target)) {
        Text("✓ Şəhərdə", Modifier.padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter),
            color = GameTokens.Background, fontFamily = GameTypeface, fontSize = GameTokens.Caption,
            fontWeight = FontWeight.Bold)
    }
}

/** Native light behind the real geometry; confined to the owned-detail hero. */
@Composable
private fun OwnedBuildingDetailArtwork(building: OwnedBuilding, modifier: Modifier) {
    BuildingPreview(building, modifier.background(Brush.radialGradient(
        listOf(GameTokens.Raised, Color.Transparent))))
}
