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
