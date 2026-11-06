# Review: ignore local review renders — 6 noyabr 2026

- Branch: `chore/ignore-review-renders` → FF `main` (`51939ed`); merge `feat/stage-three-online-screens`
- Diff: `.gitignore` + review media history-dən silindi; `docs/design/**` və Android drawable qalıb
- Tapıntı: lokal review png/jpg/gif/mp4 push üçün mənasız ~45MB+ blob idi; `origin/main`-də yox idi
- Düzəliş: ignore qaydaları; `filter-branch` `origin/main` sonrası lokal branch-lərdə; fayllar diskdə ignore olunur
- Yoxlama: `origin/main..main` / `..feat` obyektlərində `docs/reviews` media 0
- CI: branch-də işə salınmayıb (yalnız ignore/cleanup)
- Qalan: remote push ayrıca göstəriş
