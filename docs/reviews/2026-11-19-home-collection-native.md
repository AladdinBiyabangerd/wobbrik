# Ana səhifə — təsdiqli kolleksiya dizaynının native tətbiqi

Branch `feat/home-collection-native`, baza `85ae944` + bu vahidin diff-i. Authority: `docs/PLAN.md` §1.1, istifadəçinin 19 noyabr təsdiqi, `docs/design/home-collection-v1/proposal.png`. Əvvəlki işlərə toxunulmayıb. Mobil portret; legacy tablet branch dəyişmir.

## Dəyişiklik və lokal review

`MobileHomeScreen.kt` ortaq CollectionHeader/CollectionNavbar-a keçirildi: eyni skyline, crop edilmiş logo, sağ balans, yeddi native ikon və Ana selected state. Mərkəzi Play yazısızdır; active təsvir, disabled, route callback və journal block saxlanır. Təsdiqli kompozisiyanın şəhər hero-su mövcud şəffaf `home_city_cutout_v3` ilə verilir. Native gradient/rim panel, səviyyə/rekord, XP intervalı və növbəti səviyyəyədək qalan XP göstərilir. XP label dolu hissəyə sığanda tünd ink ilə həmin hissədə, sığmayanda navy örtük üzərində açıq ink ilə görünür. Raster UI skin yoxdur; yeni bitmap yoxdur. Mövcud GameTokens rəng/ölçü pillələri istifadə edilir.

Loading/error/retry/journal recovery ayrıca native vəziyyətlərdir. İlk yoxlama retry üçün mobil local olmadan legacy ProfileAction skin yolunu aşkar etdi; birbaşa NativeProfileAction istifadə edilərək düzəldildi. Böyük font body scroll edir; uzun XP/balans sətirləri horizontal scroll ilə bütöv sözləri saxlayır. Dock content-dən ayrıdır; dar320dp-də ortaq dock iki sıraya keçir, font2-də ikonlar semantik adlarını saxlayır. Native düymələr minimum48dp; testlər bütün altı route və Play-i klikləyir.

Domain/repository/API/DTO/schema dəyişmir. Ana səhifə host-un accepted snapshot-ını göstərir, yeni request/cache/polling əlavə etmir. Account/UID/revision/fresh mutation/final sync sərhədləri həmin host-da qalır. AppBackend dəyişməsi tələb olunmur; yeni backend müqaviləsi yoxdur. Son oyunçu məlumatı qəbul edilmiş kimi uydurulmur, nümunə rəqəmlər render fixture-indədir.

## Performans sərhədi

Bu UI komponetinin cold/warm/refresh zamanı öz yaratdığı domain request/byte sayı0/0-dır (source review; host-un əvvəlki sinxronizasiyası bu rəqəmə daxil deyil). Bütün şəhər/inventar oxusu, N+1 və art prewarm yoxdur; art yalnız mount/config üzrə mövcud Dispatchers.IO decoder-indədir, crop BitmapPainter source rect-dir, yeni bitmap allocation etmir. Header əlavə decode1024×341, nəzəri RGBA1,396,736byte; PNG326,573byte. Hero dəyişməyib1283×1226, PNG2,085,814byte. Yeni cache və payload yoxdur; decode helper-in cancellation/config sərhədi saxlanır. Offline/retry/null snapshot/long balance host fixture-ləri yoxlanır. ART ilk məzmun/təkrar giriş latency-si, real şəbəkə request sayı və GPU/frame ölçülməyib; optimallaşdırma və production latency iddiası yoxdur.

## Yoxlamalar və vizual məhdudiyyətlər

Render harness: `home-collection-native/render/`; Robolectric34 native host, mövcud cached dependency-lər, layihənin daimi build faylına əlavə yoxdur. İlk12test8PASS/4FAIL: iki legacy dock-bottom assertion, async logo tapılmaması və legacy retry skin xətası. İkinci14test11PASS/3FAIL: retry skin-in null dependency-si; üçüncü14/14PASS və assembleDebug/lintDebugPASS57s. Testlər header parity,390×844/320×568font1/2,360×640font2+24/48simulated inset,Long.MAX_VALUE balans, fərqli XP intervalı, offline retry, loading və journal block-u əhatə edir. İlk/ikinci XML saxlanıb. Son spacing düzəlişinin yenilənmiş nəticəsi aşağıda qeyd olunur.

Kontrast checker4/4PASS: Text10.71,Muted6.77,Accent7.40 (panel radial işığının güclü stop-u üzərində); tünd XP ink/PlayBottom7.66. CSS/checker output review qovluğundadır. Bu dekorativ artın hər pikselinin kontrast ölçüsü deyil.

Normal/kiçik/font2/inset/offline native PNG-lər açılıb; approved-vs-native müqayisəsi hazırlanıb. Əsas kompozisiya və ortaq kolleksiya chrome-u saxlanır. Generativ şəhərin xırda bina detalı, panel materialı, native vector ikonları və aşağı künc yarpaqları PNG ilə piksel eyni deyil; aşağı yarpaq dekoru ayrıca əlavə edilməyib. Dock generativ PNG-dəki boş alt sahə əvəzinə safe bottom-a bağlanır. Bəzi post-click host PNG-lərdə logo/Play display-list qatı çəkilməyə bilər; tam ilkin render müqayisə üçün seçilir. Bu runtime cihaz qüsuru kimi sübut edilməyib. Font2 uzun qalan-XP sətri horizontal scroll edir.

Emulator açılmayıb; real cihaz/API24/TalkBack/GPU/bütün lokal kombinasiyalar yoxlanmayıb. CI/local-ci.sh/merge/push/deploy yoxdur. Bu yalnız Ana səhifə vahididir, bütün app redesign qəbulu deyil.

Son spacing düzəlişindən sonra14/14PASS (host test15.704s), assembleDebug/lintDebugPASS, BUILD SUCCESSFUL40s. Son XML/log/hash saxlanıb. Son normal ilkin PNG,320font2,360font2+inset və yanaşı müqayisə yenidən açılıb yoxlanıb. `git diff --check`PASS. Generativ referensə tam piksel pariteti iddia edilmir; yuxarıdakı dekor/material fərqləri qalır.
