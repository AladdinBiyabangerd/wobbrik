# Home — kiçik portret/font2 hero itkisi

17 noyabr 2026. Branch `feat/native-ui-parity`, baza `05070ff`, yoxlanmış working diff: MobileHomeScreen.kt və HomeProductionRenderTest.kt. Authority PLAN §1.1, unified-mobile-v3 README, home-approved-frameless.png və04-game-states panel24. Normal Şəhər review-u oxundu; şəhər işi təkrarlanmadı.

## Düzəliş və lokal review

Auditdəki konkret səbəb: böyük şriftdə hero yalnız qalan body hündürlüyü PanelWidth (360dp) həddini keçəndə composition-a daxil edilirdi. Bu şərt çıxarıldı; mövcud LargeFontHero (96dp) ilə hero scroll body-də saxlanır. Normal font1 layout-u, real snapshot/stat/XP, dock və lifecycle callback-ləri saxlanır. Yeni token, artwork, dizayn və press animasiyası yoxdur. Hero dekorativdir; testTag TalkBack mətninə çevrilmir.

Presentation şərti dəyişir; backend repository çağırışı, DTO, hesab/revision/dedup, wallet, ownership və schema dəyişmir. Buna görə AppBackend dəyişikliyi tələb olunmur. Production-a demo adapter əlavə edilməyib.

Diff üzrə review: minimum dəyişiklik, mövcud token/art təkrarı, import və scroll sərhədi yoxlandı; yeni code finding yoxdur. Əvvəlki iki baseline .dm və untracked audit bu vahidə daxil deyil. Commit/merge/push/CI yoxdur.

## Yoxlama və vizual nəticə

HomeProductionRenderTest8/8 PASS, failures/errors/skipped0. Font2 hero görünməsi/pozitiv hündürlük, mərkəzi Play görünmə/enabled/click, dock safe bottom və altı route-un48dp target/callback assertions keçir. Recovery/retry və uzun balans assertion-ları da keçir. Render init yalnız host QA üçündür.

Renderlər `/private/tmp/wobbrik-home-hero/`, log `/private/tmp/wobbrik-home-hero.log`. 320×568/font1 və font2,390×844/font1 və font2,360×640/font2+simulated24/48 inset və320/font2+Long.MAX_VALUE balans PNG-ləri açılıb təsdiqli Home və panel24 ilə müqayisə edildi. Hero font2-də başlıqla statistika arasında görünür; normal Home kompozisiyası saxlanır. Böyük şriftdə hero reference-dən xırdadır (mövcud96dp pilləsi); tam pixel-parity iddiası yoxdur. Uzun balansda stat/XP aşağı hissəsi scroll arxasında qalır.390/font2 capture-da mərkəzi Play artwork-ü görünmür, halbuki semantics/display/click assertion-ları keçir; bu capture/runtime fərqi açıqdır, vizual dock PASS iddiası deyil.320/font2 və inset capture-da Play görünür.

Kontrast ui-craft checker: Text/Surface13.00, Muted/PanelTop8.59, Background/PlayBottom7.66 PASS. Palette dəyişməyib. Real cihaz/account/API24/GPU/performance/TalkBack/IME/locale/tablet yoxlanmayıb; emulator açılmayıb. Tam app/mərhələ5 qəbulu deyil.

Son source üzrə render init olmadan `:app:assembleDebug :app:lintDebug --offline --console=plain` PASS, BUILD SUCCESSFUL49s; log `/private/tmp/wobbrik-home-hero-build.log`. `git diff --check` PASS. Son production edit-dən sonra yoxlanıb; yalnız review/handoff sonradan yazılıb.
