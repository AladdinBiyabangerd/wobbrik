# Balance/rescue and Android baseline — 4 November 2026

- Branch: feat/game-balance-rescue.
- Reviewed diff: Balance.kt, PracticeEngine.kt, BalanceTest.kt, billing Gradle configuration, module READMEs, Kotlin plan and context handoff. Pre-existing untracked responsive rule files excluded.
- Rules version: practice-balance-v1, provisional arcade parameters. Landing-relative offsets change persistent lean; danger/stability use hysteresis. Only landing can rescue or topple. Cosmetic damped sway cannot mutate danger or end an attempt. Early sensitivity is lower; speed/sensitivity growth caps at floor 45.
- Findings/fixes: preserved sway velocity across pause; restart clears it. Initial test incorrectly expected danger inside the forgiving first-five range; replaced with a reachable later-floor scenario. Added actual integrated overlap/topple test and pause/restart regression.
- Backend assessment: offline practice only; no API/DTO/schema, wallet, XP, persistent achievement, checkpoint or ownership mutations. Rescue/perfect counters are attempt-local and do not grant rewards. No AppBackend change needed for this unit; online adapter compatibility not claimed.
- SDK: user-selected minSdk24 (Android 7.0), targetSdk36 (Android 16), compileSdk36 in billing library; future app host must repeat the policy. No application module/APK yet.
- Validation: local CI passed with 11 engine tests, billing core tests, Android billing debug assembly and lint. Final documentation and post-merge CI are rerun; results reported in chat.
- AGP 8.13 warns that library defaultConfig.targetSdk is deprecated for AGP 9; application target remains a separate future host setting.
- Limits: no Compose render, camera, game APK, device performance/balance measurements, process restore or online rewards. Sway is an engine signal awaiting renderer integration, not a checked animation.
