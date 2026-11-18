# Cari tapşırıq — Inventory bounded membership/cache / 18 noyabr 2026

## Completed

- İstifadəçinin performans1–2 göstərişi: InventoryScreen-in bütün şəhər membership oxusu çıxarıldı. Display inventory/detail serverdən same-statement owner-scoped optional in_city alır; şəhər ölçüsünə bağlı paging/population işi yoxdur.
- Cache mövcud page/detail key-lərində atomik qalır; oxu cache-inə ayrıca8MiB büdcə verildi. Default sole command journal140000byte dəyişmir. Detail transport1MiB-dir.3001membership və near1MiB escaped body offline replay regression-ları keçir.
- AppBackend paired field/API müqaviləsi, true/false/legacy/foreign/deleted regression-ları və lokal diff review tamamlandı.

## Current state

Android `fix/inventory-bounded-membership-cache`, baza6bb1457; backend `feat/wobbrik`, baza32ad8f7. Owner iki release baseline-profile binarisi dəyişmədən scope xaricində saxlanır. Əvvəlki city-detail review əlavəsi tarixi qeyddir. Main merge/CI/push/deploy yoxdur.

## Decisions

Yeganə plan docs/PLAN.md§1.1/§2–3. in_city yalnız display read metadata-sıdır; wallet/ownership authority, UID/login/capability/revision və final-sync/fresh mutation gate saxlanır. Legacy/old-server missing field naməlumdur, tam şəhər fallback yoxdur.8MiB cache yalnız read-only namespace-ə ötürülür; yeni chunk/manifest/DB/journal formatı yoxdur.1MiB wire/8MiB cache sonlu hədlərdir, arbitrary payload dəstəyi deyil.

## Remaining work

Yeni status metadata-sı ayrıca backend deploy tələb edir; köhnə serverdə naməlum qalır. Production10+s/2+s, RTT/SQL contention/paylanma, fiziki frame/ART/GPU/yaddaş/battery və Firebase-authenticated Android→GoE2E yoxlanmayıb. Şəhər ekranının öz tam-scene böyük-view hədləri bu Inventory vahidi ilə həll edilmiş sayılmır. Profil ilk-açılış/ümumi app performansı və əvvəlki PNG/inset/IME/locale/TalkBack borcları açıqdır.

## Relevant files

Core InventoryReads.kt, GameplayHttpBackend.kt, ReadOnlyGameCache.kt, DurableJournalFile.kt; AndroidAttemptJournal.kt; InventoryScreen.kt, MobileInventoryPresentation.kt, WobbrikApplication.kt. InventoryPayloadTest və inventory-first-open/detail-latency native harness-ləri. Backend market.go, inventory payload/city probe tests və docs/wobbrik-game-api.md. Review docs/reviews/2026-11-18-inventory-bounded-membership-cache.md; evidence inventory-bounded-membership/.

## Checks performed

- Normal core165unit/appunit/assembleDebug/lintDebugPASS11s; final native16/16PASS16s. Native route-lərdə0cityHTTP; cached detail fresh-mutation gate saxlanır. Real atomic file near1MiB escaped payload, default journal limit və3001membership cache/replayPASS.
- Disposable PostgreSQL18/race1001bina: old City100membership+sync12HTTP/209SQL/94387byte/1.701s; new inventory20+sync2HTTP/44SQL/9028byte/23.05ms. Əvvəlki ölçüyə inventory page daxil deyildi; production E2E nisbəti deyil.
- Backend full racePASS(platform34.356s), final focused probePASS16.743s, vet/build/diffcheckPASS. İlkin fixture/compile/FK səhvləri düzəldilib final yoxlamalar təkrar keçirilib. Disposable DB dayandırılıb; production istifadə olunmayıb.
- ui-craft: tokenlər saxlanır; Text/Muted/Success kontrastPASS.320/360/390font1/2native renderlər, inset/uzun balans; list/detail/status nümunələri açılıb approvedPNG ilə müqayisə edildi. Əvvəlki layout/parity borcları bağlanmır. Emulator açılmayıb.

## Exact next step

Bu paired performans1–2 vahidinin istifadəçi review-u. Main merge/CI/push/deploy avtomatik yoxdur. Yeni böyük işə keçməzdən əvvəl yeni chat tövsiyə olunur; owner binarilərinə toxunma.
