# PLAN §1.1 — Bazar alış təsdiqi / native kompozisiya

Branch `feat/community-tabs-native-consistency`, HEAD `6c96e9c`. Production dəyişiklik yalnız `MarketScreen.kt`; bu vahiddən əvvəlki mənbə `/private/tmp/market-confirm-before.kt`. Owner dirty işi saxlanır, stage/commit/merge/push/deploy/CI yoxdur.

## Dəyişiklik və müqayisə

Authority `02-market.png` aşağı sağ alış təsdiqidir. Əvvəl ayrıca mərkəzi böyük preview, altında ad/mərtəbə və scroll altında qiymət/balans vardı. İndi native horizontal preview+ad/mərtəbə kartı, ayrıca Qiymət/Balans ikon+coin sətirləri və Cart ikonlu Al var. Mövcud GameTokens/MenuRelief, ControlRadius, real BuildingPreview və server snapshot saxlanır. Font2-də etiket/məbləğ şaquli, uzun ad və məbləğ horizontal scroll-dur. Footer body-dən ayrıdır; Al/Ləğv et minimum48dp qalır.

İlk render normal fontda balans panelinin aşağısını kəsirdi; yalnız bu confirmation üçün compactHeight mövcud SaleDialogHeight+Target×2 oldu (mövcud ekran həddi saxlanır). Digər dialoqların default ölçüsü dəyişmir. Eyni summary mövcud withdraw confirmation-da da istifadə olunur; bunun input/cancel regression-u commerce hostundadır.

Domain/DTO/API/schema/wallet/repository dəyişmir, AppBackend dəyişikliyi tələb olunmur. Real katalog generativ PNG binası ilə əvəz edilmir. Profil, Ərazi, İnventar, navbar/shared tokenlər dəyişmir. Press animasiyası ayrıca qalır.

## Yoxlamalar

Son production source ilə debug/mockDebug APK+lintDebug PASS1m7s. Native commerce5+shell4 ikinci tur PASS1m28s; üçüncü turda320/font2 məbləğ assertion-ı FAIL oldu: nested horizontal scroll performScrollTo xarici body-ni kifayət qədər sürüşdürmədi. Test indi body üzərində swipeUp ilə input-u yoxlayır; son native commerce5+shell4=9/9 PASS; failure/error/skipped0. Son5PNG `market-confirmation-native/` altında saxlanıb açılıb02-market ilə müqayisə edildi. Font2/small və inset capture-ları body swipe sonrası balans görünüşüdür; binanın üst hissəsi scroll-dan kənarda ola bilər. Scoped diff/callsite və git diff --check PASS. Normal390/844,small320/568 font1; font2 həm390,həm320;360/640 font2 simulated inset+uzun ad/balans. Son test balans etiketinə deyil, məbləğin özünə scroll edir; cancel sonrası submission0, root Ərazi absence, detail, new listing, own/withdraw, retry və shared shell saxlanır.

Kontrast GameTokens-dan kodla oxunaraq ui-craft ratio funksiyası ilə5/5 PASS: Text/PanelTop13.58; Muted/PanelTop8.59; Text/PanelBottom16.16; Muted/PanelBottom10.22; Text/Raised10.24. Checker CLI nested Kotlin tokenlərini avtomatik çıxarmadı; həmin cəhd PASS sayılmır. Palitra dəyişdirilməyib.

## Açıq fərqlər

Bu vahid tam PLAN§1.1/PNG qəbulu deyil. PNG-də bottom sheet, native implementasiyada mövcud centered Dialog hostu qalır; sheet yerləşməsi/backdrop/handle, title alignment və xarici panel materialı açıqdır. Real artwork fərqi saxlanır. Font2/small məbləğ və hüquqi mətn üçün body scroll lazımdır. Ayrıca uzun qiymət fixture-i yoxdur. Device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator açılmadı.
