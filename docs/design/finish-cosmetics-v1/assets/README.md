# Finish cosmetic atlas candidates — 8 November 2026

Generated with the built-in imagegen tool after explicit user authorization to prepare missing assets in the approved style. These are source candidates, not integrated or runtime-accepted game assets. Original design remains `../phone-tablet.png`.

Five square transparent atlases, each requested as a 4×4 equal-cell grid in row-major order. `catalog-map.json` records the intended server ID assignment. This is artwork metadata only: never use it as a replacement catalog, ownership source or grant. Current server catalog contains 68 building parts and 12 unrelated decorations; decorations are excluded.

Visually inspected all five generated outputs. The purple/ivory/gold material language is consistent, and all intended occupied cells appear. Limitations: generated translucent halos and irregular margins require a dark-background crop/composition check before shipping. Wall camera differs from diamond-footprint roofs; a verified common projection/anchor is still needed for a composed building preview. No renderer or runtime UI consumes these files yet. Do not claim final preview parity or production readiness.

Keep source images unchanged. Do not silently replace these with schematic placeholders. Final pixel crop bounds/anchors must be established from the actual images, not assumed from nominal grid cells. `catalog-map.json` cell indices are intended order, not verified pixel crop bounds.

Prompts: `prompts.json`. All generation requested true transparent backgrounds. No CLI/API fallback used. Original generated PNGs were copied into this directory without image manipulation.

## Integration update — 8 November 2026

Original candidate PNGs remain unchanged. User subsequently authorized compatible
asset variants when mobile footprint is reasonable. Built-in imagegen produced
`roof-atlas-v2.png` from the original roof atlas plus wall camera reference; all
14 roof identities/order remain, with rectangular facade-facing eaves. Exact
prompt is `prompts.json` → `roof-v2`; no CLI/API image generation was used.

Runtime uses five lossless WebP atlases under
`android-billing/app/src/main/assets/finish/` (the original diamond roof is NOT
packaged). `catalog-map.json` there has 68 verified alpha-body crop rectangles
(threshold 160/255, nominal 4×4 half-open cells) and the same IDs/categories as
the server. It is artwork metadata, never catalog/ownership authority. Crops are
precomputed; app startup does not scan pixels. Common roof baseline anchors the
eave to the facade. All five PNG→WebP outputs were decoded with dwebp and every
RGBA byte compared with source: identical, including alpha/transparent RGB.
Codec: cwebp 1.6.0, `-lossless -exact -m 6`.

Logical runtime atlas bytes: 6,279,700 (5.99 MiB), versus 9,424,751 source PNG
bytes; 33.4% smaller. Five 1254×1254 ARGB bitmaps occupy 31,450,320 bytes
(30.0 MiB), excluding GPU/Compose/other app memory; no heap profiling claimed.
463,736 complete combinations are composed from these 68 modules rather than
stored as separate images. Runtime render evidence and remaining visual/live
limits: `docs/reviews/2026-11-08-finish-cosmetics-integration.md`.
