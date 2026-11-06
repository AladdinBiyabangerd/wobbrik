# Mərhələ 3 / vahid 2 — online ekran + Google link, 7 noyabr 2026

## Completed
- Branch `feat/stage-three-online-progression` main-dən açılıb; köhnə online-screens
  cherry-pick edilib (construction-cycle ilə birləşdirilib).
- MainActivity → OnlineGame → AcceptedEngineHost (start/frame/drop/resume/checkpoint).
- Credential Manager + `linkGuest` hesab ekranında bağlanıb; collision/cancel
  wallet birləşdirmir; eyni UID recovery epoch yenilənir.
- `wobbrik.googleWebClientId` BuildConfig property əlavə olunub.
- Google link + review qeydi commit olunub.

## Current state
- Vahid 2 hələ açıqdır: dizayn birəbir uyğunluğu, tablet rail, live Google/Console.
- `google-services.json`-də oauth_client/default_web_client_id yoxdur; live link
  üçün Console web client + Google provider ayrıca icazə tələb edir.
- Room keş/outbox (vahid 3) və fault proof (vahid 4) bu branch-də deyil.

## Decisions
- Link yalnız `linkWithCredential`; collision-da sign-in yoxdur.
- Console/SKU/deploy/push bağlı qalır.
- Auth uğuru Ready deyil; progression server snapshot-dandır.

## Remaining work
- Dizayn polish/render (mobile-v1 / tablet rail / account actions).
- Console web client icazəsi alınanda live link yoxla.
- Merge → yalnız istənəndə main CI.
- Sonra ayrıca branch: Room outbox, sonra inteqrasiya sübutu.

## Relevant files
- android-billing/app/.../{OnlineGame,GoogleGuestLink,WobbrikApplication,MainActivity}.kt
- android-billing/app/build.gradle.kts
- docs/reviews/2026-11-05-online-screens.md
- docs/stage-three-online-plan.md
- docs/design/mobile-v1/, docs/design/ (tablet account/primary)

## Checks performed
- `:app:compileDebugKotlin` + `:engine:test` BUILD SUCCESSFUL.
- Backend API/schema bu vahiddə dəyişməyib.
- Lokal diff review qeydi yenilənib; CI branch-də işlədilməyib.

## Exact next step
- Təsdiqlənmiş mobile/tablet account dizaynları ilə OnlineGame vizual uyğunluğunu
  bağla (ui-craft + render); sonra merge qərarı.
