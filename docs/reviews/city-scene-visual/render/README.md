# Fixed city building layer host renders

One-off native Compose/Robolectric API34 evidence. Same cached dependencies and offline Android jars as `../../city-purchases-visual/render/README.md`; no emulator, production pin changes or CI integration.

From `android-billing` with Java21:

```sh
./gradlew --offline -I ../docs/reviews/city-scene-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*CityRenderTest' --tests '*CityBuildingProjectionTest'
```

6 host tests: overview/focus at 360×640 font1/2 and 390×844 font1; empty, error, Home city navigation. 9 PNGs use explicitly synthetic geometry and ownership fixture, never a shipped demo adapter. Viewport/crop source pixels are production code. Contact and comparison sheets are QA composites, not app renders. Full design acceptance, terrain/decor/liveness, real auth/backend runtime, actual insets and TalkBack are not certified here. The fixture output path is workspace-absolute; adjust on another host.

Terrain/decor continuation: `CityTerrainRenderTest` adds 9 tests for production terrain union/materials, decor catalog/road turns, empty terrain, portrait font scaling and focus/return. Its screenshots live in `../../city-terrain-visual/`. Run with the same init script and `--tests '*CityTerrainRenderTest' --tests '*CityTerrainGeometryTest'`; details/limitations: `../../2026-11-13-phase4-city-terrain-decor.md`.

City chrome continuation (`8b4a6a4` base): current harness writes under
`../../city-chrome-visual/{city-scene-visual,city-terrain-visual}/` to preserve older evidence.
`CityRenderTest` now has 9 tests, adding 320×568dp at font2, loading and navigation/view callbacks.
It measures actual inventory label line bounds against its laid-out width (rather than
`hasVisualOverflow`, which flags the wider no-wrap paragraph even for fitting text).
Run the same command with both `--tests '*CityRenderTest' --tests '*CityTerrainRenderTest'`
for 18 host tests. Review: `../../2026-11-13-phase4-city-chrome.md`.

V1 liveness continuation: `CityLifeRenderTest` adds 8 native tests (four portrait/font
cases, empty scene, 24 animation frames, background/resume and platform motion off/on).
`CityLifeRoutesTest` adds 8 safety tests. Run the same init script with
`--tests '*CityLifeRenderTest' --tests '*CityRenderTest' --tests '*CityTerrainRenderTest'
--tests '*CityLifeRoutesTest' --tests '*CityTerrainGeometryTest' --tests '*CityBuildingProjectionTest'`.
43 tests total. Existing city fixtures use an explicit frozen life clock and write under
`city-liveness-visual/regression/` so prior accepted evidence is preserved.
Review: `../../2026-11-13-phase4-city-liveness.md`; native sequence and GIF are synthetic
scene evidence, not a live account or device capture.

City parity continuation (`7b7b676` base): captures now write under
`../../city-parity-visual/`. `CityParityRenderTest` adds three low-rise synthetic
account cases (390/font1, 360/font1, 320/font2) with five owned buildings, all
quarter turns, mixed coin/reward art, and focus/return for each building.
Add `--tests '*CityParityRenderTest'` to the prior host command; the complete
city set is now 47 tests including six projection/footprint tests.
See `../../2026-11-13-phase4-city-design-parity.md` for remaining design gaps;
these renders do not certify full concept parity or real account/device flow.

City garden continuation: captures now write under `../../city-garden-visual/`,
preserving previous parity evidence. The same production fixtures exercise the
new union shoreline, inset grass/flower tufts and modular facade planters.
`CityTerrainRenderTest` adds two shoreline tests: no union-internal seams, and
no bridge across disconnected parcels (empty shore also checked). Full set:
49 tests. Native segmentation/tuft counts are deliberately not a contract;
perimeter length, boundary location and land containment are checked instead.
Review continuation: `../../2026-11-13-phase4-city-design-parity.md`.

Approved balcony continuation: captures now write under `../../city-balcony-visual/`.
The same49test set covers the new front/side balcony source, corner-window/ground
exceptions, compact/font2/focus and tall-portrait grouped composition. Comparison
is a QA composite; synthetic fixtures remain outside production data adapters.
Review: `../../2026-11-13-phase4-city-design-parity.md`, final balcony section.
