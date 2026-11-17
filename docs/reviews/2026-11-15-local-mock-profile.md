# Local mock profile — 15 November 2026

Branch: `feat/local-mock-profile`, base `6c96e9c`; uncommitted scoped changes reviewed locally. Existing dirty Home/City/token/render/baseline-profile work is preserved and is not part of this auth/mock review. No commit, merge, push, CI or Console change.

## Result and boundaries

User explicitly authorized a Google-free local emulator mock profile while retaining release Google authentication. `mockDebug` installs as `com.aladdin.wobbrik.mock` / **Wobbrik Mock**, with separate application storage. Debug and release compile their own real-only `BuildVariantBackend`; the synthetic transport exists only in `src/mockDebug`.

Main bootstrap accepts a build-variant transport only when `BuildConfig.DEBUG` is true. Normal Firebase provider checks, auth listener and HTTP transport are retained for real variants. Mock supplies its own UID and a local-only token to the existing HTTP DTO parser, recovery owner, repositories, journal, local engine and pending-result reconciler. Firebase auto-initialization and INTERNET permission are removed from mock's merged manifest; its Google Services resource task is disabled because the isolated app has no Firebase registration. No emulator-detection heuristic or production authentication fallback.

Mock profile, attempts/checkpoints, history, operation receipts and finished buildings are persisted atomically together in separate local preferences. Journal and terminal intents use the existing app-private stores, isolated by application ID. Duplicate commands return stored receipts after recreation. Builds are synthetic, with fixed test balance and no real reward computation; buildings require ten floors. Profile editing and empty city/market/leaderboard/decorations reads are supported. Purchases/trading/building edits/deletion are unsupported and explicitly reject; this adapter is not a complete simulated server or final game-domain implementation.

AppBackend API/DTO/schema/domain/authority is unchanged. The transport never sends HTTP and mock results never enter the real wallet. Backend changes/tests were not needed for this isolated client test adapter.

## Validation

- JDK21/offline `:app:assembleMockDebug :app:assembleRelease :app:assembleDebug`: PASS, 1m29s. Release output is unsigned; no distribution signing/deploy performed.
- `:app:testMockDebugUnitTest :app:assembleMockDebug :app:lintMockDebug :app:testDebugUnitTest :app:lintDebug`: PASS, 27s at the earlier two-mock-test revision. After final mock INTERNET removal and integration-test additions, `:app:testMockDebugUnitTest :app:assembleMockDebug :app:lintMockDebug`: PASS, 2s. Final mock: 27 tests (including 3 adapter/integration tests), zero failures/errors/skips; real debug: 24 tests, zero failures/errors/skips.
- Tests cover persisted start/checkpoint/finish and inventory, retry deduplication after reopening, revision rejection, disk failure without advancing memory, unsupported mutation rejection, and production PendingResultReconciler + HTTP DTO parser + geometry + journal + durable intent acknowledgement through the synthetic backend. Test fixture method/namespace mistakes were corrected before the final PASS.
- Lint: zero errors; mock 41 / real debug 42 warnings, not claimed resolved.
- APK DEX inspection: `MockGameplayTransport` and `local-mock-profile` present in mock and absent from release. `aapt dump permissions` confirms mock package and no INTERNET permission.
- Local scoped diff review and `git diff --check`: PASS. No colors/layout/token changes made by this unit. Existing approved UI is reused; no new design generated.

## Runtime/render limits

Installation on the previously open `emulator-5554` was attempted, but ADB returned device not found; subsequent device inventory was empty. No emulator was launched. Mock startup, Google-free Home, real taps/physics and process relaunch have NOT been observed on a device; no screenshot/render PASS is claimed. This is a verification limit, not a mandatory live-check gate. Existing Home/City visual changes from other work are outside this unit's acceptance.

Usage: Android Studio **Build Variants → app → mockDebug**, then Run. Or from `android-billing`: `./gradlew :app:installMockDebug` on a user-started emulator. Choose `debug`/`release` for real Google authentication. See `android-billing/app/README.md`.
