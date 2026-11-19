package com.aladdin.wobbrik

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.test.core.app.ApplicationProvider
import appbackend.wobbrik.game.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CityIllustrationTest {
 @Test fun interiorPlantingFollowsGrowthAndLeavesWaterAndParkOpen() {
  fun garden(size:Float):Pair<androidx.compose.ui.graphics.Path,CityIllustratedGarden> {
   val land=androidx.compose.ui.graphics.Path().apply {
    addRect(androidx.compose.ui.geometry.Rect(0f,0f,size,size))
   }
   val hole=androidx.compose.ui.graphics.Path().apply { addRect(androidx.compose.ui.geometry.Rect(300f,300f,500f,500f)) }
   val surface=androidx.compose.ui.graphics.Path.combine(androidx.compose.ui.graphics.PathOperation.Difference,land,hole)
   return surface to cityIllustratedGarden(emptyList(),CityShore(emptyList(),emptyList()),emptyList(),surface)
  }
  val (_,small)=garden(800f);val (surface,grown)=garden(1600f)
  assertTrue(small.trees.isNotEmpty());assertTrue(grown.trees.any { it.anchor.x>800f || it.anchor.y>800f })
  fun region(path:androidx.compose.ui.graphics.Path)=android.graphics.Region().apply {
   setPath(path.asAndroidPath(),android.graphics.Region(0,0,1600,1600))
  }
  val land=region(surface);val park=region(grown.park)
  grown.trees.forEach { val p=it.anchor
   assertTrue(land.contains(p.x.toInt(),p.y.toInt()));assertFalse(park.contains(p.x.toInt(),p.y.toInt()))
  }
  assertTrue(grown.trees.size<=128)
 }
 @Test fun shippedSheetHasRealAlphaAndSolidMaterialSamples() {
  val art=loadCityIllustrationArt(ApplicationProvider.getApplicationContext())
  val bitmap=art.rocks.image.asAndroidBitmap()
  assertEquals(0,android.graphics.Color.alpha(bitmap.getPixel(0,0)))
  assertEquals(0,android.graphics.Color.alpha(bitmap.getPixel(510,510)))
  for(material in listOf(art.brick,art.plaster)) {
   for(x in listOf(0,material.size.width/2,material.size.width-1))
    for(y in listOf(0,material.size.height/2,material.size.height-1))
     assertTrue(android.graphics.Color.alpha(bitmap.getPixel(material.origin.x+x,material.origin.y+y))>=240)
  }
 }
 @Test fun largeGardenIsBoundedAndDoesNotInventBuildingsOrOccupyTheirFootprints() {
  val sources=(0 until 1000).map { index ->
   CitySceneBuilding(OwnedBuilding("b$index",1,10,40,20,24,emptyMap(),false,
    AcceptedCityGeometry(CityFloorGeometry(List(10) { it*3500L }),"a",1,"o","wobbrik-v1","wobbrik-structural-v1")),
    CityPosition(index*220000L,0,0))
  }
  val buildings=sources.map { requireNotNull(CityBuildingProjection.building(it)) }
  val shore=CityShore(emptyList(),(0 until 10000).map { Offset(it*48f,100f) } + buildings.map { Offset(it.foundation.x.toFloat(),it.foundation.y.toFloat()) })
  val garden=cityIllustratedGarden(buildings,shore,emptyList(), androidx.compose.ui.graphics.Path().apply { addRect(androidx.compose.ui.geometry.Rect(-200f,-230000f,230000f,230000f)) })
  assertTrue(garden.walks.size<=1000);assertEquals(2000,garden.beds.size)
  assertTrue(garden.trees.size<=128)
  assertTrue(garden.trees.isNotEmpty())
  garden.trees.forEach { tree -> assertTrue(buildings.none {
   kotlin.math.abs(it.foundation.x-tree.anchor.x)<115 && kotlin.math.abs(it.foundation.y-tree.anchor.y)<75
  }) }
  assertEquals(sources,buildings.map { it.source })
 }
}
