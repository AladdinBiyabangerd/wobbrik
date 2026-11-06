# Home müqaviləsi tamamlandı — 7 noyabr 2026

## Completed
- Birinci vahid: AppBackend rekord/XP intervalı müqaviləsi + mobil DTO uyğunluğu.
- Backend feat/home-read-contract `8ec40ec` review edilib main-ə tarix saxlanılmaqla merge olundu (`7bd5463`). Sync və attempt-read eyni account lock altında best_floors integer, level_start_xp/next_level_xp decimal string verir.
- Wobbrik feat/home-recovery-contract `8b1376f` main əsasında hazırlanıb review edildi və main-ə merge olundu (`8c1a0d7`); əvvəlki online implementasiya dolayı yolla main-ə daxil edilmədi.
- Mobil AccountRecovery.bestFloors nullable Long, xpInterval nullable XpInterval(BigInteger start/next). Köhnə serverdə unavailable; malformed/partial/mismatched interval fail closed.

## Current state
- Bu vahiddə UI dəyişməyib; rekord və XP barı hələ home-a bağlanmayıb.
- Əvvəlki online ekran/Google guest link işi feat/stage-three-online-progression branch-ində qorunur; main-ə merge edilməyib. Müqavilə main-i bu branch-ə inteqrasiya etmək olar; əks istiqamətdə böyük merge etmə.
- AppBackend main təmizdir; real DB suite keçib, disposable 55448 klasteri dayandırılıb. Deploy/push yoxdur.
- Google provider/web client Console konfiqurasiyası, Room/outbox/fault proof işi açıqdır.

## Decisions
- Rekord bütün tamamlanmış online nəticələrin maksimumudur; sub-ten/continued daxildir. Aktiv checkpoint/offline/inventar rekord deyil; satış/silmə rekordu azaltmır.
- L intervalı: 25×L×(L−1) ≤ xp < 25×L×(L+1). MaxInt64 XP-də next=9223372052185252500; əvvəl BigInteger fərqi, sonra yalnız render payı float-a çevrilməlidir.
- wobbrik-v1, XP/reward, revision/dedup receipt və vahid wallet dəyişməyib. Yeni schema lazım olmadı. Kiçik fixture query planı mövcud owner-prefix indeksindən istifadə etdi; load benchmark deyil.
- Təsdiqlənmiş dizaynlar/tokenlər məcburidir. City placement bağlıdır, geometry ölçüləri açıqdır. SKU/Console/deploy/push ayrıca göstəriş tələb edir.

## Remaining work
1. Real rekord və XP barını təsdiqlənmiş telefon/tablet home-a bağla (növbəti vahid).
2. Inventory API-dən dəqiq bina sayı, pagination/revision uyğunluğu.
3. Real Parametrlər ekranı, preference/navigation.
4. Tablet home Şəhərim read/navigation və rail runtime inteqrasiyası.
- Tam oyun/domain adapteri, offline recovery/outbox və digər məlum borclar bu müqavilə işi ilə tamamlanmır.

## Relevant files
- docs/home-contract-audit.md; docs/reviews/2026-11-07-home-recovery-contract.md
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{RecoveryDtos,AttemptCoordinator}.kt
- android-billing/core/src/test/kotlin/appbackend/wobbrik/game/RecoveryDtosTest.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{OnlineGame,GameTokens}.kt (OnlineGame online branch-də)
- docs/design/mobile-v1/{mobile-primary-v2.png,tokens.css}; docs/design/tablet-v1/{README.md,primary-landscape.png}; docs/design/game-tap-v2/
- docs/kotlin-compose-plan.md §5; docs/stage-three-online-plan.md
- /Users/mac/My Workspace/AppBackend/docs/{wobbrik-game-api.md,reviews/2026-11-07-home-read-contract.md,context/current-task.md}

## Checks performed
- Wobbrik main `./scripts/local-ci.sh` PASS: 44 core + 45 engine testi, failure/error/skip yoxdur; Android/app assemble və lint PASS. Log=/private/tmp/wobbrik-home-contract-ci.log.
- Backend go vet + native build PASS; real PostgreSQL 18.6 ilə full go test -race -count=1 -json ./... PASS: 79 əsas test (60 integration), alt testlərlə 101 PASS; test skip/failure yoxdur, 3 paket testsizdir.
- Backend: zero/sub-ten/lower/higher/continued finish, finish/read race, retry/recovery, blocked/foreign, satış/silmə, izolyasiya və read mutation invariants. XP 0/49/50/149/150/MaxInt64 hər iki tərəfdə test edilib.
- Backend log=/private/tmp/wobbrik-home-backend-tests.jsonl; query plan 4 row/2 shared hit/0.018 ms. Load/provider/device/deploy yoxlanmayıb.
- Vizual dəyişiklik və render yoxdur; dizayna uyğunluq iddia edilmir.

## Exact next step
- Bu qeyd və home auditini oxu. Müqavilə main-i online branch-ə merge edildikdən sonra online branch-dən ayrıca feat/home-record-xp-ui branch yarat; əvvəlki online işi main-ə dolayı merge etmə.
- ui-craft skill-i oxu, təsdiqlənmiş telefon/tablet şəkillərinə və mövcud tokenlərə bax. Mövcud home-da server bestFloors/xpInterval göstər; missing sahələrdə unavailable davranışını saxla.
- Telefon və planşet renderini uyğun təsdiqlənmiş dizaynla yanaşı yoxla. Bu UI vahidini review et; online branch tarixinin main-ə inteqrasiyası ayrıca tam review tələb edir. CI yalnız main-də.
- Main /Users/mac/My Workspace/Wobbrik-rail-icons worktree-sindədir; cari Wobbrik worktree online branch-də saxlanır.
