# PLAN §1.1 — Parametrlər native səth və böyük şrift düzəlişi

- Branch: `feat/community-tabs-native-consistency`, HEAD `6c96e9c`, uncommitted working diff. Owner Home/City/Rekordlar/Nailiyyətlər/token/mock/baseline/sənəd dəyişiklikləri saxlanır; index boşdur. Commit/merge/push/deploy/CI yoxdur.
- Authority: `docs/design/unified-mobile-v3/03-account.png`, README-dəki son Home header/7-slot qərarı və açıq təsdiqli `2026-11-15-account-compact-proposal.md` settings-390 kompozisiyası. Profil preview təsdiqi kimi şərh edilmir.

## Dəyişiklik və lokal review

`SettingsScreen.kt` artıq `account_approved_board` decode etmir və raster `CommunityBackdrop` çəkmir. Fon və ayrıca sətir kartları mövcud GameTokens və MenuRelief ilə native gradient/konturdur. İkon yolları, 64dp minimum sətir, dairəvi ikon, 48dp switch slot, section labels və footer-siz geri başlığı saxlanır. Yeni rəng/ölçü tokeni yoxdur.

FontScale>1-də toggle/chevron mətn sütununun altına keçir; etiket və dil dəyərinə daha çox en ayrılır. Başlıq tək sətirdir və lazım gəlsə horizontal scroll edir. Body vertical scroll qalır, dock shared shell-ə aiddir. Disabled/loading/busy switch parent semantikası klikə icazə vermir; track vurğusu disabled halda sakitləşir.

`SettingsRoute.kt` və `ProfilePresentation.kt` oxunub yoxlandı, dəyişdirilmədi. Real DataStore write, mövcud Azərbaycan dili məlumat dialoqu, ACTION_SENDTO support intent və lokal legal document viewer saxlanır. Yeni dil, support funksiyası və server hüququ uydurulmadı. Wallet/progression/profile API/DTO/domain/schema dəyişmədiyinə görə AppBackend dəyişikliyi tələb olunmur; backend testləri bu vahiddə təkrarlanmadı.

## Yoxlamalar

Final native host suite **10/10 PASS44s**, failure/error/skipped0. Son5 settings PNG açılıb03-account və təsdiqli compact composition ilə müqayisə edildi; sübutlar `settings-native/settings-*.png`-dədir. Render init `settings-native/render.init.gradle` (yalnız əl ilə host QA) istifadə edir.

- Native API34 Robolectric host: settings390/font1,320/font1,320/font2,390/font2,360/font2 simulated24/48dp inset+Long.MAX_VALUE balans. Geri48dp target, sabit Play semantikası, scroll ilə Məxfilik girişinə çatmaq və tam söz sərhədləri yoxlanır.
- Real route/DataStore: music write/read, switch state restore, back, language dialog, support intent və üç legal document entry. Əlavə loading/busy testi writes sayının0 qalmasını, enable-dan sonra1 olmasını yoxlayır.
- Token contrast **5/5 PASS**, minimum8.59:1 (Muted/PanelTop); Text/PanelTop13.58, Text/PanelBottom16.16, Muted/PanelBottom10.22, Background/Accent11.45.
- Normal `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` **PASS59s**, native init tətbiq edilmədi. `git diff --check` PASS.
- İlk iterasiyalarda paragraph `hasVisualOverflow` bayrağı yuvarlaqlaşdırılmış layout enini overflow saydı; assertion faktiki line-right +1px tolerance, ellipsis və söz sərhədi ilə əvəz edildi. İlk horizontal-scroll label renderində söz vizual kəsilirdi; production layout düzəldi. Native capture-da itən Play/layer qeydləri üçün mövcud Rekordlar testinin invalidate→frame→warmup→frame→PixelCopy ardıcıllığı və class başına təmiz JVM tətbiq edildi.

## Açıq iş və sərhəd

Profil hələ legacy raster `ProfileSkin`, `ProfileOverviewSkin`, rule və action səthlərini istifadə edir; crown ribbon/native profil kartı, editor və avatar seçimi növbəti vahiddir. Mövcud public-profile repository/session epoch, server XP/level/record və save/conflict axını saxlanmalıdır. Shared shell daxilində Parametrlər girişi ProfileNavbar-dadır.

Bu nəticə bütün Profil/Parametrlər kompozisiyasının və app redesign-ının qəbulu deyil. Parametrlər font2-də aşağı hissələr scroll altındadır. Pixel pariteti, real hesab/device E2E, TalkBack, IME/API24 runtime, GPU/performance, locale matrisi və planşet renderi yoxlanmayıb. Emulator açılmadı; press animasiyası ayrıca tapşırıqdır.
