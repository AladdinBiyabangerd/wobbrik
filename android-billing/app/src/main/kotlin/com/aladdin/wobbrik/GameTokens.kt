package com.aladdin.wobbrik

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Port of docs/design/mobile-v1/tokens.css. Scene dimensions use engine units. */
internal object GameTokens {
    val Background = Color(0xFF061831)
    val Surface = Color(0xFF102C53)
    val Raised = Color(0xFF193D69)
    val Text = Color(0xFFF4F7FF)
    val Muted = Color(0xFFB3C8EC)
    val Accent = Color(0xFF28E5EF)
    val Success = Color(0xFF32EDBD)
    val Danger = Color(0xFFFFB4C8)
    // Fine outline and dim treatment used by the approved game/pause design.
    val Hairline = 1.dp
    const val DimOpacity = 0.65f
    val Small = 8.dp
    val Gap = 16.dp
    val Inset = 24.dp
    val CardRadius = 24.dp
    val ControlRadius = 16.dp
    val Target = 48.dp
    // Expanded breakpoint from tablet-v1; logo follows the existing panel scale.
    val ExpandedWidth = 840.dp
    val PanelWidth = 400.dp
    val Body = 16.sp
    val Title = 24.sp
    val Score = 32.sp
}
