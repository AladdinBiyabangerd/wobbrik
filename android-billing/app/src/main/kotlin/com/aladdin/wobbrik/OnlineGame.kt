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
import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import com.aladdin.wobbrik.engine.*
import kotlinx.coroutines.launch

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
            host.onFrame(now, canonical as? AuthenticatedRecoveryOwner.State.Ready)
            hostState = host.snapshot()
        }
    }
    BackHandler(page != "home") { host.pause(); page = "home" }
    val ready = state as? AuthenticatedRecoveryOwner.State.Ready
    val restore = AcceptedAttemptRestore.from(state ?: AuthenticatedRecoveryOwner.State.SignedOut)
    if (page == "game" && ready != null && hostState.scene != null) {
        AcceptedScene(hostState, ready, host, onHome = { page = "home" })
        return
    }
    val error = account is WobbrikApplication.AccountState.Error || state is AuthenticatedRecoveryOwner.State.Error
    val status = when {
        app.bootstrap is WobbrikApplication.Bootstrap.MissingFirebase -> "Firebase konfiqurasiyası yoxdur"
        app.bootstrap is WobbrikApplication.Bootstrap.MissingBackendOrigin -> "Server ünvanı konfiqurasiya edilməyib"
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
                    if (configured?.commands?.start(ready) == true) page = "game"
                }
                is AcceptedAttemptRestore.Result.Paused -> { page = "game" }
                else -> Unit
            }
        }) else null,
        active = restore is AcceptedAttemptRestore.Result.Paused,
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
        linkMessage = linkMessage)
}

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
) {
    val snapshot = ready?.snapshot
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Inset)) {
        val controlsMaxHeight = maxHeight / 2
        val expanded = maxWidth >= GameTokens.ExpandedWidth
        val wide = maxWidth > maxHeight
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Small), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik", Modifier.widthIn(max = GameTokens.PanelWidth / 2).fillMaxWidth().height(GameTokens.Target), contentScale = ContentScale.Fit)
                }
                Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Background,
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                    Row(Modifier.padding(GameTokens.Small), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        ArtIcon(R.drawable.ui_coin)
                        Text(snapshot?.coins?.toString() ?: "—", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = { onPage(if (page == "account") "home" else "account") }, modifier = Modifier.size(GameTokens.Target).background(GameTokens.Raised, RoundedCornerShape(GameTokens.CardRadius))) {
                    Image(painterResource(R.drawable.ui_account), if (page == "account") "Ana ekran" else "Hesab", Modifier.size(GameTokens.Target - GameTokens.Gap))
                }
            }
            val hero: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
                    if (status == null) Text(when(page) { "account" -> "Hesab"; "achievements" -> "Nailiyyətlər"; "collection" -> "Kolleksiya"; else -> "Öz şəhərini qur" }, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold, color = GameTokens.Text)
                    Image(painterResource(if (status == "Bağlantı yoxdur") R.drawable.recovery_hero else if (page == "account") R.drawable.account_hero else R.drawable.home_city),
                        if (page == "account") "İnşaatçı və şəhər" else "Wobbrik şəhəri",
                        Modifier.fillMaxWidth().weight(1f), contentScale = ContentScale.Fit)
                }
            }
            val controls: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GameTokens.Gap), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (page == "account") Text(if ((account as? WobbrikApplication.AccountState.Session)?.anonymous == true) "Qonaq inşaatçı" else "İnşaatçı",
                        color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                    if (snapshot != null) Surface(shape = RoundedCornerShape(GameTokens.CardRadius), color = GameTokens.Surface,
                        border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                        Column(Modifier.fillMaxWidth().padding(GameTokens.Gap), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                                ArtIcon(R.drawable.ui_level)
                                Text(snapshot?.account?.let { "Səviyyə ${it.level}" } ?: "Səviyyə —", color = GameTokens.Text, fontWeight = FontWeight.Bold)
                                Text(snapshot?.let { "${it.xp} XP" } ?: "XP —", color = GameTokens.Muted)
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
                    if (status != null) {
                        Text(status, color = GameTokens.Text, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                        if (status == "Hesab və cəhd bərpa olunur…") {
                            CircularProgressIndicator(color = GameTokens.Accent)
                        }
                        OnlineAction("Yenilə", true, onClick = onRetry)
                    }
                    if (status == null) OnlineAction(if (active) "Cəhdə qayıt" else "Oyna", true, enabled = onPlay != null, onClick = { onPlay?.invoke() })
                    if (status == null) Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Button(onClick = { onPage("collection") }, modifier = Modifier.weight(1f).heightIn(min = GameTokens.Target), shape = RoundedCornerShape(GameTokens.ControlRadius), colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Raised)) {
                            ArtIcon(R.drawable.ui_medal)
                            Text("Kolleksiya", color = GameTokens.Text)
                        }
                        Button(onClick = { onPage("achievements") }, modifier = Modifier.weight(1f).heightIn(min = GameTokens.Target), shape = RoundedCornerShape(GameTokens.ControlRadius), colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Raised)) {
                            ArtIcon(R.drawable.ui_trophy)
                            Text("Nailiyyətlər", color = GameTokens.Text)
                        }
                    }
                    if (page != "home") OnlineAction("Ana ekran", false, onClick = { onPage("home") })
                }
            }
            if (wide) Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(GameTokens.Inset), verticalAlignment = Alignment.CenterVertically) {
                hero(Modifier.weight(if (expanded) 2f else 1f).fillMaxHeight())
                controls(Modifier.weight(1f))
            } else {
                hero(Modifier.weight(1f).fillMaxWidth())
                controls(Modifier.fillMaxWidth().heightIn(max = controlsMaxHeight))
            }
        }
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
