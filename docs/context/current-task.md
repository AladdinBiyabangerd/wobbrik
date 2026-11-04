# Balance/rescue — 4 November 2026

## Completed
- Engine foundation plus persistent lean, danger hysteresis, one rescue per danger episode, perfect-landing count, damped shared sway signal, first-five forgiveness and capped speed/sensitivity.
- Product OS decision: minimum Android 7.0 / API24, target Android16 / API36, compileSdk36; billing module updated.

## Current state
- Prepared on feat/game-balance-rescue. Review and verification: docs/reviews/2026-11-04-game-balance-rescue.md; final merge outcome in chat/Git history.
- No Android game host or playable APK. Existing billing preserved; pre-existing untracked responsive rule/review excluded.

## Decisions
- practice-balance-v1 parameters are provisional arcade units, not server geometry.
- Only a landing changes persistent balance, rescue or loss. Waiting merely damps cosmetic sway.
- Offline counters confer no XP/coins/achievements/buildings. No backend changes needed for this unit; online compatibility not claimed.

## Remaining work
- Compose application host, adaptive scene, camera, tap/drop and pause/lifecycle routing.
- Process restore and online checkpoint adapter; real SKU/Console/deploy excluded.
- Device gameplay feel, performance and balance measurements still pending.

## Relevant files
- android-billing/engine/src/main/kotlin/az/wobbrik/engine/{PracticeEngine,Balance}.kt
- android-billing/engine/src/test/kotlin/az/wobbrik/engine/{PracticeEngineTest,BalanceTest}.kt
- android-billing/android/build.gradle.kts; docs/kotlin-compose-plan.md; docs/game-plan.md sections 2–4
- docs/design/game-tap-v2/README.md; docs/design/mobile-v1/tokens.css
- docs/reviews/2026-11-04-game-balance-rescue.md

## Checks performed
- Initial local CI caught incorrect danger-test setup; corrected and CI passed with ten engine tests.
- Local CI passed with SDK target and 11 engine tests including pause/restart. Final docs/post-merge CI results in chat.
- No game UI render/device test; no game UI exists yet.

## Exact next step
- After current gate, create Android Compose application host using minSdk24 / targetSdk36.
- Read ui-craft, tap-v2 design and palette first; implement an offline practice scene wired to this engine, then render/view phone and tablet sizes. Keep reward/economy absent until server adapter integration.
