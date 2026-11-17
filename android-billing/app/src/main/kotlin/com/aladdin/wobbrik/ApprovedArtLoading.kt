package com.aladdin.wobbrik

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal enum class ArtPreparation { Loading, Ready, Failed }

/** Inventory metadata does not depend on decorative APK art. Other destinations retain
 * their existing route gate; this scope only defers the inventory illustration. */
internal data class BuildingArtState(val status: ArtPreparation, val retry: () -> Unit)
internal val LocalBuildingArt = staticCompositionLocalOf { BuildingArtState(ArtPreparation.Ready, {}) }

/** Bundled art is account-independent. A cancelled composition cannot publish completion. */
@Composable
internal fun rememberArtPreparation(key: Any, load: () -> Unit): ArtPreparation {
    val status by produceState(ArtPreparation.Loading, key) {
        value = ArtPreparation.Loading
        try {
            withContext(Dispatchers.IO) { load() }
            value = ArtPreparation.Ready
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { value = ArtPreparation.Failed }
    }
    return status
}

/** The native menu remains navigable while its art is decoded; no placeholder scene. */
@Composable
internal fun ApprovedArtStatus(status: ArtPreparation, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(GameTokens.Gap),
        verticalArrangement = Arrangement.spacedBy(GameTokens.Gap, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        if (status == ArtPreparation.Loading) CircularProgressIndicator(
            Modifier.size(GameTokens.Target), color = GameTokens.Accent)
        Text(if (status == ArtPreparation.Failed) "Qrafika yüklənmədi. Yenidən cəhd et." else "Qrafika yüklənir…",
            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            color = GameTokens.Text, fontFamily = GameTypeface, fontSize = GameTokens.Body)
        if (status == ArtPreparation.Failed) TextButton(onClick = onRetry,
            modifier = Modifier.heightIn(min = GameTokens.Target)) {
            Text("Yenidən cəhd et", color = GameTokens.Accent, fontFamily = GameTypeface, fontSize = GameTokens.Body)
        }
    }
}

internal fun needsBundledArt(page: String): Boolean =
    page in setOf("game", "city", "parts", "collection", "market", "market-own")
