# Şəhər — torpaq adasının qalın sahili və park səthi

18 noyabr 2026. Branch `feat/city-collection-design`, baza `a98272c`, working diff. İstifadəçinin son göstərişi: binaların altındakı torpaq adasını təsdiqli PNG-yə yaxınlaşdır, qalın daş sahil/təbii ot/geniş yollar qur, real geometry/native layout saxla və gələcək sahə artımına uyğunlaşdır. Authority `docs/PLAN.md` §1.1 və `docs/design/city-collection-v1/proposal.png`. Əvvəlki dirty iş və release `.dm` faylları saxlanıb. Commit/merge/CI/push/deploy yoxdur.

## Nəticə və sərhəd

Mobil adanın presentation bank dərinliyi32-dən48 world unit-ə artırılıb; torpağın server konturu, floor/position/rotation/footprint və ownership dəyişmir. Qayanın altı davamlı material ilə doldurulur; xırda daş zolağı əvəzinə mövcud alpha atlasdan iri mossy boulder qrupları çəkilir. Daşlar exact union/cliff daxilində clip olunur. Daxili parcel sərhədləri sahil deyil. Otun mövcud teksturası native kölgə tonu və əvvəlcədən hazırlanmış radial bitki kölgələri ilə daha təbii dərinlik alır. Yeni bitmap/atlas yoxdur.

`CityIslandArt.kt` bütöv union konturunu içəri çəkib34-world-unit daş promenade səthi yaradır, əvvəlki giriş şəbəkəsini həmin səthlə birləşdirir. Bütün real bina təməlləri və upright dekor footprint-ləri səthdən çıxarılır, nəticə real torpaqla kəsişdirilir. Dar yerlərdə yol nazilir/kəsilir; yeni sahə, körpü və ya binanın yerini dəyişmək hesabına yol genişləndirilmir. Bu park səthi domain road, placement və ya yeni dekor nüsxəsi deyil; owned road art-ı öz qatını saxlayır.

Köhnə layout, header/frame/summary/navbar və klik semantikası dəyişmir. Scene fit bounds yalnız yeni bank/su presentation həcmini nəzərə alır; native frame ölçüsü dəyişmir. Su əksləri yeni bankın altına uyğun çəkilir. Legacy tablet/Game/Inventory renderi əvvəlki branch-i saxlayır.

## Sahə artımı və xərc

Ada şəkil/sabit beş bina kompozisiyası deyil. Yeni parcel qəbul ediləndə union, xarici sahil, cliff və park konturu yenidən hesablanır. Concave/L və fırladılmış sahələr, iç tikişin yox olması və yeni sahəyə çıxan park səthi üçün testlər əlavə edilib. Eyni ekran açıqkən2parcel→6parcel scene dəyişməsi də native render/input testində yoxlanır; bina mövqeləri/parts saxlanır.

Park/grass/brush hazırlığı mövcud optional `Dispatchers.Default` job-undadır; bina məzmunu bunu gözləmir. Scene/surface dəyişməsi yeni state yaradır, köhnə job-un nəticəsi yeni scene-ə yazılmır. Path branch-ləri tək accumulator-a yığılır, hər bina üçün böyüyən union təkrarlanmır. Grass shade cap256-dır. Əvvəlki entrance routing65536cell/16-unit həddi saxlanır; çox seyrək/böyük extents-də entrance branch buraxılsa da yeni coast-following park səthi həmin grid həddinə görə itmir. Yolların bütün dar keçidlərdə birləşməsi vəd edilmir.

Yeni endpoint/DTO/bitmap/decode yoxdur. Daha qalın daşlar və kölgələr əlavə fill/draw işi yaradır; GPU/frame/heap/GC/latency ölçülməyib, sürət yaxşılaşması iddiası yoxdur. Mövcud atlas source/RGBA xərci dəyişmir. AppBackend geometry/positions müqaviləsi əvvəlki yoxlanmış müqavilədir; bu presentation vahidində API/schema/domain dəyişikliyi lazım deyil.

## Lokal review və yoxlamalar

İlk iki sıra xırda daş native renderdə çınqıl təpəsinə bənzəyirdi; daha az sayda iri alpha qrupları ilə düzəldildi. İlk growth testində2assertion `interior parcel seam` ilə uğursuz oldu:1pixel integer Region nümunəsi küncdəki çox qısa segmenti səhv qiymətləndirirdi. Test10× sub-unit Region və0.5world-unit normal ilə dəqiqləşdirildi; eyni production union kodu ilə keçdi. Son rotation fixture-ləri hər kataloq formasının rotated bounds-undan yaradılır: base-ə yanaşı və overlap-sız. Uğursuz ilk run yekun PASS kimi göstərilmir.

Lokal review scope: `CityIslandArt.kt`, `CityIllustrationArt.kt`, `CityTerrainArt.kt`, `CityWaterArt.kt` və `CityScreen.kt` integration/bounds; real footprint mask, island union/growth, background preparation/cancellation, bitmap təkrar istifadəsi, single path accumulation, legacy fallback və importlar. Tam PNG pariteti və mərhələ/app redesign qəbulu deyil: real bina sıxlığı/torpaq forması/material/roof seçimi proposal-dan fərqlənir. Real cihaz/ART/GPU/heap/GC/latency/TalkBack/API24/runtime inset/IME və bütün locales yoxlanmayıb. Emulator açılmayıb.

Son render review-da same-screen growth çəkilişinin optional Default preparation-dan tez alındığı aşkarlandı: geometry artmışdı, amma park/tree qatı hələ hazır deyildi. `CitySceneryPrepared` custom Compose semantics readiness dəyəri əlavə edildi; görünən mətn/TalkBack təsviri/klik davranışı dəyişmir. Host render ilk səhnədə və2→6growth-dan sonra real preparation-ın tamamlanmasını gözləyir. Bundan sonra bütün production dəyişiklikləri ilə build/lint və bütün fokuslu testlər yenidən işlədildi; əvvəlki flat growth screenshot yekun sübut kimi saxlanmır.

Yekun `:app:assembleDebug :app:lintDebug :app:testDebugUnitTest`8fokuslu City sinfi ilə PASS: `/private/tmp/city-island-verified.log`,1m21s.44/44,0failure/error/skipped: collection6, illustration2, park/water3, island3, projection6, terrain4, life8, reads12. XML və saylar `city-island-ground/{test-results/,checks.txt}`-dədir. Əvvəlki geniş run44/44 (2m19s) və growth-only1/1 (32s) də PASS idi, lakin son readiness source dəyişikliyindən sonra yekun44/44 təkrarlandı. Core testlər təkrar işlədilməyib. Son `git diff --check` PASS. CI işlədilməyib.

Son38native screenshot320×568/390×844 fontScale1/2,360×640 font2 inset/uzun ad/Long.MAX_VALUE və normal390-da eyni composition-da2→6parcel artımını əhatə edir. Fit/focus/custom actions/refresh/retry/navigation/cache/loading/error/empty/metadata scroll yoxlanıb. Son matrix/comparison açılıb baxılıb: `city-island-ground/matrix.jpg`, `comparison.jpg`, tamölçülü `screens/`, `preview.png`. Comparison proposal / əvvəl / yeni ada / eyni binalarla6parcel nəticəsini göstərir. Sıxlıq/real forma fərqi, font2-də scene azalması və əvvəlki320navbar wrap açıqdır; bunlar dizaynla tam piksel eyniliyi kimi təqdim edilmir.

Kontrast6/6 PASS, minimum7.41:1 (`contrast.txt`); yeni UI rəngi/ölçüsü yoxdur, artwork `GameTokens` rənglərini istifadə edir. Eyni read fixture əvvəl/sonra cold4request/760responseUTF8byte,warm0,offline0,refresh4. Yeni sorğu və raster/atlas/RGBA əlavə olunmayıb; əvvəlki art üçün44,561,632RGBAbyte təxmini saxlanır. Source/RGBA təxmini heap/GPU ölçüsü deyil. Backend dəyişməsi tələb olunmur; cihaz vaxtı/frame/heap ölçülmədiyindən performans yaxşılaşması iddiası yoxdur.
