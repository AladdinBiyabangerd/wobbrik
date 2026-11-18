package com.aladdin.wobbrik

import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class DecorationInventoryReadsTest {
    @Test fun boundedCopiesReplayWithoutCityOrPerCardReads() = runBlocking {
        val rows = mutableMapOf<String, ByteArray>()
        val cache = ReadOnlyGameCache { key -> object : JournalFile {
            override fun read() = rows[key]
            override fun replace(bytes: ByteArray) { rows[key] = bytes.clone() }
        } }
        val sessions = AccountSessions { "token" }.also { it.onAuthChanged("alice") }
        val session = requireNotNull(sessions.current())
        val sync = """{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
        val snapshot = RecoveryDtos.snapshot(sync)
        val account = ReadOnlyAccount("alice", snapshot, RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1", emptyList(), emptyList()))
        val view = AccountView(account, AuthenticatedRecoveryOwner.State.Ready(snapshot, account.catalog, "alice"))
        val page = """{"copies":[${(1..20).joinToString(",") { """{"id":"copy_${it.toString().padStart(2,'0')}","item_id":"decor_tree_1"}""" }}],"next_cursor":"copy_20"}"""
        val paths = mutableListOf<String>()
        var bytes = 0
        var gate: CompletableDeferred<Unit>? = null
        val backend = GameplayHttpBackend(sessions, GameplayTransport { path, _, _ ->
            gate?.await()
            paths += path
            val body = when (path) {
                "sync" -> sync
                "decorations?limit=20" -> page
                else -> error("Unexpected city/per-card/unbounded request: $path")
            }
            bytes += body.toByteArray().size
            GameplayResponse(200, body)
        })
        val views = AccountViews(backend, cache, sessions)
        suspend fun read(refresh: Boolean = false, local: ((DecorationPage) -> Unit)? = null) =
            views.read(view, session, "owned-decorations-", refresh = refresh, onLocal = local) {
                DecorationRepository(it).page(session)
            }
        assertEquals(20, read().copies.size)
        assertEquals(listOf("decorations?limit=20", "sync"), paths)
        val coldBytes = bytes
        repeat(3) { assertEquals(20, read().copies.size) }
        assertEquals(2, paths.size); assertEquals(coldBytes, bytes)
        // Accepted cache publishes before refresh network is allowed to finish.
        val local = CompletableDeferred<DecorationPage>()
        gate = CompletableDeferred()
        val refreshing = async { read(true) { local.complete(it) } }
        assertEquals(20, local.await().copies.size)
        assertEquals(2, paths.size)
        gate!!.complete(Unit); refreshing.await(); gate = null
        assertEquals(4, paths.size)
        val beforeOffline = paths.size
        val offline = views.read(AccountView(account, null), session, "owned-decorations-") { DecorationRepository(it).page(session) }
        assertEquals(20, offline.copies.size); assertEquals(beforeOffline, paths.size)
        sessions.onAuthChanged("bob")
        assertTrue(runCatching { read() }.exceptionOrNull() is AccountChanged)
        println("DECOR coldRequests=2 coldResponseBytes=$coldBytes warmRequests=0 warmBytes=0 offlineRequests=0 refreshRequests=2 copies=20")
    }
}
