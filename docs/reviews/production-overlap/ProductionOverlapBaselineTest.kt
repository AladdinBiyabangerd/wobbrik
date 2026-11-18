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

/** Real market/cache/owner; synthetic 12-listing transport, not production latency. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProductionOverlapBaselineTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private class Fixture {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val snapshotBody="""{"rules_version":"wobbrik-v1","revision":1,"coins":1250,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
  var snapshot=RecoveryDtos.snapshot(snapshotBody)
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",FinishCosmetics.categories.map {c->CatalogItem("base_1_$c","Bənövşəyi ev",c,"free",0,null,null)},emptyList())
  @Volatile var pagination=false
  @Volatile var secondPrice=600
  @Volatile var secondPresent=true
  fun listing(i:Int)="""{"id":"l-$i","building_id":"b-$i","revision":1,"building_revision":1,"price":${if(i==13) secondPrice else 350},"fee":${if(i==13) MarketMutations.fee(secondPrice.toLong()) else 18},"status":"active"}"""
  fun building(i:Int):String {
   val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_1_$it\""}
   return """{"id":"b-$i","revision":1,"floors":12,"residents":48,"base_value":500,"max_price":600,"parts":{$parts},"traded":false,"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0..11).joinToString(","){"0"}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
  }
  val calls=AtomicInteger();val bytes=AtomicInteger();val detailCalls=AtomicInteger();val commands=AtomicInteger()
  @Volatile var gate:CompletableDeferred<Unit>?=null
  @Volatile var blockingGate:java.util.concurrent.CountDownLatch?=null
  @Volatile var status=200
  @Volatile var delayMs=40L
  @Volatile var syncRevision=1
  val paths=ConcurrentHashMap.newKeySet<String>()
  val active=ConcurrentHashMap<String,AtomicInteger>()
  val overlaps=AtomicInteger();val cancelled=AtomicInteger()
  val pathCounts=ConcurrentHashMap<String,AtomicInteger>()
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->
   calls.incrementAndGet();paths.add(path)
   pathCounts.computeIfAbsent(path){AtomicInteger()}.incrementAndGet()
   if(active.computeIfAbsent(path){AtomicInteger()}.incrementAndGet()>1) overlaps.incrementAndGet()
   try {
   if(path.startsWith("market/l-")) detailCalls.incrementAndGet()
   blockingGate?.let {check(it.await(15,java.util.concurrent.TimeUnit.SECONDS))};gate?.await();delay(delayMs)
   val body=if(status!=200) """{"error":"forbidden"}""" else when {
    path=="sync"->snapshotBody.replace("\"revision\":1", "\"revision\":$syncRevision")
    path.startsWith("market?")||path.startsWith("market/own?")->if(path.contains("cursor=")) {
     """{"listings":[${if(secondPresent) listing(13) else ""}]}"""
    } else """{"listings":[${(1..12).joinToString(",",transform=::listing)}]${if(pagination) ",\"next_cursor\":\"l-12\"" else ""}}"""
    path.startsWith("market/l-")->path.substringAfter("market/l-").toInt().let {"""{"listing":${listing(it)},"building":${building(it)}}"""}
    else->error(path)
   }
   bytes.addAndGet(body.toByteArray(Charsets.UTF_8).size);GameplayResponse(status,body)
   } catch(e:CancellationException) {cancelled.incrementAndGet();throw e}
   finally {active.getValue(path).decrementAndGet()}
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
  fun seed(own:Boolean=false,filter:MarketFilter=MarketFilter(),cursor:String?=null)=runBlocking {
   val s=sessions.current()!!;val r=ready()
   cache.capture(s,ReadOnlyAccount(s.uid,r.snapshot,r.catalog!!),repository.previewKey(own,filter,cursor),reads) {MarketRepository(it).previewPage(s,own,filter,cursor)}
   reset()
  }
  fun reset() {calls.set(0);bytes.set(0);detailCalls.set(0);paths.clear()}
  fun close() {blockingGate?.countDown();gate?.complete(Unit);scope.cancel()}
 }

 private fun mount(f:Fixture,visible:State<Boolean> = mutableStateOf(true),pulse:State<Int> = mutableStateOf(0)) {
  compose.setContent { MaterialTheme {
   // Same screen/key boundary as OnlineGame, without Firebase/bootstrap or gameplay.
   val p=pulse.value
   Box(Modifier.fillMaxSize()) {
    if(visible.value) MobileMenuShell("market",f.ready().snapshot.coins+p,{}, {}) {
     key(InventoryReadKey(f.ready())) {MarketScreen(f.configured,f.ready(),{}, {}, {})}
    }
   }
  }}
 }
 private fun settled()=compose.waitUntil(15000) {compose.onAllNodes(hasText("Yenilə") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
 private fun report(name:String,f:Fixture) {
  println("OVERLAP scenario=$name requests=${f.calls} bytes=${f.bytes} samePathOverlaps=${f.overlaps} cancelled=${f.cancelled} maxMultiplicity=${f.pathCounts.values.maxOfOrNull{it.get()}}")
  assertEquals(0,f.commands.get())
 }
 @Test fun coldRecompositionAndWarmRemount() {
  val f=Fixture();val visible=mutableStateOf(true);val pulse=mutableStateOf(0)
  try {
   f.gate=CompletableDeferred();mount(f,visible,pulse)
   compose.waitUntil(15000){f.calls.get()==1}
   repeat(5){compose.runOnIdle{pulse.value++};compose.waitForIdle()}
   assertEquals(1,f.calls.get());assertEquals(0,f.overlaps.get())
   f.gate!!.complete(Unit);settled();assertEquals(14,f.calls.get());report("cold-plus-5-recompositions",f)
   compose.runOnIdle{visible.value=false};compose.waitForIdle()
   compose.runOnIdle{visible.value=true};compose.waitForIdle();settled()
   assertEquals(28,f.calls.get());assertEquals(17552,f.bytes.get());assertEquals(0,f.overlaps.get())
   report("warm-remount-total",f)
  } finally {f.close()}
 }
 @Test fun tabSwitchCancelsPendingListAndReturningStartsNewRead() {
  val f=Fixture()
  try {
   f.gate=CompletableDeferred();mount(f)
   compose.waitUntil(15000){f.calls.get()==1}
   compose.onNodeWithText("Mənim elanlarım").performClick();compose.waitForIdle()
   compose.waitUntil(15000){f.calls.get()==2}
   compose.onNodeWithText("Alış",useUnmergedTree=true).performClick();compose.waitForIdle()
   compose.waitUntil(15000){f.calls.get()==3}
   f.gate!!.complete(Unit);compose.waitUntil(15000){f.calls.get()==16 && f.bytes.get()==8776 && f.active.values.all{it.get()==0}};settled()
   assertEquals(16,f.calls.get());assertEquals(8776,f.bytes.get());assertEquals(2,f.cancelled.get());assertEquals(0,f.overlaps.get())
   report("public-own-public-pending",f)
  } finally {f.close()}
 }
 @Test fun blockingTransportRouteOverlap() {
  val f=Fixture()
  try {
   f.blockingGate=java.util.concurrent.CountDownLatch(1);mount(f)
   compose.waitUntil(15000){f.calls.get()==1}
   compose.onNodeWithText("Mənim elanlarım").performClick();compose.waitForIdle()
   compose.waitUntil(15000){f.calls.get()==2}
   compose.onNodeWithText("Alış",useUnmergedTree=true).performClick();compose.waitForIdle()
   compose.waitUntil(15000){f.calls.get()==3}
   assertEquals(1,f.overlaps.get())
   f.blockingGate!!.countDown()
   compose.waitUntil(15000){f.calls.get()==16 && f.active.values.all{it.get()==0}};settled()
   assertEquals(1,f.overlaps.get());assertEquals(16,f.calls.get());assertEquals(8776,f.bytes.get())
   report("blocking-public-own-public-pending",f)
  } finally {f.close()}
 }
 @Test fun directAccountViewsOverlapPositiveControl()=runBlocking {
  val f=Fixture()
  try {
   val session=f.sessions.current()!!;val ready=f.ready()
   val view=AccountView(ReadOnlyAccount(session.uid,ready.snapshot,ready.catalog!!),ready)
   suspend fun read()=f.configured.views!!.read(view,session,f.repository.previewKey(false,MarketFilter()),refresh=true,onLocal={_:MarketPreviewPage->}) {
    MarketRepository(it).previewPage(session,false,MarketFilter())
   }
   f.gate=CompletableDeferred()
   coroutineScope {
    val a=async(Dispatchers.Default){read()};val b=async(Dispatchers.Default){read()}
    withTimeout(15000){while(f.calls.get()<2) delay(5)}
    assertEquals(1,f.overlaps.get());f.gate!!.complete(Unit);a.await();b.await()
   }
   assertEquals(28,f.calls.get());assertEquals(17552,f.bytes.get());assertTrue(f.overlaps.get()>=1)
   report("direct-two-refresh-positive-control",f)
  } finally {f.close()}
 }
}
