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
    @Test fun embeddedPreviewsUseOneReadAndKeepEmptyOrClosedEntriesUnavailable() = runBlocking {
        for (empty in listOf(false, true)) {
            var calls = 0
            val repository = MarketRepository(GameplayReads { _, path ->
                calls++
                assertTrue(path.endsWith("&include=previews"))
                assertTrue(allowedReadPath(path))
                """{"listings":[${listing(1)},${listing(2, status="sold")}],"previews":[${if (empty) "" else detail(1)}]}"""
            })
            val result = repository.previewPage(session, true, MarketFilter(partId="wall"), "cursor")
            assertEquals(1, calls)
            assertEquals(if (empty) emptySet<String>() else setOf("l-1"), result.previews.keys)
            assertEquals(2, result.page.listings.size)
        }
    }

    @Test fun malformedEmbeddedPreviewsCannotFallBackOrPairWrongVersions() = runBlocking {
        for (raw in listOf("null", "{}", "[${detail(2)}]", "[${detail(1, revision=2)}]",
            "[${detail(1)},${detail(1)}]")) {
            var calls = 0
            val repository = MarketRepository(GameplayReads { _, _ ->
                calls++
                """{"listings":[${listing(1)}],"previews":$raw}"""
            })
            assertNotNull(runCatching { repository.previewPage(session, false, MarketFilter()) }.exceptionOrNull())
            assertEquals(1, calls)
        }
    }

    @Test fun bundledCaptureRemovesDetailFanoutAndReplaysWithoutNetwork() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val login = sessions.current()!!
        val snapshot = """{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
        val account = ReadOnlyAccount("alice", RecoveryDtos.snapshot(snapshot),
            RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        var old: MarketPreviewPage? = null
        for (embedded in listOf(false, true)) {
            val calls = AtomicInteger(); val bytes = AtomicInteger()
            val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
                calls.incrementAndGet(); delay(40)
                val body = when {
                    path == "sync" -> snapshot
                    path.startsWith("market?") -> """{"listings":[${(1..12).joinToString(",") { listing(it) }}]${if (embedded) ",\"previews\":[${(1..12).joinToString(",") { detail(it) }}]" else ""}}"""
                    else -> detail(path.substringAfter("market/l-").toInt())
                }
                bytes.addAndGet(body.toByteArray(Charsets.UTF_8).size)
                GameplayResponse(200, body)
            })
            val rows = mutableMapOf<String, ByteArray>()
            val cache = ReadOnlyGameCache { key -> object : JournalFile {
                override fun read() = rows[key]
                override fun replace(bytes: ByteArray) { rows[key] = bytes.clone() }
            } }
            val start = System.nanoTime()
            val result = cache.capture(login, account, "market", backend) {
                MarketRepository(it).previewPage(login, false, MarketFilter())
            }
            println("MARKET-BUNDLE embedded=$embedded requests=${calls.get()} bytes=${bytes.get()} elapsedMs=${(System.nanoTime()-start)/1_000_000}")
            assertEquals(if (embedded) 2 else 14, calls.get())
            if (embedded) assertEquals(old, result) else old = result
            if (!embedded) {
                // Simulate a complete bundle captured before include=previews existed.
                cache.capture(login, account, "legacy", backend) { reads ->
                    val repository = MarketRepository(reads)
                    val page = repository.page(login, false, MarketFilter())
                    MarketPreviewPage(page, page.listings.associate { it.id to repository.detail(login, it.id) })
                }
                val beforeReplay = calls.get()
                assertEquals(result, cache.replay(login, account, "legacy") {
                    MarketRepository(it).previewPage(login, false, MarketFilter())
                })
                assertEquals(beforeReplay, calls.get())
            }
            val beforeReplay = calls.get()
            val replay = cache.replay(login, account, "market") {
                MarketRepository(it).previewPage(login, false, MarketFilter())
            }
            assertEquals(result, replay)
            assertEquals(beforeReplay, calls.get())
        }
    }

}
