package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight

/** Collection chrome without inventory tabs, inventory reads or another portrait decoder. */
@Composable
internal fun ProfileCollectionShell(coins: Long?, onPlay: (() -> Unit)?, onPage: (String) -> Unit, content: @Composable () -> Unit) {
    val scenery=rememberResourceArt(R.drawable.inventory_collection_header).first.images?.singleOrNull()
    val ime=WindowInsets.ime.getBottom(LocalDensity.current)>0
    Column(Modifier.fillMaxSize().background(GameTokens.InventoryCollection.Background)
        .safeDrawingPadding().consumeWindowInsets(WindowInsets.safeDrawing).imePadding()) {
        CollectionHeader(coins,scenery)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            CompositionLocalProvider(LocalMobileMenuShell provides true) { content() }
        }
        if(!ime) Box(Modifier.padding(horizontal=GameTokens.Small,vertical=GameTokens.Quarter)) {
            CollectionNavbar(onPlay,onPage,"account")
        }
    }
}

@Composable
internal fun ProfileCollectionPanel(modifier: Modifier=Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape=RoundedCornerShape(GameTokens.ControlRadius)
    Column(modifier.fillMaxWidth().clip(shape)
        .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop,GameTokens.InventoryCollection.DockBottom)))
        .drawWithCache {
            val rim = Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight, GameTokens.MobileHome.Rim, GameTokens.InventoryCollection.Outline))
            val sheen = Brush.radialGradient(listOf(GameTokens.MobileHome.Rim.copy(alpha=.18f), GameTokens.MobileHome.Rim.copy(alpha=0f)),
                center=Offset(size.width*.18f,0f),radius=size.width)
            val stroke=GameTokens.Hairline.toPx()
            val radius=GameTokens.ControlRadius.toPx()
            onDrawBehind {
                drawRect(sheen)
                drawRoundRect(rim, topLeft=Offset(stroke/2,stroke/2),
                    size=androidx.compose.ui.geometry.Size(size.width-stroke,size.height-stroke),
                    cornerRadius=CornerRadius(radius),style=Stroke(stroke))
            }
        },content=content)
}

@Composable
internal fun ProfileCollectionHeading(onPage: (String) -> Unit) {
    val large=LocalDensity.current.fontScale>1f
    @Composable fun settings() {
        val interaction=remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        androidx.compose.material3.Surface(onClick={onPage("settings")},interactionSource=interaction,
            color=androidx.compose.ui.graphics.Color.Transparent,
            modifier=Modifier.heightIn(min=GameTokens.Target).collectionPress(interaction)) {
            val shape=RoundedCornerShape(GameTokens.CardRadius)
            Row(Modifier.padding(vertical=GameTokens.Quarter).clip(shape)
                .background(GameTokens.InventoryCollection.PanelBottom).border(GameTokens.Hairline,GameTokens.Accent,shape)
                .padding(GameTokens.Small),verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(GameTokens.Quarter)) {
                Canvas(Modifier.size(GameTokens.Inset)) {
                    val center=androidx.compose.ui.geometry.Offset(size.width/2,size.height/2)
                    for(i in 0..7) {
                        val angle=Math.PI*i/4
                        val d=androidx.compose.ui.geometry.Offset(kotlin.math.cos(angle).toFloat(),kotlin.math.sin(angle).toFloat())
                        drawLine(GameTokens.Muted,center+d*(size.width*.28f),center+d*(size.width*.47f),GameTokens.Quarter.toPx())
                    }
                    drawCircle(GameTokens.Muted,size.width*.34f)
                    drawCircle(GameTokens.InventoryCollection.PanelBottom,size.width*.15f)
                }
                Text("Parametrlər",color=GameTokens.Text,fontSize=GameTokens.Label,fontWeight=FontWeight.Bold)
            }
        }
    }
    if(large) Column(Modifier.fillMaxWidth()) {
        Text("Profil",color=GameTokens.Text,fontSize=GameTokens.Title,fontWeight=FontWeight.ExtraBold)
        settings()
    } else Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
        Text("Profil",Modifier.weight(1f),color=GameTokens.Text,fontSize=GameTokens.Title,fontWeight=FontWeight.ExtraBold)
        settings()
    }
}
