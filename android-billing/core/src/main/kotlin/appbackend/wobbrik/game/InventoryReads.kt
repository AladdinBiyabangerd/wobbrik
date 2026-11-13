package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*
import java.math.BigInteger

/** Owner-scoped read models; these never grant wallet, placement or cosmetic rights. */
data class OwnedBuilding(val id: String, val revision: Long, val floors: Long, val residents: Long,
    val baseValue: Long, val maxPrice: Long, val parts: Map<String, String>, val traded: Boolean,
    val geometry: AcceptedCityGeometry?)
data class InventoryPage(val buildings: List<OwnedBuilding>, val next: String?)
data class CityMembership(val revision: Long, val population: BigInteger,
    val buildings: Map<String, Long>, val next: String?)

object InventoryReads {
    private fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.also {
        require(it.isString && it.content.isNotBlank())
    }.content
    private fun JsonObject.number(key: String) = getValue(key).jsonPrimitive.also { require(!it.isString) }.long.also { require(it >= 0) }
    private fun JsonObject.next() = get("next_cursor")?.let { text("next_cursor").also(::id) }
    fun id(value: String) { require(Regex("[A-Za-z0-9_-]{1,128}").matches(value)) }
    fun building(value: JsonObject): OwnedBuilding {
        val parts = value.getValue("parts").jsonObject.mapValues { (_, v) ->
            v.jsonPrimitive.also { require(it.isString && it.content.isNotBlank()) }.content
        }
        require(parts.keys == FinishCosmetics.categories.toSet())
        val floors = value.number("floors"); require(floors >= 10)
        val geometry = value["accepted_geometry"]?.takeUnless { it == JsonNull }?.let { AcceptedCityGeometry.parse(it.jsonObject) }
        require(geometry == null || geometry.geometry.centers.size.toLong() == floors)
        return OwnedBuilding(value.text("id").also(::id), value.number("revision").also { require(it > 0) },
            floors, value.number("residents"), value.number("base_value"), value.number("max_price"), parts,
            value.getValue("traded").jsonPrimitive.also { require(!it.isString) }.boolean, geometry)
    }
    fun building(body: String) = building(Json.parseToJsonElement(body).jsonObject)
    fun inventory(body: String): InventoryPage {
        val r = Json.parseToJsonElement(body).jsonObject
        val buildings = r.getValue("buildings").jsonArray.map { building(it.jsonObject) }
        require(buildings.size <= 20 && buildings.map { it.id }.distinct().size == buildings.size)
        val next = r.next(); require(next == null || next == buildings.lastOrNull()?.id)
        return InventoryPage(buildings, next)
    }
    fun city(body: String): CityMembership {
        val r = Json.parseToJsonElement(body).jsonObject
        require(r.text("rules_version") == "wobbrik-v1")
        val population = r.text("population").also { require(Regex("0|[1-9][0-9]*").matches(it)) }.toBigInteger()
        val entries = r.getValue("buildings").jsonArray.map { e -> val b = e.jsonObject
            b.text("building_id").also(::id) to b.number("building_revision")
        }
        require(entries.size <= 20 && entries.map { it.first }.distinct().size == entries.size)
        val next = r.next(); require(next == null || next == entries.lastOrNull()?.first)
        return CityMembership(r.number("revision"), population, entries.toMap(), next)
    }
}

/** One refresh owns its pages. Different city revisions are rejected, never unioned. */
class InventoryRepository(private val backend: GameplayHttpBackend) {
    suspend fun page(session: BillingSession, cursor: String? = null) =
        InventoryReads.inventory(backend.read(session, "inventory" + query(cursor)))
    suspend fun building(session: BillingSession, id: String): OwnedBuilding {
        InventoryReads.id(id)
        return InventoryReads.building(backend.read(session, "buildings/$id")).also { require(it.id == id) }
    }
    suspend fun city(session: BillingSession): CityMembership {
        var cursor: String? = null
        var result: CityMembership? = null
        val seen = mutableSetOf<String>()
        do {
            val page = InventoryReads.city(backend.read(session, "city" + query(cursor)))
            val previous = result
            if (previous != null) require(previous.revision == page.revision && previous.population == page.population) { "City changed; refresh required" }
            require(page.buildings.keys.none { it in result?.buildings.orEmpty() })
            result = page.copy(buildings = previous?.buildings.orEmpty() + page.buildings)
            cursor = page.next
            require(cursor == null || seen.add(cursor))
        } while (cursor != null)
        return requireNotNull(result)
    }
    private fun query(cursor: String?): String {
        cursor?.let(InventoryReads::id)
        return "?limit=20" + (cursor?.let { "&cursor=$it" } ?: "")
    }
}
