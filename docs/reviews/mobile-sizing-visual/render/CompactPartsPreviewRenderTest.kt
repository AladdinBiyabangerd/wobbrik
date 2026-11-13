// Adapted from the existing host render fixture; output is scoped to this review.
package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.Modifier
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
@Config(sdk=[34], application=Application::class, qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactPartsPreviewRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val parts=FinishCosmetics.categories.associateWith { "base_3_$it" }
 private val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",
  (1..6).flatMap { id -> FinishCosmetics.categories.map { CatalogItem("base_${id}_$it", "Part $id $it",it,"free",0,null,null) } },emptyList())
 private val snapshot=GameSnapshot("wobbrik-v1",1,43,0,null)
 private fun building(style:Int=3)=OwnedBuilding("building",2,16,64,32,38,
  FinishCosmetics.categories.associateWith { "base_${style}_$it" },false,
  AcceptedCityGeometry(CityFloorGeometry(List(16) { it*4500L }),"attempt",1,"operation","wobbrik-v1","wobbrik-structural-v1"))
 private fun capture(name:String) {
  compose.waitForIdle()
  compose.runOnUiThread {
   val view=compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(Canvas(bitmap))
   val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/mobile-sizing-visual/picker-$name.png")
   file.parentFile!!.mkdirs()
   file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
  }
 }
 @Test fun inventoryAllSixFacadesAndMixedWindows() {
  compose.setContent {
   Column(Modifier.fillMaxSize().background(GameTokens.Background)) {
    for(style in 1..6) Row(Modifier.weight(1f)) {
     BuildingPreview(building(style),Modifier.weight(1f).fillMaxHeight())
     BuildingPreview(building(style).copy(parts=building(style).parts+ ("window" to "base_5_window")),Modifier.weight(1f).fillMaxHeight())
    }
   }
  }
  capture("inventory-six-and-mixed")
 }
 @Test fun all68CategoryPreviews() {
  compose.setContent {
   val context=androidx.compose.ui.platform.LocalContext.current
   val art=rememberCosmeticArt()+rememberGameWindowArt()
   val atlases=rememberGameAtlases()
   val items=remember { context.assets.open("finish/catalog-map.json").bufferedReader().use { org.json.JSONObject(it.readText()).getJSONArray("items") } }
   assertEquals(68,items.length())
   Canvas(Modifier.fillMaxSize().background(GameTokens.Background)) {
    val cellWidth=size.width/6
    val cellHeight=size.height/12
    for(i in 0 until items.length()) {
     val item=items.getJSONObject(i)
     val category=item.getString("category")
     val id=item.getString("id")
     assertTrue(art.containsKey(id))
     inset((i%6)*cellWidth,(i/6)*cellHeight,
      size.width-(i%6+1)*cellWidth,size.height-(i/6+1)*cellHeight) {
       partPreview(atlases,art,parts+(category to id),category)
     }
    }
   }
  }
  capture("all-68-category-previews")
 }
 @Test fun defaultPickerAndSelection() {
  var selection=parts
  compose.setContent {
   var selected by remember { mutableStateOf(selection) }
   FinishCosmeticScreen(AcceptedEngineHost.Snapshot(finishParts=selected),
    AuthenticatedRecoveryOwner.State.Ready(snapshot,catalog,"alice"),
    { category,id -> selected=selected+(category to id); selection=selected },{}, {})
  }
  compose.onNodeWithText("Standart görünüş").assertIsDisplayed()
  capture("picker-purple")
  compose.onNodeWithContentDescription("Part 1 wall").performClick()
  assertEquals("base_1_wall",selection["wall"])
  capture("picker-mixed")
 }
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun pickerSmallFont2() {
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,2f)) {
    FinishCosmeticScreen(AcceptedEngineHost.Snapshot(finishParts=parts),
     AuthenticatedRecoveryOwner.State.Ready(snapshot,catalog,"alice"),{_,_->},{},{})
   }
  }
  compose.onNodeWithText("✓ Təsdiqlə").assertIsDisplayed()
  compose.onNodeWithText("← Geri").assertIsDisplayed()
  val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
  compose.onNodeWithText("← Geri",useUnmergedTree=true).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
  capture("picker-320-font2")
  assertTrue("Back text: "+layouts.map { "lines=${it.lineCount} size=${it.size} overflow=${it.hasVisualOverflow}" },layouts.isNotEmpty() && layouts.all { it.lineCount == 1 && it.getLineLeft(0) >= -1 && it.getLineRight(0) <= it.size.width + 1 && it.getLineBottom(0) <= it.size.height + 1 })
 }
 @Test fun returningToEditedDetailReadsServerAndBackClearsSelection() {
  val sessions=AccountSessions { "token" }
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val calls=mutableListOf<String>()
  val http=GameplayHttpBackend(sessions,GameplayTransport { path,_,_ ->
   calls+=path
   GameplayResponse(200,when {
    path=="buildings/building" -> """{"id":"building","revision":3,"floors":10,"residents":40,"base_value":24,"max_price":28,"parts":{"wall":"base_3_wall","window":"base_3_window","entrance":"base_3_entrance","roof":"base_3_roof","lighting":"base_3_lighting"},"traded":false}"""
    path.startsWith("city?") -> """{"rules_version":"wobbrik-v1","revision":1,"population":"0","buildings":[]}"""
    path.startsWith("inventory?") -> """{"buildings":[]}"""
    else -> error(path)
   })
  })
  val backend=object:AttemptBackend {
   override suspend fun sync(session:appbackend.wobbrik.billing.BillingSession)=snapshot
   override suspend fun catalog(session:appbackend.wobbrik.billing.BillingSession)=catalog
   override suspend fun submit(session:appbackend.wobbrik.billing.BillingSession,command:AttemptCommand)=Unit
  }
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=Unit
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }
  try {
   val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,scope)
   owner.onAuthChanged("alice")
   val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
   val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(http),sessions,DecorationRepository(http),CitySceneRepository(http))
   compose.setContent {
    var selected by remember { mutableStateOf<String?>("building") }
    InventoryScreen(configured,ready,selected,{selected=it},{},{},{},{})
   }
   compose.onNodeWithText("Bina detalları").assertIsDisplayed()
   compose.onNodeWithText("Bina görünüşü").assertIsDisplayed()
   assertEquals(listOf("buildings/building","city?limit=20"),calls)
   capture("returned-detail")
   compose.onNodeWithText("İnventara qayıt").performClick()
   compose.onNodeWithText("İnventar").assertIsDisplayed()
   assertTrue(calls.last().startsWith("inventory?"))
  } finally {scope.cancel()}
 }
}
