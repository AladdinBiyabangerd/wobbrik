# Cari tapşırıq — Home header və Play yoxlaması

## Completed

PLAN §1.1 üzrə növbəti audit qalığı həll edildi: kiçik portret/font2-də Home hero-nu gizlədən body>360dp şərti çıxarıldı. Mövcud96dp hero scroll body-də saxlanır. Əlavə istifadəçi tapşırığı ilə Home logo/balans header-i ortaq shell ilə eyni16/8dp padding-ə gətirildi; əlavə24dp üst fərq çıxarıldı. Play üçün production bug təkrar yaranmadı; host capture-a saxlanmış PNG piksel guard-ı əlavə edildi. Şəhər vahidləri yenidən qurulmadı. Tam app/mərhələ5 qəbulu deyil.

## Current state

Branch `feat/native-ui-parity`, baza `05070ff`; İstifadəçi bu tamamlanmış Home vahidinin commit-ini təsdiqləyib; source/test/iki Home review/handoff bu vahidə daxildir. Əvvəlki iki baselineProfiles/{0,1}/app-release.dm və untracked current-screen-parity-audit.md toxunulmadan qalır. Merge/push/CI yoxdur; AppBackend dəyişməyib.

## Decisions

Authority PLAN §1.1 və unified-mobile-v3 README/Home/panel24. Mövcud token/art/native layout istifadə olunur; normal font1 yolu,7-slot dock, snapshot/XP/lifecycle və non-mobile yol saxlanır. Presentation şərti backend/API/domain/schema/wallet müqaviləsinə toxunmadığı üçün backend dəyişikliyi tələb olunmur. Press animasiyası/placement/Ərazi açılmır.

## Remaining work

İnventar/Görünüş səthləri və Profil/avatar kompozisiyası auditdə qalır; Şəhər Bax→bina detalı route inteqrasiyası ayrıca açıqdır. Font2 hero96dp olduğundan reference-dən xırdadır; uzun balansda XP scroll arxasındadır.Əvvəlki390/font2 boş Play capture-ı araşdırıldı: son PNG-lərdə dairə/üçbucaq görünür və piksel guard keçir; cari source-da production itkisi təkrar yaranmadı. Host/layer səbəbi ehtimaldır, real cihaz/GPU səbəbi sübut edilməyib. Real cihaz/account/API24/GPU/performance/TalkBack/IME/locale/tablet yoxlanmayıb. Tam app pariteti və mərhələ5 qəbulu açıqdır.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/README.md`, Home və04-game-states.png
- `docs/reviews/2026-11-17-home-small-font2-hero.md`
- `docs/reviews/2026-11-17-home-header-play.md`
- `docs/reviews/2026-11-17-current-screen-parity-audit.md`
- `docs/reviews/2026-11-17-city-normal-presentation.md`
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/MobileHomeScreen.kt`
- `docs/reviews/mobile-sizing-visual/render/HomeProductionRenderTest.kt`

## Checks performed

Son Home host12/12 PASS, failures/errors/skipped0.320/390 font1/font2,360/font2+24/48 inset və uzun balans PNG-ləri; header Home/shared-shell cütləri açılıb baxıldı.4header parity halında logo/balans bounds eynidir. Saxlanmış PNG Play pixel guard bütün capture-larda birinci cəhddə keçir;390/font2 ilkin/son və ayrıca icon crop açılıb təsdiqləndi. Play/dock/route48dp/callback/recovery/retry assertions keçir. Kontrast minimum7.66:1 PASS. Son production üzrə normal assembleDebug/lintDebug PASS1m1s. Diff review/diffcheck PASS. Log/render `/private/tmp/wobbrik-home-header-play-encoded*`; build log `/private/tmp/wobbrik-home-header-play-build.log`. CI/emulator yoxdur.

## Exact next step

Bu dar Home vahidi tamamlanıb və istifadəçinin göstərişi ilə commit edilir; əvvəlki .dm və ümumi audit faylı daxil edilmir. Context böyüdüyü üçün növbəti böyük vahidi yeni chatdə davam etdirmək tövsiyə olunur. Növbəti istifadəçi tapşırığında PLAN §1.1/audit üzrə bir qalıq seç; normal Şəhər/error işini təkrarlama. Merge/push/CI/deploy ayrıca göstəriş tələb edir.
