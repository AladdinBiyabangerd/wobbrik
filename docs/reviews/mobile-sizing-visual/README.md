# Mobile navbar and compact sizing evidence

The user approved `home-navbar-v3-preview.png` on 13 November 2026 and explicitly requested implementation, continued mobile compact sizing, and a local commit. The earlier standalone sizing specimen was rejected. `full-pages-v3.png` records the full-page proposal; production rendering uses real screen composables and explicitly synthetic host fixtures, never a shipped account adapter.

Production sources: `MobileHomeScreen.kt`, `MobileUiSizing.kt`, `GameTokens.kt`, `HomeScreen.kt`, `InventoryScreen.kt`, `PartsScreen.kt`, `FinishCosmeticScreen.kt`, `CityPurchaseChrome.kt`, `CityPurchasePanel.kt`, `DecorationShopScreen.kt`, `LandShopScreen.kt`, `OnlineGame.kt`.

Run from `android-billing/`, Java21, cached native host dependencies (same versions as the existing project review harness; no production build pin changes):

```sh
./gradlew --offline -I ../docs/reviews/mobile-sizing-visual/render/render.init.gradle :app:testDebugUnitTest \
  --tests '*HomeProductionRenderTest' --tests '*CompactFlowRenderTest' \
  --tests '*CompactPurchaseRenderTest' --tests '*CompactPartsPreviewRenderTest'
```

20 tests: Home6, compact read/navigation screens3, decoration/land6, existing part preview/read regression5. The host init script is opt-in and not used by normal builds/CI. It uses cached Robolectric4.14.1/Compose1.7.6/API34 native graphics, density3. `/private/tmp/wobbrik-android-jars` must contain the already cached android-all-instrumented jars as in the earlier city purchase harness.

`production-contact-{360-font1,390-font1,320-font2}.png` groups inventory, parts, part detail, building detail, part edit, account and achievements. `production-home-*.png` covers those sizes, disabled recovery/offline, a full long balance/XP, and simulated top24dp/bottom48dp reserved space with font2. The latter is a constrained-window check, not real device inset certification. `purchase-*` and `picker-*` outputs are adapted from the existing render fixtures, scoped here so historical screenshots are not overwritten. Full PNGs can be regenerated; representative contact sheets and the approved navbar are retained in git.

Play tests invoke the production HomeScreen callback exactly once per tap and verify center alignment, availability/recovery gating, all four destinations/account callbacks, and minimum48dp nav targets at the checked sizes. The unchanged OnlineGame callback connects that event to existing AcceptedAttemptCommands.start / host.enter / game navigation and existing discard-before-start reconciliation. No live Firebase/server start was performed. Tablet and landscape compositions are retained, not redesigned or certified here. Device/API24 runtime, TalkBack/GPU and exhaustive long-content/localization combinations remain outside this evidence.
