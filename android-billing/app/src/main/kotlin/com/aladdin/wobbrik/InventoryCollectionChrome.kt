package com.aladdin.wobbrik

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight

internal val LocalCollectionLayout = staticCompositionLocalOf { false }
internal val LocalCollectionDockClearance = staticCompositionLocalOf { GameTokens.InventoryCollection.DockClearance }

/** Inventory list/detail keep the same content slot: chrome changes never remount the read owner. */
@Composable
internal fun CollectionInventoryShell(coins: Long?, onPlay: (() -> Unit)?, onPage: (String) -> Unit,
    detail: Boolean, reportDetail: (Boolean) -> Unit, page: String = "inventory", content: @Composable () -> Unit) {
    val art = rememberResourceArt(R.drawable.inventory_collection_backdrop, R.drawable.inventory_collection_header).first.images
    BoxWithConstraints(Modifier.fillMaxSize().background(GameTokens.InventoryCollection.Background)
        .safeDrawingPadding().consumeWindowInsets(WindowInsets.safeDrawing).imePadding()) {
        val wrappedCollectionDock = maxWidth < (GameTokens.Target * 7 + GameTokens.Small * 2)
        val collectionClearance = if(wrappedCollectionDock) GameTokens.Target * 2 + GameTokens.Gap else GameTokens.InventoryCollection.DockClearance
        art?.getOrNull(1)?.let { Image(it, null,
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(GameTokens.Target * 2),
            contentScale = ContentScale.FillBounds) }
        Column(Modifier.fillMaxSize()) {
            CollectionHeader(coins, art?.getOrNull(1))
            if(!detail) Box(Modifier.padding(horizontal = GameTokens.InventoryCollection.ScreenInset)) {
                CollectionTabs(if(page == "market-own") "market" else page, onPage, if(mobileMenuDestination(page) == "market") marketTabs else inventoryTabs, compactLargeType = mobileMenuDestination(page) == "market")
            }
            Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().then(if(detail)
                Modifier.padding(bottom = collectionClearance) else Modifier)) {
                CompositionLocalProvider(LocalMobileMenuShell provides true, LocalCommerceDetail provides reportDetail,
                    LocalCollectionLayout provides !detail, LocalCollectionDockClearance provides collectionClearance,
                    LocalCollectionBackdrop provides art?.firstOrNull(),
                    LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = GameTypeface)) { content() }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter)) {
            CollectionNavbar(onPlay, onPage, mobileMenuDestination(page))
        }
    }
}

@Composable
internal fun CollectionHeader(coins: Long?, scenery: androidx.compose.ui.graphics.ImageBitmap?) {
    val (art, retry) = LocalMenuLogo.current ?: rememberResourceArt(R.drawable.wobbrik_logo)
    val logo = art.images?.singleOrNull()
    val longBalance = (coins?.toString()?.length ?: 1) > 6
    Box(Modifier.fillMaxWidth()) {
        scenery?.let { Image(it, null, Modifier.matchParentSize().graphicsLayer {
            scaleY = 2f; translationY = size.height / 2f
        }, contentScale = ContentScale.FillBounds) }
        Column(Modifier.fillMaxWidth().padding(horizontal = GameTokens.Inset),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
            Row(Modifier.fillMaxWidth().height(GameTokens.InventoryCollection.HeaderHeight).padding(top = GameTokens.Gap),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(GameTokens.InventoryCollection.HeaderLogoWidth).height(GameTokens.InventoryCollection.HeaderLogoHeight)) {
                    if(logo != null) {
                        val painter = remember(logo) { BitmapPainter(logo,
                            IntOffset(GameTokens.InventoryCollection.LogoSourceX, GameTokens.InventoryCollection.LogoSourceY),
                            IntSize(GameTokens.InventoryCollection.LogoSourceWidth, GameTokens.InventoryCollection.LogoSourceHeight)) }
                        Image(painter, "Wobbrik", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                    else if(art.failed) TextButton(onClick = retry) { Text("Wobbrik", color = GameTokens.Text) }
                }
                if(!longBalance) CollectionWallet(coins)
            }
            if(longBalance) Box(Modifier.fillMaxWidth().padding(bottom = GameTokens.Small), contentAlignment = Alignment.CenterEnd) {
                CollectionWallet(coins)
            }
        }
    }
}

@Composable
private fun CollectionWallet(coins: Long?) {
    val shape = RoundedCornerShape(GameTokens.CardRadius)
    Row(Modifier.clip(shape).background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelBottom,
        GameTokens.InventoryCollection.DockBottom))).border(GameTokens.InventoryCollection.OutlineWidth,
        GameTokens.InventoryCollection.Outline, shape).padding(horizontal = GameTokens.InventoryCollection.CardInset,
        vertical = GameTokens.Quarter), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
        MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.InventoryCollection.WalletCoin))
        Text(coins?.toString() ?: "—", Modifier.weight(1f, fill = false).horizontalScroll(rememberScrollState()),
            color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Label,
            lineHeight = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
    }
}

internal fun Modifier.collectionPress(interaction: MutableInteractionSource, enabled: Boolean = true): Modifier = composed {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) GameTokens.InventoryCollection.PressedScale else 1f,
        label = "collection press")
    graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
internal fun CollectionTabs(page: String, onPage: (String) -> Unit, tabs: List<CommerceTab> = inventoryTabs, compactLargeType: Boolean = false) {
    val scroll = rememberScrollState()
    val font = LocalDensity.current.fontScale
    LaunchedEffect(page, scroll.maxValue) {
        val index = tabs.indexOfFirst { it.route == page }
        if (index >= 0) scroll.scrollTo(scroll.maxValue * index / maxOf(1, tabs.lastIndex))
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val minimum = (GameTokens.Target + GameTokens.Inset) * font
        val fits = font <= 1f && maxWidth >= minimum * tabs.size
        val shape = RoundedCornerShape(if(compactLargeType) GameTokens.Small else GameTokens.ControlRadius)
        val visualHeight = if(font > 1f && !compactLargeType) GameTokens.Target * font else GameTokens.InventoryCollection.TabVisualHeight
        // Visual strip is smaller than its48dp input row; extra space is above the strip.
        Box(Modifier.matchParentSize().padding(top = if(font > 1f) GameTokens.Quarter else GameTokens.Small + GameTokens.Quarter)
            .clip(shape).background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))
            .border(GameTokens.InventoryCollection.OutlineWidth, GameTokens.InventoryCollection.Outline, shape))
        Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target).selectableGroup()
            .then(if(fits) Modifier else Modifier.horizontalScroll(scroll))) {
            tabs.forEachIndexed { index, tab ->
                val interaction = remember { MutableInteractionSource() }
                val feedback = LocalGameFeedback.current
                val selected = page == tab.route
                Surface(onClick = { feedback(); onPage(tab.route) }, interactionSource = interaction,
                    color = Color.Transparent,
                    modifier = (if(fits) Modifier.weight(1f) else Modifier.widthIn(min = minimum))
                        .collectionPress(interaction).semantics { this.selected = selected; role = Role.Tab }) {
                    Box(Modifier.heightIn(min = GameTokens.Target).padding(top = if(font > 1f) GameTokens.Quarter else GameTokens.Small + GameTokens.Quarter)
                        .heightIn(min = visualHeight).drawWithCache {
                            val inset = GameTokens.Gap.toPx()
                            val height = GameTokens.InventoryCollection.IndicatorHeight.toPx()
                            onDrawBehind {
                                if(selected) drawRoundRect(GameTokens.Accent, Offset(inset, size.height - height),
                                    Size((size.width-inset*2).coerceAtLeast(0f),height), androidx.compose.ui.geometry.CornerRadius(height))
                                if(index > 0) drawLine(GameTokens.InventoryCollection.Outline, Offset(0f,GameTokens.Small.toPx()),
                                    Offset(0f,size.height-GameTokens.Small.toPx()), GameTokens.InventoryCollection.OutlineWidth.toPx())
                            }
                        }) {
                        Row(Modifier.align(Alignment.Center).padding(horizontal = GameTokens.Small, vertical = GameTokens.Quarter),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                            MobileMenuIcon(tab.icon, Modifier.size(GameTokens.InventoryCollection.TabIcon),
                                if(selected) GameTokens.Accent else GameTokens.Text)
                            Text(tab.label, color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Caption,
                                lineHeight = GameTokens.Body, fontWeight = FontWeight.Bold, softWrap = false)
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
    Surface(onClick = { feedback(); onClick() }, enabled = enabled, interactionSource = interaction,
        color = Color.Transparent, modifier = Modifier.size(GameTokens.Target).collectionPress(interaction, enabled)
            .semantics { contentDescription = "Yenilə" }) {
        Box(contentAlignment = Alignment.Center) {
            val shape = RoundedCornerShape(GameTokens.Small)
            Box(Modifier.size(GameTokens.InventoryCollection.RefreshBody).clip(shape)
                .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop,GameTokens.InventoryCollection.PanelBottom)))
                .border(GameTokens.InventoryCollection.OutlineWidth, GameTokens.InventoryCollection.Outline, shape), contentAlignment = Alignment.Center) {
                MobileMenuIcon(MobileIcon.Refresh, Modifier.size(GameTokens.InventoryCollection.NavIcon),
                    if(enabled) GameTokens.Text else GameTokens.Muted)
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
internal fun CollectionNavbar(onPlay: (() -> Unit)?, onPage: (String) -> Unit, page: String = "inventory", active: Boolean = false) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val wrap = maxWidth < GameTokens.Target * 7
        val labels = LocalDensity.current.fontScale <= 1f && !wrap
        val side = (maxWidth - GameTokens.Target) / 2
        val shape = RoundedCornerShape(GameTokens.InventoryCollection.DockRadius)
        Row(Modifier.fillMaxWidth().clip(shape).background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop,
            GameTokens.InventoryCollection.DockBottom))).border(GameTokens.InventoryCollection.OutlineWidth,
            GameTokens.InventoryCollection.Outline, shape).heightIn(min = GameTokens.InventoryCollection.DockHeight)
            .padding(vertical = GameTokens.Quarter).selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
            CollectionNavSide(collectionLeft, page, labels, wrap, Modifier.width(side), onPage)
            val interaction = remember { MutableInteractionSource() }
            val feedback = LocalGameFeedback.current
            Surface(onClick = { feedback(); onPlay?.invoke() }, enabled = onPlay != null, interactionSource = interaction,
                color = Color.Transparent, modifier = Modifier.size(GameTokens.Target).collectionPress(interaction, onPlay != null)
                    .semantics { contentDescription = if(active) "Cəhdə qayıt" else "Oyunu başlat" }) {
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(GameTokens.InventoryCollection.PlayDiameter).clip(CircleShape)
                        .background(Brush.verticalGradient(if(onPlay != null) listOf(GameTokens.MobileHome.PlayTop,GameTokens.Accent,
                            GameTokens.MobileHome.PlayBottom) else listOf(GameTokens.Raised,GameTokens.Raised))), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.matchParentSize()) {
                            val inset = GameTokens.Quarter.toPx()
                            drawArc(GameTokens.Text,210f,42f,false,Offset(inset,inset),Size(size.width-inset*2,size.height-inset*2),
                                style = Stroke(GameTokens.Hairline.toPx(),cap=StrokeCap.Round))
                        }
                        MobileMenuIcon(MobileIcon.Play, Modifier.size(GameTokens.Gap + GameTokens.Quarter), GameTokens.Background)
                    }
                }
            }
            CollectionNavSide(collectionRight, page, labels, wrap, Modifier.width(side), onPage)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollectionNavSide(destinations: List<CollectionDestination>, page: String, labels: Boolean, wrap: Boolean,
    modifier: Modifier, onPage: (String) -> Unit) {
    FlowRow(modifier, maxItemsInEachRow = if(wrap) 2 else 3,
        horizontalArrangement = Arrangement.SpaceEvenly, verticalArrangement = Arrangement.Center) {
        destinations.forEach { item ->
            val selected = item.route == page
            val interaction = remember { MutableInteractionSource() }
            val feedback = LocalGameFeedback.current
            val shape = RoundedCornerShape(GameTokens.Small + GameTokens.Quarter)
            Surface(onClick = { feedback(); onPage(item.route) }, interactionSource = interaction,
                color = Color.Transparent, modifier = (if(wrap) Modifier.width(GameTokens.Target) else Modifier.weight(1f))
                    .heightIn(min = GameTokens.Target).collectionPress(interaction)
                    .semantics { this.selected = selected; contentDescription = item.label; role = Role.Tab }) {
                Box(contentAlignment = Alignment.Center) {
                    Column(Modifier.widthIn(min = GameTokens.Target).clip(shape)
                        .then(if(selected) Modifier.background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.Selected,
                            GameTokens.InventoryCollection.DockBottom))).border(GameTokens.InventoryCollection.OutlineWidth,GameTokens.Accent,shape) else Modifier)
                        .padding(vertical = GameTokens.Quarter), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        MobileMenuIcon(item.icon, Modifier.size(GameTokens.InventoryCollection.NavIcon))
                        if(labels) Text(item.label, color = GameTokens.Text, fontFamily = GameTypeface,
                            fontSize = GameTokens.InventoryCollection.NavLabel, lineHeight = GameTokens.Caption,
                            fontWeight = FontWeight.Bold, softWrap = false)
                    }
                }
            }
        }
    }
}
