# AccountViews cancelled-timeout fallback — 17 November 2026

Branch `fix/menu-inventory-latency`; existing work preserved in `9cac996`. Reviewed incremental production/test diff against that commit plus this review and handoff.

Blocking HTTPS may throw IOException after coroutine cancellation. AccountViews now checks currentCoroutineContext().ensureActive() immediately on entering the IOException catch, before error classification or offline replay. This preserves CancellationException and prevents a cache-miss IllegalArgumentException from replacing it. Active transient-error fallback and authorization rejection behavior remain covered by existing tests.

Regression uses a latch-blocked transport throwing SocketTimeoutException after cancellation, with and without a seeded cache. Asserts CancellationException, cancelled job, no return/callback, zero fallback disk reads and zero writes. No timing speedup or prompt socket cancellation claim.

Focused AccountViews 14/14 unit tests PASS; assembleDebug and lintDebug PASS, Gradle32s. Log `/private/tmp/wobbrik-cancel-checks.log`. Diff check PASS. Local incremental review found no further issue. Prior dirty files preserved; only AccountViews intentionally changed before documentation updates. No visual change, new render, CI, emulator, merge, push or deployment. Real HTTPS measurement harness not rerun; original socket lifetime/overlap finding remains open.

Backend compatibility: client coroutine cancellation guard only; no endpoint, DTO, domain, revision, dedup, schema or wallet contract changes. AppBackend changes/tests unnecessary for this unit; backend worktree checked clean. This does not conclude the broader menu performance task.
