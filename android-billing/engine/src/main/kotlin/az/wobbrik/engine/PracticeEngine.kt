package az.wobbrik.engine

import kotlin.math.abs
import kotlin.math.min

/** Versioned, provisional game units; not measured building dimensions or server geometry. */
object PracticeRulesV1 {
    const val VERSION = "practice-foundation-v1"
    const val TICKS_PER_SECOND = 60
    const val MAX_CATCH_UP_TICKS = 8
    const val FLOOR_WIDTH = 100.0
    const val FLOOR_HEIGHT = 30.0
    const val CRANE_RANGE = 120.0
    const val CRANE_SPEED = 1.5
    const val DROP_HEIGHT = 150.0
    const val GRAVITY_PER_TICK = 0.4
}

enum class Phase { SWINGING, FALLING, FINISHED }
data class Floor(val centerX: Double, val baseY: Double)
data class PracticeState(
    val phase: Phase = Phase.SWINGING,
    val paused: Boolean = false,
    val craneX: Double = 0.0,
    val falling: Floor? = null,
    val floors: List<Floor> = emptyList(),
)

/** Offline practice only: no wallet, rewards, progression, ownership or API writes. */
class PracticeEngine {
    var state = PracticeState()
        private set
    private var direction = 1
    private var fallVelocity = 0.0
    private var accumulatedTickUnits = 0L

    fun drop(): Boolean {
        if (state.paused || state.phase != Phase.SWINGING) return false
        fallVelocity = 0.0
        state = state.copy(
            phase = Phase.FALLING,
            falling = Floor(state.craneX, landingY() + PracticeRulesV1.DROP_HEIGHT),
        )
        return true
    }

    fun pause() {
        state = state.copy(paused = true)
        accumulatedTickUnits = 0
    }

    fun resume() {
        state = state.copy(paused = false)
        accumulatedTickUnits = 0
    }

    fun restart() {
        state = PracticeState()
        direction = 1
        fallVelocity = 0.0
        accumulatedTickUnits = 0
    }

    /** Host supplies elapsed monotonic time; background time must never be passed on resume. */
    fun advance(elapsedNanos: Long) {
        require(elapsedNanos >= 0)
        if (state.paused || state.phase == Phase.FINISHED) return
        // Integer tick units avoid rounding 1/60 second and overflowing on long stalls.
        val bounded = min(elapsedNanos, 1_000_000_000L)
        accumulatedTickUnits += bounded * PracticeRulesV1.TICKS_PER_SECOND
        val ticks = min(accumulatedTickUnits / 1_000_000_000L, PracticeRulesV1.MAX_CATCH_UP_TICKS.toLong())
        accumulatedTickUnits %= 1_000_000_000L // Discard excess whole ticks after a stall.
        repeat(ticks.toInt()) { tick() }
    }

    private fun landingY() = state.floors.size * PracticeRulesV1.FLOOR_HEIGHT

    private fun tick() {
        when (state.phase) {
            Phase.SWINGING -> {
                val next = state.craneX + direction * PracticeRulesV1.CRANE_SPEED
                state = state.copy(craneX = next.coerceIn(-PracticeRulesV1.CRANE_RANGE, PracticeRulesV1.CRANE_RANGE))
                if (abs(next) >= PracticeRulesV1.CRANE_RANGE) direction = -direction
            }
            Phase.FALLING -> {
                val floor = checkNotNull(state.falling)
                fallVelocity += PracticeRulesV1.GRAVITY_PER_TICK
                val next = floor.copy(baseY = floor.baseY - fallVelocity)
                if (next.baseY > landingY()) {
                    state = state.copy(falling = next)
                } else {
                    val supportX = state.floors.lastOrNull()?.centerX ?: 0.0
                    if (abs(floor.centerX - supportX) >= PracticeRulesV1.FLOOR_WIDTH) {
                        state = state.copy(phase = Phase.FINISHED, falling = null)
                    } else {
                        state = state.copy(
                            phase = Phase.SWINGING,
                            falling = null,
                            floors = state.floors + floor.copy(baseY = landingY()),
                        )
                    }
                }
            }
            Phase.FINISHED -> Unit
        }
    }
}
