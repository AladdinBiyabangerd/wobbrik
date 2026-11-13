package appbackend.wobbrik.game

import java.math.BigInteger

/** Read-only explanation of v1 totals, never a quote or permission to sell. */
data class BuildingValueBreakdown(val construction: BigInteger, val capacity: BigInteger,
    val parts: BigInteger, val commissionAtMaximum: BigInteger, val sellerAtMaximum: BigInteger) {
    companion object {
        fun from(building: OwnedBuilding, catalog: RecoveryCatalog?): BuildingValueBreakdown? {
            if (catalog?.rulesVersion != "wobbrik-v1") return null
            val floors = building.floors.toBigInteger()
            val residents = building.residents.toBigInteger()
            if (residents != floors * BigInteger.valueOf(4)) return null
            val weights = FinishCosmetics.categories.map { category ->
                catalog.items.singleOrNull { it.id == building.parts[category] && it.category == category }
                    ?.weight?.takeIf { it >= 0 }?.toBigInteger() ?: return null
            }
            val capacity = residents / BigInteger.valueOf(4)
            val parts = weights.fold(BigInteger.ZERO, BigInteger::add)
            val base = floors + capacity + parts
            val maximum = building.maxPrice.toBigInteger()
            if (base != building.baseValue.toBigInteger() || maximum != base * BigInteger.valueOf(6) / BigInteger.valueOf(5)) return null
            val commission = ((maximum + BigInteger.valueOf(19)) / BigInteger.valueOf(20)).max(BigInteger.ONE)
            return BuildingValueBreakdown(floors, capacity, parts, commission, maximum - commission)
        }
    }
}
