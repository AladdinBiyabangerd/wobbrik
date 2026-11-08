# UI implementasiyasından əvvəl təsdiq

- Mənbə branch: `docs/ui-before-implementation-approval`; main-dən ayrılmış inteqrasiya branch-i: `docs/ui-approval-main`. Yalnız qayda commit-i `2793657` köçürülüb; home/online tarixçəsi daxil edilməyib.
- Yoxlanmış diff: `AGENTS.md` və `.cursor/rules/visual-work.mdc` daxilində eyni UI təsdiq qaydası.
- Dəyişiklik: implementasiyadan əvvəl konkret dizayn/vizual nümunə istifadəçiyə təqdim edilir və açıq təsdiq gözlənilir. Mövcud təsdiqlənmiş dizayn təqdim olunur; ümumi icra icazəsi konkret UI təsdiqini əvəz etmir.
- Lokal review: iki mənbə eynidir; əlavə tapıntı yoxdur. Mövcud dizayna sadiqlik tələbi saxlanır.
- Yoxlama: `git diff --check` PASS. Yalnız qayda sənədləri dəyişib; build/render tələb olunmur.
- İstifadəçinin main merge göstərişindən sonra qayda branch-i tarix saxlanılaraq main-ə merge edildi. Main üzərində `./scripts/local-ci.sh` PASS: BUILD SUCCESSFUL, 103 task (2 executed, 101 up-to-date). Push/deploy edilməyib.
