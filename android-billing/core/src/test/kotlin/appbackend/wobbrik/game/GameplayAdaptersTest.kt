package appbackend.wobbrik.game

import appbackend.wobbrik.billing.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.*

class GameplayAdaptersTest {
    private fun journal(root: File) = DiskAttemptJournal { key ->
        object : JournalFile {
            val target = File(root, key)
            override fun read() = if (target.exists()) target.readBytes() else null
            override fun replace(bytes: ByteArray) {
                val temp = File(root, "$key.tmp")
                FileOutputStream(temp).use { it.write(bytes); it.fd.sync() }
                Files.move(temp.toPath(), target.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            }
        }
    }
    private class Fixture {
        var session: BillingSession? = BillingSession("alice", "epoch-1")
        var tokens = 0
        val sessions = object : Sessions {
            override fun current() = session
            override suspend fun idToken(session: BillingSession): String { tokens++; return "fresh-$tokens" }
        }
        val snapshot = """{"rules_version":"wobbrik-v1","revision":0,"coins":7,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
        val receipt = """{"revision":1,"accepted_at":"2026-11-05T00:00:00Z","result":{"attempt_id":"op"}}"""
    }
    @Test fun diskRecreationIsolationCompareRemoveAndCorruption() {
        val root = Files.createTempDirectory("attempt-test").toFile()
        try {
            val c = GameplayWire.start("op", 0)
            journal(root).save("alice", c)
            assertEquals(c, journal(root).pending("alice")); assertNull(journal(root).pending("bob"))
            assertThrows(IllegalStateException::class.java) { journal(root).save("alice", GameplayWire.start("other", 0)) }
            assertThrows(IllegalStateException::class.java) { journal(root).remove("alice", GameplayWire.start("other", 0)) }
            journal(root).remove("alice", c); assertNull(journal(root).pending("alice"))
            root.listFiles()!!.single().writeBytes(byteArrayOf(0))
            assertThrows(Exception::class.java) { journal(root).pending("alice") }
        } finally { root.deleteRecursively() }
    }
    @Test fun lostResponseRetriesIdenticalWireAfterDiskRecreation() = runBlocking {
        val root = Files.createTempDirectory("attempt-retry").toFile()
        try {
            val f = Fixture(); val bodies = mutableListOf<String>(); var lose = true
            val backend = GameplayHttpBackend(f.sessions, GameplayTransport { path, token, body ->
                assertTrue(token.startsWith("fresh-"))
                if (path == "sync") GameplayResponse(200, f.snapshot) else {
                    bodies += body!!
                    if (lose) { lose = false; throw IOException("lost committed response") }
                    GameplayResponse(200, f.receipt)
                }
            })
            val first = AttemptCoordinator(f.sessions, backend, journal(root)); first.recover()
            try { first.execute(GameplayWire.start("op", 0)); fail() } catch (_: IOException) { }
            val recreated = AttemptCoordinator(f.sessions, backend, journal(root)); recreated.recover()
            assertEquals(2, bodies.size); assertEquals(bodies[0], bodies[1]); assertTrue(recreated.ready())
            assertEquals(7L, recreated.snapshot()!!.coins); assertEquals(4, f.tokens)
        } finally { root.deleteRecursively() }
    }
    @Test fun onlyExplicitRevisionConflictIsMapped() = runBlocking {
        val f = Fixture()
        for ((status, code) in listOf(409 to "operation_payload_conflict", 404 to "not_found", 503 to "temporarily_unavailable", 400 to "revision_conflict")) {
            val backend = GameplayHttpBackend(f.sessions, GameplayTransport { _, _, _ -> GameplayResponse(status, """{"error":"$code"}""") })
            try { backend.submit(f.session!!, GameplayWire.start("op", 0)); fail() } catch (e: GameplayHttpError) { assertEquals(code, e.code) }
        }
        val backend = GameplayHttpBackend(f.sessions, GameplayTransport { _, _, _ -> GameplayResponse(409, """{"error":"revision_conflict"}""") })
        try { backend.submit(f.session!!, GameplayWire.start("op", 0)); fail() } catch (_: RevisionConflict) { }
    }
    @Test fun sameUidNewEpochRejectsResponse() = runBlocking {
        val f = Fixture(); val original = f.session!!
        val backend = GameplayHttpBackend(f.sessions, GameplayTransport { _, _, _ ->
            f.session = BillingSession("alice", "epoch-2"); GameplayResponse(200, f.snapshot)
        })
        try { backend.sync(original); fail() } catch (_: AccountChanged) { }
    }
    @Test fun catalogIsAuthenticatedVersionCheckedAndEpochGuarded() = runBlocking {
        val f = Fixture()
        val body = """{"rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1","items":[],"land_offers":[]}"""
        var response = body
        var switchEpoch = false
        val backend = GameplayHttpBackend(f.sessions, GameplayTransport { path, token, request ->
            assertEquals("catalog", path); assertEquals("fresh-${f.tokens}", token); assertNull(request)
            if (switchEpoch) f.session = BillingSession("alice", "epoch-2")
            GameplayResponse(200, response)
        })
        assertEquals("wobbrik-v1", backend.catalog(f.session!!).rulesVersion)
        response = body.replace("wobbrik-v1", "wobbrik-v2")
        try { backend.catalog(f.session!!); fail() } catch (_: IllegalArgumentException) { }
        response = body; switchEpoch = true
        try { backend.catalog(f.session!!); fail() } catch (_: AccountChanged) { }
    }

    @Test fun accountScopedLookupsValidateIdentityStatusErrorsAndEpoch() = runBlocking {
        val f = Fixture()
        val attemptJson = """{"attempt_id":"wanted","status":"abandoned","sequence":0,"floors":0,"precise":0,"rescues":0,"continued":false,"rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1","snapshot":null}"""
        var response = GameplayResponse(200, f.snapshot.replace("\"attempt\":null", "\"attempt\":$attemptJson"))
        var switchEpoch = false
        val backend = GameplayHttpBackend(f.sessions, GameplayTransport { path, _, body ->
            assertTrue(allowedReadPath(path)); assertNull(body)
            if (switchEpoch) f.session = BillingSession("alice", "new-epoch")
            response
        })
        assertTrue(backend.attempt(f.session!!, "wanted")!!.attempt!!.abandoned)
        assertThrows(Exception::class.java) { RecoveryDtos.snapshot(response.body) }
        try { backend.attempt(f.session!!, "wrong"); fail() } catch (_: IllegalArgumentException) { }
        response = GameplayResponse(404, """{"error":"not_found"}""")
        assertNull(backend.attempt(f.session!!, "wanted"))
        assertNull(backend.receipt(f.session!!, "op", AttemptCommand.Route.FINISH))
        for (status in listOf(401, 403, 429, 503)) {
            response = GameplayResponse(status, """{"error":"denied"}""")
            try { backend.attempt(f.session!!, "wanted"); fail() } catch (e: GameplayHttpError) { assertEquals(status, e.status) }
        }
        response = GameplayResponse(200, """{"revision":3,"accepted_at":"2026-11-14T00:00:00Z","result":{"attempt_id":"wanted","floors":0,"coins":0,"residents":0,"base_value":0,"max_price":0}}""")
        assertEquals("wanted", (backend.receipt(f.session!!, "op", AttemptCommand.Route.FINISH)!!.result as AttemptResult.Finished).attemptId)
        switchEpoch = true
        try { backend.receipt(f.session!!, "op", AttemptCommand.Route.FINISH); fail() } catch (_: AccountChanged) { }
        assertFalse(allowedReadPath("attempts/../sync")); assertFalse(allowedReadPath("operations/op?uid=bob"))
    }

    @Test fun metadataMismatchAndUnexpectedFieldsAreRejected() {
        val c = GameplayWire.start("op", 0)
        assertThrows(IllegalArgumentException::class.java) { GameplayWire.validate(c.copy(operationId = "other")) }
        assertThrows(Exception::class.java) { GameplayWire.validate(c.copy(route = AttemptCommand.Route.FINISH)) }
        val checkpoint = GameplayWire.checkpoint("cp", 1, "op", 1, 10, 2, 0, "{\"x\":1}")
        GameplayWire.validate(checkpoint)
        GameplayWire.validate(GameplayWire.finish("finish", 2, "op", 1, setOf("wall", "window", "entrance", "roof", "lighting").associateWith { "free_$it" }))
        // Restart/abandon policy (9 November 2026): exact two-field abandon request.
        val abandon = GameplayWire.abandon("ab", 3, "op", 1)
        GameplayWire.validate(abandon)
        assertEquals("""{"attempt_id":"op","sequence":1}""", kotlinx.serialization.json.Json.parseToJsonElement(abandon.envelope).let { (it as kotlinx.serialization.json.JsonObject).getValue("request").toString() })
        assertThrows(Exception::class.java) { GameplayWire.validate(abandon.copy(route = AttemptCommand.Route.FINISH)) }
        assertThrows(Exception::class.java) { GameplayWire.validate(abandon.copy(envelope = abandon.envelope.replace("\"sequence\":1", "\"sequence\":1,\"floors\":3"))) }
        assertThrows(IllegalArgumentException::class.java) { GameplayWire.abandon("ab", 3, "", 1) }
        assertThrows(IllegalArgumentException::class.java) { GameplayWire.abandon("ab", 3, "op", -1) }
        assertThrows(Exception::class.java) { GameplayWire.snapshot("""{"rules_version":"wobbrik-v1","revision":"0","coins":0,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}""") }
    }
}
