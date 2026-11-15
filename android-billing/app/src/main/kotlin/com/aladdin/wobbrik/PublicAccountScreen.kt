package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
    BackHandler { if (editing && !busy) editing = false else onHome() }
    CommunityFrame("Hesab", onHome, coins = ready.snapshot.coins) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            if (!editing) {
                ApprovedAccountHero(Modifier.fillMaxWidth().height(mobileUiSizing().detailArt))
                profile?.let { value ->
                    Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        PublicAvatar(value.avatarId, Modifier.size(GameTokens.Target))
                        Text(value.displayName, color = GameTokens.Text, fontSize = mobileUiSizing().heading, fontWeight = FontWeight.Bold)
                    }
                    Text("Səviyyə ${ready.snapshot.account?.level ?: "—"} · ${ready.snapshot.xp} XP",
                        Modifier.align(Alignment.CenterHorizontally), color = GameTokens.Muted)
                    ready.snapshot.account?.xpInterval?.let { interval ->
                        val earned = java.math.BigInteger.valueOf(ready.snapshot.xp).subtract(interval.start)
                        val required = interval.next.subtract(interval.start)
                        AccountXpProgress(earned, required)
                    }
                    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(GameTokens.ControlRadius),
                        color = GameTokens.Surface, border = androidx.compose.foundation.BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                        Row(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(GameTokens.Small, Alignment.CenterHorizontally)) {
                            Image(painterResource(R.drawable.ui_crown), null, Modifier.size(GameTokens.Inset))
                            Text("Rekord ${ready.snapshot.account?.bestFloors ?: "—"}", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                        }
                    }
                    CommunityLink("Profili dəyiş", R.drawable.ui_account, enabled = !busy) { editing = true; message = null }
                }
                CommunityLink("Nailiyyətlər", R.drawable.ui_trophy) { onPage("achievements") }
                CommunityLink("Rekordlar", R.drawable.ui_crown) { onPage("leaderboard") }
                CommunityLink("Parametrlər", R.drawable.ui_account) { onPage("settings") }
            } else {
                Text("Oyunçu adı", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                OutlinedTextField(name, { if (it.codePointCount(0, it.length) <= PublicProfiles.MaxNameLength ||
                    it.codePointCount(0, it.length) < name.codePointCount(0, name.length)) name = it }, modifier = Modifier.fillMaxWidth(), enabled = !busy && !uncertain,
                    singleLine = true, label = { Text("Ad") }, supportingText = { Text("1–15 simvol", color = GameTokens.Muted) },
                    colors = communityFieldColors())
                PublicProfiles.avatars.forEach { id ->
                    Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        PublicAvatar(id, Modifier.size(GameTokens.Target))
                        HomeButton(avatarLabel(id), primary = avatar == id, enabled = !busy && !uncertain,
                            modifier = Modifier.weight(1f)) { avatar = id }
                    }
                }
            }
            if (busy) CircularProgressIndicator(color = GameTokens.Accent)
            message?.let { Text(it, color = GameTokens.Muted) }
            if (failed) {
                Text("Profil yenilənmədi. Bağlantını yoxla.", color = GameTokens.Danger)
                HomeButton("Serverdən yenilə", enabled = !busy) { reload++ }
            }
        }
        if (editing) {
            val valid = runCatching { ProfileReplacement(profile?.revision ?: 0, name, avatar) }.isSuccess
            HomeButton(if (uncertain) "Təkrar saxla" else "Yadda saxla", primary = true,
                enabled = !busy && profile != null && valid) {
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
            }
            HomeButton("Geri", enabled = !busy) { editing = false }
        } else HomeButton("Ana ekran", primary = true, icon = R.drawable.ui_home, onClick = onHome)
    }
}

internal fun avatarLabel(id: String) = when (id) { "builder" -> "İnşaatçı"; "pilot" -> "Pilot"; else -> "Memar" }

@Composable
internal fun communityFieldColors() = OutlinedTextFieldDefaults.colors(focusedTextColor = GameTokens.Text,
    unfocusedTextColor = GameTokens.Text, disabledTextColor = GameTokens.Muted,
    focusedLabelColor = GameTokens.Accent, unfocusedLabelColor = GameTokens.Muted,
    focusedBorderColor = GameTokens.Accent, unfocusedBorderColor = GameTokens.Muted)

@Composable
private fun AccountXpProgress(earned: java.math.BigInteger, required: java.math.BigInteger) {
    val fraction = java.math.BigDecimal(earned).divide(java.math.BigDecimal(required), java.math.MathContext.DECIMAL64)
        .toFloat().coerceIn(0f, 1f)
    val label = "$earned / $required XP"
    val separateLabel = earned.toString().length + required.toString().length > 12
    if (separateLabel) Text(label, color = GameTokens.Muted)
    Surface(Modifier.fillMaxWidth().height(if (LocalDensity.current.fontScale > 1f) GameTokens.Target else GameTokens.Inset)
        .progressSemantics(fraction), shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Background,
        border = BorderStroke(GameTokens.Hairline, GameTokens.Muted)) {
        Box {
            Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(GameTokens.Accent, RoundedCornerShape(GameTokens.CardRadius)))
            if (!separateLabel) {
                Text(label, Modifier.align(Alignment.Center), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                Box(Modifier.matchParentSize().clearAndSetSemantics {}.drawWithContent {
                    clipRect(right = size.width * fraction) { this@drawWithContent.drawContent() }
                }) {
                    Text(label, Modifier.align(Alignment.Center), color = GameTokens.Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CommunityLink(label: String, icon: Int, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(onClick = onClick, enabled = enabled, shape = RoundedCornerShape(GameTokens.ControlRadius),
        color = GameTokens.Surface, border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
        Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)
            .background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface))).padding(GameTokens.Small),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Image(painterResource(icon), null, Modifier.size(GameTokens.Gap * 2))
            Text(label, Modifier.weight(1f), color = if (enabled) GameTokens.Text else GameTokens.Muted, fontWeight = FontWeight.Bold)
            Text("›", color = GameTokens.Text, fontSize = GameTokens.Title)
        }
    }
}
