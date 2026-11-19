# Bazar — seçilmiş material tonları və fasad silueti

19 noyabr 2026. `fix/market-approved-native`, baza `a98272c`, owner working diff saxlanır. Authority: `docs/PLAN.md` §1.1, §6 kataloq materialları və `docs/design/market-collection-v1/proposal.png`. Scope yalnız Bazar bina preview-lərinin material/siluet düzəlişidir. Commit/merge/CI/push/deploy yoxdur. `market-building-fidelity/scoped.diff` bu davam vahidinin əvvəlki working source-a görə diff-idir; əvvəlki owner/Şəhər/İnventar işi daxil deyil. İki yeni material PNG-si həmin mətn diff-indən ayrıdır.

## Material və seçilmiş hissələr

Əsas səbəb yalnız shader/glow deyildi: əvvəlki renderer `base_1_wall` seçiminə purple hörgü, tam `base_2` dəstinə teal hörgü qoyurdu. PLAN §6 və shipped catalog-map `base_1` üçün Əhəngdaşı, `base_2` üçün Kərpic hörgü, `base_3` üçün Suvaq, `base_4` üçün Panel materialını göstərir. İndi Bazar həmin seçilmiş wall ID-nin materialını çəkir. Account/catalog/part ID-lər dəyişmir; görünüşdəki şablon override-ı aradan qalxır. Native route QA fixture-indəki saxta “Kərpic hörgü1–4” adları da bu material adları ilə düzəldilib; bu yalnız test fixture-idir, production kataloq deyil.

- Kərpic üçün PNG-dəki isti qırmızı/plum, panel üçün lavender tonunda iki ayrı düz raster material hazırlanıb. Brick courses və panel seams/fasteners saxlanır. Onlar yalnız `base_2_wall` və `base_4_wall` material qatlarıdır. Suvaq, əhəngdaşı, taxta, beton və digər seçilmiş hissələr öz mövcud atlaslarından gəlir.
- Native ikiüzlü fasad, mətn/kart/idarəetmə, pəncərə, giriş, seçilmiş dam və işıq qatları ayrıca qalır. Generasiya bütöv bina və ya UI rasteri yaratmır. Built-in `image_gen` istifadə olunub; iki final prompt, source və workspace yolları `market-building-fidelity/asset-provenance.md`-dədir. Assetlər dəyişdirilmədən layihəyə köçürülüb.
- Purple/mixed fasadda material sahəsi böyüdülüb, qalın cream pilasterlər daralıb: seçilmiş hörgü/suvaq səthi daha fasiləsizdir. Ground core və annex də həmin materialdan istifadə edir; entrance/stone plinth/steps ayrıdır.
- Digər tam üslublarda aşağı mərtəbələrin təkrar open-top/cornice sahəsi seçilmiş davam edən masonry ilə dolur. Native konturun kənarı və alt lip-i saxlanır. Mövcud balkon/gül/məhəccər modulu hər ikinci yuxarı mərtəbədə çəkilir; əvvəl bu üslubların preview-lərində yox idi. Bu dekorativ renderer detalıdır, yeni domain kosmetika kateqoriyası və ya sahiblik deyil.
- Accepted centers, ofsetlər, floor count metadata-sı, floor pitch, mövcud6/8representative qaydası və selected window/roof/entrance/lighting ID-ləri dəyişmir. Başqa dam seçmiş binaya reference-in purple damı qoyulmur. Bazar parametrinin default false olması başqa ekranların görünüşünü saxlayır.

## Native müqayisə və nəticə

Son-source `reference-art-comparison.jpg`, `comparison.jpg`, `responsive.jpg` açılıb baxıldı. Altı tam dəst/qarışıq seçim üçün native selection sheet də yoxlanıb; son panel dəyişməsi ayrıca reference və responsive renderlərdə görülüb. Material tonları indi isti kərpic / lilac suvaq / lavender panel / açıq əhəngdaşı kimi ayrılır. Qalın fasad çərçivələri azalır, balkon çıxıntısı və selected materialın həcmi oxunaqlıdır.

`reference-art-comparison.jpg`: dəyişdirilməmiş PNG art crop-u / legacy default native / yenilənmiş native; native cütündə eyni düz centers və eyni wall/roof/window/entrance/lighting ID-ləridir. Bu synthetic valid seçimlərdir, real hesab screenshot-u deyil. `selected-parts-before-after.png` altı tam dəst və qarışıq hissələri eyni əyri centers ilə göstərir; sol tərəf market pass olmadan legacy renderer-dir, əvvəlki chatin son-source-u deyil. Route `comparison.jpg` isə əvvəlki `market-building-materials` native screenshot-u ilə bu davamın son native-sini müqayisə edir; fixture adlarının düzəlməsi yuxarıda qeyd olunub. Crop/montaj QA sübutudur, production asset deyil.

Reference-də suvaq/panel qüllələri bükülüb; həmin əyrilik real accepted centers-də yoxdursa əlavə edilmir. Hissələri fərqli seçmiş real binanın roof/window silueti də seçilmiş asset-dən gəlir. Bu seçim/həndəsə fərqləri PNG-yə görə dəyişdirilmir. Piksel eyniliyi iddia edilmir; reference-in arxa kol/garden kompozisiyası və bütün ekranın əvvəlki ikon/filter/header məhdudiyyətləri bu material vahidinin xaricindədir.

Yoxlanan native mobil portretlər:390×844 və320×568 fontScale1/2;360×640 font2+simulated24/48dp inset, uzun ad və Long.MAX_VALUE. Art frame-dən kəsilmə və yeni üst-üstə düşmə müşahidə edilmədi. Font2/tab/content scroll və own vertical layout əvvəlki adaptasiyadır. Emulator/canlı cihaz açılmayıb; nəticə Robolectric API34 native graphics + synthetic transport/account fixture-idir. Real ART/GPU/frame/API24/IME/TalkBack/bütün locale matrisi PASS sayılmır.

## Decode, scope və lokal review

Yeni `MarketBuildingMaterials` maksimum iki account-independent sprite saxlayır. Hər biri yalnız uyğun selected wall mounted olduqda `rememberArtPreparation` ilə IO-da açılır; metadata/network oxusunu bloklamır, art hissəsində əlçatan loading/error/retry var. Bütün kartlar və route remount-ları prosesdə eyni image-i paylaşır; main/draw thread-də asset open/decode yoxdur, eager başqa-screen prewarm yoxdur. Selected wall ID üzrə `key` loading subtree-ni ayırır; başqa materialın köhnə Ready state-i yeni hissəyə keçmir. Cancel olunmuş composition completion state yazmır; process cache yalnız immutable APK art-ıdır, account/session məlumatı deyil. Fixed asset sample density/config-dən asılı deyil; UI proyeksiyası cari DrawScope ölçüsünə uyğundur.

Disk mənbələri: brick2050799byte, panel1836425byte; cəmi3887224byte APK source materialdır, network payload deyil. Hər1254×1254PNG `inSampleSize=4` ilə313×313decode edilir; iki ARGB8888 image üçün nəzəri pixel payload783752byte (~0.75MiB), actual ART/GPU allocation ölçülməyib. Host log brick313×313 və təkrar load-da eyni sprite identity-ni təsdiqləyir. Yeni HTTP/cache/account payload yoxdur. Yeni bounded material sampling/mesh və maksimum6/8floor üçün balkon draw əməliyyatları var; latency/GPU optimallaşdırma iddiası yoxdur. Production cold/warm/frame benchmark aparılmayıb.

Lokal diff review: başqa ekranlarda false/default yolunun saxlanması; hər materialın yalnız uyğun ID-yə bağlanması; selected roof/window/entrance/lighting qatlarının itirilməməsi; below-top davam konturunun pitch/centers-i dəyişməməsi; immutable iki-entry material retention və retry/cancellation yolu yoxlanıb. Yeni layer/framework və server müqaviləsi yoxdur. DTO/API/schema/economy/read/journal dəyişmədiyindən AppBackend dəyişikliyi tələb edilmir. Backend/core/cache/offline/böyük hesab suite yenidən işlədilməyib.

## Son-source yoxlamalar

7/7 native host test PASS,0failure/error/skipped: beş mobil route/render, altı-style/mixed selection, reference-like dörd material composition. `assembleDebug`/`lintDebug` PASS; `checks.log`,7XML və native PNG-lər saxlanıb. Mövcud UI scope palette kontrast15/15 PASS, minimum7.41; raster materiallar dekorativdir. `git diff --check` PASS. İlk shell gradle əmri repo kökündə wrapper olmadığı üçün işləmədi; bu nəticə final yoxlama deyil. Son düzgün-source uğurlu run-un müddəti `checks.log`-dadır. Bir lokal review iterasiyasında inline `key` daxilində labeled return D8-in `NON_LOCAL_RETURN` xətasına səbəb oldu; loading məzmunu ayrıca private composable helper-ə çıxarıldı. Aralıq xəta `market-building-fidelity/review-key-dex-failure.log`-da saxlanıb, final nəticə deyil. Son-source nəticəsi `checks.log` və7XML-dir. Fokuslu yoxlamalar CI deyil.

Təkrar (`android-billing/` daxilində):

```sh
./gradlew --offline -I ../docs/reviews/market-building-fidelity/render/render.init.gradle :app:testDebugUnitTest --tests 'com.aladdin.wobbrik.MarketApproved*Test' --tests 'com.aladdin.wobbrik.MarketReferenceSelectionTest' --tests 'com.aladdin.wobbrik.MarketMaterialSelectionTest' :app:assembleDebug :app:lintDebug -Dwobbrik.render.output=/private/tmp/wobbrik-market-fidelity-final
```
