package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.CityFloorGeometry
import kotlin.math.abs
import kotlin.math.floor

/** Nearest tick, exact half ties away from zero (matches Go math.Round).
 * No recentering, clamping or quantization of live physics. At most 0.0005 engine
 * units are lost; a stack whose rounded support vanishes fails closed.
 */
internal fun cityTick(value: Double): Long {
    require(value.isFinite() && abs(value) <= 1_000_000.0)
    val scaled = abs(value * 1000.0)
    val integral = floor(scaled)
    val magnitude = integral.toLong() + if (scaled - integral >= 0.5) 1L else 0L
    return if (value < 0) -magnitude else magnitude
}

fun EngineCheckpoint.cityGeometry(): CityFloorGeometry {
    validate()
    require(rules == PracticeRulesV1.VERSION && state.phase != Phase.FALLING)
    return CityFloorGeometry(state.floors.map { cityTick(it.centerX) }).also { it.json() }
}
