# Game scene asset integration — 5 November 2026

Branch: `feat/game-scene-assets`, based on `66ce11a`.
Reviewed scope: MainActivity, GameTokens, new GameScene renderer, debug-only preview,
PNG resources/provenance, capture recipe and progress/context documentation.
Commit and post-merge CI result are recorded after validation below.

## Changes and review findings

- Four replacement sprites were explicitly approved in chat before integration.
  Source/runtime PNGs are byte-identical; alpha is preserved. A matching landscape
  cloud follows the approved `game-tap-v2/tablet-landscape.png` backdrop.
- Replaced schematic Canvas tower, ground, crane and pitched roof with approved
  sprites. Settled/hanging floors share the same sprite and dimensions. Active and
  paused buildings remain roofless. No network, command or engine rules changed.
- Ring was initially obscured by the suspended floor: corrected hook anchor,
  crane ratio and four cable endpoints after inspecting the phone render.
- Initial landing guide was one floor too high: aligned it with the current
  visible roof plane. Initial fixed floor culling left a tall tower floating:
  replaced it with actual viewport bounds including projected sprite depth.
- Wide-window scene initially started below the HUD and looked too small against
  the landscape reference: share the upper scene with corner HUD in wide windows.
- Preserved the pause input barrier and three actions. Pause touch target is
  circular, 48dp; panel remains bounded, inset and scrollable in short windows.
  Added gradient depth, fine outlines, bold labels and decorative title chevrons
  using the existing palette. Hairline and dim treatment are named in GameTokens.
- Debug fixtures are frozen local states, excluded from release source sets.
  Production MainActivity still uses the real offline PracticeEngine. The record
  is session-local practice data, with that meaning retained in accessibility.

## Visual comparison and limitations

References: `docs/design/game-tap-v2/{phone,tablet-portrait,tablet-landscape}.png`.
Final captures and comparison boards: `docs/reviews/game-scene-assets/`.
The comparison uses user-approved replacement artwork, not pixels extracted from
the concept boards. Runtime framing accommodates actual crane travel and tower
height; this is not a pixel-identical reproduction of the generative concept.
Native type sizes/rasterization, background lighting, garden/scene proportions,
cable shading, guide glow, and panel/button lighting differ from the painted concept. These differences remain visible in the boards;
do not treat a successful build as proof of exact visual parity.

Window matrix: phone 1080×2400 at 420dpi; tablet portrait 1200×1920 and landscape
1920×1200 at 240dpi; phone landscape 1600×720 at 280dpi; short window 1600×800 at
320dpi with font scale 1.5. Also inspect 40 floors / crane at the travel limit and
FINISHED. These are emulator window configurations, not physical-device tests.

The first emulator terminated during a larger resize. It was restarted without
snapshots using software graphics. A tablet active capture showed only the system
splash and was rejected; capture now retries small splash images and still
requires human visual inspection. Only final accepted captures count as evidence.

## Checks

- Branch rendering build `:app:assembleDebug` passed; no CI on branch.
- `git diff --check`; Python capture recipe syntax; source/runtime PNG hashes.
- Contrast: muted/surface 8.22, text/raised 10.24, on-accent/accent 11.45,
  text/background 16.57; all >=4.5. Exact report in `contrast.txt`.
- Final phone/tablet comparison boards and all matrix screenshots inspected.
  The 40-floor culling correction now continues the tower below the viewport.
  Short-window/font 1.5 pause actions remain readable and reachable.
- Real MainActivity (not fixture): system back opens pause; outside tap retains
  pause; resume tap then back leaves count 0; restart + scene tap lands count 1.
  Screenshots and input-check.txt record this. Initial XML assertion expected HUD
  text behind the modal, which is excluded from the accessibility tree; that
  assertion was rejected and counts were checked on the actual screenshots.
- Post-merge main CI: pending below.

Backend assessment: visual-only diff; AppBackend domain/API/DTO/schema, shared
wallet, revision/dedup, auth/recovery and commands are unchanged. No backend
change or backend CI is required for this unit. This does not complete online
integration (3.4), real auth/menu/result flows or physical-device performance.
No remote push, deploy, SKU or Console changes.
