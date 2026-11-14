package com.aladdin.wobbrik
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import appbackend.wobbrik.game.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
/** Synthetic state; specimens use the shipped BuildingPreview renderer. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PurpleCatalogRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun allIdsAndMixed() {
  val metadata=compose.activity.assets.open("finish/catalog-map.json").bufferedReader().use {JSONObject(it.readText()).getJSONArray("items")}
  val items=(0 until metadata.length()).map {metadata.getJSONObject(it)}
  assertEquals(68,items.size)
  val categories=FinishCosmetics.categories.toList()
  val base=categories.associateWith {"base_1_$it"}
  val cases=items.map {it.getString("id") to (base+(it.getString("category") to it.getString("id")))} +
   (0 until 16).map {index -> "mixed-$index" to categories.associateWith {category ->
    if(category==categories[index%5]) "base_1_$category" else {
     val choices=items.filter {it.getString("category")==category}
     choices[(index*3+categories.indexOf(category))%choices.size].getString("id")
    }
   }} + (0 until 16).map {index -> "mixed-no-base1-$index" to categories.associateWith {category ->
    val choices=items.filter {it.getString("category")==category && !it.getString("id").startsWith("base_1_")}
    choices[(index*3+categories.indexOf(category))%choices.size].getString("id")
   }} + (2..6).map {style -> "control-base$style" to categories.associateWith {"base_${style}_$it"}}
  assertEquals(68,items.map {it.getString("id")}.toSet().size)
  cases.filter {it.first.startsWith("mixed")}.forEach {assertTrue(it.first,usesPurpleTemplate(it.second))}
  cases.filter {it.first.startsWith("control")}.forEach {assertFalse(it.first,usesPurpleTemplate(it.second))}
  var batch by mutableStateOf(cases.take(4))
  compose.setContent {
   Column(Modifier.fillMaxSize().background(GameTokens.Background)) {
    batch.chunked(2).forEach {row -> Row(Modifier.weight(1f)) {row.forEach {(id,parts) ->
     Column(Modifier.weight(1f).fillMaxHeight()) {
      Text(id,color=GameTokens.Text,fontSize=GameTokens.Body)
      val building=OwnedBuilding(id,2,10,64,32,38,parts,false,
       AcceptedCityGeometry(CityFloorGeometry(List(10) {it*2500L}),"attempt",1,"operation","wobbrik-v1","wobbrik-structural-v1"))
      BuildingPreview(building,Modifier.fillMaxWidth().weight(1f))
     }
    }}}
   }
  }
  val out=File(requireNotNull(System.getProperty("wobbrik.render.output")));out.mkdirs()
  cases.chunked(4).forEachIndexed {index,group ->
   compose.runOnIdle {batch=group};compose.waitForIdle()
   compose.onAllNodesWithContentDescription("10 mərtəbəli bina").assertCountEquals(group.size)
   compose.onNodeWithText("Bina qrafikası mövcud deyil").assertDoesNotExist()
   compose.runOnUiThread {
    val v=compose.activity.window.decorView
    val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
    File(out,"catalog-${index.toString().padStart(2,'0')}.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
   }
  }
  File(out,"manifest.txt").writeText(cases.mapIndexed {i,c -> "${i/4}\t${i%4}\t${c.first}\t${c.second}"}.joinToString("\n"))
 }
}
