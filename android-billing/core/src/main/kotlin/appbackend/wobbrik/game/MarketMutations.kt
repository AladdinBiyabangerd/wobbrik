package appbackend.wobbrik.game

import java.util.UUID
import kotlinx.serialization.json.*
import appbackend.wobbrik.game.MarketReads.number
import appbackend.wobbrik.game.MarketReads.text

/** Uses the sole durable wire journal. No local debit, transfer, grant or automatic rebase. */
class MarketMutations(private val owner: AuthenticatedRecoveryOwner, private val operationId: () -> String = { UUID.randomUUID().toString() }) {
    fun list(ready: AuthenticatedRecoveryOwner.State.Ready, building: OwnedBuilding, price: Long): Boolean {
        if (building.traded || price !in 1..building.maxPrice) return false
        return owner.submit(ready) { GameplayWire.listBuilding(operationId(), it.revision, building.id, building.revision, price) }
    }
    fun buy(ready: AuthenticatedRecoveryOwner.State.Ready, detail: MarketDetail): Boolean {
        if (detail.listing.status != "active" || detail.listing.price > ready.snapshot.coins) return false
        return submit(ready, detail.listing, AttemptCommand.Route.MARKET_BUY)
    }
    fun withdraw(ready: AuthenticatedRecoveryOwner.State.Ready, listing: MarketListing): Boolean {
        if (listing.status != "active") return false
        return submit(ready, listing, AttemptCommand.Route.MARKET_WITHDRAW)
    }
    private fun submit(ready: AuthenticatedRecoveryOwner.State.Ready, listing: MarketListing, route: AttemptCommand.Route) =
        owner.submit(ready) { GameplayWire.marketListing(operationId(), it.revision, route, listing.id, listing.revision) }
    companion object {
        val routes = setOf(AttemptCommand.Route.MARKET_LIST, AttemptCommand.Route.MARKET_WITHDRAW, AttemptCommand.Route.MARKET_BUY)
        fun fee(price: Long): Long { require(price > 0); return 1 + (price - 1) / 20 }
        fun receipt(body: String, command: AttemptCommand) {
            require(command.route in routes)
            val receipt = RecoveryDtos.receipt(body); require(receipt.revision > command.expectedRevision)
            val result = receipt.result
            val request = Json.parseToJsonElement(command.envelope).jsonObject.getValue("request").jsonObject
            val building = result.text("building_id").also(InventoryReads::id)
            val listing = result.text("listing_id").also(InventoryReads::id)
            val price = result.number("price"); require(price > 0 && result.number("fee") == fee(price))
            require(result.number("building_revision") > 0)
            when (command.route) {
                AttemptCommand.Route.MARKET_LIST -> {
                    require(building == request.text("building_id") && price == request.number("price"))
                    require(result.number("building_revision") == request.number("building_revision"))
                    require(result.number("listing_revision") == 1L)
                }
                else -> {
                    require(listing == request.text("listing_id"))
                    val before = request.number("listing_revision"); require(before < Long.MAX_VALUE)
                    require(result.number("listing_revision") == before + 1)
                }
            }
        }
    }
}
