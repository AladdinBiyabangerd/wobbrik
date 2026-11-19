package com.aladdin.wobbrik

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

/** Native collection surfaces; no baked text, controls or sample economy. */
@Composable
internal fun MarketCollectionPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Column(modifier.fillMaxWidth().clip(shape)
        .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))
        .border(GameTokens.InventoryCollection.OutlineWidth, GameTokens.InventoryCollection.Outline, shape), content = content)
}

/** The visible pill sits inside a minimum48dp input region. */
@Composable
internal fun MarketCollectionAction(label: String, modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true, icon: MobileIcon? = null, onClick: () -> Unit) {
    val feedback = LocalGameFeedback.current
    Surface(onClick = { feedback(); onClick() }, enabled = enabled, color = Color.Transparent,
        modifier = modifier.heightIn(min = GameTokens.Target)) {
        Box(Modifier.fillMaxWidth().padding(vertical = GameTokens.Small), contentAlignment = Alignment.Center) {
            val shape = RoundedCornerShape(GameTokens.CardRadius)
            Row(Modifier.fillMaxWidth().clip(shape)
                .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.DockBottom)))
                .border(GameTokens.Hairline, if(enabled) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape)
                .padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterHorizontally)) {
                icon?.let { MobileMenuIcon(it, Modifier.size(GameTokens.Gap), if(enabled) GameTokens.Text else GameTokens.Muted) }
                Text(label, Modifier.weight(1f, fill = false), color = if(enabled) GameTokens.Text else GameTokens.Muted,
                    fontFamily = GameTypeface, fontSize = GameTokens.Label, fontWeight = FontWeight.Bold)
                if(label == "Bax") Text("→", color = GameTokens.Text, fontSize = GameTokens.Label)
            }
        }
    }
}

@Composable
internal fun MarketCollectionHeading(label: String, loading: Boolean, onRefresh: () -> Unit, onFilter: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = GameTokens.Text, fontSize = GameTokens.OwnedDetail.Title,
            fontFamily = GameTypeface, fontWeight = FontWeight.ExtraBold)
        onFilter?.let { action ->
            Surface(onClick = action, color = Color.Transparent,
                modifier = Modifier.size(GameTokens.Target).semantics { contentDescription = "Süzgəc" }) {
                Box(contentAlignment = Alignment.Center) {
                    val shape = RoundedCornerShape(GameTokens.Small)
                    Box(Modifier.size(GameTokens.InventoryCollection.RefreshBody).clip(shape)
                        .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))
                        .border(GameTokens.InventoryCollection.OutlineWidth, GameTokens.InventoryCollection.Outline, shape)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Canvas(Modifier.size(GameTokens.Gap)) {
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f,0f); lineTo(size.width,0f); lineTo(size.width*.6f,size.height*.55f)
                                    lineTo(size.width*.6f,size.height); lineTo(size.width*.4f,size.height)
                                    lineTo(size.width*.4f,size.height*.55f); close()
                                }
                                drawPath(path, GameTokens.Text, style = androidx.compose.ui.graphics.drawscope.Stroke(GameTokens.Hairline.toPx()))
                            }
                        }
                    }
                }
            }
        }
        CollectionRefresh(!loading, onRefresh)
    }
}
