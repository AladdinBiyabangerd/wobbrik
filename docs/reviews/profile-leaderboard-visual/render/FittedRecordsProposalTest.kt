package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import appbackend.wobbrik.game.LeaderboardEntry
import java.io.File
import java.time.Instant
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Review-only design; adopts Arrowz's visible-count principle, not its data/rank/country/own-row rules. */
abstract class FittedRecordsProposalHost {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 protected fun render(font:Float,name:String) {
  var shown=0
  var ranks=emptyList<Long>()
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
    MaterialTheme {FittedRecordsProposal {n,visible->shown=n;ranks=visible}}
   }
  }
  compose.waitForIdle()
  assertTrue(shown in 1..20)
  assertEquals(shown,ranks.distinct().size)
  compose.onAllNodesWithTag("dock-more").assertCountEquals(0)
  for(tag in listOf("fit-back","fit-refresh"))
   compose.onNodeWithTag(tag).assertIsDisplayed().assertHeightIsAtLeast(GameTokens.Target).assertWidthIsAtLeast(GameTokens.Target)
  val board=compose.onNodeWithTag("fit-board").fetchSemanticsNode().boundsInRoot
  for(rank in ranks.drop(if(font>1f) 0 else 3)) {
   val bounds=compose.onNodeWithTag("fit-row-$rank").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
   assertTrue("Whole row $rank must fit",bounds.top>=board.top && bounds.bottom<=board.bottom)
  }
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
   val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/profile-leaderboard-visual/fitted-proposal").apply {mkdirs()}
   File(out,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
   File(out,"$name-count.txt").writeText("Visible unique players=$shown; includes podium when present.\nPreview request budget=${shown.coerceIn(4,20)} (no network).\nVisible ranks=$ranks\n")
   bitmap.recycle()
  }
 }
}

@Composable
private fun FittedRecordsProposal(onFit:(Int,List<Long>)->Unit) {
 val board=ImageBitmap.imageResource(R.drawable.leaderboard_approved_board)
 val large=LocalDensity.current.fontScale>1f
 val names=listOf("Əli","Aydan","Murad","Nigar","Rauf","Leyla","Əli","Orxan","Aysel","Elvin","Günel","Tural","Samir","Fidan","Lala","Kamran","Vüsal","Nurlan","Səbinə","Rəşad")
 val rows=names.mapIndexed {i,n->LeaderboardEntry((i+1).toLong(),"sample-$i",listOf(42L,38,35,34,32,30,28,26,25,24,23,22,21,20,19,18,17,16,15,14)[i],Instant.EPOCH,i==6,n,listOf("builder","pilot","architect")[i%3])}
 CompositionLocalProvider(LocalCommunityBoard provides board) {
  Box(Modifier.fillMaxSize().background(GameTokens.Background)) {
   CommunityBackdrop(Modifier.matchParentSize())
   Column(Modifier.fillMaxSize().safeDrawingPadding().padding(GameTokens.Gap),verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
    Box(Modifier.fillMaxWidth().height(GameTokens.Target)) {
     CommunityBackArt(Modifier.align(Alignment.CenterStart).size(GameTokens.Target).testTag("fit-back").clickable {}
      .semantics {contentDescription="Hesaba qayıt"})
     Image(painterResource(R.drawable.wobbrik_logo),"Wobbrik",Modifier.align(Alignment.Center).width(GameTokens.Target*3).height(GameTokens.Target))
     Surface(onClick={},modifier=Modifier.align(Alignment.CenterEnd).size(GameTokens.Target).testTag("fit-refresh").semantics {contentDescription="Yenilə"},
      shape=CircleShape,color=GameTokens.Surface,border=BorderStroke(GameTokens.Hairline,GameTokens.Raised)) {
      Box(Modifier.background(Brush.verticalGradient(listOf(GameTokens.Raised,GameTokens.Surface))),contentAlignment=Alignment.Center) {
       RefreshProposalArt(Modifier.size(GameTokens.Inset))
      }
     }
    }
    Row(Modifier.align(Alignment.CenterHorizontally),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
     Text("›",color=GameTokens.Accent,fontSize=mobileUiSizing().heading,fontWeight=FontWeight.ExtraBold)
     Text("Rekordlar",color=GameTokens.Text,fontSize=mobileUiSizing().heading,fontWeight=FontWeight.ExtraBold)
     Text("‹",color=GameTokens.Accent,fontSize=mobileUiSizing().heading,fontWeight=FontWeight.ExtraBold)
    }
    val tabs:@Composable (Modifier)->Unit={modifier->
     HomeButton("Həftəlik",modifier=modifier,primary=true,leading={CommunityCalendarArt(true,Modifier.size(GameTokens.Inset))}) {}
     HomeButton("Bütün vaxtlar",modifier=modifier,leading={Image(painterResource(R.drawable.ui_trophy),null,Modifier.size(GameTokens.Inset))}) {}
    }
    if(large) Column(verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {tabs(Modifier.fillMaxWidth())}
    else Surface(color=GameTokens.Raised,shape=RoundedCornerShape(GameTokens.CardRadius)) {Row {tabs(Modifier.weight(1f).heightIn(min=GameTokens.MobileUi.PrimaryTarget))}}
    Text(if(large) "Həftə: 2026-11-09" else "Həftə: 2026-11-09 · Bakı vaxtı",color=GameTokens.Muted,fontSize=GameTokens.Caption)
    FittedPreviewBoard(rows,!large,Modifier.weight(1f).fillMaxWidth().testTag("fit-board"),onFit)
   }
  }
 }
}

/** Actual measured Wobbrik art and row heights; no scroll, no partly placed last row. */
@Composable
private fun FittedPreviewBoard(entries:List<LeaderboardEntry>,showPodium:Boolean,modifier:Modifier,onFit:(Int,List<Long>)->Unit) {
 SubcomposeLayout(modifier) {constraints->
  val loose=constraints.copy(minWidth=constraints.maxWidth,minHeight=0,maxHeight=Constraints.Infinity)
  val gap=GameTokens.Small.roundToPx()
  val podiumCount=if(showPodium) 3 else 0
  val head=if(showPodium) subcompose("head") {
   Column(verticalArrangement=Arrangement.spacedBy(GameTokens.Quarter)) {
    LeaderboardPodium(entries.take(3))
    Row(Modifier.fillMaxWidth()) {
     listOf(entries[1],entries[0],entries[2]).forEach {entry->
      Text(entry.displayName,Modifier.weight(1f),color=GameTokens.Text,fontSize=GameTokens.Caption,fontWeight=FontWeight.Bold,
       textAlign=androidx.compose.ui.text.style.TextAlign.Center,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
     }
    }
   }
  }.first().measure(loose) else null
  val dots=subcompose("dots") {
   Canvas(Modifier.fillMaxWidth().height(GameTokens.Inset)) {
    for(i in -1..1) drawCircle(GameTokens.Muted,GameTokens.Hairline.toPx()*2,
     androidx.compose.ui.geometry.Offset(size.width/2+i*GameTokens.Gap.toPx(),size.height/2))
   }
  }.first().measure(loose)
  val limit=constraints.maxHeight-dots.height-gap
  val placed=ArrayList<Pair<LeaderboardEntry,Placeable>>()
  var y=head?.let {it.height+gap}?:0
  for(entry in entries.drop(podiumCount).take(20-podiumCount)) {
   val row=subcompose("row-${entry.resultId}") {Box(Modifier.testTag("fit-row-${entry.rank}")) {ProposalRow(entry)}}.first().measure(loose)
   if(y+row.height>limit) break
   placed+=entry to row
   y+=row.height+gap
  }
  val count=podiumCount+placed.size
  onFit(count,entries.take(podiumCount).map {it.rank}+placed.map {it.first.rank})
  layout(constraints.maxWidth,constraints.maxHeight) {
   var top=0
   head?.let {it.place(0,top);top+=it.height+gap}
   placed.forEach {(_,row)->row.place(0,top);top+=row.height+gap}
   if(entries.size>count) dots.place(0,top)
  }
 }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedRecordsRegularProposalTest:FittedRecordsProposalHost() {@Test fun proposal()=render(1f,"390-font1")}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedRecordsSmallProposalTest:FittedRecordsProposalHost() {@Test fun proposal()=render(1f,"360-font1")}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w320dp-h568dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FittedRecordsFontProposalTest:FittedRecordsProposalHost() {@Test fun proposal()=render(2f,"320-font2")}
