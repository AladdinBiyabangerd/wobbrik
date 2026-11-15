package com.aladdin.wobbrik

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CancellationException

internal val LocalGameFeedback = staticCompositionLocalOf<() -> Unit> { {} }

/** Activity-owned output: no playback in the background; respects audio focus and system haptics. */
internal class GameFeedback(private val context: Context, private val view: View) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private var player: MediaPlayer? = null
    private var preferences: DevicePreferences? = null
    private var foreground = false
    private var hasFocus = false
    private var focusRequested = false
    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        hasFocus = change == AudioManager.AUDIOFOCUS_GAIN
        if (hasFocus && foreground && preferences?.music == true) startMusic() else player?.pause()
    }
    private val request = if (Build.VERSION.SDK_INT >= 26) AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener(listener).build() else null
    private val pool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private var loaded = false
    // Register before load: a fast completion must not leave clicks permanently muted.
    init { pool.setOnLoadCompleteListener { _, _, status -> loaded = status == 0 } }
    private val click = pool.load(context, R.raw.sfx_ui_click, 1)

    fun update(value: DevicePreferences?, resumed: Boolean) {
        preferences = value; foreground = resumed
        if (resumed && value?.music == true) {
            if (!focusRequested && !audio.isMusicActive) {
                @Suppress("DEPRECATION")
                val result = if (Build.VERSION.SDK_INT >= 26) audio.requestAudioFocus(requireNotNull(request))
                    else audio.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                hasFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                focusRequested = hasFocus
            }
            if (hasFocus) startMusic()
        } else {
            player?.pause()
            if (focusRequested) {
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT >= 26) audio.abandonAudioFocusRequest(requireNotNull(request)) else audio.abandonAudioFocus(listener)
            }
            focusRequested = false; hasFocus = false
        }
    }

    private fun startMusic() {
        val current = player ?: runCatching {
            MediaPlayer.create(context, R.raw.music_main, attributes, 0)?.apply {
                isLooping = true; setVolume(0.45f, 0.45f)
            }
        }.getOrNull()?.also { player = it } ?: return
        runCatching { if (!current.isPlaying) current.start() }
    }

    fun click() {
        if (!foreground) return
        if (preferences?.sound == true && loaded) pool.play(click, 0.5f, 0.5f, 0, 0, 1f)
        if (preferences?.vibration == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    fun release() { update(null, false); player?.release(); player = null; pool.release() }
}

@Composable
internal fun WithGameFeedback(settings: DeviceSettings, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val output = remember(context, view) { GameFeedback(context, view) }
    var prefs by remember { mutableStateOf<DevicePreferences?>(null) }
    var resumed by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    LaunchedEffect(settings) {
        try { settings.values.collect { prefs = it } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { prefs = null }
    }
    DisposableEffect(lifecycle, output) {
        val observer = LifecycleEventObserver { _, _ ->
            resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (!resumed) output.update(prefs, false)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); output.release() }
    }
    SideEffect { output.update(prefs, resumed) }
    CompositionLocalProvider(LocalGameFeedback provides output::click, content = content)
}
