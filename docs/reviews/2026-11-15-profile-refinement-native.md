# PLAN §1.1 — Profil overview vizual dəqiqləşdirməsi

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`. Mövcud owner dirty işi qorunub; stage/commit/merge/push/deploy/CI yoxdur. Scoped production dəyişiklikləri `NativeProfileOverview.kt`, `ProfilePresentation.kt`, `PublicAccountScreen.kt`.

Authority `design/unified-mobile-v3/03-account.png` və README-də son Home chrome qərarıdır. Yeni dizayn/palitra/token/stack yoxdur.

## Dəyişikliklər

Native tac lentində iki yan quyruq, geri qatlanan hissələr, əyri ön lent və gradient mərkəzi tac nişanı quruldu. Səviyyə shield-inə native qızılı laurel və mavi ribbon əlavə edildi; ölçüsü mövcud CrownWidth/Target pillələrinə bağlandı. XP yazısı native möhür siluetindədir. Normal fontda stat ikonları rəqəmin yanındadır, iki stat arasında şaquli rule var. Stat etiketləri Nunito/Bold istifadə edir. Mobil font1 səviyyə etiketi Body, XP zolağı Small+Quarter hündürlüyündədir; font2 adaptasiyası saxlanır. Parametrlər üçün native gear, Rekordlar üçün mövcud trophy ikonu əlavə edildi; ikon seçimi görünən mətnə bağlı deyil.

Real load/save/conflict/exact retry, epoch, XP interval hesabı, enabled action və route callback-ləri dəyişmir. Backend müqaviləsi/domain/schema dəyişmədiyinə görə AppBackend dəyişiklikləri və testləri lazım deyil. Editor/avatar picker bu vahiddə dəyişməyib.

## Yoxlama və vizual review

- Son source: dedicated Profil5 + real shell4 = 9/9 PASS; failure/error/skipped0.
- Son debug/mockDebug APK + lintDebug PASS; yekun Gradle turu1m46s.
- Token contrast6/6 PASS, minimum8.22; Text/Raised10.24, Text/PanelTop13.58, Background/Accent11.45. Palitra dəyişməyib.
- `git diff --check` PASS; index boşdur. Scoped review: mobile-only font ölçüsü, token reuse, shortcut explicit icon seçimi, native canvas dekorları, scroll/48dp hədəf və domain sərhədi yoxlandı.
- Son5 native PNG `profile-refinement-native/` altında açılıb03-account ilə müqayisə edildi:390/font1,320/font1,320/font2,390/font2,360/font2+24/48dp simulated inset+uzun ad/Long.MAX_VALUE balans. Əsas Dəyiş görünür; font2 qısayol mətnləri horizontal scroll, aşağı body vertical scroll-dadır.
- İlk turda regular portret capture-da boş göründü; son source/fresh JVM capture-da normaldır. İlk capture vizual PASS sayılmadı. Əlavə screenshot assertion yazılmadı.

## Açıq qalan fərqlər

Bu, tam PNG pariteti deyil. Approved account portretinin dəqiq crop-u üçün ayrıca uyğun artwork yoxdur; mövcud records transparent personaj sheet-i saxlanır. Lent/tac/shield-in native silueti və relyefi reference-dən hələ fərqlənir; panel/header/statların tam piksel ritmi qəbul olunmayıb. Editor/avatar picker paneli legacy raster-dir. Bu qalan fərqlər və bütün Profil/mərhələ qəbulu tamamlanmış sayılmır.

Emulator açılmadı; device/account E2E, TalkBack/IME/API24/GPU/performance/locale/tablet yoxlanmadı. Native host render runtime sübutu deyil.

## Son istifadəçi düzəlişi — başlıq sətrinin silinməsi

İstifadəçinin ekran şəkli üzrə mobil Profil overview-də geri oxu və “Profil” başlıq sətri `ProfileFrame`-dən çıxarıldı. Boş header slot saxlanmır; lent yuxarı keçir. `PublicAccountScreen` Android BackHandler və editorun geri/ləğv axını saxlanır. Yalnız mobil shell+coins overview şaxəsi dəyişib; editor və legacy/tablet header dəyişməyib. Bu tur qalan relyef/crop uyğunlaşdırması genişləndirilmədi.

Kontrast3/3 PASSminimum8.22; palitra dəyişməyib. İlk render əmri yanlış relative edit yolu səbəbindən source dəyişmədən başladı və dayandırıldı; PASS sayılmır. Düzgün son source üçün dedicated5+shell4=9/9 PASS56s, failure/error/skipped0.390/font1 və320/font2 native PNG açılıb baxıldı, `profile-no-heading-native/` altında saxlanıldı; başlıq/ox yoxdur, Dəyiş görünür. Son düzəlişdən sonra APK/lint təkrarlanmadı. Diffcheck PASS; index boşdur.
