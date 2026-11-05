# Mərhələ 3 — online host vahidi, 5 noyabr 2026

## Completed
- AcceptedEngineHost recovery/commands ilə online resume/frame/drop-a bağlandı.
- İlk settled tick-də pause/checkpoint; yeni Ready həmişə paused restore olur.
- Accepted FINISHED checkpoint davamlı finish niyyətidir; application Ready
  observer-i proses yenidən başlayanda da finish-i mövcud journal ilə göndərir.
- Backend API/catalog uyğunluğu yoxlandı; backend/API/schema dəyişikliyi lazım deyil.

## Current state
- MainActivity hələ ayrıca offline engine göstərir; UI/auth bağlantısı növbəti vahiddir.
- Online host yalnız exact Ready + foreground explicit resume ilə açılır.
- Terminal finish pulsuz base_6 hissələri istifadə edir; custom seçim hələ yoxdur.
- Əlaqəsiz untracked responsive-rule/compose-game-host faylları qorunub.

## Decisions
- Server wallet/progression authority-dir; offline proposal online-a keçmir.
- FINISHED checkpoint iki command arasındakı davamlı niyyətdir.
- Restart/abandon yoxdur; continued/unsupported checkpoint fail-closed qalır.
- Recovery callback daxilində command dispatch edilmir; host monitoru xaricindədir.

## Remaining work
- Plan vahid 2: Firebase anonymous bootstrap və təsdiqlənmiş dizaynla ekran bağlantısı.
- Sonra Room migration/outbox və real cihaz/PostgreSQL nasazlıq sübutları.
- Custom parts/continue qərarı və mövcud dizayn borcu açıqdır.

## Relevant files
- docs/stage-three-online-plan.md
- docs/reviews/2026-11-05-online-engine-host.md
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/AcceptedEngineHost.kt
- android-billing/engine/src/test/kotlin/com/aladdin/wobbrik/engine/AcceptedAttemptCommandsTest.kt
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/AuthenticatedRecoveryOwner.kt
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/WobbrikApplication.kt

## Checks performed
- Diff review, git diff --check; ilk merge sonrası main local CI keçdi.
- Final callback-identity düzəlişinin main CI nəticəsi review qeydində saxlanılır.
- Fake backend/disk tests; live auth/DB/device yoxlanmayıb. Vizual dəyişiklik yoxdur.

## Exact next step
- Review-də final CI nəticəsini oxu. Yeni chatdə plan vahid 2-ni ui-craft,
  təsdiqlənmiş dizaynlar və scoped auth/UI mənbələrindən başlayaraq implementasiya et.
