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
import java.util.concurrent.atomic.AtomicInteger

/** Real CityScreen/AccountViews/cache/repository; synthetic transport and empty city. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CityCollectionReadsTest {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 private class Fixture {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions { "token" }
  val snapshotBody="""{"rules_version":"wobbrik-v1","revision":1,"coins":12,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
  val snapshot=RecoveryDtos.snapshot(snapshotBody)
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",emptyList(),emptyList())
  val owner=AuthenticatedRecoveryOwner(sessions,object:AttemptBackend {
   override suspend fun sync(session:BillingSession)=snapshot
   override suspend fun catalog(session:BillingSession)=catalog
   override suspend fun submit(session:BillingSession,command:AttemptCommand)=Unit
  },object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=Unit
   override fun remove(uid:String,command:AttemptCommand)=Unit
  },scope)
  val city="""{"rules_version":"wobbrik-v1","geometry_version":"city-geometry-v1","placement_enabled":true,"revision":1,"population":"0","parcels":[{"kind":"land","id":"base","position":{"x_ticks":"0","y_ticks":"0","quarter_turns":0}}],"buildings":[]}"""
  val calls=AtomicInteger(); val bytes=AtomicInteger()
  @Volatile var gate:CompletableDeferred<Unit>?=null
  val entered=CompletableDeferred<Unit>()
  @Volatile var status=200
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->
   calls.incrementAndGet()
   gate?.let { entered.complete(Unit); it.await() }
   val body=if(status!=200) """{"error":"forbidden"}""" else when {
    path=="sync"->snapshotBody
    path.startsWith("city?")->city
    path.startsWith("decorations?")->"""{"copies":[]}"""
    else->error(path)
   }
   bytes.addAndGet(body.toByteArray(Charsets.UTF_8).size)
   GameplayResponse(status,body)
  })
  val rows=mutableMapOf<String,ByteArray>()
  val cache=ReadOnlyGameCache {key->object:JournalFile {
   override fun read()=rows[key]
   override fun replace(bytes:ByteArray) {rows[key]=bytes.clone()}
  } }
  val views=AccountViews(reads,cache,sessions)
  val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(reads),sessions,DecorationRepository(reads),CitySceneRepository(reads),views)
  init {owner.onAuthChanged("alice")}
  fun view():AccountView {
   val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
   return AccountView(ReadOnlyAccount(ready.accountKey!!,ready.snapshot,ready.catalog!!),ready)
  }
  fun seed()=runBlocking {val v=view(); val s=sessions.current()!!;cache.capture(s,v.account,"city",reads) {CitySceneRepository(it).load(s)} }
  fun close() {gate?.complete(Unit);scope.cancel()}
 }
 private fun mount(f:Fixture,font:Float=1f,view:State<AccountView> = mutableStateOf(f.view())) {
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font),LocalMobileMenuShell provides true) {
    MaterialTheme {key(InventoryReadKey(view.value.ready ?: view.value.account)) {
     MobileMenuShell("city",12,{}, {}) { CityScreen(f.configured,view.value,{}, {},lifeTimeSeconds=0.0) }
    } }
   }
  }
 }
 private fun waitFresh() { compose.waitUntil(15000) { text("Şəhərdə bina yoxdur") && !text("Keş · yalnız baxış") } }
 private fun text(value:String)=compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty()
 private fun waitText(value:String) {compose.waitUntil(15000) {text(value)}}
 private fun capture(name:String) = Unit
 private fun hit(font:Float=1f,name:String="hit-360-font1") {
  val f=Fixture()
  try {
   f.seed();f.calls.set(0);f.bytes.set(0);f.gate=CompletableDeferred()
   val start=System.nanoTime();mount(f,font)
   waitText("Keş · yalnız baxış")
   assertFalse(text("Şəhər yüklənmədi"));assertEquals(0,f.bytes.get())
   println("CITY-LOCAL cache-visible-host-ms=${(System.nanoTime()-start)/1_000_000} requests-started=${f.calls} completed-response-bytes=${f.bytes}")
   capture(name)
   f.gate!!.complete(Unit);waitFresh()
   assertFalse(text("Keş · yalnız baxış"));assertEquals(4,f.calls.get())
  } finally {f.close()}
 }
 @Test fun cachedSceneBeforeNetworkAndFreshMarkerClears()=hit()
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallCached()=hit(1f,"hit-320-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallLargeFontCached()=hit(2f,"hit-320-font2")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normalLargeFontCached()=hit(2f,"hit-390-font2")
 @Test fun missWaitsForLiveScene() {
  val f=Fixture();try {
   f.gate=CompletableDeferred();mount(f)
   compose.waitUntil(15000) {f.entered.isCompleted}
   assertFalse(text("Keş · yalnız baxış"));assertFalse(text("Şəhərdə bina yoxdur"))
   capture("miss-loading")
   f.gate!!.complete(Unit);waitFresh();assertEquals(4,f.calls.get())
  } finally {f.close()}
 }
 @Test fun cachedTransientErrorRetainsCacheMarker() {
  val f=Fixture();try {
   f.seed();f.status=503;mount(f);waitText("Keş · yalnız baxış")
   compose.waitUntil(15000) {f.calls.get()==5};compose.waitForIdle()
   assertFalse(text("Şəhər yüklənmədi"));capture("cached-503")
  } finally {f.close()}
 }
 @Test fun deniedRefreshClearsCachedSceneAndRetryWorks() {
  val f=Fixture();try {
   f.seed();f.status=403;mount(f);waitText("Şəhər yüklənmədi")
   assertFalse(text("Keş · yalnız baxış"));capture("denied")
   f.status=200;compose.onNodeWithContentDescription("Yenilə").performClick();waitFresh()
  } finally {f.close()}
 }
 @Test fun missAndNetworkFailureShowsError() {
  val f=Fixture();try {
   f.status=503;mount(f);waitText("Şəhər yüklənmədi");assertFalse(text("Keş · yalnız baxış"))
  } finally {f.close()}
 }
 @Test fun staleRevisionIsNotPresentedWhileRefreshing() {
  val f=Fixture();try {
   f.seed()
   f.rows.replaceAll {_,bytes->bytes.toString(Charsets.UTF_8).replace("\"revision\":1", "\"revision\":2").toByteArray(Charsets.UTF_8)}
   f.gate=CompletableDeferred();mount(f)
   compose.waitUntil(15000) {f.entered.isCompleted}
   assertFalse(text("Keş · yalnız baxış"));assertFalse(text("Şəhərdə bina yoxdur"))
   f.gate!!.complete(Unit);waitFresh()
  } finally {f.close()}
 }
 @Test fun newLoginSameUidKeepsCacheDisplayOnlyUntilNewRead() {
  val f=Fixture();try {
   val view=mutableStateOf(f.view(),referentialEqualityPolicy());mount(f,view=view)
   waitFresh();assertEquals(4,f.calls.get())
   val old=view.value;val session=f.sessions.current()
   compose.runOnIdle {
    f.gate=CompletableDeferred();f.calls.set(0);f.bytes.set(0)
    f.owner.onAuthChanged("alice")
    val next=f.view()
    assertEquals(old,next);assertNotSame(old.ready,next.ready)
    assertNotEquals(session,f.sessions.current())
    view.value=next
   }
   // Drain the state write/recomposition before polling a plain atomic counter.
   // Unlike semantics queries, that counter does not synchronize the Compose host.
   compose.waitForIdle()
   compose.waitUntil(15000) {f.calls.get()>0}
   waitText("Keş · yalnız baxış");assertEquals(0,f.bytes.get())
   f.gate!!.complete(Unit);waitFresh()
   assertFalse(text("Keş · yalnız baxış"));assertEquals(4,f.calls.get())
  } finally {f.close()}
 }
 @Test fun accountSwitchDiscardsOldLocalAndInflightScene() {
  val f=Fixture();try {
   f.seed();f.gate=CompletableDeferred();val view=mutableStateOf(f.view(),neverEqualPolicy());mount(f,view=view)
   waitText("Keş · yalnız baxış")
   compose.runOnIdle {f.owner.onAuthChanged("bob");view.value=f.view()}
   compose.waitUntil(15000) {!text("Keş · yalnız baxış")}
   assertFalse(text("Şəhərdə bina yoxdur"))
   f.gate!!.complete(Unit);waitFresh()
   assertFalse(text("Keş · yalnız baxış"))
  } finally {f.close()}
 }
 @Test fun coldWarmOfflineAndRefreshRequestBudget() = runBlocking {
  val f=Fixture()
  try {
   val v=f.view();val session=f.sessions.current()!!
   suspend fun read(view:AccountView=v,refresh:Boolean=false)=f.views.read(view,session,"city",refresh=refresh) { CitySceneRepository(it).load(session) }
   val cold=read();val calls=f.calls.get();val bytes=f.bytes.get()
   assertEquals(4,calls)
   assertEquals(cold,read());assertEquals(calls,f.calls.get())
   assertEquals(cold,read(AccountView(v.account,null)));assertEquals(calls,f.calls.get())
   assertEquals(cold,read(refresh=true));assertEquals(calls*2,f.calls.get())
   println("CITY-COLLECTION cold=$calls requests/$bytes response-UTF8-bytes warm=0 offline=0 refresh=$calls")
  } finally {f.close()}
 }

}
