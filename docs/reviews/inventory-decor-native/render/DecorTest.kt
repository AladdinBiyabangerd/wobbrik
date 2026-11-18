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
import appbackend.wobbrik.billing.AccountSessions
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


abstract class DecorHost {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 private fun capture(name:String, dialog:Boolean=false) {
  val window=if(dialog) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(window.decorView)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=window.decorView
   val warm=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(warm));warm.recycle();v.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   var result=-1
   android.view.PixelCopy.request(window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File(requireNotNull(System.getProperty("wobbrik.render.output","/private/tmp/wobbrik-commerce-native"))).apply {mkdirs()}
   File(out,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
  }
 }

 protected fun render(font:Float,name:String,insets:Boolean=false) {
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",listOf(
   CatalogItem("decor_tree_1",if(insets) "Uzun dekor adı bütöv sözlərlə göstərilir" else "Tək ağac","decoration","copy",0,1,null),
   CatalogItem("decor_tree_2","Ağac cütü","decoration","copy",0,1,null),
   CatalogItem("decor_bench_1","Tək skamya","decoration","copy",0,1,null),
   CatalogItem("decor_streetlight_1","Tək küçə fənəri","decoration","copy",0,1,null),
   CatalogItem("decor_road_1","Qısa düz yol","decoration","copy",0,1,null),
   CatalogItem("decor_tree_3","Kiçik ağaclıq","decoration","copy",0,1,null)),emptyList())
  val copies=catalog.items.mapIndexed { i,it -> DecorationCopy("copy_$i",it.id, if(i%2==0) CityPosition(0,0,0) else null) }
  var loading by mutableStateOf(false);var failed by mutableStateOf(false);var cached by mutableStateOf(false)
  var data by mutableStateOf(copies);var market=0;var refresh=0;var more=0
  compose.setContent { val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {MaterialTheme {
    Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
     MobileMenuShell("owned-decorations",if(insets) Long.MAX_VALUE else 411,{}, {}) {
      InventoryDecorContent(catalog,data,loading,failed,cached,true,{more++},{refresh++},{market++})
     }
    }
   }}
  }
  compose.waitForIdle()
  // Wait for background atlas decode and compose publication before capture.
  Thread.sleep(300)
  compose.waitForIdle()
  compose.onNodeWithText("Dekor").assertIsSelected()
  compose.onNodeWithText("Dekor üçün Bazara keç").assertIsDisplayed().performClick()
  assertEquals(1,market)
  capture("decor-$name-first");capture("decor-$name")
  if(insets) {
   compose.onNodeWithText(catalog.items.first().name).performScrollTo().assertIsDisplayed()
   capture("decor-long-$name")
   compose.onNodeWithContentDescription("Dekor nüsxələri").performScrollToIndex(0)
  }
  compose.onNodeWithText("Skamya").performScrollTo().performClick()
  compose.onNodeWithText("Tək skamya").performScrollTo().assertIsDisplayed()
  compose.onNodeWithText("Tək ağac").assertDoesNotExist()
  compose.onNodeWithContentDescription("Dekor nüsxələri").performScrollToIndex(0)
  compose.onNodeWithText("Yol").performScrollTo().performClick()
  compose.onNodeWithText("Qısa düz yol").performScrollTo().assertIsDisplayed()
  compose.onNodeWithContentDescription("Dekor nüsxələri").performScrollToIndex(3)
  compose.onNodeWithText("Daha çox").performScrollTo().performClick();assertEquals(1,more)
  capture("decor-road-$name")
  compose.onNodeWithContentDescription("Dekor nüsxələri").performScrollToIndex(0)
  compose.onNodeWithContentDescription("Yenilə").performScrollTo().performClick();assertEquals(1,refresh)
  compose.runOnIdle {data=emptyList();failed=true;cached=true}
  compose.onNodeWithContentDescription("Dekor nüsxələri").performScrollToIndex(3)
  compose.onNodeWithText("İnventar yenilənmədi.").performScrollTo().assertIsDisplayed()
  compose.onNodeWithText("Yenidən cəhd et").performScrollTo().performClick();assertEquals(2,refresh)
  capture("decor-error-$name")
  compose.runOnIdle {failed=false}
  compose.onNodeWithText("Hələ dekor nüsxən yoxdur.").performScrollTo().assertIsDisplayed()
  compose.runOnIdle {loading=true}
  compose.onNodeWithContentDescription("Dekor yüklənir").performScrollTo().assertExists()
  assertTrue(copies.filter {decorMatches(it,"tree")}.size==3)
  assertEquals(copies,copies.filter {decorMatches(it,"all")})
 }
}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class DecorRegularTest:DecorHost() {@Test fun screen()=render(1f,"regular")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class DecorSmallTest:DecorHost() {@Test fun screen()=render(1f,"small")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class DecorRegularFontTest:DecorHost() {@Test fun screen()=render(2f,"regular-font2")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class DecorSmallFontTest:DecorHost() {@Test fun screen()=render(2f,"small-font2")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class DecorInsetsTest:DecorHost() {@Test fun screen()=render(2f,"insets-font2",true)}
