# Gameplay HTTP + attempt journal — 5 noyabr 2026

## Completed
- Typed start/checkpoint/finish envelope builders and persisted metadata validation.
- GameplayHttpBackend: fresh Sessions token, UID/login epoch, exact wire retry,
  explicit 409 revision_conflict mapping; bounded HTTPS transport, no redirects.
- DiskAttemptJournal + Android AtomicFile in noBackupFilesDir: per-UID hashed files,
  embedded identity/version, pending overwrite rejection, compare removal/tombstones.
- Five adapter regressions including actual JVM disk recreation and lost response.

## Current state
- Implementation commit b6a4f9b, reviewed merge dfc10c5 on main. Local CI passed.
- Previous dirty Compose app/build/settings/rule/review files preserved and restored
  after clean-tree CI; excluded from this unit and its verification.
- Host still PracticeEngine/offline/session record/schematic Canvas, not connected.
- One application-scoped journal/coordinator owner required; multi-process writers
  unsupported. JVM disk tests do not verify Android AtomicFile process/power loss.

## Decisions
- AppBackend API/source inspected; existing contract suffices, no server/schema/rules
  changes needed for this adapter unit. Authority remains AppBackend/PostgreSQL.
- Only revision_conflict clears a rejected pending command; all other failures
  retain exact command. No operation-404 inference or automatic mutation rebasing.
- No UI changes or visual-fidelity claim. No push, deployment, SKU or Console work.

## Remaining work
- Full account/catalog/attempt/receipt DTOs beyond recovery projection.
- Versioned engine checkpoint codec and validation; auth bootstrap, input gating,
  lifecycle recovery and host binding. Resolve restart/abandon server contract first.
- Android process-kill/AtomicFile device tests and live authenticated transport tests.
- Approved graphics, real menus/navigation, phone/tablet design comparison.

## Relevant files
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{README.md,AttemptCoordinator.kt,GameplayWire.kt,GameplayHttpBackend.kt,DiskAttemptJournal.kt}
- android-billing/android/src/main/kotlin/appbackend/wobbrik/game/AndroidAttemptJournal.kt
- android-billing/core/src/test/kotlin/appbackend/wobbrik/game/GameplayAdaptersTest.kt
- docs/reviews/2026-11-05-gameplay-http-journal.md
- docs/kotlin-compose-plan.md §5; android-billing/app/README.md
- /Users/mac/My Workspace/AppBackend/docs/wobbrik-game-api.md

## Checks performed
- Branch source review + git diff --check; no branch CI.
- Main dfc10c5 ./scripts/local-ci.sh: exit 0, 47 tasks, 21 executed/26 up-to-date.
- New adapter suite: 5 tests, 0 failure/error/skip. Android assemble/lint passed.
- Backend tests, device process-kill, live HTTP/Firebase and UI renders not run.

## Exact next step
- Read this and core gameplay README; preserve dirty host files.
- Next small unit: full recovery DTO/engine checkpoint roundtrip validation, before
  host lifecycle binding. Check PracticeEngine state and backend structural bounds;
  do not invent abandon/restart-as-finish behavior. Review on feat/fix branch,
  merge retaining history, CI only on main. Keep approved visual design unchanged.
