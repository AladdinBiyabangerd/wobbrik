# Cari handoff — 13 noyabr 2026

## Completed
- Mobil portret kilidi MainActivity manifestində tətbiq edildi; mobil əvvəl / planşet sonra qərarı qayda güzgüsü və əsas plana yazıldı.
- Silent finish home: qəbul edilmiş hesab keşi olduqda Recovering loading panelini göstərmir; terminal checkpoint/finish arasında oyun səhnəsi geri görünmür. Oyna finish təsdiqinədək deaktivdir; rəqəmlər server Ready nəticəsi ilə yenilənir.
- Checkpoint `92d640d`, navigation `3130cce`, kosmetik qərar `8a718da` üzərində avtomatik finish/default görünüş vahidi tətbiq edildi.
- Kolleksiya təsdiqli mövcud kosmetik picker-i açır; Təsdiqlə cihazda hesab üzrə daimi default saxlayır, Geri saxlamadan home-a qayıdır.
- Cəhdin görünüşü ayrıca account/attempt açarı ilə capture edilir; kataloq/unlock fallback var. Default dəyişməsi başlanmış cəhdi dəyişmir.
- Terminal loss → lazım olsa checkpoint → yeni exact Ready ilə avtomatik finish → canonical home. Sıfır mərtəbədə checkpoint yoxdur. Mövcud durable journal/retry saxlanır.
- Aktiv Canvas cəhdin divar/pəncərə/giriş/işıq/dam atlaslarını istifadə edir. Yeni layout/asset/token yoxdur.

## Current state
- Son vahid review: `docs/reviews/2026-11-13-silent-finish-home.md`; mobil presentation düzəlişidir, backend müqaviləsi dəyişməyib.
- Branch `fix/navigation-consistency`; main merge/push edilməyib. İstifadəçinin untracked `android-billing/app/release/` faylları saxlanıb.
- Review: `docs/reviews/2026-11-13-default-cosmetics.md`; naviqasiya PNG/script-ləri yenilənib, Android screenshot deyil.

## Decisions
- 13 noyabr əlavə qərarı: mobil yalnız portret rejimində olacaq; mobil landşaft dizaynı və yoxlaması tələb edilmir. Müxtəlif portret ölçüləri və font/a11y uyğunluğu saxlanır. MainActivity manifestində portrait kilidi tətbiq edilib; runtime yoxlanmayıb.
- 13 noyabr: mobil tam bitmədən planşet dizaynına keçilmir; hər mobil vahid üçün planşet dizaynı hazırlanmaz. Planşet işi mobil tamamlandıqdan sonra ayrıca istifadəçi tapşırığı ilə başlayır. Qayda AGENTS.md, visual-work.mdc və PLAN.md-də qeyd edilib.
- İstifadəçi bu chatdə mövcud finish-cosmetics-v1 dizaynını Kolleksiyadan daimi seçim kimi açmağı təsdiqləyib.
- AppBackend dəyişdirilmir; emulator açılmır; CI yalnız ayrıca tələb ilə main-də. Canlı yoxlama gate deyil.
- Yeganə plan `docs/PLAN.md`; mərhələ 4-ə keçilmir. Merge istifadəçi təsdiqi tələb edir.

## Remaining work
- Android runtime/render yoxlanmayıb; atlas anchor/dizayn uyğunluğu üçün vizual PASS yoxdur. Bütün telefon/planşet/orientasiya/font/inset/accessibility render halları yoxlanmamışdır.
- Default cihazda account-scoped saxlanır; cihazlararası sync yoxdur.
- Nailiyyətlər ümumi hesab şablonudur; Şəhər/İnventar/Bazar deaktivdir; online pauzada Yenidən başla əvvəlki borcdur.

## Relevant files
- docs/PLAN.md §3; docs/reviews/2026-11-13-default-cosmetics.md.
- engine AcceptedEngineHost.kt, CosmeticDefaultsStore.kt, AcceptedAttemptCommandsTest.kt.
- app AndroidCosmeticDefaultsStore.kt, WobbrikApplication.kt, OnlineGame.kt, FinishCosmeticScreen.kt, GameScene.kt.

## Checks performed
- Mobil portret: assembleDebug və lintDebug PASS; merged manifest-də portrait yoxlanıb; diff check PASS. Review: `docs/reviews/2026-11-13-mobile-portrait.md`. Runtime/render yoxlanmayıb.
- Silent finish vahidi: app unit, core/engine unit (up-to-date), assembleDebug və lintDebug PASS; diff check PASS. Android runtime/render və performans ölçümü yoxdur; CI işə salınmayıb.
- Fokuslu assembleDebug, engine 67 test, app unit test, lintDebug PASS; son exact-Ready dəyişiklikdən sonra yenilənib (app 3 test).
- git diff --check PASS. Runtime/render, backend tests, CI, emulator/ADB yoxdur.

## Exact next step
- Mobil portret kilidi və qayda qeydlərinin commit-ini review et; yeni böyük vahidi yeni chatdə davam et.
- Silent finish home diff/commit-ini istifadəçiyə review üçün təqdim et. Merge yalnız açıq təsdiqdən sonra; CI ayrıca göstərişsiz işləmir. Növbəti məhsul vahidinə özbaşına başlama.
