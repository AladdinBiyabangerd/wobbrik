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
class InventoryDetailLatencyTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private class Fixture {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val snapshotBody="""{"rules_version":"wobbrik-v1","revision":1,"coins":1250,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
  var snapshot=RecoveryDtos.snapshot(snapshotBody)
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",FinishCosmetics.categories.map {c->CatalogItem("base_1_$c","Bənövşəyi ev",c,"free",0,null,null)},emptyList())
  fun building(i:Int):String {
   val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_1_$it\""}
   return """{"id":"b-$i","revision":1,"floors":12,"residents":48,"base_value":500,"max_price":600,"parts":{$parts},"traded":false,"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0..11).joinToString(","){"0"}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
  }
  val calls=AtomicInteger();val bytes=AtomicInteger();val cityCalls=AtomicInteger();val commands=AtomicInteger()
  val paths=ConcurrentHashMap.newKeySet<String>()
  var cityGate=CompletableDeferred<Unit>()
  var buildingGate:CompletableDeferred<Unit>?=null
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->
   calls.incrementAndGet();paths.add(path)
   val body=when {
    path=="sync"->snapshotBody
    path.startsWith("buildings/")-> { buildingGate?.await(); delay(40); building(1) }
    path.startsWith("inventory?")-> { delay(40); """{"buildings":[${building(1)}]}""" }
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

 private fun text(value:String)=compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty()
 private fun exercise(font:Float=1f,name:String="detail-360",insets:Boolean=false,longBalance:Boolean=false) {
  val f=Fixture();val selected=mutableStateOf<String?>(null)
  try {
   if(longBalance) {f.snapshot=f.snapshot.copy(coins=Long.MAX_VALUE);f.owner.onAuthChanged("alice")}
   compose.setContent {val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {MaterialTheme {
     Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
      MobileMenuShell("inventory",f.ready().snapshot.coins,{}, {}) {
       InventoryScreen(f.configured,f.ready(),selected.value,{selected.value=it},{},{},{},{})
      }
     }
    }}
   }
   compose.waitUntil(15000) {text("12 mərtəbə  ›")}
   compose.runOnIdle {selected.value="b-1"}
   compose.waitUntil(15000) {text("Bina məlumatı")}
   assertFalse(f.cityGate.isCompleted)
   assertTrue(text("Şəhərə aidlik yüklənməyib"))
   assertEquals(1,f.cityCalls.get())
   println("INVENTORY detail-visible-with-city-blocked requests=${f.calls} responseUtf8Bytes=${f.bytes}")
   f.cityGate.complete(Unit)
   compose.waitUntil(15000) {text("✓ Şəhərdə yerləşdirilib")}
   compose.waitForIdle();compose.runOnUiThread {
    val v=compose.activity.window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
    val dir=File("/private/tmp/wobbrik-inventory-detail");dir.mkdirs()
    File(dir,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
   }
   compose.onNodeWithText("Binanı sil").performScrollTo().assertIsDisplayed()
   val settledCalls=f.calls.get()
   compose.runOnIdle {selected.value=null}
   compose.waitUntil(15000) {text("12 mərtəbə  ›")}
   compose.runOnIdle {selected.value="b-1"}
   compose.waitUntil(15000) {text("Bina məlumatı")}
   assertEquals(1,f.cityCalls.get());assertEquals(settledCalls,f.calls.get())
   assertEquals(0,f.commands.get())
  } finally {f.close()}
 }

 @Test fun legacyDetailCacheShowsBeforeNetworkAndCannotDelete() {
  val f=Fixture()
  try {
   f.cityGate.complete(Unit)
   runBlocking {
    val session=f.sessions.current()!!;val r=f.ready()
    f.cache.capture(session,ReadOnlyAccount(session.uid,r.snapshot,r.catalog!!),"detail-b-1",f.reads) {
     val repo=InventoryRepository(it);repo.building(session,"b-1") to repo.city(session)
    }
   }
   f.cityGate=CompletableDeferred();f.buildingGate=CompletableDeferred()
   compose.setContent { androidx.compose.material3.MaterialTheme {
    MobileMenuShell("inventory",f.ready().snapshot.coins,{}, {}) {
     InventoryScreen(f.configured,f.ready(),"b-1",{},{},{},{},{})
    }
   }}
   compose.waitUntil(15000) {text("Bina məlumatı")}
   compose.onNodeWithText("Binanı sil").assertIsNotEnabled()
   assertFalse(f.buildingGate!!.isCompleted);assertFalse(f.cityGate.isCompleted)
   assertEquals(0,f.commands.get())
   f.buildingGate!!.complete(Unit)
   compose.waitUntil(15000) {compose.onAllNodes(hasText("Binanı sil") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
  } finally {f.close()}
 }
 @Test fun routeTiming() {
  val f=Fixture();val selected=mutableStateOf<String?>(null)
  try {
   compose.setContent { androidx.compose.material3.MaterialTheme {
    MobileMenuShell("inventory",f.ready().snapshot.coins,{}, {}) {
     InventoryScreen(f.configured,f.ready(),selected.value,{selected.value=it},{},{},{},{})
    }
   }}
   compose.waitUntil(15000) {text("12 mərtəbə  ›")}
   val start=System.nanoTime()
   f.scope.launch { delay(1200); f.cityGate.complete(Unit) }
   compose.runOnIdle {selected.value="b-1"}
   compose.waitUntil(15000) {text("Bina məlumatı")}
   println("INVENTORY detail-host-ms=${(System.nanoTime()-start)/1_000_000} requests=${f.calls} responseUtf8Bytes=${f.bytes} cityRequests=${f.cityCalls}")
   compose.waitUntil(15000) {text("✓ Şəhərdə yerləşdirilib")}
   println("INVENTORY settled requests=${f.calls} responseUtf8Bytes=${f.bytes} cityRequests=${f.cityCalls}")
  } finally {f.close()}
 }
 @Test fun detailDoesNotWaitForCity()=exercise()
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont1()=exercise(name="detail-320")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont2()=exercise(2f,"detail-320-font2")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normalFont2()=exercise(2f,"detail-390-font2",insets=true)
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun longBalance()=exercise(2f,"detail-390-long",insets=true,longBalance=true)
}
