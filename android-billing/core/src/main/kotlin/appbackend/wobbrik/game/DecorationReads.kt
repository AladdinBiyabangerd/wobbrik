package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*

data class DecorationCopy(val id: String, val itemId: String)
data class DecorationPage(val copies: List<DecorationCopy>, val next: String?)

/** Copy identities come only from the authenticated server; position is outside this flow. */
object DecorationReads {
    fun page(body: String): DecorationPage {
        val root = Json.parseToJsonElement(body).jsonObject
        fun JsonObject.id(key: String): String = getValue(key).jsonPrimitive.also {
            require(it.isString)
        }.content.also(InventoryReads::id)
        val copies = root.getValue("copies").jsonArray.map {
            val copy = it.jsonObject
            DecorationCopy(copy.id("id"), copy.id("item_id"))
        }
        require(copies.size <= 20 && copies.map { it.id }.distinct().size == copies.size)
        val next = root["next_cursor"]?.let { root.id("next_cursor") }
        require(next == null || next == copies.lastOrNull()?.id)
        return DecorationPage(copies, next)
    }
}

class DecorationRepository(private val backend: GameplayHttpBackend) {
    suspend fun page(session: BillingSession, cursor: String? = null): DecorationPage {
        cursor?.let(InventoryReads::id)
        return DecorationReads.page(backend.read(session, "decorations?limit=20" +
            (cursor?.let { "&cursor=$it" } ?: "")))
    }
}
