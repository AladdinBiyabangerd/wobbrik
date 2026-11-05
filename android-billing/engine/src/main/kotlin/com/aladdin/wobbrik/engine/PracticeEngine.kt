package com.aladdin.wobbrik.engine

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.asin
import kotlin.math.sin
import kotlin.math.PI

/** Versioned, provisional game units; not measured building dimensions or server geometry. */
object PracticeRulesV1 {
    const val VERSION = "practice-speed-v4"
    const val CAMERA_VERSION = "practice-camera-v3"
    const val PENDULUM_VERSION = "practice-pendulum-v2"
    const val LEGACY_VERSION = "practice-balance-v1"
    const val TICKS_PER_SECOND = 60
    const val MAX_CATCH_UP_TICKS = 8
    const val FLOOR_WIDTH = 100.0
    const val FLOOR_HEIGHT = 30.0
    const val CRANE_RANGE = 120.0
    const val CRANE_SPEED = 1.5
    const val DROP_HEIGHT = 150.0
    const val GRAVITY_PER_TICK = 0.4

    fun followsTower(rules: String): Boolean = rules == VERSION || rules == CAMERA_VERSION

    /** Upcoming floor is the in-attempt level: +8% base speed per landed floor, max 3x. */
    fun swingMultiplier(floorNumber: Int): Double {
        require(floorNumber > 0)
        return 1.0 + (floorNumber - 1).coerceAtMost(25) * 0.08
    }
}

enum class Phase { SWINGING, FALLING, FINISHED }
data class Floor(val centerX: Double, val baseY: Double)
data class PracticeState(
    val phase: Phase = Phase.SWINGING,
    val paused: Boolean = false,
    val craneX: Double = 0.0,
    val falling: Floor? = null,
    val floors: List<Floor> = emptyList(),
    val balance: Balance = Balance(),
    val sway: Double = 0.0,
    val perfectLandings: Int = 0,
)

/** Offline practice only: no wallet, rewards, progression, ownership or API writes. */
class PracticeEngine {
    var state = PracticeState()
        private set
    private var rules = PracticeRulesV1.VERSION
    private var direction = 1
    private var fallVelocity = 0.0
    private var accumulatedTickUnits = 0L
    private var swayVelocity = 0.0

    /** World-space center of the fixed-screen crane. Old snapshots retain their fixed origin. */
    val cameraX: Double get() = if (PracticeRulesV1.followsTower(rules))
        state.floors.lastOrNull()?.centerX ?: 0.0 else 0.0

    fun checkpoint(): EngineCheckpoint = EngineCheckpoint(rules = rules, state = state.copy(floors = state.floors.toList()),
        direction = direction, fallVelocity = fallVelocity, tickUnits = accumulatedTickUnits, swayVelocity = swayVelocity)

    /** Validate before changing any live state. Host must pause/reset its frame clock on lifecycle restore. */
    fun restore(checkpoint: EngineCheckpoint) {
        checkpoint.validate()
        state = checkpoint.state.copy(floors = checkpoint.state.floors.toList())
        rules = checkpoint.rules
        direction = checkpoint.direction
        fallVelocity = checkpoint.fallVelocity
        accumulatedTickUnits = checkpoint.tickUnits
        swayVelocity = checkpoint.swayVelocity
    }

    fun drop(): Boolean {
        if (state.paused || state.phase != Phase.SWINGING) return false
        fallVelocity = 0.0
        state = state.copy(
            phase = Phase.FALLING,
            falling = Floor(cameraX + state.craneX, landingY() + PracticeRulesV1.DROP_HEIGHT),
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
        rules = PracticeRulesV1.VERSION
        direction = 1
        fallVelocity = 0.0
        accumulatedTickUnits = 0
        swayVelocity = 0.0
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
        if (state.phase == Phase.FINISHED) return
        swayVelocity = (swayVelocity - state.sway * BalanceRulesV1.SWAY_SPRING) * BalanceRulesV1.SWAY_DAMPING
        state = state.copy(sway = state.sway + swayVelocity)
        when (state.phase) {
            Phase.SWINGING -> {
                val floorNumber = state.floors.size + 1
                val multiplier = if (rules == PracticeRulesV1.VERSION) PracticeRulesV1.swingMultiplier(floorNumber)
                    else BalanceRulesV1.difficulty(floorNumber)
                val speed = PracticeRulesV1.CRANE_SPEED * multiplier
                if (rules == PracticeRulesV1.LEGACY_VERSION) {
                    val next = state.craneX + direction * speed
                    state = state.copy(craneX = next.coerceIn(-PracticeRulesV1.CRANE_RANGE, PracticeRulesV1.CRANE_RANGE))
                    if (abs(next) >= PracticeRulesV1.CRANE_RANGE) direction = -direction
                } else {
                    // Position encodes pendulum phase; the existing direction stores its branch.
                    // Reflect excess phase so reversal is smooth and checkpoint replay is exact.
                    var phase = asin((state.craneX / PracticeRulesV1.CRANE_RANGE).coerceIn(-1.0, 1.0)) +
                        direction * speed / PracticeRulesV1.CRANE_RANGE
                    if (phase > PI / 2) { phase = PI - phase; direction = -1 }
                    if (phase < -PI / 2) { phase = -PI - phase; direction = 1 }
                    state = state.copy(craneX = PracticeRulesV1.CRANE_RANGE * sin(phase))
                }
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
                        val offset = floor.centerX - supportX
                        val balance = BalanceRulesV1.land(state.balance, offset, state.floors.size + 1)
                        swayVelocity += offset / PracticeRulesV1.FLOOR_WIDTH * BalanceRulesV1.IMPACT
                        state = state.copy(
                            phase = if (abs(balance.lean) >= BalanceRulesV1.TOPPLE) Phase.FINISHED else Phase.SWINGING,
                            falling = null,
                            floors = state.floors + floor.copy(baseY = landingY()),
                            balance = balance,
                            perfectLandings = state.perfectLandings + if (abs(offset) <= BalanceRulesV1.PERFECT_OFFSET) 1 else 0,
                        )
                    }
                }
            }
            Phase.FINISHED -> Unit
        }
    }
}
