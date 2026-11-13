package appbackend.wobbrik.game

import org.junit.Assert.*
import org.junit.Test

class BuildingValueBreakdownTest {
    private val categories = FinishCosmetics.categories
    private fun catalog(weight: Long = 0) = RecoveryCatalog("wobbrik-v1", "engine-v1",
        categories.map { CatalogItem(it, it, it, "free", weight, null, null) }, emptyList())
    private fun building(floors: Long = 10, base: Long = 20, maximum: Long = 24) =
        OwnedBuilding("b", 1, floors, floors * 4, base, maximum, categories.associateWith { it }, false, null)
    @Test fun approvedExampleRoundsCommissionUp() {
        val result = requireNotNull(BuildingValueBreakdown.from(building(), catalog()))
        assertEquals("10", result.construction.toString())
        assertEquals("10", result.capacity.toString())
        assertEquals("0", result.parts.toString())
        assertEquals("2", result.commissionAtMaximum.toString())
        assertEquals("22", result.sellerAtMaximum.toString())
    }
    @Test fun countsEachEquippedPartOnce() {
        val result = requireNotNull(BuildingValueBreakdown.from(building(35, 190, 228), catalog(24)))
        assertEquals("120", result.parts.toString())
        assertEquals("12", result.commissionAtMaximum.toString())
        assertEquals("216", result.sellerAtMaximum.toString())
    }
    @Test fun doesNotExplainInconsistentOrUnknownServerValues() {
        assertNull(BuildingValueBreakdown.from(building(base = 21), catalog()))
        assertNull(BuildingValueBreakdown.from(building(maximum = 25), catalog()))
        assertNull(BuildingValueBreakdown.from(building(), catalog().copy(items = emptyList())))
        assertNull(BuildingValueBreakdown.from(building(), catalog().copy(rulesVersion = "future")))
        assertNull(BuildingValueBreakdown.from(building().copy(residents = 41), catalog()))
    }
    @Test fun rejectsOverflowSizedInconsistentValuesWithoutWrapping() {
        assertNull(BuildingValueBreakdown.from(building().copy(floors = Long.MAX_VALUE, residents = Long.MAX_VALUE), catalog()))
    }
}
