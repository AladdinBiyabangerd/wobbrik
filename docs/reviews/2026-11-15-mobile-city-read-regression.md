# Mobil şəhər — oxu/retry və font2 viewport regression

Branch: `fix/mobile-city-read-regression`; baza `main@6c96e9c`. Uncommitted production diff yalnız `CityScreen.kt`; host diff `mobile-sizing-visual/render/MobileShellRouteRenderTest.kt`. Owner baselineProfiles və account-compact faylları qorunur. Commit/merge/push/CI/emulator yoxdur.

## Tapıntı və düzəliş

Əvvəlki shell hostunun şəhər fixture-i `geometry_version`, `placement_enabled`, `parcels` vermirdi və decorations oxusunu tanımırdı. Beləliklə yalnız error route render olunurdu; məzmunlu şəhərin shell daxilində işləməsi yoxlanmırdı. Fixture AppBackend cari CityPage/müqaviləsi ilə uyğunlaşdırıldı; bu production server xətasının root cause-u deyil.

Yeni məzmunlu ssenari **320×568 font2-də Canvas-ın görünmədiyini** tutdu: header, əhali və böyük aşağı panel bütün content hündürlüyünü tuturdu. Yalnız mobile shell/fontScale>1 qolunda header/səhnə/status/ikinci dərəcəli bina məlumatı bir scroll sahəsinə ayrıldı; səhnə mövcud Target×3 token hündürlüyündədir. Bax/Geri ayrıca sabit düymədir; xəta halında onun yerində sabit Yenilə göstərilir, dock dəyişmir. Loading/error-da boş səhnə yeri ayrılmır. Normal font və shell-dən kənar kompozisiya saxlanır; yeni palette/asset/domain əlavə olunmur.

Plan-ın deaktivliyi CityHeader-də açıq `disabled()` semantikasıdır, itmiş callback deyil. Son plan placement-i bağlı saxlayır; bu vahid Plan və ya placement mutasiyası açmır. PLAN §1.1-dəki Plan regression tələbi bu məhdudiyyətlə oxunur. Aktiv Plan davranışı ayrıca açıq qərar olmadan uydurulmur.

## Yoxlama

- Final native `MobileShellRouteRenderTest`: **4/4 PASS,43s**. 360×640font1,390×844font1,320×568font2 + əvvəlki recovery/inset ssenarisi. HTTP503→error→native Yenilə→matching revision/geometry/parts/population→Bax→Geri→İnventar; ilk failed read + iki bracket read =3, oxu body-ləri null və iqtisadi submit=0. Plan ancestor disabled assertion saxlanır.
- Host sintetik transport/account-dur. Məzmunlu şəhərin perpetual animasiyası avtomatik idle gözləməsini blokladığından mövcud `lifeTimeSeconds=0.0` injection istifadə edilir; production clock dəyişmir. Əvvəl manual-clock + scroll cəhdi dayandırıldı, PASS sayılmır. Canlılıq/performance testi deyil.
- Normal `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`: **PASS25s**, son production source üzrə. CI deyil.
- Kontrast4/4PASS, minimum6.48; palette dəyişmir. Diffcheck PASS.
- Son city360/390/font2 və errorfont2 native PNG-lər açılıb baxıldı; approved `design/unified-mobile-v3/01-main.png` ilə müqayisədə əvvəlki chrome/skin/kompozisiya fərqləri qalır. Font2-də scene scroll ilə görünür, Bax sabitdir; scroll edilmiş capture-da header-in bir hissəsinin viewport-dan çıxması gözlənilən scroll davranışıdır. Normal renderlərdə header/səhnə/panel/dock görünür; əvvəl/sonra piksel eyniliyi iddia edilmir.
- Lokal evidence: `/private/tmp/wobbrik-city-route-{native,normal}.log`; `unified-mobile-visual/production-shell-city{,-error}-{360-font1,390-font1,320-font2}.png`.

AppBackend `internal/wobbrik/city.go` və `docs/wobbrik-game-api.md`-nin son geometry müqaviləsi read-only yoxlandı; mobil DTO ilə uyğun gəlir. Backend/API/schema/wallet/revision/journal dəyişmir, backend dəyişikliyi/testi tələb olunmadı. Real hesabın əvvəlki şəhər oxu xətası log/HTTP cavabı olmadan təsdiqlənməyib. Device/API24/IME/TalkBack/GPU/locale matrisi yoxlanmayıb. Tam §1.1 və mərhələ qəbulu deyil.

## Lokal diff review

Düzəliş yalnız böyük şriftli mobile shell layout qolundadır; hesab/session fence, repository, server geometry və seçilmiş hissələr dəyişmir. Sabit primary action və scroll sahəsi ayrılıb, nested scroll yoxdur. Yeni konkret domain və ya naviqasiya problemi tapılmadı. Qalan vahidlər: tam clean skin/ikon/hero pariteti, sahiblik/alış bölgüsü, real city read səbəb sübutu və müqayisəli performans ölçməsi.
