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
import appbackend.wobbrik.game.androidSnapshotCache
import com.aladdin.wobbrik.engine.AcceptedEngineHost
import com.aladdin.wobbrik.engine.AcceptedAttemptCommands
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.auth.AuthCredential

/** Process-owned account bootstrap, recovery, durable commands and accepted online physics. */
class WobbrikApplication : Application(), DefaultLifecycleObserver {
    sealed interface Bootstrap {
        data object MissingFirebase : Bootstrap
        data object MissingBackendOrigin : Bootstrap
        data class Configured(val recovery: AuthenticatedRecoveryOwner, val commands: AcceptedAttemptCommands) : Bootstrap
    }

    internal val cosmeticDefaults by lazy { AndroidCosmeticDefaultsStore(this) }
    val engineHost by lazy { AcceptedEngineHost(AndroidFinishDraftStore(this), cosmeticDefaults) }

    lateinit var bootstrap: Bootstrap
        private set
    private val recoveryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var authListener: FirebaseAuth.AuthStateListener? = null
    sealed interface AccountState {
        data object Loading : AccountState
        data object SignedOut : AccountState
        data class Session(val uid: String) : AccountState
        data class Error(val cause: Exception) : AccountState
    }
    private val mutableAccount = MutableStateFlow<AccountState>(AccountState.Loading)
    val account = mutableAccount.asStateFlow()

    /** Only Google-backed Firebase sessions may authorize recovery or gameplay. */
    private fun googleUser() = FirebaseAuth.getInstance().currentUser?.takeIf { user ->
        user.providerData.any { it.providerId == "google.com" }
    }

    fun retryAccount() {
        if (bootstrap !is Bootstrap.Configured) return
        val user = googleUser()
        mutableAccount.value = user?.let { AccountState.Session(it.uid) } ?: AccountState.SignedOut
        if (user != null) (bootstrap as Bootstrap.Configured).recovery.onForeground()
    }

    suspend fun signInGoogle(credential: AuthCredential) {
        check(bootstrap is Bootstrap.Configured)
        mutableAccount.value = AccountState.Loading
        try {
            val user = FirebaseAuth.getInstance().signInWithCredential(credential).await().user
                ?: throw AccountChanged()
            if (googleUser()?.uid != user.uid) throw AccountChanged()
            // The Firebase listener establishes the recovery epoch, including same-UID re-login.
            mutableAccount.value = AccountState.Session(user.uid)
        } catch (cancelled: CancellationException) {
            retryAccount()
            throw cancelled
        } catch (failure: Exception) {
            mutableAccount.value = AccountState.Error(failure)
        }
    }

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
            val user = googleUser() ?: throw AccountChanged()
            if (user.uid != uid) throw AccountChanged()
            val token = user.getIdToken(false).await().token
            if (googleUser() !== user) throw AccountChanged()
            require(!token.isNullOrBlank()) { "Firebase returned an empty ID token" }
            token
        }
        val owner = AuthenticatedRecoveryOwner(
            sessions, GameplayHttpBackend(sessions, transport), androidAttemptJournal(this), recoveryScope,
            onStateChanged = engineHost::onRecoveryChanged,
            cache = androidSnapshotCache(this),
        )
        val commands = AcceptedAttemptCommands(owner)
        engineHost.bind(commands)
        bootstrap = Bootstrap.Configured(owner, commands)
        authListener = FirebaseAuth.AuthStateListener {
            // Do not deduplicate by UID: a re-login must establish a new recovery epoch.
            val user = googleUser()
            owner.onAuthChanged(user?.uid)
            mutableAccount.value = user?.let { AccountState.Session(it.uid) }
                ?: AccountState.SignedOut
        }.also(auth::addAuthStateListener)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        retryAccount()
    }

    override fun onStart(owner: LifecycleOwner) {
        engineHost.onForeground()
        (bootstrap as? Bootstrap.Configured)?.recovery?.onForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        engineHost.onPause()
    }
}
