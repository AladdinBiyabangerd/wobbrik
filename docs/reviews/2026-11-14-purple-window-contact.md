# Purple right window and ground contact — 14 November 2026

Branch `fix/purple-seams-ground-contact`, base `2be0583`, working diff. User rejected the prior right-window result and requested a small further grounding correction. Earlier build/render PASS did not constitute user acceptance. References: current handoff, PLAN §7, approved ground-floor study and previous refinement review.

## Scoped correction and review

The previous window correction changed its size but retained the long steep side-facade projection. The approved low annex has a shallower visible facade. Changed the right annex plane from130/−74 to95/−33 art units, with matching independently selected roof/eave, columns, stone band and window projection. Its far lower corner now ends inside the existing garden silhouette. No new art, palette, UI token, ID or whole-building bitmap.

Added footprint-following contact shadows under both low bays using the existing shadow renderer/material. Garden, path, steps, foreground asset positions and central footing remain unchanged. Upper-storey logic/roof/balcony/count and all category/mixed/corner-window selection logic remain unchanged. Local source review compared this continuation with saved pre-continuation source, preserving the inherited dirty diff. API/server/wallet/accepted geometry/gameplay are unchanged; no AppBackend modification is needed.

## Verification

Final native host5/5PASS39s:30production captures at360×640/font1,390×844/font1,320×568/font2 and2renderer-only1/4/40floor boundary sheets. Inspected approved-study/previous/current crop, category/mixed contact and font2 boundary. Pixel check: upper1400 image rows at390/font1 unchanged; completebase_2 entire image unchanged. These are synthetic account/HTTP fixtures driving production Compose, not live-device captures.

After final source edit: ordinary app24unit tests +assembleDebug+lintDebug PASS13s. git diff --check PASS. UI palette unchanged; preceding explicit contrast4/4PASSmin8.22 remains applicable.

Evidence: `purple-window-contact-visual/`. No commit/merge/push/CI, emulator or live server operation.

## Remaining limits

User visual acceptance is pending. This is not pixel parity or full68ID/phase acceptance. Prior font2 inner/tab scroll and initial title clipping remain. No device/GPU/inset/TalkBack/all-locales/tablet/landscape certification. This small continuation is complete for presentation; do not start another large feature in this chat.
