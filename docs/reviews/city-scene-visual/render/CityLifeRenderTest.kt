package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.game.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Synthetic accepted scene, production art and Canvas. No live server/account claims. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CityLifeRenderTest {
    private val motion = object : androidx.compose.ui.MotionDurationScale {
        var value by mutableFloatStateOf(1f)
        override val scaleFactor get() = value
    }
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>(effectContext=motion)
    private val scene=CityScene(CityMembership(1,40.toBigInteger(),mapOf("b" to 1),null),
        listOf(CityParcel("base",CityPosition(0,0,0)),CityParcel("land_1",CityPosition(440000,0,0))),
        listOf(CitySceneBuilding(OwnedBuilding("b",1,10,40,20,24,
            FinishCosmetics.categories.associateWith { "base_1_$it" },false,
            AcceptedCityGeometry(CityFloorGeometry(List(10) { it*2500L }),"a",1,"o","wobbrik-v1","wobbrik-structural-v1")),CityPosition(130000,110000,0))),
        listOf(CitySceneDecoration(DecorationCopy("tree","decor_tree_1"),CityPosition(350000,180000,0)),
            CitySceneDecoration(DecorationCopy("road","decor_road_3"),CityPosition(530000,150000,0))))
    private fun capture(name:String):Bitmap {
        lateinit var image:Bitmap
        compose.runOnUiThread {
            val v=compose.activity.window.decorView
            image=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
            v.draw(Canvas(image))
            val file=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/city-balcony-visual/$name.png")
            file.parentFile!!.mkdirs();file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
        return image
    }
    private fun render(font:Float,name:String) {
        val time=mutableDoubleStateOf(1.0)
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
                CityContent(scene,false,{}, {}, {},lifeTimeSeconds=time.doubleValue)
            }
        }
        compose.waitForIdle()
        assertTrue(CityLifeRoutes.build(scene).any { it.kind==CityLifeRoutes.Kind.Car })
        assertTrue(CityLifeRoutes.build(scene).any { it.kind==CityLifeRoutes.Kind.Resident })
        compose.onNodeWithText("Əhali: 40").assertIsDisplayed()
        compose.onNodeWithContentDescription("Ana ekran").assertIsDisplayed()
        val first=capture("$name-t1")
        compose.runOnIdle { time.doubleValue=7.0 };compose.waitForIdle()
        val second=capture("$name-t7")
        assertFalse("Life frames must change",first.sameAs(second))
        compose.onNodeWithText("Bax").performClick();compose.waitForIdle()
        val focused=capture("$name-focus-t7")
        compose.runOnIdle { time.doubleValue=11.0 };compose.waitForIdle()
        assertTrue("Focused building is steady",focused.sameAs(capture("$name-focus-t11")))
        compose.onNodeWithText("Geri").performClick();compose.waitForIdle()
        capture("$name-return")
    }
    @Test fun small()=render(1f,"360-font1")
    @Test fun largeFont()=render(2f,"360-font2")
    @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun narrow()=render(2f,"320-font2")
    @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun normal()=render(1f,"390-font1")
    @Test fun emptyCityHasNoInventedPeopleOrCars() {
        val empty=scene.copy(buildings=emptyList(),decorations=emptyList(),membership=scene.membership.copy(population=0.toBigInteger(),buildings=emptyMap()))
        assertTrue(CityLifeRoutes.build(empty).isEmpty())
        compose.setContent { CityContent(empty,false,{}, {}, {},lifeTimeSeconds=3.0) }
        compose.waitForIdle();capture("empty")
        compose.onNodeWithText("Şəhərdə bina yoxdur").assertIsDisplayed()
    }
    @Test fun animationSequence() {
        val time=mutableDoubleStateOf(1.0)
        compose.setContent { CityContent(scene,false,{}, {}, {},lifeTimeSeconds=time.doubleValue) }
        compose.waitForIdle()
        repeat(24) { frame ->
            compose.runOnIdle { time.doubleValue=1.0+frame*0.25 }
            compose.waitForIdle();capture("sequence/frame-%02d".format(frame))
        }
    }
    @Test fun productionClockPausesOutsideResumedAndRestartsWithoutCatchup() {
        compose.mainClock.autoAdvance=false
        lateinit var time:State<Double>
        compose.setContent { time=rememberCityLifeClock(true,scene) }
        compose.mainClock.advanceTimeBy(1000);compose.waitForIdle()
        val before=time.value
        assertTrue(before>0.0)
        compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
        compose.mainClock.advanceTimeBy(5000);compose.waitForIdle()
        assertEquals(before,time.value,0.00001)
        compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
        compose.mainClock.advanceTimeBy(1000);compose.waitForIdle()
        assertTrue(time.value>before)
        assertTrue(time.value-before<1.2)
    }
    @Test fun disabledPlatformAnimationsFreezeAndResume() {
        compose.mainClock.autoAdvance=false
        motion.value=0f
        lateinit var time:State<Double>
        compose.setContent { time=rememberCityLifeClock(true,scene) }
        compose.mainClock.advanceTimeBy(1000);compose.waitForIdle()
        assertEquals(0.0,time.value,0.00001)
        compose.runOnIdle { motion.value=1f; androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications() }
        compose.mainClock.advanceTimeBy(1000);compose.waitForIdle()
        assertTrue(time.value>0.0)
        compose.runOnIdle { motion.value=0f; androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications() }
        compose.mainClock.advanceTimeBy(1000);compose.waitForIdle()
        assertEquals(0.0,time.value,0.00001)
    }

}
