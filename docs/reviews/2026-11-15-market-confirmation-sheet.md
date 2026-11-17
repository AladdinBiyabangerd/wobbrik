# PLAN §1.1 — alış təsdiqinin alt paneli və Yeni elan xülasəsi

Branch `feat/community-tabs-native-consistency`, HEAD `6c96e9c`; commit/stage/merge/push/deploy/CI yoxdur. Production scope yalnız `MarketScreen.kt`; əvvəlki mənbə `/private/tmp/market-sheet-before.kt`. Owner dirty işi saxlanır. Profilə toxunulmadı, Ərazi qaytarılmadı.

## Dəyişiklik

`02-market.png` aşağı sağ əsasında yalnız mobil alış təsdiqi aşağıya bağlanır. Native Dialog window BOTTOM gravity, mövcud DimOpacity ilə platform backdrop, dekorativ tutacaq, mərkəzi başlıq, RoundedCornerShape/CardRadius və Raised→PanelTop→PanelBottom gradient/Hairline rim var. Navbarın mövcud bir/iki sıra hündürlüyü və shell boşluğu nəzərə alınır; body hündürlüyü qalan portret sahəsinə məhdudlaşır. Al/Ləğv et sabit və minimum48dp, body scroll qalır. Back/outside dismissal mövcud Dialog müqaviləsidir; tutacaq ayrıca drag əməliyyatı deyil. Default filter/new-listing/decor/withdraw hostu dəyişmir.

Sonra Yeni elan daxilində mövcud real preview adı və mərtəbəsi olan üfüqi native xülasəyə keçirildi. Confirmation və forma eyni feature-local `MarketBuildingSummary` istifadə edir; təkrar domain qaydası yoxdur. Qiymət, komissiya, maksimum5elan, list/buy/withdraw mutasiyaları, hesab/revision və API/DTO/schema/wallet dəyişmir. AppBackend dəyişikliyi tələb olunmur. Yeni palitra/token/asset/dependency əlavə edilməyib.

## Yoxlama və məhdudiyyət

Kontrast ui-craft checker-in parse_colour/ratio funksiyası və GameTokens rəngləri ilə6/6PASS: Text/Raised10.24; Muted/Raised6.48; Text/PanelTop13.58; Muted/PanelTop8.59; Text/PanelBottom16.16; Muted/PanelBottom10.22.

Tam PNG/PLAN§1.1 pariteti deyil. Host PNG dialog window-nu ayrıca çəkir; sistem backdrop-un bütün ekranla birgə kompozisiyası şəkildən təsdiqlənmir, gravity/y/dimAmount native window atributları ilə yoxlanır. Native modal dim navbarı da qaraldır. Əsas PNG-dəki artwork əvəzinə real kataloq saxlanır. Font2-də body scroll və uzun məbləğ/ad horizontal scroll qalır. Yeni elan hələ centered dialog-dur; PNG-nin tam səhifə forması, qiymət/komissiya/info panelləri və Mənim elanlarım üzrə konkret fərqlər açıqdır. Device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb; emulator açılmayıb.

Son production source ilə native commerce5+shell4=9/9PASS1m25s, failure/error/skipped0. Son test-only capture əlavəsindən sonra commerce5/5PASS55s, failure/error/skipped0; shell production mənbəyi dəyişməyib. Buy window gravity/y/dimAmount explicit assertion, Al/Ləğv et48dp/display, cancel sonrası submission0, detail/new-listing/withdraw/retry və Ərazi absence işləyir. On PNG `market-confirmation-sheet/` altında saxlanıb açılıb02-market ilə müqayisə edildi:390/844,320/568 font1/font2 və360/640 font2 simulated inset+uzun ad/balans. Yeni elan summary capture mərtəbə mətninə scroll edilmişdir; normal/small capture-larda kartın yuxarısı viewport-dan kənardadır,390/font2-də tam xülasə görünür. Body scroll-u işləyir; bu capture-lar ilkin scroll state/piksel pariteti sübutu deyil. Alış təsdiqi font2 məbləğ capture-ları body swipe sonrası ayrıca tmp qovluğundadır. İlk sandbox Gradle lock cəhdi test nəticəsi deyil; icazəli rerun-lar keçdi. Mövcud nullable String host warning və native ripple warning-ləri qalır. Scoped diff/callsite və git diff --checkPASS.

Son production normal debug/mockDebug APK+lintDebug PASS25s. CI deyil. Son source dəyişməsindən sonra normal build/lint yenilənib.
