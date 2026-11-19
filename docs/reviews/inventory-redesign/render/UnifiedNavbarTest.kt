package com.aladdin.wobbrik

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

abstract class UnifiedNavbarHost {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 protected fun exercise(font:Float) {
  var page by mutableStateOf("home")
  var enabled by mutableStateOf(false)
  var active by mutableStateOf(false)
  var plays=0
  compose.setContent { val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) { MaterialTheme {
    Box(Modifier.fillMaxSize(),contentAlignment=Alignment.BottomCenter) {
     MobileMenuNavbar(page,active,enabled,{plays++},{page=it})
    }
   } }
  }
  compose.onNodeWithContentDescription("Oyunu başlat").assertIsNotEnabled().performTouchInput { click() }
  compose.runOnIdle {assertEquals(0,plays)}
  for((label,route) in listOf("Ana" to "home","Şəhər" to "city","İnventar" to "inventory","Bazar" to "market","Uğurlar" to "achievements","Profil" to "account")) {
   compose.onNodeWithContentDescription(label).assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target).performClick()
   compose.runOnIdle {assertEquals(route,page)}
   compose.onNodeWithContentDescription(label).assertIsSelected()
  }
  compose.runOnIdle {enabled=true;active=true}
  compose.onNodeWithContentDescription("Cəhdə qayıt").assertIsDisplayed().assertIsEnabled().performClick()
  compose.runOnIdle {assertEquals(1,plays)}
 }
}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class UnifiedNavbarRegularTest:UnifiedNavbarHost() {@Test fun destinations()=exercise(1f)}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class UnifiedNavbarSmallFontTest:UnifiedNavbarHost() {@Test fun destinations()=exercise(2f)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class UnifiedNavbarSheetTest {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 private fun checkSheet(expectedDp:Float) {
  compose.setContent { MaterialTheme {
   Box(Modifier.fillMaxSize(),contentAlignment=Alignment.BottomCenter) {
    Box(Modifier.padding(horizontal=GameTokens.Small,vertical=GameTokens.Quarter)) {
     MobileMenuNavbar("market",false,true,{},{})
    }
   }
   MarketDialog("Alış təsdiqi", {}, bottomSheet=true, footer={}) { Text("Qiymət") }
  } }
  compose.onNodeWithText("Alış təsdiqi").assertIsDisplayed()
  compose.runOnIdle {
   val window=requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!!
   assertEquals(android.view.Gravity.BOTTOM,window.attributes.gravity and android.view.Gravity.VERTICAL_GRAVITY_MASK)
   assertEquals(with(compose.density) {expectedDp.toDpForTest().roundToPx()},window.attributes.y)
  }
 }
 private fun Float.toDpForTest() = androidx.compose.ui.unit.Dp(this)
 @Test fun normalDock()=checkSheet(72f)
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun wrappedDock()=checkSheet(112f)
}
