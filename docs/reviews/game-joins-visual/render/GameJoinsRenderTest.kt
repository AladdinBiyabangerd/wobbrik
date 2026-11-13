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
class GameJoinsRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private fun render(name:String, count:Int=4, crane:Double=0.0, finished:Boolean=false, cosmetic:Boolean=true, style:Int=1, mixed:Boolean=false, proposal:Boolean=false) {
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
   val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/game-joins-visual/$name.png")
   file.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
 }
 @Test fun allCatalogParts() {
  compose.setContent {
   val context=androidx.compose.ui.platform.LocalContext.current
   val art=rememberCosmeticArt()+rememberGameWindowArt()
   val atlases=rememberGameAtlases()
   val items=context.assets.open("finish/catalog-map.json").bufferedReader().use {
    org.json.JSONObject(it.readText()).getJSONArray("items")
   }
   check(items.length()==68)
   Canvas(Modifier.fillMaxSize().background(GameTokens.Background)) {
    val cellWidth=size.width/6
    val cellHeight=size.height/12
    for(i in 0 until items.length()) {
     val item=items.getJSONObject(i)
     val id=item.getString("id")
     val category=item.getString("category")
     val parts=listOf("wall","window","entrance","lighting","roof").associateWith { "base_1_$it" } + (category to id)
     val left=(i%6)*cellWidth+cellWidth*.08f
     val upper=(i/6)*cellHeight+cellHeight*.25f
     val width=cellWidth*.84f
     check(art.containsKey(id))
     if(category=="roof") drawModularRoof(art[id],id,left,upper+width*.5f,width)
     else drawModularFloor(atlases.getValue(if(parts["wall"]=="base_2_wall") "modular-teal" else "modular").floor,art,atlases,parts,left,upper,width,
       category in listOf("entrance","lighting"))
     drawIntoCanvas { canvas ->
      canvas.nativeCanvas.drawText(id,left,(i/6)*cellHeight+cellHeight*.96f,
       android.graphics.Paint().apply { color=android.graphics.Color.WHITE; textSize=cellWidth*.075f })
     }
    }
   }
  }
  compose.waitForIdle()
  compose.runOnUiThread {
   val view=compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(Canvas(bitmap))
   File("/Users/mac/My Workspace/Wobbrik/docs/reviews/game-joins-visual/catalog-68.png").outputStream().use {
    bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
   }
  }
 }
 @Test fun proposedPurple()=render("proposed-purple-windows",style=1,mixed=true,proposal=true)
 @Test fun proposedTeal()=render("proposed-teal-windows",style=2,mixed=true,proposal=true)
 @Test fun proposedPlaster()=render("proposed-plaster-windows",style=3,proposal=true)
 @Test fun proposedPanel()=render("proposed-panel-windows",style=4,proposal=true)
 @Test fun proposedWood()=render("proposed-wood-windows",style=5,proposal=true,finished=true)
 @Test fun proposedConcrete()=render("proposed-concrete-windows",style=6,proposal=true)
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
 @Test fun plaster()=render("plaster-modular",style=3)
 @Test fun panel()=render("panel-modular",style=4)
 @Test fun wood()=render("wood-modular",style=5)
 @Test fun concrete()=render("concrete-modular",style=6)
 @Test fun plasterRoof()=render("plaster-roof",style=3,finished=true)
 @Test fun panelRoof()=render("panel-roof",style=4,finished=true)
 @Test fun woodRoof()=render("wood-roof",style=5,finished=true)
 @Test fun concreteRoof()=render("concrete-roof",style=6,finished=true)
 @Test fun tealMixed()=render("teal-mixed",style=2,mixed=true)
 @Test fun tall()=render("tall",count=40)
}
