# Mərhələ 3 başlanğıcı — 5 noyabr 2026

## Completed
- İstifadəçi mərhələ 3-ə başlamağı tapşırdı; mövcud kod/müqavilə yoxlandı.
- docs/stage-three-online-plan.md icra sırası və qəbul sərhədini verir.

## Current state
- Bu vahid araşdırma/plandır; yeni runtime kodu yoxdur.
- MainActivity offline engine göstərir; online host input bağlıdır.
- Firebase auth listener var, giriş axını və Room yoxdur.
- Əlaqəsiz untracked responsive-rule/compose-game-host faylları qorunub.

## Decisions
- İlk kod vahidi vizual dəyişikliksiz online engine idarəsidir.
- Server vahid wallet/progression authority-dir; offline nəticələr daşınmır.
- Plan üçün backend dəyişikliyi lazım deyil; kod zamanı yenidən yoxla.
- Aktiv attempt üçün səssiz restart/abandon yoxdur.

## Remaining work
- Online loop, terminal finish intent recovery, auth/UI, Room, cihaz/DB sübutu.
- Dizayn borcu qalır; deploy/SKU/Console daxil deyil.

## Relevant files
- docs/stage-three-online-plan.md; docs/kotlin-compose-plan.md §5–6.
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/AcceptedEngineHost.kt
- Eyni qovluqda AcceptedAttemptCommands.kt, AcceptedAttemptRestore.kt və testləri.
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AuthenticatedRecoveryOwner.kt
- Eyni qovluqda AttemptCoordinator.kt və RecoveryDtos.kt.
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/WobbrikApplication.kt və MainActivity.kt.
- AppBackend/docs/wobbrik-game-api.md; backend AGENTS/README/context oxunub.

## Checks performed
- Scoped source/contract review; yeni live auth/DB/device yoxlaması yoxdur.
- Docs review/main CI: docs/reviews/2026-11-05-stage-three-plan.md.

## Exact next step
- Planı və host/restore/commands testlərini oxu; feat/ branch-də online engine
  idarəsini implementasiya et. Auth/UI/Room-u eyni böyük dəyişiklikdə yığma.
