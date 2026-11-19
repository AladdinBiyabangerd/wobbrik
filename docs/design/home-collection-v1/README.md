# Ana səhifə — kolleksiya üslubunda təklif

19 noyabr 2026. İstifadəçi `proposal.png` nümunəsini təsdiqləyib Kotlin + Compose implementasiyasını tapşırdı. Bu PNG Home üçün əvvəlki frameless authority-ni əvəz edir. Yeganə icra planı `../../PLAN.md` §1.1-dir.

Built-in ImageGen istifadə edilib. Prompt `generation-prompt.txt`-dədir. İstinadlar: Profil `../profile-collection-v1/proposal-v2.png`, Bazar `../market-collection-v1/proposal.png`, əvvəlki Home `../unified-mobile-v3/home-approved-frameless.png`. GameTokens.InventoryCollection və mövcud ölçü pillələri oxunub. Ortaq skyline/logo/balans header-i, tünd mavi səthlər, cyan vurğu və Ana aktiv yeddi girişli navbar əsas götürülüb. Mövcud şəhər hero-su, səviyyə, rekord və XP məzmunu saxlanır; rəqəmlər nümunədir.

Generativ nəticə açılıb vizual yoxlanıb: yeddi navbar slotu və Play-də yalnız üçbucaq görünür. Generativ PNG dəqiq native ölçü/kontrast, kiçik ekran, fontScale2 və runtime sübutu deyil. Kod, backend və API dəyişdirilməyib; build/CI/native render aparılmayıb. Təsdiqdən sonra UI native Compose ilə qurulmalıdır, bütöv PNG skin kimi istifadə edilmir.

Native tətbiq və son yoxlamalar: `../../reviews/2026-11-19-home-collection-native.md`. Yuxarıdakı kod/build yoxlanmayıb qeydləri ilkin PNG hazırlanma mərhələsinə aiddir. Son implementasiya ayrıca feature branch-dədir.
