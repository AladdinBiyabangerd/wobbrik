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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/** Approved account head-and-shoulders art; frame and selection remain native. */
@Composable
internal fun NativeAccountPortrait(id: String, modifier: Modifier, selected: Boolean = true) {
    val sheet = ImageBitmap.imageResource(R.drawable.account_portraits_v3)
    val cell = sheet.height
    val index = when(id) { "pilot" -> 1; "architect" -> 2; else -> 0 }
    val painter = remember(sheet, index) { BitmapPainter(sheet, IntOffset(index * cell, 0), IntSize(cell, cell)) }
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Image(painter, "${avatarLabel(id)} avatarı", modifier.clip(shape).background(Brush.radialGradient(listOf(GameTokens.MobileHome.Rim, GameTokens.MobileHome.PanelBottom)))
        .border(GameTokens.Hairline * 2, if(selected) GameTokens.Accent else GameTokens.MobileHome.Rim, shape))
}

@Composable
private fun EditorActions(canCancel: Boolean, onCancel: () -> Unit, apply: @Composable () -> Unit) {
    @Composable fun cancel(modifier: Modifier) {
        OutlinedButton(onCancel, enabled = canCancel, modifier = modifier.heightIn(min = GameTokens.Target).testTag("profile-cancel"),
            shape = RoundedCornerShape(GameTokens.ControlRadius)) {
            Text("Ləğv et", color = if(canCancel) GameTokens.Text else GameTokens.Muted,
                modifier = Modifier.horizontalScroll(rememberScrollState()), softWrap = false)
        }
    }
    if(LocalDensity.current.fontScale > 1f || mobileUiSizing().compact) {
        apply(); cancel(Modifier.fillMaxWidth())
    } else Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
        cancel(Modifier.weight(1f)); Box(Modifier.weight(1f)) { apply() }
    }
}

@Composable
internal fun NativeProfileEditView(name: String, avatar: String, editable: Boolean, canCancel: Boolean,
    canSave: Boolean, saveLabel: String, onName: (String) -> Unit, onAvatar: () -> Unit,
    onCancel: () -> Unit, onSave: () -> Unit, status: @Composable () -> Unit) {
    val feedback = LocalGameFeedback.current
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.MobileHome.PanelTop, GameTokens.MobileHome.PanelBottom)))
        .safeDrawingPadding().imePadding().padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onCancel, enabled = canCancel, modifier = Modifier.size(GameTokens.Target).semantics { contentDescription = "Geri" }) {
            Text("‹", color = if(canCancel) GameTokens.Text else GameTokens.Muted, fontSize = GameTokens.Title)
        }
        Text("Profili dəyiş", color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()), softWrap = false)
        }
        MenuRelief(Modifier.fillMaxWidth().weight(1f, fill = false)) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).testTag("profile-editor-body"),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    NativeAccountPortrait(avatar, Modifier.size(if(mobileUiSizing().compact || LocalDensity.current.fontScale > 1f)
                        GameTokens.Profile.CompactPortrait else GameTokens.Profile.Portrait))
                    NativeProfileAction("Avatarı dəyiş", editable, (if(LocalDensity.current.fontScale > 1f) Modifier.fillMaxWidth() else Modifier.width(GameTokens.Profile.Portrait + GameTokens.Target)).testTag("profile-avatar")) { feedback(); onAvatar() }
                    HorizontalDivider(color = GameTokens.MobileHome.Rim)
                    Text("Oyunçu adı", color = GameTokens.Text, fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), softWrap = false)
                    OutlinedTextField(name, onName, Modifier.fillMaxWidth().testTag("profile-name"), enabled = editable, singleLine = true,
                        supportingText = { Text("${name.codePointCount(0, name.length)}/15 · 1–15 simvol", color = GameTokens.Muted) },
                        shape = RoundedCornerShape(GameTokens.ControlRadius),
                        textStyle = LocalTextStyle.current.copy(color = GameTokens.Text, fontSize = GameTokens.Body), colors = communityFieldColors())
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
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().padding(GameTokens.Small), contentAlignment = Alignment.Center) {
        MenuRelief(Modifier.fillMaxWidth().heightIn(max = maxHeight * GameTokens.Profile.SheetHeightFraction)) {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                Text("Avatar seç", color = GameTokens.Text, fontWeight = FontWeight.ExtraBold, fontSize = GameTokens.Title,
                    modifier = Modifier.horizontalScroll(rememberScrollState()), softWrap = false)
                LazyVerticalGrid(GridCells.Fixed(if(LocalDensity.current.fontScale > 1f) 2 else 3),
                    Modifier.weight(1f, fill = false).fillMaxWidth().selectableGroup().testTag("avatar-grid"),
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    items(avatars, key = { it }) { id ->
                        Column(Modifier.heightIn(min = GameTokens.Target).selectable(pick == id, role = Role.RadioButton, onClick = { pick = id })
                            .testTag("avatar-$id"), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                NativeAccountPortrait(id, Modifier.fillMaxWidth().aspectRatio(1f), pick == id)
                                if(pick == id) Text("✓", Modifier.align(Alignment.TopEnd).background(GameTokens.Accent,
                                    RoundedCornerShape(GameTokens.ControlRadius)).padding(GameTokens.Quarter), color = GameTokens.Background,
                                    fontWeight = FontWeight.ExtraBold)
                            }
                            Text(avatarLabel(id), Modifier.horizontalScroll(rememberScrollState()), softWrap = false,
                                color = GameTokens.Text, fontSize = GameTokens.Label)
                        }
                    }
                }
                EditorActions(true, onClose) {
                    NativeProfileAction("Təsdiqlə", pick in avatars, Modifier.testTag("avatar-apply")) { feedback(); onApply(pick) }
                }
            }
        }
    }
}
