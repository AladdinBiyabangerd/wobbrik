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
- Existing checkpoint→finish intent persistence limitation, cosmetic linkage and Room/outbox remain explicit open work; do not claim them complete from APK acceptance.

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
