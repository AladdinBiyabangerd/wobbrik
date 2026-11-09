# AtomicFile / DiskAttemptJournal crash matrix — 9 November 2026

Branch: `test/atomicfile-crash-matrix` from `main` `213c5db`. Commits: `77c7a99` (test matrix) and `4051e68` (fix plus its regressions). Scope: PLAN §3.4 open item “full AtomicFile crash matrix”. JVM tests only. No UI, no device, no backend.

## Method

`core/src/test/.../AtomicFileCrashMatrixTest.kt` runs the production `DiskAttemptJournal` over in-memory models of:

- `android.util.AtomicFile` for API 30+ (write `.new`, sync, rename into place; also restores a legacy `.bak`). Transcribed from AOSP `android11-release`.
- `android.util.AtomicFile` for API 24–29 (rename base→`.bak`, rewrite base, sync, delete `.bak`; reading restores `.bak`). Transcribed from AOSP `android10-release`.
- The `androidAttemptJournal` adapter (write, `fd.sync()`, finish/fail; a `FileNotFoundException` counts as empty only if neither base nor `.bak` exists).

Simulated faults:

- Process death at every filesystem step. Writes are split into two chunks, so partial files occur.
- Power loss where never-synced data is cut to 0 bytes or to half.
- Rename/delete failures that AtomicFile only logs.
- Disk-full partial writes.
- Sync failure.
- Permission-denied reads.
- Stale `.bak` and `.new` files.

Model assumption: namespace operations (rename/delete) are durable once they complete. File data is durable only after sync.

Assertions:

- After restart the journal holds exactly the old or the new command (data class and envelope string equality, including a ~9 KB Unicode checkpoint envelope), or a preserved unreadable file.
- Repeated recovery returns the same result and changes no files after the first normalising read.
- An unreadable file is never treated as empty or overwritten.
- Bob's journal is byte-exact and unaffected by Alice's crashes.
- After recovery, normal remove/save continues.

## Results

- `AtomicFileCrashMatrixTest`: 6 tests, 0 failures. The main matrix covers 258 scenarios: 2 API protocols × first save / save over tombstone / remove × every crash step × 3 loss modes.
- Full `:core:test --rerun`: 60 tests, 0 failures/errors/skips. `:engine:test --rerun`: 55 tests, 0 failures. `:app:assembleDebug` PASS. `git diff --check` PASS. No branch CI.

## Defect found and fixed

- AtomicFile does not always report a failed commit:
  - API 30+ `finishWrite` only logs a failed `.new`→base rename.
  - API 24–29 `finishWrite` ignores a failed `.bak` delete, and the next read rolls back to the backup.
- In both cases `DiskAttemptJournal.save/remove` returned normally while the committed state stayed old. Before the fix, `uncommittedWriteIsNeverReportedAsDurable` failed on `LEGACY_24_29/save over tombstone` and, run separately, on `MODERN_30/first save`.
- Consequence: `AttemptCoordinator.execute` could submit a command with no durable pending envelope, losing exact replay after a crash.
- Fix (`4051e68`, 2 lines in `DiskAttemptJournal.write`): after `replace`, read back and `check(read(uid) == command)`. An uncommitted write now throws `IllegalStateException`. The caller does not submit, and retry works.
- That error surfaces through the existing generic error path, not the incompatible-journal screen.
- The AOSP behaviour is real, but whether the read-back catches it on a device has not been checked on a device.

## Known limitations (not fixed; need a decision)

- API 24–29 (minSdk 24) first write per account has no backup. Process death between file creation and a completed write, or power loss before sync, leaves an empty or partial base file. That happened in 8 of the matrix scenarios. The same happens on any write if the platform's base→`.bak` rename fails (16 scenarios).
- In those cases the journal is reported unreadable. The bytes are preserved, nothing is sent, and gameplay stays blocked behind the incompatible-journal screen, even though the uncommitted command was never sent.
- A fix would mean replacing the platform AtomicFile on old APIs, for example a `.new`+rename adapter. That is an Android-module production change needing a decision and device testing, so it was not made here.
- API 30+ shows no such case in the matrix.

## Not proven

- JVM model evidence is not real device durability. Not covered: the real kernel/filesystem (ext4/f2fs) rename/fsync ordering, directory fsync, flash or controller behaviour, OEM AtomicFile changes, and the real `android.util.AtomicFile`/adapter. The adapter is mirrored in the test, so a drift between the mirror and the code would go unnoticed.
- Also not covered: the 140000-byte bounded read path; the billing journal; actual Android process kill, power cut or OS upgrade.
- No emulator, retained 5582/attempt, shared 5554, backend or Console was touched. No merge, push or deploy.
