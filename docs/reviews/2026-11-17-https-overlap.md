# AccountViews / MarketScreen real HTTPS cancellation — 17 November 2026

Branch `fix/menu-inventory-latency`, HEAD `083f870`; current dirty implementation, PLAN §1.1. Measurement only. Production source, backend, PLAN, baseline profiles and previous reviews/tests preserved. Initial dirty/untracked SHA-256 snapshot matched before this review/handoff update. No commit/merge/push/CI/emulator/deployment.

## Real transport and measurement boundary

Opt-in `https-overlap/HttpsOverlapTest.kt` uses unchanged **UrlConnectionGameplayTransport → actual HttpsURLConnection → real loopback TLS socket** to `https_fixture.py` (Python ThreadingHTTPServer + SSL). Server events confirm **TLSv1.3**. This replaces the previous blocking transport model with real HTTPS I/O. Market test mounts production MarketScreen/MobileMenuShell, actual AccountViews/ReadOnlyGameCache/MarketRepository. Direct cancellation tests also call AccountViews, not only a primitive transport request. Account bootstrap/catalog and12-listing data remain fixtures; this is not a deployed AppBackend/device latency benchmark.

A generated localhost certificate is trusted only in the isolated test JVM, using a certificate-backed trust store. Hostname verification stays enabled (localhost/IP SAN); default SSLSocketFactory is restored after each test. Fake token only; server binds127.0.0.1. No credentials or private key are retained in the repository. Disposable server was stopped after final collection.

Client monotonic timestamps bracket cancellation notification (`Job.invokeOnCompletion(onCancelling=true)`) and exit from the real transport call, after its finally/disconnect path. `cancelToExitMs` is the lifetime of outstanding client request work after cancellation, not a packet-level timestamp for the last TCP FIN. Server monotonic timestamps independently show received requests, concurrent same-path handlers, body-prefix/write events. The clocks are not subtracted across processes. Header/body release delays are intentionally controlled, not naturally observed backend latency.

## Final measurements

- **Headers withheld:** cancel after server receives request, retain response for500ms; client request exits **515ms after cancellation** (661ms total), JobCancellationException. Full1304-byte body is written by server after cancellation.
- **Body stalled:** server flushes headers and first body byte, then cancellation; retain remaining body for750ms; client exits **756ms after cancellation** (767ms total), JobCancellationException. Server writes the full1304-byte body.
- **No response release:** cancel after request arrives, wait for unchanged production8000ms read timeout. Request exits **7996ms after cancellation** (8008ms total), SocketTimeoutException. Cancellation did not interrupt the blocking header read.
- **Real Market public→own→public:** first public and own requests remain blocked while final public request reaches the HTTPS server. Server observes **1 identical-public-path overlap**. Old public exits **563ms after cancellation** (961ms total); old own exits **534ms after cancellation** (555ms total), both LeftCompositionCancellationException. Server handler overlap from second public arrival to first public body write is **519.145ms**. This duration was induced by holding release for500ms, not a deployment percentile.

Market total: **16 server-received HTTPS requests**, one page +12details +sync for the final successful query and2cancelled page requests. **11384 server-written JSON body bytes** = final8776 +2×1304 cancelled page bodies. This is server write accounting, not client-delivered/application-published bytes or wire bytes; TLS/headers excluded. Final page is displayed, both stale requests remain cancelled, zero commands. Direct cancelled AccountViews cases publish neither onLocal nor onFresh and leave the view cache empty. No inflight coalescing or behavior change was implemented.

## Findings and limits

Cancellation invalidates publication but **does not promptly stop active HTTPS read work**. Delayed response release or socket timeout determines when the transport exits. Real socket evidence now confirms the previously modelled duplicate-public request during a controlled rapid tab return. It does not establish overlap frequency on user devices, deployment2+s/10+s timings, heap/durable disk cost, connect/DNS/TLS-handshake cancellation, or the kernel's exact socket-close instant. Desktop JBR/Robolectric HTTPS implementation was exercised; Android device/libcore transport was not.

Additional observed edge: when the cancelled request ends with SocketTimeoutException, AccountViews' IOException fallback still attempts cache replay. With no matching cache this throws `IllegalArgumentException: View not cached` instead of CancellationException. The test records this terminal exception only after confirming the job is cancelled; it does not count an active-job failure as success. No publication/write/command occurred. Preserving cancellation before fallback is a concrete follow-up; no production fix was authorized in this measurement unit.

Backend/API/domain/schema unchanged. Existing marketplace/sync contract from prior baseline remains applicable; local HTTPS fixture does not change the server contract. Backend tests unnecessary/not run.

## Checks, evidence and reproduction

Final **4/4host PASS**,0failure/error/skip,16.889s test time, Gradle20s. Exact request/cancellation counts, blocked request still active after hold, minimum cancellation lifetime, final screen data and no cancelled cache writes/publication checked. `results.xml` and `server-events.jsonl` in `https-overlap/`; final log `/private/tmp/wobbrik-https/test-final.log`. Previous fully passing run:4/4,18.412s test/Gradle25s (public571ms, own528ms, header516ms, body755ms, timeout8073ms); final assertions added no-cache-write check and unused fixture code was removed.

`git diff --check`, Python AST parse and `sh -n run.sh` PASS. Scoped local review: opt-in harness + evidence only, production files unchanged, no global certificate trust or dependency pin change. No visual changes; no new screenshot/contrast claim. No app-wide build/lint/CI rerun. Initial local bind was sandbox-blocked; permitted escalation enabled only loopback fixture and focused Gradle execution.

Initial4test run had2failures: fixture emitted string geometry ticks (fixed to integers), and the timeout's cache-miss exception propagated from a cancelled test job (now explicitly recorded while asserting cancellation/no publication). A repeat before fixture generation isolation was added also failed; stale run counters were not accepted as evidence. Per-setup generation keys and current-run server-log boundary now isolate repeats. These failures are not PASS.

Opt-in reproduction from repository root: `sh docs/reviews/https-overlap/run.sh`. The script regenerates temporary certificate, starts/stops fixture and runs only the focused host class using cached dependencies. Do not run concurrent copies (fixed temporary directory). The constituent final commands were executed; wrapper script itself received shell syntax validation. Requires permission for Gradle cache and local sockets; no remote network service.
