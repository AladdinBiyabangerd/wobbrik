package com.aladdin.wobbrik

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.aladdin.wobbrik.engine.AcceptedEngineHost
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import appbackend.wobbrik.game.*

/** Frozen server-shaped presentation fixture. No authentication or command dispatch. */
class AccountPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        val ready = AuthenticatedRecoveryOwner.State.Ready(GameSnapshot("wobbrik-v1", 1, 1250, 1280, null,
            AccountRecovery("2026-11-05T00:00:00Z", "2026-11-05T00:00:00Z", "2026-11-06T00:00:00Z",
                7, 0, emptyList(), emptyList(), emptyList(), emptyList(),
                bestFloors = 28, xpInterval = XpInterval(java.math.BigInteger.valueOf(1050), java.math.BigInteger.valueOf(1400)))))
        val previewReady = when (intent.getStringExtra("contract")) {
            "legacy" -> ready.copy(snapshot = ready.snapshot.copy(account = ready.snapshot.account?.copy(bestFloors = null, xpInterval = null)))
            "zero" -> ready.copy(snapshot = ready.snapshot.copy(xp = 0, account = ready.snapshot.account?.copy(
                level = 1, bestFloors = 0, xpInterval = XpInterval(java.math.BigInteger.ZERO, java.math.BigInteger.valueOf(50)))))
            "max" -> ready.copy(snapshot = ready.snapshot.copy(xp = Long.MAX_VALUE, account = ready.snapshot.account?.copy(
                level = 607400100, bestFloors = Long.MAX_VALUE,
                xpInterval = XpInterval(java.math.BigInteger("9223372021815247500"), java.math.BigInteger("9223372052185252500")))))
            else -> ready
        }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = GameTokens.Accent,
                onPrimary = GameTokens.Background, surface = GameTokens.Surface, onSurface = GameTokens.Text,
                background = GameTokens.Background, onBackground = GameTokens.Text)) {
                val page = intent.getStringExtra("page") ?: "home"
                if (page == "game" || page == "pause") {
                    val gameReady = ready.copy(snapshot = ready.snapshot.copy(attempt = ServerAttempt("fixture", true, 0, null)))
                    val host = androidx.compose.runtime.remember {
                        AcceptedEngineHost().also { it.onRecoveryChanged(gameReady); it.onForeground() }
                    }
                    val view = host.snapshot()
                    AcceptedScene(view.copy(scene = view.scene?.copy(paused = page == "pause")), gameReady, host, {})
                } else AccountScreen(intent.getStringExtra("page") ?: "home", if (intent.getBooleanExtra("unavailable", false)) null else previewReady,
                    WobbrikApplication.AccountState.Session(true), intent.getStringExtra("status"), {}, {},
                    if (intent.getBooleanExtra("unavailable", false)) null else ({}), false)
            }
        }
    }
}
