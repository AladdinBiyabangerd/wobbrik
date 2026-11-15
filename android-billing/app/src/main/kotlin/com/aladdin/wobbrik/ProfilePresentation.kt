package com.aladdin.wobbrik

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import appbackend.wobbrik.game.PublicProfile

private data class ProfileBoards(val profile: ImageBitmap, val editor: ImageBitmap, val skin: ImageBitmap, val panel: ImageBitmap)
private val LocalProfileBoards = staticCompositionLocalOf<ProfileBoards?> { null }

/** Share full-size source boards across portraits, skin slices and lazy cells. */
@Composable
private fun ProfileArtProvider(content: @Composable () -> Unit) {
    if (LocalProfileBoards.current != null) content()
    else {
        val profile = ImageBitmap.imageResource(R.drawable.profile_v2_board)
        val editor = ImageBitmap.imageResource(R.drawable.profile_edit_board)
        val skin = ImageBitmap.imageResource(R.drawable.profile_skin_clean)
        val panel = ImageBitmap.imageResource(R.drawable.profile_panel_clean)
        val boards = remember(profile, editor, skin, panel) { ProfileBoards(profile, editor, skin, panel) }
        CompositionLocalProvider(LocalProfileBoards provides boards,
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = GameTypeface,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(GameTokens.Background, Offset(0f, GameTokens.Profile.TextShadowOffset), GameTokens.Profile.TextShadowBlur)),
            content = content)
    }
}

@Composable
private fun profileBoard(resource: Int): ImageBitmap {
    val boards = requireNotNull(LocalProfileBoards.current)
    return when (resource) {
        R.drawable.profile_v2_board -> boards.profile
        R.drawable.profile_skin_clean -> boards.skin
        R.drawable.profile_panel_clean -> boards.panel
        else -> boards.editor
    }
}

@Composable
internal fun ProfileFrame(title: String, onBack: () -> Unit, coins: Long?, content: @Composable ColumnScope.() -> Unit) {
    ProfileArtProvider {
        Column(Modifier.fillMaxSize().background(GameTokens.Background).safeDrawingPadding().imePadding()
            .padding(horizontal = if (coins != null) GameTokens.Quarter else GameTokens.Gap)
            .padding(top = GameTokens.Gap).then(if (coins == null) Modifier.padding(bottom = GameTokens.Gap) else Modifier),
            verticalArrangement = Arrangement.spacedBy(if (coins != null) GameTokens.Quarter else GameTokens.Small)) {
            Row(if (coins != null) Modifier.padding(horizontal = GameTokens.Quarter) else Modifier,
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                ProfileSourceArt(GameTokens.Profile.Back, R.drawable.profile_v2_board,
                    Modifier.size(GameTokens.Target).clickable(role = Role.Button, onClick = onBack).semantics { contentDescription = "Geri" })
                Text(title, Modifier.weight(1f), color = GameTokens.Text, fontSize = if (mobileUiSizing().compact) GameTokens.Title else GameTokens.Score, fontWeight = FontWeight.Bold)
                if (!LocalMobileMenuShell.current && coins != null && coins.toString().length <= 6 && LocalDensity.current.fontScale <= 1f) {
                    Box(Modifier.width(GameTokens.Profile.WalletWidth).height(GameTokens.Target).clip(RoundedCornerShape(GameTokens.CardRadius)), contentAlignment = Alignment.CenterEnd) {
                        ProfileSkin(false, Modifier.matchParentSize(), "wallet")
                        ProfileSourceArt(GameTokens.Profile.WalletCoin, R.drawable.profile_v2_board,
                            Modifier.align(Alignment.CenterStart).size(GameTokens.Target).clip(androidx.compose.foundation.shape.CircleShape))
                        Text(coins.toString(), Modifier.padding(end = GameTokens.Small), color = GameTokens.Text, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            if (!LocalMobileMenuShell.current && coins != null && (coins.toString().length > 6 || LocalDensity.current.fontScale > 1f)) Text("$coins sikkə", color = GameTokens.Text)
            content()
        }
    }
}

@Composable
internal fun ProfileEditView(name: String, avatar: String, editable: Boolean, canCancel: Boolean,
    canSave: Boolean, saveLabel: String, onName: (String) -> Unit, onAvatar: () -> Unit,
    onCancel: () -> Unit, onSave: () -> Unit, status: @Composable () -> Unit = {}) {
    ProfileFrame("Profili dəyiş", { if (canCancel) onCancel() }, null) {
        ProfilePanel(Modifier.fillMaxWidth().weight(1f, fill = false)) {
            Column(Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Gap), horizontalAlignment = Alignment.CenterHorizontally) {
                ProfilePortrait(avatar, Modifier.size(GameTokens.Profile.Portrait))
                TextButton(onClick = onAvatar, enabled = editable) { Text("Avatarı dəyiş", color = GameTokens.Accent, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.ExtraBold) }
                HorizontalDivider(color = GameTokens.Raised)
                Row(Modifier.fillMaxWidth()) {
                    Text("Oyunçu adı", Modifier.weight(1f), color = GameTokens.Text, fontWeight = FontWeight.ExtraBold)
                    Text("${name.codePointCount(0, name.length)}/15", color = GameTokens.Muted)
                }
                OutlinedTextField(name, onName, Modifier.fillMaxWidth(), enabled = editable, singleLine = true,
                    supportingText = { Text("1–15 simvol", color = GameTokens.Muted) },
                    shape = RoundedCornerShape(GameTokens.ControlRadius),
                    textStyle = LocalTextStyle.current.copy(fontSize = GameTokens.Title, color = GameTokens.Text),
                    colors = communityFieldColors())
                status()
            }
            ProfileAction(saveLabel, enabled = canSave, onClick = onSave)
            TextButton(onClick = onCancel, enabled = canCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Ləğv et", color = GameTokens.Muted)
            }
        }
    }
}

/** Artwork is sampled directly from the approved boards, never a replacement portrait. */
@Composable
internal fun ProfilePortrait(id: String, modifier: Modifier = Modifier) {
    ProfileSourceArt(when (id) {
        "pilot" -> GameTokens.Profile.Pilot
        "architect" -> GameTokens.Profile.Architect
        else -> GameTokens.Profile.Builder
    }, if (id == "builder") R.drawable.profile_v2_board else R.drawable.profile_edit_board, modifier.clip(RoundedCornerShape(GameTokens.Profile.PortraitCornerPercent)).semantics { contentDescription = "${avatarLabel(id)} avatarı" })
}

@Composable
private fun ProfileSourceArt(rect: IntArray, resource: Int, modifier: Modifier, cutout: Boolean = false) {
    val board = profileBoard(resource)
    val foreground = remember(board, rect, cutout) {
        if (cutout) sourceForeground(board, rect[0], rect[1], rect[2], rect[3], cornerBackground = true) else null
    }
    Canvas(modifier.clipToBounds()) {
        val scale = minOf(size.width / rect[2], size.height / rect[3])
        withTransform({
            translate((size.width - rect[2] * scale) / 2, (size.height - rect[3] * scale) / 2)
            scale(scale, scale, pivot = Offset.Zero)
            translate(-rect[0].toFloat(), -rect[1].toFloat())
        }) {
            if (foreground != null) clipPath(foreground) { drawImage(board) }
            else drawImage(board, IntOffset(rect[0], rect[1]), IntSize(rect[2], rect[3]),
                IntOffset(rect[0], rect[1]), IntSize(rect[2], rect[3]))
        }
    }
}

/** Profile chrome uses the clean skin; editor/action retain the existing source slices. */
@Composable
private fun ProfileSkin(action: Boolean, modifier: Modifier, kind: String = "editor") {
    if (!action && kind == "overview") {
        ProfileOverviewSkin(modifier)
        return
    }
    val profileSkin = !action && kind != "editor"
    val original = profileBoard(if (profileSkin) R.drawable.profile_skin_clean else R.drawable.profile_edit_board)
    val rect = when { action -> GameTokens.Profile.ActionSource
        kind == "identity" -> GameTokens.Profile.IdentitySource; kind == "navbar" -> GameTokens.Profile.NavbarSource
        kind == "wallet" -> GameTokens.Profile.WalletSource; else -> GameTokens.Profile.PanelSource }
    val erase = if (action) GameTokens.Profile.ActionErase else GameTokens.Profile.PanelErase
    val board = remember(original, action, kind) {
        // No erasure/interpolation is applied to the newly prepared profile skin.
        val source = original.asAndroidBitmap()
        val crop = android.graphics.Bitmap.createBitmap(source, rect[0], rect[1], rect[2], rect[3])
        if (profileSkin) return@remember crop.asImageBitmap()
        val bitmap = crop.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(rect[2] * rect[3])
        bitmap.getPixels(pixels, 0, rect[2], 0, 0, rect[2], rect[3])
        for (y in erase[1] until erase[3]) {
            val start = erase[0]
            val end = erase[2]
            val left = pixels[y * rect[2] + start]
            val right = pixels[y * rect[2] + end]
            for (x in start..end) {
                val t = (x - start).toFloat() / (end - start)
                fun channel(shift: Int): Int = (((left ushr shift) and 255) * (1 - t) + ((right ushr shift) and 255) * t).toInt()
                pixels[y * rect[2] + x] = android.graphics.Color.rgb(channel(16), channel(8), channel(0))
            }
        }
        bitmap.setPixels(pixels, 0, rect[2], 0, 0, rect[2], rect[3])
        bitmap.asImageBitmap()
    }
    Canvas(modifier.clipToBounds()) {
        val corner = if (action) GameTokens.Profile.ActionCorner else GameTokens.Profile.PanelCorner
        val edge = minOf((if (action) GameTokens.ControlRadius else GameTokens.CardRadius).toPx(), size.width / 2, size.height / 2).toInt()
        val xs = intArrayOf(0, corner, rect[2] - corner, rect[2])
        val ys = intArrayOf(0, corner, rect[3] - corner, rect[3])
        val dx = intArrayOf(0, edge, size.width.toInt() - edge, size.width.toInt())
        val dy = intArrayOf(0, edge, size.height.toInt() - edge, size.height.toInt())
        for (y in 0..2) for (x in 0..2) {
            drawImage(board, IntOffset(xs[x], ys[y]), IntSize(xs[x + 1] - xs[x], ys[y + 1] - ys[y]),
                IntOffset(dx[x], dy[y]), IntSize(dx[x + 1] - dx[x], dy[y + 1] - dy[y]))
        }
    }
}

/** Ribbon and rail share one source slice; no silhouette mask or reconstructed fill. */
@Composable
private fun ProfileOverviewSkin(modifier: Modifier) {
    val board = profileBoard(R.drawable.profile_panel_clean)
    Canvas(modifier.clipToBounds()) {
        val header = GameTokens.Profile.OverviewHeader
        val interior = GameTokens.Profile.OverviewInterior
        val footer = GameTokens.Profile.OverviewFooter
        val scale = size.width / header[2]
        val top = (header[3] * scale).toInt().coerceAtMost(size.height.toInt())
        val bottom = (footer[3] * scale).toInt().coerceAtMost(size.height.toInt() - top)
        fun slice(rect: IntArray, y: Int, height: Int) {
            if (height > 0) drawImage(board, IntOffset(rect[0], rect[1]), IntSize(rect[2], rect[3]),
                IntOffset(0, y), IntSize(size.width.toInt(), height))
        }
        slice(header, 0, top)
        slice(interior, top, size.height.toInt() - top - bottom)
        slice(footer, size.height.toInt() - bottom, bottom)
    }
}

@Composable
private fun ProfileRule(vertical: Boolean = false, modifier: Modifier = Modifier) {
    val board = profileBoard(R.drawable.profile_v2_board)
    val rect = if (vertical) GameTokens.Profile.StatRule else GameTokens.Profile.Rule
    Canvas(modifier.then(if (vertical) Modifier.width(GameTokens.Hairline * 2)
        else Modifier.fillMaxWidth().height(GameTokens.Hairline * 2))) {
        drawImage(board, IntOffset(rect[0], rect[1]), IntSize(rect[2], rect[3]),
            IntOffset.Zero, IntSize(size.width.toInt(), size.height.toInt()))
    }
}

@Composable
internal fun ProfileAction(label: String, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val feedback = LocalGameFeedback.current
    Box(modifier.fillMaxWidth().heightIn(min = if (mobileUiSizing().compact) GameTokens.Target else GameTokens.MobileUi.PrimaryTarget)
        .clip(RoundedCornerShape(GameTokens.ControlRadius))
        .clickable(enabled = enabled, role = Role.Button, onClick = { feedback(); onClick() }), contentAlignment = Alignment.Center) {
        ProfileSkin(true, Modifier.matchParentSize())
        Text(label, Modifier.padding(GameTokens.Small), color = if (enabled) GameTokens.Background else GameTokens.Surface,
            fontSize = GameTokens.Title, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(shadow = null))
    }
}

@Composable
internal fun ProfilePanel(modifier: Modifier = Modifier, kind: String = "editor", content: @Composable ColumnScope.() -> Unit) {
    val compact = mobileUiSizing().compact && LocalDensity.current.fontScale <= 1f
    Box(modifier.clip(RoundedCornerShape(GameTokens.CardRadius))) {
        ProfileSkin(false, Modifier.matchParentSize(), kind)
        Column(Modifier.fillMaxWidth().padding(if (compact || kind == "navbar") GameTokens.Small else GameTokens.Profile.PanelInset)
            .then(if (kind == "overview" && !compact) Modifier.padding(bottom = GameTokens.Small) else Modifier),
            verticalArrangement = Arrangement.spacedBy(if (compact || kind == "overview") GameTokens.Small else GameTokens.Gap), content = content)
    }
}

@Composable
private fun ProfileLevelBadge(level: String) {
    val board = profileBoard(R.drawable.profile_v2_board)
    val r = GameTokens.Profile.LevelBadge
    val interior = remember {
        androidx.compose.ui.graphics.Path().apply {
            val p = GameTokens.Profile.BadgeInterior
            moveTo(p[0], p[1])
            for (i in 2 until p.size step 2) lineTo(p[i], p[i + 1])
            close()
        }
    }
    val interiorColors = remember(board) {
        GameTokens.Profile.BadgeColorSamples.map { point -> Color(board.asAndroidBitmap().getPixel(point[0], point[1])) }
    }
    val foreground = remember(board) { sourceForeground(board, r[0], r[1], r[2], r[3], cornerBackground = true) }
    Box(Modifier.width(GameTokens.Profile.BadgeWidth).height(GameTokens.Profile.BadgeHeight), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize().clipToBounds()) {
            val scale = minOf(size.width / r[2], size.height / r[3])
            withTransform({
                translate((size.width - r[2] * scale) / 2, (size.height - r[3] * scale) / 2)
                scale(scale, scale, pivot = Offset.Zero); translate(-r[0].toFloat(), -r[1].toFloat())
            }) {
                clipPath(foreground) {
                drawImage(board, IntOffset(r[0], r[1]), IntSize(r[2], r[3]), IntOffset(r[0], r[1]), IntSize(r[2], r[3]))
                drawPath(interior, Brush.linearGradient(interiorColors,
                    Offset(GameTokens.Profile.BadgeInterior[0], GameTokens.Profile.BadgeInterior[1]),
                    Offset(GameTokens.Profile.BadgeInterior[6], GameTokens.Profile.BadgeInterior[7])))
                }
            }
        }
        Text(level, Modifier.offset(x = GameTokens.Profile.BadgeTextOffsetX, y = GameTokens.Profile.BadgeTextOffsetY), color = GameTokens.Text,
            fontSize = if (level.length > 2) GameTokens.Hint else GameTokens.Title, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
internal fun ProfileOverview(profile: PublicProfile, level: String, xp: Long, best: String,
    progress: @Composable () -> Unit, showEdit: Boolean = true, enabled: Boolean, onEdit: () -> Unit) {
    val compact = mobileUiSizing().compact && LocalDensity.current.fontScale <= 1f
    Box(Modifier.fillMaxWidth()) {
    ProfilePanel(Modifier.fillMaxWidth().testTag("profile-panel"), kind = "overview") {
        Spacer(Modifier.height(if (compact) GameTokens.Target - GameTokens.Small
            else if (LocalDensity.current.fontScale > 1f) GameTokens.Target + GameTokens.Small else GameTokens.Target))
        val largeFont = LocalDensity.current.fontScale > 1f
        val portrait = if (mobileUiSizing().compact) GameTokens.Profile.CompactPortrait else GameTokens.Profile.OverviewPortrait
        @Composable fun identity() {
            Text(profile.displayName, color = GameTokens.Text, fontSize = if (compact || profile.displayName.codePointCount(0, profile.displayName.length) > 10) GameTokens.Title else GameTokens.Score, fontWeight = FontWeight.ExtraBold)
            if (largeFont || level.length > 3) Text("Səviyyə $level", color = GameTokens.Accent, fontWeight = FontWeight.Bold)
            else ProfileLevelBadge(level)
        }
        if (largeFont) {
            ProfilePortrait(profile.avatarId, Modifier.size(portrait).align(Alignment.CenterHorizontally))
            identity()
        } else Row(if (compact) Modifier else Modifier.padding(bottom = GameTokens.Small),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
            ProfilePortrait(profile.avatarId, Modifier.size(portrait).clip(RoundedCornerShape(GameTokens.ControlRadius)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) { identity() }
                    if (showEdit) ProfileSourceArt(GameTokens.Profile.Pencil, R.drawable.profile_v2_board,
                        Modifier.size(GameTokens.Target).clickable(enabled = enabled, role = Role.Button, onClick = onEdit)
                            .semantics { contentDescription = "Profili dəyiş" })
                }
            }
        }
        ProfileRule()
        progress()
        ProfileRule()
        @Composable fun stat(rect: IntArray, value: String, label: String, modifier: Modifier) {
            Column(modifier, horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                ProfileSourceArt(rect, R.drawable.profile_v2_board, Modifier.width(if (compact) GameTokens.Target else if (rect === GameTokens.Profile.Crown) GameTokens.Profile.CrownWidth else GameTokens.Profile.StatIcon).height(if (compact) GameTokens.Inset else GameTokens.Profile.StatIcon), cutout = true)
                Text(value, color = GameTokens.Text, fontSize = if (compact) GameTokens.Title else GameTokens.Score, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text(label, color = GameTokens.Muted, fontSize = if (compact) GameTokens.Label else GameTokens.Profile.StatLabel, textAlign = TextAlign.Center)
            }
        }
        if (largeFont || xp.toString().length > 9 || best.length > 9) {
            stat(GameTokens.Profile.Crown, best, "Rekord", Modifier.fillMaxWidth())
            stat(GameTokens.Profile.Xp, xp.toString(), "Təcrübə", Modifier.fillMaxWidth())
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            stat(GameTokens.Profile.Crown, best, "Rekord", Modifier.weight(1f))
            ProfileRule(vertical = true, Modifier.height(if (compact) GameTokens.Target + GameTokens.Inset else GameTokens.Profile.StatRowHeight))
            stat(GameTokens.Profile.Xp, xp.toString(), "Təcrübə", Modifier.weight(1f))
        }
        ProfileRule()
        if (showEdit) {
            Box(Modifier.fillMaxWidth()) {
            ProfileSkin(false, Modifier.matchParentSize(), "identity")
            Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                if (profile.avatarId == "builder") ProfileSourceArt(GameTokens.Profile.IdentityPortrait, R.drawable.profile_v2_board,
                    Modifier.size(if (compact) GameTokens.Target else GameTokens.MobileUi.PrimaryTarget).clip(androidx.compose.foundation.shape.CircleShape))
                else ProfilePortrait(profile.avatarId, Modifier.size(if (compact) GameTokens.Target else GameTokens.MobileUi.PrimaryTarget).clip(androidx.compose.foundation.shape.CircleShape))
                Text("Ad və avatar", Modifier.weight(1f), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                ProfileAction("Dəyiş", enabled = enabled,
                    modifier = Modifier.width(GameTokens.Target * 2).testTag("profile-edit"), onClick = onEdit)
            }
            }
        }
    }
    }
}

@Composable
internal fun ProfileNavbar(onHome: () -> Unit, onPage: (String) -> Unit) {
    if (LocalMobileMenuShell.current) {
        ProfileAction("Parametrlər") { onPage("settings") }
        return
    }
    val largeFont = LocalDensity.current.fontScale > 1f
    val items = listOf(Pair("Ana ekran", "home"),
        Pair("Nailiyyətlər", "achievements"),
        Pair("Rekordlar", "leaderboard"),
        Pair("Parametrlər", "settings"))
    ProfilePanel(Modifier.fillMaxWidth(), kind = "navbar") {
        val columns = 4
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                row.forEach { (label, route) ->
                    Column(Modifier.weight(1f).heightIn(min = GameTokens.Target)
                        .clickable(role = Role.Button) { if (route == "home") onHome() else onPage(route) }
                        .testTag("profile-nav-$route").semantics { contentDescription = label }, horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(GameTokens.Quarter)) {
                        val art = when (route) {
                            "home" -> GameTokens.Profile.NavHome
                            "achievements" -> GameTokens.Profile.NavAchievements
                            "leaderboard" -> GameTokens.Profile.NavRecords
                            else -> GameTokens.Profile.NavSettings
                        }
                        ProfileSourceArt(art, R.drawable.profile_v2_board,
                            Modifier.width(if (mobileUiSizing().compact) GameTokens.Profile.CompactNavIcon else GameTokens.Profile.NavIcon)
                                .height(GameTokens.Profile.NavIconHeight), cutout = true)
                        if (!largeFont) Text(label, color = GameTokens.Text, fontSize = GameTokens.Caption, textAlign = TextAlign.Center,
                            // Never break an Azerbaijani word across lines.
                            softWrap = false)
                    }
                }
            }
        }
    }
}

/** Only the viewport scrolls. Header and apply action are outside the lazy grid. */
@Composable
internal fun ProfileAvatarPicker(avatars: List<String>, selected: String, onClose: () -> Unit, onApply: (String) -> Unit) {
    var pick by remember(selected) { mutableStateOf(selected) }
    val parentDensity = LocalDensity.current
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ProfileArtProvider {
        CompositionLocalProvider(LocalDensity provides parentDensity) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.BottomCenter) {
            Box(Modifier.matchParentSize().clickable(onClick = onClose))
            ProfilePanel(Modifier.fillMaxWidth().then(if (avatars.size > 6)
                Modifier.fillMaxHeight(GameTokens.Profile.SheetHeightFraction)
                else Modifier.heightIn(max = maxHeight * GameTokens.Profile.SmallSheetHeightFraction))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Avatar seç", Modifier.weight(1f), color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onClose, modifier = Modifier.sizeIn(minWidth = GameTokens.Target, minHeight = GameTokens.Target)) {
                        Text("×", color = GameTokens.Text, fontSize = GameTokens.Title, modifier = Modifier.semantics { contentDescription = "Bağla" })
                    }
                }
                LazyVerticalGrid(columns = GridCells.Fixed(if (LocalDensity.current.fontScale > 1f) 2 else 3),
                    modifier = Modifier.weight(1f, fill = avatars.size > 6).then(if (avatars.size <= 6)
                        Modifier.heightIn(max = GameTokens.Profile.SmallGridHeight) else Modifier)
                        .fillMaxWidth().selectableGroup().testTag("avatar-grid"),
                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    items(avatars, key = { it }) { id ->
                        Column(Modifier.selectable(selected = pick == id, role = Role.RadioButton, onClick = { pick = id })
                            .testTag("avatar-$id").padding(GameTokens.Quarter),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.then(if (pick == id) Modifier.border(BorderStroke(GameTokens.Hairline * 2, GameTokens.Accent),
                                RoundedCornerShape(GameTokens.ControlRadius)) else Modifier)) {
                                ProfilePortrait(id, Modifier.fillMaxWidth().aspectRatio(1f))
                                if (pick == id) Text("✓", Modifier.align(Alignment.TopEnd)
                                    .background(GameTokens.Accent, androidx.compose.foundation.shape.CircleShape).padding(GameTokens.Quarter),
                                    color = GameTokens.Background, fontWeight = FontWeight.Bold,
                                    style = LocalTextStyle.current.copy(shadow = null))
                            }
                            Text(avatarLabel(id), color = GameTokens.Text,
                                fontSize = GameTokens.Label, textAlign = TextAlign.Center)
                        }
                    }
                }
                ProfileAction("Bu avatarı seç", enabled = pick in avatars,
                    modifier = Modifier.testTag("avatar-apply")) { onApply(pick) }
            }
        }
        }
    }
    }
}
