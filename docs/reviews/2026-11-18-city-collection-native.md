# Şəhər — collection layout vahidi

18 noyabr 2026. Branch `feat/city-collection-design`, baza `a98272c` üzərində working diff. Təsdiqli authority `docs/design/city-collection-v1/proposal.png`, yeganə plan `docs/PLAN.md` §1.1. Owner-in iki release `.dm` və `2026-11-18-inventory-city-detail-costs.md` dəyişiklikləri saxlanıb.

## Dəyişiklik və sərhəd

Mobil Şəhər eyni İnventar `CollectionHeader`/7-slot navbarını paylaşır. Header yalnız lazım olan scenery resursunu IO-da açır; İnventar kart fonu yüklənmir. Navbar üçün ayrıca yer rezerv edilir; scene/CTA dock altında qalmır. Köhnə mobil geri/Plan/Görünüş rail-i təsdiqli nümunəyə əsasən Şəhərim/refresh ilə əvəzlənib. Tablet/landşaft route seçimi dəyişməyib; legacy content layout saxlanır.

Scene native gradient/konturlu paneldədir. Fit/focus native Canvas ikonları48dp input sahəsi, selected/disabled semantikası və mövcud collection press helper-i ilə işləyir. Real seçilmiş binanın foundation-u cyan konturla vurğulanır. Alt panel adı real kataloqdan, mərtəbə/sakin sayını real building DTO-dan alır; preview mövcud BuildingPreview-dir. Binaya bax mövcud real bina focus-unu, Şəhərə qayıt tam səhnəni açır; Android Back əvvəl focus-dan çıxır, sonra home-a qayıdır. Boş halda İnventara keç real navigation callback-dir. Refresh mövcud account-bound read-i yeniləyir; dummy city adapter, mülkiyyət, placement və ərazi girişi yoxdur.

Böyük şriftdə info/action şaqulidir, uzun məlumat məhdud hündürlükdə scroll olunur, əsas əməliyyat görünür. Loading/error/retry/empty/cache halları native-dir. Keş yalnız baxış olaraq işarələnir; render review-da etiketi binanın üstünü örtürdü, scene-in üst padding-i ilə ayrıldı. Qismən qrafika/həndəsə çatışmazlığında bütün binalar renderlənmirsə xəbərdarlıq göstərilir. Mövcud API/DTO/revision/UID/login epoch/final-sync/cache sərhədləri dəyişməyib. AppBackend-in `docs/wobbrik-game-api.md` city bundled scene details müqaviləsi yoxlandı; UI vahidi yeni server sahəsi və endpoint tələb etmir, backend dəyişiklik/test/deploy yoxdur.

Şəhərin dekor/terrain/garden/life atlas loader-ləri mövcud mənbələri istifadə etməklə IO-ya çıxarıldı. Hesabdan asılı olmayan immutable nəticə ilk lazımi city açılışında hazırlanır və process daxilində təkrar istifadə edilir; Activity/account saxlanmır. Ləğv edilmiş route sonradan Compose state-ə nəticə yazmır. Art metadata/retry-ni bloklamır, loading/failure yalnız scene slotundadır. Yeni raster production asset və yeni stack pin əlavə edilməyib.

## Vizual nəticə və açıq iş

**Bu vahid tam PNG pariteti deyil və Şəhər redesign-ı bütövlükdə tamamlanmış sayılmır.** Layout/header/navbar/controls/info panel native tətbiq edilib, amma scene mövcud modular bina artını, real parcel konturunu və yerləşdirilmiş dekorları göstərir. Nümunənin üzvi daş sahili, sıx ağac/yol kompozisiyası və təsvir etdiyi bina üslubu hazırkı scene-dən aydın fərqlənir. Whole-screen raster ilə fərq gizlədilməyib; beş sample bina və dekor ownership kimi əlavə edilməyib. Scene artwork uyğunlaşdırması qalan işdir, yeni dizayn seçimi deyil. `city-collection-native/comparison.jpg` bunu yanaşı göstərir; əvvəlki screenshot başqa synthetic fixture-dir, performans müqayisəsi deyil.

Normal/kiçik portretdə native layout işləyir. Font2/narrow zamanı səhnə daha kiçikdir, summary scroll, compact ikon navbarı və320dp-də mövcud wrapping dock qalır. Bu uyğunlaşmalar proposal-ın statik kompozisiyasından fərqlənir. Bütün locales, real TalkBack, API24, real inset/IME, canlı hesab, cihaz/ART/GPU/frame/latency yoxlanmayıb. Emulyator avtomatik açılmayıb.

## Xərc və lokal review

Yeni rəng/ölçü sistemi yaradılmayıb; `GameTokens.InventoryCollection`, mövcud spacing/type/control pillələri təkrar istifadə edilib. Kontrast6/6 PASS, minimum7.41:1 (`city-collection-native/contrast.txt`). Native screenshot matrix/comparison açılaraq baxılıb; yalnız build PASS-a əsasən uyğunluq iddiası yoxdur.

Eyni boş-city transport fixture: cold4 sorğu/760 response UTF8 baytı, warm0, offline accepted replay0, refresh4. City data API/serializasiya dəyişmədiyindən yeni network xərc yoxdur. Mövcud core15 CitySceneReads testi12-building bundled/legacy fallback, revision/geometry/cursor/epoch və məhdud concurrency-ni yoxlayır; cache6 ilə birlikdə cəmi21core testidir. Production first-content və böyük real hesab latency-si ölçülməyib.

Atlaslar əvvəldən APK-dədir: decor/garden/balcony/terrace/life və terrain crop-larının təxmini saxlanmış RGBA həcmi37,630,176baytdır (~35.9MiB; decoder/GPU overhead daxil deyil). Əvvəl route daxilində yenidən decode edilirdi; indi process lifetime-da paylaşılır. Bu, təkrar giriş decode işini azaldır, amma memory retention artır; real cihaz heap/GC ölçülməyib, performans optimallaşdırma benchmark iddiası yoxdur. Geometry/projection işi və köhnə per-frame layer sıralaması ayrıca ölçülməyib.

Lokal diff review: native/data sərhədi, route seçimi, callback-lər, cache statusu, disabled/retry, marker camera coordinates, art cancellation/ownership, importlar və scope yoxlandı. Lint ilk run-da scene slot çıxarıldıqdan sonra legacy wrapper-in artıq istifadə edilməyən BoxWithConstraints scope-unu tapdı; Box ilə düzəldildi. İlk ümumi komanda lint səbəbindən FAILED idi, həmin run-ın keçmiş unit nəticələri ümumi PASS kimi təqdim edilmir. CI/merge/push/deploy yoxdur.

## Son yoxlama

Keş etiketi düzəlişindən sonrakı `:app:assembleDebug`, `:app:lintDebug`, seçilmiş `:app:testDebugUnitTest` və `:core:test` komandası PASS (`/private/tmp/city-collection-sealed.log`, BUILD SUCCESSFUL in57s). App53/53, core21/21;0failure/error/skipped. App daxilində native layout/action5 və account-bound City read12 testidir. Core nəticələri eyni dəyişməz mənbədə UP-TO-DATE olaraq əvvəlki uğurlu run-dan istifadə edilib; yeni core execution kimi təqdim edilmir.

Son31 native screenshot `city-collection-native/screens/` altında saxlanır.320×568/390×844 font1/font2,360×640 font2 + süni24dp top/48dp bottom inset, Long.MAX_VALUE balans və uzun material adı yoxlandı. Normal/focus/cache/empty/error/loading, fit/focus/refresh/retry/navigation, disabled kamera və real bina accessibility custom action-ları yoxlanıb. Uzun info summary-si scroll edilərək metadata-nın əlçatanlığı ayrıca assert və screenshot ilə yoxlanıb. Bütün son matrix və əvvəl/proposal/native comparison açılaraq baxılıb. Normal/native original və kiçik/font2/inset nümunələri ayrıca da açılıb. Son `git diff --check` PASS. Yoxlamalar synthetic host + bundled production art-dır; real server/account/device nəticəsi deyil.

Qalan dəqiq iş: təsdiqli proposal-dakı scene artwork-u qəbul edilmiş server kontur/geometry və kataloq hissələri ilə uyğunlaşdırmaq; mövcud renderləri piksel-paritet kimi qəbul etməmək. Bu iş üçün yenidən dizayn təsdiqi tələb olunmur. Lokal layout vahidi review olunub, amma tam şəhər redesign qəbulu, commit və main merge bu nəticə ilə iddia edilmir.
