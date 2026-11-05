# Level-dependent swing speed — 5 November 2026

## Completed
- Prior horizontal camera unit is merged/validated: top-floor tracking with fixed
  on-screen crane, unchanged suspension/range; review 2026-11-05-horizontal-camera.md.
- User then requested increasing swing speed with level. Implemented +8% of base
  per landed floor, cap 3x at upcoming floor 26. Implementation f56e0f6 reviewed.
- Phone/tablet level 1/11/26 samples at tick 30 rendered and inspected.

## Current state
- feat/level-swing-speed reviewed and merged preserving history as 10c611f.
- Main CI passed in 32s: 106 tasks, 11 executed / 95 up-to-date; 43 engine tests
  passed with zero failures/errors. Documentation follow-up repeats main CI.
- Review: docs/reviews/2026-11-05-level-swing-speed.md.
- Debug APK installed; visible game remains offline practice, online input locked.
- Unrelated untracked responsive-rule/compose-game-host files preserved.

## Decisions
- Level means in-attempt upcoming floor, not backend account XP level.
- New practice-speed-v4 curve is independent from balance difficulty. Gravity,
  balance/scoring, suspension/range and camera behavior are unchanged.
- v1/v2/v3 checkpoints retain their original speed and camera behavior. v3/v4
  share followsTower; older clients fail closed on v4. JSON keys unchanged.
- AppBackend opaque structural-v1 JSON contract inspected; no backend change.

## Remaining work
- User/physical-device assessment of level speed; no hardware test claimed.
- Broader design parity and real auth/menu/results/catalog/online integration remain
  separate unfinished work; this speed unit does not complete the product.

## Relevant files
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/{PracticeEngine,EngineCheckpoint}.kt
- android-billing/engine/src/test/kotlin/com/aladdin/wobbrik/engine/SwingSpeedTest.kt
- android-billing/engine/README.md; docs/reviews/level-swing-speed/
- Camera implementation: app/src/main/kotlin/com/aladdin/wobbrik/{GameScene,SceneMotion,MainActivity}.kt under android-billing.

## Checks performed
- Branch assembleDebug passed: 27s, 66 tasks, 8 executed. No branch CI/tests.
- Phone 1080x2400/420dpi and tablet landscape 1920x1200/240dpi samples inspected.
  First two incomplete tablet frames rejected and recaptured; settings reset.
- Source diff/whitespace reviewed; main build/lint and regression gate passed.
  MainActivity launched after APK install. No hardware/backend/new orientation run.

## Exact next step
- Assess swing speed in a new attempt: 1x at floor 1, 1.8x at floor 11, cap 3x
  at floor 26. Next chat: read this note and the level-swing-speed review first.
  Keep broader design parity and accepted online integration separate.
