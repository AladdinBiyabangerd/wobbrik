# UI implementasiyasından əvvəl təsdiq

- Branch: `docs/ui-before-implementation-approval`.
- Yoxlanmış diff: `AGENTS.md` və `.cursor/rules/visual-work.mdc` daxilində eyni UI təsdiq qaydası.
- Dəyişiklik: implementasiyadan əvvəl konkret dizayn/vizual nümunə istifadəçiyə təqdim edilir və açıq təsdiq gözlənilir. Mövcud təsdiqlənmiş dizayn təqdim olunur; ümumi icra icazəsi konkret UI təsdiqini əvəz etmir.
- Lokal review: iki mənbə eynidir; əlavə tapıntı yoxdur. Mövcud dizayna sadiqlik tələbi saxlanır.
- Yoxlama: `git diff --check` PASS. Yalnız qayda sənədləri dəyişib; build/render tələb olunmur.
- CI işlədilməyib: branch-də CI qadağandır; main merge istifadəçi OK-u gözləyir. Push/deploy edilməyib.
