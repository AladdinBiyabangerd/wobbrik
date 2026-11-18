package com.aladdin.wobbrik

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
