# Auth/lifecycle host boundary — 5 November 2026

Status: verified current API constraints and selected next implementation unit.
This document does not introduce an abandon API or approve restart economics.

## Current server contract

Verified against AppBackend `internal/wobbrik/attempts.go`, `finish.go`,
`internal/platform/wobbrik_http.go`, and `docs/wobbrik-game-api.md`.

- Start rejects a second active attempt for the same account (`attempt_conflict`).
  Replaying the exact original operation returns its historical receipt.
- Checkpoint requires positive, strictly increasing floors and the next sequence.
  It can credit coins, XP and achievements. An unchanged pause snapshot or zero-floor
  snapshot cannot be submitted as a checkpoint.
- Finish closes the active attempt at its accepted sequence, creates a result
  (including below ten floors), and creates a building at ten or more floors.
  Finish itself does not credit wallet/progression; checkpoint already does that.
- No abandon/restart endpoint exists. Local restart, logout, process death and menu
  navigation cannot close a server attempt or delete its pending journal command.

## Host rules within the existing contract

Pause/background/menu stop simulation and input; they do not imply finish.
Returning to the account reconciles its existing attempt before any new start.
Pending operations retain their exact envelope, operation ID and revision across
network failure, navigation and auth transitions. Never replace a pending command
with a restart or a fresh start. A finished attempt must not be restored as active.

Online restart/abandon stays unbound. Product decisions still required: whether
abandon is supported, whether an abandoned attempt creates a result/building, and
how already credited checkpoint rewards are treated. A server change would need
its own revision/dedup, account isolation, migration and old-client tests. Do not
invent refunds, clawbacks, forfeiture or finish-as-abandon in the mobile adapter.
Offline practice restart remains local and must never be routed to online finish.

## Selected next small unit: application-scoped authenticated recovery owner

Build a nonvisual owner with injected Sessions/token provider, backend and journal;
connect the Android application singleton only once real auth configuration is
available. Inspect existing billing bootstrap before introducing Firebase setup.
The app now has an application-owned Firebase Auth bootstrap in
`WobbrikApplication`. It requires the existing Firebase configuration and explicit
`wobbrik.backendOrigin` Gradle property. Missing configuration is exposed without
starting recovery. No real backend origin or device authentication has been verified.
The visible game remains offline practice; online engine bindings are still pending.

Acceptance criteria for that unit:

1. One journal/coordinator per application; activity recreation does not add writers.
2. Auth transitions include same-UID re-login epochs. Clear published account/engine
   state immediately; stale completion cannot publish into a new session.
3. Serialize foreground recovery; expose signed-out/recovering/ready/error state.
   Readiness requires successful reconciliation with no pending command. A previous
   snapshot alone never unlocks input during recovery or after failure.
4. Logout cancels host work and hides account state but preserves account journals;
   re-login recovers only that UID. Cancellation must not become success.
5. Focused tests cover delayed old-account completion, same-UID epoch changes,
   foreground retries, pending failures and owner reuse across host recreation.

This unit does not bind engine drops, start/checkpoint/finish or restart/menu actions.
Later engine restore uses decodeAccepted with server counters, explicitly pauses
and resets frame time, and handles zero-checkpoint, terminal and continued states
before enabling input. Continued-state stabilization is still unresolved.

No AppBackend code/schema/rules change is needed for this selected read/recovery
ownership unit. Full online gameplay and a complete domain adapter remain pending.

## Accepted attempt restore boundary

`AcceptedAttemptRestore` in the engine module consumes only a successful recovery
state. Signed-out/recovering/error exposes no attempt. An absent attempt and a
finished attempt are distinct outcomes; neither creates a local online attempt.
An active zero-sequence attempt may initialize fresh physics only with zero counters
and no checkpoint. Positive sequences require positive floors, sequence <= floors,
and `decodeAccepted` matching all three server counters. Unsupported or malformed
snapshots fail closed. Restored physics is always paused with tick accumulation reset;
the future host must also reset its monotonic frame clock before explicit resume.
Terminal active snapshots remain terminal; continued attempts remain blocked until
stabilization is specified. This boundary does not authorize input or issue commands.
It is connected to the application-owned `AcceptedEngineHost`, but not to the visible offline practice scene.

User deferred live origin/device verification on 5 November 2026. This removes that
check as a prerequisite for this implementation work, not as a release requirement.

## Application-owned engine host

`WobbrikApplication.engineHost` retains online physics across activity recreation.
Recovery publishes to the host synchronously under the recovery owner's lock, before
StateFlow publication, so conflation cannot skip an auth/recovery invalidation.
The host clears physics on every transition, restores only accepted paused snapshots,
and exposes no mutable engine. Process stop and immediate activity pause both pause
and reset timing. Foreground never resumes physics. While durable command handling
is absent, frame callbacks discard timing, resume/drop return false, and input stays
locked even for a valid Ready snapshot. No online restart/abandon binding exists.
The existing visible offline practice engine remains separate technical debt; this
unit does not complete online gameplay, approved visuals, or navigation integration.
No backend API/DTO/schema/rules change is required: this host only consumes the
existing recovery contract and sends no commands.
