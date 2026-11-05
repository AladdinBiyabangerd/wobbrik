# Horizontal tower camera — 5 November 2026

Branch: `fix/horizontal-camera-follow`, base `a72c8f8`.
Reviewed implementation: `7daa3f7` (`a72c8f8..7daa3f7`).

## Scope and review

User explicitly chose a camera that follows the tower horizontally while the
crane stays fixed on screen. They rejected shortening suspension or swing range.
Only this camera/aiming unit is implemented; broader design parity is deferred.

- New attempts use `practice-camera-v3`. Camera/crane world origin is the last
  landed floor's center; local pendulum x/range/period and suspension are retained.
  Island, stack, guide and falling floor use the same world-to-screen translation.
  Crane, HUD and backdrop stay fixed. Horizontal recentering is atomic on landing;
  vertical camera remains smoothed. This is not continuous horizontal camera easing.
- Review finding: translating only the view would make a fixed-screen crane aim
  at a different world point than the engine evaluates. Fixed by storing the drop
  at origin + local swing x and evaluating overlap/balance in world coordinates.
- Review finding: world floors can now exceed +/-120. v3 validation retains finite
  coordinates, foundation/adjacent overlap, correct heights, local swing bounds,
  exact falling-origin relation and the existing 16 KiB encoded snapshot limit.
- v1/v2 snapshots preserve their fixed origin and original trajectory on restore
  and encode; restart creates v3. Unknown inner rules still fail closed. JSON keys
  and outer structural-v1/rules contracts remain unchanged. Older clients cannot
  replay new v3 snapshots, rather than interpreting world x as local x.
- Added mirrored left/right tests beyond the old world boundary, a centered drop
  on shifted support, miss and balance-loss paths, shifted-fall encode/restore,
  pause, restart and v2 compatibility. Historical BalanceTest scenarios explicitly
  keep v2 aiming; v3 loss/recovery has separate coverage.
- Source diff and whitespace reviewed locally. No unresolved source finding.

## Checks and visual evidence

- `ui-craft` applied; GameTokens inspected. No color/type/spacing/palette changes,
  so existing contrast evidence remains applicable; no new pairs introduced.
- Branch `:app:assembleDebug` passed (34s, 66 tasks; 8 executed). Existing
  LocalLifecycleOwner deprecation remains. No CI/test gate was run on the branch.
- Installed APK on emulator-5556. Captured and inspected phone 1080x2400/420dpi
  and tablet landscape 1920x1200/240dpi: 12 floors with drift -8/0/+8, pause,
  and 40 floors with left drift and swing at +120. Device settings restored.
- Inspection sheets and individual frames in `horizontal-camera/`: top landing
  plane stays centered in both directions; tower remains connected through the
  viewport boundary at 40 floors; pause controls remain visible and reachable.
  The crane ROI is pixel-identical across left/center/right for both windows;
  exact bounds and result in `crane-pixel-check.txt`.
- Compared phone/tablet against approved concept panels side by side. User-approved
  exceptions here are horizontal following and the previously accepted longer
  suspension/framing. Existing art/background/type/lighting and scene/garden-size
  differences remain open. The comparison intentionally uses 12-floor camera
  fixtures versus the concept's four-floor scene; it is not a parity measurement.
- QA tooling: system Python lacks Pillow; first board generation failed, then a
  typo failed parsing. Boards were successfully regenerated using `uv --with pillow`
  and inspected. Neither failed attempt is a passing check.
- Main local CI: pending merge; tests are not yet claimed as passed.

## Backend and limits

Inspected AppBackend `internal/wobbrik/attempts.go` and `docs/wobbrik-game-api.md`:
opaque valid JSON <=16 KiB plus server counters remains the contract. No backend
code/DTO/schema/migration change needed; no backend test run claimed. Wallet,
revision/sequence/dedup, UID/login epoch, retries and accepted recovery gates are
unchanged. The visible game is still offline practice; online host remains locked.
This does not add mass-distribution physics or an animated collapse. No physical
phone/tablet check, new phone landscape/tablet portrait/large-font render, remote
push, deploy, SKU or Console change.
