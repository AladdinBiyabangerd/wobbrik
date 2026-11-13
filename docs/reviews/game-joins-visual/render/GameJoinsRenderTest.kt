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
class GameJoinsRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private fun render(name:String, count:Int=4, crane:Double=0.0, finished:Boolean=false, cosmetic:Boolean=true, style:Int=1, mixed:Boolean=false) {
  val state=PracticeState(phase=if(finished) Phase.FINISHED else Phase.SWINGING, craneX=crane,
   floors=List(count){Floor(0.0,it*PracticeRulesV1.FLOOR_HEIGHT)})
  val motion=SceneMotion().also {
   it.advance(state,0f)
   // Exercise rotated suspension endpoints at maximum horizontal travel too.
   if(crane != 0.0) repeat(90) { _ -> it.advance(state,1f/60f) }
  }
  compose.setContent {
   val art=SceneArt(ImageBitmap.imageResource(R.drawable.game_floor),ImageBitmap.imageResource(R.drawable.game_crane),
    ImageBitmap.imageResource(R.drawable.inventory_garden),ImageBitmap.imageResource(R.drawable.game_roof),ImageBitmap.imageResource(R.drawable.game_cloud),rememberGameAtlases())
   val cosmetics=rememberCosmeticArt()
   Canvas(Modifier.fillMaxSize().background(GameTokens.Background)) {
    drawGameScene(state,art,motion,if(cosmetic) cosmetics else emptyMap(),
     listOf("wall","window","entrance","lighting","roof").associateWith { "base_${style}_$it" }
       .let { if(!cosmetic) emptyMap() else if(mixed) it + ("window" to "base_3_window") else it })
   }
  }
  compose.waitForIdle()
  compose.runOnUiThread {
   val view=compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(Canvas(bitmap))
   val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/game-joins-visual/$name.png")
   file.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
 }
 @Test fun straight()=render("straight")
 @Test fun swing()=render("swing",crane=120.0)
 @Test fun empty()=render("empty",count=0)
 @Test fun finished()=render("finished",finished=true)
 @Test fun localDefault()=render("local-default",cosmetic=false)
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normal()=render("normal")
 @Test fun teal()=render("teal-straight",style=2)
 @Test fun tealFinished()=render("teal-finished",style=2,finished=true)
 @Test fun tealSwing()=render("teal-swing",style=2,crane=-120.0)
 @Test fun mixed()=render("mixed-catalog",mixed=true)
 @Test fun single()=render("single-ground",count=1)
 @Test fun tall()=render("tall",count=40)
}
