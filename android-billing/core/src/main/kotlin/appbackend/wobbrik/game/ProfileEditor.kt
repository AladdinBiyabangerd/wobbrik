package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.billing.Sessions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface ProfileSaveResult {
    data class Saved(val profile: PublicProfile) : ProfileSaveResult
    /** Display the current server identity; another replacement needs an explicit user action. */
    data class Conflict(val current: PublicProfile) : ProfileSaveResult
}

/** Bound to one login epoch. A lost reply can be retried with the exact replacement. */
class ProfileEditor(private val sessions: Sessions, private val access: ProfileAccess,
    private val session: BillingSession) {
    private val mutex = Mutex()
    private var loaded: PublicProfile? = null
    private var uncertain: ProfileReplacement? = null
    private fun current() { if (sessions.current() != session) throw AccountChanged() }
    suspend fun load(): PublicProfile = mutex.withLock {
        current()
        val profile = access.profile(session)
        current(); loaded = profile; uncertain = null
        profile
    }
    suspend fun save(name: String, avatar: String): ProfileSaveResult = mutex.withLock {
        current()
        val pending = uncertain
        // Do not turn an ambiguous write into a different replacement on the old revision.
        require(pending == null || (pending.displayName == name && pending.avatarId == avatar))
        val replacement = pending ?: ProfileReplacement(requireNotNull(loaded).revision, name, avatar)
        uncertain = replacement
        try {
            val profile = access.replaceProfile(session, replacement)
            current(); loaded = profile; uncertain = null
            ProfileSaveResult.Saved(profile)
        } catch (_: RevisionConflict) {
            // Drop the attempted replacement even if refetch fails: no automatic overwrite.
            uncertain = null; loaded = null
            current()
            val profile = access.profile(session)
            current(); loaded = profile
            ProfileSaveResult.Conflict(profile)
        }
    }
}
