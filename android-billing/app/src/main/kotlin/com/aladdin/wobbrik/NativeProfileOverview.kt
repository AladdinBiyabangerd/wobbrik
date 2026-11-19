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
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val shape = RoundedCornerShape(GameTokens.CardRadius)
    androidx.compose.material3.Surface(onClick = onClick, enabled = enabled, interactionSource = interaction,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = modifier.heightIn(min = GameTokens.Target).collectionPress(interaction, enabled)) {
        Box(Modifier.fillMaxWidth().padding(vertical = GameTokens.Quarter).clip(shape)
            .background(Brush.verticalGradient(listOf(GameTokens.MobileHome.Rim, GameTokens.InventoryCollection.DockBottom, GameTokens.InventoryCollection.PanelBottom)))
            .border(GameTokens.Hairline * 2, if(enabled) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape)
            .padding(horizontal = GameTokens.Small, vertical = GameTokens.Small), contentAlignment = Alignment.Center) {
            Text(label, color = if(enabled) GameTokens.Text else GameTokens.Muted, fontFamily = GameTypeface,
                fontSize = GameTokens.Body, fontWeight = FontWeight.ExtraBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
internal fun NativeProfileOverview(profile: PublicProfile, level: String, xp: Long, best: String,
    progress: @Composable () -> Unit, showEdit: Boolean, enabled: Boolean, onEdit: () -> Unit) {
    val large = LocalDensity.current.fontScale > 1f
    val compact = mobileUiSizing().compact
    val portraits = LocalAccountPortraitArt.current ?: rememberResourceArt(R.drawable.account_portraits_collection_v1)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GameTokens.Small)) {
        Column(verticalArrangement = Arrangement.spacedBy(-GameTokens.Small)) {
        // Folded ribbon and central crown are native geometry, independent of text size.
        Box(Modifier.fillMaxWidth().height(GameTokens.Target).zIndex(1f).testTag("profile-ribbon"), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxWidth(.72f).height(GameTokens.Target)) {
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
            ProfileCollectionPanel(Modifier.testTag("profile-panel")) {
                @Composable fun identity() {
                    Text(profile.displayName, Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), softWrap=false,
                        color=GameTokens.Text, fontSize=GameTokens.Title, fontWeight=FontWeight.ExtraBold,
                        textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                    NativeProfileLevel(level)
                }
                Row(Modifier.fillMaxWidth().padding(GameTokens.Small), verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
                    NativeAccountPortrait(profile.avatarId, Modifier.size(if(compact || large) GameTokens.Profile.CompactPortrait else GameTokens.Profile.OverviewPortrait), resource=portraits)
                    Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) { identity() }
                }
            }
        }
        ProfileCollectionPanel {
            Column(Modifier.padding(GameTokens.Small), verticalArrangement=Arrangement.spacedBy(GameTokens.Small)) { progress() }
        }
        @Composable fun stat(value:String,label:String,crown:Boolean,modifier:Modifier) {
            ProfileCollectionPanel(modifier) {
                Column(Modifier.fillMaxWidth().padding(GameTokens.Small), horizontalAlignment=Alignment.CenterHorizontally) {
                    if(crown) NativeProfileCrown(Modifier.size(GameTokens.Target)) else NativeProfileXp(Modifier.size(GameTokens.Target))
                    Text(value,Modifier.horizontalScroll(rememberScrollState()),softWrap=false,color=GameTokens.Text,
                        fontSize=GameTokens.Title,fontWeight=FontWeight.ExtraBold)
                    Text(label,color=GameTokens.Muted,fontWeight=FontWeight.Bold,fontSize=GameTokens.Body)
                }
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
            stat(best,"Rekord",true,Modifier.weight(1f));stat(xp.toString(),"Təcrübə",false,Modifier.weight(1f))
        }
        if(showEdit) ProfileCollectionPanel {
            Row(Modifier.padding(horizontal=GameTokens.Small),verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(GameTokens.Small)) {
                NativeAccountPortrait(profile.avatarId,Modifier.size(GameTokens.Target-GameTokens.Small),resource=portraits)
                Text("Ad və avatar",Modifier.weight(1f),color=GameTokens.Text,fontWeight=FontWeight.Bold,fontSize=GameTokens.Body)
                NativeProfileAction("Dəyiş",enabled,Modifier.width(GameTokens.Target*2).testTag("profile-edit"),onEdit)
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
                moveTo(size.width*.5f,size.height*.08f)
                quadraticTo(size.width*.6f,size.height*.14f,size.width*.71f,size.height*.25f)
                lineTo(size.width*.69f,size.height*.64f)
                quadraticTo(size.width*.68f,size.height*.77f,size.width*.5f,size.height*.94f)
                quadraticTo(size.width*.32f,size.height*.77f,size.width*.31f,size.height*.64f)
                lineTo(size.width*.29f,size.height*.25f)
                quadraticTo(size.width*.4f,size.height*.14f,size.width*.5f,size.height*.08f);close()
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
            // Soft enamel scallops, rather than sharp star points.
            val points=(0 until 20).map { i ->
                val angle=Math.PI*2*i/20-Math.PI/2
                val radius=if(i%2==0) .49f else .40f
                Offset(size.width*(.5f+radius*kotlin.math.cos(angle).toFloat()),
                    size.height*(.5f+radius*kotlin.math.sin(angle).toFloat()))
            }
            val seal=Path().apply {
                val first=(points.last()+points.first())/2f
                moveTo(first.x,first.y)
                points.forEachIndexed { i,p ->
                    val end=(p+points[(i+1)%points.size])/2f
                    quadraticTo(p.x,p.y,end.x,end.y)
                }
                close()
            }
            drawPath(seal,Brush.verticalGradient(listOf(GameTokens.MobileHome.PlayTop,GameTokens.MobileHome.PlayBottom)))
            drawPath(seal,GameTokens.MobileHome.Rim,style=Stroke(GameTokens.Quarter.toPx()))
            drawPath(seal,GameTokens.MobileHome.PlayTop,style=Stroke(GameTokens.Hairline.toPx()))
            drawCircle(Brush.radialGradient(listOf(GameTokens.MobileHome.PlayTop.copy(alpha=.65f),
                GameTokens.MobileHome.PlayBottom.copy(alpha=0f)),center=Offset(size.width*.3f,size.height*.22f),radius=size.width*.65f),
                radius=size.width*.32f)

        }
        Text("XP",color=GameTokens.Background,fontFamily=GameTypeface,fontWeight=FontWeight.ExtraBold,fontSize=GameTokens.Label)
    }
}

@Composable
internal fun NativeProfileShortcut(label: String, records: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    androidx.compose.material3.Surface(onClick=onClick, interactionSource=interaction,
        color=androidx.compose.ui.graphics.Color.Transparent,
        modifier=modifier.heightIn(min=GameTokens.Target).collectionPress(interaction)) {
        ProfileCollectionPanel {
            Row(Modifier.fillMaxWidth().padding(GameTokens.Small),verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(GameTokens.Quarter,Alignment.CenterHorizontally)) {
                if(records) NativeProfileCrown(Modifier.size(GameTokens.Inset))
                else MobileMenuIcon(MobileIcon.Trophy,Modifier.size(GameTokens.Inset))
                Text(label,Modifier.weight(1f,fill=false).horizontalScroll(rememberScrollState()),softWrap=false,color=GameTokens.Text,fontFamily=GameTypeface,
                    fontWeight=FontWeight.Bold,fontSize=GameTokens.Label)
            }
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
        drawPath(crown, GameTokens.Background.copy(alpha=.65f), style=Stroke(GameTokens.Quarter.toPx()))
        drawPath(crown,Brush.linearGradient(listOf(GameTokens.MobileHome.GoldLight,GameTokens.MobileHome.Gold,GameTokens.MobileHome.GoldLight,GameTokens.MobileHome.Gold),
            start=Offset.Zero,end=Offset(size.width,size.height)))
        drawPath(crown,GameTokens.MobileHome.GoldLight,style=Stroke(GameTokens.Hairline.toPx()))
        val band=Path().apply {
            moveTo(size.width*.17f,size.height*.69f)
            quadraticTo(size.width*.5f,size.height*.8f,size.width*.83f,size.height*.69f)
            lineTo(size.width*.81f,size.height*.79f)
            quadraticTo(size.width*.5f,size.height*.9f,size.width*.19f,size.height*.79f);close()
        }
        drawPath(band,Brush.verticalGradient(listOf(GameTokens.MobileHome.GoldLight,GameTokens.MobileHome.Gold)))
        val glint=Path().apply {
            moveTo(size.width*.13f,size.height*.32f);lineTo(size.width*.22f,size.height*.68f)
            quadraticTo(size.width*.25f,size.height*.7f,size.width*.28f,size.height*.7f)
        }
        drawPath(glint,GameTokens.MobileHome.GoldLight,style=Stroke(GameTokens.Hairline.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round))
        drawPath(band,GameTokens.MobileHome.GoldLight,style=Stroke(GameTokens.Hairline.toPx()))
        for (point in listOf(Offset(.08f,.28f),Offset(.5f,.13f),Offset(.92f,.28f))) {
            drawCircle(GameTokens.MobileHome.GoldLight,size.width*.065f,Offset(size.width*point.x,size.height*point.y))
        }
    }
}
