# PLAN §1.1 — İnventar bina kartlarının kompozisiyası

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`. Mövcud owner dirty işi qorunur; stage/commit/merge/push/deploy/CI yoxdur. Bu tur production dəyişiklik yalnız `MobileInventoryPresentation.kt` daxilində `MobileOwnedBuildingCard`-dadır. Scoped əvvəlki mənbə `/private/tmp/wobbrik-inventory-before.kt` ilə müqayisə edildi; bütün working tree diff-i bu turun işi sayılmır.

## Dəyişiklik və lokal review

`01-main.png` aşağı soldakı İnventar kompozisiyasına əsasən normal font1 bina preview sahəsi mövcud `MobileUiSizing.cardArt` pilləsinə keçir: normalda96dp, compact-da72dp (əvvəl hər ikisi120dp). Böyük şriftdə48dp saxlanır. Eyni nisbətlə çəkilən real bina və accepted geometry dəyişmir; şəklin kəsilməsi/stretch və generativ kataloq əvəzi yoxdur. Kartın kənarı mövcud RimLight→Rim gradienti ilə vurğulanır. Başlıq fontu mövcud Body tokeni ilə açıq göstərilir; ad və mərtəbə sətirlərinin horizontal scroll-u, native Surface klik semantikası və grid scroll saxlanır.

Profilə və ortaq MenuRelief-ə toxunulmadı. İstifadəçi əvvəlki Profil düzəlişlərini qəbul edib, başqa Profil dəyişikliklərini dayandırdı. Profil source SHA256 bu İnventar vahidinin əvvəlində və sonunda `4c8c8f03406ca3eb1ab5bd7426db5e3a8a0cbe8c7c9f78e461ad089ce3973f8d`-dir. Ərazi qaytarılmır; kök title/back və7-slot dock qərarı qüvvədədir. Dekor/Bazar və detail layout-u bu dar vahiddə dəyişmir.

Domain/repository/session/API/DTO/schema/wallet dəyişmir; AppBackend işi tələb olunmur. Yeni token/dependency/asset yoxdur. Press animasiyası ayrıca tapşırıqdır.

## Yoxlamalar

Kontrast6/6 PASS, minimum6.48:1 (Muted/Raised); Text və Muted bütün Commerce fill stop-larında yoxlandı. `git diff --check` PASS. İlkin relative-path edit mənbəyə yazmadan dayandı; test icrası sayılmır.

Son production source ilə commerce5+shell4=9/9 PASS1m28s, failure/error/skipped0. Normal390/844 və kiçik320/568 font1, hər iki ölçüdə font2,360/640 font2 simulated24/48dp inset+uzun ad/balans renderləri açılıb01-main ilə müqayisə edildi. Native PNG-lər `inventory-card-composition/` altındadır; bütün route renderləri `/private/tmp/wobbrik-inventory-composition`. Kiçik ekranda ikinci kart sətrinin aşağı hissəsi scroll-dadır; font2-də bir sütun və uzun mətn horizontal scroll qalır. Bu real device inset sübutu deyil. Testlər real repository/Ready yolu ilə detail/görünüş route-u, error/retry, disabled placement, Ərazi absence və iqtisadi submission0 yoxlamalarını saxlayır. Production host fixture-i sintetikdir.

Son normal invocation (render init olmadan) `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS50s. Son source-dan sonra production dəyişiklik yoxdur; yalnız review/handoff yazıldı. Index boşdur; commit yoxdur.

## Sərhəd

Bu bütün PLAN§1.1 qəbulu və tam piksel pariteti deyil. Real bina artwork-ü reference-dəki generativ nümunələrlə eyni deyil və qorunur. Dekor/Bazarın qalan vizual fərqləri ayrıca qalır. Runtime/account/device/TalkBack/real IME/inset/API24/GPU/performance/locale/tablet yoxlanmır; emulator açılmır.
