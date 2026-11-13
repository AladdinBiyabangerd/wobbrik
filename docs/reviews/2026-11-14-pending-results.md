# Durable terminal results — 14 noyabr 2026

Branch: `fix/network-resilience`; baza HEAD `a2ebcce`. Bu vahid working diff-dir, commit yoxdur. Əvvəldən dirty owner/QA/release faylları qorunub və bu review-un əhatəsi deyil.

Yoxlanmış source: core `AttemptCoordinator`/`AuthenticatedRecoveryOwner`; engine `PendingGameResults`, `PendingResultReconciler`, `AcceptedAttemptRestore`; app `AndroidFinishDraftStore`/`WobbrikApplication`; android `AndroidAttemptJournal` intent file factory. Yeni queue regression-ları `PendingGameResultsTest`-dədir.

Terminal nəticə (engine checkpoint + parts + identity/start marker) hesab üzrə atomic/fsynced local intent kimi saxlanır. Mövcud `DurableJournalFile` primitive-i ayrı namespace-də təkrar istifadə edilir; burada wire envelope/revision saxlanmır, ikinci command journal/wallet yoxdur. Storage dolanda və ya format pozulanda səssiz eviction/reset yoxdur. Terminal progress immutable-dir; host-un pre-dispatch cosmetic seçim guard-ı saxlanır.

Recovery əvvəl mövcud pending wire əmri exact retry edir, sonra terminal intent-ləri sole coordinator-da serial start/checkpoint/finish ilə göndərir. Serverdə başlanmış attempt-in terminal nəticəsi app restart-dan sonra da replay edilir. Stable operation IDs, accepted prefix/counter/geometry validation və session epoch guard qalır. Başqa aktiv attempt heç vaxt abandon edilmir. Started attempt yoxdursa intent saxlanır və manual reconciliation tələb edən xəta qaytarılır; yeni attempt yaradılmır.

Finish receipt təsdiqləndikdən sonra `onCommitted` intent-i sync və journal remove-dan əvvəl bağlayır. Callback/storage/sync xətasında journal qalır. Cavab itkisi receipt-in exact replay-ini yaradır, yeni nəticə start etmir. Köhnə draft yalnız sync-in active attempt ID-si ilə import olunur; bitmiş köhnə draft-lar yenidən növbəyə salınmır.

Lokal review-da saxta Ready yaratmaq əvəzinə snapshot-only validation ayrıldı, stage suffix üçün ID uzunluğu məhdudlaşdırıldı, store/reconciler ayrıca fayllara bölündü və əvvəlki draft üçün import əlavə edildi. Son diff review-da əlavə konkret tapıntı yoxdur.

Son yoxlama: Java21, offline Gradle `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS11s. Core105 + engine79 + app23 test; failures/errors/skipped0. Yeni12 regression: çoxsaylı nəticə/reopen, accepted cəhdin lokal progress-i, lost start/finish response, finish acknowledgement disk failure/sync failure, başqa-device attempt, missing-started attempt, account switch, revision conflict, corrupt/full storage. `git diff --check` PASS.

Tam offline məhsul axını hazır deyil: yeni şəbəkəsiz Play/start/scene, accepted catalog cache, inventory/detail/city coherent read-only cache/routes, connectivity retry scheduling və başqa-device/missing-started attempt-in tam reconciliation axını açıqdır. Növbənin yeni-start branch-i contract fake ilə yoxlanıb, production Play-dən hələ çağırılmır. Android persistence migration/runtime və real server E2E ayrıca yoxlanmayıb. UI/art/layout/token dəyişməyib; render/runtime PASS iddiası yoxdur. AppBackend API/schema dəyişməsinə bu vahiddə ehtiyac olmadı; backend yalnız read-only müqavilə ilə yoxlanıb. Emulator, CI, commit, merge, push, deploy yoxdur.
