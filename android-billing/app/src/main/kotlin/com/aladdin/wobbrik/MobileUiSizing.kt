package com.aladdin.wobbrik

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/** Android configuration dimensions track this app window, including multi-window resizing. */
internal data class MobileUiSizing(val portrait: Boolean, val compact: Boolean) {
    val heading get() = if (portrait && compact) GameTokens.Hint else GameTokens.Title
    val cardArt get() = if (portrait && compact) GameTokens.Target + GameTokens.Inset else GameTokens.Target * 2
    val detailArt get() = if (portrait) GameTokens.Target * (if (compact) 3 else 4) else GameTokens.PanelWidth
    val listPreview get() = if (portrait) GameTokens.Target * 2 + GameTokens.Inset else GameTokens.PanelWidth / 2
}

@Composable
internal fun mobileUiSizing(): MobileUiSizing {
    val window = LocalConfiguration.current
    val portrait = window.screenWidthDp < GameTokens.MediumWidth.value && window.screenHeightDp >= window.screenWidthDp
    return MobileUiSizing(portrait, portrait && (window.screenWidthDp <= GameTokens.MobileUi.CompactWidth.value ||
        window.screenHeightDp <= GameTokens.MobileUi.CompactHeight.value))
}
