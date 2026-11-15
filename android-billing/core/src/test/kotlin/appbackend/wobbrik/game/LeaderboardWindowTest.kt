package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class LeaderboardWindowTest {
    private fun page(start: Int, count: Int, next: String? = null, snapshot: String = "2026-11-15T00:00:00Z"): String {
        val entries = (start until start + count).joinToString(",") { rank ->
            """{"rank":$rank,"result_id":"result$rank","floors":${100-rank},"accepted_at":"2026-11-14T00:00:00Z","is_self":false,"display_name":"Əli","avatar_id":"builder"}"""
        }
        return """{"scope":"weekly","week_start":"2026-11-09","snapshot_at":"$snapshot","expires_at":"2026-11-15T00:15:00Z","entries":[$entries]${next?.let { ",\"next_cursor\":\"$it\"" } ?: ""}}"""
    }
    @Test fun firstBudgetIsBoundedAndSmallerViewportDoesNotReadAgain() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val paths = mutableListOf<String>()
        val traversal = LeaderboardTraversal(sessions, LeaderboardRepository(GameplayReads { _, path ->
            paths += path; page(1, 4, "after4")
        }), sessions.current()!!, LeaderboardScope.WEEKLY)
        assertEquals(4,traversal.fit(2).entries.size)
        assertEquals("leaderboard?scope=weekly&limit=4", paths.single())
        assertEquals(4,traversal.fit(1).entries.size)
        assertEquals(1,paths.size)
        assertEquals(8,LeaderboardWindowPolicy.DefaultRows)
        assertEquals(20,LeaderboardWindowPolicy.requestSize(Int.MAX_VALUE))
    }
    @Test fun growingViewportReadsOnlyMissingPlayersAndRetriesExactCursor() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val paths = mutableListOf<String>(); var lost = true
        val traversal = LeaderboardTraversal(sessions, LeaderboardRepository(GameplayReads { _, path ->
            paths += path
            when {
                "cursor=after7" in path -> { assertTrue(path.contains("limit=1")); page(8,1,"after8") }
                "cursor=after4" in path -> { if(lost) {lost=false;throw IOException("lost")}; page(5,3,"after7") }
                else -> page(1,4,"after4")
            }
        }), sessions.current()!!, LeaderboardScope.WEEKLY)
        traversal.fit(4)
        try {traversal.fit(7);fail()} catch(_:IOException) {}
        assertEquals((1L..7).toList(),traversal.fit(7).entries.map {it.rank})
        assertEquals(paths[1],paths[2]);assertTrue(paths[1].contains("limit=3"))
        assertEquals(8,traversal.fit(8).entries.size)
        traversal.fit(5);assertEquals(4,paths.size)
        traversal.fit(4,refresh=true);assertFalse(paths.last().contains("cursor="))
    }
    @Test fun exhaustedAndPrivacyFilteredBoardsDoNotInventPlayers() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        var calls=0
        val traversal=LeaderboardTraversal(sessions,LeaderboardRepository(GameplayReads {_,_->calls++;page(3,2)}),sessions.current()!!,LeaderboardScope.WEEKLY)
        assertEquals(listOf(3L,4L),traversal.fit(7).entries.map {it.rank})
        assertEquals(2,traversal.fit(20).entries.size);assertEquals(1,calls)
    }
    @Test fun readBoundaryAndResponseRespectRequestedLimit() {
        for(n in 1..20) assertTrue(allowedReadPath("leaderboard?scope=weekly&limit=$n"))
        for(n in listOf("0","21","100","04","-1","4&uid=bob")) assertFalse(allowedReadPath("leaderboard?scope=weekly&limit=$n"))
        assertThrows(IllegalArgumentException::class.java) {LeaderboardReads.parse(page(1,5),LeaderboardScope.WEEKLY,4)}
        assertThrows(IllegalArgumentException::class.java) {LeaderboardReads.parse(page(1,0,"empty"),LeaderboardScope.WEEKLY)}
    }
    @Test fun wrongSnapshotDoesNotConsumeTheGrowthCursor() = runBlocking {
        val sessions=AccountSessions {"token"};sessions.onAuthChanged("alice");var invalid=true
        val paths=mutableListOf<String>()
        val traversal=LeaderboardTraversal(sessions,LeaderboardRepository(GameplayReads {_,path->
            paths+=path
            if("cursor=" !in path) page(1,4,"after4") else page(5,2,snapshot=if(invalid) "2026-11-15T00:01:00Z" else "2026-11-15T00:00:00Z")
        }),sessions.current()!!,LeaderboardScope.WEEKLY)
        traversal.fit(4)
        try {traversal.fit(6);fail()} catch(_:IllegalArgumentException) {}
        invalid=false;assertEquals(6,traversal.fit(6).entries.size);assertEquals(paths[1],paths[2])
    }
    @Test fun cancelledReadAndReloginCannotPublishIntoTheWindow() = runBlocking {
        val sessions=AccountSessions {"token"};sessions.onAuthChanged("alice")
        val entered=CompletableDeferred<Unit>();val release=CompletableDeferred<Unit>()
        val repository=LeaderboardRepository(GameplayReads {_,_->
            entered.complete(Unit);withContext(NonCancellable) {release.await()};page(1,4)
        })
        val traversal=LeaderboardTraversal(sessions,repository,sessions.current()!!,LeaderboardScope.WEEKLY)
        val job=launch {traversal.fit(4);fail("cancelled result")}
        entered.await();job.cancel();release.complete(Unit);job.join()
        sessions.onAuthChanged(null);sessions.onAuthChanged("alice")
        try {traversal.fit(4);fail()} catch(_:AccountChanged) {}
    }
}
