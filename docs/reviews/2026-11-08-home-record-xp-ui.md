# Home rekord və XP UI — 8 noyabr 2026

- Branch: `feat/home-record-xp-ui`, baza online branch `8287f26`.
- Review edilmiş implementasiya: `8287f26..a374db4`. Lokal diff review tamamlandı.
- Home server `account.bestFloors` göstərir; rekord inventardan/aktiv cəhddən çıxarılmır. Null → `Rekord —`.
- XP: `xp - start` / `next - start`, hər iki fərq BigInteger; DECIMAL64 bölməsindən sonra yalnız render payı Float. Null interval → `XP —`, bar yoxdur. DTO-nun fail-closed yoxlaması dəyişmir.
- Account səhifəsində ümumi XP saxlanır. Wallet, revision/dedup, reward və API dəyişmir. Backend sənədinin sync/attempt-read müqaviləsi yoxlandı; AppBackend dəyişikliyi tələb olunmur.

## Tapıntılar və düzəlişlər

- Köhnə preview səviyyə 7 üçün 580 XP verirdi; server qaydasına uyğun 1280 ümumi / 230 səviyyə XP-si, interval 1050..1400 ilə dəyişdirildi.
- Rekord üçün mövcud təsdiqlənmiş `account-assets-v1/ui-icons/crown.png` runtime resursuna kopyalandı; trophy alternativi saxlanılmadı.
- Səviyyə/rekord FlowRow dar pəncərə və böyük şriftdə yeni sətrə keçir. Yeni ölçü/rəng tokeni yaradılmadı.

## Yoxlamalar

- Son implementasiyadan sonra offline `:app:installDebug` PASS; build log `/tmp/wobbrik-home-ui-build.log`. Bu render üçün build/install-dır, CI deyil.
- `git diff --check` PASS.
- ui-craft kontrast: Text/Surface 13.00, Muted/Surface 8.22, Accent/Background 11.45; hamısı həddi keçir. Palitra dəyişməyib.
- Emulator debug AccountPreviewActivity renderləri: telefon 1080×2400 @420dpi (411×914dp), tablet landscape 1280×800 @160dpi, portrait 800×1280 @160dpi, font scale 1.0. Telefon legacy, zero və MaxInt64 XP/best + font scale 1.5 də render edilib görüntülərə baxıldı.
- Normal nümunə: Səviyyə 7 / Rekord 28 / 230/350 XP (65.7%). Legacy: hər iki əlavə sahə unavailable. Zero: Səviyyə 1 / Rekord 0 / 0/50 XP. Max: 15039528307/30370005000 XP (~49.5%), overflow yoxdur; rekord və XP mətni sətirə bölünür.
- Lokal şəkillər (gitignored): `docs/reviews/home-record-xp/{phone,tablet,portrait,legacy,zero,max}.png`. Telefon/tablet `*-comparison.png` təsdiqlənmiş lövhənin home hissəsi ilə yanaşı hazırlanıb baxıldı. Ölçü/density/font override-ları capture sonunda bərpa edildi.

## Vizual uyğunluq və məhdudiyyətlər

- Rekord səviyyə ilə eyni kartda, mövcud crown asset ilə bağlandı. XP həmin kartın altında server intervalı ilə göstərilir; dizayndakı illüstrativ rəqəmlər tətbiq edilmir.
- Home-un əvvəlki implementasiyası təsdiqlənmiş lövhə ilə tam uyğun deyil: telefon logo/hero/başlıq nisbətləri, boşluqlar, düymə tipoqrafiyası və səth effektləri fərqlidir. Tablet başlığı soldadır; rail və Şəhərim girişi yoxdur, header mövqeyi fərqlidir. Bunlar bu kiçik data-binding vahidində yenidən dizayn edilmədi; təsdiqlənmiş istisna sayılmır və tam vizual qəbul PASS deyil.
- 1.5 şriftdə mövcud Nailiyyətlər düyməsində söz sonu növbəti sətrə keçir; bu əvvəlki paralel düymə layout borcudur. Yeni rekord/XP mətni tam görünür. Qısa landscape, real cihaz, TalkBack və canlı server hesab axını yoxlanmayıb.
- Renderlər frozen server-shaped fixture-dir; real auth, server sync və finish-dən sonra UI yenilənməsinin end-to-end sübutu deyil. Production göstəriciləri `ready.snapshot` oxuyur, fixture production-a daxil deyil.
- CI **işlədilməyib**. Handoff tələb etdiyi kimi online branch tarixini dolayı yolla main-ə daşımamaq üçün bu vahid online branch-ə tarix saxlanılmaqla inteqrasiya edilir. Main inteqrasiyası ayrıca bütün online diff review tələb edir; yalnız ondan sonra main-də `./scripts/local-ci.sh`. Build PASS CI PASS demək deyil. Push/deploy yoxdur.
