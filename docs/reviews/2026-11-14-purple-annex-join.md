# Purple annex-to-core join — 14 November 2026

Branch `fix/purple-seams-ground-contact`, base `2be0583`, working diff. User clarified that the detached element is the window-bearing right annex, not the window, and circled the annex on the previous comparison. This supersedes prior window-focused interpretations. Approved ground-floor study and PLAN §7 remain the visual sources.

## Correction and local review

Only `PurpleGroundFloorArt.kt` changed in this continuation. Removed this turn's unaccepted experimental window aperture/contact-seam code by restoring the exact saved pre-turn helper; window/roof/ground/selection anchors retain the preceding state. The experimental window-seat captures are superseded and are not final evidence.

Added the missing exposed return face from the annex's near edge into the central core: purple masonry under the roof, continuous cream stone footing and wrapping trim. The return shares the window-facing wall edge at every material boundary; its rear edge overlaps the core rather than leaving an open volume. The stone/trim course follows the front facade slope, while the upper masonry closes the roof gable.

No new asset/palette/UI tokens, no whole-building bitmap. Ground contact/shadow/path/steps/foreground from the previous unit, independent selected wall/window/roof/entrance/light IDs, corner-window halves, mixed mapping, upper-storey rendering/count, real geometry and gameplay remain unchanged. No domain/API/backend change; AppBackend work is unnecessary. Inherited dirty work preserved; no commit/merge/push/CI/emulator.

## Checks

After the final return-course edit: native5/5PASS35s (30production screens at360/390font1,320font2;2renderer-only1/4/40floor boundary sheets). Inspected final study/before/after, annex close crop, category/mixed contact and font2 boundary. Ordinary app24unit+assembleDebug+lintDebugPASS13s. Upper1400 image rows at390/font1 and completebase_2 whole-image pixel comparison PASS. git diff --check PASS. Palette unchanged; previous contrast4/4min8.22 remains applicable. Evidence: `purple-annex-join-visual/`; compare approved study, previous detached annex, and current closed return. Synthetic production Compose host only; not live-device/server/GPU acceptance.

## Limits

User visual acceptance remains pending; previous checks were not acceptance. Full pixel parity/68ID/phase acceptance not claimed. Existing font2 title clipping/inner/tab scroll remain. Device/insets/TalkBack/all-locales/tablet/landscape not certified. Continue only with the user's concrete feedback or separate local commit instruction.


## Material integration continuation — 14 November 2026

User requested a less patched-on appearance after the missing return was closed. Only the ground-floor helper changed. Return now reaches the core corner at x240; belt endpoints meet the existing core belt at287/301, and the plinth starts/ends at293/361. This covers the redundant light seam rather than leaving a separate attachment strip.

Previous code squeezed the entire387px wall sample onto a34px return and95px annex facade, creating much denser brickwork than the core. The helper now samples corresponding UV subsections: return .82–1 / .30–1 and annex facade0–.42 / .50–1. Stone and trim return use the matching narrow source section. Existing selected wall materials use the same UV sampling; selection IDs/mapping remain unchanged. No new asset, colour, UI token or alternate design; roof/window positions, ground/shadows/path/steps/foliage, upper floors and gameplay remain unchanged.

Local review: checked this continuation against saved pre-continuation helper; sampling remains inside sprite cells and inherited work is preserved. Final native5/5PASS40s;30production captures+2boundary sheets. Inspected close comparison, full context, selected-category/mixed and font2 boundary. Upper1400px and completebase_2 whole-image pixel comparisons PASS. After final source edit: ordinary app24unit+assembleDebug+lintDebugPASS13s; git diff --checkPASS. Palette unchanged; preceding contrast4/4min8.22 remains applicable. Evidence: `purple-annex-blend-visual/`. User visual acceptance remains pending; previous runtime/68ID/font2 limits still apply. No backend/API change or AppBackend work needed. No commit/merge/push/CI/emulator.


## Marked rear roof-to-wall junction — 14 November 2026

User marked the small annex roof's rear joint with the main wall. Only `PurpleGroundFloorArt.kt` changes in this continuation. Rear target edge now follows the core side plane114/−65 rather than the shallow annex-front slope95/−33; front eave/window/wall/plinth/ground anchors stay fixed. The roof atlas rear vertex was incorrectly sampled at139/12 (transparent region); inspection of the isolated existing roof sprite located its real corner near208/12. Corrected this source point instead of covering the transparent wedge. A narrow8art-unit cream flashing from the existing trim sample overlaps the roof/wall joint; alternate selected roofs retain their independent art and the same architectural wall joint. No new art/palette/UI token/ID, no upper-storey/gameplay/backend change. AppBackend work unnecessary.

Initial render exposed the transparent rear wedge despite the target-edge correction; fixed the source corner, narrowed the flashing and rerendered. Final native5/5PASS34s (30production screens+2boundary sheets); inspected close/context/category/mixed/font2boundary. Upper1400px/base_2 whole-image comparison PASS. After final source edit: ordinary app24unit+assembleDebug+lintDebugPASS17s; diffcheckPASS. Palette unchanged, preceding contrast4/4min8.22 applies. Evidence: `purple-roof-join-visual/`. No commit/merge/push/CI/emulator; prior synthetic/runtime/font2/68ID limits remain. User visual acceptance pending.
