package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
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
class ResourceBackgroundTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File("/private/tmp/wobbrik-resource-background").apply {mkdirs()}
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

 private fun screen(font:Float, profile:Boolean, label:String) {
  compose.setContent { val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {MaterialTheme {
    if(profile) MobileMenuShell("profile",Long.MAX_VALUE,null,{}) {
     Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
      NativeProfileOverview(appbackend.wobbrik.game.PublicProfile(1,"Aladdin", "builder"),"3",96,"40",{},true,true,{})
     }
    } else HomeScreen(GameSnapshot("wobbrik-v1",1,12,0,null),false,{}, {})
   }}
  }
  compose.waitForIdle()
  // Wait for the independently composition-owned portrait/provider producers too.
  if(profile) compose.waitUntil(15000){compose.onAllNodesWithContentDescription("İnşaatçı avatarı").fetchSemanticsNodes().isNotEmpty()}
  if(!profile) compose.waitUntil(15000){compose.onAllNodesWithTag("home-city-hero").fetchSemanticsNodes().isNotEmpty()}
  capture(label)
 }
 @Test fun homeNormal()=screen(1f,false,"home-360-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun homeSmallLargeFont()=screen(2f,false,"home-320-font2")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun homeSmall()=screen(1f,false,"home-320-font1")
 @Test fun homeNormalLargeFont()=screen(2f,false,"home-360-font2")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun profileSmall()=screen(1f,true,"profile-320-font1")
 @Test fun profileNormalLargeFont()=screen(2f,true,"profile-360-font2")
 @Test fun profileNormal()=screen(1f,true,"profile-360-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun profileSmallLargeFont()=screen(2f,true,"profile-320-font2")
 @Test fun pixelsConfigurationAndRemount() {
  var generation by mutableIntStateOf(0); var config by mutableIntStateOf(0)
  var images:List<androidx.compose.ui.graphics.ImageBitmap>?=null
  var loadingTransitions=0
  compose.setContent {
   val current=android.content.res.Configuration(androidx.compose.ui.platform.LocalConfiguration.current).apply {densityDpi=if(config==0)480 else 320}
   CompositionLocalProvider(androidx.compose.ui.platform.LocalConfiguration provides current) {key(generation) {
    val (art,_)=rememberResourceArt(R.drawable.home_city_cutout_v3,R.drawable.account_portraits_v3,
      R.drawable.profile_v2_board,R.drawable.profile_edit_board,R.drawable.profile_skin_clean,R.drawable.profile_panel_clean)
    SideEffect { images=art.images; if(images==null) loadingTransitions++ }
   }}
  }
  repeat(3) { index ->
   compose.waitUntil(15000){images!=null};compose.waitForIdle()
   compose.runOnIdle {
    val res=compose.activity.resources
    val expected=androidx.compose.ui.graphics.ImageBitmap.imageResource(res,R.drawable.home_city_cutout_v3)
    assertTrue(images!![0].asAndroidBitmap().sameAs(expected.asAndroidBitmap()))
    println("RESOURCE-BACKGROUND mount=$index ready=true bytes="+images!!.sumOf{it.asAndroidBitmap().allocationByteCount.toLong()})
    if(index==0) generation++ else if(index==1) config++
   }
   compose.waitForIdle()
  }
  assertTrue(loadingTransitions>=3)
 }
 @Test fun workerRetryAndConfigurationCancellation() {
  val calls=AtomicInteger();val entered=CountDownLatch(1);val release=CountDownLatch(1)
  var config by mutableIntStateOf(0);var result=ResourceArt();var retry:()->Unit={}
  var finalImage:androidx.compose.ui.graphics.ImageBitmap?=null
  val decode:(android.content.res.Resources,Int)->androidx.compose.ui.graphics.ImageBitmap={_,_->
   assertNotEquals(Looper.getMainLooper(),Looper.myLooper())
   when(calls.incrementAndGet()) {
    1 -> throw java.io.IOException("fixture")
    2 -> {entered.countDown();check(release.await(10,TimeUnit.SECONDS));androidx.compose.ui.graphics.ImageBitmap(1,1)}
    else -> androidx.compose.ui.graphics.ImageBitmap(2,2).also {finalImage=it}
   }
  }
  compose.setContent {
   val cfg=android.content.res.Configuration(androidx.compose.ui.platform.LocalConfiguration.current).apply {fontScale=if(config==0)1f else 2f}
   CompositionLocalProvider(androidx.compose.ui.platform.LocalConfiguration provides cfg) {
    val (art,again)=rememberResourceArt(R.drawable.home_city_cutout_v3,decode=decode)
    SideEffect {result=art;retry=again}
   }
  }
  compose.waitUntil(15000){result.failed}
  compose.runOnIdle{retry()};compose.waitForIdle()
  try {
   assertTrue(entered.await(5,TimeUnit.SECONDS))
   compose.runOnIdle{config++};compose.waitForIdle()
   compose.waitUntil(15000){result.images!=null}
   assertSame(finalImage,result.images!!.single())
  } finally {release.countDown()}
  compose.waitForIdle();assertSame(finalImage,result.images!!.single());assertEquals(3,calls.get())
 }

}
