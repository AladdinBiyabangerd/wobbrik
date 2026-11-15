package com.aladdin.wobbrik

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.deviceSettings by preferencesDataStore(name = "device-settings-v1")

internal data class DevicePreferences(val music: Boolean = true, val sound: Boolean = true, val vibration: Boolean = false)
internal enum class FeedbackSetting { Music, Sound, Vibration }

/** Device preferences only; never account progress or wallet state. Failed reads remain errors. */
internal class DeviceSettings(context: Context) {
    private val store = context.applicationContext.deviceSettings
    private fun key(setting: FeedbackSetting) = booleanPreferencesKey(setting.name.lowercase())
    val values = store.data.map { prefs ->
        DevicePreferences(prefs[key(FeedbackSetting.Music)] ?: true,
            prefs[key(FeedbackSetting.Sound)] ?: true, prefs[key(FeedbackSetting.Vibration)] ?: false)
    }
    suspend fun set(setting: FeedbackSetting, value: Boolean) {
        store.edit { it[key(setting)] = value }
    }
}
