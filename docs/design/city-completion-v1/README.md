# Şəhər üçün çatışmayan dizaynlar — 9 noyabr 2026

Status: **13 noyabrda `04-building-detail.png` görünüşləri istifadəçi tərəfindən təsdiqlənib; `02-parts-flow-v2.png` də 13 noyabrda hissələr/mağaza və binaya tətbiq vahidi üçün açıq təsdiqlənib; digər lövhələr təklif olaraq qalır.** Branch `docs/missing-city-designs-v1`. Built-in imagegen; promptlar `prompts.json`. Hər vəziyyət bir kompozisiyada verilir, eyni ekranın cihaz ölçüsü variantları hazırlanmayıb. Bunlar Android build renderləri deyil.

- [Kolleksiya və altı başlanğıc üslubu](01-collection.png).
- [68 hissə: seçim → açılma → tətbiq — düzəliş v2](02-parts-flow-v2.png).
- [12 dekor: seçim/alış → yerləşdirmə → redaktə](03-decoration-flow.png).
- [Bina detalı: sakin, dəyər, hissələr](04-building-detail.png).
- [Ərazi: seçim → yer seçimi → alış təsdiqi](05-land-purchase.png).
- [Şəhər → inventar → yerləşdirmə](06-city-inventory.png).

Mənbələr: `../city-plan.png`, `../city-view.png`, `../mobile-v1/inventory-market-v2.png`; tokenlər `../mobile-v1/tokens.css` və tətbiqin `GameTokens.kt` faylı. Köhnə nümunənin uydurma rəng adları hissə kataloqu deyil; hissə lövhəsi kanonik divar adlarına düzəldilib. Altı pulsuz üslub game-plan §23-ə əsaslanır.

## Raster mənbələri və ölçülənmə

### 9 noyabr — ikinci raster baxışı

Branch `docs/city-raster-floor-qa`; built-in imagegen ilə ayrıca versiyalar hazırlandı,
köhnə lövhələr və mənbələr dəyişdirilmədi. Bu düzəlişlər də istifadəçi təsdiqini gözləyən təklifdir.

- [Altı üslub v2](assets/starting-buildings-v2.png): 1536×1024. Altısında da giriş daxil
  10 mərtəbə əl ilə sayılıb; dam ayrıca sayılmır. [Nömrəli yoxlama](qa/starting-buildings-v2-floor-count.png).
- [Dekor plan v2](assets/decor-plan-v2.png) və [həcmli dekor v2](assets/decor-isometric-v2.png):
  hər biri 1086×1448. Eyni 12 ID və obyekt sayı saxlanıb. Geniş halo azaldılıb.
- Hər üçü navy və açıq `--text` fonunda kompozisiya edilib və görüntülərə baxılıb:
  [binalar](qa/starting-buildings-v2-dark-light.png), [plan](qa/decor-plan-v2-dark-light.png),
  [həcmli dekor](qa/decor-isometric-v2-dark-light.png). Mənbələrin RGB kanallarındakı
  görünən fon alpha tətbiq ediləndə görünmür; təkcə RGB önizləməsi alpha sübutu deyil.
- [Piksel hesabatı](qa/raster-report.json): SHA-256, alpha paylanması, hər sprite üçün
  alpha≥8 görünən sərhəd, native piksel ölçüsü və density 1/2/3/4 üçün böyütməsiz dp həddi.
  **Bu atlaslar bərabər 3×4 hüceyrələrə kəsilə bilməz**: uzun yol nominal sütunu keçir.
  Hesabat açıq yoxlama regionlarından istifadə edir; alpha 1–7 qalıqları bbox-dan kənarda qala bilər.
  Bunlar runtime crop/anchor müqaviləsi deyil; ground anchor qəsdən `null`-dır.
- Təkrar yoxlama: Pillow olan Python ilə `python qa/check_rasters.py` (bu qovluqdan).
  Script yalnız QA şəkilləri/JSON yazır, mənbə rastrını dəyişmir. Promptlar: [v2](qa/prompts-v2.json).

**Hissə lövhəsi düzəlişi:** [02-parts-flow-v2.png](02-parts-flow-v2.png) ayrıca
saxlanıb. Sağ önizləmədə giriş + 9 üst mərtəbə = 10; damdakı kiçik dormer ayrıca
mərtəbə deyil. Krem suvaq, düzbucaqlı pəncərələr və əvvəlki bənövşəyi dam saxlanıb.
[Nömrəli QA](qa/02-parts-flow-v2-floor-count.png) və JSON floor center qeydləri mövcuddur.
Bütün lövhə alpha255-dir. Bu, real cəhd həndəsəsi və Android renderi sübutu deyil.
Köhnə `02-parts-flow.png` tarixçə üçün saxlanıb; yeni təklif linki v2-dir.

**Qalan bloklar:** 01/04/06 ekran lövhələrinin əlavə imagegen düzəlişləri yenə bütün
thumbnail/hero-larda dəqiq 10 sayını saxlamadı. 04-də son konkret səviyyə-silmə cəhdi
11 mərtəbəni saxladı; 01-də hero artıq səviyyə aldı; 06-da thumbnail-lər qısaldı və
şəhərə əlavə binalar çəkildi. Bu cəhdlər yekun kimi seçilmədi. 05-də kvadrat düzəlişi
konturu yığcamlaşdırdı, amma dəqiq 1:1 nisbət/iki vəziyyətin eyniliyi alınmadı.
Yol düzəlişi də dəqiq nisbət sınağından keçmədi; əvvəlki v2 mənbə saxlanıb.
01/03/04/05/06 əsas linkləri əvvəlki lövhələrə aparır, bütün dəst tamamlanmayıb.

İstifadəçi qərarı: **yalnız imagegen**. Python/Pillow mənbə rasterinin redaktəsində
istifadə edilməyib; yalnız QA ölçmə, marker və fon kompozisiyaları üçündür.
Yeni bədii üslub təklif edilmir. Son promptlar: [v3](qa/prompts-v3.json),
[v4](qa/prompts-v4.json). Dəqiq say/formanı pozan cəhdlər qəbul edilmiş dizayn deyil.

Yolların raster nisbəti və L girintisi təsdiqlənmiş 100×30 / 100×100–30 / 210×30
collision həndəsəsi ilə dəqiq üst-üstə düşmür. Bina ground anchor-ları, səthə geyindirilən
modullar, metadata üzrə ayrıca sprite çıxarışı və renderer miqyası hələ açıqdır.
V2-lər keyfiyyətli kiçik önizləmə namizədidir; böyük şəhər zoom-u üçün production-ready deyil.
Məsələn, təxminən 240×490px bina crop-u density 3-də yalnız təxminən 80×163dp-dir;
tam ekran bina üçün bunu böyütmək yeni detal yaratmaz. Dəqiq hədlər JSON-dadır.
05-də `land_1`-in 220×220 kvadrat konturuna uyğunluğu əvvəlki kimi açıqdır.
Tətbiq/cihaz renderi və mərhələ 4 implementasiyası başlanmayıb.

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

## 13 noyabr — silmə təsdiqi təklifi

`delete-confirmation-proposal.png`: mövcud təsdiqli bina detalının üzərində tokenlərlə hazırlanmış silmə dialoqu. 13 noyabrda istifadəçi bu dialoqu və bina detalının sonundakı “Binanı sil” girişini “okeydir” cavabı ilə təsdiqlədi. Android build renderi deyil. Core adapter review-su: `../../reviews/2026-11-13-phase4-building-delete.md`.

## 13 noyabr — dekor və torpaq alışının təsdiqi
- İstifadəçi `03-decoration-flow.png` dekor dizaynını açıq təsdiqlədi.
- `05-land-purchase.png` siyahı və alış təsdiqi hissəsi ayrıca açıq təsdiqləndi; əsas əməl “Al”dır. Xəritədə yer seçimi və placement bu vahidə daxil deyil, bağlı qalır.
- Bu təsdiqlər Android render/vizual uyğunluq və ya mərhələ 4 qəbulu deyil.
