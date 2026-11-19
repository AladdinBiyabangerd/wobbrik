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
import androidx.compose.ui.unit.dp
import appbackend.wobbrik.game.*
import org.junit.Assert.*
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
class HomeCollectionRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val snapshot=GameSnapshot("wobbrik-v1",1,246,246,null,
  AccountRecovery("now","first","later",3,0,emptyList(),emptyList(),emptyList(),emptyList(),40, XpInterval(java.math.BigInteger.valueOf(150),java.math.BigInteger.valueOf(300))))
 private val out=File(System.getProperty("wobbrik.render.output", "/Users/mac/My Workspace/Wobbrik/docs/reviews/unified-mobile-visual")).apply { mkdirs() }
 private fun capture(name:String) {
  compose.waitUntil(15000) { compose.onAllNodesWithContentDescription("Wobbrik").fetchSemanticsNodes().isNotEmpty() }
  // Native host display lists can need another draw after layer invalidation.
  // Never accept a PixelCopy image whose central Play artwork did not paint.
  repeat(3) { attempt ->
   compose.waitForIdle()
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
   val bounds=compose.onNodeWithContentDescription("Oyunu başlat").fetchSemanticsNode().boundsInRoot
   var painted=false
   compose.runOnUiThread {
    val v=compose.activity.window.decorView
    val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
    var result=-1
    android.view.PixelCopy.request(compose.activity.window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    assertEquals(android.view.PixelCopy.SUCCESS,result)
    val target=File(out,"production-$name.png")
    target.outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
    val encoded=android.graphics.BitmapFactory.decodeFile(target.path)
    var cyan=0;var dark=0;var total=0
    for(y in bounds.top.toInt() until bounds.bottom.toInt() step 3) {
     for(x in bounds.left.toInt() until bounds.right.toInt() step 3) {
      val c=encoded.getPixel(x,y);total++
      if(android.graphics.Color.red(c)<130 && android.graphics.Color.green(c)>140 && android.graphics.Color.blue(c)>140) cyan++
      if(android.graphics.Color.red(c)<50 && android.graphics.Color.green(c)<100 && android.graphics.Color.blue(c)<140) dark++
     }
    }
    painted=cyan>total/4 && dark>total/20
    println("Play pixels $name: bounds=$bounds cyan=$cyan dark=$dark total=$total")
    encoded.recycle();b.recycle()
   }
   if(painted) return
   println("Redrawing native host layers for $name (attempt ${attempt+1})")
  }
  fail("$name: Play circle/triangle did not paint after three native draws")
 }
 private fun render(font:Float,name:String,insets:Boolean=false) {
  var plays=0;val routes=mutableListOf<String>()
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme { Box(if(insets) Modifier.fillMaxSize().padding(top=24.dp,bottom=48.dp) else Modifier.fillMaxSize()) {
     HomeScreen(snapshot,false,{plays++},{routes+=it})
    } }
   }
  }
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed().assertIsEnabled()
  capture("$name-initial")
  if (font > 1f) {
   compose.onNodeWithTag("home-city-hero").assertIsDisplayed()
   val hero=compose.onNodeWithTag("home-city-hero").fetchSemanticsNode().boundsInRoot
   assertTrue("hero has no visible height: $hero",hero.height > 0f)
  }
  val play=compose.onNodeWithContentDescription("Oyunu başlat").fetchSemanticsNode().boundsInRoot
  val root=compose.onRoot().fetchSemanticsNode().boundsInRoot
  assertEquals(root.center.x,play.center.x,1f)
  val bottomInset = if (insets) 48f * compose.density.density else 0f
  val dockBottom = maxOf(play.bottom, compose.onNodeWithContentDescription("Profil").fetchSemanticsNode().boundsInRoot.bottom)
  assertTrue("dock too far above safe bottom", root.bottom - bottomInset - dockBottom <= 28 * compose.density.density)
  assertTrue("dock overlaps reserved bottom",play.bottom <= root.bottom - bottomInset + 1f)
  compose.onNodeWithContentDescription("Oyunu başlat").performClick()
  assertEquals(1,plays);assertTrue(routes.isEmpty())
  for((description,route) in listOf("Şəhər" to "city","İnventar" to "inventory","Ana" to "home","Bazar" to "market","Uğurlar" to "achievements","Profil" to "account")) {
   val node=compose.onNodeWithContentDescription(description)
   node.assertIsDisplayed().performClick()
   assertEquals(route,routes.last())
   val bounds=node.fetchSemanticsNode().boundsInRoot
   val density=compose.density.density
   assertTrue("$description target $bounds",bounds.width>=48*density-1 && bounds.height>=48*density-1)
  }
  capture(name)
 }
 private fun headerParity(font:Float,name:String,insets:Boolean=false,longBalance:Boolean=false) {
  var home by mutableStateOf(true)
  val coins=if(longBalance) Long.MAX_VALUE else snapshot.coins
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme { Box(if(insets) Modifier.fillMaxSize().padding(top=24.dp,bottom=48.dp) else Modifier.fillMaxSize()) {
     MobileMenuShell(if(home) "home" else "city",coins,{}, {}) {
      if(home) HomeScreen(snapshot.copy(coins=coins),false,{}, {}) else Box(Modifier.fillMaxSize())
     }
    } }
   }
  }
  compose.waitUntil(15000) { compose.onAllNodesWithContentDescription("Wobbrik").fetchSemanticsNodes().isNotEmpty() }
  val logo=compose.onNodeWithContentDescription("Wobbrik").fetchSemanticsNode().boundsInRoot
  val balance=compose.onNodeWithText(coins.toString()).fetchSemanticsNode().boundsInRoot
  capture("$name-home-header")
  compose.runOnIdle {home=false}
  compose.waitUntil(15000) { compose.onAllNodesWithContentDescription("Wobbrik").fetchSemanticsNodes().isNotEmpty() }
  compose.onNodeWithContentDescription("Wobbrik").assertIsDisplayed()
  assertEquals("logo bounds must match shared shell",logo,compose.onNodeWithContentDescription("Wobbrik").fetchSemanticsNode().boundsInRoot)
  assertEquals("balance bounds must match shared shell",balance,compose.onNodeWithText(coins.toString()).fetchSemanticsNode().boundsInRoot)
  capture("$name-shared-header")
 }
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun headerRegular()=headerParity(1f,"390-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun headerRegularFont2()=headerParity(2f,"390-font2")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun headerSmall()=headerParity(1f,"320-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun headerSmallFont2InsetsLongBalance()=headerParity(2f,"320-font2-insets-long",true,true)
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont1()=render(1f,"home-320-font1")
 @Test fun approved360()=render(1f,"home-360-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular390()=render(1f,"home-390-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular390Font2()=render(2f,"home-390-font2")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont2()=render(2f,"home-320-font2")
 @Test fun font2Insets()=render(2f,"home-360-font2-insets",true)
 @Test fun recoveryCannotDispatchPlayAndRetryIsSeparate() {
  var status:String? by mutableStateOf(null);var retry=0;var plays=0;var allow by mutableStateOf(true)
  compose.setContent {MaterialTheme {HomeScreen(snapshot,false,if(allow) ({plays++}) else null,{},status,{retry++})}}
  compose.runOnIdle {allow=false}
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsNotEnabled().performClick()
  assertEquals(0,plays)
  compose.runOnIdle {allow=true;status="Bağlantı yoxdur"}
  // Offline local start is permitted when the host supplies a callback; status is display-only.
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsEnabled().performClick()
  compose.onNodeWithText("Yenilə").performClick();assertEquals(1,retry);assertEquals(1,plays)
  capture("home-offline")
 }
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun longBalanceAndXp() {
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,2f)) {
    MaterialTheme { HomeScreen(snapshot.copy(coins=Long.MAX_VALUE,xp=1146,account=snapshot.account!!.copy(xpInterval=XpInterval(java.math.BigInteger.valueOf(1050),java.math.BigInteger.valueOf(1400)))),false,{},{}) }
   }
  }
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed()
  compose.onNodeWithText(Long.MAX_VALUE.toString()).assertIsDisplayed()
  compose.onNodeWithTag("home-city-hero").assertIsDisplayed()
  capture("home-long-balance-font2")
 }
 @Test fun journalRecoveryCannotStart() {
  var retries=0; var plays=0
  compose.setContent { MaterialTheme { HomeScreen(snapshot,false,{plays++},{},null,{retries++},true) } }
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsNotEnabled().performClick()
  compose.onNodeWithText(JournalRecoveryCopy.Retry).performClick()
  assertEquals(0,plays); assertEquals(1,retries)
 }
 @Test fun firstReadHasAccessibleRecoveryShell() {
  compose.setContent { MaterialTheme { HomeScreen(null,false,null,{}) } }
  compose.onNodeWithText("Hesab və cəhd bərpa olunur…").assertIsDisplayed()
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsNotEnabled()
  compose.onNodeWithContentDescription("Profil").assertIsDisplayed()
 }

}
