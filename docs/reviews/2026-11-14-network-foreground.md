# İnternet davamlılığı — aktiv cəhdin foreground qorunması

14 noyabr 2026. Branch `fix/network-resilience`, HEAD baza `a2ebcce`; yoxlanmış working diff: `AuthenticatedRecoveryOwner.kt`, `AuthenticatedRecoveryOwnerTest.kt`, `AcceptedAttemptCommandsTest.kt` və bu vahidin plan/context qeydləri. Əvvəldən dirty owner faylları saxlanır, bu vahidə aid hesab edilmir.

## Düzəliş

Process foreground `onForeground()` hər dəfə recovery-yə keçərək application host-dakı unsubmitted lokal fizikanı silirdi. Şəbəkə yoxdursa həmin cəhd Error-da bloklanırdı. İndi exact Ready, active attempt və coordinator ready olduqda lifecycle refresh buraxılır. Mutasiya yenə coordinator/session/revision/journal üzərindən serverə gedir. Auth dəyişikliyi, eyni UID re-login və Error/pending recovery bu optimizasiyaya düşmür. User/background pause və explicit resume qalır.

## Yoxlama və review

`:core:test :engine:test` PASS:105+67 test, failure/error/skipped0. Son integration regression əlavəsindən sonra8s PASS (core up-to-date, engine rerun). Test lokal floor-un şəbəkəsiz background→foreground→explicit resume zamanı itməməsini, sıfır əlavə dispatch-i və frame reset-i yoxlayır. Core regression exact Ready obyektinin saxlanmasını və same-UID auth epoch-da köhnə submit-in rəddini yoxlayır. Mövcud lost-response/durable replay testləri də keçir. Scoped diff review: auth/session və mutation guard-ları zəiflədilməyib, əlavə tapıntı yoxdur. `git diff --check` PASS.

AppBackend API/schema dəyişmədiyinə görə bu lifecycle düzəlişində backend dəyişikliyi tələb olunmur. Backend testləri və real server E2E aparılmayıb. UI/art/layout/token dəyişməyib; yeni render/runtime PASS iddiası yoxdur. Emulator, CI, commit, merge, push və deploy yoxdur.

## Tam tapşırıq üzrə açıq iş

İstifadəçi **oflayn nəticələr də sonradan serverə göndərilsin** qərarını verdi. Bu, əvvəlki yalnız mükafatsız məşq qaydasını əvəz edir, implementasiya sübutu deyil. Yeni offline start, çoxsaylı durable nəticələr, process-death/lost-response/revision-conflict göndərməsi və inventory/detail/city read-only cache hələ açıqdır. Hazır fix bunların tamamlanması kimi təqdim edilmir. Server qəbuladək sikkə/XP/nailiyyət/bina/leaderboard təsdiqlənmir. Yeganə plan PLAN; exact davam addımı current-task handoff-dadır.
