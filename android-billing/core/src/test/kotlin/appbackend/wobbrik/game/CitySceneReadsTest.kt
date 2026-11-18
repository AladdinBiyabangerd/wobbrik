package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class CitySceneReadsTest {
    private val position = """{"x_ticks":"-1250","y_ticks":"220000","quarter_turns":3}"""
    private val building = """{"id":"b-1","revision":1,"floors":10,"residents":40,"base_value":20,"max_price":24,"parts":{"wall":"w","window":"v","entrance":"e","roof":"r","lighting":"l"},"traded":false}"""
    private fun city(revision: Int = 1, entries: String = "", next: String = "") =
        """{"rules_version":"wobbrik-v1","geometry_version":"city-geometry-v1","placement_enabled":true,"revision":$revision,"population":"40","parcels":[{"kind":"land","id":"base","position":{"x_ticks":"0","y_ticks":"0","quarter_turns":0}}],"buildings":[$entries]$next}"""
    private fun member(placed: Boolean = true) =
        """{"building_id":"b-1","building_revision":1${if (placed) ",\"position\":$position" else ""}}"""
    @Test fun exactPositionAndLegacyAbsenceArePreserved() {
        assertEquals(CityPosition(-1250, 220000, 3), CitySceneReads.page(city(entries = member())).positions["b-1"])
        assertNull(CitySceneReads.page(city(entries = member(false))).positions["b-1"])
    }
    @Test fun malformedCoordinatesAndUnknownVersionsFailClosed() {
        listOf(position.replace("-1250", "-0"), position.replace("-1250", "1000000001"),
            position.replace("\"-1250\"", "-1250"), position.replace(":3", ":4")).forEach { invalid ->
            rejected { CitySceneReads.position(Json.parseToJsonElement(invalid)) }
        }
        rejected { CitySceneReads.page(city().replace("city-geometry-v1", "future")) }
        rejected { CitySceneReads.page(city().replace("\"x_ticks\":\"0\"", "\"x_ticks\":\"1\"")) }
    }
    @Test fun loadsPlacedCopiesAndMatchingBuildingWithoutMutation() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        var cityCalls = 0
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
            require(allowedReadPath(path)) { "Production transport rejects $path" }
            assertNull(body)
            GameplayResponse(200, when {
                path.startsWith("city?") -> { cityCalls++; city(entries = member()) }
                path == "buildings/b-1?view=display" -> building
                else -> """{"copies":[{"id":"d-1","item_id":"tree_1","position":$position},{"id":"d-2","item_id":"tree_1"}]}"""
            })
        })
        val result = CitySceneRepository(backend).load(requireNotNull(sessions.current()))
        assertEquals(1, result.buildings.size); assertEquals(1, result.decorations.size)
        assertEquals("d-1", result.decorations.single().copy.id); assertEquals(2, cityCalls)
    }
    @Test fun decorationReadPathsAreBoundedIncludingPagination() {
        assertTrue(allowedReadPath("decorations?limit=20"))
        assertTrue(allowedReadPath("decorations?limit=20&cursor=d-1"))
        listOf("decorations", "decorations?limit=100", "decorations?limit=20&uid=other",
            "decorations?limit=20&cursor=../sync", "decorations?limit=20&cursor=d-1&extra=1",
            "decorations/buy").forEach { assertFalse(it, allowedReadPath(it)) }
    }
    @Test fun writeDuringDecorationReadDiscardsScene() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        var revision = 1
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            GameplayResponse(200, if (path.startsWith("city?")) city(revision) else {
                revision = 2; """{"copies":[]}"""
            })
        })
        rejectedSuspend { CitySceneRepository(backend).load(requireNotNull(sessions.current())) }
    }
    @Test fun changedBuildingDiscardsScene() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            GameplayResponse(200, if (path.startsWith("city?")) city(entries = member()) else building.replace("\"revision\":1", "\"revision\":2"))
        })
        rejectedSuspend { CitySceneRepository(backend).load(requireNotNull(sessions.current())) }
    }
    @Test fun mixedCityPagesAreRejected() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        var calls = 0
        val backend = GameplayHttpBackend(sessions, GameplayTransport { _, _, _ ->
            GameplayResponse(200, if (++calls == 1) city(entries = member(), next = """, "next_cursor":"b-1"""") else city(2))
        })
        rejectedSuspend { CitySceneRepository(backend).load(requireNotNull(sessions.current())) }
    }
    @Test fun emptyCityAndEmptyDecorationInventoryAreValid() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            GameplayResponse(200, if (path.startsWith("city?")) city().replace("\"population\":\"40\"", "\"population\":\"0\"")
                else """{"copies":[]}""")
        })
        val scene = CitySceneRepository(backend).load(requireNotNull(sessions.current()))
        assertTrue(scene.buildings.isEmpty())
        assertTrue(scene.decorations.isEmpty())
        assertEquals(1, scene.parcels.size)
    }
    @Test fun missingServerGeometryContractIsRejectedRatherThanInventingAnIsland() {
        assertThrows(NoSuchElementException::class.java) { CitySceneReads.page(city().replace("\"geometry_version\":\"city-geometry-v1\",", "")) }
    }
    @Test fun newLoginOfSameUidRejectsLateResponse() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val session = requireNotNull(sessions.current())
        val backend = GameplayHttpBackend(sessions, GameplayTransport { _, _, _ ->
            sessions.onAuthChanged("alice"); GameplayResponse(200, city())
        })
        try { CitySceneRepository(backend).load(session); fail("old epoch accepted") } catch (_: AccountChanged) { }
    }
    @Test fun twelveBuildingsBatchToThreeReadsWithSameSceneAsLegacy() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        suspend fun load(mode: String): Pair<CityScene, List<String>> {
            val calls = mutableListOf<String>()
            val started = System.nanoTime()
            var bytes = 0
            val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
                calls.add(path)
                kotlinx.coroutines.delay(40)
                if (mode == "strict" && path.endsWith("&include=details"))
                    return@GameplayTransport GameplayResponse(400, """{"error":"invalid_request"}""")
                val response = when {
                    path.startsWith("city?") -> city(entries = (1..12).joinToString(",") { n ->
                        val id = "b-${n.toString().padStart(2, '0')}"
                        val entry = member().replace("b-1", id)
                        if (mode == "batch" && path.endsWith("&include=details"))
                            entry.dropLast(1) + ",\"detail\":" + building.replace("b-1", id) + "}"
                        else entry
                    })
                    path.startsWith("buildings/") -> building.replace("b-1", path.substringAfter("/").substringBefore("?"))
                    else -> """{"copies":[]}"""
                }
                bytes += response.toByteArray(Charsets.UTF_8).size
                GameplayResponse(200, response)
            })
            val result = CitySceneRepository(backend).load(session)
            println("CITY-ANDROID mode=$mode requests=${calls.size} successJSONBytes=$bytes elapsedMs=${(System.nanoTime()-started)/1_000_000}")
            return result to calls
        }
        val (batch, reads) = load("batch")
        val (legacy, legacyReads) = load("legacy")
        val (strict, strictReads) = load("strict")
        assertEquals(legacy, batch); assertEquals(legacy, strict)
        assertEquals(3, reads.size); assertEquals(15, legacyReads.size); assertEquals(16, strictReads.size)
        assertEquals("city?limit=20", reads.last())
        assertEquals(1, strictReads.count { it.endsWith("&include=details") })
    }
    @Test fun invalidEmbeddedDetailFailsClosedAndMissingDetailFallsBack() {
        val good = member().dropLast(1) + ",\"detail\":$building}"
        rejected { CitySceneReads.page(city(entries = good.replace("\"id\":\"b-1\"", "\"id\":\"other\""))) }
        rejected { CitySceneReads.page(city(entries = good.replace("\"revision\":1", "\"revision\":2"))) }
        assertEquals(InventoryReads.building(building), CitySceneReads.page(city(entries = good)).details["b-1"])
        assertTrue(CitySceneReads.page(city(entries = member())).details.isEmpty())
        assertTrue(allowedReadPath("city?limit=20&cursor=b-1&include=details"))
        listOf("city?limit=100&include=details", "city?limit=20&include=details&uid=bob",
            "city?limit=20&cursor=../sync&include=details").forEach { assertFalse(allowedReadPath(it)) }
    }
    @Test fun paginatedBatchAndPartialFallbackKeepGeometryAndRevisionBracket() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val geometry = """{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[0,0,0,0,0,0,0,0,0,0]},"attempt_id":"a","sequence":1,"operation_id":"cp","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}"""
        val detail = building.dropLast(1) + ",\"accepted_geometry\":$geometry}"
        val first = city(entries = member().dropLast(1) + ",\"detail\":$detail}", next = """, "next_cursor":"b-1"""")
        val calls = mutableListOf<String>()
        var race = false
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            calls.add(path)
            GameplayResponse(200, when {
                path.startsWith("city?") && path.contains("cursor=") -> city(entries = member().replace("b-1", "b-2"))
                path.endsWith("include=details") -> first
                path.startsWith("city?") -> if (race) first.replace("\"revision\":1", "\"revision\":2") else first
                path == "buildings/b-2?view=display" -> detail.replace("b-1", "b-2")
                else -> """{"copies":[]}"""
            })
        })
        val result = CitySceneRepository(backend).load(sessions.current()!!)
        assertEquals(5, calls.size)
        assertEquals(listOf("buildings/b-2?view=display"), calls.filter { it.startsWith("buildings/") })
        assertEquals(2, result.buildings.size)
        assertEquals(List(10) { 0L }, result.buildings.first().building.geometry!!.geometry.centers)
        race = true
        rejectedSuspend { CitySceneRepository(backend).load(sessions.current()!!) }
    }
    @Test fun cityCaptureReplaysNewStrictLegacyAndPreBatchBundlesWithoutNetwork() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val snapshot = """{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(snapshot), RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        for (mode in listOf("batch", "strict", "old-cache")) {
            val rows = mutableMapOf<String, ByteArray>()
            val cache = ReadOnlyGameCache { key -> object : JournalFile {
                override fun read() = rows[key]
                override fun replace(bytes: ByteArray) { rows[key] = bytes.clone() }
            } }
            var reads = 0
            val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
                reads++
                if (mode != "batch" && path.endsWith("include=details")) return@GameplayTransport GameplayResponse(400, """{"error":"invalid_request"}""")
                GameplayResponse(200, when {
                    path == "sync" -> snapshot
                    path.startsWith("city?") -> {
                        val entry = if (mode == "batch" && path.endsWith("include=details")) member().dropLast(1) + ",\"detail\":$building}" else member()
                        city(entries = entry).dropLast(1) + ",\"server_time\":\"$reads\"}"
                    }
                    path.startsWith("buildings/") -> building
                    else -> """{"copies":[]}"""
                })
            })
            val value = cache.capture(session, account, "city", backend) { reader ->
                val legacy = if (mode == "old-cache") GameplayReads { login, path -> reader.read(login, path.removeSuffix("&include=details")) } else reader
                CitySceneRepository(legacy).load(session)
            }
            val before = reads
            assertEquals(value, cache.replay(session, account, "city") { CitySceneRepository(it).load(session) })
            assertEquals(before, reads)
            assertTrue(runCatching { cache.replay(session, account.copy(snapshot = RecoveryDtos.snapshot(snapshot.replace("\"revision\":1", "\"revision\":2"))), "city") { CitySceneRepository(it).load(session) } }.isFailure)
            val bob = appbackend.wobbrik.billing.BillingSession("bob", "epoch")
            assertTrue(runCatching { cache.replay(bob, account, "city") { CitySceneRepository(it).load(bob) } }.isFailure)
        }
    }
    @Test fun transientAndAuthorizationFailuresNeverTriggerLegacyRetry() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        for (status in listOf(401, 403, 429, 500)) {
            var calls = 0
            val backend = GameplayHttpBackend(sessions, GameplayTransport { _, _, _ ->
                calls++; GameplayResponse(status, """{"error":"invalid_request"}""")
            })
            try { CitySceneRepository(backend).load(sessions.current()!!); fail("failure ignored") }
            catch (failure: GameplayHttpError) { assertEquals(status, failure.status) }
            assertEquals(1, calls)
        }
    }
    private fun rejected(block: () -> Unit) {
        try { block(); fail("invalid scene accepted") } catch (_: IllegalArgumentException) { }
    }
    private suspend fun rejectedSuspend(block: suspend () -> Unit) {
        try { block(); fail("mixed scene accepted") } catch (_: IllegalArgumentException) { }
    }
}
