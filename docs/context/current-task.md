# Home rekord/XP UI — 8 noyabr 2026

## Completed
- Backend/home DTO müqaviləsi əvvəlki vahiddə tamamlanıb; online branch-ə `8287f26` ilə inteqrasiya olunub.
- `feat/home-record-xp-ui` / `a374db4`: home real `account.bestFloors` və `xpInterval` oxuyur. BigInteger fərqləri, render üçün DECIMAL64→Float; null → unavailable, lokal hədd/saxta sıfır yoxdur.
- Təsdiqlənmiş crown asset runtime-a əlavə edilib. Debug fixture qaydaya uyğunlaşdırılıb; legacy/zero/max halları əlavə edilib.
- Telefon və tablet renderləri təsdiqlənmiş home lövhələri ilə yanaşı müqayisə olunub; review yazılıb.

## Current state
- UI vahidi lokal review-dan sonra tarix saxlanılmaqla `feat/stage-three-online-progression`-a merge edilir. Main-ə dolayı online merge yoxdur; CI işlədilməyib.
- Data binding implementasiya olunub, tam home dizayn uyğunluğu və canlı end-to-end qəbul tamamlanmayıb.
- Backend dəyişikliyi tələb olunmur; mövcud sync/attempt-read müqaviləsi kifayətdir.

## Decisions
- Rekord yalnız server recovery-dəndir; aktiv cəhd/inventar/offline rekord deyil.
- XP mətni səviyyə daxilində qazanılmış/tələb olunan XP-dir. Account səhifəsində ümumi XP saxlanır.
- SKU/Console/deploy/push yoxdur. Online branch-in main inteqrasiyası ayrıca tam review tələb edir.

## Remaining work
- Home-un təsdiqlənmiş dizaynla əvvəlki fərqləri: logo/hero/tipoqrafiya/boşluq/düymə effektləri; tablet rail və Şəhərim runtime girişi. Bunlar təsdiqlənmiş istisna deyil.
- Canlı server sync/finish-dən sonra rekord və XP yenilənməsinin end-to-end yoxlaması; real cihaz, TalkBack, qısa landscape açıqdır.
- 1.5 font scale-də əvvəlki Nailiyyətlər düyməsinin söz bölünməsi.
- Inventory dəqiq total/pagination/revision, real Parametrlər, Room/outbox/fault proof, Google Console konfiqurasiyası ayrı vahidlərdir.

## Relevant files
- docs/reviews/2026-11-08-home-record-xp-ui.md; docs/home-contract-audit.md
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{OnlineGame,GameTokens}.kt
- android-billing/app/src/debug/kotlin/com/aladdin/wobbrik/AccountPreviewActivity.kt
- android-billing/app/src/main/res/drawable-nodpi/ui_crown.png
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/RecoveryDtos.kt
- docs/design/mobile-v1/{mobile-primary-v2.png,tokens.css}; docs/design/tablet-v1/{primary-landscape.png,portrait.png,README.md}
- docs/kotlin-compose-plan.md §5; docs/stage-three-online-plan.md

## Checks performed
- Son koddan sonra :app:installDebug --offline PASS; /tmp/wobbrik-home-ui-build.log. CI deyil.
- git diff --check PASS. Kontrast 13.00 / 8.22 / 11.45 PASS.
- Emulator: 1080×2400 @420dpi, 1280×800 və 800×1280 @160dpi, font 1.0; legacy/zero və MaxInt64 font 1.5 renderlərinə baxıldı. Override-lar bərpa edilib.
- Gitignored şəkillər docs/reviews/home-record-xp/ altındadır; phone-comparison.png və tablet-comparison.png yanaşı müqayisələrdir.
- Frozen fixture renderi canlı backend sübutu deyil. Main CI işlədilməyib; əvvəlki main müqavilə CI nəticəsi bu UI vahidini əhatə etmir.

## Exact next step
- Bu qeyd və review-u oxu. Yeni kiçik vahid kimi təsdiqlənmiş home dizaynına qalan vizual uyğunluq işini scope et; ui-craft ilə yalnız istinad edilən faylları yoxla. Tablet Şəhərim üçün real read/navigation sərhədini təsdiqlə, saxta axın yaratma.
- Online branch tarixini main-ə daşımazdan əvvəl bütün online diff ayrıca review olunmalıdır; sonra main-də ./scripts/local-ci.sh.
- Main worktree: /Users/mac/My Workspace/Wobbrik-rail-icons. Cari Wobbrik worktree online branch-də saxlanır.
