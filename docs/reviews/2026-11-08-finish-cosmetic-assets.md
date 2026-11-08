# Finish cosmetic asset preparation

- Branch: `feat/finish-cosmetics`, based on `6e22f69`, preserving `e0af061` restore fix. No merge, push, deployment or CI.
- Scope: approved board and five generated atlas candidates, prompts and intended mapping to 68 server cosmetic IDs. No Kotlin or backend implementation changes.
- User approved preparation of missing assets in the approved style; original UI approval remains valid. Live tests remain user-owned; shared emulator untouched.
- Reviewed: approved board, GameTokens, host/command/recovery/journal flow, GameplayHttpBackend, RecoveryDtos, backend catalog and finish validation. Existing transport does not load catalog. Host automatically finishes with hard-coded base_6 IDs. Backend already validates five categories and free/account-owned parts; inspected contract does not require a backend change for catalog selection. No server test PASS inferred.
- Art findings: five outputs visually inspected; intended categories/material distinctions present. Translucent halos, variable padding and incompatible projection between facade and roof require crop/composition validation before production use. Manifest maps intended row-major cells, not final pixel bounds. No UI implementation or phone/tablet runtime parity claimed.
- Checks: mapping script PASS — 68 unique IDs, exact cosmetic ID/category set from backend catalog, atlas paths exist, decorations excluded. `sips` confirms all five PNGs are 1254×1254 with alpha. Pillow-based metadata probe could not run (`PIL` unavailable); native `sips` used instead. No image manipulation performed. No build/test suite needed for this asset-only diff; no live tests executed.
- Remaining: final asset crop/anchor/alpha QA; authenticated catalog loading and stale-epoch guards; durable selection/terminal intent including no-added-floor miss; selection UI using approved tokens; explicit confirm instead of automatic defaults; unchanged journal exact retry; focused tests/build; isolated phone/tablet render comparison and accessibility/adaptive checks.

## Subsequent integration

This preparation record is historical. Crop/alpha QA, the user-authorized roof
camera variant, lossless packaging and runtime integration are documented in
`2026-11-08-finish-cosmetics-integration.md`. Original source candidates are kept.
