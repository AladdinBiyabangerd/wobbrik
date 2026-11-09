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

Online restart/abandon stays unbound in code. Product policy (user decision, 9 November 2026):

- If the player leaves an open attempt mid-game (Home, Back, or closing the app), the attempt is
  deleted and not kept: no result and no building are created, and there is no later
  "Cəhdə qayıt".
- A save happens only in two cases: (1) the player loses — the last accepted successful state from
  before the loss is kept; (2) the player finishes the game themselves — the existing flow: after the
  floors are complete, cosmetic selection and explicit confirm, then finish.
- Do not invent an abandon API, refunds, or treat finish as abandon. Offline practice restart stays local.

The current server has no endpoint that closes or deletes an open attempt, and the current host
keeps the attempt and pending command on Back/menu (see below). How the policy is realised is a
separate implementation unit; if it needs a server change, that change needs its own approval. A server change would need
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

The original recovery unit did not bind commands. The durable command integration below
now exposes typed start/checkpoint/finish; scene drops and restart/menu remain unbound.
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
and no checkpoint (`null` or the server initial empty JSON object `{}`). Nonempty
objects, arrays and malformed snapshots are rejected even at sequence zero.
Positive sequences require positive floors, sequence <= floors,
and `decodeAccepted` matching all three server counters. Unsupported or malformed
snapshots fail closed. Restored physics is always paused with tick accumulation reset;
the future host must also reset its monotonic frame clock before explicit resume.
Terminal active snapshots remain terminal; continued attempts remain blocked until
stabilization is specified. This boundary does not authorize input or issue commands.
It is connected to the application-owned `AcceptedEngineHost`, but not to the visible offline practice scene.

User deferred live origin/device verification on 5 November 2026. On 9 November 2026 the user
closed live game-flow validation (landing → checkpoint → Ready → finish, retry, process death,
live device/DB): it is not required and is not tracked as open work.

## Application-owned engine host

`WobbrikApplication.engineHost` retains online physics across activity recreation.
Recovery publishes to the host synchronously under the recovery owner's lock, before
StateFlow publication, so conflation cannot skip an auth/recovery invalidation.
The host clears physics on every transition, restores only accepted paused snapshots,
and exposes no mutable engine. Process stop and immediate activity pause both pause
and reset timing. Foreground never resumes physics. Until the online simulation and scene loop is integrated, frame callbacks discard
timing, resume/drop return false, and input stays
locked even for a valid Ready snapshot. No online restart/abandon binding exists.
The existing visible offline practice engine remains separate technical debt; this
unit does not complete online gameplay, approved visuals, or navigation integration.
No backend API/DTO/schema/rules change is required: this host only consumes the
existing recovery contract and sends no commands.

## Durable typed attempt commands — 5 November 2026

`Bootstrap.Configured.commands` is one application-owned `AcceptedAttemptCommands`
using the recovery owner's existing coordinator and AtomicFile journal. No second
writer or local wallet is introduced. The scene remains separate offline practice;
its input is never routed into online commands.

- Each call requires the exact currently published `State.Ready` object. Equal,
  copied, prior-account, prior-login and prior-recovery snapshots cannot authorize
  a command. Coordinator work additionally captures the UID/login session before
  waiting on its mutex; stale queued work cannot adopt a later account.
- Start accepts only a supported absent/finished attempt; an active attempt cannot
  be restarted. Continued, invalid and unsupported attempts remain blocked.
- Checkpoint accepts a validated settled physics proposal against that Ready. Floors
  must increase, the accepted floor prefix is immutable, counter increments are
  bounded by new floors, and sequence is exactly accepted + 1. Pause, unchanged,
  zero-floor and falling proposals are rejected. Persisted physics is paused with
  zero timing accumulation. Terminal accepted attempts cannot add more floors.
- Finish requires an explicit terminal proposal with exactly the accepted floors
  and counters, and the five selected cosmetic part IDs. Parts/unlock validity
  remains server-authoritative. A terminal landing with new floors must first be
  checkpointed; wait for the next Ready before finish. A miss may finish at zero
  floors or at the unchanged accepted sequence. Finish is never an abandon alias.
- Dispatch returns a Boolean indicating admission only. Recovering immediately
  hides accepted physics and locks input; only journal persistence followed by a
  committed receipt and fresh sync can yield a new Ready. No optimistic coins,
  XP, building ownership or results are granted. Persisted commands retain exact
  ID, revision, route and envelope on lost response/sync, logout and process death.
  Definitive revision conflict clears that rejected command, syncs, exposes Error
  and requires recovery/new explicit intent; it never silently rebases or retries
  as a new operation. Other failures preserve the original pending command.
- Checkpoint and finish remain separate journal operations. An accepted terminal
  checkpoint reopens the cosmetic picker, never automatically finishes. A local,
  app-private account/attempt draft retains terminal misses with no added floor and
  selected part IDs across restart. Reuse requires matching checkpoint/rules,
  immutable accepted floors and precise/rescue counters; it grants no progress.
  A crash before an extra-floor checkpoint reaches the wire journal still falls
  back to the accepted boundary; an unaccepted draft is never promoted to progress.
- Recovery drains any pending command verbatim before publishing Ready/catalog or
  accepting a new selection. Confirm validates exactly five category IDs against
  the authenticated wobbrik-v1/structural-v1 catalog and current-account unlocks;
  free items are implicit. Failed local selection writes retain paused physics,
  expose an error and do not dispatch a finish. Auth epochs revoke stale callbacks.
  Back/menu preserves both the active attempt and pending command (current implementation;
  the 9 November leave-mid-game policy above is not implemented yet).

No backend API/DTO/schema/rules version changes are needed: the integration uses
existing structural-v1 requests and the unified PostgreSQL wallet/ledger. Server
physics proof, continuation stabilization, final scene/navigation and the restart/abandon policy
implementation remain outstanding; live origin/device verification is not required (user decision,
9 November 2026). Input is deliberately still locked.
