# Stage 3 live validation handoff — 8 November 2026

## Completed
- Both placement feature histories merged with explicit user approval. Final recorded main tips: Wobbrik `ccd56f6`, AppBackend `25a1967`; both pushed to origin and verified 0 ahead / 0 behind.
- Original Wobbrik worktree fast-forwarded to `ccd56f6`; main is checked out separately in `/Users/mac/My Workspace/Wobbrik-rail-icons`.
- Debug APK `/Users/mac/Desktop/Wobbrik-ccd56f6-debug.apk` built from current code; assembleDebug and signature verification PASS. User installed/tested on phone and explicitly reported OK. This is user phone acceptance of this APK, not live online recovery proof.

## Current state
- User explicitly selected stage 3 live gameplay, retry and recovery after process death as the next task.
- APK has Firebase config but generated BACKEND_ORIGIN is empty. Live server/account validation has not been performed. No backend endpoint or deploy may be invented.
- Backend placement is on main; Android placement integration and stage 3/4 acceptance remain incomplete.
- This documentation-only branch is `docs/stage-three-live-validation-handoff`; no new main merge/push/deploy is included.

## Decisions
- Continue canonical PLAN stage 3.4; no city UI or new design work in this unit.
- AppBackend/PostgreSQL remains the authority. Preserve operation ID/revision/envelope on retry and account/epoch isolation.
- Current code has terminal checkpoint → automatic finish reconciliation (`AcceptedEngineHost.reconcileTerminal`, called after Ready by WobbrikApplication), with a fake recovery test. Older explicit-finish limitation notes are historical; live process-death proof remains open. Cosmetic linkage and Room/outbox remain open.

## Remaining work
- Establish available authorized backend HTTPS origin, Firebase/account configuration and connected Android device/emulator; inspect local config without exposing credentials.
- Validate real start→landing→checkpoint→accepted Ready→finish, lost response/exact retry, network interruption and process death/relaunch against real server state; verify no duplicate rewards and correct account isolation.
- Fix evidenced failures in a dedicated feature/fix branch, review locally and document evidence/limits. Main merge needs separate OK; CI only on main. No deployment/Console changes implicitly authorized.

## Relevant files
- docs/PLAN.md; docs/stage-three-online-plan.md; docs/host-lifecycle-contract.md; docs/kotlin-compose-plan.md.
- android-billing/README.md; app/build.gradle.kts; app/src/main/kotlin/com/aladdin/wobbrik/{WobbrikApplication,OnlineGame}.kt.
- AcceptedEngineHost, AcceptedAttemptCommands, AcceptedAttemptRestore and core recovery/coordinator/journal implementations and tests.
- AppBackend AGENTS.md, docs/context/current-task.md, docs/wobbrik-game-api.md and corresponding attempt handlers/tests.
- Both repositories' docs/reviews/2026-11-08-city-footprint-placement.md for merge/check evidence.

## Checks performed
- Backend merged main: full race suite with disposable real PostgreSQL PASS; go vet, make build and diff --check PASS.
- Wobbrik main CI PASS; final documentation merge rerun PASS (22s, 103 tasks). APK assembleDebug PASS (44s), apksigner verify PASS; user phone check OK.
- No live Firebase/network/process-death validation yet. Backend origin in delivered APK is empty.

## Exact next step
- In a new chat, read this handoff, PLAN stage 3.4, online plan and lifecycle contract. Inspect only relevant auth/endpoint/device configuration and recovery paths, then establish a real-server/device validation route within existing authorization. Ask only for missing endpoint/access that cannot be found locally; never substitute fake tests for live evidence.

## Live validation preflight — 8 November 2026
- Inspected Wobbrik `f2f6f61` and AppBackend `25a1967`; both working trees were clean before this documentation update.
- ADB detects only `emulator-5554`; Wobbrik is installed there. No physical phone is attached. User phone APK OK remains accepted separately.
- Firebase Android config exists. Generated debug BACKEND_ORIGIN is empty; no backend origin property found in project/user Gradle properties or relevant environment names. AppBackend local `.env` is absent. No credentials were printed.
- Live start/landing/checkpoint/finish, response-loss retry, reward deduplication and process-death recovery were NOT executed. No fake suite was substituted for live evidence.
- Asked user for the existing authorized HTTPS origin and test-account/config location; physical phone connection needed for physical-device proof. Do not deploy or invent an endpoint.
- Current exact next step: obtain that configuration, verify backend reachability/auth on the selected device, then run the live scenarios and compare server revision/receipts/wallet before and after exact retry and process restart.
- No main merge, push, deployment or CI performed; separate user OK is required for main merge and deployment.

## Local backend route — 8 November 2026
- User authorized starting AppBackend locally; no remote deployment or main merge authorized.
- Running existing AppBackend `bin/server` with isolated PostgreSQL 18 on loopback port 55441, database `wobbrik_live`; all migrations succeeded. Binary source revision has not independently been established; rebuild/verify provenance before final live acceptance.
- HTTP origin `http://127.0.0.1:18080`; loopback TLS proxy origin `https://localhost:18443`. Host HTTPS `/readyz` PASS with local certificate. ADB reverse maps device tcp:18443 to host tcp:18443.
- Local debug APK assembled and installed on emulator; origin set through command-line Gradle property. Ignored `output/stage-three-live/local.init.gradle` adds local manifest/resources for localhost-only certificate trust; no release/source trust relaxation. Final merged manifest checked for networkSecurityConfig.
- Blocking auth evidence: real Firebase anonymous sign-up returns HTTP 400 `CONFIGURATION_NOT_FOUND` with current git-excluded Android Firebase configuration. No token was printed/persisted; no guest was created by that failed request. MainActivity displays connection error; local wobbrik_accounts remains empty.
- Asked user to initialize Firebase Authentication/Anonymous provider or supply working local config. No Console settings changed. Do not bypass real token verification or substitute Firebase emulator/fake auth for live proof.
- Local artifacts/config/logs/PIDs/certificate/private key are under ignored `output/stage-three-live/`; do not commit them. Backend app enables Wobbrik only, with empty billing products and deletion disabled.
- Next: resolve Firebase configuration, verify current-source backend binary, reattempt auth/recovery and then run gameplay/retry/process-death scenarios against local DB. Physical phone still not attached. No live gameplay/retry/recovery PASS claimed.

## Live continuation result — 8 November 2026

### Completed / current state
- User enabled Firebase Auth/Anonymous. Real anonymous sign-up now succeeds; temporary diagnostic credentials are private ignored output/stage-three-live/diagnostic-session.json, never print/commit.
- Real sync returned 403: backend FirebaseAuth.Verify globally rejected anonymous providers. Fixed on AppBackend branch fix/wobbrik-guest-gameplay-auth, commit 79bfc12. No main merge/push/deploy.
- Guest acceptance is explicitly scoped to stage-three Wobbrik sync/catalog/start/checkpoint/finish/attempt/operation routes. Billing and marketplace still reject anonymous providers; other routes keep existing policy.
- Rebuilt backend from current branch into output/stage-three-live/server. Existing local DB/process/proxy route retained. Real Firebase → sync 200; emulator MainActivity showed server coins 0 / level 1. Start committed an active attempt (sequence/floors/precise/rescues all 0, snapshot {}, geometry_version city-geometry-v1).
- Android then displayed “Cəhd bu versiyada bərpa edilə bilmir”. AcceptedAttemptRestore.decode requires checkpointJson == null for sequence 0, but real server returns default {}. This is the next concrete interoperability defect; preserve active attempt and journals.
- Subsequent process restart attempted; recovery did not pass. Final emulator hierarchy showed another app, so further control stopped to avoid contention. Physical phone remains unattached.

### Decisions / remaining work
- No auth bypass or fake substitution. No schema/DTO/rules version change in auth fix. Do not close/reset the active attempt to hide restore failure.
- Zero-checkpoint fix and live landing→checkpoint→Ready→finish, lost-response/exact retry, network interruption, process death and reward dedup remain open. Full stage-three acceptance not achieved.

### Relevant files / checks
- AppBackend docs/reviews/2026-11-08-guest-gameplay-auth.md: local final-diff review complete; full race suite with separate real PostgreSQL wobbrik_guest_auth_test PASS, go vet/build/diff --check PASS. Backend branch clean after commit.
- Wobbrik android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/AcceptedAttemptRestore.kt (zero-sequence decode), core/src/main/kotlin/appbackend/wobbrik/game/RecoveryDtos.kt (snapshot mapping), their focused tests; AppBackend attempt default snapshot and read/sync contract.
- Wobbrik debug APK build/install and local-only TLS overlay previously PASS. No Wobbrik implementation change or branch CI in this continuation.

### Exact next step
- New chat: read this handoff and canonical PLAN/stage-three/lifecycle contract. Fix safe zero-sequence empty snapshot interoperability on a dedicated Wobbrik fix branch, retaining strict counters/version/account guards; add focused regression using actual server JSON. Reuse live active attempt, rebuild/install with ignored local.init.gradle + -Pwobbrik.backendOrigin=https://localhost:18443, then continue live tests once emulator control is free (or physical phone attached). No merge/deploy without separate user OK.

## Initial empty snapshot fix — 8 November 2026
- Wobbrik branch `fix/initial-empty-snapshot-restore`: initial zero-sequence restore accepts null or empty JSON object only with zero counters; all existing version/geometry/continuation/account guards retained. No backend contract/schema change needed.
- Regression covers server-shaped JSON → RecoveryDtos → paused restore with same attempt ID, plus invalid counters/shapes/version/continuation rejection. Focused test + debug build PASS (9s); engine tests + core recovery tests PASS (3s).
- Updated APK installed with data preserved and existing ignored local TLS overlay. Viewed live home screenshot: “Cəhdə qayıt”, coins 0 / level 1 / XP 0. Existing DB attempt `c77c47db-5e08-410a-bf39-26680343d979` still active at sequence 0 / zero counters / `{}`.
- Emulator switched back to DailyPlate during next interaction. Stopped device controls and asked user to reserve emulator-5554; do not compete with another task. No gameplay/checkpoint/finish/retry/process-death acceptance claimed.
- Review: `docs/reviews/2026-11-08-initial-empty-snapshot-restore.md`; lifecycle contract documents the initial empty representation. No UI redesign. No merge/push/deploy/CI.
- Exact next step: after user confirms emulator is exclusive, open existing attempt, resume/drop to real accepted checkpoint, compare DB revision/receipts/wallet, then validate exact retry/network interruption/process-death without resetting attempt. Keep separate user OK gate for merge/deploy.
