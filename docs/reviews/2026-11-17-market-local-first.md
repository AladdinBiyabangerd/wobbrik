# Bazar local-first page bundle — 17 November 2026

Branch `fix/menu-inventory-latency`, HEAD `083f870`; reviewed working diff: MarketScreen.kt, core MarketReads.kt, new MarketPreviewReadsTest.kt, opt-in `market-local-first/render/`. Existing dirty owner/initial-sync/art/city files, PLAN and baseline profiles preserved. No commit/merge/push/deploy, emulator or CI.

## Cause and change

MarketScreen previously awaited page + every active detail (four concurrent) before publishing any listing. It bypassed AccountViews, so even a previously accepted local bundle could not open the route. The fanout itself remains necessary for the existing full-building cards: the server page contains listing metadata, not building geometry/parts.

Extracted this aggregation into MarketRepository.previewPage. It keeps the four-read bound and page order, verifies the entire detail listing matches its page listing, and never pairs mismatched listing/building revisions. Closed-listing404 remains an unavailable card; cancellation, account changes, authorization, malformed data and other detail failures propagate rather than being silently treated as a successful fresh preview bundle.

MarketScreen now uses AccountViews onLocal with a cache identity containing own/public scope, every filter and cursor. Existing cache storage hashes UID and view identity, and replay requires the exact accepted account revision. Local and final publication check the exact current Ready and BillingSession. A matching replay publishes cards immediately with Keş; live success removes the marker, transient failure can retain the complete matching cache, and terminal failure/revision mismatch clears listings/previews/cursors. Public listings can change without this viewer's revision changing, so every mounted query refreshes (`refresh=true`); completed-read reuse does not suppress that refresh.

Cached cards permit navigation while refreshing, but do not supply purchase/withdrawal details or command authority. Those actions still require the existing new live detail call, confirmation and exact Ready/journal mutation path. New listing/inventory picker is unchanged.

Local review found a pagination hazard: appending fresh data after publishing the cached cursor page would retain the cached version through distinctBy. Screen state now keeps complete cursor pages and replaces the current page atomically. A fresh empty page removes its old cached listings/previews. No layout redesign or raster UI was introduced; the Keş label uses GameTokens Muted/Label in the existing purchase controls row (own listings use a scrollable status item).

## Before/after measurements

Synthetic12active listings,40ms per request, four concurrent detail reads, same account/revision and in-memory JournalFile. Final paired production-read measurement excludes Compose/art:

- Before first publication:183ms,13requests,8501response UTF-8 bytes (page +12details).
- After local publication:2ms,0requests/0response bytes at callback; matching full bundle replayed before refresh.
- After fresh publication:248ms,14requests,8776response UTF-8 bytes. The extra request/275bytes is the existing cache-capture account revision sync. Peak parallelism remains4 (core regression).

Earlier paired sample174→3ms, fresh232ms, identical traffic. This improves time to cached content, not cold network completion or request volume. No production2+s/10+s latency claim.

Same full native route host timing: before1634ms to card/1639ms settled (13/8501); final after1429/1432ms (14/8776). Here40ms network finished during host/art/layout work, so this single host sample is not evidence of a device speedup or isolated cache benefit. Blocked-network route tests supply the stronger result: cards/Keş appear with0completed response bytes and remain usable for entering a fresh-detail loading state. Baseline XML `/private/tmp/wobbrik-market-before.xml`; final paired/stdout XML `/private/tmp/wobbrik-market-local-host-final.xml`.

## Validation and visual limits

Final native Robolectric SDK34 graphics host suite15/15PASS, test26.680s, Gradle31s; `/private/tmp/wobbrik-market-local-host-final.log`. Scenarios: cached public/own pages before blocked response;320/360font1,320/390font2, synthetic insets and Long.MAX_VALUE balance;503fallback;403clear/retry; query/cursor mismatch; stale cached revision; capture revision change; Alice→Bob and same-UID inflight cancellation; cached Bax requiring one fresh detail/no command; fresh cursor price600→700 and fresh empty-page removal. Full OnlineGame/Firebase/device E2E is not proved.

Core144tests include3new preview regressions: bounded concurrency/order, sold/404/changed listing handling,403/503/epoch failure propagation, and separation of every cache query field. Normal app unit/build/lint final result recorded below.

Native PNGs `/private/tmp/wobbrik-market-local-first/` were opened and compared with approved `unified-mobile-v3/02-market.png` (with the later land-removal/7-slot decisions), using the same existing commerce shell/card composition. Keş is legible and uses the existing contrast palette: Text/Surface13.00, Muted/Surface8.22, Accent/Surface8.99PASS. This is not full PNG parity. Existing320grid price text clips (350 can show35), font2 own-tab text is horizontally clipped/scrollable, and small/font2 cards require scrolling to actions. Long balance is horizontally scrollable. Some native activity redraw captures omit unchanged shell/navbar layers; those captures prove only the drawn body/status, not complete shell parity. Insets are synthetic padding, not actual Android IME/system-bar dispatch. TalkBack, complete locale/long-name matrix and device runtime not checked.

Intermediate failures were harness assertions: coroutine exception identity (stack-trace recovery can copy AccountChanged), a native price-label predicate, and checking for an off-screen lazy spinner as evidence of completion. Corrected to exception class/status, native price text, and observable refresh-enabled/cache-marker completion. These failures are not counted as PASS.

Backend source/contract inspected: AppBackend `internal/wobbrik/market.go` MarketFiltered/ReadListing and `docs/wobbrik-game-api.md` marketplace section. Page cursors are live with stable filters; detail returns only active listing+building and commands reread authoritative listing/wallet/revisions. No API/DTO/domain/schema change necessary; AppBackend unchanged and tests not run.

Remaining: cold page+Ndetail fanout; production route/decode/filesystem baseline; redundant reads/coalescing; broader visual parity.404detail responses are not stored as successful bodies by the existing generic cache: a page containing such an optional missing preview can display live, but that bundle cannot replay completely and is treated as a cache miss later. No negative-response cache added. Cache marker refers to the current cursor read, not a frozen server-wide market snapshot; global pages retain existing live-cursor semantics. All command authority stays on AppBackend.

Final normal `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS33s; core144/app28 failures0, `/private/tmp/wobbrik-market-local-checks-final.log`. Local diff review and `git diff --check` PASS. No CI. No production source changed after these final checks.
