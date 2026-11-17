# Cari tapşırıq — Şəhər xəta görünüşü

## Completed

PLAN §1.1 və04-game-states.png panel21 əsasında mobil Şəhər/error native kartı, illüstrasiya, başlıq və sabit Refresh/Yenilə hazırdır. Geri və7-slot shell saxlanır; xəta halında səhnə/camera paneli yoxdur. Əvvəlki detal vahidləri1030d16-dək hazırdır, təkrarlanmayıb. Bu, tam Şəhər/app pariteti deyil.

## Current state

Branch `feat/native-ui-parity`, bu vahidin bazası1030d16. İstifadəçi yalnız commit istəyir; merge/push/CI yoxdur. İki əvvəlki generated `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` və əvvəlki untracked `docs/reviews/2026-11-17-current-screen-parity-audit.md` dəyişdirilmədən/commit-dən kənarda qalır. AppBackend dəyişməyib.

## Decisions

Yeganə plan docs/PLAN.md §1.1; authority unified-mobile-v3 README və04 panel21. Raster yalnız generativ, mətnsiz mürəkkəb error illüstrasiyasıdır; bütün UI native GameTokens/MenuRelief/CommerceTitle/HomeButton-dur. Font2-də tab ikonları gizlənir, söz qırılmır; supporting art/title scroll-dadır, Yenilə sabitdir. Sıx inset-də başlıq ilk viewport-da scroll arxasında ola bilər. Normal şriftdə bütün kart birlikdə görünür. Tablet yolu saxlanır. Mövcud account/session/retry/revision/cache müqaviləsi dəyişmir; backend/API/domain/wallet/schema dəyişikliyinə ehtiyac yoxdur. Plan disabled, placement/Ərazi/press animasiyası dəyişmir.

## Remaining work

Tam app/PNG pariteti və mərhələ5 qəbulu açıqdır. Detal artwork-ün şüa intensivliyi/silueti və ümumi əməliyyat kompozisiyası tam uyğun deyil; real geometry/parts saxlanmalıdır. Mövcud əlavə funksiyalar saxlanır. Böyük ad/uzun qiymət horizontal scroll-u, kiçik/font2 body scroll qalır. Uzun qiymət scroll touch assertion, disabled silmə üçün ayrıca render yoxdur. Partial detail cache/replay və AccountChanged üçün ayrıca yeni host ssenarisi yoxdur. Ölçülmüş performans, reklam/alış məhsul inteqrasiyası və buraxılış hazırlığı açıqdır. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Real SKU/Console/deploy ayrıca göstəriş tələb edir. İki .dm artifact ayrıca qərar gözləyir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,04-game-states.png}`
- `docs/reviews/2026-11-17-city-error-presentation.md`
- `docs/reviews/2026-11-17-current-screen-parity-audit.md` (əvvəlki lokal untracked audit)
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{CityScreen,CityErrorContent,CityChrome,GameTokens}.kt`
- `android-billing/app/src/main/res/drawable-nodpi/city_error_illustration_v1.png`
- `docs/reviews/mobile-sizing-visual/render/{MobileShellRouteRenderTest.kt,render.init.gradle}`

## Checks performed

Son source5/5 host test PASS, failures/errors/skipped0; lintDebug PASS, BUILD SUCCESSFUL37s.390×844/320×568 font1/font2 və360×640/font2 simulated24/48 inset+uzun balans error renderləri açılıb04 ilə müqayisə edilib.503→retry→population/scene→Bax→Geri production repository synthetic HTTP axını keçir; dəyişməyən submission0/Plan disabled və dock regression-ları saxlanır. Köhnə cumulative cityReads assertion3→4 düzəldi (İnventar membership oxusu). Retry48dp, Geri→home və error-da Bax-ın olmaması keçir. Kontrast minimum7.66 PASS. Lokal diff review/diffcheck PASS. İlk layout/overflow qüsurları düzəldilib son run yenilənib. Log `/private/tmp/wobbrik-city-error-reviewed.log`, renderlər `/private/tmp/wobbrik-city-error-reviewed/`. CI/emulator yoxdur. Real runtime/locale/performance məhdudiyyətləri review-dadır.

## Exact next step

İstifadəçinin növbəti dar tapşırığını gözlə. Normal Şəhər/Görünüş kompozisiyası, Home kiçik/font2 hero, Görünüş səthləri və Profil/avatar qalıqları auditdədir; bu error vahidini yenidən etmə. PLAN §1.1 yeganə plandır; yeni scope/mərhələ avtomatik başlamır. Context böyüyür — növbəti vahidi yeni chatdə davam et. Yalnız commit; merge/push/CI/deploy ayrıca göstəriş tələb edir.
