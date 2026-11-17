# Home — native komponent və ikon vahidi

- Branch: `feat/home-frameless-responsive`; yoxlanan diff cari Home/navbar/icon/token dəyişiklikləridir, uncommitted. Əvvəlki owner Home/shell/City/mock/baselineProfiles və sənəd işləri saxlanıb; commit/merge/push/CI yoxdur.
- Authority: `docs/design/unified-mobile-v3/home-approved-frameless.png`, PLAN §1.1. Bu yalnız Home vahididir; başqa ekranların redesign-ı və mərhələ qəbulu deyil.
- Dəyişiklik: `MobileMenuIcon.kt` Canvas ilə home/city/inventory/market/trophy/profile/play və Home coin/crown ikonlarını çəkir. Navbar və Home statlarında raster ikon çağırışları çıxarıldı. Mövcud native gradient səthlər, real accepted snapshot, XP semantics, route və Play callback-ləri saxlanıb. Şəhər illüstrasiyası və mürəkkəb logo raster asset olaraq qalır; layout/mətn/idarəetmə raster deyil.
- Home: header üst boşluğu, başlıq pilləsi, illüstrasiyanın source aspect nisbəti və hündür pəncərədə stat mövqeyi tənzimləndi. 360/390dp-də navbar label pilləsi sözün Play-a daşmasını aradan qaldırır. 320dp-də mövcud iki sıra native navbar və böyük şriftdə accessibility adları saxlanır; dekorativ hero yer çatmadıqda gizlənə bilir.
- Lokal review: input/selected/enabled semantics və source istifadəsinə baxıldı. Backend/API/DTO/domain/wallet dəyişmir; AppBackend dəyişikliyi və backend testi tələb olunmur.
- Checks: son native Home7 + shell4 = 11/11 PASS (59s). Home 320/360/390 font1, 320 font2, 360 font2 simulated inset, uzun balans/XP və recovery input yoxlanır. Native PNG-lərə baxıldı; müqayisə `home-native-v4/approved-vs-native.png`. Adi app unit + debug/mock APK + lintDebug PASS (56s); bundan sonra yalnız istifadə olunmayan köhnə HeroAspect sabiti silinib, son native tur source-u yenidən compile edib. Kontrast 4/4 PASS, minimum7.66; diff whitespace PASS.
- İlk son-render turu adi Gradle ilə paralel işə düşdüyünə görə test classpath-də NoClassDefFound/ClassNotFound ilə FAIL oldu. Normal tur bitdikdən sonra ardıcıl native tur 11/11 PASS oldu. İlkin uğursuz tur PASS sayılmır.
- Vizual fərqlər: PNG-nin xırda səth teksturası/parıltısı və ikonların bəzi kontur detalları tam eyni deyil. Şəhər raster asset-i təsdiqli mənbədən əvvəl hazırlanmış ayrıca art-dır; yenisi yaradılmadı. Navbar istifadəçi qərarına əsasən safe bottom-a bağlıdır, PNG-dəki alt boşluq təkrarlanmır. Tam piksel uyğunluğu və istifadəçi qəbulu iddia edilmir.
- Məhdudiyyət: render sintetik hesabla production composable-dır, real hesab/device E2E deyil. TalkBack/IME/API24runtime/GPU/locale/performance matrisi yoxlanmayıb. Emulator açılmayıb; planşet/landşaft bu scope-da deyil.

## İstifadəçi təsdiqi və düymə dolğunluğu

İstifadəçi Home-un indiki görünüşünü qəbul edib, yalnız düymələrə bir qədər dolğunluq istədi. Oyna dairəsinə native shadow, yüngül alt kənar və üst rim işığı; selected düyməyə daha dolğun gradient/shadow əlavə edildi. Layout və callback-lər dəyişmədi. İlk alt kölgə renderdə sərt göründü, opacity və stroke yüngülləşdirildi. Son navbar crop-u `home-native-v4/navbar-depth.png` açılıb baxıldı. Son Home7/7 native, debug/mock APK və lintDebug PASS36s; diffcheckPASS. Shell4 əvvəlki turda keçib, bu kiçik relyef düzəlişindən sonra təkrarlanmayıb. Rəng palitrası dəyişmir. Commit/merge/push/CI yoxdur.

## Yazısız play — son istifadəçi düzəlişi

“Oyna” yazısı çıxarıldı, native play işarəsi `Target - Small` (40dp) oldu; dairə64dp, contentDescription və callback/target saxlanır. PLAN və dizayn README sinxronlaşdırıldı. Son Home7/7 native və debug/mock APK PASS17s; navbar crop-u açılıb baxıldı, diffcheckPASS. Lint əvvəlki relyef turunda keçib, bu kiçik düzəlişdən sonra təkrarlanmayıb. CI/commit/merge/push yoxdur.
