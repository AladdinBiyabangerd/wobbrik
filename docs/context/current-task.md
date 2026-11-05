# Firebase auth bootstrap — 5 November 2026

## Completed
- Application-owned Firebase Auth listener, AccountSessions token loader, one recovery
  owner/journal per process, ProcessLifecycleOwner foreground recovery.
- Firebase Auth/core/Android/coroutine dependencies and INTERNET permission added.
- Reviewed feature a7dca49 and compile fix bcfe100; merged with history into main.
- Bootstrap availability exposes missing Firebase/backend config; no endpoint guessed.

## Current state
- Existing google-services.json present locally; actual backend origin not supplied.
- Configure user-local Gradle `wobbrik.backendOrigin` with actual AppBackend HTTPS
  origin to activate recovery. No live auth/backend/device verification performed.
- Visible game remains offline practice. Auth UI, engine restore/input and online
  gameplay are not wired. Restart/abandon remains unbound.
- Unrelated untracked responsive rule/review files preserved.

## Decisions
- No anonymous account or invented auth UX/config. Firebase auth state callbacks create
  epochs; SDK token refresh does not establish a new login. ID tokens not persisted.
- No AppBackend API/schema/rule changes needed for existing bearer recovery transport.
- Never erase pending journal commands or implement finish-as-abandon.

## Remaining work
- Supply actual backend origin; verify real sign-in/out, same-UID re-login, account
  isolation, foreground retry, Activity recreation and process-kill on device.
- Online engine restore and lifecycle/input gating, zero-checkpoint/terminal/continued
  behavior and approved visual/navigation implementation remain separate work.

## Relevant files
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/WobbrikApplication.kt
- android-billing/app/build.gradle.kts
- android-billing/README.md
- docs/host-lifecycle-contract.md
- docs/reviews/2026-11-05-firebase-auth-bootstrap.md
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AuthenticatedRecoveryOwner.kt

## Checks performed
- Scoped diff review and whitespace checks on branches; no branch CI.
- First post-merge CI failed on ambiguous super.onCreate; fixed via separate reviewed
  branch and merged. Subsequent main ./scripts/local-ci.sh passed (106 tasks;
  16 executed, 90 up-to-date; core/engine tests up-to-date, app build/lint passed).
- No visual change or device/live server checks. No remote push/deploy.

## Exact next step
- Configure actual AppBackend HTTPS origin and verify Firebase sessions/recovery on
  device before starting the separate online engine binding unit.
