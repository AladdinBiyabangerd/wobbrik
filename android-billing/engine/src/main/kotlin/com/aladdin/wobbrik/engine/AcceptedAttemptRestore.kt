package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.ServerAttempt
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** Read-only recovery boundary. No input permission, commands, rewards or continuation policy. */
object AcceptedAttemptRestore {
    sealed interface Result {
        data object Unavailable : Result
        data object NoAttempt : Result
        data class Finished(val attemptId: String) : Result
        data class Blocked(val reason: Reason) : Result
        /** Always paused with an empty tick accumulator; the host must reset its frame clock. */
        data class Paused(val attemptId: String, val sequence: Long, val checkpoint: EngineCheckpoint,
            val terminal: Boolean) : Result
    }
    enum class Reason { UNSUPPORTED_VERSION, INVALID_CHECKPOINT, CONTINUATION_UNRESOLVED }

    fun from(recovery: AuthenticatedRecoveryOwner.State): Result {
        if (recovery !is AuthenticatedRecoveryOwner.State.Ready) return Result.Unavailable
        val snapshot = recovery.snapshot
        if (snapshot.rulesVersion != "wobbrik-v1") return Result.Blocked(Reason.UNSUPPORTED_VERSION)
        val attempt = snapshot.attempt ?: return Result.NoAttempt
        if (!attempt.active) return Result.Finished(attempt.id)
        if (attempt.rulesVersion != "wobbrik-v1" || attempt.engineVersion != "wobbrik-structural-v1") {
            return Result.Blocked(Reason.UNSUPPORTED_VERSION)
        }
        // Server continuation can retain pre-continuation physics. Never invent stabilization.
        if (attempt.continued) return Result.Blocked(Reason.CONTINUATION_UNRESOLVED)
        return try {
            val checkpoint = decode(attempt)
            if (attempt.geometryVersion != null) {
                require(attempt.geometryVersion == appbackend.wobbrik.game.CityFloorGeometry.VERSION)
                if (attempt.floors > 0) {
                    val accepted = requireNotNull(attempt.acceptedGeometry)
                    require(accepted.attemptId == attempt.id && accepted.sequence == attempt.sequence)
                    require(checkpoint.cityGeometry() == accepted.geometry)
                }
            }
            val engine = PracticeEngine()
            engine.restore(checkpoint)
            engine.pause()
            Result.Paused(attempt.id, attempt.sequence, engine.checkpoint(), engine.state.phase == Phase.FINISHED)
        } catch (_: IllegalArgumentException) {
            Result.Blocked(Reason.INVALID_CHECKPOINT)
        } catch (_: IllegalStateException) {
            Result.Blocked(Reason.INVALID_CHECKPOINT)
        } catch (_: NoSuchElementException) {
            Result.Blocked(Reason.INVALID_CHECKPOINT)
        }
    }

    private fun decode(attempt: ServerAttempt): EngineCheckpoint {
        require(attempt.id.isNotBlank())
        if (attempt.sequence == 0L) {
            require(attempt.floors == 0L && attempt.precise == 0L && attempt.rescues == 0L)
            // PostgreSQL initializes a new attempt with snapshot {} before any checkpoint.
            attempt.checkpointJson?.let {
                val snapshot = Json.parseToJsonElement(it)
                require(snapshot is JsonObject && snapshot.isEmpty())
            }
            return PracticeEngine().checkpoint()
        }
        require(attempt.sequence > 0 && attempt.floors > 0 && attempt.sequence <= attempt.floors)
        return EngineCheckpointCodec.decodeAccepted(requireNotNull(attempt.checkpointJson),
            attempt.floors, attempt.precise, attempt.rescues)
    }
}
