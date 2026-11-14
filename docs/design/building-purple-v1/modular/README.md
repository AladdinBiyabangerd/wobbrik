# Purple modular template study — 14 November 2026

The user approved the purple building and then explicitly requested compatible modular assets and a rendered assembly study. The existing selected-part mechanism remains the target: a new visual template, not a second inventory/economy system.

`atlas-v1.png` is a built-in imagegen derivative of `../approved-reference.png`. `atlas-v1.json` records the measured sprite crops and repeat pitch. The atlas has true alpha,1254×1254px. Do not treat this as a published catalog atlas: arch/square are study layer names, not new owned IDs. The second window demonstrates layer replacement only; exact existing catalog mapping remains to be done.

Compose/Canvas assembly: `docs/reviews/mobile-sizing-visual/render/ModularBuildingStudyTest.kt`. Review-only code loads this atlas and separately draws shell, ground, roof, projected front/side windows, entrance, lamps, balconies and garden. No whole-building bitmap is used in that assembly. The render tests do not replace production BuildingPreview or change existing gameplay rendering.

Source generated file: `exec-3c93a276-2180-4c92-a81a-b3edc234b740.png`. Copied into this directory without replacing previous approved art. Built-in imagegen; no CLI fallback.

## Generation prompt

+Use case: stylized-concept. Asset type: ONE modular sprite atlas for reconstructing the approved purple Wobbrik building. Reference image: authoritative style, materials, camera and architectural proportions. Produce a square transparent PNG atlas in an EXACT 3 columns by 3 rows equal-cell grid, nine disconnected sprites, no labels, no grid lines. Every sprite entirely within its own cell with transparent margin; no sprite crosses a cell boundary. Consistent polished painterly 3D casual game rendering, purple textured brick, cream stone trim, golden window glow, elevated three-quarter view with FRONT and RIGHT SIDE visible. These are separate production modules, NOT nine whole houses.
Top left cell: one chunky repeatable purple storey shell, BLANK walls with NO windows, doors, lamps, plants or balconies. Broad deep volume, thick cream top and bottom cornices and cream corner pilasters, parallel vertical edges, flat horizontal stacking joints. Front face about 65% of visible width, right side35%. Building shell fills 85% cell width and60% height. Top and bottom matching footprints so repeated shells can stack vertically.
Top middle: purple tiled hipped roof with dormer and stone chimney, matching the shell's exact footprint and perspective, wide cream eave. Roof only, no walls below eave.
Top right: ground storey shell with cream masonry lower base and purple blank wall above, same shell width/perspective, no door windows or lamps or vegetation.
Middle left: one large warm golden glowing arched window in cream stone frame, FRONT-facing flat decal, transparent surrounding, no wall slab.
Middle middle: alternate rectangular double casement golden glowing window, same scale and material, front-facing flat decal, transparent surrounding, no wall.
Middle right: cream arched wooden entrance with short stone steps, matching FRONT facade perspective, two potted topiary at the foot, no wall background, no lamps.
Bottom left: projecting cream stone balcony slab with fine black railing, colorful flowers, hanging ivy, matching front facade perspective, no wall/window attached.
Bottom middle: small wide grassy oval foundation patch with flowers and bushes at perimeter, EMPTY CENTER for house placement, no house.
Bottom right: isolated pair of warm glowing wall lanterns separated by transparent space, FRONT-facing, no wall attached.
Match approved image fidelity, avoid skinny stacks, schematic geometry, flat illustration, text, backgrounds, cast shadows outside cells. True alpha transparency.

## Production integration — 14 November continuation

The source study remains archived here. Production copies are `android-billing/app/src/main/assets/building-purple/atlas.{png,json}`; loader and measured surface anchors are in `PurpleBuildingArt.kt`, called by the existing shared `BuildingPreview` in inventory/detail/part edit. Existing `parts` maps and ownership remain authoritative. `base_1_wall` selects the blank shell, `base_1_window` the arch, `base_1_roof` the roof, `base_1_entrance` the entrance, and `base_1_lighting` one lantern repeated at the two ground anchors. Square is unassigned study art, not a new catalog ID. Other selected IDs use their existing catalog sprites; wall material is sampled inside the pilasters and above the foundation, corner windows are split into front/side panes. Balconies/garden are template trim, not new owned parts. Mixed maps with any base_1 part keep this template; complete other styles retain the previous renderer. At most six representative floors are displayed, with accepted offsets sampled/softened by the existing presentation rule. Actual floor count remains in metadata and accessibility.

`../join-reference.png` is the user's additional floor/foundation reference. The assembly retains overlap at repeat joints, places the masonry base within the garden, and terminates the entrance steps on it. Repeated cornices and catalog replacements remain distinguishable from the bespoke reference; this is not pixel-identical artwork or full-catalog art certification.
