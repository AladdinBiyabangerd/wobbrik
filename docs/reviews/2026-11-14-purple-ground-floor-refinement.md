# Purple ground-floor refinement — 14 November 2026

Branch `fix/purple-seams-ground-contact`, working diff on `2be0583`. Scope: user explicitly requested the approved ground-floor study proportions/material joins and identified the far-right ground window as misplaced. References: `docs/context/current-task.md`, `docs/PLAN.md` §7, previous ground-floor integration review and approved `ground-floor-v1/art-study.png`.

## Change and local review

Only `PurpleGroundFloorArt.kt` production source changed in this continuation. Widened the left low bay and its closed cream cap; deepened the right stone plinth while keeping its lower endpoints. Sampled side-facing stone from the existing atlas side face, rather than stretching the bright front stone around both corners. Cap uses the existing opaque cream stone material. No asset/palette/token addition.

The right window previously crossed the roof eave: the roof's lower edge and wall/window were measured independently. A shared side-plane function now positions wall, stone course, corner columns and selected window. Repositioned the independently selected roof to that same facade edge and restored a pitched tile surface. Window quads retain upright proportions and fit between eave/columns/plinth. Existing corner-window half-sprite selection remains.

Garden/contact-shadow/path/steps/foreground assets and anchors, all category IDs/mixed mapping, upper-storey renderer/junction/balcony/main roof, six representative floors, real metadata and active gameplay are unchanged. Broader first-floor masonry necessarily changes local occlusion. No wallet/domain/API/authority change; AppBackend modification is unnecessary. Inherited dirty work preserved. No commit/merge/push/CI or emulator.

## Verification

After the final material edit: native host 5/5 PASS, 36s (30 production captures at360×640/font1,390×844/font1,320×568/font2 plus two renderer boundary sheets). Reviewed final approved-study/before/after crop, inventory/category/mixed contact, compact font2 and boundary render. Initial cap resampling exposed a transparent atlas edge; replaced it with an opaque cream material sample and rerendered before delivery.

Ordinary app24unit tests +assembleDebug+lintDebug PASS,13s after final source edit. Explicit text contrast4/4PASS,min8.22; palette unchanged. git diff --check PASS. Pixel comparison: first1400 image rows of390/font1 base detail (upper floors/UI region) identical to previous evidence; completebase_2 detail entire image identical. Main renderer git-blob `89d56e4351b3ad65c8ae32ac48a233b8a8371604` matches prior integration; final helper blob `b87a9065e74ef0d3693b182c7a179d4461312f2d`.

Evidence directory: `purple-ground-floor-refinement-visual/`; comparison is an evidence crop of actual host renders plus the approved study, not a production asset.

## Limits

This remains a modular interpretation of the approved study: stone block cadence, lighting and bespoke proportions are not pixel-identical. Full 68-ID acceptance is not claimed. Existing font2 inner/tab scrolling and initial clipped title remain outside this first-floor correction. Synthetic production Compose host fixtures do not certify live server/device/GPU/insets/TalkBack/all locales/tablet/landscape. Mobile portrait scope; no tablet or landscape redesign.
