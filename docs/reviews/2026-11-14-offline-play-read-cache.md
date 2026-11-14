# Offline Play və read-only baxış keşi — 14 noyabr 2026

Branch: `fix/network-resilience`; HEAD baza: `a2ebcce`. Yoxlanmış vahid working diff-dir, commit/merge deyil. Əvvəlki pending-results/network və owner phase4/release/QA dəyişiklikləri saxlanılıb; əlaqəsiz fayllar reset/stash edilməyib. CI/commit/merge/push/deploy yoxdur.

## Dəyişiklik və authority

- `LocalGameHost` cari Google-backed `BillingSession` epoch-u və qəbul edilmiş `ReadOnlyAccount` ilə yeni Play açır. Mövcud `PracticeEngine` fizikasını, `GameScene` renderer/input və pauza panelini istifadə edir. Yeni cəhd şəbəkə cavabını gözləmir; onlayn olduqda da ilk server start terminal intent-dən reconciler vasitəsilə gedir. Bununla əvvəl server start edib qeyri-müəyyən cavabdan sonra ikinci lokal ID yaratmaq ehtiyacı aradan qalxır. Əvvəlki accepted cəhd host-u saxlanır; serverdə qalmış açıq cəhd üçün hazır discard/abandon siyasəti dəyişmir.
- Məğlubiyyət əvvəl hesab üzrə `PendingGameResults`-a yazılır; yalnız uğurlu disk yazısından sonra lokal səhnə bağlanır. Mövcud `PendingResultReconciler` yeganə wire journal üzərindən start→checkpoint→finish edir. Sıfır mərtəbədə checkpoint yoxdur. Wallet/XP/ownership/leaderboard lokal hesablanıb accepted kimi verilmir.
- Disk xətası terminal scene/parts/ID-ni retry üçün saxlayır. Auth dəyişməsi açıq səhnəni sinxron dayandırır; əvvəlki UID-nin terminal draft-ı başqa UID ilə göndərilmir. Eyni UID yeni epoch-da yalnız saxlanmamış terminal intent-i retry edə bilər. `AcceptedEngineHost`-da da diskə yazılmamış terminal draft recovery/re-login zamanı yaddaşda saxlanır. Process/hardware itkisi ilə uğursuz disk yazısını xilas etmə zəmanəti yoxdur.
- `ReadOnlyGameCache`: app-private atomic bounded fayllarda accepted snapshot+kataloq və tam baxış üzrə raw GET paketləri saxlanır. UI model `ReadOnlyAccount`/`AccountView`-dur; saxta Ready yaradılmır. Room mövcud snapshot keşi olaraq qalır, yeni wire outbox/wallet yoxdur.
- Canlı baxış qəbul edilmiş account revision-a qarşı son `/sync` ilə yoxlanılır. Şəhər mövcud əvvəl/son city bracket-i, bina revision və parcel/dekor parser yoxlamalarını da saxlayır. Fərqli cavablı təkrar GET, dəyişmiş revision və natamam paket yazılmır. Replay yalnız həmin UID/revision və eyni parser/repository ilədir; canlı və cached payload-lar bir paket daxilində qarışdırılmır. İnventar səhifələri cursor üzrə ayrıca saxlanır.
- Inventar, əvvəl oxunmuş bina detail və şəhər offline açılır. Alış/hissə redaktəsi/silmə bağlıdır; server mutasiyası üçün əvvəlki exact Ready tələbi saxlanır. Keş rejimi mövcud tokenlərlə işarələnir. Müvəqqəti IO/408/429/5xx xətaları bütöv cached view-a düşə bilər; 400/401/403/404 və auth epoch xətaları cache ilə gizlədilmir.

AppBackend `docs/wobbrik-game-api.md` müqaviləsi yoxlanıldı: mövcud start/checkpoint/finish revision/dedup və account-scoped attempt/receipt oxuları var. Yeni API/DTO/schema/migration tələb edilmir, backend dəyişməyib. Client test serverləri real PostgreSQL/production E2E sübutu deyil. Mövcud reconciler-in başqa-device active və missing-started-attempt blokları saxlanır; avtomatik abandon, nəticəni silmə və ya yenidən start etmə yoxdur.

## Lokal review tapıntıları və düzəlişlər

- Home navbar callback olsa da status-a görə Play-i bağlayırdı: yalnız callback/journal sərhədinə uyğunlaşdırıldı; real Home→Play→Resume host regression-u əlavə edildi.
- Uzun keş xəbərdarlığı böyük şriftdə səhnəni sıxışdırırdı: inventarda caption, detail-də hazır deaktiv əməliyyat slotu, şəhərdə hazır əhali sətri istifadə olunur. Rəng/ölçü tokenləri və artwork dəyişməyib.
- Diskə yazılmamış terminal recovery-də itə bilirdi: UID/attempt üzrə yaddaşda retry qorundu; yeni epoch-da köhnə capability rədd edilir.
- Cache fallback bütün HTTP xətalarını eyni tutmamalıdır: müvəqqəti statuslar ayrıca, auth/server rəddi ayrıca yoxlanır.
- Diff-də yerləşmə/import/authority/retry sərhədləri yoxlanıldı; final scoped review-da əlavə konkret problem tapılmadı. Əvvəlki dirty source/review/QA işləri saxlanır.

## Yoxlamalar

Java 21, Gradle offline:

- `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` final PASS (8s; core/engine son 18s run-dan up-to-date). 109 core +85 engine +24 app =218 unit; failures/errors/skipped=0.
- Yeni 11 regression: 4 read-cache (reopen/account isolation, missing page/revision, racing revision/disk, auth epoch), 4 local host (real loss/multiple durable results, disk+auth retry, lifecycle/leave, corrupt queue), accepted draft re-login, real local physics→sole journal exactly-once reconciliation, HTTP fallback/auth denial.
- `-I ../docs/reviews/offline-views/render/render.init.gradle :app:testDebugUnitTest --tests '*OfflineViewsRenderTest'` final 3/3 PASS (16s). 360×640 font1/font2, 390×844 font1. Production cache/repositories/screens and Home→Play→Resume; offline view axınında sıfır network/POST, mutations disabled. Fixture hesabı sintetikdir. Şəhər animasiyası host testində sabit saatla çəkilir; ilk fasiləsiz animasiya idle xətası və sonrakı Home gate regression-u düzəldilib, uğursuz run-lar PASS sayılmır.
- 12 inventory/detail/city/game və 3 Home renderi `docs/reviews/offline-views/` altındadır. Normal və font2 görüntülərinə, dörd `*-comparison.png` şəklinə baxıldı. Font2 detail-in daxili scroll-u saxlanır, footer kəsilmir; font2 şəhər səhnəsi mövcud yığcam davranışla kiçilir.
- `ui-craft` contrast: Text/Background16.57, Text/Surface13.00, Muted/Surface8.22, Background/Accent11.45 — 4/4 PASS. `git diff --check` PASS.

## Dizayn və məhdudiyyətlər

İstifadəçi bu chat-də mövcud təsdiqli ekranların offline-a qoşulmasını və cached mutasiyaların deaktiv edilməsini açıq təsdiqlədi. Mövcud mobil kompakt kompozisiyalar, tokenlər, raster hissələr, real accepted geometry və sabit şəhər kamerası saxlanır. Dörd comparison köhnə əsas təsdiqli inventory-market-v2/detail/city/game referanslarını da göstərir; əvvəl qəbul edilmiş mobil compact/layout fərqləri və əvvəlki review-lardakı full parity fərqləri yenidən dizayn edilmir. Tək sintetik bina referansın statik çoxbinalı şəhəri kimi təqdim olunmur. Tam piksel pariteti və mərhələ qəbulu iddia edilmir.

Yalnız əvvəl uğurla oxunmuş, cari accepted revision-a uyğun baxış açılır. Keşdə olmayan detail/səhifə və köhnə revision offline boş/mövcud kimi uydurulmur; yenidən onlayn oxu tələb edir. Bounded140000-byte paket limiti aşılarsa canlı oxu saxlanır, həmin baxış keşlənmir. Cihaz yaddaşının tam dolması və process itkisi zamanı diskə yazıla bilməyən nəticəyə davamlılıq zəmanəti yoxdur. Connectivity scheduler əlavə edilməyib: terminal tamamlanması, foreground/auth və explicit retry mövcud recovery-ni işlədir. Başqa cihaz active/missing-started pending üçün ayrıca account reconciliation axını açıq qalır.

Emulator açılmayıb. Device/real server E2E, API24 runtime, real inset/keyboard/TalkBack/GPU/performance yoxlanmayıb. Mobil portret sırasına görə tablet/landşaft yeni render işi aparılmayıb. Bunlar PASS kimi göstərilmir və canlı yoxlama merge gate-i deyil.

## Lokal review və commit hazırlığı — 14 noyabr 2026

İstifadəçi mövcud vahidin lokal review/commit-ini və sonra ayrıca reconciliation vahidini təsdiqlədi. `a2ebcce` bazasına qarşı network-resilience domain/data/UI diff-i, hesab/epoch sərhədi, sole journal acknowledgement sırası, queue disk failure, cache fallback/status və mutation gate-ləri yoxlanıldı. Əlavə konkret bug tapılmadı; production kodu dəyişmədi.

Təkrar Java21/offline unit/APK/lint PASS1s (108 task,107 up-to-date; əvvəlki 218 unit nəticəsi reuse). Ayrı Compose host 3/3 PASS16s, 360×640 font1/font2 və390×844 font1; yeni inventory font2/detail font2/city390/game360 renderlərinə baxıldı. Kontrast4/4PASS min8.22. `git diff --check` PASS. Render PNG-ləri lokal ignored evidence-dir; harness review ilə saxlanır. Yeni emulator/device/real server/CI yoxlaması yoxdur.

Commit sərhədi: network-resilience source/test, qayda güzgüləri, PLAN §§3–4 və offline qaydası, üç network review və offline host harness. Əvvəlki phase4 `DetailRow` düzəlişi, phase4 review/render/handoff, release/oyun QA işləri kənarda saxlanır. Yoxlamalar qorunan owner diff-i ilə working tree-də aparılıb; isolated commit-only render yoxlaması deyil. Commit bu branch-in main-ə merge və ya tam mərhələ qəbulu demək deyil. Backend müqaviləsi dəyişmir.

Qalan reconciliation işi artıq istifadəçi tərəfindən istənib: mövcud account-scoped `/attempts/{id}` və `/operations/{id}` müqaviləsinə əsasən başqa-device active/missing-started pending halları, UID/epoch, historical receipt sonrası sync, restart etmədən və pending-i qeyri-müəyyən halda silmədən; connectivity retry scheduling də açıqdır. Böyük implementasiya bu review/commit vahidinə qatılmadı.
