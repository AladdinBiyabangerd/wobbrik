package appbackend.wobbrik.game

import java.util.UUID
import kotlinx.serialization.json.*

/** All purchases and edits use the application's existing recovery owner and durable journal. */
class PartsMutations(private val owner: AuthenticatedRecoveryOwner, private val operationId: () -> String = { UUID.randomUUID().toString() }) {
    fun buy(ready: AuthenticatedRecoveryOwner.State.Ready, itemId: String): Boolean {
        val item = ready.catalog?.items?.singleOrNull { it.id == itemId } ?: return false
        if (item.category !in FinishCosmetics.categories || item.method != "coins" || item.price == null ||
            item.price > ready.snapshot.coins || FinishCosmetics.available(item, ready.snapshot)) return false
        return owner.submit(ready) { GameplayWire.buyPart(operationId(), it.revision, itemId) }
    }
    fun apply(ready: AuthenticatedRecoveryOwner.State.Ready, building: OwnedBuilding, parts: Map<String, String>): Boolean {
        val catalog = ready.catalog ?: return false
        if (parts == building.parts || !FinishCosmetics.valid(catalog, ready.snapshot, parts)) return false
        return owner.submit(ready) { GameplayWire.editBuilding(operationId(), it.revision, building.id, building.revision, parts) }
    }
    companion object {
        fun receipt(body: String, command: AttemptCommand) {
            val receipt = RecoveryDtos.receipt(body)
            require(receipt.revision > command.expectedRevision)
            val result = receipt.result
            val request = Json.parseToJsonElement(command.envelope).jsonObject.getValue("request").jsonObject
            fun text(key: String) = result.getValue(key).jsonPrimitive.also { require(it.isString) }.content
            fun number(key: String) = result.getValue(key).jsonPrimitive.also { require(!it.isString) }.long.also { require(it >= 0) }
            when (command.route) {
                AttemptCommand.Route.SHOP_BUY -> {
                    require(text("item_id") == request.getValue("item_id").jsonPrimitive.content)
                    number("price")
                }
                AttemptCommand.Route.BUILDING_EDIT -> {
                    require(text("building_id") == request.getValue("building_id").jsonPrimitive.content)
                    require(number("building_revision") > request.getValue("building_revision").jsonPrimitive.long)
                    require(number("price") == 0L && number("fee") == 0L)
                }
                else -> error("Not a parts mutation")
            }
        }
    }
}
