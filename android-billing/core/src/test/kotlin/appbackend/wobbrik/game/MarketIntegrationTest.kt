package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class MarketIntegrationTest {
    private fun commands() = listOf(GameplayWire.listBuilding("fixed", 0, "building", 2, 21),
        GameplayWire.marketListing("fixed", 0, AttemptCommand.Route.MARKET_WITHDRAW, "listing", 1),
        GameplayWire.marketListing("fixed", 0, AttemptCommand.Route.MARKET_BUY, "listing", 1))
    private fun receipt(command: AttemptCommand) = """{"revision":1,"accepted_at":"2026-11-14T00:00:00Z","result":{"building_id":"building","building_revision":${if(command.route == AttemptCommand.Route.MARKET_BUY) 3 else 2},"listing_id":"listing","listing_revision":${if(command.route == AttemptCommand.Route.MARKET_LIST) 1 else 2},"price":21,"fee":2}}"""
    private fun sync(revision: Int) = """{"rules_version":"wobbrik-v1","revision":$revision,"coins":79,"xp":0,"attempt":null,"server_time":"2026-11-14T00:00:00Z","first_online_at":"2026-11-14T00:00:00Z","grace_until":"2026-11-14T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""

    @Test fun allRoutesRecoverExactDurableCommandAfterCommittedResponseLoss() = runBlocking {
        for (command in commands()) {
            val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
            val files = mutableMapOf<String, ByteArray>()
            fun journal() = DiskAttemptJournal { key -> object : JournalFile {
                override fun read() = files[key]
                override fun replace(bytes: ByteArray) { files[key] = bytes }
            } }
            val requests = mutableListOf<Pair<String, String>>()
            val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
                if (path == "sync") GameplayResponse(200, sync(if (requests.isEmpty()) 0 else 1)) else {
                    requests += path to requireNotNull(body)
                    if (requests.size == 1) throw IOException("committed response lost")
                    GameplayResponse(200, receipt(command))
                }
            })
            val first = AttemptCoordinator(sessions, backend, journal()); first.recover()
            try { first.execute(command); fail() } catch (_: IOException) { }
            assertEquals(command, journal().pending("alice")); assertNull(journal().pending("bob"))
            val restarted = AttemptCoordinator(sessions, backend, journal()); restarted.recover()
            assertEquals(2, requests.size); assertEquals(requests[0], requests[1])
            assertEquals("market/" + command.route.name.substringAfter("MARKET_").lowercase(), requests[0].first)
            assertNull(journal().pending("alice")); assertEquals(79L, restarted.snapshot()!!.coins)
        }
    }
    @Test fun receiptsAndEnvelopesRejectWrongIdentityFeeRevisionAndUnknownFields() {
        commands().forEach { command ->
            GameplayWire.validate(command); MarketMutations.receipt(receipt(command), command)
            for (bad in listOf(receipt(command).replace("\"fee\":2", "\"fee\":1"),
                receipt(command).replace("\"revision\":1", "\"revision\":0"),
                receipt(command).replace(if(command.route == AttemptCommand.Route.MARKET_LIST) "\"building_id\":\"building\"" else "\"listing_id\":\"listing\"", if(command.route == AttemptCommand.Route.MARKET_LIST) "\"building_id\":\"foreign\"" else "\"listing_id\":\"foreign\""))) {
                assertThrows(IllegalArgumentException::class.java) { MarketMutations.receipt(bad, command) }
            }
            assertThrows(IllegalArgumentException::class.java) { GameplayWire.validate(command.copy(envelope = command.envelope.replace("\"request\":{", "\"request\":{\"unknown\":1,"))) }
        }
        assertEquals(461168601842738791L, MarketMutations.fee(Long.MAX_VALUE))
    }
    @Test fun definitiveRejectionsClearJournalButAmbiguousFailuresRetainIt() = runBlocking {
        for ((status, code, retained) in listOf(Triple(409,"listing_conflict",false), Triple(409,"insufficient_balance",false),
            Triple(404,"not_found",false),Triple(409,"revision_conflict",false),Triple(500,"unknown",true))) {
            val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
            var pending: AttemptCommand? = null
            val journal = object : AttemptJournal {
                override fun pending(uid: String) = pending
                override fun save(uid: String, command: AttemptCommand) { pending = command }
                override fun remove(uid: String, command: AttemptCommand) { pending = null }
            }
            val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
                if(path == "sync") GameplayResponse(200,sync(0)) else GameplayResponse(status,"""{"error":"$code"}""")
            })
            val coordinator = AttemptCoordinator(sessions,backend,journal); coordinator.recover()
            try { coordinator.execute(commands().last()); fail() } catch (_: Exception) { }
            assertEquals(retained,pending != null)
        }
    }
    @Test fun filtersStayIdenticalAcrossPagesAndDoNotAcceptPathInjection() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val paths = mutableListOf<String>()
        val filter = MarketFilter(10,20,1,100,"base_1_roof")
        val repo = MarketRepository(GameplayReads { _, path -> paths += path; """{"listings":[]}""" })
        repo.page(sessions.current()!!,false,filter); repo.page(sessions.current()!!,false,filter,"last")
        assertEquals(paths[0].substringAfter("&min_floors"),paths[1].substringAfter("&min_floors"))
        paths.forEach { assertTrue(allowedReadPath(it)) }
        assertFalse(allowedReadPath("market/../profile"))
        assertFalse(allowedReadPath(paths[0]+"&uid=bob"))
        assertThrows(IllegalArgumentException::class.java) { MarketFilter(20,10) }
        assertThrows(IllegalArgumentException::class.java) { MarketFilter(partId="a&uid=bob") }
        Unit
    }
    @Test fun readModelsRejectWrongCursorDuplicateListingsAndPublicClosedHistory() {
        val listing = """{"id":"listing","building_id":"building","revision":1,"building_revision":2,"price":21,"fee":2,"status":"active"}"""
        assertEquals("listing",MarketReads.page("""{"listings":[$listing],"next_cursor":"listing"}""",false).next)
        assertEquals("withdrawn",MarketReads.page("""{"listings":[${listing.replace("active","withdrawn") }]}""",true).listings.single().status)
        listOf("""{"listings":[$listing,$listing]}""", """{"listings":[$listing],"next_cursor":"foreign"}""",
            """{"listings":[${listing.replace("active","withdrawn")}]}""", """{"listings":[${listing.replace("\"fee\":2","\"fee\":1")}]}""").forEach { body ->
            assertThrows(IllegalArgumentException::class.java) { MarketReads.page(body,false) }
        }
    }
    @Test fun staleSameUidCapabilityCannotIssueMarketCommand() = runBlocking {
        val sessions = AccountSessions { "token" }
        val scope = CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        var submissions = 0
        val owner = AuthenticatedRecoveryOwner(sessions,object : AttemptBackend {
            override suspend fun sync(session: appbackend.wobbrik.billing.BillingSession) = GameplayWire.snapshot(sync(0))
            override suspend fun submit(session: appbackend.wobbrik.billing.BillingSession, command: AttemptCommand) { submissions++ }
        },object : AttemptJournal {
            override fun pending(uid: String): AttemptCommand? = null
            override fun save(uid: String,command: AttemptCommand) = Unit
            override fun remove(uid: String,command: AttemptCommand) = Unit
        },scope)
        try {
            owner.onAuthChanged("alice"); val old = owner.state.value as AuthenticatedRecoveryOwner.State.Ready
            owner.onAuthChanged("alice")
            assertFalse(MarketMutations(owner).withdraw(old,MarketListing("listing","building",1,2,21,2,"active")))
            assertEquals(0,submissions)
        } finally { scope.cancel() }
    }
}
