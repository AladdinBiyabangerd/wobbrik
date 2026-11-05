# Auth recovery owner — 5 November 2026

## Completed
- Main includes AuthenticatedRecoveryOwner (a14ac15): injected sessions/backend/
  journal/scope, one private coordinator, epoch guarding and recovery state flow.
- Added coordinator cancellation guard and focused owner regression tests.
- Inspected Firebase changes 403569b/83ce44b: Crashlytics, Analytics, Remote Config.

## Current state
- App remains offline practice. Firebase Auth, listener and authenticated application
  bootstrap are absent; Firebase services do not supply an auth session.
- Owner is not wired to Android. Retain one instance/journal per application when
  real auth/backend configuration is supplied; no Activity-owned writer.
- Unrelated untracked responsive rule/review files preserved.

## Decisions
- Restart/abandon remains unbound; never use finish or erase pending commands.
- No backend/schema/rule change needed for recovery ownership.
- No invented Firebase project, auth UX or endpoint configuration.

## Remaining work
- Application singleton and real auth listener/token bootstrap, then lifecycle/input/
  engine restore. Resolve continued/zero-checkpoint behavior separately.
- Device recreation/process-kill verification; approved visual/navigation debt remains.

## Relevant files
- docs/host-lifecycle-contract.md
- docs/reviews/2026-11-05-auth-recovery-owner.md
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AuthenticatedRecoveryOwner.kt
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AttemptCoordinator.kt
- android-billing/core/src/test/kotlin/appbackend/wobbrik/game/AuthenticatedRecoveryOwnerTest.kt
- android-billing/app/build.gradle.kts
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/RemoteConfigInit.kt

## Checks performed
- Scoped Firebase and recovery source review; branch whitespace check.
- Post-merge ./scripts/local-ci.sh passed: 40 core tests including five owner
  tests, engine tests, Android/app assemble and lint; see review for failed retries.
- No device/live-backend or visual checks in this nonvisual unit.

## Exact next step
- Implement real auth bootstrap only with actual auth/backend configuration;
  preserve recovery journals and leave restart/abandon unbound.
