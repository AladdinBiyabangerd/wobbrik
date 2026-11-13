package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class BuildingDeletionTest {
    private val command = GameplayWire.deleteBuilding("fixed", 0, "building", 2)
    private val receipt = """{"revision":1,"accepted_at":"2026-11-13T00:00:00Z","result":{"building_id":"building","building_revision":3,"price":0,"fee":0}}"""

    @Test fun lostResponseReplaysExactDeletionFromRecreatedJournal() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val files = mutableMapOf<String, ByteArray>()
        fun journal() = DiskAttemptJournal { key -> object : JournalFile {
            override fun read() = files[key]
            override fun replace(bytes: ByteArray) { files[key] = bytes }
        } }
        val sent = mutableListOf<Pair<String, String?>>()
        var lose = true
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
            if (path == "sync") GameplayResponse(200, """{"rules_version":"wobbrik-v1","revision":${if (sent.isEmpty()) 0 else 1},"coins":800,"xp":0,"attempt":null,"server_time":"2026-11-13T00:00:00Z","first_online_at":"2026-11-13T00:00:00Z","grace_until":"2026-11-13T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}""")
            else {
                sent += path to body
                if (lose) { lose = false; throw IOException("committed response lost") }
                GameplayResponse(200, receipt)
            }
        })
        val first = AttemptCoordinator(sessions, backend, journal()); first.recover()
        try { first.execute(command); fail() } catch (_: IOException) { }
        assertEquals(command, journal().pending("alice")); assertNull(journal().pending("bob"))
        val recovered = AttemptCoordinator(sessions, backend, journal()); recovered.recover()
        assertEquals(listOf("buildings/delete" to command.envelope, "buildings/delete" to command.envelope), sent)
        assertNull(journal().pending("alice")); assertEquals(800L, recovered.snapshot()!!.coins)
        assertTrue(recovered.ready())
    }

    @Test fun rejectsWrongIdentityRevisionRefundAndExtraRequestFields() {
        GameplayWire.validate(command); BuildingDeletion.receipt(receipt, command)
        assertThrows(IllegalArgumentException::class.java) {
            appbackend.wobbrik.game.historical.GameplayWire.validate(command)
        }
        for (invalid in listOf(receipt.replace("building\"", "foreign\""),
            receipt.replace("building_revision\":3", "building_revision\":2"),
            receipt.replace("price\":0", "price\":1"), receipt.replace("fee\":0", "fee\":1"),
            receipt.replace("revision\":1", "revision\":0"))) {
            assertThrows(IllegalArgumentException::class.java) { BuildingDeletion.receipt(invalid, command) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            GameplayWire.validate(command.copy(envelope = command.envelope.replace("\"request\":{", "\"request\":{\"refund\":100,")))
        }
        assertThrows(IllegalArgumentException::class.java) { GameplayWire.deleteBuilding("id", 0, "building", 0) }
    }

    @Test fun definitiveRejectionClearsPendingButAmbiguousOutcomeRetainsIt() = runBlocking {
        for ((status, code) in listOf(409 to "listing_conflict", 409 to "building_conflict", 404 to "not_found",
            400 to "invalid_request", 409 to "revision_conflict", 409 to "operation_payload_conflict", 500 to "internal")) {
            val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
            var pending: AttemptCommand? = command
            val journal = object : AttemptJournal {
                override fun pending(uid: String) = pending
                override fun save(uid: String, command: AttemptCommand) { pending = command }
                override fun remove(uid: String, command: AttemptCommand) { pending = null }
            }
            val http = GameplayHttpBackend(sessions, GameplayTransport { _, _, _ -> GameplayResponse(status, """{"error":"$code"}""") })
            val coordinator = AttemptCoordinator(sessions, object : AttemptBackend {
                override suspend fun sync(session: appbackend.wobbrik.billing.BillingSession) = GameSnapshot("wobbrik-v1", 1, 800, 0, null)
                override suspend fun submit(session: appbackend.wobbrik.billing.BillingSession, command: AttemptCommand) = http.submit(session, command)
            }, journal)
            try { coordinator.recover(); fail() } catch (_: Exception) { }
            assertEquals(code, code in setOf("operation_payload_conflict", "internal"), pending != null)
        }
    }
}
