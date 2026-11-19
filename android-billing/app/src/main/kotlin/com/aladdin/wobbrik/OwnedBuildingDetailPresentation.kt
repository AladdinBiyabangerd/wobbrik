package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import appbackend.wobbrik.game.*

@Composable
internal fun OwnedDetailTitle(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val interaction = remember { MutableInteractionSource() }
        val feedback = LocalGameFeedback.current
        Surface(onClick = { feedback(); onBack() }, interactionSource = interaction, color = Color.Transparent,
            modifier = Modifier.size(GameTokens.Target).collectionPress(interaction).semantics { contentDescription = "Geri" }) {
            Box(contentAlignment = Alignment.Center) { MobileMenuIcon(MobileIcon.Back, Modifier.size(GameTokens.Inset), GameTokens.Text) }
        }
        Text("Bina detalı", color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body,
            lineHeight = GameTokens.Hint, fontWeight = FontWeight.Bold)
    }
}

/** Real accepted DTO + existing capabilities. No preview-only mutations or extra reads. */
@Composable
internal fun MobileOwnedBuildingDetail(building: OwnedBuilding, city: CityMembership?, catalog: RecoveryCatalog?,
    modifier: Modifier, canMutate: Boolean, onDelete: () -> Unit, appearanceLabel: String,
    onAppearance: () -> Unit, onMarket: (() -> Unit)?) {
    val largeFont = LocalDensity.current.fontScale > 1f
    val heroHeight = when {
        largeFont -> GameTokens.OwnedDetail.LargeTypeHeroHeight
        mobileUiSizing().compact -> GameTokens.OwnedDetail.CompactHeroHeight
        else -> GameTokens.OwnedDetail.HeroHeight
    }
    Column(modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GameTokens.OwnedDetail.SectionGap)) {
        Box(Modifier.fillMaxWidth().height(heroHeight)) {
            LocalCollectionBackdrop.current?.let { image ->
                Image(image, null, Modifier.matchParentSize().drawWithCache {
                    val fade = Brush.verticalGradient(0f to GameTokens.InventoryCollection.Background,
                        .3f to Color.Transparent, .75f to Color.Transparent, 1f to GameTokens.InventoryCollection.Background)
                    val sides = Brush.horizontalGradient(0f to GameTokens.InventoryCollection.Background,
                        .15f to Color.Transparent, .85f to Color.Transparent, 1f to GameTokens.InventoryCollection.Background)
                    onDrawWithContent { drawContent(); drawRect(fade); drawRect(sides) }
                }, contentScale = ContentScale.Crop)
            }
            BuildingPreview(building, Modifier.fillMaxSize())
        }
        Text(buildingName(building, catalog), Modifier.fillMaxWidth(), color = GameTokens.Text,
            fontFamily = GameTypeface, fontSize = GameTokens.OwnedDetail.Title, lineHeight = GameTokens.Title,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Surface(color = GameTokens.OwnedDetail.OwnershipFill,
            border = BorderStroke(GameTokens.InventoryCollection.OutlineWidth, GameTokens.Success.copy(alpha = .35f)),
            shape = RoundedCornerShape(GameTokens.CardRadius)) {
            Text("✓ Sənindir", Modifier.padding(horizontal = GameTokens.InventoryCollection.CardInset, vertical = GameTokens.Quarter),
                color = GameTokens.Success, fontFamily = GameTypeface, fontSize = GameTokens.Caption,
                lineHeight = GameTokens.Body, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth().padding(top = GameTokens.Small), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            DetailStatistic(MobileIcon.City, building.floors.toString(), "Mərtəbə", Modifier.weight(1f), largeFont)
            DetailStatistic(MobileIcon.Residents, building.residents.toString(), "Sakin", Modifier.weight(1f), largeFont)
        }
        DetailPanel(Modifier.fillMaxWidth()) {
            val label: @Composable () -> Unit = {
                Text("Maksimum satış", color = GameTokens.Text, fontFamily = GameTypeface,
                    fontSize = GameTokens.Label, lineHeight = GameTokens.Body, fontWeight = FontWeight.Bold)
            }
            val price: @Composable () -> Unit = {
                Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Inset))
                    Text(building.maxPrice.toString(), color = GameTokens.Text, fontFamily = GameTypeface,
                        fontSize = GameTokens.Body, lineHeight = GameTokens.Hint, fontWeight = FontWeight.Bold, softWrap = false)
                }
            }
            if(largeFont) Column(Modifier.padding(GameTokens.InventoryCollection.CardInset), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                label(); price()
            } else Row(Modifier.fillMaxWidth().padding(horizontal = GameTokens.InventoryCollection.CardInset, vertical = GameTokens.Small),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                label(); price()
            }
        }
        Row(Modifier.padding(vertical = GameTokens.Quarter / 2), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            val color = if(building.isInCity(city)) GameTokens.Success else GameTokens.InventoryCollection.Metadata
            MobileMenuIcon(MobileIcon.City, Modifier.size(GameTokens.InventoryCollection.NavIcon), color)
            Text(building.cityStatus(city), Modifier.weight(1f, fill = false), color = color,
                fontFamily = GameTypeface, fontSize = GameTokens.Label, lineHeight = GameTokens.Hint)
        }
        if(building.traded) Text("Alınmış bina yenidən satılmır", color = GameTokens.Muted,
            fontFamily = GameTypeface, fontSize = GameTokens.Caption, textAlign = TextAlign.Center)
        val actions: @Composable (Modifier) -> Unit = { actionModifier ->
            DetailAction(appearanceLabel, actionModifier, canMutate, true, onAppearance)
        }
        if(largeFont) Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            actions(Modifier.fillMaxWidth())
            onMarket?.let { DetailAction("Satışa çıxar", Modifier.fillMaxWidth(), canMutate && !building.traded, false, it) }
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            actions(Modifier.weight(1f))
            onMarket?.let { DetailAction("Satışa çıxar", Modifier.weight(1f), canMutate && !building.traded, false, it) }
        }
        val interaction = remember { MutableInteractionSource() }
        val feedback = LocalGameFeedback.current
        Surface(onClick = { feedback(); onDelete() }, enabled = canMutate, interactionSource = interaction,
            color = Color.Transparent, modifier = Modifier.heightIn(min = GameTokens.Target).collectionPress(interaction, canMutate)) {
            Box(Modifier.padding(horizontal = GameTokens.Small), contentAlignment = Alignment.Center) {
                Text("Binanı sil", color = if(canMutate) GameTokens.Danger else GameTokens.Muted,
                    fontFamily = GameTypeface, fontSize = GameTokens.Label, lineHeight = GameTokens.Hint,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
            }
        }
        Spacer(Modifier.height(GameTokens.Small))
    }
}

@Composable
private fun DetailStatistic(icon: MobileIcon, value: String, label: String, modifier: Modifier, largeFont: Boolean) {
    DetailPanel(modifier) {
        Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.OwnedDetail.StatHeight)
            .padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            if(!largeFont) MobileMenuIcon(icon, Modifier.size(GameTokens.Inset),
                if(icon == MobileIcon.City) GameTokens.Accent else null)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Text(value, Modifier.horizontalScroll(rememberScrollState()), color = GameTokens.Text,
                    fontFamily = GameTypeface, fontSize = GameTokens.Hint, lineHeight = GameTokens.Hint,
                    fontWeight = FontWeight.Bold, softWrap = false)
                Text(label, color = GameTokens.InventoryCollection.Metadata, fontFamily = GameTypeface,
                    fontSize = GameTokens.Caption, lineHeight = GameTokens.Body)
            }
        }
    }
}

@Composable
private fun DetailPanel(modifier: Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Surface(modifier = modifier, color = Color.Transparent, shape = shape,
        border = BorderStroke(GameTokens.InventoryCollection.OutlineWidth, GameTokens.InventoryCollection.Outline)) {
        Box(Modifier.background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop,
            GameTokens.InventoryCollection.PanelBottom))), propagateMinConstraints = true) { content() }
    }
}

@Composable
private fun DetailAction(label: String, modifier: Modifier, enabled: Boolean, primary: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    val interaction = remember { MutableInteractionSource() }
    val feedback = LocalGameFeedback.current
    Surface(onClick = { feedback(); onClick() }, enabled = enabled, interactionSource = interaction,
        color = Color.Transparent, shape = shape, modifier = modifier.heightIn(min = GameTokens.OwnedDetail.ActionHeight)
            .collectionPress(interaction, enabled)) {
        val fill = if(primary && enabled) listOf(GameTokens.MobileHome.PlayTop,GameTokens.Accent,GameTokens.MobileHome.PlayBottom)
            else listOf(GameTokens.InventoryCollection.PanelTop,GameTokens.InventoryCollection.PanelBottom)
        Box(Modifier.background(Brush.verticalGradient(fill)).then(if(!primary) Modifier.border(GameTokens.InventoryCollection.OutlineWidth,
            if(enabled) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape) else Modifier)
            .padding(GameTokens.Small), contentAlignment = Alignment.Center) {
            Text(label, color = if(!enabled) GameTokens.Muted else if(primary) GameTokens.Background else GameTokens.Text,
                fontFamily = GameTypeface, fontSize = GameTokens.Label, lineHeight = GameTokens.Hint,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
