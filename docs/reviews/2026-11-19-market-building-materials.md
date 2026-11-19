# Bazar — modul fasad, dam/balkon və işıq

19 noyabr 2026. `fix/market-approved-native`, baza `a98272c`, mövcud owner working diff saxlanır. Authority: `docs/PLAN.md` §1.1 və `docs/design/market-collection-v1/proposal.png`. Əvvəlki rədd edilmiş fon sınağı tətbiq edilməyib. Bu vahidin production diff-i `market-building-materials/scoped.diff`-dədir; branch-in bütün owner diff-i deyil. Commit/merge/CI/push/deploy yoxdur.

## Dəyişiklik

- Bazar kart/detail/seçim preview-ləri `MarketBuildingPreview` vasitəsilə mövcud renderer-in marketMaterials parametrini aktiv edir. Digər ekranlarda parametr false qalır. Hissə ID-ləri, atlas seçimi, qəbul edilmiş centers, mərtəbə metadata-sı, mövcud representative floor/offset qaydası dəyişmir.
- Fasadın iki üzündə native clipped işıq-kölgə: üst çıxıntının kölgəsi, ön üzdə isti əks işıq, yan üzdə tündləşmə. Mövcud `GameTokens.Background` və `MobileHome.GoldLight` istifadə edilir; yeni palitra/raster/bitmap decode yoxdur. Seçilmiş divar teksturası işığın altında saxlanır; pəncərə/entrance/lighting qatları sonra çəkilir.
- Purple modul şablonunda pəncərə çərçivəsi ətrafında daha çox hörgü görünür; native proyeksiya .45/.60 en və .82 hündürlükdən .38/.54 və .70-ə dəyişir. Eyni seçilmiş pəncərə sprite-ı, coins corner kit-in yarım-cell qaydası saxlanır. Mövcud isti pəncərə/lampa sprite və glow qalır.
- Mərtəbə arası lip14→6 art vahidi; yuxarı hörgü eyni8vahidlə uzanır, beləliklə birləşmədə boşluq açılmır. Pitch və floor centers dəyişmir. Balkon225×175→252×196 art vahidi, sol çıxıntı və sill bağlantısı dəyişir; mövcud gül/məhəccər/sallanan yaşıllıq sprite-ıdır. Yalnız base_1 roof saçağı10art vahidi genişlənir; dormer və baca həmin dam asset-indədir. Digər seçilmiş damlara purple dam tətbiq edilmir.
- Bunlar dekorativ art koordinatlarıdır, yeni UI ölçü tokenləri və ya domain geometry deyil. Bütöv bina PNG-si, saxta kataloq və ya ikinci render cache yaradılmır.

## Native müqayisə

`market-building-materials/{comparison,art-detail,responsive}.jpg` və `selected-parts-before-after.png` açılıb baxıldı. Comparison təsdiqli dəyişdirilməmiş PNG / əvvəlki card-scale native / son native-dir. Art-detail yalnız screenshot crop-udur, production asset deyil. Selection sheet altı tam üslub və qarışıq wall/window/roof/entrance/lighting seçimini eyni əyri centers ilə əvvəl/sonra göstərir. Hörgü sahəsi və balkon çıxıntıları daha oxunaqlıdır; roof saçağı görünür, side shading materialın həcmini vurğulayır. Görünən art-ı loading ilə müqayisə etməmək üçün yeni host harness hər çəkilişdə mounted `Qrafika yüklənir…` state-lərinin bitməsini gözləyir.

Tam PNG pariteti deyil: PNG-nin qırmızı/çəhrayı fasad tonları ilə mövcud selected atlas materialları fərqlənir; altı tam üslub eyni reference binaya çevrilmir. Mövcud6/8representative mərtəbə, annex/garden formaları və real offsetlər saxlanır. Digər üslublarda balkon/dam silueti seçilmiş asset-dən gəlir; yalnız işıq-kölgə dəyişir. Reference-dəki arxa kol kompozisiyası əlavə edilməyib. PNG-yə tam material/siluet uyğunluğu hələ açıq vizual borcdur; bu düzəliş onun tam həlli kimi təqdim edilmir.

Normal390×844, kiçik320×568, fontScale1/2 və360×640 font2+simulated24/48dp inset+uzun mətn/Long.MAX_VALUE halları capture edildi. Responsive montaj açılıb baxıldı; art frame daxilində qalır. Font2/tab horizontal scroll, own vertical layout və zəruri əsas-content scroll əvvəlki adaptasiyadır. Bu art vahidi əvvəlki navbar/filter/header/uzun mətn clipping borclarını həll etmir. Emulator/device/ART/GPU/frame/IME/TalkBack/API24/bütün locale matrisi yoxlanmayıb; synthetic HTTP/account ilə Robolectric native graphics nəticəsidir.

## Yoxlamalar və lokal review

Son-source5route/render+1selection native test =6/6 PASS,0failure/error/skipped; `assembleDebug`/`lintDebug` PASS. `checks.log` və6XML saxlanıb. Mövcud scope palette kontrast15/15 PASS, minimum7.41 (`contrast.txt`); art üçün dekorativ gradientlər mətn kontrastı iddiası deyil. `git diff --check` PASS. İlk shell əmri yanlış cwd səbəbilə init faylını tapa bilmədi; son düzgün-source run yuxarıdakı uğurlu run-dır. İlkin wrapper sandbox cache yazısında dayandı; icazəli fokuslu run tamamlandı.

Lokal diff review-da optional parametrin başqa ekranlarda false qalması, atlas/selected layer və representative centers yolunun saxlanması, lip daralanda shell-in boşluğu doldurması yoxlandı. Əlavə HTTP/disk/decode/cache/prewarm/frame-state yoxdur. Xərc mövcud maksimum6/8display mərtəbə üçün iki bounded face gradient/path-dir; benchmark/latency/GPU yaxşılaşması iddia edilmir. Route/mutation regression mövcud real repository/journal çağırışlarını synthetic transport-la yoxlayır. Core/cache/offline/böyük hesab/backend suite təkrar işlədilməyib. DTO/domain/API/schema/read/mutation müqaviləsi dəyişmədiyi üçün AppBackend dəyişikliyi tələb edilmir.

Təkrar native/build/lint əmri (`android-billing/` daxilində; mövcud cached host asılılıqları):

```sh
./gradlew --offline -I ../docs/reviews/market-building-materials/render/render.init.gradle :app:testDebugUnitTest --tests 'com.aladdin.wobbrik.MarketApproved*Test' --tests 'com.aladdin.wobbrik.MarketMaterialSelectionTest' :app:assembleDebug :app:lintDebug -Dwobbrik.render.output=/private/tmp/wobbrik-market-materials-final
```
