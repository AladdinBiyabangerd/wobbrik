package com.aladdin.wobbrik

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

/** Foreground-only default-network observer. Old registrations cannot deliver new wakeups. */
internal class RecoveryConnectivity(context: Context, private val connected: (Boolean) -> Unit) {
    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private val main = Handler(Looper.getMainLooper())
    private var callback: ConnectivityManager.NetworkCallback? = null

    fun start() {
        if (callback != null) return
        connected(false)
        val next = object : ConnectivityManager.NetworkCallback() {
            private var current: Network? = null
            override fun onAvailable(network: Network) {
                main.post {
                    if (callback === this) {
                        current = network
                        connected(false) // Availability alone is not proof of internet access.
                        // API 24–25 do not guarantee an initial capabilities callback. Read on main,
                        // outside the callback thread, and require this to still be the default.
                        if (Build.VERSION.SDK_INT < 26) {
                            val capabilities = manager.getNetworkCapabilities(network)
                            connected(manager.activeNetwork == network && capabilities?.online() == true)
                        }
                    }
                }
            }
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val online = capabilities.online()
                main.post { if (callback === this && current == network) connected(online) }
            }
            override fun onLost(network: Network) {
                main.post {
                    if (callback === this && current == network) {
                        current = null
                        connected(false)
                    }
                }
            }
        }
        callback = next
        try { manager.registerDefaultNetworkCallback(next) }
        catch (failure: RuntimeException) {
            callback = null
            connected(false)
            Log.w("WobbrikRecovery", "Connectivity observer unavailable", failure)
        }
    }

    private fun NetworkCapabilities.online() = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    fun stop() {
        val previous = callback
        callback = null
        connected(false)
        if (previous != null) try { manager.unregisterNetworkCallback(previous) }
        catch (failure: RuntimeException) { Log.w("WobbrikRecovery", "Connectivity observer cleanup failed", failure) }
    }
}
