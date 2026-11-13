# Two complete style proposals — 13 November 2026

Built-in image_gen, not CLI. User requested two complete color sets retaining the game-tap-v2 style, mainly changing colors. Purple/pink and teal/ivory were chosen as proposals, not approved palette changes.

- `purple.png`: reference `../phone.png` and app `game_floor.png`.
- `teal.png`: recolor edit of `purple.png`.
- Both: 3 columns × 2 rows: complete open floor, entrance floor, roof cap; isolated window, entrance, lantern. Same general perspective/style; exact pixel geometry equality is not certified.

Not installed in app. Await concrete visual approval. Keep existing catalog IDs, ownership, server parts and old assets until replacement compatibility is established. Composite floors contain windows/entrance/lights: do not draw duplicate overlays. Independent part combinations need clean facade layers before replacing existing catalog sprites. These sheets alone do not replace all 68 catalog items.

Alpha requested and PNG has alpha, but visible exterior haze/halos remain in generated sheets. Clean cutouts, crop/anchor metadata, seam/native-density checks and production renders remain required; these are design proposals, not production-ready atlases.

## Prompt set

Purple: Use case stylized-concept. Transparent Wobbrik Android game sprite atlas. Match approved game-tap-v2 phone and game_floor reference: polished 3D purple brick, cream/pink trim, golden square windows, broad front and visible right side, open top. Exactly 3 columns × 2 rows, six isolated cells, generous transparent padding, no text/grid/background/crane/ropes/island. Top: reference-shaped complete window floor; same-size entrance floor with central wooden arch, two gold lamps and side window; matching roof cap without floor walls. Bottom: isolated square golden window; arched wooden entrance; golden wall lantern. Identical reusable modular geometry and lighting. No exterior haze.

Teal: precise-object-edit of purple atlas. Preserve six sprites, placement, dimensions, camera, contours, module footprint, windows, entrance and lantern. Only purple bricks/tiles become rich teal-blue and pink trim becomes warm ivory; keep golden light/brown wood. Same 3×2 layout. Remove background/haze/halos outside physical sprites, true alpha, no new shapes/text.

Tool calls contain the expanded production wording. No image pixels edited by scripts.

## Approval and production extraction — 13 November 2026
User explicitly approved both sheets with “təsdiqlədiyim iki atlası təmizləyib GameScene-ə qoş, fizikanı dəyişmə”, then authorized scripted contour masking after built-in background extraction retained haze (purple twice, teal once). Earlier pending-approval wording above is historical.

Production copies: `android-billing/app/src/main/assets/game-v3/{purple,teal}.png`, with `atlas.json` crop/pitch/depth/top-anchor metadata. Reproducible extraction: `scripts/clean-game-atlases.py` (Pillow); original sheets and RGB remain intact, exterior alpha masked only. Six shared cells, dark/light QA images in `docs/reviews/game-joins-visual/`. No generated cleanup output was installed. Original generation prompt set above remains the artwork source; final cleanup used explicit physical contour coordinates, no repainting.

GameScene uses purple for a partless local scene or the complete `base_1_*` set, teal for the complete `base_2_*` set. Mixed/other selections retain existing independent art. These composites do not replace the 68-item catalog or migrate picker/inventory/city art. Old atlases remain required. Full-screen phone design parity is not certified by the Canvas-only harness.
