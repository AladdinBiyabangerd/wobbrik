package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Region
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
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
class CityIslandTest {
 private fun region(path:Path)=Region().apply {setPath(path.asAndroidPath(),Region(-10000,-10000,10000,10000))}
 private fun base()=CityParcel("base",CityPosition(0,0,0))
 private fun verify(parcels:List<CityParcel>) {
  val contours=CityTerrainGeometry.contours(parcels)
  val surface=citySurface(contours);val shore=cityShore(surface)
  val precise=android.graphics.Path(surface.asAndroidPath()).apply { transform(android.graphics.Matrix().apply {setScale(10f,10f)}) }
  val land=Region().apply {setPath(precise,Region(-100000,-100000,100000,100000))}
  val park=cityParkSurface(surface,emptyList(),emptyList(),emptyList())
  assertFalse(park.isEmpty)
  assertTrue(Path.combine(PathOperation.Difference,park,surface).isEmpty)
  shore.strips.forEach { (a,b) ->
   val delta=b-a;val mid=(a+b)/2f
   if(delta.getDistance()>2) {
    val n=Offset(-delta.y,delta.x)/delta.getDistance()*.5f
    fun inside(p:Offset)=land.contains((p.x*10).toInt(),(p.y*10).toInt())
    assertFalse("interior parcel seam $a $b ${parcels.map { it.id to it.position }}",inside(mid+n)&&inside(mid-n))
   }
  }
  val cliff=cityCliff(contours,CityIslandStyle.BankDepth)
  assertEquals(surface.getBounds().bottom+48f,cliff.getBounds().bottom,.01f)
  assertEquals(contours,CityTerrainGeometry.contours(parcels))
 }
 @Test fun allExpansionShapesAndRotationsRetainTheirNotchesWithoutInternalBanks() {
  for(id in (1..5).map { "land_$it" }) for(turn in 0..3) {
   val corners=CityTerrainGeometry.land(id).flatMap { r ->
    listOf(r.left to r.bottom,r.right to r.bottom,r.right to r.top,r.left to r.top)
   }.map { (x,y) -> CityTerrainGeometry.world(CityPosition(0,0,turn),x,y) }
   // Every rotated expansion is genuinely adjacent to the base, with no overlap.
   val pos=CityPosition(((440-corners.minOf { it.x })*1000).toLong(),
    (-corners.minOf { it.y }*1000).toLong(),turn)
   verify(listOf(base(),CityParcel(id,pos)))
  }
 }
 @Test fun sixParcelGrowthRebuildsCoastAndParkRatherThanKeepingOldBaseSeam() {
  val parcels=listOf(base(),CityParcel("land_1",CityPosition(440000,0,0)),
   CityParcel("land_2",CityPosition(0,220000,0)),CityParcel("land_3",CityPosition(440000,220000,0)),
   CityParcel("land_4",CityPosition(0,440000,0)),CityParcel("land_5",CityPosition(440000,440000,0)))
  for(n in 1..parcels.size) verify(parcels.take(n))
  val old=citySurface(CityTerrainGeometry.contours(listOf(base())))
  val grown=citySurface(CityTerrainGeometry.contours(parcels))
  val park=cityParkSurface(grown,emptyList(),emptyList(),emptyList())
  assertFalse(Path.combine(PathOperation.Difference,park,old).isEmpty)
  val union=region(grown)
  val p=CityBuildingProjection.project(440.0,100.0)
  assertTrue(union.contains((p.x-2).toInt(),p.y.toInt()));assertTrue(union.contains((p.x+2).toInt(),p.y.toInt()))
 }
 @Test fun parkCannotCoverActualFoundationAndDoesNotBridgeWater() {
  val surface=Path().apply { addRect(androidx.compose.ui.geometry.Rect(-300f,-200f,-60f,200f));addRect(androidx.compose.ui.geometry.Rect(60f,-200f,300f,200f)) }
  val b=requireNotNull(CityBuildingProjection.building(CitySceneBuilding(
   OwnedBuilding("a",1,1,4,20,24,emptyMap(),false,AcceptedCityGeometry(CityFloorGeometry(listOf(0L)),"a",1,"o","wobbrik-v1","wobbrik-structural-v1")),CityPosition(-90000,90000,0))))
  val walk=Path().apply {moveTo(-200f,60f);lineTo(200f,60f)}
  val park=cityParkSurface(surface,listOf(walk),listOf(b),emptyList())
  val paving=region(park)
  assertFalse(paving.contains(0,60));assertFalse(paving.contains(-180,0))
  assertTrue(Path.combine(PathOperation.Difference,park,surface).isEmpty)
 }
}
