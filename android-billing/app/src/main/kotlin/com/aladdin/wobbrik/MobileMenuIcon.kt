package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*

internal enum class MobileIcon { Home, City, Inventory, Market, Trophy, Account, Play, Crown, Coin, Records, Palette, Tree, Layers, Tag, Cart, Refresh, Back, Residents }

/** Native scalable silhouettes, following the approved dock rather than raster crops. */
@Composable
internal fun MobileMenuIcon(icon: MobileIcon, modifier: Modifier = Modifier, tint: Color? = null) {
 Canvas(modifier) { scale(size.width/32f,size.height/32f,pivot=Offset.Zero) { drawMenuGlyph(icon, tint) } }
}
private fun DrawScope.drawMenuGlyph(icon: MobileIcon, tint: Color?) {
 val ink=GameTokens.MobileHome.PanelBottom
 val light=tint ?: GameTokens.Text
 val blue=Brush.verticalGradient(if(tint != null) listOf(tint,tint) else listOf(light,GameTokens.MobileHome.RimLight,GameTokens.MobileHome.Rim),endY=32f)
 fun shape(vararg xy:Float, gold:Boolean=false) {
  val p=Path().apply { moveTo(xy[0],xy[1]);for(i in 2 until xy.size step 2) lineTo(xy[i],xy[i+1]);close() }
  drawPath(p,if(gold) Brush.verticalGradient(listOf(GameTokens.MobileHome.GoldLight,GameTokens.MobileHome.Gold),endY=30f) else blue)
  drawPath(p,light,style=Stroke(0.8f))
 }
 fun block(x:Float,y:Float,w:Float,h:Float) {
  drawRoundRect(blue,Offset(x,y),Size(w,h),CornerRadius(1.5f));drawRoundRect(light,Offset(x,y),Size(w,h),CornerRadius(1.5f),style=Stroke(0.8f))
 }
 when(icon) {
  MobileIcon.Back -> {
   drawLine(light,Offset(21f,5f),Offset(10f,16f),3f,cap=StrokeCap.Round)
   drawLine(light,Offset(10f,16f),Offset(21f,27f),3f,cap=StrokeCap.Round)
  }
  MobileIcon.Cart -> {
   drawLine(light,Offset(2f,4f),Offset(7f,4f),2.5f,cap=StrokeCap.Round)
   drawLine(light,Offset(7f,4f),Offset(11f,23f),2.5f,cap=StrokeCap.Round)
   shape(9f,9f,30f,9f,26f,20f,11f,20f)
   drawCircle(light,2.5f,Offset(13f,28f));drawCircle(light,2.5f,Offset(25f,28f))
  }
  MobileIcon.Refresh -> {
   drawArc(light,40f,290f,false,Offset(5f,5f),Size(22f,22f),style=Stroke(2.5f,cap=StrokeCap.Round))
   shape(19f,2f,30f,3f,29f,14f)
  }
  MobileIcon.Tree -> { shape(16f,2f,5f,14f,10f,14f,3f,23f,13f,23f,13f,30f,19f,30f,19f,23f,29f,23f,22f,14f,27f,14f) }
  MobileIcon.Layers -> {
   shape(3f,11f,16f,3f,29f,11f,16f,19f)
   for(y in listOf(17f,23f)) {drawLine(light,Offset(3f,y),Offset(16f,y+8f),2f);drawLine(light,Offset(16f,y+8f),Offset(29f,y),2f)}
  }
  MobileIcon.Tag -> {shape(3f,4f,16f,4f,30f,18f,18f,30f,3f,15f);drawCircle(ink,2.5f,Offset(10f,10f))}
  MobileIcon.Palette -> {
   drawOval(blue,Offset(2f,3f),Size(28f,25f));drawCircle(ink,5f,Offset(23f,24f))
   for(p in listOf(Offset(9f,10f),Offset(17f,8f),Offset(24f,13f),Offset(9f,19f))) drawCircle(ink,2.5f,p)
  }
  MobileIcon.Records -> { block(4f,20f,5f,9f);block(13f,12f,5f,17f);block(22f,4f,5f,25f);drawLine(light,Offset(2f,30f),Offset(30f,30f),2f) }
  MobileIcon.Home -> shape(3f,14f,16f,3f,29f,14f,25f,14f,25f,29f,19f,29f,19f,20f,13f,20f,13f,29f,7f,29f,7f,14f)
  MobileIcon.City -> {
   shape(4f,29f,4f,18f,10f,14f,10f,29f);shape(12f,29f,12f,7f,20f,3f,20f,29f);shape(23f,29f,23f,20f,28f,17f,28f,29f)
   for(y in listOf(11f,17f,23f)) drawCircle(ink,1.3f,Offset(16f,y))
   drawCircle(ink,1.2f,Offset(7f,22f));drawCircle(ink,1.1f,Offset(25.5f,24f));drawLine(light,Offset(2f,30f),Offset(30f,30f),1f)
  }
  MobileIcon.Inventory -> {
   drawRoundRect(light,Offset(11f,4f),Size(10f,7f),CornerRadius(2f),style=Stroke(2f));block(3f,10f,26f,18f)
   drawLine(ink,Offset(3f,18f),Offset(29f,18f),2f);drawRoundRect(ink,Offset(13f,16f),Size(6f,6f),CornerRadius(1f))
  }
  MobileIcon.Market -> {
   block(5f,14f,22f,15f);drawRect(ink,Offset(13f,20f),Size(6f,9f));shape(2f,16f,7f,4f,25f,4f,30f,16f)
   for(x in listOf(8f,16f,24f)) {drawLine(light,Offset(x,5f),Offset(x,15f),2f);drawCircle(GameTokens.MobileHome.RimLight,3f,Offset(x,16f))}
  }
  MobileIcon.Trophy -> {
   drawRoundRect(light,Offset(3f,6f),Size(26f,12f),CornerRadius(5f),style=Stroke(2f))
   shape(8f,4f,24f,4f,23f,17f,20f,21f,17f,22f,17f,27f,24f,29f,8f,29f,15f,27f,15f,22f,12f,21f,9f,17f);drawCircle(ink,3f,Offset(16f,11f))
  }
  MobileIcon.Residents -> {
   drawCircle(blue,4f,Offset(5f,11f));drawCircle(blue,4f,Offset(27f,11f))
   shape(0f,28f,0f,23f,2f,18f,6f,17f,10f,20f,10f,28f)
   shape(22f,28f,22f,20f,26f,17f,30f,18f,32f,23f,32f,28f)
   drawCircle(blue,5.5f,Offset(16f,8f));drawCircle(light,5.5f,Offset(16f,8f),style=Stroke(.8f))
   shape(7f,30f,7f,23f,10f,17f,14f,16f,16f,18f,18f,16f,22f,17f,25f,23f,25f,30f)
  }
  MobileIcon.Account -> {drawCircle(blue,6.5f,Offset(16f,9f));drawCircle(light,6.5f,Offset(16f,9f),style=Stroke(0.8f));shape(4f,29f,5f,23f,9f,18f,13f,17f,16f,19f,19f,17f,23f,18f,27f,23f,28f,29f)}
  MobileIcon.Play -> {val p=Path().apply{moveTo(9f,3f);lineTo(28f,16f);lineTo(9f,29f);close()};drawPath(p,ink);drawPath(p,GameTokens.MobileHome.Rim,style=Stroke(1f))}
  MobileIcon.Crown -> shape(3f,9f,10f,15f,16f,3f,22f,15f,29f,9f,25f,28f,7f,28f,gold=true)
  MobileIcon.Coin -> {drawCircle(GameTokens.MobileHome.Gold,14f,Offset(16f,16f));drawCircle(GameTokens.MobileHome.GoldLight,11f,Offset(16f,16f),style=Stroke(2f));shape(16f,6f,19f,12f,26f,13f,21f,18f,22f,25f,16f,22f,10f,25f,11f,18f,6f,13f,13f,12f,gold=true)}
 }
}
