package appbackend.wobbrik.game

import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.serialization.json.*

data class PartProgress(val itemId: String, val completed: Long, val required: Long, val unlocked: Boolean)
data class AccountRecovery(val serverTime: String, val firstOnlineAt: String, val graceUntil: String,
    val level: Long, val rescues: Long, val achievements: List<String>, val unlocks: List<String>,
    val land: List<String>, val partProgress: List<PartProgress>,
    val bestFloors: Long? = null, val xpInterval: XpInterval? = null)
data class XpInterval(val start: BigInteger, val next: BigInteger)
sealed interface AttemptResult {
    data class Started(val attemptId: String) : AttemptResult
    data class Checkpointed(val xp: Long, val level: Long, val rescues: Long, val granted: List<String>, val coins: Long) : AttemptResult
    data class Finished(val attemptId: String, val resultId: String?, val buildingId: String?, val floors: Long,
        val coins: Long, val residents: Long, val baseValue: Long, val maxPrice: Long) : AttemptResult
}
data class AttemptReceipt(val revision: Long, val acceptedAt: String, val result: AttemptResult)
data class OperationReceipt(val revision: Long, val acceptedAt: String, val result: JsonObject)
data class CatalogItem(val id: String, val name: String, val category: String, val method: String,
    val weight: Long, val price: Long?, val views: Long?)
data class LandOffer(val id: String, val requiredXp: Long, val price: Long)
data class RecoveryCatalog(val rulesVersion: String, val engineVersion: String,
    val items: List<CatalogItem>, val landOffers: List<LandOffer>)

/** Additive response fields are tolerated; required recovery fields fail closed. No local grants. */
object RecoveryDtos {
    private fun root(body: String) = Json.parseToJsonElement(body).jsonObject
    private fun JsonObject.s(k: String) = getValue(k).jsonPrimitive.also { require(it.isString) }.content.also { require(it.isNotBlank()) }
    private fun JsonObject.n(k: String) = getValue(k).jsonPrimitive.also { require(!it.isString) }.long.also { require(it >= 0) }
    private fun JsonObject.b(k: String) = getValue(k).jsonPrimitive.also { require(!it.isString) }.boolean
    // Server contract emits UTC RFC3339; keep exact text and support Android API 24.
    private fun JsonObject.time(k: String): String = s(k).also { value ->
        require(Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,9})?Z""").matches(value))
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
        parser.isLenient = false; parser.timeZone = TimeZone.getTimeZone("UTC")
        parser.parse(value.substring(0, 19)) ?: error("Invalid server timestamp")
    }
    private fun JsonObject.ids(k: String) = getValue(k).jsonArray.map {
        it.jsonPrimitive.also { p -> require(p.isString && p.content.isNotBlank()) }.content
    }.also { require(it.distinct().size == it.size) }
    private fun versions(r: JsonObject) {
        require(r.s("rules_version") == "wobbrik-v1")
        require(r.s("engine_version") == "wobbrik-structural-v1")
    }
    private fun JsonObject.xpInterval(xp: Long, level: Long): XpInterval? {
        val keys = listOf("level_start_xp", "next_level_xp")
        if (keys.none { containsKey(it) }) return null
        fun decimal(key: String): BigInteger {
            val value = s(key)
            require(value.length <= 20 && Regex("0|[1-9][0-9]*").matches(value))
            return BigInteger(value)
        }
        require(level > 0)
        val start = decimal(keys[0]); val next = decimal(keys[1])
        val l = BigInteger.valueOf(level); val scale = l * BigInteger.valueOf(25)
        require(start == scale * (l - BigInteger.ONE) && next == scale * (l + BigInteger.ONE))
        require(BigInteger.valueOf(xp) >= start && BigInteger.valueOf(xp) < next)
        return XpInterval(start, next)
    }
    fun snapshot(body: String): GameSnapshot {
        val r = root(body); require(r.s("rules_version") == "wobbrik-v1")
        val attempt = r.getValue("attempt").takeUnless { it == JsonNull }?.jsonObject?.let { a ->
            versions(a)
            val status = a.s("status"); require(status in setOf("active", "finished"))
            val floors = a.n("floors"); val precise = a.n("precise"); val rescues = a.n("rescues")
            require(precise <= floors && rescues <= floors)
            val snapshot = a.getValue("snapshot").takeUnless { it == JsonNull }?.toString()
            require(snapshot == null || snapshot.toByteArray(Charsets.UTF_8).size <= 16384)
            ServerAttempt(a.s("attempt_id"), status == "active", a.n("sequence"), snapshot,
                floors, precise, rescues, a.b("continued"), a.s("rules_version"), a.s("engine_version"), a["geometry_version"]?.let { a.s("geometry_version").also { require(it == CityFloorGeometry.VERSION) } },
                a["accepted_geometry"]?.takeUnless { it == JsonNull }?.let { AcceptedCityGeometry.parse(it.jsonObject) }).also { accepted ->
                    if (accepted.geometryVersion != null && floors > 0) {
                        val geometry = requireNotNull(accepted.acceptedGeometry)
                        require(geometry.attemptId == accepted.id && geometry.sequence == accepted.sequence &&
                            geometry.geometry.centers.size.toLong() == floors && geometry.rulesVersion == accepted.rulesVersion &&
                            geometry.engineVersion == accepted.engineVersion)
                    } else require(accepted.acceptedGeometry == null)
                }
        }
        val progress = r.getValue("part_progress").jsonArray.map { element ->
            val p = element.jsonObject
            PartProgress(p.s("item_id"), p.n("completed"), p.n("required"), p.b("unlocked")).also {
                require(it.required > 0 && it.completed <= it.required && it.unlocked == (it.completed == it.required))
            }
        }
        require(progress.map { it.itemId }.distinct().size == progress.size)
        val account = AccountRecovery(r.time("server_time"), r.time("first_online_at"), r.time("grace_until"),
            r.n("level"), r.n("rescues"), r.ids("achievements"), r.ids("unlocks"), r.ids("land"), progress,
            r["best_floors"]?.let { r.n("best_floors") }, r.xpInterval(r.n("xp"), r.n("level")))
        return GameSnapshot(r.s("rules_version"), r.n("revision"), r.n("coins"), r.n("xp"), attempt, account)
    }
    fun receipt(body: String): OperationReceipt {
        val r = root(body)
        return OperationReceipt(r.n("revision"), r.time("accepted_at"), r.getValue("result").jsonObject)
    }
    fun attemptReceipt(body: String, route: AttemptCommand.Route): AttemptReceipt {
        val receipt = receipt(body); val r = receipt.result
        val result = when (route) {
            AttemptCommand.Route.START -> AttemptResult.Started(r.s("attempt_id"))
            AttemptCommand.Route.CHECKPOINT -> AttemptResult.Checkpointed(r.n("xp"), r.n("level"), r.n("rescues"), r.ids("granted"), r.n("coins"))
            AttemptCommand.Route.FINISH -> AttemptResult.Finished(r.s("attempt_id"), r["result_id"]?.let { r.s("result_id") },
                r["building_id"]?.let { r.s("building_id") }, r.n("floors"), r.n("coins"), r.n("residents"), r.n("base_value"), r.n("max_price"))
        }
        return AttemptReceipt(receipt.revision, receipt.acceptedAt, result)
    }
    fun catalog(body: String): RecoveryCatalog {
        val r = root(body); versions(r)
        val items = r.getValue("items").jsonArray.map { e -> val i = e.jsonObject
            CatalogItem(i.s("id"), i.s("name"), i.s("category"), i.s("method"), i.n("weight"),
                i["price"]?.let { i.n("price") }, i["views"]?.let { i.n("views") })
        }
        val land = r.getValue("land_offers").jsonArray.map { e -> val i = e.jsonObject
            LandOffer(i.s("id"), i.n("required_xp"), i.n("price"))
        }
        require(items.map { it.id }.distinct().size == items.size && land.map { it.id }.distinct().size == land.size)
        return RecoveryCatalog(r.s("rules_version"), r.s("engine_version"), items, land)
    }
}
