package appbackend.wobbrik.game

import appbackend.wobbrik.billing.AccountChanged
import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.billing.Sessions
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import java.time.Instant
import java.time.LocalDate

/** Scope and opaque cursor are owned by one authenticated traversal. */
enum class LeaderboardScope(val wire: String) { ALL("all"), WEEKLY("weekly") }
data class LeaderboardEntry(val rank: Long, val resultId: String, val floors: Long,
    val acceptedAt: Instant, val isSelf: Boolean, val displayName: String, val avatarId: String)
data class LeaderboardPage(val scope: LeaderboardScope, val weekStart: LocalDate?, val snapshotAt: Instant,
    val expiresAt: Instant, val entries: List<LeaderboardEntry>, val next: String?)

/** Client display/read budget; the server still owns rank, score, week and cursor. */
object LeaderboardWindowPolicy {
    const val MinRows = 4
    const val MaxRows = 20
    const val DefaultRows = 8
    const val FirstFitWaitMillis = 500L
    fun requestSize(visible: Int): Int = visible.coerceIn(MinRows, MaxRows)
}

object LeaderboardReads {
    fun cursor(value: String) { require(Regex("[A-Za-z0-9_-]{1,256}").matches(value)) }
    internal fun allowedPath(path: String) = Regex("leaderboard\\?scope=(?:all|weekly)&limit=(?:[1-9]|1[0-9]|20)(?:&cursor=[A-Za-z0-9_-]{1,256})?").matches(path)
    fun parse(body: String, scope: LeaderboardScope, limit: Int = LeaderboardWindowPolicy.MaxRows): LeaderboardPage {
        require(limit in 1..LeaderboardWindowPolicy.MaxRows)
        val r = Json.parseToJsonElement(body).jsonObject
        fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.also { require(it.isString) }.content
        fun JsonObject.number(key: String) = getValue(key).jsonPrimitive.also { require(!it.isString) }.long
        require(r.text("scope") == scope.wire)
        val week = r["week_start"]?.let { LocalDate.parse(r.text("week_start")) }
        require((scope == LeaderboardScope.WEEKLY) == (week != null))
        require(week == null || week.dayOfWeek == java.time.DayOfWeek.MONDAY)
        val snapshot = Instant.parse(r.text("snapshot_at")); val expires = Instant.parse(r.text("expires_at"))
        require(expires > snapshot)
        val entries = r.getValue("entries").jsonArray.map { raw ->
            val e = raw.jsonObject
            val name = e.text("display_name"); val avatar = e.text("avatar_id"); PublicProfiles.identity(name, avatar)
            LeaderboardEntry(e.number("rank").also { require(it > 0) }, e.text("result_id").also(InventoryReads::id),
                e.number("floors").also { require(it >= 0) }, Instant.parse(e.text("accepted_at")),
                e.getValue("is_self").jsonPrimitive.also { require(!it.isString) }.boolean, name, avatar)
        }
        require(entries.size <= limit && entries.count { it.isSelf } <= 1)
        require(entries.zipWithNext().all { (a, b) -> b.rank > a.rank && b.floors <= a.floors })
        val next = r["next_cursor"]?.let { r.text("next_cursor").also(::cursor) }
        require(next == null || entries.isNotEmpty())
        return LeaderboardPage(scope, week, snapshot, expires, entries, next)
    }
}

class LeaderboardRepository(private val backend: GameplayReads) {
    suspend fun page(session: BillingSession, scope: LeaderboardScope, cursor: String? = null,
        limit: Int = LeaderboardWindowPolicy.MaxRows): LeaderboardPage {
        require(limit in 1..LeaderboardWindowPolicy.MaxRows)
        cursor?.let(LeaderboardReads::cursor)
        return LeaderboardReads.parse(backend.read(session, "leaderboard?scope=${scope.wire}&limit=$limit" +
            (cursor?.let { "&cursor=$it" } ?: "")), scope, limit)
    }
}

/** Failed pages preserve the exact cursor. Refresh creates a new server snapshot. */
class LeaderboardTraversal(private val sessions: Sessions, private val repository: LeaderboardRepository,
    private val session: BillingSession, private val scope: LeaderboardScope) {
    private val mutex = Mutex()
    private var accepted: LeaderboardPage? = null
    private fun current() { if (sessions.current() != session) throw AccountChanged() }

    /** General cursor read retained for callers; the fitted screen uses [fit]. */
    suspend fun load(refresh: Boolean = false): LeaderboardPage = mutex.withLock {
        current()
        val previous = if (refresh) null else accepted
        if (previous != null && previous.next == null) previous else advance(previous, LeaderboardWindowPolicy.MaxRows)
    }

    /** A smaller viewport reuses its snapshot; a larger one reads only the missing players. */
    suspend fun fit(visible: Int, refresh: Boolean = false): LeaderboardPage = mutex.withLock {
        current()
        val wanted = LeaderboardWindowPolicy.requestSize(visible)
        var result = (if (refresh) null else accepted) ?: advance(null, wanted)
        while (result.entries.size < wanted && result.next != null) {
            result = advance(result, wanted - result.entries.size)
        }
        result
    }

    private suspend fun advance(previous: LeaderboardPage?, limit: Int): LeaderboardPage {
        val page = repository.page(session, scope, previous?.next, limit)
        currentCoroutineContext().ensureActive()
        current()
        val result = if (previous == null) page else {
            require(page.snapshotAt == previous.snapshotAt && page.expiresAt == previous.expiresAt && page.weekStart == previous.weekStart)
            require(page.next == null || page.next != previous.next)
            require(previous.entries.isEmpty() || page.entries.isEmpty() ||
                (page.entries.first().rank > previous.entries.last().rank && page.entries.first().floors <= previous.entries.last().floors))
            val combined = previous.entries + page.entries
            require(combined.count { it.isSelf } <= 1)
            page.copy(entries = combined)
        }
        accepted = result
        return result
    }
}
