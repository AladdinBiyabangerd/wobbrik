# PLAN §1.1 — Dekor kartının künc forması

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`. Bu turun scoped production əlavəsi `CityPurchaseChrome.kt` və `MobileShopPresentation.kt`; əvvəlki mənbələr `/private/tmp/shop-radius-before-*.kt` ilə müqayisə edildi. Owner dirty işi qorunur; stage/commit/merge/push/deploy/CI yoxdur.

`02-market.png` Dekor alış kartındakı yığcam künc formasına yaxınlaşmaq üçün yalnız mobil Dekor kartı mövcud `ControlRadius16dp` istifadə edir. `PurchaseCard` shape parametrinin default-u əvvəlki `CardRadius24dp` qalır; digər çağırışlar/legacy/tablet yolu dəyişmir. Surface clipping, native material/kənar və selected vurğusu eyni shape alır. Yeni token/rəng/raster/dependency yoxdur. Əvvəlki qiymət/action sətri, font2 scroll, real artwork/nüsxə sayı/alış təsdiqi saxlanır. Relyef gradienti ayrıca dəyişdirilmədi; tam material pariteti iddiası yoxdur.

Profil/İnventar/shared chrome dəyişmir. Ərazi qaytarılmır. Domain/repository/API/DTO/schema/wallet dəyişmədiyinə görə AppBackend işi tələb olunmur. Press animasiyası ayrıca qalır.

## Yoxlama

Beş final native PNG `market-decor-card-radius/` altında:390×844 və320×568 font1/font2,360×640 font2 simulated24/48dp inset+uzun balans. Hamısı açılıb02-market və əvvəlki renderlə müqayisə edildi. Normal şriftdə qiymət/Al yanaşıdır, font2 aşağı content grid scroll-dadır. Kontrast5/5 PASSminimum6.48:1; palette dəyişmir. Callsite/scoped diff review-da yeni funksional problem tapılmadı. Son native commerce5+shell4=9/9 PASS1m50s, failure/error/skipped0. Render init olmadan normal `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS57s. `git diff --check` PASS; index boşdur.

Bütün Bazar/PLAN§1.1 qəbulu deyil. Exact sprite/reference, relyef/işıqlanma və digər ekran pariteti, ölçülmüş axıcılıq açıqdır. Ayrıca uzun dekor adı/qiymət fixture-i və real device/account/TalkBack/IME/inset/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator yoxdur.
