# Finish cosmetics live validation — 8 November 2026

- Branch: `docs/finish-cosmetics-live-validation`, based on implementation `f5441a6` (assets `17f8201`). Documentation only; no implementation/backend change, merge, push, deployment or CI.
- Authorization: user stated emulator was free and requested live checks. Existing app data and attempt were preserved; installed the previously signed local debug APK with `adb install -r`.
- Route: emulator-5554, 1080×2400, font scale 1.0; real Firebase identity, existing localhost TLS route on 18443, existing AppBackend process and PostgreSQL `wobbrik_live` on 55441. No auth bypass, direct domain/DB mutation or fabricated checkpoint.

## Observed live results

1. Initial active attempt `c77c47db-5e08-410a-bf39-26680343d979`, sequence/floors 0, resumed through the actual UI. First drop committed sequence 1 / floors 1 / precise 1. Process force-stop/relaunch retained progress; home displayed coin/XP 1. Returned and resumed the same attempt.
2. Second landing committed sequence 2 / floors 2. Next miss added no floor and opened the live catalog picker. Attempt remained active; result count remained zero before explicit confirm.
3. Selected the second free wall (red brick), pressed Back, force-stopped/relaunched and returned. Picker restored red brick and two floors. No automatic finish. The current account has zero unlock rows; this proves the free selection path only.
4. Explicit confirm while the local transport was unavailable showed connection-error UI; DB remained active with zero results. The first proxy relaunch did not survive the command session, so this was connection refusal, not the planned injected HTTP 503. Persistent journal was captured without clearing/replacing it.
5. Restored transport with a temporary local test proxy, forwarded finish to the real backend and deliberately closed the client connection after backend HTTP 200. DB now showed finished, one result, revision 4, balance 2. Client remained on connection-error UI.
6. Journal bytes before dispatch and after lost response were identical (409 bytes, SHA-256 `ec4a4a425e8e0f8c698289e7413d34ac840f6f8362113925cfee346ead66fed8`). Force-stop/relaunch resent the pending finish before sync/catalog. Both real finish requests returned 200 and had the same body SHA-256 `74d400eacb8e9902ab9d36d1a066ccaf5d35bd377b151e3d8caabc3d29dd67f9`.
7. DB snapshot immediately after lost response and after retry compared byte-identical: revision 4, balance 2, results 1, operations 4. Result floors 2 / precise 1 / rescues 0. Home reconciled to coin 2 / XP 2 / record 2 / Play. No duplicate reward/result/revision in this observed retry.

## Evidence and limits

- Screenshots actually viewed: initial home, paused attempt, accepted landing, live picker, restored red-brick draft, offline error, lost-response error and reconciled home. Private evidence: ignored `output/finish-cosmetics/live/`; request hashes in `output/stage-three-live/live-requests.jsonl`. No credentials logged in that trace. These are functional phone screenshots, not new phone/tablet design acceptance.
- No 10+ floor building ownership/persisted cosmetic-parts proof: this two-floor result correctly does not create an inventory building. No nonfree unlock/account switching, Google linking, old-version journal migration, checkpoint response-loss, mid-flight process kill, physical-device or full visual acceptance in this run. Stage 3 acceptance remains open.
- Backend binary provenance limitation: `go version -m output/stage-three-live/server` reports base revision `25a1967`, `vcs.modified=true`; exact working diff used to build that running binary is not recoverable from build metadata. Live results apply to the existing local process, not proof of a clean backend commit. Current AppBackend checkout is `feat/dailyplate-food-search` with owner changes; untouched, not rebuilt/restarted.
- After successful reconciliation and the snapshot comparison, new activity appeared without agent input: attempt `3aa65042-954f-4b94-8ad0-fb51446bd117` active at sequence/floors 1, account revision 6 / balance 3. Stopped device input rather than interfere. This later activity is outside the measured retry interval.
- Temporary fault proxy stopped; original `tls_proxy.py` restored at 18443, `/readyz` passed, fault markers removed and proxy.pid refreshed. Existing backend/DB and new attempt remain intact. Original proxy runs in execution session 47342.
- No code defect requiring a change found in the bounded scenarios. No backend contract/schema change needed for these checks. No suite/build rerun for documentation-only work; final diff whitespace/review checks performed separately.

## Next step

Coordinate exclusive emulator use before more input. Preserve the new attempt and journals. Complete remaining stage 3.4 scenarios (especially 10+ floor finish with persisted selected parts, current-account nonfree unlocks/account isolation, checkpoint response loss and old pending envelopes) against a backend with verified source provenance. No merge/deploy without separate instruction.
