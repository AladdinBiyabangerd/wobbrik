package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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

/** Synthetic HTTP fixtures through production repository, owner, composables and art. No device/server claim. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarketRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/market-visual").apply {mkdirs()}
 private fun capture(name:String) {
  compose.waitForIdle();compose.runOnUiThread {
   val view=compose.activity.window.decorView
   val image=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(Canvas(image));File(out,"$name.png").outputStream().use {image.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
 }
 private fun flow(font:Float,name:String) {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",(1..6).flatMap {i->FinishCosmetics.categories.map {c->CatalogItem("base_${i}_$c",if(c=="wall") "Bənövşəyi ev" else c,c,"free",0,null,null)}},emptyList())
  val snapshot=GameSnapshot("wobbrik-v1",1,1250,1050,null,AccountRecovery("now","first","later",7,0,emptyList(),emptyList(),emptyList(),emptyList(),28))
  fun building(id:Int):String {
   val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_${id}_$it\""}
   return """{"id":"b-$id","revision":1,"floors":12,"residents":48,"base_value":500,"max_price":600,"parts":{$parts},"traded":false,"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0..11).joinToString(","){"0"}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
  }
  fun listing(id:Int)="""{"id":"l-$id","building_id":"b-$id","revision":1,"building_revision":1,"price":350,"fee":18,"status":"active"}"""
  val requests=mutableListOf<String>()
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->
   requests+=path
   GameplayResponse(200,when {
    path.startsWith("market?") || path.startsWith("market/own?")->"""{"listings":[${listing(1)},${listing(2)}]}"""
    path.startsWith("market/l-")->path.substringAfter("l-").toInt().let {"""{"listing":${listing(it)},"building":${building(it)}}"""}
    path.startsWith("buildings/")->building(1)
    path.startsWith("city?")->"""{"rules_version":"wobbrik-v1","revision":1,"population":"0","buildings":[]}"""
    path.startsWith("inventory?")->"""{"buildings":[${building(1)}]}"""
    else->error(path)
   })
  })
  val commands=mutableListOf<AttemptCommand>()
  val backend=object:AttemptBackend {
   override suspend fun sync(session:appbackend.wobbrik.billing.BillingSession)=snapshot
   override suspend fun catalog(session:appbackend.wobbrik.billing.BillingSession)=catalog
   override suspend fun submit(session:appbackend.wobbrik.billing.BillingSession,command:AttemptCommand) {commands+=command}
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
   var navigation by mutableStateOf("")
   var initialBuilding by mutableStateOf<String?>(null)
   compose.setContent {val d=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {MaterialTheme {
    if(navigation == "inventory") InventoryScreen(configured,ready,"b-1",{},{navigation="home"},{},{},{},onMarket={initialBuilding="b-1";navigation="market"})
    else MarketScreen(configured,ready,{navigation="home"},{navigation="inventory"},{navigation="city"},initialBuilding,{initialBuilding=null})
   }}}
   compose.waitUntil(15000) {compose.onAllNodesWithText("Bax").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithContentDescription("Geri").assertIsDisplayed();capture("market-$name")
   val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
   compose.onNodeWithText("İnventar").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) {it(layouts)}
   val navLayout=layouts.single()
   File(out,"nav-$name.txt").writeText("measured=${navLayout.size}; glyphLeft=${navLayout.getLineLeft(0)}; glyphRight=${navLayout.getLineRight(0)}; parentParagraph=${navLayout.multiParagraph.width}x${navLayout.multiParagraph.height}")
   assertTrue("Inventory navigation glyphs must fit",navLayout.getLineRight(0) - navLayout.getLineLeft(0) <= navLayout.size.width + 1)
   compose.onAllNodesWithText("Bax")[0].performScrollTo().performClick()
   compose.waitUntil(15000) {compose.onAllNodesWithText("Al · 350").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithText("Al · 350").assertIsDisplayed();capture("detail-$name")
   compose.onNodeWithText("Al · 350").performClick()
   compose.onNodeWithText("Bina alınsın?").assertIsDisplayed()
   compose.onNodeWithText("Ləğv et").performClick()
   assertTrue(commands.isEmpty())
   compose.runOnUiThread {compose.activity.onBackPressedDispatcher.onBackPressed()}
   compose.onNodeWithText("Elanlarım").performClick()
   compose.onNodeWithText("Bazara çıxar").assertIsDisplayed().performClick()
   compose.waitUntil(15000) {compose.onAllNodesWithText("Seç").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithText("Seç").performScrollTo().performClick()
   compose.onNodeWithText("Elanı dərc et").assertIsDisplayed().assertIsNotEnabled()
   compose.onNodeWithText("Qiymət").performTextInput("350")
   compose.onNodeWithText("Elanı dərc et").assertIsEnabled()
   // Capture dialog separately; activity capture excludes its window.
   compose.runOnUiThread {
    val v=org.robolectric.shadows.ShadowDialog.getLatestDialog().window!!.decorView
    val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
    File(out,"sale-$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
   }
   compose.onNodeWithText("Ləğv et").performClick();assertTrue(commands.isEmpty())
   compose.onNodeWithText("İnventar").performClick();assertEquals("inventory",navigation)
   compose.waitUntil(15000) {compose.onAllNodesWithText("Bazara çıxar").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithText("Bazara çıxar").assertIsDisplayed();capture("inventory-sale-entry-$name")
   compose.onNodeWithText("Bazara çıxar").performClick()
   compose.waitUntil(15000) {compose.onAllNodesWithText("Elanı dərc et").fetchSemanticsNodes().isNotEmpty()}
   assertNull(initialBuilding)
   compose.onNodeWithText("Ləğv et").performClick()
   assertTrue(commands.isEmpty())
   assertTrue(requests.any {it.startsWith("market/own?")})
  } finally {scope.cancel()}
 }
 @Test fun compact360()=flow(1f,"360-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular390()=flow(1f,"390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun font2()=flow(2f,"320-font2")
}
