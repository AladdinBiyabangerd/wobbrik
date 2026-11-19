package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.io.File
import kotlinx.coroutines.flow.first
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

abstract class ProfileCollectionEditorHost {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(font:Float,name:String,insets:Boolean=false) {
  var saved=0; var cancelled=0; var applied=""; var changed=""
  var picker by mutableStateOf(false)
  var editable by mutableStateOf(true)
  compose.setContent { val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme(colorScheme=darkColorScheme(primary=GameTokens.Accent,onPrimary=GameTokens.Background,surface=GameTokens.Surface,onSurface=GameTokens.Text)) {
     Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
      MobileMenuShell("account",246,{}, {}) {
       ProfileEditView(if(insets) "Uzun oyunçu adı" else "Aladdin","builder",editable,editable,editable,"Yadda saxla",
        {changed=it},{picker=true},{cancelled++},{saved++})
      }
      if(picker) CompositionLocalProvider(LocalMobileMenuShell provides true) { ProfileAvatarPicker(listOf("builder","pilot","architect"),"builder",{picker=false},{applied=it;picker=false}) }
     }
    }
   }
  }
  compose.waitForIdle()
  compose.waitUntil(10000) {compose.onAllNodesWithContentDescription("Wobbrik").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithTag("profile-save").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target).performClick()
  org.junit.Assert.assertEquals(1,saved)
  compose.onNodeWithTag("profile-name").performScrollTo().performTextReplacement("Pilot")
  org.junit.Assert.assertEquals("Pilot",changed)
  compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("İnşaatçı avatarı").fetchSemanticsNodes().isNotEmpty() }
  compose.onNodeWithContentDescription("İnşaatçı avatarı").performScrollTo()
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate((if(picker) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window).decorView)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  repeat(3) {
   compose.runOnUiThread {
    fun redraw(v:android.view.View) {
     v.invalidate()
     if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
     if(v is android.view.ViewGroup) for(i in 0 until v.childCount) redraw(v.getChildAt(i))
    }
    redraw(compose.activity.window.decorView)
   }
   compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  }
  compose.runOnUiThread {
   val view=(if(picker) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window).decorView
   val warmup=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(AndroidCanvas(warmup));warmup.recycle();view.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  repeat(3) {
   compose.runOnUiThread {
    fun redraw(v:android.view.View) {
     v.invalidate()
     if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
     if(v is android.view.ViewGroup) for(i in 0 until v.childCount) redraw(v.getChildAt(i))
    }
    redraw(compose.activity.window.decorView)
   }
   compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  }
  compose.runOnUiThread {
   val view=(if(picker) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window).decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   var result = -1
   android.view.PixelCopy.request(if(picker) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window,bitmap,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   org.junit.Assert.assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File((System.getProperty("wobbrik.render.output") ?: "/private/tmp/wobbrik-profile-native")).apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   bitmap.recycle()
  }
  compose.onNodeWithTag("profile-avatar").performScrollTo().performClick()
  compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Pilot avatarı").fetchSemanticsNodes().isNotEmpty() }
  compose.onNodeWithTag("avatar-pilot").performClick().assertIsSelected()
  org.junit.Assert.assertEquals("",applied)
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate((if(picker) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window).decorView)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  repeat(3) {
   compose.runOnUiThread {
    fun redraw(v:android.view.View) {
     v.invalidate()
     if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
     if(v is android.view.ViewGroup) for(i in 0 until v.childCount) redraw(v.getChildAt(i))
    }
    redraw(compose.activity.window.decorView)
   }
   compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  }
  compose.runOnUiThread {
   val view=(if(picker) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window).decorView
   val warmup=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(AndroidCanvas(warmup));warmup.recycle();view.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  repeat(3) {
   compose.runOnUiThread {
    fun redraw(v:android.view.View) {
     v.invalidate()
     if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
     if(v is android.view.ViewGroup) for(i in 0 until v.childCount) redraw(v.getChildAt(i))
    }
    redraw(compose.activity.window.decorView)
   }
   compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  }
  compose.runOnUiThread {
   val view=(if(picker) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window).decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   var result = -1
   android.view.PixelCopy.request(if(picker) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window,bitmap,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   org.junit.Assert.assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File((System.getProperty("wobbrik.render.output") ?: "/private/tmp/wobbrik-profile-native")).apply {mkdirs()}
   File(out,"$name-picker.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   bitmap.recycle()
  }
  compose.onNodeWithTag("avatar-apply").assertIsDisplayed().performClick()
  org.junit.Assert.assertEquals("pilot",applied)
  compose.onNodeWithTag("profile-avatar").performClick()
  compose.onNodeWithTag("avatar-architect").performScrollTo().performClick()
  compose.onNodeWithTag("avatar-close").performClick()
  org.junit.Assert.assertEquals("pilot",applied)
  compose.runOnIdle {editable=false}
  compose.onNodeWithTag("profile-save").assertIsNotEnabled()
  compose.onNodeWithTag("profile-name").assertIsNotEnabled()
  compose.onNodeWithTag("profile-avatar").performScrollTo().assertIsNotEnabled()
  compose.onNodeWithTag("profile-cancel").assertIsNotEnabled()
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionEditorRegularTest:ProfileCollectionEditorHost() {@Test fun preview()=render(1f,"editor-regular",false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionEditorSmallTest:ProfileCollectionEditorHost() {@Test fun preview()=render(1f,"editor-small",false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionEditorFontTest:ProfileCollectionEditorHost() {@Test fun preview()=render(2f,"editor-font",false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionEditorRegularFontTest:ProfileCollectionEditorHost() {@Test fun preview()=render(2f,"editor-regularfont",false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionEditorInsetsTest:ProfileCollectionEditorHost() {@Test fun preview()=render(2f,"editor-insets",true)}
