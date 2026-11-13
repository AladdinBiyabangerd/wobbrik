package appbackend.wobbrik.game

import org.junit.Assert.*
import org.junit.Test

class CityPurchasesTest {
    private val catalog = RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", listOf(
        CatalogItem("decor_tree_1", "Tək ağac", "tree", "copy", 0, 25, null),
        CatalogItem("free_wall", "Wall", "wall", "free", 0, null, null)),
        listOf(LandOffer("land_1", 150, 200), LandOffer("land_2", 500, 400)))
    private fun snapshot(xp: Long = 500, coins: Long = 400, land: List<String> = emptyList()) =
        GameSnapshot("wobbrik-v1", 1, coins, xp, null, AccountRecovery("now", "first", "later",
            5, 0, emptyList(), listOf("decor_tree_1"), land, emptyList()))

    @Test fun landUsesIndependentXpThresholdAndPermanentOwnership() {
        assertTrue(CityPurchases.eligible(catalog, snapshot(), "land_2")) // land_1 is not required
        assertFalse(CityPurchases.eligible(catalog, snapshot(xp = 499), "land_2"))
        assertFalse(CityPurchases.eligible(catalog, snapshot(coins = 399), "land_2"))
        assertFalse(CityPurchases.eligible(catalog, snapshot(land = listOf("land_2")), "land_2"))
        assertTrue(CityPurchases.eligible(catalog, snapshot(xp = 150, coins = 200), "land_1"))
    }

    @Test fun copiesRemainRepeatableAndFailClosedWithoutServerState() {
        assertTrue(CityPurchases.eligible(catalog, snapshot(coins = 25), "decor_tree_1"))
        assertFalse(CityPurchases.eligible(catalog, snapshot(coins = 24), "decor_tree_1"))
        assertFalse(CityPurchases.eligible(catalog, snapshot(), "free_wall"))
        assertFalse(CityPurchases.eligible(catalog, snapshot(), "unknown"))
        assertFalse(CityPurchases.eligible(null, snapshot(), "land_1"))
        assertFalse(CityPurchases.eligible(catalog, snapshot().copy(account = null), "land_1"))
    }

    @Test fun ownedCopiesReadIndividuallyAndRejectMalformedPages() {
        val body = """{"copies":[{"id":"copy_a","item_id":"decor_tree_1","position":null},{"id":"copy_b","item_id":"decor_tree_1"}],"next_cursor":"copy_b"}"""
        val page = DecorationReads.page(body)
        assertEquals(2, page.copies.size)
        assertEquals("copy_b", page.next)
        assertEquals(emptyList<DecorationCopy>(), DecorationReads.page("""{"copies":[]}""").copies)
        for (bad in listOf(body.replace("copy_b", "copy_a"), body.replace("\"next_cursor\":\"copy_b\"", "\"next_cursor\":\"other\""), body.replace("copy_a", "bad/id"))) {
            assertThrows(IllegalArgumentException::class.java) { DecorationReads.page(bad) }
        }
    }

    @Test fun decorationReceiptRequiresServerCopyIdentity() {
        val command = GameplayWire.buyPart("purchase", 0, "decor_tree_1")
        val body = """{"revision":1,"accepted_at":"2026-11-13T00:00:00Z","result":{"item_id":"decor_tree_1","price":25,"copy_id":"copy_a"}}"""
        PartsMutations.receipt(body, command)
        assertThrows(IllegalArgumentException::class.java) { PartsMutations.receipt(body.replace("copy_a", "bad/id"), command) }
        assertThrows(NoSuchElementException::class.java) { PartsMutations.receipt(body.replace(",\"copy_id\":\"copy_a\"", ""), command) }
    }
}
