# Accepted engine host — 5 November 2026

## Completed
- AcceptedAttemptRestore wired to WobbrikApplication.engineHost via synchronous
  recovery notifications (before StateFlow, including same-UID epoch invalidation).
- Private restored physics remains paused; activity/process pause resets timing.
- Foreground never resumes; frames discard timing; resume/drop/input stay locked.
- No online restart/abandon binding or backend command/policy change.

## Current state
- Application host is nonvisual; visible game remains a separate offline practice scene.
- Main CI passed. Origin/device verification still deferred by user.
- Unrelated untracked responsive rule/review files preserved.

## Decisions
- No AppBackend changes required: consumes existing recovery contract, sends no commands.
- Continued attempts fail closed; restart/abandon policy remains unresolved.

## Remaining work
- Durable start/checkpoint/finish integration before enabling online simulation/input.
- Auth UI, approved visual/navigation integration, continued stabilization.
- Actual backend origin and live account/epoch/foreground/process-death checks.

## Relevant files
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/AcceptedEngineHost.kt
- android-billing/engine/src/test/kotlin/com/aladdin/wobbrik/engine/AcceptedEngineHostTest.kt
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AuthenticatedRecoveryOwner.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/WobbrikApplication.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/MainActivity.kt
- docs/host-lifecycle-contract.md
- docs/reviews/2026-11-05-accepted-engine-host.md

## Checks performed
- Scoped local diff review and whitespace checks; no branch CI.
- Post-merge `./scripts/local-ci.sh` passed: 106 tasks, 32 executed, 74 up-to-date;
  core/engine tests (four new), app assemble/lint passed. Existing lifecycle deprecation warning.
- No device/live/visual checks or push/deploy.

## Exact next step
- Design the small durable command integration unit against existing AttemptCoordinator
  and backend contract; retain input lock until retry/dedup and accepted checkpoint
  handling are safe. Do not bind restart/abandon or invent continuation policy.
