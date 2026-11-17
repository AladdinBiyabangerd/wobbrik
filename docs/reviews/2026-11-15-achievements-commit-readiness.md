# Nailiyyətlər — mobil commit hazırlığı

- Branch: `feat/achievements-native-mobile`; HEAD `6c96e9c`. Yoxlanmış material HEAD-ə qarşı tracked diff və Nailiyyətlərə aid untracked source/asset/render testidir. Index boşdur; commit, merge, push, deploy və CI edilmədi.
- Authority: `docs/PLAN.md` §1.1, cari handoff, `2026-11-15-achievements-native.md` və onu tamamlayan `2026-11-15-achievements-progress.md`. Bu, bütün mobil diff-in və ya redesign mərhələsinin qəbulu deyil.

## Commit sərhədi

- Ayrıca Nailiyyətlər nüvəsi: `MobileAchievementsScreen.kt`, `achievement_art_v3.png`, core `RecoveryDtos.kt` / `RecoveryDtosTest.kt`, `OnlineGame.kt` portrait dispatch hunk-u, `GameTokens.Achievements`, dedicated `AchievementsProductionRenderTest.kt` və Nailiyyətlər review qeydləri.
- Qarışıq fayllardan yalnız uyğun hunk-lar: `MobileHomeScreen.kt` stats üçün optional achievement badge və əlaqəli parametr ötürülməsi; `MobileMenuShell.kt` Nailiyyətlər tab relyefi/icon/ölçmə davranışı. Bu faylları bütöv stage etmək əvvəlki Home işini də daxil edir.
- Açıq prerequisite: `MenuRelief.kt`, `MobileMenuIcon.kt`, Home-un Panel/Play rəng tokenləri və stats-ın native səth/ikon implementasiyası əvvəlki dirty Home/chrome işidir. Bunlar HEAD-də yoxdur. Nüvəni tək götürüb təmiz checkout-da build edilmiş saymaq olmaz. Ortaq chrome/Home əvvəl ayrıca review/commit sərhədinə ayrılmalı, sonra Nailiyyətlər həmin baza üzərində yoxlanmalıdır; owner işi səssiz geniş commit-ə yığılmamalıdır.
- Scope-dan kənar owner işi: City və shell city regression hunk-ları, mock/build variantları, WobbrikApplication/build.gradle/README, baseline `.dm` faylları, Home artwork/layout/navbar, ümumi plan/qayda/proposal sənədləri. Bunlar silinmədi, stash/reset edilmədi və stage olunmadı. Handoff-un HEAD-dən böyük fərqi əvvəlki tarixçənin qısaldılmasıdır; onu Nailiyyətlər kod dəyişikliyi kimi qəbul etmə.
- PNG render sübutları `.gitignore` ilə çıxarılır. Dedicated test və init script saxlanır; review-da qeyd edilən lokal PNG-lər avtomatik commit-ə daxil deyil.

## Review nəticəsi

- Baxılan Nailiyyətlər nüvəsində yeni funksional blocker aşkarlanmadı: accepted snapshot oxunur, journal/status gate qorunur, mükafat yalnız server award ID-si ilə tamamlanır, ratio overflow-safe-dir, legacy response progress-unavailable saxlayır. Parser malformed/duplicate/negative/threshold-a zidd progress-u rədd edir.
- Backend müqaviləsi ayrıca `AppBackend cb3cab9` commit-ində mövcuddur; additive optional sahə, server threshold və capped current mobil DTO ilə uyğun gəlir. Bu turda backend dəyişdirilmədi və DB testləri təkrarlanmadı. Deploy edilmədiyindən real serverdə legacy fallback gözlənilir.
- Son istifadəçi düzəlişi: screenshot-da işarələnən geri oxu + “Uğurlar” başlıq sətri çıxarıldı. Nailiyyətlər/Rekordlar tabları saxlanır. Dedicated test geri yolunu Android Back dispatcher ilə yoxlayır; navbar Ana yolu da saxlanır. Yeni rəng/ölçü əlavə edilmədi.

## Yoxlamalar

- Başlıq düzəlişindən əvvəl cari working source: native dedicated9 + shell4 = 13/13 PASS, failure/error/skipped0; Gradle47s. Core task UP-TO-DATE, mövcud XML137/137 PASS; bu tur core testləri yenidən icra edilmədi.
- ui-craft kontrastı yenidən ölçüldü: Text/Background16.57, Muted/Surface8.22, Success/Surface9.28, Background/Accent11.45 — 4/4 PASS.
- Yeni host renderlər `/private/tmp/wobbrik-commit-review-renders`; 390/font1 və 320/font2-progress açılıb `01-main.png` ilə müqayisə edildi. Font2 horizontal tab/vertical list scroll-u və görünən dock saxlanır. Piksel pariteti və real device sübutu deyil.
- Başlıq düzəlişindən sonra dedicated9 + shell4 = **13/13 PASS**, failure/error/skipped0; Gradle45s. Yeni renderlər `/private/tmp/wobbrik-commit-review-renders-final`; 390/font1 və 320/font2 açılıb baxıldı: başlıq sətri yoxdur, tablar/header/dock qalır. Android Back testi PASS. `git diff --check` PASS. Əvvəlki APK/lint PASS sübutu progress review-undadır; son UI düzəlişindən sonra həmin nəticə yenilənmiş APK/lint kimi təqdim edilmir.
- Emulator/canlı yoxlama/CI açılmadı. TalkBack, IME, real device/API24/GPU/performance/locale matrisi yoxlanmadı; bunlar canlı yoxlama gate-i yaratmır.

## Qərar

Working-tree səviyyəsində Nailiyyətlər nüvəsi yoxlanıb. İzolyasiya olunmuş commit hələ hazır deyil: ortaq owner asılılıqları və dəqiq staged diff ayrılmayıb, təmiz candidate tree build edilməyib. Növbəti addım owner işini qoruyaraq prerequisite və Nailiyyətlər commit sərhədlərini ayırmaq, candidate üzrə fokuslu yoxlamanı yeniləməkdir. Main merge yalnız istifadəçi təsdiqi ilə; CI yalnız ayrıca tələb olunarsa main-də.
