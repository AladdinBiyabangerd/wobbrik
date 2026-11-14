# Purple junction and ground contact review — 14 November 2026

Branch: `fix/purple-seams-ground-contact`, inherited working diff on `2be0583`. No commit/merge/push/CI. Previous owner assets, release files, harness edits and historical reviews are preserved. This review covers only this session's delta from the modular-v2 source fingerprint `223550b5ba3d73d27b7fa6f7f46866e37cf3f0b0`, not approval of the entire inherited branch diff. Canonical plan: `docs/PLAN.md` §7.

The user specified the left reference, then narrowed the fix primarily to storey junctions and grounding. Six representative storeys, silhouette/offset policy, selected IDs/mixed selections, metadata, accepted geometry and gameplay are retained. No backend/domain/API change; AppBackend changes are unnecessary for this renderer-only delta.

## Changes and local review

- Exclude the shell's lower skirt from the repeated masonry crop; draw exactly one thin shared cornice from measured cream atlas samples per junction. Upper masonry ends at that joint's upper edge; front/right planes share the same corner. The initial cap crop exposed a protruding edge; moved its samples inside the opaque stone and removed the protrusion. Existing roof and balcony selection/placement remain unchanged.
- Replace the independently positioned elliptical shadow with a softly blurred polygon following the actual foundation footprint. Move the garden down to support the full front stair/path. Render independent alpha foliage in front of the footing and a short stone path behind the stairs. Initial path extended past the grass; final placement/size stays on the island. There is no rectangular/diagonal garden redraw across the masonry.
- Add `assets/building-purple/foreground-v1.png` and explicit cells in `atlas.json`; existing atlas/foundation bitmaps are unchanged in this delta. The new layer is derived from the approved reference, not a new whole-building bitmap. `rememberPurpleBuildingArt` loads its cells with the existing remembered atlas set. Boundary harness output now respects the same opt-in output-directory property as the inventory fixture, preserving historical renders.

Final reviewed git blob hashes: renderer `67e2f44a62e060f2cf4109f85e5ac703691d3cf1`; metadata `7f93a27cb2e44b5d28eabfd1d0af19bd35bcc969`; foreground PNG `ba610f94cfda1969e3e7a9a21cd53133971eac38`.

## Evidence and checks

`purple-seams-ground-visual/` contains 30 real production Compose screen captures driven by synthetic account/HTTP fixtures: inventory + nine details at 360×640/font1, 390×844/font1 and 320×568/font2. Nine details include independent category swaps, mixed selections, 40 real floors and complete base_2 fallback. Two renderer-only boundary sheets cover 1/4/40 floors, straight/offset forms and font1/font2; 1/4 floors are renderer stress cases, not valid owned-inventory objects.

Final native host run: 5/5 PASS, 32s. Inspected final boundary/font1, compact font2, normal portrait mixed selection, all nine detail variants in `detail-contact.png`, and reference/before/after. Normal app unit 24/24, assembleDebug and lintDebug PASS, 23s after the last production edit. Palette unchanged; explicit contrast pairs 4/4 PASS, minimum 8.22. `git diff --check` PASS. No emulator, live session or CI.

## Remaining visual work / latest user feedback

The user says the first floor now sits relatively well on the ground, but the first floor's own design is poor. This is unresolved: the current ground module is a single narrow volume, whereas the approved left reference has a broader stone plinth, entrance framing, left/right lower volumes and a low side roof. Do not call the first-floor design accepted or solved by foliage/shadow changes. The next separate unit should prepare the matching modular ground-floor artwork/composition from `join-reference.png`, preserving independent entrance/window/wall/light selection, and show it before integration. Do not substitute an entire fixed building bitmap.

Other inherited differences remain: regular six-storey assembly and repeated balcony arrangement versus the bespoke four-storey reference. Full 68-ID art acceptance, device/GPU/runtime/insets/TalkBack/all locales/tablet/landscape were not performed. Font2 retains the existing inner-content/tab scrolling; a clipped initial title position is not a new full-layout acceptance. No full phase or reference pixel-parity claim.

## Generated asset provenance

Built-in imagegen, transparent output, reference image `docs/design/building-purple-v1/join-reference.png`. Original output: `/Users/mac/.codex/generated_images/01a1dac6-c8e5-7f02-a12d-2add08e757a1/exec-207d7ed4-1ed4-4b16-9a10-83e39bf4f664.png`; copied unchanged into the project as `android-billing/app/src/main/assets/building-purple/foreground-v1.png` (2172×724 RGBA). Crops are metadata only.

Prompt:

> Create a production game sprite sheet of ONLY detached foreground garden details from the reference, precisely matching its polished purple-house illustrated 3D art style, warm lighting, leaf forms, cream paving stone material, camera and perspective. Do not draw any building, door, stairs, roof, backdrop, ground island or labels. Genuine transparent background including space between individual pieces. Three isolated generously separated elements in a single horizontal row: LEFT a small low flowering shrub cluster with pink blossoms, yellow flowers, lush green rounded leaves and a few grass blades at its root, natural irregular transparent outline, NO flat cut edges; CENTER a short path of three separate cream irregular flat stepping stones continuing diagonally down-left towards the viewer from a house doorstep, with a few tiny grass blades in gaps; RIGHT a small lower flowering green shrub cluster with pink flowers, irregular transparent outline, slightly different from the left cluster. Match the precise reference garden plants and paving texture; the shrub clusters will partly overlap the bottom of a stone foundation so keep them low, not tall trees. Each element fits its own third of the canvas with ample transparent gutter. No shadows painted into a solid background, only soft local translucent contact shadows.
