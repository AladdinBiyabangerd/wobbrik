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

## Continued scoped review — 18 November 2026

Reviewed Android committed head `6bb1457` against `efbd9e9` and paired backend code against `1ef8b32`. No newly introduced correctness blocker found in the city100 parser/allowlist, complete legacy cursor replay, detail projection or owner/revision boundaries. No production implementation changed in this continuation; one boundary regression was added.

- **Full-city work remains.** Inventory's optional membership effect still traverses every city page even when showing one inventory page/detail. It is independent of the primary building read and its failure does not hide inventory. Client accumulation is O(N); total server population work is approximately O(N × ceil(N/100)), because the full membership aggregate repeats for each page. The earlier shorthand “CPU grows linearly” applies to client accumulation, not total server work. SQL statement counts alone do not measure rows scanned or contention.
- **Cache is bounded per complete bundle, not per HTTP page.** `DurableJournalFile.MaxBytes` is 140000 bytes, including the JSON wrapper, path keys and escaping of captured body strings. City and building-detail transport each allow 65536 bytes per response (inventory/market allow 1048576). Summed wire bytes therefore do not prove a bundle fits. Neither the transport cap nor the journal/cache cap was raised.
- **Boundary reproduced.** New `cityOverBundleLimitStillReturnsLiveButCannotReplayOffline` uses 3001 synthetic memberships, each page below 65536 bytes. All 31 city pages plus the final sync succeed; the verified live model contains all 3001 IDs. The oversized complete bundle is not persisted and offline replay fails without network. This is an existing capacity limit, not a product building-count limit or a production-size threshold. Optional capture preserves live success; repeat entry without a replayable bundle can still repeat the complete network scan. Existing stale-bundle revision rejection remains covered.
- **Detail still has a geometry limit.** Removing the opaque snapshot does not shrink accepted geometry. A display detail above 65536 bytes still fails before cache capture; old-server full detail may also hit that transport cap before snapshot normalization. The additive query does not guarantee arbitrary-size detail/offline availability.

Validation after the added test: focused `InventoryPayloadTest`, `InventoryReadsTest` and `ReadOnlyGameCacheTest` PASS (`:core:test`, 4s); `git diff --check` PASS. Initial sandboxed Gradle launch could not acquire the wrapper cache lock; approved rerun passed. This continuation did not rerun the earlier app build/lint or real-PostgreSQL suite, and does not turn their historical evidence into a new run. Backend source/contract only was reviewed; no further backend change required for this review. Full-city redesign/cache storage changes remain a separate implementation unit; no merge, CI, push, deploy or visual changes.
