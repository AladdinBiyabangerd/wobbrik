package com.aladdin.wobbrik.engine

import appbackend.wobbrik.billing.BillingSession
import appbackend.wobbrik.game.*

/** Serial replay through recovery's existing sole wire journal. Never replaces another device's attempt. */
class PendingResultReconciler(private val results: PendingGameResults) {
    suspend fun reconcile(session: BillingSession, coordinator: AttemptCoordinator, initial: GameSnapshot): GameSnapshot {
        var snapshot = initial
        for (result in results.pending(session.uid)) {
            var attempt = snapshot.attempt?.takeIf { it.active }
            if (attempt == null) {
                check(!result.started) { "Pending attempt no longer active; reconciliation required" }
                snapshot = coordinator.execute(GameplayWire.start(result.attemptId, snapshot.revision, CityFloorGeometry.VERSION), session)
                attempt = snapshot.attempt?.takeIf { it.active }
            }
            check(attempt?.id == result.attemptId) { "Another active attempt must be reconciled first" }
            val accepted = AcceptedAttemptRestore.fromSnapshot(snapshot) as? AcceptedAttemptRestore.Result.Paused
                ?: error("Pending attempt cannot be restored")
            val old = accepted.checkpoint.state
            val terminal = result.terminal.state
            require(terminal.floors.take(old.floors.size) == old.floors && terminal.floors.size >= old.floors.size)
            require(terminal.perfectLandings - old.perfectLandings in 0..(terminal.floors.size - old.floors.size))
            require(terminal.balance.rescues - old.balance.rescues in 0..(terminal.floors.size - old.floors.size))
            if (terminal.floors.size > old.floors.size) {
                require(!accepted.terminal && accepted.sequence < Long.MAX_VALUE)
                snapshot = coordinator.execute(GameplayWire.checkpoint(result.id + "-checkpoint", snapshot.revision,
                    result.attemptId, accepted.sequence + 1, terminal.floors.size.toLong(), terminal.perfectLandings.toLong(),
                    terminal.balance.rescues.toLong(), EngineCheckpointCodec.encode(result.terminal),
                    attempt.geometryVersion?.let { result.terminal.cityGeometry() }), session)
            }
            val current = requireNotNull(snapshot.attempt)
            require(current.id == result.attemptId && current.active && current.floors == terminal.floors.size.toLong() &&
                current.precise == terminal.perfectLandings.toLong() && current.rescues == terminal.balance.rescues.toLong())
            snapshot = coordinator.execute(GameplayWire.finish(result.id + "-finish", snapshot.revision,
                result.attemptId, current.sequence, result.parts), session)
        }
        return snapshot
    }
}
