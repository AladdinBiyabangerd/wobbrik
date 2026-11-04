# Şəhər referanslarının təmizliyi — 4 noyabr 2026

- Branch: `docs/approved-city-references`.
- Yoxlanmış diff: docs/design/README.md və iki PNG, docs/game-plan.md, docs/kotlin-compose-plan.md, docs/context/current-task.md; bu review qeydi. Köhnə, Git-ignore edilmiş output/ui-concepts/ daxilində 27 fayl lokal olaraq silinib; bunların silinməsi Git tarixində göstərilmir.
- Tapıntı: seçilməmiş şəhər/bazar və digər köhnə konseptlər prompt/tokenləri ilə qalırdı; plan sənədləri iki görünüşü vahid seçim kimi göstərmirdi.
- Düzəliş: köhnə dəst silindi, istifadəçinin 2-ci/3-cü şəkilləri Git-də saxlanılan yeganə şəhər referansları oldu; plan/handoff istinadları yeniləndi. Digər ekranlar üçün silinmiş maketlərdən təsdiq fərz edilmir.
- Review: PNG-lər açılıb baxıldı; attachment-lərlə SHA-256 eyniliyi təsdiqləndi; köhnə qovluğun yoxluğu və sənəd istinadları yoxlandı. Əlavə tapıntı yoxdur.
- CI: ./scripts/local-ci.sh — BUILD SUCCESSFUL, 43 task, 1 executed / 42 up-to-date (core test, AAR və lint mövcud Gradle nəticələrindən istifadə etdi). Sandbox Gradle lock-a giriş vermədiyi üçün eyni komanda icazə ilə təkrar işlədildi.
- Backend: dəyişiklik yalnız vizual referans/sənəddir; domain/API/DTO/schema dəyişmir, AppBackend dəyişikliyi tələb olunmur.
- Məhdudiyyət: tətbiq ekranı dəyişməyib; telefon/planşet renderi və kamera realizasiyası yoxlanmayıb. Silinmiş ignored konseptlər başqa checkout-lardan Git merge ilə silinmir. Əvvəldən untracked responsive rule/review bu commit-ə daxil deyil.
