// Adapted from the existing host render fixture; output is scoped to this review.
package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Canvas
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactPurchaseRenderTest {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 private val catalog = RecoveryCatalog("wobbrik-v1", "wobbrik-structural-v1",
  listOf("tree" to listOf("Tək ağac", "Ağac cütü", "Kiçik ağaclıq"),
   "bench" to listOf("Tək skamya", "Qarşılıqlı skamyalar", "Skamyalı istirahət guşəsi"),
   "streetlight" to listOf("Tək küçə fənəri", "Qoşa küçə fənəri", "Fənər sırası"),
   "road" to listOf("Qısa düz yol", "Dönüş yol hissəsi", "Uzun düz yol")).flatMap { (key, names) ->
    names.mapIndexed { i, name -> CatalogItem("decor_${key}_${i+1}", name, key, "copy", 0, listOf(25L,75L,150L)[i], null) }
   }, listOf(LandOffer("land_1",150,200),LandOffer("land_2",500,400),LandOffer("land_3",1800,900),
    LandOffer("land_4",3900,1500),LandOffer("land_5",7650,2500)))
 private val ready = AuthenticatedRecoveryOwner.State.Ready(GameSnapshot("wobbrik-v1",1,1250,150,null,
  AccountRecovery("now","first","later",3,0,emptyList(),emptyList(),emptyList(),emptyList())),catalog,"render-only")
 private fun render(land: Boolean, font: Float, name: String) {
  compose.setContent {
   val density = LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density, font)) {
    if (land) LandShopContent(ready, {}, {}) else DecorationShopContent(ready, emptyList(), false, {}, {}, {})
   }
  }
  compose.waitForIdle()
  capture(name+"-list")
  compose.onNodeWithText(if (land) "Sahə 1" else "Tək ağac").performClick()
  compose.waitForIdle()
  compose.onNodeWithText("Al").assertIsDisplayed()
  compose.onNodeWithText("Ləğv et").assertIsDisplayed()
  capture(name+"-confirmation")
 }
 private fun capture(name: String) {
  val f=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/mobile-sizing-visual/purchase-$name.png")
  requireNotNull(f.parentFile).mkdirs()
  compose.runOnUiThread {
   val view = compose.activity.window.decorView
   val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
   view.draw(Canvas(bitmap))
   f.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
  }
 }
 @Test fun decorSmall() = render(false,1f,"decor-360-font1")
 @Test fun decorLargeFont() = render(false,2f,"decor-360-font2")
 @Test fun landSmall() = render(true,1f,"land-360-font1")
 @Test fun landLargeFont() = render(true,2f,"land-360-font2")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun decorNormal() = render(false,1f,"decor-390-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun landNormal() = render(true,1f,"land-390-font1")
}
