package com.aladdin.wobbrik

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.GameSnapshot

/** Route policy shared by every game entry; account content never renders a game route. */
internal object GameNavigation {
    /** Display only: cached values never grant permission to start or mutate an attempt. */
    fun showAcceptedHome(state: AuthenticatedRecoveryOwner.State?, cached: GameSnapshot?): Boolean =
        state is AuthenticatedRecoveryOwner.State.Recovering && cached != null

    fun accountPage(page: String): String = when (page) {
        "account", "collection", "achievements" -> page
        else -> "home"
    }

    fun completed(page: String, playing: Boolean, reconciledWithoutActiveAttempt: Boolean): Boolean =
        page == "game" && playing && reconciledWithoutActiveAttempt

    fun pauseOnBack(page: String, choosingFinish: Boolean): Boolean =
        page == "game" && !choosingFinish
}
