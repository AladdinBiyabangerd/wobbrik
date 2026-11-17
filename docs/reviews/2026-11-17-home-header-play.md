# Home — ortaq header mövqeyi və Play render yoxlaması

17 noyabr2026. Branch `feat/native-ui-parity`, baza `05070ff`; əvvəlki hero düzəlişi ilə eyni working diff. Authority PLAN §1.1, unified-mobile-v3 README/Home və04 panel24; əlavə cari istifadəçi göstərişi: Home logo/balans sətri digər səhifələr ilə eyni hündürlükdə olsun. Yeni dizayn deyil.

## Dəyişiklik və lokal review

Home header əlavə HeaderTop24dp və daxili Small8dp alırdı; ortaq MobileMenuShell header-i yalnız Small8dp alır. Header body-nin içindən çıxarıldı və shell ilə eyni Gap16dp horizontal/Small8dp vertical padding-ə keçirildi. Beləliklə əvvəlki24dp üst fərq və8dp horizontal fərq aradan qalxır. Body padding-i, native artwork/stat/XP, safe inset consumption,7-slot dock, Play/lifecycle, normal font1 və əvvəlki font2 hero yolu saxlanır. Header yuxarı qalxdığından body daha çox yer alır; hero əvvəlki aspect-fit qaydası ilə uyğunlaşır. Uzun balans əvvəlki kimi ayrıca horizontal-scroll sətrində qalır; bütün menyularda eyni davranışdır.

Play üçün production navbar dəyişdirilmədi. Əvvəlki `/private/tmp/wobbrik-home-hero/production-home-390-font2.png` crop-u boş səthdir. Ara turda eyni source/composition üçün ilkin capture-da bütün navbar itdi, növbəti capture-da çəkildi; tək test probe-da həm software draw, həm PixelCopy Play çəkdi. Bu müşahidə host capture/layer yenilənməsi ilə uyğun gəlir, konkret cihaz/GPU səbəbi sübut edilməyib. Cari source-da UI regression-u təkrar yaranmadı.

Home host helper-i artıq girişdə və kliklərdən sonra capture alır, saxlanmış PNG-ni decode edib Play bounds daxilində cyan dairə (>25%) və dark triangle/background (>5%) piksellərini assert edir. Çəkilməzsə maksimum3 native invalidate/draw cəhdi verir, sonra FAIL edir; boş snapshot PASS olmur. Son12 testdə bütün capture-lar birinci cəhddə keçdi, redraw retry olmadı.390/font2 ilkin/son capture üçün4096 nümunədən2718cyan və1230dark alındı. Mətn/semantics yoxlaması piksel sübutunun əvəzi deyil.

Diff üzrə yerləşmə/import, mövcud tokenlərin təkrarı, input/state sərhədi və PNG guard yoxlandı; yeni code finding yoxdur. AppBackend/API/domain/schema/wallet/revision/cache/account müqaviləsi dəyişmir və yeni repository çağırışı yoxdur; backend dəyişikliyi tələb olunmur. Əvvəlki iki.dm və untracked audit toxunulmadan qalır. İstifadəçi17 noyabrda bu Home vahidinin commit-ini təsdiqlədi; source/test/iki Home review/handoff həmin commit-ə daxildir. Merge/push/CI yoxdur.

## Yoxlamalar və baxılmış renderlər

Son HomeProductionRenderTest12/12 PASS, failures/errors/skipped0, BUILD SUCCESSFUL40s. Normal390 və kiçik320 font1/font2,360/font2+simulated24/48 inset, uzun balans və recovery/retry keçir. Header parity testləri Home və real MobileMenuShell chrome-unu eyni viewport/font/balansla göstərib logo və balans text bounds-u tam bərabər assert edir:390 font1/font2,320 font1,320/font2+inset+Long.MAX_VALUE. Shell müqayisəsində body qəsdən boşdur: bu şəhər scene testi və ya tam ekran qəbulu deyil.

Renderlər `/private/tmp/wobbrik-home-header-play-encoded/`, log `/private/tmp/wobbrik-home-header-play-encoded.log`. İlkin müqayisə renderləri `/private/tmp/wobbrik-home-header-play-final/`-dadır. Home/shared-header cütləri390/font1/font2,320/font1 və320/font2+inset+uzun balans açılıb baxıldı;390/font2 giriş/klik sonrası PNG-lər də açıldı. Bəzi full-image preview-larda Play görünmədiyi halda saxlanmış PNG-də guard keçir; ayrıca192×192 crop `/private/tmp/wobbrik-play-encoded-crop.png` açılaraq dairə/üçbucaq təsdiqləndi. Köhnə390/font2 crop `/private/tmp/wobbrik-play-original-crop.png` boşdur. Cari390/font2 tam renderlərdə də Play görünür. Hostdan kənar runtime nəticəsi iddia edilmir.

Normal render-init olmadan `:app:assembleDebug :app:lintDebug --offline --console=plain` PASS,1m1s; `/private/tmp/wobbrik-home-header-play-build.log`. Bundan sonra yalnız host test helper-ində encoded-PNG guard təkmilləşdirilib və12test yenidən keçib; production source dəyişməyib. `git diff --check` PASS. Kontrast Text/Surface13.00, Muted/PanelTop8.59, Background/PlayBottom7.66 PASS; palette dəyişməyib.

Real cihaz/account/API24/GPU/performance/TalkBack/IME/bütün locale/tablet yoxlanmayıb; emulator açılmayıb. Host capture itkisinə qarşı guard var, real cihazda görünməmə səbəbi təsdiqlənmir. Tam app pariteti/mərhələ5 qəbulu deyil. Font2 hero mövcud96dp-dir, çox uzun balans/sıx inset-də statistika scroll arxasında qalır.
