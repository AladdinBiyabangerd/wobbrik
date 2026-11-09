# Initial empty snapshot recovery

Branch: `fix/initial-empty-snapshot-restore`, based on `a40c6fe` documentation handoff history. Reviewed final implementation/test diff and lifecycle contract locally.

## Fix and compatibility
Backend migration 003 stores initial snapshot as non-null `{}`. Android previously required null at sequence zero. Restore now accepts null or an empty JSON object only when sequence and all three counters are zero. Positive-sequence decoding, version/geometry guards, continuation rejection and account/epoch ownership are unchanged. No mutation, journal reset, reward grant or new attempt is introduced. Backend API/schema/rules changes are unnecessary; this aligns with its existing persisted representation.

Regression parses server-shaped JSON through RecoveryDtos into AcceptedAttemptRestore and verifies the same attempt ID, paused initial state and null-snapshot equivalence. Additional cases reject nonzero counters, malformed/nonempty/array/JSON-null snapshots, unsupported version and continued state.

## Checks and live evidence
- Focused AcceptedAttemptRestoreTest and local debug assembleDebug PASS (9s).
- Engine tests (52) and core recovery-filtered tests (13) PASS (3s), zero failures/errors.
- APK installed with `adb install -r`, preserving app data; existing ignored localhost TLS overlay and backend origin reused.
- Viewed emulator screenshot: Wobbrik home shows “Cəhdə qayıt”, coins 0, level 1, XP 0/50; previous unsupported-restore message absent.
- Real PostgreSQL read confirms existing attempt `c77c47db-5e08-410a-bf39-26680343d979` remains active, sequence/floors/precise/rescues all zero, snapshot `{}`. No attempt reset/deletion performed.
- Next input/screenshot showed DailyPlate instead of Wobbrik, indicating competing emulator control. Device interaction stopped; asked user to reserve emulator. No landing/checkpoint/finish, exact retry, process-death or reward-dedup PASS claimed.
- No visual implementation changed; screenshot is functional evidence, not design acceptance.
- No main merge, push, deploy or branch CI. Separate user OK required for merge/deploy; local CI reserved for main after approved merge.

## Review
Final `git diff --check` PASS.
No remaining finding in this bounded restore fix. Stage-three live acceptance remains open; physical phone is not attached. Emulator control must be exclusive before continuing device interactions.
