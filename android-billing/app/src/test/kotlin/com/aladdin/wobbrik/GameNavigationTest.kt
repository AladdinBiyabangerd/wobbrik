package com.aladdin.wobbrik

import org.junit.Assert.*
import org.junit.Test
import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.GameSnapshot

class GameNavigationTest {
    @Test fun backgroundRecoveryKeepsAcceptedHomeButInitialLoadAndFailuresRemainVisible() {
        val accepted = GameSnapshot("wobbrik-v1", 7, 123, 45, null)
        assertTrue(GameNavigation.showAcceptedHome(AuthenticatedRecoveryOwner.State.Recovering, accepted))
        assertFalse(GameNavigation.showAcceptedHome(AuthenticatedRecoveryOwner.State.Recovering, null))
        assertFalse(GameNavigation.showAcceptedHome(AuthenticatedRecoveryOwner.State.SignedOut, accepted))
        assertFalse(GameNavigation.showAcceptedHome(AuthenticatedRecoveryOwner.State.Error(Exception("offline")), accepted))
        assertFalse(GameNavigation.showAcceptedHome(null, accepted))
    }

    @Test fun allAccountEntrypointsResolveToOneCanonicalDestination() {
        for (page in listOf("home", "account", "collection", "achievements")) {
            assertEquals(page, GameNavigation.accountPage(page))
        }
        // A finish/recovery fallback and unsupported saved routes must never show a second home.
        for (page in listOf("game", "inventory", "market", "city", "obsolete")) {
            assertEquals("home", GameNavigation.accountPage(page))
        }
    }

    @Test fun finishReturnsHomeOnlyAfterReconciliationOfAnEnteredGame() {
        assertTrue(GameNavigation.completed("game", true, true))
        assertFalse(GameNavigation.completed("game", true, false)) // active, loading or failed
        assertFalse(GameNavigation.completed("game", false, true)) // start is still being dispatched
        for (page in listOf("home", "account", "collection", "achievements")) {
            assertFalse(GameNavigation.completed(page, true, true)) // no unrelated redirect
        }
    }

    @Test fun systemBackPausesGameplayButLeavesTheFinishPickerAndAccountPages() {
        assertTrue(GameNavigation.pauseOnBack("game", false))
        assertFalse(GameNavigation.pauseOnBack("game", true))
        for (page in listOf("home", "account", "collection", "achievements")) {
            assertFalse(GameNavigation.pauseOnBack(page, false))
        }
    }
}
