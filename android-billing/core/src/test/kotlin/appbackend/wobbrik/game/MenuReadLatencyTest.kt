package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountSessions
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/** Deterministic network fixture, not a production/device latency claim. */
class MenuReadLatencyTest {
    private val count = 12
    private fun building(id: String) = """{"id":"$id","revision":1,"floors":10,"residents":40,"base_value":20,"max_price":24,"parts":{"wall":"w","window":"v","entrance":"e","roof":"r","lighting":"l"},"traded":false}"""
    private fun listing(i: Int) = """{"id":"l-$i","building_id":"b-$i","revision":1,"building_revision":1,"price":20,"fee":1,"status":"active"}"""
    private fun city() = """{"rules_version":"wobbrik-v1","geometry_version":"city-geometry-v1","placement_enabled":true,"revision":1,"population":"480","parcels":[{"kind":"land","id":"base","position":{"x_ticks":"0","y_ticks":"0","quarter_turns":0}}],"buildings":[${(1..count).map { "b-$it" }.sorted().joinToString(",") { """{"building_id":"$it","building_revision":1}""" }}]}"""
    private class Meter {
        val requests = AtomicInteger(); val bytes = AtomicInteger(); val active = AtomicInteger(); val peak = AtomicInteger()
    }
    private fun backend(sessions: AccountSessions, m: Meter) = GameplayHttpBackend(sessions, GameplayTransport { path, _, body ->
        assertNull(body)
        m.requests.incrementAndGet()
        val active = m.active.incrementAndGet(); m.peak.updateAndGet { maxOf(it, active) }
        try {
            delay(40)
            val response = when {
                path.startsWith("city?") -> city()
                path.startsWith("buildings/") -> building(path.substringAfter('/'))
                path.startsWith("decorations?") -> """{"copies":[]}"""
                path.startsWith("market?") -> """{"listings":[${(1..count).joinToString(",", transform = ::listing)}]}"""
                path.startsWith("market/") -> path.substringAfter("market/l-").toInt().let { """{"listing":${listing(it)},"building":${building("b-$it")}}""" }
                path == "sync" -> """{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
                else -> error(path)
            }
            m.bytes.addAndGet(response.toByteArray(Charsets.UTF_8).size)
            GameplayResponse(200, response)
        } finally { m.active.decrementAndGet() }
    })
    @Test fun cityCaptureAndReplayTraffic() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val meter = Meter(); val backend = backend(sessions, meter)
        val account = ReadOnlyAccount("alice", backend.sync(session), RecoveryDtos.catalog("""{"rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1","items":[],"land_offers":[]}"""))
        val files = mutableMapOf<String, ByteArray>()
        val cache = ReadOnlyGameCache { key -> object : JournalFile {
            override fun read() = files[key]
            override fun replace(bytes: ByteArray) { files[key] = bytes.clone() }
        } }
        meter.requests.set(0); meter.bytes.set(0)
        var start = System.nanoTime()
        val scene = withContext(Dispatchers.IO) { cache.capture(session, account, "city", backend) { CitySceneRepository(it).load(session) } }
        println("MENU city-capture elapsedMs=${(System.nanoTime()-start)/1_000_000} requests=${meter.requests} responseUtf8Bytes=${meter.bytes}")
        assertEquals(count+4, meter.requests.get())
        meter.requests.set(0); meter.bytes.set(0)
        start = System.nanoTime()
        assertEquals(scene, cache.replay(session, account, "city") { CitySceneRepository(it).load(session) })
        println("MENU city-replay elapsedMs=${(System.nanoTime()-start)/1_000_000} requests=${meter.requests} responseUtf8Bytes=${meter.bytes}")
        assertEquals(0, meter.requests.get()); assertEquals(0, meter.bytes.get())
    }
    @Test fun parallelBuildingConflictAndEpochChangeFailClosed() = runBlocking {
        for (changeEpoch in listOf(false, true)) {
            val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
            val session = sessions.current()!!
            val live = backend(sessions, Meter())
            val reader = GameplayReads { expected, path ->
                val body = live.read(expected, path)
                if (path == "buildings/b-1") {
                    if (changeEpoch) sessions.onAuthChanged("alice")
                    body.replace("\"revision\":1", "\"revision\":2")
                } else body
            }
            val failure = runCatching { CitySceneRepository(reader).load(session) }.exceptionOrNull()
            assertNotNull("racing scene published", failure)
            assertTrue(failure is IllegalArgumentException || failure is appbackend.wobbrik.billing.AccountChanged)
        }
    }
    @Test fun cityAndMarketReadBaseline() = runBlocking {
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = sessions.current()!!
        val cityMeter = Meter(); val cityBackend = backend(sessions, cityMeter)
        var start = System.nanoTime()
        val scene = CitySceneRepository(cityBackend).load(session)
        println("MENU city elapsedMs=${(System.nanoTime()-start)/1_000_000} requests=${cityMeter.requests} responseUtf8Bytes=${cityMeter.bytes} peak=${cityMeter.peak}")
        assertEquals(4, cityMeter.peak.get()); assertEquals(count, scene.buildings.size); assertEquals(count+3, cityMeter.requests.get())
        assertEquals((1..count).map { "b-$it" }.sorted(), scene.buildings.map { it.building.id })
        val marketMeter = Meter(); val market = MarketRepository(backend(sessions, marketMeter))
        start = System.nanoTime()
        val page = market.page(session, false, MarketFilter())
        val gate = Semaphore(4)
        val previews = coroutineScope { page.listings.map { async { gate.withPermit { market.detail(session, it.id) } } }.awaitAll() }
        println("MENU market elapsedMs=${(System.nanoTime()-start)/1_000_000} requests=${marketMeter.requests} responseUtf8Bytes=${marketMeter.bytes} peak=${marketMeter.peak}")
        assertEquals(count, previews.size); assertEquals(count+1, marketMeter.requests.get()); assertEquals(4, marketMeter.peak.get())
    }
}
