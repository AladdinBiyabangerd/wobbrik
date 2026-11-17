package appbackend.wobbrik.game

import org.junit.Assert.*
import org.junit.Test

class DecorationOwnershipReadTest {
    @Test fun ownershipStatusUsesExistingServerPositionWithoutInventingPlacement() {
        val body = """{"copies":[{"id":"copy_a","item_id":"decor_tree_1"},{"id":"copy_b","item_id":"decor_tree_1","position":{"x_ticks":"25","y_ticks":"-10","quarter_turns":2}}]}"""
        val copies = DecorationReads.page(body).copies
        assertNull(copies[0].position)
        assertEquals(CityPosition(25, -10, 2), copies[1].position)
        assertThrows(IllegalArgumentException::class.java) { DecorationReads.page(body.replace("\"quarter_turns\":2", "\"quarter_turns\":4")) }
        assertThrows(IllegalArgumentException::class.java) { DecorationReads.page(body.replace("\"25\"", "25")) }
    }
}
