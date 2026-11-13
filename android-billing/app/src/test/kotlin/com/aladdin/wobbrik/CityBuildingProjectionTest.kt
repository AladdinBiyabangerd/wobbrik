package com.aladdin.wobbrik

import appbackend.wobbrik.game.*
import org.junit.Assert.*
import org.junit.Test

class CityBuildingProjectionTest {
    private fun source(id: String = "b", position: CityPosition? = CityPosition(100000, 200000, 0),
        centers: List<Long>? = List(10) { it * 5000L }): CitySceneBuilding = CitySceneBuilding(
        OwnedBuilding(id, 1, 10, 40, 20, 24, emptyMap(), false,
            centers?.let { AcceptedCityGeometry(CityFloorGeometry(it), "attempt", 1, "op", "wobbrik-v1", "wobbrik-structural-v1") }), position)

    @Test fun rotatesOffsetsAroundFoundationWithoutRecentering() {
        val expected = listOf(-80.0 to -160.0, -120.0 to -160.0, -120.0 to -140.0, -80.0 to -140.0)
        expected.forEachIndexed { turn, (x, y) ->
            val point = CityBuildingProjection.floor(CityPosition(100000, 200000, turn), 20000, 0)
            assertEquals(x, point.x, 0.0); assertEquals(y, point.y, 0.0)
        }
        assertEquals(CityBuildingProjection.Point(-100.0, -150.0), CityBuildingProjection.floor(CityPosition(100000, 200000, 3), 0, 0))
    }
    @Test fun retainsEveryFloorAndSmallSignedOffsetAtLargeCoordinates() {
        val points = requireNotNull(CityBuildingProjection.building(source(position = CityPosition(1000000000, -1000000000, 0),
            centers = listOf(-1L, 1L) + List(8) { 2000L }))).floors
        assertEquals(10, points.size)
        assertEquals(1999999.999, points[0].x, 0.0000001)
        assertEquals(2000000.001, points[1].x, 0.0000001)
        assertEquals(-CityBuildingProjection.ArtPitch - 0.0005, points[1].y, 0.0000001)
    }
    @Test fun facadeWidthAndFrontAnchorMatchProjectedServerFootprint() {
        val position = CityPosition(100000,200000,0)
        val center = CityBuildingProjection.floor(position,0,0)
        val corners = listOf(-50.0 to -50.0,50.0 to -50.0,50.0 to 50.0,-50.0 to 50.0)
            .map { (x,y) -> CityTerrainGeometry.point(position,x,y) }
        assertEquals(corners.minOf { it.x },center.x-CityBuildingProjection.ArtWidth/2,0.0)
        assertEquals(corners.maxOf { it.x },center.x+CityBuildingProjection.ArtWidth/2,0.0)
        assertEquals(corners.maxOf { it.y },center.y+CityBuildingProjection.FoundationFront,0.0)
    }
    @Test fun missingPositionOrGeometryNeverBecomesAnOriginBuilding() {
        assertNull(CityBuildingProjection.building(source(position = null)))
        assertNull(CityBuildingProjection.building(source(centers = null)))
        assertNull(CityBuildingProjection.building(source(centers = listOf(0))))
    }
    @Test fun painterOrderFollowsWorldDepthThenStableId() {
        val near = requireNotNull(CityBuildingProjection.building(source("near", CityPosition(0, 0, 0))))
        val far = requireNotNull(CityBuildingProjection.building(source("far", CityPosition(200000, 200000, 0))))
        val tied = requireNotNull(CityBuildingProjection.building(source("a", CityPosition(0, 0, 0))))
        assertEquals(listOf("far", "a", "near"), CityBuildingProjection.ordered(listOf(near, tied, far)).map { it.source.building.id })
    }
    @Test fun fitContainsCompleteLeaningTowerAndRoofInBothPortraitSizes() {
        val buildings = listOf(requireNotNull(CityBuildingProjection.building(source())))
        for ((width, height) in listOf(360.0 to 250.0, 390.0 to 500.0)) {
            val view = requireNotNull(cityViewport(buildings, width, height))
            val b = buildings.single().bounds
            assertTrue(b.left * view.scale + view.x >= -0.000001)
            assertTrue(b.right * view.scale + view.x <= width + 0.000001)
            assertTrue(b.top * view.scale + view.y >= -0.000001)
            assertTrue(b.bottom * view.scale + view.y <= height + 0.000001)
        }
        assertNull(cityViewport(emptyList(), 360.0, 640.0))
    }
}
