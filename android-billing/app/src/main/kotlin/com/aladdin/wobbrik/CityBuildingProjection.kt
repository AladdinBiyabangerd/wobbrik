package com.aladdin.wobbrik

import appbackend.wobbrik.game.CityPosition
import appbackend.wobbrik.game.CitySceneBuilding

/** Fixed camera in world units. Rotate floor offsets around the foundation before projection.
 * A quarter turn changes world geometry, never the orientation of a screen-space bitmap. */
internal object CityBuildingProjection {
    // A server 100×100 footprint projects to 200 horizontal units at this camera.
    const val ArtWidth = 200.0
    const val FoundationFront = 50.0
    // Approved modular floor pitch (176 / 464 at the projected footprint width).
    // Presentation only: server accepted centers and horizontal footprint are unchanged.
    const val ArtPitch = ArtWidth * 176 / 464
    data class Point(val x: Double, val y: Double)
    data class Bounds(val left: Double, val top: Double, val right: Double, val bottom: Double)
    data class Building(val source: CitySceneBuilding, val foundation: Point, val floors: List<Point>, val bounds: Bounds)

    fun project(x: Double, y: Double, height: Double = 0.0) = Point(x - y, -(x + y) / 2 - height)

    fun floor(position: CityPosition, centerTicks: Long, index: Int): Point {
        val offset = centerTicks / 1000.0
        val (dx, dy) = when (position.quarterTurns) {
            0 -> offset to 0.0
            1 -> 0.0 to offset
            2 -> -offset to 0.0
            3 -> 0.0 to -offset
            else -> error("Invalid city rotation")
        }
        return project(position.xTicks / 1000.0 + dx, position.yTicks / 1000.0 + dy, index * ArtPitch)
    }

    fun building(source: CitySceneBuilding): Building? {
        val position = source.position ?: return null
        val centers = source.building.geometry?.geometry?.centers ?: return null
        if (centers.isEmpty() || centers.size.toLong() != source.building.floors) return null
        val floors = centers.mapIndexed { index, center -> floor(position, center, index) }
        // Includes conservative catalog roof extent and the full projected footprint.
        val bounds = Bounds(floors.minOf { it.x } - ArtWidth / 2, floors.minOf { it.y } + FoundationFront - ArtWidth * 1.21,
            floors.maxOf { it.x } + ArtWidth / 2, floors.maxOf { it.y } + FoundationFront)
        return Building(source, floor(position, 0, 0), floors, bounds)
    }

    fun ordered(buildings: List<Building>) = buildings.sortedWith(
        compareBy<Building> { it.foundation.y }.thenBy { it.foundation.x }.thenBy { it.source.building.id })
}
