# Səhifə naviqasiyası — 13 noyabr 2026

## Branch və scope
- `fix/navigation-consistency`, parent `92d640d` (`fix/end-of-attempt-checkpoint`); əvvəlki fix main-ə merge edilməyib. Bu review yalnız 92d640d-dən sonrakı naviqasiya diff-inə aiddir.
- Production MainActivity → OnlineGame; GoogleEntryScreen, HomeScreen/HomeRail, AccountScreen, AcceptedScene, FinishCosmeticScreen və JournalRecoveryScreen-in bütün mövcud onClick/onPage/onBack/onHome keçidləri statik yoxlandı. Ayrı offline PracticeGame production MainActivity-dən açılmır.

## Tapıntı və düzəliş
- **İkinci home:** finish qəbulundan sonra sync attempt-i silir, amma page=game qalırdı. Səhnə olmadıqda AccountScreen game üçün ümumi hesab şablonunu göstərirdi; adi home isə ayrıca HomeScreen-dir. Reconcile edilmiş NoAttempt/Finished + oynanmış game indi home-a qaytarılır. Loading/error bunu tamamlanmış nəticə kimi qəbul etmir.
- **Fallback:** game/naməlum saved route AccountScreen-dən birbaşa çağırılanda da kanonik HomeScreen-ə düşür; əlçatan account/collection/achievements marşrutları saxlanılır. Oyun bərpasında eyni home status təqdimatından istifadə olunur.
- **Lokal vəziyyət:** əlavə səhifələr üçün eyni composition callsite scroll/state yaddaşını paylaşırdı. AccountScreen `key(accountPage)` ilə ayrılır; başqa səhifədən gələn scroll mövqeyi daşınmır.
- **Son istifadəçi qərarı:** Android Geri aktiv oyunda yalnız mövcud pauza panelini açır, təkrar Geri oyunu tərk etmir. Menyuya qayıt çıxışdır; kosmetik seçimdə Geri home-a qaytarır, niyyəti saxlayır. Əvvəlki 9 noyabr Back-exit hissəsini əvəz edir; docs/PLAN.md §3 yeniləndi.

## Qalan məhdudiyyətlər
- Kolleksiya/nailiyyətlər ümumi account təqdimatıdır; ayrıca tam dizayn/feature inteqrasiyası yoxdur. İki ayrı route girişinə görə iki implementasiya tapılmadı; “yamaq” görünüşü ilə bağlı bu borc açıq qalır. Yeni dizayn/asset/layout bu branch-də yaradılmayıb.
- Şəhər/İnventar/Bazar hazır mobil səhifələr deyil, girişləri deaktivdir. Pauza panelində təsdiqli dizaynın Yenidən başla əməliyyatı çatışmır; ayrıca inteqrasiya vahidi qalır. Yeni uydurma ekran və demo adapter əlavə edilməyib.
- AppBackend API/schema/domain dəyişmir, backend dəyişikliyi lazım deyil; yoxlamalar mobil route siyasətinə aiddir.

## PNG sübutu və yoxlama
- `navigation-consistency/01-pages.png`, `02-game.png`: koddan çıxarılmış düymə→səhifə xəritələri. Bunlar tətbiq screenshot-u, yeni dizayn və runtime PASS deyil. Pillow ilə ayrıca diagram çəkilib; mövcud raster asset-lər redaktə edilməyib. Rənglər mövcud tokens.css-dən oxunur. PNG-lər açılıb baxıldı, ox/mətn düzülüşləri düzəldildi.
- GameNavigationTest route normalization, oyun tamamlanması/ongoing/recovery və Back→pause/finish picker/account halları üçün 3 fokuslu unit testi verir. Compose klik/runtime inteqrasiyasını simulyasiya etmir.
- Lokal diff review: start/abandon zamanı avtomatik home-a yanlış keçid, ready olmayan hal, finishdən sonra köhnə route, account keçidləri və auth-key izolyasiyası oxundu; yeni açıq kod tapıntısı yoxdur. Yeni test dependency mövcud core JUnit 4.13.2 pinini təkrar istifadə edir.
- Emulator/ADB/canlı yoxlama və Android render aparılmayıb; telefon/planşet/orientasiya/font 1–2 runtime halları unverified-dir. CI, main merge, remote push/deploy yoxdur.

Son koddan sonra `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :engine:test :core:test` PASS (32s): app 3 test, engine 64 və core 70 test (son ikisi up-to-date). `git diff --check` PASS.
