# Inventory city paging / detail payload — 18 November 2026

Android branch `fix/inventory-city-detail-costs`, reviewed base `efbd9e9` plus this scoped diff. AppBackend persistent `feat/wobbrik`, base `1ef8b32` plus paired diff. Owner release baseline-profile binaries remain untouched and excluded. No main merge, CI, push, deploy or emulator.

## Proven causes and changes

Inventory optional membership called plain City with limit20, despite its existing supported limit100. Each HTTP page repeats authenticated middleware, a transaction/account share lock, complete-city population aggregation, parcel read and membership query. Capture adds one final sync for the entire bundle, not one per page. InventoryRepository also copied the entire accumulated map on every page: for1001entries/20pagesize this recopies25500 prior entries, in addition to incoming inserts. The accumulator is now private to one refresh, populated once and copied once on return; revision/population/duplicate/cursor checks remain. This removes quadratic accumulation, not the need to read all city membership.

Inventory now requests plain City100. Scene include=details stays20 and its parser retains the20default; inventory explicitly opts into100. The path allowlist admits only plain100 with the same bounded cursor. Existing complete20-page cache bundles replay their exact cursor chain through the100-cap parser; no pages are skipped, no live/cache union is made. UID/login epoch, accepted account revision, final sync and mutation freshness gates remain unchanged.

Building detail requested/stored the full unused opaque engine snapshot. AppBackend additive `GET /buildings/{id}?view=display` selects NULL::jsonb in the same owner/non-deleted SQL, preserving every metadata/parts/geometry field. No view keeps the legacy full body; unknown/empty/repeated view is400. Android requests display; old servers ignore the query and still work. Capture strips only the unused detail snapshot before persistence; exact legacy detail bundles replay under the additive query. Shared scene fallback and parts fresh detail also benefit; their regression fixtures were updated. No schema, economy, rule, auth, revision, dedup or writer changes.

## Device-free evidence

Real disposable PostgreSQL18 at loopback55475/wobbrik_inventory_test, httptest HTTP/auth middleware with synthetic identity; no real account or Firebase login.1001buildings are created by the existing domain fixture, then inserted into owner membership. Before/after traverse identical IDs/revisions/population and include the one final sync.

- City limit20→100: **52→12HTTP**, **849→209SQL**, **107864→94388response UTF-8 bytes**. One race-enabled local sample **7.160s→1.561s**, including decode and final sync.51→11City pages; SQL savings640 are repeated page work. Timing is host/race/fixture-specific, not production RTT, percentile or the user's reported10+s.
- Typed10floor detail with a valid near16KiB opaque checkpoint: **16758→542JSON bytes**; display11SQL/1HTTP,8.44ms local sample. No claimed detail SQL-count reduction or paired detail latency comparison; savings come from not selecting/serializing/transferring the checkpoint. Full/display models compare equal after nulling only snapshot, accepted geometry included.
- Kotlin1001membership regression completes in11reads, retains final entry and null cursor. Mixed revision/population, duplicate/cursor, same-UID relogin and cache UID/revision protections remain. Legacy20-page bundle replays every captured cursor; missing cursor fails. New/old-server detail cache/replay and wrong-ID isolation pass.

## Checks and local review

Android core162/162 and app41/41 unit PASS; assembleDebug/lintDebug PASS35s. Initial all-core run failed6fixtures that matched the old literal detail path or treated query text as part of the ID; corrected those fixtures, reran the full checks successfully. These initial failures are not PASS. Earlier focused InventoryReads/Payload tests also passed.

Backend focused real PostgreSQL/race probes PASS16.639s. Full `go test -race ./... -count=1` PASS, platform42.509s; `go vet ./...` and server build PASS. Initial probe connection failed because pg_ctl used default5432; restarted the disposable instance explicitly on55475, then ran focused and full suites. Database stopped after validation. No production database used.

Scoped local review: bounded page paths, no scene page expansion, private linear accumulator, complete legacy cursor replay, exact detail-ID alias, account/revision bracket, static SQL projection and unchanged owner/deleted predicates checked. No further issue found. Diffcheck PASS. Evidence: `inventory-city-detail/{backend-focused,backend-race,mobile-checks}.txt`.

## Limits / remaining work

Plain City still reads all membership: CPU/bytes/cache grow linearly, population aggregation repeats per page and one final sync remains. Very large city or accepted geometry may still exceed existing transport/cache bounds. No device/runtime/frame/GPU/battery/production contention/distribution or authenticated Android→Go E2E measurement. UI/art/layout did not change; no new visual render/parity claim. City100 works with the existing backend contract; detail wire savings require separately authorized backend deployment. This completes the scoped proven fixes, not all inventory latency or app redesign acceptance.
