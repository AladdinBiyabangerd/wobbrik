package com.aladdin.wobbrik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.isActive

@Composable
internal fun LeaderboardScreen(configured: WobbrikApplication.Bootstrap.Configured,
    ready: AuthenticatedRecoveryOwner.State.Ready, onBack: () -> Unit) {
    var scope by remember { mutableStateOf(LeaderboardScope.WEEKLY) }
    val session = remember { requireNotNull(configured.sessions.current()) }
    val traversal = remember(scope) { LeaderboardTraversal(configured.sessions, requireNotNull(configured.leaderboard), session, scope) }
    var page by remember(scope) { mutableStateOf<LeaderboardPage?>(null) }
    var loading by remember(scope) { mutableStateOf(true) }
    var failed by remember(scope) { mutableStateOf(false) }
    var request by remember(scope) { mutableIntStateOf(0) }
    var resetOnNext by remember(scope) { mutableStateOf(true) }
    var retryFresh by remember(scope) { mutableStateOf(true) }
    var fitted by remember(scope) { mutableIntStateOf(0) }
    fun current() = configured.sessions.current() == session && configured.recovery.state.value === ready
    fun refresh() { resetOnNext = if (failed) retryFresh else true; request++ }
    LaunchedEffect(traversal, request) {
        val context = currentCoroutineContext()
        var fresh = resetOnNext
        var lastBudget = 0
        suspend fun read(budget: Int) {
            if (lastBudget == budget) return
            lastBudget = budget
            loading = true
            try {
                val result = traversal.fit(budget, refresh = fresh)
                context.ensureActive()
                if (current()) { page = result; failed = false; resetOnNext = false; fresh = false }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                if (current() && context.isActive) {
                    failed = true
                    retryFresh = fresh || (failure is GameplayHttpError && failure.status == 400) || failure !is java.io.IOException
                }
            } finally { if (current() && context.isActive) loading = false }
        }
        val first = withTimeoutOrNull(LeaderboardWindowPolicy.FirstFitWaitMillis) {
            snapshotFlow { fitted }.first { it > 0 }
        } ?: LeaderboardWindowPolicy.DefaultRows
        read(first)
        // A resize is serial with the in-flight read. An error waits for an explicit retry.
        snapshotFlow { fitted }.filter { it > 0 }.collect { budget -> if (!failed) read(budget) }
    }
    BackHandler(onBack = onBack)
    val largeFont = LocalDensity.current.fontScale > 1f
    CommunityFrame("Rekordlar", onBack, onRefresh = ::refresh, refreshEnabled = !loading, refreshing = loading) {
        if (LocalMobileMenuShell.current) MobileRecordsControls(scope, { scope = it }, ::refresh, !loading, loading)
        else {
        val tabs: @Composable (Modifier) -> Unit = { modifier ->
            HomeButton("Həftəlik", modifier = modifier, primary = scope == LeaderboardScope.WEEKLY,
                leading = { CommunityCalendarArt(scope == LeaderboardScope.WEEKLY, Modifier.size(GameTokens.Inset)) }) { scope = LeaderboardScope.WEEKLY }
            HomeButton("Bütün vaxtlar", modifier = modifier, primary = scope == LeaderboardScope.ALL,
                leading = { Image(painterResource(R.drawable.ui_trophy), null, Modifier.size(GameTokens.Inset)) }) { scope = LeaderboardScope.ALL }
        }
        if (largeFont) Column(verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) { tabs(Modifier.fillMaxWidth()) }
        else Surface(color = GameTokens.Raised, shape = RoundedCornerShape(GameTokens.CardRadius)) {
            Row { tabs(Modifier.weight(1f).heightIn(min = GameTokens.MobileUi.PrimaryTarget)) }
        }
        }
        if (scope == LeaderboardScope.WEEKLY) {
            val week = page?.weekStart
            Text(if (week == null) "" else if (largeFont) "Həftə: $week" else "Həftə: $week · Bakı vaxtı",
                Modifier.semantics { if (week != null) contentDescription = "Həftə: $week · Bakı vaxtı" },
                color = GameTokens.Muted, fontSize = GameTokens.Caption)
        }
        Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
            if (failed && page?.entries?.isNotEmpty() == true)
                Text("Yenilənmədi. Yenilə ilə təkrarla.", color = GameTokens.Danger, fontSize = GameTokens.Caption)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                LeaderboardBoard(page, Modifier.fillMaxSize()) { budget -> if (fitted != budget) fitted = budget }
                if (page?.entries.isNullOrEmpty()) Column(Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(GameTokens.Small), horizontalAlignment = Alignment.CenterHorizontally) {
                    when {
                        loading -> CircularProgressIndicator(color = GameTokens.Accent)
                        failed -> {
                            Text("Siyahı yenilənmədi.", color = GameTokens.Danger)
                            HomeButton("Yenidən cəhd et", onClick = ::refresh)
                        }
                        else -> Text("Hələ qəbul edilmiş nəticə yoxdur.", color = GameTokens.Muted)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CommunityFrame(title: String, onBack: () -> Unit, coins: Long? = null,
    onRefresh: (() -> Unit)? = null, refreshEnabled: Boolean = true, refreshing: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    if (LocalMobileMenuShell.current) {
        MobileRecordsFrame(content)
        return
    }
    val board = ImageBitmap.imageResource(R.drawable.leaderboard_approved_board)
    CompositionLocalProvider(LocalCommunityBoard provides board) {
        BoxWithConstraints(Modifier.fillMaxSize().background(GameTokens.Background)) {
            val scrollFrame = LocalMobileMenuShell.current && LocalDensity.current.fontScale > 1f
            val frameHeight = if (scrollFrame) maxHeight.coerceAtLeast(GameTokens.MobileUi.CompactHeight) else maxHeight
            CommunityBackdrop(Modifier.matchParentSize())
            Box(Modifier.fillMaxSize().then(if (scrollFrame) Modifier.verticalScroll(rememberScrollState()) else Modifier)) {
                Column(Modifier.fillMaxWidth().height(frameHeight).safeDrawingPadding().imePadding().padding(GameTokens.Gap),
                    verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                    if (coins == null) Box(Modifier.fillMaxWidth().height(GameTokens.Target)) {
                        if (!LocalMobileMenuShell.current) Image(painterResource(R.drawable.wobbrik_logo), "Wobbrik",
                            Modifier.align(Alignment.Center).width(GameTokens.Target * 3).height(GameTokens.Target))
                        CommunityBackArt(Modifier.align(Alignment.CenterStart).size(GameTokens.Target)
                            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onBack)
                            .semantics { contentDescription = if (onRefresh != null) "Hesaba qayıt" else "Geri" })
                        if (onRefresh != null) Surface(onClick = onRefresh, enabled = refreshEnabled,
                            modifier = Modifier.align(Alignment.CenterEnd).size(GameTokens.Target).testTag("records-refresh")
                                .semantics { contentDescription = "Yenilə" },
                            shape = androidx.compose.foundation.shape.CircleShape, color = GameTokens.Surface,
                            border = BorderStroke(GameTokens.Hairline, GameTokens.Raised)) {
                            Box(Modifier.background(Brush.verticalGradient(listOf(GameTokens.Raised, GameTokens.Surface))), contentAlignment = Alignment.Center) {
                                if (refreshing) CircularProgressIndicator(Modifier.size(GameTokens.Inset), color = GameTokens.Text, strokeWidth = GameTokens.Hairline * 2)
                                else CommunityRefreshArt(Modifier.size(GameTokens.Inset))
                            }
                        }
                    } else Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        CommunityBackArt(Modifier.size(GameTokens.Target).clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onBack)
                            .semantics { contentDescription = "Geri" })
                        Text(title, Modifier.weight(1f), color = GameTokens.Text, fontSize = mobileUiSizing().heading, fontWeight = FontWeight.Bold)
                        if (coins.toString().length <= 6 && LocalDensity.current.fontScale <= 1f) {
                            Image(painterResource(R.drawable.ui_coin), "Sikkə", Modifier.size(GameTokens.Inset))
                            Text(coins.toString(), color = GameTokens.Text, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (coins != null && (coins.toString().length > 6 || LocalDensity.current.fontScale > 1f)) Text("$coins sikkə", color = GameTokens.Text)
                    if (coins == null) Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
                        Text("›", color = GameTokens.Accent, fontSize = mobileUiSizing().heading, fontWeight = FontWeight.ExtraBold)
                        Text(title, color = GameTokens.Text, fontSize = mobileUiSizing().heading, fontWeight = FontWeight.ExtraBold)
                        Text("‹", color = GameTokens.Accent, fontSize = mobileUiSizing().heading, fontWeight = FontWeight.ExtraBold)
                    }
                    content()
                }
            }
        }
    }
}
