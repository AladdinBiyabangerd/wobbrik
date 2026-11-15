# Profil/rekord — mənbə qrafikası və capture pariteti, 15 noyabr 2026

`feat/market-leaderboard`, baza `9653b98`, uncommitted working diff. Əvvəlki bazar/data/owner/release faylları qorunur. Commit, merge, push, deploy və CI yoxdur. Yeganə plan `docs/PLAN.md` §§1–2,4-dür.

## Düzəldilən hissələr

Əsas review: `2026-11-14-phase5-profile-leaderboard-ui.md`. Təsdiqli `mobile-v1/play-progress-v2.png` və `account-states.png` yenidən təsdiqə çıxarılmadı.

- `CommunityArt.kt`: dəyişdirilməmiş təsdiqli PNG-lərdən source pəncərələri çəkilir. Border-connected navy fon üçün hesablanan clip düzbucaqlı podium fonunu aradan qaldırır; əl ilə təxmini kontur production-da yoxdur. Fonun boş source margin rəngləri hamar gradient üçün götürülür; yeni palette və generativ asset yoxdur. Geri düyməsi, təqvim və mərtəbə işarəsi source-dandır. Eyni böyük board hər avatar üçün ayrıca decode edilmir: `CommunityFrame` onu bir dəfə yükləyib `LocalCommunityBoard` ilə paylaşır.
- `LeaderboardArt.kt`: podiumun tacı, laurelləri, rank blokları və ot kompozisiyası source-dan qalır. Kiçik 60px row avatarı podiuma böyüdülmür; dəyişən podium avatarları daha böyük orijinal portretlərin daxili sahələrindən çəkilir. Əvəzləmə yalnız rank çərçivəsinin içindədir, score badge çərçivələri saxlanır. Faktiki avatar/xal mənbə ilə eynidirsə həmin hissə ümumiyyətlə örtülmür. Rəqəmlər badge-in daxilinə sığdırılır; böyük nəticə mətninə görə podium göstərilmədikdə real siyahı qalır.
- `LeaderboardScreen.kt`: ortaq scope chrome, source rank laurelləri və mərtəbə işarəsi, mərkəzlənmiş logo, real `is_self` vurğusu. Uzun rank ayrıca sətirdə, böyük xal adın altında göstərilir. Font2-də scope-lar alt-alta, həftə tarixi yığcamdır; Bakı vaxtı accessibility təsvirində qalır. Əsas geri/yenilə əməliyyatları footer-dədir, body scroll saxlanır.
- `PublicAccountScreen.kt`: hero əvvəlki fərqli `account_hero` əvəzinə orijinal `account-states.png` source hissəsidir. Source-dakı adın `İ` nöqtəsi crop-a düşmür, sağdakı ağac kəsilmir. Real ad/avatar mərkəzdədir; real server XP intervalı qalın, mətnli progress pill-də göstərilir. Mətnin dolu hissəsi tünd, qalan hissəsi açıqdır. Profil/nailiyyət/rekord keçidləri icon + mətn + chevron kompozisiyasındadır. Uzun mətn və font2 body scroll edir, Ana ekran/Save/Geri sabit qalır.
- `GameTokens.kt` source koordinatlarını saxlayır. `HomeButton`-a optional `leading` slot əlavə olunub; default `null`, əvvəlki çağırışların görünüşü dəyişmir.

`leaderboard_approved_board.png` SHA256: `1bebe3013cc277c713d47f965917d300ebdce7a095f437361932fd0b480a125d` — orijinal `play-progress-v2.png` ilə eynidir.

`account_approved_board.png` SHA256: `3ab08b9989588679816290ab2464b370d0d2926feaa3b370c52338970d5a6de7` — orijinal `account-states.png` ilə eynidir. Alpha/source clip UI renderer-də hesablanır; PNG rəngləri yenidən çəkilmir.

## Başlıq/footer çəkilişinin həlli

Adi `decorView.draw()` ardıcıl screenshot-larda native Compose/Skia qatlarının bəzi dəyişməyən hissələrini buraxırdı. Yalnız `View.invalidate()` və hətta Compose `invalidateDescendants()` təkbaşına bütün ssenarilərdə kifayət etmədi. Əvvəlki natamam görüntülər tam vizual PASS deyil.

Son host hər snapshot üçün ayrıca test sinfi və təzə JVM istifadə edir (`forkEvery=1`, `maxParallelForks=2`). Production ekranına real callback-lərlə çatılır; həmin ssenaridə yalnız tələb olunan state çəkilir. Çəkilişdən əvvəl Compose qatları invalidate edilir. Eyni state iki dəfə tam çəkilir və bitmap-lərin eyniliyi assert olunur. Şəkillərə ayrıca header/footer yapışdırılmır, postprocess ilə çatışmayan UI tamamlanmır.

`tools/CaptureIntegrityCheck.java` 28 hazır PNG-də geri düyməsinin bölgəsini account ilə, rekord başlığı və footer-ini scope/state-lər arasında piksel üzrə müqayisə edir. Görünən geri oxu və əsas düymənin açıq çərçivəsi də yoxlanır. **28/28 PASS**. Alət Android test sourceSet-in xaricindədir; Java AWT yalnız lokal PNG QA/contact sheet üçündür.

## Real data və backend sərhədi

Production ProfileAccess GET/PUT, ProfileEditor, LeaderboardRepository/Traversal və Application/OnlineGame wiring dəyişməyib. Server adı/avatarı, həftə/rank/xal, cursor və `is_self` istifadə olunur. Default nümunə ad/rank/xal server nəticəsinin əvəzinə göstərilmir. Konflikt explicit ikinci Save tələb edir, unknown PUT eyni replacement ilə retry olunur. Session/Ready fencing və iqtisadi journal sərhədi saxlanır.

AppBackend `docs/wobbrik-game-api.md` public profiles/leaderboard müqaviləsi read-only yoxlandı. Bu UI/çəkiliş işi API, DTO, schema, wallet/ledger və domain qaydalarını dəyişmir; backend dəyişikliyi tələb olunmadı. Deploy iddiası yoxdur.

## Son yoxlamalar

- Java21/offline native host: **28/28 PASS, 1m40s**. 360×640font1, 390×844font1, 320×568font2 və 320×568font2/24simvol ad/Long.MAX_VALUE rank-xal. Account/editor/weekly/all/self-page/error/empty, konflikt/no-auto-save, exact PUT retry, scope dəyişməsi, pagination/self və geri axını. `native-results.txt` ayrıca saxlanır.
- Son adi `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`: **PASS30s**, core130 + engine95 + app24 = **249**, failures/errors/skipped0. Host init tətbiq olunmadan işlədildi.
- Kontrast **4/4 PASS**, min **8.22** (`Text:Background`, `Muted:Surface`, `Background:Accent`, `Text:Raised`). Hər iki source/copy SHA256 eynidir. `git diff --check` PASS.
- Son `final-preview.png`, `final-font2.png`, `approved-comparison.png` və seçmə raw weekly360, all390, editorfont2, error390, emptyfont2, self360 və uzun-ad accountfont2 görüntülərinə baxıldı. `approved-comparison.png` təsdiqli source ilə production host-u yanaşı göstərir.

Loglar: `/private/tmp/wobbrik-profile-parity-native-final.log`, `/private/tmp/wobbrik-profile-parity-checks-final.log`.

## Paritetin dəqiq sərhədi

Review-dakı mənbə art/örtük və natamam capture problemləri bu vahiddə düzəldilib. **Bütöv ekranların tam piksel eyniliyi PASS deyil.** Yanaşı müqayisədə chrome rəngi/gradienti, typo/ölçü/boşluq və kompozisiya fərqləri görünür; bunlar üçün ümumi yeni vizual istisna təsdiqi yoxdur. Orijinal source qrafikasının saxlanması bütün ekranın birəbir qəbulunu əvəz etmir.

Statik source-dakı “İnşaatçı”, “12 bina”, “7 / Sən” kimi nümunə məzmun real data əvəzinə köçürülmür. Profil editoru realdır; hazır Parametrlər route-u və server bina sayı olmadan saxta giriş/statistika əlavə edilməyib. Öz nəticəsi yalnız server səhifəsində `is_self` olduqda görünür; olmayan me-rank endpoint uydurulmayıb. Mobil ölçü tokenləri və böyük şriftə uyğun body scroll qorunur. Bu funksional məzmun fərqləri tam dizayn qəbulu kimi təqdim edilmir.

Host sintetik server fixture-dir, real hesab/HTTP/device görüntüsü deyil. Bütün state×locale×ölçü kombinasiyaları, IME/inset/TalkBack/GPU/API24 runtime yoxlanmayıb. Emulator açılmadı; canlı yoxlama gate deyil. Planşet/landşaft bu mobil portret vahidinə daxil deyil. Tam mərhələ5 qəbulu və buraxılış hazırlığı deyil.
