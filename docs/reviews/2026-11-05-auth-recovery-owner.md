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
- Initial main CI: 40 core tests, four owner test failures from assertions before
  queued Unconfined launches ran inside runBlocking. Fix branch
  fix/recovery-test-scheduling explicitly yields before observing asynchronous work;
  reviewed scheduling-only diff and whitespace. Repeat main CI pending.
- Limits: Android singleton/auth wiring, real device recreation/process death,
  engine/input binding and full online gameplay remain open. Restart/abandon is
  unbound. No visual changes were made.

- Repeat CI still failed: further review found fixture backend methods recursively
  resolved themselves instead of injected lambdas. fix/recovery-test-backend
  qualifies both calls with this@Fixture; reviewed exact two-call fix.
