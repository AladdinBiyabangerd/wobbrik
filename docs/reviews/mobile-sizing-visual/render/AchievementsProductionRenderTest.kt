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
class AchievementsProductionRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File(System.getProperty("wobbrik.render.output", "/Users/mac/My Workspace/Wobbrik/docs/reviews/achievements-native")).apply { mkdirs() }
 private fun capture(name:String) {
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
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   var result=-1
   android.view.PixelCopy.request(compose.activity.window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   File(out,"production-$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
   b.recycle()
  }
 }
 private fun render(font:Float,name:String,empty:Boolean=false,insets:Boolean=false,legacy:Boolean=false) {
  val snapshot=GameSnapshot("wobbrik-v1",1,if(insets) Long.MAX_VALUE else 246,246,null,
   AccountRecovery("now","first","later",3,0,if(empty) emptyList() else listOf("height_10","rescue_1","precise_3","future_award_with_a_long_identifier"),emptyList(),emptyList(),emptyList(),40,
    XpInterval(java.math.BigInteger.valueOf(150),java.math.BigInteger.valueOf(300)),
    if(legacy) null else listOf(AchievementProgress("height_10","height",10,if(empty) 0 else 10),
     AchievementProgress("height_20","height",20,if(empty) 0 else 12),
     AchievementProgress("precise_5","precise",5,if(empty) 0 else 3),
     AchievementProgress("rescue_5","rescue",5,if(empty) 0 else 2))))
  var destination=""
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme { Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=24.dp,bottom=48.dp) else Modifier)) {
     MobileMenuShell("achievements",snapshot.coins,{}, {destination=it}) {
      AccountScreen("achievements",null,null,{destination=it},{},{},false,cached=snapshot)
     }
    } }
   }
  }
  compose.onNodeWithText("Nailiyyətlər").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { action ->
   val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
   action(layouts)
   val layout=layouts.single()
   assertTrue("achievement tab glyphs overflow: right=${layout.getLineRight(0)} width=${layout.size.width}",
    layout.getLineRight(0) <= layout.size.width + 1f)
  }
  compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("${if(empty) 0 else 4} mükafat açılıb"))
  compose.onNodeWithText("${if(empty) 0 else 4} mükafat açılıb").assertIsDisplayed()
  if(!empty && !legacy) {
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("12 / 20"))
   compose.onNodeWithText("12 / 20").assertIsDisplayed()
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("3 / 5"))
   compose.onNodeWithText("3 / 5").assertIsDisplayed()
   capture("$name-progress")
  }
  if(!empty) {
   compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Yeni nailiyyət"))
   compose.onNodeWithText("Yeni nailiyyət").assertIsDisplayed()
  }
  compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
  capture(name)
  if(font>1f) compose.onNodeWithText("Rekordlar").performScrollTo()
  compose.onNodeWithText("Rekordlar").performClick()
  assertEquals("leaderboard",destination)
  compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
  assertEquals("home",destination)
 }
 @Test fun compact()=render(1f,"achievements-360-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular()=render(1f,"achievements-390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun small()=render(1f,"achievements-320-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun largeFont()=render(2f,"achievements-320-font2")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regularLargeFont()=render(2f,"achievements-390-font2")
 @Test fun legacyEmpty()=render(1f,"achievements-legacy-empty",empty=true,legacy=true)
 @Test fun legacyAwards()=render(1f,"achievements-legacy-awards",legacy=true)
 @Test fun empty()=render(1f,"achievements-empty",empty=true)
 @Test fun insetLongBalance()=render(2f,"achievements-insets",insets=true)
}
