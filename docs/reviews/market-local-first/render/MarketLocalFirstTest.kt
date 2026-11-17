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
class MarketLocalFirstTest {
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
  @Volatile var status=200
  @Volatile var delayMs=40L
  @Volatile var syncRevision=1
  val paths=ConcurrentHashMap.newKeySet<String>()
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->
   calls.incrementAndGet();paths.add(path)
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
  fun close() {gate?.complete(Unit);scope.cancel()}
 }
 private fun mount(f:Fixture,font:Float=1f,own:Boolean=false,ready:State<AuthenticatedRecoveryOwner.State.Ready> = mutableStateOf(f.ready(),referentialEqualityPolicy()),insets:Boolean=false) {
  compose.setContent {val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {MaterialTheme {
    Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
     MobileMenuShell("market",ready.value.snapshot.coins,{}, {}) {
      key(InventoryReadKey(ready.value)) {MarketScreen(f.configured,ready.value,{}, {}, {},initialOwn=own)}
     }
    }
   }}
  }
 }
 private fun text(value:String)=compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty()
 private fun waitText(value:String)=compose.waitUntil(15000) {text(value)}
 private fun settled()=compose.waitUntil(15000) {compose.onAllNodes(hasText("Yenilə") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
 private fun capture(name:String) {
  compose.waitForIdle();compose.runOnUiThread {
   val v=compose.activity.window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
   val dir=File("/private/tmp/wobbrik-market-local-first");dir.mkdirs()
   File(dir,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
 }
 @Test fun routeTiming() {
  val f=Fixture();try {
   f.seed();val start=System.nanoTime();mount(f)
   waitText("350")
   println("MARKET first-card-host-ms=${(System.nanoTime()-start)/1_000_000} requests=${f.calls} responseUtf8Bytes=${f.bytes} details=${f.detailCalls}")
   settled()
   println("MARKET settled-host-ms=${(System.nanoTime()-start)/1_000_000} requests=${f.calls} responseUtf8Bytes=${f.bytes} details=${f.detailCalls}")
  } finally {f.close()}
 }
 private fun cached(font:Float=1f,name:String="cached-360",own:Boolean=false,insets:Boolean=false,longBalance:Boolean=false) {
  val f=Fixture();try {
   if(longBalance) {f.snapshot=f.snapshot.copy(coins=Long.MAX_VALUE);f.owner.onAuthChanged("alice")}
   f.seed(own);f.gate=CompletableDeferred();mount(f,font,own,insets=insets)
   waitText("Keş");waitText("350")
   assertEquals(0,f.bytes.get());assertEquals(0,f.commands.get())
   capture(name)
   f.gate!!.complete(Unit);compose.waitUntil(15000) {!text("Keş")};assertEquals(14,f.calls.get())
  } finally {f.close()}
 }
 @Test fun cachedCardsBeforeNetwork()=cached()
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallCached()=cached(name="cached-320")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont2Cached()=cached(2f,"cached-320-font2")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normalFont2InsetCached()=cached(2f,"cached-390-font2-insets",insets=true)
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun longBalanceCached()=cached(2f,"cached-long-balance",insets=true,longBalance=true)
 @Test fun ownCacheUsesSeparateQuery()=cached(name="cached-own",own=true)
 @Test fun cachedTransientErrorAndDeniedRetry() {
  val f=Fixture();try {
   f.seed();f.status=503;mount(f);waitText("Keş");settled();assertTrue(text("350"))
   assertFalse(text("Bazar yenilənmədi. Bağlantını yoxla və yenidən cəhd et."))
   capture("cached-503")
   f.status=403;compose.onNodeWithText("Yenilə").performClick()
   waitText("Bazar yenilənmədi. Bağlantını yoxla və yenidən cəhd et.")
   assertFalse(text("Keş"));assertFalse(text("350"));capture("denied")
   f.status=200;compose.onNodeWithText("Yenidən cəhd et").performScrollTo().performClick()
   waitText("350");settled();assertFalse(text("Keş"))
  } finally {f.close()}
 }
 @Test fun staleRevisionAndQueryMismatchDoNotPublishLocalCards() {
  val f=Fixture();try {
   // Only another query is cached: it must not appear in the public default query.
   f.seed(true,MarketFilter(minPrice=1),"l-12")
   f.gate=CompletableDeferred();mount(f);compose.waitForIdle()
   compose.waitUntil(15000) {f.calls.get()>0}
   assertFalse(text("Keş"));assertFalse(text("350"));capture("miss-loading")
   f.gate!!.complete(Unit);waitText("350");settled()
   f.rows.replaceAll {_,bytes->bytes.toString(Charsets.UTF_8).replace("\"revision\":1", "\"revision\":2").toByteArray(Charsets.UTF_8)}
   f.gate=CompletableDeferred();f.reset();compose.onNodeWithText("Yenilə").performClick();compose.waitForIdle()
   compose.waitUntil(15000) {f.calls.get()>0}
   assertFalse(text("Keş"));assertFalse(text("350"))
   f.gate!!.complete(Unit);waitText("350");settled()
  } finally {f.close()}
 }
 @Test fun revisionChangeDuringCaptureClearsLocalCards() {
  val f=Fixture();try {
   f.seed();f.syncRevision=2;mount(f)
   waitText("Bazar yenilənmədi. Bağlantını yoxla və yenidən cəhd et.")
   assertFalse(text("350"));assertFalse(text("Keş"))
  } finally {f.close()}
 }
 private fun switchLogin(uid:String) {
  val f=Fixture();try {
   f.seed();f.gate=CompletableDeferred()
   val ready=mutableStateOf(f.ready(),referentialEqualityPolicy());mount(f,ready=ready)
   waitText("Keş")
   val previous=ready.value;val session=f.sessions.current()
   compose.runOnIdle {
    f.owner.onAuthChanged(uid);ready.value=f.ready();f.reset()
    assertNotSame(previous,ready.value);assertNotEquals(session,f.sessions.current())
   }
   compose.waitForIdle();compose.waitUntil(15000) {f.calls.get()>0}
   if(uid=="alice") {waitText("Keş");assertEquals(0,f.bytes.get())}
   else {assertFalse(text("Keş"));assertFalse(text("350"))}
   f.gate!!.complete(Unit);waitText("350");settled();assertFalse(text("Keş"))
  } finally {f.close()}
 }
 @Test fun sameUidReloginCancelsInflightRead()=switchLogin("alice")
 @Test fun accountSwitchDoesNotPublishOldAccountCache()=switchLogin("bob")
 @Test fun cachedBuyRequiresFreshDetailAndDoesNotGrantCommand() {
  val f=Fixture();try {
   f.seed();f.gate=CompletableDeferred();mount(f);waitText("Keş")
   compose.onAllNodesWithText("Bax")[0].performScrollTo().performClick()
   compose.waitForIdle();assertFalse(text("Al · 350"));assertEquals(0,f.commands.get())
   f.gate!!.complete(Unit);waitText("Al · 350")
   assertEquals(1,f.detailCalls.get());assertEquals(0,f.commands.get())
  } finally {f.close()}
 }
 @Test fun publicationTimingAndTraffic()=runBlocking {
  val f=Fixture();try {
   f.seed();val session=f.sessions.current()!!;val r=f.ready()
   val account=ReadOnlyAccount(session.uid,r.snapshot,r.catalog!!)
   var start=System.nanoTime()
   f.repository.previewPage(session,false,MarketFilter())
   println("MARKET before-first-publication-ms=${(System.nanoTime()-start)/1_000_000} requests=${f.calls} responseUtf8Bytes=${f.bytes}")
   f.reset();start=System.nanoTime()
   f.configured.views!!.read(AccountView(account,r),session,f.repository.previewKey(false,MarketFilter()),refresh=true,
    onLocal={println("MARKET after-local-publication-ms=${(System.nanoTime()-start)/1_000_000} requests=${f.calls} responseUtf8Bytes=${f.bytes}");assertEquals(0,f.bytes.get())}) {
    MarketRepository(it).previewPage(session,false,MarketFilter())
   }
   println("MARKET after-fresh-publication-ms=${(System.nanoTime()-start)/1_000_000} requests=${f.calls} responseUtf8Bytes=${f.bytes}")
   assertEquals(14,f.calls.get())
  } finally {f.close()}
 }

 @Test fun freshCursorPageReplacesCachedPriceAndRemovedListing() {
  val f=Fixture();try {
   f.pagination=true;f.seed(cursor="l-12");f.seed();mount(f);waitText("350");settled()
   f.gate=CompletableDeferred();f.secondPrice=700
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Daha çox"))
   compose.onNodeWithText("Daha çox").performClick()
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("600"))
   waitText("600");assertFalse(text("700"))
   f.gate!!.complete(Unit)
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("700"))
   waitText("700");assertFalse(text("600"))
   // Returning from a fresh detail re-reads this same cursor. The old cached page
   // must disappear when the new live page is empty.
   compose.onAllNodesWithText("Bax").onLast().performScrollTo().performClick()
   waitText("Al · 700");f.secondPresent=false
   compose.runOnUiThread {compose.activity.onBackPressedDispatcher.onBackPressed()}
   compose.waitUntil(15000) {!text("700") && !text("Al · 700")}
   settled();assertFalse(text("700"));assertFalse(text("600"))
  } finally {f.close()}
 }

}
