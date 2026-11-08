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
