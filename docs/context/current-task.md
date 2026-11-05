# Game scene asset integration — 5 November 2026

## Completed
- User approved four replacement sprites and authorized integration/comparison.
- Runtime copies preserve approved PNG pixels/alpha; matching landscape cloud added.
- GameScene replaces schematic floor/crane/island/roof. Ring/cable anchors, guide,
  wide-window framing and tall-tower viewport culling corrected after rendering.
- Phone/tablet active/pause comparisons saved beside approved concepts. Landscape
  phone, font 1.5/short window and 40-floor crane-limit renders inspected.

## Current state
- Implementation b638536 merged as 55a28e5; main local CI passed (106 tasks,
  10 executed / 96 up-to-date). Validation note merged separately; see review.
- Sprites integrated. Exact concept parity is NOT complete: scene/garden proportions,
  native type sizes, background and panel/guide/cable lighting still differ.
  Full visual checklist remains unchecked, with a scoped progress note.
- Visible scene remains offline practice. Online AcceptedEngineHost input stays
  locked; prior durable command work is merged (9b38e31, before checklist commits).
- Unrelated untracked responsive rule and compose-game-host review files preserved.

## Decisions
- Visual-only work needs no AppBackend/domain/API/DTO/schema/wallet changes.
- No invented restart/abandon, continued physics or reward source.
- Frozen ScenePreviewActivity is debug-only, not a real accepted online attempt.
- Keep structural-v1 accepted recovery, sole journal and durable command authority.

## Remaining work
- Close visual differences against comparison boards before claiming exact design.
- Then 3.4: online simulation, per-landing checkpoint admission, accepted recovery
  before input unlock; terminal checkpoint → fresh Ready → explicit finish.
- Cosmetic catalog/unlocks; auth and real menu/navigation/results; live origin,
  account/epoch/foreground/process-death checks. Continued stabilization unresolved.
- Physical-device performance/release/accessibility checks remain outstanding.

## Relevant files
- docs/reviews/2026-11-05-game-scene-assets.md — review/results/differences.
- docs/reviews/game-scene-assets/*-comparison.png — approved concept vs emulator.
- docs/design/game-assets-v1/ — approved art, prompts and hashes.
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/GameScene.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/MainActivity.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/GameTokens.kt
- android-billing/app/src/debug/; scripts/capture-game-scene.py.
- docs/project-progress.md; docs/design/game-tap-v2/README.md.
- engine AcceptedEngineHost/AcceptedAttemptCommands; docs/host-lifecycle-contract.md.

## Checks performed
- Branch :app:assembleDebug passed; no branch CI. Contrast pairs 8.22–16.57 pass 4.5.
- Whitespace/capture syntax; source/runtime PNG byte hashes and alpha checked.
- Phone 1080×2400/420dpi; tablet 1200×1920 + 1920×1200/240dpi;
  phone landscape 1600×720/280dpi; 1600×800/320dpi/font 1.5 inspected.
- Rejected splash capture and rerendered. Initial emulator resize crash documented.
- Real MainActivity: pause barrier and resume do not drop; restart + tap lands
  count 1. Main CI passed after merge; review records exact result.
- No physical-device/live-backend checks, push or deploy.

## Exact next step
- Read this note and the game-scene-assets review, inspect the three comparison
  PNGs, and finish concept/layout/type/lighting differences in a new small visual
  branch. Re-render phone/tablet, review, merge, then local main CI. Keep full design
  checklist open until comparison supports completion. Do not fold online 3.4 or
  backend rule changes into visual finishing.
