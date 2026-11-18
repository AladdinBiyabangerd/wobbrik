package com.aladdin.wobbrik

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
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
class InventoryGardenDecodeTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun gardenResourceCost() {
  compose.setContent {
   val start=System.nanoTime()
   val image=ImageBitmap.imageResource(R.drawable.inventory_garden)
   val elapsed=(System.nanoTime()-start)/1_000_000
   SideEffect {println("GARDEN main=${android.os.Looper.myLooper()==android.os.Looper.getMainLooper()} wallMs=$elapsed rgbaBytes=${image.asAndroidBitmap().allocationByteCount}")}
  }
  compose.waitForIdle()
 }
}
