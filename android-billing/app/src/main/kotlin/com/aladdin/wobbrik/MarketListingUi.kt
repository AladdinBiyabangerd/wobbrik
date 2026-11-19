package com.aladdin.wobbrik

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import appbackend.wobbrik.game.MarketMutations
import appbackend.wobbrik.game.OwnedBuilding
import appbackend.wobbrik.game.RecoveryCatalog

/** Existing two-stage route; steps are informational rather than parallel navigation. */
@Composable
internal fun MarketListingHeading(price: Boolean, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target), contentAlignment = Alignment.Center) {
        MarketCollectionPanel(Modifier.matchParentSize().padding(vertical = GameTokens.Small - GameTokens.Quarter / 2)) {}
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(GameTokens.Target).semantics { contentDescription = "Geri" }) {
                MobileMenuIcon(MobileIcon.Back, Modifier.size(GameTokens.Inset), GameTokens.Text)
            }
            Text("Yeni elan", Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                color = GameTokens.Text, fontSize = GameTokens.OwnedDetail.Title,
                fontWeight = FontWeight.ExtraBold, softWrap = false)
        }
    }
    val large = LocalDensity.current.fontScale > 1f
    val steps = rememberScrollState()
    LaunchedEffect(price, steps.maxValue) { steps.scrollTo(if(price) steps.maxValue else 0) }
    MarketCollectionPanel {
        Row(Modifier.fillMaxWidth().then(if(large) Modifier.horizontalScroll(steps) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            listOf("Bina seç", "Qiymət").forEachIndexed { index, label ->
                val active = price == (index == 1)
                Column((if(large) Modifier.widthIn(min = GameTokens.Target * 3) else Modifier.weight(1f))
                    .heightIn(min = GameTokens.InventoryCollection.TabVisualHeight).padding(top = GameTokens.Quarter),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.padding(horizontal = GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Box(Modifier.size(GameTokens.InventoryCollection.TabIcon * LocalDensity.current.fontScale).clip(CircleShape)
                            .background(if(active) GameTokens.Accent else GameTokens.Raised), contentAlignment = Alignment.Center) {
                            Text((index + 1).toString(), color = if(active) GameTokens.Background else GameTokens.Text,
                                fontSize = GameTokens.Caption, lineHeight = GameTokens.Caption,
                                style = LocalTextStyle.current.copy(platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)),
                                fontWeight = FontWeight.Bold)
                        }
                        Text(label, color = if(active) GameTokens.Text else GameTokens.InventoryCollection.Metadata,
                            fontSize = GameTokens.Label, fontWeight = FontWeight.Bold, softWrap = false)
                    }
                    Spacer(Modifier.height(GameTokens.Quarter))
                    if(active) Box(Modifier.fillMaxWidth().padding(horizontal = GameTokens.Gap)
                        .height(GameTokens.InventoryCollection.IndicatorHeight).clip(CircleShape).background(GameTokens.Accent))
                }
            }
        }
    }
}

@Composable
internal fun MarketListingBuilding(building: OwnedBuilding, catalog: RecoveryCatalog?, enabled: Boolean, onSelect: () -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    val compact = LocalConfiguration.current.screenWidthDp < GameTokens.PanelWidth.value
    val metadataSize = if(compact) GameTokens.Label else GameTokens.Body
    val cardShape = RoundedCornerShape(GameTokens.ControlRadius)
    // Picker-only lighting follows the approved PNG; the building renderer is unchanged.
    Column(Modifier.fillMaxWidth().clip(cardShape)
        .background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.Selected, GameTokens.InventoryCollection.DockBottom)))
        .drawBehind {
            drawRect(Brush.radialGradient(
                listOf(GameTokens.MobileHome.Rim.copy(alpha = .24f), Color.Transparent),
                center = Offset(size.width * .2f, 0f), radius = size.width * .8f))
        }
        .border(GameTokens.InventoryCollection.OutlineWidth,
            Brush.linearGradient(listOf(GameTokens.InventoryCollection.Metadata.copy(alpha = .8f),
                GameTokens.MobileHome.Rim, GameTokens.InventoryCollection.Outline)), cardShape)) {
        val details: @Composable ColumnScope.() -> Unit = {
            Text(buildingName(building, catalog), color = GameTokens.Text, fontSize = if(compact) GameTokens.Body else GameTokens.OwnedDetail.Title,
                lineHeight = GameTokens.Title, fontWeight = FontWeight.ExtraBold)
            Text("${building.floors} mərtəbə", color = GameTokens.InventoryCollection.Metadata,
                fontSize = metadataSize, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(GameTokens.Small))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Text("Maksimum", color = GameTokens.InventoryCollection.Metadata,
                    fontSize = metadataSize, fontWeight = FontWeight.Bold, softWrap = false)
                Text(building.maxPrice.toString(), color = GameTokens.Text,
                    fontSize = metadataSize, fontWeight = FontWeight.ExtraBold, softWrap = false)
                MobileMenuIcon(MobileIcon.Coin, Modifier.size(if(compact) GameTokens.Gap else GameTokens.InventoryCollection.WalletCoin))
            }
            ListingAction("Seç →", enabled = enabled, compact = true, onClick = onSelect)
        }
        if(large) Column(Modifier.padding(GameTokens.InventoryCollection.CardInset)) {
            MarketBuildingPreview(building, Modifier.fillMaxWidth().height(GameTokens.InventoryCollection.LargeTypeArtHeight))
            details()
        } else Row(Modifier.padding(horizontal = GameTokens.InventoryCollection.CardInset, vertical = GameTokens.Quarter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
            MarketBuildingPreview(building, Modifier.weight(1f)
                .height(GameTokens.InventoryCollection.CompactArtHeight))
            Box(Modifier.width(GameTokens.InventoryCollection.OutlineWidth)
                .height(GameTokens.InventoryCollection.CompactArtHeight - GameTokens.Inset)
                .background(GameTokens.InventoryCollection.Metadata))
            Column(Modifier.weight(1.2f), content = details)
        }
    }
}

/** Price editing uses the system numeric IME, never a custom keypad or a second dialog. */
@Composable
internal fun MarketSalePage(building: OwnedBuilding, catalog: RecoveryCatalog?, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var price by rememberSaveable(building.id) { mutableStateOf("") }
    val value = price.toLongOrNull()?.takeIf { it in 1..building.maxPrice }
    val fee = value?.let(MarketMutations::fee)
    val invalid = price.isNotEmpty() && value == null
    val keyboard = LocalSoftwareKeyboardController.current
    val dismiss = { keyboard?.hide(); onDismiss() }
    val large = LocalDensity.current.fontScale > 1f
    Column(Modifier.fillMaxSize().padding(horizontal = GameTokens.InventoryCollection.ScreenInset),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        MarketListingHeading(true, dismiss)
        // Only the body scrolls. The primary action remains reachable above the dock/IME.
        Column(Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            MarketCollectionPanel {
                Row(Modifier.fillMaxWidth().padding(start = GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
                    Text("${buildingName(building, catalog)} · ${building.floors} mərtəbə",
                        Modifier.weight(1f).horizontalScroll(rememberScrollState()), softWrap = false,
                        color = GameTokens.Text, fontSize = GameTokens.Label, fontWeight = FontWeight.Bold)
                    TextButton(onClick = dismiss, modifier = Modifier.heightIn(min = GameTokens.Target)) {
                        Text("Dəyiş", color = GameTokens.Accent, fontSize = GameTokens.Label)
                    }
                }
            }
            Spacer(Modifier.height(GameTokens.Small))
            Text("Satış qiyməti", color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
            OutlinedTextField(price, { text -> if(text.length <= 19 && text.all { it in '0'..'9' }) price = text },
                Modifier.widthIn(max = if(large) GameTokens.Target * 5 else GameTokens.Target * 3 + GameTokens.Small)
                    .fillMaxWidth().semantics { contentDescription = "Qiymət" },
                singleLine = true, isError = invalid,
                leadingIcon = { MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Inset)) },
                placeholder = { Text("0", color = GameTokens.Muted) },
                textStyle = LocalTextStyle.current.copy(color = GameTokens.Text, fontFamily = GameTypeface,
                    fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                shape = RoundedCornerShape(GameTokens.ControlRadius),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = GameTokens.Text, unfocusedTextColor = GameTokens.Text,
                    cursorColor = GameTokens.Accent, focusedBorderColor = GameTokens.Accent,
                    unfocusedBorderColor = GameTokens.Accent, errorBorderColor = GameTokens.Danger))
            Text(if(invalid) "Qiymət 1–${building.maxPrice} sikkə olmalıdır" else "Maksimum ${building.maxPrice} sikkə",
                color = if(invalid) GameTokens.Danger else GameTokens.InventoryCollection.Metadata,
                fontSize = GameTokens.Label, textAlign = TextAlign.Center)
            // One quiet line; horizontal scroll retains every digit for long amounts/font2.
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(GameTokens.Quarter, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Komissiya: ${fee ?: "—"}", color = GameTokens.InventoryCollection.Metadata, fontSize = GameTokens.Label, softWrap = false)
                MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Gap))
                Text(" · Sənə qalır: ${value?.let { it - requireNotNull(fee) } ?: "—"}", color = GameTokens.Text,
                    fontSize = GameTokens.Label, fontWeight = FontWeight.Bold, softWrap = false)
                MobileMenuIcon(MobileIcon.Coin, Modifier.size(GameTokens.Gap))
            }
        }
        ListingAction("Elanı dərc et", enabled = value != null, icon = MobileIcon.Tag) {
            keyboard?.hide(); onConfirm(requireNotNull(value))
        }
        Spacer(Modifier.height(GameTokens.Small))
    }
}

@Composable
private fun ListingAction(label: String, enabled: Boolean, icon: MobileIcon? = null, compact: Boolean = false, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(GameTokens.CardRadius)
    Surface(onClick = onClick, enabled = enabled, interactionSource = interaction, color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target).collectionPress(interaction, enabled)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(Modifier.fillMaxWidth().then(if(compact) Modifier.padding(vertical = GameTokens.Small) else Modifier).drawBehind {
                if(enabled) {
                    val radius = CornerRadius(GameTokens.CardRadius.toPx())
                    drawRoundRect(GameTokens.Accent.copy(alpha = if(compact) .08f else .12f), cornerRadius = radius, style = Stroke(GameTokens.Small.toPx()))
                    drawRoundRect(GameTokens.Accent.copy(alpha = if(compact) .18f else .28f), cornerRadius = radius, style = Stroke(GameTokens.Quarter.toPx()))
                }
            }.clip(shape).background(Brush.verticalGradient(listOf(GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.DockBottom)))
                .border(if(enabled && !compact) GameTokens.Quarter / 2 else if(enabled) GameTokens.Hairline + GameTokens.InventoryCollection.OutlineWidth else GameTokens.Hairline, if(enabled) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape)
                .then(if(compact && enabled) Modifier.drawBehind {
                    drawRect(Brush.verticalGradient(listOf(GameTokens.Accent.copy(alpha = .10f), Color.Transparent),
                        endY = size.height / 2))
                } else Modifier)
                .padding(horizontal = GameTokens.Small, vertical = if(compact) GameTokens.Quarter else GameTokens.Small),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.CenterHorizontally)) {
                icon?.let { ListingTag(Modifier.size(GameTokens.Inset)) }
                Text(label, color = if(enabled) GameTokens.Text else GameTokens.Muted,
                    fontSize = if(compact) GameTokens.Body else GameTokens.OwnedDetail.Title, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/** Native outline matching the approved listing action rather than the shared filled tag. */
@Composable
private fun ListingTag(modifier: Modifier) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.55f)
            lineTo(size.width * 0.55f, size.height * 0.08f)
            lineTo(size.width * 0.88f, size.height * 0.08f)
            lineTo(size.width * 0.92f, size.height * 0.42f)
            lineTo(size.width * 0.45f, size.height * 0.92f)
            close()
        }
        drawPath(path, GameTokens.Text, style = Stroke(GameTokens.Quarter.toPx() / 2))
        drawCircle(GameTokens.Text, GameTokens.Hairline.toPx() * 1.5f,
            Offset(size.width * 0.73f, size.height * 0.27f))
    }
}
