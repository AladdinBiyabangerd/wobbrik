# Oyun ekranının sadələşdirilməsi — 4 noyabr 2026

## Completed
- İstifadəçi telefon və planşetdə aktiv oyun üçün yalnız pauza düyməsi, səhnəyə toxunaraq buraxma istədi.
- game-tap-v2: telefon portret, planşet portret/landşaft üçün aktiv oyun və fasilə konseptləri; promptlar/spec saxlanır.
- Mobil/planşet indeksləri və game-plan/Kotlin planında mənbə/idarəetmə qaydası uyğunlaşdırıldı.

## Current state
- PNG dizayn düzəlişidir; işlək Android UI/engine/input implementasiyası yoxdur.
- Branch fix/game-tap-design. Commit/merge/CI nəticəsi docs/reviews/2026-11-04-game-tap-design.md-dədir.
- Əvvəldən untracked responsive-ui.mdc və responsive-rule review fayllarına toxunulmur.

## Decisions
- Aktiv oyunda tək pauza, kiçik interaktiv olmayan say/rekord; Burax düyməsi, rail, yan panel, alt rəf, wallet və geri düyməsi yoxdur.
- Pauzada yalnız Davam et / Yenidən başla / Menyuya qayıt. Pauza/modal/resume toxunuşları səhnəyə ötürülmür.
- Aktiv/fasilədə dam yoxdur, mərtəbələr eyni ölçüdədir; şəkillər dəqiq geometry deyil.
- Köhnə mobil/planşet lövhələrinin yalnız oyun və fasilə hissəsi əvəz olunur; qalan ekranlar qüvvədədir. Palitra mobile-v1/tokens.css-dir.
- Domain/API/schema/wallet/reward dəyişmir, AppBackend dəyişikliyi lazım deyil. İmplementasiya avtomatik başlanmır.

## Remaining work
- İstifadəçi rəyi üzrə dar dizayn düzəlişləri; gələcək Android input, pause/lifecycle, restart/exit server uyğunluğu və responsiv runtime yoxlamaları.
- Telefon landşaftı, real cihaz, dp/touch, böyük şrift, TalkBack və resize yoxlanmayıb.
- Əvvəlki mobil alış/elan/yüklənmə axınları və əlavə ekranların portret variantları ayrıca qalır.

## Relevant files
- docs/design/game-tap-v2/README.md, üç PNG və promptlar
- docs/reviews/2026-11-04-game-tap-design.md
- docs/game-plan.md §2; docs/kotlin-compose-plan.md §4
- docs/design/mobile-v1 və tablet-v1 indeksləri, mobile-v1/tokens.css

## Checks performed
- Konseptlərə vizual baxış; ölçülər/tapıntı və CI nəticələri review-dədir.
- Altı token kontrastı keçdi (8.22–16.57). Runtime/backend testi yoxdur.

## Exact next step
- game-tap-v2/README.md və uyğun PNG-ni oxu; istifadəçinin növbəti konkret rəyini tətbiq et. İmplementasiya yalnız ayrıca tələb ediləndə başlanır.
