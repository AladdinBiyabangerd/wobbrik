package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.game.*
import com.aladdin.wobbrik.engine.AcceptedAttemptCommands
import com.aladdin.wobbrik.engine.AcceptedEngineHost
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
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileShellRouteRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File(System.getProperty("wobbrik.render.output", "/Users/mac/My Workspace/Wobbrik/docs/reviews/unified-mobile-visual")).apply { mkdirs() }
 private fun capture(name:String) {
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
   val warm=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(warm));warm.recycle();v.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   var result=-1
   android.view.PixelCopy.request(compose.activity.window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   File(out,"production-$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
   b.recycle()
  }
  painted(name,"Wobbrik")
  painted(name,"Oyunu başlat")
 }
 private fun region(name:String, description:String):Bitmap {
  val bounds=compose.onNodeWithContentDescription(description).fetchSemanticsNode().boundsInRoot
  val source=BitmapFactory.decodeFile(File(out,"production-$name.png").path)
  val crop=Bitmap.createBitmap(source,bounds.left.toInt().coerceAtLeast(0),bounds.top.toInt().coerceAtLeast(0),
   bounds.width.toInt(),bounds.height.toInt())
  source.recycle()
  return crop
 }
 private fun painted(name:String,description:String) {
  val b=region(name,description)
  val colors=HashSet<Int>()
  for(y in 0 until b.height step 3) for(x in 0 until b.width step 3) colors.add(b.getPixel(x,y))
  b.recycle()
  assertTrue("$name $description must contain rendered art",colors.size>32)
 }
 private fun flow(font:Float,name:String) {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val styles=listOf("İçərişəhər","Kərpic məhəllə","Sahil","Müasir şəhər","Bağ evi","Emalatxana")
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",(1..6).flatMap { i -> FinishCosmetics.categories.map { category ->
   CatalogItem("base_${i}_$category",if(i==1 && category=="wall") "Əhəngdaşı divar" else "${styles[i-1]} · ${mapOf("wall" to "Divar", "window" to "Pəncərə", "entrance" to "Giriş", "roof" to "Dam", "lighting" to "İşıq")[category]}",category,"free",0,null,null)
  }},emptyList())
  val snapshot=GameSnapshot("wobbrik-v1",1,1250,1050,null,AccountRecovery("now","first","later",7,0,emptyList(),emptyList(),emptyList(),emptyList(),28))
  fun json(id:Int):String {
   val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_${id}_$it\""}
   return """{"id":"b-$id","revision":1,"floors":10,"residents":40,"base_value":20,"max_price":24,"parts":{$parts},"traded":false,"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0..9).joinToString(","){(it*2500).toString()}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
  }
  var cityReads=0
  var failCity=true
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,body->
   assertNull("Scene navigation must remain read-only",body)
   if (path.startsWith("city?")) {
    cityReads++
    if (failCity) return@GameplayTransport GameplayResponse(503,"""{"error":"unavailable"}""")
   }
   GameplayResponse(200,when {
   path.startsWith("inventory?")->"""{"buildings":[${(1..4).joinToString(","){json(it)}}]}"""
   path.startsWith("buildings/")->json(path.substringAfter("b-").toInt())
   path == "profile"->"""{"revision":1,"display_name":"İnşaatçı","avatar_id":"builder"}"""
   path.startsWith("market?")->"""{"listings":[]}"""
   path.startsWith("leaderboard?")->"""{"scope":"weekly","week_start":"2026-11-09","snapshot_at":"2026-11-15T00:00:00Z","expires_at":"2026-11-16T00:00:00Z","entries":[]}"""
   path.startsWith("city?")->"""{"rules_version":"wobbrik-v1","geometry_version":"city-geometry-v1","placement_enabled":true,"revision":1,"population":"40","parcels":[{"kind":"land","id":"base","position":{"x_ticks":"0","y_ticks":"0","quarter_turns":0}}],"buildings":[{"building_id":"b-1","building_revision":1,"residents":40,"position":{"x_ticks":"0","y_ticks":"0","quarter_turns":0}}]}"""
   path.startsWith("decorations?")->"""{"copies":[]}"""
   else->error(path)
  })})
  var submissions=0
  val backend=object:AttemptBackend {
   override suspend fun sync(session:appbackend.wobbrik.billing.BillingSession)=snapshot
   override suspend fun catalog(session:appbackend.wobbrik.billing.BillingSession)=catalog
   override suspend fun submit(session:appbackend.wobbrik.billing.BillingSession,command:AttemptCommand) { submissions++ }
  }
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=Unit
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }
  try {
   val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,scope);owner.onAuthChanged("alice")
   val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
   val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(reads),sessions,DecorationRepository(reads),CitySceneRepository(reads), market=MarketRepository(reads), profiles=reads, leaderboard=LeaderboardRepository(reads))
   var page by mutableStateOf("inventory");var selected by mutableStateOf<String?>(null)
   var plays=0
   var finishParts by mutableStateOf(FinishCosmetics.selection(catalog,snapshot,emptyMap()))
   var confirmedParts: Map<String,String>? = null
   compose.setContent {
    val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
     MaterialTheme { MobileMenuShell(page, snapshot.coins, {plays++}, {page=it}) {
      key(page) {when(page) {
       "inventory"->InventoryScreen(configured,ready,selected,{selected=it},{page="home"},{page="parts"},{page="decorations"},{page="land"})
       "parts"->PartsScreen(configured,ready,null,{page="inventory"})
       "city"->CityScreen(configured,AccountView(ReadOnlyAccount("alice",snapshot,catalog),ready),{page="home"},{page="inventory"},lifeTimeSeconds=0.0)
       "decorations"->DecorationShopScreen(configured,ready,{page="inventory"})
       "land"->LandShopScreen(configured,ready,{page="inventory"})
       "market"->MarketScreen(configured,ready,{page="home"},{page="inventory"},{page="city"})
       "account"->PublicAccountScreen(configured,ready,{page="home"},{page=it})
       "settings"->SettingsScreen(DevicePreferences(),false,null,{_,_->},{},{},{},{page="account"})
       "collection"->FinishCosmeticScreen(AcceptedEngineHost.Snapshot(finishParts=finishParts),ready,{category,id->finishParts=finishParts+(category to id)},{confirmedParts=finishParts;page="inventory"},{page="inventory"})
       "leaderboard"->LeaderboardScreen(configured,ready,{page="achievements"})
       else->AccountScreen(page,ready,null,{page=it},{},{plays++},false)
      }}
     }}
    }
   }
   fun dock() {
    compose.onAllNodesWithContentDescription("Wobbrik").assertCountEquals(1)
    val play=compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed()
    val bounds=play.fetchSemanticsNode().boundsInRoot
    val root=compose.onRoot().fetchSemanticsNode().boundsInRoot
    assertEquals(root.center.x,bounds.center.x,1f)
    assertTrue(bounds.height>=48*compose.density.density-1)
    play.performClick()
   }
   dock();capture("shell-inventory-$name")
   val card=compose.onAllNodesWithText("10 mərtəbə  ›")[0].assertIsDisplayed()
   val cardBounds=card.fetchSemanticsNode().boundsInRoot
   val playBounds=compose.onNodeWithContentDescription("Oyunu başlat").fetchSemanticsNode().boundsInRoot
   assertTrue("First card must fit above dock",cardBounds.bottom < playBounds.top)
   if (font>1f) {
    compose.onAllNodesWithText("Ərazi").assertCountEquals(0)
    compose.onNodeWithText("Dekor").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Binalar").performScrollTo()
   }
   // The fixture intentionally fails city reads until the city retry segment.
   // Detail needs that read; the dedicated commerce host covers its actual input route.
   // Continue the existing production parts regression without changing failCity early.
   compose.runOnIdle { page="parts" }
   compose.onNodeWithText("Əhəngdaşı divar").assertIsDisplayed()
   dock();capture("shell-parts-$name")
   compose.onNodeWithContentDescription("Şəhərim").performClick()
   if (font>1f) compose.onNodeWithText("Şəhər yüklənmədi").performScrollTo()
   compose.onNodeWithText("Şəhər yüklənmədi").assertIsDisplayed()
   compose.onNodeWithText("Plan").assert(hasAnyAncestor(isNotEnabled()))
   compose.onNodeWithText("Bax").assertDoesNotExist()
   compose.onNodeWithText("Binaya bax").assertDoesNotExist()
   compose.onNodeWithText("Yenilə").assertIsDisplayed()
   val retryBounds=compose.onNodeWithText("Yenilə").fetchSemanticsNode().boundsInRoot
   assertTrue("Retry target at least 48dp",retryBounds.height / compose.activity.resources.displayMetrics.density >= 48f)
   dock();capture("shell-city-error-$name")
   compose.runOnIdle { failCity=false }
   compose.onNodeWithText("Yenilə").assertIsDisplayed().performTouchInput { click() }
   if (font>1f) compose.onNodeWithText("Əhali: 40").performScrollTo()
   compose.onNodeWithText("Əhali: 40").assertIsDisplayed()
   compose.onNodeWithText("Şəhər yüklənmədi").assertDoesNotExist()
   compose.onNodeWithText("Plan").assert(hasAnyAncestor(isNotEnabled()))
   if (font>1f) compose.onNodeWithContentDescription("Bina b-1: 10 mərtəbə; 1 torpaq sahəsi; 0 yerləşdirilmiş dekor").performScrollTo()
   compose.onNodeWithContentDescription("Bina b-1: 10 mərtəbə; 1 torpaq sahəsi; 0 yerləşdirilmiş dekor").assertIsDisplayed()
   compose.onNodeWithText("Plan").assert(hasAnyAncestor(isNotEnabled()))
   assertEquals("Inventory membership read, one failed read, then revision-bracketed retry",4,cityReads)
   capture("shell-city-$name")
   compose.onNode(hasText("Bax",substring=true) and hasClickAction()).performTouchInput { click() }
   if (font>1f) compose.onNodeWithText("Bina görünüşü").performScrollTo()
   compose.onNodeWithText("Bina görünüşü").assertIsDisplayed()
   compose.onNodeWithText("Geri").performTouchInput { click() }
   if (font>1f) compose.onNodeWithText("Binaya bax").performScrollTo()
   compose.onNodeWithText("Binaya bax").assertIsDisplayed()
   compose.onNodeWithContentDescription("İnventar").performClick()
   if (font>1f) compose.onNodeWithText("Görünüş").performScrollTo()
   compose.onNodeWithText("Görünüş").assertIsDisplayed()
   compose.onNodeWithText("Görünüş").performClick()
   dock();capture("shell-appearance-$name")
   if (font>1f) compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
   capture("shell-preview-before-$name")
   val before=region("shell-preview-before-$name","Seçilmiş bina görünüşü")
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Emalatxana · Divar"))
   compose.onNodeWithContentDescription("Emalatxana · Divar").performScrollTo().performTouchInput { click() }
   compose.onNodeWithContentDescription("Emalatxana · Divar").assertIsSelected()
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Kərpic məhəllə · Divar"))
   compose.onNodeWithContentDescription("Kərpic məhəllə · Divar").performScrollTo().performTouchInput { click() }
   compose.onNodeWithContentDescription("Kərpic məhəllə · Divar").assertIsSelected()
   assertEquals("base_2_wall",finishParts["wall"])
   capture("shell-picker-selected-$name")
   if (font>1f) compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
   compose.onNodeWithContentDescription("Seçilmiş bina görünüşü").assertIsDisplayed()
   capture("shell-appearance-selected-$name")
   val after=region("shell-appearance-selected-$name","Seçilmiş bina görünüşü")
   assertEquals(before.width,after.width);assertEquals(before.height,after.height)
   assertFalse("Selected wall must change native preview pixels",before.sameAs(after))
   before.recycle();after.recycle()
   compose.onNodeWithText("✓ Təsdiqlə").assertIsDisplayed().performTouchInput { click() }
   compose.runOnIdle { assertEquals("inventory",page);assertEquals(finishParts,confirmedParts) }
   compose.onNodeWithText("Binalar").assertIsDisplayed()
   compose.runOnIdle {page="decorations"}
   dock();capture("shell-decorations-$name")
   compose.onNodeWithContentDescription("Nailiyyətlər").performClick()
   if (font>1f) compose.onNodeWithText("Rekordlar").performScrollTo()
   compose.onNodeWithText("Rekordlar").assertIsDisplayed()
   dock();capture("shell-achievements-$name")
   compose.onNodeWithText("Rekordlar").performClick()
   val recordsTab = compose.onNode(hasText("Rekordlar") and hasClickAction())
   if (font>1f) recordsTab.performScrollTo()
   recordsTab.assertIsDisplayed().assertIsSelected().assertHeightIsAtLeast(48.dp)
   recordsTab.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { action ->
     val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
     action(layouts)
     val layout=layouts.single()
     assertTrue("records tab glyphs overflow",layout.getLineRight(0)<=layout.size.width+1f)
    }
   if (font>1f) compose.onNodeWithText("Hələ qəbul edilmiş nəticə yoxdur.").performScrollTo().assertIsDisplayed()
   dock();capture("shell-records-$name")
   compose.onNodeWithContentDescription("Bazar").performClick()
   dock();capture("shell-market-$name")
   compose.onNodeWithContentDescription("Profil").performClick()
   compose.onNodeWithText("Parametrlər").assertIsDisplayed().performClick()
   dock();capture("shell-settings-$name")
   compose.onNodeWithContentDescription("Profil").performClick()
   dock();capture("shell-profile-$name")
   if(font<=1f && name=="390-font1") compose.onNodeWithTag("profile-edit").performScrollTo()
   compose.onNodeWithTag("profile-edit").performClick()
   compose.onNodeWithTag("profile-avatar").performScrollTo().performClick()
   compose.onNodeWithTag("avatar-pilot").performClick()
   compose.onNodeWithTag("avatar-apply").performClick()
   compose.onNodeWithContentDescription("Pilot avatarı").performScrollTo().assertIsDisplayed()
   compose.onNodeWithTag("profile-cancel").performClick()
   compose.onAllNodesWithContentDescription("İnşaatçı avatarı").onFirst().performScrollTo().assertIsDisplayed()
   assertEquals(10,plays)
   assertEquals(0,submissions)
  } finally {scope.cancel()}
 }
 @Test fun recoveryLongBalanceInsets() {
  var retry=0
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,2f)) {
    MaterialTheme { Box(Modifier.fillMaxSize().padding(top=24.dp,bottom=48.dp)) {
     MobileMenuShell("market",Long.MAX_VALUE,null,{}) {
      AccountScreen("home",null,"Bağlantı yoxdur",{},{retry++},null,false)
     }
    } }
   }
  }
  compose.onAllNodesWithContentDescription("Wobbrik").assertCountEquals(1)
  compose.onAllNodesWithContentDescription("Oyunu başlat").assertCountEquals(1)
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed().assertIsNotEnabled()
  compose.onNodeWithText("Yenilə").performScrollTo().performClick()
  assertEquals(1,retry)
  capture("shell-recovery-insets")
 }
 private fun cityError(font: Float, name: String, insets: Boolean = false) {
  var retries=0
  var page by mutableStateOf("city")
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme {
     Box(if(insets) Modifier.fillMaxSize().padding(top=24.dp,bottom=48.dp) else Modifier.fillMaxSize()) {
      MobileMenuShell(page,if(insets) Long.MAX_VALUE else 246L,{}, {page=it}) {
       if(page=="city") CityContent(null,true,{retries++},{page="home"},{page="inventory"},lifeTimeSeconds=0.0)
      }
     }
    }
   }
  }
  compose.onNodeWithText("Yenilə").assertIsDisplayed().performTouchInput { click() }
  assertEquals(1,retries)
  compose.onNodeWithText("Bax").assertDoesNotExist()
  compose.onNodeWithText("Binaya bax").assertDoesNotExist()
  compose.onNodeWithText("Plan").assert(hasAnyAncestor(isNotEnabled()))
  capture("city-error-$name")
  if (font>1f) compose.onNodeWithText("Şəhər yüklənmədi").performScrollTo()
  compose.onNodeWithText("Şəhər yüklənmədi").assertIsDisplayed()
  compose.onNodeWithText("Yenilə").assertIsDisplayed()
  capture("city-error-title-$name")
  compose.onNodeWithContentDescription("Geri").assertIsDisplayed().performClick()
  compose.runOnIdle { assertEquals("home",page) }
 }
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun cityErrorSmall()=cityError(1f,"320-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun cityErrorLargeType()=cityError(2f,"390-font2")
 @Test fun cityErrorInsets()=cityError(2f,"360-font2-insets-long-balance",true)
 @Test fun compact()=flow(1f,"360-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular()=flow(1f,"390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun largeFont()=flow(2f,"320-font2")
}
