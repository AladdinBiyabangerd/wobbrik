package com.aladdin.wobbrik

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import appbackend.wobbrik.game.PublicProfile

/** Mobile overview only; server state and editing remain owned by PublicAccountScreen. */
@Composable
internal fun NativeProfileAction(label: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Box(modifier.fillMaxWidth().heightIn(min = GameTokens.Target) .clip(shape)
        .background(Brush.verticalGradient(if (enabled) listOf(GameTokens.MobileHome.PlayTop, GameTokens.Accent, GameTokens.MobileHome.PlayBottom)
            else listOf(GameTokens.Raised, GameTokens.Surface)))
        .border(GameTokens.Hairline, if (enabled) GameTokens.MobileHome.RimLight else GameTokens.Muted, shape)
        .border(GameTokens.Quarter, Brush.verticalGradient(listOf(
            GameTokens.Text.copy(alpha = if (enabled) 1f - GameTokens.DimOpacity else 0f),
            GameTokens.Background.copy(alpha = 0f), GameTokens.MobileHome.Rim.copy(alpha = 1f - GameTokens.DimOpacity))), shape)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, Modifier.padding(GameTokens.Small).horizontalScroll(rememberScrollState()), softWrap = false,
            color = if (enabled) GameTokens.Background else GameTokens.Muted, fontFamily = GameTypeface,
            fontSize = GameTokens.Body, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
internal fun NativeProfileOverview(profile: PublicProfile, level: String, xp: Long, best: String,
    progress: @Composable () -> Unit, showEdit: Boolean, enabled: Boolean, onEdit: () -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    val compact = mobileUiSizing().compact
    val portraits = LocalAccountPortraitArt.current ?: rememberResourceArt(R.drawable.account_portraits_v3)
    @Composable fun portrait(modifier: Modifier) {
        NativeAccountPortrait(profile.avatarId, modifier, resource = portraits)
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(-GameTokens.Small)) {
        // Folded ribbon and central crown are native geometry, independent of text size.
        Box(Modifier.fillMaxWidth().height(GameTokens.Target).zIndex(1f).testTag("profile-ribbon"), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxWidth().height(GameTokens.Target)) {
                fun polygon(vararg xy: Float): Path = Path().apply {
                    moveTo(xy[0]*size.width, xy[1]*size.height)
                    for(i in 2 until xy.size step 2) lineTo(xy[i]*size.width,xy[i+1]*size.height)
                    close()
                }
                val tails = polygon(.13f,.46f,.29f,.4f,.29f,.83f,.12f,.9f,.16f,.67f)
                val right = polygon(.71f,.4f,.87f,.46f,.84f,.67f,.88f,.9f,.71f,.83f)
                val tailFill = Brush.verticalGradient(listOf(GameTokens.Accent, GameTokens.MobileHome.PlayBottom, GameTokens.MobileHome.Rim))
                drawPath(tails, tailFill); drawPath(right, tailFill)
                drawPath(polygon(.25f,.33f,.29f,.7f,.25f,.84f), GameTokens.MobileHome.Rim)
                drawPath(polygon(.75f,.33f,.71f,.7f,.75f,.84f), GameTokens.MobileHome.Rim)
                val ribbon = Path().apply {
                    moveTo(size.width*.25f,size.height*.3f)
                    quadraticTo(size.width*.5f,size.height*.03f,size.width*.75f,size.height*.3f)
                    lineTo(size.width*.75f,size.height*.76f)
                    quadraticTo(size.width*.5f,size.height*.51f,size.width*.25f,size.height*.76f);close()
                }
                drawPath(ribbon, GameTokens.MobileHome.Rim, style=Stroke(GameTokens.Quarter.toPx()))
                drawPath(ribbon, Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop,GameTokens.MobileHome.PlayBottom,GameTokens.MobileHome.Rim)))
                drawPath(ribbon, GameTokens.Accent, style=Stroke(GameTokens.Hairline.toPx()))
                val badge = Path().apply {
                    moveTo(size.width*.45f,size.height*.13f)
                    quadraticTo(size.width*.5f,-size.height*.02f,size.width*.55f,size.height*.13f)
                    quadraticTo(size.width*.59f,size.height*.13f,size.width*.58f,size.height*.36f)
                    lineTo(size.width*.57f,size.height*.65f)
                    quadraticTo(size.width*.5f,size.height*.83f,size.width*.43f,size.height*.65f)
                    lineTo(size.width*.42f,size.height*.36f)
                    quadraticTo(size.width*.41f,size.height*.13f,size.width*.45f,size.height*.13f);close()
                }
                drawPath(badge, GameTokens.MobileHome.Rim, style=Stroke(GameTokens.Quarter.toPx()))
                drawPath(badge, Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight,GameTokens.MobileHome.Rim)))
                drawPath(badge, GameTokens.Accent, style=Stroke(GameTokens.Hairline.toPx()))
            }
            NativeProfileCrown(Modifier.size(GameTokens.Inset + GameTokens.Small).offset(y=-GameTokens.Quarter))
        }
        MenuRelief(Modifier.fillMaxWidth().testTag("profile-panel").border(GameTokens.Hairline * 2,
            Brush.verticalGradient(listOf(GameTokens.MobileHome.Rim, GameTokens.Accent, GameTokens.MobileHome.Rim)),
            RoundedCornerShape(GameTokens.CardRadius)), kind = MenuSkin.Commerce) {
            Column(Modifier.fillMaxWidth().padding(if(compact) GameTokens.Small else GameTokens.Gap),
                verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
                @Composable fun identity() {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(profile.displayName, Modifier.weight(1f).horizontalScroll(rememberScrollState()), softWrap=false,
                            color=GameTokens.Text,fontFamily=GameTypeface,fontSize=GameTokens.Title,fontWeight=FontWeight.ExtraBold)
                        if(showEdit) MenuRelief(Modifier.size(GameTokens.Target).clip(RoundedCornerShape(GameTokens.ControlRadius))
                            .clickable(enabled=enabled,role=Role.Button,onClick=onEdit).semantics { contentDescription="Profili dəyiş" }) {
                            Canvas(Modifier.padding(GameTokens.Small).fillMaxSize()) {
                                val pencil=Path().apply {
                                    moveTo(size.width*.35f,size.height*.7f);lineTo(size.width*.68f,size.height*.18f)
                                    lineTo(size.width*.84f,size.height*.34f);lineTo(size.width*.34f,size.height*.84f)
                                    lineTo(size.width*.14f,size.height*.9f);close()
                                }
                                drawPath(pencil,GameTokens.Text)
                            }
                        }
                    }
                    NativeProfileLevel(level)
                }
                if(large) {
                    portrait(Modifier.size(GameTokens.Profile.CompactPortrait).align(Alignment.CenterHorizontally))
                    identity()
                } else Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
                    portrait(Modifier.size(if(compact) GameTokens.Profile.Portrait else GameTokens.Profile.OverviewPortrait))
                    Column(Modifier.weight(1f)) { identity() }
                }
                HorizontalDivider(color=GameTokens.MobileHome.Rim)
                progress()
                HorizontalDivider(color=GameTokens.MobileHome.Rim)
                @Composable fun stat(value:String,label:String,crown:Boolean,modifier:Modifier) {
                    Column(modifier,horizontalAlignment=Alignment.CenterHorizontally) {
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
                            if(crown) NativeProfileCrown(Modifier.size(GameTokens.Inset + GameTokens.Small))
                            else NativeProfileXp(Modifier.size(GameTokens.Inset + GameTokens.Small))
                            Text(value,Modifier.weight(1f,fill=false).horizontalScroll(rememberScrollState()),softWrap=false,color=GameTokens.Text,
                                fontFamily=GameTypeface,fontSize=GameTokens.Title,fontWeight=FontWeight.ExtraBold)
                        }
                        Text(label,Modifier.horizontalScroll(rememberScrollState()),softWrap=false,color=GameTokens.Muted,
                            fontFamily=GameTypeface,fontWeight=FontWeight.Bold,fontSize=GameTokens.Body)
                    }
                }
                if(large) {
                    stat(best,"Rekord",true,Modifier.fillMaxWidth());stat(xp.toString(),"Təcrübə",false,Modifier.fillMaxWidth())
                } else Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    stat(best,"Rekord",true,Modifier.weight(1f))
                    Box(Modifier.width(GameTokens.Hairline).height(GameTokens.Target).background(GameTokens.MobileHome.Rim))
                    stat(xp.toString(),"Təcrübə",false,Modifier.weight(1f))
                }
                HorizontalDivider(color=GameTokens.MobileHome.Rim)
                if(showEdit) MenuRelief(Modifier.fillMaxWidth(), shape = RoundedCornerShape(GameTokens.ControlRadius), kind = MenuSkin.Commerce) {
                    Row(Modifier.padding(GameTokens.Small),verticalAlignment=Alignment.CenterVertically,
                        horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
                        portrait(Modifier.size(GameTokens.Target))
                        Text("Ad və avatar",Modifier.weight(1f).horizontalScroll(rememberScrollState()),softWrap=false,
                            color=GameTokens.Text,fontWeight=FontWeight.Bold)
                        NativeProfileAction("Dəyiş",enabled,Modifier.width(GameTokens.Target*2).testTag("profile-edit"),onEdit)
                    }
                }
            }
        }
    }
}


@Composable
private fun NativeProfileLevel(level: String) {
    Box(Modifier.width(GameTokens.Profile.CrownWidth).height(GameTokens.Target + GameTokens.Small), contentAlignment=Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val ribbon=Path().apply {
                moveTo(size.width*.06f,size.height*.48f);lineTo(size.width*.94f,size.height*.48f)
                lineTo(size.width*.9f,size.height*.66f);lineTo(size.width*.94f,size.height*.85f)
                lineTo(size.width*.06f,size.height*.85f);lineTo(size.width*.1f,size.height*.66f);close()
            }
            drawPath(ribbon,Brush.verticalGradient(listOf(GameTokens.MobileHome.Rim,GameTokens.MobileHome.PanelTop)))
            for(side in listOf(-1f,1f)) for(i in 0..3) {
                val x=size.width*(.5f+side*(.19f+i*.025f))
                val y=size.height*(.79f-i*.12f)
                rotate(side * (35f + i*8f), Offset(x,y)) {
                    drawOval(Brush.verticalGradient(listOf(GameTokens.MobileHome.GoldLight,GameTokens.MobileHome.Gold)),
                        Offset(x-size.width*.028f,y-size.height*.1f),Size(size.width*.056f,size.height*.2f))
                }
            }
            val shield=Path().apply {
                moveTo(size.width*.5f,size.height*.08f);lineTo(size.width*.71f,size.height*.25f)
                lineTo(size.width*.69f,size.height*.7f);lineTo(size.width*.5f,size.height*.94f)
                lineTo(size.width*.31f,size.height*.7f);lineTo(size.width*.29f,size.height*.25f);close()
            }
            drawPath(shield, GameTokens.Background, style=Stroke((GameTokens.Quarter + GameTokens.Hairline * 2).toPx()))
            drawPath(shield,Brush.verticalGradient(listOf(GameTokens.MobileHome.RimLight,GameTokens.MobileHome.Rim,GameTokens.MobileHome.PanelTop)))
            drawPath(shield,Brush.verticalGradient(listOf(GameTokens.MobileHome.GoldLight,GameTokens.MobileHome.Gold)),
                style=Stroke(GameTokens.Quarter.toPx()))
            drawPath(shield,GameTokens.MobileHome.GoldLight,style=Stroke(GameTokens.Hairline.toPx()))
        }
        Text(level,Modifier.padding(GameTokens.Small).horizontalScroll(rememberScrollState()),softWrap=false,
            color=GameTokens.Text,fontFamily=GameTypeface,fontWeight=FontWeight.ExtraBold,fontSize=GameTokens.Title)
    }
}


@Composable
private fun NativeProfileXp(modifier: Modifier) {
    Box(modifier,contentAlignment=Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val seal=Path().apply {
                for(i in 0 until 20) {
                    val angle=Math.PI*2*i/20-Math.PI/2
                    val radius=if(i%2==0) .49f else .42f
                    val x=size.width*(.5f+radius*kotlin.math.cos(angle).toFloat())
                    val y=size.height*(.5f+radius*kotlin.math.sin(angle).toFloat())
                    if(i==0) moveTo(x,y) else lineTo(x,y)
                };close()
            }
            drawPath(seal,Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop,GameTokens.MobileHome.PlayBottom)))
            drawPath(seal,GameTokens.MobileHome.RimLight,style=Stroke(GameTokens.Hairline.toPx()))
        }
        Text("XP",color=GameTokens.Background,fontFamily=GameTypeface,fontWeight=FontWeight.ExtraBold,fontSize=GameTokens.Label)
    }
}

@Composable
internal fun NativeProfileShortcut(label: String, records: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    MenuRelief(modifier.heightIn(min=GameTokens.Target)
        .clip(RoundedCornerShape(GameTokens.ControlRadius)).clickable(role=Role.Button,onClick=onClick),
        shape=RoundedCornerShape(GameTokens.ControlRadius)) {
        Row(Modifier.align(Alignment.Center).padding(GameTokens.Small),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(GameTokens.Quarter)) {
            if(records) MobileMenuIcon(MobileIcon.Trophy,Modifier.size(GameTokens.Inset))
            else Canvas(Modifier.size(GameTokens.Inset)) {
                val center=Offset(size.width/2,size.height/2)
                for(i in 0..7) {
                    val angle=Math.PI*i/4
                    val direction=Offset(kotlin.math.cos(angle).toFloat(),kotlin.math.sin(angle).toFloat())
                    drawLine(GameTokens.Muted,center+direction*(size.width*.28f),center+direction*(size.width*.47f),GameTokens.Quarter.toPx())
                }
                drawCircle(GameTokens.Muted,size.width*.34f)
                drawCircle(GameTokens.Surface,size.width*.15f)
            }
            Text(label,Modifier.weight(1f,fill=false).horizontalScroll(rememberScrollState()),
                softWrap=false,color=GameTokens.Text,fontFamily=GameTypeface,fontWeight=FontWeight.Bold,fontSize=GameTokens.Body)
        }
    }
}

/** Rounded, shaded gold crown shared by the ribbon and record stat. */
@Composable
private fun NativeProfileCrown(modifier: Modifier) {
    Canvas(modifier) {
        val crown = Path().apply {
            moveTo(size.width*.17f,size.height*.73f)
            lineTo(size.width*.08f,size.height*.28f)
            quadraticTo(size.width*.23f,size.height*.29f,size.width*.31f,size.height*.47f)
            lineTo(size.width*.5f,size.height*.13f)
            lineTo(size.width*.69f,size.height*.47f)
            quadraticTo(size.width*.77f,size.height*.29f,size.width*.92f,size.height*.28f)
            lineTo(size.width*.83f,size.height*.73f)
            quadraticTo(size.width*.5f,size.height*.86f,size.width*.17f,size.height*.73f);close()
        }
        drawPath(crown,Brush.verticalGradient(listOf(GameTokens.MobileHome.GoldLight,GameTokens.MobileHome.Gold,GameTokens.MobileHome.Gold)))
        drawPath(crown,GameTokens.MobileHome.GoldLight,style=Stroke(GameTokens.Hairline.toPx()))
        for (point in listOf(Offset(.08f,.28f),Offset(.5f,.13f),Offset(.92f,.28f))) {
            drawCircle(GameTokens.MobileHome.GoldLight,size.width*.065f,Offset(size.width*point.x,size.height*point.y))
        }
    }
}
