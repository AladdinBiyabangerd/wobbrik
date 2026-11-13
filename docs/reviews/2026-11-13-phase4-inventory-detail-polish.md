# Mərhələ 4 — inventar/detail vizual detallar

- Branch `feat/phase-4`; yoxlanmış diff: `34ff7b1` üzərində bu vahidin dəyişiklikləri. Main merge, push və AppBackend əməliyyatı yoxdur. İstifadəçinin `android-billing/app/release/` faylları qorunur.
- Mənbələr: təsdiqli `mobile-v1/inventory-market-v2.png` və `city-completion-v1/04-building-detail.png`; mövcud `GameTokens.kt`, `ui_coin` və 68 hissə atlası. Yeni rəng/spacing/radius və raster yaradılmadı.
- Sikkə nişanı, preview/detail kart çərçivələri, hissənin atlası/adı/kateqoriyasını birləşdirən kartlar əlavə edildi. Sakin izahı və passiv gəlir statusu saxlanır.
- Tikinti/sakin/hissə dəyəri, maksimum qiymətdə komissiya və satıcı məbləği core read-only izah modelindədir. Yalnız v1 kataloqu, tam hissə uyğunluğu, sakin qaydası və server base/max cəmləri uyğun olduqda göstərilir. Uyğunsuzluqda server cəmləri qalır, bölgü verilmir. BigInteger hesabı overflow yaratmır. Bu, satış əmri və ya server quote deyil; traded bina üçün yenidən satış hesabı göstərilmir.
- Böyük şriftdə tab yazıları horizontal scroll ilə bütöv qalır, preview və məlumat şaquli yerləşir; Ana ekran/İnventara qayıt əsas əməliyyatı scroll xaricindədir. Bunlar source səviyyəsində uyğunlaşdırmadır, render sübutu deyil.

## Lokal review və yoxlamalar

- Qiymət qaydaları UI-dan ayrılıb; serverin göndərdiyi cəmlər dəyişdirilmir. Naməlum qayda/kataloq və uyğunsuzluq halları fail-closed-dur.
- `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` Java 21 ilə son funksional kodda PASS. Yeni `BuildingValueBreakdownTest`: 4/4 (iki təsdiqli iqtisadi nümunə, uyğunsuz/missing/future kataloq və overflow sərhədi).
- Token kontrastı: Text/Surface 13.00; Muted/Surface 8.22; Text/Background 16.57; Muted/Background 10.48; Background/Accent 11.45 — PASS. Palitra dəyişməyib.
- Gradle ilk sandbox cəhdində cache lock yazısı ilə bloklandı; eskalasiya ilə fokuslu yoxlamalar uğurla tamamlandı. CI işlədilmədi.

## Açıq sərhədlər

- Layihədə cihazsız Compose screenshot quruluşu yoxdur; emulator avtomatik açılmadı. Android render/runtime, 360×640 və normal portret, font 1.0/2.0, inset/a11y vizual yoxlamaları aparılmadı. Dizayn şəkillərinə baxılıb, tətbiq screenshot-u yoxdur; vizual PASS və birəbir uyğunluq iddiası edilmir. Canlı yoxlama gate deyil.
- Dizayndakı ətraf bitkilər/ground dressing, parlaq çərçivə/ikon detallarının tam eyniliyi və atlas səth join-ləri açıqdır. Mövcud game_island sabit fasad daşıdığı üçün real binanın altına əlavə edilmədi; sabit maket accepted geometry-ni əvəz etmədi. Redaktə/bazar/placement düymələri işlək əmrlər qoşulmadan göstərilmədi.
- AppBackend-ə istifadəçi qadağasına görə toxunulmadı; bu diff mövcud DTO/kataloq və PLAN §5 üzərində read-only təqdimatdır, yeni API/server/schema tələb etmir. Yeni backend uyğunluğu/testi iddiası yoxdur.
- Növbəti vahidin `02-parts-flow-v2.png` dizaynını istifadəçi bu chatdə açıq təsdiqlədi; README/PLAN/handoff-a qeyd olundu. Alış/tətbiq implementasiyası bu diff-də yoxdur; reklam/real pul hazır axın kimi təqdim edilmir.
