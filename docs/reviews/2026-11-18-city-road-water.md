# Şəhər — birləşən cığırlar və su əksləri

18 noyabr 2026. `feat/city-collection-design`, baza `a98272c`, working diff. Scope yalnız `docs/PLAN.md` §1.1 və əvvəlki scene-art handoff-un qalan yol/su qrafikasıdır. Əvvəldən dirty native collection/artwork, Inventory review və release `.dm` faylları saxlanıb. Commit/merge/CI/push/deploy yoxdur.

## İcra və lokal review

`CityParkPaths.kt` real projected foundation girişlərini torpaq daxilində ortaq, yumşaq daş cığırına bağlayır. Bu presentation-dır; yeni domain road, placement, dekor nüsxəsi və sahiblik deyil. Bina footprint-ləri və yerləşdirilmiş upright dekorların təməlləri routing-dən çıxarılır. Owned road art-ı əvvəlki kimi ayrıca çəkilir. Region-lar və boolean union saxlanır; əlaqəsiz adalar arasında körpü çəkilmir. Yol səthi exact owned surface ilə clip olunur. PNG-yə çatmaq üçün heç bir bina/parcel/floor/rotation/parts dəyişdirilmir.

Routing 8 world-unit grid, böyük hədlərdə16-unit grid və maksimum65536cell istifadə edir. Bundan da kobud grid lazım olan çox böyük/seyrək extents-də presentation paving buraxılır: kiçik footprint/gap üzərindən saxta bağlantı yaradılmır. Dar keçidlər yolun eni/clearance-inə sığmırsa həmin komponentlər birləşdirilmir. Hər connected component üçün bir BFS parent tree, hər ortaq edge üçün ən çox bir emission var; all-pairs bina linkləri və bina başına tam grid scan yoxdur. İlk girişlər arasında ortaq cığır görünür; sıx bina fixture-ində yolun bir hissəsi binaların arxasında qalır. Nümunənin geniş parkı üçün real geometry dəyişdirilmir.

`CityWaterArt.kt` torpaq winding-indən asılı olmayan ön sahili tapır; native mavi halo, qırıq isti işıq zolaqları və incə ripple çəkir. Bunlar dekorativ əkslərdir, yeni lamp/simulyasiya deyil. Mövcud `GameTokens.MobileHome`/`InventoryCollection` rəngləri işlənir; UI ölçü/palette sistemi dəyişmir. Şader/brush-lar shoreline ilə bir dəfə hazırlanır; yeni bitmap/atlas/decode yoxdur. Mövcud atlasın source/RGBA xərci əvvəlki review ilə eynidir.

Garden/path/reflection preparation `Dispatchers.Default` üzərindədir. Qəbul edilmiş binalar optional scenery-ni gözləmir. Scene/input dəyişdikdə əvvəlki nəticəni göstərməyən ayrıca remembered state yaranır; coroutine cancellation köhnə cavabın yeni state-ə yazılmasını kəsir. Legacy tablet bu preparation-ı işlətmir. Native header/frame/summary/navbar və input qaydaları dəyişdirilməyib.

Lokal review: native layout-a toxunulmaması, real geometry/parts, union/gap/footprint isolation, grid/edge bound, işin UI thread-dən çıxarılması, account/scene cancellation, legacy branch, brush allocation və importlar yoxlanıb. AppBackend `docs/wobbrik-game-api.md` geometry/positions/bundled read müqaviləsi təkrar oxunub: API/DTO/schema/domain dəyişikliyi lazım deyil.

## Yoxlama və görünən fərqlər

İlk native renderdə cığır clearance-i həddən böyük idi: bəzi girişlərdə yol görünmürdü. Native müqayisədən sonra footprint/giriş boşluğuna uyğun daraldıldı. Su üçün ilkin winding fərziyyəsi bəzi ön kənarları buraxırdı; union-un içərisini yoxlayan seçimlə düzəldildi. İlk normal render + illustration/path yoxlamaları PASS idi; həmin nəticə yekun matrix-in əvəzi deyil.

Son yoxlama nəticələri və screenshot sübutu aşağıda yekunlaşdırılır. Reference/before/after müqayisəsi `city-road-water/comparison.jpg`, matrix `city-road-water/matrix.jpg`,31 tamölçülü çəkiliş `city-road-water/screens/` altındadır. Bunlar sintetik fixture və native Robolectric host render-dir; real device/ART/GPU/heap/GC/latency/TalkBack/API24/runtime inset/IME və bütün locale halları ölçülməyib. Tam piksel pariteti və bütün redesign/mərhələ qəbulu iddiası yoxdur. Real parcel sıxlığı/material/roof seçimi təsdiqli nümunədən fərqlənir; kiçik/font2-də əvvəlki scene ölçüsü və navbar wrap saxlanır.


Son production dəyişikliklərindən sonra `:app:assembleDebug :app:lintDebug :app:testDebugUnitTest` seçilmiş7 City sinfi ilə PASS: `/private/tmp/city-water-verified.log`,1m25s,39/39. Son yalnız test əlavəsi üçün `CityParkPathsTest`3/3 PASS: `/private/tmp/city-water-path-final.log`,16s. Cəmi40 unikal test: collection5, illustration2, park/water3, projection6, terrain4, life8, reads12;0failure/error/skipped. Snapshot XML və saylar `city-road-water/{test-results/,checks.txt}`-dədir. Son diff-check PASS. CI deyil; əvvəlki core testlər bu vahiddə təkrar işlədilməyib.

31 son screenshot/matrix/comparison/preview açılıb baxılıb:320×568/390×844 fontScale1/2 və360×640 font2 + inset/uzun ad/Long.MAX_VALUE; fit/focus/accessibility custom action/refresh/retry/navigation/cache/loading/error/empty/metadata scroll. UI kontrast6/6 PASS, minimum7.41:1 (`city-road-water/contrast.txt`). Bu artwork kontrastını mətn tələbi kimi təqdim etmir.

Əvvəlki eyni read fixture4cold request/760response UTF8byte,warm0,offline0,refresh4 idi; son read test də eyni4/760/0/0/4 nəticəsini verdi. Yeni endpoint/DTO/raster/APK source/atlas RGBA xərci yoxdur; əvvəlki44,561,632RGBAbyte təxmini dəyişmir. Bounded grid/parent/queue/path və static reflection brush-ları əlavə CPU yaddaşıdır, real heap/GPU/latency ölçülməyib; sürət/frame yaxşılaşması iddiası yoxdur. Hole/footprint avoidance, disconnected parcel üçün bridge olmaması, shared edge/cell cap, çox sparse extent-in təhlükəsiz buraxılması və shoreline winding regression testlə yoxlandı.1000-building/10000-shore landşaft cap/source-geometry testi də PASS. Native source və bütün screenshot-lar yenilənəndən sonra əlavə source dəyişikliyi edilməyib.

Təkrar host run mövcud offline `docs/reviews/city-collection-native/render/render.init.gradle` vasitəsilədir, production build/CI-yə yeni test framework əlavə edilmir. Şəkil matrisi/comparison generatoru `city-road-water/render/CityRoadWaterSheets.java`-dır. Son hal merge üçün avtomatik icazə deyil.

Son istifadəçi dəqiqləşməsi üzrə torpaq adasının qalın sahil/grass/geniş park və parcel artımı davamı `2026-11-18-city-island-ground.md`-dədir. Cari render sübutu həmin qeyddədir; bu fayl əvvəlki yol/su vahidinin tarixçəsidir.
