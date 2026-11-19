package com.aladdin.wobbrik

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import appbackend.wobbrik.game.FinishCosmetics
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Same prepared sprites and immutable inputs, before/after. Six full sets,
 * a mixed selection and an extreme accepted offset fixture, with no fake facade. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h1600dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarketMaterialSelectionTest : MarketApprovedHost() {
 @Test fun selectedLayersAndOffsets() {
  val assets=compose.activity.assets
  val art=ApprovedArt.cosmetics(assets)+ApprovedArt.windows(assets)
  val atlases=ApprovedArt.atlases(assets)
  val purple=ApprovedArt.purple(assets)
  val image=BitmapFactory.decodeResource(compose.activity.resources,R.drawable.inventory_garden).asImageBitmap()
  val garden=CosmeticSprite(image,IntOffset.Zero,IntSize(image.width,image.height))
  val selections=(1..6).map { style->FinishCosmetics.categories.associateWith {"base_${style}_$it"} } +
   listOf(mapOf("wall" to "base_3_wall","roof" to "base_2_roof","window" to "coins_window_2",
    "entrance" to "base_4_entrance","lighting" to "base_5_lighting"))
  val centers=listOf(0L,2500L,-1500L,6000L,4000L,12500L,9000L,20000L,18000L,22500L)
  val originals=selections.map { it.toMap() }
  val originalCenters=centers.toList()
  compose.setContent {
   Column(Modifier.fillMaxSize().background(GameTokens.InventoryCollection.Background)) {
    Text("Before / Market materials — same selected IDs",color=GameTokens.Text)
    selections.forEachIndexed { index, parts ->
     Row(Modifier.weight(1f).fillMaxWidth()) {
      listOf(false,true).forEach { detailed->
       Column(Modifier.weight(1f).fillMaxHeight()) {
        Text(if(index<6) "base_${index+1} · $detailed" else "mixed · $detailed",color=GameTokens.Text)
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
         if(usesPurpleTemplate(parts)) purpleBuilding(purple,art,atlases,parts,centers,detailed)
         else buildingPreview(atlases,art,parts,centers,garden,detailed)
        }
       }
      }
     }
    }
   }
  }
  compose.waitForIdle()
  capture("selected-parts-before-after")
  assertEquals(originals,selections)
  assertEquals(originalCenters,centers)
 }
}
