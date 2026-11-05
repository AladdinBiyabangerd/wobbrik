# Recovery DTO + engine checkpoint — 5 noyabr 2026

## Completed
- Full account recovery fields, catalog/land-offer DTO decoder, active/finished attempt
  counters/continued/pinned versions and typed start/checkpoint/finish receipts.
- HTTP sync now retains full account recovery; submitted receipt validated by route.
- practice-checkpoint-v1 JSON codec captures direction, fall/sway velocities and tick
  remainder. Restore validates before mutation; decodeAccepted checks server counters.
- Positive checkpoint floors/rescues bounds aligned with AppBackend.
- Deterministic roundtrip tests for swing, fall, pause, successful landing and finish;
  malformed/version/size/counter rejection and DTO/legacy receipt regressions.

## Current state
- Implementation 588b5f6; Gradle newline fix d07cf5d; reviewed merges on main.
- Main 1d28bab local CI passed: 47 tasks, 17 executed/30 up-to-date.
- Prior dirty Compose app/build/settings/rule/review files excluded from CI and preserved;
  restore them after verification. No host binding or visual change in this unit.

## Decisions
- AppBackend source/API checked; no backend/schema/rules changes needed.
- PostgreSQL remains authority; JSON is opaque structural-v1, never trusted geometry.
- Server UTC timestamp text validated using Android-24-compatible parser.
- Oversize 16 KiB checkpoint fails closed; no truncation or unlimited-height codec.
- No abandon/restart-as-finish, continuation stabilization, push, deploy or SKU work.

## Remaining work
- Auth bootstrap, application-scoped coordinator/journal owner, lifecycle recovery,
  accepted checkpoint decode + pause/frame-clock reset and input gating in host.
- Resolve restart/abandon server contract BEFORE wiring those actions.
- Catalog/attempt/operation read-route transport integration; other domain adapters.
- Android process-kill/AtomicFile and authenticated live HTTP tests.
- Approved graphics/menu/navigation and phone/tablet visual comparison.

## Relevant files
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{README.md,RecoveryDtos.kt,GameplayWire.kt,GameplayHttpBackend.kt,AttemptCoordinator.kt}
- android-billing/engine/src/main/kotlin/az/wobbrik/engine/{EngineCheckpoint.kt,PracticeEngine.kt}
- android-billing/{core,engine}/src/test/kotlin (RecoveryDtosTest, GameplayAdaptersTest, EngineCheckpointTest)
- docs/reviews/2026-11-05-recovery-checkpoint.md
- android-billing/app/README.md; docs/kotlin-compose-plan.md §5
- /Users/mac/My Workspace/AppBackend/docs/wobbrik-game-api.md

## Checks performed
- Scoped source review and git diff --check; no branch CI.
- First main CI failed on Gradle dependency newline; fixed on separate reviewed branch.
- Main rerun passed: core 35 tests + engine 13 tests, zero failures/errors/skips;
  Android assemble/lint passed. Verification docs merge requires final main rerun.
- No UI/device/live backend tests or complete domain-adapter claim.

## Exact next step
- Read this and core gameplay README; preserve dirty host files.
- Next chat: inspect scoped host/auth/lifecycle boundaries and unresolved abandon/restart
  contract; choose one small binding unit before implementation. Use decodeAccepted
  with authoritative attempt counters; do not blindly resume terminal/pending state.
- Review on feat/fix branch, merge with history; CI only on main.
