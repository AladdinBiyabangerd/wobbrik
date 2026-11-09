# Şəhər üçün çatışmayan dizaynlar — 9 noyabr 2026

Status: **istifadəçi baxışı üçün təklif; təsdiqlənmiş dizaynları əvəz etmir**. Branch `docs/missing-city-designs-v1`. Built-in imagegen; promptlar `prompts.json`. Hər vəziyyət bir kompozisiyada verilir, eyni ekranın cihaz ölçüsü variantları hazırlanmayıb. Bunlar Android build renderləri deyil.

- [Kolleksiya və altı başlanğıc üslubu](01-collection.png).
- [68 hissə: seçim → açılma → tətbiq](02-parts-flow.png).
- [12 dekor: seçim/alış → yerləşdirmə → redaktə](03-decoration-flow.png).
- [Bina detalı: sakin, dəyər, hissələr](04-building-detail.png).
- [Ərazi: seçim → yer seçimi → alış təsdiqi](05-land-purchase.png).
- [Şəhər → inventar → yerləşdirmə](06-city-inventory.png).

Mənbələr: `../city-plan.png`, `../city-view.png`, `../mobile-v1/inventory-market-v2.png`; tokenlər `../mobile-v1/tokens.css` və tətbiqin `GameTokens.kt` faylı. Köhnə nümunənin uydurma rəng adları hissə kataloqu deyil; hissə lövhəsi kanonik divar adlarına düzəldilib. Altı pulsuz üslub game-plan §23-ə əsaslanır.

## Raster mənbələri və ölçülənmə

- `assets/decor-isometric.png`: 1086×1448 RGBA, 3×4, 12 dekorun həcmli mənbələri.
- `assets/decor-plan.png`: 1536×1024 RGBA, 3×4, eyni 12 dekorun yuxarıdan görünüşü. Bunlar fərqli kamera görünüşləridir, cihaz ölçüsü variantları deyil.
- `assets/starting-buildings.png`: 1536×1024 RGBA, 3×2, altı başlanğıc üslubunun bədii mənbəyi. Sabit bina rəsmi real cəhd həndəsəsini əvəz etmir.
- `assets/catalog-map.json`: 12 server ID-si ilə nominal sıra uyğunluğu; crop/anchor və collision metadata deyil. Dönüş yolunun 100×100 bounding box-u daxilində 30 enli L konturu əsasdır, boş girinti sahə tutmur.
- Mövcud **68 hissə təkrar yaradılmayıb**: `../finish-cosmetics-v1/assets/`, tətbiq üçün yoxlanmış crop metadata `android-billing/app/src/main/assets/finish/catalog-map.json`-dadır. 13 divar, 14 pəncərə, 14 giriş, 14 dam, 13 işıq.
- `assets/alpha-preview.png`: mənbələrin navy fonda kompozisiya yoxlaması. Orijinal PNG-lər dəyişdirilməyib; yalnız QA preview ayrıca render edilib.

Mətnlər, düymələr, kartlar və konturlar bitmap-dən kəsilib böyüdülməməlidir; Compose ilə tokenlərdən çəkilməlidir. Raster aspect ratio saxlanaraq göstərilməli, hündür bina bir bütöv şəkli uzatmaqla deyil, həqiqi mərtəbə həndəsəsinə geyindirilən modullardan yığılmalıdır. Beləliklə 10/20/50 mərtəbə sayı və əyrilik qorunur. Böyük önizləmədə pan/zoom və bütün binaya baxış ayrılmalıdır; altı nümunə mərtəbə ilə əvəz etmək olmaz.

Rasterin sonsuz böyüdülmədə keyfiyyət saxlaması mümkün deyil. Hər istifadə ölçüsü üçün tələb olunan fiziki piksel (dp × density) həqiqi crop ölçüsündən çox olmamalıdır. Bu atlasların nominal cell ölçüsü hazır istifadə edilən sprite ölçüsü deyil. Cihazdan asılı maksimum ölçü, sıxlıq, crop və anchor yoxlaması tamamlanmayıb; bu mənbələrə production-ready/scalable zəmanəti verilmir. Daha böyük mənbə lazım gəlsə mövcud PNG-ni sadəcə upsample etmək yeni detal yaratmır.

## Vizual baxış və açıq düzəlişlər

Bütün son lövhələrə və navy alpha preview-yə baxılıb. 12 dekor tipi və 6 üslub mövcuddur. Şəffaflıq real PNG alpha kanalında yoxlanıb: plan 1,151,210 / 1,572,864 tam şəffaf piksel; bina atlası 996,241 / 1,572,864; hər ikisinin dörd küncü alpha 0. Həcmli dekorda 741,702 / 1,572,528 tam şəffaf piksel, bir küncdə alpha 1 var. Obyektlərdə yumşaq yarımşəffaf kənarlar qalır; crop/gutter/anchor QA və işıqlı fonda baxış hələ tələb olunur.

Generativ bina şəkillərində ayrı-ayrı mərtəbələrin sayı yazı ilə hər yerdə dəqiq uyğun deyil; təkrar düzəliş bu problemi tam aradan qaldırmadı. Şəkillər dəqiq həndəsə və say sübutu deyil. Xüsusən kolleksiya/keçid thumbnail-ləri, qala dişinə bənzəyən bəzi parapetlər, planın seçilmiş bina ilə uyğunluğu və land_1 kontur nisbəti implementasiyadan əvvəl düzəldilməlidir. Bina detalındakı bəzi adlarda generativ mətn səhvləri var; kodda kataloqdan dəqiq ad istifadə edilməlidir. Hissə tətbiqi lövhəsində üst “Nailiyyət” filtri ümumi kataloq üçündür; divar kateqoriyasında nailiyyət hissəsi yoxdur.

Sakin nümunəsi F=10 → R=40, pulsuz hissələr üçün B=20, maksimum24, komissiya2, satıcı22. Ərazi1: level3/150XP/200sikkə/+4 ekvivalent. Bunlar yeni balans qərarı deyil. Hissə açmaq hesab hüququdur, dekor almaq ayrıca nüsxədir; tətbiq/yerləşdirmə təsdiqi açılma/alışdan ayrıdır. Xəta, pending, insufficient coins, elan geri götürmə və hesab bağlama kimi əlavə halların tam dizaynı bu lövhələrdə yoxdur.

Token kontrastı: text/background16.57, muted/surface8.22, on-accent/accent11.45 — PASS. PNG-nin hər pikseli üçün kontrast və ya touch-target testi deyil. Telefon/planşet, böyük şrift və adaptiv Android renderi aparılmayıb: bu iş dizayn/raster təklifidir, tətbiq və ya mərhələ qəbulu deyil. İstifadəçi təsdiqi olmadan UI implementasiyası başlamır. Backend/API/schema və iqtisadi qaydalar dəyişməyib; bu art-only iş üçün AppBackend dəyişikliyi lazım deyil.
