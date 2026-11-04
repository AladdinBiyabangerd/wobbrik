# Game engine foundation — 4 November 2026

## Completed
- Pure Kotlin engine: bounded 60 Hz stepping, crane, drop/fall, support overlap, miss, pause/resume and restart.
- Five regression tests and local CI integration. Existing billing preserved.

## Current state
- Branch feat/game-engine-foundation; final review/commit/merge gate in progress.
- No Android game host or playable APK. Pre-existing untracked responsive rule files excluded.

## Decisions
- Offline engine has no wallet/reward/progression/ownership effects; no backend changes required.
- PracticeRulesV1 uses provisional simulation units, not server geometry.
- Verified Play phone-app targetSdk requirement is API 36; product minSdk remains undecided. Existing billing library minSdk is 23.

## Remaining work
- Balance/sway/rescue, first-five forgiveness, capped difficulty.
- Compose host, scene tap, pause/lifecycle, camera and adaptive renders.
- Online checkpoint integration/persistence later; real SKU/deploy excluded.

## Relevant files
- android-billing/engine/README.md and src/main/kotlin/az/wobbrik/engine/PracticeEngine.kt
- android-billing/engine/src/test/kotlin/az/wobbrik/engine/PracticeEngineTest.kt
- docs/reviews/2026-11-04-game-engine-foundation.md
- docs/kotlin-compose-plan.md; docs/game-plan.md sections 2–4
- docs/design/game-tap-v2/README.md; docs/design/mobile-v1/tokens.css

## Checks performed
- Local CI passed: five engine tests, billing core tests, billing Android debug assembly/lint.
- No game UI render/device check: game UI does not exist yet.

## Exact next step
- Complete foundation final CI/commit/merge record, then implement bounded engine balance/rescue against game-plan sections 3–4 before the Compose host.
