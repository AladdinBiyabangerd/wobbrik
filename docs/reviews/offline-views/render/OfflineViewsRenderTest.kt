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
import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import com.aladdin.wobbrik.engine.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.IOException

/** Synthetic accepted wire fixtures; actual production cache/repositories/scene/UI, no emulator/network. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OfflineViewsRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/offline-views")
 private fun capture(name:String) {
  compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(b));File(out,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
 }
 private fun flow(font:Float,name:String) = runBlocking {
  val files=mutableMapOf<String,ByteArray>()
  val factory:(String)->JournalFile={ key -> object:JournalFile {
   override fun read()=files[key]
   override fun replace(bytes:ByteArray) {files[key]=bytes.clone()}
  }}
  val cache=ReadOnlyGameCache(factory)
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val catalog=RecoveryDtos.catalog("""{"rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1","items":[${FinishCosmetics.categories.joinToString(",") {"""{"id":"base_1_$it","name":"Standart","category":"$it","method":"free","weight":0}"""}}],"land_offers":[]}""")
  val snapshotBody="""{"rules_version":"wobbrik-v1","revision":1,"coins":1250,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
  val account=ReadOnlyAccount("alice",RecoveryDtos.snapshot(snapshotBody),catalog)
  cache.accepted(account)
  val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_1_$it\""}
  val building="""{"id":"b-1","revision":1,"floors":10,"residents":40,"base_value":20,"max_price":24,"parts":{$parts},"traded":false,"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0..9).joinToString(","){(it*2500).toString()}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
  val city="""{"rules_version":"wobbrik-v1","revision":1,"population":"40","geometry_version":"city-geometry-v1","placement_enabled":true,"parcels":[{"kind":"land","id":"base","position":{"x_ticks":"0","y_ticks":"0","quarter_turns":0}}],"buildings":[{"building_id":"b-1","building_revision":1,"position":{"x_ticks":"55000","y_ticks":"55000","quarter_turns":0}}]}"""
  var online=true;var requests=0;var posts=0
  val backend=GameplayHttpBackend(sessions,GameplayTransport {path,_,body->
   requests++;if(body!=null) posts++
   if(!online) throw IOException("offline")
   GameplayResponse(200,when {
    path=="sync"->snapshotBody
    path.startsWith("inventory?")->"""{"buildings":[$building]}"""
    path=="buildings/b-1"->building
    path.startsWith("city?")->city
    path.startsWith("decorations?")->"""{"copies":[]}"""
    else->error(path)
   })
  })
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=error("No mutations")
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }
  val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,scope,readCache=cache)
  online=false;owner.onAuthChanged("alice")
  val session=sessions.current()!!
  val accepted=owner.readOnly.value!!
  online=true
  cache.capture(session,accepted,"inventory-",backend){InventoryRepository(it).page(session)}
  cache.capture(session,accepted,"detail-b-1",backend){InventoryRepository(it).building(session,"b-1") to InventoryRepository(it).city(session)}
  cache.capture(session,accepted,"city",backend){CitySceneRepository(it).load(session)}
  online=false;val before=requests
  val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(backend),sessions,DecorationRepository(backend),CitySceneRepository(backend),AccountViews(backend,cache))
  val view=AccountView(accepted,null)
  val local=LocalGameHost(PendingGameResults(factory),id={"local-game"})
  local.bind(session);local.foreground(true);assertTrue(local.start(session,accepted))
  val app=WobbrikApplication()
  org.robolectric.util.ReflectionHelpers.callInstanceMethod<Void>(app,"attach",
   org.robolectric.util.ReflectionHelpers.ClassParameter.from(android.content.Context::class.java,compose.activity.applicationContext))
  fun inject(field:String,value:Any) {WobbrikApplication::class.java.getDeclaredField(field).apply {isAccessible=true}.set(app,value)}
  inject("bootstrap",configured)
  inject("localHost\$delegate",lazyOf(local))
  inject("engineHost\$delegate",lazyOf(AcceptedEngineHost()))
  @Suppress("UNCHECKED_CAST")
  val accountState=WobbrikApplication::class.java.getDeclaredField("mutableAccount").apply {isAccessible=true}.get(app) as kotlinx.coroutines.flow.MutableStateFlow<WobbrikApplication.AccountState>
  accountState.value=WobbrikApplication.AccountState.Session("alice")
  var page by mutableStateOf("inventory");var selected by mutableStateOf<String?>(null)
  var localState by mutableStateOf(local.snapshot())
  try {
   compose.setContent {
    val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
     MaterialTheme {key(page) {when(page) {
      "inventory"->InventoryScreen(configured,view,selected,{selected=it},{page="home"},{error("parts")},{error("decor")},{error("land")})
      "city"->CityScreen(configured,view,{page="home"},{page="inventory"},lifeTimeSeconds=0.0)
      "game"->GameScene(localState,local,{local.drop();localState=local.snapshot()},{local.pause();localState=local.snapshot()}, {local.resume();localState=local.snapshot()},{page="home"})
      else->OnlineGame(app)
     }}}
    }
   }
   compose.onNodeWithContentDescription("Saxlanmış məlumat · yalnız baxış").assertIsDisplayed()
   compose.onNodeWithText("Hissələr").assertIsNotEnabled()
   capture("inventory-$name")
   compose.runOnIdle {selected="b-1"}
   compose.onNodeWithText("Binanı sil").assertIsNotEnabled()
   compose.onNodeWithText("Yalnız baxış").assertIsNotEnabled()
   capture("detail-$name")
   compose.runOnIdle {page="city"}
   compose.onNodeWithContentDescription("Saxlanmış məlumat · yalnız baxış").assertIsDisplayed()
   compose.onNodeWithContentDescription("Bina b-1:",substring=true).assertExists()
   capture("city-$name")
   compose.runOnIdle {page="game"}
   compose.onNodeWithText("Davam et").assertIsDisplayed().performClick()
   compose.onNodeWithText("Buraxmaq üçün toxun").assertIsDisplayed()
   capture("game-$name")
   compose.runOnIdle {local.leave()}
   compose.mainClock.autoAdvance=false
   compose.runOnIdle {page="entry"}
   compose.mainClock.advanceTimeBy(100)
   compose.waitForIdle()
   compose.mainClock.advanceTimeByFrame()
   capture("entry-$name")
   compose.onNodeWithContentDescription("Oyunu başlat").assertIsEnabled().performClick()
   compose.mainClock.advanceTimeBy(100)
   compose.waitForIdle()
   compose.mainClock.advanceTimeByFrame()
   compose.onNodeWithText("Davam et").assertIsDisplayed().performClick()
   compose.mainClock.advanceTimeBy(100)
   compose.waitForIdle()
   compose.mainClock.advanceTimeByFrame()
   compose.onNodeWithText("Buraxmaq üçün toxun").assertIsDisplayed()
   assertTrue(local.snapshot().playing)
   assertEquals(before,requests);assertEquals(0,posts)
  } finally {scope.cancel()}
 }
 @Test fun compact() {flow(1f,"360-font1")}
 @Test fun compactLargeFont() {flow(2f,"360-font2")}
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular() {flow(1f,"390-font1")}
}
