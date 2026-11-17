# PLAN §1.1 — Bazar/Binalar relyef və işıq

Branch `feat/community-tabs-native-consistency`, HEAD `6c96e9c`. Production edit yalnız `MarketScreen.kt`; scoped əvvəlki mənbə `/private/tmp/market-relief-before.kt`. Owner dirty işi saxlanır; stage/commit/merge/push/deploy/CI yoxdur.

## Düzəliş və review

`02-market.png` yuxarı sol alış kartlarında incə mavi kənar və yuxarı soldan işıq var. Əvvəlki native Commerce4dp daxili kənarı və Raised üst zolağı həddən artıq qalın/açıq görünürdü. Yalnız alış kartları üçün feature-local `MarketBuildingRelief`: mövcud PanelTop→PanelBottom gradienti, Rim-in mövcud1−DimOpacity alfa dəyəri ilə radial işıq, Hairline Rim→PanelTop kənar və Quarter kölgə. Shared MenuRelief/token/palitra dəyişmir. Normal grid və font2 alış sütunu eyni səth və mövcud16dp künc alır. Layout, mətn, preview ölçüləri, qiymət/Bax/confirmation və real kataloq/accepted geometry saxlanır. Elanlarım/seçim kartları əvvəlki Panel yolundadır; legacy/tablet yolu dəyişmir.

Profil, İnventar, Dekor, navbar, assetlər dəyişmir. Ərazi qaytarılmır. Domain/API/schema/wallet dəyişikliyi yoxdur, AppBackend işi tələb olunmur. Press animasiyası ayrıca qalır.

## Yoxlamalar

Native commerce5+shell4=9/9 PASS1m15s; failure/error/skipped0. Beş PNG `market-building-relief/` altında açılıb02-market və əvvəlki renderlə müqayisə edildi:390/844 və320/568 font1/font2,360/640 font2 simulated inset+uzun ad/balans. Kiçik font2/inset capture action-a scroll edilmiş vəziyyətdir, art/title viewport-un üstündə qala bilir. Sintetik HTTP fixture real reads/Ready/route üzərindən keçir; Ərazi absence, detail, confirmation cancel, error/retry və submission0 yoxlanır.

Token kontrast4/4 PASSminimum8.59; radial overlay kompozitinin101×101 gradient/alfa nümunəsində Text minimum10.38, Muted minimum6.56:1. Rənglər koddan oxunub ui-craft contrast.py funksiyaları ilə hesablandı. Scope diff/callsite review və diffcheckPASS. Kontakt-sheet yardımçı cəhdi PIL olmadığından alınmadı; beş orijinal PNG ayrıca açılıb baxıldı.

Render init olmadan normal `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS24s. Index boşdur; son source-dan sonra yalnız review/handoff yazıldı.

## Məhdudiyyətlər

Bu dar material düzəlişidir, tam Bazar/PLAN§1.1 qəbulu və exact pixel parity deyil. Artwork real kataloqdur, PNG-nin generativ binası ilə əvəz edilmir. Ayrıca uzun qiymət fixture-i, runtime/account/device/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmadı. Emulator açılmadı.
