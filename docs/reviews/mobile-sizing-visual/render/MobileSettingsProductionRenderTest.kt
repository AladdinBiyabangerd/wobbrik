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
abstract class MobileSettingsProductionHost {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(settings:Boolean,font:Float,name:String,legal:Boolean=false,insets:Boolean=false) {
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
    Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
     MobileMenuShell("settings",if(insets) Long.MAX_VALUE else 246,{}, {}) { SettingsScreen(DevicePreferences(),false,null, { _,_ -> }, {}, {}, {}, {}) }
    }
    if(legal) LegalDocumentsDialog {}
   }
   }
  }
  compose.waitForIdle()
  if(!legal) {
   compose.onNodeWithText("Musiqi").assertIsDisplayed()
   compose.onNodeWithContentDescription("Oyunu başlat").assertIsDisplayed()
   compose.onNodeWithText("Məxfilik").performScrollTo().assertIsDisplayed()
   compose.onNodeWithText("Musiqi").performScrollTo()
   compose.onNodeWithText("Səs effektləri").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { action ->
    val results=mutableListOf<androidx.compose.ui.text.TextLayoutResult>();action(results)
    org.junit.Assert.assertTrue(results.joinToString { "lines=${it.lineCount}, overflow=${it.hasVisualOverflow}, size=${it.size}, text=${it.layoutInput.text}" }, results.all { layout ->
     (0 until layout.lineCount).all { line ->
      val end=layout.getLineEnd(line,true)
      val completeWord=end==layout.layoutInput.text.length || layout.layoutInput.text[end-1].isWhitespace() || layout.layoutInput.text[end].isWhitespace()
      completeWord && layout.getLineRight(line)<=layout.size.width+1f && !layout.isLineEllipsized(line)
     }
    })
   }
  }
  else compose.onNodeWithText("Bağla").assertIsDisplayed()
  if(!legal) compose.onNodeWithText("SƏS VƏ HİSS").performScrollTo()
  if(!legal) compose.onNodeWithContentDescription("Geri").assertIsDisplayed().assertWidthIsAtLeast(GameTokens.Target).assertHeightIsAtLeast(GameTokens.Target)
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!.decorView else compose.activity.window.decorView)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val view=if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!.decorView else compose.activity.window.decorView
   val warmup=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(AndroidCanvas(warmup));warmup.recycle();view.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val view=if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!.decorView else compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   var result = -1
   android.view.PixelCopy.request(if(legal) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window,bitmap,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   org.junit.Assert.assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File((System.getProperty("wobbrik.render.output") ?: "/private/tmp/wobbrik-settings-native")).apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   bitmap.recycle()
  }
 }
}


@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileSettingsRegularTest:MobileSettingsProductionHost() {@Test fun preview()=render(true,1f,"settings-390")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileSettingsSmallTest:MobileSettingsProductionHost() {@Test fun preview()=render(true,1f,"settings-320")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileSettingsFontTest:MobileSettingsProductionHost() {@Test fun preview()=render(true,2f,"settings-font2")}

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileSettingsBehaviorTest {
 @Test fun loadingAndBusyPreventWrites() {
  var calls=0
  var prefs by mutableStateOf<DevicePreferences?>(null)
  var busy by mutableStateOf(false)
  // This test uses its own host so loading and busy can change without replacing content.
  compose.setContent { SettingsScreen(prefs,busy,null,{_,_ -> calls++},{},{},{},{}) }
  compose.onNodeWithText("Musiqi").assertIsNotEnabled().performClick()
  org.junit.Assert.assertEquals(0,calls)
  compose.runOnIdle {prefs=DevicePreferences();busy=true}
  compose.onNodeWithText("Musiqi").assertIsNotEnabled().performClick()
  org.junit.Assert.assertEquals(0,calls)
  compose.runOnIdle {busy=false}
  compose.onNodeWithText("Musiqi").assertIsEnabled().performClick()
  org.junit.Assert.assertEquals(1,calls)
 }

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
class MobileSettingsRouteTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun realRouteSavesAndOpensLegalDocuments() {
  val settings=DeviceSettings(compose.activity)
  kotlinx.coroutines.runBlocking { settings.set(FeedbackSetting.Music,true) }
  var back=false
  compose.setContent { MobileMenuShell("settings",246,{}, {}) { SettingsRoute(settings) {back=true} } }
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
class MobileSettingsLegalRegularTest:MobileSettingsProductionHost() {@Test fun preview()=render(true,1f,"legal-390",true)}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileSettingsLegalFontTest:MobileSettingsProductionHost() {@Test fun preview()=render(true,2f,"legal-font2",true)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileSettingsInsetsTest:MobileSettingsProductionHost() {@Test fun preview()=render(true,2f,"settings-insets",insets=true)}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileSettingsRegularFontTest:MobileSettingsProductionHost() {@Test fun preview()=render(true,2f,"settings-390-font2")}
