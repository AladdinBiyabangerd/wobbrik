# Cari handoff — 13 noyabr 2026

## Completed
- Əvvəlki navigation/default kosmetik/mobil portrait vahidi PR #1 ilə remote main-ə merge edilib; lokal/remote main baza `e97e78c` idi.
- Main-dən lokal `feat/phase-4` yaradıldı. İlk mobil oxu vahidi: Home inventar → paginated server inventory → bina detalı → city membership oxusu implementasiya edilib.
- Sakin, base/max dəyər və hissələr serverdən oxunur; preview yalnız accepted geometry və mövcud atlaslarla çəkilir. Legacy/missing art statusu var.
- Session/login epoch və exact Ready izolyasiyası, city mixed-revision rəddi, read DTO regression testləri əlavə edildi.
- İnventar/detail davamında sikkə nişanı, çərçivələr, hissə kartları və v1/server cəmləri ilə tutuşdurulan read-only qiymət bölgüsü əlavə edildi; böyük şrift üçün şaquli detail və scroll tabları var.

- İnventar/detail davamı: binasız şəffaf `inventory_garden.png`, GameScene ilə uyğun preview anchor-ları, üfüqi hissə kartları, dairəvi geri düyməsi və mövcud parlaq HomeButton təqdimatı lokal working diff-dədir. Review: `docs/reviews/2026-11-13-phase4-inventory-garden.md`.

- Hissələr/mağaza və binaya tətbiq working diff-də əlavə edilib: `PartsScreen.kt`, `PartsMutations.kt`, eyni durable journal-a `SHOP_BUY`/`BUILDING_EDIT` route-ları. Review: `docs/reviews/2026-11-13-phase4-parts-flow.md`.

## Current state
- Branch `feat/phase-4`; main merge/push yoxdur. Untracked `android-billing/app/release/` istifadəçi faylları qorunub.
- Review-lar: `docs/reviews/2026-11-13-phase4-inventory-read.md`, `docs/reviews/2026-11-13-phase4-inventory-detail-polish.md`. İlk oxu vahidi implementasiya edilib, Android vizual qəbul/render yoxdur; mərhələ 4 bütöv bitməyib.

## Decisions
- İstifadəçi mərhələ 4 implementasiyasına icazə verdi, AppBackend-ə toxunmağı qadağan etdi. Yalnız server müqavilə/kodu read-only oxundu; server dəyişdirilmədi.
- İstifadəçi inventory-market-v2 inventar və city-completion-v1/04-building-detail görünüşlərini açıq təsdiqlədi. Ehtiyac olsa raster asset hazırlamağa icazə var. `02-parts-flow-v2.png` bu chatdə hissələr/mağaza və binaya tətbiq üçün açıq təsdiqləndi; digər city-completion lövhələri hələ təklifdir.
- Mobil əvvəl, yalnız portret; planşet işi ayrıca göstərişlə. Emulator/canlı yoxlama tələb edilmir, avtomatik açılmır. CI yalnız ayrıca istək ilə main-də.
- Yeganə plan `docs/PLAN.md`; wallet/ownership authority AppBackend qalır. Yeni merge/push göstərişi yoxdur.

## Remaining work
- Bu UI-nin təsdiqli dizayna vizual uyğunluğu və Android renderi yoxlanmayıb. Qiymət breakdown-u əlavə edilib. Ground dressing asset-i və preview anchor düzəlişi əlavə edilib; parlaq çərçivə/ikon eyniliyi və atlas join-lərinin Android render təsdiqi hələ açıqdır; review-da sərhədlər var. Sabit fasadlı game_island accepted geometry-ni pozacağı üçün istifadə edilmədi.
- Hissələr üzrə sikkə alış/edit inteqrasiyası working diff-dədir; tam vizual uyğunluq/render təsdiqlənməyib. Mərhələ 4 qalan vahidlər: delete, placement/dekor/land, real şəhər renderer-i. Başqa lövhələrin konkret təsdiqi uyğun vahiddə lazımdır.
- Mövcud texniki borclar: default kosmetik cihaz üzrədir; nailiyyətlər ümumi hesab şablonudur, online pause restart açıqdır. Bu vahid onları dəyişmir.

## Relevant files
- `docs/PLAN.md` §1 mərhələ 4, §§2/4/6/7; yeni review.
- core `InventoryReads.kt`, `GameplayHttpBackend.kt`, `InventoryReadsTest.kt`, `BuildingValueBreakdown.kt`, `BuildingValueBreakdownTest.kt`.
- app `InventoryScreen.kt`, `OnlineGame.kt`, `HomeScreen.kt`, `WobbrikApplication.kt`, `GameNavigationTest.kt`, `GameTokens.kt`.
- Dizayn: `docs/design/mobile-v1/inventory-market-v2.png`, `docs/design/city-completion-v1/04-building-detail.png`.

## Checks performed
- Hissələr vahidinin son kodunda Java 21 ilə `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (14s); PartsMutationsTest 4/4 PASS. Token kontrastı 4 cüt PASS, minimum 8.22; diff check PASS. Runtime/render/CI/backend testləri edilməyib.
- Java 21 ilə `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS son kodda. InventoryReadsTest 7/7; BuildingValueBreakdownTest 4/4; identity regression mövcuddur.
- Mətn token kontrast cütləri PASS (minimum 8.22 (bu diff-də istifadə edilən ölçülmüş cütlər)), git diff --check PASS.
- CI/backend testləri/runtime/render/emulator/ADB yoxdur. Heç biri PASS kimi təqdim edilmir.

## Exact next step
- Cari working diff-ni `docs/reviews/2026-11-13-phase4-parts-flow.md` ilə review et; birbaşa main merge/push etmə. Əvvəlki inventory garden/polish diff-i və istifadəçi `app/release/` faylları qorunur.
- Hissələrə giriş: İnventar → Hissələr/Mağaza. Tətbiq: bina detalı → Bina görünüşü → kateqoriya → açıq hissə → Tətbiq et. Alış və edit eyni tətbiq-owned durable journal ilə işləyir; ikinci outbox/wallet yoxdur.
- AppBackend dəyişdirilməyib; mövcud `shop/buy` və `buildings/edit` müqaviləsi saxlanılıb. Reklam/real pul axını əlavə edilməyib. Android vizual uyğunluq/runtime/render yoxlanmayıb; emulator avtomatik açılmır, canlı yoxlama gate deyil.
- Əlaqəli yeni fayllar: app `PartsScreen.kt`; core `PartsMutations.kt`, `PartsMutationsTest.kt`; mövcud `AttemptCoordinator.kt`, `GameplayWire.kt`, `GameplayHttpBackend.kt`, `OnlineGame.kt`, `InventoryScreen.kt`.
- Növbəti böyük vahidə keçməzdən əvvəl istifadəçi tapşırığı gözlə; dizaynın renderer olmadan birəbir uyğunluğunu və mərhələ 4 tamamlanmasını iddia etmə. Plan yalnız `docs/PLAN.md`-dir.


## Son review davamı — 13 noyabr 2026
- Current state: `feat/phase-4` / `09185dc`; cari inteqrasiya `PLAN.md` mərhələ 4 ilə tutuşduruldu, implementation dəyişmədi. AppBackend-ə toxunulmadı.
- Completed/Checks: parts wire/receipt/coordinator/domain və UI giriş/çıxış statik review; yeni təsdiqlənmiş bug yoxdur. Java 21 fokuslu core/engine/app unit + assembleDebug/lintDebug BUILD SUCCESSFUL (107/108 UP-TO-DATE); diff check PASS. Runtime/render/CI yoxdur.
- Remaining work: silmə; placement DTO/client və atomik yerləşdirmə/dəyişdirmə/qaytarma; dekor və torpaq alışı; real şəhər renderer-i/əhali/canlılıq. Mövcud parts buy→unlock→apply→detail tam axınının regression yoxlaması və mobil vizual müqayisə açıqdır.
- Decisions: reklam/billing mərhələ 6, bazar/leaderboard mərhələ 5; planşet/landşaft hazırkı iş deyil. Yeni feature, merge/push başlamadı.
- Relevant files/Exact next step: `docs/reviews/2026-11-13-phase4-parts-flow.md` son bölməsi; növbəti review vahidi üçün `PartsMutationsTest.kt` və `PartsScreen.kt` əsasında uğurlu buy/apply və rədd ssenarilərinin regression əhatəsini tamamlamaq; yeni böyük vahid yalnız istifadəçi tapşırığı ilə.


## Son istifadəçi qərarı və silmə vahidi — 13 noyabr 2026
- Decisions: istifadəçi şəhərə yerləşdir/yerini dəyiş/inventara qaytar/atomik dəyiş və əyrilik/sahə yoxlamaları bəndini bağlı saymağı, qalan işləri davam etdirməyi tapşırdı. `PLAN.md` §1-də qeyd edildi. Bu, test/implementasiya PASS iddiası deyil; əvvəlki placement TODO-larını cari tapşırıq kimi götürmə. AppBackend-ə yazı qadağası qalır.
- Completed: bina silmə core adapteri, wire/HTTP route, receipt validation və mövcud durable journal recovery inteqrasiyası working diff-dədir. UI hələ qoşulmayıb. Review: `docs/reviews/2026-11-13-phase4-building-delete.md`.
- Current state: `feat/phase-4` / `09185dc`; əvvəlki diff/release faylları qorunur. `BuildingDeletion.kt`, `BuildingDeletionTest.kt` yenidir; PartsMutationsTest stale Ready testi delete üçün də genişləndi. Backend yalnız read-only müqavilə/kod yoxlaması, dəyişiklik yoxdur.
- Checks performed: Java21 core/engine/app unit + assembleDebug/lintDebug PASS; son test əlavəsindən sonra core:test yenidən PASS; diff check PASS. Kontrast 3 cüt PASS (min8.22). Android runtime/render/CI yoxdur.
- Remaining work: silmə UI təsdiqi və qoşulması; dekor/torpaq; real şəhər renderer-i və V1 canlılıq; mövcud parts axınının tam regression/vizual uyğunluğu. Bütün mərhələ bitmiş sayılmır.
- Relevant files: `BuildingDeletion.kt`, `BuildingDeletionTest.kt`, `GameplayWire.kt`, `GameplayHttpBackend.kt`, `InventoryScreen.kt`, `GameTokens.kt`; təsdiqli `docs/design/city-completion-v1/04-building-detail.png`.
- Exact next step: istifadəçiyə delete confirmation nümunəsini göstər və konkret təsdiqini al (AGENTS UI qapısı). Silmə girişinin görünüşü də təsdiqlənməlidir. Sonra həmin dizayn əsasında InventoryScreen-ə aydın təsdiqli silmə axınını qoş, server uğurundan sonra inventarı yenidən oxu. AppBackend yazısı/CI/merge/push etmə.


## Silmə UI təsdiqi və inteqrasiya — 13 noyabr 2026
- Decisions: istifadəçi “okeydir” ilə `delete-confirmation-proposal.png` dialoqunu və detail sonunda “Binanı sil” girişini açıq təsdiqlədi. Yenidən təsdiq istəmə.
- Completed/Current state: `InventoryScreen.kt`-də silmə girişi və təsdiqli dialoq core `BuildingDeletion`-a qoşuldu. Ləğv et/outside/Back dispatch etmir; yalnız “Binanı sil” təsdiqi exact Ready ilə submit edir. Dispatch zamanı köhnə detail/list seçimindən çıxır, növbəti Ready-də inventory serverdən yenidən oxunur; lokal wallet/refund və optimistik ownership silinməsi yoxdur. Böyük şriftdə dialoq düymələri şaqulidir, izahat scroll edə bilir.
- Relevant files: `InventoryScreen.kt`, `BuildingDeletion.kt`, `BuildingDeletionTest.kt`, `docs/reviews/2026-11-13-phase4-building-delete.md`; `docs/design/city-completion-v1/README.md` təsdiq statusu yeniləndi.
- Remaining work: dekor/torpaq və real şəhər renderer-i; parts tam axın regression/vizual uyğunluğu. Placement bağlıdır. Android silmə UI render/runtime yoxlanmayıb; birəbir vizual PASS yoxdur. Emulator avtomatik açılmır.
- Exact next step: yeni chatdə handoff və `docs/PLAN.md` oxu; dekor vahidi üçün mövcud `docs/design/city-completion-v1/03-decoration-flow.png` dizaynını göstər və konkret təsdiq al; təsdiqədək UI başlama. AppBackend dəyişmə/CI/merge/push yoxdur.

- Son UI kodunda Checks performed: Java21 `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` BUILD SUCCESSFUL (52s; 15 executed,91 up-to-date), `git diff --check` PASS. CI/merge/push yoxdur.
