# City V1 ambient actors — 13 November 2026

Reference: `../city-view.png`. Existing city composition and fixed camera remain the visual basis.

The user explicitly approved `actors-proposal.png` in this chat: “Bəli, bu qrafikanı tətbiq et”. It adds one resident and one car with four isometric directions; it does not replace city/building/decor art. The white proposal background is not production transparency.

Built-in `image_gen` generated the proposal and then extracted its background. Production: `android-billing/app/src/main/assets/city/life-actors-v1.{png,json}`. The production sheet has genuine alpha 0–255; JSON contains eight independent alpha bounds. No fallback API/CLI or scripted raster cleanup was used. Direction IDs: resident/car × se/sw/ne/nw.

Exact generation prompt:

> Use case: stylized-concept. Asset type: proposed sprite sheet for Wobbrik city ambient life, NOT a replacement city screen. Reference: the previously viewed city-view.png, match its charming polished hand-painted 3D isometric mobile game art, soft rounded forms, warm light. Create ONE sprite sheet on true transparent background with generous clear margins: 2 rows x 4 equal cells. Top row: the SAME tiny ordinary resident (teal jacket, dark trousers, brown hair), four diagonal walking directions southeast, southwest, northeast, northwest, full body, isometric elevated fixed camera. Bottom row: the SAME tiny compact rounded ivory and teal city car, same four diagonal directions. Exactly eight isolated sprites, no environment, no road, no text, no labels, no border, no interface, no extra objects, no external glow, no ground shadow beyond a tiny tight contact shadow. All residents same scale and all cars same scale. Each subject centered in its cell, facing along isometric ground axes. These are a visual proposal for explicit user approval; keep it simple enough to read at tiny city scale but detailed painted artwork, not schematic icons.

Exact extraction prompt:

> Use case: background-extraction. Edit target: the eight-sprite sheet just generated (four views of the teal-jacket resident and four of the ivory/teal car). Preserve every subject exactly, same 2x4 layout and size, same poses, same painted colors. REMOVE ONLY THE WHITE BACKGROUND, ground shadows and pale exterior haze. Output genuine transparent alpha: all empty spaces and all corner pixels must have alpha 0, no white matte and no checkerboard painted into image. Isolated clean cutout edges. Do not redesign any sprite, do not add any objects or labels.

Production uses directional static walking poses with subtle vertical motion, rather than a skeletal or multi-frame gait. Cars move along safe straight portions of placed roads; L-road arms are separate short turnaround routes. There is no simulated traffic network or individual resident economy.
