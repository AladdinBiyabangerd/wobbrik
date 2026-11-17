# Cari tapşırıq — menyu performansı / Bazar local-first

## Completed

- Inventory local-first/optional city vahidi `083f870`-dədir.
- İlkin sync gate, proses üzrə art reuse, şəhər4paralel detail scheduling və CityScreen onLocal əvvəlki working diff-dədir. Sübutlar: `docs/reviews/2026-11-17-{initial-account-sync,art-reuse,city-read-scheduling,city-local-first}.md`.
- Same-UID City host timeout dar araşdırması tamamlandı: state write-dan sonra `waitForIdle()` olmaması reproduksiya edildi, test düzəldi; production CityScreen/AccountViews dəyişmədi. Retained11/11PASS. Review `docs/reviews/2026-11-17-city-same-uid-host.md`.
- Bazar local-first dar vahidi tamamlandı: page+Ndetail aggregation repository-yə çıxarıldı, matching complete bundle AccountViews onLocal ilə dərhal göstərilir. Query scope/filter/cursor, UID/revision və exact Ready/session yoxlamaları saxlanır. Cached kartdan əməliyyat üçün yeni live detail tələb olunur. Cursor səhifəsi cached→fresh keçidində bütöv əvəz edilir. Review `docs/reviews/2026-11-17-market-local-first.md`.

- Redundant-read ölçmə vahidi tamamlandı (production dəyişməyib): normal Market14distinct request/8776byte; concurrent identical refresh28/17552; generic read-only onLocal2disk replay; capture eyni path-i coalesce etmir. Review `docs/reviews/2026-11-17-redundant-reads.md`, opt-in harness/results `docs/reviews/redundant-reads/`.

- Read-only single-replay fix tamamlandı: AccountViews ready==null onLocal yalnız bir replay edir; callback/session/cancellation yoxlamaları və8yeni regression var. Review `docs/reviews/2026-11-17-readonly-single-replay.md`.

- AccountViews/MarketScreen production-path host overlap baseline tamamlandı: cold+5recomposition0overlap; warm remount əlavə14request/0overlap; public→own→public cooperative0, blocking-I/O model1same-path overlap (16request/8776completed body byte/2cancellation). Direct2refresh positive control28request/17552byte. Real server/device overlap tezliyi deyil. Review `docs/reviews/2026-11-17-production-overlap.md`;4hostPASS. Production/əvvəlki dirty fayllar dəyişməyib.

- Real HTTPS overlap/cancellation ölçü vahidi tamamlandı: production UrlConnectionGameplayTransport + loopback TLSv1.3. Market public→own→public16request/11384server-written body byte/1same-path overlap; cancellation→transport exit public563ms/own534ms; header515ms/body756ms/timeout7996ms.4hostPASS. Review `docs/reviews/2026-11-17-https-overlap.md`, harness/evidence `docs/reviews/https-overlap/`. Production/əvvəlki dirty işlər dəyişməyib.

## Current state

Branch `fix/menu-inventory-latency`, HEAD `083f870`. Bütün son vahidlər working diff-dədir; commit/merge/push edilməyib. Owner PLAN dəyişməsi, baselineProfiles/{0,1}/app-release.dm və untracked parity audit saxlanıb, toxunma. Bütün menyu performans/redesign işi tamamlanmayıb. CI yoxdur.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1/§2–3. İlk vacib sync mövcud owner recovery/journal reconcile+catalog-dur; optional warmup gate deyil.8000ms gate çıxışı network cancellation deyil. Gate login epoch üzrədir. Cache display-only-dir, wallet/mutation authority AppBackend-dir.

Bazar public list viewer revision dəyişmədən dəyişə bilər: hər mounted query background refresh edir, completed-read reuse refresh-i udmur. Complete cache yalnız exact account revision-da replay olunur; capture sonunda mövcud sync saxlanır.404detail live unavailable card ola bilər, lakin həmin response body saxlanmadığından belə bundle sonradan tam replay olunmursa cache miss-dir; negative-response cache əlavə edilməyib. Yeni endpoint/domain/schema yoxdur. Emulator avtomatik açılmır; CI yalnız ayrıca tapşırıqla main-dədir.

## Remaining work

- Production2+s/10+s route baseline hələ yoxdur. Bazar cold page+Ndetail(4paralel) gözləməsi qalır; bu vahid matching keş olan ilk açılışı yaxşılaşdırır, request sayını azaltmır.
- Capture redundant path/coalescing sintetik ölçüldü; production overlap tezliyi və durable disk baseline qalır. Revision sync kor-koranə çıxarılmır. Public market live-cursor müqaviləsidir, server-wide frozen snapshot deyil; Keş nişanı cari cursor oxusuna aiddir.
- Art cold decode main-thread-dədir; reuse təkrar decode-u azaldır, təxminən60MiB sabit proses retention qalır. Digər art helper/frame polling/map allocation açıqdır.
- Vizual parity/mərhələ5qəbulu qalır:320grid qiymət clipping, font2 tab/navbar/scroll borcları var. Bəzi native redraw PNG-lərində dəyişməyən shell qatları çəkilmir; bunlar full shell parity sübutu deyil. Real Firebase/server/device/TalkBack/locale matrisi yoxlanmayıb.

## Relevant files

- `docs/PLAN.md` §1.1/§2–3; son review `docs/reviews/2026-11-17-market-local-first.md`.
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MarketScreen,AccountViews,InventoryScreen,OnlineGame,CityScreen}.kt`.
- Core `MarketReads.kt`, `ReadOnlyGameCache.kt`, `AuthenticatedRecoveryOwner.kt`; tests `MarketPreviewReadsTest.kt`, `MenuReadLatencyTest.kt`.
- Host `docs/reviews/market-local-first/render/{MarketLocalFirstTest.kt,render.init.gradle}`; əvvəlki City harness `docs/reviews/city-local-first/render/`.
- Əvvəlki `InitialAccountSync.kt`, `ApprovedArt.kt`, `GameAtlas.kt`, `FinishCosmeticScreen.kt`, core `CitySceneReads.kt` working diff-dədir.
- AppBackend `docs/wobbrik-game-api.md`, `internal/wobbrik/market.go` müqavilə/source kimi yoxlanıb, dəyişməyib.

## Checks performed

Real HTTPS final4/4hostPASS,16.889s test/Gradle20s; `docs/reviews/https-overlap/results.xml`, `server-events.jsonl`, `/private/tmp/wobbrik-https/test-final.log`. Cancelled view cache boşdur, publication/command yoxdur. TLS fixture dayandırıldı; hash müqayisəsində review/handoff-dan əvvəl bütün əvvəlki dirty fayllar eyni idi. Diffcheck/PythonAST/shSyntaxPASS. CI/build/lint/backend test yoxdur.


Overlap baseline final:4/4hostPASS,9.245s test/Gradle12s; `docs/reviews/production-overlap/results.xml`, `/private/tmp/wobbrik-overlap.log`. Initial stale-enabled-node wait failure harness-də final network completion ilə düzəldildi; production dəyişməyib. Başlanğıcdakı bütün dirty/untracked faylların hash-ləri review/handoff yeniləməsindən əvvəl eyni idi. DiffcheckPASS; CI/build/lint/backend test yoxdur.


Read-only fix final: focused AccountViews13/13unitPASS (8new+5existing), assembleDebug/lintDebugPASS26s; log `/private/tmp/wobbrik-readonly-replay-checks.log`, XML `docs/reviews/readonly-replay/`. Cache read2→1,0network; UID/revision/query/corruption/session epoch/callback/cancellation yoxlanıb. Production yalnız AccountViews dəyişib; əvvəlki dirty işlər saxlanıb, CI yoxdur.


Redundant-read final focused offline host2test/9scenarioPASS, Gradle6s. Exact request/byte/cache-read assertions, `docs/reviews/redundant-reads/results.xml`; normal cold239ms/warm227ms, overlap232ms, sequential457ms, reuse3ms; sintetik40ms/request, production ölçüsü deyil. Production dəyişməyib; backend müqaviləsi read-only yoxlanıb, backend test/CI yoxdur.


Bazar final15/15native hostPASS31s (test26.680s): cached public/own blocked network0response byte;320/360font1,320/390font2, synthetic inset/Long.MAX balance;503fallback,403clear/retry, query/cursor mismatch, stale cache/capture revision change, Alice→Bob/sameUID inflight cancellation, cached Bax→freshdetail/no command; cursor600→700və fresh empty-page removal. Log `/private/tmp/wobbrik-market-local-host-final.log`, XML `/private/tmp/wobbrik-market-local-host-final.xml`.9PNG `/private/tmp/wobbrik-market-local-first/` açılıb02-market ilə müqayisə edilib; yuxarıdakı borclar/host redraw məhdudiyyəti qalır. Contrast13.00/8.22/8.99PASS.

Paired12listing40ms/request fixture: before-first-publication183ms,13request/8501UTF8byte; afterlocal2ms,0request/byte; afterfresh248ms,14request/8776byte (əlavə sync1request/275byte). Əvvəlki sample174→3ms,fresh232ms. Full host route1634→1429ms sample art/layout daxil edir və isolated device speedup deyil; əsas sübut blocked-networkdə local kartların görünməsidir. Baseline `/private/tmp/wobbrik-market-before.xml`. Cold fanout qalır.

Final normal core144/app28unit,assembleDebug,lintDebugPASS33s; `/private/tmp/wobbrik-market-local-checks-final.log`.3yeni core test bounded4/order/version mismatch/404 və403/503/epoch propagation/query identity-ni yoxlayır. DiffcheckPASS. Backend API/domain/schema dəyişməsi lazım deyil, backend test işlədilməyib. CI yoxdur. Aralıq host predicate/exception identity assertion xətaları review-da qeyd olunub; PASS sayılmır.

Əvvəlki vahidlərin tam sübutları öz review-larındadır: initial-sync8host/28app; art reuse2host+5commerce; city local-first10host+sameUID11host və normal core141/app28PASS. Art ölçüsü5mount əvvəl260/229/225/256/242ms,50bitmap/314531040byte; sonra262/0/0/0/0ms,10bitmap/62906208byte. City scheduling synthetic714→264ms,15request/3630byte; capture734→316ms,16/3903; replay0request. Bunlar production latency/heap deyil.

## Exact next step

Real HTTPS baseline tamamlandı, optimizasiya edilmədi. Yeni chatdə PLAN §1.1, bu handoff və `docs/reviews/2026-11-17-https-overlap.md` oxu. Konkret tapıntı: cancellation blocking HTTPS oxusunu kəsmir, controlled rapid tab return real same-path overlap yaradır; cancelled timeout AccountViews IOException fallback-də cache replay edib cache miss IllegalArgumentException çıxara bilir. Növbəti ayrıca vahid cancellation-before-fallback düzəlişi və ya ölçüyə əsasən scoped transport cancellation ola bilər; inflight coalescing ayrıca qərar/test tələb edir. Device/deployed frequency və durable disk baseline qalır. Bütün dirty işləri saxla; merge/push/CI icazəsi yoxdur.
