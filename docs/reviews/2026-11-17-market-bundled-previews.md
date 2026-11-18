# Android + AppBackend market preview fanout — 17 November 2026

Android branch `fix/menu-inventory-latency`, base `c579e22`; AppBackend persistent `feat/wobbrik`, base `7584346`, implementation commit `9f0faf9`. User authorized performance fixes in both repositories. Previous changes committed/preserved; no stash/reset of owner work. This is one completed cross-stack unit, not app-wide performance acceptance.

## Change and compatibility

Market list requests add `include=previews`. Backend selects full building columns from its existing listing/building join, with one statement snapshot and no per-item SQL. Bounded active, undeleted, matching-revision previews accompany the same public/own/filter/cursor page. Closed own history keeps metadata only; no lookahead preview leaks. Legacy clients get the unchanged response when include is absent. New Android on old server receives no previews member and retains bounded four-way detail reads; malformed/present previews cannot silently fall back. Present empty array means no available preview. Kotlin rejects duplicate, foreign, closed and version-mismatched entries using existing building/geometry parsing.

Cache still requires exact UID/account revision/query and complete captured bodies. The sole compatible path alias removes only the additive include suffix inside that same captured bundle, letting complete legacy caches replay their old page and detail bodies offline. No scope/filter/cursor alias, negative cache, completed public-page reuse, inflight coalescing or revision-sync removal. Existing purchase/withdraw flow still reads a live detail; embedded and cached cards grant no mutation capability. No new endpoint, rules version, schema/migration, wallet, ledger or dedup change. API documented in AppBackend `docs/wobbrik-game-api.md`. Production benefit requires separately authorized backend deployment; none performed.

## Measurement and checks

Controlled12-card fixture,40ms/request, same parsed result and final revision sync:

- Legacy server representation:14requests,5174UTF-8 JSON body bytes,222ms.
- Bundled representation:2requests,5199body bytes,92ms.
- Both complete bundles replay with0additional network requests; complete pre-option legacy bundle also replays offline.

This removes12network round trips. It adds25JSON bytes; it is not a bandwidth-saving claim. Fixture timings are not deployed/device or PostgreSQL query timings. Original HTTPS blocking cancellation/overlap duration remains unresolved.

Final normal core147/app37unit PASS, assembleDebug/lintDebug PASS25s. Logs `/private/tmp/wobbrik-market-bundle-final.log`, core evidence `market-bundled-previews/core-results.xml`. Core tests cover bundled/empty/closed previews, malformed/duplicate/foreign/mismatched data, old-server fallback, full capture/replay and old-cache compatibility.

Backend full `go test -race ./... -count=1` with disposable UTF-8 PostgreSQL18 PASS (platform23.791s); go vet and server build PASS. HTTP integration verifies old shape, semantic equivalence to detail, filters, cursor/lookahead, closed/empty history and owner isolation. Initial new test failed on jsonb whitespace byte comparison, fixed to semantic JSON equality; full final suite rerun passed. DB stopped after use. Logs `/private/tmp/wobbrik-perf-backend-final.log`, `-vet.log`; backend review `docs/reviews/2026-11-17-wobbrik-market-previews.md`.

Final native Compose host18/18PASS,0failure/error/skip,26.839s test/Gradle29s. Public+own bundled cold2request/0detail, cached-before-network and live detail-before-purchase, legacy fallback/cursor/revision/login regression checked. Evidence `docs/reviews/market-bundled-previews/host-results.xml`; log `/private/tmp/wobbrik-market-bundle-host-verified.log`. Initial own-tab predicate failed twice (refresh then public-only action); final source uses the actual enabled own/public card action and full suite rerun PASS. No production fix was needed for those harness failures.

Local incremental diff review: parser/representation fallback, exact cache alias, native screen action gating, bounded SQL/cursor semantics, auth-derived owner and legacy behavior checked. Diff check PASS. No UI production/layout/style changes, no new visual parity/contrast claim. Existing host screenshots are regression harness outputs, not a new design acceptance. No CI/emulator, merge, push or deploy.

## App-wide inspection boundary and remaining work

Source checkpoints inspected: shared OnlineGame auth/capability loop, Inventory/City reads, Market list/detail, leaderboard fit traversal and profile art provider, plus backend market/inventory/city/profile/leaderboard entry points. This is source triage, not end-to-end route/GPU/device profiling. Inventory local-first, optional city and art reuse evidence remains in previous reviews. OnlineGame still polls canonical recovery and both host snapshots each display frame even in menus; preserving exact Ready/login epochs is mandatory in any redesign. ApprovedArt still performs first cold atlas decode synchronously (~60MiB retained across three groups); profile source boards use ImageBitmap.imageResource at provider entry. Neither is newly benchmarked or fixed here. City still fans out building reads. These are remaining investigation points, not newly proven root causes.

Exact next step: quantify idle-menu frame snapshot allocations and route cold/warm art decode across Home/City/Inventory/Market/Achievements/Profile/game on host, keeping UI designs and exact capability/session semantics; compare related backend query/request latency before choosing the next fix. Real Firebase/device/deployed latency, heap/durable disk, TalkBack/locales and complete visual parity remain unverified.
