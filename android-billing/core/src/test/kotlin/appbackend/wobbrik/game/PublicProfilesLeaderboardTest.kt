package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class PublicProfilesLeaderboardTest {
    private fun profile(name: String = "Əli", revision: Int = 1) = """{"revision":$revision,"display_name":"$name","avatar_id":"builder"}"""
    private fun page(rank: Int = 1, next: String? = null, self: Boolean = false) = """{"scope":"weekly","week_start":"2026-11-09","snapshot_at":"2026-11-14T00:00:00Z","expires_at":"2026-11-14T00:15:00Z","entries":[{"rank":$rank,"result_id":"result$rank","floors":20,"accepted_at":"2026-11-13T00:00:00Z","is_self":$self,"display_name":"Əli","avatar_id":"builder"}]${next?.let { ",\"next_cursor\":\"$it\"" } ?: ""}}"""
    @Test fun unicodeNamesMatchServerAndNeverAcceptRemoteAvatars() {
        listOf("Əli", "A B", "😀".repeat(24)).forEach { PublicProfiles.identity(it,"builder") }
        listOf("", " A", "A ", "A\nB", "A\u200DB", "A\u00A0B", "A\u0085B", "A\uD800B", "😀".repeat(25)).forEach {
            assertThrows(IllegalArgumentException::class.java) { PublicProfiles.identity(it,"builder") }
        }
        assertThrows(IllegalArgumentException::class.java) { PublicProfiles.identity("Əli","https://photo") }
        assertEquals("Əli",PublicProfiles.parse(profile()).displayName)
        assertEquals("😀".repeat(15), ProfileReplacement(1, "😀".repeat(15), "builder").displayName)
        assertThrows(IllegalArgumentException::class.java) { ProfileReplacement(1, "😀".repeat(16), "builder") }
        assertEquals("ə".repeat(24), PublicProfiles.parse(profile("ə".repeat(24))).displayName)
    }
    @Test fun replacementUsesIndependentRevisionAndDoesNotRetryConflict() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val calls = mutableListOf<String?>()
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
            assertEquals("profile",path); calls += body
            if(body == null) GameplayResponse(200,profile(revision=9)) else GameplayResponse(409,"""{"error":"revision_conflict"}""")
        })
        val session = sessions.current()!!
        assertEquals(9L,backend.profile(session).revision)
        try { backend.replaceProfile(session,ProfileReplacement(9,"Ayan","pilot")); fail() } catch (_: RevisionConflict) { }
        assertEquals(2,calls.size)
        assertEquals("""{"expected_profile_revision":9,"display_name":"Ayan","avatar_id":"pilot"}""",calls.last())
    }
    @Test fun changedAccountDropsProfileResponseEvenForSameUid() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val backend = GameplayHttpBackend(sessions, GameplayTransport { _, _, _ ->
            sessions.onAuthChanged(null); sessions.onAuthChanged("alice"); GameplayResponse(200,profile())
        })
        try { backend.profile(sessions.current()!!); fail() } catch (_: AccountChanged) { }
    }
    @Test fun traversalRetriesExactCursorAndRefreshDiscardsOldSnapshot() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val paths = mutableListOf<String>(); var lost = true
        val traversal = LeaderboardTraversal(sessions,LeaderboardRepository(GameplayReads { _, path ->
            paths += path
            if(path.endsWith("cursor=opaque")) {
                if(lost) { lost=false; throw IOException("lost") }; page(4)
            } else page(next="opaque")
        }), sessions.current()!!,LeaderboardScope.WEEKLY)
        assertEquals(listOf(1L),traversal.load().entries.map { it.rank })
        try { traversal.load(); fail() } catch (_: IOException) { }
        assertEquals(listOf(1L,4L),traversal.load().entries.map { it.rank })
        assertEquals(paths[1],paths[2]); traversal.load(); assertEquals(3,paths.size)
        assertEquals(listOf(1L),traversal.load(refresh=true).entries.map { it.rank })
        paths.forEach { assertTrue(allowedReadPath(it)) }
        sessions.onAuthChanged(null); sessions.onAuthChanged("alice")
        try { traversal.load(); fail() } catch (_: AccountChanged) { }
        assertEquals(4,paths.size)
    }
    @Test fun traversalRejectsRolloverAndDoesNotConsumeCursor() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice"); var invalid = true
        val traversal = LeaderboardTraversal(sessions,LeaderboardRepository(GameplayReads { _, path ->
            if(!path.contains("cursor")) page(next="opaque") else page(3).let {
                if(invalid) it.replace("2026-11-09","2026-11-16") else it
            }
        }), sessions.current()!!,LeaderboardScope.WEEKLY)
        traversal.load()
        try { traversal.load(); fail() } catch (_: IllegalArgumentException) { }
        invalid=false; assertEquals(2,traversal.load().entries.size)
    }
    @Test fun editorRetriesLostReplacementAndRequiresNewActionAfterConflict() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val writes = mutableListOf<ProfileReplacement>(); var conflict = false
        val access = object : ProfileAccess {
            override suspend fun profile(session: appbackend.wobbrik.billing.BillingSession) = PublicProfile(if(conflict) 4 else 1,"Əli","builder")
            override suspend fun replaceProfile(session: appbackend.wobbrik.billing.BillingSession, replacement: ProfileReplacement): PublicProfile {
                writes += replacement
                if(writes.size == 1) throw IOException("lost")
                if(conflict) throw RevisionConflict()
                return PublicProfile(2,replacement.displayName,replacement.avatarId)
            }
        }
        val editor = ProfileEditor(sessions,access,sessions.current()!!); editor.load()
        try { editor.save("Ayan","pilot"); fail() } catch (_: IOException) { }
        try { editor.save("Murad","architect"); fail() } catch (_: IllegalArgumentException) { }
        assertTrue(editor.save("Ayan","pilot") is ProfileSaveResult.Saved)
        assertEquals(writes[0],writes[1])
        conflict=true
        assertEquals(4L,(editor.save("Murad","architect") as ProfileSaveResult.Conflict).current.revision)
        assertEquals(3,writes.size)
        conflict=false; editor.save("Murad","architect")
        assertEquals(4L,writes.last().expectedRevision)
    }
    @Test fun readBoundaryRejectsQueryInjectionAndWrongScopeAndQuotedBooleans() {
        assertTrue(allowedReadPath("profile")); assertFalse(allowedReadPath("profile?uid=bob"))
        assertFalse(allowedReadPath("leaderboard?scope=weekly&limit=20&cursor=a&uid=bob"))
        assertThrows(IllegalArgumentException::class.java) { LeaderboardReads.cursor("../secret") }
        assertThrows(IllegalArgumentException::class.java) { LeaderboardReads.parse(page(),LeaderboardScope.ALL) }
        assertThrows(IllegalArgumentException::class.java) { LeaderboardReads.parse(page().replace("\"is_self\":false","\"is_self\":\"false\""),LeaderboardScope.WEEKLY) }
    }
}
