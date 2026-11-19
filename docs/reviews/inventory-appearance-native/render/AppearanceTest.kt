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


abstract class AppearanceHost {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 private fun capture(name:String, dialog:Boolean=false) {
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
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   var result=-1
   android.view.PixelCopy.request(window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File(requireNotNull(System.getProperty("wobbrik.render.output","/private/tmp/wobbrik-commerce-native"))).apply {mkdirs()}
   File(out,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
  }
 }

 protected fun render(font:Float,name:String,insets:Boolean=false) {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions { "token" }
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1", (1..6).flatMap { i -> FinishCosmetics.categories.map { c ->
   CatalogItem("base_${i}_$c", if(c=="wall") (listOf("Əhəngdaşı divar","Kərpic hörgü","Suvaq fasad","Daş hörgü","Taxta üzlük","Uzun divar görünüşü bütöv sözlərlə")[i-1]) else "$c $i", c,
     if(i<=3) "free" else if(i==4) "coins" else if(i==5) "achievement" else "reward", 0, if(i==4) 60 else null, if(i==6) 4 else null)
  } }, emptyList())
  val snapshot=GameSnapshot("wobbrik-v1",1,if(insets) Long.MAX_VALUE else 411,1050,null,
    AccountRecovery("now","first","later",7,0,emptyList(),emptyList(),listOf("land_1"),emptyList(),28))
  var syncs=0;var catalogReads=0;var unexpectedReads=0;var writes=0;var failPreference=true
  val backend=object:AttemptBackend {
   override suspend fun sync(session:appbackend.wobbrik.billing.BillingSession):GameSnapshot {syncs++;return snapshot}
   override suspend fun catalog(session:appbackend.wobbrik.billing.BillingSession):RecoveryCatalog {catalogReads++;return catalog}
   override suspend fun submit(session:appbackend.wobbrik.billing.BillingSession,command:AttemptCommand) {error("unexpected mutation")}
  }
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=Unit
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }
  val defaults=object:com.aladdin.wobbrik.engine.CosmeticDefaultsStore {
   var parts=emptyMap<String,String>()
   override fun read(uid:String)=parts.also {assertEquals("alice",uid)}
   override fun write(uid:String,parts:Map<String,String>) {assertEquals("alice",uid);if(failPreference) {failPreference=false;throw java.io.IOException("fixture disk failure")};this.parts=parts;writes++}
   override fun attempt(uid:String,attemptId:String):Map<String,String>?=null
   override fun capture(uid:String,attemptId:String,parts:Map<String,String>)=Unit
  }
  try {
   val artStart=System.nanoTime()
   ApprovedArt.prepare(compose.activity.assets)
   val artMs=(System.nanoTime()-artStart)/1_000_000
   val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,scope);owner.onAuthChanged("alice")
   val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
   val reads=GameplayHttpBackend(sessions,GameplayTransport { _,_,_->unexpectedReads++;error("collection must not read inventory/city") })
   val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(reads),sessions,DecorationRepository(reads),CitySceneRepository(reads))
   var readOnly by mutableStateOf(false)
   var page by mutableStateOf("collection")
   val contentStart=System.nanoTime()
   compose.setContent {val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {MaterialTheme {
     Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
      MobileMenuShell(page,snapshot.coins,{}, {page=it}) {
       InventoryAppearanceScreen(configured,AccountView(ReadOnlyAccount("alice",snapshot,catalog),if(readOnly) null else ready),defaults) {page="inventory"}
      }
     }
    }}
   }
   compose.waitUntil(15000) {compose.onAllNodesWithContentDescription("Görünüş hissələri").fetchSemanticsNodes().isNotEmpty()}
   val firstContentMs=(System.nanoTime()-contentStart)/1_000_000
   compose.onNodeWithText("Görünüş").assertIsSelected()
   compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   compose.mainClock.advanceTimeBy(250)
   capture("appearance-$name")
   capture("appearance-$name")
   compose.onNodeWithText("Açıq").performScrollTo().performClick()
   compose.onNodeWithText("Açıq").assertIsSelected()
   compose.onNodeWithText("Nailiyyət").performClick()
   compose.onNodeWithText("Bu filtrdə hissə yoxdur").assertExists()
   compose.onNodeWithText("Açıq").performClick()
   compose.onNodeWithText("Taxta üzlük").performScrollTo().assertIsDisplayed()
   capture("appearance-achievement-$name")
   compose.onNodeWithText("Nailiyyət").performScrollTo().performClick()
   compose.onNodeWithContentDescription("Görünüş hissələri").performScrollToIndex(3)
   compose.onNodeWithContentDescription("Kərpic hörgü").performScrollTo().performClick()
   compose.onNodeWithText("Standart görünüş seç").assertIsEnabled().performClick()
   compose.waitUntil(15000) {compose.onAllNodesWithText("Seçim saxlanmadı. Yenidən cəhd et.").fetchSemanticsNodes().isNotEmpty()}
   compose.runOnIdle {assertEquals(0,writes)}
   val returnStart=System.nanoTime()
   compose.onNodeWithText("Standart görünüş seç").assertIsEnabled().performClick()
   compose.waitUntil(15000) {writes==1 && compose.onAllNodesWithText("Görünüş hissələri").fetchSemanticsNodes().isNotEmpty() || writes==1 && compose.onAllNodesWithContentDescription("Görünüş hissələri").fetchSemanticsNodes().isNotEmpty()}
   val preferenceReturnMs=(System.nanoTime()-returnStart)/1_000_000
   compose.runOnIdle {assertEquals("base_2_wall",defaults.parts["wall"]); assertEquals(0,unexpectedReads);assertEquals(1,syncs);assertEquals(1,catalogReads)}
   compose.onNodeWithContentDescription("Görünüş hissələri").performScrollToIndex(7)
   compose.onNodeWithContentDescription("Uzun divar görünüşü bütöv sözlərlə").performScrollTo().assertIsDisplayed()
   compose.onNodeWithContentDescription("Görünüş hissələri").performTouchInput { swipeUp() }
   compose.waitForIdle()
   val lastCard=compose.onNodeWithText("Reklam 0/4 · Hazır deyil").fetchSemanticsNode().boundsInRoot
   val play=compose.onNodeWithContentDescription("Oyunu başlat").fetchSemanticsNode().boundsInRoot
   assertTrue("Last status scrolls clear of the dock",lastCard.bottom<=play.top)
   capture("appearance-long-$name")
   // All category controls stay reachable with long text/font2 and a short viewport.
   compose.onNodeWithContentDescription("Görünüş hissələri").performScrollToIndex(0)
   compose.onNodeWithText("İşıq").performScrollTo().performClick()
   compose.onNodeWithText("lighting 1").performScrollTo().assertIsDisplayed()
   compose.onAllNodesWithText("Divar").onFirst().performScrollTo().performClick()
   compose.waitForIdle()
   capture("appearance-ready-$name")
   capture("appearance-ready-$name")
   compose.runOnIdle { readOnly=true }
   compose.onNodeWithText("Saxlanmış məlumat · yalnız baxış").performScrollTo().assertIsDisplayed()
   compose.onNodeWithContentDescription("Görünüş hissələri").performScrollToIndex(4)
   compose.onNodeWithContentDescription("Kərpic hörgü").performScrollTo().assertIsNotEnabled()
   capture("appearance-offline-$name")
   compose.runOnIdle {assertEquals(1,writes); assertEquals(0,unexpectedReads)}
   compose.onNodeWithContentDescription("Görünüş hissələri").performScrollToIndex(0)
   compose.onNodeWithContentDescription("Yenilə").performScrollTo().performClick()
   compose.runOnIdle {assertEquals(2,syncs);assertEquals(0,unexpectedReads)}
   println("APPEARANCE $name afterRefreshSync=$syncs catalog=$catalogReads inventoryCityDetailReads=$unexpectedReads extraBytes=0 preferenceWrites=$writes artPrepareMs=$artMs firstContentMs=$firstContentMs preferenceReturnMs=$preferenceReturnMs")
  } finally {scope.cancel()}
 }
}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppearanceRegularTest:AppearanceHost() {@Test fun screen()=render(1f,"regular")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppearanceSmallTest:AppearanceHost() {@Test fun screen()=render(1f,"small")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppearanceRegularFontTest:AppearanceHost() {@Test fun screen()=render(2f,"regular-font2")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppearanceSmallFontTest:AppearanceHost() {@Test fun screen()=render(2f,"small-font2")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppearanceInsetsTest:AppearanceHost() {@Test fun screen()=render(2f,"insets-font2",true)}
