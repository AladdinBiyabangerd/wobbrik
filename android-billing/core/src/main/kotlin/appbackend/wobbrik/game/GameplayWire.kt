package appbackend.wobbrik.game

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
    fun start(id: String, revision: Long, geometryVersion: String? = null) = command(id, revision, AttemptCommand.Route.START,
        buildJsonObject { put("engine_version", "wobbrik-structural-v1"); geometryVersion?.let { require(it == CityFloorGeometry.VERSION); put("geometry_version", it) } })
    fun checkpoint(id: String, revision: Long, attempt: String, sequence: Long, floors: Long,
                   precise: Long, rescues: Long, snapshot: String, geometry: CityFloorGeometry? = null): AttemptCommand {
        require(attempt.isNotBlank() && sequence > 0 && floors > 0 && precise in 0..floors && rescues in 0..floors)
        require(snapshot.toByteArray(Charsets.UTF_8).size <= 16384)
        val state = Json.parseToJsonElement(snapshot)
        return command(id, revision, AttemptCommand.Route.CHECKPOINT, buildJsonObject {
            put("attempt_id", attempt); put("sequence", sequence); put("floors", floors)
            put("precise", precise); put("rescues", rescues); put("snapshot", state)
            geometry?.let { require(it.centers.size.toLong() == floors); put("geometry", it.json()) }
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
    /**
     * User policy (9 November 2026): leaving an open attempt mid-game closes it on the server
     * with no result and no building. Sequence is the last accepted checkpoint sequence.
     */
    fun abandon(id: String, revision: Long, attempt: String, sequence: Long): AttemptCommand {
        require(attempt.isNotBlank() && sequence >= 0)
        return command(id, revision, AttemptCommand.Route.ABANDON, buildJsonObject {
            put("attempt_id", attempt); put("sequence", sequence)
        })
    }
    fun buyPart(id: String, revision: Long, itemId: String) = command(id, revision, AttemptCommand.Route.SHOP_BUY,
        buildJsonObject { InventoryReads.id(itemId); put("item_id", itemId) })

    fun editBuilding(id: String, revision: Long, buildingId: String, buildingRevision: Long,
                     parts: Map<String, String>): AttemptCommand {
        InventoryReads.id(buildingId); require(buildingRevision > 0)
        require(parts.keys == FinishCosmetics.categories.toSet())
        parts.values.forEach(InventoryReads::id)
        return command(id, revision, AttemptCommand.Route.BUILDING_EDIT, buildJsonObject {
            put("building_id", buildingId); put("building_revision", buildingRevision)
            put("parts", buildJsonObject { parts.toSortedMap().forEach { (k, v) -> put(k, v) } })
        })
    }
    fun deleteBuilding(id: String, revision: Long, buildingId: String, buildingRevision: Long): AttemptCommand {
        InventoryReads.id(buildingId); require(buildingRevision > 0)
        return command(id, revision, AttemptCommand.Route.BUILDING_DELETE, buildJsonObject {
            put("building_id", buildingId); put("building_revision", buildingRevision)
        })
    }
    fun validate(command: AttemptCommand) {
        val root = Json.parseToJsonElement(command.envelope).jsonObject
        require(root.keys == setOf("operation_id", "expected_revision", "rules_version", "request"))
        require(root.string("operation_id") == command.operationId && root.number("expected_revision") == command.expectedRevision)
        require(root.string("rules_version") == "wobbrik-v1")
        val r = root.getValue("request").jsonObject
        val rebuilt = when (command.route) {
            AttemptCommand.Route.BUILDING_DELETE -> deleteBuilding(command.operationId, command.expectedRevision,
                r.string("building_id"), r.number("building_revision"))
            AttemptCommand.Route.SHOP_BUY -> buyPart(command.operationId, command.expectedRevision, r.string("item_id"))
            AttemptCommand.Route.BUILDING_EDIT -> editBuilding(command.operationId, command.expectedRevision,
                r.string("building_id"), r.number("building_revision"), r.getValue("parts").jsonObject.mapValues { it.value.jsonPrimitive.also { p -> require(p.isString) }.content })
            AttemptCommand.Route.START -> { require(r.string("engine_version") == "wobbrik-structural-v1"); start(command.operationId, command.expectedRevision, r["geometry_version"]?.jsonPrimitive?.content) }
            AttemptCommand.Route.CHECKPOINT -> checkpoint(command.operationId, command.expectedRevision, r.string("attempt_id"), r.number("sequence"), r.number("floors"), r.number("precise"), r.number("rescues"), r.getValue("snapshot").toString(), r["geometry"]?.let { CityFloorGeometry.parse(it.jsonObject) })
            AttemptCommand.Route.ABANDON -> abandon(command.operationId, command.expectedRevision, r.string("attempt_id"), r.number("sequence"))
            AttemptCommand.Route.FINISH -> finish(command.operationId, command.expectedRevision, r.string("attempt_id"), r.number("sequence"), r.getValue("parts").jsonObject.mapValues { (_, v) -> v.jsonPrimitive.also { require(it.isString) }.content })
        }
        require(Json.parseToJsonElement(rebuilt.envelope) == root) { "Unexpected command fields" }
    }
    fun snapshot(body: String): GameSnapshot = RecoveryDtos.snapshot(body)
    fun receipt(body: String): OperationReceipt = RecoveryDtos.receipt(body)
    fun error(body: String): String? = runCatching { Json.parseToJsonElement(body).jsonObject.string("error") }.getOrNull()
    private fun JsonObject.string(key: String) = getValue(key).jsonPrimitive.also { require(it.isString) }.content
    private fun JsonObject.number(key: String) = getValue(key).jsonPrimitive.also { require(!it.isString) }.long.also { require(it >= 0) }
}
