# Dynamic scene artwork — 18 noyabr 2026

Authority `proposal.png`; icra `docs/PLAN.md` §1.1. Native collection layout bu vahiddə dəyişmir.

`android-billing/app/src/main/assets/city/collection-illustration-v1.png` built-in ImageGen ilə proposal-dakı daş/çiçək/ağac və material dili əsasında hazırlanmış ayrıca modular artwork atlasıdır. İlk generasiyanın fonu ikinci ImageGen keçidində true alpha ilə çıxarılıb. Mənbə 1536×1024 RGBA; 2,299,240 bayt. Şəklin daxilində UI, mətn, bina sayı və idarə yoxdur. Runtime source rectangles `CityIllustrationArt.kt`-dədir. Alpha bounds (alpha≥8): rock48,171–474,340; flowers561,130–963,388; tree1075,19–1480,478; material rectangles aşağı sıradadır. Materiallarda painterly alpha var; native opaque underpaint fasadın arxasını örtür.

Kərpic atlası yalnız `base_2_wall`, suvaq yalnız `base_3_wall` üçün işləyir. Digər wall ID-lər qəbul edilmiş kataloq materiallarını saxlayır. Window/entrance/lighting/roof seçilmiş mövcud ID-lərin art-ından gəlir. City-only fasad iki pəncərəni bir mərkəzi pəncərə ilə əvəz edir, nazik mərtəbə kəməri və sağ üz kölgəsi çəkir; accepted floor centres/rotations/count və 100×100 footprint dəyişmir. Game/Inventory artı dəyişmir.

Sahil bütün owned parcel-lərin boolean union konturundan gəlir. Daşların alpha kənarı dəqiq union/cliff daxilində çəkilir; daxili parcel tikişinə sahil əlavə edilmir. Mobil cliff-in vizual dərinliyi32 world unit-dir; server həndəsəsi deyil. Çiçək, qısa giriş cığırı və landşaft ağacları presentation-dır; yeni mülkiyyət, dekor nüsxəsi, placement və keçilə bilən road deyil. Ağaclar real torpağa inset edilmiş nöqtələrdən, bina/dekor foundation-u ilə toqquşmadan və128 cap ilə seçilir. Real owned tree1 bu atlası istifadə edir; digər dekor ID-ləri saxlanır. Painter depth bütün upright qatları birlikdə sıralayır.

Binaların sayı, real material/roof seçimi, ofsetlər və torpağın konturu proposal-ın sintetik beş bina kompozisiyasından fərqlənə bilər. Atlas bütöv island və ya nümunə binaları real scene üzərinə yapışdırmır. Piksel eyniliyi iddiası yoxdur. Native yoxlama və məhdudiyyətlər `../../reviews/2026-11-18-city-scene-art.md`-dədir.

Qalan yol/su vahidində qısa giriş cığırları real torpaq/footprint daxilində bounded ortaq park şəbəkəsinə bağlanır, ön sahilə native mavi/isti işıq əksləri çəkilir. Atlas və source rectangles dəyişmir. Routing/çox böyük extent sərhədləri, fon hazırlığı və son render sübutu `../../reviews/2026-11-18-city-road-water.md`-dədir.

Torpaq adasının son dəqiqləşməsi: bank48world-unit presentation depth, iri alpha daş qrupları, kölgəli grass və union konturundan34unit geniş park səthi; yeni atlas yoxdur. Giriş yolları və promenade real footprint-lərdən kəsilir. Yeni parcel union-u yolları/sahili yenidən qurur. Son scope və sübut: `../../reviews/2026-11-18-city-island-ground.md`.
