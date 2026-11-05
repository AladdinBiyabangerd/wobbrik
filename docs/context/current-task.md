# Weighted scene motion — 5 November 2026

## Completed
- User authorized more lifelike movement while preserving approved art.
- New-attempt pendulum trajectory; legacy v1 checkpoint trajectory preserved.
- Fixed center-suspension arc, load angular inertia, impact-driven connected tower
  rotation, smooth camera. Pause freezes motion; restart resets it.
- Phone/tablet impact sequences, 40-floor renders, concept comparisons and live
  motion samples inspected. Pause captures pixel-identical after panel appeared.

## Current state
- Branch fix/weighted-game-motion reviewed; merge/main local CI pending.
- Review: docs/reviews/2026-11-05-weighted-motion.md.
- Visible game remains offline practice; accepted online input is still locked.
- Exact concept parity is open: scene/garden size, background/type/lighting.
- Unrelated untracked responsive-rule/compose-game-host files preserved.

## Decisions
- New inner rules practice-pendulum-v2; restore/encode legacy practice-balance-v1
  unchanged. Older clients reject v2 rather than replaying incorrectly.
- No AppBackend changes: opaque structural snapshot contract inspected in code;
  outer API/rules, wallet, revision/dedup, UID/epoch and retry gates unchanged.
- Motion uses original assets/tokens. Longer suspension needs taller framing.
- Visual spring state is cosmetic and resets on process recreation. No claim of
  full structural/rigid-body simulation; release has no horizontal ballistic drift.

## Remaining work
- Real-device feel/performance and user assessment of the motion excerpt.
- Existing exact-design differences; online simulation/checkpoint admission,
  accepted recovery before unlock, real auth/menu/navigation/results and catalog.
- No new phone-landscape/tablet-portrait/large-font render in this motion unit.

## Relevant files
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{GameScene,SceneMotion,MainActivity}.kt
- android-billing/app/src/debug/kotlin/com/aladdin/wobbrik/ScenePreviewActivity.kt
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/{PracticeEngine,EngineCheckpoint}.kt
- android-billing/engine/README.md and engine checkpoint/practice tests.
- docs/reviews/weighted-motion/ — frames, comparisons, live mp4/gif, pause evidence.
- docs/design/game-tap-v2/; docs/reviews/2026-11-05-game-scene-assets.md.

## Checks performed
- Branch assembleDebug passed after fixing missing ANDROID_HOME; no branch CI.
- Phone 1080x2400/420dpi; tablet landscape 1920x1200/240dpi. Impact ticks
  0/15/27/40/65/100, 40-floor swing, four-floor reference comparisons inspected.
- Live motion contact sheet inspected; settled pause scene pixel-identical.
- Scope diff review and whitespace passed; full regression pending main merge.
- No backend/live-account/physical-device/release checks, push or deployment.

## Exact next step
- Merge reviewed motion branch preserving history, run ./scripts/local-ci.sh only
  on main. If it fails, use a separate fix branch/review/merge; repeat main CI.
- After passing, update this note/review with exact hashes/results. Next chat:
  inspect the motion clip on a real device and refine only this motion if needed;
  keep unfinished online integration and broader design parity separate.
