# Fixed city building layer host renders

One-off native Compose/Robolectric API34 evidence. Same cached dependencies and offline Android jars as `../../city-purchases-visual/render/README.md`; no emulator, production pin changes or CI integration.

From `android-billing` with Java21:

```sh
./gradlew --offline -I ../docs/reviews/city-scene-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*CityRenderTest' --tests '*CityBuildingProjectionTest'
```

6 host tests: overview/focus at 360×640 font1/2 and 390×844 font1; empty, error, Home city navigation. 9 PNGs use explicitly synthetic geometry and ownership fixture, never a shipped demo adapter. Viewport/crop source pixels are production code. Contact and comparison sheets are QA composites, not app renders. Full design acceptance, terrain/decor/liveness, real auth/backend runtime, actual insets and TalkBack are not certified here. The fixture output path is workspace-absolute; adjust on another host.

Terrain/decor continuation: `CityTerrainRenderTest` adds 9 tests for production terrain union/materials, decor catalog/road turns, empty terrain, portrait font scaling and focus/return. Its screenshots live in `../../city-terrain-visual/`. Run with the same init script and `--tests '*CityTerrainRenderTest' --tests '*CityTerrainGeometryTest'`; details/limitations: `../../2026-11-13-phase4-city-terrain-decor.md`.
