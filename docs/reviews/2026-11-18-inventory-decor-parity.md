# Dekor — asset/layout davamı

18 noyabr 2026. Branch `feat/inventory-decor-native`; yoxlanmış baza `ade78e8` üzərində bu vahidin working diff-i. Authority `docs/PLAN.md` §1.1 və təsdiqli `docs/design/inventory-decor-v1/proposal.png`. Yalnız Dekor; başqa ekran və yeni dizayn yoxdur. Owner-in iki release `.dm` və city-detail-costs review dəyişiklikləri saxlanıb.

## Dəyişiklik və lokal review

Built-in image_gen ilə təsdiqli illüstrasiyalardan şəffaf ayrıca atlas hazırlanıb: tək/cüt ağac, skamya, fənər, qısa yol və ağaclıq. Qıvrım dəmir dayaqlar, ağac/çiçək/daş altlıq və fənər silueti referansa uyğunlaşdırılıb. Original alpha saxlanır; asset-də UI, mətn, kart səthi və idarəetmə yoxdur. Mənbə və tam prompt `docs/design/inventory-decor-v1/assets/` altındadır. Production faylları `app/src/main/assets/city/owned-decor-{atlas-v1.png,art-v1.json}`. Mənbə crop-ları qonşu artı daxil etməmək üçün ayrıca yoxlanıb.

Yalnız `rememberOwnedDecorationArt` altı ID-ni yeni atlasla əvəz edir. Digər altı kataloq ID-si mövcud artı saxlayır; onlar proposal-da göstərilməyib. Bazar/Şəhər atlas xəritəsi dəyişmir. Decode mount üzrə IO dispatcher-dədir; kart/recomposition üzrə bitmap allocation yoxdur. `PurchaseArt` üçün yalnız Dekorun opt-in fit scaling-i var; default pixel cap qalır. Mətn və status əvvəlki real copy/catalog data-sından gəlir, şəkil nümunəsinin statusları kopyalanmayıb.

Kart art hündürlüyü normal şriftdə sütun enindən hesablanır; böyük şriftdə mövcud bir-sütun/scroll saxlanır. Mətn sətir hündürlükləri, art gutter-i və filtr boşluğu yığcamlaşdırılıb; CTA normal şriftdə mərkəzdə dar, böyük şriftdə tam enlidir. Yeni nisbətlər `GameTokens.DecorCollection` daxilindədir; rəng palitrası dəyişməyib. Fənər filtrində proposal-a uyğun konturlu native vector var. Minimum48dp əməliyyat sahəsi və refresh/filter/CTA input davranışı saxlanır.

Diff review: UI/data sərhədi, istifadə edilməyən import/ölçülər, əlavə read olmaması, ID mapping, atlas sərhədləri və digər renderer-lərin default davranışı yoxlandı; `git diff --check` PASS. Yeni API/domain/schema dəyişikliyi yoxdur; əvvəlki bounded decorations müqaviləsi dəyişmir, AppBackend dəyişikliyi lazım deyil.

## Yoxlamalar

- Son mənbə ilə `:app:assembleDebug`, `:app:lintDebug` PASS.
- İzolə native host matrisində DecorRegular/Small, font1/font2 və uzun ad/Long.MAX_VALUE balans/inset halları:5/5 PASS; `DecorationInventoryReadsTest`:1/1 PASS. Filter/refresh/retry/market/pagination/cache/error/empty/loading yoxlamaları saxlanır. Komanda əvvəlki `render.init.gradle`-i istifadə edir; layihə/CI konfiqurasiyasına əlavə yoxdur. Log `/private/tmp/decor-parity-verified.log`, `BUILD SUCCESSFUL in 53s`.
- Kontrast6/6 PASS, minimum7.41:1; `inventory-decor-parity/contrast.txt`.
- Bütün21 son native PNG və matrix açılıb baxıldı; `inventory-decor-parity/screens/`, `matrix.jpg`. `comparison.jpg` təsdiqli PNG/əvvəl/sonra müqayisəsidir (390×844 müqayisə paneli üçün uniform ölçüləmə); native originals saxlanır. Normal390×844/font1-də altı kartın ad/statusları ilk görünüşdə görünür. Kiçik və font2-də scroll lazımdır; uzun ad bütöv sözlərlə keçir, CTA görünür.
- İlk layout iterasiyasında üçüncü sıra statusları CTA altında qalırdı; son art-height nisbəti ilə düzəldildi. Bir aralıq host readback-də header/CTA/navbar qatları itirdi; həmin şəkil son sübut deyil. Son matrisi əldə etmək üçün capture helper ilk və ikinci çəkilişi ayrıca saxlayır; şəkillər sonradan qatlarla doldurulmayıb.

## Xərc və məhdudiyyətlər

Eyni20-copy JVM read fixture əvvəl/sonra cold2 sorğu/1149 cavab baytı, warm/offline0, refresh2 saxlayır. Dəyişiklik yeni network oxusu yaratmır. Əvvəlki atlas1086×1448/1,507,366 fayl baytı/6,290,112 RGBA baytı; yeni1536×1024/1,827,141 fayl baytı/6,291,456 RGBA baytı. İnventar digər altı ID üçün legacy atlası da saxlayır: təxminən əlavə6MiB decode yaddaşı və1.74MiB packaged asset xərcidir. Bu artwork keyfiyyət xərcidir, performans optimallaşdırması iddiası deyil. Host/JVM nəticəsi production/ART/frame/GPU ölçüsü deyil; cihaz latency/first-content ölçülməyib.

Bu vahiddə əvvəlki uyğunsuz skamya/fənər/altlıq və kiçik render problemi aradan qaldırılıb, normal portretin üç-sıra kompozisiyası qurulub. Piksel eyniliyi PASS deyil: generativ art mənbənin hər yarpağını kopyalamır; ortaq təsdiqli collection chrome-u saxlanır, proposal ilə header/tab nisbətləri eyni deyil.48dp native CTA/refresh və real kataloq/sahiblik state-ləri nümunə PNG-dən fərqlənir. Kiçik/font2-də bütün kartlar ilk görünüşə sığmır; üfüqi tab scroll-u və mövcud compact navbar davranışı qalır. Real cihaz/TalkBack/ART/GPU/canlı hesab yoxlanmayıb. Emulator, CI, merge, push və deploy yoxdur. Bütün app redesign və mərhələ qəbulu deyil.

## Hazır diff-in ayrıca review-u — 18 noyabr

İstifadəçi tapşırığı ilə `docs/PLAN.md` §1.1 əsasında yalnız `ade78e8` üzərindəki hazır Dekor working diff-i yenidən yoxlandı. Scope: üç Kotlin faylı, yeni atlas/metadata, fənər vectoru, render helper diff-i və əlaqəli dizayn/review sübutları. Owner `.dm` və city-detail-costs dəyişikliklərinə toxunulmadı. Kod düzəlişi tələb edən yeni, təsdiqlənmiş problem tapılmadı; production kod dəyişdirilmədi.

Art override ID-lərinin legacy kataloqda mövcudluğu, təkrarsızlığı və altı crop-un 1536×1024 atlas daxilində qalması ayrıca script ilə PASS. Default `PurchaseArt` çağırışları pixel cap-i saxlayır; opt-in yalnız Dekor kart çağırışındadır. IO decode, stable copy key, kataloq indeksi, yerli filtr, serverdən gələn status və səhifələmə sərhədləri yoxlandı. AppBackend `shop.go` içində mövcud owner-scoped bounded `Decorations`/`DecorationCopy.Position` müqaviləsi ilə uyğundur; backend dəyişikliyi lazım deyil.

Mövcud `comparison.jpg` və bütün 21 renderi ehtiva edən `matrix.jpg` açılıb baxıldı. Yenidən alınan native normal390/font1 və kiçik320/font2 renderləri də açılıb yoxlandı; əvvəl qeyd olunan scroll/chrome/paritet məhdudiyyətləri qalır. Bunlar pixel-paritet və ya mərhələ qəbulu kimi qiymətləndirilmədi.

Təkrar yoxlama: `:app:assembleDebug`, `:app:lintDebug`, `:app:testDebugUnitTest --tests 'com.aladdin.wobbrik.Decor*Test'` PASS; beş native test və bir `DecorationInventoryReadsTest`, XML-də 6 test/0 failure/0 error/0 skipped. İlk run-da app testləri icra olundu, amma səhv əlavə edilmiş `:core:test --tests '*DecorationInventoryReadsTest'` həmin modulda test olmadığı üçün komandanı FAILED etdi. Düzgün app-only son komanda BUILD SUCCESSFUL oldu, artıq icra edilmiş dəyişməz app testləri UP-TO-DATE idi. Loglar `/private/tmp/decor-review-20261118{,-final}.log`, yeni renderlər `/private/tmp/decor-review-20261118/`. Kontrast yenidən 6/6 PASS, minimum7.41:1; `git diff --check` PASS. Əvvəlki request/byte fixture nəticələri dəyişməz read testinin keçməsi ilə yoxlandı; yeni production latency/ART/GPU ölçüsü yoxdur. CI/emulator/commit/merge/push/deploy icra edilmədi.
