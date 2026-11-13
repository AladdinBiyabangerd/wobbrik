package com.aladdin.wobbrik.engine

import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.game.*
import kotlinx.serialization.json.*
import java.security.MessageDigest

/** Terminal local intent, not a command envelope, accepted result, wallet or Ready capability. */
data class PendingGameResult(val id: String, val terminal: EngineCheckpoint, val parts: Map<String, String>,
    val attemptId: String = id, val started: Boolean = false)

/** One process-owned writer. Atomic bounded storage fails closed rather than evicting results. */
class PendingGameResults(private val file: (String) -> JournalFile) {
    private fun key(uid: String): String {
        require(uid.isNotBlank())
        return MessageDigest.getInstance("SHA-256").digest(uid.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
    @Synchronized fun pending(uid: String): List<PendingGameResult> {
        val bytes = file(key(uid)).read() ?: return emptyList()
        require(bytes.size <= DurableJournalFile.MaxBytes)
        val root = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
        require(root.getValue("version").jsonPrimitive.int == 1 && root.getValue("uid").jsonPrimitive.content == uid)
        return root.getValue("results").jsonArray.map { item ->
            val r = item.jsonObject
            PendingGameResult(r.getValue("id").jsonPrimitive.content,
                EngineCheckpointCodec.decode(r.getValue("terminal").toString()),
                r.getValue("parts").jsonObject.mapValues { it.value.jsonPrimitive.content },
                r.getValue("attempt_id").jsonPrimitive.content, r.getValue("started").jsonPrimitive.boolean).also(::validate)
        }.also { require(it.map { r -> r.id }.distinct().size == it.size && it.map { r -> r.attemptId }.distinct().size == it.size) }
    }
    @Synchronized fun enqueue(uid: String, result: PendingGameResult) {
        validate(result)
        val rows = pending(uid)
        val previous = rows.firstOrNull { it.id == result.id || it.attemptId == result.attemptId }
        if (previous != null) {
            require(previous.id == result.id && previous.terminal == result.terminal) { "Terminal progress changed" }
            // The host permits cosmetic selection only before dispatch against the exact Ready.
            // Preserve the accepted-start marker; a UI rewrite must never make a new start possible.
            if (previous.parts != result.parts) write(uid, rows.map { if (it.id == previous.id) it.copy(parts = result.parts) else it })
            return
        }
        write(uid, rows + result)
    }
    /** Called only after a validated committed receipt, before clearing its wire journal. */
    @Synchronized fun committed(session: BillingSession, command: AttemptCommand) {
        if (command.route !in setOf(AttemptCommand.Route.START, AttemptCommand.Route.FINISH)) return
        val request = Json.parseToJsonElement(command.envelope).jsonObject.getValue("request").jsonObject
        val attempt = if (command.route == AttemptCommand.Route.START) command.operationId else request.getValue("attempt_id").jsonPrimitive.content
        val rows = pending(session.uid)
        if (rows.none { it.attemptId == attempt }) return
        write(session.uid, if (command.route == AttemptCommand.Route.FINISH) rows.filterNot { it.attemptId == attempt }
            else rows.map { if (it.attemptId == attempt) it.copy(started = true) else it })
    }
    private fun validate(result: PendingGameResult) {
        InventoryReads.id(result.id); InventoryReads.id(result.attemptId)
        require(result.id.length <= 110) // Leave room for stable per-stage operation suffixes.
        result.terminal.validate()
        require(result.terminal.state.phase == Phase.FINISHED && result.terminal.state.paused && result.terminal.tickUnits == 0L)
        require(result.parts.keys == FinishCosmetics.categories.toSet())
        result.parts.values.forEach(InventoryReads::id)
        EngineCheckpointCodec.encode(result.terminal)
    }
    private fun write(uid: String, rows: List<PendingGameResult>) {
        val body = buildJsonObject {
            put("version", 1); put("uid", uid)
            put("results", JsonArray(rows.map { r -> buildJsonObject {
                put("id", r.id); put("attempt_id", r.attemptId); put("started", r.started)
                put("terminal", Json.parseToJsonElement(EngineCheckpointCodec.encode(r.terminal)))
                put("parts", buildJsonObject { r.parts.toSortedMap().forEach { (k, v) -> put(k, v) } })
            } }))
        }.toString().toByteArray(Charsets.UTF_8)
        require(body.size <= DurableJournalFile.MaxBytes) { "Pending result storage full; retry after sync" }
        file(key(uid)).replace(body)
        check(pending(uid) == rows) { "Pending result write was not committed" }
    }
}
