package appbackend.wobbrik.game

import appbackend.wobbrik.billing.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class InventoryPayloadTest {
    private val snapshot = """{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
    private fun page(full: Boolean): String = buildJsonObject {
        putJsonArray("buildings") { repeat(20) { i -> add(buildJsonObject {
            put("id", "b-$i"); put("revision", 1); put("floors", 10); put("residents", 40)
            put("base_value", 20); put("max_price", 24); put("traded", false)
            putJsonObject("parts") { for ((k,v) in mapOf("wall" to "w", "window" to "v", "entrance" to "e", "roof" to "r", "lighting" to "l")) put(k,v) }
            if (full) putJsonObject("snapshot") { put("opaque", "x".repeat(16000)) } else put("snapshot", JsonNull)
        }) } }
        put("next_cursor", "b-19")
    }.toString()

    @Test fun oversizedLegacyInventoryCanBeCachedAndReopenedWithoutNetwork() = runBlocking {
        verifyCache(full = true)
    }
    @Test fun displayInventoryKeepsSameModelAndCanBeCached() = runBlocking {
        assertEquals(InventoryReads.inventory(page(true)), InventoryReads.inventory(page(false)))
        verifyCache(full = false)
    }
    private suspend fun verifyCache(full: Boolean) {
        val files = mutableMapOf<String, ByteArray>()
        val cache = ReadOnlyGameCache { key -> object : JournalFile {
            override fun read() = files[key]
            override fun replace(bytes: ByteArray) { files[key] = bytes.clone() }
        } }
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(snapshot), RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        val response = page(full)
        var requests = 0
        var responseBytes = 0
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
            assertNull(body); requests++
            val wire = if (path == "sync") snapshot else response
            // Controlled transfer at 256 bytes/ms. This is not production throughput.
            delay(wire.toByteArray().size.toLong() / 256)
            responseBytes += wire.toByteArray().size
            GameplayResponse(200, wire)
        })
        val start = System.nanoTime()
        val live = cache.capture(session, account, "inventory-", backend) { InventoryRepository(it).page(session) }
        val captureMs = (System.nanoTime() - start) / 1_000_000
        println("INVENTORY full=$full wireBytes=$responseBytes requests=$requests cacheBytes=${files.values.sumOf { it.size }} captureMs=$captureMs")
        assertEquals(1, files.size)
        assertTrue(files.values.single().size < DurableJournalFile.MaxBytes)
        val calls = requests
        val replayAt = System.nanoTime()
        assertEquals(live, cache.replay(session, account, "inventory-") { InventoryRepository(it).page(session) })
        println("INVENTORY full=$full replayMs=${(System.nanoTime()-replayAt)/1_000_000} replayRequests=${requests-calls}")
        assertEquals(calls, requests)
        assertTrue(runCatching { cache.replay(session, account.copy(snapshot = RecoveryDtos.snapshot(snapshot.replace("\"revision\":1", "\"revision\":2"))), "inventory-") { InventoryRepository(it).page(session) } }.isFailure)
        assertTrue(runCatching { cache.replay(BillingSession("bob", "other"), account, "inventory-") { InventoryRepository(it).page(session) } }.isFailure)
    }

    @Test fun detailDisplayNormalizesOldServerAndReplaysExactLegacyBundle() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(snapshot), RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        var bytes: ByteArray? = null
        val cache = ReadOnlyGameCache { object : JournalFile {
            override fun read() = bytes
            override fun replace(value: ByteArray) { bytes = value }
        } }
        val detail = Json.parseToJsonElement(page(true)).jsonObject.getValue("buildings").jsonArray.first().toString()
        val paths = mutableListOf<String>()
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            paths += path
            GameplayResponse(200, if (path == "sync") snapshot else detail)
        })
        val result = cache.capture(session, account, "detail-b-0", backend) { InventoryRepository(it).building(session, "b-0") }
        assertEquals(listOf("buildings/b-0?view=display", "sync"), paths)
        assertFalse(bytes!!.toString(Charsets.UTF_8).contains("opaque"))
        assertEquals(result, cache.replay(session, account, "detail-b-0") { InventoryRepository(it).building(session, "b-0") })
        cache.capture(session, account, "detail-b-0", backend) { InventoryReads.building(it.read(session, "buildings/b-0")) }
        assertEquals(result, cache.replay(session, account, "detail-b-0") { InventoryRepository(it).building(session, "b-0") })
        assertTrue(runCatching { cache.replay(session, account, "detail-b-0") { InventoryRepository(it).building(session, "b-1") } }.isFailure)
    }
    @Test fun oldTwentyCityBundleReplaysAllPagesWithHundredPageRepository() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(snapshot), RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        var bytes: ByteArray? = null
        val cache = ReadOnlyGameCache { object : JournalFile {
            override fun read() = bytes
            override fun replace(value: ByteArray) { bytes = value }
        } }
        val first = """{"rules_version":"wobbrik-v1","revision":1,"population":"40","buildings":[{"building_id":"b-1","building_revision":1}],"next_cursor":"b-1"}"""
        val last = """{"rules_version":"wobbrik-v1","revision":1,"population":"40","buildings":[]}"""
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            GameplayResponse(200, if (path == "sync") snapshot else if ("cursor" in path) last else first)
        })
        cache.capture(session, account, "inventory-city", backend) {
            it.read(session, "city?limit=20"); it.read(session, "city?limit=20&cursor=b-1")
        }
        val city = cache.replay(session, account, "inventory-city") { InventoryRepository(it).city(session) }
        assertEquals(mapOf("b-1" to 1L), city.buildings)
        assertTrue(runCatching { cache.replay(session, account, "inventory-city") { it.read(session, "city?limit=100&cursor=b-2") } }.isFailure)
    }

    /** Synthetic HTTP response generated by AppBackend TestIntegrationInventoryPayloadProbe. */
    @Test fun backendDisplayFixtureRetainsAcceptedGeometry() {
        val body = requireNotNull(javaClass.getResource("/inventory-display-backend.json")).readText()
        val page = InventoryReads.inventory(body)
        assertEquals(20, page.buildings.size)
        assertEquals(page.buildings.last().id, page.next)
        page.buildings.forEach { building ->
            val accepted = requireNotNull(building.geometry)
            assertEquals(10, accepted.geometry.centers.size)
            assertEquals(List(10) { 0L }, accepted.geometry.centers)
            assertEquals(10L, building.floors)
        }
        assertEquals(page, InventoryReads.inventory(InventoryReads.displayBody(body)))
    }

    @Test fun legacyBundleAliasesOnlyExactDisplayQuery() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(snapshot), RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        var bytes: ByteArray? = null
        val cache = ReadOnlyGameCache { object : JournalFile {
            override fun read() = bytes
            override fun replace(value: ByteArray) { bytes = value }
        } }
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ -> GameplayResponse(200, if (path == "sync") snapshot else page(false)) })
        cache.capture(session, account, "inventory-", backend) { reader -> InventoryReads.inventory(reader.read(session, "inventory?limit=20")) }
        val result = cache.replay(session, account, "inventory-") { InventoryRepository(it).page(session) }
        assertEquals(20, result.buildings.size)
        assertTrue(runCatching { cache.replay(session, account, "inventory-") { InventoryRepository(it).page(session, "b-19") } }.isFailure)
    }
    @Test fun inlineMembershipIsStrictAndDoesNotRequestCity() = runBlocking {
        val base = Json.parseToJsonElement(page(false)).jsonObject.getValue("buildings").jsonArray.first().jsonObject
        val session = BillingSession("alice", "epoch")
        for (placed in listOf(true, false)) {
            val body = JsonObject(base + ("in_city" to JsonPrimitive(placed))).toString()
            var calls = 0
            val repo = InventoryRepository(GameplayReads { _, path ->
                assertEquals("buildings/b-0?view=display", path); calls++; body
            })
            assertEquals(placed, repo.building(session, "b-0").inCity)
            assertEquals(1, calls)
        }
        assertNull(InventoryReads.building(base).inCity)
        for (invalid in listOf(JsonPrimitive("true"), JsonNull, JsonPrimitive(1))) {
            assertTrue(runCatching { InventoryReads.building(JsonObject(base + ("in_city" to invalid))) }.isFailure)
        }
    }

    @Test fun maximumEscapingPageSurvivesRealAtomicFileAndLegacyJournalStaysBounded() = runBlocking {
        val dir = java.nio.file.Files.createTempDirectory("inventory-cache-budget").toFile()
        try {
            val fs = IoJournalFileSystem { }
            fun files(key: String): JournalFile = DurableJournalFile(dir, key, fs, ReadOnlyGameCache.MaxBytes)
            val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
            val session = sessions.current()!!
            val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(snapshot), RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
            // A valid response body just below the 1 MiB transport ceiling; escaping doubles it on disk.
            val base = Json.parseToJsonElement(page(false)).jsonObject.getValue("buildings").jsonArray.first().jsonObject
            val wire = JsonObject((base - "snapshot") + ("padding" to JsonPrimitive("\\".repeat(520000)))).toString()
            assertTrue(wire.toByteArray().size < 1_048_576)
            val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
                GameplayResponse(200, if(path == "sync") snapshot else wire)
            })
            val cache = ReadOnlyGameCache(::files)
            cache.capture(session, account, "large", backend) { it.read(session, "buildings/b-0?view=display") }
            assertEquals(wire, ReadOnlyGameCache(::files).replay(session, account, "large") {
                it.read(session, "buildings/b-0?view=display")
            })
            val file = dir.listFiles()!!.single()
            assertTrue(file.length() > 1_048_576)
            val committed = file.readBytes()
            assertTrue(runCatching { files(file.name).replace(ByteArray(ReadOnlyGameCache.MaxBytes + 1)) }.isFailure)
            assertArrayEquals(committed, file.readBytes())
            assertTrue(runCatching { DurableJournalFile(dir, "command", fs).replace(ByteArray(DurableJournalFile.MaxBytes + 1)) }.isFailure)
        } finally { dir.deleteRecursively() }
    }

    // Retains the previous 3001-member boundary regression, now expected to persist/replay.
    @Test fun cityAboveLegacyJournalLimitReplaysWithIndependentCacheBudget() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(snapshot), RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        val files = mutableMapOf<String, ByteArray>()
        val cache = ReadOnlyGameCache { key -> object : JournalFile {
            override fun read() = files[key]
            override fun replace(bytes: ByteArray) { files[key] = bytes.clone() }
        } }
        var requests = 0
        var network = true
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            check(network); requests++
            val wire = if (path == "sync") snapshot else {
                val start = path.substringAfter("&cursor=b-", "0").toInt() + 1
                val end = minOf(start + 99, 3001)
                buildJsonObject {
                    put("rules_version", "wobbrik-v1"); put("revision", 1); put("population", "120040")
                    putJsonArray("buildings") { for (i in start..end) add(buildJsonObject {
                        put("building_id", "b-$i"); put("building_revision", 1)
                    }) }
                    if (end < 3001) put("next_cursor", "b-$end")
                }.toString()
            }
            assertTrue(wire.toByteArray().size <= 65_536)
            GameplayResponse(200, wire)
        })
        var verified = false
        val live = cache.capture(session, account, "inventory-city", backend,
            onVerified = { verified = true }) { InventoryRepository(it).city(session) }
        assertTrue(verified); assertEquals(3001, live.buildings.size); assertEquals(32, requests)
        assertTrue(files.values.single().size > DurableJournalFile.MaxBytes)
        network = false
        assertEquals(live, cache.replay(session, account, "inventory-city") { InventoryRepository(it).city(session) })
        assertEquals(32, requests)
    }
}
