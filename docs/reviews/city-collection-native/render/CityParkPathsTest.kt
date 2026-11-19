package com.aladdin.wobbrik

import android.app.Application
import android.graphics.PathMeasure
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asAndroidPath
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
class CityParkPathsTest {
 private fun building(id:String,x:Long,y:Long) = requireNotNull(CityBuildingProjection.building(
  CitySceneBuilding(OwnedBuilding(id,1,1,4,20,24,emptyMap(),false,
   AcceptedCityGeometry(CityFloorGeometry(listOf(0L)),"a",1,"o","wobbrik-v1","wobbrik-structural-v1")),CityPosition(x,y,0))))
 @Test fun joinsEntrancesAndAvoidsHoleWithBoundedSharedEdges() {
  val outer=Path().apply { addRect(Rect(-300f,-300f,300f,300f)) }
  val hole=Path().apply { addRect(Rect(-30f,-25f,30f,25f)) }
  val surface=Path.combine(PathOperation.Difference,outer,hole)
  val buildings=listOf(building("a",-90000,90000),building("b",90000,-90000))
  val result=cityParkPaths(surface,buildings,emptyList())
  assertEquals(2,result.paths.size);assertTrue(result.cells<=65536)
  assertTrue(result.edges>30);assertTrue(result.edges<result.cells)
  val ends=mutableListOf<Pair<Float,Float>>()
  result.paths.forEach { path ->
   val measure=PathMeasure(path.asAndroidPath(),false);val p=FloatArray(2)
   var d=0f
   while(d<=measure.length) {
    measure.getPosTan(d,p,null)
    assertFalse("path crosses hole at ${p.toList()}",p[0]>-30 && p[0]<30 && p[1]>-25 && p[1]<25)
    buildings.forEach { b -> assertTrue(kotlin.math.abs(p[0]-b.foundation.x)/100 + kotlin.math.abs(p[1]-b.foundation.y)/50 >= 1) }
    d+=2
   }
   measure.getPosTan(measure.length,p,null);ends+=p[0] to p[1]
  }
  assertEquals(ends[0],ends[1])
 }
 @Test fun disconnectedParcelsDoNotAcquireBridgeAndHugeBoundsAreCapped() {
  val surface=Path().apply { addRect(Rect(-300f,-150f,-80f,150f));addRect(Rect(80f,-150f,300f,150f)) }
  val result=cityParkPaths(surface,listOf(building("a",-90000,90000),building("b",90000,-90000)),emptyList())
  assertEquals(2,result.paths.size);assertEquals(0,result.edges)
  val huge=Path().apply { addRect(Rect(-1000000f,-1000000f,1000000f,1000000f)) }
  val sparse=cityParkPaths(huge,listOf(building("a",0,0)),emptyList())
  assertEquals(0,sparse.cells);assertTrue(sparse.paths.isEmpty())
 }
 @Test fun waterUsesFrontCoastRegardlessOfBooleanUnionWinding() {
  val surface=Path().apply { addRect(Rect(0f,0f,100f,100f)) }
  val strips=listOf(
   androidx.compose.ui.geometry.Offset(0f,0f) to androidx.compose.ui.geometry.Offset(100f,0f),
   androidx.compose.ui.geometry.Offset(100f,100f) to androidx.compose.ui.geometry.Offset(0f,100f))
  val forward=cityWaterReflections(surface,CityShore(strips,emptyList()))
  val reverse=cityWaterReflections(surface,CityShore(strips.map { it.second to it.first },emptyList()))
  assertEquals(1,forward.size);assertEquals(1,reverse.size)
  assertEquals(androidx.compose.ui.geometry.Offset(50f,100f),forward.single().midpoint)
  assertEquals(forward.single().midpoint,reverse.single().midpoint)
 }

}
