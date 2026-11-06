# Beş home çatışmazlığı — müqavilə auditi, 7 noyabr 2026

## Completed
- Rekord/XP üçün Wobbrik DTO, online home və AppBackend read/finish/Level/
  leaderboard müqaviləsi yoxlanıb. Audit: `docs/home-contract-audit.md`.
- Tablet rail asset-lərinin əvvəlki main merge-i saxlanıb; runtime inteqrasiyası deyil.

## Current state
- Beş tələb implementasiya edilməyib. Bu vahid yalnız müqavilə auditidir.
- Online ekran/Google guest link işi `feat/stage-three-online-progression`
  branch-ində, b7d97a4-də qorunur; main-ə merge edilməyib.
- AppBackend main oxunub, dəyişdirilməyib. Rekord və XP intervalı üçün backend
  read müqaviləsinə additive sahələr lazımdır; hazır sync bunları vermir.
- Google provider/web client Console konfiqurasiyası açıqdır.

## Decisions
- Rekord tamamlanmış online nəticələrin maksimum mərtəbəsidir; sub-ten və
  continued daxildir. Aktiv checkpoint/offline/inventar sayından çıxarılmır.
- L başlanğıcı 25×L×(L−1), növbəti hədd 25×L×(L+1). Təklif olunan müqavilə:
  best_floors integer, level_start_xp/next_level_xp decimal string; overflow
  və köhnə serverdə unavailable davranışı testlərlə bağlanmalıdır.
- Təsdiqlənmiş dizaynlar və tokenlər qorunur. City placement açılmır;
  geometry qərarları açıqdır. Real SKU/Console/deploy/push ayrıca göstəriş tələb edir.

## Remaining work
1. Backend read müqaviləsi + mobil DTO/test uyğunluğu (UI-siz kiçik vahid).
2. Real rekord və XP barının təsdiqlənmiş home-a inteqrasiyası.
3. Inventory API-dən dəqiq bina sayı; pagination/concurrent revision yoxlaması.
4. Real Parametrlər ekranı və preference/navigation.
5. Tablet home Şəhərim real read/navigation axını və rail inteqrasiyası.
- Əvvəlki Room cache/outbox/fault proof işi də tamamlanmayıb.

## Relevant files
- docs/home-contract-audit.md — müqavilə tapıntıları, mərhələlər, qəbul yoxlamaları.
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{RecoveryDtos,AttemptCoordinator}.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{OnlineGame,GameTokens}.kt
- docs/design/{mobile-v1,tablet-v1,game-tap-v2,tablet-rail-icons-v1}/
- docs/kotlin-compose-plan.md §5; docs/stage-three-online-plan.md
- /Users/mac/My Workspace/AppBackend/{AGENTS.md,README.md,docs/context/current-task.md}
- AppBackend internal/wobbrik/{read,attempts,finish,leaderboard}.go və docs/wobbrik-game-api.md

## Checks performed
- Mənbə/müqavilə yoxlaması; runtime/API/DB test icra edilməyib.
- Vizual dəyişiklik/render yoxdur; dizayn uyğunluğu iddia edilmir.
- Audit branch-inin review/CI nəticəsi docs/reviews/2026-11-07-home-contract-audit.md-dədir.

## Exact next step
- Bu qeyd və audit sənədini oxu. AppBackend-də ayrıca feat branch-də sync və
  attempt-read üçün best_floors + XP intervalı implementasiya et; real PostgreSQL
  izolyasiya/retry/boundary testləri və backend yoxlamalarını tamamla.
- Mobil DTO işini online branch-dən ayrıca feat branch-də et; main-ə hələ
  review edilməmiş online işi dolayı yolla merge etmə. Branch review-dan sonra
  tarix saxlanılmaqla main merge; Wobbrik CI yalnız main-də.
- Main hazırda /Users/mac/My Workspace/Wobbrik-rail-icons worktree-sindədir;
  həmin worktree-nin təmizliyini yoxla. Cari Wobbrik worktree online branch-ə qaytarılır.
