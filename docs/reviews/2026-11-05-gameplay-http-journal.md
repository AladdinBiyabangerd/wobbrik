# Gameplay HTTP and attempt journal — 5 November 2026

- Branch: `feat/gameplay-http-journal`. Scope: GameplayWire, GameplayHttpBackend,
  DiskAttemptJournal, AndroidAttemptJournal, adapter tests, core dependency/README.
  Prior dirty Compose host/build/settings/rule/review files are excluded.
- Source review: typed builders bind metadata to envelopes; submit validates before
  I/O and sends the original body. Fresh token and UID/login epoch checks surround
  transport suspension. Only HTTP 409 + revision_conflict is mapped. All other
  failures leave the pending command recoverable. HTTPS origin only, redirects
  disabled, 8-second connect/read bounds, 64-KiB response bound, 32-KiB command bound.
- Journal: SHA-256 UID filenames plus embedded UID/version, bounded reads, synced
  AtomicFile writes, compare-before-remove tombstones; backup excluded through
  noBackupFilesDir. One application owner is required; concurrent processes or
  multiple journal instances writing the same directory are unsupported.
- Findings corrected: route/metadata mismatch is rejected; malformed persistence
  fails closed instead of silently opening a new attempt; tests accept any parser
  failure for a mismatched request route rather than assuming one exception class.
- AppBackend read.go/attempts.go/finish.go/transaction.go and gameplay API inspected.
  Existing endpoint/DTO/dedup contract suffices; no backend/schema/rules changes.
  Full account/catalog/result DTOs and host/engine codec remain separate work.
- Tests added: real JVM disk recreation, UID isolation, overwrite/compare guard,
  corruption, identical retry after lost response, error mapping, login epoch,
  typed checkpoint/finish construction and malformed numeric/metadata rejection.
  JVM atomic-file test storage is not Android AtomicFile instrumentation. No device
  process-kill or power-loss verification. No live Firebase/server request made.
- Visuals unchanged. No UI rendering or design-fidelity claim.
- Branch checks: source review and git diff --check passed. CI not run on branch.
  Main post-merge CI at `dfc10c5`: passed, exit 0; 47 tasks (21 executed,
  26 up-to-date). New GameplayAdaptersTest: 5 tests, zero failures/errors/skips.
  Existing dirty host files were stashed and excluded from this clean-tree gate.
