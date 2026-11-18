package com.aladdin.wobbrik

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.game.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class InventoryRowsTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private fun lastRow(count:Int,font:Float) {
  val f=InventoryCompositionProfileTest.Fixture().apply {inventoryCount=count;distinctFloors=true}
  try {
   runBlocking {val session=f.sessions.current()!!;val r=f.ready()
    f.cache.capture(session,ReadOnlyAccount(session.uid,r.snapshot,r.catalog!!),"inventory-",f.reads) {InventoryRepository(it).page(session)}
   }
   f.networkGate=CompletableDeferred()
   var selected:String?=null
   compose.setContent {val d=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {MaterialTheme {
     MobileMenuShell("inventory",1250,{}, {}) {
      CompositionLocalProvider(LocalBuildingArt provides BuildingArtState(ArtPreparation.Loading){}) {
       InventoryScreen(f.configured,f.ready(),null,{selected=it},{},{},{},{})
      }
     }
    }}
   }
   val columns=if(font>1f) 1 else 2
   val collection=SemanticsMatcher("inventory collection") {node ->
    node.config.getOrNull(SemanticsProperties.CollectionInfo)?.let {
     it.columnCount==columns && it.rowCount==(count+columns-1)/columns
    }==true
   }
   compose.waitUntil(10000) {compose.onAllNodes(collection).fetchSemanticsNodes().isNotEmpty()}
   val last="${10+count} mərtəbə  ›"
   compose.onNode(collection).performScrollToNode(hasText(last))
   compose.onNodeWithText(last).assertIsDisplayed()
   compose.onNode(hasClickAction() and hasText(last)).performClick()
   assertEquals("b-$count",selected);assertEquals(0,f.commands.get())
   assertFalse(f.networkGate!!.isCompleted)
  } finally {f.networkGate?.complete(Unit);f.close()}
 }
 @Test fun fullPageScrollsToTwentiethBuilding()=lastRow(20,1f)
 @Test fun oddPagePreservesFinalBuildingAndClickId()=lastRow(19,1f)
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun largeFontSingleColumnScrollsToLastBuilding()=lastRow(19,2f)
 @Test fun shellKeepsSameLogoAcrossHomeAndInventory() {
  var page by mutableStateOf("home")
  var image:ImageBitmap?=null
  compose.setContent {MaterialTheme {MobileMenuShell(page,1250,{}, {}) {
   val value=LocalMenuLogo.current?.first?.images?.singleOrNull()
   SideEffect {image=value}
   Text("content")
  }}}
  compose.waitUntil(10000) {image!=null}
  val first=image
  compose.runOnIdle {page="inventory"};compose.waitForIdle()
  assertSame(first,image)
  compose.runOnIdle {page="home"};compose.waitForIdle()
  assertSame(first,image)
 }
}
