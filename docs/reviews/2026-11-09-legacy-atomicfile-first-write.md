# Legacy AtomicFile first-write fix — 9 November 2026

Branch: `fix/legacy-atomicfile-first-write` from `test/atomicfile-crash-matrix` (`43d3ad1`), so it includes the crash matrix and the read-back fix. User instruction: “duzelt” for the API 24–29 gap in `2026-11-09-atomicfile-crash-matrix.md`.

Commits:

- `2c7b034` core `DurableJournalFile` + real-filesystem test
- `645511d` Android adapter switched to it
- `012088d` crash matrix runs the production code

No UI, backend, emulator or merge.

## Problem

On API 24–29, `android.util.AtomicFile` rewrites the base file in place. A first write per account has no `.bak`, and if the platform's base→`.bak` rename fails there is no backup on any write. A crash during that window left an empty or partial base. The journal then reported it unreadable and blocked the player on the incompatible-journal screen, even though the command was never sent.

## Design choice

- New `DurableJournalFile` in `core`, a pure JVM class using only `java.io`, because API 24–25 have no `java.nio.file`. It uses the same protocol on every API level:
  1. Write `<key>.journal.new`.
  2. `fd.sync()`.
  3. `File.renameTo` over the base (POSIX atomic replace). A `false` result throws.
  4. fsync the directory.
- The base file therefore only ever holds a complete, synced write, so a crash leaves exactly the old or the new state.
- This approach was chosen over `androidx.core.util.AtomicFile`, which copies the legacy `.bak` scheme on older releases and adds a dependency, and over keeping the platform class, whose API 30+ rename failures are only logged.
- File primitives sit behind `JournalFileSystem`. The Android module supplies `IoJournalFileSystem` with an `android.system.Os` directory fsync. `EINVAL` (directory fsync unsupported) is tolerated; other errors throw.
- `core` has no Android dependency and the dependency direction is unchanged (`android` → `core`), so there is no cycle.
- The crash matrix now runs the real production class, not a copy. `IoJournalFileSystem` is also exercised on the host filesystem.
- The read-back check in `DiskAttemptJournal.write` stays.
- A write failure after the atomic rename (directory fsync) throws, while the new state may already be visible. The caller then does not submit. Later recovery replays the same envelope with the same operation ID, so server dedup applies. That is safe.

## Backward compatibility

- Same file name (`<sha256(uid)>.journal`), directory (`noBackupFilesDir/game-attempts`) and byte format. Existing committed journals read unchanged.
- Leftovers from the old adapter are resolved on read and before every write, with the same meaning `AtomicFile.openRead` gave them:
  - `.bak` is the committed state and is renamed back over the base.
  - `.new` was never committed and is deleted.
  - If restore or delete fails, an error is thrown and the journal is reported unreadable with the bytes preserved. It is never treated as empty.
- The upgrade matrix checks this: after a crash at every step of the old API 24–29 and API 30+ adapters (258 scenarios), the new adapter reads exactly what the old adapter would have read, then continues normally.
- Residual case: 8 scenarios where the old API 24–29 adapter had already written an empty or partial base before the upgrade. Those bytes cannot be told apart from corruption, so they stay unreadable (preserved, nothing sent), as before. This only affects a device that crashed under the old build; the app has not been released.

## Checks

- `AtomicFileCrashMatrixTest`: 5 tests, 0 failures.
  - Durable matrix: 250 scenarios (3 cases × every crash step × 5 loss modes, including power loss that reverts un-fsynced directory changes). Unreadable outcomes: 0. Completed writes survive every loss mode.
  - Upgrade matrix: 258 scenarios.
  - Faults: disk full, file fsync failure, refused rename, rename reporting success without effect (caught by read-back), directory fsync failure. Also covered: stale `.bak`/`.new`, permission-denied read, and an unrestorable `.bak`.
  - Every scenario also checks exact envelope equality, stable repeated recovery, no leftovers, Bob's isolation, and that normal use continues.
- `DurableJournalFileTest` (real host filesystem): 2 tests, 0 failures.
- Mutation sanity: removing the directory fsync, writing the base in place, or skipping `.bak` restore each made the matrix fail. The production file was restored and checked byte-for-byte.
- Full `:core:test --rerun`: 61 tests and `:engine:test --rerun`: 55 tests, all with 0 failures/errors/skips. `:app:assembleDebug` PASS. `:android:lintDebug`: 0 errors, the same 3 existing warnings, none in the changed file. `git diff --check` PASS. Full diff from `43d3ad1` reviewed. No branch CI.

## Not proven

- This is JVM simulation, not device proof. Not covered:
  - real ext4/f2fs rename and fsync ordering, flash or controller behaviour, OEM kernels
  - whether `Os.fsync` on a directory is supported or returns `EINVAL` on real devices
  - SELinux behaviour
  - real process kill, power cut, or an APK upgrade over old-layout files on API 24–29 and 30+ devices or emulators
- The Android `syncDirectory` code compiles and passes lint, but it does not run in any test.
- Not covered either: the billing journal, and multiple processes, which are unsupported by design.
- Retained 5582/attempt and shared 5554 were not touched. No AppBackend, UI, merge, push or deploy.
