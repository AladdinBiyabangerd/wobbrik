package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.billing.AccountSessions
import appbackend.wobbrik.game.*
import com.aladdin.wobbrik.engine.AcceptedAttemptCommands
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File


@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CityCollectionTest {
 @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
 private fun capture(name:String, dialog:Boolean=false) {
  val window=if(dialog) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog()).window!! else compose.activity.window
  compose.runOnUiThread {
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(window.decorView)
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=window.decorView
   val warm=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(Canvas(warm));warm.recycle();v.invalidate()
  }
  compose.mainClock.advanceTimeByFrame();compose.waitForIdle()
  compose.runOnUiThread {
   val v=window.decorView;val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   var result=-1
   android.view.PixelCopy.request(window,b,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File(requireNotNull(System.getProperty("wobbrik.render.output","/private/tmp/wobbrik-commerce-native"))).apply {mkdirs()}
   File(out,"$name.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
  }
 }
    private fun building(index: Int, x: Long, y: Long): CitySceneBuilding {
        val floors = 6 + index % 3
        val parts = FinishCosmetics.categories.associateWith { "base_${index % 6 + 1}_$it" }.toMutableMap()
        if (index == 4) {
            parts["wall"] = "base_2_wall"
            parts["window"] = "coins_window_2"
            parts["roof"] = "reward_roof_1"
        }
        return CitySceneBuilding(OwnedBuilding("b$index",1,floors.toLong(),(floors*4).toLong(),20,24,parts,false,
            AcceptedCityGeometry(CityFloorGeometry(List(floors) { it * 3500L }),"a$index",1,"o$index",
                "wobbrik-v1","wobbrik-structural-v1")),CityPosition(x,y,index%4))
    }
    private val scene = CityScene(CityMembership(1,76.toBigInteger(),(0..4).associate { "b$it" to 1 },null),
        listOf(CityParcel("base",CityPosition(0,0,0)),CityParcel("land_5",CityPosition(0,220000,0))),
        listOf(building(0,80000,80000),building(1,310000,80000),building(2,80000,310000),
            building(3,310000,310000),building(4,195000,195000)),
        listOf(CitySceneDecoration(DecorationCopy("tree1","decor_tree_1"),CityPosition(195000,65000,0)),
            CitySceneDecoration(DecorationCopy("tree2","decor_tree_2"),CityPosition(195000,360000,0)),
            CitySceneDecoration(DecorationCopy("bench","decor_bench_1"),CityPosition(65000,195000,0)),
            CitySceneDecoration(DecorationCopy("lamp","decor_streetlight_1"),CityPosition(360000,195000,0))))

 private fun render(font:Float,name:String,insets:Boolean=false,data:CityScene=scene,growTo:CityScene?=null) {
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",(1..6).map { CatalogItem("base_${it}_wall",if(insets) "Çox uzun kərpic hörgü bina materialının adı" else "Kərpic hörgü","wall","permanent",0,1,null) },emptyList())
  var current by mutableStateOf<CityScene?>(data)
  var failed by mutableStateOf(false)
  var cached by mutableStateOf(false)
  var refresh=0;var inventory=0;var page="city"
  compose.setContent { val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) { MaterialTheme {
    Box(Modifier.fillMaxSize().then(if(insets) Modifier.padding(top=GameTokens.Inset,bottom=GameTokens.Target) else Modifier)) {
     MobileMenuShell("city",if(insets) Long.MAX_VALUE else 411,{}, {page=it}) {
      CityContent(current,failed,{refresh++},{page="home"},{inventory++},lifeTimeSeconds=0.0,cached=cached,catalog=catalog)
     }
    }
   }}
  }
  compose.waitUntil(20000) { compose.onAllNodesWithContentDescription("Bina b0:",substring=true).fetchSemanticsNodes().isNotEmpty() }
  fun awaitScenery() = compose.waitUntil(20000) {
   compose.onAllNodes(SemanticsMatcher.expectValue(CitySceneryPrepared,true)).fetchSemanticsNodes().isNotEmpty()
  }
  awaitScenery()
  compose.onNodeWithText("Şəhərim").assertIsDisplayed()
  compose.onNodeWithContentDescription("Şəhər").assertIsSelected()
  compose.onNodeWithText("Binaya bax").assertIsDisplayed()
  compose.onNodeWithContentDescription("Yenilə").performClick();assertEquals(1,refresh)
  if(growTo==null) capture("city-$name") else {
   capture("city-before-growth-$name")
   compose.runOnIdle { current=growTo }
   compose.onNodeWithContentDescription("6 torpaq sahəsi",substring=true).assertExists()
   awaitScenery()
   capture("city-$name")
  }
  if(insets) {
   compose.onNodeWithText("6 mərtəbə · 24 sakin").performScrollTo().assertIsDisplayed()
   capture("city-summary-scroll-$name")
  }
  compose.onNodeWithText("Binaya bax").performClick()
  compose.onNodeWithText("Şəhərə qayıt").assertIsDisplayed()
  capture("city-focus-$name")
  compose.onNodeWithContentDescription("Kameranı sıfırla").performClick()
  compose.onNodeWithText("Binaya bax").assertIsDisplayed()
  val actions=compose.onNodeWithContentDescription("Bina b0:",substring=true).fetchSemanticsNode()
   .config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
  assertEquals(5,actions.size)
  compose.runOnIdle { assertTrue(actions.first().action()) }
  compose.onNodeWithText("Şəhərə qayıt").assertIsDisplayed()
  compose.onNodeWithContentDescription("Kameranı sıfırla").performClick()
  compose.runOnIdle {cached=true}
  compose.onNodeWithText("Keş · yalnız baxış").assertIsDisplayed()
  capture("city-cached-$name")
  compose.runOnIdle {current=scene.copy(buildings=emptyList(),decorations=emptyList());cached=false}
  compose.onNodeWithText("Şəhərdə bina yoxdur").assertIsDisplayed()
  compose.onNodeWithText("İnventara keç").assertIsDisplayed().performClick();assertEquals(1,inventory)
  compose.onNodeWithContentDescription("Binanı böyüt").assertIsNotEnabled()
  capture("city-empty-$name")
  compose.runOnIdle {current=null;failed=true}
  compose.onNodeWithText("Şəhər yüklənmədi").assertIsDisplayed()
  compose.onNodeWithText("Yenidən cəhd et").assertIsDisplayed().performClick();assertEquals(2,refresh)
  capture("city-error-$name")
  compose.runOnIdle {failed=false}
  compose.onNodeWithText("Şəhər yüklənir…").assertIsDisplayed()
  compose.onNodeWithContentDescription("Yenilə").assertIsNotEnabled()
  capture("city-loading-$name")
  compose.onNodeWithContentDescription("İnventar").performClick();assertEquals("inventory",page)
 }
 @Test fun normal()=render(1f,"390-font1")
 @Test fun normalLarge()=render(2f,"390-font2")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun small()=render(1f,"320-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun smallLarge()=render(2f,"320-font2")
 @Test @Config(qualifiers="w360dp-h640dp-xxhdpi") fun longInsets()=render(2f,"360-font2-insets-long",true)
 @Test fun expandedIsland()=render(1f,"390-expanded-six-parcels",growTo=scene.copy(parcels=listOf(
  CityParcel("base",CityPosition(0,0,0)),CityParcel("land_1",CityPosition(440000,0,0)),
  CityParcel("land_2",CityPosition(0,220000,0)),CityParcel("land_3",CityPosition(440000,220000,0)),
  CityParcel("land_4",CityPosition(0,440000,0)),CityParcel("land_5",CityPosition(440000,440000,0)))))

}
