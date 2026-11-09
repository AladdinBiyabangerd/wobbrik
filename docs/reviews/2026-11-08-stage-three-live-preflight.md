# Stage 3 local live-validation preflight — 8 November 2026

Branch: `docs/stage-three-live-validation-handoff`; inspected Wobbrik HEAD `f2f6f61`, backend source HEAD `25a1967`. Review covers documentation diff plus local runtime setup; no tracked application/backend code was changed.

## Evidence
- User phone APK acceptance retained; it does not prove backend/recovery.
- Only emulator-5554 connected. Local AppBackend and isolated PostgreSQL 18 started after user authorization; migrations and HTTP readiness PASS.
- HTTPS localhost:18443 proxy → HTTP 127.0.0.1:18080. Host readiness with explicit certificate verification PASS; emulator ADB reverse configured.
- Debug assemble PASS with local origin; installed without clearing existing app data. Local debug-only manifest/resource overlay trusts local certificate for localhost only. Merged manifest verified. Build/config artifacts and secrets remain ignored under output/stage-three-live.
- MainActivity reports connection error. Direct anonymous signup using existing Firebase project config returns HTTP 400 CONFIGURATION_NOT_FOUND. No auth bypass, Console change or successful test account creation occurred. Database game-account count remains zero.

## Findings and limits
Current Firebase Auth setup blocks live gameplay. User was asked to enable/configure Authentication/Anonymous or identify working local config. Existing backend executable was used; source provenance must be verified/rebuilt before final acceptance. Host TLS readiness is not proof of authenticated Android connectivity. No start/checkpoint/finish, exact retry, process-death or reward deduplication acceptance yet.

Code inspection also shows terminal checkpoint reconciliation in AcceptedEngineHost and its Application Ready collector; historical documentation asserting explicit finish is always required is stale. A fake recovery test exists, but no live proof was obtained.

## Checks and disposition
Debug assemble and diff whitespace check PASS. No branch CI run; no main merge/push/deploy. No backend code/schema/API changes were required for setup. Stage 3 remains incomplete. Continue with resolved Firebase config, then compare accepted attempts, operation receipts and wallet ledger across gameplay, lost responses and process restart.

## Auth resolved and next live defect
User enabled Firebase Authentication/Anonymous; real signup succeeded. Shared backend auth then rejected the real guest with 403. AppBackend fix/wobbrik-guest-gameplay-auth commit 79bfc12 fixes only explicit stage-three game routes and preserves billing/market rejection. Full race suite with real disposable PostgreSQL, go vet/build and local diff review PASS; see backend review of the same date.

Current-source branch binary replaced old executable locally. Real sync 200 and emulator server balance/level display succeeded; real start persisted sequence/floors/precise/rescues 0 and snapshot {}. Android restore requires null for sequence 0 and blocks the active attempt. Live gameplay/retry/process recovery remain incomplete. Final device hierarchy was another app; no further competing controls. Separate user OK still required for main merge/deploy.
