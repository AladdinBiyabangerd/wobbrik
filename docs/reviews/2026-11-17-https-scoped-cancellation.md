# Scoped HTTPS cancellation — 17 November 2026

Branch `fix/https-scoped-cancellation`, final scoped diff against `85307bf` (original work began against `8fd0bc6`). Scope: PLAN §1.1 network work during menu changes. No UI/backend/API/schema/domain change; no merge/push/deploy/CI/emulator.

## Change and local review

`UrlConnectionGameplayTransport` installs a request-owned delegating SSLSocketFactory, registering the underlying raw socket before TLS layering. A cancelling coroutine closes only sockets registered by that request. Closing raw TCP first interrupts an SSL read without waiting for HttpsURLConnection's disconnect/read lock; the existing finally still disconnects. Registration after cancellation closes/rejects the late socket. The cancellation handle is disposed on exit. IOException is converted back to coroutine cancellation when the context is inactive, preserving AccountViews' existing cancellation-before-offline-fallback guard.

Redirect refusal, hostname verification, certificate trust, request/response limits, 8s timeouts, bearer and session checks remain. No process/global transport cancellation registry. Local review checked socket-registration/cancel race, handle lifetime, per-request ownership and cache/session publication boundaries. The factory identity is now per request, which can reduce HTTPS connection-pool reuse; no pooling/performance improvement is claimed. DNS/connect and factory implementations that block before returning/registering a socket remain timeout-bound and were not measured.

## Real TLS before/after

The opt-in existing fixture runs actual production transport → HttpsURLConnection → loopback TLS, trusting only its generated certificate in the isolated test JVM. Hostname verification remains enabled; no trust-all implementation.

Previous unchanged transport: headers 515ms, body 756ms with controlled delayed release; no release 7996ms until timeout (previous review). Final changed transport: headers 7ms, body <1ms, no-release <1ms cancellation-to-transport-exit. Market public→own→public stale calls also exit <1ms while the final page completes. These are local host observations, not device/deployment percentiles or TCP packet timing.

Final **5/5 TLS tests PASS**, 6.461s test / 10s Gradle. Tests assert <1500ms exit before fixture response release, CancellationException, no cancelled cache writes/onLocal/onFresh, final market page and zero mutations. Added same-URL concurrent test cancels one request while the other remains blocked, then releases the second and checks response-200. Server handler overlap may persist until fixture release; socket cancellation does not promise server-side work cancellation.

Evidence: `https-overlap/cancellation-results.xml`, `cancellation-server-events.jsonl`. Reproduce: `sh docs/reviews/https-overlap/run.sh` (no concurrent copies). Earlier measurement assertions were updated to regression expectations; historical `results.xml` remains unchanged. First fixture start was sandbox-denied, then authorized loopback/Gradle execution succeeded. Initial 4-test changed-transport run also passed; final run adds isolation coverage.

Normal regression run: core147 and AccountViews14 tests PASS (Gradle7s), including epoch/cache/callback cancellation coverage; production and test Kotlin compilation passed. `git diff --check` PASS. Logs `/private/tmp/wobbrik-https/cancellation-{final,regressions}.log`. No lint/full app build rerun, Android device/libcore, DNS/connect/handshake cancellation, upload cancellation or runtime performance benchmark. No visual change/render claim. Backend contract unchanged; backend tests not required/run for this transport-only unit.

## Final continuation review

Fresh opt-in TLS rerun: 5/5 PASS, zero skipped, 7.45s test / 11s Gradle; headers cancellation 10ms, body/no-release/same-path and replaced market reads <1ms. Updated evidence XML and server events. Initial invocation was UP-TO-DATE and is not counted as a fresh execution. Opt-in init script now disables test up-to-date reuse because the external loopback origin/certificate/gates change each run. No production transport edits were needed during this final review. City paired commits were reviewed separately; AppBackend market+City were merged/pushed to remote main at 838c0c7 by explicit user instruction, then its persistent feat/wobbrik was synchronized.

Final normal `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS, 28s; core152/app37, zero failures/errors/skips. No CI. Remaining Android/libcore/DNS/connect/handshake/upload/pooling limitations above still apply.
