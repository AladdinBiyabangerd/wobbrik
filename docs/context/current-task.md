# Cari tapşırıq — City detail batching / 17 noyabr 2026

## Completed

- City Android+AppBackend batching tətbiq edildi: additive include=details, owner-filtered mövcud SQL join, snapshot/creator-sız compact DTO. Tam City JSON60KiB budget; sığmayan detail üçün əvvəlki4paralel fallback. Pagination və son plain City revision bracket saxlanır. Strict köhnə server və pre-batch cache dəstəklənir; clock-only legacy cache rejection düzəldildi.
- Ölçü12bina:15→3HTTP,175→43SQL. Həndəsə1000center/bina:33022→32698JSONbyte; lokal race67–75→45–47ms. Android production repository40ms/request fixture:batch3read145ms,legacy15read267ms,strict16read305ms. Bunlar host fixture ölçüləridir, device/production deyil.
- Əvvəlki HTTPS request-owned socket cancellation working diff-i və fixture/review/evidence qorunur; City commit-inə daxil edilmir. Əvvəlki Home/Profil IO resource loader və atlas işləri dəyişməyib.

## Current state

Android `fix/city-detail-batching`, baza8fd0bc6; City ayrıca scoped commit. AppBackend `feat/wobbrik`, City batching scoped commit (dəqiq hash git log-dadır). Merge/main/push/deploy/CI yoxdur. App-wide performans/redesign bitməyib.

## Decisions

- Yeganə plan `docs/PLAN.md` §1.1/§2–3. Android+AppBackend işi icazəlidir; backend persistent feat/wobbrik-dədir. Main merge/push/deploy ayrıca göstəriş; CI yalnız ayrıca tələb ilə main-də lokal. Emulator açılmır, canlı yoxlama gate deyil.
- Auth/app/UID/deletion/blocked/rate-limit, account share lock, accepted geometry/provenance, revision/dedup/exact Ready/login epoch/sole journal/server wallet authority saxlanır. Schema/rules/mutation dəyişməyib. İlk sync recovery/reconcile+catalog olaraq qalır.
- Legacy individual detail64KiB və cache140000byte limitləri artırılmayıb. Cache persistence optional-dır; böyük bundle diskə sığmasa live read uğuru saxlanır. Strict old server include üçün bir əlavə probe/load çəkir.

## Checks performed

- Android normal core149/app37unit+assembleDebug/lintDebug PASS58s; son genişlənmiş core152PASS14s. Pagination, partial fallback/geometry, ID/revision mismatch, racing bracket, cache new/strict/legacy replay, account/revision/epoch isolation və401/403/429/500no-retry yoxlanıb.
- Real TLS5/5PASS7.1stest/12sGradle: cancellation, sameURLsocket isolation, market overlap; mövcud HTTPS source qorunur. DiffcheckPASS; ilkin working patch-in reverse-check-i handoff yenilənməzdən əvvəl PASS.
- Backend final production source full race/disposable PostgreSQL18PASS(platform26.371s), vet/buildPASS. Aggregate geometry test əlavə ediləndən sonra City4focusedPASS3.322s; bütün measured request-lər real middleware-dən keçir. Fixture rate history timing-dən əvvəl təmizlənir. DB dayandırılır.
- Review və evidence: `docs/reviews/2026-11-17-city-detail-batching.md`, `docs/reviews/city-batching/`. Əvvəlki uğursuz fixture/sandbox cəhdləri PASS sayılmır. UI dəyişmədiyindən yeni visual parity/render iddiası yoxdur.

## Remaining work

- Production/device latency, real workload/SQLcontention/payload distribution, authenticated Android→GoE2E, HTTPS libcore/DNS/connect/handshake/upload cancellation və pooling ölçülməyib. Tam app performansı/mərhələ qəbulu deyil.
- Əvvəlki atlas~60MiBretention, Home logo/font və Profil foreground/skin CPUişi, font2Long.MAXheaderclipping/navbar/scroll/grid və tam visual parity borcları qalır. Əvvəlki native10matrix və kontrast nəticələri resource review-dadır; bu vahiddə təkrarlanmayıb.

## Relevant files

- Android `android-billing/core/.../game/{CitySceneReads,GameplayHttpBackend,ReadOnlyGameCache}.kt`, `CitySceneReadsTest.kt`.
- AppBackend `internal/wobbrik/city.go`, `internal/platform/{wobbrik_http.go,wobbrik_city_details_test.go,wobbrik_city_batch_probe_test.go}`, `docs/wobbrik-game-api.md`.
- HTTPS ayrı dirty vahid: `docs/reviews/2026-11-17-https-scoped-cancellation.md`, `docs/reviews/https-overlap/`; Home/Profil əvvəlki review `2026-11-17-resource-background-city-https.md`.

## Exact next step

City vahidi lokal review/check-dən keçib; istifadəçiyə paired commit-ləri təqdim et. HTTPS dirty işini ayrıca saxla. Main merge/CI/push/deploy etmə. Yeni istifadəçi tapşırığı varsa yeni chatdə bu handoff və PLAN §1.1 əsasında yalnız həmin vahidi başla; production/device performansını yoxlanmış kimi göstərmə.
