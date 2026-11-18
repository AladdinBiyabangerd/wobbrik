package com.aladdin.wobbrik

import android.content.Context
import appbackend.wobbrik.game.*
import kotlinx.serialization.json.*

/** Compiled only into mockDebug. No HTTP transport, Firebase identity or real economy. */
internal object BuildVariantBackend {
    const val localUid = "local-mock-profile"
    fun transport(context: Context): GameplayTransport {
        val storage = context.getSharedPreferences("mock-backend-v1", Context.MODE_PRIVATE)
        return MockGameplayTransport(storage.getString("state", null)) { next ->
            check(storage.edit().putString("state", next).commit()) { "Mock state could not be saved" }
        }
    }
}

/** Deliberately synthetic data, persisted atomically with operation receipts for retry/relaunch. */
internal class MockGameplayTransport(saved: String?, private val save: (String) -> Unit) : GameplayTransport {
    private val time = "2026-11-15T00:00:00Z"
    private fun obj(block: JsonObjectBuilder.() -> Unit) = buildJsonObject(block)
    private fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.content
    private fun JsonObject.number(key: String) = getValue(key).jsonPrimitive.long
    private var state = saved?.let { Json.parseToJsonElement(it).jsonObject } ?: obj {
        put("revision", 1); put("coins", 1250); put("xp", 0); put("attempt", JsonNull)
        put("buildings", JsonArray(emptyList())); put("receipts", obj {}); put("history", obj {})
        put("profile", obj { put("revision", 1); put("display_name", "Mock İnşaatçı"); put("avatar_id", "builder") })
    }
    private val catalog = obj {
        put("rules_version", "wobbrik-v1"); put("engine_version", "wobbrik-structural-v1")
        put("items", buildJsonArray {
            for (style in 1..6) for (category in FinishCosmetics.categories) add(obj {
                put("id", "base_${style}_$category"); put("name", "Mock $style · $category")
                put("category", category); put("method", "free"); put("weight", 0)
            })
        }); put("land_offers", JsonArray(emptyList()))
    }
    private fun snapshot(source: JsonObject = state): JsonObject = obj {
        put("rules_version", "wobbrik-v1"); put("revision", source.getValue("revision"))
        put("coins", source.getValue("coins")); put("xp", source.getValue("xp")); put("attempt", source.getValue("attempt"))
        put("server_time", time); put("first_online_at", time); put("grace_until", "2099-01-01T00:00:00Z")
        put("level", 1); put("rescues", 0); put("best_floors", 0)
        for (key in listOf("achievements", "unlocks", "land", "part_progress")) put(key, JsonArray(emptyList()))
    }
    private fun response(body: JsonElement) = GameplayResponse(200, body.toString())
    private fun failure(code: String, status: Int = 404) = GameplayResponse(status, obj { put("error", code) }.toString())
    override suspend fun request(path: String, bearer: String, body: String?): GameplayResponse = synchronized(this) {
        check(bearer == "local-mock-only")
        if (body != null && path.startsWith("attempts/")) return@synchronized mutate(path, body)
        when {
            path == "sync" -> response(snapshot())
            path == "catalog" -> response(catalog)
            path == "profile" -> {
                if (body != null) {
                    val request = Json.parseToJsonElement(body).jsonObject
                    val profile = obj {
                        put("revision", state.getValue("profile").jsonObject.number("revision") + 1)
                        put("display_name", request.getValue("display_name")); put("avatar_id", request.getValue("avatar_id"))
                    }
                    persist(JsonObject(state + ("profile" to profile)))
                }
                response(state.getValue("profile"))
            }
            path.startsWith("inventory?") -> response(obj { put("buildings", state.getValue("buildings")) })
            path.startsWith("buildings/") -> state.getValue("buildings").jsonArray.firstOrNull {
                it.jsonObject.text("id") == path.substringAfter("buildings/").substringBefore("?")
            }?.let(::response) ?: failure("not_found")
            path.startsWith("operations/") -> state.getValue("receipts").jsonObject[path.substringAfter("operations/")]?.let(::response) ?: failure("not_found")
            path.startsWith("attempts/") -> state.getValue("history").jsonObject[path.substringAfter("attempts/")]?.let(::response) ?: failure("not_found")
            path.startsWith("decorations?") -> response(obj { put("copies", JsonArray(emptyList())) })
            path.startsWith("market?") -> response(obj { put("listings", JsonArray(emptyList())) })
            path.startsWith("leaderboard?") -> response(obj {
                put("scope", "weekly"); put("week_start", "2026-11-09"); put("snapshot_at", time)
                put("expires_at", "2099-01-01T00:00:00Z"); put("entries", JsonArray(emptyList()))
            })
            path.startsWith("city?") -> response(obj {
                put("rules_version", "wobbrik-v1"); put("geometry_version", "city-geometry-v1")
                put("placement_enabled", true); put("revision", state.getValue("revision")); put("population", "0")
                put("parcels", buildJsonArray { add(obj {
                    put("kind", "land"); put("id", "base"); put("position", obj {
                        put("x_ticks", "0"); put("y_ticks", "0"); put("quarter_turns", 0)
                    })
                }) }); put("buildings", JsonArray(emptyList()))
            })
            // Mock is for local play and inspection, not simulated purchases/trading authority.
            else -> failure("invalid_request", 400)
        }
    }
    private fun persist(next: JsonObject) {
        save(next.toString())
        state = next
    }
    private fun mutate(path: String, body: String): GameplayResponse {
        val envelope = Json.parseToJsonElement(body).jsonObject
        val id = envelope.text("operation_id")
        val receipts = state.getValue("receipts").jsonObject
        receipts[id]?.let { return response(it) }
        if (envelope.number("expected_revision") != state.number("revision")) return failure("revision_conflict", 409)
        val request = envelope.getValue("request").jsonObject
        val old = state["attempt"]?.takeUnless { it == JsonNull }?.jsonObject
        val revision = state.number("revision") + 1
        var attempt = old
        var buildings = state.getValue("buildings").jsonArray
        val result: JsonObject
        when (path) {
            "attempts/start" -> {
                if (old?.text("status") == "active") return failure("invalid_request", 400)
                attempt = obj {
                    put("attempt_id", id); put("status", "active"); put("sequence", 0); put("snapshot", JsonNull)
                    put("floors", 0); put("precise", 0); put("rescues", 0); put("continued", false)
                    put("rules_version", "wobbrik-v1"); put("engine_version", "wobbrik-structural-v1")
                    request["geometry_version"]?.let { put("geometry_version", it) }
                }
                result = obj { put("attempt_id", id) }
            }
            "attempts/checkpoint", "attempts/finish", "attempts/abandon" -> {
                if (old == null || old.text("status") != "active" || old.text("attempt_id") != request.text("attempt_id")) return failure("invalid_request", 400)
                val floors = if (path.endsWith("checkpoint")) request.number("floors") else old.number("floors")
                attempt = when {
                    path.endsWith("checkpoint") -> obj {
                        old.forEach { (k,v) -> put(k,v) }
                        for (key in listOf("sequence", "snapshot", "floors", "precise", "rescues")) put(key, request.getValue(key))
                        request["geometry"]?.let { geometry -> put("accepted_geometry", obj {
                            put("geometry", geometry); put("attempt_id", old.getValue("attempt_id")); put("sequence", request.getValue("sequence"))
                            put("operation_id", id); put("rules_version", "wobbrik-v1"); put("engine_version", "wobbrik-structural-v1")
                        }) }
                    }
                    else -> JsonObject(old + ("status" to JsonPrimitive(if (path.endsWith("finish")) "finished" else "abandoned")))
                }
                result = obj {
                    if (path.endsWith("checkpoint")) {
                        put("xp", 0); put("level", 1); put("rescues", request.getValue("rescues")); put("granted", JsonArray(emptyList())); put("coins", 0)
                    } else {
                        put("attempt_id", old.getValue("attempt_id")); put("floors", floors)
                        if (path.endsWith("finish")) {
                            put("result_id", id); put("coins", 0); put("residents", floors * 4); put("base_value", floors * 2); put("max_price", floors * 2)
                            if (floors >= 10) put("building_id", "mock-$id")
                        }
                    }
                }
                if (path.endsWith("finish") && floors >= 10) buildings = JsonArray(buildings + obj {
                    put("id", "mock-$id"); put("revision", 1); put("floors", floors); put("residents", floors * 4)
                    put("base_value", floors * 2); put("max_price", floors * 2); put("parts", request.getValue("parts")); put("traded", false)
                    attempt["accepted_geometry"]?.let { put("accepted_geometry", it) }
                })
            }
            else -> return failure("invalid_request", 400)
        }
        val receipt = obj { put("revision", revision); put("accepted_at", time); put("result", result) }
        var next = JsonObject(state + mapOf("revision" to JsonPrimitive(revision), "attempt" to requireNotNull(attempt), "buildings" to buildings,
            "receipts" to JsonObject(receipts + (id to receipt))))
        val history = JsonObject(state.getValue("history").jsonObject + (attempt.text("attempt_id") to snapshot(next)))
        next = JsonObject(next + ("history" to history))
        persist(next)
        return response(receipt)
    }
}
