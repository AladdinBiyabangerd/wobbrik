package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.aladdin.wobbrik.engine.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Frozen synthetic states render production drawGameScene; no game adapter or emulator. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GameClearanceRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private fun render(name:String, count:Int=4, crane:Double=0.0, finished:Boolean=false, cosmetic:Boolean=true, style:Int=1, mixed:Boolean=false, proposal:Boolean=false, offset:Double=0.0, progress:Double?=null) {
  val state=PracticeState(phase=if(progress != null) Phase.FALLING else if(finished) Phase.FINISHED else Phase.SWINGING, craneX=crane,
   floors=List(count){Floor(offset,it*PracticeRulesV1.FLOOR_HEIGHT)},
   falling=progress?.let { Floor(offset+crane,count*30.0+150.0*it) })
  val motion=SceneMotion().also {
   it.advance(state,0f,offset)
   // Exercise rotated suspension endpoints at maximum horizontal travel too.
   if(crane != 0.0) repeat(90) { _ -> it.advance(state,1f/60f,offset) }
  }
  compose.setContent {
   val art=SceneArt(ImageBitmap.imageResource(R.drawable.game_floor),ImageBitmap.imageResource(R.drawable.game_crane),
    ImageBitmap.imageResource(R.drawable.inventory_garden),ImageBitmap.imageResource(R.drawable.game_roof),ImageBitmap.imageResource(R.drawable.game_cloud),rememberGameAtlases())
   val cosmetics=rememberCosmeticArt()+rememberGameWindowArt()
   // Candidate windows are test-only files, deliberately absent from app assets.
   val renderCosmetics=if(proposal) {
    val candidate=android.graphics.BitmapFactory.decodeFile("/Users/mac/My Workspace/Wobbrik/docs/design/game-tap-v2/modular-window-proposal/windows.png")
    val image=candidate.asImageBitmap()
    val crops=mapOf("base_3_window" to listOf(640,76,285,198),"base_4_window" to listOf(955,110,286,148),
      "base_5_window" to listOf(34,330,250,276),"base_6_window" to listOf(350,338,242,262))
    cosmetics+crops.mapValues { (_,c)->CosmeticSprite(image,androidx.compose.ui.unit.IntOffset(c[0],c[1]),androidx.compose.ui.unit.IntSize(c[2],c[3])) }
   } else cosmetics
   Canvas(Modifier.fillMaxSize().background(GameTokens.Background)) {
    drawGameScene(state,art,motion,if(cosmetic) renderCosmetics else emptyMap(),
     listOf("wall","window","entrance","lighting","roof").associateWith { "base_${style}_$it" }
       .let { if(!cosmetic) emptyMap() else if(mixed) it + ("window" to "base_3_window") else it })
   }
  }
  compose.waitForIdle()
  compose.runOnUiThread {
   val view=compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(Canvas(bitmap))
   val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/game-camera-clearance/$name.png")
   file.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
 }
 @Test fun offsetLeft()=render("offset-left",count=60,offset=-40.0,crane=40.0)
 @Test fun offsetRight()=render("offset-right",count=60,offset=40.0,crane=-40.0)
 @Test fun release()=render("release",count=60,offset=40.0,crane=-40.0,progress=1.0)
 @Test fun contact()=render("contact",count=60,offset=40.0,crane=-40.0,progress=.001)
 @Test fun empty()=render("empty",count=0)
 @Test fun first()=render("first",count=1)
 @Test fun fourth()=render("fourth",count=4)
 @Test fun tall()=render("tall",count=60)
 @Test fun swing()=render("swing",count=60,crane=120.0)
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun small()=render("small",count=60)
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normal()=render("normal",count=60)
}
