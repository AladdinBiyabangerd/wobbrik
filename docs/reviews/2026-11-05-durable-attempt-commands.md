# Durable attempt commands — 5 November 2026

- Branch: `feat/durable-attempt-commands`.
- Implementation commit: `d748830`; reviewed merge: `9b38e31`.
- Reviewed diff: typed command adapter, application bootstrap binding, shared owner
  dispatch, coordinator session capture, focused integration tests and documentation.
- Findings/fixes: explicitly pin queued coordinator work to its originating session;
  exact Ready identity prevents an equal cross-account/epoch snapshot authorizing
  work. Finish rejects nonterminal/unaccepted progress; checkpoint rejects rewriting
  accepted floors/counters and terminal/unchanged/zero/falling updates.
- Authority/retry: existing atomic journal precedes network; no second coordinator,
  wallet, reward delta, optimistic result or automatic conflict rebase. Process-owned
  work continues on activity pause; input and physics remain locked/paused.
- Backend inspection: AppBackend attempts.go, finish.go, catalog-v1.json and relevant
  wobbrik-game-api.md contract. Existing domain rules/DTO/schema support these exact
  operations; no backend changes or separate backend branch/checks required.
- Local review: scoped source diff including new adapter/tests and whitespace check
  passed. No branch CI executed. Post-merge main `./scripts/local-ci.sh` passed:
  106 tasks, 32 executed, 74 up-to-date; core/engine tests, app assemble/lint passed.
  Ten command integration tests and six coordinator tests report zero failures/errors
  (11 newly added tests across these suites).
- Test scope: real serialized/fsynced atomic JVM journal plus deterministic server
  contract fake; not a real PostgreSQL/network/device verification. Tests cover all
  route response losses and owner recreation, delayed commit account switch, stale
  Ready, duplicate dispatch, post-receipt sync failure, save failure, revision
  conflict, terminal zero-floor finish and checkpoint structural safety.
- Validation-note follow-up reviewed as documentation only; whitespace check passed.
- Limits: no automatic durable multi-command finish intent, no online frame/input
  loop or UI binding, continued stabilization and restart/abandon remain unresolved.
  No visual changes or live auth/origin/process-death checks; no push/deploy.
