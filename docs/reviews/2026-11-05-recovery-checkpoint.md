# Recovery DTO and engine checkpoint review

- Branch: feat/recovery-checkpoint; reviewed implementation commit: recorded by the merge parent.
- Scope: core recovery DTO/parser and typed attempt receipts; engine versioned JSON codec,
  hidden-state capture/restore and accepted-counter validation; regression tests/docs.
- Source checks: AppBackend internal/wobbrik/{read,attempts,finish,catalog,shop,part_rewards}.go
  and docs/wobbrik-game-api.md. Existing API suffices; no backend/schema/rules changes.
- Findings corrected: request floors must be positive and rescues <= floors; previous
  projection lost counters/continued/account recovery fields; timestamp parsing must
  work on Android 24 without java.time; receipt results now checked for the submitted route.
- Review: inspected scoped diff, strict scalar types, null attempt/snapshot, pinned versions,
  exact historical receipt handling, validate-before-mutation, floor/phase/numeric bounds,
  deterministic fractional-time and velocity preservation. git diff --check passed.
- Branch CI: deliberately not run. Main CI after merge 588b5f6 failed during Gradle script compilation: missing newline between dependencies.
  fix/checkpoint-gradle-dependencies separates declarations; scoped diff reviewed; no branch CI. Main rerun pending.
- Limits: no host/UI/auth/lifecycle binding or visual change; no render claim. No abandon,
  restart-as-finish or trusted geometry. Structural server does not verify physics.
  16 KiB snapshot limit rejects oversize state; no unbounded-height encoding. Android
  process-kill, live API and continuation stabilization not tested. Catalog decoder is
  supplied; catalog/attempt/operation read-route transport integration remains separate.
- Existing dirty Compose app/build/settings/rule/review files excluded and preserved.

## Verification result
- Implementation 588b5f6; reviewed fix d07cf5d; main merge 1d28bab.
- Main ./scripts/local-ci.sh exit 0: 47 tasks, 17 executed/30 up-to-date.
- XML results: core 35 + engine 13 tests; zero failures/errors/skips.
- Android assembleDebug and lintDebug passed. Existing uncommitted Compose application
  excluded, so this result does not validate that host. No backend changes/tests required.
- Verification/context documentation updated on docs/recovery-checkpoint-verification;
  diff reviewed, git diff --check passed; final main rerun follows its merge.
