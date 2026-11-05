# Attempt recovery core — 5 November 2026

- Branch: `feat/attempt-recovery-core`.
- Reviewed scope: new `core/.../game/AttemptCoordinator.kt`, its tests and README;
  continuation record. Existing uncommitted Compose host/build/rule/review files
  are preserved and excluded from this unit.
- Local source review: pending command is written before network I/O; uncertain
  outcomes retain the original command. Mutation acknowledgement alone does not
  unblock gameplay: sync must succeed. UID/login epoch is checked after every
  suspended request. Equal revision does not suppress wallet refresh. Definitive
  revision conflict refreshes and surfaces the error without rebasing/rewarding.
- Limitation found during review: interfaces alone do not provide durable process
  recovery or authenticated transport. These are explicitly documented as missing;
  tests recreate the coordinator against a retained in-memory journal, not a real
  Android process/disk. No complete host or server integration is claimed.
- AppBackend AGENTS/README/context/API inspected. Existing API provides the
  required sync/idempotent submission semantics; no backend change is needed for
  this core unit. Pause/restart/abandon still needs contract resolution; no new
  finish/reward semantics are invented.
- Visuals unchanged; no render or design-fidelity claim. Existing schematic host
  remains outstanding debt against approved mobile-v1/game-tap-v2 designs.
- Checks: source review completed; `git diff --check` passed. CI not run on the
  branch. Post-merge main CI result will be recorded after execution.
