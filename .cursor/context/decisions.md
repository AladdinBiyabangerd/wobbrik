# Decisions

- Əsas workspace: `/Users/mac/My Workspace/Wobbrik`; digər worktree-lər üçün `git worktree list` yoxla. Remote: `https://github.com/AladdinBiyabangerd/wobbrik.git`; remote mövcudluğu push icazəsi deyil.
- Yeganə aktual plan `docs/PLAN.md`-dir; handoff yalnız `docs/context/current-task.md`, review-lar tarixi sübutdur.
- 4 noyabr: Kotlin + Compose ilə yekun Android oyununun implementasiyası, minSdk 24 / target+compileSdk 36 təsdiqlənib; köhnə “kod icazəsi yoxdur” qeydləri qüvvədən düşüb.
- PostgreSQL vahid authority; Firebase Auth kimlik; AppBackend DB qəbul vaxtı (3 noyabr qərarı). Server sərhədi vahid plan §2-də, dəqiq API backend müqaviləsindədir.
- Hər məntiqi vahid `feat/`, `fix/`, `chore/`, `docs/` branch → kiçik commit → lokal diff review → tarix saxlanılmaqla main merge → yalnız istifadəçi ayrıca CI tələb edəndə main-də `./scripts/local-ci.sh`. Uğursuzluq ayrıca fix branch/review/merge/main CI tələb edir.
- Remote push/deploy/Console/real SKU avtomatik deyil. Növbəti mərhələ yalnız ayrıca istifadəçi tapşırığı ilə başlayır.
- 8 noyabr: şablon mövcud qaydalara uyğunlaşdırılır; `docs/context/current-task.md` canonical qalır. Cursor giriş faylı onu göstərir; paralel handoff və paralel plan yaradılmır.
- Təsdiqlənmiş dizaynlar saxlanılır; template C.2 yenidən dizayn icazəsi vermir. Nümunə OK-u, mərhələ qəbulu və növbəti mərhələ startı ayrıdır.
- Aktiv branch statusu və tamamlanmamış işlər yalnız handoff-da saxlanır; bu fayl mərhələ qəbulu iddia etmir.

- 13 noyabr cari vəziyyəti: məcburi Google entry və guest/link çıxarılması hər iki main-dədir. Anonymous fallback, wallet merge və data deletion yoxdur. Canlı yoxlama tələb etmə, emulatoru avtomatik açma.
