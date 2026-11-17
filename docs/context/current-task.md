# Cari tapşırıq — İnventar detalı gecikməsi / 17 noyabr 2026

## Completed

- Əvvəlki City batching Android85307bf/AppBackend23102b4; backend remote main838c0c7. HTTPS socket-owned cancellation Android02fbf5a. Əvvəlki review/evidence saxlanır, bu chatdə həmin işlər təkrarlanmayıb.
- İnventar detalının building→bütün city səhifələri asılılığı ayrıldı. Optional membership `inventory-city` effekti Ready/retry üzrədir; seçim/pagination onu yenidən başlatmır. Detail mövcud `detail-<id>` bundle-dan local display-only məlumatı əvvəl göstərir; fresh cavab yalnız mövcud capability ilə əməliyyatları açır.
- Production native host fixture: city gate1200ms, building40ms. İlk detail1408→197ms; city2→1read. Total6→6HTTP,1623→1898responsebyte (city capture son sync indi tamamlanır). Bunlar tək host nümunələridir, device/production deyil.

## Current state

Android `fix/inventory-detail-latency`, baza02fbf5a; kiçik production diff yalnız InventoryScreen.kt, review/evidence aşağıdadır. Main merge/push/deploy/CI yoxdur. AppBackend dəyişməyib. App-wide performans/redesign bitməyib.

## Decisions

- Yeganə plan `docs/PLAN.md` §1.1/§2–3. Backend lazım olsa mövcud feat/wobbrik-də; main merge/push/deploy ayrıca göstəriş. CI yalnız ayrıca tələb ilə main-də lokal. Emulator avtomatik açılmır, canlı yoxlama gate deyil.
- Exact Ready/login epoch/account/revision, accepted geometry, server wallet/ownership authority və sole journal sərhədləri saxlanır. Membership yalnız təqdimatdır. Yeni cache açarı ilə köhnə offline bundle-lar itirilməyib.

## Checks performed

- Son production diff: core152/app37unit+assembleDebug/lintDebugPASS29s; diffcheckPASS.
- Native host7/7PASS18s: blocked city ilk məzmunu saxlamır, legacy cached detail şəbəkədən əvvəl görünür/silmə disabled; fresh bina cavabı sonrası enabled; list↔detail təkrarı əlavə HTTP yaratmır; scroll düyməyə çatır.
- Native beş matrix PNG açılıb:320×568font1/2,360×640font1,390×844font2synthetic inset və Long.MAX balans. `02-market.png` ilə müqayisə edildi; vizual layout dəyişməyib, tam parity deyil. Kontrast Text13.00/Muted8.22/Accent8.99PASS.
- Backend Inventory/City owner-filtered read müqaviləsinə baxıldı; scheduling düzəlişi API/domain/schema dəyişikliyi tələb etmir. Bu vahiddə backend testləri işlədilməyib.

## Remaining work

- Root İnventarın bildirilən10+s və menyuların2+s gecikməsi device-də ölçülməyib. BuildingPreview sinxron art/atlas decode, atlas~60MiB retention, Profile foreground/skin CPU, real payload və production/device gecikməsi açıqdır.
- Font2/Long.MAX header clipping, kiçik portret dock, scroll və tam visual parity borcları qalır. Real OS inset/IME/locale/TalkBack/GPU yoxlanmayıb; fixture padding real inset deyil.
- Android main merge/push yoxdur; yeni vahid üçün ayrıca istifadəçi göstərişi ilə davam et. HTTPS/libcore/DNS/connect/handshake/upload cancellation və authenticated Android→GoE2E ölçülməyib.

## Relevant files

- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{InventoryScreen,MobileInventoryPresentation,ResourceArtLoading,AccountViews}.kt`; art çağırışları üçün InventoryScreen.kt BuildingPreview və mövcud rememberGameAtlases/rememberCosmeticArt.
- `docs/reviews/2026-11-17-inventory-detail-latency.md`, `docs/reviews/inventory-detail-latency/{render,evidence}/`.
- Əvvəlki review: `2026-11-17-resource-background-city-https.md`, `2026-11-17-city-detail-batching.md`, `2026-11-17-https-scoped-cancellation.md`.

## Exact next step

Bu handoff və PLAN §1.1-i oxu; root İnventarın qalan gecikməsi üçün BuildingPreview/atlas/resource decode CPU/byte/allocation və ilk kart gecikməsini production native host fixture-də ölç. Mövcud GameTokens/ui-craft və PNG authority-ni saxla; yalnız ölçülmüş işi optimallaşdır. Tamamlanan detail scheduling/Market/City batching/HTTPS işini təkrarlama. Cihazda ölçülməyən performansı PASS göstərmə. CI/merge/push/deploy avtomatik yoxdur.
