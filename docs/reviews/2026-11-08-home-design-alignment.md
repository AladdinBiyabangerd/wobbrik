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
