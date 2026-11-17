package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Review-only fixture. No production navigation, settings persistence or server calls. */
abstract class CompactAccountProposalHost {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(settings:Boolean,font:Float,name:String) {
  compose.setContent { val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme {
    val board=androidx.compose.ui.graphics.ImageBitmap.imageResource(R.drawable.account_approved_board)
    CompositionLocalProvider(LocalCommunityBoard provides board) { CompactAccountProposal(settings) }
   }
   }
  }
  compose.waitForIdle()
  if(!settings) compose.onNodeWithTag("footer").assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target)
  compose.onNodeWithTag("back").assertIsDisplayed().assertWidthIsAtLeast(GameTokens.Target).assertHeightIsAtLeast(GameTokens.Target)
  compose.runOnUiThread {
   val view=compose.activity.window.decorView
   fun invalidate(v:android.view.View) {
    v.invalidate()
    if(v.javaClass.name=="androidx.compose.ui.platform.AndroidComposeView") v.javaClass.getMethod("invalidateDescendants").invoke(v)
    if(v is android.view.ViewGroup) for(i in 0 until v.childCount) invalidate(v.getChildAt(i))
   }
   invalidate(view)
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(AndroidCanvas(bitmap))
   val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/account-compact-visual").apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   bitmap.recycle()
  }
 }
}

@Composable private fun CompactAccountProposal(settings:Boolean) {
 Box(Modifier.fillMaxSize().background(GameTokens.Background)) {
  CommunityBackdrop(Modifier.matchParentSize())
  Column(Modifier.fillMaxSize().safeDrawingPadding().padding(GameTokens.Gap),verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   Row(verticalAlignment=Alignment.CenterVertically) {
    Surface(onClick={},modifier=Modifier.size(GameTokens.Target).testTag("back"),color=GameTokens.Surface,shape=RoundedCornerShape(GameTokens.ControlRadius)) {
     Box(contentAlignment=Alignment.Center) {Text("‹",fontSize=GameTokens.Title,color=GameTokens.Text)}
    }
    Text(if(settings) "Parametrlər" else "Hesab",Modifier.padding(start=GameTokens.Small),fontSize=mobileUiSizing().heading,color=GameTokens.Text,fontWeight=FontWeight.Bold)
   }
   Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
    if(settings) {
     SettingsSection("SƏS VƏ HİSS")
     CompactSetting("Musiqi",true)
     CompactSetting("Səs effektləri",true)
     CompactSetting("Titrəmə",false)
     SettingsSection("OYUN")
     CompactSetting("Dil",value="Azərbaycan")
     SettingsSection("DƏSTƏK")
     CompactSetting("Dəstək")
     CompactSetting("Məxfilik")
    } else {
     ApprovedAccountHero(Modifier.fillMaxWidth().height(mobileUiSizing().detailArt))
     Row(Modifier.align(Alignment.CenterHorizontally),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
      val avatarBoard=androidx.compose.ui.graphics.ImageBitmap.imageResource(R.drawable.leaderboard_approved_board)
      CompositionLocalProvider(LocalCommunityBoard provides avatarBoard) {PublicAvatar("builder",Modifier.size(GameTokens.Target))}
      Text("İnşaatçı",color=GameTokens.Text,fontSize=mobileUiSizing().heading,fontWeight=FontWeight.Bold)
     }
     Text("Səviyyə 7 · 240 XP",Modifier.align(Alignment.CenterHorizontally),color=GameTokens.Muted,fontSize=GameTokens.Body)
     LinearProgressIndicator(progress={0.6f},modifier=Modifier.fillMaxWidth(),color=GameTokens.Accent,trackColor=GameTokens.Raised)
     Text("Rekord 28",Modifier.align(Alignment.CenterHorizontally),color=GameTokens.Text,fontSize=GameTokens.Body)
     CompactAction("Profili dəyiş",R.drawable.ui_account)
     CompactAction("Nailiyyətlər",R.drawable.ui_trophy)
     CompactAction("Rekordlar",R.drawable.ui_crown)
     CompactAction("Parametrlər",R.drawable.ui_account)
    }
   }
   if(!settings) Surface(onClick={},modifier=Modifier.fillMaxWidth().heightIn(min=GameTokens.Target).testTag("footer"),color=GameTokens.Accent,shape=RoundedCornerShape(GameTokens.ControlRadius)) {
    Box(Modifier.padding(GameTokens.Small),contentAlignment=Alignment.Center) {Text(if(settings) "Geri" else "Ana ekran",color=GameTokens.Background,fontSize=GameTokens.Body,fontWeight=FontWeight.Bold)}
   }
  }
 }
}
@Composable private fun CompactGroup(content:@Composable ColumnScope.()->Unit) {
 Surface(color=GameTokens.Surface,shape=RoundedCornerShape(GameTokens.ControlRadius),border=BorderStroke(GameTokens.Hairline,GameTokens.Raised)) {Column(content=content)}
}
@Composable private fun SettingsSection(label:String) {
 Text(label,Modifier.padding(top=GameTokens.Small,start=GameTokens.Quarter),color=GameTokens.Muted,fontSize=GameTokens.Label,fontWeight=FontWeight.Bold)
}
@Composable private fun CompactSetting(label:String,toggle:Boolean?=null,value:String?=null) {
 Surface(color=GameTokens.Surface,shape=RoundedCornerShape(GameTokens.ControlRadius),border=BorderStroke(GameTokens.Hairline,GameTokens.Raised)) {
 Row(Modifier.fillMaxWidth().heightIn(min=GameTokens.Target+GameTokens.Gap).padding(GameTokens.Small),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
  Box(Modifier.size(GameTokens.Target-GameTokens.Small).background(GameTokens.Background,androidx.compose.foundation.shape.CircleShape),contentAlignment=Alignment.Center) {
   Icon(settingsIcon(label),null,Modifier.size(GameTokens.Inset),tint=GameTokens.Text)
  }
  Column(Modifier.weight(1f)) {
   Text(label,fontSize=GameTokens.Body,color=GameTokens.Text,fontWeight=FontWeight.Bold)
   if(value!=null && LocalDensity.current.fontScale>1f) Text(value,color=GameTokens.Muted,fontSize=GameTokens.Label)
  }
  if(toggle!=null) Box(Modifier.size(GameTokens.Target),contentAlignment=Alignment.Center) {
   Box(Modifier.size(GameTokens.Target,GameTokens.Inset).background(if(toggle) GameTokens.Accent else GameTokens.Raised,androidx.compose.foundation.shape.CircleShape),contentAlignment=if(toggle) Alignment.CenterEnd else Alignment.CenterStart) {
    Box(Modifier.padding(GameTokens.Quarter).size(GameTokens.Gap).background(if(toggle) GameTokens.Background else GameTokens.Muted,androidx.compose.foundation.shape.CircleShape))
   }
  }
  else {
   if(value!=null && LocalDensity.current.fontScale<=1f) Text(value,fontSize=GameTokens.Label,color=GameTokens.Muted)
   Text("›",fontSize=GameTokens.Title,color=GameTokens.Muted)
  }
 }
 }
}
@Composable private fun CompactAction(label:String,icon:Int) {
 Surface(onClick={},color=GameTokens.Surface,shape=RoundedCornerShape(GameTokens.ControlRadius),border=BorderStroke(GameTokens.Hairline,GameTokens.Raised)) {
  Row(Modifier.fillMaxWidth().heightIn(min=GameTokens.Target).padding(GameTokens.Small),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   Image(painterResource(icon),null,Modifier.size(GameTokens.Inset))
   Text(label,Modifier.weight(1f),fontSize=GameTokens.Body,color=GameTokens.Text,fontWeight=FontWeight.Bold)
   Text("›",color=GameTokens.Muted,fontSize=GameTokens.Body)
  }
 }
}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactAccountRegularTest:CompactAccountProposalHost() {@Test fun preview()=render(false,1f,"account-390")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactSettingsRegularTest:CompactAccountProposalHost() {@Test fun preview()=render(true,1f,"settings-390")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactAccountSmallTest:CompactAccountProposalHost() {@Test fun preview()=render(false,1f,"account-360")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactSettingsSmallTest:CompactAccountProposalHost() {@Test fun preview()=render(true,1f,"settings-360")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactAccountFontTest:CompactAccountProposalHost() {@Test fun preview()=render(false,2f,"account-font2")}
@RunWith(RobolectricTestRunner::class) @Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactSettingsFontTest:CompactAccountProposalHost() {@Test fun preview()=render(true,2f,"settings-font2")}

private fun settingsIcon(label:String):androidx.compose.ui.graphics.vector.ImageVector {
 val paths=when(label) {
 "Musiqi" -> listOf("M9 18V5l12-2v13","M3.0,18.0a3.0,3.0 0 1 0 6.0,0a3.0,3.0 0 1 0 -6.0,0z","M15.0,16.0a3.0,3.0 0 1 0 6.0,0a3.0,3.0 0 1 0 -6.0,0z")
 "Səs effektləri" -> listOf("M11 5 6 9H2v6h4l5 4V5Z","M15.5 8.5a5 5 0 0 1 0 7","M19 5a10 10 0 0 1 0 14")
 "Titrəmə" -> listOf("m2 8 2 2-2 2 2 2-2 2","m22 8-2 2 2 2-2 2 2 2","M9.5,5.0h5.0a1.5,1.5 0 0 1 1.5,1.5v11.0a1.5,1.5 0 0 1 -1.5,1.5h-5.0a1.5,1.5 0 0 1 -1.5,-1.5v-11.0a1.5,1.5 0 0 1 1.5,-1.5z")
 "Dil" -> listOf("M2.0,12.0a10.0,10.0 0 1 0 20.0,0a10.0,10.0 0 1 0 -20.0,0z","M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20","M2 12h20")
 "Dəstək" -> listOf("M2.0,12.0a10.0,10.0 0 1 0 20.0,0a10.0,10.0 0 1 0 -20.0,0z","M12 16v-4","M12 8h.01")
 "Məxfilik" -> listOf("M20 13c0 5-3.5 7.5-7.7 9a1 1 0 0 1-.6 0C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.2-2.7a1.2 1.2 0 0 1 1.6 0C14.5 3.8 17 5 19 5a1 1 0 0 1 1 1z","m9 12 2 2 4-4")
 else -> emptyList()
 }
 val b=androidx.compose.ui.graphics.vector.ImageVector.Builder(label,GameTokens.Inset,GameTokens.Inset,24f,24f)
 for(path in paths) b.addPath(androidx.compose.ui.graphics.vector.PathParser().parsePathString(path).toNodes(),stroke=androidx.compose.ui.graphics.SolidColor(GameTokens.Text),strokeLineWidth=2f,strokeLineCap=androidx.compose.ui.graphics.StrokeCap.Round,strokeLineJoin=androidx.compose.ui.graphics.StrokeJoin.Round)
 return b.build()
}
