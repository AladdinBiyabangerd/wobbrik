package appbackend.wobbrik.game

import appbackend.wobbrik.billing.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ReadOnlyGameCacheTest {
    private class Fixture {
        val bytes = mutableMapOf<String, ByteArray>()
        var fail = false
        val files: (String) -> JournalFile = { key -> object : JournalFile {
            override fun read() = bytes[key]
            override fun replace(bytes: ByteArray) { if (fail) throw IOException("disk"); this@Fixture.bytes[key] = bytes.clone() }
        } }
        val cache = ReadOnlyGameCache(files)
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session get() = sessions.current()!!
        var revision = 1
        var network = true
        var calls = 0
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            calls++; if (!network) throw IOException("offline")
            GameplayResponse(200, if (path == "sync") body(revision) else """{"buildings":[]}""")
        })
        val account get() = ReadOnlyAccount("alice", RecoveryDtos.snapshot(body(1)), RecoveryDtos.catalog(catalog))
    }
    @Test fun acceptedCatalogAndSnapshotReopenWithoutGrantingReady() {
        val f = Fixture(); f.cache.accepted(f.account)
        val reopened = ReadOnlyGameCache(f.files).account("alice")!!
        assertEquals(f.account, reopened); assertNull(f.cache.account("bob"))
        f.cache.accepted(ReadOnlyAccount("alice", RecoveryDtos.snapshot(body(0)), reopened.catalog))
        assertEquals(1L, f.cache.account("alice")!!.snapshot.revision)
    }
    @Test fun completeViewReplaysWithoutNetworkAndMissingPagesOrRevisionFailClosed() = runBlocking {
        val f = Fixture()
        val live = f.cache.capture(f.session, f.account, "inventory", f.backend) { InventoryRepository(it).page(f.session) }
        f.network = false; val calls = f.calls
        val replay = ReadOnlyGameCache(f.files).replay(f.session, f.account, "inventory") { InventoryRepository(it).page(f.session) }
        assertEquals(live, replay); assertEquals(calls, f.calls)
        assertTrue(runCatching { f.cache.replay(f.session, f.account, "inventory") { InventoryRepository(it).page(f.session, "next") } }.isFailure)
        assertTrue(runCatching { f.cache.replay(f.session, f.account.copy(snapshot = RecoveryDtos.snapshot(body(2))), "inventory") { InventoryRepository(it).page(f.session) } }.isFailure)
        val bob = BillingSession("bob", "epoch")
        assertTrue(runCatching { f.cache.replay(bob, f.account, "inventory") { InventoryRepository(it).page(bob) } }.isFailure)
    }
    @Test fun racingRevisionDoesNotReplacePreviousBundleAndCacheDiskFailureDoesNotFailLiveView() = runBlocking {
        val f = Fixture()
        f.cache.capture(f.session, f.account, "inventory", f.backend) { InventoryRepository(it).page(f.session) }
        val original = f.bytes.mapValues { it.value.clone() }
        f.revision = 2
        assertTrue(runCatching { f.cache.capture(f.session, f.account, "inventory", f.backend) { InventoryRepository(it).page(f.session) } }.isFailure)
        original.forEach { (key, value) -> assertArrayEquals(value, f.bytes[key]) }
        f.revision = 1; f.fail = true
        assertEquals(emptyList<OwnedBuilding>(), f.cache.capture(f.session, f.account, "other", f.backend) { InventoryRepository(it).page(f.session) }.buildings)
    }
    @Test fun epochChangeDuringReadCannotCacheAnotherLoginResponse() = runBlocking {
        val f = Fixture(); val session = f.session
        assertTrue(runCatching { f.cache.capture(session, f.account, "inventory", f.backend) {
            val result = InventoryRepository(it).page(session)
            f.sessions.onAuthChanged("alice")
            result
        } }.isFailure)
        assertTrue(f.bytes.isEmpty())
    }
    @Test fun inventoryReplayDoesNotWaitForUnrelatedAccountFsync() = runBlocking {
        val f = Fixture()
        val entered = java.util.concurrent.CountDownLatch(1)
        val release = java.util.concurrent.CountDownLatch(1)
        var blockWrites = false
        val cache = ReadOnlyGameCache { key ->
            val file = f.files(key)
            object : JournalFile {
                override fun read() = file.read()
                override fun replace(bytes: ByteArray) {
                    if (blockWrites) {
                        entered.countDown()
                        check(release.await(5, java.util.concurrent.TimeUnit.SECONDS))
                    }
                    file.replace(bytes)
                }
            }
        }
        cache.capture(f.session, f.account, "inventory-", f.backend) { InventoryRepository(it).page(f.session) }
        blockWrites = true
        val writer = async(Dispatchers.IO) { cache.accepted(f.account) }
        check(entered.await(5, java.util.concurrent.TimeUnit.SECONDS))
        val start = System.nanoTime()
        val local = async(Dispatchers.IO) {
            cache.replay(f.session, f.account, "inventory-") { InventoryRepository(it).page(f.session) }
        }
        val completedWhileWriteBlocked = try {
            withTimeoutOrNull(600) { local.await() } != null
        } finally { release.countDown() }
        local.await(); writer.await()
        println("CACHE-LOCK localAndBlockedWriterMs=${(System.nanoTime()-start)/1_000_000} localBeforeRelease=$completedWhileWriteBlocked HTTP=${f.calls}")
        assertTrue("Inventory must not wait for the account file fsync", completedWhileWriteBlocked)
    }
    @Test fun sameViewReadCannotResolveAnInProgressWrite() = runBlocking {
        val f = Fixture()
        val entered = java.util.concurrent.CountDownLatch(1)
        val release = java.util.concurrent.CountDownLatch(1)
        val reading = CompletableDeferred<Unit>()
        var blockWrites = false
        val writing = java.util.concurrent.atomic.AtomicBoolean()
        val overlapped = java.util.concurrent.atomic.AtomicBoolean()
        val cache = ReadOnlyGameCache { key ->
            val file = f.files(key)
            object : JournalFile {
                override fun read(): ByteArray? {
                    if (writing.get()) overlapped.set(true)
                    return file.read()
                }
                override fun replace(bytes: ByteArray) {
                    if (blockWrites) {
                        writing.set(true); entered.countDown()
                        check(release.await(5, java.util.concurrent.TimeUnit.SECONDS))
                    }
                    file.replace(bytes); writing.set(false)
                }
            }
        }
        suspend fun capture() = cache.capture(f.session, f.account, "inventory-", f.backend) { InventoryRepository(it).page(f.session) }
        capture(); blockWrites = true
        val writer = async(Dispatchers.IO) { capture() }
        check(entered.await(5, java.util.concurrent.TimeUnit.SECONDS))
        val reader = async(Dispatchers.IO) {
            reading.complete(Unit)
            cache.replay(f.session, f.account, "inventory-") { InventoryRepository(it).page(f.session) }
        }
        try {
            reading.await(); delay(100)
            assertFalse(reader.isCompleted); assertFalse(overlapped.get())
        } finally { release.countDown() }
        writer.await(); assertEquals(emptyList<OwnedBuilding>(), reader.await().buildings)
        assertFalse(overlapped.get())
        // Reacquisition after the last user exits still serializes the same file.
        repeat(10) { assertTrue(cache.replay(f.session, f.account, "inventory-") { InventoryRepository(it).page(f.session) }.buildings.isEmpty()) }
    }
    companion object {
        private const val catalog = """{"rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1","items":[],"land_offers":[]}"""
        private fun body(revision: Int) = """{"rules_version":"wobbrik-v1","revision":$revision,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
    }
}
