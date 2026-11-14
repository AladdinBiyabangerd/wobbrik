# Account/receipt reconciliation — 14 noyabr 2026

Branch `fix/network-resilience`, HEAD `3ddc047`; yoxlanmış vahid source/test working diff-dir. Əvvəlki owner InventoryScreen/phase4/PLAN/handoff/render/release/QA dəyişiklikləri saxlanır. Commit/merge/push/deploy/CI yoxdur.

## Davranış və sərhədlər

- GameplayHttpBackend mövcud authenticated `GET attempts/{id}` və `GET operations/{id}` müqaviləsini istifadə edir. ID/path yoxlanır, token və cavab eyni UID/login epoch-a bağlıdır. Yalnız 404 `not_found` unknown-dur; auth/429/5xx və malformed cavablar gizlədilmir.
- Historical attempt parser abandoned status-u ayrıca saxlayır. Adi sync parser-i əvvəlki active/finished sərhədini qoruyur. Historical account məlumatı cari snapshot/Ready kimi publish edilmir.
- Cari sync-də pending attempt görünmürsə və started-dırsa, yaxud başqa active varsa, coordinator mutex-i altında historical attempt və sabit `<result-id>-finish` receipt oxunur. Attempt finished olmalı, attempt ID və receipt/local/attempt mərtəbələri uyğun olmalıdır. Yalnız sonra fresh sync oxunur; revision historical snapshot/receipt-dən geri ola bilməz. Equal revision balans refresh-i saxlanır.
- Pending exact intent ilə compare edilərək atomik bağlanır, sonra cari sync publish olunur. Disk/sync/epoch xətası bağlanmanı dayandırır; process itkisi sonrası receipt yenidən oxuna bilər. Yeni wallet/grant/journal yoxdur; mövcud sole wire journal exact retry dəyişmir.
- Başqa cihazın aktiv cəhdi abandon edilmir. Unknown/abandoned/receipt-siz və uyğunsuz nəticə silinmir və yenidən start edilmir. 404 davam edən əməliyyatın gələcəkdə commit etməyəcəyinin sübutu sayılmır. Növbədə cari active bizim başqa pending nəticəmizdirsə, əvvəl həmin nəticə tamamlanır; daha köhnə unstarted nəticə onun slotunu bloklamır.

AppBackend `docs/wobbrik-game-api.md`, `internal/wobbrik/read.go` account/app lock və receipt scope-u, `finish.go` immutable result/operation əlaqəsi read-only yoxlandı. Mövcud müqavilə kifayətdir; API/DTO server schema/migration dəyişikliyi lazım deyil, backend dəyişməyib və backend test run iddiası yoxdur.

## Lokal review və yoxlamalar

Scoped diff-də authority, journal/intent acknowledgement sırası, account epoch, path sərhədi, unknown nəticənin saxlanması və historical məlumatın cari sync-i əvəz etməməsi yoxlandı. Əlavə konkret bug tapılmadı. UI/token/art dəyişmədi; render tələb edən vizual vahid deyil.

Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS20s. Son iki regression-dan sonra eyni fokuslu yoxlama PASS3s (108 task,105 up-to-date). XML nəticələri: core110 + engine94 + app24 =228 unit; failures/errors/skipped=0. Yeni10 test: HTTP lookup status/ID/path/error/epoch; historical finish/current account; unknown/abandoned/no receipt; receipt ID mismatch; sync failure; disk failure/reopen; same UID epoch; stale sync/post-receipt epoch; floor mismatch/exact pending bytes; queued own active priority. Əvvəlki lost start/finish exact journal retry regression-ları da keçir. `git diff --check` PASS.

İlk sandbox Gradle run-u lokal wrapper lock-a yazı icazəsi olmadığı üçün başlamadı; uyğun escalation ilə yuxarıdakı yoxlamalar tamamlandı. Test əlavə etmənin ilk shell run-u yanlış cwd səbəbindən fayl tapmadı; düzəldildi, uğursuz run PASS sayılmır.

Yoxlamalar owner diff-i ilə working tree-dədir; isolated commit-only yoxlama deyil. Emulator açılmayıb; device/runtime/real-server E2E/render/CI yoxdur. Scheduler bu vahidə daxil deyil. Başqa-device active/abandoned/missing və receipt-siz finished nəticələrin avtomatik resolution siyasəti yoxdur: pending saxlanır, recovery Error olur. Əvvəlki full design parity və mərhələ qəbulu məhdudiyyətləri qalır.


## Lokal review və scoped commit — 14 noyabr 2026

İstifadəçinin tapşırığı ilə `3ddc047` üzərinə reconciliation working diff-i müstəqil lokal review edildi. Bugbot bir dəfə yalnız bu vahid üzrə işlədildi; actionable bug tapılmadı. Əlavə lokal yoxlamada historical attempt/receipt ID və mərtəbə uyğunluğu, fresh sync, UID/epoch, atomik exact intent removal, unknown/abandoned retention, öz aktiv pending-ə üstünlük və sole journal retry sərhədləri yoxlandı. Production/test dəyişiklik tələb edən yeni problem tapılmadı.

Təkrar Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS979ms; 108 task,107 up-to-date. Əvvəlki XML nəticələri core110+engine94+app24=228 unit, failures/errors/skipped0 olaraq yoxlandı. `git diff --check` PASS. Yoxlamalar owner diff-i olan working tree-dədir; isolated commit-only/device/real-server yoxlaması deyil.

Commit sərhədi: yalnız yeddi reconciliation source/test faylı, bu review, PLAN §3 reconciliation yeniləməsi və handoff-un bu vahidə aid ilk bölməsi. Owner InventoryScreen/phase4/render/release/QA və qarışıq sənədlərin digər hissələri index-dən kənarda qorunur. UI/art/backend/scheduler dəyişmədi. Merge/push/CI/emulator yoxdur. Naməlum nəticə silinmir və restart edilmir; qalan məhdudiyyətlər yuxarıdakı kimidir.
