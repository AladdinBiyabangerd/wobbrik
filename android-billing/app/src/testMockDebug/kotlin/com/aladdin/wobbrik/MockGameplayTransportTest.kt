package com.aladdin.wobbrik

import appbackend.wobbrik.game.*
import appbackend.wobbrik.billing.AccountSessions
import com.aladdin.wobbrik.engine.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MockGameplayTransportTest {
    @Test fun localLifecyclePersistsAndDeduplicatesAfterRestart() = runBlocking {
        var disk: String? = null
        var backend = MockGameplayTransport(disk) { disk = it }
        suspend fun read() = RecoveryDtos.snapshot(backend.request("sync", "local-mock-only", null).body)
        suspend fun send(path: String, command: AttemptCommand): AttemptReceipt {
            val response = backend.request(path, "local-mock-only", command.envelope)
            assertEquals(200, response.status)
            return RecoveryDtos.attemptReceipt(response.body, command.route)
        }
        assertNull(read().attempt)
        assertEquals(30, RecoveryDtos.catalog(backend.request("catalog", "local-mock-only", null).body).items.size)
        val start = GameplayWire.start("local-attempt", 1)
        send("attempts/start", start)
        val checkpoint = GameplayWire.checkpoint("checkpoint", 2, "local-attempt", 1, 10, 1, 0, "{}")
        send("attempts/checkpoint", checkpoint)
        backend = MockGameplayTransport(disk) { disk = it }
        assertEquals(10L, read().attempt!!.floors)
        val parts = FinishCosmetics.categories.associateWith { "base_1_$it" }
        val finish = GameplayWire.finish("finish", 3, "local-attempt", 1, parts)
        val receipt = send("attempts/finish", finish)
        backend = MockGameplayTransport(disk) { disk = it }
        assertEquals(receipt, send("attempts/finish", finish))
        assertEquals(4L, read().revision)
        assertFalse(read().attempt!!.active)
        val inventory = InventoryReads.inventory(backend.request("inventory?limit=20", "local-mock-only", null).body)
        assertEquals(1, inventory.buildings.size)
        val detail = backend.request("buildings/${inventory.buildings.single().id}?view=display", "local-mock-only", null)
        assertEquals(200, detail.status)
        assertEquals(inventory.buildings.single().id, InventoryReads.building(detail.body).id)
        assertEquals(1250L, read().coins) // Mock play never invents real rewards.
    }

    @Test fun productionPendingReconcilerDrainsMockResultsWithGeometry() = runBlocking {
        var disk: String? = null
        val transport = MockGameplayTransport(disk) { disk = it }
        val sessions = AccountSessions { "local-mock-only" }.also { it.onAuthChanged("local-mock-profile") }
        val backend = GameplayHttpBackend(sessions, transport)
        val storageBytes = mutableMapOf<String, ByteArray>()
        val files: (String) -> JournalFile = { key -> object : JournalFile {
            override fun read() = storageBytes[key]
            override fun replace(bytes: ByteArray) { storageBytes[key] = bytes.clone() }
        } }
        val results = PendingGameResults(files)
        val cp = PracticeEngine().checkpoint()
        val terminal = cp.copy(tickUnits = 0, state = cp.state.copy(phase = Phase.FINISHED, paused = true,
            floors = List(10) { Floor(0.0, it * PracticeRulesV1.FLOOR_HEIGHT) }))
        val parts = FinishCosmetics.categories.associateWith { "base_1_$it" }
        results.enqueue("local-mock-profile", PendingGameResult("terminal", terminal, parts))
        val coordinator = AttemptCoordinator(sessions, backend, DiskAttemptJournal { files("wire-$it") }, results::committed)
        val session = requireNotNull(sessions.current())
        val reconciler = PendingResultReconciler(results)
        val final = reconciler.reconcile(session, coordinator, coordinator.recover(session))
        assertTrue(results.pending(session.uid).isEmpty())
        assertEquals(10L, final.attempt!!.floors)
        assertFalse(final.attempt!!.active)
        assertNotNull(final.attempt!!.acceptedGeometry)
        assertEquals(1, InventoryRepository(backend).page(session).buildings.size)
        val restarted = GameplayHttpBackend(sessions, MockGameplayTransport(disk) { disk = it })
        assertEquals(final, restarted.sync(session))
    }

    @Test fun rejectedRevisionAndFailedPersistenceDoNotAdvanceState() = runBlocking {
        val backend = MockGameplayTransport(null) { error("disk unavailable") }
        val stale = backend.request("attempts/start", "local-mock-only", GameplayWire.start("stale", 0).envelope)
        assertEquals(409, stale.status)
        try {
            backend.request("attempts/start", "local-mock-only", GameplayWire.start("new", 1).envelope)
            fail("Disk failure must be visible")
        } catch (expected: IllegalStateException) { assertEquals("disk unavailable", expected.message) }
        assertEquals(1L, RecoveryDtos.snapshot(backend.request("sync", "local-mock-only", null).body).revision)
        assertEquals(400, backend.request("market/buy", "local-mock-only", "{}").status)
    }
}
