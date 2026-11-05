# Continuous tower and stationary crane — 5 November 2026

Branch: `fix/continuous-tower-stationary-crane`, base `62228ac`.
Reviewed diff: GameScene, scene Canvas modifier, debug fixture/capture parameter,
design behavior clarification and context. Commit/merge/CI recorded after merge.

## Findings and fixes

1. The reported 12-floor image has a detached foundation and omitted lower
   section. Earlier suffix culling could omit visible floors. The renderer now
   checks each floor's projected sprite bounds instead of selecting a suffix.
   A first regression render exposed the additional cause: Canvas did not clip
   to its measured scene viewport. The foundation could paint into the bottom
   reserved/inset area even when connecting floors were culled there. Added
   `clipToBounds()` after scene padding so culling and actual painting share
   exactly the same viewport. Tall towers continue through the viewport edge;
   the offscreen foundation does not leak back into view.
2. The entire crane sprite and horizontal camera previously followed `craneX`.
   Removed horizontal tracking. Crane body and upper suspension point now use
   fixed window coordinates, independent of module x and floor count. Only the
   cables' lower endpoints and suspended floor follow horizontal engine position.
   Framing reserves full travel plus floor width so endpoint sprites remain visible.

No bitmap repainting, palette changes, new assets, engine/collision/checkpoint
rules or input/command changes. Existing token palette retained. Per-floor scan
is linear in state size, but only visible sprites issue drawing calls.

## Verification

- Branch `:app:assembleDebug` passed; no branch CI.
- Frozen DEBUG fixture: phone 1080×2400 / 420dpi at 1, 12 and 40 floors.
  Twelve-floor fixture uses 3 units of drift per floor to reproduce staggered
  construction, with module x=-120 and +120. Tablet 1920×1200 / 240dpi checked
  at both endpoints with the same 12-floor drift fixture.
- Final images: `docs/reviews/stationary-crane/`. All must be inspected after the
  clipping correction; initial screenshots that still showed the leak were rejected.
- All six final images inspected after the clipping change. One-floor image
  shows connected foundation/first module; 12/40-floor towers remain continuous
  to the scene boundary, with no foundation leaking below it.
- Crane-body pixel ROI is identical at left/right endpoints on phone and tablet;
  phone ROI also identical at 1/12/40 floors. Exact coordinates/pixel counts in
  `pixel-check.txt`; paired screenshots show that the floor/ropes do move.
- Capture tooling now waits for an exact debug-only semantic fixture marker.
  The old PNG-size heuristic admitted a stale Android task snapshot (count 4 in
  a requested count-1 capture); it was rejected and the full matrix regenerated.
  The marker identifies count, x, drift, pause and terminal flags, not just activity.
- Capture script syntax parsed with Python AST; branch rendering build passed.
- Whitespace check and scoped source review; no remaining findings for these fixes.
- Post-merge main CI: pending.

The body and upper attachment are stationary; the current practice renderer does
not introduce a new pendulum simulation or constant-length rope physics. The
engine's horizontal trajectory and drop/collision rules remain unchanged.

Backend assessment: rendering only; AppBackend domain/API/DTO/schema/wallet,
revision/dedup and accepted recovery are unchanged. No backend change required.
Offline practice and broader concept/type/lighting differences remain open. These
fixture screenshots do not prove online play or physical-device performance.
No remote push or deploy. The separately running Pixel_6_34 emulator was left
untouched; capture commands target this task's Pixel_6 (emulator-5554).
