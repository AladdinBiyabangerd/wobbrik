package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Visual assembly study only: not catalog IDs, an account adapter or shipped renderer. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ModularBuildingStudyTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val root=File("/Users/mac/My Workspace/Wobbrik")
 private fun render(font:Float) {
  val bitmap=BitmapFactory.decodeFile(File(root,"docs/design/building-purple-v1/modular/atlas-v1.png").path).asImageBitmap()
  fun cell(x:Int,y:Int,w:Int,h:Int)=CosmeticSprite(bitmap,IntOffset(x,y),IntSize(w,h))
  val art=mapOf("floor" to cell(30,98,370,268),"roof" to cell(420,100,410,270),
   "ground" to cell(865,88,370,287),"arch" to cell(85,478,245,295),
   "square" to cell(494,502,250,251),"entrance" to cell(851,444,373,363),
   "balcony" to cell(18,925,390,288),"garden" to cell(425,948,415,202),
   "lamp" to cell(872,942,133,200))
  var selection by mutableStateOf("arch")
  var floors by mutableIntStateOf(4)
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
    Column(Modifier.fillMaxSize().background(GameTokens.Background).padding(GameTokens.Gap),
     verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
     Text("Modullu bina · ilk sınaq",color=GameTokens.Text,fontSize=GameTokens.Title)
     Text(if(selection=="arch") "Tağlı pəncərə · $floors vizual mərtəbə" else "Düz pəncərə · $floors vizual mərtəbə",
      color=GameTokens.Muted,fontSize=GameTokens.Body)
     Canvas(Modifier.fillMaxWidth().weight(1f)) { assemble(art,selection,floors) }
     Text("Ayrı gövdə, dam, pəncərə, giriş, balkon və torpaq qatları",
      color=GameTokens.Muted,fontSize=GameTokens.Caption)
    }
   }
  }
  fun capture(name:String) {
   compose.waitForIdle()
   compose.runOnUiThread {
    val view=compose.activity.window.decorView
    val output=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
    view.draw(AndroidCanvas(output))
    val file=File(root,"docs/reviews/modular-building-visual/$name.png")
    file.parentFile!!.mkdirs()
    file.outputStream().use { output.compress(Bitmap.CompressFormat.PNG,100,it) }
   }
  }
  capture("arch-font$font")
  compose.runOnIdle { selection="square" }
  capture("square-font$font")
  compose.runOnIdle { selection="arch";floors=6 }
  capture("six-floors-font$font")
 }
 @Test fun regular()=render(1f)
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun compactLargeFont()=render(2f)
}

/** Coordinates are measured in this atlas, not UI dimensions or server geometry. */
private fun DrawScope.assemble(art:Map<String,CosmeticSprite>,window:String,floors:Int) {
 val pitch=180f
 val worldHeight=340f+(floors-1)*pitch+170f
 val zoom=minOf(size.width/550f,size.height/worldHeight)
 withTransform({translate((size.width-550f*zoom)/2,(size.height-worldHeight*zoom)/2);scale(zoom,zoom,Offset.Zero)}) {
  val bottom=worldHeight-90f
  sprite(art.getValue("garden"),0f,bottom-100f,550f,268f)
  for (i in 0 until floors) {
   val offset=listOf(0f,-15f,14f,-7f,12f,0f)[i]
   val x=90f+offset
   val y=bottom-268f-i*pitch
   sprite(art.getValue(if(i==0) "ground" else "floor"),x,y,370f,if(i==0)287f else 268f)
   fun point(u:Float,v:Float,side:Boolean)=if(side)
    Offset(x+240f+104f*u,y+115f-65f*u+135f*v)
    else Offset(x+16f+224f*u,y+86f+29f*u+135f*v)
   fun window(side:Boolean) {
    val u=if(side).2f else .26f;val v=.06f
    val w=if(side).6f else .45f;val h=.82f
    projected(art.getValue(window),listOf(point(u,v,side),point(u+w,v,side),point(u+w,v+h,side),point(u,v+h,side)))
   }
   if(i==0) {
    sprite(art.getValue("entrance"),x+35f,y+95f,200f,195f)
    sprite(art.getValue("lamp"),x+10f,y+115f,35f,53f)
    sprite(art.getValue("lamp"),x+213f,y+130f,27f,41f)
   } else window(false)
   window(true)
   if(i>0 && i%2==1) sprite(art.getValue("balcony"),x-30f,y+164f,225f,166f)
   if(i==floors-1) sprite(art.getValue("roof"),x-19f,y-137f,410f,270f)
  }
 }
}

private fun DrawScope.projected(item:CosmeticSprite,points:List<Offset>) {
 drawIntoCanvas { canvas ->
  val w=item.size.width.toFloat();val h=item.size.height.toFloat()
  val matrix=Matrix()
  matrix.setPolyToPoly(floatArrayOf(0f,0f,w,0f,w,h,0f,h),0,points.flatMap {listOf(it.x,it.y)}.toFloatArray(),0,4)
  val native=canvas.nativeCanvas
  native.save();native.concat(matrix)
  native.drawBitmap(item.image.asAndroidBitmap(),Rect(item.origin.x,item.origin.y,item.origin.x+item.size.width,item.origin.y+item.size.height),
   RectF(0f,0f,w,h),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
  native.restore()
 }
}
