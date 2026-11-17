# PLAN §1.1 — Dekor status kompozisiyası

Branch `feat/community-tabs-native-consistency`, HEAD `6c96e9c`. Bu turun production diff-i yalnız `OwnedCityItemsScreen.kt`; əvvəlki mənbə `/private/tmp/wobbrik-decor-status-before.kt` ilə scoped review edildi. Owner dirty işi saxlanır; Profil/shared chrome/token/asset/bina kartı dəyişmir. Ərazi qaytarılmır. Stage/commit/merge/push/deploy/CI yoxdur.

`05-owned-and-listings.png` sol İnventar/Dekor əsasında Şəhərdə statusu mətn sütununun tam enini tutan native read-only capsule olur. Mövcud Target48dp minimum hündürlük, Target radius və Body bold; content/font böyüdükdə hündürlük artır. Native Track gradienti və Accent saxlanır. Statusda onClick, düymə rolu və feedback yoxdur. Disabled Yerləşdir eyni eni/minimum hündürlüyü saxlayır; placement capability açılmır. Saxlanılır sətri qalan eni ölçən horizontal scroll və mövcud Body/Medium ilə göstərilir; söz ortasında qırılmır. Əlavə icon/asset/yeni palette yoxdur.

Domain/session/repository/API/DTO/schema/wallet dəyişmir; AppBackend işi tələb olunmur. Server mövcud copy.position authority-si olaraq qalır. Backend testləri bu layout vahidində təkrarlanmadı.

## Yoxlamalar

Son native commerce5+shell4=9/9 PASS1m31s, failure/error/skipped0. Mövcud real repository/Ready route hostunda retry/error, disabled placement, detail/görünüş, Ərazi absence və iqtisadi submission0 yoxlamaları qalır; host fixture sintetikdir. Test source dəyişmədi.

390/844 və320/568 font1/font2,360/640 font2 simulated24/48dp inset+uzun balans renderləri açılıb əvvəlki render və05-owned ilə müqayisə edildi. Beş native PNG `inventory-decor-status/` altında. Font2-də content scroll-dadır, bazara keçid pinned/horizontal scroll-dur. Inset halında statusun aşağı hissəsinə çatmaq üçün scroll lazımdır; bu scroll saxlanır. Uzun dekor adına ayrıca fixture/assertion yoxdur. Source artwork/reference fərqləri qalır; exact pixel parity deyil.

Kontrast6/6 PASSminimum6.48:1; Accent/Track gradient stop-ları, Text/Commerce stop-ları və Muted/Raised. Scoped review-da yeni funksional problem tapılmadı. `git diff --check` PASS.

Son normal invocation (render init olmadan) `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS45s. Son production dəyişiklikdən sonra icra edildi. Index boşdur; commit yoxdur.

## Sərhəd

Bütün PLAN§1.1/İnventar qəbulu deyil. Sprite/reference və digər material/layout fərqləri, ölçülmüş axıcılıq açıqdır. Real hesab/device/TalkBack/IME/inset/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator açılmayıb. Press animasiyası ayrıca tapşırıqdır.
