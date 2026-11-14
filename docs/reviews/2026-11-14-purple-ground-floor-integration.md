# Purple ground-floor integration — 14 November 2026

Branch `fix/purple-seams-ground-contact`, working diff on `2be0583`. User approved ground-floor-v1/art-study.png with “okeydir”. Inherited owner diff/release/harness/assets preserved. No commit/merge/push/CI. Canonical plan: `docs/PLAN.md` §7.

## Change and local review

Added transparent ground masonry/side-roof/entrance derivative and metadata cells. PurpleGroundFloorArt owns only ground-floor art-plane geometry: broad stone plinth, cream columns/trim, projecting lower bays and small right roof. Main renderer loads the additional atlas and invokes this at i=0. Existing upper-storey shell/junction/roof/balcony logic, representative count, real metadata/geometry and active game remain unchanged.

Garden, contact shadow, steps, path and foreground source assets, anchor coordinates and draw ordering remain unchanged. Existing wall selection now covers all ground purple planes, preserving cream masonry; entrance/window/light/roof remain independent selected-category layers. Ground base_1 windows use existing square art matching the approved board; upper windows retain arch art. Corner-window sprite halves are retained. Alternate roof selections also control the side roof; their existing decorative pixels remain a limitation. Complete base_2 retains the previous renderer. No new IDs, wallet/authority/API/backend changes; AppBackend work is unnecessary for this art-only change.

Reviewed the session delta against the saved pre-session renderer, rather than treating inherited branch changes as this unit. Initial low left bay lacked a top surface; added cream cap and corner columns, then reran focused checks. Generated masonry shell did not contain the exact requested low-bay silhouette, so measured material samples are projected onto explicit low-bay planes; the shell is never shipped as a fixed assembly. No new UI palette or dimension token.

## Verification

Final opt-in native Compose host checks:5/5 PASS,34s.30 production screen captures: inventory+9details at360×640/font1,390×844/font1,320×568/font2; independent category swaps, mixed,40real floors,completebase_2. Two renderer-only boundary sheets:1/4/40floor straight/offset at font1/font2.1/4floor are stress inputs, not valid owned inventory. Inspected final detail-contact, regular boundary, font2 detail, mixed/fallback and reference-comparison. Evidence: `purple-ground-floor-visual/`. Synthetic account/HTTP fixtures drive actual production Compose screens; these are not live-server/device captures.

After final source/asset edit: ordinary app24unit tests,assembleDebug,lintDebug PASS22s. Explicit token contrast4/4PASS,min8.22. PNG2172×724 has alpha; atlas cells are within bounds. git diff --checkPASS. Metadata whitespace was minimized afterward without changing parsed cells. Emulator and CI were not started.

## Limits

Not pixel parity: six regular representative floors and inherited balcony composition differ from bespoke reference; low-bay proportions, stone sampling, surface highlights and lamp/window bounce are modular approximations. Grounding source anchors were preserved, but the broader masonry changes occlusion around them. Full68IDs, device/GPU/runtime/inset/TalkBack/all locales/tablet/landscape not certified. Font2 retains existing inner-content/tab scrolling and initial clipped title position; this unit does not claim full-screen accessibility acceptance or full phase acceptance.

Reviewed source fingerprints: renderer `89d56e4351b3ad65c8ae32ac48a233b8a8371604`, ground helper `7a74253c4194463ce30e6cafcc79be4341996071`, new PNG `f43dde8f07dbbe2b8e89b3e84ddd3c752719396e`. Generation provenance/prompt specification is in ground-floor-v1/README.md.
