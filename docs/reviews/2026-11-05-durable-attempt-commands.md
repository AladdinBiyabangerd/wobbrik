# Durable attempt commands — 5 November 2026

- Branch: `feat/durable-attempt-commands`.
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
  passed. No branch CI executed. Main CI pending post-merge.
- Test scope: real serialized/fsynced atomic JVM journal plus deterministic server
  contract fake; not a real PostgreSQL/network/device verification. Tests cover all
  route response losses and owner recreation, delayed commit account switch, stale
  Ready, duplicate dispatch, post-receipt sync failure, save failure, revision
  conflict, terminal zero-floor finish and checkpoint structural safety.
- Limits: no automatic durable multi-command finish intent, no online frame/input
  loop or UI binding, continued stabilization and restart/abandon remain unresolved.
  No visual changes or live auth/origin/process-death checks; no push/deploy.
