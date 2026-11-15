package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.Density
import appbackend.wobbrik.billing.*
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
import java.time.Instant

abstract class ProfileV2Host {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(font:Float,name:String,state:String) {
  softwareLayers()
  val ownerScope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
  val sessions=AccountSessions {"token"}
  val snapshot=GameSnapshot("wobbrik-v1",1,1250,1250,null,AccountRecovery("now","first","later",7,0,emptyList(),emptyList(),emptyList(),emptyList(),28,XpInterval(java.math.BigInteger.valueOf(1050),java.math.BigInteger.valueOf(1400))))
  val catalog=RecoveryCatalog("wobbrik-v1","wobbrik-structural-v1",emptyList(),emptyList())
  val backend=object:AttemptBackend {
   override suspend fun sync(session:BillingSession)=snapshot
   override suspend fun catalog(session:BillingSession)=catalog
   override suspend fun submit(session:BillingSession,command:AttemptCommand)=error("No economic write")
  }
  val journal=object:AttemptJournal {
   override fun pending(uid:String):AttemptCommand?=null
   override fun save(uid:String,command:AttemptCommand)=error("No economic write")
   override fun remove(uid:String,command:AttemptCommand)=Unit
  }

  var publicProfile=PublicProfile(1,"Aladdin","builder")
  var saves=0
  val access=object:ProfileAccess {
   override suspend fun profile(session:BillingSession)=publicProfile
   override suspend fun replaceProfile(session:BillingSession,replacement:ProfileReplacement):PublicProfile {
    saves++; return PublicProfile(2,replacement.displayName,replacement.avatarId).also {publicProfile=it}
   }
  }
  val http=GameplayHttpBackend(sessions,GameplayTransport {_,_,_->error("No gameplay write")})
  val owner=AuthenticatedRecoveryOwner(sessions,backend,journal,ownerScope);owner.onAuthChanged("alice")
  val ready=owner.state.value as AuthenticatedRecoveryOwner.State.Ready
  val configured=WobbrikApplication.Bootstrap.Configured(owner,AcceptedAttemptCommands(owner),InventoryRepository(http),sessions,
   DecorationRepository(http),CitySceneRepository(http),profiles=access)
  val pages=mutableListOf<String>()
  try {
   compose.setContent {
    val density=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
     MaterialTheme { PublicAccountScreen(configured,ready,{pages+="home"},{pages+=it}) }
    }
   }
   compose.waitUntil(15000) {compose.onAllNodesWithTag("profile-edit").fetchSemanticsNodes().isNotEmpty()}
   compose.waitForIdle()
   if(state!="profile") {
    compose.onNodeWithTag("profile-edit").also { if (runCatching { it.assertIsDisplayed() }.isFailure) it.performScrollTo() }.performClick()
    compose.onNodeWithText("Yadda saxla").assertIsDisplayed()
    if(state=="picker") {
     compose.onNodeWithText("Avatarı dəyiş").performScrollTo().performClick()
     compose.onNodeWithTag("avatar-apply").assertIsDisplayed()
     compose.onNodeWithTag("avatar-pilot").performScrollTo().performClick()
     assertEquals(0,saves)
    }
   } else {
    compose.onNodeWithTag("profile-edit").assertIsDisplayed()
    val density = compose.activity.resources.displayMetrics.density
    val editBounds = compose.onNodeWithTag("profile-edit").fetchSemanticsNode().boundsInRoot
    assertTrue("Entire edit target remains visible: $editBounds", editBounds.height >= 48f * density - 1f)
    listOf("home","achievements","leaderboard","settings").forEach {route->
     compose.onNodeWithTag("profile-nav-$route").assertIsDisplayed()
    }
   }
   // Flush the input-triggered recomposition before drawing the native window.
   if (state == "edit") compose.onNode(hasSetTextAction()).performScrollTo().assertIsDisplayed()
   compose.mainClock.advanceTimeByFrame()
   compose.waitForIdle()
   val field = if (state == "edit") compose.onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot else null
   capture(name,state=="picker",field)
   if(state=="picker") {
    compose.onNodeWithTag("avatar-apply").performClick()
    compose.onNodeWithText("Yadda saxla").performClick()
    compose.waitUntil(15000) {saves==1}
    assertEquals("pilot",publicProfile.avatarId)
   }
   if(state=="edit") {
    compose.onNode(hasSetTextAction()).performTextReplacement("Yeni ad")
    compose.onNodeWithText("Ləğv et").performClick()
    compose.onNodeWithTag("profile-edit").also { if (runCatching { it.assertIsDisplayed() }.isFailure) it.performScrollTo() }.performClick()
    compose.onNode(hasSetTextAction()).assertTextContains("Aladdin")
    assertEquals(0,saves)
   }
  } finally {ownerScope.cancel()}
 }
 private fun softwareLayers() {
  // Compose's cached ViewLayer display lists are incomplete on a native bitmap canvas.
  // Use the library's own software dispatch path, without changing production UI.
  val type=Class.forName("androidx.compose.ui.platform.ViewLayer")
  val companion=type.getDeclaredField("Companion").apply {isAccessible=true}.get(null)
  companion.javaClass.declaredMethods.first {it.name.startsWith("setShouldUseDispatchDraw")}
   .apply {isAccessible=true}.invoke(companion,true)
 }
 protected fun cold(name:String, editing:Boolean, font:Float = 1f) {
  softwareLayers()
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {MaterialTheme {
   if(editing) ProfileEditView("Aladdin","builder",true,true,true,"Yadda saxla",{},{},{},{})
   else ProfileFrame("Profil",{},1250) {
    androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.weight(1f)) {
     ProfileOverview(PublicProfile(1,"Aladdin","builder"),"7",1250,"28",progress={
      AccountXpProgress(java.math.BigInteger.valueOf(200),java.math.BigInteger.valueOf(350))
      androidx.compose.material3.Text("Növbəti səviyyəyə 150 XP",color=GameTokens.Muted)
     },enabled=true,onEdit={})
    }
    ProfileNavbar({}, {})
   }
  }}}
  compose.waitForIdle();capture(name,false)
 }
 private fun capture(name:String,dialog:Boolean,field:androidx.compose.ui.geometry.Rect? = null) {
  compose.runOnUiThread {
   val view=if(dialog) org.robolectric.shadows.ShadowDialog.getLatestDialog().window!!.decorView else compose.activity.window.decorView
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(view)
  }
  // Invalidation must reach a frame before native display-list capture.
  compose.mainClock.advanceTimeByFrame()
  compose.waitForIdle()
  // Native Skia may populate nested text/outline display lists on the first draw.
  // Warm the complete window, then capture a second complete draw on a fresh bitmap.
  // Never stitch field pixels or headers from a different render/state.
  compose.runOnUiThread {
   val view=if(dialog) org.robolectric.shadows.ShadowDialog.getLatestDialog().window!!.decorView else compose.activity.window.decorView
   val warm=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(Canvas(warm));warm.recycle()
   view.invalidate()
  }
  compose.mainClock.advanceTimeByFrame()
  compose.waitForIdle()
  compose.runOnUiThread {
   val view=if(dialog) org.robolectric.shadows.ShadowDialog.getLatestDialog().window!!.decorView else compose.activity.window.decorView
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   // PixelCopy uses Robolectric's native HardwareRenderingScreenshot path. Unlike
   // direct software View.draw it preserves the TextField's difference-clip outline.
   val window=if(dialog) org.robolectric.shadows.ShadowDialog.getLatestDialog().window!! else compose.activity.window
   var result = -1
   android.view.PixelCopy.request(window,bitmap,{result=it},android.os.Handler(android.os.Looper.getMainLooper()))
   org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
   assertEquals(android.view.PixelCopy.SUCCESS,result)
   val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/profile-v2-visual").apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   if (field != null) {
    var glyphPixels = 0
    val outlineRows = mutableListOf<Int>()
    for (y in field.top.toInt().coerceAtLeast(0) until field.bottom.toInt().coerceAtMost(bitmap.height)) {
     var outlinePixels = 0
     for (x in field.left.toInt().coerceAtLeast(0) until field.right.toInt().coerceAtMost(bitmap.width)) {
      val pixel=bitmap.getPixel(x,y)
      if (android.graphics.Color.red(pixel)>225 && android.graphics.Color.green(pixel)>225 && android.graphics.Color.blue(pixel)>225) glyphPixels++
      if (android.graphics.Color.red(pixel) in 150..220 && android.graphics.Color.green(pixel) in 170..235 && android.graphics.Color.blue(pixel)>200) outlinePixels++
     }
     if (outlinePixels > field.width * 0.75f) outlineRows += y
    }
    assertTrue("Native input capture must contain the real Aladdin glyphs ($name): $glyphPixels",glyphPixels>field.width*field.height/200)
    assertTrue("Native input capture must contain both top and bottom outline ($name): $outlineRows",
     outlineRows.isNotEmpty() && outlineRows.last() - outlineRows.first() > field.height / 3)
    File(out,"$name-input-proof.txt").writeText("bounds=$field\nglyphPixels=$glyphPixels\noutlineRows=$outlineRows\nPixelCopy=$result\n")
   }
   bitmap.recycle()
  }
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2RegularProfileRenderTest:ProfileV2Host() {@Test fun render()=render(1f,"regular-profile","profile")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h761dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2ReferenceProfileRenderTest:ProfileV2Host() {@Test fun render()=render(1f,"reference-profile","profile")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2RegularEditRenderTest:ProfileV2Host() {@Test fun render()=render(1f,"regular-edit","edit")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2RegularPickerRenderTest:ProfileV2Host() {@Test fun render()=render(1f,"regular-picker","picker")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2SmallProfileRenderTest:ProfileV2Host() {@Test fun render()=render(1f,"small-profile","profile")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2SmallEditRenderTest:ProfileV2Host() {@Test fun render()=render(1f,"small-edit","edit")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2SmallPickerRenderTest:ProfileV2Host() {@Test fun render()=render(1f,"small-picker","picker")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2FontProfileRenderTest:ProfileV2Host() {@Test fun render()=render(2f,"font-profile","profile")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2FontEditRenderTest:ProfileV2Host() {@Test fun render()=render(2f,"font-edit","edit")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2FontPickerRenderTest:ProfileV2Host() {@Test fun render()=render(2f,"font-picker","picker")}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2ManyAvatarRenderTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun boundedGridKeepsApplyVisible() {
  // Synthetic IDs exercise capacity only. No production catalog or unlock is added.
  val ids=(1..30).map {"fixture-$it"}
  var applied:String?=null
  compose.setContent {MaterialTheme {ProfileAvatarPicker(ids,ids.first(),{}, {applied=it})}}
  compose.onNodeWithTag("avatar-apply").assertIsDisplayed()
  compose.onNodeWithTag("avatar-grid").performScrollToIndex(29)
  compose.onNodeWithTag("avatar-fixture-30").performClick()
  compose.onNodeWithTag("avatar-apply").assertIsDisplayed().performClick()
  assertEquals("fixture-30",applied)
 }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2ColdProfileRenderTest:ProfileV2Host() {@Test fun render()=cold("final-profile",false)}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2ColdEditRenderTest:ProfileV2Host() {@Test fun render()=cold("final-edit",true)}

@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2ColdSmallEditRenderTest:ProfileV2Host() {@Test fun render()=cold("cold-small-edit",true)}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileV2ColdFontEditRenderTest:ProfileV2Host() {@Test fun render()=cold("cold-font-edit",true,2f)}
