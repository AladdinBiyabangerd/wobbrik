package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Read-only scene coordinates. Absence is legacy membership, never an invented origin. */
data class CityPosition(val xTicks: Long, val yTicks: Long, val quarterTurns: Int)
data class CityParcel(val id: String, val position: CityPosition)
data class CitySceneBuilding(val building: OwnedBuilding, val position: CityPosition?)
data class CitySceneDecoration(val copy: DecorationCopy, val position: CityPosition)
data class CityScene(val membership: CityMembership, val parcels: List<CityParcel>,
    val buildings: List<CitySceneBuilding>, val decorations: List<CitySceneDecoration>)

internal data class CityScenePage(val membership: CityMembership, val parcels: List<CityParcel>,
    val positions: Map<String, CityPosition?>, val details: Map<String, OwnedBuilding> = emptyMap())

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
        val details = root.getValue("buildings").jsonArray.mapNotNull { item ->
            val b = item.jsonObject
            b["detail"]?.let { raw ->
                val detail = InventoryReads.building(raw.jsonObject)
                val id = b.getValue("building_id").jsonPrimitive.content
                require(detail.id == id && detail.revision == membership.buildings.getValue(id)) {
                    "Building changed; refresh required"
                }
                detail.id to detail
            }
        }.toMap()
        return CityScenePage(membership, parcels.sortedBy { it.id }, positions, details)
    }
}

/** Bracket all live reads with the city's account revision. A racing write discards the scene.
 * No placement mutation, wallet, cache or second authority is introduced here. */
class CitySceneRepository(private val backend: GameplayReads) {
    suspend fun load(session: BillingSession): CityScene {
        var supportsDetails = true
        suspend fun page(cursor: String? = null, details: Boolean = true): CityScenePage {
            cursor?.let(InventoryReads::id)
            val path = "city?limit=20" + (cursor?.let { "&cursor=$it" } ?: "")
            val body = if (details && supportsDetails) {
                try { backend.read(session, "$path&include=details") }
                catch (failure: GameplayHttpError) {
                    // Older City handlers strictly reject unknown query parameters.
                    // Never retry auth, network, rate-limit or malformed-body failures.
                    if (failure.status != 400 || failure.code != "invalid_request") throw failure
                    supportsDetails = false
                    backend.read(session, path)
                }
            } else backend.read(session, path)
            return CitySceneReads.page(body)
        }
        val first = page()
        val members = linkedMapOf<String, Long>()
        val positions = mutableMapOf<String, CityPosition?>()
        val details = mutableMapOf<String, OwnedBuilding>()
        var current = first
        var cursor: String? = null
        do {
            require(current.membership.revision == first.membership.revision &&
                current.membership.population == first.membership.population && current.parcels == first.parcels) { "City changed; refresh required" }
            require(current.membership.buildings.keys.none { it in members })
            require(cursor == null || current.membership.buildings.keys.all { it > cursor!! })
            members.putAll(current.membership.buildings); positions.putAll(current.positions)
            details.putAll(current.details)
            cursor = current.membership.next
            if (cursor != null) current = page(cursor)
        } while (cursor != null)
        val inventory = InventoryRepository(backend)
        // Only independent detail reads overlap. Membership pagination and the final
        // revision bracket stay ordered; awaitAll preserves the server membership order.
        val gate = Semaphore(4)
        val buildings = coroutineScope {
            members.map { (id, revision) -> async {
                gate.withPermit {
                    val building = details[id] ?: inventory.building(session, id)
                    require(building.revision == revision) { "Building changed; refresh required" }
                    CitySceneBuilding(building, positions[id])
                }
            } }.awaitAll()
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
        val last = page(details = false)
        require(last.copy(details = emptyMap()) == first.copy(details = emptyMap())) { "City changed; refresh required" }
        return CityScene(first.membership.copy(buildings = members.toMap(), next = null), first.parcels, buildings, decorations)
    }
}
