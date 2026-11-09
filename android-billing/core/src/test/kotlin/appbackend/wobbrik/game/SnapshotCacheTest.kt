package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.billing.BillingSession
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class SnapshotCacheTest {
    private class MemoryStore : SnapshotCacheStore {
        val rows = mutableMapOf<Pair<String, String>, CachedSnapshotRow>()
        var replaces = 0
        var fail = false
        override suspend fun read(appId: String, uid: String): CachedSnapshotRow? { check(!fail) { "disk" }; return rows[appId to uid] }
        override suspend fun replace(row: CachedSnapshotRow) { check(!fail) { "disk" }; replaces++; rows[row.appId to row.uid] = row }
        override suspend fun delete(appId: String, uid: String) { rows.remove(appId to uid) }
    }

    @Test fun newerRevisionWinsAndOlderIsIgnored() = runBlocking {
        val store = MemoryStore(); val cache = SnapshotCache(store)
        cache.accepted("alice", wire(revision = 3, coins = 30))
        cache.accepted("alice", wire(revision = 5, coins = 50))
        cache.accepted("alice", wire(revision = 4, coins = 40))
        val last = cache.lastKnown("alice")!!
        assertEquals(5L, last.revision); assertEquals(50L, last.coins)
        assertEquals(2, store.replaces)
    }

    @Test fun sameRevisionBalanceUpdateIsAcceptedAndIdenticalBodyIsNotRewritten() = runBlocking {
        val store = MemoryStore(); val cache = SnapshotCache(store)
        cache.accepted("alice", wire(revision = 2, coins = 10))
        cache.accepted("alice", wire(revision = 2, coins = 510)) // Billing grant: same revision, new balance.
        cache.accepted("alice", wire(revision = 2, coins = 510))
        assertEquals(510L, cache.lastKnown("alice")!!.coins)
        assertEquals(2, store.replaces)
    }

    @Test fun accountsAndAppsAreIsolated() = runBlocking {
        val store = MemoryStore(); val alice = SnapshotCache(store); val other = SnapshotCache(store, "other-app")
        alice.accepted("alice", wire(revision = 9, coins = 1))
        alice.accepted("bob", wire(revision = 1, coins = 2))
        assertEquals(1L, alice.lastKnown("alice")!!.coins)
        assertEquals(2L, alice.lastKnown("bob")!!.coins)
        assertNull(alice.lastKnown("carol"))
        assertNull(other.lastKnown("alice"))
        assertEquals(setOf("wobbrik" to "alice", "wobbrik" to "bob"), store.rows.keys)
    }

    @Test fun rulesMismatchIsDroppedOnWriteAndRead() = runBlocking {
        val store = MemoryStore(); val cache = SnapshotCache(store)
        val foreign = GameSnapshot("wobbrik-v2", 7, 70, 0, null).also { it.wire = body(7, 70).replace("wobbrik-v1", "wobbrik-v2") }
        cache.accepted("alice", foreign)
        assertTrue(store.rows.isEmpty())
        store.rows["wobbrik" to "alice"] = CachedSnapshotRow("wobbrik", "alice", "wobbrik-v2", 7, body(7, 70))
        assertNull(cache.lastKnown("alice")); assertTrue(store.rows.isEmpty())
        store.rows["wobbrik" to "alice"] = CachedSnapshotRow("wobbrik", "alice", "wobbrik-v1", 7, body(7, 70).replace("wobbrik-v1", "wobbrik-v2"))
        assertNull(cache.lastKnown("alice")); assertTrue(store.rows.isEmpty())
        store.rows["wobbrik" to "alice"] = CachedSnapshotRow("wobbrik", "alice", "wobbrik-v1", 8, body(7, 70))
        assertNull(cache.lastKnown("alice")); assertTrue(store.rows.isEmpty())
        store.rows["wobbrik" to "alice"] = CachedSnapshotRow("wobbrik", "alice", "wobbrik-v1", 7, "{")
        assertNull(cache.lastKnown("alice")); assertTrue(store.rows.isEmpty())
        // A newer foreign-rules row never blocks a valid accepted snapshot.
        store.rows["wobbrik" to "alice"] = CachedSnapshotRow("wobbrik", "alice", "wobbrik-v0", 99, "{}")
        cache.accepted("alice", wire(revision = 1, coins = 3))
        assertEquals(3L, cache.lastKnown("alice")!!.coins)
    }

    @Test fun snapshotsNotParsedFromTheServerAreNeverCached() = runBlocking {
        val store = MemoryStore()
        SnapshotCache(store).accepted("alice", GameSnapshot("wobbrik-v1", 1, 1, 0, null))
        SnapshotCache(store).accepted("alice", wire(1, 1).copy(coins = 999))
        assertTrue(store.rows.isEmpty())
    }

    @Test fun cacheNeverMakesReadyOrSubmitPassAndJournalBytesStayUntouched() = runBlocking {
        val f = OwnerFixture()
        try {
            f.store.rows["wobbrik" to "alice"] = CachedSnapshotRow("wobbrik", "alice", "wobbrik-v1", 6, body(6, 60))
            f.journal.save("alice", GameplayWire.start("retained", 6))
            val retained = f.bytes!!.copyOf(); val writes = f.writes
            f.submitFails = true
            f.owner.onAuthChanged("alice"); yield()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            val cached = f.owner.cached.value!!
            assertEquals(60L, cached.coins)
            val forged = AuthenticatedRecoveryOwner.State.Ready(cached, null, "alice")
            assertFalse(f.owner.submit(forged) { error("Cached values must never build a command") })
            assertFalse(AttemptCoordinator(f.sessions, f.backend, f.journal).ready())
            assertArrayEquals(retained, f.bytes); assertEquals(writes, f.writes)
            assertEquals(1, f.submits); assertEquals(0, f.syncs)
            assertEquals(1, f.store.rows.size); assertEquals(0, f.store.replaces)
        } finally { f.close() }
    }

    @Test fun onlyAcceptedPublishedSnapshotsAreWrittenAndJournalIsNotTouched() = runBlocking {
        val f = OwnerFixture()
        try {
            f.coins = 12
            f.owner.onAuthChanged("alice"); yield()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Ready)
            assertEquals(12L, SnapshotCache(f.store).lastKnown("alice")!!.coins)
            assertEquals(0, f.writes); assertNull(f.bytes)
            // A rules-mismatched server answer is rejected by the coordinator and never cached.
            f.rules = "wobbrik-v2"
            f.owner.onForeground(); yield()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            assertEquals(1, f.store.replaces)
            assertEquals(12L, f.owner.cached.value!!.coins)
        } finally { f.close() }
    }

    @Test fun accountSwitchAndSignOutNeverShowAnotherAccountsValues() = runBlocking {
        val f = OwnerFixture()
        try {
            f.coins = 12
            f.owner.onAuthChanged("alice"); yield()
            assertEquals(12L, f.owner.cached.value!!.coins)
            f.syncFails = true
            f.owner.onAuthChanged("bob"); yield()
            assertNull(f.owner.cached.value)
            f.store.rows["wobbrik" to "bob"] = CachedSnapshotRow("wobbrik", "bob", "wobbrik-v1", 2, body(2, 22))
            f.owner.onAuthChanged("bob"); yield()
            assertEquals(22L, f.owner.cached.value!!.coins)
            f.owner.onAuthChanged(null); yield()
            assertNull(f.owner.cached.value)
            assertEquals(AuthenticatedRecoveryOwner.State.SignedOut, f.owner.state.value)
        } finally { f.close() }
    }

    @Test fun failingCacheStoreNeverBlocksReady() = runBlocking {
        val f = OwnerFixture()
        try {
            f.store.fail = true
            f.owner.onAuthChanged("alice"); yield()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Ready)
            assertEquals(1L, f.owner.cached.value!!.coins)
        } finally { f.close() }
    }

    private class OwnerFixture {
        val sessions = AccountSessions { "token" }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val store = MemoryStore()
        var bytes: ByteArray? = null
        var writes = 0
        val journal = DiskAttemptJournal { object : JournalFile {
            override fun read() = bytes
            override fun replace(value: ByteArray) { writes++; bytes = value }
        } }
        var coins = 1L; var rules = "wobbrik-v1"; var syncFails = false; var submitFails = false
        var syncs = 0; var submits = 0
        val backend = object : AttemptBackend {
            override suspend fun catalog(session: BillingSession): RecoveryCatalog? = null
            override suspend fun sync(session: BillingSession): GameSnapshot {
                syncs++; check(!syncFails) { "offline" }
                return if (rules == "wobbrik-v1") wire(0, coins) else GameSnapshot(rules, 0, coins, 0, null)
            }
            override suspend fun submit(session: BillingSession, command: AttemptCommand) { submits++; check(!submitFails) { "lost" } }
        }
        val owner = AuthenticatedRecoveryOwner(sessions, backend, journal, scope, cache = SnapshotCache(store))
        fun close() = scope.cancel()
    }

    companion object {
        private fun body(revision: Long, coins: Long) =
            """{"rules_version":"wobbrik-v1","revision":$revision,"coins":$coins,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
        private fun wire(revision: Long, coins: Long) = GameplayWire.snapshot(body(revision, coins))
    }
}
