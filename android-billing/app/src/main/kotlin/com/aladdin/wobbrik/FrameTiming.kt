package com.aladdin.wobbrik

import kotlin.math.roundToInt

/**
 * Ring-buffer frame Δt collector for the practice host loop.
 * Presentation metrics only — never scored, checkpointed or sent to the server.
 */
internal class FrameTiming(capacity: Int = 3_600) {
    private val samplesMs = FloatArray(capacity.coerceAtLeast(60))
    private var written = 0
    private var next = 0
    private var over16 = 0
    private var over33 = 0

    val frameCount: Int get() = written.coerceAtMost(samplesMs.size)

    fun reset() {
        written = 0
        next = 0
        over16 = 0
        over33 = 0
    }

    fun record(deltaNanos: Long) {
        if (deltaNanos <= 0L) return
        val ms = deltaNanos / 1_000_000f
        if (written >= samplesMs.size) {
            val leaving = samplesMs[next]
            if (leaving > 16.7f) over16--
            if (leaving > 33f) over33--
        }
        samplesMs[next] = ms
        next = (next + 1) % samplesMs.size
        written++
        if (ms > 16.7f) over16++
        if (ms > 33f) over33++
    }

    fun summary(): Summary {
        val n = frameCount
        if (n == 0) return Summary(0, 0.0, 0.0, 0.0, 0.0, 0, 0)
        val ordered = FloatArray(n)
        val start = if (written <= samplesMs.size) 0 else next
        for (i in 0 until n) ordered[i] = samplesMs[(start + i) % samplesMs.size]
        ordered.sort()
        val mean = ordered.average()
        return Summary(
            frames = n,
            avgFps = if (mean > 0.0) 1000.0 / mean else 0.0,
            p50Ms = percentile(ordered, 0.50),
            p95Ms = percentile(ordered, 0.95),
            p99Ms = percentile(ordered, 0.99),
            jankOver16 = over16,
            jankOver33 = over33,
        )
    }

    data class Summary(
        val frames: Int,
        val avgFps: Double,
        val p50Ms: Double,
        val p95Ms: Double,
        val p99Ms: Double,
        val jankOver16: Int,
        val jankOver33: Int,
    ) {
        fun line(): String =
            "frames=$frames avg=${avgFps.roundToInt()}fps p50=${p50Ms.format1()}ms " +
                "p95=${p95Ms.format1()}ms p99=${p99Ms.format1()}ms jank16=$jankOver16 jank33=$jankOver33"
    }

    private companion object {
        fun percentile(sorted: FloatArray, p: Double): Double {
            if (sorted.isEmpty()) return 0.0
            val i = ((sorted.size - 1) * p).roundToInt().coerceIn(0, sorted.lastIndex)
            return sorted[i].toDouble()
        }

        fun Double.format1(): String = ((this * 10).roundToInt() / 10.0).toString()
    }
}
