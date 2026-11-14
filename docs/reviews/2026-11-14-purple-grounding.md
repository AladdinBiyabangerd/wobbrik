# Purple foundation and lighting review — 14 November 2026

Branch: `fix/mobile-consistency-performance`; reviewed working diff on `2be0583`, scoped production change `PurpleBuildingArt.kt`. Owner files preserved; no commit/merge/push/CI.

Read current handoff, PLAN §7, approved join reference, production atlas/metadata, existing GameTokens and ui-craft. Existing approved design and the user's direct correction request authorize this adjustment; no alternate design or new asset.

Findings and fixes:
- The garden ended 78 scene units beyond the declared scene height. Increased bottom scene allowance without moving the building's architectural anchors; the complete island now fits.
- Garden was centred independently of the ground-floor offset. It now follows the foundation anchor, with a raised rear edge, soft compressed contact shadow beneath the masonry and selected perimeter foliage redrawn in front.
- Base_1 lamps and windows had self-contained sprite glow but no surrounding surface bounce. Warm light is sampled from the approved lamp artwork. Window bounce is clipped to its facade; ground lamp/step bounce only follows base_1 lighting. Existing selected-part replacement logic is unchanged.
- Initial foliage polygon produced a diagonal grass wedge across the footing. Restricted the final foreground mask to outer foliage margins, preserving entrance and step visibility.

Final local review: unchanged catalog IDs, mixed selection policy, six representative floors, real floor metadata, accepted offsets and API/authority. No backend change required. Existing raster roof/window/entrance/light/wall replacements still render; atlas pixels remain unchanged. No new UI palette/type/spacing values. Art coordinates and opacity are renderer measurements, not layout tokens.

Validation after final production edit: native PurpleInventoryRenderTest3/3PASS23s,27captures at360×640/font1,390×844/font1,320×568/font2, inventory plus8details including each category swap, mixed and40real floors. Final inventory/detail contact, reference comparison and font2 screenshot inspected. Ordinary app24unit tests, assembleDebug and lintDebugPASS13s. Token contrast4/4PASS,min8.22; diffcheckPASS.

Evidence: `purple-grounding-visual/inventory-detail-contact.png`, `reference-comparison.png`, individual `production-*.png`; prior regular screenshots retained under `before/`. These are actual production Compose screens with synthetic account/HTTP fixtures, not live server/device captures. Emulator and CI were not started.

Limits: recurring modular cornices, six-floor composition and bespoke reference offsets remain visibly different; exact pixel parity and full68ID parity are not claimed. Font2 retains existing inner content/tab scrolling. Device/GPU/insets/TalkBack/all-locale/tablet/landscape were not checked. Other styles, city and active gameplay are outside this change.
