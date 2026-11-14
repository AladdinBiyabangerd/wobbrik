package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailPartsParityRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/detail-parts-visual")
 private fun capture(name:String) {
  compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(b));File(out,"production-$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
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
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->GameplayResponse(200,when {
   path.startsWith("inventory?")->"""{"buildings":[${(1..4).joinToString(","){json(it)}}]}"""
   path.startsWith("buildings/")->json(path.substringAfter("b-").toInt())
   path.startsWith("city?")->"""{"rules_version":"wobbrik-v1","revision":1,"population":"0","buildings":[]}"""
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
   val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(reads),sessions,DecorationRepository(reads),CitySceneRepository(reads))
   var page by mutableStateOf("inventory");var selected by mutableStateOf<String?>(null)
   compose.setContent {
    val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
     MaterialTheme {key(page) {when(page) {
      "inventory"->InventoryScreen(configured,ready,selected,{selected=it},{page="home"},{page="parts"},{page="decor"},{page="land"})
      "parts"->PartsScreen(configured,ready,null,{page="inventory"})
      "edit"->PartsScreen(configured,ready,"b-1",{page="inventory"})
      else->AccountScreen(page,ready,null,{page=it},{},{},false)
     }}}
    }
   }
   compose.onNodeWithText("Ana ekran").assertIsDisplayed()
   capture("inventory-$name")
   compose.onNodeWithText("Hissələr").performClick()
   compose.onNodeWithText("Əhəngdaşı divar").assertIsDisplayed()
   capture("parts-$name")
   compose.onNodeWithText("Əhəngdaşı divar").performClick()
   capture("part-detail-$name")
   compose.runOnIdle {page="inventory";selected="b-1"}
   compose.onNodeWithText("Bina görünüşü").assertIsDisplayed()
   compose.onNodeWithText("İnventara qayıt").assertIsDisplayed()
   capture("building-detail-$name")
   for (tab in listOf("Dəyər", "Hissələr")) {
    val button = compose.onNode(hasText(tab) and hasClickAction())
    if (font > 1.3f) button.performScrollTo()
    button.performClick()
    capture("building-${if(tab == "Dəyər") "value" else "parts"}-$name")
   }
   val residents = compose.onNode(hasText("Sakin") and hasClickAction())
   if (font > 1.3f) residents.performScrollTo()
   residents.performClick()
   compose.onNodeWithText("Binanı sil").assertIsDisplayed().performClick()
   compose.onNodeWithText("Bina silinsin?").assertIsDisplayed()
   compose.onNodeWithText("Ləğv et").assertIsDisplayed()
   compose.runOnUiThread {
    val v=requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!.decorView
    val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
    v.draw(Canvas(b))
    File(out,"production-delete-$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
   }
   compose.onNodeWithText("Ləğv et").performClick()
   compose.onNodeWithText("Bina silinsin?").assertDoesNotExist()
   compose.runOnIdle {assertEquals(0,submissions);assertEquals("b-1",selected)}
   val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
   compose.onNodeWithText("Hər mərtəbəyə").performScrollTo()
    .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) {it(layouts)}
   val layout=layouts.single()
   for(line in 0 until layout.lineCount-1) {
    val end=layout.getLineEnd(line,visibleEnd=true)
    val text=layout.layoutInput.text.text
    assertFalse("Detail label breaks inside a word",end>0 && end<text.length && text[end-1].isLetter() && text[end].isLetter())
   }
   compose.runOnIdle {page="edit"}
   compose.onNodeWithText("✓ Tətbiq et").assertIsDisplayed()
   capture("parts-edit-$name")
   for(destination in listOf("account","achievements")) {
    compose.runOnIdle {page=destination}
    compose.onNodeWithText("Ana ekran").assertIsDisplayed()
    capture("$destination-$name")
   }
   val screens=listOf("inventory","parts","part-detail","building-detail","parts-edit","account","achievements")
   val board=Bitmap.createBitmap(768,1728,Bitmap.Config.ARGB_8888)
   val canvas=Canvas(board);canvas.drawColor(0xFF061831.toInt())
   val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=0xFFB3C8EC.toInt();textSize=13f}
   screens.forEachIndexed {i,screen->
    val bitmap=BitmapFactory.decodeFile(File(out,"production-$screen-$name.png").absolutePath)
    val x=12+(i%3)*256;val y=12+(i/3)*576
    canvas.drawText(screen,x.toFloat(),(y+16).toFloat(),paint)
    val h=(240f*bitmap.height/bitmap.width).toInt()
    canvas.drawBitmap(bitmap,null,Rect(x,y+28,x+240,y+28+h),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
   }
   File(out,"production-contact-$name.png").outputStream().use {board.compress(Bitmap.CompressFormat.PNG,100,it)}
  } finally {scope.cancel()}
 }
 @Test fun compact360()=flow(1f,"360-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular390()=flow(1f,"390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont2()=flow(2f,"320-font2")
}
