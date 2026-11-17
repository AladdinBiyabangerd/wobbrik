# PLAN §1.1 — İnventar / Dekor kart kompozisiyası

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`. Production dəyişiklik yalnız `OwnedCityItemsScreen.kt` daxilindədir; scoped diff `/private/tmp/wobbrik-owned-before.kt` ilə yoxlandı. Əvvəlki dirty owner işi qorunur; stage/commit/merge/push/deploy/CI yoxdur. Profilə, bina kartlarına, ortaq chrome/tokenlərə toxunulmadı. Ərazi qaytarılmır.

## Dəyişiklik

Authority `05-owned-and-listings.png` sol İnventar/Dekor; köhnə title/avatar/dock və Ərazi hissələri son qərarlarla əvəz edilir. Dekor kartının radiusu mövcud ControlRadius16dp olur. Font1 preview konteyneri normal120dp, compact96dp; minimum kart hündürlüyü normal144dp, compact120dp-dir. Hündürlük sərt kilid deyil: content böyüyəndə kart artır. Şəhərdə/saxlanılır kartlarının ölçü ritmi yaxınlaşır. Font2 əvvəlki72dp art + şaquli content/scroll davranışını saxlayır. Ad sətiri söz ortasında qırılmadan horizontal scroll edir; Saxlanılır mövcud Body tokeni ilə göstərilir.

Real decoration sprite/crop mapping saxlanır; PurchaseArt source pixel limitinə görə bitmap böyüdülmür. Buna görə daha böyük konteyner real illüstrasiyanı həmişə böyütmür. Reference artwork-ü ilə exact paritet yoxdur. Generativ yeni kataloq, fontan və yeni asset əlavə edilmədi. Status read-only, Yerləşdir disabled, bazara keçid və retry/page çağırışları saxlanır. Press animasiyası ayrıca tapşırıqdır.

Domain/repository/session/API/DTO/schema/wallet dəyişmir; mövcud server copy.position oxusu istifadə olunur. Bu sırf layout vahidi AppBackend dəyişikliyi tələb etmir; backend testləri təkrarlanmadı.

## Yoxlamalar

Native commerce5 + shell4 =9/9 PASS, failure/error/skipped0,1m27s. Host fixture sintetikdir, production repository/Ready route-ları çağırır; error/retry, disabled placement, detail/görünüş keçidi, Ərazi absence və iqtisadi submission0 assertion-ları saxlanır. Test hostu dəyişdirilmədi.

390×844 və320×568 font1/font2,360×640 font2 simulated24/48dp inset+uzun balans/ad fixture-i. Beş `inventory-decor-composition/owned-decor-*.png` açılıb reference ilə müqayisə edildi. Uzun fixture adı binaya aiddir; uzun dekor adı üçün ayrıca render/assertion əlavə edilməyib. Font2-də kartlar scroll-dadır, əsas bazara keçid pinned və etiketi horizontal scroll-dur. Kiçik font1 ekranda aşağı qeyd scroll-da qala bilər. Bunlar real device inset və TalkBack yoxlaması deyil.

Kontrast7/7 PASSmin6.48:1; Text/Muted Commerce gradient stop-ları və Accent/PanelBottom. `git diff --check` PASS. İlk relative-path edit yazmadan dayandı və sandbox Gradle lock çıxışı test sayılmadı; son lokal render icrası keçdi.

Son normal invocation (render init olmadan) `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS49s. Son production dəyişiklikdən sonra icra edilib. Index boşdur; commit yoxdur.

## Açıq sərhədlər

Bütün PLAN§1.1 və bütün İnventar qəbulu deyil. Sprite/reference fərqləri, status/action relyefi və digər ekranların pariteti qalır. Real hesab/device/IME/inset/TalkBack/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator açılmayıb. Profilə əlavə iş yoxdur; Ərazi üçün yeni yer uydurulmur.
