# PLAN §1.1 — Bazar/Binalar alış kartları

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`. Yoxlanan mənbələr `MarketScreen.kt`, `MobileInventoryPresentation.kt`; bu turun production dəyişikliyi yalnız birinci fayldadır. Scoped əvvəlki mənbə `/private/tmp/market-building-before.kt`-dir. Owner dirty işi qorunur; stage/commit/merge/push/deploy/CI yoxdur.

## Tapıntı və düzəliş

`02-market.png` yuxarı sol alış grid-i ilə müqayisədə kartlar ümumi Panel materialı və24dp künc istifadə edirdi. Yalnız mobil alış grid-i mövcud Commerce materialı və ControlRadius16dp alır; şaquli aralıq8→4dp olur. Preview normal96/compact72dp, qiymət+Bax sətri və minimum48dp action saxlanır. Mobil bina kartının adı explicit Body/Bold və horizontal scroll, mərtəbə də söz qırılmadan horizontal scroll alır. Font2 grid deyil: əvvəlki bir sütun,48dp art, şaquli qiymət/action və list scroll qalır. Legacy/tablet yolu dəyişmir.

İnventarın mövcud96/72/48dp art,16dp künc və Commerce materialı artıq uyğundur; `MobileInventoryPresentation.kt` dəyişdirilmədi. Profilə, Dekor kartlarına, tokenlərə, shared chrome-a və assetlərə toxunulmadı. Ərazi qaytarılmadı. Real kataloq/accepted geometry/eligibility/detail/confirmation axını saxlanır. Domain/repository/API/schema/wallet dəyişmədiyinə görə AppBackend işi tələb olunmur. Press animasiyası ayrıca qalır.

## Yoxlamalar

Native commerce5+shell4=9/9 PASS1m47s, failure/error/skipped0. Beş PNG `market-building-card-composition/` altında açılıb02-market və əvvəlki material renderi ilə müqayisə edildi:390×844 və320×568 font1/font2,360×640 font2 simulated24/48dp inset+uzun bina adı/balans. Host alış action-ına scroll etdiyindən kiçik font2/inset capture-da art və ya başlıq viewport-un üstündədir; bu tam kartın eyni anda görünməsi sübutu deyil. Uzun ad mövcud synthetic fixture-dən gəlir; ayrıca uzun qiymət fixture-i yoxdur. Native host real reads/Ready/route istifadə edir, HTTP cavabları sintetikdir; iqtisadi submission0, confirmation cancel, Ərazi absence, detail/navigation/error/retry yoxlamaları keçdi.

Kontrast6/6 PASSminimum6.48:1 (Text/Muted, Commerce gradientinin bütün3stop-u). `git diff --check` PASS. İlk wrapper yolu və sandbox Gradle lock cəhdləri test sayılmadı; icazəli rerun keçdi.

Render init olmadan normal `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` PASS37s. Son mənbədən sonra yalnız review/handoff yazıldı; index boşdur.

## Məhdudiyyətlər

Bütün Bazar və PLAN§1.1 qəbulu deyil. Exact artwork, relyef/işıqlanma və ümumi ekran ölçü ritmi hələ tam PNG pariteti deyil; real kataloq generativ nümunə bina ilə əvəz edilmir. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmadı; emulator açılmadı.
