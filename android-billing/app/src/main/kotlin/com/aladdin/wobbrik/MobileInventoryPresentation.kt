package com.aladdin.wobbrik

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.staticCompositionLocalOf
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*

internal val LocalCollectionBackdrop = staticCompositionLocalOf<ImageBitmap?> { null }

@Composable
internal fun MobileOwnedBuildingCard(building: OwnedBuilding, catalog: RecoveryCatalog?, city: CityMembership?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    val largeFont = LocalDensity.current.fontScale > 1f
    val artHeight = when {
        largeFont -> GameTokens.InventoryCollection.LargeTypeArtHeight
        mobileUiSizing().compact -> GameTokens.InventoryCollection.CompactArtHeight
        else -> GameTokens.InventoryCollection.ArtHeight
    }
    val interaction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val feedback = LocalGameFeedback.current
    Surface(onClick = { feedback(); onClick() }, interactionSource = interaction,
        modifier = Modifier.fillMaxWidth().collectionPress(interaction),
        color = Color.Transparent, shape = shape,
        border = androidx.compose.foundation.BorderStroke(GameTokens.InventoryCollection.OutlineWidth, GameTokens.InventoryCollection.Outline)) {
        Column(Modifier.background(GameTokens.InventoryCollection.PanelBottom)) {
            Box(Modifier.fillMaxWidth().padding(top = GameTokens.Quarter).height(artHeight).clipToBounds().background(Brush.verticalGradient(listOf(
                GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))) {
                LocalCollectionBackdrop.current?.let { Image(it, null, Modifier.matchParentSize().drawWithCache {
                    val fade = Brush.verticalGradient(0f to GameTokens.InventoryCollection.PanelTop,
                        .2f to Color.Transparent, .8f to Color.Transparent, 1f to GameTokens.InventoryCollection.PanelBottom)
                    onDrawWithContent { drawContent(); drawRect(fade) }
                }, contentScale = ContentScale.Crop) }
                BuildingPreview(building, Modifier.fillMaxSize().graphicsLayer {
                    scaleX = GameTokens.InventoryCollection.ArtScale; scaleY = GameTokens.InventoryCollection.ArtScale
                })
            }
            Column(Modifier.padding(start = GameTokens.InventoryCollection.CardInset, end = GameTokens.InventoryCollection.CardInset,
                top = GameTokens.Quarter, bottom = GameTokens.InventoryCollection.CardInset),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Text(buildingName(building, catalog), color = GameTokens.Text, fontFamily = GameTypeface,
                    fontSize = GameTokens.Body, fontWeight = FontWeight.Bold, lineHeight = GameTokens.Hint)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    Text("${building.floors} mərtəbə", Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                        color = GameTokens.InventoryCollection.Metadata, fontFamily = GameTypeface, fontSize = GameTokens.Caption,
                        lineHeight = GameTokens.Body, softWrap = false)
                    if(building.isInCity(city)) MobileMenuIcon(MobileIcon.City,
                        Modifier.size(GameTokens.Gap).semantics { contentDescription = "Şəhərdə" }, GameTokens.Success)
                }
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
        val placed = building.isInCity(city)
        MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius), kind = MenuSkin.Track) {
            Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                MobileMenuIcon(MobileIcon.City, Modifier.size(GameTokens.Inset),
                    if (placed) GameTokens.Success else GameTokens.Muted)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    Text("Şəhər statusu", color = GameTokens.Muted, fontFamily = GameTypeface,
                        fontSize = GameTokens.Label)
                    Text(building.cityStatus(city), color = if (placed) GameTokens.Success else GameTokens.Text,
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

/** Native light behind the real geometry; confined to the owned-detail hero. */
@Composable
private fun OwnedBuildingDetailArtwork(building: OwnedBuilding, modifier: Modifier) {
    BuildingPreview(building, modifier.drawWithCache {
        // Illustration coordinates, independent of density and font scale. The rays fade
        // before the art bounds; clipping keeps them out of the neighbouring labels.
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val light = Brush.radialGradient(listOf(GameTokens.Raised, Color.Transparent), center, radius)
        val rays = Path().apply {
            repeat(12) { ray ->
                val angle = ray * Math.PI / 6
                fun edge(a: Double) = Offset(center.x + (kotlin.math.cos(a) * radius).toFloat(),
                    center.y + (kotlin.math.sin(a) * radius).toFloat())
                val start = edge(angle)
                val end = edge(angle + Math.PI / 24)
                moveTo(center.x, center.y)
                lineTo(start.x, start.y)
                lineTo(end.x, end.y)
                close()
            }
        }
        onDrawBehind {
            clipRect {
                drawPath(rays, light)
                drawRect(light)
            }
        }
    })
}

/** Same-statement server membership wins; legacy cached/server bodies remain explicitly unknown. */
internal fun OwnedBuilding.cityStatus(city: CityMembership?): String = when {
    inCity == true -> "✓ Şəhərdə yerləşdirilib"
    inCity == false -> "İnventardadır"
    city == null -> "Şəhərə aidlik yüklənməyib"
    city.buildings[id] == revision -> "✓ Şəhərdə yerləşdirilib"
    id in city.buildings -> "Şəhər məlumatı dəyişib. Yenilə."
    else -> "İnventardadır"
}
internal fun OwnedBuilding.isInCity(city: CityMembership?): Boolean = inCity ?: (city?.buildings?.get(id) == revision)
