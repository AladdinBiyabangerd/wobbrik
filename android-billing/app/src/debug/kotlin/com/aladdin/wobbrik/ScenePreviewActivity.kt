package com.aladdin.wobbrik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import com.aladdin.wobbrik.engine.*

/** Debug-only frozen fixture for repeatable comparisons. Never an accepted online attempt. */
class ScenePreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        val count = intent.getIntExtra("floors", 4).coerceIn(0, 40)
        val drift = intent.getIntExtra("drift", 0).coerceIn(-8, 8)
        val state = PracticeState(paused = intent.getBooleanExtra("paused", false),
            phase = if (intent.getBooleanExtra("finished", false)) Phase.FINISHED else Phase.SWINGING,
            craneX = intent.getIntExtra("crane", 0).coerceIn(-120, 120).toDouble(),
            floors = List(count) { Floor((it * drift).coerceIn(-120, 120).toDouble(), it * PracticeRulesV1.FLOOR_HEIGHT) })
        val engine = PracticeEngine().also {
            it.restore(EngineCheckpoint(state = state, direction = 1, fallVelocity = 0.0,
                tickUnits = 0, swayVelocity = 0.0))
        }
        // Deterministic animation samples for frame-by-frame visual review.
        val motion = SceneMotion()
        motion.advance(engine.state, 0f,
            intent.getIntExtra("camera_from", engine.cameraX.toInt()).toDouble())
        val dropTick = intent.getIntExtra("drop_tick", -1)
        repeat(intent.getIntExtra("ticks", 0).coerceIn(0, 600)) { tick ->
            if (tick == dropTick) engine.drop()
            engine.advance(16_666_667)
            motion.advance(engine.state, 1f / 60f, engine.cameraX)
        }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = GameTokens.Accent,
                onPrimary = GameTokens.Background, surface = GameTokens.Surface,
                onSurface = GameTokens.Text, background = GameTokens.Background, onBackground = GameTokens.Text)) {
                // Reject Android's stale task-snapshot starting window in capture tooling.
                val marker = "Wobbrik fixture floors=$count crane=${state.craneX.toInt()} paused=${state.paused} drift=$drift finished=${state.phase == Phase.FINISHED}"
                Box(Modifier.fillMaxSize().semantics { contentDescription = marker }) {
                    val parts = listOf("wall", "window", "entrance", "lighting", "roof").associateWith { category ->
                        intent.getStringExtra(category) ?: "base_${intent.getIntExtra("style",1).coerceIn(1,6)}_$category"
                    }
                    if (intent.getBooleanExtra("scene_only",false)) {
                        // Explicit frozen Android-renderer fixture for inspecting terminal roof joins
                        // without the result panel. Never presented as an interactive attempt.
                        val art = SceneArt(ImageBitmap.imageResource(R.drawable.game_floor),
                            ImageBitmap.imageResource(R.drawable.game_crane),ImageBitmap.imageResource(R.drawable.inventory_garden),
                            ImageBitmap.imageResource(R.drawable.game_roof),ImageBitmap.imageResource(R.drawable.game_cloud),rememberGameAtlases())
                        val cosmetics = rememberCosmeticArt()+rememberGameWindowArt()
                        Canvas(Modifier.fillMaxSize().safeDrawingPadding().background(GameTokens.Background)) {
                            drawGameScene(engine.state,art,motion,cosmetics,parts)
                        }
                    } else PracticeGame(engine, renderOnly = !intent.getBooleanExtra("animate", false), previewMotion = motion,
                        previewObserver = if (intent.getBooleanExtra("telemetry", false)) { current ->
                            android.util.Log.d("AtlasQA", "${current.phase} ${current.craneX} ${current.floors.size}")
                        } else null,
                        previewParts = parts)
                }
            }
        }
    }
}
