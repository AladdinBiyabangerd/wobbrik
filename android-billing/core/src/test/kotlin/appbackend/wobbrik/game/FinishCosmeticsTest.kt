package appbackend.wobbrik.game

import org.junit.Assert.*
import org.junit.Test

class FinishCosmeticsTest {
    private val free = FinishCosmetics.categories.map { CatalogItem("free_$it", it, it, "free", 0, null, null) }
    private val paid = CatalogItem("owned_wall", "Wall", "wall", "coins", 1, 5, null)
    private val catalog = RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", free + paid, emptyList())
    private val account = AccountRecovery("time", "time", "time", 1, 0, emptyList(), listOf(paid.id), emptyList(), emptyList())
    private val snapshot = GameSnapshot("wobbrik-v1", 1, 0, 0, null, account)

    @Test fun freeImplicitAndOwnedOnlyWithCurrentAccountUnlock() {
        val selected = FinishCosmetics.selection(catalog, snapshot, mapOf("wall" to paid.id))
        assertEquals(paid.id, selected["wall"])
        assertTrue(FinishCosmetics.valid(catalog, snapshot, selected))
        assertFalse(FinishCosmetics.valid(catalog, snapshot.copy(account = null), selected))
        val other = FinishCosmetics.selection(catalog, snapshot.copy(account = null), selected)
        assertEquals("free_wall", other["wall"])
        assertTrue(FinishCosmetics.valid(catalog, snapshot.copy(account = null), other))
        assertFalse(FinishCosmetics.valid(catalog, snapshot, selected + ("decoration" to "tree")))
        assertFalse(FinishCosmetics.valid(catalog, snapshot, selected + ("roof" to "free_window")))
        assertFalse(FinishCosmetics.valid(catalog.copy(engineVersion = "future"), snapshot, selected))
    }

    @Test fun noDefaultForCategoryWithoutAvailablePart() {
        val missing = catalog.copy(items = catalog.items.filter { it.category != "roof" })
        val parts = FinishCosmetics.selection(missing, snapshot, emptyMap())
        assertFalse(FinishCosmetics.valid(missing, snapshot, parts))
        assertFalse(parts.containsKey("roof"))
    }
}
