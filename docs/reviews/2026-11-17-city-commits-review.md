# City paired commit review — 17 November 2026

Scope: Android `fix/city-detail-batching`, commit `85307bfd0f541e37902f0cdbb390062d5bd0bc6d` against `8fd0bc6`; AppBackend `feat/wobbrik`, commit `23102b44ad4794d1efff522ef1f3520f93bd9fc0` against `9f0faf9`. Read the active handoff and PLAN §1.1. Manual review of the committed City changes, surrounding parsers/cache and backend read/HTTP boundaries; existing HTTPS working changes excluded from the review scope.

No actionable correctness finding identified. Checked compact DTO/parser compatibility, owner filtering, optional detail budget and fallback, pagination, final revision bracket, strict old-server handling and legacy cache replay. No source fixes made. This is not phase acceptance or measured device/production performance.

Fresh checks: `:core:test --tests appbackend.wobbrik.game.CitySceneReadsTest --tests appbackend.wobbrik.game.ReadOnlyGameCacheTest` PASS, 19 tests, zero skipped/failures/errors. Both committed diffs pass `git show --check`. Initial sandbox Gradle invocation failed on the wrapper lock; the authorized rerun passed. Tests use the current working tree including the preserved HTTPS transport, rather than an isolated City-only checkout.

Backend PostgreSQL/race/build checks were not rerun in this review. Reviewed the committed tests and existing evidence in `2026-11-17-city-detail-batching.md`; their prior results remain historical evidence. Authenticated Android→Go E2E, device/libcore behavior and production latency remain unverified.

The tracked working diff was byte-identical before/after checks (`git diff --binary` comparison); existing untracked HTTPS evidence was untouched. No CI, merge, commit, push, deploy or emulator launch.
