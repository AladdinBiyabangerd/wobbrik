package com.aladdin.wobbrik

import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import kotlinx.coroutines.*
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
        val views = AccountViews(backend, cache, sessions)
        val view = AccountView(account, AuthenticatedRecoveryOwner.State.Ready(snapshot, account.catalog, "alice"))
        // First online visit captures the view and revision; subsequent navigation is local.
        val callerThread = Thread.currentThread()
        views.read(view, session, "inventory-") {
            assertNotSame(callerThread, Thread.currentThread())
            InventoryRepository(it).page(session)
        }
        val capturedCalls = calls
        views.read(view, session, "inventory-") { InventoryRepository(it).page(session) }
        assertEquals(capturedCalls, calls)
        views.read(view, session, "inventory-", refresh = true) { InventoryRepository(it).page(session) }
        assertEquals(capturedCalls + 2, calls)
        // Equal snapshots from a new capability still require fresh reads.
        val nextView = AccountView(account, AuthenticatedRecoveryOwner.State.Ready(snapshot, account.catalog, "alice"))
        views.read(nextView, session, "inventory-") { InventoryRepository(it).page(session) }
        assertEquals(capturedCalls + 4, calls)
        for (failure in listOf(0, 408, 429, 500, 503)) {
            status = failure
            var cached = false
            val page = views.read(view, session, "inventory-", onCached = { cached = true }, refresh = true) { InventoryRepository(it).page(session) }
            assertTrue(cached); assertTrue(page.buildings.isEmpty())
        }
        for (denied in listOf(400, 401, 403, 404)) {
            status = denied
            val failure = runCatching { views.read(view, session, "inventory-", refresh = true) { InventoryRepository(it).page(session) } }.exceptionOrNull()
            assertTrue(failure is GameplayHttpError)
        }
        // A denied refresh invalidates online reuse too.
        assertTrue(runCatching {
            views.read(view, session, "inventory-") { InventoryRepository(it).page(session) }
        }.exceptionOrNull() is GameplayHttpError)
        val before = calls
        views.read(AccountView(account, null), session, "inventory-") { InventoryRepository(it).page(session) }
        assertEquals(before, calls)
        sessions.onAuthChanged("alice")
        assertTrue(runCatching { views.read(view, session, "inventory-", refresh = true) { InventoryRepository(it).page(session) } }.exceptionOrNull() is AccountChanged)
    }
    private class Fixture {
        val rows = mutableMapOf<String, ByteArray>()
        val cache = ReadOnlyGameCache { key -> object : JournalFile {
            override fun read() = rows[key]
            override fun replace(bytes: ByteArray) { rows[key] = bytes.clone() }
        } }
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = requireNotNull(sessions.current())
        val body = """{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(body),
            RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        val view = AccountView(account, AuthenticatedRecoveryOwner.State.Ready(account.snapshot, account.catalog, "alice"))
        var calls = 0
        var bytes = 0
        var gate: CompletableDeferred<Unit>? = null
        val entered = CompletableDeferred<Unit>()
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            calls++
            if (gate != null) { entered.complete(Unit); gate?.await() }
            val response = if (path == "sync") body else """{"buildings":[]}"""
            bytes += response.toByteArray().size
            GameplayResponse(200, response)
        })
        val views = AccountViews(backend, cache, sessions)
        suspend fun seed() { cache.capture(session, account, "inventory-", backend) { InventoryRepository(it).page(session) } }
    }

    @Test fun localPresentationPrecedesNetworkAndNeverGrantsFreshness() = runBlocking {
        val f = Fixture(); f.seed()
        f.gate = CompletableDeferred()
        val local = CompletableDeferred<Long>()
        var cached = false; var fresh = false
        val start = System.nanoTime()
        val read = async {
            f.views.read(f.view, f.session, "inventory-", onCached = { cached = true },
                onLocal = { assertTrue(cached); assertFalse(fresh); local.complete(System.nanoTime()) },
                onFresh = { fresh = true }) { InventoryRepository(it).page(f.session) }
        }
        val localAt = withTimeout(5000) { local.await() }
        withTimeout(5000) { f.entered.await() }
        assertFalse(read.isCompleted); assertFalse(fresh)
        // Controlled slow transport, not a device/network benchmark.
        delay(250)
        f.gate!!.complete(Unit)
        read.await()
        assertTrue(fresh)
        val liveMs = (System.nanoTime() - start) / 1_000_000
        val localMs = (localAt - start) / 1_000_000
        val calls = f.calls; val bytes = f.bytes
        repeat(10) { f.views.read(f.view, f.session, "inventory-") { InventoryRepository(it).page(f.session) } }
        assertEquals(calls, f.calls); assertEquals(bytes, f.bytes)
        println("PERF controlled250ms local=$localMs ms live=$liveMs ms; warm10 requests=${f.calls-calls} bytes=${f.bytes-bytes}")
    }

    @Test fun accountSwitchDuringLocalReplayRejectsBeforeTransport() = runBlocking {
        val f = Fixture(); f.seed()
        val calls = f.calls
        val result = runCatching {
            f.views.read(f.view, f.session, "inventory-", onLocal = { f.sessions.onAuthChanged("bob") }) {
                InventoryRepository(it).page(f.session)
            }
        }
        assertTrue(result.exceptionOrNull() is AccountChanged)
        assertEquals(calls, f.calls)
    }

    @Test fun cancellationAfterLocalPresentationDoesNotPublishFreshData() = runBlocking {
        val f = Fixture(); f.seed(); f.gate = CompletableDeferred()
        var fresh = false
        val read = launch {
            f.views.read(f.view, f.session, "inventory-", onLocal = {}, onFresh = { fresh = true }) {
                InventoryRepository(it).page(f.session)
            }
        }
        withTimeout(5000) { f.entered.await() }
        read.cancelAndJoin()
        assertFalse(fresh)
    }

    @Test fun otherRevisionCannotBePresentedFromLocalCache() = runBlocking {
        val f = Fixture(); f.seed()
        val changed = f.account.copy(snapshot = RecoveryDtos.snapshot(f.body.replace("\"revision\":1", "\"revision\":2")))
        var local = false
        runCatching {
            f.views.read(AccountView(changed, f.view.ready), f.session, "inventory-", onLocal = { local = true }) {
                InventoryRepository(it).page(f.session)
            }
        }
        assertFalse(local)
    }

}
