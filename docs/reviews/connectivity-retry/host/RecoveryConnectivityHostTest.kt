package com.aladdin.wobbrik

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Looper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowNetwork

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28, 34], application=Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RecoveryConnectivityHostTest {
    @Test fun defaultNetworkValidationHandoverAndStoppedCallbacks() {
        val context = RuntimeEnvironment.getApplication()
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val shadow = shadowOf(manager)
        val events = mutableListOf<Boolean>()
        val monitor = RecoveryConnectivity(context, events::add)
        monitor.start(); monitor.start()
        assertEquals(1, shadow.networkCallbacks.size)
        val callback = shadow.networkCallbacks.single()
        val wifi = ShadowNetwork.newInstance(100)
        val mobile = ShadowNetwork.newInstance(101)
        val capabilities = NetworkCapabilities()
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        callback.onAvailable(wifi); shadowOf(Looper.getMainLooper()).idle()
        assertFalse(events.last())
        callback.onCapabilitiesChanged(wifi, capabilities); shadowOf(Looper.getMainLooper()).idle()
        assertFalse(events.last()) // Captive/unvalidated internet does not enable automatic retries.
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        callback.onCapabilitiesChanged(wifi, capabilities); shadowOf(Looper.getMainLooper()).idle()
        assertTrue(events.last())
        callback.onAvailable(mobile); callback.onCapabilitiesChanged(mobile, capabilities)
        callback.onLost(wifi); shadowOf(Looper.getMainLooper()).idle()
        assertTrue(events.last()) // Loss of the former default cannot hide its validated replacement.
        shadowOf(capabilities).removeCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        callback.onCapabilitiesChanged(mobile, capabilities); shadowOf(Looper.getMainLooper()).idle()
        assertFalse(events.last())
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        callback.onCapabilitiesChanged(mobile, capabilities) // Queued before stop; must not wake recovery later.
        monitor.stop(); monitor.stop(); shadowOf(Looper.getMainLooper()).idle()
        assertFalse(events.last()); assertTrue(shadow.networkCallbacks.isEmpty())
        monitor.start(); val count = events.size
        callback.onAvailable(wifi); callback.onCapabilitiesChanged(wifi, capabilities)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(count, events.size) // Previous registration is stale even after foreground re-entry.
        monitor.stop()
    }
}
