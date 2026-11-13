# Şəhər oxu — decorations transport düzəlişi

- Branch: `fix/city-decoration-read`; baza və review edilmiş diff: `ad0a396` → bu vahidin iki core faylı. Owner handoff/release/live-check/XML/QA faylları qorunur.
- Səbəb: `CitySceneRepository.load` şəhər/building oxusundan sonra həmişə `decorations?limit=20` çağırır. Production `UrlConnectionGameplayTransport` bu yolu `allowedReadPath` daxilində tanımırdı; `require` sorğunu şəbəkədən əvvəl rədd edirdi. Boş dekor inventarı da həmin sorğunu tələb edir. Əvvəlki fake transport testləri production yol məhdudiyyətini tətbiq etmirdi.
- Sübut: production yol validator-u mövcud scene-load regression testinə əlavə edildi. Düzəlişdən əvvəl həm scene-load, həm yeni decoration path testi FAIL (10 testdən 2). Sonra decorations eyni limit/cursor regex-inə əlavə edildi; limit=20 və yalnız təhlükəsiz ID cursor qəbul olunur. Əlavə query, başqa limit, traversal və mutation yolu rədd edilir.
- Lokal diff review: yalnız oxu transport allowlist və regression testləri dəyişir. Mobil dizayn/UI, geometry, wallet, journal, account/epoch yoxlamaları saxlanır. AppBackend city/decorations implementasiyası read-only tutuşduruldu; mövcud müqavilə ilə uyğun olduğundan backend/schema/API dəyişikliyi tələb olunmur.
- Son Java21/offline `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (13s); CitySceneReadsTest 10/10 PASS. `git diff --check` PASS. CI/merge/push yoxdur; istifadəçi lokal review və problem yoxdursa commit istəyib.
- Məhdudiyyət: tarixi screenshot-un konkret exception logu yoxdur. Deterministik production maneəsi regression ilə təkrarlandı və düzəldildi; real hesab/server E2E təsdiqi iddia edilmir. ADB cihaz inventarı sandbox-da daemon socket icazəsinə görə alınmadı; emulator açılmadı. UI dəyişmədiyindən yeni render hazırlanmadı. Əlavə server/deploy/şəbəkə xətalarının olmadığı iddia edilmir.

Dəqiq növbəti addım: bu kiçik diff-i istifadəçiyə təhvil ver; ayrıca göstərişsiz yeni feature, merge/push/CI başlama.

## Commit öncəsi lokal review

`ad0a396` üzərinə yalnız transport allowlist, iki regression ssenarisi və bu vahidin sənədləri review edildi. Əlavə bug tapılmadı; source/test son PASS-dan sonra dəyişməyib, təkrar build tələb edən yeni dəyişiklik yoxdur. İcazə yalnız bounded decorations GET səhifələrinə genişlənir; origin, auth/epoch, mutation yolları və cursor məhdudiyyəti saxlanır. Owner handoff-un əvvəlki oyun bölmələri və untracked release/live-check/XML/QA faylları index-ə daxil edilmir.
