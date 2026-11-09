package com.aladdin.wobbrik

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

internal object JournalRecoveryCopy {
    const val Title = "Cəhd bu versiyada bərpa edilmir"
    const val Description = "Saxlanmış cəhd məlumatı tətbiqin bu versiyasına uyğun deyil. Məlumat silinməyib. Uyğun tətbiq versiyası ilə yenidən yoxla."
    const val Retry = "Yenidən yoxla"
}

/** Approved account-states error artwork and tokens; no gameplay or journal mutations. */
@Composable
internal fun JournalRecoveryScreen(onRetry: () -> Unit, onHome: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(GameTokens.Surface, GameTokens.Background)))
        .safeDrawingPadding().padding(GameTokens.Gap)) {
        val wide = maxWidth > maxHeight
        val artwork: @Composable (Modifier) -> Unit = { modifier ->
            Image(painterResource(R.drawable.recovery_hero), null,
                modifier.widthIn(max = GameTokens.PanelWidth).aspectRatio(1f), contentScale = ContentScale.Fit)
        }
        val title: @Composable () -> Unit = {
            Text(JournalRecoveryCopy.Title, color = GameTokens.Text, fontSize = GameTokens.Title,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
        }
        val description: @Composable () -> Unit = {
            Text(JournalRecoveryCopy.Description, color = GameTokens.Muted, fontSize = GameTokens.Body,
                textAlign = TextAlign.Center)
        }
        val retry: @Composable () -> Unit = { HomeButton(JournalRecoveryCopy.Retry, primary = true, onClick = onRetry) }
        val home: @Composable () -> Unit = { HomeButton("Ana ekrana qayıt", icon = R.drawable.ui_home, onClick = onHome) }
        val controls: @Composable (Modifier) -> Unit = { modifier ->
            Column(modifier, horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
                title(); description(); retry(); home()
            }
        }
        if (wide) Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameTokens.Inset, Alignment.CenterHorizontally)) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { artwork(Modifier) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                controls(Modifier.widthIn(max = GameTokens.PanelWidth).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(vertical = GameTokens.Small))
            }
        } else LazyColumn(Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GameTokens.Gap, Alignment.CenterVertically)) {
            item { artwork(Modifier) }
            item { Box(Modifier.widthIn(max = GameTokens.PanelWidth).fillMaxWidth(), contentAlignment = Alignment.Center) { title() } }
            item { Box(Modifier.widthIn(max = GameTokens.PanelWidth).fillMaxWidth(), contentAlignment = Alignment.Center) { description() } }
            item { Box(Modifier.widthIn(max = GameTokens.PanelWidth).fillMaxWidth()) { retry() } }
            item { Box(Modifier.widthIn(max = GameTokens.PanelWidth).fillMaxWidth()) { home() } }
        }
    }
}
