package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class PartsMutationsTest {
    private val parts = FinishCosmetics.categories.associateWith { "free_$it" }
    private fun command(edit: Boolean) = if (edit) GameplayWire.editBuilding("fixed", 0, "building", 2, parts)
        else GameplayWire.buyPart("fixed", 0, "coins_wall_1")
    private fun receipt(edit: Boolean) = """{"revision":1,"accepted_at":"2026-11-13T00:00:00Z","result":${if (edit) """{"building_id":"building","building_revision":3,"price":0,"fee":0}""" else """{"item_id":"coins_wall_1","price":100}"""}}"""

    @Test fun bothRoutesPersistExactEnvelopeAndRetryAfterLostResponse() = runBlocking {
        for (edit in listOf(false, true)) {
            val sessions = AccountSessions { "fresh" }; sessions.onAuthChanged("alice")
            val files = mutableMapOf<String, ByteArray>()
            fun journal() = DiskAttemptJournal { key -> object : JournalFile {
                override fun read() = files[key]
                override fun replace(bytes: ByteArray) { files[key] = bytes }
            } }
            val sent = mutableListOf<Pair<String, String>>()
            var lose = true
            val sync = """{"rules_version":"wobbrik-v1","revision":1,"coins":900,"xp":0,"attempt":null,"server_time":"2026-11-13T00:00:00Z","first_online_at":"2026-11-13T00:00:00Z","grace_until":"2026-11-13T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":["coins_wall_1"],"land":[],"part_progress":[]}"""
            val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
                if (path == "sync") GameplayResponse(200, sync.replace("\"revision\":1", if (sent.isEmpty()) "\"revision\":0" else "\"revision\":1"))
                else {
                    sent += path to requireNotNull(body)
                    if (lose) { lose = false; throw IOException("committed response lost") }
                    GameplayResponse(200, receipt(edit))
                }
            })
            val first = AttemptCoordinator(sessions, backend, journal()); first.recover()
            try { first.execute(command(edit)); fail() } catch (_: IOException) { }
            assertEquals(command(edit), journal().pending("alice"))
            assertNull(journal().pending("bob"))
            val second = AttemptCoordinator(sessions, backend, journal()); second.recover()
            assertEquals(listOf(sent.first(), sent.first()), sent)
            assertEquals(if (edit) "buildings/edit" else "shop/buy", sent.first().first)
            assertNull(journal().pending("alice")); assertEquals(900, second.snapshot()!!.coins.toInt())
            assertTrue(second.ready())
        }
    }

    @Test fun staleReadyCannotPurchaseOrEditAfterSameUidRelogin() = runBlocking {
        val sessions = AccountSessions { "token" }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        var submits = 0
        val catalog = RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1",
            parts.map { (category, id) -> CatalogItem(id, id, category, "free", 0, null, null) } +
                CatalogItem("coins_wall_1", "Wall", "wall", "coins", 4, 100, null), emptyList())
        val backend = object : AttemptBackend {
            override suspend fun catalog(session: appbackend.wobbrik.billing.BillingSession) = catalog
            override suspend fun sync(session: appbackend.wobbrik.billing.BillingSession) = GameSnapshot("wobbrik-v1", 0, 1000, 0, null)
            override suspend fun submit(session: appbackend.wobbrik.billing.BillingSession, command: AttemptCommand) { submits++ }
        }
        val journal = object : AttemptJournal {
            override fun pending(uid: String): AttemptCommand? = null
            override fun save(uid: String, command: AttemptCommand) = Unit
            override fun remove(uid: String, command: AttemptCommand) = Unit
        }
        try {
            val owner = AuthenticatedRecoveryOwner(sessions, backend, journal, scope)
            owner.onAuthChanged("alice"); yield()
            val stale = owner.state.value as AuthenticatedRecoveryOwner.State.Ready
            owner.onAuthChanged("alice"); yield()
            val commands = PartsMutations(owner)
            assertFalse(commands.buy(stale, "coins_wall_1"))
            val building = OwnedBuilding("building", 2, 10, 40, 20, 24, parts + ("wall" to "coins_wall_1"), false, null)
            assertFalse(commands.apply(stale, building, parts))
            assertFalse(BuildingDeletion(owner).confirmedDelete(stale, building))
            assertEquals(0, submits)
        } finally { scope.cancel() }
    }

    @Test fun wireAndReceiptRejectChangedIdentityRevisionAndExtraFields() {
        for (edit in listOf(false, true)) {
            val c = command(edit); GameplayWire.validate(c); PartsMutations.receipt(receipt(edit), c)
            assertThrows(IllegalArgumentException::class.java) { GameplayWire.validate(c.copy(expectedRevision = 1)) }
            assertThrows(IllegalArgumentException::class.java) { PartsMutations.receipt(receipt(edit).replace(if (edit) "building\"" else "coins_wall_1\"", "foreign\""), c) }
            assertThrows(IllegalArgumentException::class.java) { GameplayWire.validate(c.copy(envelope = c.envelope.replace("\"request\":{", "\"request\":{\"price\":0,"))) }
        }
    }

    @Test fun definitiveRejectionClearsJournalButUnknownFailureRetainsIt() = runBlocking {
        for (code in listOf("insufficient_balance", "listing_conflict", "invalid_request", "operation_payload_conflict")) {
            val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
            var pending: AttemptCommand? = null
            val journal = object : AttemptJournal {
                override fun pending(uid: String) = pending
                override fun save(uid: String, command: AttemptCommand) { pending = command }
                override fun remove(uid: String, command: AttemptCommand) { assertEquals(pending, command); pending = null }
            }
            val backend = GameplayHttpBackend(sessions, GameplayTransport { _, _, _ -> GameplayResponse(if (code == "invalid_request") 400 else 409, """{"error":"$code"}""") })
            // Seed the same journal used by recovery; no new operation on retry.
            pending = command(code == "listing_conflict")
            val coordinator = AttemptCoordinator(sessions, object : AttemptBackend {
                override suspend fun sync(session: appbackend.wobbrik.billing.BillingSession) = GameSnapshot("wobbrik-v1", 0, 0, 0, null)
                override suspend fun submit(session: appbackend.wobbrik.billing.BillingSession, command: AttemptCommand) = backend.submit(session, command)
            }, journal)
            try { coordinator.recover(); fail() } catch (_: Exception) { }
            assertEquals(code == "operation_payload_conflict", pending != null)
        }
    }
}
