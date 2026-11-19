# Game aiming, clearance and fixed horizontal view — 19 November 2026

User authorized all three fixes on the existing branch, preserving other work.
Started on `feat/home-collection-native`; workspace switched externally to
`chore/home-review-cleanup` (HEAD `9ae78ca`) during checks. This task did not switch
branches, reset, stash, merge or alter Home changes. The user subsequently authorized local review and a separate commit on the
current branch, preserving existing work.

## Reviewed scope and behavior

- `SceneMotion.kt`: horizontal view offset is always zero. Remove presentation
  rotation of the tower, which displaced the visible support from the engine's
  unrotated collision coordinates. Balance/topple scoring remains authoritative
  engine behavior; removing the cosmetic rotation does not disable losing.
- Vertical view follows `(floorCount - 4) * FLOOR_HEIGHT` from the initial state,
  including negative initial offset; no four-floor startup gap contraction or
  post-landing easing lag. The top plane stays at four projected pitches.
- `GameScene.kt`: reserve eight pitches of vertical framing and lift the crane
  by the additional four pitches. This retains the former initial clearance in
  scene units while leaving multiple tower floors visible. Height-limited windows
  use a smaller fitted scale, so absolute physical pixel size can differ.
- Crane origin remains the engine's last-support world x; the crane moves over
  that origin while the world/view does not pan. Cable arc uses displacement
  relative to the crane origin, not absolute world x. No horizontal snapping or
  post-release drift was added. The existing nonzero-overlap acceptance remains.
- The engine/checkpoint rules and server wire contract are unchanged. Checked
  `AppBackend/internal/wobbrik/accepted_geometry.go`: settled geometry binds
  unrotated centers and floor bases under `practice-speed-v4`. No backend code,
  API, DTO, migration or repository mutation was needed.

## Checks and local review

- Final `:engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`
  with the scoped offline render init script: PASS, 31 seconds. Engine 97 tests;
  app 55 tests, including two motion invariants and 11 production-canvas renders.
- Initial attempt was blocked by sandbox access to the Gradle lock. First build
  found a stale `hangingY` reference; fixed before the final successful run.
- Native Robolectric API34 production-canvas renders: empty, first, fourth,
  sixty floors, swing extreme, +/-40 support offsets, released/contact positions,
  320x568 and 390x844 portrait, default 360x640. Opened empty/small/offset/contact
  images and first/tall/swing/normal comparison. Constant top clearance, stationary
  world and visible-release/contact x were inspected against the supplied video.
  These are synthetic host fixtures, not a real account or device run.
- `SceneMotionTest` covers fixed x/rotation, zero-time restore, restart, tall and
  changing counts, and no delayed vertical settling. Existing engine tests retain
  drop/miss/balance/checkpoint replay coverage.
- Scoped contrast: Text/Muted/Accent/Danger on Background PASS, minimum 9.93:1.
  Broad automatic token pairing also reports unrelated menu combinations; these
  are not used or changed here and are not treated as a whole-app contrast pass.
- Local diff reviewed; `git diff --check` PASS. No additional data reads, network,
  allocations per floor, cache or persistence mechanism added. Existing O(n)
  floor traversal remains unchanged; no performance improvement is claimed.

## Limits

No emulator/live-device/ART/GPU timing check. Canvas-only fixtures do not verify
HUD fontScale2/inset/localized-text layout, unchanged by this patch. No tablet
acceptance or exact prior PNG parity claim; framing necessarily changes to keep
initial clearance. With fixed horizontal view, an accumulated world-space tower
that drifts far enough can leave the viewport; camera recentering is intentionally
absent as requested. Existing relative aiming rules remain unchanged. No CI,
merge, push, deployment or real SKU action. Render evidence and scoped harness:
`docs/reviews/game-camera-clearance/`.

## Final local review and commit — 19 November 2026

Reviewed the complete task diff against `9ae78ca`, including scene transforms,
crane-relative cable arc, world-space release/contact x, settled/restore behavior,
regression tests and the scoped native render harness. No additional blocking
finding was found. Existing render and 97 engine / 55 app test results remain
applicable: no production/test code changed after the final successful run.
`git diff --check` was repeated successfully. The documented fixed-view drift
limit remains; this review does not claim device validation or whole-app parity.

Only this task's two production files, motion regression tests, review/handoff,
contrast report and scoped render harness are included in the separate commit.
Ignored PNG render evidence remains local, following the repository convention.
No merge, CI, remote push or deploy was performed. The review is included in the
same commit as the reviewed changes; resolve its hash with `git log` for this file.
