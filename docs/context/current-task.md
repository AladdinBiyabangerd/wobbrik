# Mərhələ 3 / vahid 2 — online ekran + progression, 6 noyabr 2026

## Completed
- Branch `feat/stage-three-online-progression` main-dən (construction-cycle daxil)
  açılıb; əvvəlki `feat/stage-three-online-screens` işi cherry-pick edilib.
- MainActivity offline PracticeSession yaratmır; OnlineGame application-owned
  AcceptedEngineHost frame/drop/resume və typed start axınına bağlıdır.
- Hazır şəhər/logo/hesab art + UI ikonları/recovery hero Android resurslarındadır.
- Compose framing/FrameTiming main-də qalır; PracticeGame yalnız debug fixture üçündür.

## Current state
- Vahid 2 hələ açıqdır: Google guest link UI və dizayn birəbir uyğunluğu qalıb.
- Room keş/outbox (vahid 3) və inteqrasiya sübutu (vahid 4) bu branch-ə daxil deyil.
- Backend origin/canlı Firebase bu APK-da sübut edilməyib.

## Decisions
- Eyni qonaq UID Google credential ilə `linkGuest`-ə bağlanır; wallet birləşməsi yoxdur.
- Console/SKU/deploy/push ayrıca göstərişədək bağlıdır.
- Auth uğuru Ready deyil; balans/XP/nailiyyət yalnız server snapshot-dandır.
- AppBackend API/schema bu UI bağlantısı üçün dəyişmir.

## Remaining work
- Google credential alınmasını hesab ekranında `linkGuest`-ə bağla.
- Təsdiqlənmiş mobile-v1/tablet-v1/game-tap-v2 ilə qalan vizual fərqləri bağla.
- Diff review → merge → yalnız istənəndə main-də `./scripts/local-ci.sh`.
- Sonra ayrıca branch-lərdə Room outbox və fault proof.

## Relevant files
- android-billing/app/.../{MainActivity,OnlineGame,GameTokens,WobbrikApplication}.kt
- android-billing/engine/.../AcceptedEngineHost.kt
- docs/reviews/2026-11-05-online-screens.md
- docs/stage-three-online-plan.md
- docs/design/account-assets-v1/

## Checks performed
- Cherry-pick conflict: GameTokens (ExpandedWidth + PanelElevation) və MainActivity
  (OnlineGame host; PracticeGame FrameTiming saxlanıb) əl ilə birləşdirilib.
- Build/test bu handoff yazılana qədər hələ yenidən işlədilməyib.

## Exact next step
- Google Sign-In credential axınını mövcud `linkGuest`-ə bağla (Console dəyişmə).
- Sonra eyni vahid 2 daxilində açıq dizayn tapıntılarını bağla və review et.
