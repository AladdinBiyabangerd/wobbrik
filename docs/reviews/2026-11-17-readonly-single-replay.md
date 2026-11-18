# AccountViews read-only single replay — 17 November 2026

Branch `fix/menu-inventory-latency`, HEAD `083f870`; local review scope: AccountViews.kt and new normal app unit test AccountViewsReadOnlyTest.kt. Existing dirty Market/City/cache/art/PLAN/baseline profiles and historical opt-in measurement harness left unchanged. Handoff updated additively. No commit/merge/push/CI/deploy/emulator.

## Change and isolation

Previously ready==null with onLocal performed speculative cache.replay, then another final cache.replay: two file reads, envelope parses and repository blocks. Only online Ready views now use speculative local publication. Read-only views use the existing final replay once, then invoke onCached once and onLocal once with that exact result. No second attempt on miss/corruption/revision failure, and nullable results are passed directly without null-as-miss ambiguity. onFresh is never called for read-only data.

Replay remains on Dispatchers.IO. Existing UID/cache-key/exact accepted revision validation remains in ReadOnlyGameCache; entry and post-replay session checks remain. Additional current-session and cancellation checks between onCached/onLocal and after onLocal prevent a callback-triggered account switch, same-UID login epoch change or cancellation from returning accepted data under the old session. Screen-level AccountView.current/exact Ready/readOnly reference checks remain the owner's publication guard; this does not create a new mutable recovery capability. Online refresh/fallback/completed-read semantics unchanged. No coalescing or revision-sync removal.

## Validation

Focused normal app unit suite13/13PASS (8new read-only +5existing AccountViews); assembleDebug/lintDebug PASS, Gradle26s. Log `/private/tmp/wobbrik-readonly-replay-checks.log`; retained XML `readonly-replay/{AccountViewsReadOnlyTest,AccountViewsTest}.xml`.

New regressions assert one disk read and one repository block, callback order/count and caller thread, exact same result identity, no callback path, nullable result, cache miss/corruption/revision/query rejection without retry/publication, wrong UID rejection before disk, Alice→Bob/same-UID epoch change during replay, epoch change in either callback, cancellation during replay/after local callback, and zero transport requests. Existing tests cover online local-before-blocked-network, transient fallback versus authorization, exact capability reuse/refresh, account change and cancellation. No wall-clock speedup claim; measured work changes from2cache reads to1.

Historical `redundant-reads/RedundantReadMeasurementTest.kt` is intentionally preserved as requested; its old read-only2reads assertion describes the pre-fix baseline and is not a current regression gate. The new normal test provides the post-fix1read gate. Historical XML/reviews unchanged.

Local diff review: change limited to read-only speculative gate and final callback delivery, no shared result memoization across epochs, no new authority, no cache/server contract change. `git diff --check` PASS. UI unchanged, native visual render/contrast not applicable. No AppBackend API/DTO/schema/domain change needed; prior contract check remains applicable; backend tests not run. Full app suite/device runtime not claimed.
