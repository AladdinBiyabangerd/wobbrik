# Bazar — təsdiqli Yeni elan native axını

19 noyabr 2026. Branch `feat/market-new-listing-native`, baza `82e6ca0`, working diff. İstifadəçi `design/market-new-listing-v1/proposal-v4.png` və `price-keyboard-v1.png` nümunələrini təsdiqləyib implementasiyaya başlamağı tapşırdı. PLAN§1.1 və design README yeniləndi. Əvvəlki Uğurlar/Şəhər owner işi saxlanır. Commit/merge/CI/push/deploy yoxdur.

## Dəyişən davranış

`MarketListingUi.kt` yeni native feature UI-dir: kolleksiya səthli Yeni elan başlığı, informativ iki mərhələ strip-i, real bina kartları, şəkilsiz qiymət mərhələsi. Bina seçimi `MarketScreen.kt`-nin mövcud accepted inventory səhifəsidir; traded bina seçimdən çıxarılır, bounded pagination/nullable next saxlanır. Bina önizləməsi mövcud `MarketBuildingPreview` və art helper-lərindən gəlir; yeni raster və bütöv PNG skin yoxdur. Optional art yüklənməsi metadata/seçimi bloklamır.

Qiymət mərhələsində bina adı/mərtəbə horizontal scroll ilə yığcam sətirdə, Dəyiş əməliyyatı sağdadır. Şəkil təkrarlanmır. Normal fontda qiymət sahəsi mövcud scale-dən152dp, font2-də240dp maksimum enli native TextField-dir. Rəqəmlər birbaşa həmin xanada redaktə edilir. Sistem Number IME + Done istifadə olunur; Done yalnız klaviaturanı bağlayır, dərc etmir. Geri/Dəyiş də IME-ni bağlayır. Boş input placeholder0-dır, sample100 prefili və saxta qiymət yoxdur. Qiymət building.id üzrə saveable state-dir; route/account exact Ready sərhədi əvvəlkidir.

Maksimum və interval xətası qəbul edilmiş `building.maxPrice`-dan gəlir.19ASCII rəqəm limiti, safe `toLongOrNull`,1..maxPrice qəbul qaydası var;0,limitdən böyük və Long overflow dərc düyməsini deaktiv edir. Komissiya mövcud domain `MarketMutations.fee`-dir (ceil5%), net eyni qiymətdən hesablanır; UI qaydanı təkrarlamır. Xülasə bir sətirdədir; font2/uzun məbləğdə horizontal scroll tam rəqəmləri saxlayır. Yalnız bir əsas dərc əməliyyatı var. Disabled press feedback yoxdur;48dp touch minimumu və native semantics saxlanır. Bina/art/mətn/səthlər real UI-dir.

`InventoryCollectionChrome.kt` market detail axınında IME inset>0 olduqda dock-u gizlədir, onun clearance-ni çıxarır; mövcud root imePadding sistem IME sahəsini ayırır. Klaviatura bağlananda7slotdock bərpa olunur. Digər kök route-ların dock-u dəyişmir. Mərhələ strip-i font2-də üfüqi scroll edir, cari mərhələ görünən tərəfə gətirilir; mərhələ nömrəsinin dairəsi fontScale ilə böyüyür. Qiymət body-si zəruri halda şaquli scroll edir; əsas action onun xaricində görünən qalır. Normalda body intrinsic hündürlükdədir, düymə xülasəni izləyir.

## Authority və oxu sərhədi

Mutasiya `MarketMutations.list(ready,building,price)` → application owner/sole durable command journal ilə əvvəlki yoldur. UI revision/dedup, fresh Ready, UID/login epoch, building revision və server ownership yoxlamalarını çıxarmır. AppBackend `internal/wobbrik/market.go` read-only yoxlandı: ownBuilding/revision, creator/traded, maxPrice,5active limit və ceil5%fee var. Müqavilə/DTO/schema/SQL/index dəyişmir; backend kod/test/deploy tələb edilmir. Android focused domain test server yoxlamasının əvəzi deyil.

Qiymət səhifəsi0HTTP-dir; redaktə/Dəyiş/picker return əlavə oxu yaratmır. İlk picker mövcud bir bounded inventory sorğusudur, kart başına N+1 və bütün şəhər skanı əlavə edilmir. Qiymət səhifəsində art decode yoxdur. Yeni cache/framework/prewarm/polling yaradılmayıb; cari mounted picker səhifəsi saxlanır. Route yenidən mount/cache-miss/offline davranışı əvvəlki inventory request/error/retry-dir; persistent picker cache-hit/offline təqdimatı bu vahiddə əlavə edilməyib. Refresh/revision/account cancellation mexanizmi MarketScreen-in əvvəlki effect/current guard-ıdır. Yeni cache və optimallaşdırma iddiası yoxdur.

## Native yoxlama və məhdudiyyətlər

Sübut `market-new-listing-native/`: render harness, final test XML/log, contrast, comparison/responsive və raw renders.390×844/320×568font1/2,360×640font2+24/48simulated inset+uzun ad/Long.MAX_VALUE wallet. Fixture market/inventory JSON-u real GameplayHttpBackend parser, Ready owner və production route ilə işləyir; real hesab deyil. Valid/empty input,0,25>max24,9223372036854775808overflow,24→fee2/net22, Dəyiş return reuse və bir MARKET_LIST submission yoxlanır. Native5/5host və6/6MarketIntegrationTest PASS; assembleDebug/lintDebug nəticəsi final checks.log-da.

IME harness AndroidComposeView-a API34 platform inset inject edir, dock-un olmadığını və dərc düyməsinin görünməsini yoxlayır. Birbaşa decorView dispatch Compose-a inset vermədiyindən aralıq test uğursuz oldu; həqiqi listener view-a dispatch ilə düzəldildi. İlk inset capture-də native qatlar boş görünürdü; iki host draw/capture settle-dən sonra mətn/action göründü. Bu real IME, Gboard, animasiya/GPU/frame və ya API24runtime təsdiqi deyil; actual keyboard pixel-ləri host tərəfindən çəkilmir. Emulator açılmayıb.

`comparison.jpg`/`responsive.jpg` açılıb baxıldı. Normalda yığcam qiymət və birsətir xülasə/publish görünür. Kiçik/font2-də scroll zamanı üst identity/label qəsdən viewport-dan çıxır, yayınma kimi gizlədilmir; action görünür. Böyük rəqəmlərin hamısı bir dəfə ekrana sığmır; horizontal scroll/semantics saxlanır. Real bina geometry/art/ad/balans/maxPrice və sample rəqəmlər fərqlidir. Ortaq native back/tag/icon və gradient/parıltı PNG ilə piksel eyni deyil. Tam app redesign/mərhələ qəbulu iddia edilmir. TalkBack,real cihaz/API24,real keyboard,locale matrisi,production performance yoxlanmayıb.

Palette yeni rəng əlavə etmir; GameTokens.InventoryCollection istifadə olunur. Contrast5/5PASS,minimum7.41. Local diff review feature/data ayrımı, bounded lazy key-lər, importlar, Ready/session guard və authority-nin saxlanmasını yoxladı; `git diff --check`PASS. Host rəqəmləri ART/production latency deyil, sərt millisecond assertion yoxdur.

Son-source nəticə:5/5host,6/6domain,assembleDebug/lintDebugPASS; son measurement harness də5/5PASS. Bounded4building fixture: picker1HTTP/2019response-bodybyte, qiymət redaktəsi və warm return0HTTP/byte. İlk metadata host26–150ms,warm return22–45ms. Bunlar eyni synthetic fixture/cold-mounted picker müqayisəsidir; əvvəlki APK ilə optimizasiya benchmark deyil, persist cache/offline/large-account latency ölçməsi deyil. Mövcud bounded inventory DTO geometry-ni preview üçün daşıyır; yeni wire/cache payload əlavə edilmir. Backend sorğusu dəyişmədiyi üçün yeni SQL plan/scan benchmark yoxdur.

Təkrar (`android-billing/`):
```sh
./gradlew --offline -I ../docs/reviews/market-new-listing-native/render/render.init.gradle :app:testDebugUnitTest --tests 'com.aladdin.wobbrik.MarketNewListing*Test' :core:test --tests 'appbackend.wobbrik.game.MarketIntegrationTest' :app:assembleDebug :app:lintDebug -Dwobbrik.render.output=/private/tmp/wobbrik-listing-native
```
Fokuslu yoxlamalardır, CI deyil.

## Eyni təsdiqli PNG üzrə native düzəliş — 19 noyabr

İstifadəçi iki təsdiqli PNG-ni yenidən göstərib tətbiqi tamamlamağı tapşırdı. Eyni `feat/market-new-listing-native` working diff-də `MarketListingUi.kt` yeniləndi: bina kartının native şaquli ayırıcısı, yığcam yumru aktiv mərhələ xətti, enabled seçim/dərc düymələrinin qalın cyan konturu və token rəngli native halo, dərc üçün ağ konturlu Canvas tag silueti, action tipoqrafiyası və xülasədən əvvəl artıq boşluğun çıxarılması. Yeni rəng/asset/API/cache/domain qaydası yoxdur; əvvəlki authority və request sərhədləri dəyişmir. Bu düzəliş yeni performans optimizasiyası iddiası deyil.

Son renderlər `market-new-listing-native/parity/` daxilindədir. Normal/kiçik portret, font2, simulated inset/uzun balans və simulated IME renderləri açılıb yoxlandı. Font2 scroll sonuna gətirilmiş capture-də identity/qiymət sahəsinin üst hissəsi viewport-dan çıxır, summary üfüqi scroll ilə saxlanır; dərc görünür. Ortaq chrome/back/coin və native bina illüstrasiyası nümunə ilə piksel eyni deyil; sample balans/bina/qiymət köçürülmür. Real klaviatura pikselləri və cihaz runtime yoxlanmayıb; host inset navbarın gizlənməsi üçün sübutdur. Tam piksel pariteti və bütün app qəbulu deyil.

Son focused host XML: 5 test, 0 failure/error; MarketIntegrationTest: 6 test, 0 failure/error. Kontrast 5/5 PASS (minimum 7.41), diff-check PASS. Native compose dəyişiklikləri üçün ayrıca backend dəyişməsi tələb edilmir. Commit/merge/CI/push/deploy yoxdur.

Son-source fokuslu yoxlama: assembleDebug/lintDebug və yuxarıdakı host/domain testləri eyni Gradle çağırışında BUILD SUCCESSFUL (1m20s). CI deyil.

## Başlıq və mərhələ zolağının qalınlığı — istifadəçi düzəlişi

19 noyabr, eyni branch/working diff. İstifadəçi Yeni elan və Bina seç/Qiymət səthlərinin digər səhifələrdən qalın olduğunu bildirdi. `MarketListingHeading` daxilində iri CommerceTitle çıxarıldı: başlıq20sp kolleksiya heading tokeni,48dp back input və onun daxilində36dp vizual səth istifadə edir. Mərhələ səthi əvvəlki48dp əvəzinə ortaq `InventoryCollection.TabVisualHeight`36dp, label14sp, nömrə12sp/16dp badge və ortaq2dp indikator istifadə edir. Font2-də intrinsic artım və horizontal scroll saxlanır. Rəqəmdə inherited lineHeight/font padding-in yaratdığı kəsilmə ayrıca düzəldildi. Yeni token/rəng/API və backend dəyişməsi yoxdur; sorğu/cache/domain davranışı dəyişmir.

Son sübut qovluğu `market-new-listing-native/compact/` nəzərdə tutulur; yoxlamalar tamamlandıqdan sonra nəticə aşağıda qeyd olunur. Commit/merge/CI/deploy yoxdur.

Son-source:5/5 native host testi, assembleDebug/lintDebug PASS (BUILD SUCCESSFUL49s); kontrast3/3PASS, minimum7.41; diff-check PASS. Normal/kiçik/font2 və inset renderləri açılıb yoxlandı; mərhələ rəqəmi kəsilmir. Font2 scroll sonundakı qiymət sahəsinin üst hissəsinin viewport-dan çıxması və üfüqi xülasə əvvəlki davranışdır. Real cihaz/IME yoxlanmayıb.

## Bina seç kartları — 19 noyabr, istifadəçi düzəlişi

Eyni `feat/market-new-listing-native`, baza82e6ca0, working diff; əvvəlki owner diff saxlanır. İstifadəçi kartlarda qalan fərqləri təsdiqli proposal-v4 üzrə düzəltməyi tapşırdı. `MarketListingBuilding`: art/mətn nisbəti və16dp ayırıcı boşluqları, nazik120dp şaquli separator,4dp şaquli/12dp üfüqi kart padding-i; normal kart152dp. Başlıq extraBold20sp, mərtəbə bold16sp; Maksimum metadata və ağ məbləğ/native coin bir sətirdədir. Seç düyməsində32dp vizual pill/48dp input,16sp mətn,1dp cyan kontur və daha zəif halo istifadə olunur; publish variantı əvvəlki qalır. Kiçik portretdə mövcud360dp breakpoint-ə görə16sp başlıq,14sp metadata/16dp coin istifadə edilir. Uzun ad sözlər üzrə wrap, uzun məbləğ horizontal scroll saxlayır. MarketScreen-də aşağı seçim helper-i nümunədəki kimi mərkəzləşdirilib. Yeni token/rəng/raster/framework yoxdur.

`cards/` son17 raw renderi və5 test XML-i saxlayır. Normal390×844, kiçik320×568 və font2/uzun mətn+24/48simulated inset picker renderləri açılıb proposal-v4 ilə müqayisə edildi. Kiçik portretdə adi24məbləğ/coin sığır; Long.MAX məbləğ scroll edir. Font2-də əvvəlki şaquli kart adaptasiyası qalır. Picker screenshot-u ilk kartın art-ını gözləyir; kiçik screenshot-da ikinci kartın optional art loading nöqtəsi görünür, metadata/seçim bloklanmır. Bina görünüşü real hissə/geometry-dir; nümunənin dekorativ park artwork-u deyil və tam piksel pariteti iddia edilmir.

Son-source host5/5, assembleDebug/lintDebug PASS: BUILD SUCCESSFUL49s; kontrast3/3PASS(min7.41), diff-checkPASS. Eyni harness valid/invalid price, bir MARKET_LIST, əlavə reads olmadan Dəyiş return assert-lərini də saxlayır. Layout-only dəyişiklikdir, API/cache/domain/backend/sorğu hüdudları dəyişmir; yeni performance optimizasiyası iddiası yoxdur. Mövcud bounded fixture ölçməsi əvvəlki bölmədədir, production və böyük-hesab ölçməsi deyil. Real cihaz/IME/TalkBack/locale yoxlanmayıb, emulator açılmayıb; CI/commit/merge/push/deploy yoxdur.

Review source SHA256: MarketListingUi.kt `a6976d8e6a45ffbf18300e6abedd85b6a1fa59695648d0ea1687b6894f8fe19e`; MarketScreen.kt `80a1f33190edbe43198f07714809320974110a760121b08c47818be3580f1020`.

## Kartların ayrıca native nəticə review-u — 19 noyabr

İstifadəçi tapşırığı ilə current-task, PLAN §1.1 və yuxarıdakı kartlar bölməsi oxundu. `cards/preview.png`, `picker-small.png`, `picker-regularfont.png`, `picker-insets.png` açılıb təsdiqli `proposal-v4.png` ilə müqayisə edildi. İki source hash-i yuxarıdakı sübutla eynidir; yeni render/build/test işlədilmədi, mövcud host renderləri yoxlandı. App kodu dəyişdirilmədi.

- Normal görünüşdə kartın art/mətn bölgüsü, şaquli ayırıcı, ağ maksimum məbləğ + coin, yığcam cyan Seç pill-i və mərkəzli helper təsdiqli kompozisiyanı izləyir. Baxılan kartlarda mətn/düymə üst-üstə düşməsi görünmür.
- Qalan vizual fərq: real geometry ilə bina daha nazikdir və art sahəsində nümunənin enli dekorativ illüstrasiyası qədər yer tutmur. Kart səthi və düymə də nümunədəki daha parlaq gradient/halo ilə tam eyni deyil. Bunlar tam vizual paritetin qəbuluna imkan vermir; real binanı saxta nümunə binası ilə əvəz etmək həll sayılmır.
- Kiçik portretdə birinci kart oxunaqlıdır; ikinci kart viewport-dan qismən kənardadır və optional art loading vəziyyətindədir. Bu şəkil ikinci kartın yüklənmiş art görünüşünü təsdiqləmir. Font2-də şaquli kompozisiya və uzun adın sözlər üzrə wrap olması görünür; inset renderi scroll edilmiş mövqedir, bütün kartın eyni anda görünməsi sübutu deyil.
- Uzun maksimum məbləğ normal preview-da tam görünmür; source horizontalScroll saxlayır. Statik şəkil bütün rəqəmlərə toxunma/TalkBack ilə çatmağı ayrıca sübut etmir. 48dp minimum input source-da saxlanır; real cihaz semantikası yoxlanmayıb.

Bu review yeni bloklayıcı kart layout xətası aşkarlamadı; tam PNG pariteti və mərhələ qəbulu vermir. Əvvəlki kontrast/build/test nəticələri əvvəlki icranın sübutudur, bu review-da təkrar PASS kimi təqdim olunmur. CI/merge/commit/push/deploy yoxdur.

## Kart səthi və Seç işıqlanması — son düzəliş

19 noyabr, `feat/market-new-listing-native`, baza82e6ca0, əvvəlki owner diff saxlanır. `MarketListingUi.kt` kartına feature-local native şaquli + radial gradient, yuxarı-sol işıqlı nazik gradient konturu əlavə edildi. Mövcud Selected/DockBottom/Rim/Metadata tokenləri istifadə olunur; yeni rəng/ölçü tokeni və raster yoxdur. Seç pill-i 1.5dp cyan kontur, yumşaq ikiqat halo və daxili üst highlight alır; 32dp vizual/48dp minimum input, press/disabled semantics saxlanır. İlk renderdəki 2dp kontur qalın göründüyünə görə son renderdən əvvəl incəldildi. Publish görünüşü dəyişmir.

Real bina renderer-i, accepted geometry/parts və art miqyası dəyişmir: nümunədəki enli dekorativ binanı əldə etmək üçün real bina dartılmır və saxta park əlavə edilmir. Buna görə bina silueti üzrə fərq qalır; tam pixel parity iddiası yoxdur. API/domain/cache/request dəyişmir, backend dəyişməsi lazım deyil. Bounded request/byte sərhədi əvvəlki harness assert-ləri ilə saxlanır; yeni performans optimizasiyası/production ölçməsi iddiası yoxdur.

Son sübut `market-new-listing-native/cards-polish/`: 17 raw render, preview,5test XML və checks.txt. Son-source native host5/5, assembleDebug/lintDebug PASS (50s), diff-check PASS. Əvvəlki dərhal capture əvəzinə font1-də mövcud art-pixel wait istifadə olunur; kiçik renderdə ikinci art da görünür. İlkin bütün variantlarda art-wait cəhdi insetdə viewport-dan kənar art gözlədiyindən timeout verdi; font1-only gözləmə ilə düzəldildi və bütün5test yenidən keçdi. Font2/inset şəkilləri scroll edilmiş mövqedir; art-ın viewport-dan çıxması clipping xətası kimi təqdim olunmur.

Normal390×844,kiçik320×568,fontScale2 və360×640font2+simulated24/48inset/uzun ad renderləri açılıb proposal-v4 ilə müqayisə edildi. Kart gradient/konturu və Seç parıltısı yaxınlaşdırıldı; baxılan mətn/action-da üst-üstə düşmə görünmür. Uzun maksimum məbləğin horizontal scroll-u saxlanır. Token checker4/4PASS,min7.41; .24Rim-in ən parlaq Selected üzərində konservativ kompoziti üçün Metadata6.07:1,Text10.25:1. Mətnə təsir etməyən dekorativ rim üçün3:1 tələb tətbiq edilmir. Real cihaz/API24/IME/Gboard/TalkBack/locale/GPU yoxlanmayıb; emulator/CI/commit/merge/push/deploy yoxdur.

## İstifadəçi qəbulu və commit — 19 noyabr

İstifadəçi son kart nəticəsini “okeydir commit et” ilə qəbul etdi. Commit yalnız Yeni elan source/chrome IME hissəsi, dizayn istinadları və yoxlama sübutlarını əhatə edir; əvvəlki Uğurlar/Şəhər owner diff-i saxlanır. Son yoxlamalar yuxarıdadır; qəbuldan sonra app kodu dəyişməyib. Merge/CI/push/deploy tapşırılmayıb. Növbəti dizayn işi Profil, sonra ana səhifədir; yeni Profil dizaynı və implementasiyası hələ hazırlanmayıb/təsdiqlənməyib.
