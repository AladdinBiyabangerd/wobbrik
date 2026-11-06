# Əsas tikinti dövrü — Compose uyğunluğu + FPS, 6 noyabr 2026

## Completed
- Branch `feat/construction-cycle-compose` main-dən açılıb.
- GameScene framing/guide/cable polish; HUD/fasilə token polish; FrameTiming.
- Emulator capture matrisi və kontrast yoxlaması; review yazılıb.

## Current state
- Offline PracticeGame hələ hostdur; online 3.4 açıqdır.
- Compose səhnə game-tap-v2-yə əvvəlkindən yaxındır; yeni garden/sky art yoxdur.
- FrameTiming emulator soft-GPU-da ~19 FPS göstərir — fiziki cihaz ölçüsü yoxdur.

## Decisions
- Fizika vahidləri dəyişmədi; yalnız presentation framing/HUD/metrics.
- Yeni sprite olmadan glow/cable draw ilə yaxınlaşma; konsept işığı açıq borc.
- Online AcceptedEngineHost bu branch-ə daxil deyil.

## Remaining work
- Branch lokal review bitəndən sonra main-ə merge → yalnız main-də lokal CI.
- Fiziki zəif/orta cihazda release-tipli 60 Hz P95/P99/jank/RAM/startup.
- Sonra mərhələ 3.4: onlayn simulyasiya + checkpoint dövrü.

## Relevant files
- android-billing/app/.../{GameScene,MainActivity,GameTokens,FrameTiming}.kt
- docs/reviews/2026-11-06-construction-cycle-compose.md
- docs/reviews/construction-cycle-compose/
- docs/design/game-tap-v2/

## Checks performed
- assembleDebug/installDebug (Studio JBR); contrast text pairs ≥4.5.
- Emulator captures inspected; pause hierarchy + cyan pixels confirmed.
- FrameTiming logcat on Pixel_6 swiftshader (not a 60 FPS claim).

## Exact next step
- Diff review → merge `feat/construction-cycle-compose` → `./scripts/local-ci.sh` on main.
- Separately: physical-device FrameTiming run, then plan unit / stage 3.4 online host.
