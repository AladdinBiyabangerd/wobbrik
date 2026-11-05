# Accepted attempt restore — 5 November 2026

## Completed
- Firebase application-scoped auth/recovery bootstrap already on main.
- Added read-only AcceptedAttemptRestore: Ready-only, authoritative counter/version
  validation, fresh zero-checkpoint handling, finished/terminal/continued separation.
- Restored engine checkpoint is paused with zero tick accumulation; no input granted.

## Current state
- Boundary is not yet wired into application/scene. Visible game is offline practice.
- Firebase local config matches com.aladdin.wobbrik; user Gradle/project/generated
  BuildConfig origin is empty. ADB devices list empty. Live checks were not performed.
- Unrelated untracked responsive rule/review files preserved.

## Decisions
- User explicitly deferred origin/device verification and authorized next step.
- Real live checks remain required later; do not claim auth/recovery verified on device.
- No backend API/schema/rules change needed for this read-only boundary, verified
  against AppBackend attempts.go/finish.go. No wallet or command changes.
- Continued state fails closed; never invent stabilization, restart or abandon policy.

## Remaining work
- Application-owned host connecting current recovery state to restored physics;
  synchronous auth invalidation, lifecycle pause, frame-clock reset and input gating.
- Safe start/checkpoint/finish integration before any online drop is enabled.
- Auth UI, approved visual/navigation integration, continued stabilization.
- Actual backend origin and live account/epoch/foreground/process-death checks.
- Restart/abandon remains unbound.

## Relevant files
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/AcceptedAttemptRestore.kt
- android-billing/engine/src/test/kotlin/com/aladdin/wobbrik/engine/AcceptedAttemptRestoreTest.kt
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AuthenticatedRecoveryOwner.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/WobbrikApplication.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/MainActivity.kt
- docs/host-lifecycle-contract.md
- docs/reviews/2026-11-05-accepted-attempt-restore.md

## Checks performed
- Scoped source/diff review and branch whitespace check; no branch CI.
- Post-merge main `./scripts/local-ci.sh` passed: 106 tasks, 16 executed, 90
  up-to-date. Seven new restoration tests passed; app assemble/lint passed.
- No visual, device/live server, push or deploy checks/actions.

## Exact next step
- Build the nonvisual application-owned engine host using AcceptedAttemptRestore;
  clear on auth/recovery transitions, pause/reset frame timing on lifecycle changes.
  Keep input locked until durable command/checkpoint handling is connected.
