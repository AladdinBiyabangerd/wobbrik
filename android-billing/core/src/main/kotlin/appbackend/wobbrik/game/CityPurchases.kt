package appbackend.wobbrik.game

import java.util.UUID

/** Buys ownership only. No placement, local wallet delta or second journal. */
class CityPurchases(private val owner: AuthenticatedRecoveryOwner,
    private val operationId: () -> String = { UUID.randomUUID().toString() }) {
    fun buy(ready: AuthenticatedRecoveryOwner.State.Ready, itemId: String): Boolean {
        if (!eligible(ready.catalog, ready.snapshot, itemId)) return false
        return owner.submit(ready) { GameplayWire.buyPart(operationId(), it.revision, itemId) }
    }

    companion object {
        fun eligible(catalog: RecoveryCatalog?, snapshot: GameSnapshot, itemId: String): Boolean {
            catalog ?: return false
            // Require a complete server-owned account snapshot before purchasing rights.
            val account = snapshot.account ?: return false
            val land = catalog.landOffers.singleOrNull { it.id == itemId }
            if (land != null) return land.price > 0 && land.price <= snapshot.coins &&
                snapshot.xp >= land.requiredXp && itemId !in account.land
            val decor = catalog.items.singleOrNull { it.id == itemId } ?: return false
            // A copy is repeatable; unlocks never turn it into a reusable right.
            return decor.method == "copy" && decor.price?.let { it > 0 && it <= snapshot.coins } == true
        }
    }
}
