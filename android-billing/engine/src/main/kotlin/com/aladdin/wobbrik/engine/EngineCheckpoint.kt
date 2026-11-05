package com.aladdin.wobbrik.engine

import kotlinx.serialization.json.*
import kotlin.math.abs

/** Local deterministic state, carried as opaque JSON by structural-v1; never trusted geometry. */
data class EngineCheckpoint(
    val version: String = "practice-checkpoint-v1",
    val rules: String = PracticeRulesV1.VERSION,
    val state: PracticeState,
    val direction: Int,
    val fallVelocity: Double,
    val tickUnits: Long,
    val swayVelocity: Double,
) {
    fun validate() {
        require(version == "practice-checkpoint-v1" && rules == PracticeRulesV1.VERSION)
        require(direction == -1 || direction == 1)
        require(tickUnits in 0 until 1_000_000_000L)
        require(fallVelocity.isFinite() && fallVelocity >= 0 && swayVelocity.isFinite())
        require(state.craneX.isFinite() && abs(state.craneX) <= PracticeRulesV1.CRANE_RANGE)
        require(state.sway.isFinite() && state.balance.lean.isFinite())
        require(state.balance.rescues in 0..state.floors.size && state.perfectLandings in 0..state.floors.size)
        require((state.phase == Phase.FALLING) == (state.falling != null))
        state.floors.forEachIndexed { index, f ->
            require(f.centerX.isFinite() && abs(f.centerX) <= PracticeRulesV1.CRANE_RANGE)
            require(f.baseY == index * PracticeRulesV1.FLOOR_HEIGHT)
            val support = state.floors.getOrNull(index - 1)?.centerX ?: 0.0
            require(abs(f.centerX - support) < PracticeRulesV1.FLOOR_WIDTH)
        }
        state.falling?.let {
            val y = state.floors.size * PracticeRulesV1.FLOOR_HEIGHT
            require(it.centerX == state.craneX && it.baseY.isFinite() && it.baseY > y && it.baseY <= y + PracticeRulesV1.DROP_HEIGHT)
        }
        require(state.phase == Phase.FINISHED || abs(state.balance.lean) < BalanceRulesV1.TOPPLE)
    }
}

object EngineCheckpointCodec {
    private const val MAX_BYTES = 16384
    fun encode(value: EngineCheckpoint): String {
        value.validate()
        val s = value.state
        val json = buildJsonObject {
            put("version", value.version); put("rules", value.rules)
            put("phase", s.phase.name); put("paused", s.paused); put("crane", s.craneX)
            put("direction", value.direction); put("fall_velocity", value.fallVelocity)
            put("tick_units", value.tickUnits); put("sway", s.sway); put("sway_velocity", value.swayVelocity)
            put("lean", s.balance.lean); put("danger", s.balance.dangerEpisode)
            put("rescues", s.balance.rescues); put("perfect", s.perfectLandings)
            put("floors", JsonArray(s.floors.map { floor(it) }))
            put("falling", s.falling?.let { floor(it) } ?: JsonNull)
        }.toString()
        require(json.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "Checkpoint exceeds server snapshot limit" }
        return json
    }
    /** Match the authoritative attempt counters before allowing a host to restore. */
    fun decodeAccepted(json: String, floors: Long, precise: Long, rescues: Long): EngineCheckpoint = decode(json).also {
        require(it.state.floors.size.toLong() == floors && it.state.perfectLandings.toLong() == precise &&
            it.state.balance.rescues.toLong() == rescues) { "Attempt/checkpoint counter mismatch" }
    }
    private fun floor(f: Floor) = buildJsonArray { add(f.centerX); add(f.baseY) }
    fun decode(json: String): EngineCheckpoint {
        require(json.toByteArray(Charsets.UTF_8).size <= MAX_BYTES)
        val r = Json.parseToJsonElement(json).jsonObject
        require(r.keys == setOf("version", "rules", "phase", "paused", "crane", "direction", "fall_velocity",
            "tick_units", "sway", "sway_velocity", "lean", "danger", "rescues", "perfect", "floors", "falling"))
        fun s(k: String) = r.getValue(k).jsonPrimitive.also { require(it.isString) }.content
        fun n(k: String) = r.getValue(k).jsonPrimitive.also { require(!it.isString) }
        fun f(e: JsonElement): Floor {
            val a = e.jsonArray; require(a.size == 2)
            require(a.all { !it.jsonPrimitive.isString })
            return Floor(a[0].jsonPrimitive.double, a[1].jsonPrimitive.double)
        }
        return EngineCheckpoint(s("version"), s("rules"), PracticeState(
            Phase.valueOf(s("phase")), n("paused").boolean, n("crane").double,
            r.getValue("falling").takeUnless { it == JsonNull }?.let { f(it) },
            r.getValue("floors").jsonArray.map { f(it) },
            Balance(n("lean").double, n("danger").boolean, n("rescues").int), n("sway").double, n("perfect").int),
            n("direction").int, n("fall_velocity").double, n("tick_units").long, n("sway_velocity").double).also { it.validate() }
    }
}
