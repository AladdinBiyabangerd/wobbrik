package com.aladdin.wobbrik

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

/** Loads in-app defaults and fetches Remote Config once. No-op without google-services.json. */
object RemoteConfigInit {
    private const val RELEASE_FETCH_INTERVAL_SECONDS = 12L * 60 * 60

    fun start(context: android.content.Context) {
        if (FirebaseApp.getApps(context).isEmpty()) return
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else RELEASE_FETCH_INTERVAL_SECONDS
        })
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate().addOnFailureListener { Log.w("RemoteConfig", "fetch failed", it) }
    }
}
