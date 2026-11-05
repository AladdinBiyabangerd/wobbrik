package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.billing.Sessions
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AttemptCoordinatorTest {
    private class Fixture {
        var session: BillingSession? = BillingSession("alice", "login-1")
        val commands = mutableMapOf<String, AttemptCommand>()
        val sent = mutableListOf<AttemptCommand>()
        var value = GameSnapshot("wobbrik-v1", 0, 0, 0, null)
        var submit: suspend () -> Unit = {}
        var sync: suspend () -> Unit = {}
        val sessions = object : Sessions {
            override fun current() = session
            override suspend fun idToken(session: BillingSession) = "unused"
        }
        val journal = object : AttemptJournal {
            override fun pending(uid: String) = commands[uid]
            override fun save(uid: String, command: AttemptCommand) { check(commands.putIfAbsent(uid, command) == null) }
            override fun remove(uid: String, command: AttemptCommand) { check(commands.remove(uid, command)) }
        }
        val backend = object : AttemptBackend {
            override suspend fun sync(session: BillingSession): GameSnapshot { sync(); return value }
            override suspend fun submit(session: BillingSession, command: AttemptCommand) {
                assertEquals(command, commands[session.uid]) // Durable write precedes request.
                sent += command
                submit()
            }
        }
        fun coordinator() = AttemptCoordinator(sessions, backend, journal)
        fun command() = AttemptCommand("op-1", 0, AttemptCommand.Route.START,
            """{"operation_id":"op-1","expected_revision":0,"rules_version":"wobbrik-v1","request":{"engine_version":"wobbrik-structural-v1"}}""")
    }

    @Test fun lostResponseSurvivesCoordinatorRecreationAndRetriesExactCommand() = runBlocking {
        val f = Fixture()
        val first = f.coordinator()
        first.recover()
        f.submit = { f.value = f.value.copy(revision = 1, coins = 5); error("lost response") }
        try { first.execute(f.command()); fail() } catch (_: IllegalStateException) { }
        assertFalse(first.ready())
        assertNull(first.snapshot())
        f.submit = {}
        val restored = f.coordinator()
        assertEquals(5L, restored.recover().coins)
        assertEquals(listOf(f.command(), f.command()), f.sent)
        assertTrue(restored.ready())
        assertTrue(f.commands.isEmpty())
    }

    @Test fun lostSyncKeepsPendingEvenAfterSuccessfulReceipt() = runBlocking {
        val f = Fixture()
        val c = f.coordinator()
        c.recover()
        f.sync = { error("offline") }
        try { c.execute(f.command()); fail() } catch (_: IllegalStateException) { }
        assertEquals(f.command(), f.commands["alice"])
        assertFalse(c.ready())
        f.sync = {}
        c.recover()
        assertTrue(c.ready())
    }

    @Test fun sameUidNewLoginCannotApplyOldResponseOrRemovePending() = runBlocking {
        val f = Fixture()
        val c = f.coordinator()
        c.recover()
        f.submit = { f.session = BillingSession("alice", "login-2") }
        try { c.execute(f.command()); fail() } catch (_: AccountChanged) { }
        assertNull(c.snapshot())
        assertFalse(c.ready())
        assertEquals(f.command(), f.commands["alice"])
        f.session = BillingSession("bob", "login-3")
        c.recover()
        assertEquals(1, f.sent.size) // Bob never drains Alice's command.
    }

    @Test fun revisionConflictSyncsWithoutRebasingCommand() = runBlocking {
        val f = Fixture()
        val c = f.coordinator()
        c.recover()
        f.submit = { f.value = f.value.copy(revision = 4); throw RevisionConflict() }
        try { c.execute(f.command()); fail() } catch (_: RevisionConflict) { }
        assertTrue(f.commands.isEmpty())
        assertEquals(4L, c.snapshot()!!.revision)
        assertEquals(1, f.sent.size)
    }

    @Test fun equalRevisionRefreshReplacesWalletAndUnsupportedRulesBlockInput() = runBlocking {
        val f = Fixture()
        val c = f.coordinator()
        c.recover()
        f.value = f.value.copy(coins = 100)
        assertEquals(100L, c.recover().coins)
        f.value = f.value.copy(rulesVersion = "future")
        try { c.recover(); fail() } catch (_: IllegalStateException) { }
        assertNull(c.snapshot())
        assertFalse(c.ready())
    }
}
