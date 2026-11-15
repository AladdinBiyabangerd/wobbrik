# Profil v2 — təsdiqlənmiş mobil istinadlar

15 noyabr 2026: istifadəçi profil, redaktə və avatar seçimi görünüşlərini açıq təsdiqlədi və tətbiqini tapşırdı.

- `profile-approved.png`: istifadəçinin geri göndərdiyi vahid panel + tac lentli profil. Sonradan təklif edilmiş lentsiz variant deyil.
- `edit-avatar-approved.png`: Profili dəyiş və ayrıca avatar seçim pəncərəsi.
- `avatar-grid-approved.png`: çox avatar halında scroll viewport + sabit başlıq/təsdiq prinsipi. Əlavə personajlar yalnız nümunədir; server kataloquna hüquq və ya yeni avatar ID-si əlavə etmir.

Nümunələr built-in ImageGen ilə bu chatdə yaradılıb. İnternetdəki Royal Match/Monopoly profil axınları yalnız kompozisiya araşdırması idi; xarici oyun asset-i tətbiqə köçürülmür. Profil art-ı və redaktə lövhəsi production drawable-nodpi daxilində dəyişdirilməmiş PNG kimi saxlanır; source düzbucaqlıları GameTokens.Profile-dədir. UI mətnləri/əməliyyatlar real Compose komponentləridir.

Nümunə ad, səviyyə, XP, balans və rekord authority deyil. Real UI hesab snapshot-u və PublicProfile istifadə edir. XP intervalı serverdən gəlir; yeni formula uydurulmur. Nümunədəki `+28/+580` yeni mükafat kimi göstərilmir. Oyunçu yalnız mövcud builder/pilot/architect ID-lərindən seçir.

Bu istinadlar əvvəlki account-states profil dizaynını həmin ekran üçün əvəz edir. Settings-390 və rekordlar fitted dizaynları dəyişmir. Yeganə plan `docs/PLAN.md`-dir. Mobil portret; planşet ayrıca tapşırıqdır.

## Native profil tipoqrafiyası — 15 noyabr P1 düzəlişi

İstifadəçi şrifti agentin tapıb hazırlamasını tapşırdı. Profil üçün rəsmi Google Fonts `ofl/nunito/Nunito[wght].ttf` mənbəyindən Nunito 400/700/900 statik çəkiləri hazırlanıb; dəyişiklik yalnız profil təqdimatına tətbiq olunur. Azərbaycan `ƏəĞğİıŞşÇçÖöÜü` glyph-ləri cmap-də yoxlanıb. Statik instansiyalar API24-də variable-font dəstəyindən asılı deyil; API24 runtime yoxlaması iddia edilmir. Lisenziya app assets/licenses/Nunito-OFL.txt-də paylanır.

Mənbə: https://github.com/google/fonts/tree/main/ofl/nunito — Nunito generativ PNG-dəki naməlum şriftin dəqiq identifikasiyası deyil; istifadəçinin “özün tap hazırla” qərarı əsasında yumru native yazı üçün seçilib. PNG-lər dəyişmir.

Font mənbəyinin SHA-256: `bb55a5ca5c2042335b3991af27c4d0705d0ef41cac6164ac737fd8f2a1e85207`. Statik fayllar fontTools4.68.0 `instantiateVariableFont(..., {"wght": 400|700|900})` ilə yaradılıb. Bu alət app dependency-si deyil; app yalnız hazırlanmış TTF-ləri istifadə edir.

## Mətnsiz skin — 15 noyabr 2026, istifadəçinin ayrıca göstərişi

İstifadəçi mətnsiz skin-i agentin hazırlamasını, təsdiqli dizaynı dəyişmədən lent/stat/navbar və input capture fərqlərini düzəltməyi tapşırdı. Əvvəlki “clean skin asset-i çatışmır” statusu artıq cari blok deyil.

Built-in ImageGen edit rejimi ilə eyni898×1752 kompozisiyadan content silindi; orijinal `profile-approved.png` dəyişdirilmədi. Rəng, mottled tekstura, relyef, tac lenti, çərçivə və yerləşmə əsas götürüldü; yeni dizayn/alternativ kompozisiya yaradılmadı. Generativ inpaint örtülü fonun tarixi piksellərinin dəqiq bərpası iddiası deyil.

- `profile-skin-text-free-v1.png`: ilkin mətnsiz content extraction; production-a qoşulmur.
- `profile-skin-clean-v2.png`: boş identity kartı/navbar/wallet skin-i. Production byte-identical nüsxə: `android-billing/app/src/main/res/drawable-nodpi/profile_skin_clean.png`. SHA256 `04c6b9cb360e235b2e0b46276882b81e4dcf741574c512b6e17ec2c4b6e8e473`.
- `profile-panel-clean-v3.png`: bütöv boş panel interior-u və tac lenti; əlavə identity kartı silinib ki, dəyişən hündürlükdə təkrar baked kart çıxmasın. Production nüsxə: `android-billing/app/src/main/res/drawable-nodpi/profile_panel_clean.png`. SHA256 `67265b0470997348ff5617bc45512afe6ddd452b68f6a3b763d15d7dbaf996b5`.

Tam prompt-lar və input ardıcıllığı: [skin-generation-prompts.md](skin-generation-prompts.md). CLI/API fallback istifadə edilmədi. Oyunçu, mətn və iqtisadi dəyərlər skin-ə bişirilmir; native real məlumat/callback-lər üstündən çəkilir. Son yoxlama və viewport müqayisəsi profil-v2 review-un son bölməsindədir.
