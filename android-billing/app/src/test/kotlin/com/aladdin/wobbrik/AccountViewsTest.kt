package com.aladdin.wobbrik

import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class AccountViewsTest {
    @Test fun transientReadFailuresUseWholeCachedViewButAuthorizationNeverFallsBack() = runBlocking {
        val rows = mutableMapOf<String, ByteArray>()
        val cache = ReadOnlyGameCache { key -> object : JournalFile {
            override fun read() = rows[key]
            override fun replace(bytes: ByteArray) { rows[key] = bytes.clone() }
        } }
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = requireNotNull(sessions.current())
        val snapshotBody = """{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
        val snapshot = RecoveryDtos.snapshot(snapshotBody)
        val account = ReadOnlyAccount("alice", snapshot, RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        var status = 200
        var calls = 0
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
            assertNull(body); calls++
            if (status == 0) throw IOException("offline")
            GameplayResponse(status, if (status != 200) """{"error":"forbidden"}"""
                else if (path == "sync") snapshotBody else """{"buildings":[]}""")
        })
        cache.capture(session, account, "inventory-", backend) { InventoryRepository(it).page(session) }
        val views = AccountViews(backend, cache)
        val view = AccountView(account, AuthenticatedRecoveryOwner.State.Ready(snapshot, account.catalog, "alice"))
        for (failure in listOf(0, 408, 429, 500, 503)) {
            status = failure
            var cached = false
            val page = views.read(view, session, "inventory-", onCached = { cached = true }) { InventoryRepository(it).page(session) }
            assertTrue(cached); assertTrue(page.buildings.isEmpty())
        }
        for (denied in listOf(400, 401, 403, 404)) {
            status = denied
            val failure = runCatching { views.read(view, session, "inventory-") { InventoryRepository(it).page(session) } }.exceptionOrNull()
            assertTrue(failure is GameplayHttpError)
        }
        val before = calls
        views.read(AccountView(account, null), session, "inventory-") { InventoryRepository(it).page(session) }
        assertEquals(before, calls)
        sessions.onAuthChanged("alice")
        assertTrue(runCatching { views.read(view, session, "inventory-") { InventoryRepository(it).page(session) } }.exceptionOrNull() is AccountChanged)
    }
}
