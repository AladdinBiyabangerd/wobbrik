# Cari UI dəyişikliklərinin commit review-u — 19 noyabr 2026

Branch: `feat/profile-collection-native`; başlanğıc `5af3eab`. İstifadəçi bütün cari dəyişiklikləri uyğun commit etməyi tapşırdı. Owner diff-i saxlanıb və məntiqi vahidlərə bölünüb; yeni implementasiya əlavə edilməyib.

- `c5aae9f`: native Şəhər kolleksiyası, scalable island/scene artwork, yalnız City shell route-u, uyğun PLAN hissəsi və design/review/test mənbələri.
- `333740c`: Uğurlar/Rekordlar native kolleksiyası/podium, bounded display memory və primitive expiry metadata, uyğun shell route/tab/tokenlər, PLAN və design/review/test mənbələri.
- `67962b3`: native Profil, matching portrait atlas/material, uyğun shell route, PLAN/design/review/test mənbələri. Son PNG yanaşı müqayisəsindən sonra istifadəçinin “okeydir” təsdiqi review-a əlavə edildi.
- `7ed83b3`: əvvəlcədən dəyişmiş iki release baseline `.dm` artefaktı ayrıca saxlanıb. ZIP-lər açılaraq primary.prof/primary.profm üzvləri yoxlandı; hər ikisində payload fərqlidir. Bu commit yeni release/profile generation və performance yoxlaması deyil.
- Bu docs commit-i: əvvəlki inventory-city-detail-costs review əlavəsi, aktual handoff və bu commit review-u.

Lokal review: dəyişmiş source/stat/route shared hunks, feature asılılıqları, PLAN bölgüsü, review-lardakı son-source nəticələr və baseline arxivləri yoxlandı. Yeni blocker tapılmadı. Shared MobileMenuShell və PLAN hissələri working tree-yə toxunmadan index-də aid commit-lərə bölündü. Hər staged diff üçün whitespace yoxlaması PASS. Əvvəlki Şəhər45/45, Uğurlar5/5native+21/21core və Profil13/13 son-source sübutları öz review-larındadır; commit işi zamanı testlər yenidən icra edilmiş sayılmır. Son production source üçün Profil task-ındakı assembleDebug/lintDebugPASS qüvvədədir, bu task yalnız sənədləşdirmə/staging/commit etdi.

Repo `.gitignore` qaydaları docs/reviews/**/*.png,/**/*.jpg və log kimi generasiya edilmiş sübutları Git-dən kənar saxlayır. Bu qayda dəyişdirilmədi, ignore edilmiş fayllar force-add edilmədi və silinmədi. İstehsal/design PNG-ləri, review mətnləri və ignore edilməyən XML/harness/kontrast mənbələri commit edildi. Lokal renderlər əvvəlki review yollarında qalır.

CI işlədilməyib; main-ə merge, push/deploy edilməyib. Runtime/API24/IME/TalkBack/GPU və locale məhdudiyyətləri əvvəlki review-larda qalır. Bu sənəd bütün app redesign-ının qəbul sənədi deyil.
