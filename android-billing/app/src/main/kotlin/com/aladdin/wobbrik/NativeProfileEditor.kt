package com.aladdin.wobbrik

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/** One route-owned sheet shared by overview, editor and picker; no account data. */
internal val LocalAccountPortraitArt = staticCompositionLocalOf<Pair<ResourceArt, () -> Unit>?> { null }

/** Approved account head-and-shoulders art; frame and selection remain native. */
@Composable
internal fun NativeAccountPortrait(id: String, modifier: Modifier, selected: Boolean = true,
    resource: Pair<ResourceArt, () -> Unit>? = null) {
    val (art, retry) = resource ?: LocalAccountPortraitArt.current ?: rememberResourceArt(R.drawable.account_portraits_collection_v1)
    val sheet = art.images?.single()
    if (sheet == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            if (art.failed) TextButton(onClick = retry) { Text("Yenilə", color = GameTokens.Accent) }
            else CircularProgressIndicator(Modifier.size(GameTokens.Target), color = GameTokens.Accent)
        }
        return
    }
    val cell = sheet.height
    val index = when(id) { "pilot" -> 1; "architect" -> 2; else -> 0 }
    val painter = remember(sheet, index) { BitmapPainter(sheet, IntOffset(index * cell, 0), IntSize(cell, cell)) }
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Image(painter, "${avatarLabel(id)} avatarı", modifier.clip(shape).background(Brush.radialGradient(listOf(GameTokens.MobileHome.Rim, GameTokens.MobileHome.PanelBottom)))
        .border(GameTokens.Hairline * 2, if(selected) GameTokens.Accent else GameTokens.MobileHome.Rim, shape))
}

@Composable
private fun EditorActions(canCancel: Boolean, onCancel: () -> Unit, apply: @Composable () -> Unit) {
    apply()
    TextButton(onCancel,enabled=canCancel,modifier=Modifier.fillMaxWidth().heightIn(min=GameTokens.Target).testTag("profile-cancel")) {
        Text("Ləğv et",color=GameTokens.Muted)
    }

}

@Composable
internal fun NativeProfileEditView(name: String, avatar: String, editable: Boolean, canCancel: Boolean,
    canSave: Boolean, saveLabel: String, onName: (String) -> Unit, onAvatar: () -> Unit,
    onCancel: () -> Unit, onSave: () -> Unit, status: @Composable () -> Unit) {
    val feedback = LocalGameFeedback.current
    Column(Modifier.fillMaxSize().padding(horizontal=GameTokens.InventoryCollection.ScreenInset), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
        val backInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        Surface(onClick = onCancel, enabled = canCancel, interactionSource = backInteraction,
            color = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier.size(GameTokens.Target).collectionPress(backInteraction, canCancel)
                .semantics { contentDescription = "Geri" }) {
            val shape = RoundedCornerShape(GameTokens.ControlRadius)
            Canvas(Modifier.padding(GameTokens.Quarter).clip(shape)
                .background(Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight, GameTokens.MobileHome.Rim, GameTokens.MobileHome.PanelBottom)))
                .border(GameTokens.Hairline, GameTokens.MobileHome.RimLight, shape)) {
                val arrow = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width*.48f,size.height*.28f)
                    lineTo(size.width*.26f,size.height*.5f)
                    lineTo(size.width*.48f,size.height*.72f)
                    moveTo(size.width*.26f,size.height*.5f)
                    lineTo(size.width*.76f,size.height*.5f)
                }
                drawPath(arrow, if(canCancel) GameTokens.Text else GameTokens.Muted,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(GameTokens.Quarter.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round))
            }
        }
        Text("Profili dəyiş", color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()), softWrap = false)
        }
        ProfileCollectionPanel(Modifier.fillMaxWidth().weight(1f)) {
            Column(Modifier.fillMaxSize().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).testTag("profile-editor-body"),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    NativeAccountPortrait(avatar, Modifier.size(if(mobileUiSizing().compact || LocalDensity.current.fontScale > 1f)
                        GameTokens.Profile.CompactPortrait else GameTokens.Profile.OverviewPortrait))
                    NativeProfileAction("Avatarı dəyiş", editable, (if(LocalDensity.current.fontScale > 1f) Modifier.fillMaxWidth() else Modifier.width(GameTokens.Profile.Portrait + GameTokens.Target)).testTag("profile-avatar")) { feedback(); onAvatar() }
                    HorizontalDivider(color = GameTokens.MobileHome.Rim)
                    Row(Modifier.fillMaxWidth()) {
                        Text("Oyunçu adı",color=GameTokens.Text,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                        Text("${name.codePointCount(0,name.length)}/15",color=GameTokens.Muted)
                    }
                    OutlinedTextField(name, onName, Modifier.fillMaxWidth().testTag("profile-name"), enabled = editable, singleLine = true,
                        supportingText = { Text("1–15 simvol", color = GameTokens.Muted) },
                        shape = RoundedCornerShape(GameTokens.ControlRadius),
                        textStyle = LocalTextStyle.current.copy(color = GameTokens.Text, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold), colors = communityFieldColors())
                    status()
                }
                EditorActions(canCancel, onCancel) {
                    NativeProfileAction(saveLabel, canSave, Modifier.testTag("profile-save")) { feedback(); onSave() }
                }
            }
        }
    }
}

/** Only choices scroll; selection remains local until confirmation. */
@Composable
internal fun NativeProfileAvatarSheet(avatars: List<String>, selected: String, onClose: () -> Unit, onApply: (String) -> Unit) {
    val feedback = LocalGameFeedback.current
    var pick by remember(selected) { mutableStateOf(selected) }
    val portraits = LocalAccountPortraitArt.current ?: rememberResourceArt(R.drawable.account_portraits_collection_v1)
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().padding(GameTokens.Small), contentAlignment = Alignment.BottomCenter) {
        ProfileCollectionPanel(Modifier.fillMaxWidth().heightIn(max = maxHeight * GameTokens.Profile.SheetHeightFraction)) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Box(Modifier.align(Alignment.CenterHorizontally).width(GameTokens.Target).height(GameTokens.Quarter)
                    .background(GameTokens.MobileHome.PlayTop, RoundedCornerShape(GameTokens.ControlRadius)))
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("Avatar seç",color=GameTokens.Text,fontWeight=FontWeight.ExtraBold,fontSize=GameTokens.Title,modifier=Modifier.weight(1f))
                    TextButton(onClose,modifier=Modifier.size(GameTokens.Target).testTag("avatar-close").semantics { contentDescription="Bağla" }) {
                        Text("×",color=GameTokens.Text,fontSize=GameTokens.Title)
                    }
                }
                LazyVerticalGrid(GridCells.Fixed(if(LocalDensity.current.fontScale > 1f) 2 else 3),
                    Modifier.weight(1f, fill = false).fillMaxWidth().selectableGroup().testTag("avatar-grid"),
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    items(avatars, key = { it }) { id ->
                        Column(Modifier.heightIn(min = GameTokens.Target).selectable(pick == id, role = Role.RadioButton, onClick = { pick = id })
                            .testTag("avatar-$id"), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                NativeAccountPortrait(id, Modifier.fillMaxWidth().aspectRatio(1f), pick == id, portraits)
                                if(pick == id) Box(Modifier.align(Alignment.TopEnd).size(GameTokens.Inset + GameTokens.Small)
                                    .background(GameTokens.Accent, androidx.compose.foundation.shape.CircleShape)
                                    .border(GameTokens.Hairline, GameTokens.Text, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    Canvas(Modifier.fillMaxSize().padding(GameTokens.Small)) {
                                        val check = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(0f,size.height*.5f);lineTo(size.width*.36f,size.height*.85f)
                                            lineTo(size.width,size.height*.12f)
                                        }
                                        drawPath(check, GameTokens.Background,
                                            style=androidx.compose.ui.graphics.drawscope.Stroke(GameTokens.Hairline.toPx()*2,
                                                cap=androidx.compose.ui.graphics.StrokeCap.Round,
                                                join=androidx.compose.ui.graphics.StrokeJoin.Round))
                                    }
                                }
                            }
                            Text(avatarLabel(id), Modifier.horizontalScroll(rememberScrollState()), softWrap = false,
                                color = GameTokens.Text, fontSize = GameTokens.Label)
                        }
                    }
                }
                NativeProfileAction("Bu avatarı seç",pick in avatars,Modifier.fillMaxWidth().testTag("avatar-apply")) { feedback();onApply(pick) }
            }
        }
    }
}
