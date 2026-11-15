# Mərhələ 5 — profil və rekordlar UI, 14 noyabr 2026

Branch `feat/market-leaderboard`, baza `9653b98`; bu davam uncommitted-dir. Əvvəlki bazar/data və owner/release working diff-i qorunur. Commit/merge/push/deploy/CI yoxdur.

## Scope və davranış

- `PublicAccountScreen.kt`: real server profili, seçilən ad/avatar, qəbul edilmiş level/XP/rekord, profil GET/PUT. PublicProfile Google adı/şəkli deyil. Revision konflikti server profilini göstərir, avtomatik overwrite yoxdur; növbəti Save ayrıca istifadəçi əməliyyatıdır. Qeyri-müəyyən cavabda draft bağlanır, eyni replacement təkrar edilir və ya ayrıca GET ilə yenilənir. Yazı iqtisadi journal/wallet-ə toxunmur.
- `LeaderboardScreen.kt`: server həftəsi, all/weekly scope, immutable traversal, refresh, pagination, loading/error/empty. Öz sətri yalnız server `is_self` verdikdə vurğulanır; uydurma me rank/endpoint yoxdur. Snapshot bitdikdə istifadəçi Yenilə ilə yeni traversal açır. Sıra boşluqları uydurma oyunçularla doldurulmur; podium yalnız rank 1/2/3 varsa göstərilir.
- `OnlineGame.kt` və `WobbrikApplication.kt`: real ProfileAccess/LeaderboardRepository wiring; Hesab→Rekordlar və geri. Exact Ready identity key + login epoch və session/current Ready fencing saxlanır. Background işi yeni hesabın UI-sinə qəbul edilmir.
- UI/domain/data ayrıdır; yeni framework/stack/pin yoxdur. AppBackend public profile/leaderboard müqaviləsi read-only yoxlandı (`docs/wobbrik-game-api.md` public profiles bölməsi). Mövcud müqavilə kifayətdir; əlavə backend/API/schema/ledger dəyişikliyi tələb olunmur. Backend branch-in deploy edildiyi iddia edilmir.

## Vizual əsas və məhdudiyyət

İstinadlar `docs/design/mobile-v1/play-progress-v2.png` (rekordlar), `account-states.png` (hesab), mövcud GameTokens/MobileUiSizing/HomeButton/account_hero. Təsdiqli PNG yenidən təsdiqə çıxarılmayıb. Yeni palette/spacing/radius yoxdur; podiumun raster source bounds-u token faylındadır.

`leaderboard_approved_board.png` təsdiqli `play-progress-v2.png`-nin dəyişdirilməmiş surətidir (SHA256 eyniliyi yoxlanır). PublicAvatar mənbə portretlərini circular source window ilə çəkir; podium orijinal laurels/crown/1-2-3 blok/çəmən kompozisiyasını saxlayır, avatar və nəticələr real entry ilə üstündə dəyişir. Yeni asset dizaynı yoxdur. İlkin imagegen törəmələri detal uyğunluğu yetərli olmadığı üçün production-dan çıxarıldı; generated output yalnız alətin xarici output qovluğundadır. Kiçik source portretlərin böyüdülməsi keyfiyyəti və raster background düzbucaqlısının kənarı tam təmiz şəffaf asset keyfiyyəti deyil; source overlay framing/dinamik score badge, typography/row ölçüləri üzrə tam pixel parity PASS sayılmır. Bu review həmin fərqləri təsdiqlənmiş istisna etmir.

360×640 və 390×844 font1, 320×568 font2 production Compose host renderləri. Font2-də scope tabları tam enli/alt-alta, ikinci dərəcəli podium gizlidir; ad/avatar/score siyahıda qalır. Body scroll, əsas geri/save/home əməliyyatları fixed footer-dadır; safeDrawing/IME padding var. Yalnız mobil portret; planşet/landşaft ayrıca tapşırıqdır.

Şəkillər: `profile-leaderboard-visual/{account,editor,weekly,all,self-page,error,empty}-*.png`. Host synthetic server fixture-dir; real hesab/device/runtime screenshot deyil. Native decorView.draw capture-da bəzi təkrar transition renderlərində başlıq/back kimi dəyişməyən elementlərin çəkilməsi natamam ola bilər; bununla tam vizual qəbulu iddia etmək olmaz. PixelCopy/captureToImage host-da timeout verdi, PASS deyil. İlk və son host müqayisələri, mətn/controls və production callback axını ayrı qiymətləndirildi.

## Lokal review və yoxlamalar

Lokal scope review: source placement/imports, epoch/current fencing, cancellation, revision konfliktinin explicit save sərhədi, unknown write retry, immutable cursor/scope və iqtisadi authority yoxlandı. İlkin supporting-text default rəngi token Muted ilə düzəldildi. Podium/avatar boşluğu sıxlaşdırıldı; font2 tab söz qırılması alt-alta layout ilə aradan qaldırıldı. Qısa ekranda lazy item-i gözləyən host testi scroll-aware edildi (ilk uğursuz run-lar PASS deyil).

Final Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`: PASS28s; core130/engine95/app24, failures/errors/skipped0. Final ayrıca native host `ProfileLeaderboardRenderTest`: 3/3PASS19s. Konflikt/no-auto-overwrite, unknown PUT exact retry, fərqli all avatar/score, weekly server week, cursor/self/error/empty/back və fixed action visibility yoxlanıb. 21PNG saxlanır (7state×3ölçü); seçmə son all390/weekly360/weeklyfont2/account390/editorfont2/errorfont2/emptyfont2/self renderlərinə baxıldı. Native capture məhdudiyyəti yuxarıdadır; tam vizual PASS deyil.

Kontrast4/4PASSmin8.22 (`Text:Background`, `Muted:Surface`, `Background:Accent`, `Text:Raised`). Approved board SHA256 source/copy eyni: `1bebe3013cc277c713d47f965917d300ebdce7a095f437361932fd0b480a125d`. `git diff --check`PASS. Loglar `/private/tmp/wobbrik-profile-render-final.log`, `/private/tmp/wobbrik-profile-checks-final.log`. Əvvəlki uğursuz host/PixelCopy run-lar bu nəticəyə daxil deyil.

## Açıq sərhədlər

Tam PNG pixel parity, şəffaf source art sərhədi, kiçik portret raster keyfiyyəti və dinamik overlay framing üzrə uyğunluq açıqdır. Bütün state×ölçü×font/locales və uzun ad/rank/balance kombinasiyaları yoxlanmayıb. Real HTTP/device/iki hesab, keyboard/inset/TalkBack/GPU/API24 runtime yoxlanmayıb; emulator açılmayıb. Bu inteqrasiya mərhələ 5 qəbulu və ictimai buraxılış hazırlığı deyil. Ayrıca göstərişsiz merge/push/CI yoxdur.
