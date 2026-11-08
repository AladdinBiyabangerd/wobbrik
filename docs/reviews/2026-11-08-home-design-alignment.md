# Home design alignment — WIP, 8 noyabr 2026

- Branch: `feat/home-design-alignment`, baza `a230b3b` (online progression).
- Scope: hazır home kompozisiyasını təsdiqlənmiş telefon/planşet lövhələrinə yaxınlaşdırmaq. Tam vizual qəbul və şəhər implementasiyası **tamam deyil**.
- Yeni `HomeScreen.kt` yalnız server GameSnapshot oxuyur; accepted start/resume callback-ləri dəyişmir. Qrafika mövcud təsdiqlənmiş runtime assetləridir, dekorativ şəhər real şəhər məlumatı kimi təqdim edilmir.
- Telefon başlıq/hero/kart/CTA; geniş pəncərədə hero + idarəetmə paneli; medium/expanded rail. Medium 600dp mövcud tablet README-dən mərkəzi tokenə keçirilib. Böyük şriftdə köməkçi düymələr ayrı sətirlərdədir; portretdə məzmun scroll olur.
- Lokal source review: home yalnız Ready + status-null olduqda yeni təqdimata keçir; bərpa/xəta mövcud təqdimatdadır. BigInteger XP fərqləri saxlanır. Domain/wallet/API dəyişməyib; bu vizual patch üçün backend dəyişikliyi yoxdur.
- Tapıntı: hazır city route yoxdur; city action aktiv olsaydı AccountScreen-in ümumi fallback-na düşərdi. Müvəqqəti deaktiv edildi, tam şəhər axını bitənədək hazır funksiya sayılmır. Inventory/market da deaktivdir. İstifadəçi yalnız deaktiv keçidlərlə işi bitirməyi seçməyib: real şəhər və tam yerləşdirmə də scope-a daxil edilib.

## Yoxlamalar

- Son kod: `:app:installDebug --offline` PASS, `/tmp/wobbrik-home-align-build.log`. Bu fokuslu build/install-dır, lokal CI deyil.
- Kontrast: Text/Surface 13.00; Muted/Surface 8.22; Background/Accent 11.45 PASS. Palitra dəyişməyib.
- `git diff --check` PASS.
- Renderlər `docs/reviews/home-design-alignment/` (gitignored). Erkən renderlərdə telefon 1080×2400 @420, planşet 1280×800 və 800×1280 @160 görüntülərinə baxıldı və müvafiq təsdiqlənmiş lövhələrlə müqayisə edildi. Header coin yeri və sağ panel eni həmin baxışdan sonra düzəldildi.
- Son kodun yekun render yoxlamasının nəticələri aşağıda əlavə edilir.

## Qalan uyğunluq / qəbul deyil

- Təsdiqlənmiş PNG-nin şrift faylı yoxdur; hazır sistem fontu, başlıq işarələri və düymə bevel/işıq effektləri hələ birəbir deyil. Alternativ dizayn təsdiqlənməyib.
- Planşet portretində Oyna/Şəhərim hazırda ardıcıl, lövhədə yan-yanadır. Tam şəhər implementasiyası ilə kompozisiya tamamlanmalıdır.
- Home ready vəziyyəti yenilənib; loading/error təqdimatı ayrıca uyğunlaşdırılmalıdır. Canlı auth/sync/start/finish, real cihaz, TalkBack yoxlanmayıb.
- Tam şəhər üçün typed mərtəbə geometry/provenance, server union/transform/containment/overlap, ərazi/dekor ölçüləri, atomik placement/move/replace/return və mobile renderer lazımdır. Mövcud backend placement_enabled:false-dır.
- Cari icra göstərişi (kotlin-compose-plan §Cari icra göstərişi) ölçülərin versiyalı oyun parametrləri kimi seçilməsinə icazə verir; köhnə “ölçü mənbəyini gözlə” qeydi buna mane deyil. Bu söhbətdə yeni ölçülər seçilməyib və backend dəyişməyib.
- Branch açıqdır, main-ə merge edilməyib; CI/push/deploy yoxdur. Bu qeyd PASS mərhələ qəbulu deyil.

## Son render baxışı

- Son kodun 1080×2400 @420 font1.0/1.5; 1280×800 və 800×1280 @160 font1.0; 1600×900 @240 font1.5 görüntülərinə baxıldı. Normal fixture-də mətn və idarəetmələr kəsilmir. Font1.5-də köməkçi düymələr ayrı sətirlərdədir. Bu beş nümunə bütün mətn/data ölçülərinin yoxlanması deyil.
- İlk avtomatik batch splash ekranını çox tez çəkmişdi; həmin fayllar `am start -W` və render gözləməsi ilə yenidən çəkilib əvəz edildi. Emulator override-ları reset edildi, font1.0-a qaytarıldı.
- Yan-yana phone/tablet/portrait comparison faylları hazırlanıb. Qalan fərqlər yuxarıdadır; tam vizual PASS deyil.

## Davam düzəlişi — 8 noyabr 2026

- Yoxlanmış diff: `3115e88` üzərində HomeScreen.kt və OnlineGame.kt düzəlişi; branch `feat/home-design-alignment`. Branch hələ açıqdır; bütün online baza bu lokal review-un əhatəsi deyil.
- İstifadəçi sistem şriftini razılaşdırılmış istisna kimi saxladı. Şrift faylı axtarışı artıq bloklayıcı deyil; digər vizual fərqlər avtomatik təsdiqlənmir.
- Başlıqdakı font chevron-ları təsdiqlənmiş nümunədəki iki ayrı şüa ilə əvəz edildi. Mövcud Accent və ölçü tokenləri istifadə olunur.
- Portret medium/expanded pəncərədə Oyna/Şəhərim yan-yana və eyni minimum hündürlükdədir; fontScale > 1 zamanı ardıcıl qalır. Şəhər ikonu ui_city oldu.
- Əsas düyməyə mövcud Text/Accent tokenləri ilə daxili işıq kənarı əlavə edildi. Bu PNG-dəki bütün işıq/bevel piksellərinin eyniliyi iddiası deyil.
- Home loading/error da eyni adaptiv çərçivəyə keçirildi; snapshot yoxdursa balans `—`, gameplay/statistics göstərilmir, status/retry saxlanır. Server authority və callback-lər dəyişmir.
- Lokal review tapıntısı: ilk error/loading renderində Yenilə düyməsində oyun üçbucağı görünürdü; play işarəsi ayrıca parametrə çıxarılıb yalnız gameplay girişində saxlandı.
- Backend müqaviləsi/domain/schema dəyişməyib; bu təqdimat düzəlişi üçün AppBackend dəyişikliyi tələb olunmur. Tam şəhər implementasiyası üçün bu nəticə keçərli deyil.

### Fokuslu yoxlama və məhdudiyyətlər

- `:app:installDebug --offline` PASS; `/tmp/wobbrik-home-followup-build.log`. `git diff --check` PASS. Lokal CI deyil.
- Kontrast yenidən ölçüldü: Text/Surface 13.00, Muted/Surface 8.22, Background/Accent 11.45 PASS; palitra dəyişməyib.
- `home-design-alignment/final-{phone,portrait,landscape,large,error,loading}.png` renderlərinə baxıldı. Ready: 1080×2400 @420 font1.0, 800×1280 @160 font1.0/1.5, 1280×800 @160 font1.0. Error: telefon; loading: geniş planşet. Override-lar reset, əvvəlki font bərpa edildi.
- Mobile-primary-v2, tablet portrait və primary-landscape ilə müqayisə: işarə və portret CTA düzülüşü düzəlib; sistem fontu təsdiqlənmiş istisnadır. Runtime asset detalları, dəqiq nisbətlər, ikon miqyası, fon/işıq effektləri və əlavə XP sətrinin kart hündürlüyünə təsiri hələ birəbir deyil. Tam dizayn qəbulu verilmir.
- Böyük font renderində aşağı köməkçi məzmun scroll altındadır; bu capture scroll sonunu yoxlamır. Yeni lokal/real cihaz/TalkBack/canlı sync-start-finish yoxlaması aparılmayıb.
- Şəhər/İnventar/Bazar bağlıdır; tam placement scope-u saxlanır və yerinə yetirilməyib. Main merge/CI/push/deploy yoxdur.

## İkinci davam — proporsiya və miqyas düzəlişi, 8 noyabr 2026

- Branch `feat/home-design-alignment`; baxış bazası `69bc4fd`. Yalnız bu davamın diff-i lokal yoxlanır; bütün online baza review edilmiş sayılmır.
- Logo hündürlüyü artırıldı; geniş pəncərədə səhnə/panel eyni en payını alır, idarəetmə yuxarıdan hizalanır. Planşet başlıq/statistika/ikon/düymələr mövcud Title/Score/Target tokenləri ilə böyüdü; portret CTA-lar yenə yan-yana və eyni minimum hündürlükdədir. Böyük şriftdə ardıcıl təqdimat saxlanır.
- XP rəqəmləri və BigInteger hesabı saxlanır. Kartın daxili boşluğu və XP mətni canonical CSS-dəki `--space-1`/`--type-caption` ilə yığcamlaşdırıldı; GameTokens həmin mövcud tokenləri ixrac edir. Material3 progress son nöqtəsi çıxarıldı. Əsas CTA üçün mövcud Text/Accent rənglərindən işıq gradienti tətbiq edildi.
- Lokal source review: yeni domain/data qaydası yoxdur; snapshot/callback və auth authority dəyişmir. Rail minimum hədəfi 56dp, digər düymələr ən az 48dp-dir; data uzunluğunda FlowRow və scroll saxlanır. Bu təqdimat diff-i AppBackend dəyişikliyi tələb etmir; şəhər scope-u üçün bu nəticə tətbiq edilmir.
- Fokuslu `:app:installDebug --offline` PASS (`/tmp/wobbrik-home-refinement-build.log`); ara build-də named/positional argument səhvi düzəldildikdən sonra son build keçdi. CI deyil. `git diff --check` PASS; Text/Surface 13.00, Muted/Surface 8.22, Background/Accent 11.45 PASS.
- Son layout renderləri: 1080×2400 @420 font1.0; 1280×800 və 800×1280 @160 font1.0; 800×1280 @160 font1.5; MaxInt64 rekord/XP 1080×2400 @420 font1.5; 900×450 @240 font1.5. Error telefonda, loading geniş planşetdə. `refined-*.png` görüntülərinə və phone/landscape/portrait yan-yana müqayisələrinə baxıldı. Böyük şrift və max nümunələrdə scroll-dan sonra aşağı düymələr görünür; qısa landşaftda əlavə swipe-larla son Kolleksiya/Nailiyyətlər də yoxlandı (`refined-short-bottom.png`). Override-lar əvvəlki vəziyyətə qaytarıldı.
- Tam vizual PASS deyil: şəhər/logo/ikon runtime detalları və səth effektləri nümunənin piksel eyniliyi deyil; XP nümunəyə əlavə real məlumatdır, onun kart hündürlüyünə təsiri tam yox olmur. Sistem fontu əvvəlki təsdiqlənmiş istisnadır. Şəhər/İnventar/Bazar hələ bağlıdır. Real cihaz/TalkBack/canlı auth-sync-finish yoxlanmayıb. Main merge/CI/push/deploy yoxdur.
- İstifadəçi bu davamda home şəhərini təsdiqlənmiş lövhədən ayırmağı açıq təsdiqlədi. Built-in imagegen ilə şəffaf cutout hazırlanır; nəticə və Android inteqrasiyasının son yoxlaması aşağıda qeyd ediləcək. Bu icazə alternativ şəhər dizaynı icazəsi deyil.

### Şəhər cutout namizədi — runtime-a qəbul edilmədi

- İstifadəçinin lövhədən ayırma təsdiqi ilə built-in imagegen background-extraction çağırıldı; edit target `docs/design/mobile-v1/mobile-primary-v2.png`, yalnız soldakı home şəhəri, şəffaf fon, bütün bina/mərtəbə/mövqe/detal invariants saxlanılması tələb edildi. CLI/API fallback istifadə edilmədi.
- Çıxış: `docs/reviews/home-design-alignment/home-city-imagegen-candidate.png` (gitignored QA). Generativ alət orijinal pikselləri qoruyan cutout əvəzinə yeni rekonstruksiya verdi; bu orijinal piksel cutout-u deyil. Baxıldı, runtime-a bağlanmadı; əvvəlki asset qorunur.
- Piksel qoruyan lokal Python maskalaması üçün ayrıca sual göndərildi. İstifadəçi həmin üsulu ayrıca açıq təsdiqlədi; aşağıdakı piksel qoruyan çıxış bununla hazırlandı. Əvvəlki təsdiq tam şəhər scope-unu azaltmır.

- Dəqiqləşdirmə: ilk baxışda “əlavə çətir” kimi qeyd edilmiş detal orijinalda da var; həmin konkret fərq iddiası yanlışdır və çıxarıldı. Generativ nəticənin original pixel extraction olmaması ayrı səbəb olaraq qalır.
- Qəbul edilən texniki asset: `docs/design/account-assets-v1/home-city-cutout-v2.png` və eyni runtime PNG. Orijinal `(20,154,435,605)` crop, 415×451 RGBA; RGB üzrə ImageChops fərqi sıfır, alpha 0/255. Tünd/açıq fon QA baxıldı. Reproduksiya script-i source SHA256 yoxlayır; OpenCV runtime asılılığı yoxdur, committed alpha maskası + Pillow kifayətdir. Köhnə assetlər saxlanır. Bu ölçü mənbə lövhənin keyfiyyət həddidir, süni upscale edilmir.
- HomeScreen yeni cutout-u oxuyur; portretdə intrinsic aspect ratio saxlanır. Bu real hesab şəhər renderer-i deyil.

### Son cutout kodunun render və diff baxışı

- İlk cutout renderi şəhəri intrinsic piksel ölçüsündə kiçik göstərirdi; `fitWidth` təqdimatı painter-in real aspect ratio-sundan hündürlük hesablayır. Bu tapıntı düzəldildi; son `:app:installDebug --offline` PASS (8s, 67 task; `/tmp/wobbrik-home-refinement-build.log`).
- Son kodla bütün `refined-{phone,landscape,portrait,large,max,short,error,loading}.png` və large/max/short scroll renderləri yenidən çəkildi. Phone, landscape və portrait comparison faylları da yeniləndi və görüntülərə baxıldı. Şəhər artıq mənbə formasıdır, lakin aşağı resolution-un yumşaqlığı görünür. Portret normal planşetdə köməkçi düymələr ekran altına keçir; scroll qalır, böyük şrift nümunəsinin scroll sonunda hər ikisinə çatıldı. Qısa landşaftın son köməkçi düymə yoxlaması layout dəyişmədən əvvəlki `refined-short-bottom.png`-dədir; son kodun bir swipe renderi bütün scroll sonunu göstərmir.
- Son source review: server binding, BigInteger XP hesabı, legacy null davranışı və callback-lər saxlanıb; alpha çıxışı üçün source hash və RGB invariantı yoxlanıb. Bu diff-də əlavə həll olunmamış funksional tapıntı görünmədi; bu bütün online bazanın auditi deyil. `git diff --check` və kontrast yenidən PASS. Source extractor yenidən PASS; runtime PNG dizayn çıxışı ilə eynidir.
- Bu davam home artwork/layout vahidini irəlilədir, tam home dizayn qəbulu deyil. Tam şəhər yerləşdirməsi başlanmayıb və icra icazəsi qüvvədədir. AGENTS.md-dəki kontekst artımı qaydasına görə növbəti böyük backend/Android şəhər vahidi üçün handoff yeniləndi. Main merge/CI/remote push/deploy yoxdur.

- Bu davamda lokal baxışdan keçmiş implementasiya commit-i: `44946d8` (`69bc4fd..44946d8`). Sonrakı review/handoff commit-i yalnız sənəddir; main gate işə salınmayıb.
