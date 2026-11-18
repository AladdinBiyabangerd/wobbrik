# Bina detalı və vahid navbar — native review

18 noyabr 2026. Branch `feat/inventory-art-led-redesign`, baza `c2600f1`; yoxlanan diff bu review ilə saxlanan feature dəyişiklikləridir. İstifadəçi detail proposal üçün “okey implement” dedi. ui-craft token/render/kontrast workflow tətbiq edildi.

## Dəyişiklik və lokal review

`OwnedBuildingDetailPresentation.kt` native hero, real bina adı, sahiblik, mərtəbə/sakin, maksimum satış, şəhər statusu və mövcud əməliyyatları göstərir. Köhnə mobil detail renderer çıxarıldı; tablet yolu saxlandı. Satış/traded və fresh mutation icazələri, silmə təsdiqi saxlanır. Account/revision/cache/domain müqaviləsi dəyişmir. Mock-only detail lookup `?view=display` query-ni düzgün ayırır; regression testi əlavə edildi.

Mobil menyu və Home `CollectionNavbar`-a yönləndirildi; legacy navbar painter çıxarıldı. Active route, disabled/active Play və 48dp minimum toxunma sahələri saxlanır. Bazar sheet offset-i yeni dock ölçüsünə uyğunlaşdırıldı. Digər səhifələrin məzmunu redesign edilmədi.

Review zamanı hero ölçüsü, məlumat panellərinin hündürlüyü və böyük şriftdə scroll düzəldildi; regular hero upscale yalnız uyğun purple template üçün tətbiq edilir. Əlavə blocking tapıntı yoxdur. Owner iki release .dm və əvvəlki city-detail review dəyişiklikləri daxil edilmir.

## Sübut və məhdudiyyət

Final native Robolectric/Compose testləri 9/9 PASS: 390×844 və 320×568, fontScale 1/2, 360×640 böyük şrift/inset/uzun mətn; route/disabled Play, market sheet və silmə dialoqunu ləğv etmə. `building-detail-native/evidence/native.txt` və XML-lər. Renderlər açılıb yoxlanıldı; normal kompozisiya approved proposal ilə müqayisə edildi. Kiçik/böyük şriftdə məlumatlar scroll olunur, əməliyyatlar çatımlıdır. Bu host native renderdir, production ART/frame/GPU ölçməsi deyil. Kontrast altı cütdə 4.5-dən yüksəkdir (`contrast.txt`).

İstifadəçinin əvvəlki debug göstərişi ilə Android Studio mockDebug build yeniləndi, debugger Pixel6 API37.1-ə qoşuldu, inventardan detail açılıb görüldü. Faktiki lokal fixture: 14 mərtəbə, 56 sakin, 28 maksimum satış, balans1250. Mock-da in_city yoxdur, buna görə “Şəhərə aidlik yüklənməyib” dürüst göstərilir. Real hesab/backend runtime yoxlanmayıb.

Yeni şəbəkə sorğusu və art decode yolu yoxdur; mövcud qəbul edilmiş detail/cache və paylaşılan IO art helper istifadə olunur. Əvvəlki eyni fixture axın ölçüsü 3 inventory +1 detail,13833byte,0city/0mutation saxlanır; bu vahid production latency optimallaşdırması iddia etmir. Art iki paylaşılan bitmap təxminən2.33MiB RGBA; əlavə art yoxdur. Backend domain/API/schema dəyişikliyi tələb olunmadı; iqtisadiyyat authority-si dəyişmir. Cold/warm/offline böyük-account performans matrisi bu vizual vahiddə yenidən ölçülməyib.

Main merge, CI, remote push və deploy edilməyib.

Final normal yoxlama: debug41/41 və mockDebug44/44 unit PASS; assembleDebug, assembleMockDebug, lintDebug PASS (1m1s). Native9/9 PASS (1m15s). Device PNG ayrıca açılıb yoxlanıldı; mock art və geometry proposal-dakı nümunədən fərqlənir, bina hero-sunda üst dam/baca hissəsinin kənara yaxın kəsilməsi mövcuddur. Pixel-perfect uyğunluq iddia edilmir.
