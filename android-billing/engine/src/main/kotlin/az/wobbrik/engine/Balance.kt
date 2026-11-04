package az.wobbrik.engine

import kotlin.math.abs

/** Provisional arcade balance, independent of cosmetic sway and server authority. */
data class Balance(
    val lean: Double = 0.0,
    val dangerEpisode: Boolean = false,
    val rescues: Int = 0,
)

object BalanceRulesV1 {
    const val DANGER = 0.55
    const val STABLE = 0.30
    const val TOPPLE = 1.0
    const val PERFECT_OFFSET = 5.0
    const val SWAY_SPRING = 0.06
    const val SWAY_DAMPING = 0.88
    const val IMPACT = 0.08

    // Floors 1–5 share the forgiving base. Growth after that is gradual and capped.
    fun difficulty(floorNumber: Int): Double {
        require(floorNumber > 0)
        return 1.0 + ((floorNumber - 5).coerceIn(0, 40) * 0.025)
    }

    fun land(previous: Balance, offset: Double, floorNumber: Int): Balance {
        require(offset.isFinite())
        val lean = previous.lean + offset / PracticeRulesV1.FLOOR_WIDTH * 0.45 * difficulty(floorNumber)
        val rescued = previous.dangerEpisode && abs(lean) <= STABLE
        return Balance(
            lean = lean,
            dangerEpisode = abs(lean) >= DANGER || (previous.dangerEpisode && !rescued),
            rescues = previous.rescues + if (rescued) 1 else 0,
        )
    }
}
