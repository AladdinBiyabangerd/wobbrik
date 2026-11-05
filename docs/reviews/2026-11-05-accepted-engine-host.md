# Accepted engine host — 5 November 2026

- Branch: `feat/accepted-engine-host`.
- Implementation commit: `976376c`; merge: `dbbaf99`.
- Reviewed diff: application host, recovery synchronous observer, application/activity
  lifecycle wiring, focused owner/host tests, and lifecycle contract documentation.
- Review: engine is private; no input or restart/abandon command can escape the gate.
  Every recovery transition resets the old engine before decoding; auth epochs use
  the existing serialized owner guard. Callback runs before StateFlow publication.
- Finding corrected during implementation: ProcessLifecycleOwner stop is delayed;
  MainActivity.onPause now pauses the application host immediately as well.
- Backend compatibility: inspected AppBackend attempts.go/finish.go; no command,
  API/DTO, schema, reward, wallet or policy changes required for this read-only host.
- Branch checks: scoped source/diff review and `git diff --check` passed; no branch CI.
- Main CI: post-merge `./scripts/local-ci.sh` passed (106 tasks: 32 executed,
  74 up-to-date), including core/engine tests, app assemble and lint. Four new
  focused tests passed. Existing LocalLifecycleOwner deprecation warning remains.
- Documentation follow-up: reviewed only CI/context status updates; whitespace clean.
- Limits: frame clock remains empty and simulation stays paused while durable
  start/checkpoint/finish is absent. Visible scene is still offline practice.
  No visual changes, live origin/device/auth/process-death checks, remote push or deploy.
