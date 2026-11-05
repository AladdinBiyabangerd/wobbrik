# Firebase auth bootstrap — 5 November 2026

- Branch: `feat/firebase-auth-bootstrap`.
- Reviewed diff: app Gradle dependencies/configuration, manifest, new
  `WobbrikApplication.kt`, adapter README and host contract/context updates.
- One process-owned recovery owner/journal and auth listener; no Activity writer.
  Firebase initial/sign-in/sign-out events create recovery epochs, including same
  UID callbacks. Tokens are not persisted and current user is checked around await;
  AccountSessions/backend retain epoch checks. Process foreground retries recovery.
- Configuration is explicit: existing google-services configuration plus
  `wobbrik.backendOrigin`. Missing config exposes an unavailable bootstrap state;
  nonempty invalid HTTPS origin fails explicitly. No endpoint/account is invented.
- Review findings: app needed INTERNET permission and explicit coroutine/core/Android
  dependencies; included. Firebase token refresh uses getIdToken(false), allowing SDK
  expiry refresh without treating routine token refresh as a new login.
- Backend contract inspected: existing bearer-authenticated recovery endpoints used;
  no API/DTO, rule, schema or backend changes required. No second wallet introduced.
- Restart/abandon and online engine input remain unbound. Visible app is still offline
  practice; no visual changes. No UI render needed for this nonvisual unit.
- Branch checks: `git diff --check` passed. CI deliberately not run on branch.
- Post-merge CI: pending.
- Limitations: no supplied real backend origin or device sign-in; live token, account
  switch, foreground and process-kill verification not performed. Existing core owner
  regressions cover epoch isolation, recovery failures and retained journal behavior.
