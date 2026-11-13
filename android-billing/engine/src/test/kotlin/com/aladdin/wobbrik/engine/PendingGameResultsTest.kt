package com.aladdin.wobbrik.engine

import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class PendingGameResultsTest {
    private class Fixture {
        val files = mutableMapOf<String, ByteArray>()
        var diskFails = false
        val factory: (String) -> JournalFile = { key -> object : JournalFile {
            override fun read() = files[key]?.clone()
            override fun replace(bytes: ByteArray) { if (diskFails) throw IOException("disk"); files[key] = bytes.clone() }
        } }
        var results = PendingGameResults(factory)
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val journal = DiskAttemptJournal { factory("wire-$it") }
        var snapshot = GameSnapshot("wobbrik-v1", 0, 0, 0, null)
        val receipts = mutableMapOf<String, AttemptCommand>()
        val sent = mutableListOf<AttemptCommand>()
        var loseResponse = false
        var syncFails = false
        var afterSubmit: () -> Unit = {}
        val backend = object : AttemptBackend {
            override suspend fun sync(session: BillingSession): GameSnapshot {
                if (syncFails) throw IOException("sync")
                return snapshot
            }
            override suspend fun submit(session: BillingSession, command: AttemptCommand) {
                assertEquals(command, journal.pending(session.uid))
                sent += command
                val previous = receipts[command.operationId]
                if (previous != null) assertEquals(previous, command) else {
                    if (command.expectedRevision != snapshot.revision) throw RevisionConflict()
                    val r = Json.parseToJsonElement(command.envelope).jsonObject.getValue("request").jsonObject
                    fun n(k: String) = r.getValue(k).jsonPrimitive.long
                    val attempt = when (command.route) {
                        AttemptCommand.Route.START -> ServerAttempt(command.operationId, true, 0, "{}")
                        AttemptCommand.Route.CHECKPOINT -> requireNotNull(snapshot.attempt).copy(sequence = n("sequence"),
                            floors = n("floors"), precise = n("precise"), rescues = n("rescues"), checkpointJson = r.getValue("snapshot").toString())
                        AttemptCommand.Route.FINISH -> null
                        else -> error("unexpected")
                    }
                    snapshot = snapshot.copy(revision = snapshot.revision + 1, attempt = attempt)
                    receipts[command.operationId] = command
                }
                afterSubmit()
                if (loseResponse) { loseResponse = false; throw IOException("lost response") }
            }
        }
        fun coordinator() = AttemptCoordinator(sessions, backend, journal) { session, command -> results.committed(session, command) }
        suspend fun replay(): GameSnapshot {
            val c = coordinator()
            val session = requireNotNull(sessions.current())
            return PendingResultReconciler(results).reconcile(session, c, c.recover(session))
        }
    }
    private fun terminal(floors: Int = 0): EngineCheckpoint {
        val cp = PracticeEngine().checkpoint()
        return cp.copy(tickUnits = 0, state = cp.state.copy(phase = Phase.FINISHED, paused = true,
            floors = List(floors) { Floor(0.0, it * PracticeRulesV1.FLOOR_HEIGHT) }))
    }
    private val parts = FinishCosmetics.categories.associateWith { "base_1_$it" }
    private fun result(id: String, floors: Int = 0) = PendingGameResult(id, terminal(floors), parts)

    @Test fun multipleResultsSurviveReopenAndFinishOnceThroughSoleJournal() = runBlocking {
        val f = Fixture()
        f.results.enqueue("alice", result("first", 2)); f.results.enqueue("alice", result("second"))
        f.results = PendingGameResults(f.factory)
        f.replay()
        assertTrue(f.results.pending("alice").isEmpty())
        assertEquals(listOf(AttemptCommand.Route.START, AttemptCommand.Route.CHECKPOINT, AttemptCommand.Route.FINISH,
            AttemptCommand.Route.START, AttemptCommand.Route.FINISH), f.sent.map { it.route })
        assertEquals(5, f.receipts.size)
        f.replay(); assertEquals(5, f.sent.size)
    }
    @Test fun realLocalPlayQueuesThenReconcilesExactlyOnceWithoutStartingOnPlay() = runBlocking {
        val f = Fixture()
        val session = requireNotNull(f.sessions.current())
        val catalog = RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", FinishCosmetics.categories.map {
            CatalogItem("base_1_$it", it, it, "free", 0, null, null)
        }, emptyList())
        val local = LocalGameHost(f.results, id = { "real-local" })
        local.bind(session); local.foreground(true)
        assertTrue(local.start(session, ReadOnlyAccount("alice", f.snapshot, catalog)))
        assertTrue(f.sent.isEmpty()); assertNull(f.journal.pending("alice"))
        assertTrue(local.resume())
        var now = 0L
        repeat(90) { now += 16_666_667; local.frame(now) }
        assertTrue(local.drop())
        repeat(80) { now += 16_666_667; local.frame(now) }
        assertFalse(local.snapshot().playing)
        assertEquals(1, PendingGameResults(f.factory).pending("alice").size)
        f.replay(); f.replay()
        assertEquals(listOf(AttemptCommand.Route.START, AttemptCommand.Route.FINISH), f.sent.map { it.route })
        assertEquals(2, f.receipts.size); assertTrue(f.results.pending("alice").isEmpty())
    }

    @Test fun lostStartResponseReplaysOriginalEnvelopeAfterRestart() = runBlocking {
        val f = Fixture(); f.results.enqueue("alice", result("first")); f.loseResponse = true
        try { f.replay(); fail() } catch (_: IOException) { }
        val original = requireNotNull(f.journal.pending("alice"))
        f.results = PendingGameResults(f.factory); f.replay()
        assertEquals(original, f.sent[1]); assertTrue(f.results.pending("alice").isEmpty())
        assertEquals(2, f.receipts.size)
    }
    @Test fun lostFinishResponseDoesNotStartAgain() = runBlocking {
        val f = Fixture(); f.results.enqueue("alice", result("first"))
        f.afterSubmit = { if (f.sent.last().route == AttemptCommand.Route.FINISH) f.loseResponse = true }
        try { f.replay(); fail() } catch (_: IOException) { }
        assertEquals(1, f.results.pending("alice").size)
        f.afterSubmit = {}; f.results = PendingGameResults(f.factory); f.replay()
        assertEquals(1, f.sent.count { it.route == AttemptCommand.Route.START })
        assertEquals(f.sent[1], f.sent[2]); assertTrue(f.results.pending("alice").isEmpty())
    }
    @Test fun finishAcknowledgementStorageFailureRetainsExactJournalForRetry() = runBlocking {
        val f = Fixture(); f.results.enqueue("alice", result("first"))
        f.afterSubmit = { if (f.sent.last().route == AttemptCommand.Route.FINISH) f.diskFails = true }
        try { f.replay(); fail() } catch (_: IOException) { }
        assertEquals(AttemptCommand.Route.FINISH, f.journal.pending("alice")?.route)
        assertEquals(1, f.results.pending("alice").size)
        f.diskFails = false; f.afterSubmit = {}; f.replay()
        assertTrue(f.results.pending("alice").isEmpty()); assertEquals(2, f.receipts.size)
    }
    @Test fun finishAcknowledgementBeforeSyncFailureCannotResurrectResult() = runBlocking {
        val f = Fixture(); f.results.enqueue("alice", result("first"))
        f.afterSubmit = { if (f.sent.last().route == AttemptCommand.Route.FINISH) f.syncFails = true }
        try { f.replay(); fail() } catch (_: IOException) { }
        assertTrue(f.results.pending("alice").isEmpty())
        assertNotNull(f.journal.pending("alice"))
        f.syncFails = false; f.afterSubmit = {}; f.replay(); assertEquals(2, f.receipts.size)
    }
    @Test fun otherDeviceAttemptIsNeverAbandonedOrOverwritten() = runBlocking {
        val f = Fixture(); f.results.enqueue("alice", result("first"))
        f.snapshot = f.snapshot.copy(attempt = ServerAttempt("other-device", true, 0, "{}"))
        try { f.replay(); fail() } catch (_: IllegalStateException) { }
        assertTrue(f.sent.isEmpty()); assertEquals(1, f.results.pending("alice").size)
    }
    @Test fun missingStartedAttemptIsKeptAndNeverRestarted() = runBlocking {
        val f = Fixture(); f.results.enqueue("alice", result("first").copy(started = true))
        try { f.replay(); fail() } catch (_: IllegalStateException) { }
        assertTrue(f.sent.isEmpty()); assertEquals(1, f.results.pending("alice").size)
    }
    @Test fun accountChangeAfterServerCommitCannotAcknowledgeAnotherEpoch() = runBlocking {
        val f = Fixture(); f.results.enqueue("alice", result("first"))
        f.afterSubmit = { f.sessions.onAuthChanged("bob") }
        try { f.replay(); fail() } catch (_: AccountChanged) { }
        assertEquals(1, f.results.pending("alice").size); assertTrue(f.results.pending("bob").isEmpty())
        assertNotNull(f.journal.pending("alice")); assertNull(f.journal.pending("bob"))
    }
    @Test fun failedEnqueueAndCorruptStorageNeverDiscardExistingResults() {
        val f = Fixture(); f.results.enqueue("alice", result("first")); f.diskFails = true
        try { f.results.enqueue("alice", result("second")); fail() } catch (_: IOException) { }
        assertEquals(listOf("first"), f.results.pending("alice").map { it.id })
        val key = f.files.keys.single(); f.files[key] = "corrupt".toByteArray()
        try { f.results.pending("alice"); fail() } catch (_: IllegalArgumentException) { }
        assertEquals("corrupt", f.files.getValue(key).toString(Charsets.UTF_8))
    }

    @Test fun acceptedAttemptLocalProgressSurvivesRestartWithoutAnotherStart() = runBlocking {
        val f = Fixture()
        f.snapshot = f.snapshot.copy(attempt = ServerAttempt("accepted", true, 0, "{}"))
        f.results.enqueue("alice", result("first", 3).copy(attemptId = "accepted", started = true))
        f.results = PendingGameResults(f.factory); f.replay()
        assertEquals(listOf(AttemptCommand.Route.CHECKPOINT, AttemptCommand.Route.FINISH), f.sent.map { it.route })
        assertTrue(f.results.pending("alice").isEmpty())
    }

    @Test fun definitiveRevisionConflictRetainsIntentAndAllowsFreshRevisionRetry() = runBlocking {
        val f = Fixture(); f.results.enqueue("alice", result("first"))
        val session = requireNotNull(f.sessions.current()); val c = f.coordinator()
        val stale = c.recover(session)
        f.snapshot = f.snapshot.copy(revision = 1)
        try { PendingResultReconciler(f.results).reconcile(session, c, stale); fail() } catch (_: RevisionConflict) { }
        assertNull(f.journal.pending("alice")); assertEquals(1, f.results.pending("alice").size)
        f.replay()
        assertEquals(0L, f.sent[0].expectedRevision); assertEquals(1L, f.sent[1].expectedRevision)
        assertTrue(f.results.pending("alice").isEmpty()); assertEquals(2, f.receipts.size)
    }

    @Test fun storageCapacityNeverEvictsAPendingResult() {
        val f = Fixture(); var count = 0
        try {
            repeat(1000) { f.results.enqueue("alice", result("result-$it")); count++ }
            fail("Expected bounded storage")
        } catch (_: IllegalArgumentException) { }
        assertTrue(count > 1); assertEquals(count, f.results.pending("alice").size)
        assertEquals("result-0", f.results.pending("alice").first().id)
    }
}
