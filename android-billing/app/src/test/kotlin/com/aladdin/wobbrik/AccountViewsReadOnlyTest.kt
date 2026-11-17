package com.aladdin.wobbrik

import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class AccountViewsReadOnlyTest {
    private class Fixture {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val body = """{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(body),
            RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        val rows = ConcurrentHashMap<String, ByteArray>()
        val reads = AtomicInteger()
        val network = AtomicInteger()
        val cache = ReadOnlyGameCache { key -> object : JournalFile {
            override fun read(): ByteArray? { reads.incrementAndGet(); return rows[key] }
            override fun replace(bytes: ByteArray) { rows[key] = bytes.clone() }
        } }
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            network.incrementAndGet()
            GameplayResponse(200, if (path == "sync") body else """{"buildings":[]}""")
        })
        val views = AccountViews(backend, cache, sessions)
        suspend fun seed() {
            cache.capture(session, account, "inventory-", backend) { InventoryRepository(it).page(session) }
            reads.set(0); network.set(0)
        }
    }

    @Test fun oneReplayPublishesSameResultOnceOnCallerThread() = runBlocking {
        val f = Fixture(); f.seed()
        val events = mutableListOf<String>()
        val caller = Thread.currentThread()
        var parsed = 0
        var local: Any? = null
        val result = f.views.read(AccountView(f.account, null), f.session, "inventory-", refresh = true,
            onCached = { assertSame(caller, Thread.currentThread()); events.add("cached") },
            onLocal = { assertSame(caller, Thread.currentThread()); local = it; events.add("local") },
            onFresh = { fail("read-only cannot become fresh") }) {
            assertNotSame(caller, Thread.currentThread()); parsed++
            InventoryRepository(it).page(f.session)
        }
        assertSame(result, local)
        assertEquals(listOf("cached", "local"), events)
        assertEquals(1, parsed); assertEquals(1, f.reads.get()); assertEquals(0, f.network.get())
    }

    @Test fun withoutOnLocalStillReadsOnce() = runBlocking {
        val f = Fixture(); f.seed()
        var cached = 0
        f.views.read(AccountView(f.account, null), f.session, "inventory-", onCached = { cached++ }) {
            InventoryRepository(it).page(f.session)
        }
        assertEquals(1, cached); assertEquals(1, f.reads.get()); assertEquals(0, f.network.get())
    }

    @Test fun nullableResultDoesNotTriggerAnotherReplay() = runBlocking {
        val f = Fixture(); f.seed()
        var localCalls = 0; var parses = 0
        val value = f.views.read<String?>(AccountView(f.account, null), f.session, "inventory-",
            onLocal = { assertNull(it); localCalls++ }) { parses++; null }
        assertNull(value); assertEquals(1, localCalls); assertEquals(1, parses)
        assertEquals(1, f.reads.get()); assertEquals(0, f.network.get())
    }

    @Test fun missCorruptionRevisionAndQueryMismatchNeverPublishOrRetry() = runBlocking {
        for (mode in listOf("miss", "corrupt", "revision", "query")) {
            val f = Fixture(); f.seed()
            if (mode == "miss") f.rows.clear()
            if (mode == "corrupt") f.rows.replaceAll { _, _ -> "{}".toByteArray() }
            val account = if (mode == "revision") f.account.copy(snapshot =
                RecoveryDtos.snapshot(f.body.replace("\"revision\":1", "\"revision\":2"))) else f.account
            val error = runCatching {
                f.views.read(AccountView(account, null), f.session, if (mode == "query") "other" else "inventory-",
                    onCached = { fail(mode) }, onLocal = { _: InventoryPage -> fail(mode) }, onFresh = { fail(mode) }) {
                    InventoryRepository(it).page(f.session)
                }
            }.exceptionOrNull()
            assertNotNull(mode, error); assertEquals(mode, 1, f.reads.get()); assertEquals(0, f.network.get())
        }
    }

    @Test fun sessionChangeDuringReplayRejectsBeforeCallbacks() = runBlocking {
        for (uid in listOf("alice", "bob")) {
            val f = Fixture(); f.seed()
            val error = runCatching {
                f.views.read(AccountView(f.account, null), f.session, "inventory-",
                    onCached = { fail("old epoch published") }, onLocal = { _: InventoryPage -> fail("old epoch published") }) {
                    InventoryRepository(it).page(f.session).also { f.sessions.onAuthChanged(uid) }
                }
            }.exceptionOrNull()
            assertTrue(error is AccountChanged); assertEquals(1, f.reads.get()); assertEquals(0, f.network.get())
        }
    }

    @Test fun sessionChangeInCallbackRejectsReturn() = runBlocking {
        for (changeInCached in listOf(false, true)) {
            val f = Fixture(); f.seed(); var localCalls = 0
            val error = runCatching {
                f.views.read(AccountView(f.account, null), f.session, "inventory-",
                    onCached = { if (changeInCached) f.sessions.onAuthChanged("alice") },
                    onLocal = { _: InventoryPage -> localCalls++; f.sessions.onAuthChanged("bob") }) {
                    InventoryRepository(it).page(f.session)
                }
            }.exceptionOrNull()
            assertTrue(error is AccountChanged); assertEquals(if (changeInCached) 0 else 1, localCalls)
            assertEquals(1, f.reads.get()); assertEquals(0, f.network.get())
        }
    }

    @Test fun cancellationDuringReplayAndAfterLocalNeverReturnsValue() = runBlocking {
        for (duringReplay in listOf(false, true)) {
            val f = Fixture(); f.seed()
            val entered = CompletableDeferred<Unit>(); val blocked = CompletableDeferred<Unit>()
            var returned = false; var localCalls = 0
            val job = launch {
                val ownJob = currentCoroutineContext().job
                f.views.read(AccountView(f.account, null), f.session, "inventory-",
                    onLocal = { _: InventoryPage -> localCalls++; ownJob.cancel() }) {
                    if (duringReplay) { entered.complete(Unit); blocked.await() }
                    InventoryRepository(it).page(f.session)
                }
                returned = true
            }
            if (duringReplay) { withTimeout(5000) { entered.await() }; job.cancel() }
            job.join()
            assertTrue(job.isCancelled); assertFalse(returned)
            assertEquals(if (duringReplay) 0 else 1, localCalls)
            assertEquals(1, f.reads.get()); assertEquals(0, f.network.get())
        }
    }

    @Test fun mismatchedAccountRejectedBeforeDiskRead() = runBlocking {
        val f = Fixture(); f.seed()
        val error = runCatching {
            f.views.read(AccountView(f.account.copy(uid = "bob"), null), f.session, "inventory-",
                onLocal = { _: InventoryPage -> fail("wrong account") }) { InventoryRepository(it).page(f.session) }
        }.exceptionOrNull()
        assertTrue(error is AccountChanged); assertEquals(0, f.reads.get()); assertEquals(0, f.network.get())
    }
}
