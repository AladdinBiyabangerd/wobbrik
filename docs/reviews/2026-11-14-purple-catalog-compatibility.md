# Purple template — all catalog parts and mixed selections, 14 November 2026

Branch `fix/purple-seams-ground-contact`, HEAD/base `2be0583`, uncommitted working changes. Scope is this continuation's renderer changes against saved pre-turn source, preserving inherited dirty files. The user's latest request treats the last annex/roof-wall join as the approved template; older pending-acceptance statements in the annex review are historical for this task. Sources: PLAN §7, `2026-11-14-purple-annex-join.md`, approved ground-floor study and the last roof-join production captures.

## Findings and corrections

- Upper-storey alternate wall material was an inset rectangle, exposing purple strips between the selected covering and pilasters/cornices. It now reaches the existing inner pilaster edges and the shared storey boundary, with the same offset-dependent facade projection. No hull, storey joint or ground anchor moved.
- Alternate main roofs used the older template's shallow front eave. Front and side are now independently projected to the approved three shell anchors (16/68, 240/128, 354/63). Teal uses its existing modular asset with its measured eave points. Catalog roof IDs and their main-roof silhouettes/ornaments remain independent; known neighbour-sprite contamination crops remain excluded.
- Annex roofs were the entire catalog roof sprite squeezed onto a quadrilateral, including transparent margins, undersides and ornaments. Measured opaque covering samples from each selected roof now fill the exact approved annex plane. Teal covering comes from the same existing teal modular asset as its main roof. During close inspection, vent/crown ornaments were found inside two material samples; those samples were tightened before the final rerender. The annex volume, rear flashing, front eave, window, stone courses and ground/shadow/foliage anchors are unchanged. No generated/replacement art or new palette.
- Alternate entrances included a baked right return and stairs/supports flattened onto the facade. Frontal source crops end at each entrance's front threshold and meet the template's existing independent landing/stair layer. Original catalog entrance styles (including portico and balustrade styles) remain; no substitute door is invented for an open portico.
- Template routing previously depended on any remaining `base_1` ID: changing the last such part sent a mixed building back to the old body. Mixed selections now keep the approved template even without `base_1`; complete matching `base_2`–`base_6` sets retain their original renderer. This deliberately changes the old routing rule in PLAN §7 in accordance with the user's current preserve-template request.
- All window and lighting IDs were inspected with the other categories. Their source/projection was retained, including split `coins_window_2` panes. No newly observed protrusion/gap required a window or lighting redesign.

Production files: `PurpleBuildingArt.kt`, `PurpleGroundFloorArt.kt`, `PurpleSelectedRoofArt.kt`, `PurpleSelectedEntranceArt.kt`. Existing assets, game geometry, metadata counts, ownership and gameplay are unchanged. No domain/API/DTO/schema/wallet/ledger change; AppBackend work is unnecessary.

## Coverage and evidence

`purple-catalog-visual/after/manifest.txt` maps every specimen's sheet/slot to its exact five selected IDs. `catalog-00`–`catalog-26.png`: 105 production `BuildingPreview` specimens = 68 single-category selections, 16 mixtures retaining a base1 part, 16 mixtures without any base1 part, and 5 complete-style controls. Synthetic owned-building state has 10 real floors and accepted nonzero offsets; the production representative-floor renderer is called unchanged. Harness labels are diagnostic only and can clip; the manifest is authoritative for IDs.

All 68 catalog IDs, grouped by category:

- **wall (13):** `base_1_wall`, `base_2_wall`, `base_3_wall`, `base_4_wall`, `base_5_wall`, `base_6_wall`, `coins_wall_1`, `coins_wall_2`, `coins_wall_3`, `reward_wall_1`, `reward_wall_2`, `reward_wall_3`, `paid_wall_1`.
- **window (14):** `base_1_window`, `base_2_window`, `base_3_window`, `base_4_window`, `base_5_window`, `base_6_window`, `coins_window_1`, `coins_window_2`, `coins_window_3`, `reward_window_1`, `reward_window_2`, `reward_window_3`, `paid_window_1`, `precision_frame`.
- **entrance (14):** `base_1_entrance`, `base_2_entrance`, `base_3_entrance`, `base_4_entrance`, `base_5_entrance`, `base_6_entrance`, `coins_entrance_1`, `coins_entrance_2`, `coins_entrance_3`, `reward_entrance_1`, `reward_entrance_2`, `reward_entrance_3`, `paid_entrance_1`, `balance_entry`.
- **roof (14):** `base_1_roof`, `base_2_roof`, `base_3_roof`, `base_4_roof`, `base_5_roof`, `base_6_roof`, `coins_roof_1`, `coins_roof_2`, `coins_roof_3`, `reward_roof_1`, `reward_roof_2`, `reward_roof_3`, `paid_roof_1`, `summit_crown`.
- **lighting (13):** `base_1_lighting`, `base_2_lighting`, `base_3_lighting`, `base_4_lighting`, `base_5_lighting`, `base_6_lighting`, `coins_lighting_1`, `coins_lighting_2`, `coins_lighting_3`, `reward_lighting_1`, `reward_lighting_2`, `reward_lighting_3`, `paid_lighting_1`.

The test reads shipped `finish/catalog-map.json`, checks 68 unique IDs, asserts mixed/uniform routing and the presence of each production building Canvas with no missing-art fallback. The same Compose state is updated between selections, exercising replacement of all five layers. `BuildingPreview` combines the actual catalog and game-window art; this is not a duplicate drawing implementation. The full production inventory/detail host additionally covers category changes, mixed selection, 40 real floors and complete base2 at 360×640/font1, 390×844/font1 and 320×568/font2. Boundary sheets cover 1/4/40 floors and straight/offset forms; 1/4 are renderer-only stress cases, not valid owned inventory fixtures.

Inspected final category boards, main roof context, ground closeups and mixed/no-base1 boards, plus compact font2 detail and boundary captures. `before/board-*.png` retains initial comparison boards; original full pre-edit sheets except sheet00 were archived to `/private/tmp/purple-catalog-before-full`. `roof-source-grid.png` and `entrance-source-grid.png` document existing source-art inspection. `after/ground-*.png` magnifies the ground/annex joins. `render/contact.swift`, `ground.swift`, `parity.swift` regenerate the supporting evidence from native captures; the production host is `mobile-sizing-visual/render/PurpleCatalogRenderTest.kt`.

## Focused checks

After the final source edit:

- Native render suite **6/6 PASS, 43s**: catalog1, inventory3, boundary2; 27 catalog sheets/105 specimens, 30 full inventory/detail captures and 2 boundary sheets. The last two material-sample corrections were rerendered and inspected in the final ground closeups.
- Ordinary app unit **24/24 PASS**, `assembleDebug` and `lintDebug` **PASS, 17s**. This is focused validation, not `local-ci.sh`.
- Exact pixel checks **3/3 PASS**: approved base1 catalog specimen against this turn's pre-edit capture; complete inventory detail1/base1 and detail9/base2 against the preceding roof-join production captures. This includes the base1 shaft, joints, right annex, flashing, footing and garden unchanged.
- GameTokens contrast **4/4 PASS**, minimum10.48 (Text/Background, Muted/Background, Accent/Background, Text/Surface). Palette/text tokens were not modified.
- Scoped source diff review and `git diff --check` **PASS**. Confirmed sampled coordinates remain within existing sprite cells; only the selection projection/routing changes described above are added to inherited production changes. No additional issue remains from this scoped review.

Machine-readable summaries: `purple-catalog-visual/native-results.txt`, `parity.txt`, `focused-checks.txt`.

## Limits

This is native Robolectric/API34 production Compose rendering with synthetic state, not a live account/server flow or device/GPU certification. No emulator was opened. Device insets, TalkBack, all locales, tablet/landscape and API24 runtime were not checked. Mobile portrait only; tablet work remains deferred. Exhaustive Cartesian selection coverage (463,736 combinations), all 68 IDs at every screen/font/floor/offset boundary, and real ownership mutation/retry E2E were not run; 32 explicit mixed selections were rendered. No pixel-perfect parity with every design-study pixel is claimed. Exact parity is required only for the unchanged approved baseline and stated controls.

Existing font2 detail title clipping and horizontal/inner scroll behavior remain visible and outside this building-art unit. Native render success is not full phase/game release acceptance. No commit, merge, push, deployment or CI was run.

## Reproduce

From `android-billing/`, Java21:

```sh
./gradlew --offline -I ../docs/reviews/mobile-sizing-visual/render/render.init.gradle \
  -Dwobbrik.render.output='/Users/mac/My Workspace/Wobbrik/docs/reviews/purple-catalog-visual/after' \
  :app:testDebugUnitTest --tests '*PurpleCatalogRenderTest' \
  --tests '*PurpleInventoryRenderTest' --tests '*PurpleModuleBoundaryRenderTest'
./gradlew --offline :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

The opt-in native-host init script and cache prerequisites are documented in `mobile-sizing-visual/README.md`; normal project dependencies/pins and CI are unchanged.

## Commit / merge / push authorization

The user subsequently requested commit, merge and push. The reviewed renderer unit includes its preceding uncommitted template/ground-floor dependencies, approved design assets, opt-in render harness and review notes. Existing branch ancestry has four commits beyond local main and will be retained by a non-fast-forward merge. Unrelated release output and the unused whole-building bitmap remain outside the commit. CI was not requested and is not run. The final commit/merge/remote identities are reported in the chat and Git history.

Implementation commit: `d63029f`. Staged source diff passed local review and whitespace checks; production sources are identical to the final 6/6 native, 24/24 unit, assemble/lint validation. Subsequent documentation records authorization/status only.
