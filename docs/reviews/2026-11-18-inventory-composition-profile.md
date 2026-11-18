# İnventar — ilk composition/class-load ayrımı / 18 noyabr 2026

Branch `fix/inventory-detail-latency`, baza54621a7üstündə working diff. Bu vahidin production diff-i `InventoryScreen.kt`, `MobileHomeScreen.kt`, `MobileMenuShell.kt`-dir. Əvvəlki eyni branch-dəki ReadOnlyGameCache fayl-kilidi diff-i saxlanır. Commit/merge/push/deploy/CI yoxdur.

## Ölçünün sərhədi və361ms-in izahı

Əvvəlki361ms-in trace-i yox idi; həmin tarixi nümunəni sonradan dəqiq millisanələrə bölmək mümkün deyil. Eyni host yolunu təkrar instrumentasiya etdik: Compose `CompositionTracer`, JDK21JFR ClassLoad/Compilation/ExecutionSample və test-only root measure/place modifier-i. App koduna profiler hook, yeni dependency və ya prewarm əlavə edilmir. JFR system classloader-dən test reflection-u ilə açılır; Android/Robolectric sandbox-ın JDK siniflərini klonlaması ilə qarışdırılmır.

Əvvəlki fixture Home-da sadəcə Text göstərirdi; production Home header/navbar-ı qurmurdu. Bu yolun profilli ilk nümunəsi392,54ms oldu: əsas thread-də1382class-load event-in interval birləşməsi211,91ms; composition interval birləşməsi238,12ms; header102,30ms, onun painterResource çağırışı68,53ms. Bunlar üst-üstə düşən intervallardır, **toplanmır**. C1/C2compiler thread-lərində76,59/344,08ms compilation işi paralel gedirdi; bu cəmi392ms-ə əlavə etmək və ya onu UI-nin JIT gözləməsi adlandırmaq olmaz. Interpreted/JIT stack nümunələri kiçik sample-dir, CPU faizi deyil. Əsas cold xərcin əhəmiyyətli hissəsi JVM/Compose/shared-chrome ilk hazırlığıdır; Android ART/device nəticəsi deyil.

Daha uyğun müqayisə üçün əvvəl production `MobileHomeScreen` göstərilib, logo hazır olana qədər gözlənilib, sonra İnventara keçilib. Bu, production-da onsuz da görünən Home-dur; süni gizli İnventar precompose/prewarm deyil. Atlas əvvəl hazırlanır, route-də status Loading saxlanır, accepted inventory bundle əvvəl yazılır, fresh transport cavabları blokludur. Ölçü route dəyişməsindən ilk native metadata semantikasına qədərdir; fiziki GPU/frame təqdimatı deyil.

## Müqayisə və düzəliş

Eyni detailed fixture-də əvvəl/sonra:

- İlk route, profilli:224,89→178,06ms. Əvvəlki real-Home profilli təkrarda258,06ms, yalnız lazy-row experiment-də218,88ms alınıb; bunlar fərqli host nümunələridir.
- Əsas thread class-load interval birləşməsi101,20→92,57ms, class sayı482→464.
- Composition interval birləşməsi137,38→98,55ms. Header27,84→0,46ms; sinxron `painterResource`27,05→0ms.
- Test root measure/place müşahidəsi15,44→12,83ms. Bu wrapper parentin ölçülməsini görür; daxili lazy remeasure onu keçə bilər, buna görə **bütün grid layout xərcinin ölçüsü deyil**. Gridin composition çağırışları ayrıca spans.tsv-dədir (əvvəl rememberLazyGridState13,91ms, LazyVerticalGrid11,88ms; sonra InventoryBuildingGrid36,94ms daxilində LazyColumn12,52ms və state hazırlığı). Nested span-lər toplanmır; yeni wrapper ilə köhnə alt funksiyanı təkbətək müqayisə etmək olmaz.
- Profilsiz eyni real-Home fixture-də ilk keçid191,27→160,36ms. Profilsiz warm nümunələr əvvəl35–40ms, sonra46–63ms olub; ümumi warm sürətlənmə iddia edilmir. Bunlar bir neçə host nümunəsidir, percentile və sabit device benchmarkı deyil.
- Son profilli compiler cəmləri C1/C2:66,47/177,66ms,512compilation. Background JIT CPU ilə UI wall vaxtı ayrıca saxlanır; JIT-in causal wall payı dəqiq müəyyən edilməyib.

Ən aydın çıxarılan təkrarlanan iş header logo decode-udur. Eyni APK logo artıq mövcud config/density-scoped `rememberResourceArt` ilə IO-da decode edilir. MobileMenuShell onun state-ni Home və menyular arasında saxlayır; hər route-da sinxron decode/BitmapPainter resource load təkrarlanmır. Shell unmount/config dəyişəndə əvvəlki worker yeni state-ə nəşr edə bilmir. Process bitmap cache və Activity singleton yoxdur. Hazır bitmap eyni asset, Fit və alignment ilə çəkilir. Loading-də eyni ölçü rezerv olunur, ekran gözləmir; failure-də retry var.

Mobil İnventarın təsdiqli1/2sütun kompozisiyası lazy sətirlərlə saxlanır. Lazımsız grid track/slot yolu mobil route-dan çıxır; kartlar və building renderer dəyişmir. Row/column collection semantikası, building ID-ləri, tək son kart üçün boş yarım sütun və scroll saxlanır. Planşet əvvəlki adaptive LazyVerticalGrid yolundadır; bu vahiddə render/planşet dəyişikliyi yoxdur. Kiçik class-load azalması ölçülüb; bütün qazancları grid dəyişikliyinə aid etmirik.

## Uyğunluq və yoxlama

AccountViews, repository, accepted DTO/geometry, revision/epoch yoxlamaları və ReadOnlyGameCache formatı bu vahiddə dəyişmir. Eyni accepted məlumat eyni kart/renderer-ə verilir; fresh cavabadək cached detail mutasiya capability-si almır. Fake lokal ownership/wallet/state və ya serverdən fərqli bina modeli yoxdur. Əvvəlki fayl-kilidi düzəlişi saxlanır. Backend API/domain/schema/rules dəyişmir; əvvəlki owner/revision müqaviləsi ilə uyğundur, AppBackend dəyişikliyi tələb olunmur. Backend test/authenticated E2E bu vahiddə işlədilməyib.

Native regression11/11PASS14s: əvvəlki local-first7hal +20bina,19binanın tək son kartı,320font2single-column scroll, doğru clickID, eyni logo bitmapinin Home→İnventar→Home arasında saxlanması. Scroll fixture-də cached metadata fresh network bloklu ikən görünür,0command; collection row/column sayları yoxlanır. Əvvəlki suite320/360/390portret font1/2syntheticpadding, purple/teal, art failure/retry və cached detail mutation gate-i saxlayır. Bu matrix boş-Home stress fixture-dir, profilli real-Home nəticəsi kimi təqdim edilmir.

Beş son PNG açılıb əvvəlki renderlər və01-main.pngauthority-si ilə müqayisə edildi. Hazır logo/crop/kart sütunları saxlanır.320loading-də async logo hələ hazır olmaya bilər; header ölçüsü rezervlidir, metadata görünür. Əvvəlki font2kiçik illustration/tabs və iki sətir dock borcları qalır. Yeni rəng/ölçü/token yoxdur; `ui-craft` ilə GameTokens oxundu, kontrast Text/Surface13.00, Muted8.22, Accent8.99PASS. Tam PNG parity və real inset/IME/locale/TalkBack/GPU yoxlanmayıb.

Lokal diff review: heç bir suspend call UI composition-da disk/decode etmir; logo resource helper-in config/cancellation sərhədi təkrar istifadə olunur. Row key-ləri birinci buildingID, kart content key-ləri buildingID-dir; append son tək row-un yarısını doldurur, metadata authority-si dəyişmir. Native scroll/click regression keçdi. Fokuslu core154/app37unit, assembleDebug, lintDebug PASS54s; nəticə evidence/checks.log-da saxlanır; CI deyil. Diffcheck PASS.

## Təkrar ölçmə

`cd android-billing`; `./gradlew -I ../docs/reviews/inventory-composition-profile/render/render.init.gradle :app:testDebugUnitTest --tests '*InventoryCompositionProfileTest' --offline --console=plain`. Profilsiz üçün `-Dinventory.profile=false` əlavə et. JFR/rounds/spans/measure `/private/tmp/wobbrik-inventory-profile` altına çıxır. `jfr print --json --events jdk.ClassLoad,jdk.Compilation,jdk.ExecutionSample route.jfr` çıxışını `render/analyze.py EVIDENCE_DIR JFR_JSON` ilə xülasələndir. JFR wall timestamp-i round CSV-nin millisecond clock-u ilə hizalanır, sərhəddə~1ms dəqiqlik məhdudiyyəti var. Raw span/CSV və xülasələr `inventory-composition-profile/evidence/`-dədir; binary JFR-lər `/private/tmp/inventory-{blank-home,detailed-before,detailed-after}.jfr`-də qalıb, repo-ya əlavə edilmir.

Regression üçün eyni init-ə `-I ../docs/reviews/inventory-first-open/render/render.init.gradle` əlavə edib `--tests '*InventoryRowsTest' --tests '*InventoryFirstOpenTest'` işlə. Ölçü zamanı yalnız profile testini seç; əvvəl başqa test işlədib cold nəticə kimi təqdim etmə. Production baza UI faylları54621a7versiyasıdır; əvvəlki core fayl-kilid düzəlişi bütün müqayisələrdə aktiv qalıb.

Cihaz/emulator açılmayıb. Production10+s şikayəti, Android ART/startup/GPU/payload/yaddaş və app-wide menyu performansı hələ ölçülməyib. Bu vahid host xərcini ayırır və ölçülmüş təkrarlanan main-thread decode-u çıxarır; bütün ilk-açılış xərcinin yox olduğu iddia edilmir.
