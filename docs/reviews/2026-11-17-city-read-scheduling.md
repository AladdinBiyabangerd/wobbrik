# City detail read scheduling — 17 noyabr 2026

Branch `fix/menu-inventory-latency`, base HEAD `083f870`; reviewed working diff: `CitySceneReads.kt`, `ReadOnlyGameCache.kt`, new `MenuReadLatencyTest.kt`. Earlier initial-sync/art and owner changes preserved. No commit/merge/push/CI.

## Measurement and scope

PLAN §1.1 request/byte baseline uses production GameplayHttpBackend, repositories, DTO parsers and cache capture/replay with an injected transport: 12 buildings/active listings, one city/listing page, empty decorations, 40ms suspending delay per request. Bytes are successful response body UTF-8 bytes before compression, excluding headers/TLS/request bytes. Fixture omits large accepted_geometry/snapshot payloads; these byte counts are not representative production payload sizes. Elapsed is host wall time including parse/scheduling, single samples, not Android route/frame latency or a statistically stable benchmark. Market harness mirrors MarketScreen page + semaphore(4) detail scheduling; it does not mount Compose. CityScreen's AccountViews onLocal/reuse callbacks are source-audited, not route-mounted here. No real auth/server traffic, emulator or live device capture.

Before → after (final source):

- City repository: 714 → 264ms; 15 requests / 3630 bytes both; detail peak 1 → 4.
- City capture: 734 → 316ms; 16 requests / 3903 bytes both, including final accepted-revision sync. Account bootstrap sync excluded.
- City replay: 3 → 2ms; 0 requests / 0 network response bytes both. Full fixture replay includes all scene parsers; not disk/fsync timing (in-memory JournalFile).
- Market list + active previews: 220 → 180ms; 13 requests / 4901 bytes both, peak4. Market source unchanged; timing variation is not an optimization claim.

Raw stdout retained below; reproducible fixture is core `MenuReadLatencyTest`. Before logs `/private/tmp/wobbrik-menu-before.{log,xml}`; final `/private/tmp/wobbrik-menu-final.log`.

## Change and local review

Only independent building detail reads overlap, bounded to four network reads per scene load. Membership pagination, ordered building output, per-building revision equality, decoration traversal, final city reread and capture sync remain. First/final city reads intentionally remain separate: deduplicating them would remove the racing-write check. Sibling work is cancelled by coroutineScope on failure; no partial scene publishes. Cache capture's shared response map now synchronizes insertion/duplicate-body validation because completion may occur on distinct IO workers. No new cache, mutation capability, local wallet, endpoint, schema or retry contract.

Reviewed AppBackend `docs/wobbrik-game-api.md`, `internal/platform/wobbrik_http.go` wobBuilding/wobCity and `internal/wobbrik/market.go` ReadBuilding: independent owner-scoped read endpoint accepts unchanged requests. Server changes unnecessary. Actual DB contention/load under four concurrent reads remains unmeasured; backend tests were not run.

## Checks and limitations

- Final `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (5s incremental; preceding full run41s). New3 fixture tests PASS: bounded fanout/order/bytes, multi-IO cache capture/replay, concurrent building conflict/login epoch fail closed. Existing city pagination/revision/decorations/account isolation and cache tests run in full core suite.
- Local diff review: checked concurrency ownership, order, error cancellation, final revision brackets and unchanged API. `git diff --check` PASS.
- No UI/layout/token changes and no visual acceptance claim; native renders not run for this scheduling-only unit. Real 2+s/10+s route reports still need a production/device baseline.
- Market page+N active detail fanout still blocks list publication. CityScreen still does not pass onLocal; completed matching AccountViews replay skips network, while first cached-ready entry waits for capture. Missing/stale cache must still refresh. General capture coalescing and redundant reads remain open. This unit reduces serialized waiting, not request/byte volume.

## Retained measurement stdout

Before:
```text
MENU city elapsedMs=714 requests=15 responseUtf8Bytes=3630 peak=1
MENU market elapsedMs=220 requests=13 responseUtf8Bytes=4901 peak=4
MENU city-capture elapsedMs=734 requests=16 responseUtf8Bytes=3903
MENU city-replay elapsedMs=3 requests=0 responseUtf8Bytes=0
```

After:
```text
MENU city elapsedMs=264 requests=15 responseUtf8Bytes=3630 peak=4
MENU market elapsedMs=180 requests=13 responseUtf8Bytes=4901 peak=4
MENU city-capture elapsedMs=316 requests=16 responseUtf8Bytes=3903
MENU city-replay elapsedMs=2 requests=0 responseUtf8Bytes=0
```
