package com.aladdin.wobbrik

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ColdArtBaselineTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun initialBundledArtComposition() {
  compose.setContent { MaterialTheme {
   val start=System.nanoTime()
   val atlases=rememberGameAtlases();val windows=rememberGameWindowArt();val cosmetics=rememberCosmeticArt()
   val elapsed=System.nanoTime()-start
   SideEffect {println("COLD-ART mainThread=${android.os.Looper.myLooper()==android.os.Looper.getMainLooper()} compositionMs=${elapsed/1_000_000} atlases=${atlases.size} windows=${windows.size} cosmetics=${cosmetics.size}")}
  }}
  compose.waitForIdle()
 }
}
