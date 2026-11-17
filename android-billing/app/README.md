# Android app variants

Run Gradle from `android-billing/` (JDK 21, existing project pins).

- `debug`: `:app:installDebug`, package `com.aladdin.wobbrik`. Real Firebase Google sign-in and configured AppBackend are required.
- `release`: `:app:assembleRelease`. Google sign-in remains required; mock implementation is not compiled into this APK. Signing/distribution is a separate operation.
- `mockDebug`: `:app:installMockDebug`, package `com.aladdin.wobbrik.mock`, launcher name **Wobbrik Mock**. Starts with a synthetic local profile without Firebase/Google/backend configuration. In Android Studio select **app → mockDebug** in **Build Variants**, then Run on the emulator.

Mock installs beside the real app and has separate Android storage. It uses the existing production navigation, local game engine, command journal and result reconciler with a mock transport. Profile edits, attempts, receipts and completed buildings are stored locally; balance is synthetic and rewards are not real economy calculations. Inventory contains completed buildings with at least ten floors; city starts empty. Purchases and market mutations are unsupported. This is a local test adapter, not the accepted server domain or a release alternative.

The mock manifest removes Firebase auto-initialization and the INTERNET permission. Neither mock credentials nor results are sent to AppBackend. Relaunch preserves mock data; clearing **Wobbrik Mock** app storage resets only that profile.

Focused checks: `:app:testMockDebugUnitTest :app:assembleMockDebug :app:lintMockDebug`. CI is separate and runs only when explicitly requested, locally on main, according to repository instructions.
