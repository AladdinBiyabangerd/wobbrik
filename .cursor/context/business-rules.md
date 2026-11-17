# Business rules

Ətraflı mənbə: `docs/PLAN.md`; server sərhədi: `docs/PLAN.md` §2.

- Bütün xərclənən sikkələr vahid AppBackend/PostgreSQL wallet/ledger-dədir. Client delta, Room və Firestore ikinci authority deyil.
- Online cəhdin reward/progression/checkpoint nəticəsi server tərəfindən qəbul edilir; revision/dedup, retry və hesab izolyasiyası qorunur.
- 13 noyabr son qərarı: oflayn nəticələr lokal saxlanıb sonradan serverə göndərilməlidir; qəbuladək sikkə/XP/nailiyyət/bina/leaderboard təsdiqlənmir. 14 noyabr: terminal intent növbəsi/recovery replay əlavə edilib; yeni sessiyaya bağlı lokal Play və accepted snapshot+kataloq/revision-scoped read-only inventar/detail/şəhər baxış keşi branch-də inteqrasiya edilib; əvvəldən oxunmamış/köhnə baxış offline uydurulmur, mutation bağlıdır.
- Minimum 10 uğurlu mərtəbə saxlama/satış üçün uyğundur; könüllü “Binanı tamamla” yoxdur. Bir cəhd bir yekun bina və bir dəfə nəticə mükafatı yaradır.
- Kosmetika fiziki üstünlük vermir. Alınan bina hissələri hesabda açmır; alınan bina təkrar satılmır. Detallar məhsul planındadır.
- Placement typed geometry/provenance və server containment/overlap yoxlamaları hazır olmadan açılmır.
- Hazır olmayan domain adapteri, demo, fixture, session-only nəticə və placeholder yekun/live oyun kimi göstərilmir.
- Vizual mənbə: `docs/design/unified-mobile-v3/README.md`; native Compose layout və navbar/vector ikonları, yalnız mürəkkəb qrafika üçün raster. Mobil portret; planşet ayrıca tapşırıqdır.
- Mövcud tokeni tap; ui-craft istifadə et; telefon/planşet renderlərini dizaynla yanaşı müqayisə edib bax. Böyük şrift/inset/keyboard/a11y və lazımi scroll qorunur.
- Real SKU, Console, remote push və deploy ayrıca göstərişədək əhatədən kənardır.

- 13 noyabr cari vəziyyəti: məcburi Google entry və guest/link çıxarılması hər iki main-dədir. Anonymous fallback, wallet merge və data deletion yoxdur. Canlı yoxlama tələb etmə, emulatoru avtomatik açma.

- 15 noyabr lokal test istisnası: ayrıca `mockDebug` (`com.aladdin.wobbrik.mock`, Wobbrik Mock) Google-sız sintetik profil/lokal adapter istifadə edir. Real backend və wallet deyil; adi debug/release Google tələbini saxlayır. Mock source release-ə daxil edilmir.
