package appbackend.wobbrik.game

import appbackend.wobbrik.billing.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class MarketPreviewReadsTest {
    private val session = BillingSession("alice", "epoch")
    private fun listing(i: Int, revision: Int = 1, status: String = "active") =
        """{"id":"l-$i","building_id":"b-$i","revision":$revision,"building_revision":1,"price":20,"fee":1,"status":"$status"}"""
    private fun detail(i: Int, revision: Int = 1) =
        """{"listing":${listing(i, revision)},"building":{"id":"b-$i","revision":1,"floors":10,"residents":40,"base_value":20,"max_price":24,"parts":{"wall":"w","window":"v","entrance":"e","roof":"r","lighting":"l"},"traded":false}}"""

    @Test fun previewReadsAreBoundedOrderedAndClosedOrChangedListingsHaveNoArt() = runBlocking {
        val active = AtomicInteger(); val peak = AtomicInteger(); val calls = AtomicInteger()
        val repository = MarketRepository(GameplayReads { expected, path ->
            assertEquals(session, expected); calls.incrementAndGet()
            if (path.startsWith("market/own?")) """{"listings":[${(1..12).joinToString(",") {listing(it)}},${listing(13, status="sold")}]}"""
            else {
                val now = active.incrementAndGet(); peak.updateAndGet {maxOf(it, now)}
                try {
                    delay(20)
                    val i = path.substringAfter("market/l-").toInt()
                    if (i == 1) throw GameplayHttpError(404, "not_found")
                    detail(i, if (i == 2) 2 else 1)
                } finally {active.decrementAndGet()}
            }
        })
        val result = repository.previewPage(session, true, MarketFilter())
        assertEquals(13, calls.get()); assertEquals(4, peak.get())
        assertEquals((3..12).map {"l-$it"}, result.previews.keys.toList())
        assertEquals(13, result.page.listings.size)
    }

    @Test fun failedOrRevokedDetailCannotBecomeSuccessfulCachedBundle() = runBlocking {
        for (failure in listOf(GameplayHttpError(403, "forbidden"), GameplayHttpError(503, "unavailable"), AccountChanged())) {
            val repository = MarketRepository(GameplayReads { _, path ->
                if (path.startsWith("market?")) """{"listings":[${listing(1)}]}""" else throw failure
            })
            val actual = runCatching {repository.previewPage(session, false, MarketFilter())}.exceptionOrNull()
            assertEquals(failure.javaClass, actual?.javaClass)
            if (failure is GameplayHttpError) assertEquals(failure.status, (actual as GameplayHttpError).status)
        }
    }

    @Test fun queryCacheIdentitySeparatesEveryFilterScopeAndCursor() {
        val repository = MarketRepository(GameplayReads { _, _ -> error("no network") })
        val base = repository.previewKey(false, MarketFilter())
        val variants = listOf(repository.previewKey(true, MarketFilter()),
            repository.previewKey(false, MarketFilter(minFloors=1)), repository.previewKey(false, MarketFilter(maxFloors=20)),
            repository.previewKey(false, MarketFilter(minPrice=1)), repository.previewKey(false, MarketFilter(maxPrice=20)),
            repository.previewKey(false, MarketFilter(partId="part")), repository.previewKey(false, MarketFilter(), "cursor"))
        assertFalse(base in variants); assertEquals(variants.size, variants.distinct().size)
    }
}
