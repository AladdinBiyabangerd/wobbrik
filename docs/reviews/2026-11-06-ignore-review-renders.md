# Review: ignore local review renders — 6 noyabr 2026

- Branch: `chore/ignore-review-renders` (from `main`)
- Diff: `.gitignore` + `docs/reviews/**` media untrack; dizayn/Android asset toxunulmayıb
- Tapıntı: lokal review png/jpg/gif/mp4 push üçün mənasız ~45MB+ blob idi; `origin/main`-də yox idi
- Düzəliş: tip-dən `git rm --cached`; history rewrite `origin/main`-dən sonrakı lokal commitlərdən silinir
- CI: branch-də işə salınmayıb (yalnız ignore/cleanup)
- Qalan: merge → `main`; feat branch-ləri rewrite olunur; remote push ayrıca göstəriş
