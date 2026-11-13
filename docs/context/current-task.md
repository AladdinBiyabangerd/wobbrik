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

## Dekor/torpaq alış vahidi — 13 noyabr 2026
- Completed: əvvəlki inventory/parts/delete diff artıq `c6c51de` commit-indədir. Dekor/torpaq alış adapteri və UI girişləri əlavə edildi; dekor copy inventory serverdən paginated oxunur. Alış mövcud SHOP_BUY + eyni durable journal/recovery/sync ilədir.
- Current state: `feat/phase-4`; release APK/metadata qorunur və commit-ə daxil edilmir. Placement bağlıdır, AppBackend yazısı yoxdur.
- Decisions: istifadəçi dekor dizaynını və torpaq siyahısı/alış təsdiqini açıq təsdiqlədi. Torpağın map selection hissəsi yoxdur, əməl “Al”dır. Mövcud dəyişiklikləri uyğun commit etmək açıq istənildi.
- Remaining work: Android mobil portret/böyük şrift/inset/a11y renderləri və dizaynla müqayisə açıqdır; art crop/halo/native-pixel hədləri review-da qeyd olunub. Real şəhər renderer-i/V1 canlılıq və əvvəlki parts axınının vizual qəbulu qalır; mərhələ bütöv bitməyib.
- Relevant files: `CityPurchases.kt`, `DecorationReads.kt`, `CityPurchasesTest.kt`, `PartsMutationsTest.kt`; app `DecorationShopScreen.kt`, `LandShopScreen.kt`, `InventoryScreen.kt`, `OnlineGame.kt`, `WobbrikApplication.kt`; `docs/reviews/2026-11-13-phase4-city-purchases.md`; yeganə plan `docs/PLAN.md`.
- Checks performed: core unit testlər (XP/balans/daimi hüquq/təkrar copy, malformed pages/receipt, lost-response disk recovery, stale Ready) PASS; core/app unit + assembleDebug/lintDebug PASS ilk UI kodunda. Son UI əlavələrindən sonra eyni yoxlamalar yenidən başladılıb; son nəticə review qeydinə əlavə edilir. Kontrast 4 cüt PASS min8.22; diff check PASS. Android runtime/render/backend test/CI yoxdur.
- Exact next step: son fokuslu yoxlamanın nəticəsini review et, bu alış vahidini ayrıca commit et; main merge/push/CI yoxdur. Sonra yalnız cari alış vahidinin açıq vizual uyğunluq işini davam etdir; placement-i yenidən açma və AppBackend-ə toxunma. Böyük yeni şəhər renderer vahidi bu chatə yığılmamalıdır.
- Son nəticə: Java21 core/app unit + assembleDebug/lintDebug PASS (11s); CityPurchasesTest4/4, PartsMutationsTest4/4; diff check PASS. Alış vahidi lokal commit üçün review edilib; runtime/render PASS yoxdur.

## Dekor/torpaq vizual davamı — 13 noyabr 2026
- Completed: alış vahidi artıq `faf4af7` commit-indədir. Cari working diff alış təsdiqlərini ortaq `CityPurchasePanel.kt` çərçivəsinə keçirir; torpaq xülasəsini siyahı scroll-undan ayırır, daimi hüquq mətni əlavə edir. Düymələr normal şriftdə yanaşı, böyüdülmüş şriftdə şaqulidir; izahat ayrıca scroll edir.
- Current state: `feat/phase-4` / `faf4af7`; bu davam commit edilməyib. Release istifadəçi faylları qorunur.
- Decisions: əvvəlki dekor və torpaq alış dizayn təsdiqləri qüvvədədir. Placement bağlıdır, AppBackend-ə toxunulmur; emulator açılmır, CI/merge/push yoxdur.
- Remaining work: Android render və dizaynla birəbir uyğunluq açıqdır. Crop/halo/native-pixel keyfiyyəti, kart/ikonlar, kiçik/normal portret font1/2, inset/a11y yoxlanmayıb. Yeni şəhər renderer vahidi başlanmayıb.
- Relevant files: `CityPurchasePanel.kt`, `DecorationShopScreen.kt`, `LandShopScreen.kt`; `docs/reviews/2026-11-13-phase4-city-purchases.md`; `docs/design/city-completion-v1/{03-decoration-flow,05-land-purchase}.png`; yeganə plan `docs/PLAN.md`.
- Checks performed: Java21 `:app:compileDebugKotlin :app:lintDebug` PASS (11s), diff check PASS, 3 kontrast cütü PASS (min8.22). Android runtime/render yoxdur; host screenshot infrastrukturu layihədə yoxdur. Build vizual qəbul deyil.
- Exact next step: cari kiçik panel diff-ni review et; növbəti uyğunluq işində əvvəl host Android render imkanını müəyyənləşdir (emulator avtomatik açma, yeni stack pinləri uydurma). Render mümkün olmazsa məhdudiyyəti saxla; təsdiqli art-ı özbaşına dəyişmə, placement və AppBackend işini açma.

## Kart/ikon/dekor crop davamı — 13 noyabr 2026
- Completed: dekor kartları/qiymət pill-i/cyan kontur, kateqoriya və alış vektor ikonları, alış panelində art+divider+izahat, torpaq kartlarının yığcam/detallı görünüşü və adaptiv başlıq cari working diff-dədir. `CityPurchaseArt.kt` + `purchase-art.json` 12 ölçülmüş crop və native-pixel scale≤1 verir; orijinal dekor atlası dəyişməyib.
- Current state: `feat/phase-4` / `faf4af7`; bu davam hələ commit edilməyib. Əvvəlki panel diff-i daxildir. `app/release/` istifadəçi faylları qorunub.
- Decisions: placement bağlıdır, AppBackend yazısı yoxdur. Əvvəlki dekor/torpaq alış təsdiqləri qüvvədədir. Yeni şəffaf torpaq cutout namizədi göstərildi; orijinaldan xırda detallar fərqləndiyi üçün ayrıca tətbiq təsdiqi async soruşuldu, cavab HƏLƏ YOXDUR. Təsdiqsiz tətbiq etmə.
- Remaining work: aktiv torpaq 153×141px crop-un tünd düzbucaqlı matte-i qalır. `docs/reviews/city-purchases-visual/land-thumbnail-candidate.png` bunun şəffaf yüksək çözünürlüklü namizədidir, app assets-də deyil. Tam piksel eyniliyi/mərhələ qəbulu iddiası yoxdur; server nüsxə/status mətnləri, tall portrait spacing və mövcud inventory return naviqasiyası review-da qeyd olunub. Real inset/TalkBack/cihaz runtime açıqdır.
- Relevant files: `DecorationShopScreen.kt`, `LandShopScreen.kt`, `CityPurchase{Art,Chrome,Panel}.kt`, `assets/city/purchase-art.json`, 6 city vector drawable; `scripts/check-city-purchase-art.py`; review `docs/reviews/2026-11-13-phase4-city-purchases.md` son bölmə; render evidence/harness `docs/reviews/city-purchases-visual/`; yeganə plan `docs/PLAN.md`.
- Checks performed: 6/6 host native Compose render ssenarisi PASS, 12 list/confirmation PNG-yə və design comparison-lara baxıldı: dekor/torpaq 360×640dp font1/2 və390×844dp font1, density3/API34. 12/12 source crop QA PASS, tünd/açıq fonlarda baxıldı. Normal Java21 core/app unit + assembleDebug/lintDebug PASS (15s); kontrast5cüt PASS(min8.22); diff check PASS. Emulator açılmadı; CI/backend test/merge/push yoxdur. Host harness production build pinlərini dəyişmir.
- Exact next step: istifadəçinin torpaq cutout təsdiq cavabını yoxla. Təsdiq varsa namizədi app city assets-ə ayrıca adla köçür, JSON-dakı land bounds-u aktiv metadata-ya əlavə et, `rememberLandArt`-ı ona keçir və host land render/build/lint-ni yenilə. Təsdiq yoxdursa orijinalı saxla, namizədi tətbiq etmə. Bu chatdə başqa böyük vahid açma; yeni chat üçün bu handoff/review və PLAN.md kifayətdir.

## Son qərar — torpaq cutout-u tətbiq edildi
- Completed/Decisions: istifadəçi “Bəli, şəffaf cutout-u tətbiq et” ilə namizədi açıq təsdiqlədi. `city/land-thumbnail-v2.png` app assets-ə dəyişdirilmədən kopyalandı; `purchase-art.json.land` və `rememberLandArt()` ilə siyahı+alış panelinə qoşuldu. Əvvəlki “cavab yoxdur/aktiv matte qalır” qeydləri artıq cari vəziyyət deyil.
- Checks performed: torpaq3/3 host render PASS(15s); altı yeni land list/confirmation PNG-si/contact sheet-ə və tünd/açıq fon cutout QA-sına baxıldı, matte yoxdur. Alpha/bounds QA PASS; Java21 assembleDebug/lintDebug PASS(17s), diff check PASS. Əvvəlki unit/kontrast nəticələri qüvvədədir; domain/rəng tokenləri dəyişməyib.
- Current state: `feat/phase-4` / `faf4af7`, bütün vizual davam working diff-dir, commit/merge/push yoxdur. Placement bağlıdır, AppBackend dəyişmir. CI/emulator yoxdur. Məhdudiyyətlər əvvəlki bölmədə/review-da qalır; tam mərhələ qəbulu deyil.
- Relevant files: CityPurchaseArt.kt; assets/city/{purchase-art.json,land-thumbnail-v2.png}; scripts/check-city-purchase-art.py; docs/reviews/city-purchases-visual/ və 2026-11-13-phase4-city-purchases.md son bölmə.
- Exact next step: bu kiçik alış vizual diff-inin review/qəbulunu yekunlaşdır; yeni feature/şəhər renderer-i başlama. Cutout üçün yenidən təsdiq istəmə. Növbəti böyük vahid yeni chat və ayrıca istifadəçi tapşırığı ilədir.

## Commit təhvil qeydi — 13 noyabr 2026
- İstifadəçi bu vizual alış vahidini commit etməyi tapşırdı. Kart/ikon/panel, 12 dekor crop-u, təsdiqli şəffaf torpaq cutout-u və review/render sübutları eyni lokal commit-ə daxil edilir. Əvvəlki “commit edilməyib” qeydləri bu commit-dən əvvəlki tarixçədir.
- Branch `feat/phase-4`; review bazası `faf4af7`. Son uyğun yoxlamalar yuxarıda qeyd edilib; commit öncəsi diff check PASS. Owner `app/release/` faylları xaricdə saxlanır. Merge/push/CI yoxdur.
- Növbəti addım: yalnız istifadəçinin ayrıca göstərişi ilə review/qəbul və ya növbəti vahid; placement/AppBackend açılmır.
