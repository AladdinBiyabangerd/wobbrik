package com.aladdin.wobbrik

import android.os.Bundle
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import az.wobbrik.engine.*
import kotlin.math.max
import kotlin.math.min

class PracticeSession : ViewModel() {
    val engine = PracticeEngine()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        val engine = ViewModelProvider(this)[PracticeSession::class.java].engine
        setContent { MaterialTheme(colorScheme = darkColorScheme(
            primary = GameTokens.Accent, onPrimary = GameTokens.Background,
            surface = GameTokens.Surface, onSurface = GameTokens.Text,
            background = GameTokens.Background, onBackground = GameTokens.Text,
        )) { PracticeGame(engine) } }
    }
}

@Composable
private fun PracticeGame(engine: PracticeEngine) {
    // Coordinates are read in the draw phase; HUD only observes discrete changes.
    val snapshot = remember { mutableStateOf(engine.state) }
    var paused by remember { mutableStateOf(engine.state.paused) }
    var menu by remember { mutableStateOf(false) }
    var count by remember { mutableIntStateOf(engine.state.floors.size) }
    var finished by remember { mutableStateOf(engine.state.phase == Phase.FINISHED) }
    var best by rememberSaveable { mutableIntStateOf(0) }
    var learned by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current
    var hudHeight by remember { mutableStateOf(GameTokens.Target + GameTokens.Inset) }
    fun publish() {
        snapshot.value = engine.state
        count = engine.state.floors.size
        best = max(best, count)
        finished = engine.state.phase == Phase.FINISHED
    }
    fun pause() { engine.pause(); paused = true; publish() }
    fun restart() { engine.restart(); paused = false; menu = false; publish() }
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
        while (!paused && !menu && !finished) {
            withFrameNanos { now ->
                if (previous != 0L) { engine.advance(now - previous); publish() }
                previous = now
            }
        }
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Background, GameTokens.Surface)))) {
        if (!menu) {
            Canvas(Modifier.fillMaxSize().safeDrawingPadding()
                .padding(top = hudHeight, bottom = GameTokens.Target)
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
                }) { drawScene(snapshot.value) }
            Row(Modifier.fillMaxWidth().safeDrawingPadding().padding(GameTokens.Inset)
                .onSizeChanged { hudHeight = with(density) { it.height.toDp() } },
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(end = GameTokens.Small)) {
                    Text("$count mərtəbə", color = GameTokens.Text, fontSize = GameTokens.Score)
                    Text("Məşq rekordu $best", color = GameTokens.Muted, fontSize = GameTokens.Body)
                }
                Button(onClick = { pause() }, modifier = Modifier.size(GameTokens.Target),
                    contentPadding = PaddingValues(GameTokens.Small), shape = RoundedCornerShape(GameTokens.ControlRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Raised, contentColor = GameTokens.Text)) {
                    Text("Ⅱ", modifier = Modifier.semantics { contentDescription = "Fasilə" })
                }
            }
            if (!learned && !finished) Text("Buraxmaq üçün toxun", color = GameTokens.Text,
                modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(GameTokens.Inset))
        }
        if (menu || paused || finished) {
            // Full-window input barrier: outside taps never reach the scene.
            Box(Modifier.fillMaxSize().background(GameTokens.Background.copy(alpha = 0.8f))
                .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {})
            Column(Modifier.align(Alignment.Center).safeDrawingPadding().padding(GameTokens.Inset)
                .widthIn(max = GameTokens.PanelWidth).fillMaxWidth()
                .background(GameTokens.Surface, RoundedCornerShape(GameTokens.CardRadius))
                .verticalScroll(rememberScrollState()).padding(GameTokens.Inset),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Gap), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (menu) "Wobbrik" else if (finished) "Cəhd bitdi" else "Fasilə", color = GameTokens.Text, fontSize = GameTokens.Title)
                if (menu || finished) Text(if (menu) "Offline məşq · mükafatsız" else "$count mərtəbə · məşq nəticəsi",
                    color = GameTokens.Muted)
                if (!menu && !finished) Action("Davam et", true) { engine.resume(); paused = false; publish() }
                Action(if (menu) "Məşqə başla" else "Yenidən başla", menu || finished) { restart() }
                if (!menu) Action("Menyuya qayıt", false) { engine.pause(); paused = false; menu = true; publish() }
            }
        }
    }
}

@Composable
private fun Action(label: String, primary: Boolean, action: () -> Unit) {
    Button(onClick = action, modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target),
        shape = RoundedCornerShape(GameTokens.ControlRadius),
        colors = ButtonDefaults.buttonColors(containerColor = if (primary) GameTokens.Accent else GameTokens.Raised,
            contentColor = if (primary) GameTokens.Background else GameTokens.Text)) { Text(label) }
}

private fun DrawScope.drawScene(state: PracticeState) {
    if (size.width <= 0f || size.height <= 0f) return
    val scale = min(size.width / 380f, size.height / 420f)
    val top = state.floors.size * PracticeRulesV1.FLOOR_HEIGHT.toFloat()
    val camera = max(0f, top - 120f)
    val ground = min(size.height - 32f * scale, size.height / 2 + 175f * scale)
    fun point(x: Double, y: Double) = Offset(size.width / 2 + x.toFloat() * scale, ground - (y.toFloat() - camera) * scale)
    val w = PracticeRulesV1.FLOOR_WIDTH.toFloat() * scale
    val h = PracticeRulesV1.FLOOR_HEIGHT.toFloat() * scale
    fun floor(x: Double, y: Double) {
        val p = point(x, y)
        drawRect(GameTokens.Raised, Offset(p.x - w / 2, p.y - h), Size(w, h))
        val side = androidx.compose.ui.graphics.Path().apply {
            moveTo(p.x + w / 2, p.y); lineTo(p.x + w / 2 + 12 * scale, p.y - 8 * scale)
            lineTo(p.x + w / 2 + 12 * scale, p.y - h - 8 * scale)
            lineTo(p.x + w / 2, p.y - h); close()
        }
        drawPath(side, GameTokens.Surface)
        drawRect(GameTokens.Muted, Offset(p.x - w / 2, p.y - h), Size(w, 3f * scale))
        drawRect(GameTokens.Surface, Offset(p.x - w / 2, p.y - 3f * scale), Size(w, 3f * scale))
        repeat(3) { i ->
            drawRect(GameTokens.Accent, Offset(p.x - w / 2 + (14 + i * 28) * scale, p.y - h + 8 * scale), Size(14 * scale, 13 * scale))
        }
    }
    if (camera == 0f) drawOval(GameTokens.Success, Offset(size.width / 2 - 150 * scale, ground - 5 * scale), Size(300 * scale, 28 * scale))
    val start = max(0, ((camera - 30) / 30).toInt())
    for (index in start until state.floors.size) {
        val f = state.floors[index]
        floor(f.centerX + state.sway * (index + 1), f.baseY)
    }
    val target = point(state.floors.lastOrNull()?.centerX ?: 0.0, top.toDouble())
    drawRect(if (state.balance.dangerEpisode) GameTokens.Danger else GameTokens.Accent,
        Offset(target.x - w / 2, target.y - h), Size(w, h), style = Stroke(2 * scale))
    if (state.phase != Phase.FINISHED) {
        val hangingY = top + PracticeRulesV1.DROP_HEIGHT.toFloat()
        val module = state.falling ?: Floor(state.craneX, hangingY.toDouble())
        floor(module.centerX, module.baseY)
        val hook = point(state.craneX, hangingY.toDouble() + 85)
        drawLine(GameTokens.Accent, Offset(0f, hook.y - 12 * scale), Offset(size.width, hook.y - 12 * scale), 8 * scale)
        drawLine(GameTokens.Muted, Offset(0f, hook.y - 28 * scale), Offset(size.width, hook.y - 28 * scale), 3 * scale)
        repeat((size.width / (24 * scale)).toInt() + 1) { i ->
            val x = i * 24 * scale
            drawLine(GameTokens.Accent, Offset(x, hook.y - 12 * scale), Offset(x + 12 * scale, hook.y - 28 * scale), 2 * scale)
            drawLine(GameTokens.Accent, Offset(x + 12 * scale, hook.y - 28 * scale), Offset(x + 24 * scale, hook.y - 12 * scale), 2 * scale)
        }
        if (state.phase == Phase.SWINGING) {
            val p = point(module.centerX, module.baseY + PracticeRulesV1.FLOOR_HEIGHT)
            drawLine(GameTokens.Muted, hook, Offset(p.x - w / 2, p.y), 2 * scale)
            drawLine(GameTokens.Muted, hook, Offset(p.x + w / 2, p.y), 2 * scale)
        }
    } else if (state.floors.isNotEmpty()) {
        val p = point(state.floors.last().centerX, top.toDouble())
        val roof = androidx.compose.ui.graphics.Path().apply {
            moveTo(p.x - w / 2, p.y); lineTo(p.x, p.y - h); lineTo(p.x + w / 2, p.y); close()
        }
        drawPath(roof, GameTokens.Danger)
    }
}
