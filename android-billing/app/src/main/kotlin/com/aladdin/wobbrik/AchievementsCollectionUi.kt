package com.aladdin.wobbrik

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush

internal val achievementTabs = listOf(CommerceTab("Nailiyyətlər", "achievements", MobileIcon.Trophy),
    CommerceTab("Rekordlar", "leaderboard", MobileIcon.Records))

/** Shared native surface for awards and records; illustrations never contain controls. */
@Composable
internal fun AchievementPanel(modifier: Modifier = Modifier, selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(GameTokens.ControlRadius)
    Column(modifier.clip(shape).background(Brush.verticalGradient(listOf(
        GameTokens.InventoryCollection.PanelTop, GameTokens.InventoryCollection.PanelBottom)))
        .border(if(selected) GameTokens.Hairline else GameTokens.InventoryCollection.OutlineWidth,
            if(selected) GameTokens.Accent else GameTokens.InventoryCollection.Outline, shape), content = content)
}
