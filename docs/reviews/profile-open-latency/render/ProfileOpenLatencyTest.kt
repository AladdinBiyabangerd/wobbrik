package com.aladdin.wobbrik

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import appbackend.wobbrik.game.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/** Real Home -> PublicAccountScreen, synthetic delayed HTTP. No device timing claim. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileOpenLatencyTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()

 /** Profile-only account fixture: production owner/HTTP contracts, no Firebase or commands. */
 internal class Fixture {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=appbackend.wobbrik.billing.AccountSessions {"token"}
  private val snapshot=RecoveryDtos.snapshot("""{"rules_version":"wobbrik-v1","revision":1,"coins":1250,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}""")
  private val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",FinishCosmetics.categories.map {c->CatalogItem("base_1_$c","Bənövşəyi ev",c,"free",0,null,null)},emptyList())
  val owner=AuthenticatedRecoveryOwner(sessions,object:AttemptBackend {
   override suspend fun sync(session:appbackend.wobbrik.billing.BillingSession)=snapshot
   override suspend fun catalog(session:appbackend.wobbrik.billing.BillingSession)=catalog
   override suspend fun submit(session:appbackend.wobbrik.billing.BillingSession,command:AttemptCommand)=error("No profile game command")
  },object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=error("No profile journal write")
   override fun remove(uid:String,command:AttemptCommand)=error("No profile journal write")
  },scope)
  private val unused=GameplayHttpBackend(sessions,GameplayTransport {path,_,_->error("Unexpected read $path")})
  val configured=WobbrikApplication.Bootstrap.Configured(owner,com.aladdin.wobbrik.engine.AcceptedAttemptCommands(owner),
   InventoryRepository(unused),sessions,DecorationRepository(unused),CitySceneRepository(unused))
  init {owner.onAuthChanged("alice")}
  fun ready()=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
  fun close() {scope.cancel()}
 }


 private fun capture(name:String) {
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(compose.activity.window.decorView)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val warm=android.graphics.Bitmap.createBitmap(v.width,v.height,android.graphics.Bitmap.Config.ARGB_8888)
   v.draw(android.graphics.Canvas(warm));warm.recycle();v.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val b=android.graphics.Bitmap.createBitmap(v.width,v.height,android.graphics.Bitmap.Config.ARGB_8888)
   var result = -1
   android.view.PixelCopy.request(compose.activity.window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   Assert.assertEquals(android.view.PixelCopy.SUCCESS,result)
   val dir=File("/private/tmp/wobbrik-profile-open/rendered").apply {mkdirs()}
   File(dir,"$name.png").outputStream().use {b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};b.recycle()
  }
 }

 @OptIn(InternalComposeTracingApi::class)
 @Test fun firstAndReturningProfile() {
  val f=Fixture()
  val calls=AtomicInteger();val bytes=AtomicInteger()
  val access=GameplayHttpBackend(f.sessions,GameplayTransport {path,_,_->
   check(path=="profile");calls.incrementAndGet();withContext(Dispatchers.IO) { Thread.sleep(400) }
   val body="""{"revision":1,"display_name":"Aladdin","avatar_id":"builder"}"""
   bytes.addAndGet(body.toByteArray().size);GameplayResponse(200,body)
  })
  val configured=f.configured.copy(profiles=access)
  var page by mutableStateOf("home")
  val spans=java.util.Collections.synchronizedList(mutableListOf<String>())
  val stack=ThreadLocal.withInitial {java.util.ArrayDeque<Pair<String,Long>>()}
  var tracing=false
  val tracer=object:CompositionTracer {
   override fun isTraceInProgress()=tracing
   override fun traceEventStart(key:Int,dirty1:Int,dirty2:Int,info:String) {stack.get()!!.push(info to System.nanoTime())}
   override fun traceEventEnd() {val s=stack.get()!!.poll()?:return;val ns=System.nanoTime()-s.second
    if(ns>1_000_000) spans.add("${s.first}\t${ns/1_000_000.0}\t${Thread.currentThread().name}")}
  }
  try {
   compose.setContent {val display = rememberProfileDisplayMemory(configured, configured.sessions.current())
    MaterialTheme {MobileMenuShell(page,1250,{}, {page=it}) {
    if(page=="home") MobileHomeScreen(f.ready().snapshot,false,{}, {page=it},null,{},false)
    else PublicAccountScreen(configured,f.ready(),{page="home"},{page=it},display)
   }}}
   compose.onNodeWithText("Öz şəhərini qur").assertExists()
   compose.waitUntil(10000) {compose.onAllNodesWithContentDescription("Wobbrik").fetchSemanticsNodes().isNotEmpty()}
   Composer.setTracer(tracer)
   val rounds=mutableListOf("round,contentMs,avatarMs,requests,responseBytes")
   repeat(4) {r->
    val start=System.nanoTime();tracing=true
    compose.runOnIdle {page="account"}
    compose.waitUntil(10000) {compose.onAllNodesWithText("Aladdin").fetchSemanticsNodes().isNotEmpty()}
    val content=System.nanoTime()
    compose.waitUntil(10000) {compose.onAllNodesWithContentDescription("İnşaatçı avatarı").fetchSemanticsNodes().isNotEmpty()}
    val end=System.nanoTime();tracing=false
    compose.waitUntil(10000) {compose.onAllNodesWithTag("profile-edit").fetchSemanticsNodes().any {
     !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled)
    }}
    rounds.add("$r,${(content-start)/1_000_000.0},${(end-start)/1_000_000.0},${calls.get()},${bytes.get()}")
    compose.runOnIdle {page="home"};compose.waitForIdle()
   }
   val dir=File("/private/tmp/wobbrik-profile-open").apply {mkdirs()}
   File(dir,"rounds.csv").writeText(rounds.joinToString("\n"))
   File(dir,"spans.tsv").writeText(spans.joinToString("\n"))
   println(rounds.joinToString("\n"))
   Assert.assertEquals(4,calls.get())
  } finally {tracing=false;f.close()}
 }
 @Test fun cachedDisplayIsNotAnEditorCapabilityAndEpochsAreIsolated() {
  val f=Fixture()
  var response=PublicProfile(1,"Aladdin","builder")
  var gate:CompletableDeferred<Unit>?=null
  var fail=false
  val calls=AtomicInteger();val writes=AtomicInteger()
  val access=object:ProfileAccess {
   override suspend fun profile(session:appbackend.wobbrik.billing.BillingSession):PublicProfile {
    calls.incrementAndGet();gate?.await();if(fail) error("offline");return response
   }
   override suspend fun replaceProfile(session:appbackend.wobbrik.billing.BillingSession,replacement:ProfileReplacement):PublicProfile {
    writes.incrementAndGet();return PublicProfile(2,replacement.displayName,replacement.avatarId).also {response=it}
   }
  }
  val configured=f.configured.copy(profiles=access)
  var page by mutableStateOf("account");var epoch by mutableIntStateOf(0)
  lateinit var memory:ProfileDisplayMemory
  try {
   compose.setContent {
    // Same lifetime/key boundary as AuthenticatedGame + InventoryReadKey.
    val login = epoch
    val display=rememberProfileDisplayMemory(configured, configured.sessions.current())!!;memory=display
    MaterialTheme {MobileMenuShell(page,1250,{}, {page=it}) {
     key(login) {if(page=="account") PublicAccountScreen(configured,f.ready(),{page="home"},{page=it},display)}
    }}
   }
   compose.waitUntil(10000) {compose.onAllNodesWithText("Aladdin").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithTag("profile-edit").assertIsEnabled()
   compose.waitUntil(10000) {compose.onAllNodesWithContentDescription("İnşaatçı avatarı").fetchSemanticsNodes().isNotEmpty()}
   capture("profile-fresh")
   val original=memory
   compose.runOnIdle {page="home"};compose.waitForIdle()
   gate=CompletableDeferred()
   compose.runOnIdle {page="account"}
   compose.waitForIdle()
   compose.waitUntil(10000) {calls.get()==2}
   compose.onNodeWithText("Aladdin").assertExists()
   compose.onNodeWithTag("profile-edit").assertIsNotEnabled()
   capture("profile-cached-refresh")
   Assert.assertEquals(0,writes.get())
   response=PublicProfile(3,"Yeni","pilot");gate!!.complete(Unit)
   compose.waitUntil(10000) {compose.onAllNodesWithText("Yeni").fetchSemanticsNodes().isNotEmpty()}
   Assert.assertEquals(response,memory.profile)
   compose.onNodeWithTag("profile-edit").assertIsEnabled()
   compose.runOnIdle {page="home"};compose.waitForIdle()
   gate=null;fail=true
   compose.runOnIdle {page="account"}
   compose.waitUntil(10000) {compose.onAllNodesWithText("Profil yenilənmədi. Bağlantını yoxla.").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithText("Yeni").assertExists()
   compose.onNodeWithTag("profile-edit").assertIsNotEnabled()
   capture("profile-cached-error")
   fail=false
   compose.onNodeWithText("Serverdən yenilə").performScrollTo().performClick()
   compose.waitForIdle()
   compose.waitUntil(10000) {calls.get()==4}
   compose.onNodeWithTag("profile-edit").assertIsEnabled()
   // Same UID login gets a new memory; cached identity is not allowed into it.
   compose.runOnIdle {page="home"};compose.waitForIdle()
   gate=CompletableDeferred()
   compose.runOnIdle {f.owner.onAuthChanged("alice");epoch++;page="account"}
   compose.waitForIdle()
   compose.waitUntil(10000) {calls.get()==5}
   Assert.assertNotSame(original,memory);Assert.assertNull(memory.profile)
   compose.onNodeWithText("Yeni").assertDoesNotExist()
   compose.onNodeWithTag("profile-edit").assertDoesNotExist()
   gate!!.complete(Unit)
   compose.waitUntil(10000) {compose.onAllNodesWithText("Yeni").fetchSemanticsNodes().isNotEmpty()}
   val alice=memory
   compose.runOnIdle {page="home"};compose.waitForIdle()
   gate=CompletableDeferred()
   compose.runOnIdle {f.owner.onAuthChanged("bob");epoch++;page="account"}
   compose.waitForIdle()
   compose.waitUntil(10000) {calls.get()==6}
   Assert.assertNotSame(alice,memory);Assert.assertNull(memory.profile)
   compose.onNodeWithText("Yeni").assertDoesNotExist()
   Assert.assertEquals(0,writes.get())
  } finally {gate?.complete(Unit);f.close()}
 }

 @Test fun successfulSaveIsTheNextCachedDisplayButStillRequiresFreshLoad() {
  val f=Fixture()
  var response=PublicProfile(1,"Aladdin","builder")
  var gate:CompletableDeferred<Unit>?=null
  val writes=mutableListOf<ProfileReplacement>()
  val access=object:ProfileAccess {
   override suspend fun profile(session:appbackend.wobbrik.billing.BillingSession):PublicProfile {gate?.await();return response}
   override suspend fun replaceProfile(session:appbackend.wobbrik.billing.BillingSession,replacement:ProfileReplacement):PublicProfile {
    writes.add(replacement);return PublicProfile(2,replacement.displayName,replacement.avatarId).also {response=it}
   }
  }
  val configured=f.configured.copy(profiles=access)
  var page by mutableStateOf("account")
  lateinit var memory:ProfileDisplayMemory
  try {
   compose.setContent {val display=rememberProfileDisplayMemory(configured, configured.sessions.current())!!;memory=display
    MaterialTheme {MobileMenuShell(page,1250,{}, {page=it}) {
     if(page=="account") PublicAccountScreen(configured,f.ready(),{page="home"},{page=it},display)
    }}
   }
   compose.waitUntil(10000) {compose.onAllNodesWithText("Aladdin").fetchSemanticsNodes().isNotEmpty()}
   compose.onNodeWithTag("profile-edit").performClick()
   compose.onNodeWithTag("profile-name").performScrollTo().performTextReplacement("Ayan")
   compose.onNodeWithTag("profile-save").performClick()
   compose.waitUntil(10000) {compose.onAllNodesWithText("Ayan").fetchSemanticsNodes().isNotEmpty()}
   Assert.assertEquals(listOf(ProfileReplacement(1,"Ayan","builder")),writes)
   Assert.assertEquals(response,memory.profile)
   compose.runOnIdle {page="home"};compose.waitForIdle()
   gate=CompletableDeferred()
   compose.runOnIdle {page="account"}
   compose.onNodeWithText("Ayan").assertExists()
   compose.onNodeWithTag("profile-edit").assertIsNotEnabled()
  } finally {gate?.complete(Unit);f.close()}
 }

}
