package com.aladdin.wobbrik

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight

/** Trial-specific native controls; other screens retain their approved chrome. */
internal fun Modifier.collectionPress(interaction: MutableInteractionSource, enabled: Boolean = true): Modifier = composed {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) GameTokens.InventoryCollection.PressedScale else 1f,
        label = "collection press")
    graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
internal fun CollectionTabs(page: String, onPage: (String) -> Unit) {
    val scroll = rememberScrollState()
    val font = LocalDensity.current.fontScale
    LaunchedEffect(page, scroll.maxValue) {
        val index = inventoryTabs.indexOfFirst { it.route == page }
        if (index >= 0) scroll.scrollTo(scroll.maxValue * index / maxOf(1, inventoryTabs.lastIndex))
    }
    Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.ControlRadius),
        border = BorderStroke(GameTokens.Hairline, GameTokens.InventoryCollection.Outline)) {
        BoxWithConstraints {
            val minimum = (GameTokens.Target + GameTokens.Inset) * font
            val fits = font <= 1f && maxWidth >= minimum * inventoryTabs.size
            Row(Modifier.fillMaxWidth().selectableGroup().then(if(fits) Modifier else Modifier.horizontalScroll(scroll))) {
                inventoryTabs.forEach { tab ->
                    val interaction = remember { MutableInteractionSource() }
                    val feedback = LocalGameFeedback.current
                    val selected = page == tab.route
                    Surface(onClick = { feedback(); onPage(tab.route) }, interactionSource = interaction,
                        color = Color.Transparent,
                        modifier = (if(fits) Modifier.weight(1f) else Modifier.widthIn(min = minimum))
                            .collectionPress(interaction).semantics { this.selected = selected; role = Role.Tab }) {
                        Box(Modifier.heightIn(min = GameTokens.Target).drawWithCache {
                            val inset = GameTokens.Gap.toPx()
                            val height = GameTokens.InventoryCollection.IndicatorHeight.toPx()
                            onDrawBehind {
                                if(selected) drawRect(GameTokens.Accent, Offset(inset, size.height - height),
                                    Size((size.width - inset * 2).coerceAtLeast(0f), height))
                            }
                        }) {
                            Row(Modifier.align(Alignment.Center).padding(horizontal = GameTokens.Small, vertical = GameTokens.Small),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                                MobileMenuIcon(tab.icon, Modifier.size(GameTokens.InventoryCollection.TabIcon),
                                    if(selected) GameTokens.Accent else GameTokens.Muted)
                                Text(tab.label, color = GameTokens.Text, fontFamily = GameTypeface,
                                    fontSize = GameTokens.Caption, fontWeight = FontWeight.Bold, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CollectionRefresh(enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val feedback = LocalGameFeedback.current
    // 48dp input target surrounds a smaller visual body.
    Surface(onClick = { feedback(); onClick() }, enabled = enabled, interactionSource = interaction,
        color = Color.Transparent, modifier = Modifier.size(GameTokens.Target).collectionPress(interaction, enabled)
            .semantics { contentDescription = "Yenilə" }) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(GameTokens.Target - GameTokens.Small).background(GameTokens.Surface,
                RoundedCornerShape(GameTokens.Small)).border(GameTokens.Hairline, GameTokens.Raised,
                RoundedCornerShape(GameTokens.Small)), contentAlignment = Alignment.Center) {
                Text("↻", color = if(enabled) GameTokens.Text else GameTokens.Muted, fontSize = GameTokens.Title)
            }
        }
    }
}

private data class CollectionDestination(val label: String, val route: String, val icon: MobileIcon)
private val collectionLeft = listOf(CollectionDestination("Ana", "home", MobileIcon.Home),
    CollectionDestination("Şəhər", "city", MobileIcon.City), CollectionDestination("İnventar", "inventory", MobileIcon.Inventory))
private val collectionRight = listOf(CollectionDestination("Bazar", "market", MobileIcon.Market),
    CollectionDestination("Uğurlar", "achievements", MobileIcon.Trophy), CollectionDestination("Profil", "account", MobileIcon.Account))

@Composable
internal fun CollectionNavbar(onPlay: (() -> Unit)?, onPage: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val wrap = maxWidth < GameTokens.Target * 7
        val labels = LocalDensity.current.fontScale <= 1f && !wrap
        val side = (maxWidth - GameTokens.InventoryCollection.PlayDiameter) / 2
        Surface(color = GameTokens.Surface, shape = RoundedCornerShape(GameTokens.CardRadius),
            border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
            Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.InventoryCollection.DockHeight)
                .padding(vertical = GameTokens.Quarter).selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
                CollectionNavSide(collectionLeft, labels, wrap, Modifier.width(side), onPage)
                val interaction = remember { MutableInteractionSource() }
                val feedback = LocalGameFeedback.current
                Surface(onClick = { feedback(); onPlay?.invoke() }, enabled = onPlay != null,
                    interactionSource = interaction, shape = CircleShape, color = Color.Transparent,
                    modifier = Modifier.size(GameTokens.InventoryCollection.PlayDiameter)
                        .collectionPress(interaction, onPlay != null).semantics { contentDescription = "Oyunu başlat" }) {
                    Box(Modifier.background(if(onPlay != null) Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop,
                        GameTokens.Accent, GameTokens.MobileHome.PlayBottom)) else Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Raised))),
                        contentAlignment = Alignment.Center) {
                        MobileMenuIcon(MobileIcon.Play, Modifier.size(GameTokens.Inset + GameTokens.Small), GameTokens.Background)
                    }
                }
                CollectionNavSide(collectionRight, labels, wrap, Modifier.width(side), onPage)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollectionNavSide(destinations: List<CollectionDestination>, labels: Boolean, wrap: Boolean,
    modifier: Modifier, onPage: (String) -> Unit) {
    FlowRow(modifier, maxItemsInEachRow = if(wrap) 2 else 3,
        horizontalArrangement = Arrangement.SpaceEvenly, verticalArrangement = Arrangement.Center) {
        destinations.forEach { item ->
            val selected = item.route == "inventory"
            val interaction = remember { MutableInteractionSource() }
            val feedback = LocalGameFeedback.current
            val shape = RoundedCornerShape(GameTokens.ControlRadius)
            Surface(onClick = { feedback(); onPage(item.route) }, interactionSource = interaction,
                color = if(selected) GameTokens.Raised else Color.Transparent, shape = shape,
                border = if(selected) BorderStroke(GameTokens.Hairline, GameTokens.Accent) else null,
                modifier = (if(wrap) Modifier.width(GameTokens.Target) else Modifier.weight(1f))
                    .heightIn(min = GameTokens.Target).collectionPress(interaction)
                    .semantics { this.selected = selected; contentDescription = item.label; role = Role.Tab }) {
                Column(Modifier.padding(vertical = GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                    MobileMenuIcon(item.icon, Modifier.size(GameTokens.InventoryCollection.NavIcon))
                    if(labels) Text(item.label, color = GameTokens.Text, fontFamily = GameTypeface,
                        fontSize = GameTokens.InventoryCollection.NavLabel, lineHeight = GameTokens.Caption, fontWeight = FontWeight.Bold, softWrap = false)
                }
            }
        }
    }
}
