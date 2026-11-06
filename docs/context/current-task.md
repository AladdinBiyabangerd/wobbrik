# Tablet rail asset-ləri və home çatışmazlıqları — 7 noyabr 2026

## Completed
- `feat/tablet-rail-icons`: ui_home/city/inventory/market RGBA PNG-ləri hazırlanıb;
  abe0d90 review-dan sonra tarix saxlanılmaqla main-ə merge edilib.
- Asset QA: 390×844 və 1280×800, 24/32/48/96px renderlərə baxılıb.
- Main lokal CI: BUILD SUCCESSFUL, 103 task executed. Backend dəyişməyib.

## Current state
- Cari istifadəçi branch-i `feat/stage-three-online-progression`; oradakı online
  ekran + Google guest link işi qorunur, main-ə merge edilməyib.
- Yeni ikonlar resursdur; runtime rail və yekun dizayn uyğunluğu hazır deyil.
- Google live link üçün web client/provider Console konfiqurasiyası açıqdır.

## Decisions
- İstinad tablet-v1/primary-landscape.png; generativ rekonstruksiya piksel eyniliyi deyil.
- Saxta Rekord 28, XP 580/800 və 12 bina yazılmır; real server məlumatı tələb olunur.
- Real SKU/Console/deploy/push ayrıca icazəsiz edilmir.
- Əlavə tələblər bu asset vahidində implementasiya edilməyib.

## Remaining work
- İstifadəçi əlavə olaraq istəyir: rekord, növbəti XP həddi ilə XP bar,
  inventory API-dən bina sayı, Parametrlər ekranı, tablet home Şəhərim axını.
- Bu domain tələblərində AppBackend-in öz AGENTS.md-si, API/DTO/schema/rule
  revision/dedup/account isolation/retry/old-client davranışı yoxlanmalıdır.
- Tablet rail runtime inteqrasiyası; təsdiqlənmiş dizaynla telefon/planşet müqayisəsi.
- Əvvəlki mərhələ: Room cache/outbox və fault proof hələ qalıb.

## Relevant files
- docs/design/tablet-rail-icons-v1/{README.md,render-preview.py,preview-*.png}
- docs/reviews/2026-11-07-tablet-rail-icons.md
- docs/design/{tablet-v1,mobile-v1,game-tap-v2}/
- android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{OnlineGame,GameTokens,GoogleGuestLink}.kt
- docs/kotlin-compose-plan.md (bölmə 5), docs/stage-three-online-plan.md
- /Users/mac/My Workspace/AppBackend/AGENTS.md və docs/wobbrik-billing.md

## Checks performed
- Yeni dörd asset 1254×1254 RGBA, alpha 0–255, künclər alpha 0.
- Asset preview renderləri açılıb baxılıb; Compose/device screenshot deyil.
- Main-də ./scripts/local-ci.sh uğurludur; online branch CI nəticəsi deyil.
- Docs whitespace düzəlişi ayrıca review branch-indədir; son merge-dən sonra CI yenilənir.

## Exact next step
- Bu sənədi oxu, online branch-də qorunan işi nəzərə al; beş əlavə tələbdən
  əvvəl rekord + XP növbəti həddinin real server müqaviləsini mobil və AppBackend
  tərəfində məhdud scope ilə yoxla və ayrıca məntiqi branch-də tamamla.
