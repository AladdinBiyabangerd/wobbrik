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
class HomeProductionRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val snapshot=GameSnapshot("wobbrik-v1",1,1250,1050,null,
  AccountRecovery("now","first","later",7,0,emptyList(),emptyList(),emptyList(),emptyList(),28))
 private val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/mobile-sizing-visual")
 private fun capture(name:String) {
  compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
   File(out,"production-$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
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
  val play=compose.onNodeWithContentDescription("Oyunu başlat").fetchSemanticsNode().boundsInRoot
  val root=compose.onRoot().fetchSemanticsNode().boundsInRoot
  assertEquals(root.center.x,play.center.x,1f)
  compose.onNodeWithContentDescription("Oyunu başlat").performClick()
  assertEquals(1,plays);assertTrue(routes.isEmpty())
  for((description,route) in listOf("Şəhərim" to "city","İnventar" to "inventory","Kolleksiya" to "collection","Nailiyyətlər" to "achievements","Hesab" to "account")) {
   val node=compose.onNodeWithContentDescription(description)
   node.assertIsDisplayed().performClick()
   assertEquals(route,routes.last())
   val bounds=node.fetchSemanticsNode().boundsInRoot
   val density=compose.density.density
   assertTrue("$description target $bounds",bounds.width>=48*density-1 && bounds.height>=48*density-1)
  }
  capture(name)
 }
 @Test fun approved360()=render(1f,"home-360-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular390()=render(1f,"home-390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallFont2()=render(2f,"home-320-font2")
 @Test fun font2Insets()=render(2f,"home-360-font2-insets",true)
 @Test fun recoveryCannotDispatchPlayAndRetryIsSeparate() {
  var status:String? by mutableStateOf(null);var retry=0;var plays=0;var allow by mutableStateOf(true)
  compose.setContent {MaterialTheme {HomeScreen(snapshot,false,if(allow) ({plays++}) else null,{},status,{retry++})}}
  compose.runOnIdle {allow=false}
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsNotEnabled().performClick()
  assertEquals(0,plays)
  compose.runOnIdle {allow=true;status="Bağlantı yoxdur"}
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsNotEnabled().performClick()
  compose.onNodeWithText("Yenilə").performClick();assertEquals(1,retry);assertEquals(0,plays)
  capture("home-offline")
 }
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun longBalanceAndXp() {
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,2f)) {
    MaterialTheme { HomeScreen(snapshot.copy(coins=Long.MAX_VALUE,account=snapshot.account!!.copy(xpInterval=XpInterval(java.math.BigInteger.valueOf(1050),java.math.BigInteger.valueOf(1400)))),false,{},{}) }
   }
  }
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed()
  compose.onNodeWithText(Long.MAX_VALUE.toString()).assertIsDisplayed()
  capture("home-long-balance-font2")
 }
}
