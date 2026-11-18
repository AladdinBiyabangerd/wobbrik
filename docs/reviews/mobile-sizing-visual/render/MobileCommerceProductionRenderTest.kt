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
abstract class MobileCommerceProductionHost {
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
   val out=File(System.getProperty("wobbrik.render.output","/private/tmp/wobbrik-commerce-native")).apply {mkdirs()}
   File(out,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
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
  fun listing(id:Int)="""{"id":"l-$id","building_id":"b-$id","revision":1,"building_revision":1,"price":24,"fee":2,"status":"active"}"""
  var failDecor=false
  var failCity=false
  var cityFailureStatus=503
  var submissions=0
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,body->
   assertNull(body)
   if(failCity && path.startsWith("city?")) return@GameplayTransport GameplayResponse(cityFailureStatus,"""{"error":"unavailable"}""")
   if(failDecor && path.startsWith("decorations?")) return@GameplayTransport GameplayResponse(503,"""{"error":"unavailable"}""")
   GameplayResponse(200,when {
    path.startsWith("inventory?")->"""{"buildings":[${(1..4).joinToString(","){building(it)}}]}"""
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
   fun waitText(text:String) {compose.waitUntil(15000) {compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()}}
   fun marketLoaded(action:String) {
    compose.waitUntil(15000) { compose.onAllNodesWithContentDescription("Bazar yüklənir").fetchSemanticsNodes().isEmpty() }
    compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(action))
    waitText(action)
   }
   fun go(route:String) { compose.runOnIdle {page=route;selected=null};compose.waitForIdle() }
   waitText("10 mərtəbə  ›")
   compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed()
   compose.onAllNodesWithContentDescription("Geri").assertCountEquals(0)
   compose.onAllNodesWithText("✓ Şəhərdə").assertCountEquals(1)
   compose.onNodeWithText("✓ Şəhərdə").assertIsDisplayed()
   capture("inventory-$name")
   compose.onAllNodesWithText("10 mərtəbə  ›")[0].performScrollTo().performClick()
   waitText("✓ Sənindir")
   compose.onNodeWithText("İnventar · Binalar").assertIsDisplayed().assertHasNoClickAction()
   compose.onNodeWithContentDescription("Geri").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   compose.onNodeWithText("Bina görünüşü").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   capture("detail-$name")
   compose.onNodeWithText("✓ Sənindir").performScrollTo().assertIsDisplayed().assertHasNoClickAction()
   capture("detail-ownership-$name")
   compose.onNodeWithText("Bina məlumatı").performScrollTo().assertIsDisplayed()
   capture("detail-information-heading-$name")
   compose.onNodeWithText("Hündürlük").performScrollTo().assertIsDisplayed()
   compose.onNodeWithText("Bina növü").performScrollTo().assertIsDisplayed()
   capture("detail-information-$name")
   compose.onNodeWithText("Sakinlər").performScrollTo().assertIsDisplayed()
   compose.onNodeWithText("40").performScrollTo().assertIsDisplayed()
   compose.onAllNodesWithText("Səviyyə", substring = true).assertCountEquals(0)
   capture("detail-information-residents-$name")
   compose.onNodeWithText("Şəhər statusu").performScrollTo().assertIsDisplayed().assertHasNoClickAction()
   compose.onNodeWithText("✓ Şəhərdə yerləşdirilib").performScrollTo().assertIsDisplayed().assertHasNoClickAction()
   capture("detail-city-status-$name")
   compose.onNodeWithText("Maksimum satış").performScrollTo().assertIsDisplayed().assertHasNoClickAction()
   // The coin row owns horizontal scroll; reveal that row through the outer detail scroll first.
   compose.onNodeWithText("24").onParent().performScrollTo()
   compose.onNodeWithText("24").assertIsDisplayed().assertHasNoClickAction()
   capture("detail-sale-price-$name")
   compose.onNodeWithText("Binanı sil").performScrollTo().assertIsDisplayed().assertIsEnabled().assertHeightIsAtLeast(GameTokens.Target)
   capture("detail-delete-action-$name")
   compose.onNodeWithText("Binanı sil").performClick()
   waitText("Bina silinsin?")
   compose.runOnIdle { assertEquals(0, submissions) }
   capture("detail-delete-confirm-$name", true)
   compose.onNodeWithText("Ləğv et").assertIsDisplayed().performClick()
   compose.onAllNodesWithText("Bina silinsin?").assertCountEquals(0)
   compose.onNodeWithText("Bina görünüşü").assertIsDisplayed()
   compose.onNodeWithText("Bina görünüşü").performClick()
   compose.runOnIdle {assertEquals("parts",page)}
   for ((index, status) in listOf(1 to "Şəhər məlumatı dəyişib. Yenilə.", 2 to "İnventardadır")) {
    go("inventory");waitText("10 mərtəbə  ›")
    compose.runOnIdle { selected = "b-${index + 1}" }
    waitText("✓ Sənindir")
    compose.onNodeWithText(status).performScrollTo().assertIsDisplayed().assertHasNoClickAction()
    compose.onAllNodesWithText("✓ Şəhərdə yerləşdirilib").assertCountEquals(0)
   }
   compose.onNodeWithText("Alınmış bina yenidən satılmır").performScrollTo().assertIsDisplayed()
   compose.onNodeWithText("Satışa çıxar").assertIsNotEnabled()
   compose.onNodeWithText("Binanı sil").performScrollTo().assertIsEnabled()
   capture("detail-traded-$name")
   go("inventory");waitText("10 mərtəbə  ›")
   compose.runOnIdle { selected = "b-4" }
   waitText("✓ Sənindir")
   compose.onAllNodesWithText(Long.MAX_VALUE.toString()).onLast().onParent().performScrollTo()
   compose.onAllNodesWithText(Long.MAX_VALUE.toString()).onLast().assertIsDisplayed().assertHasNoClickAction()
   capture("detail-sale-long-price-$name")
   go("owned-decorations");waitText("✓ Şəhərdə")
   capture("owned-decor-$name")
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Saxlanılır"))
   compose.onNodeWithText("Yerləşdir").assertIsNotEnabled()
   go("inventory")
   compose.runOnIdle {failDecor=true}
   go("owned-decorations");waitText("İnventar yenilənmədi.")
   compose.runOnIdle {failDecor=false}
   compose.onNodeWithText("Yenidən cəhd et").performScrollTo().performClick();waitText("✓ Şəhərdə")
   compose.onAllNodesWithText("Ərazi").assertCountEquals(0)
   go("decorations");waitText("Nüsxə: 1");capture("decor-shop-$name")
   compose.onAllNodesWithText("✓ Sahibsən")[0].performScrollTo().assertIsDisplayed()
   compose.onAllNodesWithText("Nüsxə: 1")[0].performScrollTo().assertIsDisplayed()
   if(font > 1f) compose.onNode(hasScrollToIndexAction()).performSemanticsAction(
       androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { scroll ->
       scroll(0f, with(compose.density) { (GameTokens.Target * 2).toPx() })
   }
   capture("decor-ownership-$name")
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Skamya"))
   compose.onNodeWithText("Skamya").performClick();compose.waitForIdle()
   // Owning a copy must not disable buying another one.
   compose.onAllNodesWithText("Al").onLast().assertIsEnabled()
   capture("decor-selected-$name",font>1f)
   compose.onNodeWithText("Ləğv et").assertIsDisplayed().performClick()
   compose.onNodeWithText("Mənim elanlarım").performClick();marketLoaded("Elanı geri götür")
   compose.onNodeWithText("+ Yeni elan").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   compose.onAllNodesWithText("Ərazi").assertCountEquals(0)
   compose.onAllNodesWithText("Süzgəc").assertCountEquals(0)
   compose.onAllNodesWithText("✓ Satışdadır")[0].assertExists()
   compose.onAllNodesWithText("Elanı geri götür")[0].assertHeightIsAtLeast(GameTokens.Target)
   capture("own-listings-$name")
   compose.onAllNodesWithText("Elanı geri götür")[0].performScrollTo().performClick();waitText("Elan geri götürülsün?")
   capture("withdraw-confirm-$name",true)
   compose.onNodeWithText("Ləğv et").performClick()
   compose.onNodeWithContentDescription("Geri").performClick()
   compose.onNodeWithText("+ Yeni elan").performClick();marketLoaded("Seç")
   compose.onAllNodesWithText("Seç")[0].performScrollTo().performClick();waitText("Qiymət")
   compose.onAllNodesWithText("10 mərtəbə").onLast().performScrollTo().assertIsDisplayed()
   capture("new-listing-summary-$name")
   compose.onNodeWithText("Elanı dərc et").assertIsNotEnabled()
   compose.onNodeWithContentDescription("Qiymət").performScrollTo().performTextInput("0")
   compose.onNodeWithText("Elanı dərc et").assertIsNotEnabled()
   compose.onNodeWithContentDescription("Qiymət").performTextReplacement("24")
   compose.onNodeWithText("Elanı dərc et").assertIsEnabled().assertIsDisplayed()
   capture("new-listing-$name")
   compose.onNodeWithText("Komissiya (5%)").performScrollTo().assertIsDisplayed()
   compose.onNodeWithText("−2").assertExists()
   compose.onNodeWithText("22").assertExists()
   compose.onNodeWithText("Alacağın").performScrollTo().assertIsDisplayed()
   capture("new-listing-fee-$name")
   repeat(24) {
    if(!compose.onNodeWithText("22").isDisplayed()) {
     compose.onNode(hasScrollAction() and hasAnyDescendant(hasText("Komissiya (5%)")) and hasAnyDescendant(hasText("Qiymət")))
         .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { scroll -> scroll(0f, with(compose.density) { GameTokens.Small.toPx() }) }
    }
   }
   compose.onNodeWithText("22").assertIsDisplayed()
   if(font > 1f) compose.onNode(hasScrollAction() and hasAnyDescendant(hasText("Komissiya (5%)")) and hasAnyDescendant(hasText("Qiymət")))
       .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { scroll -> scroll(0f, with(compose.density) { GameTokens.Target.toPx() }) }
   capture("new-listing-net-$name")
   compose.onNodeWithText("Elanı dərc et").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   compose.onNodeWithText("Ləğv et").performClick()
   go("market");marketLoaded("Bax");compose.onAllNodesWithContentDescription("Geri").assertCountEquals(0);capture("market-$name")
   compose.onAllNodesWithText("Bax")[0].performScrollTo().performClick();waitText("Al · 24")
   compose.onNodeWithText("Al · 24").assertIsDisplayed().performClick();waitText("Alış təsdiqi")
   compose.runOnIdle {
    val window = requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!
    assertEquals(android.view.Gravity.BOTTOM, window.attributes.gravity and android.view.Gravity.VERTICAL_GRAVITY_MASK)
    assertEquals(GameTokens.DimOpacity, window.attributes.dimAmount)
    val width = compose.activity.resources.configuration.screenWidthDp
    val wrapped = width < (GameTokens.Target * 6 + GameTokens.MobileHome.PlayDiameter + GameTokens.Quarter * 2).value
    val dock = (if(wrapped) GameTokens.Target * 2 else GameTokens.MobileHome.NavbarHeight) + GameTokens.Small * 2
    assertEquals(with(compose.density) { dock.roundToPx() }, window.attributes.y)
   }
   compose.onNodeWithText("Qiymət").assertExists()
   if(font == 1f) compose.onNodeWithText("Balans").assertIsDisplayed()
   compose.onNodeWithText("Al", useUnmergedTree = false).assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
   capture("buy-confirm-$name",true)
   if(font > 1f) compose.onNode(hasScrollAction() and hasAnyDescendant(hasText("Qiymət")) and hasAnyDescendant(hasText("Balans")))
       .performTouchInput { swipeUp() }
   compose.onAllNodesWithText(snapshot.coins.toString()).onLast().assertIsDisplayed()
   capture("buy-confirm-balance-$name",true)
   compose.onNodeWithText("Ləğv et").assertIsDisplayed().performClick()
   compose.runOnIdle {assertEquals(0,submissions);failCity=true}
   go("inventory");waitText("10 mərtəbə  ›")
   compose.onAllNodesWithText("✓ Şəhərdə").assertCountEquals(0)
   compose.onAllNodesWithText("Məlumat yenilənmədi.", substring=true).assertCountEquals(0)
   capture("inventory-city-unavailable-$name")
   compose.onAllNodesWithText("10 mərtəbə  ›")[0].performScrollTo().performClick()
   waitText("✓ Sənindir")
   compose.onNodeWithText("Şəhərə aidlik yüklənməyib").performScrollTo().assertIsDisplayed().assertHasNoClickAction()
   compose.onAllNodesWithText("✓ Şəhərdə yerləşdirilib").assertCountEquals(0)
   compose.onAllNodesWithText("Məlumat yenilənmədi.", substring=true).assertCountEquals(0)
   compose.onNodeWithText("Bina görünüşü").assertIsDisplayed().assertIsEnabled()
   compose.onNodeWithText("Satışa çıxar").assertIsDisplayed().assertIsEnabled()
   capture("detail-city-unavailable-$name")
   compose.onNodeWithText("Binanı sil").performScrollTo().assertIsDisplayed().assertIsEnabled()
   compose.runOnIdle {assertEquals(0,submissions)}
   // An unavailable city must not trap the user on the detail route.
   compose.onNodeWithContentDescription("Geri").performClick()
   waitText("10 mərtəbə  ›")
   compose.runOnIdle {cityFailureStatus=403}
   compose.onAllNodesWithText("10 mərtəbə  ›")[0].performScrollTo().performClick()
   waitText("Məlumat yenilənmədi. Bağlantını yoxla və yenidən cəhd et.")
   compose.onAllNodesWithText("✓ Sənindir").assertCountEquals(0)
   compose.onAllNodesWithText("Binanı sil").assertCountEquals(0)
   compose.onAllNodesWithText("Satışa çıxar").assertCountEquals(0)
   compose.onNodeWithContentDescription("Geri").performClick()
   compose.runOnIdle {failDecor=true}
   go("decorations")
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Dekor inventarı yüklənmədi"))
   waitText("Dekor inventarı yüklənmədi")
   compose.onAllNodesWithText("✓ Sahibsən").assertCountEquals(0)
   compose.onNodeWithText("Yenidən cəhd et").performScrollTo().assertIsDisplayed()
  } finally {scope.cancel()}
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileCommerceRegularTest:MobileCommerceProductionHost() {@Test fun routes()=flow(1f,"regular")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileCommerceSmallTest:MobileCommerceProductionHost() {@Test fun routes()=flow(1f,"small")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileCommerceFontTest:MobileCommerceProductionHost() {@Test fun routes()=flow(2f,"font")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileCommerceRegularFontTest:MobileCommerceProductionHost() {@Test fun routes()=flow(2f,"regularfont")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileCommerceInsetsTest:MobileCommerceProductionHost() {@Test fun routes()=flow(2f,"insets",true)}
