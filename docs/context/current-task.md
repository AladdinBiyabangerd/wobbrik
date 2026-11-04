# Planşet dizayn konseptləri — 4 noyabr 2026

## Completed
- Mövcud dörd mobil referans oxundu; ui-craft və imagegen tətbiq edildi.
- Planşet landşaft üçün 16 mövcud mobil görünüşün ayrıca kompozisiyası, portret üçün dörd əsas görünüş hazırlandı. Promptlar və PNG-lər docs/design/tablet-v1 altındadır.
- Ana ekranda birbaşa Şəhərim; səhnə + sağ panel; inventar/bazar üçün grid + seçilmiş detal; portretdə aşağı idarəetmə rəfi.

## Current state
- Bu iş yalnız PNG konseptləridir; Android host/engine və responsiv Compose implementasiyası yoxdur.
- Branch feat/tablet-design-png; commit/merge və son CI statusu docs/reviews/2026-11-04-tablet-design.md-də saxlanır.
- Əvvəldən untracked .cursor/rules/responsive-ui.mdc və docs/reviews/2026-11-04-responsive-rule.md fayllarına toxunulmur.

## Decisions
- Palitra/spacing/radius/type mənbəyi mobile-v1/tokens.css; şəhər city-plan/view istiqamətində qalır.
- Pəncərə əsaslı compact/medium/expanded davranış tablet-v1/README.md-dədir; generativ naviqasiya fərqlərində mətn qaydası əsasdır.
- Aktiv oyun damsız, eyni ölçülü mərtəbələr; dam uduzduqdan sonra. 12 mərtəbə adi nümunəsi +12 sikkə/+12 XP. Rəsmdə modul sayı dəqiq deyil.
- Domain/API/schema/wallet dəyişmədiyindən backend dəyişikliyi lazım deyil; server placement bağlıdır. Android implementasiyasına avtomatik keçilmir.

## Remaining work
- İstifadəçi rəyi əsasında konsept düzəlişləri; bütün əlavə ekranların portret variantları.
- Mobil sənəddə açıq qalan alış təsdiqi, elan yaratma/geri götürmə, əldəetmə şərtləri, yüklənmə axınları.
- Gələcək implementasiyada real telefon/planşet, resize, böyük şrift, safe inset, touch target və accessibility yoxlaması. PNG-lər bunları təsdiqləmir.

## Relevant files
- docs/design/tablet-v1/README.md və beş cari PNG; *-prompt.txt
- docs/design/mobile-v1/{README.md,remaining-screens.md,tokens.css,mobile-primary-v2.png,inventory-market-v2.png,play-progress-v2.png,account-states.png}
- docs/reviews/2026-11-04-tablet-design.md
- Implementasiya istənərsə docs/kotlin-compose-plan.md, docs/game-plan.md və AppBackend AGENTS/müqavilələri ayrıca oxunmalıdır.

## Checks performed
- Mobil referansları və planşet son PNG-lərinə vizual baxış; dəqiq ölçülər review qeydindədir.
- Altı token kontrastı keçdi (8.22–16.57). İlkin icazəli lokal CI BUILD SUCCESSFUL; son fayl dəsti/merge nəticəsi review-dədir.
- Runtime/device və backend testləri bu konsept işində aparılmayıb.

## Exact next step
- tablet-v1/README.md və seçilmiş PNG-ni oxu; istifadəçi rəyinə əsaslanan bir kiçik dizayn vahidini hazırla. İmplementasiya yalnız ayrıca tələb ediləndə başlanır.
