package com.aladdin.wobbrik

import appbackend.wobbrik.game.CityParcel
import appbackend.wobbrik.game.CityPosition
import org.junit.Assert.*
import org.junit.Test

class CityTerrainGeometryTest {
    @Test fun landAreasAndLNotchesMatchServerContract() {
        val expected = mapOf("base" to 96800.0, "land_1" to 48400.0, "land_2" to 60500.0,
            "land_3" to 72600.0, "land_4" to 84700.0, "land_5" to 96800.0)
        expected.forEach { (id, area) -> assertEquals(area, CityTerrainGeometry.land(id).sumOf {
            (it.right-it.left)*(it.top-it.bottom)
        }, 0.0) }
        assertFalse(CityTerrainGeometry.land("land_2").any { 300.0 > it.left && 300.0 < it.right && 200.0 > it.bottom && 200.0 < it.top })
        assertFalse(CityTerrainGeometry.land("land_4").any { 400.0 > it.left && 400.0 < it.right && 200.0 > it.bottom && 200.0 < it.top })
        assertTrue(CityTerrainGeometry.land("unknown").isEmpty())
    }
    @Test fun parcelOriginAndAllQuarterTurnsRemainExactAtLargeSignedTicks() {
        val expected = listOf(20.0 to 10.0, -10.0 to 20.0, -20.0 to -10.0, 10.0 to -20.0)
        expected.forEachIndexed { turn, (x,y) ->
            val p = CityTerrainGeometry.world(CityPosition(999999999,-999999999,turn),20.0,10.0)
            assertEquals(999999.999+x,p.x,0.00000001)
            assertEquals(-999999.999+y,p.y,0.00000001)
        }
    }
    @Test fun emptyCityStillFitsActualParcelAndCliffWithoutBuildings() {
        val contours = CityTerrainGeometry.contours(listOf(CityParcel("base",CityPosition(0,0,0)),
            CityParcel("land_2",CityPosition(440000,0,0))))
        assertEquals(3,contours.size)
        val bounds = requireNotNull(CityTerrainGeometry.bounds(contours))
        val view = requireNotNull(cityViewport(emptyList(),360.0,300.0,extraBounds=listOf(bounds)))
        contours.flatten().forEach { p ->
            assertTrue(p.x*view.scale+view.x in -0.000001..360.000001)
            assertTrue(p.y*view.scale+view.y in -0.000001..300.000001)
        }
        assertTrue(bounds.bottom*view.scale+view.y <= 300.000001)
        assertNull(CityTerrainGeometry.bounds(emptyList()))
    }
    @Test fun catalogDecorDimensionsAndUnknownArtAreExplicit() {
        assertEquals(30.0 to 30.0,CityTerrainGeometry.decorationSize("decor_tree_1"))
        assertEquals(100.0 to 100.0,CityTerrainGeometry.decorationSize("decor_road_2"))
        assertEquals(210.0 to 30.0,CityTerrainGeometry.decorationSize("decor_road_3"))
        assertNull(CityTerrainGeometry.decorationSize("unknown"))
    }
}
