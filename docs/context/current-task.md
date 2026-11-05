# Attempt recovery core — 5 noyabr 2026

## Completed
- AppBackend AGENTS/README/context və gameplay API, Compose host və dizayn davranışı oxundu.
- Transportdan asılı olmayan AttemptCoordinator əlavə edildi: UID/login epoch, serial mutation, göndərmədən əvvəl journal save, exact retry, definitive revision conflict sonrası sync, receipt sonrası sync və equal-revision wallet replacement.
- Beş məqsədli regression testi əlavə edildi; durable disk/process yoxlaması deyil.

## Current state
- Koordinator hələ Compose hosta bağlı deyil. HTTP/Firebase adapteri, durable journal, tam DTO və engine checkpoint codec yoxdur. Yekun oyun axını hazır deyil.
- Əvvəlki uncommitted host/build/rule/review dəyişiklikləri saxlanır; bu vahidə commit edilmir.
- MainActivity hələ PracticeEngine, sxematik Canvas, offline məşq menyusu və session rekord istifadə edir; process death yeni cəhd yaradır. Dizayna uyğunluğu təsdiqlənməyib.

## Decisions
- Authority yalnız AppBackend/PostgreSQL; lokal mükafat əlavə edilmir.
- Bu core vahidi mövcud API ilə uyğundur; backend/schema/rules dəyişiklik tələb etmir. Tam adapter uyğunluğu hələ yoxlanmayıb.
- API-də abandon yoxdur. Restart/menu davranışını finish/mükafat kimi yozmaq olmaz; hosta bağlamadan əvvəl lifecycle müqaviləsi həll edilməlidir.
- Real SKU/Console/deploy/push üçün ayrıca göstəriş gözlənir.

## Remaining work
- Typed HTTP DTO/envelope serializer və authenticated backend adapter; per-UID atomik durable journal və backup istisnası.
- Tam account/attempt DTO, engine checkpoint roundtrip/validation, host auth bootstrap və input gating/recovery.
- Restart/abandon server davranışı; təsdiqlənmiş qrafika və real menyu/naviqasiya; telefon/planşet dizayn müqayisəsi və cihaz yoxlamaları.

## Relevant files
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{AttemptCoordinator.kt,README.md}
- android-billing/core/src/test/kotlin/appbackend/wobbrik/game/AttemptCoordinatorTest.kt
- docs/reviews/2026-11-05-attempt-recovery-core.md
- android-billing/app/src/main/kotlin/az/wobbrik/game/{MainActivity,GameTokens}.kt; app/README.md
- docs/kotlin-compose-plan.md §5; docs/design/{mobile-v1,game-tap-v2}/
- /Users/mac/My Workspace/AppBackend/docs/wobbrik-game-api.md və AGENTS.md

## Checks performed
- Lokal source review və diff whitespace check keçib; branch-də CI işlədilməyib.
- Main merge sonrası CI nəticəsi bu vahidin review qeydində yazılacaq.
- UI dəyişdirilməyib/render edilməyib. Backend dəyişdirilməyib/test suite işlədilməyib.

## Exact next step
- Bu qeydi, core README və review qeydini oxu. Əvvəlki dirty hostu qoru.
- Növbəti kiçik vahid: mövcud billing AppBackendClient/AccountSessions sərhədlərini istifadə edərək typed gameplay HTTP adapter və durable per-UID attempt journal qur; exact wire retry, login epoch, timeout/conflict və real disk recreation testləri ilə yoxla. Branch-də review, main-ə merge, yalnız sonra main-də lokal CI.
- Sonra ayrıca vahiddə checkpoint codec və host lifecycle bağlanması; restart/abandon üçün server müqaviləsini əvvəl həll et. Placeholder qrafikanı yekun dizayn sayma.
