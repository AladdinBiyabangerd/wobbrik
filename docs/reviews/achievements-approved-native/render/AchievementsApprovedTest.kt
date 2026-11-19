package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.billing.AccountSessions
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

abstract class AchievementsHost {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 protected fun capture(name:String, dialog:Boolean=false) {
  val window=if(dialog) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(window.decorView)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=window.decorView
   val warm=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(warm));warm.recycle();v.invalidate()
   fun redraw(node:android.view.View) {
    node.invalidate()
    if(node.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") node.javaClass.getMethod("invalidateDescendants").invoke(node)
    if(node is android.view.ViewGroup) for(i in 0 until node.childCount) redraw(node.getChildAt(i))
   }
   redraw(v)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   var result=-1
   android.view.PixelCopy.request(window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File(System.getProperty("wobbrik.render.output","/private/tmp/wobbrik-commerce-native")).apply {mkdirs()}
   File(out,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
  }
 }

 protected fun flow(font:Float,name:String,insets:Boolean=false) {
  val worker=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions { "token" }
  val requests=mutableListOf<String>(); var bytes=0; var offline=false
  val progress=listOf(AchievementProgress("height_50","height",50,20),AchievementProgress("precise_25","precise",25,18),
   AchievementProgress("rescues_5","rescues",5,3),AchievementProgress("height_10","height",10,10))
  val snapshot=GameSnapshot("wobbrik-v1",1,if(insets) Long.MAX_VALUE else 411,6600,null,
   AccountRecovery("now","first","later",12,3,listOf("height_10"),emptyList(),emptyList(),emptyList(),35,achievementProgress=progress))
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",emptyList(),emptyList())
  val backend=object:AttemptBackend {
   override suspend fun sync(session:appbackend.wobbrik.billing.BillingSession)=snapshot
   override suspend fun catalog(session:appbackend.wobbrik.billing.BillingSession)=catalog
   override suspend fun submit(session:appbackend.wobbrik.billing.BillingSession,command:AttemptCommand)=error("read only")
  }
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=error("read only")
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,body->
   assertNull(body);requests+=path
   if(offline) return@GameplayTransport GameplayResponse(503,"{}")
   val scope=path.substringAfter("scope=").substringBefore('&')
   val limit=path.substringAfter("limit=").substringBefore('&').toInt()
   val offset=if(path.contains("cursor=")) path.substringAfter("cursor=").removePrefix("p").toInt() else 0
   val players=(offset until minOf(offset+limit,20)).joinToString(",") { i->
    val names=listOf("Murad","Aylin","Nigar","Kamran","Leyla",if(insets) "Uzun Oyunçu Adı" else "Səbinə","Elvin")
    """{"rank":${i+1},"result_id":"result-${i+1}","floors":${if(insets && i==0) Long.MAX_VALUE else 86-i*5},"accepted_at":"2026-11-19T00:00:00Z","is_self":${i==5},"display_name":"${names[i%names.size]}","avatar_id":"${listOf("builder","pilot","architect")[i%3]}"}"""
   }
   val bodyText="""{"scope":"$scope",${if(scope=="weekly") "\"week_start\":\"2026-11-16\"," else ""}"snapshot_at":"2026-11-19T00:00:00Z","expires_at":"2099-11-19T00:15:00Z","entries":[$players]${if(offset+limit<20) ",\"next_cursor\":\"p${offset+limit}\"" else ""}}"""
   bytes+=bodyText.toByteArray().size
   GameplayResponse(200,bodyText)
  })
  try {
   val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,worker);owner.onAuthChanged("alice")
   val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
   val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(reads),sessions,
    DecorationRepository(reads),CitySceneRepository(reads),leaderboard=LeaderboardRepository(reads))
   val display=LeaderboardDisplayMemory(configured)
   var route by mutableStateOf("achievements")
   val start=System.nanoTime()
   compose.setContent {val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {MaterialTheme {
     Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
      MobileMenuShell(route,snapshot.coins,{}, {route=it}) {key(route) {
       when(route) {
        "achievements" -> MobileAchievementsScreen(snapshot) {route="home"}
        "leaderboard" -> LeaderboardScreen(configured,ready,{route="achievements"},display)
       }
      }}
     }
    }}
   }
   compose.waitUntil(15000) { compose.onAllNodesWithText("Nailiyyətlər").fetchSemanticsNodes().isNotEmpty() }
   compose.waitForIdle()
   // Wait for the optional IO sprite load to settle without a latency assertion.
   Thread.sleep(1200);compose.waitForIdle()
   capture("$name-achievements")
   assertEquals(0,requests.size)
   compose.onNodeWithTag("awards-list").performScrollToIndex(6)
   compose.onNodeWithTag("award-height_10").assertIsDisplayed()
   capture("$name-awards-scrolled")
   val recordsStart=System.nanoTime()
   compose.onNodeWithText("Rekordlar").performClick()
   compose.waitUntil(15000) {display.slot(LeaderboardScope.ALL).page!=null}
   val firstMetadataMs=(System.nanoTime()-recordsStart)/1000000
   Thread.sleep(500);compose.waitForIdle()
   compose.onAllNodesWithText("Murad", substring=true).onFirst().assertIsDisplayed()
   capture("$name-records")
   Thread.sleep(500);compose.waitForIdle()
   val coldRequests=requests.size;val coldBytes=bytes
   val warmStart=System.nanoTime()
   compose.onNodeWithText("Nailiyyətlər").performClick()
   compose.onNodeWithText("Rekordlar").performClick()
   compose.waitForIdle()
   val warmHostMs=(System.nanoTime()-warmStart)/1000000
   assertEquals("warm tab must reuse its accepted traversal",coldRequests,requests.size)
   compose.onNodeWithText("Həftəlik").performClick()
   compose.waitUntil(15000) {display.slot(LeaderboardScope.WEEKLY).page!=null}
   Thread.sleep(500);compose.waitForIdle()
   val weekly=requests.size
   compose.onNodeWithText("Ümumi").performClick();compose.waitForIdle()
   assertEquals("scope return reuses snapshot",weekly,requests.size)
   offline=true
   compose.onNodeWithContentDescription("Yenilə").performClick()
   compose.waitUntil(15000) {compose.onAllNodesWithText("Yenilənmədi. Yenilə ilə təkrarla.").fetchSemanticsNodes().isNotEmpty()}
   capture("$name-offline-retained")
   offline=false
   compose.onNodeWithContentDescription("Yenilə").performClick()
   compose.waitUntil(15000) {compose.onAllNodesWithText("Yenilənmədi. Yenilə ilə təkrarla.").fetchSemanticsNodes().isEmpty()}
   if(name == "regular") {
    // Expired display must not be presented as a fresh offline page.
    compose.onNodeWithText("Nailiyyətlər").performClick()
    display.slot(LeaderboardScope.ALL).page=display.slot(LeaderboardScope.ALL).page!!.copy(expiresAtMillis=0)
    offline=true
    compose.onNodeWithText("Rekordlar").performClick()
    compose.waitUntil(15000) {compose.onAllNodesWithText("Siyahı yenilənmədi.").fetchSemanticsNodes().isNotEmpty()}
    capture("regular-empty-error")
    offline=false
    compose.onNodeWithText("Yenidən cəhd et").performClick()
    compose.waitUntil(15000) {compose.onAllNodesWithText("Siyahı yenilənmədi.").fetchSemanticsNodes().isEmpty()}
   }
   println("UGURLAR $name cold=$coldRequests/$coldBytes bytes warm=0 firstMetadataHostMs=$firstMetadataMs warmRouteHostMs=$warmHostMs elapsedHostMs=${(System.nanoTime()-start)/1000000} requests=$requests")
  } finally {worker.cancel()}
 }
}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AchievementsRegularTest:AchievementsHost(){@Test fun render()=flow(1f,"regular")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AchievementsSmallTest:AchievementsHost(){@Test fun render()=flow(1f,"small")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AchievementsRegularLargeTest:AchievementsHost(){@Test fun render()=flow(2f,"regular-font2")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AchievementsSmallLargeTest:AchievementsHost(){@Test fun render()=flow(2f,"small-font2")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AchievementsInsetsTest:AchievementsHost(){@Test fun render()=flow(2f,"insets-long",true)}
