package com.aladdin.wobbrik

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

/** Production screen/cache/HTTPS transport against a real loopback TLS server; fixture data. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HttpsOverlapTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private class Fixture {
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val snapshotBody="""{"rules_version":"wobbrik-v1","revision":1,"coins":1250,"xp":0,"attempt":null,"server_time":"2026-11-05T00:00:00Z","first_online_at":"2026-11-05T00:00:00Z","grace_until":"2026-11-05T20:00:00Z","level":1,"rescues":0,"achievements":[],"unlocks":[],"land":[],"part_progress":[]}"""
  var snapshot=RecoveryDtos.snapshot(snapshotBody)
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",FinishCosmetics.categories.map {c->CatalogItem("base_1_$c","Bənövşəyi ev",c,"free",0,null,null)},emptyList())
  val commands=AtomicInteger()
  data class Trace(val path:String,val start:Long=System.nanoTime(),@Volatile var cancel:Long=0,@Volatile var end:Long=0,@Volatile var outcome:String="")
  val traces=java.util.concurrent.CopyOnWriteArrayList<Trace>()
  val transport=UrlConnectionGameplayTransport(File("/private/tmp/wobbrik-https/origin").readText().trim())
  @OptIn(InternalCoroutinesApi::class)
  val reads=GameplayHttpBackend(sessions,GameplayTransport {path,bearer,body->
   val trace=Trace(path);traces.add(trace)
   val handle=currentCoroutineContext()[Job]!!.invokeOnCompletion(onCancelling=true,invokeImmediately=true) { cause->
    if(cause is CancellationException) trace.cancel=System.nanoTime()
   }
   try {transport.request(path,bearer,body).also{trace.outcome="response-${it.status}"}}
   catch(e:Exception){trace.outcome=e.javaClass.simpleName;throw e}
   finally {trace.end=System.nanoTime();handle.dispose()}
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
  fun close() {scope.cancel()}
 }

 private val root=File("/private/tmp/wobbrik-https")
 private var serverLogStart=0
 private var previousFactory:javax.net.ssl.SSLSocketFactory?=null
 private fun setup(mode:String) {
  serverLogStart=root.resolve("server.jsonl").takeIf{it.exists()}?.readLines()?.size ?: 0
  root.resolve("mode").writeText(mode)
  root.resolve("$mode-release").delete()
  root.listFiles()!!.filter{it.name.startsWith("$mode-received-")}.forEach{it.delete()}
  root.resolve("categories.json").writeText(FinishCosmetics.categories.joinToString(",","[","]"){"\"$it\""})
  // Trust this generated localhost certificate only; retain hostname verification.
  val certificate=root.resolve("cert.pem").inputStream().use{java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(it)}
  val store=java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType()).apply{load(null,null);setCertificateEntry("fixture",certificate)}
  val tm=javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm()).apply{init(store)}
  val context=javax.net.ssl.SSLContext.getInstance("TLS").apply{init(null,tm.trustManagers,null)}
  previousFactory=javax.net.ssl.HttpsURLConnection.getDefaultSSLSocketFactory()
  javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(context.socketFactory)
 }
 private fun cleanup(mode:String,f:Fixture) {
  root.resolve("$mode-release").writeText("release")
  f.close()
  previousFactory?.let{javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(it)}
 }
 private suspend fun waitFor(predicate:()->Boolean) {withTimeout(15000){while(!predicate()) delay(5)}}
 private fun report(mode:String,f:Fixture) {
  f.traces.forEach{t->println("HTTPS mode=$mode path=${t.path} cancelToExitMs=${if(t.cancel>0)(t.end-t.cancel)/1_000_000 else -1} totalMs=${(t.end-t.start)/1_000_000} outcome=${t.outcome}")}
  assertEquals(0,f.commands.get())
 }
 private fun cancelRead(mode:String,holdMs:Long)=runBlocking {
  setup(mode);val f=Fixture()
  try {
   val session=f.sessions.current()!!;val ready=f.ready()
   var published=false
   val job=launch(Dispatchers.Default) {
    try {f.configured.views!!.read(AccountView(ReadOnlyAccount(session.uid,ready.snapshot,ready.catalog!!),ready),session,
     f.repository.previewKey(false,MarketFilter()),refresh=true,onLocal={_:MarketPreviewPage->published=true},onFresh={published=true}) {
      MarketRepository(it).previewPage(session,false,MarketFilter())
    };published=true
    } catch(e:Exception) {if(currentCoroutineContext().isActive) throw e else println("CANCELLED-READ mode=$mode terminal=${e.javaClass.simpleName}")}
   }
   waitFor{root.resolve("$mode-received-1").exists()}
   job.cancel();waitFor{f.traces.first().cancel>0}
   if(mode!="timeout") {
    withTimeout(1500){job.join()}
    root.resolve("$mode-release").writeText("release")
   }
   withTimeout(12000){job.join()}
   val t=f.traces.single();assertTrue(job.isCancelled);assertFalse(published)
   assertTrue("Cancellation must interrupt TLS read", (t.end-t.cancel)/1_000_000 < 1500)
   assertTrue(t.outcome.contains("Cancellation"))
   assertTrue(f.rows.isEmpty())
   report(mode,f)
  } finally {cleanup(mode,f)}
 }
 @Test fun cancelWhileWaitingForHeaders()=cancelRead("headers",500)
 @Test fun cancelWhileReadingBody()=cancelRead("body",750)
 @Test fun cancelWithoutResponseRelease()=cancelRead("timeout",0)
 @Test fun cancellationDoesNotCloseConcurrentSamePathSocket()=runBlocking {
  setup("market");val f=Fixture()
  try {
   // Use the exact same URL on separate request-owned sockets.
   val url="market?limit=20&min_floors=0&max_floors=0&min_price=0&max_price=0&include=previews"
   val first=launch(Dispatchers.Default){f.reads.read(f.sessions.current()!!,url)}
   waitFor{root.resolve("market-received-1").exists()}
   val second=async(Dispatchers.Default){f.reads.read(f.sessions.current()!!,url)}
   waitFor{root.resolve("market-received-2").exists()}
   first.cancel()
   withTimeout(1500){first.join()}
   assertFalse(second.isCompleted)
   root.resolve("market-release").writeText("release")
   assertTrue(withTimeout(3000){second.await()}.contains("listings"))
   assertEquals(2,f.traces.size)
   assertTrue(f.traces[0].outcome.contains("Cancellation"))
   assertEquals("response-200",f.traces[1].outcome)
   report("same-path-isolation",f)
  } finally {cleanup("market",f)}
 }
 @Test fun realHttpsMarketTabOverlap() {
  setup("market");val f=Fixture()
  try {
   compose.setContent{MaterialTheme{MobileMenuShell("market",f.ready().snapshot.coins,{}, {}) {
    key(InventoryReadKey(f.ready())){MarketScreen(f.configured,f.ready(),{}, {}, {})}
   }}}
   compose.waitUntil(15000){root.resolve("market-received-1").exists()}
   compose.onNodeWithText("Mənim elanlarım").performClick();compose.waitForIdle()
   compose.waitUntil(15000){root.resolve("market-received-2").exists() && f.traces[0].cancel>0}
   compose.onNodeWithText("Alış",useUnmergedTree=true).performClick();compose.waitForIdle()
   compose.waitUntil(15000){f.traces.size>=3 && f.traces[1].cancel>0}
   // Cancelled sockets must exit while the replacement request remains usable.
   compose.waitUntil(15000){f.traces.take(2).all{it.end>0}}
   root.resolve("market-release").writeText("release")
   compose.waitUntil(15000){f.traces.size==16 && f.traces.all{it.end>0}}
   compose.waitUntil(15000){compose.onAllNodesWithText("350").fetchSemanticsNodes().isNotEmpty()}
   assertEquals(2,f.traces.count{it.cancel>0});assertEquals(16,f.traces.size)
   assertTrue(f.traces.take(2).all{(it.end-it.cancel)/1_000_000<1500})
   report("market",f)
  } finally {cleanup("market",f)}
 }
}
