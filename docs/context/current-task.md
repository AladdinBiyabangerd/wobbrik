# Cari tapşırıq — bütün app performansı / Android + AppBackend

## Completed

- Cross-stack Bazar bundled previews: server mövcud SQL join-dan `include=previews` ilə matching kartları qaytarır, Android12detail fanout-u çıxarır. Köhnə server bounded fallback, köhnə complete cache offline replay, fresh detail-before-command saxlanır. Backend `feat/wobbrik` commit `9f0faf9`; Android ayrıca `perf: consume bundled market previews without detail fanout` commit-indədir. Review `docs/reviews/2026-11-17-market-bundled-previews.md`.

- AccountViews cancelled-timeout fallback düzəldi: IOException catch girişində ensureActive; ləğv edilmiş iş replay/cache-miss yoluna keçmir. Cache olan/olmayan blocking timeout regressiyası PASS. Review `docs/reviews/2026-11-17-cancelled-timeout-fallback.md`.

- Inventory local-first/optional city vahidi `083f870`-dədir.
- İlkin sync gate, proses üzrə art reuse, şəhər4paralel detail scheduling və CityScreen onLocal əvvəlki working diff-dədir. Sübutlar: `docs/reviews/2026-11-17-{initial-account-sync,art-reuse,city-read-scheduling,city-local-first}.md`.
- Same-UID City host timeout dar araşdırması tamamlandı: state write-dan sonra `waitForIdle()` olmaması reproduksiya edildi, test düzəldi; production CityScreen/AccountViews dəyişmədi. Retained11/11PASS. Review `docs/reviews/2026-11-17-city-same-uid-host.md`.
- Bazar local-first dar vahidi tamamlandı: page+Ndetail aggregation repository-yə çıxarıldı, matching complete bundle AccountViews onLocal ilə dərhal göstərilir. Query scope/filter/cursor, UID/revision və exact Ready/session yoxlamaları saxlanır. Cached kartdan əməliyyat üçün yeni live detail tələb olunur. Cursor səhifəsi cached→fresh keçidində bütöv əvəz edilir. Review `docs/reviews/2026-11-17-market-local-first.md`.

- Redundant-read ölçmə vahidi tamamlandı (production dəyişməyib): normal Market14distinct request/8776byte; concurrent identical refresh28/17552; generic read-only onLocal2disk replay; capture eyni path-i coalesce etmir. Review `docs/reviews/2026-11-17-redundant-reads.md`, opt-in harness/results `docs/reviews/redundant-reads/`.

- Read-only single-replay fix tamamlandı: AccountViews ready==null onLocal yalnız bir replay edir; callback/session/cancellation yoxlamaları və8yeni regression var. Review `docs/reviews/2026-11-17-readonly-single-replay.md`.

- AccountViews/MarketScreen production-path host overlap baseline tamamlandı: cold+5recomposition0overlap; warm remount əlavə14request/0overlap; public→own→public cooperative0, blocking-I/O model1same-path overlap (16request/8776completed body byte/2cancellation). Direct2refresh positive control28request/17552byte. Real server/device overlap tezliyi deyil. Review `docs/reviews/2026-11-17-production-overlap.md`;4hostPASS. Production/əvvəlki dirty fayllar dəyişməyib.

- Real HTTPS overlap/cancellation ölçü vahidi tamamlandı: production UrlConnectionGameplayTransport + loopback TLSv1.3. Market public→own→public16request/11384server-written body byte/1same-path overlap; cancellation→transport exit public563ms/own534ms; header515ms/body756ms/timeout7996ms.4hostPASS. Review `docs/reviews/2026-11-17-https-overlap.md`, harness/evidence `docs/reviews/https-overlap/`. Production/əvvəlki dirty işlər dəyişməyib.

## Current state

Android branch `fix/menu-inventory-latency`, bu vahidin bazası `c579e22`; əvvəlki dirty işlər `9cac996`-də saxlanıb. Bazar bundled-preview vahidi ayrıca commit-dədir. AppBackend təmiz checkout `feat/dailyplate`-dan required persistent `feat/wobbrik`-ə keçirildi, local main `7584346`-ə ff edildi; Bazar dəyişməsi `9f0faf9`-dədir. DailyPlate işi saxlanıb. Heç bir merge/push/deploy/CI edilməyib. Bütün app performansı və redesign tamamlanmayıb.

## Decisions

İstifadəçi bütün app performansı üçün Android və AppBackend-də lazım olan düzəlişlərə açıq icazə verib; backend üçün təkrar icazə soruşma. Push/main merge/deploy ayrıca qalır. Yeganə plan `docs/PLAN.md` §1.1/§2–3. İlk vacib sync mövcud owner recovery/journal reconcile+catalog-dur; optional warmup gate deyil.8000ms gate çıxışı network cancellation deyil. Gate login epoch üzrədir. Cache display-only-dir, wallet/mutation authority AppBackend-dir.

Bazar public list viewer revision dəyişmədən dəyişə bilər: hər mounted query background refresh edir, completed-read reuse refresh-i udmur. Complete cache yalnız exact account revision-da replay olunur; capture sonunda mövcud sync saxlanır.404detail live unavailable card ola bilər, lakin həmin response body saxlanmadığından belə bundle sonradan tam replay olunmursa cache miss-dir; negative-response cache əlavə edilməyib. Yeni endpoint/domain/schema yoxdur. Emulator avtomatik açılmır; CI yalnız ayrıca tapşırıqla main-dədir.

## Remaining work

- Production2+s/10+s route baseline hələ yoxdur. Yeni backend ilə Bazar cold page+Ndetail fanout-u2sorğuya endi; köhnə serverdə4paralel detail fallback qalır. Backend deploy edilməyib.
- Capture redundant path/coalescing sintetik ölçüldü; production overlap tezliyi və durable disk baseline qalır. Revision sync kor-koranə çıxarılmır. Public market live-cursor müqaviləsidir, server-wide frozen snapshot deyil; Keş nişanı cari cursor oxusuna aiddir.
- Art cold decode main-thread-dədir; reuse təkrar decode-u azaldır, təxminən60MiB sabit proses retention qalır. Digər art helper/frame polling/map allocation açıqdır.
- Vizual parity/mərhələ5qəbulu qalır:320grid qiymət clipping, font2 tab/navbar/scroll borcları var. Bəzi native redraw PNG-lərində dəyişməyən shell qatları çəkilmir; bunlar full shell parity sübutu deyil. Real Firebase/server/device/TalkBack/locale matrisi yoxlanmayıb.

## Relevant files

- Son vahid: `MarketReads.kt`, `ReadOnlyGameCache.kt`, `MarketPreviewReadsTest.kt`; host `docs/reviews/market-local-first/render/MarketLocalFirstTest.kt`.
- Backend `internal/wobbrik/market.go`, `internal/platform/wobbrik_{http.go,market_previews_test.go}`, `docs/wobbrik-game-api.md`, `docs/reviews/2026-11-17-wobbrik-market-previews.md`.

- `docs/PLAN.md` §1.1/§2–3; son review `docs/reviews/2026-11-17-market-local-first.md`.
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MarketScreen,AccountViews,InventoryScreen,OnlineGame,CityScreen}.kt`.
- Core `MarketReads.kt`, `ReadOnlyGameCache.kt`, `AuthenticatedRecoveryOwner.kt`; tests `MarketPreviewReadsTest.kt`, `MenuReadLatencyTest.kt`.
- Host `docs/reviews/market-local-first/render/{MarketLocalFirstTest.kt,render.init.gradle}`; əvvəlki City harness `docs/reviews/city-local-first/render/`.
- Əvvəlki `InitialAccountSync.kt`, `ApprovedArt.kt`, `GameAtlas.kt`, `FinishCosmeticScreen.kt`, core `CitySceneReads.kt` working diff-dədir.
- AppBackend `docs/wobbrik-game-api.md`, `internal/wobbrik/market.go` müqavilə/source kimi yoxlanıb, dəyişməyib.

## Checks performed

Cross-stack unit: core147/app37unit, assembleDebug/lintDebugPASS25s; `/private/tmp/wobbrik-market-bundle-final.log`. Fixture12kart40ms/request14→2sorğu,5174→5199body byte,222→92ms; real cihaz/production latency deyil. Backend full race+disposablePostgreSQL18PASS(platform23.791s), vet/buildPASS; `/private/tmp/wobbrik-perf-backend-final.log`. DB dayandırılıb. Final native Compose host18/18PASS,0failure/error/skip,26.839s test/Gradle29s. Public+own bundled cold2request/0detail, cached-before-network and live detail-before-purchase, legacy fallback/cursor/revision/login regression checked. Evidence `docs/reviews/market-bundled-previews/host-results.xml`; log `/private/tmp/wobbrik-market-bundle-host-verified.log`. Initial own-tab predicate failed twice (refresh then public-only action); final source uses the actual enabled own/public card action and full suite rerun PASS. No production fix was needed for those harness failures.

Cancelled-timeout fix: AccountViews14/14unit, assembleDebug/lintDebugPASS32s; `/private/tmp/wobbrik-cancel-checks.log`. Əvvəlki dirty faylların hash-lərində yalnız nəzərdə tutulan AccountViews dəyişdi (review/handoff yenilənməsindən əvvəl). Real HTTPS harness təkrar işlədilməyib. CI yoxdur.

Real HTTPS final4/4hostPASS,16.889s test/Gradle20s; `docs/reviews/https-overlap/results.xml`, `server-events.jsonl`, `/private/tmp/wobbrik-https/test-final.log`. Cancelled view cache boşdur, publication/command yoxdur. TLS fixture dayandırıldı; hash müqayisəsində review/handoff-dan əvvəl bütün əvvəlki dirty fayllar eyni idi. Diffcheck/PythonAST/shSyntaxPASS. CI/build/lint/backend test yoxdur.


Overlap baseline final:4/4hostPASS,9.245s test/Gradle12s; `docs/reviews/production-overlap/results.xml`, `/private/tmp/wobbrik-overlap.log`. Initial stale-enabled-node wait failure harness-də final network completion ilə düzəldildi; production dəyişməyib. Başlanğıcdakı bütün dirty/untracked faylların hash-ləri review/handoff yeniləməsindən əvvəl eyni idi. DiffcheckPASS; CI/build/lint/backend test yoxdur.


Read-only fix final: focused AccountViews13/13unitPASS (8new+5existing), assembleDebug/lintDebugPASS26s; log `/private/tmp/wobbrik-readonly-replay-checks.log`, XML `docs/reviews/readonly-replay/`. Cache read2→1,0network; UID/revision/query/corruption/session epoch/callback/cancellation yoxlanıb. Production yalnız AccountViews dəyişib; əvvəlki dirty işlər saxlanıb, CI yoxdur.


Redundant-read final focused offline host2test/9scenarioPASS, Gradle6s. Exact request/byte/cache-read assertions, `docs/reviews/redundant-reads/results.xml`; normal cold239ms/warm227ms, overlap232ms, sequential457ms, reuse3ms; sintetik40ms/request, production ölçüsü deyil. Production dəyişməyib; backend müqaviləsi read-only yoxlanıb, backend test/CI yoxdur.


Bazar final15/15native hostPASS31s (test26.680s): cached public/own blocked network0response byte;320/360font1,320/390font2, synthetic inset/Long.MAX balance;503fallback,403clear/retry, query/cursor mismatch, stale cache/capture revision change, Alice→Bob/sameUID inflight cancellation, cached Bax→freshdetail/no command; cursor600→700və fresh empty-page removal. Log `/private/tmp/wobbrik-market-local-host-final.log`, XML `/private/tmp/wobbrik-market-local-host-final.xml`.9PNG `/private/tmp/wobbrik-market-local-first/` açılıb02-market ilə müqayisə edilib; yuxarıdakı borclar/host redraw məhdudiyyəti qalır. Contrast13.00/8.22/8.99PASS.

Paired12listing40ms/request fixture: before-first-publication183ms,13request/8501UTF8byte; afterlocal2ms,0request/byte; afterfresh248ms,14request/8776byte (əlavə sync1request/275byte). Əvvəlki sample174→3ms,fresh232ms. Full host route1634→1429ms sample art/layout daxil edir və isolated device speedup deyil; əsas sübut blocked-networkdə local kartların görünməsidir. Baseline `/private/tmp/wobbrik-market-before.xml`. Cold fanout qalır.

Final normal core144/app28unit,assembleDebug,lintDebugPASS33s; `/private/tmp/wobbrik-market-local-checks-final.log`.3yeni core test bounded4/order/version mismatch/404 və403/503/epoch propagation/query identity-ni yoxlayır. DiffcheckPASS. Backend API/domain/schema dəyişməsi lazım deyil, backend test işlədilməyib. CI yoxdur. Aralıq host predicate/exception identity assertion xətaları review-da qeyd olunub; PASS sayılmır.

Əvvəlki vahidlərin tam sübutları öz review-larındadır: initial-sync8host/28app; art reuse2host+5commerce; city local-first10host+sameUID11host və normal core141/app28PASS. Art ölçüsü5mount əvvəl260/229/225/256/242ms,50bitmap/314531040byte; sonra262/0/0/0/0ms,10bitmap/62906208byte. City scheduling synthetic714→264ms,15request/3630byte; capture734→316ms,16/3903; replay0request. Bunlar production latency/heap deyil.

## Exact next step

Bütün app performans tapşırığı davam edir. PLAN §1.1, bu handoff və son `docs/reviews/2026-11-17-market-bundled-previews.md` oxu. Növbəti dar vahid: Home/City/Inventory/Market/Achievements/Profile/game üçün cold/warm art decode və idle-menu frame snapshot allocation ölç; `OnlineGame` hər frame exact Ready və iki host snapshot-u poll edir, `ApprovedArt` cold decode sinxrondur, `ProfilePresentation` source board-ları provider-də açır. Bunlar bu vahiddə ölçülmüş root cause deyil. Uyğun backend request/query latency-ni də eyni ölçüyə bağla; City building fanout və HTTPS transport cancellation açıqdır. Exact Ready/login epoch, journal, revision sync və approved UI qorunsun. AppBackend yeni iş `feat/wobbrik`-də; icazə əvvəlcədən var, deploy/push/merge/CI yoxdur. Bütün app bitmiş kimi bildirmə.
