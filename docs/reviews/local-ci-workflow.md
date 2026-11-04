# Lokal review — chore/local-ci-workflow

Tarix: 4 noyabr 2026. Reviewer: bu chatdə agent tərəfindən lokal self-review; ayrıca subagent istifadə edilməyib.

## Scope
- Baseline `b61a6ef` üzərindən AGENTS.md, always-apply rule, lokal CI skripti, Gradle ci task və icra/handoff sənədləri yoxlandı.
- Arrowz root Gradle ci graph yanaşması tətbiq edildi; GitHub workflow köçürülmədi.

## Findings and fixes
- Köhnə AGENTS “oyun tətbiqi qurulmur” məhdudiyyəti son göstərişə uyğun yeniləndi.
- Əvvəlki ölçü mənbəyi gözləmə qərarının artıq əvəz olunduğu plan və handoff-da qeyd edildi.
- İlk CI core testlərini up-to-date saydı; təzə icranı təsdiqləmək üçün tam gate `--rerun-tasks` ilə təkrar işə salındı.
- Skript fail-fast işləyir, staged/unstaged whitespace yoxlayır, mövcud Android Studio JDK və SDK üçün yalnız boş environment dəyişənlərinə fallback verir. İstifadəçinin JAVA_HOME/ANDROID_HOME seçimi qorunur.

## Validation
- `sh -n scripts/local-ci.sh`: passed.
- `git diff --check`: passed.
- `./scripts/local-ci.sh`: BUILD SUCCESSFUL; test up-to-date, AAR və lint icra edildi.
- `./scripts/local-ci.sh --rerun-tasks`: BUILD SUCCESSFUL, 43/43 actionable task icra edildi; 21 test, 0 failure/error/skipped; AAR/lint keçdi. Review üzrə açıq bloklayıcı tapıntı yoxdur; merge üçün hazırdır. Merge nəticəsində eyni gate təkrar işlədiləcək, nəticə chat yekununda veriləcək.

## Limits
- Bu gate hazırda yalnız mövcud billing modulunu əhatə edir. App/engine yaradıldıqda onların test/build/lint və render yoxlamaları gate-ə əlavə edilməlidir.
- Android lint-də üç warning var; error yoxdur. Real Play/KeyStore cihaz sınağı deyil.
- Backend dəyişməyib, backend CI nəticəsi iddia edilmir. Placement bağlıdır.
- Oyun hostu/tikinti dövrü və vizual render bu branch-in nəticəsi deyil.
