# Real Firebase account isolation / locked finish API — 8 November 2026

Branch: `docs/account-isolation-live-api`, based on `1e0f088`. Bounded authenticated HTTP contract validation from PLAN §3.4; no Android/UI/domain/schema implementation. No stage acceptance claim.

## Independent runtime and provenance

Copied the verified immutable archive and binary from checkpoint evidence; independently asserted archive SHA-256 `40109e29e1b277eec0a21c6161acd711f02c03819fc5bd8c8cf83787768099b8` and binary SHA-256 `93dd3e00916c7a2968ee8692fc4acb260fd9e26c4d3d9ba071d437542a58e85b`, backend commit `79bfc123009b4428d45142904d5ac682810c9dd4`, previously built with Go 1.27.3. This is a verified artifact reuse, not a new build or attribution to today's backend checkout.

New PostgreSQL 18.6 cluster on loopback 55449/database `wobbrik_account_unlock`; migrations succeeded. Backend HTTP 18084 PID 27357/session 72229, TLS loopback 18447 PID 27379/session 59279; retained for continuation. Backend binds wildcard through its existing implementation. Certificate-verified new readiness PASS. No device/reverse attached to this API environment, no new emulator or APK install. This API-only subset needs no Android account mutation. New DB/TLS/artifacts/job key are independent of retained runtime; no copied Firebase app session/journal.

Initial bare initdb resolved to libpq client distribution without postgres and failed before cluster creation. Retried using full PostgreSQL 18 binary successfully; not a migration or server failure.

## Observed API results

Created two distinct real Firebase anonymous identities through normal signup, with real token verification by backend (no auth bypass). Tokens/refresh tokens are private ignored files with restricted permissions, never printed or committed. Initial sync both revision 0 / coins 0 / no nonfree unlocks. Started disposable A/B attempts with real start requests; no checkpoint/floor/reward was fabricated.

- B read of A attempt and operation: 404, complete 22-table domain/wallet snapshot unchanged.
- B finish against A attempt with B current revision and valid free categories: 409, unchanged snapshot.
- B finish its own sequence-0 attempt using versioned `coins_wall_1` plus four free category IDs: 400 `invalid_request`; exact rejected request replay also 400. Both leave all 22 tables unchanged, including revision/receipt/result/building/unlock/ledger.
- Positive control changes only the locked wall to catalog `base_1_wall`, keeping same operation ID, revision, own attempt and sequence: 200. Therefore the prior failure is isolated to nonfree ownership rather than malformed categories/stale revision/foreign attempt. Exact accepted retry returns identical receipt, one zero-floor result, no building/ledger/unlock. This is an explicit API contract control, not real terminal gameplay acceptance.
- Replaying A start envelope with B token: 409 while B already has an active attempt; no effects. Together with foreign operation 404 this confirms this observed replay does not leak A receipt. It is not a claim that all operation-ID collision races are covered.
- A/B sync remains account-specific with coins 0/no unlocks. A disposable attempt stays active at sequence 0/revision 1. B zero-floor control is finished at revision 2.
- Guest `/shop/buy` for catalog `coins_wall_1` (price 100) returns 403 with no domain writes. This is existing route policy, not a newly introduced defect or proof of insufficient-funds handling. Guest auth is deliberately restricted to sync/catalog/start/checkpoint/finish/attempt/operation routes.

Private evidence: ignored `output/account-unlock-live/{manifest.json,catalog.json,api-evidence.json,verification.json,baseline.json,before-rejections.json,after-free-control.json,after.json,retained-before.json,retained-final.json,retained-journal-final.tar}`. A local harness initially used `id` instead of wire `attempt_id` in final sync assertion; corrected it and finalized against existing identities/DB without rerunning starts or creating replacement accounts. All final assertions PASS. Scripts are private scenario harnesses, not a new product framework.

## Preservation and limits

Retained checkpoint environment compared before/after across all 22 wobbrik/domain/wallet tables in repeatable-read read-only transactions: exactly equal. Active retained attempt `f73722ea-b54f-4985-9c7e-85a442c1d0a3` remains sequence/floors 2. Journal payload equals preflight tombstone; 5582 reverse remains 18443 → 18445. No device input/sign-out/restart/install or retained DB write. New, retained and original TLS readiness all PASS; AppBackend owner checkout clean/unmodified.

Positive nonfree acquisition, actual Android account switching, delayed response/new epoch with real sessions and paid-part persistence in a >=10-floor building remain OPEN. Existing JVM epoch coverage is not live evidence. The Android app only bootstraps anonymous auth and links an existing guest in place; it has no sign-out/switch-account flow. Local google-services.json contains no OAuth clients and inspected project/user Gradle properties have no Google web-client override. No Console/provider configuration changed. Cannot legitimately buy through guest shop or claim a linked-account purchase using these guest identities. No synthetic balance/unlock grants or ad-earned assertions used.

Next bounded unit needs an existing authorized non-guest test session/configuration (or real supported acquisition flow), with actual gameplay earning the catalog price. Inspect/establish Google link access in a separate disposable app environment before further Android work; do not inject REST guest refresh tokens into SDK state. Preserve the retained two-floor attempt. Do not broaden guest policy or introduce UI merely to force acceptance.

Local review: checked tracked documentation against redacted statuses, assertions, source route/finish policy and final snapshots. Documentation diff whitespace PASS before commit. No production/API change needed for this check. No new JVM/full backend suite (previous preflight results remain separate), branch CI, merge/push/deploy, visual or full stage acceptance.
