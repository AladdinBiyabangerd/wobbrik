package com.aladdin.wobbrik

import androidx.compose.runtime.Composable

/** All menu callers use the user-approved dock; the former renderer is removed. */
@Composable
internal fun MobileMenuNavbar(page: String, active: Boolean, enabled: Boolean,
    onPlay: () -> Unit, onPage: (String) -> Unit) {
    CollectionNavbar(if(enabled) onPlay else null, onPage, page, active)
}
