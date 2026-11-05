# Host lifecycle contract — 5 November 2026

## Completed
- Read recovery core, Compose host and AppBackend start/checkpoint/finish source.
- Documented current restart/abandon constraints and selected next small unit in
  docs/host-lifecycle-contract.md. No new server semantics were invented.

## Current state
- Main now includes a2f6f96 Compose app and 18d1825 engine package rename.
- Package is com.aladdin.wobbrik.engine. App remains offline practice.
- Contract documentation only; auth/lifecycle binding is not implemented.
- Existing untracked responsive rule/review files preserved in primary worktree.

## Decisions
- No abandon endpoint; start rejects a second active attempt. Finish creates results
  and possibly buildings, not wallet credit. Checkpoints grant progression/rewards.
- Never map restart/menu/logout to finish or erase pending commands.
- Next unit is application-scoped authenticated recovery owner, with injected auth;
  engine/action/UI binding and restart economics remain separate.
- No backend/schema/rules changes required for this ownership boundary.

## Remaining work
- Implement/test owner and actual auth bootstrap; lifecycle/input/engine restore later.
- Decide restart/abandon product semantics before adding mobile/server actions.
- Continued/zero-checkpoint recovery, catalog routes, real device/process-kill tests.
- Approved graphics/navigation and phone/tablet visual comparison remain technical debt.

## Relevant files
- docs/host-lifecycle-contract.md
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{README.md,AttemptCoordinator.kt}
- android-billing/core/src/main/kotlin/appbackend/wobbrik/billing/AccountSessions.kt
- android-billing/app/{README.md,build.gradle.kts,src/main/kotlin/com/aladdin/wobbrik/MainActivity.kt}
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/EngineCheckpoint.kt
- docs/kotlin-compose-plan.md section 5
- /Users/mac/My Workspace/AppBackend/internal/wobbrik/{attempts.go,finish.go}

## Checks performed
- Scoped source/contract review; branch diff whitespace check. No branch CI.
- See docs/reviews/2026-11-05-host-lifecycle-contract.md for post-merge CI status.
- No visual changes or device/live-backend verification in this unit.

## Exact next step
- Read this and docs/host-lifecycle-contract.md. Implement only the selected auth/
  recovery owner and focused tests on a feat branch; inspect billing bootstrap first.
- Preserve unrelated files. Review/merge with history; run local CI only on main.
