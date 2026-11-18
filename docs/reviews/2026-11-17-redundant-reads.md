# Market redundant-read measurement — 17 November 2026

Branch `fix/menu-inventory-latency`, HEAD `083f870`; measured current dirty MarketScreen/AccountViews/ReadOnlyGameCache flow under PLAN §1.1. Production files unchanged. Existing dirty work, PLAN, baseline profiles and other reviews preserved. No commit, merge, push, CI, emulator or deployment.

## Reproducible scope

Opt-in host test `redundant-reads/RedundantReadMeasurementTest.kt` calls real AccountViews, ReadOnlyGameCache, GameplayHttpBackend and MarketRepository using an isolated copy of the prior market fixture. Twelve active listings, four concurrent detail reads per previewPage, fixed 40ms per synthetic response, in-memory JournalFile. Counters cover HTTP requests, completed response UTF-8 bytes, per-path multiplicity, JournalFile reads/writes. Timings are single host samples, not production/device or filesystem latency. Account owner bootstrap is outside the counters; no Firebase/full route benchmark. Source tracing ties this to MarketScreen's `LaunchedEffect` list branch, which passes refresh=true and onLocal. No claim that one normal mount launches two refreshes.

## Final results

- Cold Market:239ms,14requests,8776response bytes,1cache read/miss,1write; no duplicate HTTP path.
- Warm Market refresh:227ms,14requests,8776bytes,1cache read,1write; no duplicate HTTP path. Warm cache does not reduce background traffic.
- Completed exact-capability reuse (refresh=false):3ms,0requests/bytes,1cache read,0writes.
- Two sequential refreshes:457ms,28requests,17552bytes,2reads/2writes; all14paths called twice. Distinct refresh events must not automatically be treated as redundant because listings are live.
- Two concurrent identical refreshes:232ms,28requests,17552bytes,2reads/2writes; all14paths called twice. AccountViews has a completed-key set, no inflight coalescing. This demonstrates overlap cost, not observed frequency in production navigation.
- Read-only view with onLocal:3ms,0requests/bytes,2reads/0writes. AccountViews first replays for onLocal and then replays again in the ready==null branch. Each replay reads/parses the envelope and reruns repository DTO parsing; genuine duplicate local work. MarketScreen itself requires Ready, so this is a generic AccountViews path, not its normal market list path.
- Fresh action detail:42ms,1request,599bytes,0cache reads/writes. MarketScreen detail branch deliberately bypasses display-only cached previews; this is a safety read, not a removable duplicate.
- One capture calling the same detail twice sequentially:153ms,3requests,1473bytes,0reads/1write; same detail path twice plus sync.
- Same capture with concurrent duplicate detail calls:96ms,3requests,1473bytes,0reads/1write. ReadOnlyGameCache stores one body per path but sends both requests. Its duplicate-body equality check is consistency detection, not request coalescing. Synthetic duplicate blocks probe the cache primitive; normal previewPage has unique listing IDs and did not generate duplicate detail paths.

## Interpretation and bounded next step

The normal14requests are one page,12distinct details and one revision sync (275bytes). Page metadata lacks building geometry/parts, so current cards need detail fanout. The final sync checks captured account revision; it was not removed. Public listings change independently of viewer revision, so completed-read reuse cannot replace mounted Market refresh.

Candidate follow-up: eliminate the proven ready==null double replay first, retaining session/UID/revision checks and callback semantics. Evaluate exact session/capability/query inflight coalescing only with cancellation, late subscriber, failure/retry and revision-race tests; do not reuse completed public reads indefinitely or merge distinct query/cursor/account epochs. No optimization implemented in this measurement unit, so there is no before/after speedup claim.

Backend contract/source checked read-only: AppBackend docs/wobbrik-game-api.md marketplace/sync and internal/wobbrik/market.go MarketFiltered/ReadListing confirm live cursors, metadata page versus full detail and account sync. No API/DTO/domain/schema change needed for measurement. Backend unchanged; backend tests not run.

## Validation and review

Final focused offline host suite2tests/9scenarios PASS,0failures/errors; Gradle6s. Exact request, byte and JournalFile read assertions; equality checked for duplicate detail results. Evidence `redundant-reads/results.xml`; log `/private/tmp/wobbrik-redundant-reads-final.log`. Re-run from repository root:

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' android-billing/gradlew -p android-billing :app:testDebugUnitTest --tests '*RedundantReadMeasurementTest' -I "$PWD/docs/reviews/redundant-reads/measure.init.gradle" --offline --console=plain
```

Initial launch used the wrong wrapper location (exit127), then sandbox blocked the Gradle cache lock; neither counted as PASS. Authorized escalated focused run succeeded. Final local review: opt-in harness only, fixture duplication explicit, no production/config/dependency pin changes; diff whitespace check PASS. No UI visuals changed; rendering/contrast not applicable. Full app checks/CI not rerun for measurement-only files. Real transport/header bytes, failed requests, durable disk timings, route frequency and production2+s/10+s remain unmeasured.
