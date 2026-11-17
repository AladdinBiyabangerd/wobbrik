package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*
import appbackend.wobbrik.billing.AccountChanged
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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
/** One query page with optional matching previews; cached only as a complete read bundle. */
data class MarketPreviewPage(val page: MarketPage, val previews: Map<String, MarketDetail>)

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
    /** Absent on older servers. A present array is authoritative, including an empty one. */
    fun previews(body: String, page: MarketPage): Map<String, MarketDetail>? {
        val r = Json.parseToJsonElement(body).jsonObject
        val raw = r["previews"] ?: return null
        val details = raw.jsonArray.map { detail(it.jsonObject) }
        require(details.size <= page.listings.size && details.map { it.listing.id }.distinct().size == details.size)
        val listings = page.listings.associateBy { it.id }
        require(details.all { listings[it.listing.id] == it.listing })
        return details.associateBy { it.listing.id }
    }
    fun detail(body: String): MarketDetail = detail(Json.parseToJsonElement(body).jsonObject)
    private fun detail(r: JsonObject): MarketDetail {
        val listing = listing(r.getValue("listing").jsonObject)
        val building = InventoryReads.building(r.getValue("building").jsonObject)
        require(listing.status == "active" && building.id == listing.buildingId && building.revision == listing.buildingRevision)
        return MarketDetail(listing, building)
    }
    internal fun allowedPath(path: String): Boolean =
        Regex("market/[A-Za-z0-9_-]{1,128}").matches(path) ||
            Regex("market(?:/own)?\\?limit=20(?:&cursor=[A-Za-z0-9_-]{1,128})?&min_floors=[0-9]{1,19}&max_floors=[0-9]{1,19}&min_price=[0-9]{1,19}&max_price=[0-9]{1,19}(?:&part_id=[A-Za-z0-9_-]{1,128})?(?:&include=previews)?").matches(path)
}

class MarketRepository(private val backend: GameplayReads) {
    suspend fun page(session: BillingSession, own: Boolean, filter: MarketFilter, cursor: String? = null): MarketPage {
        return MarketReads.page(backend.read(session, pagePath(own, filter, cursor)), own)
    }
    private fun pagePath(own: Boolean, filter: MarketFilter, cursor: String?): String {
        cursor?.let(InventoryReads::id)
        return "market" + (if (own) "/own" else "") + "?limit=20" +
            (cursor?.let { "&cursor=$it" } ?: "") + filter.query()
    }
    /** Stable query identity: private/public, filters and cursor never share a cached bundle. */
    fun previewKey(own: Boolean, filter: MarketFilter, cursor: String? = null): String =
        "market:$own:${filter.minFloors}:${filter.maxFloors}:${filter.minPrice}:${filter.maxPrice}:${filter.partId.orEmpty()}:${cursor.orEmpty()}"

    suspend fun previewPage(session: BillingSession, own: Boolean, filter: MarketFilter, cursor: String? = null): MarketPreviewPage {
        val body = backend.read(session, pagePath(own, filter, cursor) + "&include=previews")
        val page = MarketReads.page(body, own)
        MarketReads.previews(body, page)?.let { return MarketPreviewPage(page, it) }
        // Older servers ignore the additive query option and keep the bounded detail path.
        val gate = Semaphore(4)
        val previews = coroutineScope {
            page.listings.filter { it.status == "active" }.map { listing -> async {
                gate.withPermit {
                    try {
                        val detail = detail(session, listing.id)
                        // A listing may close/change between page and detail; never pair mismatched versions.
                        if (detail.listing == listing) listing.id to detail else null
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (changed: AccountChanged) { throw changed }
                    catch (failure: GameplayHttpError) {
                        if (failure.status != 404) throw failure
                        null // Closed listing: retain the existing unavailable-card presentation.
                    }
                }
            } }.awaitAll().filterNotNull().toMap()
        }
        return MarketPreviewPage(page, previews)
    }

    suspend fun detail(session: BillingSession, id: String): MarketDetail {
        InventoryReads.id(id)
        return MarketReads.detail(backend.read(session, "market/$id")).also { require(it.listing.id == id) }
    }
}
