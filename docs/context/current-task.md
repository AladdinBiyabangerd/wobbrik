# Mərhələ 3 — hesab/ekran hazırlığı, 5 noyabr 2026

## Completed
- Online host əvvəlki main-dədir; exact Ready/foreground və durable finish saxlanır.
- Application anonymous bootstrap əlavə edildi: mövcud sessiya saxlanır,
  currentUser yoxdursa guest yaradılır; auth state backend Ready deyil.
- Guest link sərhədi linkWithCredential + eyni UID-dir; wallet merge yoxdur.
- İstifadəçi çatışmayan asset-ləri əvvəl hazırlamağı seçdi; sadələşdirilmiş
  ekran alternativinə razılıq vermədi. Built-in imagegen asset hazırlığı var.

## Current state
- Plan vahid 2 TAMAM DEYİL. MainActivity hələ offline PracticeGame göstərir.
- docs/design/account-assets-v1/ altında şəhər/logo/hesab hero hazırdır;
  alpha-preview.png real token fonunda render edilib və görüntüyə baxılıb.
- Asset-lər Android resurslarına bağlanmayıb; ekran uyğunluğu yoxlanmayıb.
- Backend anonymous gameplay API-ni dəstəkləyir; API/schema dəyişikliyi lazım deyil.
- Əlaqəsiz untracked responsive-rule/compose-game-host faylları qorunub.

## Decisions
- Təsdiqlənmiş mobile-v1/game-tap-v2/tablet-v1 əsasdır; özbaşına sadələşdirmə yoxdur.
- Generativ asset extraction piksel eyniliyi deyil; layout review bunu yoxlamalıdır.
- Auth uğuru server sync uğuru deyil. Start/resume exact Ready ilə olmalıdır.
- Restart/abandon yoxdur; aktiv cəhd silinmir. Balanslar serverdən əvəz edilir.

## Remaining work
- Ana/hesab/recovery ekranlarını təsdiqlənmiş dizaynlarla implementasiya et.
- MainActivity-ni AcceptedEngineHost-a bağla; offline engine online-a ötürülməsin.
- Ready/Recovering/Error, explicit start/resume və server XP/level/coins/achievements.
- UI ikonları/dekor, auth retry və provider credential UI-si dəqiqləşdirilməlidir.
- Telefon/planşet renderini real dizaynlarla yanaşı müqayisə et; böyük şrift/landşaft.
- Sonra plan vahid 3 Room/outbox və vahid 4 real cihaz/PostgreSQL nasazlıq sübutları.

## Relevant files
- docs/stage-three-online-plan.md
- docs/reviews/2026-11-05-account-bootstrap-assets.md
- docs/design/account-assets-v1/{README.md,prompts.md,*.png}
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{WobbrikApplication,MainActivity,GameTokens,GameScene,SceneMotion}.kt
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/{AcceptedEngineHost,AcceptedAttemptCommands,AcceptedAttemptRestore}.kt
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{AuthenticatedRecoveryOwner,RecoveryDtos}.kt

## Checks performed
- Auth diff manual review, git diff --check; backend gameplay API compatibility read.
- Asset outputs visually inspected, PNG alpha metadata checked; UI render yoxdur.
- Reviewed commit 1b98d11, main merge 3541f13; main local CI keçdi (33s,
  106 task: 13 executed, 93 up-to-date). Canlı auth/DB sınağı yoxdur.

## Exact next step
- Review-də final CI/asset vəziyyətini oxu. ui-craft ilə tokens və
  mobile-v1/game-tap-v2/tablet-v1 referanslarına əsasən plan vahid 2-nin
  ekran bağlantısını implementasiya et. Yeni böyük araşdırmaya başlamadan
  scope-u bu vahidlə saxla; hazırlığı yenidən etmə.
