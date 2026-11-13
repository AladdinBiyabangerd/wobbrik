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
        var draftFailure = false
        val drafts = mutableMapOf<Pair<String, String>, FinishDraft>()
        val store = object : FinishDraftStore {
            override fun read(uid: String, attemptId: String) = drafts[uid to attemptId]
            override fun write(uid: String, attemptId: String, draft: FinishDraft) { check(!draftFailure); drafts[uid to attemptId] = draft }
        }
        val savedDefaults = mutableMapOf<String, Map<String, String>>()
        val captured = mutableMapOf<Pair<String, String>, Map<String, String>>()
        var defaultsFailure = false
        val defaults = object : CosmeticDefaultsStore {
            override fun read(uid: String) = savedDefaults[uid].orEmpty()
            override fun write(uid: String, parts: Map<String, String>) { savedDefaults[uid] = parts.toMap() }
            override fun attempt(uid: String, attemptId: String) = captured[uid to attemptId]
            override fun capture(uid: String, attemptId: String, parts: Map<String, String>) {
                check(!defaultsFailure)
                captured[uid to attemptId] = parts.toMap()
            }
        }
        val host = AcceptedEngineHost(store, defaults)
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
            override suspend fun catalog(session: BillingSession) = RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1",
                FinishCosmetics.categories.flatMap { category -> listOf(
                    CatalogItem("base_6_$category", category, category, "free", 0, null, null),
                    CatalogItem("paid_$category", category, category, "paid", 0, null, null),
                    CatalogItem("base_1_$category", category, category, "free", 0, null, null)) }, emptyList())
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
                        AttemptCommand.Route.SHOP_BUY, AttemptCommand.Route.BUILDING_EDIT, AttemptCommand.Route.BUILDING_DELETE -> error("Not an attempt command")
                        AttemptCommand.Route.START -> {
                            check(old.attempt?.active != true)
                            ServerAttempt(command.operationId, true, 0, null, geometryVersion = request["geometry_version"]?.jsonPrimitive?.content)
                        }
                        AttemptCommand.Route.CHECKPOINT -> {
                            val a = old.attempt!!
                            check(a.active && string("attempt_id") == a.id && number("sequence") == a.sequence + 1)
                            check(number("floors") > a.floors)
                            a.copy(sequence = number("sequence"), floors = number("floors"), precise = number("precise"),
                                rescues = number("rescues"), checkpointJson = request.getValue("snapshot").toString(),
                                acceptedGeometry = request["geometry"]?.let { AcceptedCityGeometry(CityFloorGeometry.parse(it.jsonObject),
                                    a.id, number("sequence"), command.operationId, a.rulesVersion, a.engineVersion) })
                        }
                        AttemptCommand.Route.FINISH -> {
                            check(old.attempt!!.sequence == number("sequence"))
                            null // /sync returns only active attempts.
                        }
                        AttemptCommand.Route.ABANDON -> {
                            val a = old.attempt!!
                            check(a.active && string("attempt_id") == a.id && a.sequence == number("sequence"))
                            null // Abandoned: no result/building; /sync returns only active attempts.
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
    @Test fun defaultAppearanceIsAccountScopedFrozenAcrossRecoveryAndUsedByNextAttempt() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.defaults.write("alice", parts + ("wall" to "paid_wall")) // locked -> free fallback
            f.login(); yield(); f.commands.start(f.ready()); yield()
            assertEquals(parts, f.host.snapshot().finishParts)
            val first = f.ready()
            f.defaults.write("alice", parts + ("wall" to "base_1_wall"))
            val fresh = AcceptedEngineHost(f.store, f.defaults)
            fresh.onRecoveryChanged(first)
            assertEquals(parts, fresh.snapshot().finishParts)
            f.commands.abandon(first); yield()
            f.commands.start(f.ready()); yield()
            assertEquals("base_1_wall", f.host.snapshot().finishParts["wall"])
            f.login("bob"); yield(); f.commands.start(f.ready()); yield()
            assertEquals(parts, f.host.snapshot().finishParts)
            assertEquals(3, f.captured.size)
            f.host.onFrame(1, first) // stale account capability cannot send anything
            assertEquals(3, routes(f).count { it == AttemptCommand.Route.START })
            assertFalse(routes(f).contains(AttemptCommand.Route.FINISH))
        }
    }

    @Test fun terminalAutoFinishRetriesOnNewReadyAndNeverDuplicatesSameCapability() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield(); f.commands.start(f.ready()); yield()
            f.commands.checkpoint(f.ready(), landed(true)); yield()
            val old = f.ready()
            f.syncFailure = true
            f.host.onFrame(1, old); yield()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Error)
            f.host.onFrame(2, old)
            f.syncFailure = false
            f.owner.onForeground(); yield()
            f.host.onFrame(3, f.ready()); yield()
            assertNull(f.ready().snapshot.attempt)
            assertEquals(1, f.sent.map { it.second }.filter { it.route == AttemptCommand.Route.FINISH }.distinct().size)
        }
    }

    @Test fun failedAppearanceCaptureBlocksGameplayUntilDurableRecovery() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield()
            f.defaultsFailure = true
            f.commands.start(f.ready()); yield()
            f.host.onForeground()
            assertTrue(f.host.snapshot().finishStorageError)
            assertFalse(f.host.resume(f.ready()))
            f.defaultsFailure = false
            f.host.onRecoveryChanged(f.ready())
            assertTrue(f.host.resume(f.ready()))
            assertEquals(parts, f.host.snapshot().finishParts)
        }
    }

    private val parts = setOf("wall", "window", "entrance", "roof", "lighting").associateWith { "base_6_$it" }
    private fun landed(terminal: Boolean = false): EngineCheckpoint = PracticeEngine().checkpoint().let {
        it.copy(state = it.state.copy(floors = listOf(Floor(0.0, 0.0)), perfectLandings = 1,
            phase = if (terminal) Phase.FINISHED else Phase.SWINGING))
    }
    private fun terminalZero() = PracticeEngine().checkpoint().let { it.copy(state = it.state.copy(phase = Phase.FINISHED)) }

    @Test fun newAttemptOptsInAndSendsTypedCheckpointThroughDurableJournal() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            assertTrue(f.commands.start(f.ready())); yield()
            assertEquals(CityFloorGeometry.VERSION, f.ready().snapshot.attempt!!.geometryVersion)
            assertTrue(f.commands.checkpoint(f.ready(), landed())); yield()
            val request = Json.parseToJsonElement(f.sent.last().second.envelope).jsonObject.getValue("request").jsonObject
            assertEquals(listOf(0L), CityFloorGeometry.parse(request.getValue("geometry").jsonObject).centers)
        }
    }

    @Test fun onlineHostKeepsSuccessfulLandingsLocalAndRevokesStaleAccountInput() = runBlocking {
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
            assertSame(old, f.ready())
            assertEquals(0L, f.ready().snapshot.attempt!!.floors)
            assertEquals(1, f.host.snapshot().scene!!.floors.size)
            assertEquals(1, f.sent.size) // start only: no landing checkpoint or sync
            assertTrue(f.host.snapshot().inputEnabled)
            assertFalse(f.host.snapshot().waiting)
            val scene = f.host.snapshot().scene
            f.host.onFrame(Long.MAX_VALUE, old.copy())
            assertEquals(scene, f.host.snapshot().scene)
            assertFalse(f.host.drop(old.copy()))
            f.host.onPause()
            f.host.onFrame(Long.MAX_VALUE)
            assertFalse(f.host.drop())
            f.login("bob"); yield()
            assertNull(f.host.snapshot().scene)
        }
    }

    @Test fun localRunSendsOneAccumulatedTerminalCheckpointThenFinishes() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield(); f.commands.start(f.ready()); yield()
            val initial = f.ready()
            f.host.onForeground()
            assertTrue(f.host.resume(initial))
            var time = 1L
            f.host.onFrame(time, initial)
            // Drop near the centre for several floors, then wait for an edge miss.
            var frames = 0
            while (!f.host.snapshot().choosingFinish && frames++ < 20000) {
                val scene = f.host.snapshot().scene!!
                if (scene.phase == Phase.SWINGING) {
                    val centre = scene.floors.lastOrNull()?.centerX ?: 0.0
                    val offset = kotlin.math.abs(f.host.snapshot().cameraX + scene.craneX - centre)
                    if ((scene.floors.size >= 3 && offset >= PracticeRulesV1.FLOOR_WIDTH) ||
                        (scene.floors.size < 3 && offset < 3.0)) {
                        f.host.drop(initial)
                    }
                }
                time += 16_666_666L
                f.host.onFrame(time, initial)
                if (!f.host.snapshot().choosingFinish) {
                    assertSame(initial, f.ready())
                    assertEquals(listOf(AttemptCommand.Route.START), routes(f))
                    assertFalse(f.host.snapshot().waiting)
                }
            }
            yield()
            assertTrue("Run must reach loss", f.host.snapshot().choosingFinish)
            val terminal = f.host.snapshot().scene!!
            assertTrue(terminal.floors.size >= 3)
            assertEquals(listOf(AttemptCommand.Route.START, AttemptCommand.Route.CHECKPOINT), routes(f))
            assertEquals(1L, f.ready().snapshot.attempt!!.sequence)
            assertEquals(terminal.floors.size.toLong(), f.ready().snapshot.attempt!!.floors)
            assertFalse(f.host.snapshot().inputEnabled)
            f.host.onFrame(time + 16_666_666L, f.ready()); yield()
            assertEquals(AttemptCommand.Route.FINISH, routes(f).last())
            assertNull(f.ready().snapshot.attempt)
        }
    }

    @Test fun hostZeroFloorLossAutomaticallyFinishesWithoutCheckpoint() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield(); f.commands.start(f.ready()); yield()
            val ready = f.ready()
            f.host.onForeground(); assertTrue(f.host.resume(ready))
            var time = 1L
            f.host.onFrame(time, ready)
            repeat(600) {
                val scene = f.host.snapshot().scene
                if (scene?.phase == Phase.SWINGING && kotlin.math.abs(scene.craneX) >= PracticeRulesV1.FLOOR_WIDTH) {
                    f.host.drop(ready)
                }
                time += 16_666_666L
                f.host.onFrame(time, ready)
            }
            yield()
            assertNull(f.ready().snapshot.attempt)
            assertEquals(listOf(AttemptCommand.Route.START, AttemptCommand.Route.FINISH), routes(f))
            assertEquals(0L, f.ready().snapshot.coins)
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
            assertTrue(freshHost.snapshot().choosingFinish)
            assertEquals(2, f.sent.size) // Dispatch waits for the exact Ready frame.
            freshHost.onFrame(1L, f.ready()); yield()
            assertFalse(freshHost.confirmFinish(f.ready()))
            assertNull(f.ready().snapshot.attempt)
            assertEquals(listOf(AttemptCommand.Route.START, AttemptCommand.Route.CHECKPOINT, AttemptCommand.Route.FINISH),
                f.sent.map { it.second.route })
            assertEquals(1L, f.ready().snapshot.coins)
        }
    }


    @Test fun zeroFloorMissAndSelectionSurviveHostRestartAndLostFinishResponse() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            val initial = f.ready()
            val attempt = initial.snapshot.attempt!!
            f.store.write("alice", attempt.id, FinishDraft(terminalZero(), parts))
            val host = AcceptedEngineHost(f.store).also { it.bind(f.commands); it.onRecoveryChanged(initial) }
            assertTrue(host.snapshot().choosingFinish)
            assertFalse(host.resume(initial))
            assertFalse(host.selectPart(initial.copy(), "wall", "base_6_wall"))
            assertFalse(host.selectPart(initial, "wall", "paid_wall"))
            assertTrue(host.selectPart(initial, "wall", "base_6_wall"))
            f.afterCommit = { throw java.io.IOException("lost finish response") }
            assertTrue(host.confirmFinish(initial)); yield()
            val pending = f.journal().pending("alice")!!
            assertEquals(AttemptCommand.Route.FINISH, pending.route)
            assertFalse(host.confirmFinish(initial))
            f.afterCommit = {}
            f.recreate(); yield()
            assertEquals(pending, f.sent.last().second)
            assertNull(f.journal().pending("alice"))
            assertNull(f.ready().snapshot.attempt)
            assertEquals(2, f.receipts.size) // start + exactly one finish
            val other = initial.copy(accountKey = "bob")
            host.onRecoveryChanged(other)
            assertFalse(host.snapshot().choosingFinish)
            host.onRecoveryChanged(initial)
            assertTrue(host.snapshot().choosingFinish)
            assertEquals(parts, host.snapshot().finishParts)
            assertFalse(host.selectPart(other, "wall", "base_6_wall"))
        }
    }

    @Test fun failedDraftWriteKeepsSelectionAndDoesNotDispatchFinish() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield(); f.commands.start(f.ready()); yield()
            f.commands.checkpoint(f.ready(), landed(true)); yield()
            val ready = f.ready()
            f.draftFailure = true
            assertFalse(f.host.selectPart(ready, "wall", "base_6_wall"))
            assertTrue(f.host.snapshot().finishStorageError)
            assertFalse(f.host.confirmFinish(ready))
            assertFalse(f.host.snapshot().waiting)
            assertEquals(2, f.sent.size)
            assertNull(f.journal().pending("alice"))
            f.draftFailure = false
            assertTrue(f.host.confirmFinish(ready)); yield()
            assertNull(f.ready().snapshot.attempt)
        }
    }

    @Test fun staleLocalTerminalCannotReplaceNewAcceptedProgress() = runBlocking {
        Fixture().use { f ->
            f.login(); yield(); f.commands.start(f.ready()); yield()
            val id = f.ready().snapshot.attempt!!.id
            f.store.write("alice", id, FinishDraft(terminalZero(), parts))
            f.commands.checkpoint(f.ready(), landed()); yield()
            val fresh = AcceptedEngineHost(f.store)
            fresh.onRecoveryChanged(f.ready())
            assertFalse(fresh.snapshot().choosingFinish)
            assertEquals(1, fresh.snapshot().scene!!.floors.size)
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
        for (route in listOf(AttemptCommand.Route.START, AttemptCommand.Route.CHECKPOINT, AttemptCommand.Route.FINISH, AttemptCommand.Route.ABANDON)) Fixture().use { f ->
            f.login(); yield()
            if (route != AttemptCommand.Route.START) { f.commands.start(f.ready()); yield() }
            if (route == AttemptCommand.Route.FINISH) { f.commands.checkpoint(f.ready(), landed(true)); yield() }
            f.afterCommit = { error("lost committed response") }
            when (route) {
                AttemptCommand.Route.SHOP_BUY, AttemptCommand.Route.BUILDING_EDIT, AttemptCommand.Route.BUILDING_DELETE -> error("Not an attempt command")
                AttemptCommand.Route.START -> f.commands.start(f.ready())
                AttemptCommand.Route.CHECKPOINT -> f.commands.checkpoint(f.ready(), landed(true))
                AttemptCommand.Route.FINISH -> f.commands.finish(f.ready(), landed(true), parts)
                AttemptCommand.Route.ABANDON -> f.commands.abandon(f.ready())
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
            assertEquals(old.snapshot, f.ready().snapshot)
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

    // Restart/abandon policy (user decision, 9 November 2026).
    private fun routes(f: Fixture) = f.sent.map { it.second.route }

    @Test fun leavingOpenAttemptMidGameAbandonsItWithoutRefundAndFreesStart() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield()
            assertTrue(f.commands.start(f.ready())); yield()
            f.host.enter()
            assertTrue(f.commands.checkpoint(f.ready(), landed())); yield()
            assertEquals(1L, f.ready().snapshot.coins)
            assertTrue(f.host.snapshot().playing)
            assertTrue(f.host.leave(f.ready())); yield()
            assertFalse(f.host.snapshot().playing)
            assertEquals(AttemptCommand.Route.ABANDON, f.sent.last().second.route)
            val request = Json.parseToJsonElement(f.sent.last().second.envelope).jsonObject.getValue("request").jsonObject
            assertEquals(1L, request.getValue("sequence").jsonPrimitive.long)
            assertNull(f.ready().snapshot.attempt)
            assertEquals(1L, f.ready().snapshot.coins) // accepted checkpoint rewards stay; no refund/clawback
            f.host.enforceLeave(f.ready())
            assertFalse(f.host.leavePending())
            assertEquals(1, routes(f).count { it == AttemptCommand.Route.ABANDON })
            assertTrue(f.commands.start(f.ready())); yield()
            assertTrue(f.ready().snapshot.attempt!!.active)
        }
    }

    @Test fun lossIsKeptOnLeaveAndItsFinishPickerStaysResumable() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            f.host.enter()
            f.commands.checkpoint(f.ready(), landed(true)); yield()
            assertTrue(f.host.snapshot().choosingFinish)
            assertFalse(f.host.leave(f.ready()))
            f.host.enforceLeave(f.ready()); yield()
            assertFalse(routes(f).contains(AttemptCommand.Route.ABANDON))
            assertTrue(f.ready().snapshot.attempt!!.active)
            assertThrows(IllegalArgumentException::class.java) { f.commands.abandon(f.ready()) }
            assertFalse(f.host.discardOpen(f.ready()))
            f.host.enter()
            assertTrue(f.host.confirmFinish(f.ready())); yield()
            assertEquals(AttemptCommand.Route.FINISH, routes(f).last())
        }
    }

    @Test fun zeroFloorLocalMissIsKeptOnLeave() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            f.store.write("alice", f.ready().snapshot.attempt!!.id, FinishDraft(terminalZero(), parts))
            val host = AcceptedEngineHost(f.store).also { it.bind(f.commands); it.onRecoveryChanged(f.ready()) }
            host.enter()
            assertFalse(host.leave(f.ready()))
            assertFalse(host.discardOpen(f.ready()))
            yield()
            assertEquals(listOf(AttemptCommand.Route.START), routes(f))
        }
    }

    @Test fun leaveWhileCheckpointInFlightDecidesOnNextReady() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            f.host.enter()
            val gate = CompletableDeferred<Unit>()
            f.beforeCommit = { gate.await() }
            assertTrue(f.commands.checkpoint(f.ready(), landed())); yield()
            assertTrue(f.owner.state.value is AuthenticatedRecoveryOwner.State.Recovering)
            assertTrue(f.host.leave(null))
            assertTrue(f.host.leavePending())
            f.beforeCommit = {}
            gate.complete(Unit); yield()
            f.host.enforceLeave(f.ready()); yield()
            assertEquals(AttemptCommand.Route.ABANDON, routes(f).last())
            assertNull(f.ready().snapshot.attempt)
        }
    }

    @Test fun lossLandingInFlightIsNotAbandonedByAnEarlierLeave() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            f.host.enter()
            val gate = CompletableDeferred<Unit>()
            f.beforeCommit = { gate.await() }
            f.commands.checkpoint(f.ready(), landed(true)); yield()
            assertTrue(f.host.leave(null))
            f.beforeCommit = {}
            gate.complete(Unit); yield()
            f.host.enforceLeave(f.ready()); yield()
            assertFalse(f.host.leavePending())
            assertFalse(routes(f).contains(AttemptCommand.Route.ABANDON))
            assertTrue(f.host.snapshot().choosingFinish)
        }
    }

    @Test fun appClosedMidGameIsNotResumedAndIsAbandonedOnlyWhenPlayIsPressed() = runBlocking {
        Fixture().use { f ->
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            f.commands.checkpoint(f.ready(), landed()); yield()
            f.recreate(); yield() // process death: the new host never entered this attempt
            val host = AcceptedEngineHost(f.store).also { it.bind(f.commands); it.onRecoveryChanged(f.ready()) }
            assertFalse(host.snapshot().playing)
            assertFalse(host.leave(f.ready())) // launching alone abandons nothing (another device may be playing)
            host.enforceLeave(f.ready()); yield()
            assertFalse(routes(f).contains(AttemptCommand.Route.ABANDON))
            assertFalse(host.discardOpen(f.ready().copy()))
            assertTrue(host.discardOpen(f.ready())); yield()
            assertEquals(AttemptCommand.Route.ABANDON, routes(f).last())
            assertNull(f.ready().snapshot.attempt)
            host.onRecoveryChanged(f.ready())
            host.enforceLeave(f.ready())
            assertFalse(host.leavePending())
        }
    }

    @Test fun leftAttemptIsNeverAbandonedForAnotherAccountOrAfterReentry() = runBlocking {
        Fixture().use { f ->
            f.host.bind(f.commands)
            f.login(); yield()
            f.commands.start(f.ready()); yield()
            f.host.enter()
            val alice = f.ready()
            val gate = CompletableDeferred<Unit>()
            f.beforeCommit = { gate.await() }
            f.commands.checkpoint(alice, landed()); yield()
            assertTrue(f.host.leave(null))
            f.host.enter() // re-entered before the abandon could go out
            assertFalse(f.host.leavePending())
            f.beforeCommit = {}
            gate.complete(Unit); yield()
            f.host.enforceLeave(f.ready()); yield()
            assertFalse(routes(f).contains(AttemptCommand.Route.ABANDON))
            assertTrue(f.host.leave(null)) // left, abandon not yet dispatched
            val bob = f.ready().copy(accountKey = "bob")
            f.host.onRecoveryChanged(bob)
            f.host.enforceLeave(bob)
            assertFalse(f.host.leavePending())
            assertFalse(routes(f).contains(AttemptCommand.Route.ABANDON))
        }
    }
}
