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

## Şəhər renderer-i başlanğıcı — 13 noyabr 2026 (cari)
- Decisions: istifadəçi göstərilmiş `city-view.png` və V1 canlılığı “basla” ilə təsdiqlədi. Yenidən eyni təsdiqi istəmə. İstifadəçinin öz tikdiyi/yerləşdirdiyi binalar həqiqi mərtəbə sayını, əyriliyini və hissələrini saxlamalıdır. Placement bağlı qalır.
- Decisions (əvvəlki backend qadağasını əvəz edir): AppBackend-də ehtiyac olan dəyişikliklərə icazə var; onun main-indən ayrıca branch aç, worktree yaratma. Bu oxu vahidində backend dəyişikliyi tələb olunmadı və edilmədi.
- Completed: `CitySceneReads.kt` server parcel/position oxusu, paginated city + building detail + placed decoration səhnə yığımı; revision bracket/mixed-page rəddi, legacy null mövqe, canonical tick yoxlamaları. `WobbrikApplication.Configured.cityScene` eyni auth backend-ə qoşulub.
- Current state: `feat/phase-4`, baza artıq `bb02b50` (əvvəlki alış vizualı commit olunub); yeni scene reader/test/bootstrap diff-i commit edilməyib. İstifadəçi release faylları qorunur. Ekran/renderer/animasiya hələ əlavə edilməyib.
- Checks performed: Java21 core:test PASS; CitySceneReadsTest7/7. Son bootstrap compile/lint nəticəsi review-da. UI/render/runtime/CI/backend test yoxdur.
- Relevant files: core `CitySceneReads.kt`, `CitySceneReadsTest.kt`; app `WobbrikApplication.kt`, `InventoryScreen.kt` (`BuildingPreview`), `CosmeticArt.kt`, `GameTokens.kt`; review `docs/reviews/2026-11-13-phase4-city-scene-reads.md`; dizayn `docs/design/city-view.png`; yeganə plan `docs/PLAN.md` §§1/4/7.
- Remaining work/Exact next step: həmin məlumat modelindən real şəhər səhnəsini çək: əvvəl mövcud GameScene/cosmetic atlasları və native crop/anchor hədlərini yoxla, accepted floor geometry + server x/y/quarter-turn projection/depth ordering qur, real parcel union-una ada səthi oturt; sonra təsdiqli şəhər UI/bina seçimi və V1 canlılıq. Şəhəri sabit hazır raster və ya sadə placeholder bina ilə əvəz etmə. Asset/kamera məhdudiyyəti təsdiqli görünüşə mane olarsa açıq göstər, özbaşına alternativ tətbiq etmə. Host Compose render harness-i `docs/reviews/city-purchases-visual/`-dədir; emulator açma. Merge/push/CI göstərişi yoxdur.

- Son yoxlama: Java21 `:core:test :app:compileDebugKotlin :app:lintDebug` PASS (28s); `git diff --check` PASS. UI/render/CI yoxdur.

## Renderer asset sərhədi — 13 noyabr 2026
- Completed: istifadəçinin cari tapşırığı üzrə handoff/PLAN/CitySceneReads və mövcud BuildingPreview/atlas anchor-ları yoxlandı; city-view, wall atlas və land cutout source şəkillərinə baxıldı. Review: `docs/reviews/2026-11-13-city-renderer-art-boundary.md`.
- Current state: renderer/UI kodu əlavə edilməyib; əvvəlki scene-reader/bootstrap owner diff və release faylları qorunur. Placement bağlıdır.
- Açıq texniki sərhəd: atlas hər hissə üçün bir baxış sprite-idir; quarter-turn/kamera dönüşü üçün səth və əlavə baxış məlumatı yoxdur. Alış land cutout-u real parcel union-u deyil və sabit ağaclar daşıyır. Bunları təsdiqli şəhər səhnəsinin tam əvəzi sayma.
- Checks: source inspeksiyası; yeni build/render/CI/backend yoxlaması yoxdur. Android vizual PASS yoxdur.
- Exact next step: mövcud atlaslarla ilkin sabit bucaq təqdimatına istifadəçi razılığını və ya modul asset istiqamətini dəqiqləşdir; bundan əvvəl sadələşdirilmiş şəhər görünüşü tətbiq etmə. Real binaların mərtəbə sayını, qəbul edilmiş ofsetlərini, hissələrini və server mövqeyini saxla; legacy null mövqeni uydurma.

## Cari handoff — sabit şəhər bina qatı və növbəti oyun UI fix-i
- Completed: istifadəçi sabit kameranı “beli” ilə təsdiqlədi. `CityScreen.kt` real scene repository-yə qoşuldu; `CityBuildingProjection.kt` server position/quarter-turn və accepted mərtəbə ofsetlərini qoruyur, depth sort/bounds/focus verir. Server parts mövcud atlasdan, native pixel cap ilə çəkilir. `BuildingArt.kt` inventarla ortaq anchor helper-dir. Home mobil şəhər girişi açılıb. Placement bağlıdır, backend dəyişmədi.
- Current state: `feat/phase-4`, baza `bb02b50`; istifadəçi cari vahidi bitəndə commit etməyi tapşırdı. Scene reader/bootstrap daxil cari vahid commit edilir; release owner faylları xaricdədir. Merge/push/CI yoxdur.
- Decisions: sabit kamera qəbul edilib; yenidən təsdiq istəmə. Tam city-view uyğunluğu iddia edilmir: terrain parcel union/dekor/tam chrome/orbit/V1 canlılıq açıqdır. Əvvəlki art-boundary blok sualı bağlandı.
- Checks performed: 6/6 host native Compose render testi +5/5 projection unit PASS; 9 screenshot/contact/design comparison açılıb baxıldı. 360×640 font1/2,390×844 font1, overview/focus, empty/error və Home entry/click. Normal Java21 core/engine/app unit +assembleDebug/lintDebug PASS(24s); 4 kontrast cütü PASS(min8.22),diff check PASS. Real hesab/device/inset/TalkBack/backend test/CI/emulator yoxdur.
- Relevant files: `CityScreen.kt`, `CityBuildingProjection.kt`, `BuildingArt.kt`, `HomeScreen.kt`, `OnlineGame.kt`, `InventoryScreen.kt`; reader/bootstrap; `docs/reviews/2026-11-13-phase4-city-buildings.md`, `docs/reviews/city-scene-visual/`; yeganə plan `docs/PLAN.md`.
- Remaining work / Exact next step: istifadəçi commit-dən sonra ayrıca aktiv oyun UI problemini düzəltməyi tapşırdı. Screenshot: `/var/folders/02/vb81250n1ld9gwqlq74ybm000000gn/T/codex-clipboard-22dc0624-dab8-41b9-b618-d23b60a3198e.png`. Mərtəbə panel seams/tünd boşluqlar, game island-da hazır ev üzündən ikinci baza/giriş, aktiv oyunda dam, kəndir-floor anchor ayrılması. `GameScene.kt` oxundu, aktiv roof çağırışı təsdiqləndi; dəyişdirilməyib. Yeni chatdə ui-craft/token/game-tap-v2 və screenshot-u oxu, mövcud təsdiqli istinadı təqdim et, konkret fix UI təsdiqini mövcud söhbət göstərişi ilə tutuşdur; fizika/accepted geometry-ni dəyişmədən anchors/crops/layering/roof phase düzəlişini hazırla. Yeni dizayn alternativi uydurma, emulator/CI açma. Başqa şəhər feature-i başlama.

## Active game joins + two atlas proposals — 13 November 2026
- Completed: separate `fix/game-scene-joins` from `e308d62`. GameScene visual pitch/full-width wall drawing, building-free garden in both callers, ground-only entrance, active roof removal and cable anchors/framing corrected. Physics/SceneMotion/accepted geometry/backend unchanged. Review `docs/reviews/2026-11-13-game-scene-joins.md`.
- Current state: working diff, no commit/merge/push/CI. Owner `app/release/` intact. Existing shallow wall art cannot satisfy full game-tap-v2 geometry; do not claim design acceptance.
- Decisions: user explicitly permits old atlas removal if needed and asks TWO complete color sets, same game-tap-v2 style/forms, mainly colors changed. Built-in imagegen produced purple/pink and teal/ivory proposals; no asset approval yet, not installed. Do not request generation scope again.
- Checks performed: 7 native host production-Canvas render fixtures PASS; final Java21 assembleDebug/lintDebug +engine:test PASS(24s), diff check PASS. Final screenshots viewed; no emulator/device/account/HUD/font/inset/a11y/CI/backend checks. PNG alpha exists; generated exterior haze still requires cleanup.
- Relevant files: GameScene.kt, MainActivity.kt, OnlineGame.kt; `docs/reviews/game-joins-visual/render/`; `docs/design/game-tap-v2/atlas-v3-proposal/{purple,teal}.png` and README (prompt set/limitations); plan only `docs/PLAN.md`.
- Remaining work: concrete style approval; clean alpha + matching crop/anchor geometry, independent-part layering compatibility without breaking 68-item ownership catalog; then install approved assets and render seams/ropes/ground/finish. Fallback lacks a dedicated entrance; new ground asset must resolve it. No old atlas deletion before consumer/ID compatibility checked.
- Exact next step: show the two saved proposals and receive visual approval under AGENTS; then continue only this game-scene/atlas integration in new chat. No new city feature, automatic emulator, CI or merge/push.

## Atlas təmizləmə davamı — 13 noyabr 2026
- Decisions: istifadəçinin “təsdiqlədiyim iki atlası təmizləyib GameScene-ə qoş, fizikanı dəyişmə” göstərişi purple/teal dizayn təsdiqidir; yenidən vizual təsdiq istəmə.
- Checks performed: handoff/PLAN/ui-craft/imagegen, orijinal iki atlas, GameScene/CosmeticSprite loader və mövcud host render harness oxundu. Built-in imagegen ilə purple iki dəfə, teal bir dəfə background-extraction sınandı; çıxışlarda xarici halo qalır, app-ə tətbiq edilmədi. Yeni kod/build/render/CI yoxdur; əvvəlki working diff qorunur.
- Current state/Remaining work: imagegen alternativ redaktə üsulu üçün açıq istifadəçi seçimi tələb etdiyindən kontur maskası ilə skript təmizliyinə async icazə sualı göndərildi; cavab gözlənilir. Asset inteqrasiyası başlanmayıb. Fizika/backend dəyişməyib.
- Exact next step: kontur maskasına cavabı yoxla; icazə varsa orijinal iki atlasın daxili piksellərini saxlayaraq xarici halo-nu təmizlə, crop/anchor metadata və GameScene render qoşulmasını hazırla. Tam kompozit sprite-lərin pəncərə/giriş/lampa daşıdığını nəzərə al: duplicate overlay etmə, qarışıq 68 hissə kataloqunu səssiz əvəz etmə; köhnə atlasları compatibility sübutu olmadan silmə. Host production Canvas renderində iki rəng, seams, swing/ropes, ground və finish-i yoxla; emulator/CI açma.

## Cari nəticə — təsdiqli iki atlas GameScene-də
- Decisions: istifadəçi kontur maskası ilə skript təmizliyinə açıq icazə verdi; əvvəlki gözlənilən cavab qeydi bağlanıb. Purple/teal təsdiqlidir.
- Completed: orijinal RGB-ni dəyişmədən xarici alpha halo maskalandı; `assets/game-v3/{purple,teal}.png` + crop/pitch/depth/top-anchor metadata, `GameAtlas.kt` loader və GameScene/both callers inteqrasiyası hazırdır. Giriş yalnız accepted foundation-da, dam yalnız finish-də; composite-da duplicate overlays yoxdur. Renderdə tapılan kəndir ucu/guide plane ölçülmüş anchor-larla düzəldildi.
- Current state: `fix/game-scene-joins`, baza `e308d62`, working diff; commit/merge/push/CI yoxdur, owner app/release qorunub. Purple partless/local və tam base_1; teal tam base_2; qarışıq/digər 68-item seçimləri köhnə independent art-da qalır. Köhnə atlaslar silinməyib. Fizika/SceneMotion/accepted geometry/core/backend diff-i yoxdur.
- Checks performed: 12/12 native host Canvas render PASS, iki rəng dark/light alpha QA və final contact/design comparison görüntülərinə baxıldı. 360×640dp və390×844dp; straight/swing/empty/finish/one floor/40 floors/mixed catalog. Java21 offline engine:test +app:testDebugUnitTest +assembleDebug +lintDebug PASS(12s); diff check PASS. UI rəng/mətn tokenləri dəyişmədiyindən kontrast təkrarlanmadı.
- Remaining work/limits: tam phone.png uyğunluğu iddia edilmir; framing/rope length/background/HUD/garden fərqləri qalır. Picker/inventory/city 68-item art migrasiyası bu vahiddə deyil. Full HUD/font2/inset/a11y/live/device/account yoxlanmayıb, emulator/CI açılmayıb. Bunlar runtime PASS deyil və avtomatik gate yaratmır.
- Relevant files: `GameAtlas.kt`, `GameScene.kt`, `MainActivity.kt`, `OnlineGame.kt`, `assets/game-v3/`, `scripts/clean-game-atlases.py`; review `docs/reviews/2026-11-13-game-scene-joins.md`, `docs/reviews/game-joins-visual/atlas-integration-contact.png`; dizayn `docs/design/game-tap-v2/atlas-v3-proposal/`; plan yalnız `docs/PLAN.md`.
- Exact next step: cari atlas inteqrasiyasını istifadəçiyə təhvil ver; ayrıca göstəriş olmadan yeni feature/art migrasiyası/merge/push/CI başlama. Mövcud source və köhnə catalog atlases-i saxla.

## Atlas review və lokal commit — 13 noyabr 2026
- Completed/Decisions: istifadəçi review və problem yoxdursa lokal commit tapşırdı; merge/push/CI qadağası saxlanır. `e308d62` bazasına qarşı atlas/scene/callers/asset/metadata/script/harness diff-i lokal review edildi; yeni production bug tapılmadı.
- Fixes: yalnız host QA-da hissəsiz lokal default fixture-i düzəldildi, maksimal yellənmə fixture-ləri artıq sıfır bucaq deyil, 90 frame settle edilmiş yük bucağını yoxlayır. Fizika/engine/core/SceneMotion dəyişmir.
- Checks performed: düzəldilmiş son 12 host render 12/12 PASS(15s,0 skips/failures/errors); iki rəngin final rotated swing və local-default PNG-lərinə baxıldı. Production dəyişmədiyi üçün əvvəlki final unit/APK/lint PASS qüvvədədir; staged diff check PASS. Runtime/a11y/CI yoxdur.
- Current state: `fix/game-scene-joins`; bu review və atlas vahidi uyğun lokal commit-ə daxil edilir. Owner app/release xaricdə, PNG render evidence mövcud .gitignore üzrə lokal qalır; source/production atlaslar və harness versionlanır. Merge/push/CI yoxdur.
- Remaining work: əvvəlki tam ekran/runtime/a11y və full design parity məhdudiyyətləri qalır; yeni feature və 68-item migrasiya başlanmadı.
- Relevant files: əvvəlki atlas vahidi + `docs/reviews/2026-11-13-game-scene-joins.md` son review bölməsi, `docs/reviews/game-joins-visual/render/GameJoinsRenderTest.kt`; plan `docs/PLAN.md`.
- Exact next step: lokal commit nəticəsini istifadəçiyə bildir; ayrıca göstəriş olmadan merge/push/CI və yeni böyük vahid başlama.
