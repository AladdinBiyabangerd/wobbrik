package appbackend.wobbrik.game

import java.util.UUID
import kotlinx.serialization.json.*

/** Deletion is server-owned and uses the same durable journal as gameplay and purchases. */
class BuildingDeletion(private val owner: AuthenticatedRecoveryOwner,
    private val operationId: () -> String = { UUID.randomUUID().toString() }) {
    /** The caller must obtain explicit confirmation before invoking this destructive operation. */
    fun confirmedDelete(ready: AuthenticatedRecoveryOwner.State.Ready, building: OwnedBuilding): Boolean =
        owner.submit(ready) {
            GameplayWire.deleteBuilding(operationId(), it.revision, building.id, building.revision)
        }

    companion object {
        fun receipt(body: String, command: AttemptCommand) {
            require(command.route == AttemptCommand.Route.BUILDING_DELETE)
            val receipt = RecoveryDtos.receipt(body)
            require(receipt.revision > command.expectedRevision)
            val request = Json.parseToJsonElement(command.envelope).jsonObject.getValue("request").jsonObject
            val result = receipt.result
            val id = result.getValue("building_id").jsonPrimitive
            require(id.isString && id.content == request.getValue("building_id").jsonPrimitive.content)
            fun number(key: String) = result.getValue(key).jsonPrimitive.also { require(!it.isString) }.long
            require(number("building_revision") > request.getValue("building_revision").jsonPrimitive.long)
            require(number("price") == 0L && number("fee") == 0L)
        }
    }
}
