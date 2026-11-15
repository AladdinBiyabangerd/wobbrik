# Vahid mobil UI — ilk naviqasiya vahidi

Branch: `feat/unified-mobile-ui`; baza/HEAD `446cd11`. Review uncommitted source diff-i əhatə edir: `MobileHomeScreen.kt`, yeni `MobileMenuNavbar.kt`, `GameTypography.kt`, `ProfilePresentation.kt` şrift extraction-u və `HomeProductionRenderTest.kt`. Owner baselineProfiles, əvvəlki design/proposal və digər dirty fayllar dəyişdirilməyib. Commit/merge/push/CI/emulator yoxdur.

## Dəyişiklik

Home yeddi real route callback-i verir: Ana/Şəhər/İnventar/Oyna/Bazar/Uğurlar/Profil. Play route adı yazmır, əvvəlki onPlay callback-i ilə lifecycle sərhədinə keçir. Üst profil çıxarıldı, balans sağda saxlandı; böyük balans sağa düzülmüş ayrıca sıraya keçir. Mövcud Nunito eyni font resursları ilə ortaq fayla çıxarıldı, profil provider-i eyni font ailəsindən istifadə edir. Menyu selected semantics və minimum48dp sahələr verir. Dar pəncərədə yan qruplar iki sıraya keçir, Play mərkəzdədir; font2-də mətn yerinə əlçatan adlar saxlanır. Bu ilkin adaptasiyadır, tam təsdiqli chrome pariteti deyil.

## Yoxlamalar və tapıntılar

- Son native host: `HomeProductionRenderTest` 6/6 PASS, 26s. 360×640font1,390×844font1,320×568font2,360font2+simulyasiya24/48dp inset, uzun balans/XP və error state. Real composable input, route callback, Play center, minimum48dp ölçü assertion-ları var. Firebase/server route E2E deyil.
- Fokuslu normal `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS,38s (bəzi dəyişməyən task-lar UP-TO-DATE). CI deyil.
- Kontrast: Text/Surface13.00,Text/Raised10.24,Background/Accent11.45,Muted/Raised6.48;4/4PASS. Palette dəyişmir.
- Köhnə offline test callback veriləndə Play-in disabled olmasını gözləyirdi. Bu, mövcud lokal offline start siyasətinə ziddir. Test indi callback null olduqda startın bloklandığını, callback veriləndə status mətnindən asılı olmayaraq host icazəsinin saxlandığını yoxlayır. Production authority/gating dəyişdirilməyib.
- Son source lokal review: domain/API/wallet/revision/account isolation və start callback-i dəyişmir. AppBackend-də müqavilə dəyişikliyi tələb edilmir; backend testləri bu UI vahidi üçün işlədilməyib.
- Son renderlər `docs/reviews/unified-mobile-visual/production-*.png` daxilindədir (lokal generated evidence). Approved home və native390/360/font2/inset/uzun balans renderləri açılıb baxıldı. Mövcud screenshot capture software decor draw-dır, hardware/GPU sübutu deyil.

## Açıq vizual və funksional iş

Təsdiqli home ilə müqayisədə relyefli mavi chrome/coin pill/stat panel/XP/nav skin və ikon üslubu uyğun deyil; mövcud city bitmap aşağı keyfiyyətlidir və kompozisiya nisbətləri fərqlənir. Native ekranın bu ilkin hissəsi son vizual dizayn kimi qəbul edilmir. Dar ölçüdə labels gizlənir; uzun balans iki sətirdədir. Clean asset/paritet turunda bunlar ayrıca saxlanmalı və yoxlanmalıdır.

Navbar/header hələ yalnız Home-da inteqrasiya olunub. Digər menyulara vahid shell, İnventar/Görünüş birləşməsi, Bazar/sahiblik ayrılması, Uğurlar tabları, city read/Plan regression-u və müqayisəli performans ölçməsi açıqdır. Tablet/landşaft dəyişmir. Real cihaz/API24/IME/TalkBack/GPU və bütün locale/state kombinasiyaları yoxlanmayıb. Tam §1.1 və mərhələ5 qəbulu deyil.

Növbəti vahid: mövcud route content-lərini ortaq mobil shell-ə inteqrasiya et; ekranın öz header/footer-lərini ikiqat göstərmə; mövcud capability/recovery və Play lifecycle sərhədlərini saxla. Yeganə plan `docs/PLAN.md`§1.1-dir.

## Davam — ortaq shell-in real menyu route-larına inteqrasiyası

Branch `feat/unified-mobile-ui`, HEAD/baza `446cd11`, uncommitted diff. Bu vahid `MobileMenuShell.kt`, `OnlineGame.kt` route hostu və City/Inventory/Parts/Purchase/Market/Profile/Records/Home content chrome uyğunlaşdırmalarını əhatə edir. Əvvəlki Home/typeface işi və owner baselineProfiles/design/proposal faylları qorunur; commit/merge/push/CI yoxdur.

- Mobil portret hostu Şəhər, İnventar/detal, Hissələr, Görünüş, Dekor/Ərazi alışları, Bazar, Uğurlar/Rekordlar, Profil/redaktə və Parametrləri ortaq header/navbar ilə əhatə edir. Aktiv oyun/pauza və pre-auth bu hostdan kənardadır. Tablet/landşaft branch-ləri saxlanır.
- Play-in əvvəlki startLocal/discard/terminal restore qərarı vahid nullable callback-ə çıxarıldı; dock onu çağırır, `page="game"` ilə saxta start yaratmır. Wallet/snapshot display-only, exact Ready/session fence və mutasiya/journal müqaviləsi dəyişmir.
- İçəridə təkrar logo/balans və destination navbar gizlənir; lokal geri/yenilə, profil redaktə/save/cancel və Parametrlər girişi qalır. Görünüş İnventar alt-girişidir, təsdiq/geri İnventara qayıdır. Uğurlar/Rekordlar keçidi əlçatandır; mobil Records geri yolu Uğurlardır. Bunlar tam sahiblik/bazar bölgüsünün tamamlanması deyil.
- Content viewport shell sərhədində clip edilir; inset shell tərəfindən consumed olur. Font2-də kontekst tabları bir sırada üfüqi scroll edir, minimum48dp və bütöv söz saxlanır. Dar dock-da mətnlər a11y adları ilə əvəz olunur; 390dp-də İnventar label-inin kəsilməsini önləmək üçün label eni mövcud Target+Small token pilləsinə görə yoxlanır. Bu, əvvəlki dar-dock adaptasiyasının davamıdır, approved bitmap-in tam pariteti deyil.
- Native yoxlama iki inteqrasiya problemi tutdu: azalmış content hündürlüyünün legacy landşaft layout/ikinci logo seçməsi və font2 Records/Görünüş content-inin çəkisiz qalması. Shell context-i mobil layout seçimini saxlayır; Records/Görünüş daxili məzmunu bounded scroll alır, əsas dock/Play və Görünüş təsdiqi ayrıca qalır.

### Fokuslu sübut

Host: `docs/reviews/mobile-sizing-visual/render/MobileShellRouteRenderTest.kt`, mövcud opt-in `render.init.gradle`. Real screen composable/repository-lər sintetik transport/account ilə işləyir; bu production fake adapter və Firebase/server E2E deyil. Üç portret ssenarisi: 360×640font1,390×844font1,320×568font2; inventory→parts→city→inventory→appearance, decoration/land, achievements→records, market→profile→settings→profile. Hər route-da tək logo, görünən mərkəzi Play və callback dispatch yoxlanır; ssenari boyu iqtisadi submit sıfırdır. Əlavə recovery/null Play, Long.MAX_VALUE balans, font2 və simulyasiya24/48dp inset ssenarisi tək header/dock və retry-ni yoxlayır. Home6 regression-ları da təkrar işləyir.

Renderlər: lokal `unified-mobile-visual/production-shell-*.png`, `shell-contact-{360-font1,390-font1,320-font2}.png`. Kontaktlar və raw recovery/font2 Records açılıb təsdiqli home chrome-u ilə müqayisə edilir. Relyefli clean skin/ikon, hero və bəzi daxili ekran kompozisiyaları hələ uyğun deyil. Font2-də bəzi secondary content scroll tələb edir; bütün state-lər və məzmunlu böyük siyahılar bu fixture ilə təsdiqlənmir. Şəhər və dekor fixture-lərində xəta halları da çəkilir; real şəhər oxu root cause-u bu vahiddə həll edilmiş sayılmır. Son shell capture-ları mövcud profil harness-indəki invalidate→frame→warm draw→frame→native PixelCopy üsulu ilə çıxarılır; ilkin birbaşa decor draw-da natamam header/dock çəkilişi müşahidə olundu. Home6 əvvəlki decor capture üsulunu saxlayır. Native PixelCopy də real GPU/device sübutu deyil.

API/DTO/domain/schema/ledger dəyişməyib; core repository/capability müqavilələri saxlanır. Bu UI host vahidi AppBackend dəyişikliyi tələb etmir, backend testləri işlədilməyib. Real cihaz/API24runtime/IME/TalkBack/GPU və locale matrisi yoxlanmayıb. Məzmunlu şəhər regression-u, Plan davranışı, müqayisəli performans, sahiblik/alış ayrılması və tam asset/dizayn pariteti açıqdır; tam §1.1/mərhələ5 qəbulu deyil.

### Son nəticə və açıq tapıntı

- Son native: **10/10 PASS,33s** (Shell4 + Home6). Son normal Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`: **PASS,21s**. Son source sonrası aparılıb; native harness-in capture yenilənməsi production source-u dəyişməyib. Loglar lokal `unified-mobile-visual/shell-{native,normal}.log`-dadır. CI deyil.
- Kontrast4/4PASS: minimum6.48; palette dəyişmir. `git diff --check` PASS. Lokal diff review-da API/authority/start gating dəyişikliyi tapılmadı.
- **Vizual qəbul PASS deyil.** İlkin 320font2 Inventory/Görünüş raw baxışlarında natamam header/dock çəkilişi müşahidə edildi. Son PixelCopy turundan sonra yenidən yaradılmış360/390/320 kontaktlarda header/dock görünür. Capture ardıcıllığının ayrıca piksel-region assertion-ları hələ yoxdur; bu son kontakt baxışı ilə real cihaz/GPU nəticəsi iddia edilmir.
- Font2 Inventory toolbar/list viewport-u həddən artıq sıxır; ilkin kartın görünməsi bu turda qəbul edilməyib. Görünüş preview/picker daxili scroll alıb, lakin font2-də tam seçim→önizləmə→təsdiq input regression-u hələ yoxdur. Records boş vəziyyətinin daxili scroll ilə görünməsi assertion-la yoxlanıb. Bu açıq hallar tam adaptiv UI kimi təqdim edilmir.
- Növbəti kiçik vahid: font2 Inventory viewport-unu və Görünüş seçim axınını piksel-region+real input assertion-ları ilə düzəlt/yoxla; sonra tam skin/sahiblik/şəhər/performance vahidlərinə keç. Bu chatdə əlavə böyük vahid başlanmır.

## Davam — font2 İnventar viewport-u və Görünüş input/render regression-u

Branch `feat/unified-mobile-ui`, HEAD `446cd11`, uncommitted diff. Bu kiçik vahidin production dəyişiklikləri yalnız `InventoryScreen.kt` və `FinishCosmeticScreen.kt`-dədir; əvvəlki shell/Home işi və owner dirty fayllar qorunur. Commit/merge/push/CI/emulator yoxdur.

- Font2 İnventar toolbar-ı eyni tokenlər və minimum48dp ilə bir üfüqi scroll sırasına yığılır. Shell daxilində font2 siyahıda təkrar İnventar/Binalar başlıqları çıxarılır; Bina detalları başlığı və normal şrift kompozisiyası saxlanır. 320×568font2-də ilkin kart dock-dan əvvəl tam görünür; Ərazi toolbar girişi scroll ilə əlçatandır.
- Son kartın native toxunma regression-u əvvəlki Görünüş həllində real iç-içə scroll problemi tutdu: grid-ə scroll xarici viewport-u hərəkət etdirmirdi və kartın toxunma mərkəzi görünən sahədən kənarda qalırdı. Shell/font>1 portret qolunda preview, kateqoriyalar, iki sütunlu kart sıraları və status mətni indi bir `LazyColumn` daxilindədir. Geri/Təsdiq ayrıca sabit qalır. Preview hündürlüyü mövcud `Target * 3` token nisbətidir; yeni rəng/ölçü tokeni və alternativ skin əlavə olunmayıb. Normal şrift və wide qol eyni preview/category/card composable-larını istifadə edir.
- Host artıq dəyişən real composable selection state-i ilə işləyir: siyahının son Emalatxana divarına scroll→native touch→selected, sonra Kərpic divarına qayıdış→native touch→selected, preview-a qayıdış və eyni ölçülü native preview crop-larının fərqi; görünən Təsdiq düyməsinə native touch və seçilmiş map ilə İnventara dönüş. Lazy siyahıda hələ compose edilməyən elementlər `performScrollToNode`, görünən hədəflər `performScrollTo` ilə gətirilir; kart/təsdiq klikləri `performTouchInput`-dur. Bu gesture sürəti/fling testi deyil.
- Hər shell capture-dan sonra logo və Play semantics region-ları native PNG-dən crop olunur və rəng variasiyası yoxlanır (>32 fərqli sampled rəng): əvvəlki boş/natamam chrome capture-ı indi assertion tutar. Mövcud invalidate→frame→warm draw→frame→PixelCopy ardıcıllığı saxlanır. Preview əvvəl/sonra crop ölçüləri bərabər və pikselləri fərqlidir. Bu tam screenshot golden/paritet assertion-u deyil.

Son yoxlamalar (son source-dan sonra): **native Shell4+Home6 = 10/10 PASS,36s**; **normal `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS,27s**. Kontrast4/4PASS, minimum6.48; palette dəyişmir. `git diff --check` PASS. Loglar lokal `unified-mobile-visual/viewport-{native,normal}.log`; capture-lar `production-shell-{inventory,appearance,picker-selected,preview-before,appearance-selected}-{360-font1,390-font1,320-font2}.png`.

Final font2 İnventar/picker/preview və normal390 İnventar renderləri açılıb baxıldı; approved home chrome-u ilə yanaşı əvvəlki skin/ikon fərqləri qalır. Font2 preview/status mətninin hamısı bir anda görünmür, tək məzmun scroll-u ilə oxunur; əsas təsdiq və dock sabitdir. Ərazi üfüqi scroll-da ilkin anda qismən görünür, scroll regression-u onun açılmasını yoxlayır. Bu vahidin viewport/selection tapıntıları düzəldilib; **tam vizual paritet və §1.1 qəbulu PASS deyil**.

Backend/API/DTO/schema/wallet/authority dəyişmir; mövcud OnlineGame seçimin validlik/Ready fence və kosmetik defaults yazma kodu source üzrə yoxlandı, dəyişdirilmədi. AppBackend dəyişikliyi tələb olunmur, backend testləri işlədilməyib. Host təsdiq callback-i seçilmiş map-i tutur və route-u dəyişir; real SharedPreferences write/restart/account-isolation/Firebase/server E2E sübutu deyil. Real device/API24runtime/IME/TalkBack/GPU və locale matrisi yoxlanmayıb. Font2 recovery+24/48dp simulyasiya inset və digər əvvəlki shell route regression-ları keçir; bu, bütün ekranlar üçün inset sertifikasiyası deyil.

Növbəti addım: bu kiçik vahidi təhvil ver. Tam clean skin/sahiblik bölgüsü/şəhər/Plan və performans ayrıca açıq qalır; ayrıca göstərişsiz növbəti böyük vahidə keçmə.

## Lokal review — font2 İnventar/Görünüş (15 noyabr 2026)

Branch `feat/unified-mobile-ui`, HEAD `446cd11`; review hədəfi font2 viewport və selection vahidinin uncommitted diff-idir: `InventoryScreen.kt`, `FinishCosmeticScreen.kt`, `MobileShellRouteRenderTest.kt`. Shell/OnlineGame konteksti, PLAN §1.1 və son handoff da oxundu. Əvvəlki ümumi shell/Home və owner baseline/design/proposal işləri bu dar review-un ayrıca qəbul hədəfi deyil; hamısı qorunur.

**Nəticə: yeni konkret bug tapılmadı.** Production source, asset və harness dəyişdirilmədi; düzəliş tələb edən yeni tapıntı yoxdur. Font2 toolbar-ın bounded üfüqi scroll-u, siyahı viewport-u, tək LazyColumn və sabit Geri/Təsdiq, shared category/card renderer-in normal şrift qolunda istifadəsi yoxlandı. Seçim/availability/validity qaydaları, account/Ready fence və defaults persistence callback-i dəyişmir; backend/API/domain dəyişmədiyindən AppBackend dəyişikliyi tələb olunmur, backend testləri işlədilmədi.

Təkrar yoxlamalar: **native Shell4+Home6 10/10 PASS,42s** (XML:0failure/0error); **normal core/engine/app unit+assembleDebug+lintDebug PASS,3s**, bəzi task-lar UP-TO-DATE. Kontrast4/4PASS minimum6.48; `git diff --check` PASS. Loglar lokal `unified-mobile-visual/viewport-review-{native,normal}.log`. Native testlər ilkin kart/dock sərhədi, toolbar scroll, son kart→geri seçim native touch, selected state, eyni ölçülü preview pixel diff və təsdiq/map/Inventory dönüşünü təkrar təsdiqlədi. Mövcud logo/Play PNG-region, route, recovery/nullPlay və simulyasiya inset yoxlamaları da keçdi.

Yenidən yaradılmış font2 Inventory/picker/preview görüntüləri açılıb baxıldı; ilk kart görünür, seçilmiş kart native işarə alır, preview dəyişir, Təsdiq və dock sabitdir. Ərazi ilkin anda qismən görünür və üfüqi scroll ilə açılır; preview alt mətni və əlavə məzmun tək scroll ilə oxunur. Bunlar əvvəl qeyd edilmiş adaptasiyalardır. Təsdiqli home ilə əvvəlki clean skin/ikon/kompozisiya fərqləri qalır; **tam paritet/§1.1 qəbulu verilmir**. Synthetic host təsdiq callback-i real defaults write/restart və server/Firebase E2E-ni əvəz etmir. Device/API24runtime/IME/TalkBack/GPU/locale/planşet yoxlanmayıb. CI, commit, merge, push və emulator yoxdur.

Review edilmiş faylların SHA256 izi: InventoryScreen.kt `07aa190f31e08bf3a72f882e96ca54146597f2f03890e0dffa95e3ccf78c99b2`; FinishCosmeticScreen.kt `492e754d3a99353751e389ee87f860003c8e01af52f8320747659f692a2d5c9c`; MobileShellRouteRenderTest.kt `1d32f4191d00f891624ddc97c9025403d2b92a2bfc701e23e4994fbc7514d0f5`.

## Lokal commit/integration sərhədi — 15 noyabr 2026

İstifadəçi lokal commit, main merge və push tapşırdı; Font2 vahidinin hələ commit edilməmiş shell/Home-dan asılılığı göstərildikdən sonra prerequisite-in ayrıca commit kimi daxil edilməsini açıq təsdiqlədi. Prerequisite `f727fe8` əvvəlki shell/Home source, approved unified-mobile-v3 istinadları, PLAN §1.1 və Home harness-i saxlayır. Sonrakı Font2 commit-i yalnız Inventory/FinishCosmetic font2 delta-sı, MobileShellRouteRenderTest və bu vahidin review/handoff qeydlərini saxlayır. Production source/harness review SHA256 izlərindən dəyişmir; yalnız index üzrə bölünüb.

Owner baselineProfiles/0,1 app-release.dm və account-compact proposal/visual faylları stage edilmədi, stash/reset edilmədi. Fetch sonrası lokal main və origin/main ayrılmır. Son native10/10PASS42s və normal unit/APK/lintPASS3s qüvvədədir; commit bölünməsi yeni source implementasiyası deyil. CI ayrıca tələb edilmədiyindən işlədilmir. Merge tarixçəni saxlayan --no-ff ilə və push istifadəçinin açıq göstərişi əsasında aparılır; yekun hash-lər Git tarixçəsi/agent təhvilində yoxlanır. Tam §1.1/paritet qəbulu elan edilmir.
