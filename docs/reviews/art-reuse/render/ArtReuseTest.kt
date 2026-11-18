package com.aladdin.wobbrik

import android.app.Application
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.game.*
import com.aladdin.wobbrik.engine.AcceptedAttemptCommands
import com.aladdin.wobbrik.engine.AcceptedEngineHost
import kotlinx.coroutines.*
import org.junit.Assert.*
import appbackend.wobbrik.billing.BillingSession
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtReuseTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun repeatedArtBaseline() {
  var generation by mutableStateOf(0)
  val times=mutableListOf<Long>()
  val images=java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Bitmap,Boolean>())
  var bytes=0L
  var firstCosmetic: Any? = null
  var firstWindows: Any? = null
  var firstAtlases: Any? = null
  compose.setContent {
   key(generation) {
    val start=System.nanoTime()
    val cosmetic=rememberCosmeticArt()
    val windows=rememberGameWindowArt()
    val atlases=rememberGameAtlases()
    val elapsed=(System.nanoTime()-start)/1_000_000
    SideEffect {
     if (firstCosmetic == null) {
      firstCosmetic=cosmetic; firstWindows=windows; firstAtlases=atlases
     } else {
      assertSame(firstCosmetic,cosmetic); assertSame(firstWindows,windows); assertSame(firstAtlases,atlases)
     }
     times.add(elapsed)
     val unique=(cosmetic.values.map {it.image}+windows.values.map {it.image}+atlases.values.flatMap {it.cells.values.map {s->s.image}}).map {it.asAndroidBitmap()}.distinct()
     unique.forEach { if(images.add(it)) bytes+=it.allocationByteCount.toLong() }
    }
   }
  }
  repeat(4) { compose.runOnIdle {generation++}; compose.waitForIdle() }
  assertEquals(10,images.size)
  println("ART_BASELINE host compose ms=$times uniqueBitmaps=${images.size} allocatedRGBABytes=$bytes")
 }
 @Test fun bundledPixelsAndCropsUnchanged() {
  val assets=compose.activity.assets
  fun same(expected:Map<String,CosmeticSprite>,actual:Map<String,CosmeticSprite>) {
   assertEquals(expected.keys,actual.keys)
   val checked=java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Bitmap,Boolean>())
   expected.forEach { (id,sprite) ->
    val other=actual.getValue(id)
    assertEquals(sprite.origin,other.origin); assertEquals(sprite.size,other.size)
    val bitmap=sprite.image.asAndroidBitmap()
    if(checked.add(bitmap)) assertTrue(bitmap.sameAs(other.image.asAndroidBitmap()))
   }
  }
  same(loadCosmeticArt(assets),ApprovedArt.cosmetics(assets))
  same(loadGameWindowArt(assets),ApprovedArt.windows(assets))
  val original=loadGameAtlases(assets); val cached=ApprovedArt.atlases(assets)
  assertEquals(original.keys,cached.keys)
  original.forEach { (style,atlas) ->
   val other=cached.getValue(style)
   assertEquals(atlas.pitch,other.pitch);assertEquals(atlas.depth,other.depth)
   assertEquals(atlas.topAnchors,other.topAnchors);same(atlas.cells,other.cells)
  }
 }
}
