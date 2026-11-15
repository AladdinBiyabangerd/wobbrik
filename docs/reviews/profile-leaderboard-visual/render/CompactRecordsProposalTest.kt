package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import appbackend.wobbrik.game.LeaderboardEntry
import java.io.File
import java.time.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Approval specimen only: synthetic content; pagination states are driven by the host, never a backend adapter. Never in production sourceSets. */
abstract class CompactRecordsProposalHost {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(font:Float,name:String,moreStage:String?=null) {
  var stage by mutableStateOf(if(moreStage==null) "static" else "ready")
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
    MaterialTheme { CompactRecordsProposal(stage) {stage="loading"} }
   }
  }
  for(tag in listOf("dock-account","dock-more","dock-refresh"))
   compose.onNodeWithTag(tag).assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target).assertWidthIsAtLeast(GameTokens.Target)
  if(moreStage!=null) {
   compose.onNodeWithTag("dock-more").performClick()
   compose.onNodeWithTag("dock-more").assertIsNotEnabled()
   if(font<=1f) compose.onNodeWithText("Yüklənir").assertIsDisplayed()
   if(moreStage!="loading") {
    compose.runOnIdle {stage=moreStage}
    // Verify the first page still exists, then reveal its appended continuation.
    compose.onNodeWithTag("proposal-list").performScrollToIndex(if(font>1f) 0 else 1)
    compose.onNodeWithText("Aydan").assertIsDisplayed()
    compose.onNodeWithTag("proposal-list").performScrollToIndex(if(font>1f) 3 else 4)
    compose.onNodeWithText("Nigar").assertIsDisplayed()
    compose.onNodeWithText("Sən").assertIsDisplayed()
    if(moreStage=="end") compose.onNodeWithTag("dock-more").assertIsNotEnabled()
    else compose.onNodeWithTag("dock-more").assertIsEnabled()
   }
  }
  compose.waitForIdle()
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
   val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/profile-leaderboard-visual/compact-proposal").apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   bitmap.recycle()
  }
 }
}

@Composable
private fun CompactRecordsProposal(stage:String="static",onMore:()->Unit={}) {
 val large=LocalDensity.current.fontScale>1f
 val entries=listOf(Triple("Əli","builder",42),Triple("Aydan","pilot",38),Triple("Murad","architect",35)).mapIndexed {i,e->
  LeaderboardEntry((i+1).toLong(),"preview-$i",e.third.toLong(),Instant.EPOCH,false,e.first,e.second)
 }
 CommunityFrame("Rekordlar",{}) {
  val tabs:@Composable (Modifier)->Unit={modifier->
   HomeButton("Həftəlik",modifier=modifier,primary=true,leading={CommunityCalendarArt(true,Modifier.size(GameTokens.Inset))}) {}
   HomeButton("Bütün vaxtlar",modifier=modifier,leading={Image(painterResource(R.drawable.ui_trophy),null,Modifier.size(GameTokens.Inset))}) {}
  }
  if(large) Column(verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {tabs(Modifier.fillMaxWidth())}
  else Surface(color=GameTokens.Raised,shape=RoundedCornerShape(GameTokens.CardRadius)) {Row {tabs(Modifier.weight(1f).heightIn(min=GameTokens.MobileUi.PrimaryTarget))}}
  Text(if(large) "Həftə: 2026-11-09" else "Həftə: 2026-11-09 · Bakı vaxtı",color=GameTokens.Muted,fontSize=GameTokens.Caption)
  LazyColumn(Modifier.weight(1f).testTag("proposal-list"),verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   if(!large) item {LeaderboardPodium(entries)}
   items(entries) {entry-> ProposalRow(entry)}
   if(stage=="static") item {ProposalRow(LeaderboardEntry(7,"preview-self",28,Instant.EPOCH,true,"Əli","builder"))}
   if(stage in setOf("appended","end")) {
    val next=listOf(Triple("Nigar","pilot",34),Triple("Rauf","architect",32),Triple("Leyla","pilot",30),Triple("Əli","builder",28),Triple("Orxan","architect",26))
    items(next.mapIndexed {i,e->LeaderboardEntry((i+4).toLong(),"next-$i",e.third.toLong(),Instant.EPOCH,i==3,e.first,e.second)}) {ProposalRow(it)}
   }
  }
  ProposalDock(large,stage,onMore)
 }
}

@Composable
internal fun ProposalRow(entry:LeaderboardEntry) {
 val large=LocalDensity.current.fontScale>1f
 Surface(shape=RoundedCornerShape(GameTokens.ControlRadius),color=GameTokens.Surface,
  border=BorderStroke(GameTokens.Hairline,if(entry.isSelf) GameTokens.Accent else GameTokens.Raised)) {
  Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(GameTokens.Raised,GameTokens.Surface))).padding(GameTokens.Small),
   verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   LeaderboardRank(entry.rank)
   PublicAvatar(entry.avatarId,Modifier.size(GameTokens.Target))
   Column(Modifier.weight(1f)) {
    Text(entry.displayName,color=GameTokens.Text,fontWeight=FontWeight.Bold)
    if(entry.isSelf) Text("Sən",color=GameTokens.Accent,fontSize=GameTokens.Caption)
    if(large) Text("${entry.floors} mərtəbə",color=GameTokens.Muted)
   }
   if(!large) {CommunityFloorsArt(Modifier.size(GameTokens.Inset));Text(entry.floors.toString(),color=GameTokens.Text,fontWeight=FontWeight.Bold)}
  }
 }
}

@Composable
private fun ProposalDock(large:Boolean,stage:String="static",onMore:()->Unit={}) {
 val loading=stage=="loading"
 val ended=stage=="end"
 Surface(shape=RoundedCornerShape(GameTokens.CardRadius),color=GameTokens.Surface,
  border=BorderStroke(GameTokens.Hairline,GameTokens.Raised)) {
  Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(GameTokens.Raised,GameTokens.Background))).padding(GameTokens.Small),
   verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   Surface(onClick={},shape=RoundedCornerShape(GameTokens.ControlRadius),color=androidx.compose.ui.graphics.Color.Transparent,
    modifier=Modifier.width(GameTokens.Target+GameTokens.Small).heightIn(min=GameTokens.Target).testTag("dock-account")
     .semantics {contentDescription="Hesaba qayıt"}) {
    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
     Image(painterResource(R.drawable.ui_account),null,Modifier.size(if(large) GameTokens.Gap*2 else GameTokens.Inset))
     if(!large) Text("Hesab",color=GameTokens.Muted,fontSize=GameTokens.Caption,fontWeight=FontWeight.Bold)
    }
   }
   Surface(onClick=onMore,enabled=!loading&&!ended,shape=RoundedCornerShape(GameTokens.ControlRadius),color=if(ended) GameTokens.Surface else GameTokens.Accent,
    border=BorderStroke(GameTokens.Hairline,if(ended) GameTokens.Raised else GameTokens.Text),modifier=Modifier.weight(1f).heightIn(min=GameTokens.Target).testTag("dock-more")
     .semantics {if(loading) contentDescription="Nəticələr yüklənir"}) {
    Row(Modifier.background(Brush.verticalGradient(if(ended) listOf(GameTokens.Raised,GameTokens.Surface) else listOf(lerp(GameTokens.Accent,GameTokens.Text,1f-GameTokens.DimOpacity),GameTokens.Accent)))
     .padding(horizontal=GameTokens.Small),verticalAlignment=Alignment.CenterVertically,
     horizontalArrangement=Arrangement.spacedBy(GameTokens.Small,Alignment.CenterHorizontally)) {
     if(loading) CircularProgressIndicator(progress={0.72f},modifier=Modifier.size(GameTokens.Inset),color=GameTokens.Background,strokeWidth=GameTokens.Hairline*2)
     if(!loading||!large) Text(if(loading) "Yüklənir" else if(ended) {if(large) "Bitdi" else "Hamısı yükləndi"} else "Daha çox",
      color=if(ended) GameTokens.Muted else GameTokens.Background,fontSize=if(large) GameTokens.Caption else GameTokens.Hint,fontWeight=FontWeight.ExtraBold)
     if(!large&&!loading&&!ended) Canvas(Modifier.size(GameTokens.Inset)) {
      drawPath(Path().apply {moveTo(size.width/4,size.height*3/8);lineTo(size.width/2,size.height*5/8);lineTo(size.width*3/4,size.height*3/8)},
       GameTokens.Background,style=Stroke(GameTokens.Hairline.toPx()*2,cap=StrokeCap.Round))
     }
    }
   }
   Surface(onClick={},shape=RoundedCornerShape(GameTokens.ControlRadius),color=androidx.compose.ui.graphics.Color.Transparent,
    modifier=Modifier.width(GameTokens.Target+GameTokens.Small).heightIn(min=GameTokens.Target).testTag("dock-refresh")
     .semantics {contentDescription="Yenilə"}) {
    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
     RefreshProposalArt(Modifier.size(if(large) GameTokens.Gap*2 else GameTokens.Inset))
     if(!large) Text("Yenilə",color=GameTokens.Muted,fontSize=GameTokens.Caption,fontWeight=FontWeight.Bold)
    }
   }
  }
 }
}

@Composable
internal fun RefreshProposalArt(modifier:Modifier) {
 Canvas(modifier) {
  val stroke=GameTokens.Hairline.toPx()*2
  drawArc(GameTokens.Text,45f,285f,false,topLeft=Offset(stroke,stroke),size=androidx.compose.ui.geometry.Size(size.width-stroke*2,size.height-stroke*2),
   style=Stroke(stroke,cap=StrokeCap.Round))
  drawPath(Path().apply {moveTo(size.width-stroke,0f);lineTo(size.width-stroke,size.height/3);lineTo(size.width-size.width/3,size.height/3)},
   GameTokens.Text,style=Stroke(stroke,cap=StrokeCap.Round))
 }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactRecordsRegularProposalTest:CompactRecordsProposalHost() {@Test fun proposal()=render(1f,"390-font1")}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactRecordsSmallProposalTest:CompactRecordsProposalHost() {@Test fun proposal()=render(1f,"360-font1")}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactRecordsFontProposalTest:CompactRecordsProposalHost() {@Test fun proposal()=render(2f,"320-font2")}

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactRecordsLoadingFlowTest:CompactRecordsProposalHost() {@Test fun show()=render(1f,"more-loading","loading")}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactRecordsAppendedFlowTest:CompactRecordsProposalHost() {@Test fun show()=render(1f,"more-appended","appended")}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactRecordsEndFlowTest:CompactRecordsProposalHost() {@Test fun show()=render(1f,"more-end","end")}
