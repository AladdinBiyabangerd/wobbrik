package com.aladdin.wobbrik.engine

import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.game.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption.*

class AcceptedAttemptCommandsTest {
    /** Real serialized journal with atomic/fsynced JVM files; server is a deterministic contract fake. */
    private class Fixture : AutoCloseable {
        val root = Files.createTempDirectory("accepted-commands").toFile()
        val records = mutableMapOf<String, GameSnapshot>()
        val receipts = mutableMapOf<Pair<String, String>, AttemptCommand>()
        val sent = mutableListOf<Pair<String, AttemptCommand>>()
        var afterCommit: suspend () -> Unit = {}
        var beforeCommit: suspend () -> Unit = {}
        var syncFailure = false
        var saveFailure = false
        var nextId = 0
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val sessions = AccountSessions { "token" }
        val host = AcceptedEngineHost()
        fun journal() = DiskAttemptJournal { key -> object : JournalFile {
            val file = File(root, key)
            override fun read(): ByteArray? = if (file.exists()) file.readBytes() else null
            override fun replace(bytes: ByteArray) {
                check(!saveFailure) { "storage failed" }
                val temp = File(root, "$key.tmp")
                FileOutputStream(temp).use { it.write(bytes); it.fd.sync() }
                Files.move(temp.toPath(), file.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            }
        } }
        val backend = object : AttemptBackend {
            override suspend fun sync(session: BillingSession): GameSnapshot {
                check(!syncFailure) { "sync offline" }
                return records.getOrPut(session.uid) { GameSnapshot("wobbrik-v1", 0, 0, 0, null) }
            }
            override suspend fun submit(session: BillingSession, command: AttemptCommand) {
                assertEquals(command, journal().pending(session.uid))
                GameplayWire.validate(command)
                sent += session.uid to command
                beforeCommit()
                val key = session.uid to command.operationId
                val previous = receipts[key]
                if (previous == null) {
                    val old = records.getValue(session.uid)
                    if (old.revision != command.expectedRevision) throw RevisionConflict()
                    val request = Json.parseToJsonElement(command.envelope).jsonObject.getValue("request").jsonObject
                    fun number(k: String) = request.getValue(k).jsonPrimitive.long
                    fun string(k: String) = request.getValue(k).jsonPrimitive.content
                    val attempt = when (command.route) {
                        AttemptCommand.Route.START -> {
                            check(old.attempt?.active != true)
                            ServerAttempt(command.operationId, true, 0, null)
                        }
                        AttemptCommand.Route.CHECKPOINT -> {
                            val a = old.attempt!!
                            check(a.active && string("attempt_id") == a.id && number("sequence") == a.sequence + 1)
                            check(number("floors") > a.floors)
                            a.copy(sequence = number("sequence"), floors = number("floors"), precise = number("precise"),
                                rescues = number("rescues"), checkpointJson = request.getValue("snapshot").toString())
                        }
                        AttemptCommand.Route.FINISH -> {
                            check(old.attempt!!.sequence == number("sequence"))
                            null // /sync returns only active attempts.
                        }
                    }
                    val reward = if (command.route == AttemptCommand.Route.CHECKPOINT) attempt!!.floors - old.attempt!!.floors else 0
                    records[session.uid] = old.copy(revision = old.revision + 1, attempt = attempt,
                        coins = old.coins + reward, xp = old.xp + reward)
                    receipts[key] = command
                } else assertEquals(previous, command)
                afterCommit()
            }
        }
        var owner = newOwner()
        var commands = newCommands()
        private fun newOwner() = AuthenticatedRecoveryOwner(sessions, backend, journal(), scope, host::onRecoveryChanged)
        private fun newCommands() = AcceptedAttemptCommands(owner) { "op-${++nextId}" }
        fun ready() = owner.state.value as AuthenticatedRecoveryOwner.State.Ready
        fun login(uid: String = "alice") { owner.onAuthChanged(uid) }
        fun recreate() {
            scope.cancel()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
            owner = newOwner()
            commands = newCommands()
            login()
        }
        override fun close() { scope.cancel(); root.deleteRecursively() }
    }
    private val parts = setOf("wall", "window", "entrance", "roof", "lighting").associateWith { "base_6_$it" }
    private fun landed(terminal: Boolean = false): EngineCheckpoint = PracticeEngine().checkpoint().let {
        it.copy(state = it.state.copy(floors = listOf(Floor(0.0, 0.0)), perfectLandings = 1,
            phase = if (terminal) Phase.FINISHED else Phase.SWINGING))
    }
    private fun terminalZero() = PracticeEngine().checkpoint().let { it.copy(state = it.state.copy(phase = Phase.FINISHED)) }

    @Test fun onlineHostStopsAtLandingAndRequiresFreshExplicitResume() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            val old = f.ready()
            f.host.onForeground()
            assertFalse(f.host.resume(old.copy()))
            assertTrue(f.host.resume(old))
            f.host.onFrame(1L, old)
            f.host.pause()
            assertTrue(f.host.snapshot().foreground)
            assertFalse(f.host.snapshot().inputEnabled)
            assertNull(f.host.snapshot().previousFrameNanos)
            assertFalse(f.host.drop(old))
            assertTrue(f.host.resume(old))
            assertFalse(f.host.drop(old.copy()))
            assertTrue(f.host.drop(old))
            var time = 0L
            repeat(20) { time += 100_000_000L; f.host.onFrame(time, old) }
            yield()
            assertEquals(1L, f.ready().snapshot.attempt!!.floors)
            assertEquals(2, f.sent.size)
            assertFalse(f.host.snapshot().inputEnabled)
            assertFalse(f.host.resume(old))
            assertTrue(f.host.resume(f.ready()))
            val scene = f.host.snapshot().scene
            f.host.onFrame(Long.MAX_VALUE, old)
            assertEquals(scene, f.host.snapshot().scene)
            assertNull(f.host.snapshot().previousFrameNanos)
            assertFalse(f.host.drop(old))
            f.host.onPause()
            f.host.onFrame(Long.MAX_VALUE)
            assertFalse(f.host.drop())
            f.login("bob"); yield()
            assertNull(f.host.snapshot().scene)
        }
    }

    @Test fun terminalCheckpointRecoversFinishIntentWithoutLocalMemory() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            f.commands.checkpoint(f.ready(), landed(true)); yield()
            // No finish has been submitted; recreate from only server checkpoint + disk.
            f.recreate(); yield()
            val freshHost = AcceptedEngineHost()
            freshHost.bind(f.commands)
            freshHost.onRecoveryChanged(f.ready())
            assertFalse(freshHost.resume())
            assertTrue(freshHost.reconcileTerminal()); yield()
            assertFalse(freshHost.reconcileTerminal())
            assertNull(f.ready().snapshot.attempt)
            assertEquals(listOf(AttemptCommand.Route.START, AttemptCommand.Route.CHECKPOINT, AttemptCommand.Route.FINISH),
                f.sent.map { it.second.route })
            assertEquals(1L, f.ready().snapshot.coins)
        }
    }

    @Test fun completeFlowUsesAcceptedSequenceAndOnlyServerWalletWhileInputRemainsLocked() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            assertTrue(f.commands.start(f.ready())); yield()
            assertTrue(f.commands.checkpoint(f.ready(), landed(true))); yield()
            assertTrue(f.commands.finish(f.ready(), landed(true), parts)); yield()
            assertEquals(listOf(AttemptCommand.Route.START, AttemptCommand.Route.CHECKPOINT, AttemptCommand.Route.FINISH),
                f.sent.map { it.second.route })
            assertEquals(1L, f.ready().snapshot.coins)
            assertNull(f.ready().snapshot.attempt)
            assertEquals(3L, f.ready().snapshot.revision)
            assertFalse(f.host.snapshot().inputEnabled)
            assertFalse(f.host.resume()); assertFalse(f.host.drop())
        }
    }

    @Test fun lostCommittedResponseForEveryRouteRetriesExactDiskEnvelopeAfterRecreation() = runBlocking {
        for (route in AttemptCommand.Route.entries) Fixture().use { f ->
            f.login(); yield()
            if (route != AttemptCommand.Route.START) { f.commands.start(f.ready()); yield() }
            if (route == AttemptCommand.Route.FINISH) { f.commands.checkpoint(f.ready(), landed(true)); yield() }
            f.afterCommit = { error("lost committed response") }
            when (route) {
                AttemptCommand.Route.START -> f.commands.start(f.ready())
                AttemptCommand.Route.CHECKPOINT -> f.commands.checkpoint(f.ready(), landed(true))
                AttemptCommand.Route.FINISH -> f.commands.finish(f.ready(), landed(true), parts)
            }
            yield()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            assertEquals(AcceptedAttemptRestore.Result.Unavailable, f.host.snapshot().restore)
            val pending = f.journal().pending("alice")!!
            assertEquals(route, pending.route)
            val coins = f.records.getValue("alice").coins
            f.afterCommit = {}
            f.recreate(); yield()
            assertEquals(listOf(pending, pending), f.sent.takeLast(2).map { it.second })
            assertNull(f.journal().pending("alice"))
            assertEquals(coins, f.ready().snapshot.coins)
            assertFalse(f.host.snapshot().inputEnabled)
        }
    }

    @Test fun receiptWithoutSyncRetainsJournalAndNoNewCommandCanReplaceIt() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            val old = f.ready()
            f.afterCommit = { f.syncFailure = true }
            f.commands.start(old); yield()
            val pending = f.journal().pending("alice")!!
            assertFalse(f.commands.start(old))
            assertEquals(pending, f.journal().pending("alice"))
            f.afterCommit = {}; f.syncFailure = false
            f.owner.onForeground(); yield()
            assertEquals(pending, f.sent.last().second)
            assertEquals(pending.operationId, f.ready().snapshot.attempt!!.id)
        }
    }

    @Test fun exactReadyIdentityRejectsOldAccountAndSameUidEpochEvenWithEqualSnapshots() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            val old = f.ready()
            assertFalse(f.commands.start(old.copy()))
            f.login("bob"); yield()
            assertEquals(old, f.ready())
            assertFalse(f.commands.start(old))
            f.login(); yield()
            assertFalse(f.commands.start(old))
            assertTrue(f.sent.isEmpty())
        }
    }

    @Test fun duplicateDispatchAndAuthSwitchDuringDelayedCommitKeepOnlyOldAccountsJournal() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            val old = f.ready()
            val gate = CompletableDeferred<Unit>()
            f.afterCommit = { withContext(NonCancellable) { gate.await() } }
            assertTrue(f.commands.start(old)); yield()
            assertFalse(f.commands.start(old))
            val pending = f.journal().pending("alice")!!
            f.login("bob"); yield()
            gate.complete(Unit); yield()
            assertNull(f.ready().snapshot.attempt)
            assertEquals(pending, f.journal().pending("alice"))
            assertNull(f.journal().pending("bob"))
            f.afterCommit = {}
            f.login(); yield()
            assertEquals(pending, f.sent.last().second)
            assertEquals(pending.operationId, f.ready().snapshot.attempt!!.id)
        }
    }

    @Test fun revisionConflictDoesNotRebaseOrCreateNewOperation() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            f.records["alice"] = f.ready().snapshot.copy(revision = 5, coins = 90)
            f.commands.start(f.ready()); yield()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            assertNull(f.journal().pending("alice"))
            assertEquals(1, f.sent.size)
            f.owner.onForeground(); yield()
            assertEquals(5L, f.ready().snapshot.revision)
            assertEquals(90L, f.ready().snapshot.coins)
            assertEquals(1, f.sent.size)
        }
    }

    @Test fun failedDurableWriteSendsNothingAndInvalidatesHostUntilRecovery() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            f.saveFailure = true
            f.commands.start(f.ready()); yield()
            assertTrue(f.sent.isEmpty())
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            assertFalse(f.host.snapshot().inputEnabled)
            f.saveFailure = false
            f.owner.onForeground(); yield()
            assertNull(f.ready().snapshot.attempt)
        }
    }

    @Test fun terminalZeroFloorMissCanFinishButRestartAndUnacceptedProgressCannot() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            val ready = f.ready()
            assertThrows(IllegalStateException::class.java) { f.commands.start(ready) }
            assertThrows(IllegalArgumentException::class.java) { f.commands.checkpoint(ready, terminalZero()) }
            assertThrows(IllegalArgumentException::class.java) { f.commands.finish(ready, landed(true), parts) }
            assertThrows(IllegalArgumentException::class.java) { f.commands.finish(ready, PracticeEngine().checkpoint(), parts) }
            assertEquals(1, f.sent.size)
            assertTrue(f.commands.finish(ready, terminalZero(), parts)); yield()
            assertEquals(0L, f.ready().snapshot.coins)
            assertNull(f.ready().snapshot.attempt)
        }
    }

    @Test fun checkpointCannotRewriteAcceptedFloorsRegressCountersOrPersistAnUnsettledDrop() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            f.commands.checkpoint(f.ready(), landed()); yield()
            val ready = f.ready()
            val first = landed()
            val next = first.copy(state = first.state.copy(floors = first.state.floors + Floor(0.0, 30.0)))
            val rewritten = next.copy(state = next.state.copy(floors = listOf(Floor(10.0, 0.0), Floor(0.0, 30.0))))
            val regressed = next.copy(state = next.state.copy(perfectLandings = 0))
            val falling = PracticeEngine().also { it.restore(first); it.drop() }.checkpoint()
            for (invalid in listOf(first, rewritten, regressed, falling)) {
                assertThrows(IllegalArgumentException::class.java) { f.commands.checkpoint(ready, invalid) }
            }
            assertSame(ready, f.ready())
            assertEquals(2, f.sent.size)
            assertTrue(f.commands.checkpoint(ready, next)); yield()
            assertEquals(2L, f.ready().snapshot.attempt!!.sequence)
        }
    }

    @Test fun continuedAndUnsupportedAttemptsStayBlockedWithoutWrites() = runBlocking {
        for (attempt in listOf(ServerAttempt("a", true, 0, null, continued = true),
            ServerAttempt("a", true, 0, null, engineVersion = "future"))) Fixture().use { f ->
            f.records["alice"] = GameSnapshot("wobbrik-v1", 1, 0, 0, attempt)
            f.login(); yield()
            assertThrows(IllegalStateException::class.java) { f.commands.checkpoint(f.ready(), landed()) }
            assertThrows(IllegalStateException::class.java) { f.commands.finish(f.ready(), terminalZero(), parts) }
            assertTrue(f.sent.isEmpty())
        }
    }
}
