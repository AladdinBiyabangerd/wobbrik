# Purple modular art study v2 — 14 November 2026

Status: art study explicitly approved by the user (“okey”); modular implementation in local review. Canonical plan: `docs/PLAN.md` §7. Authoritative art: `../join-reference.png`.

`art-study.png` is a built-in imagegen study using the approved join reference and existing production atlas as inputs. Left: intended connected assembly; right: proposed separable art. It preserves purple masonry, cream trim, warm windows, roof/dormer/chimney, staggered balconies and grounded garden.

Inspected visually. The left illustration has a more continuous architectural volume and integrated foundation than the current repeated-box render. This is generated concept art, NOT a render assembled from the pictured modules and NOT a validated atlas. Right-hand window/door studies still require exact facade projection; shell seams, crop bounds, attachment anchors and alternate-part compatibility must be measured and verified in Compose before claiming the perspective/join defects fixed.

Production integration now uses `atlas-v2.png`, a transparent 1254×1254 derivative generated from the approved study, copied to `assets/building-purple/atlas.png`. Its measured crops are in `atlas.json`. Existing owner edits were retained. No whole-building bitmap is used.

Existing selection contract stays unchanged: base_1_wall→shell/ground; base_1_window→arch; base_1_roof→roof; base_1_entrance→entrance; base_1_lighting→lamp. Alternate existing IDs retain their selection and mixed mapping; square is not a new item. No wallet, ownership, server geometry, real floor count, city or gameplay change. No backend change needed for this art-only scope.

`PurpleBuildingArt.kt` rectifies measured front/right shell quadrilaterals, excluding the open top and redundant top cornice; the bottom joint owns the shared seam. Each upper storey bottom follows the lower storey top offset so the faces meet. Window/door/lamp decals share the same facade coordinate function. Three corresponding eave points align the roof. `foundation-v2.png` supplies independent empty garden and stairs/pots sprites, copied to production `foundation.png`; `foundation_cells` records their crops. The stair landing has the same front-edge slope as the door threshold. The entrance is an independent door decal. No rectangular foreground garden mask is redrawn. The opaque study board is not shipped as an atlas.

This remains a modular interpretation: six representative floors, repeated trim and generic balcony placement differ from the bespoke approved four-storey assembly. Existing alternate catalog sprites retain their original art quality. Do not claim pixel parity or full catalog conversion.

Validation and remaining limits: `docs/reviews/2026-11-14-purple-modular-v2.md`. No emulator/CI/commit/merge/push.
