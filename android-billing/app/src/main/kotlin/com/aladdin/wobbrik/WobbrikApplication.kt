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
import com.aladdin.wobbrik.engine.AcceptedAttemptCommands
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.coroutines.Job
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

    val engineHost = AcceptedEngineHost()

    lateinit var bootstrap: Bootstrap
        private set
    private val recoveryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var authListener: FirebaseAuth.AuthStateListener? = null
    sealed interface AccountState {
        data object Loading : AccountState
        data class Session(val anonymous: Boolean) : AccountState
        data class Error(val cause: Exception) : AccountState
    }
    private val mutableAccount = MutableStateFlow<AccountState>(AccountState.Loading)
    val account = mutableAccount.asStateFlow()
    private var accountWork: Job? = null

    /** Existing Firebase sessions are retained. Only a signed-out installation creates a guest. */
    fun retryAccount() {
        if (bootstrap !is Bootstrap.Configured || accountWork?.isActive == true) return
        val auth = FirebaseAuth.getInstance()
        accountWork = recoveryScope.launch {
            mutableAccount.value = AccountState.Loading
            try {
                val user = auth.currentUser ?: auth.signInAnonymously().await().user
                    ?: error("Firebase returned no user")
                if (auth.currentUser?.uid != user.uid) throw AccountChanged()
                mutableAccount.value = AccountState.Session(user.isAnonymous)
                // The listener owns auth epochs; only backend recovery can publish Ready.
                (bootstrap as Bootstrap.Configured).recovery.onForeground()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                mutableAccount.value = AccountState.Error(failure)
            }
        }
    }

    /** Link in place, never sign in over a guest or combine two server wallets. */
    suspend fun linkGuest(credential: AuthCredential) {
        check(bootstrap is Bootstrap.Configured)
        val auth = FirebaseAuth.getInstance()
        val guest = auth.currentUser ?: throw AccountChanged()
        require(guest.isAnonymous) { "An anonymous session is required" }
        // Collision must fail here: callers must not fall back to signInWithCredential.
        val linked = guest.linkWithCredential(credential).await().user ?: throw AccountChanged()
        check(linked.uid == guest.uid && auth.currentUser?.uid == guest.uid)
        check(!linked.isAnonymous) { "Google link left the session anonymous" }
        mutableAccount.value = AccountState.Session(linked.isAnonymous)
        // Same UID still needs a recovery epoch so stale Ready cannot authorize input.
        (bootstrap as? Bootstrap.Configured)?.recovery?.onAuthChanged(linked.uid)
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
        val commands = AcceptedAttemptCommands(owner)
        engineHost.bind(commands)
        bootstrap = Bootstrap.Configured(owner, commands)
        recoveryScope.launch {
            owner.state.collect { state ->
                // Never submit inside the synchronous recovery invalidation callback.
                yield()
                if (state is AuthenticatedRecoveryOwner.State.Ready) engineHost.reconcileTerminal()
            }
        }
        authListener = FirebaseAuth.AuthStateListener { source ->
            // Do not deduplicate by UID: a re-login must establish a new recovery epoch.
            owner.onAuthChanged(source.currentUser?.uid)
            mutableAccount.value = source.currentUser?.let { AccountState.Session(it.isAnonymous) }
                ?: AccountState.Loading
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
