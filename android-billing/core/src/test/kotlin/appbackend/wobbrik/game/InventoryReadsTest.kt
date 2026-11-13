package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class InventoryReadsTest {
    private val building = """{"id":"b-1","revision":1,"floors":10,"residents":40,"base_value":20,"max_price":24,"parts":{"wall":"w","window":"v","entrance":"e","roof":"r","lighting":"l"},"traded":false}"""
    private fun city(revision: Int, entries: String, next: String = "") =
        """{"rules_version":"wobbrik-v1","revision":$revision,"population":"18446744073709551616","buildings":[$entries]$next}"""
    @Test fun legacyBuildingDoesNotAcquireInventedGeometry() {
        val result = InventoryReads.building(building)
        assertNull(result.geometry); assertEquals(40L, result.residents)
    }
    @Test fun missingPartFailsClosed() {
        rejected { InventoryReads.building(building.replace(",\"lighting\":\"l\"", "")) }
    }
    @Test fun cursorMustMatchLastReturnedBuilding() {
        rejected { InventoryReads.inventory("""{"buildings":[$building],"next_cursor":"other"}""") }
    }
    @Test fun populationRemainsExactBeyondLong() {
        assertEquals("18446744073709551616", InventoryReads.city(city(1, "")).population.toString())
    }
    @Test fun onlyBoundedReadPathsAreAllowed() {
        assertTrue(allowedReadPath("inventory?limit=20&cursor=b-1"))
        assertTrue(allowedReadPath("buildings/b-1"))
        listOf("buildings/../sync", "inventory?limit=100000", "inventory?limit=20&uid=other", "city/placement").forEach {
            assertFalse(it, allowedReadPath(it))
        }
    }
    @Test fun cityRejectsMixedRevisions() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        var calls = 0
        val backend = GameplayHttpBackend(sessions, GameplayTransport { _, _, _ ->
            calls++
            GameplayResponse(200, if (calls == 1) city(1, """{"building_id":"b-1","building_revision":1}""", """, "next_cursor":"b-1"""") else city(2, ""))
        })
        try { InventoryRepository(backend).city(requireNotNull(sessions.current())); fail("mixed revisions accepted") }
        catch (_: IllegalArgumentException) { }
    }
    @Test fun lateReadAfterSameUidLoginIsRejected() = runBlocking {
        val sessions = AccountSessions { "token" }; sessions.onAuthChanged("alice")
        val old = requireNotNull(sessions.current())
        val backend = GameplayHttpBackend(sessions, GameplayTransport { _, _, _ ->
            sessions.onAuthChanged("alice"); GameplayResponse(200, """{"buildings":[]}""")
        })
        try { InventoryRepository(backend).page(old); fail("stale response accepted") } catch (_: AccountChanged) { }
    }
    private fun rejected(block: () -> Unit) {
        try { block(); fail("invalid read accepted") } catch (_: IllegalArgumentException) { }
    }
}
