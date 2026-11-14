package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.billing.BillingSession
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.Assert.*
import org.junit.Test

class RecoveryRetrySchedulerTest {
    private class Tick(val millis: Long, val release: CompletableDeferred<Unit> = CompletableDeferred())
    private class Fixture {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val sessions = AccountSessions { "token" }
        val pending = mutableMapOf<String, AttemptCommand>()
        val sent = mutableListOf<Pair<String, AttemptCommand>>()
        val receipts = mutableMapOf<String, AttemptCommand>()
        val timers = Channel<Tick>(Channel.UNLIMITED)
        var failure: Exception? = IOException("offline")
        var loseResponse = false
        var syncCalls = 0
        var syncGate: CompletableDeferred<Unit>? = null
        var snapshot = GameSnapshot("wobbrik-v1", 0, 0, 0, null)
        val journal = object : AttemptJournal {
            override fun pending(uid: String) = pending[uid]
            override fun save(uid: String, command: AttemptCommand) { check(pending.putIfAbsent(uid, command) == null) }
            override fun remove(uid: String, command: AttemptCommand) { check(pending.remove(uid, command)) }
        }
        val backend = object : AttemptBackend {
            override suspend fun sync(session: BillingSession): GameSnapshot {
                syncCalls++
                syncGate?.await()
                failure?.let { throw it }
                return snapshot
            }
            override suspend fun submit(session: BillingSession, command: AttemptCommand) {
                assertEquals(command, journal.pending(session.uid))
                sent += session.uid to command
                val old = receipts.putIfAbsent(command.operationId, command)
                if (old != null) assertEquals(old, command)
                else snapshot = snapshot.copy(revision = snapshot.revision + 1, attempt = ServerAttempt(command.operationId, true, 0, "{}"))
                if (loseResponse) { loseResponse = false; throw IOException("lost committed response") }
            }
        }
        val owner = AuthenticatedRecoveryOwner(sessions, backend, journal, scope)
        val scheduler = RecoveryRetryScheduler(owner, scope) { ms ->
            val tick = Tick(ms); timers.send(tick); tick.release.await()
        }
        suspend fun login(uid: String? = "alice") { owner.onAuthChanged(uid); settle() }
        suspend fun online() { scheduler.foreground(true); scheduler.connected(true); settle() }
        suspend fun tick() = withTimeout(2_000) { timers.receive() }
        fun close() { scheduler.close(); scope.cancel() }
    }

    @Test fun reconnectRetriesLostResponseThroughExactSoleJournalAndStopsOnReady() = runBlocking {
        val f = Fixture()
        try {
            f.failure = null; f.login()
            val ready = f.owner.state.value as AuthenticatedRecoveryOwner.State.Ready
            val command = GameplayWire.start("stable", ready.snapshot.revision)
            f.loseResponse = true
            assertTrue(f.owner.submit(ready) { command }); settle()
            assertEquals(command, f.pending["alice"])
            assertTrue(f.timers.tryReceive().isFailure)
            f.online(); val tick = f.tick(); assertEquals(1_000L, tick.millis)
            repeat(10) { f.scheduler.connected(true) }; settle()
            assertEquals(1, f.sent.size)
            tick.release.complete(Unit); settle()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Ready)
            assertEquals(listOf("alice" to command, "alice" to command), f.sent)
            assertEquals(1, f.receipts.size); assertNull(f.pending["alice"])
            assertTrue(f.timers.tryReceive().isFailure)
        } finally { f.close() }
    }

    @Test fun backoffIsCappedAndConnectivityCallbacksCannotCreateParallelRetries() = runBlocking {
        val f = Fixture()
        try {
            f.login(); f.online()
            for (ms in listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 32_000L, 60_000L, 60_000L)) {
                val tick = f.tick(); assertEquals(ms, tick.millis)
                repeat(10) { f.scheduler.connected(true); f.scheduler.foreground(true) }; settle()
                assertTrue(f.timers.tryReceive().isFailure)
                tick.release.complete(Unit); settle()
            }
            assertEquals(9, f.syncCalls)
        } finally { f.close() }
    }

    @Test fun backgroundAndOfflineCancelWaitingRetriesAndResumeOnlyWhenBothPermit() = runBlocking {
        val f = Fixture()
        try {
            f.login(); f.online(); val first = f.tick()
            f.scheduler.foreground(false); settle(); first.release.complete(Unit); settle()
            assertEquals(1, f.syncCalls)
            f.scheduler.connected(false); f.scheduler.foreground(true); settle()
            assertTrue(f.timers.tryReceive().isFailure)
            f.scheduler.connected(true); settle(); val second = f.tick()
            f.scheduler.connected(false); settle(); second.release.complete(Unit); settle()
            assertEquals(1, f.syncCalls)
            f.failure = null; f.scheduler.connected(true); settle()
            f.tick().release.complete(Unit); settle()
            assertEquals(2, f.syncCalls); assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Ready)
        } finally { f.close() }
    }

    @Test fun permanentErrorsAndUnresolvedResultsNeverAutoRetry() = runBlocking {
        for (failure in listOf(GameplayHttpError(400, "invalid_request"), GameplayHttpError(401, "denied"),
            GameplayHttpError(403, "blocked_account"), GameplayHttpError(404, "not_found"),
            GameplayHttpError(409, "attempt_conflict"), GameplayHttpError(422, "unsupported_rules_version"),
            IllegalStateException("Pending attempt unresolved"), UnreadableAttemptJournal(IOException("disk")))) {
            val f = Fixture()
            try {
                f.failure = failure; f.login(); f.online()
                repeat(3) { f.scheduler.connected(false); f.scheduler.connected(true); settle() }
                assertTrue(f.timers.tryReceive().isFailure); assertEquals(1, f.syncCalls)
                assertTrue(f.sent.isEmpty()); assertFalse(f.owner.retryTransient(f.owner.state.value as AuthenticatedRecoveryOwner.State.Error))
            } finally { f.close() }
        }
    }

    @Test fun retryableHttpStatusesAndRevisionConflictUseExistingRecovery() = runBlocking {
        for (failure in listOf(GameplayHttpError(408, null), GameplayHttpError(429, null),
            GameplayHttpError(500, null), GameplayHttpError(503, null), RevisionConflict())) {
            val f = Fixture()
            try {
                f.failure = failure; f.login(); f.online(); val tick = f.tick()
                f.failure = null; tick.release.complete(Unit); settle()
                assertEquals(2, f.syncCalls); assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Ready)
            } finally { f.close() }
        }
    }

    @Test fun logoutAndSameUidReloginInvalidateOldErrorAndTimer() = runBlocking {
        val f = Fixture()
        try {
            f.login(); f.online(); val first = f.tick()
            val oldError = f.owner.state.value as AuthenticatedRecoveryOwner.State.Error
            val oldSession = f.sessions.current()
            f.login(); assertNotEquals(oldSession, f.sessions.current())
            assertFalse(f.owner.retryTransient(oldError))
            first.release.complete(Unit); settle(); assertEquals(2, f.syncCalls)
            val second = f.tick()
            f.login(null); second.release.complete(Unit); settle()
            assertEquals(2, f.syncCalls); assertEquals(AuthenticatedRecoveryOwner.State.SignedOut, f.owner.state.value)
            assertTrue(f.timers.tryReceive().isFailure)
        } finally { f.close() }
    }

    @Test fun recoveringIsNeverReplacedAndActiveReadyCapabilityIsNeverRevoked() = runBlocking {
        val f = Fixture()
        try {
            f.login(); f.online(); val tick = f.tick()
            f.syncGate = CompletableDeferred(); f.failure = null
            f.snapshot = f.snapshot.copy(attempt = ServerAttempt("active", true, 0, "{}"))
            tick.release.complete(Unit); settle()
            assertEquals(AuthenticatedRecoveryOwner.State.Recovering, f.owner.state.value)
            repeat(3) { f.scheduler.connected(false); f.scheduler.connected(true); settle() }
            assertEquals(2, f.syncCalls); assertTrue(f.timers.tryReceive().isFailure)
            f.syncGate!!.complete(Unit); settle()
            val ready = f.owner.state.value as AuthenticatedRecoveryOwner.State.Ready
            repeat(3) { f.scheduler.connected(false); f.scheduler.connected(true); settle() }
            assertSame(ready, f.owner.state.value); assertEquals(2, f.syncCalls)
        } finally { f.close() }
    }

    companion object { private suspend fun settle() { repeat(20) { yield() } } }
}
