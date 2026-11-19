# Uğurlar — podium art və chrome davamı

19 noyabr 2026. `feat/achievements-approved-native`, baza `82e6ca0`; yoxlanmış working diff. Əvvəlki native inteqrasiyanın davamıdır. Şəhər owner işi, baselineProfiles və data/cache/authority mənbələri bu vahiddə dəyişdirilmədi. Commit/merge/CI/push/deploy yoxdur.

## Dəyişiklik

`GameTokens.RecordsPodium` təsdiqli proposal-dakı daş, bağ və bürünc materiallarını adlandırır. `MobileRecordsPresentation.kt` podiuma native daş platforma, tikişlər, yan kol/çiçək qrupları, üç metal üzrə daraldılmış kürsü, alt rim, formalı dəfnə yarpaqları, avatar ətrafı görünən laurel, birinci yerin radial işığı və yumru gradient ad lövhələri əlavə edir. Üçüncü yer artıq Danger/pink deyil, ayrıca bürüncdür. Ad/rank/xal/real seçilmiş avatar native və əvvəlki server məlumatıdır. Bütöv PNG/crop skin və yeni raster yoxdur; garden yalnız dekorativ Canvas-dır.

`InventoryCollectionChrome.kt` yalnız `achievementTabs` üçün14sp Label və ağ ikon istifadə edir; aktiv cyan underline saxlanır. Digər tab qruplarının font/ikon seçimi dəyişmir. Ortaq təsdiqli collection header/logo/navbar saxlanır; ayrıca alternativ header yaradılmır.

## Lokal review

Dəyişiklik podium/chrome ilə məhduddur. Domain, API, DTO, wallet, accepted progress/award, account revision/login epoch, request cancellation və display cache dəyişmir; AppBackend dəyişiklik tələb etmir. Garden sabit bounded draw loop-larıdır; network/disk/bitmap decode və frame state yoxdur. Podiumun ölçüsü əvvəlki measured-fit mexanizminə daxildir: üç oyunçu bir dəfə sayılır, list rank4-dən davam edir, az sahə/font2 üçün list fallback saxlanır. Yeni read/polling/prewarm yoxdur.

İlk renderdə enli kürsülər yan bağ dekorunu örtürdü; kürsü eni və dekor kənarları düzəldildi. İlk explicit contrast yoxlamasında qızılı ad lövhəsi4.41 idi; gradient55% mövcud Background-a tündləşdirildi. Son contrast log8/8PASS, minimum5.36. `contrast.css` Kotlin rənglərinin resolved surətidir; badge rəngləri45%metal+55%Background-dır, ayrıca məhsul palitrası deyil. Score/rank ink üçün gradientin mərkəzi açıq stop-u ölçülür; tünd kənarlar yazı arxasına düşmür.

## Sübut və sərhəd

`achievements-podium-native/` son-source render/checks/contrast sübutlarını saxlayır. Normal/kiçik portret390×844/320×568, fontScale1/2,360×640font2+24/48simulated inset və uzun mətn/xal fixture istifadə edilir. Native host synthetic accepted backend-dir; cihaz/API24/ART/GPU/frame/IME/TalkBack/locale təsdiqi deyil. Emulator açılmır.

Approved PNG ilə son müqayisə `comparison.jpg`-dədir. Daş/bağ bazası, kürsü və badge/light detalları artıq implementasiyadadır; native Canvas dekorunun üzvi teksturası generativ PNG ilə piksel eyni deyil. Sample üz/ad/rəqəm/sətir sayı real profil/viewport qaydalarını əvəz etmir. Ortaq header/logo və native ikonlar təsdiqli collection komponentidir; PNG-nin generativ tipoqrafiyası rasterlə kopyalanmır. Tam app redesign və mərhələ5 qəbulu iddia edilmir.

Son-source host5/5PASS. Render matrisi açılıb baxıldı: normalda podium və rank4–7 sətirləri görünür; kiçikfont1/font2-də ölçüyə görə siyahı fallback işləyir; inset/uzun mətn font2-də ilk oyunçu, son award isə scroll sonunda görünür. Normal cold1request/1130response-bodybyte, digər fixture-lər1request/696–713byte; warm tab/scope0əlavəHTTP. Əvvəlki eyni normal fixture2request/1391byte idi; daha hündür dekor podiumu visible read budget-ni8-dən7-yə endirir, bu ayrıca sürət optimizasiyası iddiası deyil. Font2/kiçik oxu davranışı dəyişmir. İlk metadata host19–163ms, warm route40–72ms; production/ART/frame ölçməsi deyil.

Son-source `:app:assembleDebug` və `:app:lintDebug` PASS (`BUILD SUCCESSFUL`); `git diff --check` PASS. Fokuslu host render/build/lint işlədilib, CI deyil. Aralıq compile yoxlamasında Compose padding overload-u uyğun deyildi; ayrılmış padding çağırışları ilə düzəldildi və son yoxlamalar yeniləndi. Təkrar əmr əvvəlki review-un eyni render init/test harness-idir, yalnız `-Dwobbrik.render.output=/private/tmp/wobbrik-achievements-podium` istifadə olunur.
