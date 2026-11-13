package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Canvas
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
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

/** Explicitly synthetic render fixtures; production loads only CitySceneRepository. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CityRenderTest {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 private fun building(id: Int, x: Long, y: Long, turns: Int) = CitySceneBuilding(
  OwnedBuilding("b$id", 1, 10, 40, 20, 24,
   FinishCosmetics.categories.associateWith { "base_${id}_$it" }, false,
   AcceptedCityGeometry(CityFloorGeometry(List(10) { (it * if (id == 2) -5500L else 4500L) }),
    "attempt$id", 1, "op$id", "wobbrik-v1", "wobbrik-structural-v1")), CityPosition(x,y,turns))
 private val scene = CityScene(CityMembership(1,120.toBigInteger(), mapOf("b1" to 1,"b2" to 1,"b3" to 1),null),
  listOf(CityParcel("base",CityPosition(0,0,0))),
  listOf(building(1,55000,55000,0),building(2,275000,165000,1),building(3,385000,55000,3)),emptyList())
 private fun render(font: Float, name: String, data: CityScene? = scene, failed: Boolean = false) {
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
    CityContent(data,failed,{}, {}, {})
   }
  }
  compose.waitForIdle()
  compose.onNodeWithContentDescription("Ana ekran").assertIsDisplayed()
  compose.onNodeWithText("İnventar").assertIsDisplayed()
  val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
  compose.onNodeWithText("İnventar", useUnmergedTree = true).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
  org.junit.Assert.assertTrue("Inventory label must fit: " + layouts.map { "${it.size} right=${it.getLineRight(0)}" }, layouts.isNotEmpty() && layouts.all { layout -> (0 until layout.lineCount).all { line -> layout.getLineRight(line) <= layout.size.width + 1 && layout.getLineLeft(line) >= -1 } })
  capture(name)
  if (data != null && data.buildings.isNotEmpty()) {
   val actions = compose.onNodeWithContentDescription("Bina b1:", substring = true)
    .fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
   compose.runOnIdle { check(actions.first().action()) }
   compose.waitForIdle()
   compose.onNodeWithText("40 sakin",substring=true).assertIsDisplayed()
   capture(name+"-focus")
  }
 }
 private fun capture(name: String) {
  val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/city-chrome-visual/city-scene-visual/$name.png")
  file.parentFile!!.mkdirs()
  compose.runOnUiThread {
   val view=compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(Canvas(bitmap)); file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
  }
 }
 @Test fun homeEntry() {
  var destination = "home"
  compose.setContent { HomeScreen(GameSnapshot("wobbrik-v1",1,0,0,null),false,{}, { destination=it }) }
  compose.onNodeWithText("Şəhərim").performScrollTo().assertIsEnabled()
  capture("home-city-entry")
  compose.onNodeWithText("Şəhərim").performClick()
  org.junit.Assert.assertEquals("city",destination)
 }
 @Test fun small()=render(1f,"city-360-font1")
 @Test fun largeFont()=render(2f,"city-360-font2")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normal()=render(1f,"city-390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun narrowLargeFont()=render(2f,"city-320-font2")
 @Test fun navigationAndView() {
  var home = 0; var inventory = 0
  compose.setContent { CityContent(scene,false,{}, { home++ }, { inventory++ }) }
  compose.onNodeWithText("Bazar").assertIsNotEnabled()
  compose.onNodeWithText("İnventar").performClick()
  compose.onNodeWithContentDescription("Ana ekran").performClick()
  org.junit.Assert.assertEquals(1,home)
  org.junit.Assert.assertEquals(1,inventory)
  compose.onNodeWithText("Bax").performClick()
  compose.onNodeWithText("Bina görünüşü").assertIsDisplayed()
  compose.onNodeWithText("Geri").performClick()
  compose.onNodeWithText("Binaya bax").assertIsDisplayed()
 }
 @Test fun loading()=render(2f,"city-loading",null)
 @Test fun empty()=render(1f,"city-empty",scene.copy(buildings=emptyList()))
 @Test fun error()=render(2f,"city-error",null,true)
}
