# Durable attempt commands — 5 November 2026

## Completed
- Application-owned AcceptedAttemptCommands uses recovery's sole coordinator/journal.
- Typed start/checkpoint/terminal finish admitted only from exact current Ready.
- Coordinator captures expected UID/login session before queued work; stale account
  proposals, duplicate dispatch, invalid counters/floor rewrites fail closed.
- Atomic command journal/retry and fresh sync remain the only success path.
- Application host input remains locked; resume/drop false, physics paused.

## Current state
- Implementation locally reviewed and merged: `d748830` → `9b38e31`; main CI passed.
- Visible scene remains separate offline practice; no online input binding.
- Unrelated untracked responsive rule/review files preserved.

## Decisions
- No AppBackend API/DTO/schema/rules change needed; existing structural-v1 and shared
  wallet remain authoritative. No local grants, rebase, restart/abandon binding.
- Terminal landing with new floors requires checkpoint → new Ready → explicit finish.
  Each command is durable separately; no automatic multi-command finish intent.
- Live origin/device checks remain deferred; continuation stabilization stays blocked.

## Remaining work
- Integrate online simulation with per-landing checkpoint admission and accepted
  recovery before unlocking input; never route offline practice into server commands.
- Decide explicit terminal finish orchestration and cosmetic part selection using
  catalog/unlocks. No invented continuation or restart/abandon policy.
- Auth UI, approved visual/navigation integration, real origin/account/epoch/
  foreground/process-death checks.

## Relevant files
- docs/project-progress.md — user-facing seven-stage checklist; next unit is 3.4.
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/AcceptedAttemptCommands.kt
- android-billing/engine/src/test/kotlin/com/aladdin/wobbrik/engine/AcceptedAttemptCommandsTest.kt
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/AcceptedEngineHost.kt
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AuthenticatedRecoveryOwner.kt
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AttemptCoordinator.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/WobbrikApplication.kt
- docs/host-lifecycle-contract.md
- docs/reviews/2026-11-05-durable-attempt-commands.md

## Checks performed
- Scoped source review and git diff --check; no branch CI.
- Post-merge main ./scripts/local-ci.sh passed: 106 tasks, 32 executed, 74 up-to-date;
  core/engine tests, app assemble/lint passed. Ten command integration tests and
  six coordinator tests report zero failures/errors (11 new tests).
- No live/backend DB/device/visual checks or push/deploy.

## Exact next step
- In a new chat, design and implement the small online simulation/landing admission
  loop using AcceptedEngineHost and AcceptedAttemptCommands, with lifecycle resets,
  current-Ready identity and checkpoint retry/reconciliation gates. Keep input locked
  until this loop is verified; restart/abandon and continuation remain unresolved.
