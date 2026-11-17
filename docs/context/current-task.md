# Cari tapşırıq — normal Şəhər/Görünüş

## Completed

PLAN §1.1 və01-main.png panel02 əsasında normal mobil Şəhər/Görünüş başlığı, native tab/relyef səthləri, real bina önizləməli Bax kartı və native kamera/fokus idarələri hazırlanıb. Əvvəlki error vahidi təkrarlanmayıb; ortaq tab helper-i CityChrome-a keçirilib. Bu, tam app və mərhələ5 qəbulu deyil.

## Current state

Branch `feat/native-ui-parity`, baza `4e78c68`. İstifadəçi yalnız commit istəyir; merge/push/CI yoxdur. Əvvəlki iki `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` və untracked `docs/reviews/2026-11-17-current-screen-parity-audit.md` commit-dən kənarda qalır. AppBackend dəyişməyib.

## Decisions

Authority yalnız PLAN §1.1 və unified-mobile-v3 README/01-main panel02;7-slot/header saxlanır. Real server geometry/parts/terrain və səhnə/life qalır; PNG-dəki bina/fontan sayı uydurulmur. Plan disabled, placement/Ərazi/press animasiyası açılmır. Kamera sıfırlama/fokus mövcud local view state-dir. Böyük şrift və qısa portretdə body scroll, kamera+Bax/Geri sabitdir. Backend/API/domain/schema/wallet/cache müqaviləsi dəyişmədiyi üçün server dəyişikliyi lazım deyil. Non-mobile yolu saxlanır.

## Remaining work

Tam app pariteti, digər audit qalıqları və mərhələ5 qəbulu açıqdır. Bax mövcud read-only fokusdur; PLAN §1.1-dəki ayrıca bina detalı route inteqrasiyası bu vahiddə dəyişməyib. Real geometry səbəbilə şəhər silueti/ada PNG ilə eyni deyil. Böyük şrift/sıx inset-də supporting başlıq/izah scroll arxasında qala bilər. Home kiçik/font2 hero, İnventar/Görünüş səthləri və Profil/avatar qalıqları auditdə qalır. Real device/account/API24/GPU/performance/TalkBack/IME/locale/tablet yoxlanmayıb. Real SKU/Console/deploy ayrıca tapşırıqdır. Əvvəlki digər review məhdudiyyətləri qüvvədədir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,01-main.png}`
- `docs/reviews/2026-11-17-city-normal-presentation.md`
- `docs/reviews/2026-11-17-current-screen-parity-audit.md` (əvvəlki lokal audit)
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{CityScreen,CityChrome,CityErrorContent,GameTokens}.kt`
- `docs/reviews/mobile-sizing-visual/render/{MobileShellRouteRenderTest.kt,render.init.gradle}`

## Checks performed

Son source5/5 host test PASS, failures/errors/skipped0; lintDebug PASS, BUILD SUCCESSFUL1m8s.390/320 font1/font2 və360/font2+simulated24/48 inset+uzun balans normal/fokus renderləri açılıb01-main ilə müqayisə edilib. İlk320/font1 xırda scene qüsuru düzəldilib son tur yenilənib. Kamera focus/reset, Bax/Geri, disabled Plan,48dp Bax, retry/population40 və submission0 assertions keçir. Kontrast minimum7.66:1 PASS. Lokal diff review/diffcheck PASS. Log `/private/tmp/wobbrik-city-normal-reviewed.log`, renderlər `/private/tmp/wobbrik-city-normal-reviewed/`. Son EOF whitespace təmizliyi davranış dəyişikliyi deyil. CI/emulator yoxdur.

## Exact next step

Bu dar vahidin commit-indən sonra istifadəçinin növbəti tapşırığını gözlə. Error görünüşünü təkrarlama. Növbəti scope-u audit və PLAN §1.1 əsasında seç; mərhələ/alternativ dizayn avtomatik başlamır. Yalnız commit; merge/push/CI/deploy ayrıca göstəriş tələb edir.
