# Profil — portrait və native material

Branch: `feat/profile-collection-native`, baza `5af3eab` + əvvəlki owner working diff. Authority: PLAN §1.1, `docs/design/profile-collection-v1/proposal-v2.png`. Bu vahid əvvəlki polish review-da qalan portrait və material işini davam etdirir; əvvəlki diff saxlanıb.

## Dəyişiklik və lokal review

`account_portraits_collection_v1.png` built-in image_gen ilə təsdiqli PNG əsasında ayrıca şəffaf portrait atlası kimi hazırlanıb. Pilot artıq köhnə aviator deyil: kapitan papağı, qızılı qanad və epauletli navy formadır. Memar ağ dəbilqə, dairəvi eynək, saqqal və blueprint ilə nümunənin personajına uyğundur. Üç portrait eyni illüstrasiya dəstindədir. Native çərçivə, yazı və seçim nişanı atlasda yoxdur. Köhnə v3 asset saxlanır. Son prompt və alət `profile-collection-material/asset-prompt.txt`-dədir.

Mobil PublicAccount provider və native overview/editor/sheet eyni atlası mövcud route-owned async helper vasitəsilə paylaşır. Tablet legacy branch-i və server profil ID-ləri dəyişmir. Yeni cache, request, polling və dependency yoxdur. GET/PUT/revision/retry/account sərhədinə müdaxilə edilməyib; bu asset/material diff üçün AppBackend API/DTO/schema dəyişməsi lazım deyil.

ProfileCollectionPanel native gradient kənar işığı və radial material işığı aldı; panelin aşağı hissəsi mövcud DockBottom tokeninə söykənir. NativeProfileAction relyefli mavi gradientlə çəkilir. Tacda qızılı işıq zolağı və alt kölgə var. XP seal sərt ulduzdan yumşaq quadratic scallop-a keçirildi, mərkəzi enamel işığı əlavə edildi. Rəng/spacing/radius üçün mövcud GameTokens təkrar istifadə edildi; ortaq CollectionHeader/Navbar authority-si dəyişdirilmədi. UI raster skin deyil.

Lokal diff review: bitmap yalnız mürəkkəb portrait-dir; source rect-lər atlasın üç bərabər724px hüceyrəsinə uyğundur. Öncəki və yeni atlas2172×724, nəzəri RGBA6,290,112byte. PNG1,922,117→1,960,875byte (+38,758). Decode main thread-də deyil, mövcud Dispatchers.IO helper-dədir; bir hesab ekranında paylaşılır, kart/recomposition başına bitmap yaradılmır. Shader panel ölçüsü üzrə drawWithCache-dədir. Yeni performans optimallaşdırması/production latency iddiası yoxdur; bounded profil HTTP regression fixture-i dəyişməyib.

## Sübut və sərhədlər

İlk qaçış12/13: EditorFont logo semantic wait10000ms timeout, əvvəlki review-dakı eyni harness55 sərhədi. XML `first-run/`-də saxlanır. Source/harness dəyişmədən təkrar qaçış13/13PASS; assembleDebug/lintDebugPASS, BUILD SUCCESSFUL1m5s. Bu fokuslu lokal yoxlamadır; local-ci.sh işə salınmayıb. `git diff --check`PASS.

Kontrast: radial işığın ən güclü .18alpha stop-u PanelTop üzərinə kompozit edilib; Text10.71, Muted6.77, Accent7.40, Danger6.89 —4/4PASS. `contrast.css` və `contrast.txt` sübutdur. Dekorativ artın bütün pikselləri üçün kontrast iddiası deyil.

15native host PNG və11testXML, sourcehash bu review-nun `profile-collection-material/` qovluğundadır.390×844 və320×568/font1–2,360×640/font2+simulated24/48inset/uzun ad/balans scope-u saxlanıb. Normal overview/editor/sheet, kiçik overview, font2overview/editor/sheet və insetoverview/sheet açılıb proposal-v2 ilə müqayisə edildi. Pilot/Memar personaj uyğunsuzluğu aradan qaldırılıb; panel/tac/XP materialı daha relyeflidir. Kiçik/font2 body scroll edir; əsas save/apply/edit pinned qalır; sözlər üçün əvvəlki horizontal scroll saxlanır. Dialogun qara arxası host capture məhdudiyyətidir.

Generativ referenslə portrait üz detalı və native relyef piksel eyni deyil; tam PNG pariteti iddia edilmir. Ortaq təsdiqli kolleksiya chrome-u saxlanır. Real cihaz/API24/IME/TalkBack/GPU və bütün locale kombinasiyaları yoxlanmayıb. Emulator açılmayıb. Commit/merge/CI/push/deploy yoxdur. Bütün app redesign-ının mərhələ qəbulu deyil.

Təkrar yoxlama: əvvəlki native review-dakı Gradle command; `-Dwobbrik.render.output=/private/tmp/wobbrik-profile-material`, ProfileCollection*Test + ProfileOpenLatencyTest, assembleDebug/lintDebug. Core/domain dəyişmədiyi üçün əvvəlki core nəticələri yenidən icra edilmiş sayılmır.

19 noyabr istifadəçi qəbulu: son native render təsdiqli PNG ilə eyni hündürlükdə yanaşı göstərildi; istifadəçi “okeydir” ilə Profilin son görünüşünü təsdiqlədi. Bu, merge/CI göstərişi deyil. Sonrakı ayrıca göstəriş bütün cari dəyişikliklərin uyğun commit edilməsidir.
