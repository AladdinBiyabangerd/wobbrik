package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.layout
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
class InventoryCompositionProfileTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 internal class Fixture {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val snapshotBody="""{"rules_version":"wobbrik-v1","revision":1,"coins":1250,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
  var snapshot=RecoveryDtos.snapshot(snapshotBody)
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",FinishCosmetics.categories.map {c->CatalogItem("base_1_$c","Bənövşəyi ev",c,"free",0,null,null)},emptyList())
  var style=1
  var inventoryCount=4
  var distinctFloors=false
  fun building(i:Int):String {
   val floors=if(distinctFloors) 10+i else 12
   val parts=FinishCosmetics.categories.joinToString(",") {"\"$it\":\"base_${style}_$it\""}
   return """{"id":"b-$i","revision":1,"floors":$floors,"residents":${floors*4},"base_value":500,"max_price":600,"parts":{$parts},"traded":false,"accepted_geometry":{"geometry":{"version":"city-geometry-v1","centers_x_ticks":[${(0 until floors).joinToString(","){"0"}}]},"attempt_id":"attempt","sequence":1,"operation_id":"op","rules_version":"wobbrik-v1","engine_version":"wobbrik-structural-v1"}}"""
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
    path.startsWith("inventory?")-> { delay(40); """{"buildings":[${(1..inventoryCount).joinToString(","){building(it)}}]}""" }
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
  val diskEntered=java.util.concurrent.CountDownLatch(1)
  val diskRelease=java.util.concurrent.CountDownLatch(1)
  @Volatile var diskBlocked=false
  val cache=ReadOnlyGameCache {key->object:JournalFile {
   override fun read()=rows[key]
   override fun replace(bytes:ByteArray) {
    if(diskBlocked) {diskEntered.countDown();check(diskRelease.await(10,java.util.concurrent.TimeUnit.SECONDS))}
    rows[key]=bytes.clone()
   }
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

 @OptIn(InternalComposeTracingApi::class)
 private class Tracer : CompositionTracer {
  data class Span(val info:String,val start:Long,val end:Long,val thread:String)
  val spans=java.util.Collections.synchronizedList(mutableListOf<Span>())
  private val stack=ThreadLocal.withInitial {java.util.ArrayDeque<Pair<String,Long>>()}
  @Volatile var active=false
  override fun isTraceInProgress()=active
  override fun traceEventStart(key:Int,dirty1:Int,dirty2:Int,info:String) {requireNotNull(stack.get()).push(info to System.nanoTime())}
  override fun traceEventEnd() {val entry=requireNotNull(stack.get()).poll()?:return;spans.add(Span(entry.first,entry.second,System.nanoTime(),Thread.currentThread().name))}
 }
 @OptIn(InternalComposeTracingApi::class)
 @Test fun profileFirstAndWarmComposition() {
  val f=Fixture();val assets=compose.activity.assets;runBlocking(Dispatchers.IO) {ApprovedArt.prepare(assets)}
  val dir=File("/private/tmp/wobbrik-inventory-profile");dir.mkdirs()
  try {
   runBlocking {val session=f.sessions.current()!!;val r=f.ready()
    f.cache.capture(session,ReadOnlyAccount(session.uid,r.snapshot,r.catalog!!),"inventory-",f.reads) {InventoryRepository(it).page(session)}
   }
   f.networkGate=CompletableDeferred()
   val tracer=Tracer()
   val measures=java.util.Collections.synchronizedList(mutableListOf<Tracer.Span>())
   var page by mutableStateOf("home")
   compose.setContent {MaterialTheme {MobileMenuShell(page,1250,{}, {page=it}) {
    if(page=="home") MobileHomeScreen(f.ready().snapshot,false,{}, {page=it},null,{},false)
    else CompositionLocalProvider(LocalBuildingArt provides BuildingArtState(ArtPreparation.Loading){}) {
     Box(Modifier.fillMaxSize().layout { measurable, constraints ->
      val start=System.nanoTime();val placeable=measurable.measure(constraints)
      if(tracer.active) measures.add(Tracer.Span("inventory.measure",start,System.nanoTime(),Thread.currentThread().name))
      layout(placeable.width,placeable.height) {
       val placeStart=System.nanoTime();placeable.placeRelative(0,0)
       if(tracer.active) measures.add(Tracer.Span("inventory.place",placeStart,System.nanoTime(),Thread.currentThread().name))
      }
     }) {InventoryScreen(f.configured,f.ready(),null,{},{},{},{},{})}
    }
   }}}
   compose.onNodeWithText("Öz şəhərini qur").assertExists()
   compose.waitUntil(10000) {compose.onAllNodesWithContentDescription("Wobbrik").fetchSemanticsNodes().isNotEmpty()}
   val profiled=System.getProperty("inventory.profile","true").toBoolean()
   val recording=if(profiled) ClassLoader.getSystemClassLoader().loadClass("jdk.jfr.Recording").getConstructor().newInstance().also { r ->
    for(event in listOf("jdk.ClassLoad","jdk.Compilation","jdk.ExecutionSample","jdk.ThreadPark","jdk.JavaMonitorEnter")) {
     val settings=r.javaClass.getMethod("enable",String::class.java).invoke(r,event)
     val settingClass=ClassLoader.getSystemClassLoader().loadClass("jdk.jfr.EventSettings")
     for((key,value) in mapOf("threshold" to "0 ns","period" to "1 ms","stackTrace" to "true"))
      settingClass.getMethod("with",String::class.java,String::class.java).invoke(settings,key,value)
    }
    r.javaClass.getMethod("start").invoke(r)
   } else null
   Composer.setTracer(tracer)
   val lines=mutableListOf("round,start,end,elapsedMs,wallStartMs,wallEndMs")
   repeat(4) {round ->
    val wallStart=System.currentTimeMillis();val start=System.nanoTime();tracer.active=profiled
    compose.runOnIdle {page="inventory"}
    compose.waitUntil(10000) {compose.onAllNodesWithText("12 mərtəbə  ›").fetchSemanticsNodes().isNotEmpty()}
    val end=System.nanoTime();val wallEnd=System.currentTimeMillis();tracer.active=false
    println("COMPOSITION round=$round profiled=$profiled ms=${(end-start)/1_000_000}")
    lines.add("$round,$start,$end,${(end-start)/1_000_000.0},$wallStart,$wallEnd")
    capture("profile-$round")
    compose.runOnIdle {page="home"};compose.waitForIdle()
   }
   File(dir,"rounds.csv").writeText(lines.joinToString("\n"))
   File(dir,"spans.tsv").writeText("info\tstart\tend\tthread\n"+tracer.spans.joinToString("\n") {"${it.info}\t${it.start}\t${it.end}\t${it.thread}"})
   File(dir,"measure.tsv").writeText("info\tstart\tend\tthread\n"+measures.joinToString("\n") {"${it.info}\t${it.start}\t${it.end}\t${it.thread}"})
   recording?.let {r ->
    r.javaClass.getMethod("stop").invoke(r)
    r.javaClass.getMethod("dump",Class.forName("java.nio.file.Path")).invoke(r,File(dir,"route.jfr").toPath())
    r.javaClass.getMethod("close").invoke(r)
   }
   assertEquals(0,f.commands.get())
  } finally {f.networkGate?.complete(Unit);f.close()}
 }
}
