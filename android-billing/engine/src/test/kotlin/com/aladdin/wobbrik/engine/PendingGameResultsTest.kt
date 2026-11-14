package com.aladdin.wobbrik.engine

import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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
        var historical: GameSnapshot? = null
        var historicalReceipt: AttemptReceipt? = null
        var afterLookup: () -> Unit = {}
        var loseResponse = false
        var syncFails = false
        var afterSync: () -> Unit = {}
        var afterSubmit: () -> Unit = {}
        val backend = object : AttemptBackend {
            override suspend fun attempt(session: BillingSession, id: String): GameSnapshot? {
                afterLookup(); return historical
            }
            override suspend fun receipt(session: BillingSession, id: String, route: AttemptCommand.Route): AttemptReceipt? {
                assertEquals("first-finish", id); assertEquals(AttemptCommand.Route.FINISH, route)
                return historicalReceipt
            }
            override suspend fun sync(session: BillingSession): GameSnapshot {
                if (syncFails) throw IOException("sync")
                afterSync()
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

    private fun historicalFinish(f: Fixture) {
        f.results.enqueue("alice", result("first", 2).copy(started = true))
        f.historical = f.snapshot.copy(revision = 3, attempt = ServerAttempt("first", false, 1,
            EngineCheckpointCodec.encode(terminal(2)), 2))
        f.historicalReceipt = AttemptReceipt(3, "2026-11-14T00:00:00Z",
            AttemptResult.Finished("first", "result", null, 2, 0, 0, 0, 0))
        f.snapshot = f.snapshot.copy(revision = 7, coins = 99,
            attempt = ServerAttempt("other-device", true, 0, "{}"))
    }
    @Test fun historicalFinishClosesConfirmedIntentAndPublishesFreshAccount() = runBlocking {
        val f = Fixture(); historicalFinish(f)
        val latest = f.replay()
        assertEquals(99L, latest.coins); assertEquals(7L, latest.revision)
        assertEquals("other-device", latest.attempt?.id)
        assertTrue(f.results.pending("alice").isEmpty()); assertTrue(f.sent.isEmpty())
        f.replay(); assertTrue(f.sent.isEmpty())
    }
    @Test fun finishedWithoutOurReceiptAndAbandonedAndUnknownRemainPending() = runBlocking {
        for (status in listOf("unknown", "abandoned", "finished")) {
            val f = Fixture(); historicalFinish(f)
            f.historical = when (status) {
                "unknown" -> null
                "abandoned" -> f.historical!!.copy(attempt = f.historical!!.attempt!!.copy(abandoned = true))
                else -> f.historical
            }
            f.historicalReceipt = null
            try { f.replay(); fail(status) } catch (_: IllegalStateException) { }
            assertEquals(1, PendingGameResults(f.factory).pending("alice").size)
            assertTrue(f.sent.isEmpty()); assertNull(f.journal.pending("alice"))
        }
    }
    @Test fun mismatchedReceiptNeverAcknowledgesLocalResult() = runBlocking {
        val f = Fixture(); historicalFinish(f)
        f.historicalReceipt = f.historicalReceipt!!.copy(result = AttemptResult.Finished("wrong", null, null, 2, 0, 0, 0, 0))
        try { f.replay(); fail() } catch (_: IllegalArgumentException) { }
        assertEquals(1, f.results.pending("alice").size); assertTrue(f.sent.isEmpty())
    }
    @Test fun historicalSyncFailureRetainsIntentUntilFreshAccountIsRead() = runBlocking {
        val f = Fixture(); historicalFinish(f)
        val c = f.coordinator(); val session = f.sessions.current()!!; val initial = c.recover(session)
        f.syncFails = true
        try { PendingResultReconciler(f.results).reconcile(session, c, initial); fail() } catch (_: IOException) { }
        assertFalse(c.ready()); assertEquals(1, f.results.pending("alice").size)
        f.syncFails = false; f.replay(); assertTrue(f.results.pending("alice").isEmpty())
    }
    @Test fun historicalAcknowledgementDiskFailureCanRetryAfterReopen() = runBlocking {
        val f = Fixture(); historicalFinish(f); f.diskFails = true
        try { f.replay(); fail() } catch (_: IOException) { }
        assertEquals(1, f.results.pending("alice").size)
        f.diskFails = false; f.results = PendingGameResults(f.factory); f.replay()
        assertTrue(f.results.pending("alice").isEmpty()); assertTrue(f.sent.isEmpty())
    }
    @Test fun sameUidNewEpochDuringHistoricalLookupCannotAcknowledge() = runBlocking {
        val f = Fixture(); historicalFinish(f)
        f.afterLookup = { f.sessions.onAuthChanged("alice") }
        try { f.replay(); fail() } catch (_: AccountChanged) { }
        assertEquals(1, f.results.pending("alice").size); assertTrue(f.sent.isEmpty())
    }
    @Test fun staleSyncOrChangedEpochAfterReceiptCannotAcknowledge() = runBlocking {
        for (changeEpoch in listOf(false, true)) {
            val f = Fixture(); historicalFinish(f)
            val c = f.coordinator(); val session = f.sessions.current()!!; val initial = c.recover(session)
            if (changeEpoch) f.afterSync = { f.sessions.onAuthChanged("alice") }
            else f.snapshot = f.snapshot.copy(revision = 2)
            try { PendingResultReconciler(f.results).reconcile(session, c, initial); fail() }
            catch (e: Exception) {
                if (changeEpoch) assertTrue(e is AccountChanged) else assertTrue(e is IllegalArgumentException)
            }
            assertEquals(1, f.results.pending("alice").size); assertFalse(c.ready()); assertTrue(f.sent.isEmpty())
        }
    }
    @Test fun historicalFloorMismatchRetainsExactPendingBytes() = runBlocking {
        val f = Fixture(); historicalFinish(f)
        val before = f.files.mapValues { it.value.toList() }
        f.historical = f.historical!!.copy(attempt = f.historical!!.attempt!!.copy(floors = 1))
        try { f.replay(); fail() } catch (_: IllegalArgumentException) { }
        assertEquals(before, f.files.mapValues { it.value.toList() }); assertTrue(f.sent.isEmpty())
    }

    @Test fun queuedOwnActiveAttemptDrainsBeforeOlderUnstartedResult() = runBlocking {
        val f = Fixture()
        f.results.enqueue("alice", result("older")); f.results.enqueue("alice", result("active").copy(started = true))
        f.snapshot = f.snapshot.copy(attempt = ServerAttempt("active", true, 0, "{}"))
        f.replay()
        assertEquals(listOf("active-finish", "older", "older-finish"), f.sent.map { it.operationId })
        assertTrue(f.results.pending("alice").isEmpty())
    }

    @Test fun connectivityRetryDrainsDurableTerminalThroughExistingOwnerAndSoleJournal() = runBlocking {
        val f = Fixture()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val ticks = Channel<CompletableDeferred<Unit>>(Channel.UNLIMITED)
        val owner = AuthenticatedRecoveryOwner(f.sessions, f.backend, f.journal, scope,
            onCommitted = f.results::committed,
            reconcile = { session, coordinator, snapshot -> PendingResultReconciler(f.results).reconcile(session, coordinator, snapshot) })
        val scheduler = RecoveryRetryScheduler(owner, scope) {
            val tick = CompletableDeferred<Unit>(); ticks.send(tick); tick.await()
        }
        try {
            f.results.enqueue("alice", result("first", 2)); f.loseResponse = true
            owner.onAuthChanged("alice"); repeat(20) { yield() }
            assertTrue(owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            val exact = requireNotNull(f.journal.pending("alice"))
            assertEquals(1, f.results.pending("alice").size)
            scheduler.foreground(true); scheduler.connected(true)
            withTimeout(2_000) { ticks.receive() }.complete(Unit)
            repeat(30) { yield() }
            assertTrue(owner.state.value is AuthenticatedRecoveryOwner.State.Ready)
            assertEquals(exact, f.sent[1]); assertTrue(f.results.pending("alice").isEmpty())
            assertNull(f.journal.pending("alice")); assertEquals(3, f.receipts.size)
            assertEquals(listOf(AttemptCommand.Route.START, AttemptCommand.Route.START,
                AttemptCommand.Route.CHECKPOINT, AttemptCommand.Route.FINISH), f.sent.map { it.route })
            assertTrue(ticks.tryReceive().isFailure)
        } finally { scheduler.close(); scope.cancel() }
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
