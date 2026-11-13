# One-off host render evidence

These render the production `DecorationShopContent` / `LandShopContent` functions with explicitly synthetic server snapshots. They are not backend, Firebase, device, emulator, or complete accessibility evidence. No demo adapter is shipped.

Robolectric native graphics API 34; Compose 1.7.6, matching the application. The one-off init script uses already-cached Robolectric 4.14.1 and does not change project build files, stack pins, or CI. No emulator is started. Android supports host screenshot rendering through Robolectric native graphics: https://developer.android.com/training/testing/ui-tests/screenshot

From `android-billing/`, with Java 21:

```sh
./gradlew --offline -I ../docs/reviews/city-purchases-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*PurchaseRenderTest'
```

Prerequisite on this host: `/private/tmp/wobbrik-android-jars/` contains symlinks to already-downloaded `android-all-instrumented-*.jar` files from `~/.m2/repository/org/robolectric/android-all-instrumented/`. No download is performed. The fixture currently writes to this workspace's absolute review path; update that output path for another checkout.

Six cases: decoration/land at 360×640dp, fontScale 1 and 2, plus 390×844dp fontScale 1; density 3. Each captures list and confirmation, and asserts that Al/Ləğv et are displayed. Native `decorView.draw(Canvas(bitmap))` capture is used because PixelCopy/captureToImage timed out on the initial experiment. Status/navigation inset combinations and TalkBack are not certified by this harness.

Read-only artwork inspection:

```sh
python scripts/check-city-purchase-art.py
```

Requires Pillow; original pixels are never rewritten. `decor-crops-dark-light.png` composites the exact production crop metadata on token Surface/Text backgrounds. `art-report.json` records native pixel/density limits and source SHA-256.

`land-thumbnail-candidate.png` was explicitly approved by the user on 13 November 2026 and copied without pixel modification to app assets as `city/land-thumbnail-v2.png`. It replaces the original 153×141px opaque crop for purchase previews; the original board remains unchanged. `land-cutout-dark-light.png` and the land section of `art-report.json` verify the active cutout transparency/bounds.
