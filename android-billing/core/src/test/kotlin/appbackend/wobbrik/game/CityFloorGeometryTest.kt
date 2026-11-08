package appbackend.wobbrik.game

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class CityFloorGeometryTest {
    @Test fun typedWireAndLegacyJournalRoundTrip() {
        val geometry = CityFloorGeometry(listOf(-1250, 500))
        val typed = GameplayWire.checkpoint("cp", 1, "attempt", 1, 2, 0, 0, "{}", geometry)
        GameplayWire.validate(typed)
        val request = Json.parseToJsonElement(typed.envelope).jsonObject.getValue("request").jsonObject
        assertEquals(geometry, CityFloorGeometry.parse(request.getValue("geometry").jsonObject))
        GameplayWire.validate(GameplayWire.start("typed", 0, CityFloorGeometry.VERSION))
        val legacy = GameplayWire.start("legacy", 0)
        GameplayWire.validate(legacy)
        assertFalse(legacy.envelope.contains("geometry_version"))
        GameplayWire.validate(GameplayWire.checkpoint("old", 1, "attempt", 1, 1, 0, 0, "{}"))
    }
    @Test fun malformedTicksAndUnsupportedGeometryFailClosed() {
        listOf("""{"version":"city-geometry-v1","centers_x_ticks":[0.5]}""",
            """{"version":"city-geometry-v1","centers_x_ticks":["0"]}""",
            """{"version":"future","centers_x_ticks":[0]}""",
            """{"version":"city-geometry-v1","centers_x_ticks":[0],"operation_id":"forged"}""").forEach {
            assertThrows(IllegalArgumentException::class.java) { CityFloorGeometry.parse(Json.parseToJsonElement(it).jsonObject) }
        }
        listOf(emptyList(), listOf(100000L), listOf(Long.MAX_VALUE)).forEach {
            assertThrows(IllegalArgumentException::class.java) { CityFloorGeometry(it).json() }
        }
    }
}
