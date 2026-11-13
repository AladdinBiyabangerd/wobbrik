package com.aladdin.wobbrik

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aladdin.wobbrik.engine.*
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        (application as WobbrikApplication).engineHost.onForeground()
    }

    override fun onPause() {
        // Process ON_STOP is delayed; revoke timing/input at the actual activity pause.
        (application as WobbrikApplication).engineHost.onPause()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RemoteConfigInit.start(applicationContext)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent { MaterialTheme(colorScheme = darkColorScheme(
            primary = GameTokens.Accent, onPrimary = GameTokens.Background,
            surface = GameTokens.Surface, onSurface = GameTokens.Text,
            background = GameTokens.Background, onBackground = GameTokens.Text,
        )) { OnlineGame(application as WobbrikApplication) } }
    }
}

@Composable
internal fun PracticeGame(
    engine: PracticeEngine,
    renderOnly: Boolean = false,
    previewMotion: SceneMotion? = null,
    frameTiming: FrameTiming = remember { FrameTiming() },
) {
    // Coordinates are read in the draw phase; HUD only observes discrete changes.
    val snapshot = remember { mutableStateOf(engine.state) }
    val motion = remember { previewMotion ?: SceneMotion().also { it.advance(engine.state, 0f, engine.cameraX) } }
    var motionFrame by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(engine.state.paused) }
    var menu by remember { mutableStateOf(false) }
    var count by remember { mutableIntStateOf(engine.state.floors.size) }
    var finished by remember { mutableStateOf(engine.state.phase == Phase.FINISHED) }
    var best by rememberSaveable { mutableIntStateOf(engine.state.floors.size) }
    var learned by rememberSaveable { mutableStateOf(false) }
    var fpsLine by remember { mutableStateOf("") }
    val art = SceneArt(
        ImageBitmap.imageResource(R.drawable.game_floor), ImageBitmap.imageResource(R.drawable.game_crane),
        ImageBitmap.imageResource(R.drawable.inventory_garden), ImageBitmap.imageResource(R.drawable.game_roof),
        ImageBitmap.imageResource(R.drawable.game_cloud), rememberGameAtlases())
    val density = LocalDensity.current
    var hudHeight by remember { mutableStateOf(GameTokens.Target + GameTokens.Inset) }
    fun publish() {
        snapshot.value = engine.state
        count = engine.state.floors.size
        best = max(best, count)
        finished = engine.state.phase == Phase.FINISHED
    }
    fun pause() {
        engine.pause()
        paused = true
        val summary = frameTiming.summary()
        if (summary.frames > 0) {
            fpsLine = summary.line()
            Log.i("WobbrikFrameTiming", summary.line())
        }
        publish()
    }
    fun restart() {
        engine.restart()
        motion.reset()
        frameTiming.reset()
        fpsLine = ""
        paused = false
        menu = false
        publish()
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) pause()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    BackHandler(enabled = !menu) { pause() }
    LaunchedEffect(paused, menu, finished) {
        var previous = 0L
        var sinceLog = 0
        while (!renderOnly && !paused && !menu && (!finished || !motion.settled)) {
            withFrameNanos { now ->
                if (previous != 0L) {
                    val delta = now - previous
                    frameTiming.record(delta)
                    engine.advance(delta)
                    motion.advance(engine.state, delta / 1_000_000_000f, engine.cameraX)
                    motionFrame++
                    sinceLog++
                    if (sinceLog >= 120) {
                        fpsLine = frameTiming.summary().line()
                        sinceLog = 0
                    }
                    publish()
                }
                previous = now
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Background, GameTokens.Surface)))) {
        // Wide windows share the top scene with the corner HUD, as in tablet-landscape.
        val sceneTop = if (maxWidth > maxHeight) 0.dp else hudHeight
        if (!menu) {
            Canvas(Modifier.fillMaxSize().safeDrawingPadding()
                .padding(top = sceneTop, bottom = GameTokens.Target)
                .clipToBounds()
                .semantics {
                    contentDescription = "Tikinti səhnəsi"
                    if (!paused && !finished) onClick("Mərtəbəni burax") {
                        val accepted = engine.drop(); if (accepted) learned = true; publish(); accepted
                    }
                }
                .clickable(enabled = !paused && !finished, indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                    if (engine.drop()) learned = true
                    publish()
                }) { motionFrame; drawGameScene(snapshot.value, art, motion) }
            Row(Modifier.fillMaxWidth().safeDrawingPadding()
                .onSizeChanged { hudHeight = with(density) { it.height.toDp() } }.padding(GameTokens.Inset),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(end = GameTokens.Small)) {
                    Text("$count mərtəbə", color = GameTokens.Text, fontSize = GameTokens.Score, fontWeight = FontWeight.Bold)
                    Text("Rekord $best", color = GameTokens.Muted, fontSize = GameTokens.Body, fontWeight = FontWeight.Medium,
                        modifier = Modifier.semantics { contentDescription = "Məşq rekordu $best" })
                }
                Button(onClick = { pause() }, modifier = Modifier.size(GameTokens.Target),
                    contentPadding = PaddingValues(0.dp), shape = CircleShape,
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Text.copy(alpha = 0.35f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = GameTokens.Small),
                    colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Raised, contentColor = GameTokens.Text)) {
                    Text("Ⅱ", fontSize = GameTokens.Title, fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { contentDescription = "Fasilə" })
                }
            }
            if (!learned && !finished && !paused) Text("Buraxmaq üçün toxun", color = GameTokens.Text,
                fontSize = GameTokens.Hint, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(GameTokens.Inset))
            if (BuildConfig.DEBUG && fpsLine.isNotEmpty() && !paused && !menu) {
                Text(fpsLine, color = GameTokens.Muted, fontSize = GameTokens.Body,
                    modifier = Modifier.align(Alignment.BottomStart).safeDrawingPadding()
                        .padding(GameTokens.Inset).semantics { contentDescription = "Frame timing $fpsLine" })
            }
        }
        if (menu || paused || finished) {
            // Full-window input barrier: outside taps never reach the scene.
            Box(Modifier.fillMaxSize().background(GameTokens.Background.copy(alpha = GameTokens.DimOpacity))
                .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {})
            Column(Modifier.align(Alignment.Center).safeDrawingPadding().padding(GameTokens.Inset)
                .widthIn(max = GameTokens.PanelWidth).fillMaxWidth()
                .shadow(GameTokens.PanelElevation, RoundedCornerShape(GameTokens.CardRadius))
                .background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface)), RoundedCornerShape(GameTokens.CardRadius))
                .border(GameTokens.Hairline, GameTokens.Accent.copy(alpha = 0.28f), RoundedCornerShape(GameTokens.CardRadius))
                .verticalScroll(rememberScrollState()).padding(GameTokens.Inset),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Gap), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(GameTokens.Gap), verticalAlignment = Alignment.CenterVertically) {
                    if (!menu && !finished) Text("›", color = GameTokens.Accent, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clearAndSetSemantics {})
                    Text(if (menu) "Wobbrik" else if (finished) "Cəhd bitdi" else "Fasilə", color = GameTokens.Text,
                        fontSize = GameTokens.Title, fontWeight = FontWeight.Bold)
                    if (!menu && !finished) Text("‹", color = GameTokens.Accent, fontSize = GameTokens.Title, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clearAndSetSemantics {})
                }
                if (menu || finished) Text(if (menu) "Offline məşq · mükafatsız" else "$count mərtəbə · məşq nəticəsi",
                    color = GameTokens.Muted, fontWeight = FontWeight.Medium)
                if (!menu && !finished) Action("Davam et", true) { engine.resume(); paused = false; publish() }
                Action(if (menu) "Məşqə başla" else "Yenidən başla", menu || finished) { restart() }
                if (!menu) Action("Menyuya qayıt", false) { engine.pause(); paused = false; menu = true; publish() }
            }
        }
    }
}

@Composable
private fun Action(label: String, primary: Boolean, action: () -> Unit) {
    val fill = if (primary) listOf(GameTokens.Accent, GameTokens.Success) else listOf(GameTokens.Raised, GameTokens.Surface)
    Button(onClick = action, modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)
            .clip(RoundedCornerShape(GameTokens.ControlRadius))
            .background(Brush.verticalGradient(fill))
            .then(if (primary) Modifier.shadow(GameTokens.Small, RoundedCornerShape(GameTokens.ControlRadius)) else Modifier),
        shape = RoundedCornerShape(GameTokens.ControlRadius),
        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = if (primary) GameTokens.Background else GameTokens.Text),
        border = BorderStroke(GameTokens.Hairline, if (primary) GameTokens.Text.copy(alpha = 0.45f) else GameTokens.Muted.copy(alpha = 0.4f))) {
        Text(label, fontSize = GameTokens.Body, fontWeight = FontWeight.Bold)
    }
}
