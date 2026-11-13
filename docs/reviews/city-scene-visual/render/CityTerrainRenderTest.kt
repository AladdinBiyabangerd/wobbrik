package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Region
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.game.*
import androidx.compose.ui.graphics.asAndroidPath
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Synthetic account/placements, production Canvas and production assets. No live backend. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CityTerrainRenderTest {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 private fun building(id:Int,x:Long,y:Long)=CitySceneBuilding(OwnedBuilding("b$id",1,10,40,20,24,
  FinishCosmetics.categories.associateWith { "base_${id}_$it" },false,
  AcceptedCityGeometry(CityFloorGeometry(List(10) { it*3500L }),"a$id",1,"o$id","wobbrik-v1","wobbrik-structural-v1")),CityPosition(x,y,0))
 private fun decor(id:String,x:Long,y:Long,turn:Int=0,copy:String=id)=CitySceneDecoration(DecorationCopy(copy,id),CityPosition(x,y,turn))
 private val base=listOf(CityParcel("base",CityPosition(0,0,0)),CityParcel("land_1",CityPosition(440000,0,0)))
 private val scene=CityScene(CityMembership(1,80.toBigInteger(),mapOf("b1" to 1,"b2" to 1),null),base,
  listOf(building(1,80000,65000),building(2,300000,150000)),listOf(
   decor("decor_tree_1",190000,50000),decor("decor_tree_2",500000,170000),decor("decor_tree_3",590000,60000),
   decor("decor_bench_1",190000,160000),decor("decor_streetlight_2",390000,60000),decor("decor_road_1",480000,70000,1)))
 private fun render(data:CityScene,font:Float,name:String) {
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) { CityContent(data,false,{}, {}, {}) }
  }
  compose.waitForIdle()
  compose.onNodeWithText("Ana ekran").assertIsDisplayed()
  compose.onNodeWithText("İnventar").assertIsDisplayed()
  capture(name)
 }
 private fun capture(name:String) {
  val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/city-terrain-visual/$name.png")
  file.parentFile!!.mkdirs()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(b));file.outputStream().use { b.compress(Bitmap.CompressFormat.PNG,100,it) }
  }
 }
 @Test fun small()=render(scene,1f,"360-font1")
 @Test fun largeFont()=render(scene,2f,"360-font2")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normal()=render(scene,1f,"390-font1")
 @Test fun landWithoutBuildings()=render(scene.copy(buildings=emptyList()),2f,"land-decor-no-buildings")
 @Test fun noPlacedDecor()=render(scene.copy(buildings=emptyList(),decorations=emptyList()),1f,"land-only")
 @Test fun focusAndReturn() {
  render(scene,1f,"overview-before-focus")
  compose.onNodeWithText("Binaya bax").performClick()
  compose.waitForIdle()
  capture("focus")
  compose.onAllNodesWithText("Şəhər görünüşü")[1].performClick()
  compose.waitForIdle()
  capture("returned-overview")
 }
 @Test fun allDecorAndRoadTurns() {
  val ids=listOf("tree","bench","streetlight","road").flatMap { category -> (1..3).map { "decor_${category}_$it" } }
  val copies=ids.mapIndexed { i,id -> decor(id,(i%4)*160000L+60000,(i/4)*140000L+60000) } +
   (0..3).map { turn -> decor("decor_road_2",turn*160000L+60000,490000,turn,"road-turn-$turn") }
  render(scene.copy(parcels=base+CityParcel("land_5",CityPosition(0,220000,0))+CityParcel("land_3",CityPosition(440000,220000,0))+CityParcel("land_4",CityPosition(0,440000,0))+CityParcel("land_2",CityPosition(440000,440000,0)),
   buildings=emptyList(),decorations=copies),1f,"catalog-and-road-turns")
 }
 @Test fun allParcelsIncludingRotatedL() {
  val parcels=listOf(CityParcel("base",CityPosition(0,0,0)),CityParcel("land_1",CityPosition(440000,0,0)),
   CityParcel("land_2",CityPosition(330000,440000,2)),CityParcel("land_3",CityPosition(660000,0,0)),
   CityParcel("land_4",CityPosition(330000,220000,0)),CityParcel("land_5",CityPosition(770000,220000,0)))
  render(scene.copy(parcels=parcels,buildings=emptyList(),decorations=emptyList()),1f,"all-parcels")
 }
 @Test fun unionRetainsLNotchAndJoinsParcels() {
  val parcels=listOf(CityParcel("base",CityPosition(0,0,0)),CityParcel("land_2",CityPosition(440000,0,0)))
  val path=citySurface(CityTerrainGeometry.contours(parcels))
  val region=Region().apply { setPath(path.asAndroidPath(),Region(-5000,-5000,5000,5000)) }
  fun contains(x:Double,y:Double):Boolean { val p=CityBuildingProjection.project(x,y);return region.contains(p.x.toInt(),p.y.toInt()) }
  assertTrue(contains(439.0,100.0)); assertTrue(contains(441.0,100.0))
  assertTrue(contains(700.0,50.0)); assertFalse(contains(740.0,200.0))
 }
}
