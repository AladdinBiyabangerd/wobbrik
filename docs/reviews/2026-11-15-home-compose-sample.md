# Home — native Compose nümunəsi, 15 noyabr 2026

Branch: `feat/home-approved-parity`, baza `main@6c96e9c`. Əvvəlki CityScreen/harness/handoff, baselineProfiles və account-compact owner işi qorunur. Commit/merge/push/CI yoxdur.

## Scope və son qərar

İstifadəçi sadə UI-ni bitmap skin ilə qurmağı rədd etdi və əvvəl bir Compose nümunəsi göstərməyi istədi. Authority dəyişmir: `docs/design/unified-mobile-v3/home-approved-final.png`; yeganə plan `docs/PLAN.md` §1.1. Bu yalnız Home nümunəsidir; bütün mobil UI/mərhələ qəbulu deyil.

- `MenuRelief.kt`: panel/kapsul/stat/track/navbar native gradient, border, shadow və selected səthidir; UI bitmap skin yoxdur. İlk generativ clean skin cəhdi istehsal source-dan çıxarılıb.
- `MobileHomeScreen.kt`: vahid panel/header, ölçüsü mövcud width+height ilə məhdud şəhər illüstrasiyası, accepted snapshot statları, native XP track və ölçülmüş label yerləşməsi; progress semantics saxlanır. Font2-də məzmun scroll, dock sabitdir.
- `MobileMenuNavbar.kt`: mərkəzi native gradient Play + Canvas parıltı, real enabled/selected/input state; 360dp-də kənar padding4dp ilə 7 slotun min48dp toxunma sahəsi saxlanır. 320dp-də yan girişlər iki sıradır; font2 accessible adları saxlanır.
- `MobileMenuShell.kt`: Home öz vahid header/body/dock-unu çəkir; başqa route-un shell daxilində Home recovery content-i header/dock-u təkrarlamır.
- İllüstrasiya, logo və mövcud ikonlar raster art olaraq qalır; panel, XP, düymə, yazılar və layout şəklə bişirilmir.

Backend müqaviləsi: GameSnapshot/account/xpInterval oxunur; DTO/API/domain/wallet/schema və server qaydası dəyişmir. AppBackend source dəyişikliyi və backend testi tələb olunmadı. Host fixture sintetikdir, real hesab/server E2E sübutu deyil.

## Yoxlamalar və vizual müqayisə

Son native10/10 PASS39s; normal core/engine/app unit+assembleDebug+lintDebug PASS65s; real foreground kontrast5/5 PASS min7.66; diffcheck PASS. Fokus: native Home6 + shell4 (360×640,390×844,320×568font2; recovery/uzun balans/simulyasiya inset); normal core/engine/app unit, assembleDebug, lintDebug; kontrast və diffcheck. Emulator açılmır, local-ci işlədilmir.

`home-compose-visual/approved-vs-compose.png`: solda təsdiqli PNG, sağda real390dp native Compose renderi. `home-360.png`, `home-390.png`, `home-320-font2.png` açılıb baxılıb. Bunlar synthetic fixture state-dir. Son390 capture-də stat yazıları tamdır; son360-da hero başlığa/stat-a daşmır.

Təsdiqli istinadla baxışda native səthlər daha sadədir: incə tekstura/parıltı, ikonların forması/nisbəti və bütün ekranın tam piksel pariteti qəbul edilməyib. Home nümunəsi istifadəçinin baxışı üçündür. İlk 360 renderində şəhər art-ı başlığa daşdı; ölçü həm width, həm height ilə məhdudlaşdırıldı və art viewport-u clip edildi. Batch native capture-də bir 390 görüntüdə stat glyph-ləri natamam göründü; ayrıca390 turunda tam capture alındı. Semantics/input PASS bütöv piksel çəkilişi iddiasını əvəz etmir.

Device/API24 runtime, IME/TalkBack, GPU/route performansı, locale matrisi və planşet yoxlanmayıb. Performans düzəlişi və tam §1.1 qəbulu iddia edilmir.

## Şəhər art-ının provenance-i

`android-billing/app/src/main/res/drawable-nodpi/home_city_cutout_v3.png` — built-in ImageGen, təsdiqli Home PNG edit reference, transparent background. 1283×1226, ayrıca production illüstrasiya; native BitmapPainter yalnız şəffaf yan boşluğu source rect ilə istisna edir, yeni crop bitmap yaratmır.

Prompt: “Extract ONLY the central illustrated city island from this approved game UI reference as a production transparent PNG asset. Preserve the exact five colorful leaning buildings, roofs, plants, benches, small central cyan fountain, green island ground and golden soil edge, positions, perspective, silhouettes, vivid polished 3D cartoon game style, lighting and detail. NO redesign, NO extra buildings. Remove all UI, background, logo, text, stats, borders, navbar and shadows belonging to UI. Genuine transparent background outside island and buildings. Tight bounds around the complete city, do not clip edges. High resolution crisp details at least 1200px tall. This is a decorative home illustration, not gameplay.”

Lokal loglar: `/private/tmp/home-compose-final-native.log`, `/private/tmp/home-compose-final-normal.log`. Source/render local review bu scoped diff üzrə aparıldı; sadalanmış vizual fərqlər açıqdır.
