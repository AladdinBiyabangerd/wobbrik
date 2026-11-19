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

/** Production settings and real DataStore route; synthetic host, no server writes. */
abstract class ProfileCollectionOverviewHost {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(settings:Boolean,font:Float,name:String,legal:Boolean=false,insets:Boolean=false) {
  if(legal) compose.runOnUiThread {
   val resources=compose.activity.resources
   val config=android.content.res.Configuration(resources.configuration).apply {fontScale=font}
   @Suppress("DEPRECATION")
   resources.updateConfiguration(config,resources.displayMetrics)
  }
  var edits=0
  var page=""
  compose.setContent { val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme(colorScheme = darkColorScheme(primary = GameTokens.Accent, onPrimary = GameTokens.Background,
      surface = GameTokens.Surface, onSurface = GameTokens.Text, background = GameTokens.Background, onBackground = GameTokens.Text)) {
    Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
     MobileMenuShell("account",if(insets) Long.MAX_VALUE else 246,{}, {}) {
      ProfileFrame("Profil",{},246) {
       ProfileCollectionHeading {page=it}
       Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
        ProfileOverview(appbackend.wobbrik.game.PublicProfile(1,if(insets) "Uzun oyunçu adı burada" else "Aladdin","builder"),"3",96,"40",
         progress={ AccountXpProgress(java.math.BigInteger.valueOf(96),java.math.BigInteger.valueOf(150),"3") },
         showEdit=font<=1f && !mobileUiSizing().compact,enabled=true,onEdit={edits++})
        if(font<=1f && !mobileUiSizing().compact) ProfileNavbar({}, {page=it})
       }
       if(font>1f || mobileUiSizing().compact) ProfileAction("Dəyiş",modifier=Modifier.testTag("profile-edit")) {edits++}
       if(font>1f || mobileUiSizing().compact) ProfileNavbar({}, {page=it})
      }
     }
    }
    if(legal) LegalDocumentsDialog {}
   }
   }
  }
  compose.waitForIdle()
  compose.waitUntil(10000) {compose.onAllNodesWithContentDescription("Wobbrik").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed()
  if(name=="profile-regular") compose.onNodeWithTag("profile-edit").performScrollTo()
  compose.onNodeWithTag("profile-edit").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
  compose.onNodeWithTag("profile-edit").performClick()
  org.junit.Assert.assertEquals(1,edits)
  compose.onNodeWithText("Parametrlər").performClick()
  org.junit.Assert.assertEquals("settings",page)
  if(font<=1f && !compose.activity.resources.configuration.screenWidthDp.let {it<360}) compose.onNodeWithText("Rekordlar").performScrollTo()
  compose.onNodeWithText("Rekordlar").performClick()
  org.junit.Assert.assertEquals("leaderboard",page)
  compose.onNodeWithTag("profile-ribbon").performScrollTo()
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!.decorView else compose.activity.window.decorView)
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
   val view=if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!.decorView else compose.activity.window.decorView
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
   val view=if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!.decorView else compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   var result = -1
   android.view.PixelCopy.request(if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window,bitmap,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   org.junit.Assert.assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File((System.getProperty("wobbrik.render.output") ?: "/private/tmp/wobbrik-profile-native")).apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   bitmap.recycle()
  }
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionRegularTest:ProfileCollectionOverviewHost() {@Test fun preview()=render(true,1f,"profile-regular",insets=false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionSmallTest:ProfileCollectionOverviewHost() {@Test fun preview()=render(true,1f,"profile-small",insets=false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionFontTest:ProfileCollectionOverviewHost() {@Test fun preview()=render(true,2f,"profile-font",insets=false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionRegularFontTest:ProfileCollectionOverviewHost() {@Test fun preview()=render(true,2f,"profile-regularfont",insets=false)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileCollectionInsetsTest:ProfileCollectionOverviewHost() {@Test fun preview()=render(true,2f,"profile-insets",insets=true)}
