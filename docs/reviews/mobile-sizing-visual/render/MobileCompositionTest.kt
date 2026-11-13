package com.aladdin.wobbrik

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import appbackend.wobbrik.game.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Full-page design proposal. Review-only fixture, not shipped and not an account adapter. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class, qualifiers="w360dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MobileCompositionTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val out=File("/Users/mac/My Workspace/Wobbrik/docs/reviews/mobile-sizing-visual")
 private fun capture(name:String) {
  compose.waitForIdle()
  compose.runOnUiThread {
   val v=compose.activity.window.decorView
   val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
   v.draw(AndroidCanvas(b))
   File(out,"$name.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG,100,it) }
  }
 }
 private fun render(font:Float,name:String) {
  var inventory by mutableStateOf(false)
  compose.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,font)) {
    MaterialTheme { if(inventory) InventoryProposal() else HomeProposal() }
   }
  }
  compose.onNodeWithText("Oyna").assertIsDisplayed()
  compose.onNodeWithContentDescription("Şəhərim").assertIsDisplayed()
  capture("home-v3-$name")
  compose.runOnIdle { inventory=true }
  compose.onNodeWithText("İnventar").assertIsDisplayed()
  compose.onNodeWithText("Ana ekran").assertIsDisplayed()
  capture("inventory-v3-$name")
  if(name=="360-font1") {
   val w=360; val h=640; val gap=24; val top=48
   val board=Bitmap.createBitmap(w*2+gap*3,h+top+gap,Bitmap.Config.ARGB_8888)
   val c=AndroidCanvas(board); c.drawColor(0xFF061831.toInt())
   val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=0xFFB3C8EC.toInt(); textSize=14f}
   listOf("home" to "ANA EKRAN", "inventory" to "İNVENTAR").forEachIndexed { i,(screen,label) ->
    val x=gap+i*(w+gap)
    c.drawText(label,x.toFloat(),30f,paint)
    val bitmap=BitmapFactory.decodeFile(File(out,"$screen-v3-$name.png").absolutePath)
    c.drawBitmap(bitmap,null,Rect(x,top,x+w,top+h),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
   }
   File(out,"full-pages-v3.png").outputStream().use {board.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
 }
 @Test fun small()=render(1f,"360-font1")
 @Test @Config(qualifiers="w390dp-h844dp-xxhdpi") fun regular()=render(1f,"390-font1")
 @Test @Config(qualifiers="w320dp-h568dp-xxhdpi") fun largeFont()=render(2f,"320-font2")
}

private object ProposalMetrics {
 // All values derive from the existing GameTokens scale; names are local to this unapproved design.
 val ActionHeight=GameTokens.Target+GameTokens.Small
 val HeroMax=GameTokens.Target*7
 val ArtThumb=GameTokens.Target*2
 val ScreenInset=GameTokens.Gap
 val SectionGap=GameTokens.Gap
 val ItemGap=GameTokens.Small
}

@Composable private fun ProposalShell(content:@Composable ColumnScope.()->Unit) {
 Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GameTokens.Surface,GameTokens.Background)))
  .safeDrawingPadding().padding(ProposalMetrics.ScreenInset),verticalArrangement=Arrangement.spacedBy(ProposalMetrics.ItemGap),content=content)
}

@Composable private fun CoinBalance() {
 Row(Modifier.background(GameTokens.Background,RoundedCornerShape(GameTokens.CardRadius)).padding(GameTokens.Small),
  verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Quarter)) {
  Image(painterResource(R.drawable.ui_coin),null,Modifier.size(GameTokens.Inset))
  Text("1250",fontSize=GameTokens.Body,color=GameTokens.Text,fontWeight=FontWeight.Bold,softWrap=false)
 }
}

@Composable private fun HomeProposal() {
 val big=LocalDensity.current.fontScale>1f
 ProposalShell {
  Row(Modifier.fillMaxWidth().heightIn(min=GameTokens.Target),verticalAlignment=Alignment.CenterVertically,
   horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   Image(painterResource(R.drawable.wobbrik_logo),"Wobbrik",Modifier.weight(1f).height(GameTokens.Target),contentScale=ContentScale.Fit,alignment=Alignment.CenterStart)
   CoinBalance()
   Surface(onClick={},modifier=Modifier.size(GameTokens.Target),color=GameTokens.Raised,shape=CircleShape) {
    Box(contentAlignment=Alignment.Center) { Image(painterResource(R.drawable.ui_account),"Hesab",Modifier.size(GameTokens.Inset)) }
   }
  }
  // The content area may scroll at large fonts; both primary actions remain outside it.
  BoxWithConstraints(Modifier.weight(1f)) {
   val available=maxHeight
   Column(Modifier.fillMaxSize().then(if(big) Modifier.verticalScroll(rememberScrollState()) else Modifier),
    verticalArrangement=Arrangement.spacedBy(GameTokens.Small),horizontalAlignment=Alignment.CenterHorizontally) {
    Text("Öz şəhərini qur",color=GameTokens.Text,fontSize=GameTokens.Title,fontWeight=FontWeight.ExtraBold,
     modifier=Modifier.fillMaxWidth().padding(top=GameTokens.Small),textAlign=TextAlign.Center)
    if(!big) Image(painterResource(R.drawable.home_city_cutout_v2),null,
     Modifier.fillMaxWidth().weight(1f).heightIn(max=ProposalMetrics.HeroMax),contentScale=ContentScale.Fit)
    else if(available>GameTokens.PanelWidth) Image(painterResource(R.drawable.home_city_cutout_v2),null,
     Modifier.fillMaxWidth().height(GameTokens.Target*2),contentScale=ContentScale.Fit)
    Surface(color=GameTokens.Background.copy(alpha=GameTokens.DimOpacity),shape=RoundedCornerShape(GameTokens.ControlRadius)) {
     Column(Modifier.fillMaxWidth().padding(GameTokens.Small),verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
      if(big) {
       StatLabel(R.drawable.ui_level,"Səviyyə 7")
       StatLabel(R.drawable.ui_crown,"Rekord 28")
      } else Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceEvenly) {
       StatLabel(R.drawable.ui_level,"Səviyyə 7")
       Box(Modifier.width(GameTokens.Hairline).height(GameTokens.Inset).background(GameTokens.Raised))
       StatLabel(R.drawable.ui_crown,"Rekord 28")
      }
     }
    }
   }
  }
  HomeNavbarProposal()

 }
}

@Composable private fun StatLabel(icon:Int,label:String) {
 Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
  Image(painterResource(icon),null,Modifier.size(GameTokens.Inset))
  Text(label,color=GameTokens.Text,fontSize=GameTokens.Body,fontWeight=FontWeight.SemiBold)
 }
}

@Composable private fun QuietEntry(label:String,icon:Int,modifier:Modifier) {
 Surface(onClick={},modifier=modifier.heightIn(min=GameTokens.Target),color=Color.Transparent,shape=RoundedCornerShape(GameTokens.ControlRadius)) {
  Row(Modifier.padding(horizontal=GameTokens.Small,vertical=GameTokens.Small),verticalAlignment=Alignment.CenterVertically,
   horizontalArrangement=Arrangement.spacedBy(GameTokens.Small,Alignment.CenterHorizontally)) {
   Image(painterResource(icon),null,Modifier.size(GameTokens.Inset))
   Text(label,color=GameTokens.Muted,fontSize=GameTokens.Body,fontWeight=FontWeight.SemiBold,softWrap=false)
  }
 }
}

@Composable private fun ProposalAction(label:String,primary:Boolean=false,icon:Int?=null) {
 val shape=RoundedCornerShape(GameTokens.ControlRadius)
 Button({},modifier=Modifier.fillMaxWidth().heightIn(min=if(primary) ProposalMetrics.ActionHeight else GameTokens.Target)
  .background(Brush.verticalGradient(if(primary) listOf(lerp(GameTokens.Accent,GameTokens.Text,1f-GameTokens.DimOpacity),GameTokens.Accent)
   else listOf(GameTokens.Raised,GameTokens.Surface)),shape),shape=shape,
  border=BorderStroke(GameTokens.Hairline,if(primary) GameTokens.Text else GameTokens.Raised),
  colors=ButtonDefaults.buttonColors(containerColor=Color.Transparent,contentColor=if(primary) GameTokens.Background else GameTokens.Text),
  contentPadding=PaddingValues(GameTokens.Small)) {
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   if(icon!=null) Image(painterResource(icon),null,Modifier.size(GameTokens.Inset))
   if(primary) Text("▶",fontSize=GameTokens.Body)
   Text(label,fontSize=if(primary) GameTokens.Hint else GameTokens.Body,fontWeight=FontWeight.Bold,softWrap=false)
  }
 }
}

private fun proposalBuilding(style:Int,floors:Int)=OwnedBuilding("proposal-$style",1,floors.toLong(),floors*4L,floors*2L,(floors*2.4).toLong(),
 FinishCosmetics.categories.associateWith {"base_${style}_$it"},false,
 AcceptedCityGeometry(CityFloorGeometry(List(floors) {index -> index*2500L}),"proposal",1,"proposal","wobbrik-v1","wobbrik-structural-v1"))

@Composable private fun InventoryProposal() {
 val big=LocalDensity.current.fontScale>1f
 val buildings=remember { listOf(proposalBuilding(1,10),proposalBuilding(2,12),proposalBuilding(3,14),proposalBuilding(4,10)) }
 val names=listOf("İçərişəhər","Kərpic məhəllə","Sahil","Müasir şəhər")
 ProposalShell {
  Row(Modifier.fillMaxWidth().heightIn(min=GameTokens.Target),verticalAlignment=Alignment.CenterVertically,
   horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   Surface(onClick={},modifier=Modifier.size(GameTokens.Target),shape=RoundedCornerShape(GameTokens.ControlRadius),color=GameTokens.Raised) {
    Box(contentAlignment=Alignment.Center) {Text("‹",color=GameTokens.Text,fontSize=GameTokens.Title)}
   }
   if(!big) Text("İnventar",Modifier.weight(1f),fontSize=GameTokens.Title,color=GameTokens.Text,fontWeight=FontWeight.Bold)
   else Spacer(Modifier.weight(1f))
   CoinBalance()
  }
  if(big) Text("İnventar",fontSize=GameTokens.Title,color=GameTokens.Text,fontWeight=FontWeight.Bold)
  // Three existing destinations share one visual toolbar instead of three full-width stacked buttons.
  if(big) Column(verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   Row(horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
    ShopEntry("Hissələr",null,Modifier.weight(1f))
    ShopEntry("Dekor",null,Modifier.weight(1f))
   }
   ShopEntry("Ərazi",null,Modifier.fillMaxWidth())
  } else Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   ShopEntry("Hissələr",R.drawable.ui_inventory,Modifier.weight(1f))
   ShopEntry("Dekor",R.drawable.ui_city,Modifier.weight(1f))
   ShopEntry("Ərazi",R.drawable.ui_home,Modifier.weight(1f))
  }
  Row(Modifier.fillMaxWidth().padding(top=GameTokens.Small,bottom=GameTokens.Quarter),verticalAlignment=Alignment.CenterVertically) {
   Text("Binalar",fontSize=GameTokens.Hint,fontWeight=FontWeight.Bold,color=GameTokens.Text,modifier=Modifier.weight(1f))
   Text("4 bina",fontSize=GameTokens.Caption,color=GameTokens.Muted)
  }
  LazyVerticalGrid(columns=GridCells.Fixed(if(big) 1 else 2),modifier=Modifier.weight(1f),
   horizontalArrangement=Arrangement.spacedBy(GameTokens.Small),verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
   itemsIndexed(buildings) { index,building ->
    Surface(onClick={},shape=RoundedCornerShape(GameTokens.ControlRadius),
     color=GameTokens.Surface,border=BorderStroke(GameTokens.Hairline,GameTokens.Raised)) {
     Column(Modifier.background(Brush.verticalGradient(listOf(GameTokens.Raised,GameTokens.Surface))).padding(GameTokens.Small),
      verticalArrangement=Arrangement.spacedBy(GameTokens.Quarter)) {
      BuildingPreview(building,Modifier.fillMaxWidth().height(if(big) ProposalMetrics.ArtThumb else GameTokens.Target*2+GameTokens.Inset))
      Text(names[index],fontSize=GameTokens.Body,color=GameTokens.Text,fontWeight=FontWeight.SemiBold)
      Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
       Text("${building.floors} mərtəbə",Modifier.weight(1f),fontSize=GameTokens.Caption,color=GameTokens.Muted)
       Text("›",fontSize=GameTokens.Hint,color=GameTokens.Accent)
      }
     }
    }
   }
  }
  ProposalAction("Ana ekran",icon=R.drawable.ui_home)
 }
}

@Composable private fun ShopEntry(label:String,icon:Int?,modifier:Modifier) {
 Surface(onClick={},modifier=modifier.heightIn(min=GameTokens.Target),shape=RoundedCornerShape(GameTokens.ControlRadius),color=GameTokens.Raised) {
  Row(Modifier.padding(GameTokens.Small),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Quarter,Alignment.CenterHorizontally)) {
   if(icon!=null) Image(painterResource(icon),null,Modifier.size(GameTokens.Inset))
   Text(label,fontSize=GameTokens.Caption,color=GameTokens.Text,fontWeight=FontWeight.SemiBold,softWrap=false)
  }
 }
}


/** Proposed navigation: all existing entries plus direct inventory, central Play starts the game. */
@Composable private fun HomeNavbarProposal() {
 BoxWithConstraints(Modifier.fillMaxWidth()) {
  val largeFont=LocalDensity.current.fontScale>1f
  val labels=!largeFont && maxWidth>=GameTokens.PanelWidth-GameTokens.Gap*2
  val barHeight=if(largeFont) GameTokens.Target*2+GameTokens.Gap else GameTokens.Target+GameTokens.Inset+GameTokens.Small
  val navHeight=barHeight+GameTokens.Gap
  val centerWidth=GameTokens.Target+GameTokens.Inset
  Box(Modifier.fillMaxWidth().height(navHeight)) {
   Surface(Modifier.fillMaxWidth().height(barHeight).align(Alignment.BottomCenter),
    shape=RoundedCornerShape(GameTokens.CardRadius),color=GameTokens.Surface,
    border=BorderStroke(GameTokens.Hairline,GameTokens.Raised)) {
    Row(Modifier.fillMaxSize().padding(horizontal=GameTokens.Small),verticalAlignment=Alignment.CenterVertically) {
     NavbarItem("Şəhərim",R.drawable.ui_city,labels,Modifier.weight(1f))
     NavbarItem("İnventar",R.drawable.ui_inventory,labels,Modifier.weight(1f))
     Spacer(Modifier.width(centerWidth))
     NavbarItem("Kolleksiya",R.drawable.ui_medal,labels,Modifier.weight(1f))
     NavbarItem("Nailiyyətlər",R.drawable.ui_trophy,labels,Modifier.weight(1f))
    }
   }
   Column(Modifier.width(centerWidth).align(Alignment.TopCenter),horizontalAlignment=Alignment.CenterHorizontally,
    verticalArrangement=Arrangement.spacedBy(GameTokens.Quarter)) {
    Surface(onClick={},modifier=Modifier.size(GameTokens.Target+GameTokens.Gap).semantics {contentDescription="Oyunu başlat"},
     shape=CircleShape,color=GameTokens.Accent,border=BorderStroke(GameTokens.Hairline*2,GameTokens.Text),
     shadowElevation=GameTokens.Small) {
     Box(Modifier.background(Brush.verticalGradient(listOf(lerp(GameTokens.Accent,GameTokens.Text,1f-GameTokens.DimOpacity),GameTokens.Accent))),
      contentAlignment=Alignment.Center) {Text("▶",fontSize=GameTokens.Title,color=GameTokens.Background,fontWeight=FontWeight.Bold)}
    }
    Text("Oyna",fontSize=GameTokens.Caption,color=GameTokens.Text,fontWeight=FontWeight.Bold,softWrap=false)
   }
  }
 }
}

@Composable private fun NavbarItem(label:String,icon:Int,showLabel:Boolean,modifier:Modifier) {
 Surface(onClick={},modifier=modifier.heightIn(min=GameTokens.Target).semantics {contentDescription=label},
  shape=RoundedCornerShape(GameTokens.ControlRadius),color=Color.Transparent) {
  Column(Modifier.padding(vertical=GameTokens.Small),horizontalAlignment=Alignment.CenterHorizontally,
   verticalArrangement=Arrangement.spacedBy(GameTokens.Quarter,Alignment.CenterVertically)) {
   Image(painterResource(icon),null,Modifier.size(GameTokens.Inset))
   if(showLabel) Text(when(label) { "Nailiyyətlər" -> "Uğurlar"; "Şəhərim" -> "Şəhər"; else -> label },fontSize=GameTokens.Caption,color=GameTokens.Muted,softWrap=false)
  }
 }
}
