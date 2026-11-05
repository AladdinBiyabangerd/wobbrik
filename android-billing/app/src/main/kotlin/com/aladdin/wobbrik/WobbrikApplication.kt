package com.aladdin.wobbrik

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.GameplayHttpBackend
import appbackend.wobbrik.game.UrlConnectionGameplayTransport
import appbackend.wobbrik.game.androidAttemptJournal
import com.aladdin.wobbrik.engine.AcceptedEngineHost
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.tasks.await

/** Process-owned auth/recovery only. No online gameplay or restart actions are exposed. */
class WobbrikApplication : Application(), DefaultLifecycleObserver {
    sealed interface Bootstrap {
        data object MissingFirebase : Bootstrap
        data object MissingBackendOrigin : Bootstrap
        data class Configured(val recovery: AuthenticatedRecoveryOwner) : Bootstrap
    }

    val engineHost = AcceptedEngineHost()

    lateinit var bootstrap: Bootstrap
        private set
    private val recoveryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var authListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate() {
        super<Application>.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            bootstrap = Bootstrap.MissingFirebase
            return
        }
        if (BuildConfig.BACKEND_ORIGIN.isBlank()) {
            bootstrap = Bootstrap.MissingBackendOrigin
            return
        }
        // Invalid configured origins fail explicitly; never fall back to a guessed endpoint.
        val transport = UrlConnectionGameplayTransport(BuildConfig.BACKEND_ORIGIN)
        val auth = FirebaseAuth.getInstance()
        val sessions = AccountSessions { uid ->
            val user = auth.currentUser ?: throw AccountChanged()
            if (user.uid != uid) throw AccountChanged()
            val token = user.getIdToken(false).await().token
            if (auth.currentUser !== user) throw AccountChanged()
            require(!token.isNullOrBlank()) { "Firebase returned an empty ID token" }
            token
        }
        val owner = AuthenticatedRecoveryOwner(
            sessions, GameplayHttpBackend(sessions, transport), androidAttemptJournal(this), recoveryScope,
            onStateChanged = engineHost::onRecoveryChanged,
        )
        bootstrap = Bootstrap.Configured(owner)
        authListener = FirebaseAuth.AuthStateListener { source ->
            // Do not deduplicate by UID: a re-login must establish a new recovery epoch.
            owner.onAuthChanged(source.currentUser?.uid)
        }.also(auth::addAuthStateListener)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        engineHost.onForeground()
        (bootstrap as? Bootstrap.Configured)?.recovery?.onForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        engineHost.onPause()
    }
}
