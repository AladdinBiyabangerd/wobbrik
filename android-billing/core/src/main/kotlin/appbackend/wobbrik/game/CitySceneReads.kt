package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*

/** Read-only scene coordinates. Absence is legacy membership, never an invented origin. */
data class CityPosition(val xTicks: Long, val yTicks: Long, val quarterTurns: Int)
data class CityParcel(val id: String, val position: CityPosition)
data class CitySceneBuilding(val building: OwnedBuilding, val position: CityPosition?)
data class CitySceneDecoration(val copy: DecorationCopy, val position: CityPosition)
data class CityScene(val membership: CityMembership, val parcels: List<CityParcel>,
    val buildings: List<CitySceneBuilding>, val decorations: List<CitySceneDecoration>)

internal data class CityScenePage(val membership: CityMembership, val parcels: List<CityParcel>,
    val positions: Map<String, CityPosition?>)

internal object CitySceneReads {
    fun position(value: JsonElement): CityPosition {
        val p = value.jsonObject
        fun tick(key: String): Long {
            val raw = p.getValue(key).jsonPrimitive
            require(raw.isString)
            val number = raw.content.toLong()
            require(number.toString() == raw.content && number in -1_000_000_000L..1_000_000_000L)
            return number
        }
        val turns = p.getValue("quarter_turns").jsonPrimitive
        require(!turns.isString && turns.int in 0..3)
        return CityPosition(tick("x_ticks"), tick("y_ticks"), turns.int)
    }
    fun page(body: String): CityScenePage {
        val root = Json.parseToJsonElement(body).jsonObject
        require(root.getValue("geometry_version").jsonPrimitive.let { it.isString && it.content == CityFloorGeometry.VERSION })
        require(root.getValue("placement_enabled").jsonPrimitive.let { !it.isString && it.boolean })
        val membership = InventoryReads.city(body)
        require(membership.buildings.values.all { it > 0 })
        val parcels = root.getValue("parcels").jsonArray.map { item ->
            val p = item.jsonObject
            require(p.getValue("kind").jsonPrimitive.let { it.isString && it.content == "land" })
            val id = p.getValue("id").jsonPrimitive.also { require(it.isString) }.content
            require(id == "base" || id in (1..5).map { "land_$it" })
            CityParcel(id, position(p.getValue("position")))
        }
        require(parcels.size in 1..6 && parcels.map { it.id }.distinct().size == parcels.size)
        require(parcels.single { it.id == "base" }.position == CityPosition(0, 0, 0))
        val positions = root.getValue("buildings").jsonArray.associate { item ->
            val b = item.jsonObject
            b.getValue("building_id").jsonPrimitive.content to b["position"]?.takeUnless { it == JsonNull }?.let(::position)
        }
        return CityScenePage(membership, parcels.sortedBy { it.id }, positions)
    }
}

/** Bracket all live reads with the city's account revision. A racing write discards the scene.
 * No placement mutation, wallet, cache or second authority is introduced here. */
class CitySceneRepository(private val backend: GameplayHttpBackend) {
    suspend fun load(session: BillingSession): CityScene {
        suspend fun page(cursor: String? = null): CityScenePage {
            cursor?.let(InventoryReads::id)
            return CitySceneReads.page(backend.read(session, "city?limit=20" + (cursor?.let { "&cursor=$it" } ?: "")))
        }
        val first = page()
        val members = linkedMapOf<String, Long>()
        val positions = mutableMapOf<String, CityPosition?>()
        var current = first
        var cursor: String? = null
        do {
            require(current.membership.revision == first.membership.revision &&
                current.membership.population == first.membership.population && current.parcels == first.parcels) { "City changed; refresh required" }
            require(current.membership.buildings.keys.none { it in members })
            require(cursor == null || current.membership.buildings.keys.all { it > cursor!! })
            members.putAll(current.membership.buildings); positions.putAll(current.positions)
            cursor = current.membership.next
            if (cursor != null) current = page(cursor)
        } while (cursor != null)
        val inventory = InventoryRepository(backend)
        val buildings = members.map { (id, revision) ->
            val building = inventory.building(session, id)
            require(building.revision == revision) { "Building changed; refresh required" }
            CitySceneBuilding(building, positions[id])
        }
        val decorations = mutableListOf<CitySceneDecoration>()
        val copies = mutableSetOf<String>()
        cursor = null
        do {
            val body = backend.read(session, "decorations?limit=20" + (cursor?.let { "&cursor=$it" } ?: ""))
            val parsed = DecorationReads.page(body)
            require(parsed.copies.all { copies.add(it.id) && (cursor == null || it.id > cursor!!) })
            val raw = Json.parseToJsonElement(body).jsonObject.getValue("copies").jsonArray
            parsed.copies.zip(raw).forEach { (copy, item) ->
                item.jsonObject["position"]?.takeUnless { it == JsonNull }?.let {
                    decorations.add(CitySceneDecoration(copy, CitySceneReads.position(it)))
                }
            }
            cursor = parsed.next
        } while (cursor != null)
        val last = page()
        require(last == first) { "City changed; refresh required" }
        return CityScene(first.membership.copy(buildings = members.toMap(), next = null), first.parcels, buildings, decorations)
    }
}
