# Business rules

Ətraflı mənbə: `docs/game-plan.md`; server sərhədi: `docs/kotlin-compose-plan.md` §5.

- Bütün xərclənən sikkələr vahid AppBackend/PostgreSQL wallet/ledger-dədir. Client delta, Room və Firestore ikinci authority deyil.
- Online cəhdin reward/progression/checkpoint nəticəsi server tərəfindən qəbul edilir; revision/dedup, retry və hesab izolyasiyası qorunur.
- Offline məşq sikkə, XP, nailiyyət, satılan bina və leaderboard nəticəsi yaratmır.
- Minimum 10 uğurlu mərtəbə saxlama/satış üçün uyğundur; könüllü “Binanı tamamla” yoxdur. Bir cəhd bir yekun bina və bir dəfə nəticə mükafatı yaradır.
- Kosmetika fiziki üstünlük vermir. Alınan bina hissələri hesabda açmır; alınan bina təkrar satılmır. Detallar məhsul planındadır.
- Placement typed geometry/provenance və server containment/overlap yoxlamaları hazır olmadan açılmır.
- Hazır olmayan domain adapteri, demo, fixture, session-only nəticə və placeholder yekun/live oyun kimi göstərilmir.
- Vizual mənbələr: `docs/design/mobile-v1/`, `docs/design/game-tap-v2/`, `docs/design/tablet-v1/`; şəhər üçün `docs/design/README.md`. Eyni ekranın son təsdiqlənmiş versiyası əsasdır. Sxematik əvəzetmə və icazəsiz redesign olmaz.
- Mövcud tokeni tap; ui-craft istifadə et; telefon/planşet renderlərini dizaynla yanaşı müqayisə edib bax. Böyük şrift/inset/keyboard/a11y və lazımi scroll qorunur.
- Real SKU, Console, remote push və deploy ayrıca göstərişədək əhatədən kənardır.
