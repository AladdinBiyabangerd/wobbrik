# Bazar native — PNG fərqlərinin düzəlişi

19 noyabr 2026. `fix/market-approved-native`, baza `a98272c`, working diff. İstifadəçi PNG ilə eyni olmayan görünən hissələrin düzəldilməsini istədi. Əvvəlki owner dirty işi saxlanır; merge/CI/push/deploy yoxdur.

## Bu vahid

- Yalnız Bazarın iki tab zolağı mövcud Small radius-u ilə daha yığcam künclərə keçir; Inventory default-u saxlanır.
- Normal bina grid art sahəsi160→144dp; ad sətiri24→16sp, mərtəbə sətiri24→14sp line-height. Body16/Caption12 font ölçüləri saxlanır. Normal kart əvvəlki təxminən284dp-dən258dp-yə yığcamlaşır. Art-ı120dp-yə endirən aralıq variant həddindən artıq kiçik görünərək qəbul edilmədi.
- Normal dekor art120→96dp. Öz elanlarında normal art/divider144→120dp; sağ sütunun minimum hündürlüyü məlumat və48dp input-dan gəlir, onu zorla kəsmirik.
- Süzgəc gövdəsinin əvvəlki16dp radius-u32dp gövdədə dairə yaradırdı; Refresh ilə eyni8dp künc tətbiq edilir. Bütün dəyərlər əvvəlcədən mövcud GameTokens-dandır.
- Host capture warm draw-dan sonra Compose descendant qatlarını yenidən invalidate edir. Əvvəlki capture-da loqo/balans/navbarın boş çıxması vizual sübut sayılmadı. Son normal üç kökdə bu qatlar görünür.

## Yoxlama və sərhəd

Son source ilə beş route/render ssenarisi (390×844,320×568 font1/2;360×640 font2+simulated inset/uzun mətn/balans) PASS; alış/cancel, dekor filtr/select, elan/new/withdraw və fresh confirmation yoxlamaları həmin ssenarilərdədir. Dəyişən kod presentation-dır; repository/domain/API/schema dəyişmir, backend dəyişməsi lazım deyil. Cache suite və core suite bu vahiddə təkrar işə salınmayıb; əvvəlki nəticələr yeni run kimi göstərilmir.

`market-collection-followup/comparison.jpg`, `regular.jpg` və `responsive.jpg` açılıb baxıldı. Son screenshot-lar eyni qovluqdadır. Reference üst sırada əvvəlki comparison-dan dəyişdirilmədən götürülüb; yeni native host aşağıdadır. Tam piksel pariteti hələ yoxdur: dekor filter/input hündürlüyü, native ikon siluetləri, real bina art-ının kompozisiyası,48dp input və kiçik/font2 adaptasiyası PNG-dən fərqlənir. Fixture2dekor/4elan olduğu üçün6dekor/2elan sıxlığı kopyalanmır. Növbəti vizual iş daha çox kart/ikon/filter kompozisiyası üzərində olmalıdır; real kataloq/geometry dəyişdirilməməlidir.

Kiçik enli navbarın2sıra/icon-only görünüşü və böyük şriftdə labels-in gizlənməsi mövcud ortaq navbar adaptasiyasıdır; capture xətası deyil. Font2 tab/uzun ad/balans horizontal scroll saxlanır; sonrakı lazy kartlarda art loading və capture kəsilməsi tam görünüş sübutu sayılmır. Emulator/device/ART/GPU/IME/TalkBack yoxlanmayıb; host render production frame ölçməsi deyil. Performans optimallaşdırması iddiası yoxdur, şəbəkə/sorğu yolu dəyişmir.

Palitra dəyişməyib. Mövcud scope-extracted palette üzərində contrast.py --fail-under4.5:15/15 uyğun cütlük PASS (minimum7.41). Son build/lint nəticəsi və XML/loglar sübut qovluğuna əlavə edilir. CI işlədilmir.

Son yoxlama: `:app:assembleDebug :app:lintDebug` PASS;5/5host PASS,0failure/error/skipped; `git diff --check` PASS.
