package appbackend.wobbrik.billing

import java.util.UUID

/** Host supplies Firebase token loading; this module never stores the ID token. */
class AccountSessions(private val loadToken: suspend (uid: String) -> String) : Sessions {
    @Volatile private var session: BillingSession? = null
    /** Call on every auth sign-in/sign-out transition, including re-login with the same UID. */
    fun onAuthChanged(uid: String?) { session = uid?.let { BillingSession(it, UUID.randomUUID().toString()) } }
    override fun current() = session
    override suspend fun idToken(session: BillingSession): String {
        if (this.session != session) throw AccountChanged()
        val token = loadToken(session.uid)
        if (this.session != session) throw AccountChanged()
        return token
    }
}
