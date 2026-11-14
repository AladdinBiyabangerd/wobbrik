# Purple modular v2 local review — 14 November 2026

Branch: `fix/mobile-consistency-performance`, working diff on `2be0583`. User explicitly approved `docs/design/building-purple-v1/modular-v2/art-study.png` (“okey”) before implementation. Existing owner edits/release/contact sheets/harness changes preserved. No commit, merge, push, deployment or CI.

## Scope and changes

Canonical plan: `docs/PLAN.md` §7. Read current handoff, approved join reference, atlas metadata/source, PurpleBuildingArt, existing GameTokens and ui-craft/imagegen guidance. Generated transparent modular derivatives with built-in imagegen. The study board and reference are not shipped whole-building bitmaps.

- Production atlas replaces the old self-contained box artwork with open-top shells, independent windows/door/lantern/roof/balcony. The renderer samples measured facade quadrilaterals, excluding open top and redundant top cornice. Only the lower seam is repeated.
- An upper storey's bottom uses the lower storey's top offset, joining both front and side faces continuously. Facade windows/material/door/lantern projections use the same measured planes. Roof eave correspondence uses three measured points instead of independent rectangular positioning.
- Separate foundation atlas contains empty garden and correctly sloped stairs/pots. Landing aligns with the projected door threshold. Removed the rectangular foliage redraw: the boundary render exposed an obvious cut across the masonry.
- Existing category IDs and mixed selection mapping remain unchanged. Each wall/window/roof/entrance/lighting category and mixed selection still draws independently; complete base_2 falls back to the existing renderer. Six maximum representative floors and actual metadata remain unchanged. No wallet, ownership, domain, API, server geometry, gameplay or city change; no AppBackend change is needed for this art-only contract.

## Review and evidence

Inspected final production inventory/detail contact sheet, 320/font2 detail, 1/4/40-floor renderer boundary sheet and complete base_2 fallback. Compared approved reference, before and after in `purple-modular-v2-visual/reference-comparison.png`. First iteration showed disconnected offset edges and roof angle mismatch; fixed common edges/eave correspondence. Boundary render exposed garden mask cuts and frontal stairs; removed the mask and supplied independent projected stairs/pots.

Evidence: `purple-modular-v2-visual/` — 30 production screen captures (inventory +9 details ×360×640/font1,390×844/font1,320×568/font2), 2 renderer-only boundary sheets (1/4/40 floors, straight and offset; font1 and font2). The 1/4-floor cases are renderer stress inputs, not valid owned buildings. Initial attempt to use them as inventory HTTP fixtures failed because inventory requires at least10 floors; corrected the fixture and separated those renderer-only checks. No production domain validation was weakened.

Final native host checks:5/5 PASS,36s. Synthetic account/HTTP fixtures drive the real production Compose screens; these are not live-server/device screenshots. Final normal app24unit tests +assembleDebug +lintDebug PASS,22s after the final source/asset edit.

Existing palette unchanged. Explicit text/background contrast pairs4/4 PASS, minimum8.22. Automatic role inference initially compared Raised against Surface/Background as if text and reported1.27; these are surface fills, not changed text pairs. `git diff --check` PASS.

Reviewed source fingerprints (git blob hashes): PurpleBuildingArt `223550b5ba3d73d27b7fa6f7f46866e37cf3f0b0`; atlas metadata `a8deca6dd9aecc6631569181033e6d2dec394bb7`; atlas PNG `a76aabfda46152a37a90e73523c9b3519f9943ed`; foundation PNG `7febbb5f636c062533cee3284851394beefadce5`.

## Limits

This fixes modular connection/projection defects; it is not pixel parity with the bespoke reference. Six representative storeys, regular repeated trim, one repeated balcony arrangement and alternate catalog art remain different from the approved four-storey illustration. All68IDs were not visually certified. The selected alternate entrance retains its own decorative pixels. Font2 retains existing inner content and horizontal tab scroll. Device/GPU/insets/TalkBack/all-locales/tablet/landscape, live account and runtime were not checked; emulator was not opened. No full phase acceptance claim.
