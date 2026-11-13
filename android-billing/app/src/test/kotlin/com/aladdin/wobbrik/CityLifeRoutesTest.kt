package com.aladdin.wobbrik

import appbackend.wobbrik.game.*
import org.junit.Assert.*
import org.junit.Test

class CityLifeRoutesTest {
    private fun building(x: Long = 100000, y: Long = 100000) = CitySceneBuilding(
        OwnedBuilding("b",1,2,8,4,5,FinishCosmetics.categories.associateWith { "base_1_$it" },false,
            AcceptedCityGeometry(CityFloorGeometry(listOf(0,20000)),"a",1,"o","wobbrik-v1","wobbrik-structural-v1")),
        CityPosition(x,y,0))
    private fun road(id: String, x: Long, y: Long, turn: Int = 0) =
        CitySceneDecoration(DecorationCopy(id,id),CityPosition(x,y,turn))
    private fun scene(decor: List<CitySceneDecoration> = emptyList()) = CityScene(
        CityMembership(1,8.toBigInteger(),mapOf("b" to 1),null),
        listOf(CityParcel("base",CityPosition(0,0,0))),listOf(building()),decor)
    @Test fun deterministicBoundedPathsDoNotChangeScene() {
        val data = scene(listOf(road("decor_road_3",300000,180000)))
        val routes = CityLifeRoutes.build(data)
        assertTrue(routes.any { it.kind == CityLifeRoutes.Kind.Resident })
        assertTrue(routes.any { it.kind == CityLifeRoutes.Kind.Car })
        assertTrue(routes.count { it.kind == CityLifeRoutes.Kind.Resident } <= 6)
        assertTrue(routes.count { it.kind == CityLifeRoutes.Kind.Car } <= 2)
        assertEquals(routes,CityLifeRoutes.build(data.copy(parcels=data.parcels.reversed(),decorations=data.decorations.reversed())))
        assertEquals(8.toBigInteger(),data.membership.population)
    }
    @Test fun vehiclesNeedActualRoadAndStayOutOfBuildingsAndParcelEdges() {
        assertFalse(CityLifeRoutes.build(scene()).any { it.kind == CityLifeRoutes.Kind.Car })
        listOf(road("decor_road_1",100000,100000),road("decor_road_3",10000,180000)).forEach {
            assertFalse(CityLifeRoutes.build(scene(listOf(it))).any { r -> r.kind == CityLifeRoutes.Kind.Car })
        }
    }
    @Test fun lRoadUsesItsTwoArmsForAllRotations() {
        (0..3).forEach { turn ->
            val routes = CityLifeRoutes.build(scene(listOf(road("decor_road_2",300000,100000,turn))))
                .filter { it.kind == CityLifeRoutes.Kind.Car }
            assertEquals(2,routes.size)
            routes.forEach { route -> (0..100).forEach { tick ->
                val p = route.position(tick/3.0); val dx=p.x-300; val dy=p.y-100
                val (x,y) = when(turn) { 0 -> dx to dy; 1 -> dy to -dx; 2 -> -dx to -dy; else -> -dy to dx }
                assertTrue((x in -50.0..50.0 && y in -50.0..-20.0) || (x in -50.0..-20.0 && y in -20.0..50.0))
            } }
        }
    }
    @Test fun unknownGeometryAndNoPopulationDoNotInventResidents() {
        val data=scene()
        assertTrue(CityLifeRoutes.build(data.copy(buildings=listOf(building().copy(position=null)))).isEmpty())
        assertTrue(CityLifeRoutes.build(data.copy(buildings=listOf(building().let { it.copy(building=it.building.copy(geometry=null)) }))).isEmpty())
        assertFalse(CityLifeRoutes.build(data.copy(membership=data.membership.copy(population=0.toBigInteger())))
            .any { it.kind == CityLifeRoutes.Kind.Resident })
    }
    @Test fun rotatedLParcelNeverCreatesWalksAcrossNotch() {
        val data=scene().copy(parcels=listOf(CityParcel("land_2",CityPosition(500000,500000,2))),buildings=listOf(building(450000,450000)))
        val routes=CityLifeRoutes.build(data)
        assertTrue(routes.isNotEmpty())
        routes.forEach { route -> (0..300).forEach { tick ->
            val p=route.position(tick.toDouble()); val x=500-p.x; val y=500-p.y
            assertTrue((x in 0.0..330.0 && y in 0.0..110.0) || (x in 0.0..220.0 && y in 110.0..220.0))
        } }
    }
    @Test fun carsCannotCrossPlacedDecorOrGroundGaps() {
        val road=road("decor_road_3",300000,180000)
        val tree=CitySceneDecoration(DecorationCopy("tree","decor_tree_1"),CityPosition(300000,180000,0))
        assertFalse(CityLifeRoutes.build(scene(listOf(road,tree))).any { it.kind==CityLifeRoutes.Kind.Car })
        val gap=scene(listOf(road("decor_road_3",440000,100000))).copy(parcels=listOf(
            CityParcel("base",CityPosition(0,0,0)),CityParcel("land_1",CityPosition(450000,0,0))))
        assertFalse(CityLifeRoutes.build(gap).any { it.kind==CityLifeRoutes.Kind.Car })
    }
    @Test fun leaningFootprintObstructsVehiclesAfterEveryQuarterTurn() {
        (0..3).forEach { turn ->
            val b=building(220000,110000).let { it.copy(position=it.position!!.copy(quarterTurns=turn),
                building=it.building.copy(geometry=it.building.geometry!!.copy(geometry=CityFloorGeometry(listOf(0,80000))))) }
            val p=CityTerrainGeometry.world(b.position!!,80.0,0.0)
            val road=road("decor_road_1",(p.x*1000).toLong(),(p.y*1000).toLong(),turn)
            assertFalse(CityLifeRoutes.build(scene(listOf(road)).copy(buildings=listOf(b))).any { it.kind==CityLifeRoutes.Kind.Car })
        }
    }
    @Test fun motionHasContinuousTurnaroundAndNoAccumulatedIntegrationError() {
        val a=CityBuildingProjection.Point(0.0,0.0); val b=CityBuildingProjection.Point(90.0,0.0)
        val route=CityLifeRoutes.Route("test",CityLifeRoutes.Kind.Resident,a,b)
        assertEquals(a,route.position(0.0)); assertEquals(b,route.position(10.0)); assertEquals(a,route.position(20.0))
        assertEquals(route.position(3.5).x,route.position(200003.5).x,0.00000001)
        assertTrue(kotlin.math.abs(route.position(9.999).x-route.position(10.001).x)<0.0001)
    }
}
