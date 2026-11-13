# City-view modular garden assets

Reference: `../city-view.png`, already approved. This is a component extraction/reconstruction for that design, not a replacement city composition. User instruction: complete its shoreline/garden assets and remaining composition differences while preserving the existing diff.

Built-in imagegen was used, with the reference image. Production output is `android-billing/app/src/main/assets/city/garden-v1.png`; crop metadata is its sibling JSON. The generated source includes an unused entrance/step component: it is deliberately not drawn over selected doors/lamps or outside the accepted footprint. No catalog IDs, owned decorations, routes or server state are created by these presentation ornaments.

## Generation prompt

Extract/reconstruct production modular sprite assets matching the supplied approved city-view illustration EXACTLY in style, palette, lush painted 3D game quality, lighting and isometric projection. Output a transparent RGBA sprite sheet, 1536x1024, arranged as SIX isolated sprites in a strict 3 columns x 2 rows grid with wide transparent gutters, NO labels/text/UI/background/buildings. Top row: (1) narrow continuous horizontal strip of the golden grassy island lip with warm brown faceted cliff below, straight horizontal usable texture strip (2) a small lush mixed pink/yellow flower shrub like the reference perimeter plants, no pot (3) a smaller yellow-flower grass tuft. Bottom row: (1) front-facing isometric balcony flower box, pink/yellow flowers with green trailing ivy, pale cream ledge and dark thin railing, no wall/window behind (2) side-facing companion flower box matching that balcony, opposite isometric face (3) two compact entrance topiary plants in terracotta pots with a short cream stone doorstep, no door/wall. Assets are components only, not a new design, not complete island or building, preserve reference material/color/detail. Each sprite fits fully inside its own cell, no overlap, transparent background.

## Alpha cleanup prompt

Precise background extraction fix. Keep all six sprite objects, their positions, their colors, their shapes and grid exactly unchanged. REMOVE ALL colored glow, haze, blur, shadows, atmosphere and backdrop outside the actual solid sprite silhouettes. Absolutely clean tight cutout alpha around each leaf, flower, stone, rail and cliff. Every gutter and empty area must be alpha=0. No drop shadows, no outer glow. Preserve holes between railings as transparent. This is a production game sprite sheet requiring hard clean transparent cutouts.

## Contract

- Shore uses the boolean union's exterior contours, including disconnected parcels and L notches. Rock strips are clipped to the existing cliff; grass tufts are inset and clipped to the owned surface. Geometry is unchanged.
- Front/side planters follow the modular shell's existing presentation coordinates; corner windows use their own front anchor. Ground doors/lighting are unobscured. Selected parts and accepted centers remain authoritative.
- Organic pixel edges are material detail inside the real parcel boundary. A rounded static concept island would misrepresent that boundary and is not substituted.
- Host evidence: `docs/reviews/city-garden-visual/`. Synthetic account fixtures are not live account screenshots.
