# Historical journal compatibility — 8 November 2026

Branch: test/historical-journal-compatibility. Test-only unit; no production/API/schema/UI changes, merge, push, deploy or CI.

## Evidence and review

Historical DiskAttemptJournal and GameplayWire sources copied from b6a4f9b into test-only historical package. Automated source comparison confirms only documented package/import/header changes. Historical code itself writes temporary-file journals; current journal/coordinator then reads/drains them. This is JVM regression evidence with a fake backend, not live APK upgrade acceptance.

Historical START, CHECKPOINT and FINISH retain exact operation ID, revision, route and envelope across reader recreation and replay. Bytes remain untouched through submit and sync; removal occurs only afterward; historical reader also recognizes final tombstone. Another UID sees no pending command.

Deliberately invalid storage/rules/engine versions (not claimed historical formats) fail recover with IllegalArgumentException, leave ready false, make zero backend calls, preserve original bytes and reject replacement save without overwriting. This proves core failure behavior, not end-user UI error handling. Historical checkpoint uses an opaque legacy snapshot; successful read/replay does not prove modern backend acceptance of that snapshot or new geometry requirements. Test file adapters intentionally use simple temporary file writes; Android AtomicFile crash safety is outside this unit.

Focused command: ./gradlew :core:test --tests appbackend.wobbrik.game.HistoricalJournalCompatibilityTest --tests appbackend.wobbrik.game.GameplayAdaptersTest — PASS (6s); two new test methods, zero failures/errors/skips. Source provenance assertion and diff whitespace PASS. Reviewed source placement, no production dependency, exact-envelope assertions and unsupported-version no-write/no-send coverage. No backend contract change needed.

## Environment observation

No device command/input, app install/restart, backend process control or live DB write in this unit. Final read-only eight-table snapshot is output/checkpoint-live-isolated/historical-journal-final.json. It differs from earlier final.json: same active attempt f73722ea-b54f-4985-9c7e-85a442c1d0a3 is now sequence/floors 2; operations 2→3, ledger 1→2. This is externally observed progress, not performed by these temporary-file tests. Preserve newest state; do not restore older snapshot/journal. Existing isolated runtime remains archived backend 79bfc12 despite separate guest branch update to current main.

Remaining: genuine APK upgrade/unsupported-state UI acceptance in separate disposable device data; account isolation/nonfree unlock live proof, linking, Room/outbox and full stage/visual/physical acceptance. Exact next step: scope account-isolation/nonfree-unlock preflight from PLAN §3.4, verify current runtime/device ownership and inspect newest journal read-only before any gameplay. Do not advance stage or replace active journal.
