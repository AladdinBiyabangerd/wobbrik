package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
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

/** Synthetic low-rise account, never a production adapter or invented ownership. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CityParityRenderTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun building(index: Int, x: Long, y: Long): CitySceneBuilding {
        val floors = 3 + index % 3
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
    private fun capture(name: String) {
        compose.runOnUiThread {
            val view=compose.activity.window.decorView
            val image=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            view.draw(Canvas(image))
            val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/city-remaining-visual/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
    }
    private fun render(font:Float,name:String) {
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
                CityContent(scene,false,{}, {}, {},lifeTimeSeconds=7.0)
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Bax").assertIsDisplayed()
        compose.onNodeWithText("İnventar").assertIsDisplayed()
        capture(name)
        val actions=compose.onNodeWithContentDescription("Bina b0:",substring=true).fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        actions.forEachIndexed { index, action ->
            compose.runOnIdle { check(action.action()) };compose.waitForIdle()
            compose.onNodeWithText("Geri").assertIsDisplayed();capture("$name-focus-$index")
            compose.onNodeWithText("Geri").performClick();compose.waitForIdle()
        }
    }
    @Test fun normal()=render(1f,"representative-390-font1")
    @Test @Config(qualifiers="w360dp-h640dp-xxhdpi") fun small()=render(1f,"representative-360-font1")
    @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun narrowLargeFont()=render(2f,"representative-320-font2")
}
