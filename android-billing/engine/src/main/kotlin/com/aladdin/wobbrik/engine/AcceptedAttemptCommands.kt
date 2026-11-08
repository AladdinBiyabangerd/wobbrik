package com.aladdin.wobbrik.engine

import appbackend.wobbrik.game.AuthenticatedRecoveryOwner
import appbackend.wobbrik.game.GameplayWire
import java.util.UUID

/**
 * Application-owned typed command entry point; shares recovery's sole coordinator/journal.
 * No scene input binding. Physics proposals must be captured against the exact Ready instance;
 * never feed the separate offline practice scene here. The server remains structural-v1,
 * not a physics replay verifier. Dispatch success is not a committed gameplay result.
 */
class AcceptedAttemptCommands(
    private val recovery: AuthenticatedRecoveryOwner,
    private val operationId: () -> String = { UUID.randomUUID().toString() },
) {
    fun start(expected: AuthenticatedRecoveryOwner.State.Ready): Boolean = recovery.submit(expected) { snapshot ->
        when (AcceptedAttemptRestore.from(expected)) {
            AcceptedAttemptRestore.Result.NoAttempt, is AcceptedAttemptRestore.Result.Finished -> Unit
            else -> error("An accepted active attempt must be reconciled, not restarted")
        }
        GameplayWire.start(operationId(), snapshot.revision, appbackend.wobbrik.game.CityFloorGeometry.VERSION)
    }

    /** Only a settled landing/terminal proposal with additional floors may be checkpointed. */
    fun checkpoint(expected: AuthenticatedRecoveryOwner.State.Ready, proposal: EngineCheckpoint): Boolean =
        recovery.submit(expected) { snapshot ->
            val accepted = active(expected)
            require(!accepted.terminal) { "Terminal attempts cannot add floors" }
            val candidate = settled(proposal)
            val old = accepted.checkpoint.state
            val next = candidate.state
            require(next.floors.size > old.floors.size) { "Pause/zero/unchanged snapshots are not checkpoints" }
            require(next.floors.take(old.floors.size) == old.floors) { "Accepted floors are immutable" }
            val delta = next.floors.size - old.floors.size
            require(next.perfectLandings - old.perfectLandings in 0..delta)
            require(next.balance.rescues - old.balance.rescues in 0..delta)
            require(accepted.sequence < Long.MAX_VALUE)
            GameplayWire.checkpoint(operationId(), snapshot.revision, accepted.attemptId, accepted.sequence + 1,
                next.floors.size.toLong(), next.perfectLandings.toLong(), next.balance.rescues.toLong(),
                EngineCheckpointCodec.encode(candidate),
                snapshot.attempt?.geometryVersion?.let { candidate.cityGeometry() })
        }

    /**
     * Explicit terminal finish only. If the terminal landing added floors, checkpoint it first,
     * wait for a new Ready, then finish against that accepted state. Never finish-as-abandon.
     * A miss may finish at zero floors or without adding a floor to the accepted sequence.
     */
    fun finish(expected: AuthenticatedRecoveryOwner.State.Ready, terminal: EngineCheckpoint,
               parts: Map<String, String>): Boolean = recovery.submit(expected) { snapshot ->
        val accepted = active(expected)
        val candidate = settled(terminal).state
        require(candidate.phase == Phase.FINISHED) { "Only terminal physics may request finish" }
        val old = accepted.checkpoint.state
        require(candidate.floors == old.floors && candidate.perfectLandings == old.perfectLandings &&
            candidate.balance.rescues == old.balance.rescues) { "Checkpoint unaccepted progress before finish" }
        GameplayWire.finish(operationId(), snapshot.revision, accepted.attemptId, accepted.sequence, parts.toMap())
    }

    private fun active(expected: AuthenticatedRecoveryOwner.State.Ready): AcceptedAttemptRestore.Result.Paused =
        (AcceptedAttemptRestore.from(expected) as? AcceptedAttemptRestore.Result.Paused)
            ?: error("A supported accepted active attempt is required")

    private fun settled(proposal: EngineCheckpoint): EngineCheckpoint {
        proposal.validate()
        require(proposal.state.phase != Phase.FALLING) { "Checkpoint only at a settled boundary" }
        // Normalize timing without modifying the caller's engine or retaining mutable proposal lists.
        return proposal.copy(tickUnits = 0, state = proposal.state.copy(paused = true, floors = proposal.state.floors.toList()))
    }
}
