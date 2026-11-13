# Mobil ölçü nümunəsi — təsdiq gözləyir

Branch: `feat/mobile-compact-sizing`; production UI/token dəyişikliyi yoxdur. Əvvəlki owner handoff/release/live-check/QA faylları qorunur. Plan: `docs/PLAN.md` §8; mobil portret, planşet ayrıca.

## Baxılan ortaq mənbələr
- `GameTokens.kt`: 4/8/16/24dp spacing, 48dp target, 16/24dp radius, 12/16/18/24/32sp type; mobil density profili yoxdur.
- `HomeScreen.kt`: HomeButton primary 64dp/32sp, secondary 48dp/16sp; HomeIcon32dp, header logo72dp. Telefon artı width-aspect ilə ölçülür, controls scroll daxilindədir.
- `CityPurchaseChrome.kt`: PurchaseAction64dp, primary24sp; PurchaseHeader48dp və fontScale əsasında inline/stack.
- `CityPurchasePanel.kt`: summary art96dp; fontScale>1 üçün72dp və column; action-lar şaquli olur.
- `LandShopScreen.kt`: art96/72dp; DecorationShopScreen art96dp və iki sətir ad; InventoryScreen/PartsScreen ortaq HomeButton və ayrıca tab/card ölçüləri işlədir.

## Təsdiq üçün konkret pillələr (hələ tətbiq edilmir)
- Compact mobile: pəncərə eni≤360dp VƏ YA hündürlüyü≤640dp; ölçü hesabı inset-dən sonrakı mövcud sahəni də nəzərə almalıdır.
- Regular mobile: qalan mobil portret. Tablet profili dəyişmir.
- Primary action: compact min48dp, regular min56dp (`Target+Small`); yazı24sp (`Title`), secondary16sp (`Body`). Bütün interaktiv hədəflər ən az48dp.
- Page heading: compact18sp (`Hint`), regular24sp (`Title`); kart mətni16sp qalır.
- Control icon:24dp (`Inset`), hədəf48dp. List card art: compact48dp, regular72dp (`Target+Inset`); detail/game art-a kor-koranə tətbiq olunmur.
- Section gap: compact8dp, regular16dp; card iç boşluğu8dp, page inset16dp; radius/color/gradient/assets dəyişmir.
- FontScale böyüyəndə mətn scale-i söndürülmür; kontrol hündürlüyü sərbəst artır, dar row-lar column olur. Primary action-lar scroll altında gizlədilmir; secondary məzmun üçün zəruri scroll saxlanılır.

## Render sübutu və sərhədlər
Mövcud cached native Robolectric/Compose harness təkrar istifadə edilir, build pinləri/CI dəyişmir. `mobile-sizing-visual/render/SizingProposalTest.kt` yalnız review specimen-dir, app source/navigation-a daxil deyil. Cari sütun HomeButton/PurchaseCard/PurchaseArt-ı çağırır; təklif düyməsi eyni gradient/frame tokenləri ilə ölçü nümunəsidir. Bu, tam səhifə və ya yekun responsive implementasiya deyil.

360×640dp/API34/density3: current font1, proposal font1 və proposal font2 host renderləri hazırlandı və şəkillərinə baxıldı. Başlıq/düymə/kart mətnləri görünür; font2 kart hündürlüyü artır. Specimen-in əlavə izah mətnləri font2-də viewport sonuna çatır; production layout yoxlaması deyil. Native host render uğuru cihaz/inset/TalkBack və bütün ekranların qəbulunu təsdiqləmir. Emulator açılmadı. 4 kontrast cütü PASS (minimum10.48). Backend/API dəyişmədi, AppBackend dəyişikliyi lazım deyil. CI/merge/push/commit yoxdur.

Dəqiq növbəti addım: istifadəçinin konkret ölçü nümunəsini təsdiqləməsini gözlə; sonra token profili və ortaq komponentlərdən başlayaraq mövcud mobil ekranlara tətbiq et, kiçik/normal portret və font1/2 host renderlərini yoxla. Təsdiqədək production UI dəyişmə.

## Yeni tam səhifə təklifi — əvvəlki specimen rədd edildi

İstifadəçi ilk ölçü specimen-ini “yamaq kimi” qiymətləndirib qəbul etmədi və yeni UI hazırlamağı tapşırdı. Yuxarıdakı tək komponentli nümunə təsdiqli dizayn deyil; production-a tətbiq edilməyəcək.

`render/MobileCompositionTest.kt` tam ana ekran və inventar kompozisiyası üçün ayrıca review-only Compose fixture-dir. GameTokens palitrası/ölçü pillələri və orijinal home_city_cutout_v2, logo, UI ikonları, real modular BuildingPreview istifadə olunur. Bina center/parts/sayları və balans yalnız açıq sintetik dizayn fixture-indədir, oyunçu hesabı və server adapteri deyil.

Yeni istiqamət: ortaq16dp screen inset/control radius, 8dp element gap, əsas action min56dp/18sp, secondary48dp/16sp. Ana ekranda art→statistika→Oyna→Şəhərim→ikinci dərəcəli girişlər; inventarda üç iri şaquli giriş əvəzinə48dp toolbar, 120dp bina preview sahəli vahid iki sütunlu kartlar. Kartlarda əlavə iç çərçivə yoxdur. Normal görünüşdə prioritet yalnız Oyna-da cyan fill ilədir. Böyük şriftdə home art qısa görünüşdən çıxır, content scroll saxlanır, Oyna/Şəhərim sabitdir; inventar başlığı ayrıca sətrə, toolbar iki sətrə, kartlar bir sütuna keçir. Bu kompozisiya dəyişiklikləri təklifdir, əvvəlki təsdiqli ekranları əvəz etməsi üçün konkret istifadəçi təsdiqi HƏLƏ YOXDUR.

Yoxlamalar: 3 host ssenarisi / 6 tam səhifə PNG (360×640,font1;390×844,font1;320×568,font2), son native Compose render run PASS. Oyna/Şəhərim/Ana ekran görünürlüğü assert edilir. Əvvəlki font2 horizontal secondary girişdə yarımçıq görünən yazılar şaquli/iki sətirli düzülüşlə düzəldildi. Normal390 viewport və 360 contact sheet/kiçik font2 görüntülərinə baxılıb; emulator və production UI dəyişmədi. Son kontrast4/4PASS(min6.48). Cihaz/inset/TalkBack/real hesab, bütün digər səhifələr, uzun balans/naməlum lokal kombinasiyaları yoxlanmayıb. Tam responsive implementation və mərhələ qəbulu deyil. Gradle pinləri/CI/backend dəyişmir.

Nəticə: `mobile-sizing-visual/full-pages-v2.png` konkret təsdiq üçün göstərilir. Təsdiqdən sonra production token/component inteqrasiyasına keçmək olar; həmin vaxt bütün tələb olunan mobil ekran/state yoxlamaları ayrıca tamamlanmalıdır. Commit/merge/push/CI yoxdur.

## Ana ekran navbar təklifi v3
- İstifadəçi ilk səhifədə düymələrin aşağı navbar-da olmasını və oyunu başladan Play-in tam ortada yerləşməsini tapşırdı. Review-only HomeProposal buna uyğun yeniləndi; app UI dəyişmədi.
- Sıra: Şəhər / İnventar / mərkəzi Oyna / Kolleksiya / Nailiyyətlər. Birbaşa inventar girişi mövcud route üçün əlavə təklifdir. Dar yerdə görünən qısa yazılar Şəhər və Uğurlar-dır, accessibility adları Şəhərim/Nailiyyətlər qalır. Play64dp, digər hədəflər min48dp, rəng/ölçülər yalnız mövcud GameTokens-dən gəlir. Böyük şriftdə yan yazılar ikonlara keçir, Play yazısı görünür və navbar hündürlüyü artır.
- `full-pages-v3.png`, `home-navbar-v3-preview.png`, `home-v3-{360-font1,390-font1,320-font2}.png`. V2 şəkilləri və fixture `MobileComposition-v2.kt.txt` saxlanır. Inventar kompozisiyası dəyişmədi.
- Son3/3 host ssenarisi PASS11s,360 board/home və320font2 final PNG-lərinə baxıldı. İlkin sınaqda uzun navbar yazılarının toqquşması və font2 Oyna kəsilməsi düzəldildi. Kontrast palitrası dəyişmədi; əvvəlki ölçülmüş4cüt minimum6.48 qüvvədədir. Emulator/CI/backend/merge/push yoxdur. Real start/navigation funksiyası bu preview-də qoşulmayıb; konkret UI tətbiq təsdiqindən sonra production onPlay/onPage ilə bağlanmalıdır.

## Təsdiqli tətbiq, yığcamlaşdırma və lokal review

İstifadəçi “Bəli belə yaxşıdır, tətbiq et. Ortadakı Play düyməsi oyunu başlatsın” ilə v3 Home navbar-ı açıq təsdiqlədi, sonra kiçiltmə işini davam etdirməyi və commit etməyi tapşırdı. Əvvəlki pending qeydləri tarixçədir. Branch `feat/mobile-compact-sizing`, review bazası `ec84e7a`; production diff və bu review-nun render fixture-ləri yoxlandı.

### Nəticə
- HomeScreen mobil portretdə təsdiqli MobileHomeScreen-i göstərir. Mərkəzi64dp Play mövcud onPlay callback-inə bağlıdır; OnlineGame-də həmin callback əvvəlki server-authoritative start/abandon reconciliation→host.enter→game keçididir, ayrıca lokal/demo oyun açılmır. Snapshot/status/Ready/onPlay yoxdursa Play dispatch edə bilmir. Digər4navbar girişi və Hesab real mövcud route-ları çağırır.
- Balans/level/rekord/XP yalnız qəbul edilmiş snapshot-dandır. Uzun balans ayrı sətrə keçir və bütün rəqəmlər saxlanır. Navbar safeDrawing padding xaricində fixed qalır, minimum hədəf48dp, icon24dp; font2-də yan label-lər a11y adları qalmaqla yığılır, Play yazısı böyüyür. Oyun artı dəyişməyib.
- MobileUiSizing mobil portret app window en+hündürlük konfiqurasiyasını izləyir: compact≤360dp və ya≤640dp. GameTokens-də eyni pillələrdir; heading18/24sp, ortaq primary56dp/18sp, card art72/96dp, detail art144/192dp. FontScale söndürülmür. Yeni runtime/library pinləri yoxdur.
- Inventory real read/mutation qaydaları saxlanaraq üç iri giriş əvəzinə toolbar, normal şriftdə2sütun, font2-də yığcam üfüqi1sütun kartlar və başlıqda Yenilə ilə işləyir. Ana ekran geri düyməsi təsdiqli secondary təqdimatdadır. Parts normal grid2sütun/font2-də1sütun, preview-lar yığcamdır; font2 edit kateqoriyaları tam sözlü horizontal scroll-dadır. Kolleksiya başlıq/thumbnail-ləri, dekor/torpaq card-summary/Al düymələri eyni ölçü pillələrinə keçib.
- Hesab/nailiyyət mobil portretdə art/body yığcam scroll daxilindədir, Oyna/Ana ekran sabit alt əməliyyatlardır; normal olmayan böyük şriftdə ikinci dərəcəli girişlər şaqulidir. Aktiv oyun/gameplay/şəhər fizikası və server geometry dəyişməyib.

### Review tapıntıları və düzəlişlər
- İlk lint unused BoxWithConstraints scope xətası → adi Box ilə düzəldi.
- Host regression kiçik/font2 hesabda Ana ekranın scroll altında qaldığını göstərdi → mobil əsas əməliyyatlar content scroll-dan ayrıldı.
- Font2 hissə edit rail-ində Pəncərə sözü kəsilirdi → həmin mobil font pilləsində horizontal kateqoriya zolağı.
- Kiçik/font2 inventory-də preview adı viewport-dan sıxışdırırdı → üfüqi kompakt kart, Yenilə başlığa köçürüldü.
- Lokal diff review: UI/data/domain ayrılığı, real callback/route, stale Ready/server command sərhədi, imports/yerləşmə yoxlandı; əlavə konkret bug aşkarlanmadı. Backend/API/DTO/domain dəyişikliyi yoxdur, AppBackend dəyişməsinə ehtiyac yoxdur. Təkrar wallet/journal yaradılmayıb.

### Son yoxlamalar
- Son production kodunda Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS14s,108task.
- Opt-in native host20/20PASS27s: Home6,CompactFlow3,CompactPurchase6,CompactPartsPreview5.360×640font1,390×844font1,320×568font2; əlavə Home360font2-də top24/bottom48dp reserved-space, recovery/offline/long-balance/XP. Həqiqi production composable-lar, sintetik qəbul edilmiş məlumat və fake read transport; real hesab/server E2E deyil.
- Home Play callback+4nav/account+center/min48dp tests; shop Al/Ləğv et və read/detail/apply/back görünürlüğü; picker/seçim/68art/6fasad və edited-detail read regression-ları keçir. Bütün screenshot-a tək assert baxışın əvəzi sayılmır.
- Home360,small/font2,reserved-space,long-balance;3production contact sheet; purchase dekor/land list-confirmation və picker böyük şrift renderlərinə baxıldı. Təsdiqli home preview ilə composition/icon/gradient/central button/nav sıra/art müqayisəsi aparıldı: struktur eynidir, serverdə XP intervalı varsa əlavəprogress göstərilir, uzun dəyər/font2/dar pəncərə zəruri adaptasiya verir. Inventar real item adı və pagination/loading/retry məzmunu proposal-dakı4qısa adla süni eyniləşdirilmir. Generic card/detail məzmunu zəruri scroll saxlayır.
- Kontrast4/4PASS(min6.48), diff checkPASS. Emulator/device/API24runtime/TalkBack/GPU və bütün dillər yoxlanmayıb; planşet/landşaft implementasiya/render işi başlanmadı. CI/merge/push yoxdur. Tam mərhələ4/ümumi məhsul qəbulu deyil.

İstifadəçi lokal commit istəyib. Owner release/live-check/XML/QA patch və əvvəlki oyun handoff əlavələri commit-dən kənarda saxlanır. Commit yalnız bu UI source/token, review/harness və bu vahidin handoff/plan qeydlərinə aiddir. Sonrakı böyük dəyişiklik yeni chatdə həmin handoff-dan davam etməlidir.
