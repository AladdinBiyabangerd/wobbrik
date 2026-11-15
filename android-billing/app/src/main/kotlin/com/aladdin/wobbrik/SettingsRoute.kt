package com.aladdin.wobbrik

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun SettingsRoute(settings: DeviceSettings, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val feedback = LocalGameFeedback.current
    var preferences by remember { mutableStateOf<DevicePreferences?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(settings) {
        try { settings.values.collect { preferences = it } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { error = "Parametrlər oxunmadı. Ekranı yenidən aç." }
    }
    SettingsScreen(preferences, busy, error, onSetting = { setting, value ->
        if (!busy && preferences != null) {
            busy = true; error = null
            scope.launch {
                try { settings.set(setting, value) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { error = "Seçim saxlanmadı. Yenidən cəhd et." }
                finally { busy = false }
            }
        }
    }, onLanguage = { feedback(); dialog = "Dil" },
        onSupport = {
            feedback()
            try {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SupportEmail"))
                    .putExtra(Intent.EXTRA_SUBJECT, "Wobbrik — dəstək")
                    .putExtra(Intent.EXTRA_TEXT, "Wobbrik ${BuildConfig.VERSION_NAME}\n\n"))
            } catch (_: ActivityNotFoundException) { dialog = "Dəstək" }
        },
        onPrivacy = { feedback(); dialog = "Məxfilik" }, onBack = onBack)
    if (dialog == "Məxfilik") LegalDocumentsDialog { dialog = null }
    dialog?.takeIf { it != "Məxfilik" }?.let { title ->
        AlertDialog(onDismissRequest = { dialog = null }, title = { Text(title) }, text = {
            SelectionContainer { Text(when (title) {
                "Dil" -> "Hazırda yalnız Azərbaycan dili mövcuddur."
                "Dəstək" -> "E-poçt tətbiqi tapılmadı. Ünvan: $SupportEmail"
                else -> SupportEmail
            }) }
        }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("Bağla") } },
            containerColor = GameTokens.Surface, titleContentColor = GameTokens.Text, textContentColor = GameTokens.Text)
    }
}
