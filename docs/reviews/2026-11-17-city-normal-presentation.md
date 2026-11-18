# Normal Şəhər / Görünüş — native mobil kompozisiya

17 noyabr 2026. Branch `feat/native-ui-parity`, baza `4e78c68`. Authority: PLAN §1.1, unified-mobile-v3 README və01-main.png panel02; son7-slot/header qərarı saxlanır. Yalnız normal Şəhər/Görünüş vahididir, tam app pariteti və mərhələ qəbulu deyil.

## Dəyişiklik və review

Mobil başlıq contextual Geri/Şəhərim və bərabər Plan/Görünüş tabları ilə qurulur. Xəta vahidində mövcud native tab helper-i ortaq CityChrome-a keçirilib; error kartı/illüstrasiya/retry yenidən qurulmayıb. Tablar native MenuRelief/GameTokens, aktiv gradient və vector ikonlardır. Yeni rəng/ölçü tokeni və raster skin yoxdur.

Normal ölçüdə aşağı kart MenuRelief/Commerce səthidir; kamera illüstrasiyasının yerinə seçilmiş real binanın mövcud BuildingPreview komponenti, real mərtəbə/sakin göstəricisi və Bax var. İlkin seçim göstərilmədikdə Bax-ın əvvəldən açdığı son bina kartda göstərilir. Bax/Geri əvvəlki read-only fokus axınıdır. Native Canvas kamera və böyütmə idarələri48dp-dir: sıfırlama tam şəhər kamerasına qayıdır, böyütmə seçilmiş binanın mövcud fokusunu açır/bağlayır. Bunlar screenshot paylaşımı və ya sərbəst placement kamerası deyil.

Böyük şrift və qısa mobil hündürlükdə başlıq/səhnə/izah scroll olunur, kamera idarələri və əsas Bax/Geri sabitdir. İlk320/font1 renderində həddən artıq kiçik səhnə aşkarlandı; qısa viewport mövcud bounded144dp scene+scroll adaptasiyasına keçirildi. Font2 supporting mətn və başlıq scroll arxasında qala bilər; əsas əməliyyat görünür. Plan disabled,7-slot dock, portret kilidi və non-mobile yol saxlanır. Press animasiyası ayrıca tapşırıqdır.

Səhnə serverdən gələn real geometry/parts/terrain/decor və mövcud depth/life renderini saxlayır. PNG-dəki5 bina/fontan və nümunə adları əlavə edilmir. Real fixture-də10 mərtəbəli bir bina və uzunsov torpaq var; buna görə siluet/kompozisiya nümunə adadan fərqlidir. Tam pixel-parity iddiası yoxdur. Bina detalına ayrıca route açılması bu vahiddə dəyişmir; mövcud Bax fokus davranışıdır və PLAN §1.1 üzrə detal route inteqrasiyası açıq qalır.

AppBackend `docs/wobbrik-game-api.md` və mövcud CitySceneRepository müqaviləsi yoxlandı: dəyişiklik presentation/local camera state-dir; sorğu, DTO/domain, wallet, ownership, revision/dedup/cache, schema və mutation dəyişmir. Backend dəyişikliyinə ehtiyac yoxdur. Synthetic HTTP production repository axını testdə saxlanır, server inteqrasiyasının tam qəbulu deyil.

## Yoxlama

Son source üzrə5/5 host test PASS (failures/errors/skipped0), lintDebug PASS, BUILD SUCCESSFUL1m8s. Əvvəlki turdakı kiçik səhnə qüsurundan sonra uyğun yoxlamalar yenidən işlədildi. Yalnız sonradan EOF boş sətri təmizlənib; davranış dəyişməyib. Lokal diff/import/yerləşmə/domain sərhədi review və `git diff --check` PASS; açıq yeni code finding yoxdur.

390×844/font1,320×568/font1,390×844/font2,320×568/font2 və360×640/font2+simulated24/48dp inset+Long.MAX_VALUE balans native renderləri alındı. Son normal səhnələr və fokus nümunələri açılıb01-main panel02 ilə müqayisə edildi; QA kontakt vərəqi `/private/tmp/wobbrik-city-normal-contact.png`. Normal390-da bütün kompozisiya görünür; kiçik/font2-də scroll vəziyyətinə görə başlıq/tab/izah viewport-dan çıxa bilir, əsas əməliyyatlar sabitdir. Sıx inset-in ilkin viewport-unda səhnə scroll aşağısındadır; ayrıca scene capture-da görünür. Kamera ikonları native, real bina/torpaq silueti əvvəlki domain renderidir. Real device/GPU/API24/IME/TalkBack/bütün locale/performance/tablet və real hesab yoxlanmayıb.

Production shell testləri synthetic HTTP503→retry→population40→scene→camera focus/reset→Bax/Geri və mövcud dock/route axınını keçir. Bax minimum48dp assertion-u, Plan disabled və submission0 saxlanır. Kamera fokus semantikası və sıfırlamadan sonra tam şəhər rejimi assert edilir. Bu testlər real server/device nəticəsi deyil.

Log `/private/tmp/wobbrik-city-normal-reviewed.log`; renderlər `/private/tmp/wobbrik-city-normal-reviewed/`, `production-shell-city-{initial-,focused-,}<ölçü>.png`. Son5 test Gradle XML-də təsdiqlənib. Əmr (android-billing daxilində Android Studio JBR ilə):

```sh
./gradlew -I ../docs/reviews/mobile-sizing-visual/render/render.init.gradle :app:testDebugUnitTest --tests '*MobileShellRouteRenderTest.regular' --tests '*MobileShellRouteRenderTest.largeFont' --tests '*MobileShellRouteRenderTest.cityNormal*' :app:lintDebug -Dwobbrik.render.output=/private/tmp/wobbrik-city-normal-reviewed --offline --console=plain
```

ui-craft checker Text/PanelTop13.58, Muted/PanelTop8.59, Text/Surface13.00, Background/PlayBottom7.66, Background/PlayTop13.83 PASS. İlk çağırışda nested token adları tanınmadı; düz flat adlarla təkrar ölçüldü.

CI/emulator/merge/push/deploy yoxdur. Əvvəlki iki `.dm` və untracked audit faylı commit-dən kənarda qalır.
