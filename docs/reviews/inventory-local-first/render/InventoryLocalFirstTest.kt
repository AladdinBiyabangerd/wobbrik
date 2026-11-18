package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.billing.*
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/** Production InventoryScreen/cache/owner; synthetic transport, not device latency. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class InventoryLocalFirstTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private class Fixture {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val snapshotBody="""{"rules_version":"wobbrik-v1","revision":1,"coins":1250,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
  var snapshot=RecoveryDtos.snapshot(snapshotBody)
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",FinishCosmetics.categories.map {c->CatalogItem("base_1_$c","Bənövşəyi ev",c,"free",0,null,null)},emptyList())
  var style=1
  fun building(i:Int):String {
   val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_${style}_$it\""}
   return """{"id":"b-$i","revision":1,"floors":12,"residents":48,"base_value":500,"max_price":600,"parts":{$parts},"traded":false,"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0..11).joinToString(","){"0"}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
  }
  val calls=AtomicInteger();val bytes=AtomicInteger();val cityCalls=AtomicInteger();val commands=AtomicInteger()
  val paths=ConcurrentHashMap.newKeySet<String>()
  var cityGate=CompletableDeferred<Unit>()
  var buildingGate:CompletableDeferred<Unit>?=null
  var networkGate:CompletableDeferred<Unit>?=null
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->
   calls.incrementAndGet();paths.add(path);networkGate?.await()
   val body=when {
    path=="sync"->snapshotBody
    path.startsWith("buildings/")-> { buildingGate?.await(); delay(40); building(1) }
    path.startsWith("inventory?")-> { delay(40); """{"buildings":[${(1..4).joinToString(","){building(it)}}]}""" }
    path.startsWith("city?")-> { cityCalls.incrementAndGet(); cityGate.await(); """{"rules_version":"wobbrik-v1","revision":1,"population":"48","buildings":[{"building_id":"b-1","building_revision":1}]}""" }
    else->error(path)
   }
   bytes.addAndGet(body.toByteArray(Charsets.UTF_8).size);GameplayResponse(200,body)
  })
  val owner=AuthenticatedRecoveryOwner(sessions,object:AttemptBackend {
   override suspend fun sync(session:BillingSession)=snapshot
   override suspend fun catalog(session:BillingSession)=catalog
   override suspend fun submit(session:BillingSession,command:AttemptCommand) {commands.incrementAndGet()}
  },object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=Unit
   override fun remove(uid:String,command:AttemptCommand)=Unit
  },scope)
  val rows=ConcurrentHashMap<String,ByteArray>()
  val cache=ReadOnlyGameCache {key->object:JournalFile {
   override fun read()=rows[key]
   override fun replace(bytes:ByteArray) {rows[key]=bytes.clone()}
  }}
  val repository=MarketRepository(reads)
  val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(reads),sessions,DecorationRepository(reads),CitySceneRepository(reads),AccountViews(reads,cache,sessions),market=repository)
  init {owner.onAuthChanged("alice")}
  fun ready()=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
  fun close() {cityGate.complete(Unit);buildingGate?.complete(Unit);scope.cancel()}

 }



 private fun capture(name:String) {
  compose.waitForIdle();compose.runOnUiThread {
   val v=compose.activity.window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
   val dir=File("/private/tmp/wobbrik-inventory-local-first");dir.mkdirs()
   File(dir,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
  }
 }
 private fun cachedRoute(font:Float=1f,name:String="360",insets:Boolean=false,style:Int=1) {
  val f=Fixture().apply {this.style=style};val assets=compose.activity.assets
  runBlocking(Dispatchers.IO) {ApprovedArt.prepare(assets)}
  try {
   runBlocking {
    val session=f.sessions.current()!!;val r=f.ready()
    f.cache.capture(session,ReadOnlyAccount(session.uid,r.snapshot,r.catalog!!),"inventory-",f.reads) {InventoryRepository(it).page(session)}
   }
   f.networkGate=CompletableDeferred()
   var selected by mutableStateOf<String?>(null)
   var page by mutableStateOf("home")
   var art by mutableStateOf(ArtPreparation.Loading)
   compose.setContent {val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {MaterialTheme {
    Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
    MobileMenuShell(page,1250,{}, {page=it}) {
     if(page=="home") androidx.compose.material3.Text("Başlanğıc")
     else if(needsBundledArt(page) && art!=ArtPreparation.Ready) ApprovedArtStatus(art){}
     else CompositionLocalProvider(LocalBuildingArt provides BuildingArtState(art){}) {
      InventoryScreen(f.configured,f.ready(),selected,{selected=it},{},{},{},{})
     }
    }
   }}}}
   compose.onNodeWithText("Başlanğıc").assertExists()
   val before=f.calls.get();val start=System.nanoTime()

   compose.runOnIdle {page="inventory"}
   compose.waitUntil(10000) {compose.onAllNodesWithText("12 mərtəbə  ›").fetchSemanticsNodes().isNotEmpty()}
   assertEquals(ArtPreparation.Loading,art)
   println("LOCAL-FIRST warmRouteCachedMs=${(System.nanoTime()-start)/1_000_000} art=$art blockedNetwork=${!f.networkGate!!.isCompleted} additionalRequests=${f.calls.get()-before}")
   assertFalse(f.networkGate!!.isCompleted)
   capture("$name-cached-art-loading")
   compose.runOnIdle {art=ArtPreparation.Failed}
   compose.onAllNodesWithText("Yenidən cəhd et").onFirst().assertExists()
   compose.onAllNodesWithText("12 mərtəbə  ›").onFirst().assertExists()
   compose.runOnIdle {art=ArtPreparation.Ready}
   compose.waitUntil(10000) {compose.onAllNodesWithContentDescription("12 mərtəbəli bina").fetchSemanticsNodes().isNotEmpty()}
   capture("$name-cached-art-ready")
   f.cityGate.complete(Unit);f.networkGate!!.complete(Unit)
   compose.waitUntil(10000) {f.calls.get()>=before+4}
   compose.waitForIdle()
   compose.onAllNodesWithText("12 mərtəbə  ›").onFirst().assertExists()
   capture("$name-fresh-art-ready")
   repeat(2) {mount ->
    compose.runOnIdle {page="home"};compose.waitForIdle()
    val calls=f.calls.get();val nextStart=System.nanoTime()
    compose.runOnIdle {page="inventory"}
    compose.waitUntil(10000) {compose.onAllNodesWithText("12 mərtəbə  ›").fetchSemanticsNodes().isNotEmpty()}
    println("LOCAL-FIRST remount=$mount hostMs=${(System.nanoTime()-nextStart)/1_000_000} addedHTTP=${f.calls.get()-calls}")
    assertEquals(calls,f.calls.get())
   }
   assertEquals(0,f.commands.get())
  } finally {f.networkGate?.complete(Unit);f.close()}
 }

 @Test fun cachedRouteWithArtAndNetworkBlocked()=cachedRoute()
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont1()=cachedRoute(name="320")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont2()=cachedRoute(2f,"320-font2")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normalFont2()=cachedRoute(2f,"390-font2",true)
 @Test fun otherTemplateUsesSameAsyncPreview()=cachedRoute(name="360-teal",style=2)
 @Test fun cachedDetailDoesNotGrantCommandsWhileArtLoads() {
  val f=Fixture();val assets=compose.activity.assets
  runBlocking(Dispatchers.IO) {ApprovedArt.prepare(assets)}
  try {
   runBlocking {
    val session=f.sessions.current()!!;val r=f.ready()
    f.cache.capture(session,ReadOnlyAccount(session.uid,r.snapshot,r.catalog!!),"detail-b-1",f.reads) {InventoryRepository(it).building(session,"b-1")}
   }
   f.networkGate=CompletableDeferred()
   compose.setContent {MaterialTheme {
    MobileMenuShell("inventory",1250,{}, {}) {
     CompositionLocalProvider(LocalBuildingArt provides BuildingArtState(ArtPreparation.Loading){}) {
      InventoryScreen(f.configured,f.ready(),"b-1",{},{},{},{},{})
     }
    }
   }}
   compose.waitUntil(10000) {compose.onAllNodesWithText("Bina məlumatı").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithText("Binanı sil").assertIsNotEnabled()
   compose.onNodeWithText("Yalnız baxış").assertIsNotEnabled()
   assertFalse(f.networkGate!!.isCompleted)
   f.cityGate.complete(Unit);f.networkGate!!.complete(Unit)
   compose.waitUntil(10000) {compose.onAllNodes(hasText("Binanı sil") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
   assertEquals(0,f.commands.get())
  } finally {f.networkGate?.complete(Unit);f.close()}
 }
 @Test fun cachedAccountBypassesInitialGateButNewLoginDoesNot() {
  var epoch by mutableStateOf(BillingSession("alice","1"))
  var cached by mutableStateOf(true)
  compose.setContent {MaterialTheme {
   if(!InitialAccountSync(epoch,AuthenticatedRecoveryOwner.State.Recovering,null,cached,{})) androidx.compose.material3.Text("Menyu hazırdır")
  }}
  compose.onNodeWithText("Menyu hazırdır").assertExists()
  compose.runOnIdle {epoch=BillingSession("bob","2");cached=false}
  compose.onNodeWithText("Menyu hazırdır").assertDoesNotExist()
  compose.onNodeWithText("Yüklənir…").assertExists()
 }
}
