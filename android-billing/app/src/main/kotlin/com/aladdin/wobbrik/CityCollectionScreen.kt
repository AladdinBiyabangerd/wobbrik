package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import appbackend.wobbrik.game.*

internal val LocalCityCollection = staticCompositionLocalOf { false }

/** Shared inventory header and a reserved, safe-area dock. No offscreen card art decode. */
@Composable
internal fun CityCollectionShell(coins: Long?, onPlay: (() -> Unit)?, onPage: (String) -> Unit,
    content: @Composable () -> Unit) {
    val scenery = rememberResourceArt(R.drawable.inventory_collection_header).first.images?.singleOrNull()
    Column(Modifier.fillMaxSize().background(GameTokens.InventoryCollection.Background)
        .safeDrawingPadding().consumeWindowInsets(WindowInsets.safeDrawing).imePadding()) {
        CollectionHeader(coins, scenery)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            CompositionLocalProvider(LocalMobileMenuShell provides true, LocalCityCollection provides true) { content() }
        }
        Box(Modifier.padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter)) {
            CollectionNavbar(onPlay, onPage, page = "city")
        }
    }
}

@Composable
internal fun CityCollectionContent(scene: CityScene?, failed: Boolean, cached: Boolean,
    building: OwnedBuilding?, catalog: RecoveryCatalog?, focused: Boolean,
    onRetry: () -> Unit, onInventory: () -> Unit, onFit: () -> Unit, onFocus: () -> Unit,
    sceneContent: @Composable () -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    Column(Modifier.fillMaxSize().padding(horizontal = GameTokens.InventoryCollection.ScreenInset),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Şəhərim", Modifier.weight(1f), color = GameTokens.Text, fontFamily = GameTypeface,
                fontSize = GameTokens.OwnedDetail.Title, fontWeight = FontWeight.ExtraBold)
            CollectionRefresh(scene != null || failed, onRetry)
        }
        CityCollectionFrame(Modifier.weight(1f).fillMaxWidth()) {
            when {
                failed -> CityCollectionMessage("Şəhər yüklənmədi", "Yenidən cəhd et", onRetry)
                scene == null -> CityCollectionMessage("Şəhər yüklənir…")
                else -> {
                    Box(Modifier.fillMaxSize().padding(start = GameTokens.Small, end = GameTokens.Small,
                        top = if(cached) { if(large) GameTokens.Target else GameTokens.Inset } else GameTokens.Small,
                        bottom = GameTokens.Target + GameTokens.Small)) { sceneContent() }
                    if(cached) Text("Keş · yalnız baxış", Modifier.align(Alignment.TopCenter).padding(GameTokens.Small)
                        .semantics { liveRegion = LiveRegionMode.Polite }, color = GameTokens.InventoryCollection.Metadata,
                        fontFamily = GameTypeface, fontSize = GameTokens.Caption)
                    if(scene.buildings.isEmpty()) CityCollectionMessage("Şəhərdə bina yoxdur")
                    else if(building == null) CityCollectionMessage("Binaların həndəsəsi və ya qrafikası mövcud deyil")
                    Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(GameTokens.Small),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        CityCollectionCamera(false, false, scene.parcels.isNotEmpty() || building != null, onFit)
                        CityCollectionCamera(true, focused, building != null, onFocus)
                    }
                }
            }
        }
        if(scene != null && !failed) CityCollectionFrame(Modifier.fillMaxWidth()) {
            val action = if(building == null) "İnventara keç" else if(focused) "Şəhərə qayıt" else "Binaya bax"
            val click = if(building == null) onInventory else onFocus
            if(large) Column(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Column(Modifier.fillMaxWidth().heightIn(max = GameTokens.Target * 2).verticalScroll(rememberScrollState())) {
                    CityCollectionSummary(building,catalog)
                }
                CityCollectionAction(action,click,Modifier.fillMaxWidth())
            } else Row(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if(building != null) BuildingPreview(building,Modifier.size(GameTokens.Target + GameTokens.Small))
                Column(Modifier.weight(1f)) { CityCollectionSummary(building,catalog) }
                CityCollectionAction(action,click,Modifier.widthIn(max = GameTokens.Target * 3))
            }
        }
        Spacer(Modifier.height(GameTokens.Quarter))
    }
}

@Composable
private fun CityCollectionSummary(building: OwnedBuilding?, catalog: RecoveryCatalog?) {
    Text(building?.let { buildingName(it,catalog) } ?: "Hələ bina seçilməyib",color = GameTokens.Text,
        fontFamily = GameTypeface,fontSize = GameTokens.Body,fontWeight = FontWeight.Bold)
    if(building != null) Text("${building.floors} mərtəbə · ${building.residents} sakin",color = GameTokens.InventoryCollection.Metadata,
        fontFamily = GameTypeface,fontSize = GameTokens.Caption)
}

@Composable
internal fun CityCollectionFrame(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    Surface(modifier,color = Color.Transparent,shape = RoundedCornerShape(GameTokens.CardRadius),
        border = BorderStroke(GameTokens.InventoryCollection.OutlineWidth,GameTokens.InventoryCollection.Outline)) {
        Box(Modifier.background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop,
            GameTokens.InventoryCollection.PanelBottom))),content = content)
    }
}

@Composable
private fun BoxScope.CityCollectionMessage(message: String, action: String? = null, onClick: () -> Unit = {}) {
    Column(Modifier.align(Alignment.Center).fillMaxWidth().verticalScroll(rememberScrollState()).padding(GameTokens.Gap),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Small),horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message,Modifier.semantics { liveRegion = LiveRegionMode.Polite },color = GameTokens.Text,
            fontFamily = GameTypeface,fontSize = GameTokens.Body,textAlign = TextAlign.Center)
        if(action != null) CityCollectionAction(action,onClick,Modifier.fillMaxWidth())
    }
}

@Composable
private fun CityCollectionAction(label: String, onClick: () -> Unit, modifier: Modifier) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val feedback = LocalGameFeedback.current
    Surface(onClick = { feedback(); onClick() },interactionSource = interaction,
        modifier = modifier.heightIn(min = GameTokens.Target).collectionPress(interaction),
        shape = RoundedCornerShape(GameTokens.ControlRadius),color = GameTokens.InventoryCollection.Selected,
        border = BorderStroke(GameTokens.Hairline,GameTokens.Accent)) {
        Box(Modifier.padding(horizontal = GameTokens.Small,vertical = GameTokens.Quarter),contentAlignment = Alignment.Center) {
            Text(label,color = GameTokens.Text,fontFamily = GameTypeface,fontSize = GameTokens.Label,
                fontWeight = FontWeight.Bold,textAlign = TextAlign.Center)
        }
    }
}

/** Native fit brackets and focus target, with minimum48dp input targets. */
@Composable
private fun CityCollectionCamera(focus: Boolean, active: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val feedback = LocalGameFeedback.current
    Surface(onClick = { feedback(); onClick() },enabled = enabled,interactionSource = interaction,
        modifier = Modifier.size(GameTokens.Target).collectionPress(interaction,enabled).semantics {
            contentDescription = if(focus) { if(active) "Şəhər görünüşünə qayıt" else "Binanı böyüt" } else "Kameranı sıfırla"
            if(focus) selected = active
        },shape = RoundedCornerShape(GameTokens.ControlRadius),color = GameTokens.InventoryCollection.PanelBottom,
        border = BorderStroke(GameTokens.InventoryCollection.OutlineWidth,if(active) GameTokens.Accent else GameTokens.InventoryCollection.Outline)) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(GameTokens.Inset)) {
                val ink = if(enabled) GameTokens.Text else GameTokens.Muted
                val stroke = GameTokens.Hairline.toPx()*2
                if(focus) {
                    drawCircle(ink,size.width/3,style = Stroke(stroke));drawCircle(ink,stroke)
                    listOf(Offset(0f,size.height/2) to Offset(size.width/4,size.height/2),
                        Offset(size.width*3/4,size.height/2) to Offset(size.width,size.height/2),
                        Offset(size.width/2,0f) to Offset(size.width/2,size.height/4),
                        Offset(size.width/2,size.height*3/4) to Offset(size.width/2,size.height)).forEach { (a,b) -> drawLine(ink,a,b,stroke,StrokeCap.Round) }
                } else listOf(0f to 0f,size.width to 0f,0f to size.height,size.width to size.height).forEach { (x,y) ->
                    drawLine(ink,Offset(x,y),Offset(if(x==0f) size.width/4 else size.width*3/4,y),stroke,StrokeCap.Round)
                    drawLine(ink,Offset(x,y),Offset(x,if(y==0f) size.height/4 else size.height*3/4),stroke,StrokeCap.Round)
                }
            }
        }
    }
}
