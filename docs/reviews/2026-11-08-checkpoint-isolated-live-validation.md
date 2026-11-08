# Isolated checkpoint lost-response replay — 8 November 2026

Branch: `docs/checkpoint-isolated-live-validation`, based on `5d8de29a3763f64979bdb7642cd4e295c2af5042`. Documentation/evidence only; no production source changes, merge, push, deploy or CI.

## Environment and provenance

Read current handoff and PLAN §3.4. Original backend PID 42589, DB/TLS route and emulator-5554 were not changed. All device mutations used explicit `-s emulator-5582` (Pixel_6_34). Existing AppBackend owner checkout remained clean and untouched.

Prepared independent PostgreSQL 18 cluster in ignored `output/checkpoint-live-isolated/pgdata`, loopback port 55447, database `wobbrik_checkpoint`; migrations passed. Port 55443 was already occupied by Android Studio; startup failed without using that service, then a free port was selected. Separate backend port 18082 and TLS proxy 127.0.0.1:18445; only emulator-5582 reverse maps device tcp:18443 to host tcp:18445. Server binary binds wildcard by its existing implementation; TLS frontend and DB bind loopback. No remote deployment.

Ready immutable `25a1967` binary hash matched handoff, but real Firebase guest sync returned 403. That revision globally rejects anonymous providers and omits existing guest fix. Therefore actual gameplay proof uses immutable existing fix commit `79bfc123009b4428d45142904d5ac682810c9dd4`, whose parent is `25a1967`. Archived via `git archive` without modifying owner checkout; build `go build -trimpath -buildvcs=false -o ../server-guest ./cmd/server`, Go 1.27.3 darwin/arm64. Archive SHA-256 `40109e29e1b277eec0a21c6161acd711f02c03819fc5bd8c8cf83787768099b8`; binary SHA-256 `93dd3e00916c7a2968ee8692fc4acb260fd9e26c4d3d9ba071d437542a58e85b`. Manifest is ignored `guest-manifest.json`. No new backend contract/schema/source fix required. Existing guest fix review records its earlier tests; this unit did not rerun that suite or establish historical dirty binary provenance.

Installed existing final APK f5441a6, SHA-256 `e3c08906773e7ad8e091f156a98c56fda52f391bdd067bb55e4d9b794a894045`, with `install -r`, no app-data clear. Emulator was initially very slow: package installation stalled, System UI ANR, Wobbrik splash. Guest reboot, device reconnect and console pause/resume on 5582 preserved overlay; subsequent no-streaming install succeeded. Initial journal directory absent; no synthetic command, checkpoint or DB grant. Real Firebase guest sync/catalog succeeded after isolated guest-fix build. Wi-Fi/data enabled only on 5582.

## Measured scenario — PASS

Real UI started attempt `f73722ea-b54f-4985-9c7e-85a442c1d0a3` at sequence/floors 0, account revision 1. Saved repeatable-read read-only DB baseline and app-private journal before landing. Actual resume/drop produced one settled floor. TLS fault proxy forwarded real checkpoint, read backend HTTP 200 after commit, then closed client connection without response while marker existed. Proxy records request bodies only for checkpoint, never Authorization headers. Client showed connection-error screen without gameplay drop controls; screenshot viewed.

Accepted checkpoint operation `d93706b3-ac2d-40ae-a0a9-8c1974489dab`, expected_revision 1, sequence/floors 1. Request SHA-256 `578e17ec6e42cea2db3e83f1380f07d56c2caea1bb01e5b5134123e99d54ac8a`. Post-commit state: revision 2, balance 1, XP 1, operations 2 (start + checkpoint), wallet ledger 1, results 0, attempt rewards 0. Snapshot includes accepted engine and typed geometry data.

Saved pending AtomicFile journal; force-stopped app, saved again, removed only fault marker and relaunched with data retained. Pending journal bytes before/after force-stop identical: 743 bytes, SHA-256 `f6afb84a6f777ef3092d97e049cbea860c03b97ce2cd90c4248a6f0a4d88ee69`. Exactly two checkpoint requests recorded, both HTTP 200 from backend, byte-identical body. Decoded journal operation/revision/envelope equals both captured request bodies. First response was suppressed; second delivered normally. No new command or floor intervened.

Post-commit/pre-retry and post-retry/final snapshots are exactly equal across wobbrik_accounts, wobbrik_attempts, wobbrik_operations, wallets, wallet_ledger, wobbrik_progression, wobbrik_results and wobbrik_rewards. No extra revision, sequence/floor, balance/XP, operation receipt or ledger credit. Initial post-retry journal capture preceded final drain; final capture is 35-byte tombstone with pending=false. Home showed coins/XP 1 and return-to-attempt. Entered accepted one-floor attempt, viewed pause state, resumed without dropping and viewed actual one-floor game, then paused again. No second floor/finish started.

## Evidence and final state

Private ignored artifacts: `output/checkpoint-live-isolated/{guest-manifest.json,verification.json,live_proxy.py,live-requests.jsonl,before.json,post-commit.json,post-retry.json,final.json,journal-*.tar,lost-response.png,restored.png,ready-resumed.png}`. Programmatic assertions verify exact journal/wire equality, pending survival/tombstone and final DB equality. Screenshots actually viewed; no UI implementation/design acceptance implied.

Fault marker removed; isolated and original certificate-verified readyz both pass. Isolated server PID 8474 (execution session 48174), TLS PID 8484 (session 65268), PostgreSQL retained; emulator-5582 paused at accepted one-floor attempt, final journal empty. Original backend PID 42589 still unchanged; owner Git status clean. No suite/branch CI run; CI is main-only. Documentation final diff reviewed and whitespace check passed.

Bounded checkpoint response-loss → app process death → exact retry/reconciliation PASS. Full PLAN §3.4/stage acceptance remains open: old-version journals, account isolation/nonfree unlocks, Google linking, Room/outbox, physical-device and full visual acceptance. Do not advance stages or repeat this proof as a substitute for those checks.
