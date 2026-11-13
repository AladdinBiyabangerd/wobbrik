package com.aladdin.wobbrik

/** Route policy shared by every game entry; account content never renders a game route. */
internal object GameNavigation {
    fun accountPage(page: String): String = when (page) {
        "account", "collection", "achievements" -> page
        else -> "home"
    }

    fun completed(page: String, playing: Boolean, reconciledWithoutActiveAttempt: Boolean): Boolean =
        page == "game" && playing && reconciledWithoutActiveAttempt

    fun pauseOnBack(page: String, choosingFinish: Boolean): Boolean =
        page == "game" && !choosingFinish
}
