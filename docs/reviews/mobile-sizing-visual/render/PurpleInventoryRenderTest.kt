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
class PurpleInventoryRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File(requireNotNull(System.getProperty("wobbrik.render.output", "/Users/mac/My Workspace/Wobbrik/docs/reviews/purple-inventory-visual"))).apply { mkdirs() }
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
   val category = mapOf(2 to "window",3 to "roof",4 to "entrance",5 to "lighting",6 to "wall")[id]
   val parts=FinishCosmetics.categories.joinToString(",") {
    val style=if(id==9 || it==category || (id==7 && it!="wall")) 2 else 1
    "\"$it\":\"base_${style}_$it\""
   }
   val floors=if(id==8) 40 else 10
   return """{"id":"b-$id","revision":1,"floors":$floors,"residents":${floors*4},"base_value":${floors*2},"max_price":${floors*2+4},"parts":{$parts},"traded":false,"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0 until floors).joinToString(","){(it*2500).toString()}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
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
   for(id in 1..9) {
    compose.runOnIdle {selected="b-$id"}
    compose.onNodeWithText("İnventara qayıt").assertIsDisplayed()
    compose.onNodeWithContentDescription("${if(id==8) 40 else 10} mərtəbəli bina").assertExists()
    capture("detail-$id-$name")
   }
   compose.runOnIdle {assertEquals(0,submissions)}
  } finally {scope.cancel()}
 }
 @Test fun compact360()=flow(1f,"360-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular390()=flow(1f,"390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont2()=flow(2f,"320-font2")
}
