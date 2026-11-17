# PLAN §1.1 — Bazar / Dekor alış kartı

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`. Bu turun production dəyişikliyi yalnız `MobileShopPresentation.kt`; scoped baza `/private/tmp/wobbrik-shop-composition-before.kt` ilə müqayisə edildi. Owner dirty işi saxlanır. Profil, İnventar, shared token/chrome, asset və legacy/tablet yolu dəyişdirilmədi. Ərazi qaytarılmadı. Stage/commit/merge/push/deploy/CI yoxdur.

## Kompozisiya

`02-market.png` sağ yuxarı Dekor kartı əsasında font1 qiymət solda və Al sağda eyni sətirdədir. Düymə mövcud Target+Inset72dp en, minimum Target48dp hündürlük, compact native cart/Caption istifadə edir. Qiymət qalan eni alır və lazım olduqda horizontal scroll edir. Nüsxə sayı real oxu nəticəsi olaraq başlıqla qiymət/action sətri arasında saxlanır; nüsxəyə sahib olmaq təkrar copy alışını bağlamır. Font2 bir sütunlu kartda qiymət və action alt-alta qalır; aşağı content üçün grid scroll saxlanır. Başlıq explicit Body/Bold, softWrap=false və horizontal scroll-dur.

Mövcud real sprite mapping və source pixel limit dəyişmir. PNG-dəki nümunə fəvvarə/gül yeni kataloq kimi əlavə edilmir; sahiblik statusu nümunədən domain qaydası kimi götürülmür. Seçim və Al əvvəlki real alış təsdiqi/CityPurchases eligibility axınına gedir. API/DTO/schema/domain/wallet/repository dəyişmədiyinə görə AppBackend dəyişikliyi tələb olunmur. Press animasiyası ayrıca qalır.

## Yoxlama

Native commerce5+shell4=9/9 PASS1m25s; failure/error/skipped0. Mövcud production read/Ready route hostu synthetic HTTP fixture-dir; selection/cancel/retry, Ərazi absence, disabled placement və iqtisadi submission0 regression-ları saxlanır. Host source dəyişdirilmədi. Beş final PNG `market-decor-card-composition/` altındadır:390×844 və320×568 font1/font2,360×640 font2 simulated24/48dp inset+uzun balans. Hamısı açılıb02-market və əvvəlki native renderlə müqayisə edildi. Font1-də qiymət və action yanaşı sığır; font2-də aşağı action/content scroll tələb edir. Hostda uzun bina adı var, ayrıca uzun dekor adı/uzun qiymət fixture-i yoxdur; bu hallar sertifikatlaşdırılmır.

Kontrast5/5 PASSminimum6.48:1: Text/Commerce gradient stop-ları, Muted/Raised və Background/Accent. Scoped diff review-da yeni funksional problem tapılmadı. İlkin relative-path cəhdi source yazmadan dayandı; sandbox Gradle lock və ilk escalation auto-review timeout test nəticəsi deyil. İcazəli təkrar native yoxlama keçdi.

Normal render-init-siz `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS (müddət final log-dadır). `git diff --check` PASS; index boşdur. Bu bütün PLAN§1.1 və ya Bazar qəbulu deyil. Card radius/relyef, exact artwork və digər ekran pariteti, ölçülmüş axıcılıq açıqdır. Real device/account/TalkBack/IME/inset/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator açılmayıb.
