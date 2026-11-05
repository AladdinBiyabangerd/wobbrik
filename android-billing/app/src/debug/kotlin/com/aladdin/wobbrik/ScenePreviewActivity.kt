package com.aladdin.wobbrik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        val state = PracticeState(paused = intent.getBooleanExtra("paused", false),
            phase = if (intent.getBooleanExtra("finished", false)) Phase.FINISHED else Phase.SWINGING,
            craneX = intent.getIntExtra("crane", 0).coerceIn(-120, 120).toDouble(),
            floors = List(count) { Floor(0.0, it * PracticeRulesV1.FLOOR_HEIGHT) })
        val engine = PracticeEngine().also {
            it.restore(EngineCheckpoint(state = state, direction = 1, fallVelocity = 0.0,
                tickUnits = 0, swayVelocity = 0.0))
        }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = GameTokens.Accent,
                onPrimary = GameTokens.Background, surface = GameTokens.Surface,
                onSurface = GameTokens.Text, background = GameTokens.Background, onBackground = GameTokens.Text)) {
                PracticeGame(engine, renderOnly = true)
            }
        }
    }
}
