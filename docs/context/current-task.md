# Horizontal tower camera — 5 November 2026

## Completed
- User rejected shortening suspension/swing range and explicitly chose horizontal
  camera following with the crane fixed on screen.
- Implemented v3 world/local aiming and shared camera translation; retained v1/v2
  restore behavior. Reviewed implementation 7daa3f7 on fix/horizontal-camera-follow.
- Phone/tablet left/center/right, pause and 40-floor frames rendered and inspected.
  Crane ROI pixel-identical across all three horizontal directions.

## Current state
- Review: docs/reviews/2026-11-05-horizontal-camera.md.
- Branch assembled and APK installed. Merge/main CI pending at this note revision.
- Visible game remains offline practice; accepted online input locked.
- Unrelated untracked responsive-rule/compose-game-host files preserved.

## Decisions
- Camera origin is the last landed floor center; craneX stays local pendulum x.
  Falling/landed floor centers are world x; drop = origin + local x.
- Horizontal camera recenters atomically on landing; vertical smoothing retained.
- New rule practice-camera-v3; legacy v1/v2 retain original fixed-origin behavior.
- No mass-distribution solver, art/palette/framing or suspension/range changes.
- AppBackend opaque JSON contract inspected; no backend change needed. Wallet,
  revision/dedup, account isolation/retry/recovery gates unchanged.

## Remaining work
- Main local CI after reviewed merge; then physical-device/user assessment.
- Broader exact-design differences and real auth/menu/results/catalog/online host
  remain separate unfinished work; this camera unit does not complete the game.

## Relevant files
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{GameScene,SceneMotion,MainActivity}.kt
- android-billing/app/src/debug/kotlin/com/aladdin/wobbrik/ScenePreviewActivity.kt
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/{PracticeEngine,EngineCheckpoint}.kt
- android-billing/engine/src/test/kotlin/com/aladdin/wobbrik/engine/{CameraFollowTest,BalanceTest}.kt
- android-billing/engine/README.md; docs/reviews/horizontal-camera/

## Checks performed
- Branch assembleDebug passed: 34s, 66 tasks, 8 executed. No branch CI/tests.
- Phone 1080x2400/420dpi, tablet landscape 1920x1200/240dpi inspected, with
  concept comparisons. Source diff/whitespace reviewed. Device overrides restored.
- No physical-device, new phone-landscape/tablet-portrait/font-scale or backend run.

## Exact next step
- Merge reviewed branch preserving history and run ./scripts/local-ci.sh on main.
  Record actual result; if it fails, fix on a separate reviewed branch and remerge.
