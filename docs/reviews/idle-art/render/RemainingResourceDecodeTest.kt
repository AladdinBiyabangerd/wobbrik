package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Isolates actual imageResource groups; not a whole-screen or backend timing. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RemainingResourceDecodeTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 @Test fun remainingScreenResourceColdAndRemountCost() {
  var generation by mutableIntStateOf(0)
  val seen=java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Bitmap,Boolean>())
  val groups=listOf("home" to listOf(R.drawable.home_city_cutout_v3),
   "profile" to listOf(R.drawable.profile_v2_board,R.drawable.profile_edit_board,R.drawable.profile_skin_clean,R.drawable.profile_panel_clean),
   "achievements" to listOf(R.drawable.achievement_art_v3),
   "leaderboard" to listOf(R.drawable.leaderboard_approved_board))
  compose.setContent {MaterialTheme {key(generation) {
   groups.forEach {(label,ids)->
    val start=System.nanoTime()
    val bitmaps=ids.map {ImageBitmap.imageResource(it).asAndroidBitmap()}
    val ms=(System.nanoTime()-start)/1_000_000
    SideEffect {
     val added=bitmaps.filter{seen.add(it)}
     println("RESOURCE-DECODE group=$label mount=$generation mainThread=${android.os.Looper.myLooper()==android.os.Looper.getMainLooper()} elapsedMs=$ms newBitmaps=${added.size} newRGBABytes=${added.sumOf{it.allocationByteCount.toLong()}}")
    }
   }
  }}}
  repeat(2){compose.runOnIdle{generation++};compose.waitForIdle()}
 }
}
