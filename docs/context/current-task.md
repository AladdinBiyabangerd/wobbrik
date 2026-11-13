# Cari handoff — 13 noyabr 2026

## Completed
- Checkpoint fix `92d640d`, branch `fix/end-of-attempt-checkpoint`: uğurlu eniş lokal davam edir; məğlubiyyətdə bir yekun checkpoint, sonra explicit finish. Review: 2026-11-13-end-of-attempt-checkpoint.md.
- Bunun üzərində ayrıca `fix/navigation-consistency`: finish sonrası köhnə game route-un ikinci home şablonuna düşməsi düzəldildi; eyni kanonik HomeScreen istifadə olunur. Əlavə səhifələr key ilə scroll/local state baxımından ayrıldı.
- Son açıq qərar tətbiq edildi: aktiv oyunda Android Geri → pauza; təkrar Geri də çıxmır. Menyuya qayıt → home/abandon. Finish picker Geri → home/niyyət qalır.
- Koddan düymə→ekran PNG xəritələri hazırlandı, açılıb baxıldı: docs/reviews/navigation-consistency/{01-pages,02-game}.png.

## Current state
- Cari branch fix/navigation-consistency əvvəlki fix commit-i üzərindədir; heç biri main-ə merge/push edilməyib.
- AppBackend və UI vizual layout/asset/token dəyişməyib. Yeni ekran yaradılmayıb. Runtime/Android render unverified; emulator açılmayıb.

## Decisions
- docs/PLAN.md yeganə plan; §3 son Geri→pauza qərarını qeyd edir. Əvvəlki Geri→çıxış bununla əvəz olunub.
- Əvvəl Android, backend dəyişiklikləri ayrıca. Mövcud vizuallar qalır, əlavə variantlar iki ilə məhdud; konkret asset seçimi sonrakı vahiddə.
- CI yalnız ayrıca tələb ilə main-də; merge üçün istifadəçi təsdiqi lazımdır. Canlı yoxlama gate deyil.

## Remaining work
- Vizual borc: kolleksiya/nailiyyətlər ümumi hesab şablonudur, ayrıca tam feature ekranları deyil. Şəhər/İnventar/Bazar deaktivdir. Online pauzada Yenidən başla çatışmır. Bunlar hazır göstərilmir.
- Mövcud dizaynın konkret təsdiqi olmadan UI redesign edilməməlidir. Mərhələ 4 başlanmayıb.

## Relevant files
- docs/PLAN.md; docs/reviews/2026-11-13-navigation-consistency.md və navigation-consistency/ PNG/script.
- app OnlineGame.kt, GameNavigation.kt, GameNavigationTest.kt; əvvəlki fix engine AcceptedEngineHost.kt.

## Checks performed
- Son fokuslu app 3 unit test/build/lint PASS (32s); engine 64/core 70 test PASS (up-to-date). git diff --check PASS. PNG-lər vizual yoxlanıb; Android render deyil.
- Backend dəyişmədiyindən backend test/deploy yoxdur. Emulator/ADB/canlı yoxlama və CI yoxdur.

## Exact next step
- Naviqasiya fix nəticəsi və PNG-ləri istifadəçiyə təqdim et; main merge yalnız təsdiqdən sonra. Sonrakı chatdə qalan səhifə şablon borcunu mövcud dizaynla tutuşdur; docs/PLAN.md əsasdır, mərhələ 4-ə özbaşına keçmə.
