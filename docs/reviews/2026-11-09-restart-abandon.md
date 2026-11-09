# Review: restart/abandon policy implementation — 9 November 2026

Branch `feat/restart-abandon` from main `551d346`. Server counterpart: AppBackend
`feat/wobbrik-attempt-abandon` (`c1b229a`, from AppBackend main `0e39282`). Nothing merged, pushed or deployed.

## Policy (user decision, 9 November 2026)
Leaving an open attempt mid-game (Home, Back, closing the app) deletes it: no result, no building,
no later "Cəhdə qayıt". Saves only on (1) a loss — the last accepted successful state before the loss —
and (2) the player's own finish (cosmetic selection, explicit confirm, finish). No refunds,
no finish-as-abandon; offline practice restart stays local.

## Investigation → why the server changed
- `finish` always writes a result and, at >=10 floors, a building → using it for leaving is
  finish-as-abandon (forbidden).
- Leaving the attempt `active` blocks every later `start` (`wobbrik_one_active_attempt`,
  `attempt_conflict`) and keeps "Cəhdə qayıt".
- No route could close an attempt without a result. So: new `POST /game/attempts/abandon`
  (`status='abandoned'`, migration 017; no result/building/wallet/progression write; checkpoint
  rewards stay). Details: AppBackend `docs/wobbrik-game-api.md` "Attempt abandon".

## Client changes
- core: `Route.ABANDON`, `GameplayWire.abandon` + strict validate, `AttemptResult.Abandoned`
  receipt, transport allowlist `attempts/abandon`. Journal persists the route by name.
- engine: `AcceptedAttemptCommands.abandon` (active, non-terminal, exact accepted sequence).
  `AcceptedEngineHost`: `playing` (process scoped), `enter`, `leave`, `discardOpen`,
  `enforceLeave`, `leavePending`. Abandon targets the exact account+attempt left; sent once per
  Ready, outside the monitor; skipped (and cleared) for a loss, the finish picker, a finished/absent
  attempt, another account, or after re-entry. A pending checkpoint drains first (coordinator).
- app (`OnlineGame`): leaving the game page by any route calls `host.leave`; the frame loop calls
  `enforceLeave`. A "game" page restored after process death goes home. Home shows "Cəhdə qayıt"
  only for a loss (finish picker); otherwise "Oyna", which abandons a still-open attempt from a
  closed app and then starts a new one.

## Interpretations to confirm
- Android background (Home button / app switcher) without process death is not "leaving": the
  attempt is paused as before. If the process dies or is swiped away, it is treated as left.
- App launch alone abandons nothing: the open attempt may be in play on another device. It is
  abandoned when this device presses Play (the server allows only one active attempt anyway).
- No confirm dialog before "Menyuya qayıt"/Back deletes an open attempt (would need a design).
- Checkpoint coins/XP/achievements already credited before leaving are kept (no clawback invented).

## Verification
- Backend: vet/build, `go test -race ./...` OK; disposable PostgreSQL integration 69 pass, 1
  pre-existing DailyPlate failure (also fails on unchanged main).
- Android: `./gradlew ci` on this branch (local-ci gates on main only): BUILD SUCCESSFUL,
  core 70 + engine 62 tests, 0 failures, lint passed. The existing every-route lost-response
  test now also covers ABANDON. New tests: 7 host/command tests in `AcceptedAttemptCommandsTest`
  (leave → abandon without refund and start freed; loss kept and finishable; zero-floor local miss
  kept; leave during in-flight checkpoint; loss landing in flight not abandoned; closed app not
  resumed, abandoned only on Play; no abandon for another account or after re-entry), wire/receipt
  tests in core.
- No renders: no new screen or layout. The only visible change is the Home primary label
  ("Oyna" instead of "Cəhdə qayıt" for an open attempt), an existing label/state.
