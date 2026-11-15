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

/** Review-only fixture. No production navigation, settings persistence or server calls. */
abstract class SettingsImplementationHost {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(settings:Boolean,font:Float,name:String,legal:Boolean=false) {
  if(legal) compose.runOnUiThread {
   val resources=compose.activity.resources
   val config=android.content.res.Configuration(resources.configuration).apply {fontScale=font}
   @Suppress("DEPRECATION")
   resources.updateConfiguration(config,resources.displayMetrics)
  }
  compose.setContent { val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme(colorScheme = darkColorScheme(primary = GameTokens.Accent, onPrimary = GameTokens.Background,
      surface = GameTokens.Surface, onSurface = GameTokens.Text, background = GameTokens.Background, onBackground = GameTokens.Text)) {
    SettingsScreen(DevicePreferences(),false,null, { _,_ -> }, {}, {}, {}, {})
    if(legal) LegalDocumentsDialog {}
   }
   }
  }
  compose.waitForIdle()
  if(!legal) compose.onNodeWithText("Musiqi").assertIsDisplayed()
  else compose.onNodeWithText("Bağla").assertIsDisplayed()
  if(!legal) compose.onNodeWithContentDescription("Geri").assertIsDisplayed().assertWidthIsAtLeast(GameTokens.Target).assertHeightIsAtLeast(GameTokens.Target)
  compose.runOnUiThread {
   val view=if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!.decorView else compose.activity.window.decorView
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(view)
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(AndroidCanvas(bitmap))
   val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/settings-implementation-visual").apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   bitmap.recycle()
  }
 }
}


@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsRegularTest:SettingsImplementationHost() {@Test fun preview()=render(true,1f,"settings-390")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsSmallTest:SettingsImplementationHost() {@Test fun preview()=render(true,1f,"settings-360")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsFontTest:SettingsImplementationHost() {@Test fun preview()=render(true,2f,"settings-font2")}

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsBehaviorTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun switchStateBackAndPersistence() {
  val settings=DeviceSettings(compose.activity)
  kotlinx.coroutines.runBlocking {
   settings.set(FeedbackSetting.Sound,true)
   settings.set(FeedbackSetting.Vibration,false)
   settings.set(FeedbackSetting.Music,true)
  }
  var back=false
  compose.setContent {
   var preferences by remember { mutableStateOf(DevicePreferences()) }
   SettingsScreen(preferences,false,null,{ setting,value ->
    kotlinx.coroutines.runBlocking {settings.set(setting,value)}
    preferences=when(setting) {
     FeedbackSetting.Music -> preferences.copy(music=value)
     FeedbackSetting.Sound -> preferences.copy(sound=value)
     FeedbackSetting.Vibration -> preferences.copy(vibration=value)
    }
   },{},{},{},{back=true})
  }
  compose.onNodeWithText("Musiqi").assertIsOn().performClick().assertIsOff()
  compose.onNodeWithText("Titrəmə").assertIsOff().performClick().assertIsOn()
  kotlinx.coroutines.runBlocking {
   val restored=DeviceSettings(compose.activity).values.first()
   org.junit.Assert.assertFalse(restored.music)
   org.junit.Assert.assertTrue(restored.sound)
   org.junit.Assert.assertTrue(restored.vibration)
  }
  compose.onNodeWithContentDescription("Geri").performClick()
  org.junit.Assert.assertTrue(back)
 }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsRouteTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun realRouteSavesAndOpensLegalDocuments() {
  val settings=DeviceSettings(compose.activity)
  kotlinx.coroutines.runBlocking { settings.set(FeedbackSetting.Music,true) }
  var back=false
  compose.setContent { SettingsRoute(settings) {back=true} }
  compose.waitUntil(5000) { compose.onAllNodes(isToggleable() and isEnabled()).fetchSemanticsNodes().size==3 }
  compose.onNodeWithText("Musiqi").performClick()
  compose.waitUntil(5000) {
   kotlinx.coroutines.runBlocking { !settings.values.first().music }
  }
  compose.onNodeWithText("Musiqi").assertIsOff()
  compose.onNodeWithText("Dil").performClick()
  compose.onNodeWithText("Hazırda yalnız Azərbaycan dili mövcuddur.").assertIsDisplayed()
  compose.onNodeWithText("Bağla").performClick()
  compose.onNodeWithText("Dəstək").performClick()
  val mail=org.robolectric.Shadows.shadowOf(compose.activity).nextStartedActivity
  org.junit.Assert.assertEquals(android.content.Intent.ACTION_SENDTO,mail.action)
  org.junit.Assert.assertEquals("mailto:aladdinbiyabangerd@gmail.com",mail.dataString)
  compose.onNodeWithText("Məxfilik").performClick()
  compose.onNodeWithText("Məxfilik siyasəti").assertIsDisplayed()
  compose.onNodeWithText("Sənədi seç").performClick()
  compose.onNodeWithText("Xidmət şərtləri").performClick()
  compose.onNodeWithText("Xidmət şərtləri").assertIsDisplayed()
  compose.onNodeWithText("Sənədi seç").performClick()
  compose.onNodeWithText("Wobbrik hesabını sil").performClick()
  compose.onNodeWithText("Wobbrik hesabını sil").assertIsDisplayed()
  compose.onNodeWithText("Bağla").performClick()
  compose.onNodeWithContentDescription("Geri").performClick()
  org.junit.Assert.assertTrue(back)
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class LegalRegularTest:SettingsImplementationHost() {@Test fun preview()=render(true,1f,"legal-390",true)}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class LegalFontTest:SettingsImplementationHost() {@Test fun preview()=render(true,2f,"legal-font2",true)}
