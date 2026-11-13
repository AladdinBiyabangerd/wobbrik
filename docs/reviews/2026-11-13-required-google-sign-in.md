# Required Google sign-in — 13 November 2026

Branch: `feat/required-google-sign-in`; reviewed implementation diff against `3ce3ac7`. Existing owner changes in product plans, context, city proposals and release output are preserved and excluded from the implementation commit.

## Result and local review

- Google is required before recovery/game navigation. Google-backed Firebase sessions persist; UID-keyed authenticated composition resets account-local UI on account change. Token loads and recovery listener admit only Google-backed sessions. Firebase listener still owns auth epochs; backend remains wallet/progression authority.
- Removed anonymous creation, anonymous Session flag/copy, GoogleGuestLink, guest linking/collision messages and the corresponding debug guest fixture. No fallback, account merge or new wallet. User clarified there are no legacy production accounts; no migration implementation or test-data deletion.
- `GoogleSignIn` only obtains a Google credential; Firebase sign-in lives in the application, presentation/request state in GoogleEntry, native adaptive layout in GoogleEntryScreen. Cancel/no-credential/error remain at entry; no fabricated Ready. Web OAuth client exists in local Firebase config (presence only checked, no secrets printed). Firebase Console was not changed.
- Approval: user “okeydir” for docs/design/google-entry-v1/approved-phone.png. Runtime art displays exact approved source rectangles, including builder/island, logo, ornaments and Google mark. A generated extraction was rejected for altering the artwork. Native text/button use GameTokens. Review corrected a compile receiver error and a missing-config state that otherwise showed an indefinite sign-in spinner.
- AppBackend counterpart: `/private/tmp/wobbrik-required-google-backend`, `fix/wobbrik-required-google-auth`, base `bb3072a`. Seven formerly exceptional game routes now use shared permanent-account authorization; obsolete guest verifier/policy removed. Shared default verification unchanged. No DTO/schema/migration/rules version/economy change. Old anonymous clients receive 403 and must upgrade/sign in. Backend contract and rejection tests updated.

## Checks

- `:app:assembleDebug :app:lintDebug :core:test :engine:test`: PASS; unchanged core/engine tasks reused valid Gradle results (UP-TO-DATE). Final app assemble/lint repeated after last source change: PASS, 20s. This is focused verification, not local-ci.sh.
- Explicit Text/Muted/Danger on Background and Background on Text contrast: 16.57/10.48/10.67/16.57, PASS 4.5.
- Backend `go vet ./...`, `go build ./cmd/server`: PASS. Full `go test -race ./...` with disposable real PostgreSQL: PASS, including platform and Wobbrik. Focused signed-token verification/permanent-account integration repeated: PASS (2.220s).
- Scoped source search: no anonymous bootstrap, guest linking or guest allowlist remains in Android production code/backend Go code. Anonymous denial checks remain intentionally. Local diff review and diff --check PASS.
- No CI, merge, remote push, deploy, Console change, real Google sign-in or live backend request.

## Visual and device limits

Isolated read-only emulator 5588 was started, debug APK installed, but no valid screenshot was obtained (boot/install/activity sequence did not reach a captured frame). User then explicitly said not to open an emulator every time; isolated emulator was stopped and no further render attempted. Shared 5586 was not controlled. Do not automatically reopen an emulator for continuation.

All runtime visual cases remain UNVERIFIED: small/normal phone portrait and landscape, tablet portrait/landscape, font 1.0/2.0, insets, keyboard and accessibility. Native type/spacing, source-rectangle background seams and adaptive composition have not been visually compared with the approved board. Concept approval and a passing build are not runtime visual acceptance. Live credential-picker cancel/retry, sign-in, relaunch/session retention and account changes also remain unverified. Keep this unit on its feature branches pending any requested device/visual review and merge decision; do not advance stage four.

## Validation continuation — no emulator launch

- Rechecked `d972775` auth entry, credential cancellation/error handling, Google-provider gate, token UID/object guard, Firebase listener recovery epoch and UID-keyed composition. Source inspection only; no new runtime PASS.
- `adb devices -l`: only previously shared `emulator-5586`; no physical device. No emulator launched, activity opened, installation, account change or device data mutation performed. ADB inventory required sandbox escalation for its local socket and succeeded.
- Existing generated debug configuration has a non-empty backend origin. Explicit Google web-client override is empty; resource fallback remains implemented. This does not verify the installed APK, endpoint reachability, OAuth configuration or real Google credentials.
- Wobbrik `git diff --check` PASS; backend feature worktree remains clean. Existing build/test evidence above was not rerun because source is unchanged. No CI/merge/push/deploy/Console changes.
- Await selected device: physical phone, or explicit reservation of existing 5586. Then validate picker cancel/retry, successful Google → backend recovery, force-stop/relaunch session retention and controlled account isolation without clearing data or resetting attempts. All visual/device acceptance remains open.

## Superseding user decision — live checks

User explicitly instructed “canli yoxlama hec vaxt teleb etme” (never require live validation). Device selection/reservation and live-test requests are withdrawn; live checks are not a mandatory completion/merge gate. Do not auto-launch an emulator or request user-run live results. Existing runtime/visual cases remain unverified evidence limits. No new CI/merge/push/deploy/Console authorization is implied.

## Code-validation completion — 13 November 2026

- Scope: Android `d972775` versus `3ce3ac7`, backend `47cf75e` versus `bb3072a`; source unchanged. Reviewed Google credential type/non-empty token and client-ID fallback, cancel/error/retry paths, required session gate, token UID/object guard, auth epoch invalidation, UID-keyed UI, recovery cache/journal isolation and removal of guest entry points. No additional actionable defect found. No UI changes or rendered-visual acceptance claimed.
- Fresh Android `./gradlew :app:assembleDebug :app:lintDebug :core:test :engine:test --rerun-tasks`: PASS, 34s, all 103 actionable tasks executed. Core 70 and engine 62 tests, zero failures/errors/skips. Lint: zero errors, 17 warnings; existing MainActivity LocalLifecycleOwner deprecation also reported during compilation. Warnings are not claimed resolved by this auth review.
- Backend fresh `go test -race ./internal/platform -run '^TestFirebaseTokens$' -count=1 -v`: PASS, 1.888s. This is a local signed-token unit test, not live Firebase or database integration. Earlier full real-disposable-PostgreSQL suite remains the integration evidence; unchanged backend required no DTO/schema/economy/migration changes or further implementation.
- Recovery regressions in the rerun cover delayed old-account/catalog responses, same-UID re-login epochs, synchronous invalidation, logout/pending-command preservation and canceled receipts. These verify core behavior, not real Firebase listener delivery or real credential-picker behavior.
- Both repositories' diff checks PASS; backend worktree clean. Owner edits preserved. No emulator/ADB/device use in this completion, no live-test request, CI, merge, push, deploy or Console action.
- Result: remaining code checks complete. Runtime/visual behavior remains unverified evidence only; per user instruction it is not a requested or required gate. Feature branches remain unmerged pending separate merge authorization.

## Authorized main integration

User authorized merge/push in both repositories and Wobbrik branch/worktree cleanup. Auth merged preserving history as 09f3b49; only current-task handoff had content conflicts, resolved preserving both records. Main focused assembleDebug/lintDebug/core:test/engine:test PASS (1m9s, 100 tasks: 22 executed, 78 up-to-date). This clean main worktree has no local Firebase config, so this build verifies the supported config-absent checkout; configured feature build was separately verified above. No emulator/live test or local-ci.sh (existing explicit no-CI decision). Existing owner edits remain separate and are to be archived before cleanup.
