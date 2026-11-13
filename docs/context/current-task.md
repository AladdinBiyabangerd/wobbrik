# Cari handoff — 13 noyabr 2026

## Completed
- Əvvəlki navigation/default kosmetik/mobil portrait vahidi PR #1 ilə remote main-ə merge edilib; lokal/remote main baza `e97e78c` idi.
- Main-dən lokal `feat/phase-4` yaradıldı. İlk mobil oxu vahidi: Home inventar → paginated server inventory → bina detalı → city membership oxusu implementasiya edilib.
- Sakin, base/max dəyər və hissələr serverdən oxunur; preview yalnız accepted geometry və mövcud atlaslarla çəkilir. Legacy/missing art statusu var.
- Session/login epoch və exact Ready izolyasiyası, city mixed-revision rəddi, read DTO regression testləri əlavə edildi.
- İnventar/detail davamında sikkə nişanı, çərçivələr, hissə kartları və v1/server cəmləri ilə tutuşdurulan read-only qiymət bölgüsü əlavə edildi; böyük şrift üçün şaquli detail və scroll tabları var.

## Current state
- Branch `feat/phase-4`; main merge/push yoxdur. Untracked `android-billing/app/release/` istifadəçi faylları qorunub.
- Review-lar: `docs/reviews/2026-11-13-phase4-inventory-read.md`, `docs/reviews/2026-11-13-phase4-inventory-detail-polish.md`. İlk oxu vahidi implementasiya edilib, Android vizual qəbul/render yoxdur; mərhələ 4 bütöv bitməyib.

## Decisions
- İstifadəçi mərhələ 4 implementasiyasına icazə verdi, AppBackend-ə toxunmağı qadağan etdi. Yalnız server müqavilə/kodu read-only oxundu; server dəyişdirilmədi.
- İstifadəçi inventory-market-v2 inventar və city-completion-v1/04-building-detail görünüşlərini açıq təsdiqlədi. Ehtiyac olsa raster asset hazırlamağa icazə var. `02-parts-flow-v2.png` bu chatdə hissələr/mağaza və binaya tətbiq üçün açıq təsdiqləndi; digər city-completion lövhələri hələ təklifdir.
- Mobil əvvəl, yalnız portret; planşet işi ayrıca göstərişlə. Emulator/canlı yoxlama tələb edilmir, avtomatik açılmır. CI yalnız ayrıca istək ilə main-də.
- Yeganə plan `docs/PLAN.md`; wallet/ownership authority AppBackend qalır. Yeni merge/push göstərişi yoxdur.

## Remaining work
- Bu UI-nin təsdiqli dizayna vizual uyğunluğu və Android renderi yoxlanmayıb. Qiymət breakdown-u əlavə edilib. Ground dressing/bitkilər, parlaq çərçivə/ikon eyniliyi və atlas join-ləri hələ açıqdır; review-da sərhədlər var. Sabit fasadlı game_island accepted geometry-ni pozacağı üçün istifadə edilmədi.
- Mərhələ 4 qalan vahidlər: mağaza/hissələr, edit/delete, placement/dekor/land, real şəhər renderer-i. Başqa lövhələrin konkret təsdiqi uyğun vahiddə lazımdır.
- Mövcud texniki borclar: default kosmetik cihaz üzrədir; nailiyyətlər ümumi hesab şablonudur, online pause restart açıqdır. Bu vahid onları dəyişmir.

## Relevant files
- `docs/PLAN.md` §1 mərhələ 4, §§2/4/6/7; yeni review.
- core `InventoryReads.kt`, `GameplayHttpBackend.kt`, `InventoryReadsTest.kt`, `BuildingValueBreakdown.kt`, `BuildingValueBreakdownTest.kt`.
- app `InventoryScreen.kt`, `OnlineGame.kt`, `HomeScreen.kt`, `WobbrikApplication.kt`, `GameNavigationTest.kt`, `GameTokens.kt`.
- Dizayn: `docs/design/mobile-v1/inventory-market-v2.png`, `docs/design/city-completion-v1/04-building-detail.png`.

## Checks performed
- Java 21 ilə `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS son kodda. InventoryReadsTest 7/7; BuildingValueBreakdownTest 4/4; identity regression mövcuddur.
- Mətn token kontrast cütləri PASS (minimum 8.22 (bu diff-də istifadə edilən ölçülmüş cütlər)), git diff --check PASS.
- CI/backend testləri/runtime/render/emulator/ADB yoxdur. Heç biri PASS kimi təqdim edilmir.

## Exact next step
- Bu oxu vahidinin qalan ground dressing/ikon/atlas join sərhədlərini review-dan götür; lazım olan rasteri yalnız imagegen ilə hazırla, mövcud real həndəsəni qoruyaraq inteqrasiya et; render olmadan vizual PASS demə. Mövcud təsdiqlənmiş layout/atlaslardan istifadə et, raster lazım olsa istifadəçi icazəsi var.
- Sonra təsdiqli `docs/design/city-completion-v1/02-parts-flow-v2.png` əsasında hissələr/mağaza və binaya tətbiq vahidinə keç; konkret dizaynı yenidən təsdiqə çıxarma; AppBackend-i dəyişdirmə, API boşluğu çıxsa bildir. Yeni əsas plan yaratma.
