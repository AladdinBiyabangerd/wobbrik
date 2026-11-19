# Profil kolleksiya — native implementasiya

Branch: `feat/profile-collection-native`, baza `5af3eab` üzərində working diff. Əvvəlki Şəhər/Uğurlar və digər owner diff-i saxlanır. Vizual authority: istifadəçinin təsdiqlədiyi `docs/design/profile-collection-v1/proposal-v2.png`; PLAN §1.1.

## Dəyişiklik

Mobil account route-u feature-local ProfileCollectionShell ilə ortaq CollectionHeader/CollectionNavbar istifadə edir, inventory tab/repository çağırmır. Navbar üçün yer layout-da ayrılır; IME görünəndə gizlənir. Ad/avatar art-ı route-owned provider-dən paylaşılır. Profil tac lentli identity, ayrıca XP kartı, iki statistika kartı və yığcam Ad və avatar/Dəyiş sətri ilə native qurulur. Parametrlər yuxarıda, Nailiyyətlər/Rekordlar aşağıdadır. Kiçik portret/font2-də mövcud pinned Dəyiş qalır; body scroll edir. Shortcut sözləri qırılmır, dar yerdə horizontal scroll edir.

Redaktə eyni kolleksiya paneli, ad sayğacı, server limitinə uyğun field və sabit saxla/ləğv əməliyyatlarıdır. Avatar sheet aşağıya bağlıdır; bağla, üç mövcud ID, lokal seçim və ayrıca təsdiq saxlanır. Yeni domain/asset/framework/pin yoxdur. Legacy/tablet branch-i saxlanır. Sadə UI üçün raster skin istifadə edilmir.

## Müqavilə və oxu sərhədi

PublicAccountScreen orchestration/load/save/conflict/uncertain retry/login epoch mexanizmi dəyişmir. AppBackend `docs/wobbrik-game-api.md` public profile müqaviləsi yoxlandı: GET/PUT, müstəqil revision, 1–15 Unicode codepoint və builder/pilot/architect. API/DTO/schema/domain dəyişməsi lazım deyil; backend faylları dəyişdirilməyib. XP üçün qalan miqdar yalnız qəbul edilmiş server intervalından çıxılır, yeni level formula yoxdur.

Soyuq giriş mövcud GET profile-ı saxlayır; display memory varsa təkrar girişdə dərhal həmin hesabın qəbul edilmiş adı göstərilir, mutation üçün yenə fresh load tələb olunur. Hər mount bir bounded profile oxuyur; yeni kolleksiya/şəhər/N+1/polling yoxdur. Rəsm decode mövcud async art helper-dədir. Profil ölçüsü inventarın böyüklüyü ilə böyümür. Yeni performans optimizasiyası iddiası yoxdur.

## Yoxlama və məhdudiyyətlər

Host harness `profile-collection-native/render/`: normal390×844, kiçik320×568, font1/font2 və360×640 font2 + simulated24/48inset/uzun ad/balans. Editor save/cancel/name/disabled, avatar lokal seçim/təsdiq/ləğv və shortcut callback-ləri yoxlanır. Real PublicAccountScreen harness fresh/cache/error, UID/login epoch və uğurlu save-in display memory-yə keçməsini yoxlayır. Fokuslu core testləri revision/conflict/retry/name/avatar/account sərhədlərini saxlayır.

İlkin host qaçışları harness-də köhnə heading source-u və pinned shortcut üçün əsassız performScrollTo səbəbindən uğursuz oldu; son source/harness yeniləndi. Vizual baxışda font2 shortcut söz qırılması tapılıb horizontal scroll ilə düzəldildi. Bu uğursuz qaçışlar PASS sayılmır.

Kontrast GameTokens-dən çıxarılmış `contrast.css` ilə skill checker: Text12.51, Muted7.91, Accent8.65, Danger8.05 PanelTop üzərində;4/4 PASS. Panel gradientinin ən açıq stop-u əsas götürülür. Art və dekorativ konturlar üçün tam kontrast ölçməsi deyil.

PNG ilə fərqlər: əvvəlki production account_portraits_v3 personajları saxlanır; Pilot və Memar generativ nümunədən fərqlidir. Tac/shield/XP native geometry-dir, nümunənin illüstrativ relyefi ilə piksel eyniliyi yoxdur. Host dialog capture arxa activity-ni göstərmir, qara sahə real ekranın qara olmasına sübut deyil. Kiçik/font2-də body scroll və zəruri dock wrap mövcuddur; bütün content eyni anda görünmür. Real cihaz/API24/IME/Gboard/TalkBack/GPU və bütün locale kombinasiyaları yoxlanmayıb. Emulator/CI/merge/push/deploy yoxdur. Tam PNG pariteti və bütün app redesign qəbulu iddia edilmir.


Son-source nəticəsi: native host13/13 və core7/7 PASS; assembleDebug/lintDebug PASS, BUILD SUCCESSFUL26s. `git diff --check` PASS. Sübutlar `profile-collection-native/` altında15 raw render, test XML-ləri, checks.txt, profile-reads.csv və source hash-ləridir. Normal/kiçik/font2/inset əsas profil/editor/sheet nümunələri açılıb PNG ilə yanaşı müqayisə edildi. Son harness optional logo-nun decode-unu gözləyir və native display-list warmup edir; production gecikməsi deyil. Font2 shortcut sözləri artıq qırılmır; kənara çıxan hissə horizontal scroll ilə əlçatandır. Kiçik editor top capture-da field-in alt hissəsi viewport-dan çıxır; field-ə scroll/text replacement testi var və save/cancel sabit görünür.

Real PublicAccountScreen synthetic HTTP fixture-i: ilk və3təkrar giriş cəmi4GET/244response-byte (hər cavab61byte), əlavə domain read və write yoxdur. `profile-reads.csv` cari host content/avatar vaxtlarını saxlayır; fixture400ms süni server gecikməsi daşıyır. Bu JVM/host rəqəmləri ART/frame/GPU/production latency deyil; əvvəl-sonra optimallaşdırma iddiası yoxdur. Account epoch/cache/error/fresh gating və save regression testləri3/3keçib. Final harness üzrə request/byte sərhədi sabitdir.

Təkrar yoxlama (android-billing daxilindən, JDK21):
`./gradlew --offline -I ../docs/reviews/profile-collection-native/render/render.init.gradle :app:testDebugUnitTest --tests 'com.aladdin.wobbrik.ProfileCollection*Test' --tests 'com.aladdin.wobbrik.ProfileOpenLatencyTest' :core:test --tests 'appbackend.wobbrik.game.PublicProfilesLeaderboardTest' :app:assembleDebug :app:lintDebug -Dwobbrik.render.output=/private/tmp/wobbrik-profile-collection`
