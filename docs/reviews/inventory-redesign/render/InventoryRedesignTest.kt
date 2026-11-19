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

/** Production composables + repositories with synthetic HTTP, not a device/account screenshot. */
abstract class InventoryRedesignHost {
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
 protected fun flow(font:Float, name:String, insets:Boolean=false) {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",(1..6).flatMap {i->FinishCosmetics.categories.map {c->
   CatalogItem("base_${i}_$c",if(c=="wall") (if(insets) "Uzun bina adı bütöv sözlərlə burada" else mapOf(1 to "Əhəngdaşı divar", 2 to "Kərpic hörgü", 3 to "Suvaq fasad")[i] ?: "Divar") else c,c,"free",0,null,null)
  }} + listOf(CatalogItem("decor_tree_1","Ağac","tree","copy",0,25,null),CatalogItem("decor_bench_1","Skamya","bench","copy",0,20,null)),
   listOf(LandOffer("land_1",150,200),LandOffer("land_2",500,400),LandOffer("land_3",1800,900)))
  val snapshot=GameSnapshot("wobbrik-v1",1,if(insets) Long.MAX_VALUE else 411,1050,null,
   AccountRecovery("now","first","later",7,0,emptyList(),emptyList(),listOf("land_1"),emptyList(),28))
  fun building(id:Int):String {
   val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_${if(it=="wall") (if(id==2) 3 else 2) else 1}_$it\""}
   return """{"id":"b-$id","revision":1,"floors":${listOf(15,16,10,12)[(id-1)%4]},"residents":${listOf(15,16,10,12)[(id-1)%4]*4},"base_value":20,"max_price":${if(id == 4) Long.MAX_VALUE else 30},"parts":{$parts},"traded":${id == 3},"in_city":${id==3},"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0 until listOf(15,16,10,12)[(id-1)%4]).joinToString(","){(if(id==4) it*1200 else 0).toString()}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
  }
  fun listing(id:Int)="""{"id":"l-$id","building_id":"b-$id","revision":1,"building_revision":1,"price":24,"fee":2,"status":"active"}"""
  var failDecor=false
  var failCity=false
  var cityFailureStatus=503
  var submissions=0
  var inventoryReads=0; var cityReads=0; var bytes=0
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,body->
   println("REQUEST $path")
   assertNull(body)
   if(path.startsWith("inventory?")) inventoryReads++
   if(path.startsWith("city?")) cityReads++
   if(failCity && path.startsWith("city?")) return@GameplayTransport GameplayResponse(cityFailureStatus,"""{"error":"unavailable"}""")
   if(failDecor && path.startsWith("decorations?")) return@GameplayTransport GameplayResponse(503,"""{"error":"unavailable"}""")
   val response = when {
    path.startsWith("inventory?")->"""{"buildings":[${(1..9).joinToString(","){building(it)}}]}"""
    path.startsWith("buildings/")->building(path.substringAfter("b-").substringBefore("?").toInt())
    path.startsWith("market?") || path.startsWith("market/own?")->"""{"listings":[${(1..4).joinToString(","){listing(it)}}]}"""
    path.startsWith("market/l-")->path.substringAfter("l-").toInt().let {"""{"listing":${listing(it)},"building":${building(it)}}"""}
    path.startsWith("city?")->"""{"rules_version":"wobbrik-v1","revision":1,"population":"40","buildings":[{"building_id":"b-1","building_revision":1,"residents":40},{"building_id":"b-2","building_revision":2,"residents":40}]}"""
    path.startsWith("decorations?")->"""{"copies":[{"id":"copy-a","item_id":"decor_bench_1","position":{"x_ticks":"0","y_ticks":"0","quarter_turns":0}},{"id":"copy-b","item_id":"decor_tree_1"}]}"""
    else->error(path)
   }
   bytes+=response.toByteArray().size
   GameplayResponse(200,response)
  })
  val backend=object:AttemptBackend {
   override suspend fun sync(session:appbackend.wobbrik.billing.BillingSession)=snapshot
   override suspend fun catalog(session:appbackend.wobbrik.billing.BillingSession)=catalog
   override suspend fun submit(session:appbackend.wobbrik.billing.BillingSession,command:AttemptCommand) {submissions++}
  }
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=Unit
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }
  try {
   val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,scope);owner.onAuthChanged("alice")
   val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
   val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(reads),sessions,DecorationRepository(reads),CitySceneRepository(reads),market=MarketRepository(reads))
   val view=AccountView(ReadOnlyAccount("alice",snapshot,catalog),ready)
   var page by mutableStateOf("inventory")
   var selected by mutableStateOf<String?>(null)
   compose.setContent { val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) { MaterialTheme {
     Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
      MobileMenuShell(page,snapshot.coins,{}, {page=it}) { key(page) { when(page) {
       "inventory"->InventoryScreen(configured,view,selected,{selected=it},{page="home"},{page="parts"},{page="owned-decorations"},{page="owned-land"},onMarket={page="market-own"})
       "parts"->PartsScreen(configured,ready,selected,{page="inventory"})
       "owned-decorations","owned-land"->OwnedCityItemsScreen(configured,view,page=="owned-land",{page="inventory"},{page="decorations"})
       "decorations"->DecorationShopScreen(configured,ready,{page="market"},{page="market-own"})
       "market","market-own"->MarketScreen(configured,ready,{page="home"},{page="inventory"},{page="city"},initialOwn=page=="market-own")
      } } }
     }
    } }
   }

   compose.waitUntil(15000) {compose.onAllNodesWithText("9 bina").fetchSemanticsNodes().isNotEmpty()}
   compose.waitUntil(15000) {compose.onAllNodesWithContentDescription("15 mərtəbəli bina").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithText("Binalar").assertIsDisplayed()
   compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   compose.onNodeWithContentDescription("Yenilə").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   for(label in listOf("Ana","Şəhər","İnventar","Bazar","Uğurlar","Profil")) {
    compose.onNodeWithContentDescription(label).assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   }
   capture("inventory-$name")
   // A fixed overlay must never prevent scrolling the final card above its controls.
   compose.onNode(hasScrollToIndexAction()).performScrollToIndex(if(font > 1f) 8 else 4)
   compose.onAllNodesWithText("15 mərtəbə").onLast().performScrollTo().assertIsDisplayed()
   val lastCard = compose.onAllNodesWithText("15 mərtəbə").onLast().fetchSemanticsNode().boundsInRoot
   val play = compose.onNodeWithContentDescription("Oyunu başlat").fetchSemanticsNode().boundsInRoot
   assertTrue("Last card must scroll clear of dock", lastCard.bottom <= play.top)
   compose.onAllNodesWithText("15 mərtəbə")[0].performScrollTo().performClick()

   compose.waitUntil(15000) {compose.onAllNodesWithText("✓ Sənindir").fetchSemanticsNodes().isNotEmpty()}
   capture("detail-$name")
   compose.onNodeWithText("✓ Sənindir").assertHasNoClickAction()
   compose.onNodeWithText("60").performScrollTo().assertIsDisplayed()
   compose.onNodeWithText("Maksimum satış").performScrollTo().assertIsDisplayed()
   compose.onNodeWithText("30").assertExists()
   compose.onNodeWithText("Bina görünüşü").performScrollTo().assertIsDisplayed().assertIsEnabled().assertHeightIsAtLeast(GameTokens.Target)
   compose.onNodeWithText("Satışa çıxar").performScrollTo().assertIsEnabled().assertHeightIsAtLeast(GameTokens.Target)
   capture("detail-actions-$name")
   compose.onNodeWithText("Binanı sil").performScrollTo().assertIsDisplayed().performClick()
   compose.onNodeWithText("Bina silinsin?").assertIsDisplayed()
   compose.runOnIdle { assertEquals(0,submissions) }
   compose.onNodeWithText("Ləğv et").performClick()
   compose.onNodeWithContentDescription("Geri").performClick()
   compose.waitUntil(15000) {compose.onAllNodesWithText("9 bina").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithContentDescription("Yenilə").performClick()
   compose.waitUntil(15000) {inventoryReads >= 2}
   compose.waitForIdle()
   compose.runOnIdle {assertEquals(0,cityReads); assertEquals(0,submissions)}
   println("INVENTORY-REDESIGN $name inventoryReads=$inventoryReads cityReads=$cityReads bytes=$bytes")
   if(font > 1f) compose.onNodeWithText("Dekor").performScrollTo()
   compose.onNodeWithText("Dekor").assertIsDisplayed().performClick()
   compose.runOnIdle { assertEquals("owned-decorations",page) }
  } finally {scope.cancel()}
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class InventoryRedesignRegularTest:InventoryRedesignHost() {@Test fun render()=flow(1f,"regular",false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class InventoryRedesignSmallTest:InventoryRedesignHost() {@Test fun render()=flow(1f,"small",false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class InventoryRedesignRegularFontTest:InventoryRedesignHost() {@Test fun render()=flow(2f,"regularfont",false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class InventoryRedesignSmallFontTest:InventoryRedesignHost() {@Test fun render()=flow(2f,"smallfont",false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class InventoryRedesignInsetsTest:InventoryRedesignHost() {@Test fun render()=flow(2f,"insets",true)}
