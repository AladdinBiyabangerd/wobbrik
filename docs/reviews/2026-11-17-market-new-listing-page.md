# Yeni elan — native səhifə / qiymət / komissiya

Branch: feat/community-tabs-native-consistency; HEAD 6c96e9c üzərində mövcud owner diff. Bu vahidin müqayisə bazası /private/tmp/market-listing-before.kt; commit/stage/merge/push/deploy/CI yoxdur.

PLAN §1.1 və unified-mobile-v3/02-market aşağı orta əsasında yalnız MarketScreen.kt mobil Yeni elan forması ayrıca shell səhifəsinə keçirildi. Header/navbar saxlanır; contextual geri və Android Back seçilmiş formadan bina seçiminə qaytarır. Mövcud bina xülasəsi təkrar istifadə edilir. Native coin qiymət sahəsi, maksimum qiymət/invalid state, komissiya (5%) və Alacağın paneli, info və mövcud 5 aktiv elan/şəhərdə qalma məlumatı əlavə edilib. Primary və cancel düymələri scroll body-dən kənardadır; IME padding tətbiq olunur. Kiçik/font2 səhifədə body scroll tələb edir.

Qiymət əvvəlki 1..maxPrice/Long qaydasındadır. Komissiya mövcud MarketMutations.fee ilə yuxarı yuvarlaqlaşdırılır (24 → 2; alınacaq 22). Yeni domain qaydası/wallet/API/DTO/schema yoxdur; AppBackend dəyişikliyi lazım deyil. Legacy non-mobile sale dialog və digər modal callsite-lar saxlanır. Profil, Ərazi, token/asset/shared chrome dəyişdirilmədi. Press animasiyası ayrıca qalır.

Kontrast ui-craft checker: 5/5 PASS; Muted/Raised6.48, Danger/Raised6.59, Muted/PanelTop8.59, Text/Raised10.24, Text/PanelTop13.58. Scoped source review: real list mutation, Back/cancel, default modal callsite-ları və owner işi qorunur.

İlk commerce5+shell4 native host 9/9 PASS1m36s. 390/844 və320/568 font1/font2,360/640 font2 simulated inset/uzun ad-balans PNG-ləri /private/tmp/wobbrik-new-listing-renders altında açılıb02-market ilə müqayisə edildi. Normal portretdə qiymət/komissiya/info birlikdə görünür; kiçik/font2 capture-lar scroll state-dir. Real kataloq artwork saxlanır; tam piksel pariteti iddiası yoxdur. Son validasiya/fee assertion və build nəticələri aşağıda əlavə edilir.

Device/account/TalkBack/real IME/real inset/API24/GPU/performance/locale/tablet yoxlanmadı; emulator açılmadı. Host render real cihaz və server E2E sübutu deyil.

Son validasiya/fee assertion-ları ilə commerce5+shell4=9/9 PASS1m28s; boş və sıfır qiymətdə publish disabled,24 üçün fee2/net22, cancel və əvvəlki route/retry/Ərazi absence işləyir. Son production debug/mockDebug APK+lintDebug PASS1m14s. git diff --check PASS; index boşdur. İlk sandbox Gradle lock cəhdi yoxlama nəticəsi deyil; icazəli rerun keçib. Mövcud nullable String/ripple warning-ləri qalır.

Əlavə net-amount visibility testində 320/font2 və inset/font2 performScrollTo sonrası22 display assertion FAIL oldu: məbləğin horizontalScroll konteyneri vertical body-ni scroll etmirdi. Əvvəlki5+4 PASS nəticəsi bu yeni assertion-u əhatə etmirdi. Test production-a toxunmadan outer body swipe-a keçirildi; son nəticə aşağıda qeyd olunur.

Swipe ilə əlavə net display yoxlaması da iki kiçik/font2 halda FAIL: gesture target-i keçirdi. Testdə outer vertical ScrollBy kiçik addımlarla işlədildi; commerce5/5 PASS. Son capture-da net məbləği viewport kənarından içəri gətirmək üçün əlavə48dp scroll qoyuldu. Bu düzəlişlər yalnız QA hostundadır; production source/build dəyişmir.

Son test-only capture ilə commerce5/5 PASS53s, failure/error/skipped0. 320/font2 və inset/font2 net22 renderləri ayrıca açılıb baxıldı: məbləğ tam görünür, sabit action/cancel/nav qorunur. Beş qiymət və beş net capture market-new-listing-page/ altında saxlanıb; ilkin beş qiymət və son iki kiçik/font2 net PNG vizual baxılıb. Shell4 əvvəlki son production9/9 run-da keçib; production bundan sonra dəyişməyib. Bütün redesign/mərhələ qəbulu deyil.
