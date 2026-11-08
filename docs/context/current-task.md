# Stage 3 live validation handoff — 8 November 2026

## Latest handoff — Finish cosmetic integration, code complete / live acceptance open
- Completed: PLAN §3.4 server-catalog/account-unlock picker on `feat/finish-cosmetics`, retaining `e0af061`. Removed automatic hard-coded finish; five categories, free/current-account parts only, explicit confirm, back preserves attempt. Durable app-private UID/attempt draft restores selected parts and terminal misses without added floors only against matching accepted progress. Existing AtomicFile wire journal is unchanged and drains pending envelopes verbatim before any new selection.
- Current state: feature branch only; no merge/push/deploy/CI. Backend unchanged after inspecting catalog/finish/HTTP contract. User owns live tests. Existing shared emulator, active attempt, journals, backend and local DB were not controlled. UI evidence came from a separate read-only emulator on port 5580 and an ignored render-only activity, excluded from final APK.
- Decisions: approved picker design remains the reference; user additionally authorized compatible asset variants when footprint is reasonable. Kept 68 modular parts instead of 463,736 complete combinations. Built-in imagegen created `roof-atlas-v2.png` to align the 14 roofs to facade camera; source originals preserved. Runtime ships five RGBA-exact lossless WebP atlases (~6.28 MB ZIP payload, ~30 MiB decoded pixels), verified crop metadata and a shared roof eave anchor. No new pins, SKU, grants or catalog authority.
- Remaining work: user live catalog/unlocks → terminal → back/return → process death → explicit confirm, lost-response exact retry, old pending envelopes, account isolation and reward dedup checks. Full visual/stage 3 acceptance remains open: modular art is not the literal concept flower-island tower; preview uses at most six representative courses plus the actual floor-count label. No full 463,736-combination render or measured total heap/GPU/profile proof. A crash before an extra-floor checkpoint reaches the journal restores accepted progress, never promotes an unaccepted local draft.
- Relevant files: `docs/PLAN.md` §3.4; `docs/reviews/2026-11-08-finish-cosmetics-integration.md` and `docs/reviews/finish-cosmetics/`; `docs/design/finish-cosmetics-v1/`; AcceptedEngineHost, FinishDraftStore, AndroidFinishDraftStore, FinishCosmeticScreen, FinishCosmetics, AuthenticatedRecoveryOwner, GameplayHttpBackend, OnlineGame and WobbrikApplication. Existing AcceptedAttemptCommands/AttemptCoordinator journal mutation behavior is preserved.
- Checks performed: core 51 and engine 55 tests PASS; app assembleDebug/lintDebug PASS (warnings, no errors); explicit token contrast pairs >4.5:1; 68 exact server ID/category matches and nonempty alpha-body crops; all five WebP→PNG full RGBA byte comparisons PASS; actual phone/tablet portrait/landscape and 1.3 font renders visually inspected. Final diff review recorded; no live proof claimed. Local test APK uses existing ignored localhost TLS overlay/origin and excludes render fixture; signature verified.
- Exact next step: install `output/finish-cosmetics/Wobbrik-finish-cosmetics-debug.apk` for the existing `https://localhost:18443` test route without clearing app data; user runs the live scenarios above. Review their evidence in a new chat with this handoff and PLAN §3.4. Do not merge/deploy, reset the attempt or advance stage without separate user instruction.

## Previous handoff — Finish cosmetic asset candidates prepared
- Completed: read approved finish design, PLAN §3.4 and scoped host/command/recovery/transport paths; checked backend catalog and finish ownership validation. User explicitly authorized preparing missing assets in the approved style. Generated and visually inspected five transparent atlas candidates; copied originals into `docs/design/finish-cosmetics-v1/assets/`, saved prompts and intended 68-ID catalog map.
- Current state: `feat/finish-cosmetics`, based on `6e22f69`, retaining restore fix `e0af061`. No Kotlin/backend implementation changes yet. Existing attempt, journals, emulator and backend processes untouched. No live tests, merge, push, deploy or CI.
- Decisions: existing UI approval remains valid, do not ask again. User owns live tests; separate explicit OK for merge/deploy. Free parts implicit, other parts from current account unlocks only; no purchase/reward action. Candidate atlas map is art metadata, never authoritative catalog/unlocks.
- Findings: 68 cosmetics (13 wall, 14 window, 14 entrance, 14 roof, 13 lighting), 12 decorations excluded. Transport allowlist lacks catalog GET; DTO/account unlocks exist. Host auto-finishes with hard-coded base_6 parts; must wait for explicit selection. Existing pending finish journal must drain verbatim before any new selection command. Backend already checks exact five category IDs and free/current-account ownership; inspected contract needs no backend change for this selection unit.
- Remaining work: candidates need dark-background alpha/crop QA and consistent preview projection/anchors (facades and roofs differ). Then authenticated/versioned catalog load, account/epoch isolation, durable selection and terminal intent across restart (including misses without added floors), approved adaptive UI, explicit confirm, focused tests/build and isolated phone/tablet runtime renders. No production-art or implementation acceptance claimed.
- Relevant files: `docs/design/finish-cosmetics-v1/{README.md,phone-tablet.png,assets/}`; `docs/reviews/2026-11-08-finish-cosmetic-assets.md`; AcceptedEngineHost, AcceptedAttemptCommands, AuthenticatedRecoveryOwner, GameplayHttpBackend, RecoveryDtos, WobbrikApplication, OnlineGame, GameTokens; PLAN §3.4 and stage-three-online-plan; AppBackend catalog-v1.json, finish.go and wobbrik-game-api.md.
- Checks performed: 68 unique intended IDs exactly match backend cosmetic set; referenced atlas paths exist. Native sips confirms all PNGs 1254×1254 with alpha. Generated images visually inspected; runtime composition/phone/tablet render NOT performed. Pillow probe unavailable; no build/test/CI or live scenario executed for asset-only work.
- Exact next step: finish candidate crop/alpha/preview anchor QA using approved board and actual atlas pixels, then implement catalog/account-linked picker and durable explicit finish integration on current feature branch. Do not reset active attempt, overwrite pending envelope, control shared emulator, merge or deploy. Context rule stops this research/asset-preparation chat before a large code implementation; continue in a new chat unless user chooses otherwise.

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

## User steering after restore fix — 8 November 2026
- Restore fix committed as `e0af061`; branch `fix/initial-empty-snapshot-restore` is clean before this handoff update. Review/test results above remain valid.
- User explicitly chose code-only completion while emulator is shared, then requested the next part and said live testing will be performed personally. Do not resume emulator interaction without new instruction; live scenarios remain unverified.
- Next bounded implementation unit: PLAN §3.4 finish cosmetic-part selection linked to server catalog/account unlocks. Do not advance to stage 4 or invent a parallel plan. Before visual implementation read ui-craft and existing approved designs; present the applicable design for specific approval as AGENTS.md requires.
- Exact next step in a new chat: read this handoff, PLAN §3.4 and stage-three-online-plan; inspect AcceptedAttemptCommands/AcceptedEngineHost finish configuration, RecoveryCatalog/account unlock mapping and AppBackend finish contract. Establish the existing approved cosmetic-selection behavior/design before coding; preserve current active attempt and pending journals. New work uses its own feature branch retaining the restore-fix history. No merge/push/deploy or Console changes authorized.
