package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import kotlinx.coroutines.delay

internal const val InitialSyncWaitMillis = 8_000L

/** Presentation only. Recovery remains the sole sync/journal owner; no optional reads here. */
@Composable
internal fun InitialAccountSync(epoch: BillingSession?, state: AuthenticatedRecoveryOwner.State?,
    coins: Long?, canBrowseCached: Boolean, onRetry: () -> Unit): Boolean {
    var entered by remember(epoch) { mutableStateOf(false) }
    var elapsed by remember(epoch) { mutableStateOf(false) }
    LaunchedEffect(epoch) {
        delay(InitialSyncWaitMillis)
        elapsed = true
    }
    val ready = state is AuthenticatedRecoveryOwner.State.Ready
    // Latch success: later mutations/recoveries must not replace an already opened menu.
    SideEffect { if (ready) entered = true }
    if (epoch == null || ready || entered) return false
    val failed = state is AuthenticatedRecoveryOwner.State.Error
    val canContinue = canBrowseCached || elapsed || failed
    BackHandler(canContinue) { entered = true }
    InitialSyncScreen(coins, failed, elapsed, canContinue,
        onContinue = { entered = true }, onRetry = onRetry)
    return true
}

/** Native loading state from 04-game-states: shared header, cyan indicator and relief panel. */
@Composable
internal fun InitialSyncScreen(coins: Long?, failed: Boolean, elapsed: Boolean, canContinue: Boolean,
    onContinue: () -> Unit, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap)) {
        MobileMenuHeader(coins)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Gap, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (!failed) CircularProgressIndicator(Modifier.size(GameTokens.Target), color = GameTokens.Accent)
            Text(if (failed) "Hesab yenilənmədi" else "Yüklənir…",
                Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Title,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            MenuRelief(Modifier.fillMaxWidth()) {
                Text(when {
                    failed -> "Hesab yenilənmədi. Menyuya keçə və sonra yenidən cəhd edə bilərsən."
                    elapsed -> "Bağlantı vaxt aparır. Mövcud məlumatla davam edə bilərsən."
                    else -> "Hesab və cəhd bərpa olunur…"
                }, Modifier.fillMaxWidth().padding(GameTokens.Gap), color = GameTokens.Muted,
                    fontFamily = GameTypeface, fontSize = GameTokens.Body, textAlign = TextAlign.Center)
            }
        }
        // Outside scrolling copy: large fonts and short windows retain a reachable exit.
        if (failed) TextButton(onClick = onRetry,
            modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)) {
            Text("Yenidən cəhd et", color = GameTokens.Accent, fontFamily = GameTypeface,
                fontSize = GameTokens.Body, textAlign = TextAlign.Center)
        }
        if (canContinue) TextButton(onClick = onContinue,
            modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target)) {
            Text("Menyuya keç", color = GameTokens.Accent, fontFamily = GameTypeface,
                fontSize = GameTokens.Body, textAlign = TextAlign.Center)
        }
    }
}
