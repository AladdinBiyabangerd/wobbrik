# Mərhələ 4 — hissələr/mağaza və binaya tətbiq

- Branch: `feat/phase-4`; yoxlanmış baza HEAD `09185dc`, nəticə working diff-dədir. Əvvəldən mövcud inventory garden/polish və `app/release/` istifadəçi faylları qorunub. Commit/merge/push yoxdur.
- Mənbə: `docs/PLAN.md` §§1/2/4/6; açıq təsdiqli `docs/design/city-completion-v1/02-parts-flow-v2.png` oxunub və görüntüsünə baxılıb.

## İnteqrasiya

- İnventar → Hissələr/Mağaza; bina detalı → Bina görünüşü → kateqoriya → açıq hissə seçimi → hazırkı/seçilmiş müqayisəsi → Tətbiq et.
- Server kataloqu, category counts, qiymət, açılmış hüquqlar və reklam irəliləyişi oxunur. Alış yalnız `coins` bina hissələrinə aiddir; free/achievement/reward/real pul hüquqları client-də verilmir. Reklam və real pul SDK axını hazır olmadığı açıq göstərilir.
- `PartsMutations` UI-dan domain/submit yoxlamalarını ayırır. Tətbiq bütün beş hissəni yoxlayır; alınmış binadakı bağlı hissə hesabda açılmır və server qaydasına uyğun bütün göndərilən hissələr hesabda açıq olmalıdır.
- `SHOP_BUY` və `BUILDING_EDIT` mövcud `AttemptCommand.Route`-a əlavə olunub; eyni `AuthenticatedRecoveryOwner`/coordinator/`DiskAttemptJournal` yazıçısı işləyir. Disk formatının versiyası və əvvəlki route adları dəyişmir. Yeni command-ları anlamayan historical client onları rədd edir və baytları saxlayır.
- Şəbəkədən əvvəl exact envelope saxlanır; operation ID/account revision/building revision dəyişmədən retry edilir. Commit receipt yoxlanır, sonra `/sync` snapshot-u balans/hüquqları əvəz edir; lokal wallet delta yoxdur.
- Hər Ready dəyişəndə bina yenidən serverdən oxunur; gec cavab session və exact Ready üzrə rədd edilir. Same-UID re-login köhnə capability ilə alış/edit dispatch edə bilmir. Draft yalnız təqdimatdır; Tətbiq et-dən əvvəl mutasiya deyil.
- Documented definitive shop/balance/building/listing/not-found/invalid-request rəddində pending silinir və sync tələb edilir; naməlum nəticə, operation payload conflict, auth/transport/version problemi pending envelope-u saxlayır. Revision conflict avtomatik rebase edilmir.

## Lokal diff review və düzəlişlər

- Yeni enum route-larına görə attempt-only engine fixture və historical fixture exhaustive branch-ləri yeniləndi; historical client yeni route-u qəbul etmir.
- Aktiv elandakı edit `listing_conflict` verir; journal-ı əbədi bloklamaması üçün backend-in mövcud error mapping-i read-only yoxlanıb düzgün definitive rədd siyahısına salındı. Eyni yanaşma serverin `invalid_request` rəddinə tətbiq edildi.
- Xarici modulun nullable price property-si üçün smart-cast compile problemi nullable `let` ilə düzəldildi.
- Böyük şriftdə ikinci dərəcəli preview/müqayisə məzmunu scroll sahəsinə alındı; əsas Tətbiq et/Geri düymələri onun xaricindədir. Runtime uyğunluq iddiası yoxdur.

## Yoxlamalar

- Java 21: `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` — PASS, son kodda BUILD SUCCESSFUL (14s). `PartsMutationsTest` 4/4 PASS; `git diff --check` PASS.
- `PartsMutationsTest`: hər iki route üçün lost response → journal recreation → exact retry, hesab üzrə journal izolyasiyası, receipt identity/content revision yoxlaması, envelope extra field rəddi, definitive/ambiguous error fərqi və same-UID stale Ready rəddi.
- Token kontrastı: Text/Surface 13.00, Muted/Surface 8.22, Background/Accent 11.45, Success/Surface 9.28 — PASS.
- AppBackend yalnız müqavilə və əlaqəli mövcud kod üçün read-only yoxlanıb; API/DTO/schema/rules dəyişmir, backend dəyişiklik/test/deploy yoxdur.
- CI/main merge/push yoxdur. Emulator/ADB/runtime/Android render yoxdur; mövcud screenshot test infrastrukturu tapılmayıb və yeni stack əlavə edilməyib.

## Vizual məhdudiyyətlər

Android renderi olmadığı üçün təsdiqli lövhə ilə yanaşı müqayisə və birəbir vizual uyğunluq PASS deyil. Mövcud tokenlər, HomeButton və catalog atlasları istifadə olunur; yeni raster asset yaradılmayıb. Lövhədəki tam parlaq çərçivə/ikon/hero eyniliyi və alt Şəhər/İnventar/Bazar naviqasiyası tam vizual qəbul deyil: bu vahidin işlək giriş/çıxışı inventardır; şəhər/bazar vahidləri əlavə edilməyib. Kiçik/normal telefon portret, font 1.0/2.0, inset və accessibility runtime renderləri yoxlanmayıb. Planşet və mobil landşaft işi son istifadəçi qərarına əsasən edilməyib. Bu qeyd mərhələ 4-ün bütöv qəbulu deyil.


## Davam review-su — 13 noyabr 2026

- İstifadəçi tapşırığı ilə `feat/phase-4`, HEAD `09185dc` və cari tracked/untracked parts inteqrasiyası handoff və `docs/PLAN.md` §1 mərhələ 4, §§2/4/6/7 ilə tutuşduruldu. Bu keçiddə implementation dəyişdirilmədi; AppBackend açılmadı/dəyişdirilmədi.
- Oxunan yollar: `PartsScreen`, inventory giriş/çıxışı, `OnlineGame`, `PartsMutations`, wire/HTTP route və receipt yoxlaması, coordinator/journal recovery, `PartsMutationsTest`, inventory DTO/repository və value breakdown. Yeni təsdiqlənmiş funksional bug qeydə alınmadı; bu, tam məhsul və vizual qəbul deyil.
- Mövcud testlər hər iki route üçün durable exact retry və stale Ready rəddini əhatə edir. Lakin bunlar real UI-dan uğurlu buy → unlock → seçim → apply → yenilənmiş detail axınının testi deyil. Alınmış binanın bağlı hissələri, aktiv elan, başqa cihazın revision dəyişikliyi üzrə tam istifadəçi axını yoxlanmayıb.
- Yenidən yoxlama (Java 21): `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` — BUILD SUCCESSFUL, 1s, 108 task-dan 107 UP-TO-DATE. Bu, testlərin yenidən icrası deyil, cari input-lar üçün Gradle yoxlamasının uğurlu nəticəsidir. İlkin sandbox cəhdi Gradle lock-a çıxışda dayandı; eyni əmr genişləndirilmiş icazə ilə uğurla tamamlandı. `git diff --check` PASS.
- Mərhələ 4-də qalan mobil iş: aydın təsdiqli bina silmə (refund yoxdur, aktiv elan məhdudiyyəti); typed geometry/position/parcels/decor DTO-ları və placement client; yerləşdir/yerini dəyiş/inventara qaytar/atomik bina dəyiş; dekor nüsxələrinin alışı və yerləşdirilməsi; level/qiymət üzrə beş daimi torpaq genişlənməsi; real hesab şəhərinin renderer-i, yerləşmiş əhali və V1 yüngül canlılıq. Cari `CityMembership` yalnız aidlik/əhali oxuyur, placement modeli deyil; Home şəhər girişi disabled-dır.
- Mövcud inventory/detail/parts vahidlərinin təsdiqli dizayna uyğunluğu və kiçik/normal mobil portret, böyük şrift, inset/a11y halları üçün render sübutu açıqdır. Bu keçiddə vizual qiymətləndirmə aparılmadı. Canlı yoxlama tələb edilmir və gate deyil. Planşet və mobil landşaft cari iş deyil.
- Reklam hissələrinin açılması və real pul axını mərhələ 6; bazar və leaderboard mərhələ 5-dir. Bunlar mərhələ 4-ün ayrıca mobil şəhər işləri ilə qarışdırılmır. `PLAN.md` §1-də mağaza/edit-in “sonrakı vahid” ifadəsi əvvəlki statusdur: hazırda bunlar working diff-dədir, tam qəbul olunmayıb.
- CI/merge/push/deploy yoxdur. Növbəti kiçik addım: mövcud parts buy/apply istifadəçi axını üçün fokuslu regression əhatəsini tamamlamaq; sonra istifadəçinin ayrıca istədiyi mərhələ 4 vahidinə keçmək. Backend uyğunluğu bu keçiddə yenidən yoxlanmayıb; əvvəlki müqavilə review-su yuxarıdadır.
