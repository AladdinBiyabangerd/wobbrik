# Game modular atlas — in progress, 13 November 2026

Branch: `fix/game-modular-atlas`, base `826a441`. No commit/merge/push/CI.
User's pre-existing handoff/release/live-review/QA patch preserved; city source unchanged.

## Implementation under review

- `GameAtlas.kt` routes all non-complete purple/teal combinations to clean modular shells. Exact approved complete purple/teal composites remain.
- `GameFacade.kt` uses the approved 464×288 cell, pitch/depth/contour and front/right facade planes. Old wall interiors supply material only; their panel hull/frame is never rendered. Window/entrance/light IDs supply independent surface layers. No baked window/door/lamp underneath them.
- Two clean shells generated with built-in image_gen from the approved purple and teal sheets. Runtime contour clipping excludes exterior generated haze. Source `modular.png` / `modular-teal.png` keep generated RGBA unchanged. Prompt: preserve approved layout/camera/trim/material; remove all attached windows/doors/lamps, reconstruct continuous brick; teal recolor follows approved teal/ivory reference.
- Roof reprojection preserves selected ID and aligns front/side eaves. Three legacy crop samples tightened to exclude neighbouring roof fragments without changing catalog-map or city consumers. Initial roof rise rejected by user; current correction avoids counting source depth twice. It remains a review candidate, not final visual acceptance.
- Preview extras accept style and category IDs in debug activity; optional read-only `AtlasQA` telemetry supports real touch timing. Physics, SceneMotion, accepted geometry, catalog/ownership/default selection/backend untouched.

## Latest user decisions

User accepted the building bodies shown, rejected roof placement and windows. New 14-window sheet was shown; user explicitly chose **show on building first**, not implementation approval. New sheet exists ONLY in `docs/design/game-tap-v2/modular-window-proposal/`; test harness reads it directly. No production window replacement installed.

Six native host previews: `game-joins-visual/proposed-{purple,teal,plaster,panel,wood,concrete}-windows.png`. Contact sheet: `proposed-windows-on-buildings.png`; scene pixels cropped/composed only, not repainted. Purple/teal show mixed selections. Wood shows revised terminal roof. Four base window shapes are represented here; the remaining 10 candidate window types still need their own crop/projection QA after design approval.

## Checks / limits

- Normal Java21 offline core/engine/app unit + APK + lint PASS before final debug telemetry addition (21s). Telemetry APK assemble PASS (5s).
- Native host 22/22 tests PASS after roof-depth correction (26s), including all 68 catalog IDs; six additional proposal renders PASS (11s). Candidate roof/window acceptance is not implied by test success.
- Catalog map/68 IDs unchanged; no economics/backend contract change, backend modification unnecessary.
- Live emulator-5592 real touch attempts began at zero floors. Some reached 2–3 floors; latency also caused misses/topples. Partial raw `*-multi-live.png`, telemetry `*-touches.json` are diagnostic only. They are NOT final six-style acceptance. Capture automation stopped once windows required design approval.
- Requested `live/variants/all-six-emulator.png` is still the earlier rejected comparison, NOT renewed final proof. Do not label it PASS or overwrite it with host previews.
- Emulator-5586 untouched. No server-account/release-flow acceptance, performance/a11y or device-wide visual acceptance claimed.
- No token palette change; contrast rerun unnecessary. The new roof/window code must complete visual review and final focused checks after accepted artwork is installed.

## Exact next step

Get feedback on the on-building window previews. If approved, integrate all 14 new window IDs with clean contours, per-shape aspect and correct corner handling, preserving complete purple/teal acceptance unless user explicitly changes it. Verify every window/roof crop and all 68 IDs; repeat final focused unit/APK/lint. Then rebuild/install only disposable emulator-5592 and obtain actual multi-floor six-base + representative mixed/earned/paid screenshots and terminal checks. Replace requested contact sheet ONLY with clearly labelled new raw emulator captures. No physics/city edits, commit/merge/push/CI without instruction.

Final focused check on current production/debug sources: Java21 offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (13s), `git diff --check` PASS. No changes in engine/core/SceneMotion/CityScreen. Read-only telemetry capture helper saved under `render/capture-live-variants.py`; its partial diagnostics are not final evidence. Capture/logcat child processes stopped while design feedback is pending.

## Approved integration and final emulator review

User explicitly approved the on-building windows and revised roof: apply these windows, retain this roof, preserve all 68 IDs/selections; no physics/city/merge/push/CI. This supersedes the earlier pending-window notes above.

- All 14 window IDs now map one-to-one through `game-v3/windows.json`; production PNG is byte-identical to the approved proposal. Full sets also use clean modular facades so a baked window cannot override the selected ID. Approved purple/teal material/trim and base roof caps remain. The roof rise/eave correction shown to the user is retained.
- Window replacements are game-scoped in PracticeGame and AcceptedScene. Shared catalog map, ownership/unlocks/default selections, city/picker/inventory art and server contract unchanged. Corner window uses its two panes once across front/right faces.
- Normal Java21 offline core/engine/app unit + assembleDebug + lintDebug PASS on final sources (29s). Native host suite 28/28 PASS including the 68-ID sheet (29s); exact key-set check PASS: all 68 unique catalog IDs preserved, all 14 window IDs mapped. QA Python syntax and diff check PASS. No CI.
- Disposable WobbrikLive emulator5592 was restarted with host GPU after SwiftShader became slow. Final captures use 540×1200 pixels at density210, retaining 411×914dp logical portrait size. Emulator5586 was untouched. No general performance certification is claimed.
- **Real touch-built attempts:** six base sets plus mixed-base, mixed-teal, mixed-earned, all started at zero and reached three landed floors. Raw `*-multi-live.png` plus `*-touches.json` record each run; PID-scoped read-only debug telemetry only timed actual ADB taps. No physics/state injection in these nine attempts. Screenshot captures inspected. Two initial capture failures (wood/mixed-teal) were retried successfully; not counted as PASS before retry.
- Requested proof renewed: `game-joins-visual/live/variants/all-six-emulator.png`. Old rejected contact backed up as `all-six-emulator-rejected-before-modular-fix.png`. `mixed-emulator.png` contains the three real mixed attempts. Contacts compose raw screenshots without repainting scene pixels.
- **All-ID visual fixtures:** 14 combinations cover all 68 IDs, each with five seeded floors and drift2, captured active and terminal on the real Android emulator (28 raw screenshots). `live/catalog/manifest.json` records exact ID coverage. `all-68-active-emulator.png` and `all-68-roof-emulator.png` were opened and reviewed: shared deep hull/joins retained, selected windows visible, no duplicated baked windows/entrances/lights or neighbouring roof fragments. These are explicitly seeded fixtures, not touch-built towers or exhaustive enumeration of every possible Cartesian combination. Terminal scene-only debug Canvas hides the result panel solely to inspect roof joins.
- Real wood attempt also captured drop, pause/resume and actual miss → FINISHED at three floors: `wood-drop-live.png`, `wood-pause-live.png`, `wood-terminal.png` and `wood-terminal-state.json`; all opened and inspected. Roof appears only at terminal, no floor count added.
- Remaining scope limits: local debug gameplay, not authenticated server/release acceptance. No tablet/landscape, TalkBack or full product/stage acceptance claim. Backend changes unnecessary. No commit, merge, push or CI performed.

Compact check: actual emulator 360×640dp (540×960/density240), font scale2, five-floor panel fixture captured as `compact-font2-emulator.png` and inspected. HUD/pause and hint remain readable and accessible; no new HUD/layout change. Decorative garden sits behind the existing hint, so this is not a blanket full-screen parity claim. Emulator screen/density/font overrides restored to original physical1080×2400/density420/font1 afterward; left in a frozen five-floor wood terminal scene. Local final diff review found no outstanding defect in this scoped atlas change; engine/core/SceneMotion/city/catalog files have no diff. Broader online/release/product limitations above remain.

## Local commit authorization
User requested local commit after accepting the implementation. No source change since final checks; diff review/check repeated. Commit excludes pre-existing owner release files, earlier live-review/QA patch and pre-existing handoff additions; only this unit's handoff sections are staged. No merge/push/CI. Future parts require the same modular crop/surface/anchor contract and mixed multi-floor visual QA; arbitrary new art is not automatically certified by this fix.
