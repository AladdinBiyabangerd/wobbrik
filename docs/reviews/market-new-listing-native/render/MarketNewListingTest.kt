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

/** Synthetic HTTP responses through production reads, Ready owner, route inputs and native UI. */
abstract class MarketNewListingHost {
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
 // Async atlas IO is intentionally off the UI thread. Wait for actual artwork pixels
 // before recording the initial shop, rather than mistaking a loading frame for parity.
 private fun waitArtwork() {
  compose.waitUntil(15000) {
   var visible = false
   compose.runOnUiThread {
    val v = compose.activity.window.decorView
    val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
    v.draw(Canvas(b))
    var green = 0
    for(y in (b.height / 4) until (b.height * 4 / 5) step 4) for(x in 0 until b.width step 4) {
     val c = b.getPixel(x,y)
     val r = android.graphics.Color.red(c); val g = android.graphics.Color.green(c); val blue = android.graphics.Color.blue(c)
     if(g > r * 1.25 && g > blue * 1.2) green++
    }
    visible = green > 50
    b.recycle()
   }
   visible
  }
 }
 protected fun flow(font:Float, name:String, insets:Boolean=false) {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",(1..6).flatMap {i->FinishCosmetics.categories.map {c->
   CatalogItem("base_${i}_$c",if(c=="wall") (if(insets) "Uzun bina adı bütöv sözlərlə burada" else "Kərpic hörgü $i") else c,c,"free",0,null,null)
  }} + listOf(CatalogItem("decor_tree_1","Ağac","tree","copy",0,25,null),CatalogItem("decor_bench_1","Skamya","bench","copy",0,20,null)),
   listOf(LandOffer("land_1",150,200),LandOffer("land_2",500,400),LandOffer("land_3",1800,900)))
  val snapshot=GameSnapshot("wobbrik-v1",1,if(insets) Long.MAX_VALUE else 1250,1050,null,
   AccountRecovery("now","first","later",7,0,emptyList(),emptyList(),listOf("land_1"),emptyList(),28))
  fun building(id:Int):String {
   val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_${id}_$it\""}
   return """{"id":"b-$id","revision":1,"floors":10,"residents":40,"base_value":20,"max_price":${if(id == 4) Long.MAX_VALUE else 24},"parts":{$parts},"traded":${id == 3},"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0..9).joinToString(","){(it*2500).toString()}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
  }
  fun listing(id:Int)="""{"id":"l-$id","building_id":"b-$id","revision":1,"building_revision":1,"price":350,"fee":18,"status":"active"}"""
  var failDecor=false
  var failCity=false
  var cityFailureStatus=503
  var submissions=0
  val commands=mutableListOf<AttemptCommand>()
  val requests=mutableListOf<String>()
  var inventoryBytes=0
  var inventoryRequests=0
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,body->
   requests+=path
   assertNull(body)
   if(failCity && path.startsWith("city?")) return@GameplayTransport GameplayResponse(cityFailureStatus,"""{"error":"unavailable"}""")
   if(failDecor && path.startsWith("decorations?")) return@GameplayTransport GameplayResponse(503,"""{"error":"unavailable"}""")
   GameplayResponse(200,when {
    path.startsWith("inventory?")->"""{"buildings":[${(1..4).joinToString(","){building(it)}}]}""".also { inventoryBytes+=it.toByteArray().size;inventoryRequests++ }
    path.startsWith("buildings/")->building(path.substringAfter("b-").toInt())
    path.startsWith("market?") || path.startsWith("market/own?")->"""{"listings":[${(1..4).joinToString(","){listing(it)}}]}"""
    path.startsWith("market/l-")->path.substringAfter("l-").toInt().let {"""{"listing":${listing(it)},"building":${building(it)}}"""}
    path.startsWith("city?")->"""{"rules_version":"wobbrik-v1","revision":1,"population":"40","buildings":[{"building_id":"b-1","building_revision":1,"residents":40},{"building_id":"b-2","building_revision":2,"residents":40}]}"""
    path.startsWith("decorations?")->"""{"copies":[{"id":"copy-a","item_id":"decor_bench_1","position":{"x_ticks":"0","y_ticks":"0","quarter_turns":0}},{"id":"copy-b","item_id":"decor_tree_1"}]}"""
    else->error(path)
   })
  })
  val backend=object:AttemptBackend {
   override suspend fun sync(session:appbackend.wobbrik.billing.BillingSession)=snapshot
   override suspend fun catalog(session:appbackend.wobbrik.billing.BillingSession)=catalog
   override suspend fun submit(session:appbackend.wobbrik.billing.BillingSession,command:AttemptCommand) {submissions++;commands+=command}
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
   var page by mutableStateOf("market")
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
   fun waitText(text:String) {compose.waitUntil(15000) {compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()}}
   fun marketLoaded(action:String, scroll:Boolean=true) {
    compose.waitUntil(15000) { compose.onAllNodesWithContentDescription("Bazar yüklənir").fetchSemanticsNodes().isEmpty() }
    if(scroll) compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(action))
    waitText(action)
   }
   fun go(route:String) { compose.runOnIdle {page=route;selected=null};compose.waitForIdle() }
   go("market-own");marketLoaded("Elanı geri götür")
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("+ Yeni elan"))
   val coldStart=System.nanoTime()
   compose.onNodeWithText("+ Yeni elan").performClick();marketLoaded("Seç →")
   val firstMetadataMs=(System.nanoTime()-coldStart)/1_000_000
   val pickerReads=requests.size
   compose.waitForIdle();if(font == 1f) waitArtwork();capture("picker-$name")
   compose.onAllNodesWithText("Seç →")[0].performScrollTo().performClick();waitText("Satış qiyməti")
   compose.onNodeWithText("Elanı dərc et").assertIsNotEnabled().assertIsDisplayed()
   capture("price-empty-$name")
   val input=compose.onNodeWithContentDescription("Qiymət")
   input.performScrollTo().performTextInput("0")
   compose.onNodeWithText("Elanı dərc et").assertIsNotEnabled()
   input.performTextReplacement("25")
   compose.onNodeWithText("Elanı dərc et").assertIsNotEnabled()
   input.performTextReplacement("9223372036854775808")
   compose.onNodeWithText("Elanı dərc et").assertIsNotEnabled()
   input.performTextReplacement("24")
   compose.onNodeWithText("Elanı dərc et").assertIsEnabled().assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   compose.onNode(hasScrollAction() and hasAnyDescendant(hasText("Satış qiyməti")))
       .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 10000f) }
   compose.onNodeWithText("Komissiya: 2").assertIsDisplayed()
   compose.onNodeWithText(" · Sənə qalır: 22").assertExists()
   capture("price-valid-$name")
   if(name == "regular") {
    compose.runOnUiThread {
     val height = (260 * compose.activity.resources.displayMetrics.density).toInt()
     val ime = android.view.WindowInsets.Builder().setInsets(android.view.WindowInsets.Type.ime(), android.graphics.Insets.of(0,0,0,height))
       .setVisible(android.view.WindowInsets.Type.ime(),true).build()
     fun dispatch(v: android.view.View) {
       if(v.javaClass.name == "androidx.compose.ui.platform.AndroidComposeView") v.dispatchApplyWindowInsets(ime)
       if(v is android.view.ViewGroup) for(i in 0 until v.childCount) dispatch(v.getChildAt(i))
     }
     dispatch(compose.activity.window.decorView)
    }
    compose.waitForIdle()
    compose.onAllNodesWithText("Bazar").assertCountEquals(0)
    compose.onNodeWithText("Elanı dərc et").assertIsDisplayed()
    compose.mainClock.advanceTimeBy(1000);compose.waitForIdle()
    capture("price-simulated-ime-warm-$name")
    compose.mainClock.advanceTimeBy(1000);compose.waitForIdle()
    capture("price-simulated-ime-$name")
    compose.runOnUiThread {
     val clear = android.view.WindowInsets.Builder().setInsets(android.view.WindowInsets.Type.ime(),android.graphics.Insets.NONE)
       .setVisible(android.view.WindowInsets.Type.ime(),false).build()
     fun dispatch(v: android.view.View) {
       if(v.javaClass.name == "androidx.compose.ui.platform.AndroidComposeView") v.dispatchApplyWindowInsets(clear)
       if(v is android.view.ViewGroup) for(i in 0 until v.childCount) dispatch(v.getChildAt(i))
     }
     dispatch(compose.activity.window.decorView)
    }
    compose.waitForIdle()
   }
   assertEquals("Price edits do not read or mutate",pickerReads,requests.size)
   assertEquals(0,submissions)
   val warmStart=System.nanoTime()
   compose.onNodeWithText("Dəyiş").performScrollTo().performClick();marketLoaded("Seç →")
   val warmMs=(System.nanoTime()-warmStart)/1_000_000
   assertEquals("Return to picker reuses its bounded accepted page",pickerReads,requests.size)
   compose.onAllNodesWithText("Seç →")[0].performScrollTo().performClick();waitText("Satış qiyməti")
   compose.onNodeWithContentDescription("Qiymət").performScrollTo().performTextInput("24")
   compose.onNodeWithText("Elanı dərc et").performClick()
   compose.waitUntil(15000) { submissions == 1 }
   compose.runOnIdle { assertEquals(AttemptCommand.Route.MARKET_LIST,commands.single().route) }
   println("LISTING $name inventoryRequests=$inventoryRequests inventoryResponseBytes=$inventoryBytes firstMetadataHostMs=$firstMetadataMs warmReturnHostMs=$warmMs editsAndReturnRequests=0 submissions=$submissions")
  } finally {scope.cancel()}
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarketNewListingRegularTest:MarketNewListingHost() {@Test fun routes()=flow(1f,"regular")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarketNewListingSmallTest:MarketNewListingHost() {@Test fun routes()=flow(1f,"small")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarketNewListingFontTest:MarketNewListingHost() {@Test fun routes()=flow(2f,"font")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarketNewListingRegularFontTest:MarketNewListingHost() {@Test fun routes()=flow(2f,"regularfont")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarketNewListingInsetsTest:MarketNewListingHost() {@Test fun routes()=flow(2f,"insets",true)}
