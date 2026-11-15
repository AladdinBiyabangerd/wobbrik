package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import com.aladdin.wobbrik.engine.AcceptedAttemptCommands
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Instant

/** Production screen/repository/wire. Only server transport and recovered account are synthetic. */
abstract class FittedLeaderboardProductionHost {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(font:Float,name:String,scenario:String="default") {
  val ownerScope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val snapshot=GameSnapshot("wobbrik-v1",1,1250,1050,null,AccountRecovery("now","first","later",7,0,emptyList(),emptyList(),emptyList(),emptyList(),28))
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",emptyList(),emptyList())
  val backend=object:AttemptBackend {
   override suspend fun sync(session:BillingSession)=snapshot
   override suspend fun catalog(session:BillingSession)=catalog
   override suspend fun submit(session:BillingSession,command:AttemptCommand)=error("No economic write")
  }
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=error("No economic write")
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }
  var fail=scenario=="error"
  var renamed=false
  var cursorFailure=true
  val entered=CompletableDeferred<Unit>()
  val gate=CompletableDeferred<Unit>()
  val requests=mutableListOf<String>()
  var generations=0
  val names=listOf("Əli","Aydan","Murad","Nigar","Rauf","Leyla","Əli","Orxan","Aysel","Elvin","Günel","Tural","Samir","Fidan","Lala","Kamran","Vüsal","Nurlan","Səbinə","Rəşad")
  val scores=listOf(42L,38,35,34,32,30,28,26,25,24,23,22,21,20,19,18,17,16,15,14)
  val http=GameplayHttpBackend(sessions,GameplayTransport {path,_,body->
   assertNull(body);requests+=path
   if(scenario=="loading"||scenario=="session") {entered.complete(Unit);gate.await()}
   if(fail) GameplayResponse(503,"""{"error":"unavailable"}""") else {
    val query=path.substringAfter('?').split('&').associate {it.substringBefore('=') to it.substringAfter('=')}
    val scope=query.getValue("scope");val limit=query.getValue("limit").toInt()
    assertTrue(limit in 1..20)
    val cursor=query["cursor"]
    if(cursor!=null && scenario in setOf("expired","growth-error") && cursorFailure) {
     cursorFailure=false
     return@GameplayTransport GameplayResponse(if(scenario=="expired") 400 else 503,"""{"error":"invalid_request"}""")
    }
    val generation=cursor?.substringAfter('g')?.substringBefore('_')?.toInt() ?: ++generations
    val start=cursor?.substringAfter("_n")?.toInt() ?: 0
    val total=when(scenario) {"empty"->0;"few"->2;"long"->1;else->20}
    val end=minOf(total,start+limit)
    val entries=(start until end).joinToString(",") {i->
     val rank=when(scenario) {"gap"->(i*2+1).toLong();"long"->Long.MAX_VALUE;else->(i+1).toLong()}
     val score=if(scenario=="long") Long.MAX_VALUE else scores[i]-(if(scope=="all") 1 else 0)
     val player=if(scenario=="long") "Əli".repeat(5) else if(renamed&&i==0) "Yenilənmiş" else names[i]
     val avatar=if(scope=="all") listOf("architect","builder","pilot")[i%3] else listOf("builder","pilot","architect")[i%3]
     """{"rank":$rank,"result_id":"r$i","floors":$score,"accepted_at":"2026-11-14T00:00:00Z","is_self":${i==6||scenario=="long"},"display_name":"$player","avatar_id":"$avatar"}"""
    }
    val at=Instant.parse("2026-11-15T00:00:00Z").plusSeconds(generation.toLong())
    GameplayResponse(200,"""{"scope":"$scope",${if(scope=="weekly") "\"week_start\":\"2026-11-09\"," else ""}"snapshot_at":"$at","expires_at":"${at.plusSeconds(900)}","entries":[$entries]${if(end<total) ",\"next_cursor\":\"g${generation}_n$end\"" else ""}}""")
   }
  })
  val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,ownerScope);owner.onAuthChanged("alice")
  val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
  var publicProfile=PublicProfile(1,"ə".repeat(24),"builder")
  val profileAccess=object:ProfileAccess {
   override suspend fun profile(session:BillingSession)=publicProfile
   override suspend fun replaceProfile(session:BillingSession,replacement:ProfileReplacement):PublicProfile {
    assertEquals(15,replacement.displayName.codePointCount(0,replacement.displayName.length))
    return PublicProfile(2,replacement.displayName,replacement.avatarId).also {publicProfile=it}
   }
  }
  val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(http),sessions,
   DecorationRepository(http),CitySceneRepository(http),leaderboard=LeaderboardRepository(http),profiles=profileAccess)
  var back=0
  var scale by mutableFloatStateOf(if(scenario in setOf("expired","growth-error")) 2f else font)
  var route by mutableStateOf(true)
  fun ready() {
   compose.waitUntil(15000) {
    compose.onAllNodesWithTag("records-refresh").fetchSemanticsNodes().singleOrNull()?.config?.contains(SemanticsProperties.Disabled)==false
   }
   compose.waitForIdle()
  }
  try {
   compose.setContent {
    val density=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density,scale)) {
     MaterialTheme {if(route) {if(scenario=="profile") PublicAccountScreen(configured,ready,{back++},{}) else LeaderboardScreen(configured,ready,{back++})}}
    }
   }
   if(scenario=="profile") {
    compose.waitUntil(15000) {compose.onAllNodesWithTag("profile-edit").fetchSemanticsNodes().isNotEmpty()}
    compose.onNodeWithTag("profile-edit").performClick()
    compose.onNodeWithText("1–15 simvol").assertExists()
    val field=compose.onNode(hasSetTextAction())
    field.performTextReplacement("ə".repeat(23))
    field.assertTextContains("ə".repeat(23))
    compose.onNodeWithText("Yadda saxla").assertIsNotEnabled()
    field.performTextReplacement("ə".repeat(15))
    field.performTextReplacement("ə".repeat(16))
    field.assertTextContains("ə".repeat(15))
    compose.onNodeWithText("Yadda saxla").performClick()
    compose.waitUntil(15000) {publicProfile.revision==2L}
    compose.waitForIdle()
    compose.onNodeWithTag("profile-edit").performClick()
    capture(name,requests,"New names max15; legacy24 readable/reducible; 16 rejected; PUT15 accepted")
    return
   }
   if(scenario=="loading"||scenario=="session") {
    compose.waitUntil(15000) {entered.isCompleted}
    compose.onNodeWithTag("records-refresh").assertIsNotEnabled()
   } else ready()
   if(scenario in setOf("expired","growth-error")) {
    compose.runOnIdle {scale=1f};ready()
    compose.onNodeWithText("Yenilənmədi.",substring=true).assertIsDisplayed()
   }
   if(scenario=="all") {compose.onNodeWithText("Bütün vaxtlar").performClick();ready();assertTrue(requests.any {"scope=all" in it})}
   if(scenario=="refresh") {
    renamed=true
    compose.onNodeWithTag("records-refresh").performClick();ready()
    compose.onNodeWithText("Yenilənmiş",substring=true).assertIsDisplayed()
    assertFalse(requests.last().contains("cursor="))
   }
   if(scenario=="resize") {
    val before=requests.size
    compose.runOnIdle {scale=2f};ready();assertEquals(before,requests.size)
    compose.runOnIdle {scale=1f};ready();assertEquals(before,requests.size)
   }
   if(scenario=="empty") compose.onNodeWithText("Hələ qəbul edilmiş nəticə yoxdur.").assertIsDisplayed()
   if(scenario=="error") {
    compose.onNodeWithText("Siyahı yenilənmədi.").assertIsDisplayed()
    val before=requests.size;compose.mainClock.advanceTimeBy(500);compose.waitForIdle();assertEquals(before,requests.size)
   }
   if(scenario=="few") {
    compose.onNodeWithTag("records-player-1").assertIsDisplayed();compose.onNodeWithTag("records-player-2").assertIsDisplayed()
    assertEquals(1,requests.size)
   }
   if(scenario=="long") {
    assertEquals("9999", leaderboardRankLabel(9999))
    assertEquals("99999+", leaderboardRankLabel(10000))
    assertEquals("99999+", leaderboardRankLabel(Long.MAX_VALUE))
    val rankNodes = compose.onAllNodesWithText("Yer 99999+", substring=true)
    assertTrue(rankNodes.fetchSemanticsNodes().indices.any { runCatching { rankNodes[it].assertIsDisplayed() }.isSuccess })
    File("/private/tmp/wobbrik-long-semantics.txt").writeText(compose.onRoot().printToString())
    val compact=runCatching {compose.onNodeWithTag("records-compact-player-${Long.MAX_VALUE}").assertIsDisplayed()}.isSuccess
    compose.onNodeWithTag(if(compact) "records-compact-player-${Long.MAX_VALUE}" else "records-player-${Long.MAX_VALUE}").assertIsDisplayed()
   }
   if(scenario=="gap") {
    compose.onNodeWithTag("records-player-1").assertIsDisplayed()
    compose.onNodeWithTag("records-player-3").assertIsDisplayed()
    compose.onAllNodesWithTag("records-player-2").assertCountEquals(0)
    assertTrue(requests.any {"cursor=" in it})
   }
   compose.onAllNodesWithText("Daha çox").assertCountEquals(0)
   compose.onAllNodesWithText("Hesaba qayıt").assertCountEquals(0)
   val board=compose.onNodeWithTag("records-board").fetchSemanticsNode()
   assertFalse(board.config.contains(SemanticsProperties.VerticalScrollAxisRange))
   val all=compose.onAllNodes(SemanticsMatcher("placed records rows") {
    it.config.getOrNull(SemanticsProperties.TestTag)?.let {tag->tag.startsWith("records-player-")||tag.startsWith("records-compact-player-")}==true
   }).fetchSemanticsNodes()
   for(row in all) if(row.layoutInfo.isPlaced && row.boundsInRoot.height>0f) {
    assertTrue("Whole row must remain in board",row.boundsInRoot.top>=board.boundsInRoot.top && row.boundsInRoot.bottom<=board.boundsInRoot.bottom)
   }
   compose.onNodeWithContentDescription("Hesaba qayıt").assertIsDisplayed().assertWidthIsAtLeast(GameTokens.Target).assertHeightIsAtLeast(GameTokens.Target)
   compose.onNodeWithTag("records-refresh").assertIsDisplayed().assertWidthIsAtLeast(GameTokens.Target).assertHeightIsAtLeast(GameTokens.Target)
   if(scenario!="session") capture(name,requests,board.config[SemanticsProperties.ContentDescription].joinToString())
   if(scenario in setOf("expired","growth-error")) {
    val failedPath=requests.last();val before=requests.size
    compose.onNodeWithTag("records-refresh").performClick();ready()
    if(scenario=="expired") assertFalse(requests[before].contains("cursor="))
    else assertEquals(failedPath.substringAfter("cursor="),requests[before].substringAfter("cursor="))
    compose.onAllNodesWithText("Yenilənmədi.",substring=true).assertCountEquals(0)
   }
   if(scenario=="error") {fail=false;compose.onNodeWithTag("records-refresh").performClick();ready();compose.onNodeWithTag("records-player-4").assertIsDisplayed()}
   if(scenario=="session") {
    compose.runOnIdle {route=false;owner.onAuthChanged(null);owner.onAuthChanged("alice");gate.complete(Unit)}
    compose.waitForIdle();compose.onAllNodesWithTag("records-player-4").assertCountEquals(0)
   } else {
    compose.onNodeWithContentDescription("Hesaba qayıt").performClick();assertEquals(1,back)
   }
  } finally {gate.complete(Unit);ownerScope.cancel()}
 }
 private fun capture(name:String,requests:List<String>,description:String) {
  compose.runOnUiThread {
   val view=compose.activity.window.decorView
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(view)
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap))
   var arrow=0
   for(y in 48 until 192) for(x in 48 until 192) {
    val pixel=bitmap.getPixel(x,y)
    if(android.graphics.Color.red(pixel)>210&&android.graphics.Color.green(pixel)>210&&android.graphics.Color.blue(pixel)>210) arrow++
   }
   assertTrue("Back arrow must be drawn",arrow>30)
   val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/profile-leaderboard-visual/fitted-production").apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   File(out,"$name.txt").writeText(description+"\n"+requests.joinToString("\n"))
   bitmap.recycle()
  }
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardRegularRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"390-font1")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardSmallRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"360-font1")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardFontRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(2f,"320-font2")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardAllRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"all","all")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardErrorRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"error","error")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardEmptyRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(2f,"empty","empty")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardFewRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"few","few")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardGapRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"gap","gap")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardLongRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(2f,"long","long")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardRefreshRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"refresh","refresh")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardLoadingRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"loading","loading")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardResizeRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"resize","resize")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardSessionRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"session","session")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardExpiredRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"expired","expired")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardGrowthErrorRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"growth-error","growth-error")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedLeaderboardProfileNameRenderTest:FittedLeaderboardProductionHost() {@Test fun render()=render(1f,"profile-name15","profile")}
