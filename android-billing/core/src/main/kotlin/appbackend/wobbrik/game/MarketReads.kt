package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*

/** Inclusive server filters; one immutable value owns a cursor traversal. */
data class MarketFilter(val minFloors: Long = 0, val maxFloors: Long = 0,
    val minPrice: Long = 0, val maxPrice: Long = 0, val partId: String? = null) {
    init {
        require(listOf(minFloors, maxFloors, minPrice, maxPrice).all { it >= 0 })
        require(maxFloors == 0L || maxFloors >= minFloors)
        require(maxPrice == 0L || maxPrice >= minPrice)
        partId?.let(InventoryReads::id)
    }
    internal fun query() = "&min_floors=$minFloors&max_floors=$maxFloors&min_price=$minPrice&max_price=$maxPrice" +
        (partId?.let { "&part_id=$it" } ?: "")
}
data class MarketListing(val id: String, val buildingId: String, val revision: Long,
    val buildingRevision: Long, val price: Long, val fee: Long, val status: String)
data class MarketPage(val listings: List<MarketListing>, val next: String?)
data class MarketDetail(val listing: MarketListing, val building: OwnedBuilding)

object MarketReads {
    internal fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.also { require(it.isString) }.content
    internal fun JsonObject.number(key: String) = getValue(key).jsonPrimitive.also { require(!it.isString) }.long.also { require(it >= 0) }
    fun listing(r: JsonObject): MarketListing {
        val price = r.number("price"); require(price > 0)
        val fee = r.number("fee"); require(fee == MarketMutations.fee(price))
        return MarketListing(r.text("id").also(InventoryReads::id), r.text("building_id").also(InventoryReads::id),
            r.number("revision").also { require(it > 0) }, r.number("building_revision").also { require(it > 0) },
            price, fee, r.text("status").also { require(it in setOf("active", "withdrawn", "sold")) })
    }
    fun page(body: String, own: Boolean): MarketPage {
        val r = Json.parseToJsonElement(body).jsonObject
        val listings = r.getValue("listings").jsonArray.map { listing(it.jsonObject) }
        require(listings.size <= 20 && listings.map { it.id }.distinct().size == listings.size)
        require(own || listings.all { it.status == "active" })
        val next = r["next_cursor"]?.let { r.text("next_cursor").also(InventoryReads::id) }
        require(next == null || next == listings.lastOrNull()?.id)
        return MarketPage(listings, next)
    }
    fun detail(body: String): MarketDetail {
        val r = Json.parseToJsonElement(body).jsonObject
        val listing = listing(r.getValue("listing").jsonObject)
        val building = InventoryReads.building(r.getValue("building").jsonObject)
        require(listing.status == "active" && building.id == listing.buildingId && building.revision == listing.buildingRevision)
        return MarketDetail(listing, building)
    }
    internal fun allowedPath(path: String): Boolean =
        Regex("market/[A-Za-z0-9_-]{1,128}").matches(path) ||
            Regex("market(?:/own)?\\?limit=20(?:&cursor=[A-Za-z0-9_-]{1,128})?&min_floors=[0-9]{1,19}&max_floors=[0-9]{1,19}&min_price=[0-9]{1,19}&max_price=[0-9]{1,19}(?:&part_id=[A-Za-z0-9_-]{1,128})?").matches(path)
}

class MarketRepository(private val backend: GameplayReads) {
    suspend fun page(session: BillingSession, own: Boolean, filter: MarketFilter, cursor: String? = null): MarketPage {
        cursor?.let(InventoryReads::id)
        return MarketReads.page(backend.read(session, "market" + (if (own) "/own" else "") + "?limit=20" +
            (cursor?.let { "&cursor=$it" } ?: "") + filter.query()), own)
    }
    suspend fun detail(session: BillingSession, id: String): MarketDetail {
        InventoryReads.id(id)
        return MarketReads.detail(backend.read(session, "market/$id")).also { require(it.listing.id == id) }
    }
}
