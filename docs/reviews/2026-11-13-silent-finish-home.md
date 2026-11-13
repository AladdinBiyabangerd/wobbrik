# Silent finish home — 13 noyabr 2026

- Branch: `fix/navigation-consistency`; reviewed diff: this unit's GameNavigation.kt, OnlineGame.kt and GameNavigationTest.kt against the preceding branch HEAD.
- User approved retaining the existing Home appearance during automatic terminal checkpoint/finish; no new layout, assets, tokens or palette.
- With an account-scoped accepted cache, Recovering renders the existing Home values without loading text, spinner or refresh panel. Initial recovery without a snapshot still loads normally; sign-out and errors are not hidden.
- Terminal choosingFinish no longer renders the game scene between checkpoint Ready and finish dispatch. Storage failures retain the existing retry scene. The route stays game until reconciliation, preserving terminal host ownership and durable retry; display falls back to canonical Home.
- Review finding fixed: intermediate terminal Ready must not enable Play or briefly label it Cəhdə qayıt. Home keeps Oyna disabled until automatic finish completes. Cached data never grants command permission.
- Server result remains authoritative: existing submit → sync → catalog → Ready publication updates the displayed snapshot. No optimistic wallet/XP, extra requests, timers, polling or network/cache contract changes. Existing frame polling is unchanged; no performance benchmark was run.
- AppBackend changes are unnecessary: API/DTO, rules, revision/dedup and journal behavior are unchanged. Backend tests were not run.
- Checks: `:app:testDebugUnitTest :core:test :engine:test :app:assembleDebug :app:lintDebug` PASS (core/engine tests up-to-date); `git diff --check` PASS. Added policy coverage for accepted-cache recovery versus initial load, signed out, null and error states. Local diff review completed.
- CI not requested/run. No merge/push. Owner's untracked app/release files untouched.
- Android runtime/render not checked; no emulator opened. Phone/tablet, orientation, font 1.0/2.0, insets, keyboard and accessibility render comparison remain unverified. No visual PASS claimed. Network failure still uses the existing explicit error/retry presentation.
