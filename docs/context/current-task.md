# Home alignment + şəhər scope-u — 8 noyabr 2026

## Completed
- Əvvəlki home server rekord/XP vahidi online branch-dədir (`a374db4`, əvvəlki review saxlanır).
- Bu vahiddə HomeScreen.kt təqdimatı əlavə edildi: telefon home, geniş pəncərədə hero/idarəetmə, planşet rail; böyük şriftdə köməkçi düymələr ardıcıl. Hazır server snapshot və play/resume callback-ləri saxlanır.
- Fokuslu build/install, kontrast və beş pəncərə/font renderi yoxlanıldı. Bu tam dizayn qəbulu deyil.

## Current state
- Branch `feat/home-design-alignment`, baza online `a230b3b`; WIP, main/online-a merge yoxdur. CI işlədilməyib.
- Home Ready və loading/error eyni adaptiv çərçivədədir. Son davam düzəlişi başlıq şüaları, portret tablet yan-yana/equal-height CTA və əsas düymənin daxili işıq kənarını əlavə edir.
- Şəhərim/İnventar/Bazar hazır route olmadığından müvəqqəti deaktivdir. Bu yekun scope deyil, istifadəçi deaktiv keçidlərlə işi bitirməyi seçməyib.
- İstifadəçi bu chatdə real şəhər axınını **və tam yerləşdirmənin qərarlarını açmağı** scope-a əlavə edib. Bu icazə davam edir; sadəcə read-only şəhərlə işi bitmiş sayma.
- Backend yoxlanıb: GET city membership/population/revision/pagination və return var; placement_enabled:false. Backend dəyişdirilməyib.

## Decisions
- Mövcud təsdiqlənmiş mobile/tablet dizaynları əsasdır; alternativ dizayn təsdiqlənməyib. İstifadəçi sistem şriftini açıq şəkildə razılaşdırılmış istisna kimi saxladı; bu sualı yenidən açma.
- Cari planın ilk icra göstərişi ölçüləri versiyalı oyun parametrləri kimi seçməyə icazə verir. Köhnə “ölçülər açıqdır” qeydini yenidən icazə gözləməyə çevirmə. Yeni ölçü/model hələ seçilməyib.
- Server authority qalır; home_city dekorativdir, real hesab şəhəri deyil. Opaque structural-v1-dən etibarlı footprint çıxarılmır.
- Yeni home UI online branch üzərindədir; main-ə dolayı online merge etmə. SKU/Console/push/deploy yoxdur.

## Remaining work
- Home tam uyğunluq hələ qəbul edilməyib: dəqiq nisbətlər, ikon miqyası, runtime asset detalları, fon/işıq və bevel effekti, XP sətrinin kart hündürlüyünə təsiri. Başlıq işarələri, portret yan-yana CTA və loading/error çərçivəsi düzəlib; font təsdiqlənmiş istisnadır.
- Real şəhər: versiyalı typed floor geometry/provenance, integer-tick dəqiqlik/dönmə/normalization, bina/torpaq/12 dekor footprint-ləri; union/transform/containment/overlap; atomik place/move/replace/return və dekor copy ownership; Android renderer/naviqasiya; backend+mobile testləri.
- Legacy geometry etibarlı əldə olunana qədər inventory-only; serverlə razılaşdırılmamış düz bina forması uydurma.
- Canlı home sync/finish e2e, real cihaz/TalkBack; inventory pagination, parametrlər, Room/outbox/fault proof ayrıca açıqdır.

## Relevant files
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{HomeScreen,OnlineGame,GameTokens}.kt
- docs/reviews/2026-11-08-home-design-alignment.md; docs/reviews/2026-11-08-home-record-xp-ui.md
- docs/design/mobile-v1/{mobile-primary-v2.png,tokens.css}; docs/design/tablet-v1/{primary-landscape.png,portrait.png,README.md}
- docs/kotlin-compose-plan.md (ilk cari icra göstərişi, §5, §6); docs/stage-three-online-plan.md; docs/game-plan.md §7
- /Users/mac/My Workspace/AppBackend/docs/wobbrik-game-api.md şəhər və geometry bölmələri; AppBackend AGENTS.md backend kodundan əvvəl oxunmalıdır.
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{GameplayHttpBackend,RecoveryDtos}.kt

## Checks performed
- Son kod :app:installDebug --offline PASS; /tmp/wobbrik-home-followup-build.log. CI deyil. Son diff HomeScreen.kt + OnlineGame.kt; ayrıca lokal source review home review-a əlavə edilib.
- git diff --check PASS. Text/Surface 13.00, Muted/Surface 8.22, Background/Accent 11.45 PASS.
- Əvvəlki kod emulator renderlərinə baxıldı: 1080×2400 @420 font1.0/1.5; 1280×800 və 800×1280 @160 font1.0; 1600×900 @240 font1.5. Normal nümunədə mətn/idarəetmə kəsilməsi görünmür; böyük font köməkçi düymələri ayrı sətirlərdədir.
- Renderlər/yan-yana müqayisələr gitignored docs/reviews/home-design-alignment/. İlkin batch splash capture idi, yenidən am start -W + render gözləməsi ilə çəkilib əvəz edildi. Ölçü/density/font override-ları reset/1.0-a bərpa edildi.
- Frozen fixture canlı backend/auth/start/finish sübutu deyil. Tam dizayn qəbul deyil; CI/main merge/push/deploy yoxdur.

- Davam renderləri: home-design-alignment/final-{phone,portrait,landscape,large,error,loading}.png; telefon, portret/geniş planşet, font1.5, error/loading baxıldı. Ətraflı ölçü və məhdudiyyətlər home review-un son bölməsindədir. Tam dizayn PASS deyil.

## Exact next step
- Bu handoff və home review-un son davam bölməsini oxu; hazır branch işini qoru. Home-un qalan nisbət/asset/işıq fərqlərini təsdiqlənmiş lövhələrlə tamamla; sistem şriftini yenidən bloklayıcı etmə. Sonra artıq təsdiqlənmiş tam şəhər scope-unu AppBackend və Android-də icra et; yeni scope icazəsi lazım deyil. ui-craft render qaydası qüvvədədir.
- Ayrı məntiqi city vahidi üçün AppBackend AGENTS.md və yalnız mövcud şəhər/engine geometry fayllarını yoxla; vahid versiyalı geometry qərarını mövcud canonical plana qeyd et, ayrıca paralel master plan yaratma. Tam placement artıq scope-dadır, read-only ilə scope-u azaltma.
- Online diff bütöv review olunmadan main-ə merge etmə. Main worktree /Users/mac/My Workspace/Wobbrik-rail-icons; CI yalnız main-də ./scripts/local-ci.sh.
