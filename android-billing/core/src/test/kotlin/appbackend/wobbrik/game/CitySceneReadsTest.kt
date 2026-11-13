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
                path == "buildings/b-1" -> building
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
    private fun rejected(block: () -> Unit) {
        try { block(); fail("invalid scene accepted") } catch (_: IllegalArgumentException) { }
    }
    private suspend fun rejectedSuspend(block: suspend () -> Unit) {
        try { block(); fail("mixed scene accepted") } catch (_: IllegalArgumentException) { }
    }
}
