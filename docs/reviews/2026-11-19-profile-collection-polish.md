# Profil native — vizual düzəliş

Branch: `feat/profile-collection-native`; baza `5af3eab`, əvvəlki owner working diff saxlanır. Authority: PLAN §1.1 və `docs/design/profile-collection-v1/proposal-v2.png`. Əvvəlki native review və `/private/tmp/wobbrik-profile-collection` renderləri başlanğıc müqayisədir.

## Bu vahid

NativeProfileOverview: lentin həddindən artıq eni azaldıldı; səviyyə shield-in sərt küncləri yumşaldıldı; tacın aşağı relyef bandı və XP nişanının ikiqat konturu əlavə edildi. XP yazısında tünd token saxlanır: kiçik yazını açıq cyan üzərində ağ etmək kontrastı zəiflədərdi. PublicAccountScreen: yalnız mobil profil XP barı 12dp-dən mövcud token pilləsi ilə20dp-ə qalxdı. NativeProfileAction konturu mövcud ikiqat hairline ilə gücləndi. NativeProfileEditor: geri əməliyyatı sabit48dp touch target və native Canvas oxlu relyefli düymə səthinə keçirildi, sheet tutacağı və dairəvi konturlu seçilmə nişanı əlavə edildi.

Yeni token/pin/framework/domain/cache/oxu əlavə olunmayıb. Əvvəlki GET/PUT/revision/retry sərhədləri dəyişmir; bu vizual diff üçün AppBackend müqavilə dəyişikliyi lazım deyil. Köhnə owner diff-i reset/stash edilməyib. CI/merge/push/deploy və emulator yoxdur.

## Qalan fərqlər

Production portrait personajları hələ proposal-dakı Pilot/Memardan fərqlənir. Təsdiqli PNG ayrıca təmiz, yüksək keyfiyyətli portrait asset dəsti deyil; yeni personaj uydurulmayıb, UI şəkilə bişirilməyib. Native crown/shield/XP və ortaq chrome generativ referensin illüstrativ materialı ilə piksel eyni deyil. Bu iş tam paritet və mərhələ qəbulu deyil. Avatar dialog host capture-da arxa activity qara qala bilər; real cihaz nəticəsi kimi təqdim edilmir. API24, real IME/TalkBack/GPU və bütün locale kombinasiyaları yoxlanmayıb.

## Ölçmə və review

Palitra dəyişmir. Skill checker: Text/PanelTop12.51, Muted7.91, Accent8.65, Danger8.05;4/4PASS. Diff whitespace yoxlaması PASS. Əvvəl/sonra render scope-u eyni390×844 və320×568, font1/2,360×640font2+24/48simulated inset-dir. Normal renderlərdə lent təxminən282dp-dən203dp-ə endi; XP bar12dp-dən20dp-ə yüksəldi. Bunlar layout ölçüləridir, performans optimallaşdırması iddiası deyil. İlk qaçışdan sonra shield/XP bar düzəlişi olduğundan ilk PASS son-source sübutu sayılmır. İkinci qaçış13testdən1-də logo semantic wait10000ms timeout ilə dayandı (EditorFont, harness55); bu uğursuzluq saxlanır. Son qaçış nəticəsi ayrıca aşağıda qeyd edilir.

Vizual iterasiya: ilk seçim işarəsi mətnin ölçüsünə görə oval çıxdı;32dp sabit dairəvi Box ilə düzəldilib yenidən render edildi. Eyni baxışda geri əməliyyatının ümumi pill-button vasitəsilə oval olması görüldü; onun yerinə48dp touch target daxilində40dp rounded-rectangle native Canvas oxu çəkildi. Font2-də body kəsilməsi scroll viewport-udur, statların və field-in bütünlüklə eyni anda görünməsi iddia edilmir; save/cancel pinned qalır. Shortcut mətnləri horizontal scroll tələb edir; söz ortadan sətirə bölünmür.

Font2 sheet baxışında dairədəki mətn check-in kəsildiyi görüldü. Onun yerinə fontScale-dan asılı olmayan native Canvas check çəkildi; dairə və toxunma semantikası saxlanır. Geri oxu da fontScale ilə böyüyən mətn deyil. Bunlar ayrıca düymə davranışı/animasiya tapşırığı deyil, mövcud interaction saxlanır.

Son-source: host13/13PASS (10render/input +3real orchestration synthetic HTTP regression), assembleDebug/lintDebugPASS; BUILD SUCCESSFUL40s. Kontrast4/4PASS, diff-checkPASS. Core domain dəyişmədiyi üçün əvvəlki7/7nəticəsi yenidən icra edilmiş kimi göstərilmir.15raw PNG,11testXML və sourcehash `profile-collection-polish/` altındadır. Normal/kiçik/font1/font2/inset əsas overview/editor/sheet renderləri açılıb proposal-v2 ilə müqayisə edildi; son font2 selection check artıq kəsilmir.

Təkrar yoxlama: əvvəlki review-dakı Gradle command, output `-Dwobbrik.render.output=/private/tmp/wobbrik-profile-polish`; eyni render.init.gradle və ProfileCollection*Test + ProfileOpenLatencyTest, assembleDebug/lintDebug. Bu fokuslu lokal yoxlamadır, local-ci.sh deyil.
