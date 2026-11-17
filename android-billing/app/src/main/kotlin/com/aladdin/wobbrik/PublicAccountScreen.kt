package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Profile operations deliberately bypass the economic journal. One editor per login epoch. */
@Composable
internal fun PublicAccountScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onHome: () -> Unit, onPage: (String) -> Unit) {
    val session = remember { requireNotNull(configured.sessions.current()) }
    val editor = remember { ProfileEditor(configured.sessions, requireNotNull(configured.profiles), session) }
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<PublicProfile?>(null) }
    var name by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("builder") }
    var editing by remember { mutableStateOf(false) }
    var pickingAvatar by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var uncertain by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    fun current() = configured.sessions.current() == session && configured.recovery.state.value === ready
    fun adopt(value: PublicProfile) { profile = value; name = value.displayName; avatar = value.avatarId }
    LaunchedEffect(reload) {
        busy = true; failed = false
        try { val value = editor.load(); if (current()) { adopt(value); uncertain = false } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { if (current()) failed = true }
        finally { if (current()) busy = false }
    }
    fun closeEditor() {
        if (!busy) {
            editing = false
            if (!uncertain) profile?.let(::adopt)
        }
    }
    val back: () -> Unit = { if (editing) closeEditor() else onHome() }
    BackHandler(onBack = back)
    val pinnedEdit = LocalDensity.current.fontScale > 1f || mobileUiSizing().compact
    val inlineShortcuts = LocalMobileMenuShell.current && !pinnedEdit
    val status: @Composable () -> Unit = {
        if (busy) CircularProgressIndicator(color = GameTokens.Accent)
        message?.let { Text(it, color = GameTokens.Muted) }
        if (failed) {
            Text("Profil yenilənmədi. Bağlantını yoxla.", color = GameTokens.Danger)
            ProfileAction("Serverdən yenilə", enabled = !busy) { reload++ }
        }
    }
    if (editing) {
        val valid = runCatching { ProfileReplacement(profile?.revision ?: 0, name, avatar) }.isSuccess
        ProfileEditView(name, avatar, !busy && !uncertain, !busy, !busy && profile != null && valid,
            if (uncertain) "Təkrar saxla" else "Yadda saxla",
            onName = { if (it.codePointCount(0, it.length) <= PublicProfiles.MaxNameLength ||
                it.codePointCount(0, it.length) < name.codePointCount(0, name.length)) name = it },
            onAvatar = { pickingAvatar = true }, onCancel = ::closeEditor, onSave = {
                busy = true; failed = false; message = null
                scope.launch {
                    try {
                        val result = editor.save(name, avatar)
                        if (current()) {
                            uncertain = false
                            when (result) {
                                is ProfileSaveResult.Saved -> { adopt(result.profile); editing = false; message = "Profil yadda saxlanıldı." }
                                is ProfileSaveResult.Conflict -> {
                                    adopt(result.current)
                                    message = "Profil başqa yerdə dəyişib. Cari ad və avatar göstərilir. Dəyişiklik üçün yenidən Yadda saxla seç."
                                }
                            }
                        }
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { if (current()) { failed = true; uncertain = true } }
                    finally { if (current()) busy = false }
                }
            }, status = status)
    } else ProfileFrame("Profil", back, ready.snapshot.coins) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            profile?.let { value ->
                ProfileOverview(value, ready.snapshot.account?.level?.toString() ?: "—", ready.snapshot.xp,
                    ready.snapshot.account?.bestFloors?.toString() ?: "—", progress = {
                        ready.snapshot.account?.xpInterval?.let { interval ->
                            val earned = java.math.BigInteger.valueOf(ready.snapshot.xp).subtract(interval.start)
                            val required = interval.next.subtract(interval.start)
                            AccountXpProgress(earned, required, ready.snapshot.account?.level?.toString())
                        }
                    }, showEdit = !pinnedEdit, enabled = !busy,
                    onEdit = { editing = true; message = null })
            }
            status()
            if (inlineShortcuts) ProfileNavbar(onHome, onPage)
        }
        if (pinnedEdit && profile != null) ProfileAction("Dəyiş", enabled = !busy,
            modifier = Modifier.testTag("profile-edit")) { editing = true; message = null }
        if (!inlineShortcuts) ProfileNavbar(onHome, onPage)
    }
    if (pickingAvatar) ProfileAvatarPicker(PublicProfiles.avatars.toList(), avatar,
        onClose = { pickingAvatar = false }, onApply = { avatar = it; pickingAvatar = false })
}

internal fun avatarLabel(id: String) = when (id) { "builder" -> "İnşaatçı"; "pilot" -> "Pilot"; else -> "Memar" }

@Composable
internal fun communityFieldColors() = OutlinedTextFieldDefaults.colors(focusedTextColor = GameTokens.Text,
    unfocusedTextColor = GameTokens.Text, disabledTextColor = GameTokens.Muted,
    focusedLabelColor = GameTokens.Accent, unfocusedLabelColor = GameTokens.Muted,
    focusedBorderColor = GameTokens.Accent, unfocusedBorderColor = GameTokens.Muted)

@Composable
internal fun AccountXpProgress(earned: java.math.BigInteger, required: java.math.BigInteger, level: String? = null) {
    val fraction = java.math.BigDecimal(earned).divide(java.math.BigDecimal(required), java.math.MathContext.DECIMAL64)
        .toFloat().coerceIn(0f, 1f)
    val label = "$earned / $required XP"
    if (LocalDensity.current.fontScale > 1f || label.length > 20) {
        if (level != null) Text("Səviyyə $level", color = GameTokens.Text, fontWeight = FontWeight.ExtraBold)
        Text(label, color = GameTokens.Muted, fontWeight = FontWeight.ExtraBold)
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        if (level != null) Text("Səviyyə $level", Modifier.weight(1f), color = GameTokens.Text,
            fontSize = if (LocalMobileMenuShell.current || mobileUiSizing().compact) GameTokens.Body else GameTokens.Title, fontWeight = FontWeight.ExtraBold)
        Text(label, color = GameTokens.Muted,
            fontSize = if (mobileUiSizing().compact) GameTokens.Body else GameTokens.Hint, fontWeight = FontWeight.ExtraBold)
    }
    Surface(Modifier.fillMaxWidth().height(if (LocalDensity.current.fontScale > 1f) GameTokens.Target else if (LocalMobileMenuShell.current) GameTokens.Small + GameTokens.Quarter else GameTokens.Inset)
        .progressSemantics(fraction), shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Background,
        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Box {
            Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(
                androidx.compose.ui.graphics.Brush.verticalGradient(listOf(GameTokens.Text, GameTokens.Accent, GameTokens.Accent)),
                RoundedCornerShape(GameTokens.CardRadius)))
        }
    }
}
