package com.aladdin.wobbrik

import android.app.Application
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.game.*
import com.aladdin.wobbrik.engine.AcceptedAttemptCommands
import com.aladdin.wobbrik.engine.AcceptedEngineHost
import kotlinx.coroutines.*
import org.junit.Assert.*
import appbackend.wobbrik.billing.BillingSession
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class InitialSyncRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File("/private/tmp/wobbrik-initial-sync-renders").apply { mkdirs() }
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
   val warm=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(warm));warm.recycle();v.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   var result=-1
   android.view.PixelCopy.request(compose.activity.window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   val colors=HashSet<Int>()
   for(y in 0 until b.height step 6) for(x in 0 until b.width step 6) colors.add(b.getPixel(x,y))
   assertTrue("$name must contain painted UI", colors.size>32)
   File(out,"production-$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
   b.recycle()
  }
 }

 private fun render(font:Float,name:String,failed:Boolean=false) {
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme { InitialSyncScreen(987654321L,failed,true,true,{}, {}) }
   }
  }
  compose.onNodeWithText("Menyuya keç").assertIsDisplayed()
  capture(name)
 }
 @Test fun repeatedArtBaseline() {
  var generation by mutableStateOf(0)
  val times=mutableListOf<Long>()
  val images=java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Bitmap,Boolean>())
  var bytes=0L
  compose.setContent {
   key(generation) {
    val start=System.nanoTime()
    val cosmetic=rememberCosmeticArt()
    val windows=rememberGameWindowArt()
    val atlases=rememberGameAtlases()
    val elapsed=(System.nanoTime()-start)/1_000_000
    SideEffect {
     times.add(elapsed)
     val unique=(cosmetic.values.map {it.image}+windows.values.map {it.image}+atlases.values.flatMap {it.cells.values.map {s->s.image}}).map {it.asAndroidBitmap()}.distinct()
     unique.forEach { if(images.add(it)) bytes+=it.allocationByteCount.toLong() }
    }
   }
  }
  repeat(4) { compose.runOnIdle {generation++}; compose.waitForIdle() }
  println("ART_BASELINE host compose ms=$times uniqueBitmaps=${images.size} allocatedRGBABytes=$bytes")
 }
 @Test fun compact()=render(1f,"360-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun small()=render(1f,"320-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallLarge()=render(2f,"320-font2",true)
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normal()=render(1f,"390-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normalLarge()=render(2f,"390-font2")
 @Test fun gateTimeoutErrorAndEpoch() {
  var epoch by mutableStateOf(BillingSession("alice","one"))
  var state by mutableStateOf<AuthenticatedRecoveryOwner.State>(AuthenticatedRecoveryOwner.State.Recovering)
  var retries=0
  compose.mainClock.autoAdvance=false
  compose.setContent { MaterialTheme {
   if (!InitialAccountSync(epoch,state,null,false,{retries++})) Text("Menu")
  }}
  compose.mainClock.advanceTimeByFrame()
  compose.onNodeWithText("Menyuya keç").assertDoesNotExist()
  compose.mainClock.advanceTimeBy(InitialSyncWaitMillis+64)
  compose.onNodeWithText("Menyuya keç").assertIsDisplayed().performClick()
  compose.mainClock.advanceTimeByFrame()
  compose.onNodeWithText("Menu").assertExists()
  compose.runOnUiThread {state=AuthenticatedRecoveryOwner.State.Error(java.io.IOException())}
  compose.mainClock.advanceTimeByFrame()
  compose.onNodeWithText("Menu").assertExists()
  compose.runOnUiThread {epoch=BillingSession("alice","two")}
  compose.mainClock.advanceTimeByFrame()
  compose.onNodeWithText("Yenidən cəhd et").performClick()
  assertEquals(1,retries)
  compose.onNodeWithText("Menyuya keç").assertIsDisplayed()
 }
 @Test fun readyDoesNotRegateAndCacheCanExit() {
  var epoch by mutableStateOf(BillingSession("alice","one"))
  val snapshot=GameSnapshot("wobbrik-v1",1,0,0,null,null)
  var state by mutableStateOf<AuthenticatedRecoveryOwner.State>(AuthenticatedRecoveryOwner.State.Ready(snapshot))
  compose.setContent {MaterialTheme {
   if (!InitialAccountSync(epoch,state,null,true,{})) Text("Menu")
  }}
  compose.onNodeWithText("Menu").assertExists()
  compose.runOnIdle {state=AuthenticatedRecoveryOwner.State.Recovering}
  compose.onNodeWithText("Menu").assertExists()
  compose.runOnIdle {epoch=BillingSession("bob","two")}
  compose.onNodeWithText("Menyuya keç").assertIsDisplayed().performClick()
  compose.onNodeWithText("Menu").assertExists()
 }
}
