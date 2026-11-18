# Cari tapşırıq — İnventar refresh/art reuse / 18 noyabr 2026

## Completed

- Yeni vahid: sticky `retry > 0` bütün sonrakı list/detail/pagination keçidlərində cache bypass edirdi. Refresh generation yalnız bir oxuda istehlak edilir; explicit city refresh saxlanır.
- `inventory_garden` hər qeyri-purple kart üçün ayrıca decode edilirdi. İndi İnventar composition scope-u ilk real tələbdən sonra bir bitmap-i IO-da yükləyib kart/detal/refresh arasında paylaşır; ekran çıxanda buraxır. Process cache/prewarm/token/dizayn dəyişikliyi yoxdur.
- Əvvəlki commit-lər: `940ec3d` accepted təqdimatı optional disk yazısından əvvəl verir; `3c7c19d` display payload/cache normalizasiyası; `8cd7bae` əvvəlki disk-lock/art/composition işi. Server revision bracket və fresh/mutasiya gate-i saxlanır.

## Current state

Android `fix/inventory-refresh-art-reuse`, baza `940ec3d`; scoped source/test diff lokal review edilib. AppBackend mövcud `feat/wobbrik` dəyişməyib. Main merge/push/deploy/CI yoxdur. İki owner release baseline-profile binarisi dirty saxlanır və commit-ə daxil deyil.

## Decisions

- Yeganə plan `docs/PLAN.md` §1.1/§2–3. Son istifadəçi: cihazsız qalan gecikmə səbəblərini yoxla və düzəlt; ayrıca ölçmə hesabatı deliverable deyil.
- Cihaz/emulator tələb edilmir və açılmır. Lokal native/HTTP fixture production10+s-in tam həlli sübutu deyil.
- Cache yalnız dəqiq UID/login/capability/revision daxilində təqdimat üçündür. Server authority/sole journal dəyişmir.
- Backend display payload qənaəti production-da ayrıca deploy tələb edir. Bu yeni vahidə backend/API/schema dəyişikliyi lazım deyil.

## Remaining work

- Böyük hesabda optional `inventory-city` bütün city səhifələrini oxuyur; bunun HTTP/SQL/final-sync xərci və detail-in istifadə olunmayan opaque payload-u növbəti dar ölçmə nöqtələridir. Bunlar bu vahiddə ölçülmüş yeni root cause kimi təsdiqlənməyib və düzəldilməyib.
- Production RTT/SQL contention/paylanma, Android ART/frame/GPU/battery və Firebase-authenticated device E2E yoxlanmayıb. Əvvəlki lokal Kotlin→Go HTTP→real disposable PostgreSQL synthetic identity probe-u PASS idi; yeni native test real server probe-u deyil.
- Çox böyük accepted geometry transport/cache limitini hələ keçə bilər. Final sync fresh təqdimatdan əvvəl, persistence freshness/completion yolunda qalır.
- Tam PNG parity, kiçik illustration/horizontal tabs, iki-sətir dock, inset/IME/locale/TalkBack borcları qalır.

## Relevant files

- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/InventoryScreen.kt`: refresh token, garden scope, preview.
- App `AccountViews.kt`, `AccountViewsTest.kt`; core `InventoryReads.kt`, `ReadOnlyGameCache.kt`, `GameplayHttpBackend.kt`.
- `docs/reviews/2026-11-18-inventory-refresh-art-reuse.md`; əvvəlki `inventory-presentation-persistence` və `inventory-payload-latency` review-ları.
- Lokal native QA: `/private/tmp/wobbrik-inventory-refresh-host`, `/private/tmp/wobbrik-inventory-refresh.init.gradle`, `/private/tmp/wobbrik-inventory-refresh-renders`.

## Checks performed

- Baseline sticky-refresh regression FAIL; düzəlişlə app41unit PASS, assembleDebug/lintDebug PASS. Son native7/7PASS: list→refresh→detail→back reuse və dörd consumer üçün bir native bitmap/scope davamlılığı.
- Native320/360/390portret, font1/font2 renderləri alınıb; kiçik/normal font1/font2 açılıb baxılıb və approved PNG/README ilə müqayisə edilib. Əvvəlki vizual borclar bağlanmır. Seçilmiş real text/background kontrast cütləri PASS; app-wide contrast PASS deyil.
- Lokal diff review/diffcheckPASS. Backend source/API/schema dəyişməyib; yeni backend test/deploy yoxdur. Owner binariləri toxunulmazdır.

## Exact next step

Cari refresh/art reuse vahidinin istifadəçi review-u; avtomatik main merge/CI/deploy yoxdur. Yeni chatdə yalnız böyük-hesab İnventar city paging/detail payload yolunu ölç, sübut edilmiş redundant işi düzəlt. `docs/PLAN.md` §1.1 və burada göstərilən dar fayllarla başla; bütün app-i yenidən scan etmə. Owner binarilərinə toxunma.
