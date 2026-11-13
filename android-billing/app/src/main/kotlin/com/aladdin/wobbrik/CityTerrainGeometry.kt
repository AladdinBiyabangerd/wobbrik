package com.aladdin.wobbrik

import appbackend.wobbrik.game.CityParcel
import appbackend.wobbrik.game.CityPosition

/** Read-side projection of PLAN §7 / AppBackend city_footprints.go. Never validates placement. */
internal object CityTerrainGeometry {
    data class Rect(val left: Double, val bottom: Double, val right: Double, val top: Double)
    const val CliffDepth = 14.0 // Scene world units, independent of UI dimensions.
    private const val Module = 110.0

    fun land(id: String): List<Rect> = when (id) {
        "base", "land_5" -> listOf(Rect(0.0, 0.0, 4 * Module, 2 * Module))
        "land_1" -> listOf(Rect(0.0, 0.0, 2 * Module, 2 * Module))
        "land_2" -> listOf(Rect(0.0, 0.0, 3 * Module, Module), Rect(0.0, Module, 2 * Module, 2 * Module))
        "land_3" -> listOf(Rect(0.0, 0.0, 3 * Module, 2 * Module))
        "land_4" -> listOf(Rect(0.0, 0.0, 4 * Module, Module), Rect(0.0, Module, 3 * Module, 2 * Module))
        else -> emptyList()
    }

    fun decorationSize(id: String): Pair<Double, Double>? = when (id) {
        "decor_tree_1" -> 30.0 to 30.0
        "decor_tree_2" -> 70.0 to 30.0
        "decor_tree_3" -> 80.0 to 80.0
        "decor_bench_1" -> 40.0 to 20.0
        "decor_bench_2" -> 50.0 to 60.0
        "decor_bench_3" -> 90.0 to 80.0
        "decor_streetlight_1" -> 10.0 to 10.0
        "decor_streetlight_2" -> 40.0 to 10.0
        "decor_streetlight_3" -> 100.0 to 10.0
        "decor_road_1" -> 100.0 to 30.0
        "decor_road_2" -> 100.0 to 100.0
        "decor_road_3" -> 210.0 to 30.0
        else -> null
    }

    fun world(position: CityPosition, x: Double, y: Double): CityBuildingProjection.Point {
        val (dx, dy) = when (position.quarterTurns) {
            0 -> x to y
            1 -> -y to x
            2 -> -x to -y
            3 -> y to -x
            else -> error("Invalid city rotation")
        }
        return CityBuildingProjection.Point(position.xTicks / 1000.0 + dx, position.yTicks / 1000.0 + dy)
    }

    fun point(position: CityPosition, x: Double, y: Double): CityBuildingProjection.Point =
        world(position, x, y).let { CityBuildingProjection.project(it.x, it.y) }

    fun contours(parcels: List<CityParcel>): List<List<CityBuildingProjection.Point>> = parcels.flatMap { parcel ->
        land(parcel.id).map { rect ->
            listOf(rect.left to rect.bottom, rect.right to rect.bottom, rect.right to rect.top, rect.left to rect.top)
                .map { (x, y) -> point(parcel.position, x, y) }
        }
    }

    fun bounds(contours: List<List<CityBuildingProjection.Point>>): CityBuildingProjection.Bounds? {
        val points = contours.flatten()
        return if (points.isEmpty()) null else CityBuildingProjection.Bounds(points.minOf { it.x }, points.minOf { it.y },
            points.maxOf { it.x }, points.maxOf { it.y } + CliffDepth)
    }
}
