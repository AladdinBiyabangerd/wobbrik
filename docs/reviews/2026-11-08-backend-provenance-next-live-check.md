# Running backend provenance and next live check — 8 November 2026

- Branch: `docs/backend-provenance-next-live-check`, based on Wobbrik `e59570290716780a967b84748b1157bd498b3a7b`. Documentation-only unit. Existing modified handoff and untracked ten-floor review preserved. No merge, deploy, push or CI.
- Read-only provenance checks: PID 42589, started 8 November 17:08:32 Asia/Baku, executes `/Users/mac/My Workspace/Wobbrik/output/stage-three-live/server`; cwd is Wobbrik; TCP listener is 18080. Existing certificate-verified TLS route `https://localhost:18443/readyz` returns `{"status":"ready"}`.
- Binary SHA-256 remains `ef8bbab51b01369ff823e52c6d5b326c41dffcef54e9bf0a7306700d7e0edefa`. Go embedded metadata: `appbackend/cmd/server`, Go 1.27.0, darwin/arm64, CGO enabled, revision `25a19676495d1fee13c9655ee2422e702722c939`, revision time `2026-11-08T12:53:12Z`, `vcs.modified=true`. Build ID: `RMFlgrQg09LGXJix4JzA/xUK9hfZECsgtGn05shp6/a_tvSfNDkArdxDd3nv1V/NHtOzwz9Q9HaND_s0Lnx`.
- Confirmed scope: process/executable identity and embedded base revision. Exact source tree NOT confirmed: dirty metadata does not enumerate changes or untracked inputs. No archived build-source manifest is provided by the handoff. Do not relabel this binary as clean `25a1967`, guest-auth `79bfc12`, or current AppBackend HEAD.
- Current AppBackend observation: clean `feat/dailyplate-food-search`, HEAD `c8e7d7b`; parent/base `25a1967`. Its commit changes server wiring as well as DailyPlate files. This is another task's work, not evidence of which dirty inputs were built earlier. Local Go is now 1.27.3, different from the binary's 1.27.0. Rebuilding the current checkout would neither prove the old source nor preserve the current process if substituted.
- No backend files, branches, index, processes, routes, DB or device state changed. Only scoped read-only Git/process/binary/readiness inspection. No backend contract change needed to select the existing checkpoint scenario.

## Selected remaining PLAN §3.4 scenario — not executed

Checkpoint committed, response lost, process death, exact journal replay and reconciliation. Finish lost-response replay and ten-floor building/parts already have bounded live proof; do not repeat them as a substitute for checkpoint coverage.

Preconditions: establish verified source in an isolated source/build directory using an explicit immutable commit and recorded inputs/toolchain/hash. Keep the existing owner checkout and running process untouched; arrange a separate approved test route/DB for execution or coordinate a later process replacement explicitly. A newly verified build proves its own source, not the provenance of historical runs. Reserve emulator exclusively before any input; inspect current account/attempt/journal without reset. Latest observed attempt was finished at ten floors; do not blindly resume it or create a replacement during preflight.

Execution criteria for the later bounded live unit:
1. Capture account revision, balance/XP, accepted attempt sequence/floors, operation/ledger counts and current pending-journal bytes. If no active attempt exists, starting a fresh real attempt belongs to that live unit, not this read-only check.
2. Use actual resume/drop to produce a settled checkpoint. Forward its real request to the backend; suppress only the successful checkpoint response after commit. The existing temporary fault proxy targets finish only and must not be assumed to cover checkpoint.
3. Confirm the DB accepted the checkpoint exactly once while the client remains waiting/error with gameplay input blocked. Save pending envelope and request-body hash without credentials.
4. Force-stop/relaunch with app data retained. Pending checkpoint must replay the same operation ID, expected revision, sequence and payload before new commands; capture request/journal equality and successful reconciliation.
5. Compare post-commit/pre-retry and post-retry DB snapshots: no additional revision, floor, wallet/XP, receipt or ledger grant caused by replay. Resume only from the accepted checkpoint after Ready. Restore temporary fault routing and verify readiness.

Not covered: old-version journals, nonfree unlock/account isolation, Google linking, Room/outbox, physical-device/full visual or stage acceptance. No live PASS claimed here.

## Local review/checks

Reviewed the documentation delta for unsupported source/live claims and scope violations; `git diff --check` passed after edits. No build/test suite or local CI: no implementation changed and CI is main-only. Exact old dirty-source provenance remains unresolved; recovering its build-time snapshot/manifest or a reproducible matching artifact is required to close it.

## Authorized continuation — isolated build prepared

User requested committing the documentation and proceeding, allowing another emulator for this project. Initial documentation/evidence commit: `b3bb82a`.

Prepared an isolated source archive of AppBackend `25a19676495d1fee13c9655ee2422e702722c939`, deliberately excluding the unrelated DailyPlate commit and owner working tree. Archive SHA-256: `286f45e8469cc510cd6742243acacd2dd51bbb6338c8f83eda9b74ac8056feac`. Ignored directory: `output/checkpoint-live-isolated/`; manifest records revision, archive hash, toolchain, build command and binary hash. Build command from `source/`: `go build -trimpath -buildvcs=false -o ../server ./cmd/server`, Go 1.27.3 darwin/arm64, CGO enabled. Revision is linked through the archive/manifest, not embedded Git metadata. Two successive builds passed and produced identical SHA-256 `7f400514baa2dc89d7167b68c8461a45e356add9c8fd19035ebbbfb0cf1f8278`. This verifies repeat-build equality in the current environment, not cross-machine reproducibility or the source of the historical dirty binary.

Started `Pixel_6_34` on `emulator-5582` with `-read-only -no-snapshot-save -no-boot-anim -no-audio`; boot completed (`sys.boot_completed=1`) and AVD name verified. Emulator process is retained in execution session 27647. This is a separate disposable overlay; no input/install/data reset or reverse route configured. Existing `Pixel_6` on emulator-5554 untouched.

Original backend PID 42589 still runs unchanged and existing TLS readyz passes. AppBackend owner checkout remains clean at `c8e7d7b`; no backend source/branch/index/process replacement or shared DB mutation. No isolated backend process/DB/TLS route started yet. No live checkpoint scenario or test suite executed; no merge/push/deploy/CI.

Next bounded unit: recheck emulator-5582 ownership and build hash, create a separate disposable PostgreSQL database and backend listener/TLS fault route using this archived source and existing local Firebase project configuration without exposing credentials. Install the existing final APK only on emulator-5582, retaining any existing app data; inspect identity/journal before gameplay. Execute the selected real checkpoint post-commit response-loss/restart/exact-replay scenario. Never reroute emulator-5554 or replace PID 42589. Any absent active attempt on the isolated account is established through actual app flow, not synthetic commands or DB grants.

Documentation delta reviewed and `git diff --check` passed. No visual implementation/render acceptance claimed. Context handoff required before beginning the larger isolated runtime/live unit.
