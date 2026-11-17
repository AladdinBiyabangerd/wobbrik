package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileRecordsProductionRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private fun render(font:Float,name:String,error:Boolean=false,empty:Boolean=false,insets:Boolean=false,long:Boolean=false) {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val snapshot=GameSnapshot("wobbrik-v1",1,if(insets) Long.MAX_VALUE else 246,246,null)
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",emptyList(),emptyList())
  val backend=object:AttemptBackend {
   override suspend fun sync(session:BillingSession)=snapshot
   override suspend fun catalog(session:BillingSession)=catalog
   override suspend fun submit(session:BillingSession,command:AttemptCommand)=error("No economic writes")
  }
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=error("No economic writes")
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }
  fun SemanticsNodeInteraction.reveal() = if(font>1f) performScrollTo() else this
  var fail=error
  val acceptedName=if(long) "Ə".repeat(15) else "Aladdin"
  val requests=mutableListOf<String>()
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,body->
   assertNull(body);requests+=path
   if(fail) GameplayResponse(503,"""{"error":"unavailable"}""") else {
    val all=path.contains("scope=all")
    val entries=if(empty) "" else (1..if(long) 1 else 4).joinToString(",") { rank ->
     """{"rank":${if(long) Long.MAX_VALUE else rank},"result_id":"r$rank","floors":${if(long) Long.MAX_VALUE else 45-rank},"accepted_at":"2026-11-15T00:00:00Z","is_self":${rank==1},"display_name":"${if(rank==1) acceptedName else "Oyunçu$rank"}","avatar_id":"${listOf("builder","pilot","architect","builder")[rank-1]}"}"""
    }
    GameplayResponse(200,"""{"scope":"${if(all) "all" else "weekly"}",${if(all) "" else "\"week_start\":\"2026-11-09\","}"snapshot_at":"2026-11-15T00:00:00Z","expires_at":"2026-11-16T00:00:00Z","entries":[$entries]}""")
   }
  })
  try {
   val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,scope);owner.onAuthChanged("alice")
   val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
   val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(reads),sessions,
    DecorationRepository(reads),CitySceneRepository(reads),leaderboard=LeaderboardRepository(reads))
   compose.setContent {
    val density=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
     MaterialTheme { Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=24.dp,bottom=48.dp) else Modifier)) {
      MobileMenuShell("leaderboard",snapshot.coins,{}, {}) { LeaderboardScreen(configured,ready,{}) }
     } }
    }
   }
   if(error) {
    compose.onNodeWithText("Siyahı yenilənmədi.").reveal().assertIsDisplayed()
    compose.runOnIdle {fail=false}
    compose.onNodeWithTag("records-refresh").reveal().performClick()
   }
   if(empty) compose.onNodeWithText("Hələ qəbul edilmiş nəticə yoxdur.").reveal().assertIsDisplayed()
   else {
    compose.waitUntil(15000) {compose.onAllNodesWithText(acceptedName).fetchSemanticsNodes().isNotEmpty()}
    if(font>1f) compose.onNodeWithText(acceptedName).reveal().assertIsDisplayed()
    else {
     val names=compose.onAllNodesWithText(acceptedName)
     assertTrue("Accepted first place must be visible",names.fetchSemanticsNodes().indices.any {names[it].isDisplayed()})
    }
    if(name=="390-font1") compose.onNodeWithTag("records-player-1").assertIsDisplayed()
   }
   compose.onNodeWithText("Ümumi").reveal().performClick()
   compose.waitUntil(15000) {requests.any {it.contains("scope=all")}}
   compose.onNodeWithText("Ümumi").assertIsSelected().assertHeightIsAtLeast(48.dp)
   compose.onNodeWithText("Həftəlik").reveal().performClick()
   compose.onNodeWithText("Həftəlik").assertIsSelected()
   compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed()
   compose.onNodeWithTag("records-refresh").reveal()
   if(long) compose.onNodeWithText(acceptedName).performSemanticsAction(
    androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { action ->
     val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>();action(layouts)
     assertEquals("A public name must stay on one horizontal scroll line",1,layouts.single().lineCount)
   }
   capture(name)
  } finally {scope.cancel()}
 }
 private fun capture(name:String) {
  compose.waitForIdle()
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(compose.activity.window.decorView)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(b));b.recycle();v.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   var result=-1
   android.view.PixelCopy.request(compose.activity.window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File(System.getProperty("wobbrik.render.output","/private/tmp/wobbrik-records-native")).apply {mkdirs()}
   File(out,"production-records-$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
   b.recycle()
  }
 }
 @Test fun regular()=render(1f,"390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun small()=render(1f,"320-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun largeFont()=render(2f,"320-font2")
 @Test fun errorRetry()=render(1f,"error-retry",error=true)
 @Test fun empty()=render(1f,"empty",empty=true)
 @Test @Config(qualifiers="w360dp-h640dp-xxhdpi") fun insets()=render(2f,"insets",insets=true)
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun longNameAndScore()=render(2f,"long-name-score",long=true)
}
