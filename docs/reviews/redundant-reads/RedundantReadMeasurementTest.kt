package com.aladdin.wobbrik
import android.app.Application
import appbackend.wobbrik.billing.*
import appbackend.wobbrik.game.*
import com.aladdin.wobbrik.engine.AcceptedAttemptCommands
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
class RedundantReadMeasurementTest {
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
  @Volatile var status=200
  @Volatile var delayMs=40L
  @Volatile var syncRevision=1
  val diskReads=AtomicInteger();val diskWrites=AtomicInteger()
  val pathCounts=ConcurrentHashMap<String,AtomicInteger>()
  val paths=ConcurrentHashMap.newKeySet<String>()
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->
   calls.incrementAndGet();paths.add(path);pathCounts.computeIfAbsent(path){AtomicInteger()}.incrementAndGet()
   if(path.startsWith("market/l-")) detailCalls.incrementAndGet()
   gate?.await();delay(delayMs)
   val body=if(status!=200) """{"error":"forbidden"}""" else when {
    path=="sync"->snapshotBody.replace("\"revision\":1", "\"revision\":$syncRevision")
    path.startsWith("market?")||path.startsWith("market/own?")->if(path.contains("cursor=")) {
     """{"listings":[${if(secondPresent) listing(13) else ""}]}"""
    } else """{"listings":[${(1..12).joinToString(",",transform=::listing)}]${if(pagination) ",\"next_cursor\":\"l-12\"" else ""}}"""
    path.startsWith("market/l-")->path.substringAfter("market/l-").toInt().let {"""{"listing":${listing(it)},"building":${building(it)}}"""}
    else->error(path)
   }
   bytes.addAndGet(body.toByteArray(Charsets.UTF_8).size);GameplayResponse(status,body)
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
   override fun read():ByteArray? {diskReads.incrementAndGet();return rows[key]}
   override fun replace(bytes:ByteArray) {diskWrites.incrementAndGet();rows[key]=bytes.clone()}
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
  fun reset() {calls.set(0);bytes.set(0);detailCalls.set(0);paths.clear();pathCounts.clear();diskReads.set(0);diskWrites.set(0)}
  fun close() {gate?.complete(Unit);scope.cancel()}
 }

 private suspend fun read(f:Fixture, refresh:Boolean=true, local:Boolean=true):MarketPreviewPage {
  val s=f.sessions.current()!!;val r=f.ready()
  return f.configured.views!!.read(AccountView(ReadOnlyAccount(s.uid,r.snapshot,r.catalog!!),r),s,
   f.repository.previewKey(false,MarketFilter()), refresh=refresh,
   onLocal=if(local) ({_:MarketPreviewPage -> Unit}) else null) { MarketRepository(it).previewPage(s,false,MarketFilter()) }
 }
 private suspend fun measured(f:Fixture,name:String,requests:Int,disk:Int,block:suspend ()->Unit) {
  f.reset();val start=System.nanoTime();block()
  println("REDUNDANT $name elapsedMs=${(System.nanoTime()-start)/1_000_000} requests=${f.calls} responseUtf8Bytes=${f.bytes} diskReads=${f.diskReads} diskWrites=${f.diskWrites} repeatedPaths=${f.pathCounts.filterValues{it.get()>1}.mapValues{it.value.get()}.toSortedMap()}")
  assertEquals(requests,f.calls.get());assertEquals(disk,f.diskReads.get())
  val expectedBytes=when(name){"cold-market","warm-market-refresh"->8776;"two-sequential-refreshes","two-concurrent-refreshes"->17552;"capture-sequential-same-path","capture-concurrent-same-path"->1473;"fresh-action-detail"->599;else->0}
  assertEquals(expectedBytes,f.bytes.get())
 }
 @Test fun accountViewsReadMatrix()=runBlocking {
  val f=Fixture();try {
   measured(f,"cold-market",14,1){read(f)}
   measured(f,"warm-market-refresh",14,1){read(f)}
   measured(f,"completed-reuse",0,1){read(f,refresh=false)}
   measured(f,"two-sequential-refreshes",28,2){read(f);read(f)}
   measured(f,"two-concurrent-refreshes",28,2){coroutineScope{listOf(async{read(f)},async{read(f)}).awaitAll()}}
   measured(f,"read-only-double-replay",0,2){
    val s=f.sessions.current()!!;val r=f.ready()
    f.configured.views!!.read(AccountView(ReadOnlyAccount(s.uid,r.snapshot,r.catalog!!),null),s,
     f.repository.previewKey(false,MarketFilter()),onLocal={_:MarketPreviewPage -> Unit}) {MarketRepository(it).previewPage(s,false,MarketFilter())}
   }
   // The action detail deliberately bypasses display-only cached previews.
   measured(f,"fresh-action-detail",1,0){f.repository.detail(f.sessions.current()!!,"l-1")}
  } finally {f.close()}
 }
 @Test fun captureDoesNotCoalesceDuplicatePaths()=runBlocking {
  val f=Fixture();try {
   val s=f.sessions.current()!!;val r=f.ready();val a=ReadOnlyAccount(s.uid,r.snapshot,r.catalog!!)
   measured(f,"capture-sequential-same-path",3,0){f.cache.capture(s,a,"duplicate-sequential",f.reads){reader->
    val repo=MarketRepository(reader);assertEquals(repo.detail(s,"l-1"),repo.detail(s,"l-1"))
   }}
   measured(f,"capture-concurrent-same-path",3,0){f.cache.capture(s,a,"duplicate-parallel",f.reads){reader->
    coroutineScope{val repo=MarketRepository(reader);val values=listOf(async{repo.detail(s,"l-1")},async{repo.detail(s,"l-1")}).awaitAll();assertEquals(values[0],values[1])}
   }}
  } finally {f.close()}
 }
}
