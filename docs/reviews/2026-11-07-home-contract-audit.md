# Home müqavilə auditi review — 7 noyabr 2026

- Branch: docs/home-contract-audit, base fb14bc1 (main).
- Yoxlanmış diff: docs/home-contract-audit.md, docs/context/current-task.md və bu qeyd.
- Tapıntılar: sync-də şəxsi rekord/XP intervalı yoxdur; leaderboard səhifəsi
  şəxsi home rekord mənbəyi kimi yetərli deyil; növbəti hədd int64-dən böyük
  ola bilər; online branch main-də deyil; city placement hazır deyil.
- Düzəliş: kodla təsdiqlənmiş sərhədlər və ayrıca implementasiya/test mərhələləri
  qeyd edildi; beş tələb və backend adapteri tamamlanmış göstərilmir.
- Lokal review: mənbələr üzrə yoxlama və git diff --check; kod/UI dəyişmir.
- CI: main merge-dən sonra ./scripts/local-ci.sh nəticəsi aşağıda qeyd ediləcək.
- Məhdudiyyətlər: API/DB/device runtime test yoxdur; vizual dəyişiklik və render
  yoxdur. Backend oxunub, dəyişdirilməyib; backend CI icra edilməyib.
