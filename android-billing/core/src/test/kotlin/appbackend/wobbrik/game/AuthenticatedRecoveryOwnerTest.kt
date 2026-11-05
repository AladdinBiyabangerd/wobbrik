package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.billing.BillingSession
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class AuthenticatedRecoveryOwnerTest {
    private class Fixture {
        val sessions = AccountSessions { "token" }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val pending = mutableMapOf<String, AttemptCommand>()
        val calls = mutableListOf<BillingSession>()
        var sync: suspend (BillingSession) -> GameSnapshot = { snapshot(it.uid) }
        var submit: suspend () -> Unit = {}
        val journal = object : AttemptJournal {
            override fun pending(uid: String) = pending[uid]
            override fun save(uid: String, command: AttemptCommand) { pending[uid] = command }
            override fun remove(uid: String, command: AttemptCommand) { check(pending.remove(uid, command)) }
        }
        val backend = object : AttemptBackend {
            override suspend fun sync(session: BillingSession): GameSnapshot {
                calls += session
                return sync(session)
            }
            override suspend fun submit(session: BillingSession, command: AttemptCommand) { submit() }
        }
        val owner = AuthenticatedRecoveryOwner(sessions, backend, journal, scope)
        fun ready() = (owner.state.value as AuthenticatedRecoveryOwner.State.Ready).snapshot
        fun close() = scope.cancel()
    }

    @Test fun delayedOldAccountCannotPublishAndForegroundIsSerialized() = runBlocking {
        val f = Fixture()
        try {
            val gate = CompletableDeferred<Unit>()
            f.sync = { session ->
                if (session.uid == "alice") withContext(NonCancellable) { gate.await() }
                snapshot(session.uid)
            }
            f.owner.onAuthChanged("alice")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            repeat(3) { f.owner.onForeground() }
            assertEquals(1, f.calls.size)
            f.owner.onAuthChanged("bob")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertEquals(AuthenticatedRecoveryOwner.State.Recovering, f.owner.state.value)
            gate.complete(Unit)
            yield()
            assertEquals(2L, f.ready().coins)
            assertEquals(listOf("alice", "bob"), f.calls.map { it.uid })
        } finally { f.close() }
    }

    @Test fun sameUidReloginUsesNewEpochAndClearsPublishedState() = runBlocking {
        val f = Fixture()
        try {
            f.owner.onAuthChanged("alice")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            val first = f.sessions.current()!!
            val gate = CompletableDeferred<Unit>()
            f.sync = { gate.await(); snapshot(it.uid) }
            f.owner.onAuthChanged("alice")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertNotEquals(first, f.sessions.current())
            assertEquals(AuthenticatedRecoveryOwner.State.Recovering, f.owner.state.value)
            f.owner.onAuthChanged("alice")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            gate.complete(Unit)
            yield()
            assertEquals(f.sessions.current(), f.calls.last())
            assertEquals(1L, f.ready().coins)
        } finally { f.close() }
    }

    @Test fun foregroundFailureHidesPreviousSnapshotAndRetriesWithoutNewOwner() = runBlocking {
        val f = Fixture()
        try {
            f.owner.onAuthChanged("alice")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertEquals(1L, f.ready().coins)
            f.sync = { error("offline") }
            // Recreated hosts retain this application owner and invoke foreground on it.
            val recreatedHostOwner = f.owner
            recreatedHostOwner.onForeground()
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            f.sync = { snapshot(it.uid) }
            recreatedHostOwner.onForeground()
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertSame(f.owner, recreatedHostOwner)
            assertEquals(1L, f.ready().coins)
            assertEquals(3, f.calls.size)
        } finally { f.close() }
    }

    @Test fun pendingFailureAndLogoutPreserveExactAccountCommand() = runBlocking {
        val f = Fixture()
        try {
            val command = AttemptCommand("retained", 7, AttemptCommand.Route.START, "exact-body")
            f.pending["alice"] = command
            f.submit = { error("lost response") }
            f.owner.onAuthChanged("alice")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            assertEquals(command, f.pending["alice"])
            f.owner.onAuthChanged(null)
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertEquals(AuthenticatedRecoveryOwner.State.SignedOut, f.owner.state.value)
            f.owner.onAuthChanged("bob")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertEquals(2L, f.ready().coins)
            assertEquals(command, f.pending["alice"])
            f.submit = {}
            f.owner.onAuthChanged("alice")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            assertEquals(1L, f.ready().coins)
            assertNull(f.pending["alice"])
        } finally { f.close() }
    }

    @Test fun cancelledReceiptCannotEraseJournalOrBecomeReady() = runBlocking {
        val f = Fixture()
        try {
            val command = AttemptCommand("retained", 7, AttemptCommand.Route.START, "exact-body")
            f.pending["alice"] = command
            val gate = CompletableDeferred<Unit>()
            f.submit = { withContext(NonCancellable) { gate.await() } }
            f.owner.onAuthChanged("alice")
            yield() // Allow the unconfined launch queued by runBlocking to run.
            f.owner.onAuthChanged(null)
            yield() // Allow the unconfined launch queued by runBlocking to run.
            gate.complete(Unit)
            yield()
            assertEquals(AuthenticatedRecoveryOwner.State.SignedOut, f.owner.state.value)
            assertEquals(command, f.pending["alice"])
            assertTrue(f.calls.isEmpty())
        } finally { f.close() }
    }

    companion object {
        private fun snapshot(uid: String) = GameSnapshot("wobbrik-v1", 0, if (uid == "alice") 1 else 2, 0, null)
    }
}
