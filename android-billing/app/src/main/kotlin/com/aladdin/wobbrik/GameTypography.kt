package com.aladdin.wobbrik

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/** Existing bundled Nunito shared by approved mobile menus and profile. */
internal val GameTypeface = FontFamily(
    Font(R.font.profile_nunito_regular, FontWeight.Normal),
    Font(R.font.profile_nunito_bold, FontWeight.Bold),
    Font(R.font.profile_nunito_black, FontWeight.ExtraBold),
    Font(R.font.profile_nunito_black, FontWeight.Black))
