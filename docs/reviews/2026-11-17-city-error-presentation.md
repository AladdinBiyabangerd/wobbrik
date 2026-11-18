# Şəhər xəta görünüşü — native mobil panel

17 noyabr 2026. Branch `feat/native-ui-parity`, baza `1030d16`; review bu commit-in `CityScreen.kt`, yeni `CityErrorContent.kt`, illüstrasiya və `MobileShellRouteRenderTest.kt` diff-inə aiddir. Authority: `docs/PLAN.md` §1.1 və `docs/design/unified-mobile-v3/04-game-states.png`, panel21; son7-slot/header qərarı qüvvədədir. Yalnız mobil xəta vahidi tamamlanır; normal Şəhər/Görünüş kompozisiyası və tam app pariteti deyil.

## Dəyişiklik və lokal review

Mobil `failed` halı səhnə/art/terrain hazırlığından əvvəl ayrıca təqdimata keçir. Native contextual Geri/Şəhərim, disabled Plan/selected Görünüş, relyefli xəta kartı, “Şəhər yüklənmədi” və Refresh ikonlu əsas Yenilə var. Səhnə, Binaya bax və disabled Bax xəta halında yoxdur. Şrift2-də tab ikonları çıxır, etiketlər söz ortasından bölünmür; mövcud horizontal scroll mətn strategiyası saxlanır. Normal şriftdə artwork+başlıq+retry birlikdə sığır. Böyük şriftdə artwork/izah sahəsi scroll olunur, retry kartın sabit aşağı hissəsindədir. Kiçik hündürlükdə mövcud Small boşluq pilləsi seçilir. Yeni rəng/ölçü tokeni yoxdur.

Raster yalnız ayrıca mürəkkəb illüstrasiyadır: `city_error_illustration_v1.png`,1536×1024 RGBA,2MB. Built-in imagegen ilə04 lövhəsinin21-ci panelinə əsasən yaradılıb: çəhrayı/mavi əyri binalar, çatlamış platforma və qırmızı xəbərdarlıq. Mətn/UI/logo yoxdur; source board kəsiyi və skin deyil. Kontur/üçölçülü detallar generativ variantdır, exact piksel surəti deyil. Səthlər, tablar, Geri/Refresh, mətn və əməliyyatlar native-dir. Mövcud MenuRelief, CommerceTitle, HomeButton və GameTokens təkrar istifadə olunur.

İlk turda kart hündürlüyü başlığı gizlətdi; sonrakı baxışda aspectRatio illüstrasiyanı öz layout sərhədindən daşırırdı. Bunlar düzəldildi: ayrıca bounded Box+Fit+clipToBounds, font2 supporting-content scroll. Sıx inset halında art-ı bir neçə pikselə sıxan variant saxlanmadı. Son diff lokal nəzərdən keçirildi: source yerləşməsi, importlar, dublikat/domain sərhədi və git diff --check PASS; açıq yeni code finding yoxdur.

Retry əvvəlki LaunchedEffect/session/current-account/read-refresh müqaviləsini çağırır. Sorğu, API/DTO, revision/dedup, wallet, ownership və server/domain dəyişməyib; AppBackend dəyişikliyinə ehtiyac yoxdur. Tablet/non-mobile xəta yolu saxlanır. Plan aktivləşdirilmir, placement/Ərazi girişi əlavə olunmur. Ümumi press animasiyası ayrıca tapşırıq olaraq qalır.

## Son yoxlama

Son source üzrə5/5 host test PASS, failures/errors/skipped0; lintDebug PASS; BUILD SUCCESSFUL37s. İki production shell flow (390×844/font1,320×568/font2)503→Yenilə→real CitySceneRepository fixture population40→Bax→Geri axınını, dock/navigation və digər mövcud route regression-larını yoxlayır. Synthetic HTTP istifadə olunur; real backend hesab testi deyil. Auditdəki köhnə cumulative cityReads3 gözləntisi4 edildi: əvvəlki İnventar membership oxusu + uğursuz şəhər oxusu + revision-bracketed retry. POST/submission0 assertion-u saxlanır. Retry minimum48dp assertion-u keçir.

Əlavə3 ssenari:320×568/font1,390×844/font2,360×640/font2+simulated24/48dp inset+Long.MAX_VALUE balans. Yenilə touch/callback, Geri→home, disabled Plan və xəta halında camera controls-un olmaması yoxlanır. Son renderlər bu beş ölçü/şrift halında açılıb04 PNG ilə müqayisə edildi. Normalda illüstrasiya→başlıq→Yenilə ardıcıllığı görünür; kiçik/font2 və inset-də izahı scroll açır, Yenilə/Geri/dock görünür. İnset-in ilk viewport-unda başlıq scroll arxasındadır; sonrakı title capture-da tam oxunur. Şrift2-də art əvvəlki viewport-dan qismən scroll kənarına çıxa bilər; overlap deyil. Adaptiv art ölçüsü və mövcud shell boşluqları PNG ilə exact eyni deyil; tam pixel-parity iddiası yoxdur.

Log `/private/tmp/wobbrik-city-error-reviewed.log`; renderlər `/private/tmp/wobbrik-city-error-reviewed/`, xüsusən `production-shell-city-error-{390-font1,320-font2}.png`, `production-city-error-{320-font1,390-font2,360-font2-insets-long-balance}.png` və inset üçün `production-city-error-title-360-font2-insets-long-balance.png`.

Yoxlama əmri (android-billing daxilində, mövcud Android Studio JBR):

```sh
./gradlew -I ../docs/reviews/mobile-sizing-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*MobileShellRouteRenderTest.regular' --tests '*MobileShellRouteRenderTest.largeFont' --tests '*MobileShellRouteRenderTest.cityError*' :app:lintDebug -Dwobbrik.render.output=/private/tmp/wobbrik-city-error-reviewed --offline --console=plain
```

ui-craft contrast checker: Text/PanelTop13.58, Muted/PanelTop8.59, Background/PlayBottom7.66, Background/PlayTop13.83, Background/Accent11.45 PASS. Səth/gradient-də ən zəif yoxlanmış mətn cütü də4.5-dən yuxarıdır.

CI/emulator/merge/push/deploy işlədilməyib. Real device/account/API24/GPU/performance/TalkBack/IME/bütün locale və real inset yoxlanmayıb. Əvvəlki dirty iki baseline .dm və untracked cari audit faylı bu commit-ə daxil edilmir.
