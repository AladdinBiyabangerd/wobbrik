package com.aladdin.wobbrik

import appbackend.wobbrik.game.*

/** Mounted authenticated host only, replaced on login epoch/account revision changes.
 * At most two bounded (20 player) traversals; no disk writes or mutation capability. */
internal class LeaderboardDisplayMemory(private val configured: WobbrikApplication.Bootstrap.Configured) {
    val session = requireNotNull(configured.sessions.current())
    private val slots = mutableMapOf<LeaderboardScope, LeaderboardDisplaySlot>()
    fun slot(scope: LeaderboardScope): LeaderboardDisplaySlot = slots.getOrPut(scope) {
        LeaderboardDisplaySlot(LeaderboardTraversal(configured.sessions, requireNotNull(configured.leaderboard), session, scope))
    }
}

internal class LeaderboardDisplaySlot(val traversal: LeaderboardTraversal) {
    var page: LeaderboardPage? = null
}

/** Display replay deadline/week check uses APIs available at the product's API24 baseline. */
internal fun currentLeaderboardWeek(): String {
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Baku"))
    val days = (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
    calendar.add(java.util.Calendar.DAY_OF_MONTH, -days)
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).apply { timeZone = calendar.timeZone }.format(calendar.time)
}
