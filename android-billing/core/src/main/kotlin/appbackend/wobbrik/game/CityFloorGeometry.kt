package appbackend.wobbrik.game

import kotlinx.serialization.json.*
import kotlin.math.abs

/** Immutable wire geometry. Tick coordinates use the foundation origin, never recentered. */
data class CityFloorGeometry(val centers: List<Long>) {
    fun json(): JsonObject {
        require(centers.isNotEmpty())
        var previous = 0L
        centers.forEach { x ->
            require(x in -999_950_000L..999_950_000L)
            require(abs(x - previous) < 100_000L) { "Quantized floors must retain positive support" }
            previous = x
        }
        return buildJsonObject {
            put("version", VERSION)
            put("centers_x_ticks", JsonArray(centers.map { JsonPrimitive(it) }))
        }
    }
    companion object {
        const val VERSION = "city-geometry-v1"
        fun parse(value: JsonObject): CityFloorGeometry {
            require(value.keys == setOf("version", "centers_x_ticks"))
            require(value.getValue("version").jsonPrimitive.let { it.isString && it.content == VERSION })
            return CityFloorGeometry(value.getValue("centers_x_ticks").jsonArray.map {
                it.jsonPrimitive.also { p -> require(!p.isString) }.long
            }).also { it.json() }
        }
    }
}

/** Server receipt provenance, scoped by the authenticated containing response. */
data class AcceptedCityGeometry(val geometry: CityFloorGeometry, val attemptId: String,
    val sequence: Long, val operationId: String, val rulesVersion: String, val engineVersion: String) {
    companion object {
        fun parse(value: JsonObject): AcceptedCityGeometry {
            fun text(key: String) = value.getValue(key).jsonPrimitive.also {
                require(it.isString && it.content.isNotBlank())
            }.content
            val sequence = value.getValue("sequence").jsonPrimitive.also { require(!it.isString) }.long
            require(sequence > 0)
            return AcceptedCityGeometry(CityFloorGeometry.parse(value.getValue("geometry").jsonObject),
                text("attempt_id"), sequence, text("operation_id"), text("rules_version"), text("engine_version")).also {
                require(it.rulesVersion == "wobbrik-v1" && it.engineVersion == "wobbrik-structural-v1")
            }
        }
    }
}
