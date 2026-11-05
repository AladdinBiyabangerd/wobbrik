# Weighted scene motion — 5 November 2026

Branch: `fix/weighted-game-motion`, base `d6854c0`.
Reviewed scope: PracticeEngine/EngineCheckpoint and their tests, GameScene,
SceneMotion, PracticeGame frame lifecycle, debug fixture and engine contract notes.
Reviewed commit `40f2bf5` (diff `d6854c0..40f2bf5`); merged preserving history
as `229f058`. No branch CI was run. Main `./scripts/local-ci.sh` passed in 36s:
106 actionable tasks, 11 executed / 95 up-to-date. Engine tests executed;
app assemble/lint and core/Android gates passed (some up-to-date).

## Request and implementation

User approved replacing mechanical movement with the most lifelike motion
possible while retaining approved art. New attempts slow at the ends of a smooth
pendulum trajectory instead of reversing constant horizontal speed. Actual engine
x is used for the visible load and its drop; no substituted aiming position.
The center suspension follows a fixed-radius circular arc. Four cable endpoints
rotate with the original sprite. Load angle has a damped inertial response and
retains angular velocity on release. The connected tower rotates about its base
in response to off-center impacts; height lowers its response frequency. The
camera eases after landings instead of jumping one floor at a time.

Scene framing now reserves 520 vertical engine units (previously 405), with a
higher stationary hook and extra horizontal rotation margin. This is necessary
to fit the real arc across the full engine travel; art, palette, typography,
HUD and navigation are unchanged. These framing differences are within the
user-approved motion work, not a claim of exact prior static-board parity.

## Local review findings and fixes

- Do not change replay of accepted legacy snapshots: retain inner v1 rules on
  restore and encode; new attempts/restart use v2. Unknown rules fail closed.
- Cable length must not change when the camera settles: suspension is anchored
  directly to the fixed hook, and fall projection meets the landing plane.
- Per-floor lateral shifts look like sliding: rotate the connected stack, guide
  and terminal roof together. Keep conservative culling and viewport clipping.
- Pause must freeze presentation springs as well as engine; restart clears them.
  Terminal springs may finish settling, then stop requesting frames. Settling
  checks include displacement, avoiding an early stop at a turning point.
- Engine tests cover v1 replay, v2 reversal/replay, unsupported rules and bounded
  stalls. Timing-dependent miss fixtures were moved to an actual v2 miss position.

## Visual and build checks

- `ui-craft` read; existing GameTokens located before changes. No palette change,
  so no new contrast pairs were introduced (previous contrast evidence retained).
- Branch `:app:assembleDebug` passed. First invocation failed because ANDROID_HOME
  was missing; corrected environment and rebuilt successfully. Not counted as a
  passing first attempt. Existing LocalLifecycleOwner deprecation remains.
- Pixel emulator `emulator-5556`: phone 1080x2400/420dpi; tablet landscape
  1920x1200/240dpi. Both: frozen impact samples at 0/15/27/40/65/100 ticks,
  six initial floors, drift 3, release x=75; forty-floor swing fixture at tick 30.
- `weighted-motion/phone-sequence.jpg`, `tablet-sequence.jpg`, both tall captures
  inspected. Load/ropes remain connected; stack remains connected to viewport
  edge at forty floors; no detached offscreen foundation. Approved phone concept
  was inspected; static art/layout/type mismatches from the earlier assets review
  remain open. Phone/tablet concept comparison boards were inspected side by side:
  runtime scene/garden are smaller, suspension longer, background/type/lighting
  differ. The required longer suspension/framing is part of this motion change;
  the other pre-existing concept differences remain unresolved. Reference fixture
  includes 3 units of floor drift, whereas the approved stack is straight.
- Live animated debug fixture recorded as `phone-motion.mp4`; sampled frames in
  `live-contact.jpg` inspected for turning, angle response and connected geometry.
  This recording does not isolate a complete release/contact event; deterministic
  impact samples provide that evidence. `phone-motion.gif` is a short excerpt.
- Initial pause capture preceded the rendered panel and was rejected. Subsequent
  paused captures one second apart are pixel-identical excluding system bars;
  see `pause-check.txt`. Emulator size/density overrides restored.
- Final source diff reviewed locally; `git diff --check` passed. No remaining
  source-review finding for this unit; main regression gate passed.

## Backend and limitations

Read AppBackend `docs/wobbrik-billing.md`, `docs/wobbrik-game-api.md`, and
`internal/wobbrik/attempts.go`: snapshot JSON is opaque, <=16 KiB. Outer
wobbrik-v1/structural-v1, DTOs, sequence/revision/dedup, UID/login-epoch isolation,
retry journal, wallet, schema and migrations are unchanged. No AppBackend change
required. Inner rule IDs and older-client fail-closed behavior are documented in
engine/README.md. No backend test run is claimed.

This is stylized 2D motion, not a validated rigid-body/structural solver. Tower
motion remains cosmetic; release has no horizontal ballistic drift. Visual spring
state resets on process recreation. No physical-device feel/performance test,
new phone landscape/tablet portrait/large-font validation or online playable
accepted-attempt integration is claimed. Existing broader product/design debt
remains open. No push, deployment, new SKU or Console changes.


Validation documentation follow-up is docs-only. Stationary phone crane-body ROI
is pixel-identical at all five post-release samples; hook/cables excluded. See
`weighted-motion/crane-pixel-check.txt`. Final APK reinstalled after the terminal
settling-displacement guard; active/pause rendering is unaffected by that guard.
No physical-device performance or subjective realism claim is inferred from CI.
Docs-only follow-up diff reviewed locally; whitespace passed; no findings.
Main CI is repeated after its merge and the exact result is reported in chat.
