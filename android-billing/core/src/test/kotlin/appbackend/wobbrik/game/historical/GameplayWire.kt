// Historical test-only source from b6a4f9b. Only package/imports changed, plus the ABANDON
// branch required by the shared enum (9 November 2026): that version had no abandon route.
package appbackend.wobbrik.game.historical

import appbackend.wobbrik.game.*

import kotlinx.serialization.json.*

/** Typed request builders keep persisted metadata and wire envelopes together. */
object GameplayWire {
    private fun command(id: String, revision: Long, route: AttemptCommand.Route, request: JsonObject): AttemptCommand {
        require(id.isNotBlank() && revision >= 0)
        val body = buildJsonObject {
            put("operation_id", id); put("expected_revision", revision); put("rules_version", "wobbrik-v1")
            put("request", request)
        }.toString()
        require(body.toByteArray(Charsets.UTF_8).size <= 32768)
        return AttemptCommand(id, revision, route, body)
    }
    fun start(id: String, revision: Long) = command(id, revision, AttemptCommand.Route.START,
        buildJsonObject { put("engine_version", "wobbrik-structural-v1") })
    fun checkpoint(id: String, revision: Long, attempt: String, sequence: Long, floors: Long,
                   precise: Long, rescues: Long, snapshot: String): AttemptCommand {
        require(attempt.isNotBlank() && sequence > 0 && floors >= 0 && precise in 0..floors && rescues >= 0)
        require(snapshot.toByteArray(Charsets.UTF_8).size <= 16384)
        val state = Json.parseToJsonElement(snapshot)
        return command(id, revision, AttemptCommand.Route.CHECKPOINT, buildJsonObject {
            put("attempt_id", attempt); put("sequence", sequence); put("floors", floors)
            put("precise", precise); put("rescues", rescues); put("snapshot", state)
        })
    }
    fun finish(id: String, revision: Long, attempt: String, sequence: Long, parts: Map<String, String>): AttemptCommand {
        require(attempt.isNotBlank() && sequence >= 0)
        require(parts.keys == setOf("wall", "window", "entrance", "roof", "lighting") && parts.values.all { it.isNotBlank() })
        return command(id, revision, AttemptCommand.Route.FINISH, buildJsonObject {
            put("attempt_id", attempt); put("sequence", sequence)
            put("parts", buildJsonObject { parts.toSortedMap().forEach { (k, v) -> put(k, v) } })
        })
    }
    fun validate(command: AttemptCommand) {
        val root = Json.parseToJsonElement(command.envelope).jsonObject
        require(root.keys == setOf("operation_id", "expected_revision", "rules_version", "request"))
        require(root.string("operation_id") == command.operationId && root.number("expected_revision") == command.expectedRevision)
        require(root.string("rules_version") == "wobbrik-v1")
        val r = root.getValue("request").jsonObject
        val rebuilt = when (command.route) {
            AttemptCommand.Route.SHOP_BUY, AttemptCommand.Route.BUILDING_EDIT, AttemptCommand.Route.BUILDING_DELETE -> throw IllegalArgumentException("Unsupported by historical client")
            AttemptCommand.Route.START -> { require(r.string("engine_version") == "wobbrik-structural-v1"); start(command.operationId, command.expectedRevision) }
            AttemptCommand.Route.CHECKPOINT -> checkpoint(command.operationId, command.expectedRevision, r.string("attempt_id"), r.number("sequence"), r.number("floors"), r.number("precise"), r.number("rescues"), r.getValue("snapshot").toString())
            AttemptCommand.Route.FINISH -> finish(command.operationId, command.expectedRevision, r.string("attempt_id"), r.number("sequence"), r.getValue("parts").jsonObject.mapValues { (_, v) -> v.jsonPrimitive.also { require(it.isString) }.content })
            AttemptCommand.Route.ABANDON -> throw IllegalArgumentException("No abandon route in this version")
        }
        require(Json.parseToJsonElement(rebuilt.envelope) == root) { "Unexpected command fields" }
    }
    fun snapshot(body: String): GameSnapshot {
        val r = Json.parseToJsonElement(body).jsonObject
        require(r.string("rules_version") == "wobbrik-v1")
        val a = r.getValue("attempt").takeUnless { it == JsonNull }?.jsonObject?.let {
            require(it.string("rules_version") == "wobbrik-v1" && it.string("engine_version") == "wobbrik-structural-v1")
            val status = it.string("status"); require(status == "active" || status == "finished")
            ServerAttempt(it.string("attempt_id"), status == "active", it.number("sequence"), it.getValue("snapshot").takeUnless { v -> v == JsonNull }?.toString())
        }
        return GameSnapshot(r.string("rules_version"), r.number("revision"), r.number("coins"), r.number("xp"), a)
    }
    fun receipt(body: String) {
        val r = Json.parseToJsonElement(body).jsonObject
        r.number("revision"); require(r.string("accepted_at").isNotBlank()); require(r.getValue("result") is JsonObject)
    }
    fun error(body: String): String? = runCatching { Json.parseToJsonElement(body).jsonObject.string("error") }.getOrNull()
    private fun JsonObject.string(key: String) = getValue(key).jsonPrimitive.also { require(it.isString) }.content
    private fun JsonObject.number(key: String) = getValue(key).jsonPrimitive.also { require(!it.isString) }.long.also { require(it >= 0) }
}
