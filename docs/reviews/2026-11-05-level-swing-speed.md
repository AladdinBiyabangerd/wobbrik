# Level-dependent swing speed — 5 November 2026

Branch: `feat/level-swing-speed`, base `536afda`.
Reviewed implementation: `f56e0f6` (`536afda..f56e0f6`).

## Request and review

User requested that suspension swing faster as level increases. Here level means
upcoming floor (landed count + 1), matching the current in-attempt game loop.
Previously swing reused balance difficulty: first five floors unchanged, then
+2.5% per floor to a maximum 2x. New attempts use an independent swing curve:
base * (1 + min(landed count, 25) * 0.08). Thus floor 1 is 1x, floor 11 is 1.8x,
and floor 26 onward is capped at 3x. Speed only changes after a successful landing.
Suspension length/range, fixed crane, camera follow, gravity, overlap, balance and
scoring are unchanged; speed does not also multiply stability difficulty.

- Inner rule bumped to `practice-speed-v4` to avoid altering checkpoint replay.
  Restored v1/v2/v3 retain their exact original trajectory and speed curve.
- Review finding: changing the latest rule ID must not disable the v3 camera or
  reapply its old absolute x bound. Shared followsTower predicate retains both v3
  and v4 world/local coordinate behavior in engine and checkpoint validation.
- Four regression cases cover increasing measured engine phase speed, cap and
  invalid input; real landing/pause/restart; high-speed range and exact replay
  through turns/drop; and old v1/v2/v3 speed/replay plus shifted v3 camera origin.
- Local source/contract/test review and whitespace passed; no open code finding.

## Checks and visuals

- `ui-craft` used; existing GameTokens inspected. No palette/type/spacing change,
  so no new contrast pair; original contrast evidence remains applicable.
- Branch assembleDebug passed in 27s: 66 tasks, 8 executed / 58 up-to-date.
  No CI/tests were run on the branch; regressions ran after the main merge.
- Installed debug APK on emulator-5556. Phone 1080x2400/420dpi and tablet landscape
  1920x1200/240dpi: levels 1/11/26 after the same 30 ticks (0.5s) from x=0.
  Captures and inspection sheets in `level-swing-speed/` were viewed. The faster
  swing travels farther in the same interval; cables connect, crane stays fixed,
  and highest-level payload remains visible. These are deterministic frame samples,
  not a physical-device feel/performance measurement.
- Two first tablet frames omitted cable strokes and were rejected after inspection.
  Relaunch plus extra wait produced complete frames, which were recaptured/viewed;
  the capture recipe now includes extra settling time. No source fix is inferred
  from this transient capture behavior. Size/density overrides reset afterwards.
- Compared framing/art against the approved game-tap-v2 phone/tablet concept panels
  used in the preceding camera review. Only requested movement timing changes;
  existing long-suspension/framing exception and background/type/lighting/scene-size
  differences remain. These level fixtures are not a claim of concept parity.
- Main `10c611f` after history-preserving merge: `./scripts/local-ci.sh` passed
  in 32s, 106 tasks, 11 executed / 95 up-to-date. All 43 engine tests passed
  (zero failures/errors); engine tests executed, app build/lint and core/Android
  gates passed. MainActivity launched successfully after installation.
- Documentation-only validation follow-up reviewed against 10c611f; whitespace
  passed, no findings. Main local CI repeated after its merge; result in chat.

## Backend and limits

AppBackend attempts.go still accepts opaque valid JSON <=16 KiB under the same
outer structural-v1 envelope; no DTO/schema/migration/backend code change needed.
Inner v4 rule is documented in engine/README.md; older clients fail closed on an
unknown rule. Server counters, UID/epoch isolation, revision/sequence/dedup, retry
and wallet remain unchanged. No backend test run is claimed. The playable flow
remains offline practice; this does not unlock accepted online input, real account
progression or rewards. No new landscape-phone/portrait-tablet/font-scale rendering,
physical-device test, remote push, deploy, SKU or Console work.
