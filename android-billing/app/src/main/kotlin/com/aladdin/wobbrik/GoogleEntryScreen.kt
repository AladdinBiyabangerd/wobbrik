package com.aladdin.wobbrik

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun GoogleEntry(app: WobbrikApplication, account: WobbrikApplication.AccountState) {
    val activity = LocalContext.current as? ComponentActivity
    val scope = rememberCoroutineScope()
    var requesting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val configuration = when (app.bootstrap) {
        WobbrikApplication.Bootstrap.MissingFirebase -> "Firebase konfiqurasiyası yoxdur"
        WobbrikApplication.Bootstrap.MissingBackendOrigin -> "Server ünvanı konfiqurasiya edilməyib"
        else -> null
    }
    val loading = configuration == null && (requesting || account == WobbrikApplication.AccountState.Loading)
    GoogleEntryScreen(loading, configuration ?: message ?: if (account is WobbrikApplication.AccountState.Error)
        "Google ilə giriş alınmadı. Yenidən cəhd et." else null, configuration == null && activity != null) {
        if (requesting || activity == null) return@GoogleEntryScreen
        requesting = true
        message = null
        scope.launch {
            try {
                val credential = GoogleSignIn.googleCredential(activity, GoogleSignIn.serverClientId(activity))
                app.signInGoogle(credential)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                message = GoogleSignIn.userMessage(failure)
            } finally {
                requesting = false
            }
        }
    }
}

/** Real entry layout, shared with the explicitly synthetic debug presentation fixture. */
@Composable
internal fun GoogleEntryScreen(loading: Boolean, message: String?, configured: Boolean, onSignIn: () -> Unit) {
    val board = ImageBitmap.imageResource(R.drawable.google_entry_board)
    // Artwork coordinates in the approved 1024×1536 board; not layout dimensions.
    val logo = remember(board) { BitmapPainter(board, IntOffset(244, 80), IntSize(540, 140)) }
    val hero = remember(board) { BitmapPainter(board, IntOffset(32, 256), IntSize(928, 576)) }
    val google = remember(board) { BitmapPainter(board, IntOffset(194, 1198), IntSize(108, 108)) }
    val left = remember(board) { BitmapPainter(board, IntOffset(48, 858), IntSize(88, 150)) }
    val right = remember(board) { BitmapPainter(board, IntOffset(886, 858), IntSize(88, 150)) }
    BoxWithConstraints(Modifier.fillMaxSize().background(GameTokens.Background).safeDrawingPadding()) {
        val short = maxHeight < GameTokens.CompactHeight
        val wide = maxWidth > maxHeight || maxWidth >= GameTokens.ExpandedWidth
        val spacing = if (short) GameTokens.Small else GameTokens.Gap
        val art: @Composable (Modifier) -> Unit = { modifier ->
            Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(spacing)) {
                Image(logo, "Wobbrik", Modifier.fillMaxWidth().height(GameTokens.Target + if (short) GameTokens.Small else GameTokens.Gap), contentScale = ContentScale.Fit)
                Image(hero, null, Modifier.fillMaxWidth().weight(1f), contentScale = ContentScale.Fit)
            }
        }
        val actions: @Composable (Modifier) -> Unit = { modifier ->
            Column(modifier, horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically)) {
                // Supporting text may scroll; the primary action remains outside that area.
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(left, null, Modifier.width(GameTokens.Inset).height(GameTokens.Target))
                        Text("Wobbrik-ə xoş gəldin", Modifier.weight(1f), color = GameTokens.Text,
                            fontSize = if (short) GameTokens.Hint else GameTokens.Title,
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Image(right, null, Modifier.width(GameTokens.Inset).height(GameTokens.Target))
                    }
                    Text("Oynamaq üçün Google hesabınla daxil ol", color = GameTokens.Muted,
                        fontSize = GameTokens.Body, textAlign = TextAlign.Center)
                    if (message != null) Text(message, color = GameTokens.Danger, fontSize = GameTokens.Body, textAlign = TextAlign.Center)
                }
                Button(onClick = onSignIn, enabled = configured && !loading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = GameTokens.Target + GameTokens.Gap),
                    shape = RoundedCornerShape(GameTokens.CardRadius),
                    border = BorderStroke(GameTokens.Hairline, GameTokens.Accent),
                    colors = ButtonDefaults.buttonColors(containerColor = GameTokens.Text, contentColor = GameTokens.Background,
                        disabledContainerColor = GameTokens.Text, disabledContentColor = GameTokens.Background)) {
                    if (loading) CircularProgressIndicator(Modifier.size(GameTokens.Inset), color = GameTokens.Background)
                    else Image(google, null, Modifier.size(GameTokens.Inset))
                    Spacer(Modifier.width(GameTokens.Small))
                    Text(if (loading) "Daxil olunur…" else "Google ilə daxil ol", fontSize = GameTokens.Body,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize().padding(if (short) GameTokens.Gap else GameTokens.Inset)) {
            val controlsMaxHeight = maxHeight / 2
            if (wide) Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(GameTokens.Inset), verticalAlignment = Alignment.CenterVertically) {
                art(Modifier.weight(1f).fillMaxHeight())
                actions(Modifier.weight(1f).fillMaxHeight())
            } else Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GameTokens.Gap)) {
                art(Modifier.weight(1f).fillMaxWidth())
                actions(Modifier.fillMaxWidth().heightIn(max = controlsMaxHeight))
                Spacer(Modifier.height(GameTokens.Inset))
            }
        }
    }
}
