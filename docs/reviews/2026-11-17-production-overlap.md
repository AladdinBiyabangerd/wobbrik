# AccountViews / MarketScreen overlap baseline — 17 November 2026

Branch `fix/menu-inventory-latency`, HEAD `083f870`, current dirty implementation. Read handoff and PLAN §1.1. Measurement only: no production source, backend, PLAN, profile, dependency pin or existing test changes. Initial dirty/untracked file SHA-256 snapshot verified unchanged before this review/handoff update. No commit/merge/push/CI/emulator.

## Scope and definition

Opt-in `production-overlap/ProductionOverlapBaselineTest.kt` mounts the **production MarketScreen** inside MobileMenuShell, using the OnlineGame-equivalent `InventoryReadKey(ready)` boundary. It exercises real AccountViews, ReadOnlyGameCache, GameplayHttpBackend, MarketRepository and owner/session checks. It does **not** mount full OnlineGame/Firebase/bootstrap, use an actual device, contact production, or measure real-user overlap frequency. The fixture duplicates the existing market host fixture deliberately; twelve listings, 40ms synthetic response delay, in-memory JournalFile. No new app instrumentation or coalescing implementation.

`samePathOverlaps` counts request entries whose exact path already has an active request; activity ends on return/exception. Multiplicity counts all entries including cancelled requests. This differs from total request concurrency: four different detail paths in flight are expected fanout, not duplicate-path overlap. All scenarios use one account/session/Ready; they do not assert cross-account sharing is safe.

Two transport modes are essential. Cooperative `CompletableDeferred.await` cancels promptly. Blocking `CountDownLatch.await` retains work until released, modelling the blocking-I/O cancellation boundary in production UrlConnectionGameplayTransport (`GameplayHttpBackend.kt`): responseCode/read are blocking, disconnect is in finally, and no cancellation handler interrupts the connection. This is a controlled transport model, **not execution of an actual HTTPS socket or proof of its cancellation timing**. The model's subsequent delay cooperates with cancellation, so cancelled bodies are not added to completed-byte counters. Bytes below exclude headers/TLS, cancelled wire traffic and bootstrap.

## Measured results

- Cold mount plus five separately settled shell recompositions while page response is blocked: exactly one pending page request; after release **14 requests / 8776 completed response UTF-8 bytes / 0 same-path overlaps / 0 cancellations**. No duplicate refresh caused by these recompositions.
- Hide screen, settle disposal, then remount with the same configured AccountViews/Ready after completion: cumulative **28 / 17552 / 0 overlaps / 0 cancellations**, multiplicity2. Remount adds a full fresh14-request pass despite matching cache. These are sequential refreshes, not overlapping duplicates; public market intentionally uses refresh=true.
- Public → own listings → public, each transition after its blocked page request starts, cooperative transport: **16 / 8776 / 0 overlaps / 2 cancellations**, maximum path multiplicity2. Two cancelled list entries plus the final14-request pass. Repeated path alone would incorrectly identify this as parallel duplicate work.
- Same production screen transitions with blocking transport model: **16 / 8776 / 1 same-path overlap / 2 cancellations**, maximum multiplicity2. The first public list is still active when the final public list enters. Thus cancellation-only source reasoning cannot rule out production overlap during rapid return. Neither cancelled read publishes stale cards or submits a command in this scenario; successful final read completes.
- Positive control: two manually concurrent AccountViews refresh=true reads, identical capability/query: **28 / 17552**, every path twice, **5 overlap entries in this run**. At least the gated page overlap is deterministic; later overlap count is scheduler-dependent and the assertion deliberately requires >=1. This control proves the counter can detect overlap, not that a single mounted screen starts two reads.

No production latency percentile or2+s/10+s confirmation follows from this sample. No durable disk measurement. No optimization or speedup claim. Production overlap frequency and actual socket lifetime remain unmeasured.

## Interpretation / next boundary

Normal mounted-screen recomposition did not duplicate requests. Same-key remount refresh is intentional. Rapid tab return can overlap a cancelled-but-still-blocking identical public request in the model; a narrowly scoped real-socket/device trace would be needed to quantify occurrence/duration in deployment. Direct concurrent AccountViews reads still have no inflight coalescing. Do not introduce coalescing merely from the positive control, remove final revision sync, or indefinitely reuse completed public pages. Any future sharing needs exact capability/session/query boundaries, subscriber cancellation and late-result/revision tests.

Backend contract read-only checked: AppBackend `docs/wobbrik-game-api.md` marketplace section confirms live cursors, page metadata versus detail bodies and sync authority. Measurement changes no API/DTO/domain/schema; backend changes/tests unnecessary for this unit.

## Validation and local review

Focused offline host **4 tests PASS**,0failure/error/skip,9.245s test time; Gradle12s. Results `production-overlap/results.xml`; log `/private/tmp/wobbrik-overlap.log`. No commands submitted. `git diff --check` PASS. Existing dirty files preserved by hash comparison. Review: harness only, no production behavior or visual change; no new render/contrast claim and no app-wide build/lint/CI rerun.

The initial sandbox run failed on Gradle cache lock; escalated focused execution was permitted. First host run3tests/1failure: a stale enabled Yenilə node made the wait return before the final read completed. The harness now waits for expected final network completion/zero active requests before checking settlement; no production fix was made. That initial failure is not counted as PASS.

Reproduce from repository root:

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' android-billing/gradlew -p android-billing :app:testDebugUnitTest --tests '*ProductionOverlapBaselineTest' -I "$PWD/docs/reviews/production-overlap/measure.init.gradle" --offline --console=plain
```
