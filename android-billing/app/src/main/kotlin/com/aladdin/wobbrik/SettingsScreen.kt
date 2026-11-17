package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

/** Approved settings-390 composition. State and side effects belong to the route. */
@Composable
internal fun SettingsScreen(preferences: DevicePreferences?, busy: Boolean, error: String?,
    onSetting: (FeedbackSetting, Boolean) -> Unit, onLanguage: () -> Unit,
    onSupport: () -> Unit, onPrivacy: () -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
        GameTokens.MobileHome.PanelTop, GameTokens.MobileHome.PanelBottom)))) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(GameTokens.Gap),
            verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = onBack, modifier = Modifier.size(GameTokens.Target)
                    .semantics { contentDescription = "Geri" }, color = GameTokens.Surface,
                    shape = RoundedCornerShape(GameTokens.ControlRadius)) {
                    Box(contentAlignment = Alignment.Center) { Text("‹", fontSize = GameTokens.Title, color = GameTokens.Text) }
                }
                Text("Parametrlər", Modifier.weight(1f).padding(start = GameTokens.Small)
                    .horizontalScroll(rememberScrollState()), softWrap = false, fontSize = mobileUiSizing().heading,
                    color = GameTokens.Text, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                SettingsSection("SƏS VƏ HİSS")
                SettingsRow("Musiqi", preferences?.music, enabled = preferences != null && !busy,
                    toggle = { onSetting(FeedbackSetting.Music, it) })
                SettingsRow("Səs effektləri", preferences?.sound, enabled = preferences != null && !busy,
                    toggle = { onSetting(FeedbackSetting.Sound, it) })
                SettingsRow("Titrəmə", preferences?.vibration, enabled = preferences != null && !busy,
                    toggle = { onSetting(FeedbackSetting.Vibration, it) })
                SettingsSection("OYUN")
                SettingsRow("Dil", value = "Azərbaycan", onClick = onLanguage)
                SettingsSection("DƏSTƏK")
                SettingsRow("Dəstək", onClick = onSupport)
                SettingsRow("Məxfilik", onClick = onPrivacy)
                if (preferences == null && error == null) CircularProgressIndicator(color = GameTokens.Accent)
                error?.let { Text(it, color = GameTokens.Danger, fontSize = GameTokens.Body) }
            }
        }
    }
}

@Composable private fun SettingsSection(label: String) {
    Text(label, Modifier.padding(top = GameTokens.Small, start = GameTokens.Quarter),
        color = GameTokens.Muted, fontSize = GameTokens.Label, fontWeight = FontWeight.Bold)
}

@Composable private fun SettingsRow(label: String, checked: Boolean? = null, value: String? = null,
    enabled: Boolean = true, toggle: ((Boolean) -> Unit)? = null, onClick: () -> Unit = {}) {
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    val largeFont = LocalDensity.current.fontScale > 1f
    val trailing: @Composable () -> Unit = {
        if (toggle != null) Box(Modifier.size(GameTokens.Target), contentAlignment = Alignment.Center) {
            Box(Modifier.size(GameTokens.Target, GameTokens.Inset)
                .background(if (enabled && checked == true) GameTokens.Accent else GameTokens.Raised, CircleShape),
                contentAlignment = if (checked == true) Alignment.CenterEnd else Alignment.CenterStart) {
                Box(Modifier.padding(GameTokens.Quarter).size(GameTokens.Gap)
                    .background(if (enabled && checked == true) GameTokens.Background else GameTokens.Muted, CircleShape))
            }
        } else Text("›", fontSize = GameTokens.Title, color = GameTokens.Muted)
    }
    val content: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().heightIn(min = GameTokens.Target + GameTokens.Gap).padding(GameTokens.Small),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            Box(Modifier.size(GameTokens.Target - GameTokens.Small).background(GameTokens.Background, CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(remember(label) { settingsIcon(label) }, null, Modifier.size(GameTokens.Inset), tint = GameTokens.Text)
            }
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = GameTokens.Body, color = GameTokens.Text, fontWeight = FontWeight.Bold)
                if (largeFont) {
                    if (value != null) Text(value, color = GameTokens.Muted, fontSize = GameTokens.Label)
                    Box(Modifier.align(Alignment.End)) { trailing() }
                }
            }
            if (!largeFont) {
                if (value != null) Text(value, fontSize = GameTokens.Label, color = GameTokens.Muted)
                trailing()
            }
        }
    }
    if (toggle != null) MenuRelief(Modifier.toggleable(value = checked == true, enabled = enabled,
        role = Role.Switch, onValueChange = toggle), shape = shape) { content() }
    else Surface(onClick = onClick, enabled = enabled, color = androidx.compose.ui.graphics.Color.Transparent, shape = shape) {
        MenuRelief(shape = shape) { content() }
    }
}

private fun settingsIcon(label:String):androidx.compose.ui.graphics.vector.ImageVector {
 val paths=when(label) {
 "Musiqi" -> listOf("M9 18V5l12-2v13","M3.0,18.0a3.0,3.0 0 1 0 6.0,0a3.0,3.0 0 1 0 -6.0,0z","M15.0,16.0a3.0,3.0 0 1 0 6.0,0a3.0,3.0 0 1 0 -6.0,0z")
 "Səs effektləri" -> listOf("M11 5 6 9H2v6h4l5 4V5Z","M15.5 8.5a5 5 0 0 1 0 7","M19 5a10 10 0 0 1 0 14")
 "Titrəmə" -> listOf("m2 8 2 2-2 2 2 2-2 2","m22 8-2 2 2 2-2 2 2 2","M9.5,5.0h5.0a1.5,1.5 0 0 1 1.5,1.5v11.0a1.5,1.5 0 0 1 -1.5,1.5h-5.0a1.5,1.5 0 0 1 -1.5,-1.5v-11.0a1.5,1.5 0 0 1 1.5,-1.5z")
 "Dil" -> listOf("M2.0,12.0a10.0,10.0 0 1 0 20.0,0a10.0,10.0 0 1 0 -20.0,0z","M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20","M2 12h20")
 "Dəstək" -> listOf("M2.0,12.0a10.0,10.0 0 1 0 20.0,0a10.0,10.0 0 1 0 -20.0,0z","M12 16v-4","M12 8h.01")
 "Məxfilik" -> listOf("M20 13c0 5-3.5 7.5-7.7 9a1 1 0 0 1-.6 0C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.2-2.7a1.2 1.2 0 0 1 1.6 0C14.5 3.8 17 5 19 5a1 1 0 0 1 1 1z","m9 12 2 2 4-4")
 else -> emptyList()
 }
 val b=androidx.compose.ui.graphics.vector.ImageVector.Builder(label,GameTokens.Inset,GameTokens.Inset,24f,24f)
 for(path in paths) b.addPath(androidx.compose.ui.graphics.vector.PathParser().parsePathString(path).toNodes(),stroke=androidx.compose.ui.graphics.SolidColor(GameTokens.Text),strokeLineWidth=2f,strokeLineCap=androidx.compose.ui.graphics.StrokeCap.Round,strokeLineJoin=androidx.compose.ui.graphics.StrokeJoin.Round)
 return b.build()
}
