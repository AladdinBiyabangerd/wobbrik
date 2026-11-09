package com.aladdin.wobbrik

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import appbackend.wobbrik.game.UnreadableAttemptJournal
import appbackend.wobbrik.game.GameSnapshot
import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import com.aladdin.wobbrik.engine.*
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.math.BigDecimal
import java.math.MathContext

/** UI reads server recovery; only the application host may own accepted physics. */
@Composable
internal fun OnlineGame(app: WobbrikApplication) {
    val account by app.account.collectAsState()
    val configured = app.bootstrap as? WobbrikApplication.Bootstrap.Configured
    // Ready is a capability: structural equality must not retain an old auth epoch.
    // Read the canonical object every frame; StateFlow/Compose conflation may skip
    // a short Recovering transition between two equal server snapshots.
    var state by remember(configured) {
        mutableStateOf(configured?.recovery?.state?.value, referentialEqualityPolicy())
    }
    // Display-only last accepted snapshot of the current uid; never a capability.
    var cached by remember(configured) { mutableStateOf<GameSnapshot?>(null) }
    var page by rememberSaveable { mutableStateOf("home") }
    val host = app.engineHost
    var hostState by remember { mutableStateOf(host.snapshot()) }
    var linkMessage by remember { mutableStateOf<String?>(null) }
    var linking by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? ComponentActivity
    val scope = rememberCoroutineScope()
    // Snapshot has no Compose dependency. Poll even when paused so lifecycle revocation is visible.
    LaunchedEffect(host, configured) {
        while (true) withFrameNanos { now ->
            val canonical = configured?.recovery?.state?.value
            state = canonical
            cached = configured?.recovery?.cached?.value
            host.onFrame(now, canonical as? AuthenticatedRecoveryOwner.State.Ready)
            // Restart/abandon policy (9 November 2026): a left mid-game attempt is abandoned on the next Ready.
            host.enforceLeave(canonical as? AuthenticatedRecoveryOwner.State.Ready)
            hostState = host.snapshot()
        }
    }
    BackHandler(page != "home") { host.pause(); page = "home" }
    val ready = state as? AuthenticatedRecoveryOwner.State.Ready
    val restore = AcceptedAttemptRestore.from(state ?: AuthenticatedRecoveryOwner.State.SignedOut)
    // Restart/abandon policy (user decision, 9 November 2026): leaving the game page by any route
    // (Back, "Menyuya qayıt", finish picker back) leaves the attempt. The host keeps a loss for its
    // finish and abandons an open mid-game attempt. A "game" page restored after process death was
    // left when the app closed: go home; Play then abandons that attempt instead of resuming it.
    LaunchedEffect(page) {
        if (page != "game") host.leave(state as? AuthenticatedRecoveryOwner.State.Ready)
        else if (!host.snapshot().playing) page = "home"
    }
    // Play pressed while an attempt from a closed app was still open: start once it is abandoned.
    var startAfterDiscard by remember(configured) { mutableStateOf(false) }
    LaunchedEffect(startAfterDiscard, state) {
        if (!startAfterDiscard) return@LaunchedEffect
        val current = state
        when {
            current is AuthenticatedRecoveryOwner.State.Error -> startAfterDiscard = false
            current is AuthenticatedRecoveryOwner.State.Ready -> when (AcceptedAttemptRestore.from(current)) {
                AcceptedAttemptRestore.Result.NoAttempt, is AcceptedAttemptRestore.Result.Finished -> {
                    startAfterDiscard = false
                    if (configured?.commands?.start(current) == true) { host.enter(); page = "game" }
                }
                else -> if (!host.leavePending()) startAfterDiscard = false
            }
            else -> Unit
        }
    }
    if (page == "game" && hostState.playing && ready != null && hostState.scene != null) {
        if (hostState.choosingFinish) FinishCosmeticScreen(hostState, ready,
            onSelect = { category, id -> host.selectPart(ready, category, id) },
            onConfirm = { host.confirmFinish(ready) }, onBack = { page = "home" })
        else AcceptedScene(hostState, ready, host, onHome = { page = "home" })
        return
    }
    val journalBlocked = (state as? AuthenticatedRecoveryOwner.State.Error)?.cause is UnreadableAttemptJournal
    val error = account is WobbrikApplication.AccountState.Error || state is AuthenticatedRecoveryOwner.State.Error
    val status = when {
        app.bootstrap is WobbrikApplication.Bootstrap.MissingFirebase -> "Firebase konfiqurasiyası yoxdur"
        app.bootstrap is WobbrikApplication.Bootstrap.MissingBackendOrigin -> "Server ünvanı konfiqurasiya edilməyib"
        journalBlocked -> JournalRecoveryCopy.Title
        error -> "Bağlantı yoxdur"
        ready == null -> "Hesab və cəhd bərpa olunur…"
        restore is AcceptedAttemptRestore.Result.Blocked -> "Cəhd bu versiyada bərpa edilə bilmir"
        else -> null
    }
    val guest = (account as? WobbrikApplication.AccountState.Session)?.anonymous == true
    AccountScreen(page, ready, account, status,
        onPage = { page = it; linkMessage = null },
        onRetry = {
            linkMessage = null
            if (account is WobbrikApplication.AccountState.Error) app.retryAccount()
            else configured?.recovery?.onForeground()
        },
        onPlay = if (ready != null && restore !is AcceptedAttemptRestore.Result.Blocked) ({
            when (restore) {
                AcceptedAttemptRestore.Result.NoAttempt, is AcceptedAttemptRestore.Result.Finished -> {
                    if (configured?.commands?.start(ready) == true) { host.enter(); page = "game" }
                }
                // Only a loss is resumable (its cosmetic finish). An open attempt is never resumed.
                is AcceptedAttemptRestore.Result.Paused ->
                    if (restore.terminal || hostState.choosingFinish) { host.enter(); page = "game" }
                    else if (host.discardOpen(ready)) startAfterDiscard = true
                else -> Unit
            }
        }) else null,
        active = restore is AcceptedAttemptRestore.Result.Paused && (restore.terminal || hostState.choosingFinish),
        onLinkGoogle = if (guest && configured != null && activity != null && !linking) ({
            linking = true
            linkMessage = null
            scope.launch {
                try {
                    val clientId = GoogleGuestLink.serverClientId(activity)
                    app.linkGuest(GoogleGuestLink.googleCredential(activity, clientId))
                } catch (failure: Throwable) {
                    GoogleGuestLink.userMessage(failure)?.let { linkMessage = it }
                } finally {
                    linking = false
                }
            }
        }) else null,
        linking = linking,
        linkMessage = linkMessage,
        journalBlocked = journalBlocked,
        cached = if (ready == null && account is WobbrikApplication.AccountState.Session) cached else null)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountScreen(
    page: String,
    ready: AuthenticatedRecoveryOwner.State.Ready?,
    account: WobbrikApplication.AccountState,
    status: String?,
    onPage: (String) -> Unit,
    onRetry: () -> Unit,
    onPlay: (() -> Unit)?,
    active: Boolean,
    onLinkGoogle: (() -> Unit)? = null,
    linking: Boolean = false,
    linkMessage: String? = null,
    journalBlocked: Boolean = false,
    // Shown in the existing value slots only; Play, commands and status still follow [ready].
    cached: GameSnapshot? = null,
) {
    var journalDetailsClosed by rememberSaveable(journalBlocked) { mutableStateOf(false) }
    if (journalBlocked && (!journalDetailsClosed || page != "home")) {
        JournalRecoveryScreen(onRetry, onHome = { journalDetailsClosed = true; onPage("home") })
        return
    }
    val snapshot = ready?.snapshot ?: cached
    if (page == "home") {
        HomeScreen(snapshot, active, if (journalBlocked) null else onPlay, onPage, status, onRetry, journalBlocked)
        return
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding()) {
        // Responsive rule: short wide windows use the compact spacing/type tier.
        val compactOuter = maxWidth > maxHeight && maxHeight < GameTokens.CompactHeight
        BoxWithConstraints(Modifier.fillMaxSize().padding(if (compactOuter) GameTokens.Gap else GameTokens.Inset)) {
        val controlsMaxHeight = maxHeight / 2
        val expanded = maxWidth >= GameTokens.ExpandedWidth
        val wide = maxWidth > maxHeight
        // Approved design C2 (docs/design/account-landscape-actions-v1): short wide windows (phone landscape).
        val threePane = wide && maxHeight < GameTokens.CompactHeight
        // Type tier: compact (Hint) in short wide windows, regular (Title) elsewhere.
        val titleSize = if (threePane) GameTokens.Hint else GameTokens.Title
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
            // Approved v3 (9 November): three-pane moves the logo into the hero pane, so there is no header row.
            if (!threePane) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.widthIn(max = GameTokens.PanelWidth / 2).fillMaxWidth().height(GameTokens.Target), contentScale = ContentScale.Fit)
                }
                // User decision (9 November): the three-pane layout drops the coin chip and profile button;
                // "Ana ekran" in the action pane returns home, where both are shown.
                if (!threePane) Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Background,
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                    Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        ArtIcon(R.drawable.ui_coin)
                        Text(snapshot?.coins?.toString() ?: "—", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    }
                }
                if (!threePane) IconButton(onClick = { onPage(if (page == "account") "home" else "account") }, modifier = Modifier.size(GameTokens.Target).background(GameTokens.Raised, RoundedCornerShape(GameTokens.CardRadius))) {
                    Image(painterResource(R.drawable.ui_account), if (page == "account") "Ana ekran" else "Hesab", Modifier.size(GameTokens.Target - GameTokens.Gap))
                }
            }
            val hero: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(if (threePane) GameTokens.Small else GameTokens.Gap)) {
                    if (threePane) Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.fillMaxWidth().height(GameTokens.Target), contentScale = ContentScale.Fit)
                    if (status == null) Text(when(page) { "account" -> "Hesab"; "achievements" -> "Nailiyyətlər"; "collection" -> "Kolleksiya"; else -> "Öz şəhərini qur" }, fontSize = titleSize, fontWeight = FontWeight.Bold, color = GameTokens.Text)
                    Image(painterResource(if (status == "Bağlantı yoxdur") R.drawable.recovery_hero else if (page == "account") R.drawable.account_hero else R.drawable.home_city),
                        if (page == "account") "İnşaatçı və şəhər" else "Wobbrik şəhəri",
                        Modifier.fillMaxWidth().weight(1f), contentScale = ContentScale.Fit)
                }
            }
            val info: @Composable ColumnScope.() -> Unit = {
                    if (page == "account") Text(if ((account as? WobbrikApplication.AccountState.Session)?.anonymous == true) "Qonaq inşaatçı" else "İnşaatçı",
                        color = GameTokens.Text, fontSize = titleSize, fontWeight = FontWeight.Bold)
                    if (snapshot != null) Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
                        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                        Column(Modifier.fillMaxWidth().padding(if (threePane) GameTokens.Small + GameTokens.Quarter else GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(if (threePane) GameTokens.Quarter else GameTokens.Small)) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap),
                                verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                                    ArtIcon(R.drawable.ui_level)
                                    Text("Səviyyə ${snapshot.account?.level ?: "—"}",
                                        color = GameTokens.Text, fontWeight = FontWeight.Bold)
                                }
                                if (page == "home") Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                                    ArtIcon(R.drawable.ui_crown)
                                    Text("Rekord ${snapshot.account?.bestFloors ?: "—"}",
                                        color = GameTokens.Text, fontWeight = FontWeight.Bold)
                                }
                            }
                            val interval = snapshot.account?.xpInterval
                            if (page != "home") {
                                Text("${snapshot.xp} XP", color = GameTokens.Muted)
                            } else if (interval != null) {
                                // Subtract as integers before converting only the drawing fraction.
                                val earned = BigInteger.valueOf(snapshot.xp).subtract(interval.start)
                                val required = interval.next.subtract(interval.start)
                                val fraction = BigDecimal(earned).divide(BigDecimal(required), MathContext.DECIMAL64)
                                    .toFloat().coerceIn(0f, 1f)
                                Text("$earned / $required XP", color = GameTokens.Muted)
                                LinearProgressIndicator(progress = { fraction },
                                    modifier = Modifier.fillMaxWidth(), color = GameTokens.Accent,
                                    trackColor = GameTokens.Background)
                            } else {
                                Text("XP —", color = GameTokens.Muted)
                            }
                            if (page == "account") {
                                Text("Nailiyyətlər", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                                Text(snapshot?.account?.achievements?.let { if (it.isEmpty()) "Hələ nailiyyət yoxdur" else "${it.size} nailiyyət açılıb" } ?: "—", color = GameTokens.Muted)
                            }
                        }
                    }
                    if (page == "account" && onLinkGoogle != null) {
                        OnlineAction(if (linking) "Google bağlanır…" else "Google ilə bağla", true,
                            enabled = !linking, onClick = onLinkGoogle)
                        Text("Eyni oyun hesabı saxlanır; balansı başqa hesaba birləşdirmir.",
                            color = GameTokens.Muted, fontSize = GameTokens.Body)
                        if (linkMessage != null) Text(linkMessage, color = GameTokens.Danger, fontSize = GameTokens.Body)
                    }
                    if (page == "achievements" || page == "collection") {
                        val items = if (page == "achievements") snapshot?.account?.achievements else snapshot?.account?.unlocks
                        Text(items?.let { if (it.isEmpty()) "Hələ açılmış mükafat yoxdur" else "${it.size} mükafat açılıb" } ?: "—", color = GameTokens.Muted)
                    }
                    if (snapshot?.attempt?.active == false) {
                        Text("Cəhd bitdi · ${snapshot.attempt?.floors} mərtəbə", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    }
            }
            val secondary: @Composable (String, Int, String, Modifier) -> Unit = { label, icon, target, modifier ->
                Button(onClick = { onPage(target) }, modifier = modifier.heightIn(min = GameTokens.Target), shape = RoundedCornerShape(GameTokens.ControlRadius), colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Raised)) {
                    ArtIcon(icon)
                    Text(label, color = GameTokens.Text)
                }
            }
            // stacked: full-width Kolleksiya/Nailiyyətlər so no label wraps in the narrow action pane.
            val actions: @Composable ColumnScope.(Boolean) -> Unit = { stacked ->
                    if (status != null) {
                        Text(status, color = GameTokens.Text, fontSize = titleSize, fontWeight = FontWeight.Bold,
                            // Centre only in the narrow three-pane action column, where the status can wrap.
                            textAlign = if (stacked) TextAlign.Center else null)
                        if (status == "Hesab və cəhd bərpa olunur…") {
                            CircularProgressIndicator(color = GameTokens.Accent)
                        }
                        OnlineAction("Yenilə", true, onClick = onRetry)
                    }
                    if (status == null) OnlineAction(if (active) "Cəhdə qayıt" else "Oyna", true, enabled = onPlay != null, onClick = { onPlay?.invoke() })
                    if (status == null && stacked) Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        secondary("Kolleksiya", R.drawable.ui_medal, "collection", Modifier.fillMaxWidth())
                        secondary("Nailiyyətlər", R.drawable.ui_trophy, "achievements", Modifier.fillMaxWidth())
                    } else if (status == null) Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        secondary("Kolleksiya", R.drawable.ui_medal, "collection", Modifier.weight(1f))
                        secondary("Nailiyyətlər", R.drawable.ui_trophy, "achievements", Modifier.weight(1f))
                    }
                    if (page != "home") OnlineAction("Ana ekran", false, onClick = { onPage("home") })
            }
            val controls: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Gap), horizontalAlignment = Alignment.CenterHorizontally) {
                    info()
                    actions(false)
                }
            }
            if (threePane) Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
                hero(Modifier.weight(1f).fillMaxHeight())
                CenteredPane(Modifier.weight(1f), GameTokens.Small) { info() }
                CenteredPane(Modifier.weight(1f), GameTokens.Small) { actions(true) }
            } else if (wide) Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(GameTokens.Inset), verticalAlignment = Alignment.CenterVertically) {
                hero(Modifier.weight(if (expanded) 2f else 1f).fillMaxHeight())
                controls(Modifier.weight(1f))
            } else {
                hero(Modifier.weight(1f).fillMaxWidth())
                controls(Modifier.fillMaxWidth().heightIn(max = controlsMaxHeight))
            }
        }
        }
    }
}

/** Vertically centred pane; scrolls only when its content is taller than the pane (e.g. large fonts). */
@Composable
private fun CenteredPane(modifier: Modifier, spacing: androidx.compose.ui.unit.Dp = GameTokens.Gap, content: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(modifier.fillMaxHeight()) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
            verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally, content = content)
    }
}

@Composable
internal fun AcceptedScene(view: AcceptedEngineHost.Snapshot, ready: AuthenticatedRecoveryOwner.State.Ready,
                          host: AcceptedEngineHost, onHome: () -> Unit) {
    val scene = view.scene ?: return
    val art = SceneArt(ImageBitmap.imageResource(R.drawable.game_floor), ImageBitmap.imageResource(R.drawable.game_crane),
        ImageBitmap.imageResource(R.drawable.game_island), ImageBitmap.imageResource(R.drawable.game_roof), ImageBitmap.imageResource(R.drawable.game_cloud))
    val motion = remember(ready) { SceneMotion() }
    val density = LocalDensity.current
    var hudHeight by remember { mutableStateOf(GameTokens.Target + GameTokens.Inset) }
    // Host is the only simulation clock. Motion merely follows accepted scene coordinates.
    motion.advance(scene, 1f / 60f, view.cameraX)
    BoxWithConstraints(Modifier.fillMaxSize().background(GameTokens.Background).safeDrawingPadding()) {
        val sceneTop = if (maxWidth > maxHeight) GameTokens.Hairline * 0 else hudHeight
        Canvas(Modifier.fillMaxSize().padding(top = sceneTop, bottom = GameTokens.Target).clipToBounds().clickable(enabled = view.inputEnabled) { host.drop(ready) }) {
            drawGameScene(scene, art, motion)
        }
        Row(Modifier.fillMaxWidth().onSizeChanged { hudHeight = with(density) { it.height.toDp() } }.padding(GameTokens.Inset), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${scene.floors.size} mərtəbə", color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
            Button(onClick = { host.pause() }, modifier = Modifier.size(GameTokens.Target).semantics { contentDescription = "Fasilə" },
                shape = CircleShape, contentPadding = PaddingValues(GameTokens.Small),
                colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Raised, contentColor = GameTokens.Text)) { Text("Ⅱ") }
        }
        if (view.inputEnabled) Text("Buraxmaq üçün toxun", color = GameTokens.Text,
            modifier = Modifier.align(Alignment.BottomCenter).padding(GameTokens.Gap))
        if (scene.paused || view.waiting) Box(Modifier.fillMaxSize().background(GameTokens.Background.copy(alpha = GameTokens.DimOpacity)))
        if (scene.paused || view.waiting) Column(Modifier.align(Alignment.Center).padding(GameTokens.Inset)
            .widthIn(max = GameTokens.PanelWidth).background(GameTokens.Surface, RoundedCornerShape(GameTokens.CardRadius))
            .verticalScroll(rememberScrollState()).padding(GameTokens.Inset), verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
            Text(if (view.waiting) "Cəhd saxlanılır…" else "Fasilə", color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
            OnlineAction("Davam et", true, enabled = !view.waiting && (view.restore as? AcceptedAttemptRestore.Result.Paused)?.terminal == false) { host.resume(ready) }
            OnlineAction("Menyuya qayıt", false) { host.pause(); onHome() }
        }
    }
}

@Composable
private fun OnlineAction(label: String, primary: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)
            .background(Brush.verticalGradient(if (primary) listOf(GameTokens.Accent, GameTokens.Accent) else listOf(GameTokens.Raised, GameTokens.Surface)), RoundedCornerShape(GameTokens.ControlRadius)),
        border = BorderStroke(GameTokens.Hairline, if (primary) GameTokens.Text else GameTokens.Raised),
        shape = RoundedCornerShape(GameTokens.ControlRadius),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent,
            contentColor = if (primary) GameTokens.Background else GameTokens.Text)) {
        Text(label, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ArtIcon(resource: Int) {
    Image(painterResource(resource), null, Modifier.size(GameTokens.Inset), contentScale = ContentScale.Fit)
}
