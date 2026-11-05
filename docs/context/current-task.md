# Level-dependent swing speed — 5 November 2026

## Completed
- Prior horizontal camera unit is merged/validated: top-floor tracking with fixed
  on-screen crane, unchanged suspension/range; review 2026-11-05-horizontal-camera.md.
- User then requested increasing swing speed with level. Implemented +8% of base
  per landed floor, cap 3x at upcoming floor 26. Implementation f56e0f6 reviewed.
- Phone/tablet level 1/11/26 samples at tick 30 rendered and inspected.

## Current state
- Branch feat/level-swing-speed; source review complete, main merge/CI pending.
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
- Merge reviewed branch and run main local CI, then user/physical-device assessment.
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
- Source diff/whitespace reviewed. No hardware, backend or new orientation/font run.

## Exact next step
- Merge preserving history; run ./scripts/local-ci.sh on main and record actual
  results. If it fails, fix on a separate reviewed branch before remerging.
