package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.game.GameSnapshot
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OffMainArtTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File("/private/tmp/wobbrik-idle-art").apply {mkdirs()}
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
   val v=compose.activity.window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(b));File(out,"$name.png").outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
  }
 }
 @Test fun coldDecodeRunsOffMainWhileNativeNavigationResponds() {
  val entered=CountDownLatch(1);val release=CountDownLatch(1)
  val calls=AtomicInteger();var offMain=false;var decodeMs=0L
  var route by mutableStateOf("inventory");var ready by mutableStateOf(false)
  val assets=compose.activity.assets
  val load:()->Unit={
   calls.incrementAndGet();offMain=Looper.myLooper()!=Looper.getMainLooper();entered.countDown()
   check(release.await(10,TimeUnit.SECONDS))
   val start=System.nanoTime();ApprovedArt.prepare(assets);decodeMs=(System.nanoTime()-start)/1_000_000
  }
  compose.setContent {MaterialTheme {
   val status=rememberArtPreparation(Unit,load)
   SideEffect{ready=status==ArtPreparation.Ready}
   if(status!=ArtPreparation.Ready) MobileMenuShell(route,12,null,{route=it}) {ApprovedArtStatus(status,{})}
   else {
    val start=System.nanoTime()
    rememberGameAtlases();rememberGameWindowArt();rememberCosmeticArt()
    val helperUs=(System.nanoTime()-start)/1000
    SideEffect{println("ART-READY helperCompositionUs=$helperUs")}
    HomeScreen(GameSnapshot("wobbrik-v1",1,12,0,null),false,{}, {route=it})
   }
  }}
  try {
   assertTrue(entered.await(5,TimeUnit.SECONDS));compose.waitForIdle()
   compose.onNodeWithText("Qrafika yüklənir…").assertIsDisplayed()
   val start=System.nanoTime()
   compose.onNodeWithContentDescription("Şəhərim").performClick()
   compose.runOnIdle{assertEquals("city",route);assertFalse(ready)}
   println("ART-BLOCKED navigationHostMs=${(System.nanoTime()-start)/1_000_000}")
   capture("loading-360-font1")
  } finally {release.countDown()}
  compose.waitUntil(15000){ready};compose.waitForIdle();capture("ready-home-360-font1")
  assertTrue(offMain);assertEquals(1,calls.get());assertTrue(ApprovedArt.prepared)
  println("ART-IO decodeMs=$decodeMs offMain=$offMain")
 }
 private fun loading(font:Float,failed:Boolean,name:String,insets:Boolean=false) {
  var retries=0
  compose.setContent {val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {MaterialTheme {
    Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
     MobileMenuShell("city",Long.MAX_VALUE,null,{}) {
      ApprovedArtStatus(if(failed) ArtPreparation.Failed else ArtPreparation.Loading,{retries++})
     }
    }
   }}
  }
  compose.waitForIdle();capture(name)
  if(failed) {compose.onNodeWithText("Yenidən cəhd et").performScrollTo().performClick();assertEquals(1,retries)}
 }
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun loadingSmallFont2()=loading(2f,false,"loading-320-font2")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun failureSmallFont2()=loading(2f,true,"failure-320-font2")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun loadingSmallFont1()=loading(1f,false,"loading-320-font1")
 @Test fun loadingNormalFont2Insets()=loading(2f,false,"loading-360-font2-insets",true)
 @Test fun failureNormalFont1()=loading(1f,true,"failure-360-font1")
 @Test fun failedLoadRetriesAndCancelledProducerCannotPublishReady() {
  var key by mutableIntStateOf(0);var ready by mutableStateOf(false)
  val entered=CountDownLatch(1);val release=CountDownLatch(1);val calls=AtomicInteger()
  compose.setContent {MaterialTheme {
   val status=rememberArtPreparation(key) {
    when(calls.incrementAndGet()) {
     1->throw java.io.IOException("fixture")
     2->{entered.countDown();check(release.await(10,TimeUnit.SECONDS))}
     3->throw java.io.IOException("new generation fails")
    }
   }
   SideEffect{ready=status==ArtPreparation.Ready}
   ApprovedArtStatus(status){key++}
  }}
  compose.waitUntil(5000){calls.get()==1};compose.waitForIdle()
  compose.onNodeWithText("Yenidən cəhd et").performClick()
  compose.waitForIdle()
  assertTrue(entered.await(5,TimeUnit.SECONDS))
  compose.runOnIdle {key++}
  compose.waitForIdle()
  compose.waitUntil(5000){calls.get()==3};release.countDown();compose.waitForIdle()
  compose.runOnIdle{assertFalse(ready)}
  compose.onNodeWithText("Yenidən cəhd et").performClick()
  compose.waitForIdle()
  compose.waitUntil(5000){ready};assertEquals(4,calls.get())
 }
}
