# Mərhələ 4 — detail/parts vizual detalları

14 noyabr 2026. Branch `fix/phase4-detail-parts-parity`, HEAD/baza `eaf43e3`; review obyekti source working diff-dir. Merge/push/CI/commit yoxdur. Yeganə plan `docs/PLAN.md`.

İstifadəçinin bu chatdəki açıq göstərişi təsdiqli detail/parts fərqlərinin tətbiqidir. Əsaslar: `docs/design/city-completion-v1/04-building-detail.png`, `02-parts-flow-v2.png`, acceptance-in detail/parts bəndi. Son compact/navbar/modular art qərarları köhnə board-un uyğun hissələrini əvəz edir.

## Dəyişiklik və lokal diff review

- Detail tablarına sakin/sikkə/hissə ikonları, metadata sətirlərinə mərtəbə/sakin/tikinti/hissə/sikkə/info ikonları əlavə edildi. İkonlar dekorativdir; semantikanı mövcud mətn verir. Yeni vector resursları konkret board motivlərini izləyir; bina artı dəyişmir.
- Parts listi və bina redaktəsinin beş kateqoriyasında divar/pəncərə/giriş/dam/işıq ikonları var. Aktiv tab/kateqoriya ağ-cyan kənar, token gradienti və yumşaq cyan glow alır. Metadata və hissə kartlarında navy gradient/cyan çərçivə, seçilmiş kartda glow var.
- `PartsChrome.kt` yalnız bu feature-lərin ortaq vizual helper-idir. Mövcud GameTokens rəng/ölçü/radiusları işlənir; yeni palitra və stack pini yoxdur. Mobil portret şərti əvvəlki planşet rəng/selected border davranışını saxlayır.
- Owner-in `DetailRow` dar en/fontScale üzrə stacked etiket/dəyər həlli saxlanıb. İlk host icrası owner contact PNG-lərini yeniləmişdi; bu iki tracked PNG başlanğıcdakı binary diff-dən bərpa edildi və bayt eyniliyi yoxlandı. Əlaqəsiz tracked owner faylları başlanğıc baseline ilə eynidir. Owner phase4/release/live/QA işinə müdaxilə edilməyib.
- Review zamanı qeyri-mobil selected border-in Raised-ə düşməsi tapıldı və düzəldildi; son host yoxlaması bundan sonradır. Production domain/data/wallet/ownership/API/revision/cache və mutations dəyişməyib. Backend müqaviləsinə təsir yoxdur; AppBackend dəyişikliyi/testi tələb olunmadı.

## Yoxlamalar

- Final opt-in native Compose host **8/8 PASS,23s**: yeni `DetailPartsParityRenderTest` 3 ölçüdə inventory→parts→part detail→bina detalının üç tabı→delete cancel→parts edit axınını, mövcud `CompactPartsPreviewRenderTest` 5 preview regression-unu yoxlayır. 360×640/font1,390×844/font1,320×568/font2; API34/density3 synthetic hesabdır.
- Yeni harness owner `CompactFlowRenderTest`-dən ayrıca çıxarılıb, nəticələri `docs/reviews/detail-parts-visual/` altına yazır. İlk iki yeni test icrasında selector/scroll fixture səhvləri düzəldilib; uğursuz cəhdlər PASS sayılmır. Final8/8 nəticəsi həmin düzəlişlərdən sonradır.
- Adi Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` **BUILD SUCCESSFUL,28s**. Core117/engine95 mövcud nəticələri UP-TO-DATE; app24 icra edildi; failure/error/skipped0. Opt-in host init normal build-ə tətbiq edilməyib.
- Kontrast4/4PASS: Text/Surface13.00,Muted/Surface8.22,Background/Accent11.45,Background/Success11.82. Gradient son nöqtələri həmin rənglərdir; yeni rəng yoxdur. `git diff --check` PASS.
- Təsdiqli iki board və final renderlər yanaşı müqayisə edildi: [müqayisə](detail-parts-visual/design-comparison.png). Native360 detail/parts,390 value/edit,320/font2 parts tab/edit görüntülərinə baxıldı. Əsas action-lar görünür; font2 tabları horizontal, secondary content vertical scroll saxlayır. 320/font2-də bina preview-su panelin böyük hissəsini tutur; metadata üçün scroll lazımdır, bütün məzmunun eyni anda görünməsi iddia edilmir.

## Sərhəd

Bu vahid acceptance-dəki ikon və çərçivə çatışmazlıqlarını aradan qaldırır. Son compact kompozisiyası, serverdən gələn real say/ad/value və seçilmiş modular bina board-un köhnə nümunə binası ilə əvəz edilmir. Köhnə navbar/bazar/reklam action-ları qaytarılmayıb. Statik raster konseptlə piksel eyniliyi və bütöv mərhələ4 qəbulu iddia edilmir. Şəhər parity fərqləri ayrıca açıqdır. Emulator açılmayıb; cihaz/API24runtime/TalkBack/GPU/inset və planşet/landşaft yoxlanmayıb. Bunlar PASS sayılmır; canlı yoxlama gate deyil.

## Lokal review və seçilmiş commit — 14 noyabr

İstifadəçinin review→yalnız detail/parts lokal commit göstərişi ilə production diff, helper-lərin mobil/planşet şərtləri, ikon resursları, selected/normal rənglər və host axını yenidən yoxlandı. Yeni konkret bug tapılmadı; production source dəyişdirilmədi. Əvvəlki final8/8native,236adi unit/APK/lint,4/4kontrast və baxılmış render nəticələri eyni source üçündür.

Index yalnız detail/parts source,10ikon,ayrıca host harness,bu review və handoff-un bu vahidə aid yekun bölməsindən qurulur. InventoryScreen-də owner-in əvvəlki dar en/fontScale `DetailRow` adaptasiyası unstaged saxlanılır; acceptance-in mövcud owner faylı və ona əlavə edilmiş bu vahidin linki də bütöv şəkildə index-dən kənardadır. PLAN/city host/contact/release/live/QA və handoff-un başqa hissələri commit-ə daxil edilmir. Yoxlama owner diff-i də olan working tree üçündür; izolə olunmuş commit-only render/word-break PASS iddiası yoxdur. Dar metadata söz-qırılması düzəlişi owner vahidinin ayrıca asılılığı olaraq qalır. Merge/push/CI yoxdur.

Review run-u: eyni production source-da adi unit/APK/lint **PASS2s**,108task-dan107UP-TO-DATE; əvvəlki236unit nəticəsi reuse edildi. Kontrast4/4min8.22,diffcheckPASS.31owner faylı üçün commit əvvəli SHA256 manifesti saxlanılıb və commit-dən sonra tutuşdurulur. Yeni source düzəlişi olmadığı üçün host render təkrarlanmadı; əvvəlki final8/8 və görüntüyə baxış sübutu qüvvədədir.
