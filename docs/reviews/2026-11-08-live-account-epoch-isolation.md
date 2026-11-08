# Account / login epoch isolation with real HTTP — 8 November 2026

Branch: `docs/live-account-epoch-isolation`, based on `812ec28`. User requested next check after explicitly skipping Google preparation/dependent nonfree purchase test. Guest shop policy stays unchanged. No production/backend/UI changes; PLAN §3.4 remains open.

## Executed scope

Used retained independent account-test backend/DB/TLS on 18084/55449/18447 (verified archived backend 79bfc12). TLS readiness PASS before run. Refreshed two existing anonymous Firebase identities through the normal Firebase refresh endpoint; no new accounts, auth bypass, Console changes or credentials printed/committed.

Private ignored JVM scenario injects an additional test source directory using a task-only Gradle init script. Executes real production `AccountSessions`, `GameplayHttpBackend`, `UrlConnectionGameplayTransport`, `AttemptCoordinator`, and `DiskAttemptJournal`. Task-only JVM truststore trusts the existing local certificate; no production TLS changes. A transport wrapper waits AFTER a real HTTPS 200 response but BEFORE returning it to production response handling. This gives a deterministic response/application race; it does not claim an actual server delay, Android auth event, Firebase SDK sign-out/re-login or Android UI acceptance. Host auth callbacks are explicitly injected.

1. Seeded only a temporary test journal with the already-committed A start operation ID/revision/payload. Envelope is reconstructed from saved structured API evidence; byte identity with the original Python request is not claimed. First real receipt replay is held; host callback changes to B; releasing response produces AccountChanged, ready=false/no accepted snapshot. A pending command remains. B recovery sync returns B revision 2/empty active attempt and does not submit/remove A command. Returning to A establishes a new local login epoch; coordinator sends the identical temporary-journal envelope a second time, syncs A revision 1/own active attempt, then removes the command. No new gameplay command, checkpoint, floor or grant.
2. Held a real A sync response; invoked same-UID host auth callback to create a new epoch. Old response is rejected with AccountChanged, ready=false/no accepted snapshot. New-epoch recovery obtains correct A revision/attempt through another real sync.

Temporary journal file adapter uses local atomic rename, not Android AtomicFile or crash-durability acceptance. Temporary journal directories removed afterward; live device journal never seeded or changed. Server receipts are existing exact dedup operations.

## Results and preservation

Focused command from android-billing: `./gradlew -I ../output/account-unlock-live/epoch.init.gradle :core:test --tests appbackend.wobbrik.game.LiveAccountEpochIsolationTest` — PASS (11s build, two tests, zero failures/errors/skips). Private init/test code is not part of committed product/test suite and requires local sessions/runtime to rerun. Report retained privately before routine Gradle runs can replace it.

Before/after repeatable-read snapshots across 22 domain/wallet tables are exactly equal in BOTH account-test DB and retained checkpoint DB. Active retained attempt `f73722ea-b54f-4985-9c7e-85a442c1d0a3` remains sequence/floors 2. Retained 5582 journal payload equals prior final capture. Read-only journal capture only; no device input, install, force-stop, sign-out, reverse change or retained backend mutation. AppBackend owner working tree clean/unmodified.

Private ignored evidence under output/account-unlock-live: `epoch-src/LiveAccountEpochIsolationTest.kt`, `epoch.init.gradle`, `epoch-truststore.p12`, `epoch-test-report.xml`, `epoch-{receipt,sync}-verification.json`, `epoch-{before,after}.json`, `retained-epoch-{before,after}.json`, `retained-epoch-journal.tar`. Identities file remains private; never print or commit tokens. No credentials in journal or test source.

Local review: checked production session guards, gate placement after real network response, command persistence/removal order, exact envelope equality for both scenario sends, report counts and all-table equality. No backend API/schema change required. Documentation whitespace checked before commit. No branch CI/full suite/merge/push/deploy or visual acceptance.

Remaining: actual Android Firebase lifecycle/account UI, live APK upgrade/unsupported journal UI, Room/outbox and full stage/physical/visual acceptance. Google-dependent positive nonfree purchase remains USER-SKIPPED, not PASS; do not reopen it without user instruction. Next bounded selection: disposable APK upgrade / unsupported-version recovery acceptance, with retained app data untouched. Read current handoff, historical-journal review and PLAN §3.4 first; establish exclusive separate disposable device data before any install/journal fixture. Do not start Room/outbox implementation implicitly.
