package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.game.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildingWidthRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private fun render(font:Float) {
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
    Column(Modifier.fillMaxSize().background(GameTokens.Background).padding(GameTokens.Gap)) {
     for (count in listOf(1,16,40)) {
      Text("$count mərtəbə",color=GameTokens.Text,fontSize=GameTokens.Body)
      Row(Modifier.weight(1f)) {
       for (offset in listOf(0L,4500L)) {
        val building=OwnedBuilding("width",2,count.toLong(),64,32,38,
         FinishCosmetics.categories.associateWith { "base_3_$it" },false,
         AcceptedCityGeometry(CityFloorGeometry(List(count) { it*offset }),"attempt",1,"operation","wobbrik-v1","wobbrik-structural-v1"))
        BuildingPreview(building,Modifier.weight(1f).fillMaxHeight())
       }
      }
     }
    }
   }
  }
  compose.waitForIdle()
  compose.runOnUiThread {
   val view=compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(Canvas(bitmap))
   val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/building-width-visual/after-font$font.png")
   file.parentFile!!.mkdirs()
   file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
  }
 }
 @Test fun regular()=render(1f)
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun compactLargeText()=render(2f)
}
