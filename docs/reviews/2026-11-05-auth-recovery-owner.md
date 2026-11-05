# Auth recovery owner review

- Branch: feat/auth-recovery-tests; reviewed main a14ac15 owner and cancellation
  guard plus the new focused AuthenticatedRecoveryOwnerTest diff.
- Findings: initial owner had no regression tests. Added delayed old-account,
  same-UID epoch, foreground coalescing/retry, prior snapshot hiding, pending
  failure/logout/account isolation and cancellation-after-receipt coverage.
- Ownership: owner constructs one private coordinator; callers must retain one
  owner/journal at application scope. Host recreation test verifies reuse of that
  same owner, not Android Activity recreation instrumentation.
- Firebase inspection: main 403569b/83ce44b adds Crashlytics, Analytics and Remote
  Config; no Firebase Auth dependency, auth listener or authenticated host bootstrap.
  No new Firebase project/configuration or sign-in flow was invented.
- Backend: no API, DTO, schema or rule change required by this recovery-only unit.
- Branch checks: source/diff review and git diff --check; no branch CI.
- CI: pending post-merge ./scripts/local-ci.sh on main.
- Limits: Android singleton/auth wiring, real device recreation/process death,
  engine/input binding and full online gameplay remain open. Restart/abandon is
  unbound. No visual changes were made.
