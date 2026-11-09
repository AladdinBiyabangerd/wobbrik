# Room snapshot cache — local review (9 November 2026)

Branch `feat/room-cache` from main `d0ccbc3`. Scope: PLAN §3.4 / stage-three unit 3, decision B.

## What changed
- **core** `SnapshotCache` + `SnapshotCacheStore` port. Stores the exact validated `/sync` body
  (`GameSnapshot.wire`, set only by `RecoveryDtos.snapshot`, excluded from equality/copies) with
  rules version and revision, keyed by app + uid. Write rules: no wire body or foreign rules → ignored;
  lower revision → ignored; same revision + identical body → no rewrite; same revision + new body
  (billing balance) → replaced; newer revision → replaced. Reads re-parse with the wire parser; a
  foreign-rules row, revision mismatch or unparsable body is deleted and returns null.
- **core** `AuthenticatedRecoveryOwner(cache = …)`: writes only after it published an accepted
  Ready for the still-current epoch/session; exposes `cached: StateFlow<GameSnapshot?>` — cleared on
  every auth change, loaded for the new uid under the generation guard, null when signed out. Cache
  exceptions never change gameplay state. `ready()`, `submit`, `execute` and the journal never read it.
- **android** Room 2.7.2 + KSP 2.1.10-1.0.31: `cached_snapshot(app_id, uid, rules_version, revision,
  body)`, PK (app_id, uid), DAO read/upsert/delete, DB in `noBackupFilesDir/snapshot-cache.db`,
  `exportSchema = false`, destructive migration (cache only). Resolved stdlib stays 2.1.10, sqlite 2.5.1.
- **app** `AccountScreen(cached = …)`: value slots use `ready?.snapshot ?: cached`; `cached` is passed
  only when not Ready and the account is a session. Play, commands, restore and status use the live state.

## Display behaviour (rendered on emulator-5586, AccountPreviewActivity `cached` extra)
- Home while recovering/offline: coin chip shows the cached balance; the controls keep the existing
  status view (stats stay hidden behind status, as before).
- Account/achievements/collection while recovering/offline: coin chip, stats card and counts show the
  cached values with the existing status text, hero and "Yenilə" — the same composition as the existing
  Blocked-restore state. No new elements or copy.
- Phone landscape account page offline with cache: "Yenilə" is below the fold of the scrollable controls
  column (verified reachable by scroll). Needs user decision whether that is acceptable.

## Tests
- JVM `SnapshotCacheTest` (9): newer revision wins; same-revision balance accepted; app/uid isolation;
  rules mismatch dropped on write and read (and corrupt/revision-mismatched rows); non-wire snapshots
  never cached; cache never makes Ready/submit pass and journal bytes/writes stay untouched; only
  accepted published snapshots written; account switch/sign-out never show another account; failing
  store never blocks Ready. Mutations (`>=` on revision guard, not clearing cached on auth change) caught.
- Instrumented `CachedSnapshotDaoTest` (2) on emulator-5586 (API 34): upsert/read/delete isolation by
  app+uid; rows survive DB reopen and cache rules run over real Room.

## Not verified / open
- No API 24–29 instrumented run (only API 34 image available on 5586).
- androidx.test runner 1.6.2 / ext-junit 1.2.1 are new test-only pins (implied by the instrumented-test decision).
- No live backend run of the cache path (preview fixture + fakes only).
