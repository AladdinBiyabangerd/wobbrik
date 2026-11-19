package com.aladdin.wobbrik

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import appbackend.wobbrik.game.FinishCosmetics
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Reference-like valid part combinations, explicitly synthetic; not an account snapshot.
 * Source centres and selected IDs are identical on both sides. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarketReferenceSelectionTest : MarketApprovedHost() {
 @Test fun referenceMaterialsSameParts() {
  val assets=compose.activity.assets
  val original=ApprovedArt.cosmetics(assets)+ApprovedArt.windows(assets)
  val updated=original+("base_2_wall" to MarketBuildingMaterials.load(assets,"base_2_wall"))+("base_4_wall" to MarketBuildingMaterials.load(assets,"base_4_wall"))
  val atlases=ApprovedArt.atlases(assets)
  val purple=ApprovedArt.purple(assets)
  val names=listOf("Kərpic", "Suvaq", "Panel", "Əhəngdaşı")
  val walls=listOf(2,3,4,1)
  val centres=List(10) {0L}
  compose.setContent {
   Column(Modifier.fillMaxSize().background(GameTokens.InventoryCollection.Background)) {
    Text("Same selected parts · before / after",color=GameTokens.Text)
    walls.forEachIndexed { index, wall->
     val parts=FinishCosmetics.categories.associateWith {"base_1_$it"}+("wall" to "base_${wall}_wall")
     Row(Modifier.fillMaxWidth().weight(1f)) {
      listOf(false,true).forEach { detailed->
       Column(Modifier.weight(1f).fillMaxHeight()) {
        Text(names[index],color=GameTokens.Text)
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
         purpleBuilding(purple,if(detailed) updated else original,atlases,parts,centres,detailed)
        }
       }
      }
     }
    }
   }
  }
  compose.waitForIdle()
  capture("reference-materials-before-after")
 }
}
