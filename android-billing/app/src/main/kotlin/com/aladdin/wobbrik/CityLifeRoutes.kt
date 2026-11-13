package com.aladdin.wobbrik

import appbackend.wobbrik.game.CityPosition
import appbackend.wobbrik.game.CityScene
import kotlin.math.abs

/** Presentation-only ambient paths from the accepted scene. Never placement validation,
 * a population simulation, persistence, or an economy input. Coordinates are world units. */
internal object CityLifeRoutes {
    enum class Kind { Resident, Car }
    data class Route(val id: String, val kind: Kind, val from: CityBuildingProjection.Point,
        val to: CityBuildingProjection.Point) {
        /** Ping-pong motion: position is continuous at the turnaround; no frame integration drift. */
        fun position(seconds: Double): CityBuildingProjection.Point {
            val length = abs(to.x - from.x) + abs(to.y - from.y)
            if (length == 0.0) return from
            val speed = if (kind == Kind.Resident) ResidentSpeed else CarSpeed
            val phase = ((seconds.coerceAtLeast(0.0) * speed / length) % 2.0)
            val t = if (phase <= 1.0) phase else 2.0 - phase
            return CityBuildingProjection.Point(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t)
        }
    }

    // Ambient motion parameters, not UI spacing or server gameplay rules.
    private const val ResidentSpeed = 9.0
    private const val CarSpeed = 22.0
    private const val ResidentClearance = 4.0
    private const val CarClearance = 8.0
    private const val MaxResidents = 6
    private const val MaxCars = 2

    private data class Rect(val left: Double, val bottom: Double, val right: Double, val top: Double) {
        fun overlaps(b: Rect) = left < b.right && right > b.left && bottom < b.top && top > b.bottom
        fun subtract(b: Rect): List<Rect> {
            if (!overlaps(b)) return listOf(this)
            val l = maxOf(left, b.left); val r = minOf(right, b.right)
            val d = maxOf(bottom, b.bottom); val u = minOf(top, b.top)
            return listOf(Rect(left,bottom,l,top), Rect(r,bottom,right,top),
                Rect(l,bottom,r,d), Rect(l,u,r,top)).filter { it.left < it.right && it.bottom < it.top }
        }
    }
    private fun rect(position: CityPosition, l: Double, b: Double, r: Double, t: Double): Rect {
        val points = listOf(l to b, r to b, r to t, l to t).map { (x,y) -> CityTerrainGeometry.world(position,x,y) }
        return Rect(points.minOf { it.x },points.minOf { it.y },points.maxOf { it.x },points.maxOf { it.y })
    }
    private fun covered(area: Rect, union: List<Rect>): Boolean {
        var remaining = listOf(area)
        for (piece in union) {
            remaining = remaining.flatMap { it.subtract(piece) }
            if (remaining.isEmpty()) return true
        }
        return false
    }
    private fun corridor(route: Route): Rect {
        val c = if (route.kind == Kind.Car) CarClearance else ResidentClearance
        return Rect(minOf(route.from.x,route.to.x)-c,minOf(route.from.y,route.to.y)-c,
            maxOf(route.from.x,route.to.x)+c,maxOf(route.from.y,route.to.y)+c)
    }

    fun build(scene: CityScene): List<Route> {
        // Legacy/unknown geometry is not empty ground. Fail closed rather than walk through it.
        if (scene.buildings.any { it.position == null || it.building.geometry == null }) return emptyList()
        val land = scene.parcels.sortedBy { it.id }.flatMap { parcel ->
            CityTerrainGeometry.land(parcel.id).map { rect(parcel.position,it.left,it.bottom,it.right,it.top) }
        }
        val buildings = scene.buildings.map { entry ->
            val centers = requireNotNull(entry.building.geometry).geometry.centers
            if (centers.isEmpty()) return emptyList()
            rect(requireNotNull(entry.position),centers.min()/1000.0-50,-50.0,centers.max()/1000.0+50,50.0)
        }
        val obstacles = buildings.toMutableList()
        val roads = mutableListOf<Rect>()
        val cars = mutableListOf<Route>()
        scene.decorations.sortedBy { it.copy.id }.forEach { decor ->
            val id = decor.copy.itemId
            val (w,h) = CityTerrainGeometry.decorationSize(id) ?: return emptyList()
            if (!id.startsWith("decor_road_")) {
                obstacles += rect(decor.position,-w/2,-h/2,w/2,h/2)
            } else {
                // road_2 is an L, not its bounding square (AppBackend city_footprints.go).
                val shapes = if (id == "decor_road_2") listOf(
                    Rect(-50.0,-50.0,50.0,-20.0),Rect(-50.0,-20.0,-20.0,50.0))
                else listOf(Rect(-w/2,-h/2,w/2,h/2))
                shapes.forEachIndexed { index, shape ->
                    roads += rect(decor.position,shape.left,shape.bottom,shape.right,shape.top)
                    val c = CarClearance
                    val horizontal = shape.right-shape.left >= shape.top-shape.bottom
                    val x = (shape.left+shape.right)/2; val y = (shape.bottom+shape.top)/2
                    val a = CityTerrainGeometry.world(decor.position,if (horizontal) shape.left+c else x,if (horizontal) y else shape.bottom+c)
                    val b = CityTerrainGeometry.world(decor.position,if (horizontal) shape.right-c else x,if (horizontal) y else shape.top-c)
                    cars += Route("car:${decor.copy.id}:$index",Kind.Car,a,b)
                }
            }
        }
        fun safe(route: Route, ground: List<Rect>): Boolean {
            val area = corridor(route)
            return route.from != route.to && covered(area,land) && covered(area,ground) && obstacles.none { it.overlaps(area) }
        }
        val residents = if (scene.membership.population.signum() <= 0 || buildings.isEmpty()) emptyList() else {
            // Bounded candidates per parcel, including rotated L parcels; no map-sized grid scan.
            scene.parcels.sortedBy { it.id }.flatMap { parcel ->
                CityTerrainGeometry.land(parcel.id).flatMapIndexed { index, shape ->
                    (1..4).flatMap { lane ->
                        val fraction = lane/5.0
                        val x = shape.left+(shape.right-shape.left)*fraction
                        val y = shape.bottom+(shape.top-shape.bottom)*fraction
                        val inset = ResidentClearance*2
                        listOf(
                            Route("resident:${parcel.id}:$index:$lane:x",Kind.Resident,
                                CityTerrainGeometry.world(parcel.position,shape.left+inset,y),CityTerrainGeometry.world(parcel.position,shape.right-inset,y)),
                            Route("resident:${parcel.id}:$index:$lane:y",Kind.Resident,
                                CityTerrainGeometry.world(parcel.position,x,shape.bottom+inset),CityTerrainGeometry.world(parcel.position,x,shape.top-inset)))
                    }
                }
            }.filter { safe(it,land) && roads.none { road -> road.overlaps(corridor(it)) } }
                .take(minOf(MaxResidents,scene.membership.population.min(MaxResidents.toBigInteger()).toInt()))
        }
        return residents + cars.filter { safe(it,roads) }.take(MaxCars)
    }
}
