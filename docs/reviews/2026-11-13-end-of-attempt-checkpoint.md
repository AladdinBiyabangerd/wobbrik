# Hər enişdə save/reload düzəlişi — 13 noyabr 2026

- Branch: `fix/end-of-attempt-checkpoint`; əsas: `main`. Review scope: bu branch-in main-dən diff-i (AcceptedEngineHost, regressiya testləri və sənədlər).
- Səbəb: host hər FALLING → settled keçidində fizikanı pause edib checkpoint göndərirdi; recovery səhnəni yenidən qururdu və explicit resume tələb olunurdu.
- Düzəliş: uğurlu mərtəbələr eyni lokal engine-də fasiləsiz davam edir. Yalnız FINISHED keçidində yığılmış müsbət irəliləyiş bir durable checkpoint kimi göndərilir. Qəbuldan sonra mövcud kosmetik seçim və explicit finish qalır; sıfır mərtəbədə checkpoint yoxdur. Exact Ready/account epoch, pause/frame clock və durable retry sərhədləri dəyişmir.
- AppBackend yalnız read-only yoxlandı: `internal/wobbrik/attempts.go` Checkpoint çoxmərtəbəli delta qəbul edir, sequence accepted+1-dir; `AcceptedAttemptCommands` də eyni müqaviləyə uyğundur. API/DTO/schema/rules/backend dəyişiklik lazım deyil və edilməyib. Start/checkpoint/finish mövcud server authority-sini saxlayır; hər mərtəbədə mükafat yox, yekunda qəbul zamanı kredit var.
- Lokal diff review: uğurlu eniş, terminal miss/topple keçidi, sıfır mərtəbə, stale Ready/account, explicit finish və lifecycle sərhədləri yoxlandı; qalan tapıntı yoxdur. UI/layout/asset/token dəyişməyib.
- Yoxlamalar: `:engine:test` 64 test PASS; `:core:test` 70 test PASS (mövcud up-to-date nəticə). Yeni regressiyalar real lokal fizika + deterministik backend fake/disk journal ilə uğurlu enişdə request olmamasını, bir yığılmış terminal checkpoint-i, explicit finish-i və sıfır mərtəbədə checkpoint atlanmasını yoxlayır. Backend fake real server testi deyil.
- İlkin yoxlamada yeni testdə `Floor.x` əvəzinə `centerX` tələb olundu; sonra testin məğlubiyyət yaratmaq üçün kənar mövqeni gözləməsi düzəldildi. Son testlər PASS; əvvəlki uğursuz cəhdlər PASS sayılmır.
- Emulator, ADB, canlı yoxlama və render edilməyib; runtime/visual uyğunluq unverified-dir. CI/main merge/push/deploy edilməyib. Məlum limit: 16 KiB checkpoint sərhədi və durable journal yazısından əvvəl prosesin itirdiyi lokal irəliləyiş; yeni limitsiz encoding və ya ikinci wallet yaradılmayıb.

Son koddan sonra `./gradlew :engine:test :core:test :app:assembleDebug :app:lintDebug` — PASS (18s). `git diff --check` PASS. Mövcud LocalLifecycleOwner deprecation xəbərdarlığı qalır.
