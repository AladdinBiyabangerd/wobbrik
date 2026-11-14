# Navigation read performance — 14 November 2026

- Branch: `fix/mobile-consistency-performance`, base `aa9554ea646e19034c4c3d535cf16cb936ecee71`.
- Authorization: user approved the shown active/pause reference, consistent mobile styling/sizing, latency investigation and separate logical commits on this branch. Remaining UI work is authorized; do not ask again.
- Reviewed diff: `AccountViews.kt`, construction in `WobbrikApplication.kt`, inventory/city read call sites and `AccountViewsTest.kt`.

## Findings and changes

Every online inventory/detail/city revisit repeated its full repository read plus the cache capture's final sync, even if the exact accepted Ready capability had not changed. Cache file hashing/parsing/replay/persistence also ran in the caller context (the destination's Compose LaunchedEffect).

Successful online bundles are now reused only within the same BillingSession and identical Ready object. The existing complete, revision-scoped cache is replayed; no new wallet, command queue or storage schema is introduced. A new Ready identity (even equal snapshot), login epoch, or explicit refresh requires a live read. Cache failures retry live reads; transient network failures retain existing offline fallback and authorization rejections still propagate. Explicit refresh invalidates the reuse marker before attempting the request. Sessions are checked before and after work. Work runs on Dispatchers.IO; the cached-status callback returns to the caller context. Fresh retries reset the destination's fallback flag.

This improves repeat navigation after a successful read; first/uncached visits still wait for the backend and revision fence. Reuse is accepted data from the current capability, not a claim that other-device edits have been polled. Foreground recovery/new Ready and inventory refresh invalidate reuse. City has no new refresh control in this nonvisual unit. No live device latency/FPS benchmark was performed.

## Local review

Checked session isolation, equal-snapshot/new-capability invalidation, offline/rejection handling, cancellation propagation, optional cache persistence, refresh behavior and import/placement. Review corrected refresh invalidation and fallback-flag reset. AppBackend domain/API/DTO/schema is unchanged; backend source changes/tests are not required for this read scheduling/cache reuse change.

## Checks

Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`: PASS, 25s; 236 tests (core117, engine95, app24), zero failures/errors. `git diff --check`: PASS. Regression verifies no added HTTP calls on repeat navigation, live explicit refresh, equal-snapshot/new-capability reread, off-caller-thread execution, transient fallback, denied refresh invalidation and stale-login rejection. No emulator, remote CI, local CI, merge, push or deployment. No visual layout/art changes or render PASS claim.

## Remaining authorized work

Shared mobile logo/header/control styling and proportional sizing including larger phones; route transition behavior; modular floor seams and foundation/island contact. Use existing GameTokens and approved references; mobile portrait only. These are not completed by this performance commit.
