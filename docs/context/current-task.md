# Vahid mobil UI — commit və inteqrasiya təhvili (15 noyabr 2026, cari)

- Completed: istifadəçi Font2 vahidi ilə onun əvvəlki review edilmiş shell/Home prerequisite-ini birlikdə commit, tarixçə saxlanan main merge və push üçün açıq təsdiqlədi. Prerequisite `f727fe8`; font2 source/harness və vahidin review/handoff qeydləri ayrıca növbəti commit-də saxlanır.
- Current state: funksional source review edilmiş SHA256 izləri ilə eynidir; commit bölünməsi index üzərində edilib, owner işləri stash/reset edilməyib. Son inteqrasiya hash/status-u Git tarixçəsi ilə yoxla.
- Decisions: baselineProfiles/0,1 `app-release.dm`, account-compact proposal/visual faylları commitdən kənarda qalır. CI ayrıca istənməyib və işə salınmır. Backend müqaviləsi dəyişmir.
- Checks performed: son lokal review native10/10PASS42s, normal core/engine/appunit+APK/lintPASS3s; contrast4/4min6.48, staged/worktree diffcheck. Source/harness bu yoxlamalardan sonra dəyişməyib. Remote main fetch edildi, lokal main ilə ayrılma yoxdur.
- Remaining work: tam skin/paritet, sahiblik/şəhər/Plan/performance və əvvəlki runtime məhdudiyyətləri qalır; bu commit onları tamamlanmış saymır.
- Relevant files: `docs/PLAN.md`§1.1; `docs/reviews/2026-11-15-unified-mobile-ui.md` son lokal review/commit bölməsi; InventoryScreen.kt, FinishCosmeticScreen.kt, MobileShellRouteRenderTest.kt.
- Exact next step: bu commitdən sonra istifadəçinin təsdiqinə əsasən `main`-ə `--no-ff` merge et, origin/main push et və hash-ləri/owner status-u yoxlayıb bildir. Ayrıca CI işlətmə, yeni feature başlama.

---

# Font2 İnventar/Görünüş — lokal review (15 noyabr 2026, cari)

- Completed: font2 viewport/selection uncommitted diff-i lokal review edildi; yeni konkret bug tapılmadı. Production source/assets/harness dəyişmədi; review qeydi yeniləndi.
- Current state: `feat/unified-mobile-ui`, HEAD `446cd11`; əvvəlki dirty/owner işlər qorunur. Commit/CI/merge/push/emulator yoxdur.
- Decisions: bu dar font2 review tam unified UI/§1.1 və skin pariteti qəbulu deyil. Backend müqaviləsi dəyişmir, AppBackend dəyişikliyi/testi tələb olunmadı.
- Checks performed: təkrar native10/10PASS42s; normal core/engine/appunit+APK/lintPASS3s (qismən UP-TO-DATE); contrast4/4min6.48; diffcheckPASS. Final font2 Inventory/picker/preview görüntüləri oxundu. Synthetic host; real defaults write/restart/server E2E deyil.
- Remaining work: əvvəlki skin/sahiblik/şəhər/Plan/performance və device/API24/IME/TalkBack/GPU/locale məhdudiyyətləri qalır; yeni tapıntı açılmadı.
- Relevant files: `docs/PLAN.md`§1.1; `docs/reviews/2026-11-15-unified-mobile-ui.md` son lokal review bölməsi (fayl SHA256 izləri); InventoryScreen.kt, FinishCosmeticScreen.kt, MobileShellRouteRenderTest.kt; lokal `viewport-review-{native,normal}.log`.
- Exact next step: lokal review nəticəsini təhvil ver; ayrıca göstərişsiz commit/CI/merge/push və yeni böyük vahid başlama.

---

# Vahid mobil UI — font2 viewport və Görünüş axını (15 noyabr 2026, cari)

- Completed: `feat/unified-mobile-ui` üzərində font2 İnventar toolbar bir üfüqi scroll sırasıdır; shell-də təkrar font2 başlıqları çıxarıldı, ilk kart dock-dan əvvəl görünür. Görünüş font2 nested scroll problemi tək LazyColumn ilə düzəldi; preview/kateqoriya/iki sütunlu kartlar bir scroll, Geri/Təsdiq sabitdir.
- Current state: HEAD `446cd11`, source uncommitted; əvvəlki shell/Home və owner dirty işlər qorunur. Commit/merge/push/CI/emulator yoxdur. Bu kiçik düzəliş hazırdır, tam §1.1/vizual qəbul deyil.
- Decisions: mövcud təsdiqli dizayn/token/art saxlanır; mobil portret, normal şrift/wide kompozisiyası qorunur. Backend/API/domain/wallet/defaults persistence müqaviləsi dəyişmir; AppBackend dəyişikliyi lazım deyil, backend testləri yoxdur.
- Checks performed: final native Shell4+Home6=10/10PASS36s; normal core/engine/appunit+APK/lintPASS27s; contrast4/4min6.48; diffcheckPASS. 360×640font1,390×844font1,320×568font2 + recovery/font2 simulyasiya24/48dp inset. Toolbar scroll, son kart→geri seçim native touch, selected state, bərabər ölçülü preview pixel diff, sabit confirm touch→Inventory/map assertion; logo/Play native PNG region assertion-ları. Final renderlər açılıb baxıldı. Synthetic host callback-dir, real defaults write/restart/server E2E deyil.
- Remaining work: tam clean skin/ikon/hero pariteti, sahiblik/alış bölgüsü, city read/Plan regression və ölçülmüş performans. Font2 məzmun scroll tələb edir; tablet/device/API24/IME/TalkBack/GPU/locale matrisi yoxlanmayıb. Əvvəlki aşağıdakı font2 viewport/selection açıq qeydləri bu vahid üçün tarixçədir.
- Relevant files: `docs/PLAN.md`§1.1; `docs/reviews/2026-11-15-unified-mobile-ui.md` son font2 bölməsi; `InventoryScreen.kt`, `FinishCosmeticScreen.kt`; `docs/reviews/mobile-sizing-visual/render/MobileShellRouteRenderTest.kt`; lokal `docs/reviews/unified-mobile-visual/viewport-{native,normal}.log`, `production-shell-*.png`.
- Exact next step: hazır font2 düzəlişi/input nəticəsini təhvil ver; ayrıca göstərişsiz yeni böyük vahid/commit/merge/CI/push/planşet/mərhələ6 başlama.

---

# Vahid mobil UI — real menu shell inteqrasiyası (15 noyabr 2026, cari)

- Completed: `feat/unified-mobile-ui` üzərində ortaq `MobileMenuShell` OnlineGame-in mövcud city/inventory/parts/appearance/purchase/market/achievements/records/profile/settings route-larına qoşuldu. Təkrar logo/balans/destination dock gizlənir, contextual geri/yenilə/edit/settings qalır. Play əvvəlki nullable lifecycle callback-idir. İnventar/Görünüş və Uğurlar/Rekordlar tab girişləri var; Görünüş geri/təsdiq İnventara dönür.
- Current state: HEAD `446cd11`, source uncommitted. Owner baselineProfiles/design/proposal və əvvəlki Home/typeface işi qorunur. Commit/merge/push/CI/emulator yoxdur. Shell funksional inteqrasiyası var, tam vizual qəbul YOXDUR.
- Decisions: dizayn/tətbiq təsdiqlidir, yenidən soruşma. Mobil portret; pre-auth/active game/pause dock-suz, tablet/landşaft dəyişmir. Source API/DTO/domain/schema/wallet/journal/Ready fence dəyişmir; AppBackend dəyişikliyi lazım deyil, backend testləri işlədilməyib.
- Checks performed: son native Shell4+Home6=10/10PASS33s; son normal core/engine/appunit+APK/lintPASS21s; contrast4/4min6.48; diffcheckPASS. 360×640font1,390×844font1,320×568font2; recovery/nullPlay/longbalance+24/48dp simulyasiya inset. Synthetic repository/transport host, real server E2E deyil. Kontaktlar, raw font2 Inventory/Görünüş/Profile/Records və recovery approved home chrome-u ilə müqayisə edildi.
- Remaining work: Son PixelCopy turundan sonra yenidən çıxarılan360/390/320 kontaktlarda header/dock görünür; ilkin natamam raw capture müşahidəsi üçün ayrıca piksel-region regression hələ yoxdur. Inventory toolbar list viewport-unu sıxır; font2 appearance selection→preview→confirm input yoxlaması açıqdır. Records font2 məzmunu bounded scroll ilə görünür. Bütün bunlar visual PASS deyil. Tam clean skin/ikon/hero pariteti, sahiblik/alış bölgüsü, city read/Plan regression və ölçülmüş performans da açıqdır. Real device/API24/IME/TalkBack/GPU/locale matrisi yoxlanmayıb.
- Relevant files: `docs/PLAN.md`§1.1; `docs/reviews/2026-11-15-unified-mobile-ui.md` son davam bölməsi; `docs/design/unified-mobile-v3/`; `MobileMenuShell.kt`, `MobileMenuNavbar.kt`, `OnlineGame.kt`, `HomeScreen.kt`, `MobileHomeScreen.kt`, `InventoryScreen.kt`, `FinishCosmeticScreen.kt`, `LeaderboardScreen.kt`, contextual chrome faylları. Host `docs/reviews/mobile-sizing-visual/render/MobileShellRouteRenderTest.kt`; lokal `docs/reviews/unified-mobile-visual/{production-shell-*,shell-contact-*,shell-native.log,shell-normal.log}`.
- Exact next step: eyni branch-də yalnız font2 Inventory viewport-unu düzəlt, Görünüş seçim axınını və capture ardıcıllığını yoxla; native frame/piksel-region və selection/scroll/confirm input regression-u ilə düzəlt. Sonra review/handoff-u yenilə. Yeni plan/stack/planşet/mərhələ6 açma; CI/emulator/merge/push avtomatik yoxdur.

---

# Vahid mobil UI — ilk Home naviqasiya vahidi (15 noyabr 2026, cari)

- Completed: `feat/unified-mobile-ui` Home-da 7 giriş/real callback, mərkəzi Play, üst profilsiz sağ balans; reusable navbar/header və eyni bundled Nunito ortaq GameTypography faylındadır. Bu yalnız ilk hissədir, tam §1.1 deyil.
- Current state: HEAD446cd11, source uncommitted. Owner baselineProfiles/design/proposal dirty işləri qorunur. Commit/merge/push/CI/emulator yoxdur.
- Decisions: dizayn və tətbiq təsdiqlidir, yenidən soruşma. Son home authority qüvvədədir. Native minimum48dp üçün dar dock yanları iki sıraya yığılır, font2 labels a11y adları ilə əvəz olunur; tam vizual paritet deyil. Offline Play host callback icazəsi ilə işləyir; status özlüyündə ikinci authority deyil.
- Remaining work: bütün menyulara shell; clean relyefli skin/ikon/hero və home nisbətləri/XP; İnventar/Görünüş, Bazar/sahiblik, Uğurlar tabları; city read/Plan səbəb+regression; müqayisəli route performansı. AppBackend API/domain dəyişməyib, bu ilk UI vahidi backend dəyişikliyi tələb etmir.
- Relevant files: `docs/PLAN.md`§1.1, `docs/design/unified-mobile-v3/`, `docs/reviews/2026-11-15-unified-mobile-ui.md`; `MobileHomeScreen.kt`, `MobileMenuNavbar.kt`, `GameTypography.kt`, `ProfilePresentation.kt`, `OnlineGame.kt`; `docs/reviews/mobile-sizing-visual/render/HomeProductionRenderTest.kt`; lokal `docs/reviews/unified-mobile-visual/production-*.png`.
- Checks performed: son native6/6PASS26s; normal core/engine/appunit+APK/lintPASS38s; contrast4/4min6.48; native renderlər açılıb approved home ilə müqayisə edildi. Flat chrome/blurred hero/nisbətlər açıqdır; visual parity PASS deyil. Real device/API24/GPU/TalkBack/server E2E yoxlanmayıb.
- Exact next step: mövcud OnlineGame menu route-larını reusable mobil shell ilə birləşdir; content-in daxili header/footer-lərini ikiqat göstərmə. Əvvəl bu handoff+review+PLAN§1.1 oxu. Yeni stack/plan/planşet/mərhələ6 açma.

---

# Vahid mobil UI — branch və icra planı hazır (15 noyabr 2026, cari)

- Completed: istifadəçinin tətbiq göstərişi ilə `feat/unified-mobile-ui` branch-i `main@446cd11` üzərindən açıldı; `docs/PLAN.md` §1.1-də konkret fix/implementasiya ardıcıllığı və qəbul şərtləri yazıldı. 5lövhə + son təsdiqli home PNG `docs/design/unified-mobile-v3/` daxilində saxlandı.
- Current state: branch/plan/istinad hazırlığı tamamdır; production source implementasiyası BAŞLAMAYIB. Əvvəlki owner release2.dm və account-compact review/untracked faylları qorunur. Commit/merge/push/CI/emulator yoxdur.
- Decisions: istifadəçi DİZAYN VƏ İMPLEMENTASİYANI TƏSDİQLƏYİB, yenidən təsdiq istəmə. Son authority `home-approved-final.png`: Ana/Şəhər/İnventar/Oyna/Bazar/Uğurlar/Profil; Oyna navbarın dördüncü slotunda tam ortada, ayrıca yuxarı düymə deyil. Üst profil YOXDUR, balans sağ kənarda. Aşağıdakı6item/üstavatar/pendingapproval qeydləri köhnə tarixçədir. Bütün menyu ekranlarının chrome-u son home-a uyğunlaşır. Kolleksiya vahid İnventar/Görünüş-ə birləşir; sahiblik İnventarda, alışlar Bazarda; Uğurlar Nailiyyətlər/Rekordlar tabları; Profil alt menyudan,settings Profildən. Aktiv oyun/pauza və pre-auth dock-suz qalır.
- Remaining work: PLAN§1.1-in 4vahidinin hamısı açıqdır: ortaq native skin/header/navbar+realroute; bütün screen inteqrasiyası və city read/Plan click regression; çatışmayan yüksək keyfiyyətli assetlər+ölçülmüş route axıcılığı; fokuslu unit/build/lint/native render/review. Tək navbar və ya animasiya bütün işi tamamlamır. Backend uyğunluğu lazım olan hər axında yoxlanacaq, hazırda müqavilə/source dəyişməyib.
- Relevant files: `docs/PLAN.md`§1.1; `docs/design/unified-mobile-v3/{README.md,home-approved-final.png,01-main.png,02-market.png,03-account.png,04-game-states.png,05-owned-and-listings.png,prompts.json}`; app `GameTokens.kt`,`MobileUiSizing.kt`,`MobileHomeScreen.kt`,`HomeScreen.kt`,`GameNavigation.kt`,`OnlineGame.kt`; existing host `docs/reviews/mobile-sizing-visual/render/`.
- Checks performed: branch/dirty status, scoped route/home/token/harness source baxışı; PNG byte-copy/hash və git diffcheck. Build/unit/lint/native render/PERF hələ icra edilməyib; PASS deyil. Özəl backend root cause-u hələ tapılmayıb.
- Exact next step: yeni chatdə bu handoff + PLAN§1.1 + unified-mobile-v3 istinadlarını oxu, sonra native ortaq chrome/7-slot navbar ilə ilk real inteqrasiya vahidinə başla. Yoxlamaları mövcud host harness ilə et, emulator açma. AppBackend contract dəyişikliyi tələb olarsa onun AGENTS/branch/test qaydalarına əməl et. Plan ayrıca fayla köçürülmür.

---

# Vahid mobil UI — naviqasiya PNG təklifi, 15 noyabr 2026 (cari)

- Completed: production UI dəyişmədən built-in ImageGen ilə 5 PNG lövhə / 27 ekran-vəziyyət hazırlandı; şəkillərə baxıldı. Əvvəlki 5-item/yanlış bölməli təkliflər cari deyil.
- Current state: yalnız dizayn təklifi, son PNG-lərin açıq tətbiq təsdiqi hələ yoxdur. Mənbə kodu/backend dəyişməyib. Əvvəlki profil işi və owner dirty fayllar qorunur.
- Decisions: istifadəçi ekranların aid olduğu bölmələri/giriş yollarını düzəltməyi və 6 alt-menu girişini təsdiqlədi: Ana / Şəhər / Oyna / İnventar / Bazar / Uğurlar. Oyna üçüncü slotda, tam mərkəz tələbi bu təklifdə saxlanmır. Şəhər = Görünüş/Plan/placement; İnventar = sahib olunan Binalar/Görünüş/Dekor/Ərazi; Bazar = Binalar/Dekor/Ərazi alışları + Mənim elanlarım/Yeni elan; Uğurlar = Nailiyyətlər/Rekordlar. Profil avatardan, settings profildən. Görünüş kataloq+önizləmə+təsdiq vahid ekrandır; ayrıca Kolleksiya yoxdur. Aktiv oyun/pauza və pre-auth girişdə alt-menu yoxdur. Yeganə plan docs/PLAN.md; bu handoff ikinci plan deyil.
- Relevant files: /Users/mac/.codex/visualizations/2026/11/15/01a1df44-41e0-7692-b389-1fc21bc824d9/navigation-v2/{01-main.png,02-market.png,03-account.png,04-game-states.png,05-owned-and-listings.png,prompts.json}; GameTokens.kt; docs/design/profile-v2 və game-tap-v2 istinadları.
- Checks performed: final5PNG vizual oxundu; six-menu item order/section markers/entry buttons baxıldı. Bazar caption-ları arasındakı yanlış sequential oxlar silindi; owned land-in səhv XP capacity mətni çıxarıldı; error state Play skin düzəldildi. Bunlar generativ bitmap-dir, pixel-exact ortaq komponent və real48dp/font2/responsive/runtime/sürət/input sübutu deyil. Qiymət/XP/area/count göstəriciləri illüstrativdir, domain qaydası yaratmır.
- Remaining work: istifadəçinin bu son PNG-lərə düzəlişi və ya açıq tətbiq təsdiqi; sonra lazımi source/clean assets+ortaq real komponentlər, həqiqi route inteqrasiyası, city read root cause və keçid performansı araşdırması, fokuslu test/native render. Bütün tətbiq problemləri həll edilmiş sayılmır.
- Exact next step: əvvəl bu5PNG-ni göstər. Konkret tətbiq təsdiqi olmadan app UI implementasiyasına başlama; ayrıca göstərişsiz emulator/CI/merge/push/deploy yoxdur.

---

# Profil v2 — müstəqil lokal review (15 noyabr 2026, cari)

- Completed: hazır profile-v2 source/assets/harness diff-i son clean-skin review və docs/PLAN.md əsasında lokal review edildi; yeni konkret bug tapılmadı, production source dəyişmədi.
- Current state: `feat/profile-approved-design`, HEAD `3e9af84`; review tamamlandı. İstifadəçinin göstərişi ilə yalnız profil vahidi lokal commit-də saxlanır, owner release .dm/proposal və əvvəlki əlaqəsiz handoff saxlanır.
- Decisions: yeganə plan `docs/PLAN.md`; backend müqaviləsi dəyişmir, AppBackend dəyişikliyi lazım deyil. Tam ekran piksel pariteti/mərhələ5 qəbulu ayrıca açıqdır; planşet/mərhələ6 başlamır.
- Checks performed: təkrar native15/15PASS59s; normal255unit/APK/lintPASS3s; kontrast4/4min8.22, asset SHA parity və diffcheckPASS. Mövcud comparison/contact və təkrar raw reference-profile/small-edit/font-profile/font-edit görüntüləri oxundu.
- Remaining work: real device/API24/HTTP/IME/inset/TalkBack/GPU/locale yoxlanmayıb. CI/emulator/merge/push/deploy yoxdur.
- Relevant files: `docs/reviews/2026-11-15-profile-v2-implementation.md` son lokal review; `docs/reviews/profile-v2-visual/local-review-*`; əvvəlki cari clean-skin handoff aşağıdadır.
- Exact next step: lokal review/commit nəticəsini bildir; ayrıca göstərişsiz merge/push/CI və yeni feature başlama.

---
# Profil v2 — mətnsiz skin/lent/stat/navbar/capture tamamlandı (15 noyabr 2026)

- Completed: istifadəçinin göstərişi ilə built-in ImageGen edit-dən clean chromeV2+panelV3 hazırlandı/qoşuldu; approved PNG dəyişmir. Lent+rail vahid slice, geniş text-free interior, tam alt kontur; köhnə maska/interpolasiya/texture təkrarı çıxarıldı. Stat separator/ölçü və navbar nisbətləri yeniləndi; compact Dəyiş tam48dp görünür. Native hardware PixelCopy, real input scroll, glyph+üst/alt outline piksel assertion-ları işləyir.
- Current state: `feat/profile-approved-design`, baza `3e9af84`, iş uncommitted; owner release .dm/proposal/başqa working fayllar qorunur. Bu konkret düzəliş vahidi hazırdır; tam mərhələ5/pixel-identical bütün ekran qəbulu deyil.
- Decisions: son istifadəçi clean skin-i özümün hazırlamağı açıq tapşırıb; asset çatışmazlığı artıq blok deyil. Yeganə plan `docs/PLAN.md`; native Nunito və real server məzmunu saxlanır. Save/cancel/draft/exact retry/session fencing/API dəyişmir; AppBackend dəyişikliyi lazım deyil. Mobil portret; planşet/mərhələ6 başlamır.
- Remaining work: bu vahiddə açıq konkret skin/maska/stat/navbar/input-capture qüsuru müşahidə olunmadı. Real cihaz/API24/HTTP/IME/inset/TalkBack/GPU/locale yoxlanmayıb; phase5 ümumi qəbul ayrıca qalır. Font2 input capture real scroll vəziyyətidir; portretin bir hissəsi viewport-dan yuxarıdır.
- Relevant files: `ProfilePresentation.kt`, `GameTokens.kt`; drawable-nodpi `profile_{skin,panel}_clean.png`; `docs/design/profile-v2/{README.md,skin-generation-prompts.md,profile-skin-clean-v2.png,profile-panel-clean-v3.png}`; profil-v2 review-un son bölməsi; `docs/reviews/phase5/final-review.md`; `profile-v2-visual/p3-{profile-comparison.png,contact.png,native-final.log,native-results.txt,normal-final.log,normal-results.txt,contrast.txt}` və input-proof.txt-lər; render harness.
- Checks performed: final native15/15PASS1m; normal255unit/APK/lintPASS23s; kontrast4/4min8.22, approved/production PNG byte parity və diffcheckPASS.390×844font1,360×640font1,320×568font2 contact və eyni390×761 comparison oxundu. Son lokal source/diff review aparıldı. Emulator/CI/commit/merge/push/deploy yoxdur.
- Exact next step: bu hazır vahidi təhvil ver. Ayrı göstərişsiz yeni feature/mərhələ/commit/merge/push/CI başlama. Sonrakı istifadəçi review/commit istəsə yeni chatdə bu handoff + son review + docs/PLAN.md əsasında owner fayllarını ayıraraq davam et.

---
# Profil v2 P1 tekstura/lent/nisbət davamı — 15 noyabr 2026 (cari)

- Completed: source yazısız texture strip; təkrarlanan portret kölgəsi sınağı rədd edildi; mənbənin alt konturundan bütöv üst rail; lent silhouette düzəlişi; normal profil portreti136/badge144/navbar56, compact navbar40; profil horizontal inset8. Editor ölçüləri saxlanır.
- Current state: `feat/profile-approved-design`, baza `3e9af84`, uncommitted owner diff qorunur. P1 qismən düzəlib; tam paritet/mərhələ5 PASS deyil.
- Decisions: yeganə plan `docs/PLAN.md`; approved PNG dəyişmir; alternativ art/ümumi istisna yoxdur. Server data/callback/save/cancel/retry/API dəyişmir; backend dəyişikliyi lazım deyil. Mobil portret, planşet/mərhələ6 başlamır.
- Remaining work: source texture təkrarı orijinal bütöv fon deyil; lent overlap və header/stat/nav nisbətləri açıqdır. Dəqiq örtülü fon üçün clean skin/layer asset-i yoxdur. Son editor route input glyph/border capture fraqmentləri natamamdır; visual PASS deyil. Device/API24/HTTP/IME/inset/TalkBack/GPU/locale yoxlanmayıb.
- Relevant files: `ProfilePresentation.kt`, `GameTokens.kt`; `docs/design/profile-v2/README.md`; `docs/reviews/2026-11-15-profile-v2-implementation.md` son bölmə; `docs/reviews/profile-v2-visual/p2-{profile-comparison.png,contact.png,native.log,normal.log,contrast.txt}`; `docs/reviews/phase5/final-review.md`.
- Checks performed: native14/14PASS54s; normal255unit/APK/lintPASS21s; kontrast4/4min8.22; source byte parity/diffcheckPASS.390×844font1,360×640font1,320×568font2 contact və comparison oxundu. Emulator/CI/commit/merge/push/deploy yoxdur.
- Exact next step: P1-i bitmiş saymadan cari müqayisəni təhvil ver; dəqiq clean skin mənbə çatışmazlığını və qalan lent/stat/navbar/capture fərqlərini açıq saxla. Yeni plan/stack/feature açma.

---
# Profil v2 P1 davam turu — 15 noyabr 2026 (cari)

- Completed: profilin öz relyefli panel/wallet/identity/navbar source skin-ləri, dairəvi identity crop, real pencil callback48dp; badge/stat/XP/nav ölçü-boşluq düzəlişləri. Frame+idle invalidate-dən sonra capture ilə son9route başlığı tam görünür. Save/cancel/exact retry/server authority saxlanır.
- Current state: `feat/profile-approved-design`, baza `3e9af84`, iş uncommitted. Owner release .dm/proposal və əvvəlki iş qorunur. Commit/merge/push/CI/deploy/emulator yoxdur.
- Decisions: təsdiqli PNG-lər dəyişmir, şrift əvvəlki Nunito qərarıdır; ümumi vizual istisna yoxdur. Backend müqaviləsi dəyişmir, DB suite təkrarlanmayıb. Mobil portret; planşet/mərhələ6 başlamır.
- Remaining work: P1 açıq: panel interior teksturası interpolasiya ilə tam bərpa olunmur; lent maska/overlap konturu və header/portrait/stat/nav nisbətləri istinadla birəbir deyil. Wallet/pencil/identity və route prefix capture əvvəlki açıq statusları bu turla yenilənir. Real cihaz/API24/HTTP/IME/inset/TalkBack/GPU/locale yoxlanmayıb.
- Relevant files: `docs/PLAN.md`; `docs/design/profile-v2/README.md`+approved PNG-lər; `ProfilePresentation.kt`, `PublicAccountScreen.kt`, `GameTokens.kt`; `docs/reviews/2026-11-15-profile-v2-implementation.md` son bölmə; `docs/reviews/phase5/final-review.md`; `profile-v2-visual/p1-profile-comparison.png`, `p1-continuation-contact.png`, render harness.
- Checks performed: final native14/14PASS51s; normal136+95+24=255unit/APK/lintPASS32s; explicit text contrast4/4min8.22; diffcheckPASS.390×844font1,360×640font1,320×568font2 profil/editor/picker contact və final390müqayisə oxundu. Tam P1/mərhələ5 PASS deyil.
- Exact next step: son müqayisə əsasında yalnız qalan source tekstura/lent konturu/kompozisiya nisbətlərini davam etdir; digər scope açma. Header capture artıq son9route-də tamdır. Təkrar böyük repo scan/yeni plan/stack/CI/merge/push yoxdur.

---
# Profil v2 P1 vizual düzəliş turu — 15 noyabr 2026 (cari)

- Completed: Nunito400/700/900 statik native font+OFL (istifadəçi “özün tap hazırla”), Az cmap; text-free single-source skin/nine-slice, ilkin seams/bitmap təkrarı təmizləndi; tam builder/pilot/memar çərçivə crop-ları; source lent maskası/overlap; badge baked7 düzbucaqlı patch əvəzinə interior gradient+real rəqəm; XP/editor sayğacı/üç-avatar sheet nisbətləri düzəldildi. Save/cancel/retry/server authority saxlanır.
- Current state: `feat/profile-approved-design`, baza `3e9af84`, bütün iş uncommitted. Owner release .dm/proposal və əvvəlki iş qorunur. Commit/merge/push/CI/deploy/emulator yoxdur.
- Decisions: Nunito seçimini agent hazırlayır; təsdiqli PNG-lər dəyişmir. Bu, digər vizual fərqlərə ümumi istisna deyil. Backend API/schema/domain dəyişmir; backend dəyişikliyi lazım deyil, DB suite təkrarlanmayıb. Mobil portret; mərhələ6/planşet başlamır.
- Remaining work: **P1 hələ açıqdır:** source panel relyefi/gradient, lent maskasının konturu, wallet pill, identity pencil/aşağı identity kartı və profil/stat/nav ölçü-boşluqları təsdiqli nümunə ilə birəbir deyil. `p1-profile-comparison.png` konkret fərqləri göstərir. Frame+idle seçilmiş picker-i capture-a çatdırdı, lakin bəzi route header glyph-ləri hələ natamamdır; cold editor başlıqları tamdır. Natamam capture/paritet PASS deyil. Real device/API24/HTTP/IME/inset/TalkBack/GPU/locale yoxlanmayıb.
- Relevant files: `docs/PLAN.md`, `docs/design/profile-v2/README.md`+approved PNG-lər; `ProfilePresentation.kt`, `PublicAccountScreen.kt`, `GameTokens.kt`, `CommunityArt.kt`; res/font/profile_nunito_*.ttf; assets/licenses/Nunito-OFL.txt; `docs/reviews/2026-11-15-profile-v2-implementation.md` son P1 bölməsi; `docs/reviews/phase5/final-review.md`; `docs/reviews/profile-v2-visual/p1-*` və render harness.
- Checks performed: final source+frame-flushed harness14/14PASS55s; normal core136+engine95+app24=255unit/APK/lintPASS34s; kontrast4/4min8.22; statik font/Az cmap/source parity/diffcheckPASS.390×844font1,360×640font1,320×568font2 profil/editor/picker, əlavə cold editor PNG-lər və yanaşı müqayisə oxundu. Müqayisə production composable renderidir, generativ yeni nümunə deyil. Tam P1/mərhələ5 qəbul edilmir.
- Exact next step: yeni chatdə yalnız qalan P1 mask/skin/kompozisiya və host capture vahidini bu konkret müqayisədən davam etdir; yeni dizayn/stack/plan uydurma. Review-lardakı son P1 bölmələri cari authority-dir; aşağıdakı qeydlər tarixçədir. İstifadəçiyə cari native müqayisəni və açıq statusu göstər. Yeni böyük addım/merge/CI/push avtomatik yoxdur.

---
# Profil v2 funksional inteqrasiya və mərhələ5 review — 15 noyabr 2026

- Completed: istifadəçi profil/redaktə/ayrı avatar grid dizaynlarını təsdiqlədi; real ProfileEditor axınına qoşuldu. Mənbə PNG-lər docs/design/profile-v2-də saxlanır; art/skin source kəsikləri istifadə olunur. Navbar, server səviyyə/XP/rekord, ad15, draft cancel, avatar apply→Save, exact retry/conflict sərhədləri qorunur. Mərhələ5 lokal review aparılıb; tam qəbul verilməyib.
- Current state: `feat/profile-approved-design`, baza `3e9af84`, working diff uncommitted. Source bitmap-lər provider ilə paylaşılır. Owner iki release baseline .dm və köhnə compact proposal qorunur. Commit/merge/push/CI/deploy yoxdur.
- Decisions: yeni profil-v2 dizaynı təsdiqlidir, əvvəlki account-states profilini əvəz edir; yenidən dizayn seçim sualı açma. Çox-avatar nümunəsinin əlavə personajları real kataloqa keçirilmir; mövcud3ID qalır. Settings/fitted rekord görünüşü dəyişmir. User bitəndə real ekran şəklini istəyir: native production renderini model-generated nümunə ilə qarışdırma.
- Remaining work: **P1 — tam vizual paritet açıqdır:** native typo, skin seams/crop və dinamik badge mərkəzi təsdiqli generativ PNG ilə birəbir deyil; bəzi small/font2 native header capture-ları natamamdır. Ümumi vizual istisna təsdiqi yoxdur. Funksional inteqrasiya tam dizayn qəbulu kimi göstərilmir. Real device/API24/HTTP/IME/TalkBack/GPU/locale matris yoxlanmayıb; emulator tələb olunmur/açılmır. Mərhələ6 başlanmır.
- Relevant files: `docs/PLAN.md`; `docs/design/profile-v2/{README.md,profile-approved.png,edit-avatar-approved.png,avatar-grid-approved.png}`; `PublicAccountScreen.kt`, `ProfilePresentation.kt`, `GameTokens.kt`, `CommunityArt.kt`; review `docs/reviews/2026-11-15-profile-v2-implementation.md`, `docs/reviews/phase5/final-review.md`; `docs/reviews/profile-v2-visual/{regular-profile.png,regular-edit.png,regular-picker.png,small-*.png,font-*.png,render/,native-results.txt,checks.txt}`.
- Checks performed: son profil native12/12PASS42s; 30sintetikID grid-scroll/fixed apply, real route save/cancel/picker,3portret ölçü. Son feedback callback-only sonrası picker→save1/1PASS16s. Market3/3PASS22s, fitted16/16PASS62s. Final normal core136+engine95+app24=255 unit/APK/lint PASS18s; checks.txt-də saxlanır. Kontrast4/4min8.22, source/drawable byte parity və diffcheckPASS. Regular3 və small/font2 UI renderlərinə baxılıb; natamam capture/paritet məhdudiyyəti review-dadır. Backend müqaviləsi main integration cbd0eeb-də read-only yoxlandı; API/schema dəyişikliyi lazım deyil, DB suite bu tur təkrarlanmadı.
- Exact next step: yeni chatdə bu handoff+profil review+phase5 final-review+PLAN əsasında yalnız P1 vizual paritet vahidini tamamla; təsdiqli görünüşün composition/font/skin/source crop nisbətlərini düzəlt, real renderləri yanaşı yoxla və istifadəçiyə göstər, sonra mərhələ5 review-u yenilə. Yeni mərhələ və avtomatik commit/merge/CI/push yoxdur.

---
# Android settings review və lokal commit — 15 noyabr 2026

- Completed: settings diff review edildi; SoundPool callback qeydiyyatı load-dan əvvələ keçirildi; sürətli completion/sound-off/background regression testi əlavə edildi. İstifadəçinin göstərişi ilə settings source/assets/review/harness lokal commit edilir.
- Current state: feat/settings-flow, baza a192f4d; owner release baseline və ayrıca compact proposal faylları commit-dən kənarda qorunur. Aşağıdakı əvvəlki uncommitted statuslar tarixçədir.
- Decisions: yalnız lokal commit; Android merge/push/CI/deploy yoxdur. Təsdiqli settings görünüşü dəyişmir, yeni dizayn yoxdur.
- Checks performed: Java21/offline app unit/APK/lint PASS29s; host8/8PASS42s; beş yeni renderə baxıldı; üç settings PNG approved fixture ilə byte-identical; contrast3/3min8.22, JSON parity/diff checkPASS.
- Remaining work: real device/audio/haptic/focus interruption/API24/TalkBack və tam mərhələ qəbulu yoxlanmayıb. Backend public HTTPS ayrıca deploy tələb edir.
- Relevant files: docs/reviews/2026-11-15-settings-implementation.md; settings-implementation-visual/render/; DeviceSettings/GameFeedback/SettingsScreen/SettingsRoute/LegalDocuments.kt; docs/PLAN.md.
- Exact next step: lokal commit hash-ini və review nəticəsini bildir; növbəti konkret göstəriş olmadan yeni mərhələ/merge/CI/push başlama.

---
# AppBackend commit/merge/push — 15 noyabr 2026

- Completed: Wobbrik public profil/ad limiti/rekord identity və hüquqi sənədlər ayrıca ca0d9c8 və 0075791 commit-ləri ilə tarixçə saxlanaraq AppBackend main-ə merge və origin/main-ə push edildi.
- Current state: backend lokal main=origin/main=cbd0eebc64e5afc6936a5b32b9d82031afa0b660; integration worktree /private/tmp/wobbrik-backend-main təmizdir. Android settings working diff dəyişməyib. Backend owner recipe/.gitignore işi qorunub.
- Decisions: istifadəçi backend commit/merge/remote main push istədi; CI/deploy/Console yoxdur.
- Checks performed: feature race/vet/build; merged focused race/vet/build və disposable PostgreSQL18 profile/leaderboard PASS; remote hash equality PASS. Müvəqqəti DB dayandırıldı.
- Relevant files: AppBackend docs/reviews/2026-11-15-wobbrik-backend-integration.md; aşağıdakı settings review və docs/PLAN.md.
- Remaining work: Android settings uncommitted; deployment/public HTTPS və tam mərhələ qəbulu ayrıca açıqdır.
- Exact next step: nəticəni bildir; növbəti konkret istifadəçi göstərişini gözlə.

---
# Cari vahid — Parametrlər və hüquqi məzmun, 15 noyabr 2026

- Completed: təsdiqli SettingsScreen və Hesab route, DataStore səs/musiqi/haptic, CC0 audio; təsdiqli dəstək mailto `aladdinbiyabangerd@gmail.com`; real offline privacy/terms/delete-account sənədləri və seçimi. Əsas settings görünüşü saxlanır.
- Current state: Android `feat/settings-flow`, baza `a192f4d`, uncommitted. Backend `feat/wobbrik-legal-pages`, baza `b2cd617`, daimi worktree `/Users/mac/My Workspace/AppBackend-wobbrik-legal-pages`, uncommitted. Fasilədə silinmiş tmp worktree bu daimi nüsxə ilə əvəz edilib. Owner release baseline/proposal və backend profil/recipe işi qorunur.
- Decisions: görünüş yenidən təsdiqə çıxarılmır. User DailyPlate public legal mexanizmini Wobbrik üçün istədi; sağlamlıq/subscription qaydaları köçürülmür. Sənədlər en+az, Android UI hazırda az; yeni UI dil siyahısı yoxdur. Backend JSON və Android asset bayt-bayt eynidir. Deploy olmadan da offline oxunur.
- Remaining work: bu vahiddə implementasiya tamamlanır; public HTTPS URL-lərin işləməsi ayrıca backend deploy tələb edir. Real cihaz/audio/haptic/TalkBack/API24 runtime və tam mərhələ5 qəbulu yoxlanmayıb. CI/commit/merge/push/deploy yoxdur.
- Relevant files: `SettingsScreen.kt`, `SettingsRoute.kt`, `DeviceSettings.kt`, `GameFeedback.kt`, `LegalDocuments.kt`, `assets/wobbrik-legal.json`; `docs/reviews/2026-11-15-settings-implementation.md`, `docs/reviews/settings-implementation-visual/`; backend `internal/legalpages/content/wobbrik.json`, `wobbrik_test.go`, `internal/platform/legal_pages_test.go`, `docs/public-legal-pages.md`; yeganə plan `docs/PLAN.md`.
- Checks performed: normal Android app unit/APK/lint38sPASS; settings3render əvvəl approved0pixel fərq; real route+persistence və legal normal/font2 host testləri PASS, final7/7PASS44s, mailto keçidi və3sənəd seçimi daxil. Hüquqi real window renderlərinə baxıldı. Backend focus race/vet/buildPASS; web390px/1280px renderlərə baxıldı, horizontal overflow0. Kontrast mobilemin8.22/webmin4.88PASS; JSON parity/diffcheckPASS.
- Exact next step: nəticəni istifadəçiyə bildir. Əlavə mərhələ/merge/CI/deploy başlama. Commit və deploy ayrıca göstərişlə; gələcək böyük işdə bu handoff+review və `docs/PLAN.md`-dən davam et.

---
# Cari vahid — Arrowz əsaslı Parametrlər nümunəsi, 15 noyabr 2026

- Completed: iri profil düymələri və settings üçün review-only Compose nümunəsi; son istifadəçi düzəlişi ilə settings Arrowz telefon sətir quruluşuna yenidən çəkildi. Köhnə grouped settings nümunəsi cari deyil.
- Current state: `docs/compact-account-preview`, baza `a192f4d`; production dəyişmir; istifadəçi son settings-390 nümunəsini “yes” ilə açıq təsdiqlədi.
- Decisions: Wobbrik tokens/palitra saxlanır; ayrıca kart+dairəvi xətti ikon+mətn+kiçik toggle/ox. Settings aşağı iri Geri əvəzinə48dp başlıq geri. Profil preview ayrıca pending. Arrowz read-only, yeni funksiya/lokal siyahı qərarı yoxdur.
- Remaining work: təsdiqli Parametrlər dizaynının real UI/domain inteqrasiyası. Settings production ekranı/persistence hələ yoxdur; səs/haptic, dəstəklənən dillər, dəstək və məxfilik məzmunu/ünvanlarının mövcud müqaviləsini dəqiqləşdir; saxta işlək settings kimi təqdim etmə.
- Relevant files: `docs/reviews/2026-11-15-account-compact-proposal.md`, `docs/reviews/account-compact-visual/{settings-390.png,settings-360.png,settings-font2.png,render/CompactAccountProposalTest.kt}`; `GameTokens.kt`, `PublicAccountScreen.kt`; yeganə plan `docs/PLAN.md`.
- Checks performed: final settings native3/3PASS;390/360font1,320font2 renderlərinə baxıldı, font2 scroll; kontrast3/3PASSmin8.22. Sintetik fixture; production build/device/real settings deyil; emulator/CI yoxdur.
- Exact next step: yeni chatdə PLAN və bu review əsasında təsdiqli settings-390 dizaynını real axına tətbiq et. Yalnız açıq funksional sualları dəqiqləşdir; görünüşü yenidən təsdiqə çıxarma. Profilin ayrıca ölçü nümunəsi hələ təsdiqlənməyib. Commit/merge/push/CI ayrıca göstərişsiz yoxdur.

---
# Lokal commit — 15 noyabr 2026

- Completed: istifadəçi bütün cari commit edilməmiş Wobbrik dəyişikliklərinin lokal commit-ini istədi. Bazar/profil/rekord source, test, asset, plan və review qeydləri saxlanılır; release metadata/profilləri ayrıca commit olunur.
- Current state: `feat/market-leaderboard`; aşağıdakı əvvəlki uncommitted qeydləri commitdən əvvəlki tarixçədir. AppBackend ayrıca repository-dir və bu commit tapşırığının scope-una daxil edilməyib.
- Decisions: merge/push/deploy/CI yoxdur. Profilin bütöv ekran vizual fərqləri və mərhələ5 qəbulu açıq qalır.
- Checks performed: mövcud lokal review və son 255 unit/assembleDebug/lintDebug PASS sübutları yoxlandı; bu tur source dəyişməyib, testlər təkrarlanmayıb. Commit öncəsi diff whitespace yoxlaması PASS.
- Remaining work: profil vizual pariteti, mərhələ5 yekun review/qəbul; PLAN §1-də mərhələ6 reklam/alış və mərhələ7 tamamlama açıqdır. Planşet ayrıca tapşırıqdır.
- Relevant files: `docs/PLAN.md`, `docs/reviews/2026-11-15-records-fitted-implementation.md`, `docs/reviews/2026-11-15-local-commit.md`.
- Exact next step: lokal commit nəticəsini və tətbiqdə qalan işləri istifadəçiyə bildir; əlavə implementasiya/merge/CI başlama.

---
# Cari vahid — təsdiqli panelsiz rekordlar tətbiqi, 15 noyabr 2026

- Completed: istifadəçi fit-preview-board-u açıq təsdiqlədi, real LeaderboardScreen/Board və bounded traversal/data axınına tətbiq edildi. Header Back/Refresh, podium3+tam sığan real sətirlər, no footer/More/list scroll. Əvvəlki aşağıdakı pending nümunə qeydləri tarixçədir.
- Current state: feat/market-leaderboard, baza9653b98, uncommitted; inherited market/profile/data/owner/release işi qorunur. AppBackend feat/wobbrik-public-profiles-də input validation-only ad limiti15 də uyğunlaşdırılıb; onun ayrıca review-u var. Commit/merge/push/deploy/CI yoxdur.
- Decisions: hədəf4–20/fallback8; shrink reuse, grow yalnız çatışmayan1–N nəticə/eyni opaque snapshot cursor. Server rank/score/Bakı həftəsi/is_self/session authority qalır. User: rank>9999 UI-də99999+; ad max15 Unicode simvol (PUT/editor), köhnə24 ad oxunur/qısaldılır. Arrowz yalnız göstərilən say prinsipidir. Təsdiqli PNG-lər dəyişməyib, yenidən təsdiq tələb olunmur.
- Relevant files: docs/PLAN.md §1/§4; docs/reviews/2026-11-15-records-fitted-implementation.md; profile-leaderboard-visual/fitted-production/{production-board,approved-comparison,long,profile-name15}.png, native-results/parity/checks.txt; render/FittedLeaderboardProductionRenderTest.kt; LeaderboardScreen/Board/Art, CommunityArt, GameTokens, PublicAccountScreen; core LeaderboardReads/PublicProfiles və testləri. Köhnə ProfileLeaderboardRenderTest.kt.txt arxivdir.
- Checks performed: native16/16 PASS55s, final3raw təsdiqli fitted-proposal ilə changed0;15PNG back-region integrity PASS; normal/font2/long/error/empty/profile15/board görüntülərinə baxıldı; kontrast4/4PASSmin8.22. Backend ad normal/race focus+vet PASS; DB suite bu validation-only dəyişiklikdə təkrarlanmayıb. Son adi core136/engine95/app24=255 unit, APK/lint PASS15s; review/checks.txt-də saxlanır.
- Remaining work: bu rekord vahidi mərhələ5 qəbulu deyil; profilin əvvəlki bütün ekran paritet fərqləri ayrıca açıqdır. Native SDK34/sintetik server, device/realHTTP/IME/TalkBack/GPU/API24/bütün locale yoxlanmayıb. Mobil portret, planşet/landşaft ayrıca tapşırıqdır; emulator açılmadı.
- Exact next step: tamamlanmış bu vahidin nəticəsini və production renderlərini göstər. Sonrakı iş ayrıca istifadəçi göstərişi ilə; avtomatik merge/CI/push və növbəti mərhələ yoxdur.

---
# Cari vahid — ekran tutumuna görə rekord sayı və yeni nümunə, 15 noyabr 2026

- Completed: `/Users/mac/My Workspace/arrowz` HEAD8ca9c86a current UI/fit/query-size source və testləri read-only yoxlandı. Sabit top50 deyil: podium3 + sığan tam sətirlər, scroll/More yoxdur; request4–20/fallback8. Yalnız bu say prinsipi istifadəçi qərarı kimi PLAN-a qeyd edildi. Alt panelsiz yeni Wobbrik nümunəsi render edilib: Geri solda, Yenilə sağda, podium/adlar, davam sətirləri, passiv dots.
- Current state: `feat/market-leaderboard`, inherited working diff qorunur. **Yeni dizayn təsdiq gözləyir; production UI/data/API bu tur dəyişməyib.** Köhnə Daha çox dock/flow artıq tətbiq üçün cari nümunə deyil. Arrowz layihəsi dəyişdirilməyib; commit/merge/push/CI yoxdur.
- Decisions: yalnız göstərilən say/qabiliyyət prinsipi köçürülür; Arrowz Firestore/cache/country/tie/reward/UTC/pinned-own-row qaydaları yox. Wobbrik real data/AppBackend/Bakı həftəsi/is_self qalır. UI-yə sığmayan oyunçu minimum4 adı ilə məcbur edilmir; font2-də əvvəlki podiumsuz adaptasiya qalır.
- Relevant files: `docs/PLAN.md` §4 Rekord; `docs/reviews/2026-11-15-records-fit-count-proposal.md`; `profile-leaderboard-visual/fitted-proposal/{fit-preview-board.png,*.png,*count.txt}`; `render/FittedRecordsProposalTest.kt`, iki ortaq test helper-i, `tools/DockProposalSheet.java`; real implementation üçün `LeaderboardScreen.kt`, `LeaderboardReads.kt`.
- Checks performed: son native3/3PASS21s; bütün yerləşdirilən sətirlər tam board daxilində, unikal say, no More, header48×48dp və visibility. 390font1=7 (3+4), 360font1=5 (3+2), 320font2=2; rəqəmlər sabit deyil, faktiki ölçüdəndir. Son raw və board-a baxıldı; kontrast4/4min8.22; diffcheckPASS. Sintetik UI fixture-dir, HTTP/device/emulator yoxdur.
- Remaining work: yeni görünüş təsdiqi; sonra real screen-fit count/request budget və UI-ni mövcud session/scope/revision/cursor sərhədləri ilə birləşdirmək, real empty/error/few-player/rank-gap/uzun ad və ölçü dəyişməsini yoxlamaq. Yeni server authority/phantom me-rank yaratma.
- Exact next step: fit-preview-board-u istifadəçiyə göstər və görünüş cavabını gözlə. Təsdiqdən sonra yalnız bu yeni panelsiz dizaynı real UI/data axınına tətbiq et. Təsdiqli PNG-ləri təkrar təsdiqə çıxarma; köhnə Daha çox nümunəsini implement etmə. Ayrıca göstərişsiz merge/push/CI yoxdur.

---
# Cari vahid — rekordlar üçün yığcam alt panel nümunəsi, 15 noyabr 2026

- Completed: istifadəçinin Daha çox/Yenilə/Hesaba qayıt düzülüşü iradı əsasında tək yığcam alt panel nümunəsi render edilib: solda Hesab, ortada Daha çox, sağda Yenilə. Mövcud tokens/source art saxlanır; font2-də yanlar icon-dur. Daha çox üçün loading → əlavə 4–8-ci yerlər → hamısı yükləndi state board-u da göstərilir.
- Current state: `feat/market-leaderboard`, inherited working diff qorunur. **Yeni nümunə istifadəçi təsdiqi gözləyir; production UI/data bu tur dəyişməyib.** Preview sintetikdir; Daha çox state-lərini host idarə edir, server callback-i deyil. Commit/merge/push/CI yoxdur.
- Decisions: istifadəçi əvvəl nümunə, yalnız konkret təsdiqdən sonra implementasiya istəyib. Üç böyük alt-alta düymə bu təklifdə tək panelə yığılır; source PNG-lərin ümumi təkrar təsdiqi tələb olunmur.
- Relevant files: `docs/reviews/2026-11-15-records-compact-footer-proposal.md`; `profile-leaderboard-visual/compact-proposal/{preview-board,more-flow-board}.png` və rawPNG-lər; `render/CompactRecordsProposalTest.kt`, `tools/DockProposalSheet.java`; production `LeaderboardScreen.kt`/CommunityArt/GameTokens; yeganə plan `docs/PLAN.md`.
- Checks performed: son native3/3PASS20s, 390×844font1/360×640font1/320×568font2; 48×48dp targets+visibility assert; raw-lara və son board-a baxıldı. Əlavə click-flow native3/3PASS21s: loading disabled, köhnə/yeni sətrlərin saxlanması, end disabled; 390font1 flow board-a baxıldı. Kontrast3/3PASSmin6.48; diffcheckPASS. Emulator yoxdur; real callback/server/runtime yoxlaması deyil.
- Remaining work: konkret footer dizaynının təsdiqi; sonra real loading/retry/pagination-end/disabled state-ləri və mövcud session/cursor/onBack callback-ləri ilə tətbiq. Əvvəlki bütöv ekran pariteti məhdudiyyətləri ayrıca qalır.
- Exact next step: preview-board və more-flow-board-u istifadəçiyə göstər və cavabını gözlə; konkret təsdiq gəlmədən production-a qoşma. Təsdiq gəlsə bu paneli mövcud real axına tətbiq et, əlavə alternativ dizayn uydurma.

---
# Cari vahid — profil/rekord source art və capture, 15 noyabr 2026

- Completed: təsdiqli source-dan podium fon clip-i, dar daxili avatar/xal overlay-ləri, orijinal rank/təqvim/mərtəbə/geri art-ı; hesabın orijinal hero-su, real XP pill-i, profil link chrome-u. Board ekran üzrə bir dəfə decode edilir. Header/footer üçün ayrıca snapshot sinfi+təzə JVM və piksel-region QA həlli hazırdır. PNG yenidən təsdiqə çıxarılmadı.
- Current state: `feat/market-leaderboard`, baza `9653b98`, uncommitted; inherited bazar/data/owner/release işi qorunur. Review-dakı source art/capture məsələləri düzəldilib; bütöv ekranların tam vizual/piksel pariteti və mərhələ5 qəbulu açıqdır. Commit/merge/push/deploy/CI yoxdur.
- Decisions: real GET/PUT, explicit konflikt/eyni replacement retry, epoch/Ready fencing, server scope/week/rank/score/cursor/is_self və sole journal saxlanır. Saxta settings/statistika/me rank əlavə edilmir. AppBackend müqaviləsi read-only yoxlandı, bu vizual vahid üçün backend dəyişikliyi tələb olunmadı. Mobil portret; emulator açılmır.
- Relevant files: `docs/reviews/2026-11-15-phase5-profile-leaderboard-parity.md`; `profile-leaderboard-visual/{README.md,final-preview.png,final-font2.png,approved-comparison.png,final/,native-results.txt,render/,tools/}`; `CommunityArt.kt`, `LeaderboardArt.kt`, `LeaderboardScreen.kt`, `PublicAccountScreen.kt`, `GameTokens.kt`, `HomeScreen.kt`; dəyişdirilməmiş `account_approved_board.png` və `leaderboard_approved_board.png`; yeganə plan `docs/PLAN.md` §§1–2,4.
- Checks performed: son native28/28PASS1m40s, 28PNG/4window-font-data×7state; geri düyməsi və rekord header/footer bölgələrinin piksel eyniliyi PASS. Son adi core130/engine95/app24 + assembleDebug/lintDebugPASS30s; kontrast4/4min8.22, hər iki approved source/copySHA256 eyni, diffcheckPASS. Final board-lara və seçmə raw/mobile/font2/error/empty/self/long-name renderlərə baxıldı. Sintetik production host, real HTTP/device deyil.
- Remaining work: `approved-comparison.png`-də bütöv ekran chrome/typo/ölçü/boşluq/kompozisiya fərqləri qalır; bunlar ümumi təsdiqli istisna deyil və source art düzəlişi tam ekran qəbulunu əvəz etmir. Bütün locale/state/IME/inset/TalkBack/GPU/API24 yoxlanmayıb; planşet ayrıca qalır. Mərhələ5 qəbulu deyil.
- Exact next step: yeni chatdə əvvəl bu review+approved-comparison+PLAN əsasında qalan bütöv ekran vizual fərqlərini scoped həll et; real data, təsdiqli source və son mobil ölçü qərarlarını qoru. Təsdiqli PNG-ləri yenidən təsdiqə çıxarma. Final capture üçün `ProfileLeaderboard*RenderTest` və `CaptureIntegrityCheck.java` istifadə et; köhnə root/ara PNG-ləri final sayma. Ayrıca göstərişsiz merge/push/CI yoxdur.

---
# Cari vahid — mərhələ 5: profil/rekord real UI, 14 noyabr 2026

- Completed: real PublicAccountScreen GET/PUT editoru, ad/avatar seçimi, explicit revision conflict və exact replacement retry; LeaderboardScreen həftəlik/ümumi server siyahısı, refresh/pagination/loading/error/empty/is_self; Application/OnlineGame hesab→rekordlar naviqasiyası qoşuldu. Təsdiqli PNG-lər üçün yenidən təsdiq istənmədi.
- Current state: `feat/market-leaderboard`, baza `9653b98`, inherited bazar/data/owner/release working diff qorunur; bu davam da uncommitted-dir. Funksional UI inteqrasiyası var, tam vizual paritet hazır deyil. Commit/merge/push/deploy/CI yoxdur.
- Decisions: profil iqtisadi journal-dan ayrıdır, Google identity deyil; konflikt avtomatik overwrite etmir. Exact Ready/session epoch fencing saxlanır. Yeni server müqaviləsi tələb olunmadı; AppBackend docs/wobbrik-game-api.md public profile/leaderboard read-only yoxlandı. Mobil portret; emulator açılmır. İlkin generativ avatar/podium uyğun gəlmədiyindən production-dan çıxarıldı. `leaderboard_approved_board.png` orijinal play-progress-v2 ilə SHA256-identikdir; source hissələri Canvas-da çəkilir, score/avatar real data ilə üstündən dəyişir.
- Relevant files: `PublicAccountScreen.kt`, `LeaderboardScreen.kt`, `GameTokens.kt`, `OnlineGame.kt`, `WobbrikApplication.kt`; `res/drawable-nodpi/leaderboard_approved_board.png`; `docs/reviews/2026-11-14-phase5-profile-leaderboard-ui.md`, `docs/reviews/profile-leaderboard-visual/{render/,*.png}`; data review və yeganə `docs/PLAN.md` §§1–2,4.
- Checks performed: final native production host3/3 PASS19s; 360×640font1,390×844font1,320×568font2; conflict/no-auto-save, unknown PUT/exact retry, weekly/all fərqli avatar/score, paging/self/error/empty/back. 21PNG-yə seçmə baxıldı; final normal core130/engine95/app24+assembleDebug+lintDebugPASS28s. Contrast4/4PASSmin8.22, approved raster SHA256 identical, diffcheckPASS. Bəzi native transition captures-da dəyişməyən başlıq/footer çəkilişi natamamdır; PixelCopy timeout PASS deyil. Emulator/device/real HTTP yoxdur.
- Remaining work: source podiumun fon düzbucaqlısı, 60px avatarların böyüdülmə keyfiyyəti və köhnə portraiti örtən geniş overlay-lərin laurels/frame ölçülərinə təsiri; tam PNG parity hazır deyil və bu fərqlər istifadəçi təsdiqli istisna deyil. Bütün locale/state/uzun ad/rank/balance, real HTTP/device/IME/inset/TalkBack/GPU/API24 yoxlanmayıb. Mərhələ5 qəbulu deyil.
- Exact next step: yeni chatdə bu UI review+PLAN və təsdiqli play-progress-v2/account-states əsasında yalnız profil/rekord vizual paritet vahidini tamamla; real UI/data axınını qoruyub original art source/overlay sərhədlərini və etibarlı host capture-ı həll et. Mövcud təsdiqli PNG-ni yenidən təsdiqə çıxarma, yeni alternativi səssiz tətbiq etmə. Main merge/push/CI ayrıca göstərişsiz yoxdur.

---
# Cari vahid — mərhələ 5: rekordlar/profil data inteqrasiyası, 14 noyabr 2026

- Completed: PLAN+aktiv bazar handoff+AppBackend profil/rekord müqavilələri oxundu. PublicProfiles/ProfileAccess GET/PUT, Unicode ad/avatar allowlist, ProfileEditor exact retry+conflict/refetch, LeaderboardRepository və login epoch-a bağlı immutable səhifələmə əlavə edildi. Təsdiqli PNG yenidən təsdiqə çıxarılmadı.
- Current state: `feat/market-leaderboard`, inherited bazar/owner working diff qorunur. Bu davam da uncommitted-dir. UI hələ qoşulmayıb. Commit/merge/push/deploy/CI yoxdur.
- Decisions: profil revision-u iqtisadi journal/revision-dan ayrıdır; konflikt avtomatik overwrite etmir, yeni save ayrıca istifadəçi əməliyyatıdır. Rekordlar server həftəsini/rank/snapshot/cursor-u saxlayır; öz nəticəsini göstərmək üçün uydurma me endpoint yoxdur. Backend müqaviləsi yoxlandı, əlavə server dəyişikliyi lazım deyil.
- Relevant files: docs/PLAN.md §§1–2,4; docs/reviews/2026-11-14-phase5-profile-leaderboard-data.md; core game/{PublicProfiles,ProfileEditor,LeaderboardReads,GameplayHttpBackend}.kt; PublicProfilesLeaderboardTest.kt; əvvəlki bazar review-u və aşağıdakı bazar handoff-u. Təsdiqli docs/design/mobile-v1/{play-progress-v2,inventory-market-v2}.png.
- Checks performed: Java21/offline `:core:test :app:compileDebugKotlin` PASS5s; core130/130, yeni7/7; lokal scoped review və diffcheck PASS. UI dəyişməyib/render yoxdur; emulator/CI açılmayıb. Backend read-only yoxlandı.
- Remaining work: təsdiqli art-dan builder/pilot/architect və dəyişən nəticələr üçün təmiz podium asset-ləri; real rekordlar all/weekly/error/empty/paging ekranı; real profil editoru və naviqasiya. UI save Conflict-i göstərib yenidən Save istifadəçi əməliyyatını tələb etməlidir. Real hesab/device/HTTP/render/large font yoxlanmayıb; mərhələ5qəbulu deyil.
- Exact next step: yeni chatdə bu data vahidi+PLAN+bazar review-u əsasında təsdiqli PNG-lərin art asset-lərini hazırla və real rekordlar/profil UI-ni qoş; PNG təsdiqini təkrar istəmə. Mövcud GameTokens/mobile sizing/purchase chrome təkrar istifadə olunsun; placeholder/yeni alternativ dizayn yox. Hər vizual nəticəni production host render edib PNG ilə müqayisə et. Ayrıca göstərişsiz merge/push/CI yoxdur.

---
# Cari vahid — mərhələ 5: Android bazar, 14 noyabr 2026

- Completed: handoff/PLAN və AppBackend profil review-u oxundu. Android MarketReads/Repository, list/withdraw/buy wire+receipt və sole durable journal retry inteqrasiyası, bazar alış/elanlarım/filter/detail/pagination, inventardan satış seçimi və birbaşa detail→sale girişi hazırlanıb. Təsdiqli inventory-market-v2 üslubu, mövcud tokens/production BuildingPreview istifadə olunur; dizayn təsdiqi yenidən istənilməyib.
- Current state: Wobbrik `feat/market-leaderboard`, baza `9653b98`, bu vahid uncommitted working diff-dir. AppBackend `feat/wobbrik-public-profiles` əvvəlki review üzrə hazırdır, main/deploy deyil. Owner PLAN/handoff/release/untracked purple asset qorunur. Commit/merge/push/deploy/CI yoxdur.
- Decisions: mobil portret əvvəl; planşet ayrıca, emulator açılmır. Vahid wallet/sole journal, exact retry, hesab epoch/Ready fencing və fresh server sync saxlanır. Backend bazar müqaviləsi yoxlandı, əlavə server dəyişikliyi lazım deyil. İctimai ad/avatar Google identity deyil; profil revision-u iqtisadi revision-dan ayrıdır. Placement bağlıdır. Son istifadəçi satış forması screenshot-unu dəyişməyi istədi: böyük boş sahə çıxarılır, mövcud kompozisiya/token üslubunda bounded yığcam sale dialog tətbiq olunur; böyük şrift scroll body/fixed footer saxlanır. Son yığcamlaşdırma render/build yoxlamalarından keçib; native dialog daxilində parent fontScale də qorunur.
- Remaining work: leaderboard all/weekly pagination+epoch, profile GET/PUT editor/revision conflict və PNG-yə uyğun ayrıca builder/pilot/architect avatar/podium asset-ləri hələ yoxdur; placeholder qoyulmayıb. Bazar real iki hesab/device E2E və bütün empty/error/closed/filter/paging kombinasiyaları yoxlanmayıb. Tam PNG pixel parity və mərhələ5 qəbulu deyil.
- Relevant files: docs/PLAN.md §§1–2,4; docs/reviews/2026-11-14-phase5-market-android.md; docs/reviews/market-visual/{render/,checks.txt,*.png}; MarketScreen.kt, GameTokens.kt, OnlineGame.kt, InventoryScreen.kt, WobbrikApplication.kt; core game/{MarketReads,MarketMutations,GameplayWire,GameplayHttpBackend,AttemptCoordinator,RecoveryDtos}.kt; MarketIntegrationTest.kt; AppBackend docs/wobbrik-game-api.md və docs/reviews/2026-11-14-wobbrik-public-profiles.md; təsdiqli docs/design/mobile-v1/{inventory-market-v2,play-progress-v2}.png.
- Checks performed: final Java21/offline core123 (bazar6)/engine95/app24 unit+assembleDebug+lintDebug PASS26s; final native3/3 PASS26s, 12 production PNG360×640font1/390×844font1/320×568font2, market/detail/sale/inventory-entry; son yığcam sale hər üç ölçüdə render edilib baxıldı, font2 dialog real böyük şriftlə scroll olunur. Kontrast4/4PASSmin8.22 və diffcheckPASS. Əvvəlki iki ilişmiş host run PASS deyil; son bounded host3/3PASS-dır. Backend əvvəlki disposablePG18 full race PASS profil review-dadır; bu tur backend dəyişməyib. Canlı runtime/device/IME/TalkBack/GPU/planşet yoxlanmayıb.
- Exact next step: əvvəlcə son yığcam satış dialogunun render/build yoxlamasını və nəticə şəklini yekunlaşdır. Sonra yeni chatdə bu handoff+PLAN mərhələ5+market review+AppBackend profil müqaviləsi əsasında rekordlar/profil vahidini davam et. Təsdiqli PNG-ləri yenidən təsdiq sualına çevirmə; avatar/podium-u ayrıca həmin təsdiqli art-dan hazırla, saxta UI/placeholder yaratma. Bazar working diff-ni qoruyub review et; ayrıca göstərişsiz merge/push/CI yoxdur.

---
# Cari davam — purple template: bütün hissə ID-lərinin uyğunluğu, 14 noyabr 2026

- Completed: son annex/dam birləşməsi istifadəçinin cari tapşırığı ilə təsdiqli əsas kimi qorundu. Bütün68ID (wall13/window14/entrance14/roof14/lighting13),32qarışıq və5tam-üslub nəzarəti production BuildingPreview-də render edildi. Alternativ divarların purple kənar zolaqları, əsas damların saçaq proyeksiyası, annex-də bütöv dam hüceyrəsinin sıxılması və girişdə təkrar pilləkən/sağ-yan proyeksiyası düzəldildi.
- Current state: `fix/purple-seams-ground-contact`, implementasiya commit`d63029f` (baza`2be0583`); inherited owner dəyişiklikləri qorunur. Bu davamın production scope-u `PurpleBuildingArt.kt`, `PurpleGroundFloorArt.kt`, yeni `PurpleSelectedRoofArt.kt` və `PurpleSelectedEntranceArt.kt`. İstifadəçi commit/merge/push istəyib; lokal review edilmiş vahid commit edilib, tarixçə saxlayan main merge+origin push bu göstərişin davamıdır. CI işlədilmir.
- Decisions: qarışıqlar sonbase1hissəsi dəyişəndə də təsdiqli template-də qalır; yalnız tam uyğunbase2–6dəstləri əvvəlki renderer-dədir. Eyni mövcud art-dan səth/frontal nümunə alınır; yeni dizayn/asset/palette/ID yoxdur. Base1gövdə/joint/annex/flashing/torpaq pixel-parityPASS. Domain/API/Backend işi tələb olunmur.
- Relevant files: `docs/reviews/2026-11-14-purple-catalog-compatibility.md`; `docs/reviews/purple-catalog-visual/{after/manifest.txt,after/catalog-*.png,after/ground-*.png,render/,native-results.txt,parity.txt}`; host `docs/reviews/mobile-sizing-visual/render/PurpleCatalogRenderTest.kt`; yeganə plan `docs/PLAN.md`§7; əvvəlki annex-joinreview istinaddır.
- Checks performed: son native6/6PASS43s;105specimen/27sheet +30inventory/detail +2boundary. 360×640font1/390×844font1/320×568font2inventory;1/4/40floorstress. Final category/mixed/no-base1/ground/context/font2görüntülərinə baxıldı. Normal app24/24unit+assembleDebug+lintDebugPASS17s;pixel3/3PASS;contrast4/4PASSmin10.48;diffcheckPASS.
- Remaining work: canlı hesab/mutationE2E/device/GPU/inset/TalkBack/API24/all-locales/planşet/landşaft yoxlanmayıb; emulator açılmayıb. Bütün463736kombinasiya və hərID×ölçü/font/ofset yoxlanmayıb;32mixnümunədir. Əvvəlki font2detailtitle/inner/tabscroll qalır. Tam mərhələ qəbulu deyil.
- Exact next step: bu vahidin nəticəsini və review/renderləri istifadəçiyə bildir; yalnız konkret iradla eyni təsdiqli template-ni düzəlt. Merge/push nəticəsini Git tarixçəsi və origin/main ilə tutuşdurub bildir; sonra yalnız konkret irad və ya yeni istifadəçi tapşırığı ilə davam et. CI ayrıca istənməyib və işlədilmir; əlavə feature açma.

---
# Cari davam — işarələnmiş dam/divar arxa birləşməsi, 14 noyabr 2026

- Completed: istifadəçinin işarələdiyi kiçik damın arxa kənarı əsas divarın114/−65perspektivinə bağlandı. Atlasda arxa künc139/12şəffaf sahədə idi; real dam küncü208/12ilə düzəldildi. Mövcud krem trim-dən dar8art-unit birləşmə örtüyü əlavə edildi.
- Current state: `fix/purple-seams-ground-contact`, baza `2be0583`; bu davamda yalnız `PurpleGroundFloorArt.kt` production dəyişib. Inherited diff qorunur. Commit/merge/push/CI yoxdur.
- Decisions: ön saçaq/pəncərə/divar/plintus və torpaq/shadow/path/steps/foreground anchor-ları, hissə ID/mixed/corner seçimi, yuxarı mərtəbə/count/real metadata saxlanır. Yeni asset/dizayn/Backend/API yoxdur; AppBackend işi lazım deyil.
- Relevant files: `PurpleGroundFloorArt.kt`; `docs/reviews/2026-11-14-purple-annex-join.md` son dam bölməsi; `docs/reviews/purple-roof-join-visual/`; təsdiqli ground-floor-v1/art-study; yeganə plan `docs/PLAN.md` §7.
- Checks performed: native5/5PASS34s,30production+2boundary; adi app24unit+assembleDebug+lintDebugPASS17s; upper1400px/base_2pixelPASS,diffcheckPASS. Son close/context/category/mixed/font2boundary görüntülərinə baxıldı; sintetik production Compose host. Canlı device/GPU/inset/TalkBack yoxlanmayıb; emulator/CI yoxdur.
- Remaining work: son dam/divar renderinin istifadəçi vizual qəbulu gözlənir; tam pixel pariteti/68ID/mərhələ qəbulu deyil. Əvvəlki font2 title/tab/inner-scroll qalır.
- Exact next step: son yaxın plan müqayisəsini göstər; yalnız konkret irad və ya ayrıca lokal commit göstərişi ilə davam et. Böyük davam yeni chatdə bu handoff+PLAN§7+annex-join review əsasında; ayrıca merge/push/CI yoxdur.

---
# Cari davam — sağ yan həcmin material/birləşmə uyğunluğu, 14 noyabr 2026

- Completed: istifadəçi yan həcmin yamaq təsirini azaltmağı istədi. Qayıdan divar əsas küncə qədər çəkildi, təkrar açıq tikiş örtüldü, daş kəmər/core plintus endpoint-ləri birləşdirildi. Tam fasad teksturasını dar səthə sıxmaq əvəzinə uyğun UV hissələri istifadə olunur; kərpic/daş miqyası əsas mərtəbəyə yaxınlaşdırıldı.
- Current state: `fix/purple-seams-ground-contact`, baza `2be0583`; bu davamda yalnız `PurpleGroundFloorArt.kt` production dəyişib. Inherited diff qorunur. Commit/merge/push/CI yoxdur.
- Decisions: təsdiqli nümunə saxlanır; pəncərə/dam anchor-ları, torpaq/shadow/path/steps/foreground, hissə ID/mixed/corner seçimi, yuxarı mərtəbələr və real metadata dəyişmir. Backend/API dəyişmir, AppBackend işi lazım deyil.
- Relevant files: `PurpleGroundFloorArt.kt`; `docs/reviews/2026-11-14-purple-annex-join.md` son bölmə; `docs/reviews/purple-annex-blend-visual/`; təsdiqli ground-floor-v1/art-study; yeganə plan `docs/PLAN.md` §7.
- Checks performed: native5/5PASS40s,30production+2boundary; adi app24unit+assembleDebug+lintDebugPASS13s; upper1400px/base_2pixelPASS,diffcheckPASS. Close/context/category/mixed/font2 renderlərə baxıldı. Sintetik host; canlı device/GPU/inset/TalkBack yoxlanmayıb; emulator/CI yoxdur.
- Remaining work: son renderin istifadəçi vizual qəbulu gözlənir; tam pixel pariteti/68ID/mərhələ qəbulu və əvvəlki font2 title/tab/inner-scroll məhdudiyyətləri qalır.
- Exact next step: son müqayisəni göstər, yalnız konkret istifadəçi iradı və ya ayrıca lokal commit göstərişi ilə davam et. Böyük davam yeni chatdə bu handoff+PLAN§7+annex-join review əsasında; ayrıca merge/push/CI yoxdur.

---
# Cari vahid — pəncərə deyil, sağ yan həcmin gövdəyə birləşməsi, 14 noyabr 2026

- Completed: istifadəçi dəqiqləşdirdi və şəkildə işarələdi: pəncərə yox, onu daşıyan sağ yan həcm mərtəbəyə yapışmır. Bu turdakı pəncərə aperture/kölgə təcrübəsi geri götürüldü. Əsas gövdəyə qayıdan çatışmayan yan divar, daş plintus və kəmər səthi əlavə edildi; damaltı həcm bağlandı.
- Current state: `fix/purple-seams-ground-contact`, baza `2be0583`; bu davamın production dəyişməsi yalnız `PurpleGroundFloorArt.kt`-dədir. Inherited source/assets/owner/release/harness diff qorunur. Commit/merge/push/CI yoxdur.
- Decisions: son konkret irad sağ əlavə həcmin birləşməsidir; pəncərəni yenidən ölçüləndirmə/sürüşdürmə. Torpaq/path/steps/foreground/shadow və əvvəlki seçim/ID/mixed/corner, yuxarı mərtəbə/count/real metadata saxlanır. Backend/API dəyişmir; AppBackend işi tələb olunmur.
- Relevant files: `PurpleGroundFloorArt.kt`; `docs/reviews/2026-11-14-purple-annex-join.md`; `docs/reviews/purple-annex-join-visual/`; təsdiqli `docs/design/building-purple-v1/ground-floor-v1/art-study.png`; yeganə plan `docs/PLAN.md` §7.
- Checks performed: son native5/5PASS35s; adi app24unit+assembleDebug+lintDebugPASS13s; diffcheckPASS; upper1400px/base_2pixelPASS; final müqayisə/yaxın plan/category/mixed/font2boundary görüntülərinə baxıldı; sintetik production Compose host, canlı device/server/GPU/inset/TalkBack deyil. Emulator/CI yoxdur.
- Remaining work: istifadəçinin son bağlanmış yan həcm renderinə vizual qəbulu gözlənir; tam pixel pariteti/68ID/mərhələ qəbulu yoxdur. Əvvəlki font2 inner/tab scroll/title clipping qalır.
- Exact next step: son annex-join müqayisəsini göstər; yalnız konkret irad və ya ayrıca lokal commit göstərişi ilə davam et. Böyük davam üçün bu handoff+PLAN§7+annex-join review yeni chatdə oxunur. Ayrı göstərişsiz merge/push/CI yoxdur.

---
# Cari vahid — sağ pəncərə və torpaq təması, 14 noyabr 2026

- Completed: istifadəçi əvvəlki pəncərəni yenə rədd etdi; əvvəlki PASS vizual qəbul deyil. Sağ yan həcmin sərt bucağı95/−33art müstəvisinə dəyişdi; pəncərə/dam/sütun/plintus birlikdə proyeksiya olunur. Xarici ayaq torpaq silueti daxilindədir. Hər iki yan həcmə mövcud materialla təmas kölgəsi əlavə edildi.
- Current state: `fix/purple-seams-ground-contact`, baza `2be0583`; bu davamın production diff-i `PurpleGroundFloorArt.kt` və `PurpleBuildingArt.kt` daxilindədir. Inherited source/assets/owner/release/harness diff qorunur. Commit/merge/push/CI yoxdur.
- Decisions: təsdiqli nümunə əsasdır; hissə ID/mixed/corner seçimi və yuxarı mərtəbələr saxlanır. Garden/path/steps/foreground asset və anchor-ları, core footing dəyişmir. Yan həcmin yerə izi artıq son istifadəçi grounding tapşırığına görə düzəldilib. Backend/API/authority dəyişmir; AppBackend işi lazım deyil.
- Relevant files: yuxarıdakı2source; `docs/reviews/2026-11-14-purple-window-contact.md`; `docs/reviews/purple-window-contact-visual/`; təsdiqli `docs/design/building-purple-v1/ground-floor-v1/art-study.png`; yeganə plan `docs/PLAN.md` §7.
- Checks performed: son native5/5PASS39s,30production capture+2boundarysheet; comparison/category/mixed/font2boundary görüntülərinə baxıldı. Upper1400px və tam base_2 piksel müqayisəsi PASS; adi app24unit+assembleDebug+lintDebugPASS13s. DiffcheckPASS. Sintetik host; canlı device/server/GPU/inset/TalkBack yoxlanmayıb; emulator/CI yoxdur.
- Remaining work: istifadəçinin son render vizual qəbulu gözlənir; tam pixel pariteti/68ID/mərhələ qəbulu yoxdur. Əvvəlki font2 inner/tab scroll/title clipping qalır.
- Exact next step: yeni chatdə bu handoff+PLAN§7+son window-contact review əsasında yalnız istifadəçinin konkret iradını və ya ayrıca lokal commit göstərişini icra et. Əlavə böyük işə burada başlama; ayrıca göstərişsiz merge/push/CI yoxdur.

---
# Cari vahid — ilk mərtəbənin yan həcmləri/pəncərə düzəlişi, 14 noyabr 2026

- Completed: istifadəçinin təsdiqli nümunəyə uyğun düzəliş tapşırığı tətbiq edildi: sol alçaq həcm genişləndi, sağ daş plintus qalınlaşdı, yan daş səthi atlasın yan materialından götürülür. Sağ pəncərə/dam/divar ortaq fasad müstəvisinə bağlandı; saçaqla kəsişmə və pəncərənin üfüqi dartılması düzəldildi. Üst örtüyün şəffaf material kənarı son iterasiyada çıxarıldı.
- Current state: `fix/purple-seams-ground-contact`, baza `2be0583`; bu davamda production üzrə yalnız `PurpleGroundFloorArt.kt` dəyişdi. Inherited owner/source/assets/harness/release diff qorunur. Commit/merge/push/CI yoxdur.
- Decisions: təsdiqli ground-floor-v1 nümunəsi əsasdır; torpaq/kölgə/path/steps/foreground asset və anchor-ları, hissə ID/mixed/corner seçimləri, yuxarı mərtəbələr və real metadata saxlanır. Yuxarı renderer git-blob əvvəlki review ilə eynidir. Backend/API dəyişmir; AppBackend işi tələb olunmur.
- Relevant files: `PurpleGroundFloorArt.kt`; `docs/reviews/2026-11-14-purple-ground-floor-refinement.md`; `docs/reviews/purple-ground-floor-refinement-visual/`; təsdiqli `docs/design/building-purple-v1/ground-floor-v1/art-study.png`; yeganə plan `docs/PLAN.md` §7.
- Checks performed: son native5/5PASS36s; adi app24unit+assembleDebug+lintDebugPASS13s; kontrast4/4min8.22,diffcheckPASS. Son comparison/contact/font2/boundary renderlərə baxıldı; upper1400px və tam base_2 pixel müqayisəsi PASS. 360/390font1 və320font2 production detail/inventory, seçim variantları və1/4/40floor renderer stress halları istifadə olunur. Emulator/CI yoxdur; sintetik Compose host canlı runtime/GPU/TalkBack/inset qəbulu deyil.
- Remaining work: tam pixel pariteti və68ID qəbulu yoxdur; əvvəlki font2 title/tab/inner scroll məhdudiyyətləri qalır. Bu vahid mərhələ/oyun qəbulu deyil.
- Exact next step: son render müqayisəsini istifadəçiyə göstər; sonra yalnız konkret istifadəçi düzəlişi və ya ayrıca lokal commit göstərişi ilə davam et. Ayrı göstərişsiz merge/push/CI yoxdur.

---
# Cari vahid — təsdiqli ilk mərtəbə tətbiq edildi, 14 noyabr 2026

- Completed: istifadəçi ground-floor-v1/art-study.png-ni “okeydir” ilə təsdiqlədi. Şəffaf modular derivative və ilk mərtəbə helper-i tətbiq edildi: geniş daş plintus, tağlı giriş, alçaq yan həcmlər, kiçik yan dam, krem sütun/cap. İlk açıq sol bay renderdə görüldü və daş üst örtüklə düzəldildi.
- Current state: `fix/purple-seams-ground-contact`, baza `2be0583`; source/atlas/asset/review/handoff working diff, commit edilməyib. Inherited owner/release/harness diff qorunur. Commit/merge/push/CI yoxdur.
- Decisions: garden/kölgə/path/steps/foreground asset və anchor-ları saxlanır. ID/mixed xəritə, alternativ seçilən hissələr, upper-storey geometry/junction/roof/balcony,6nümayəndə mərtəbə və real metadata dəyişmir. Ground base_1 window mövcud square art-dır, yuxarılar arch qalır; yeni ID deyil. Backend/API/authority/oyun dəyişmir; AppBackend işi lazım deyil.
- Relevant files: `PurpleBuildingArt.kt`, `PurpleGroundFloorArt.kt`, `assets/building-purple/{atlas.json,ground-floor-v1.png}`, `docs/design/building-purple-v1/ground-floor-v1/{art-study.png,atlas-v1.png,README.md}`; `docs/reviews/2026-11-14-purple-ground-floor-integration.md`, `docs/reviews/purple-ground-floor-visual/`; yeganə plan `docs/PLAN.md` §7.
- Checks performed: son native5/5PASS34s,30productioncapture+2boundarysheet; final contact/boundary/font2/reference müqayisəsinə baxıldı. Source/asset son editdən sonra adi app24unit+assembleDebug+lintDebugPASS22s. Kontrast4/4PASSmin8.22; JSON/cellbounds/diffcheckPASS. Sintetik host, canlı device/server/GPU/inset/TalkBack/planşet/landşaft/alllocale yoxlanmayıb; emulator/CI yoxdur.
- Remaining work: tam pixel pariteti deyil; regular6storey/balcony və modular bay/stone/işıq fərqləri qalır,68IDqəbulu yoxdur. Font2inner/tabscroll+ilk titleclip əvvəlki kimidir. Hazırki vahidin renderləri istifadəçi vizual qiymətləndirməsi üçün təqdim edilir; tam mərhələ/oyun qəbulu deyil.
- Exact next step: son render müqayisəsini göstər. Sonra yalnız istifadəçinin konkret düzəlişini və ya ayrıca lokal commit tapşırığını icra et; əlavə böyük iş üçün yeni chatdə bu handoff+PLAN§7+son integration review-u oxu. Ayrı göstərişsiz commit/merge/push/CI yoxdur.

---
# Cari vahid — ilk mərtəbə modular nümunəsi, 14 noyabr 2026

- Completed: current handoff, PLAN §7, son seams/ground review və canonical join-reference oxundu; ui-craft/imagegen istifadə edildi. `docs/design/building-purple-v1/ground-floor-v1/art-study.png` hazırlanıb və baxılıb: geniş daş plintus, tağlı giriş, alçaq yan həcmlər və yan dam.
- Current state: yalnız dizayn nümunəsi; tətbiq kodu/atlas dəyişməyib. Konkret nümunə üçün istifadəçi təsdiqi gözlənilir. Branch `fix/purple-seams-ground-contact`, baza `2be0583`; inherited dirty iş qorunur. Commit/merge/push/CI yoxdur.
- Decisions: mövcud torpaq anchor/kölgə/bitki/path oturuşu və selected-part ID/mixed xəritəsi saxlanır. Şəkildəki garden yalnız kontekstdir, production garden əvəzi deyil. Bütöv bina bitmap-i tətbiq edilmir; backend/API dəyişməsi yoxdur.
- Remaining work: təsdiqdən sonra ayrıca şəffaf modullar/ölçülmüş anchor-lar hazırlamaq və real renderer inteqrasiyası. Board modullardan deterministik yığılmış render deyil; sağ üst shell-də yan dam artıq görünür, production atlasda dam ayrıca ayrılmalıdır. Torpaq və seçim uyğunluğu hələ runtime/render ilə sübut edilməyib.
- Relevant files: `docs/design/building-purple-v1/ground-floor-v1/{art-study.png,README.md}`, `docs/reviews/2026-11-14-purple-ground-floor-study.md`; əvvəlki scoped fayllar aşağıda.
- Checks performed: generated board vizual baxış; source/atlas toxunulmayıb, build/test/emulator/CI işlədilməyib. Bu tətbiq və ya mərhələ qəbulu deyil.
- Exact next step: nümunəni istifadəçiyə göstər və konkret təsdiqi gözlə; təsdiqdən sonra yalnız ilk mərtəbə üçün modular asset/anchor inteqrasiyası, mövcud grounding və seçimlərin production host yoxlaması.

---
# Cari vahid — purple birləşmə/torpaq, ilk mərtəbə dizaynı açıq — 14 noyabr 2026

- Completed: istifadəçinin işi mərtəbə birləşməsi və torpağa oturuşla məhdudlaşdırması əsasında təkrarlanan alt ətək çıxarıldı, hər sərhəddə vahid incə karniz çəkilir. Təməl konturuna uyğun təmas kölgəsi, ön alpha bitkilər və pilləkəndən davam edən daş yolu qoşuldu; son yoldan ot kənarına çıxma düzəldildi. İstifadəçi ilk mərtəbənin indi torpağa nisbətən yaxşı oturduğunu bildirdi.
- Current state: `fix/purple-seams-ground-contact`, baza `2be0583`; əvvəlki owner/asset/release/harness diff saxlanır. Bu vahid commit edilməyib, merge/push/CI yoxdur. Son istifadəçi ayrıca ilk mərtəbənin öz dizaynını bəyənmədi; bu iş həll edilməyib.
- Decisions: soldakı ilk şəkil/canonical `docs/design/building-purple-v1/join-reference.png` vizual əsasdır. Altı nümayəndə mərtəbə, selected-part ID/mixed xəritə, real say/geometry/aktiv oyun dəyişmir; bütöv bina bitmap-i tətbiq edilmir. Backend/API dəyişmədiyindən AppBackend işi lazım deyil. Yeganə plan `docs/PLAN.md` §7.
- Remaining work: ilk mərtəbə sadə dar həcmdir; referansdakı geniş daş plintus, giriş ətrafı memarlıq, yan alçaq həcmlər və yan dam yoxdur. Bitki/kölgə bunu həll etmiş sayılmır. Yeni ground-floor modular art/kompozisiya konkret göstərilməli, inteqrasiyadan əvvəl UI təsdiqi alınmalıdır. Digər tam paritet/68ID/runtime/font2 scroll məhdudiyyətləri review-da qalır.
- Relevant files: `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/PurpleBuildingArt.kt`; `assets/building-purple/{atlas.json,foreground-v1.png,foundation.png}`; canonical referans; `docs/reviews/2026-11-14-purple-seams-ground-contact.md`; `docs/reviews/purple-seams-ground-visual/{reference-before-after.png,detail-contact.png,module-boundaries-font1.0.png}`; `PurpleInventoryRenderTest.kt`, `PurpleModuleBoundaryRenderTest.kt`, GameTokens.kt. Əvvəlki review `2026-11-14-purple-modular-v2.md`.
- Checks performed: son native host5/5PASS32s,30production capture+2renderer-only boundary sheet. Final boundary/font2/mixed390/9variant contact/reference comparison-a baxıldı. Son source/asset editdən sonra adi app24unit+APK+lintPASS23s; kontrast4/4PASSmin8.22;diffcheckPASS. Sintetik host; device/emulator/serverruntime/GPU/inset/TalkBack/planşet/landşaft yoxlanmayıb. Imagegen alpha ön bitki/daş atlası builtin ilə yaradılıb, prompt/provenance review-dadır.
- Exact next step: yeni chatdə bu handoff+PLAN§7+son review və canonical referansı oxu; yalnız ilk mərtəbənin memarlığını referansdakı kimi modular düzəltmək üçün konkret art nümunəsi hazırla və istifadəçiyə göstər. Təsdiqdən sonra source/atlas anchor-ları və real detail/selected-part renderləri ilə inteqrasiya et. Torpaq oturuşunu və mərtəbə sayını yenidən dəyişmə; ayrıca göstərişsiz commit/merge/push/CI yoxdur.

---
Əvvəlki qeydlər aşağıdadır; cari qərar və açıq istifadəçi iradı yuxarıdadır.

# Cari vahid — purple modul art v2 tətbiqi, 14 noyabr 2026

- Completed: istifadəçi art-study.png nümunəsini “okey” ilə təsdiqlədi. Şəffaf atlas və ayrı foundation atlası tətbiq edildi; gövdə fasadları ölçülmüş dördbucaqlarla kəsilib proyeksiya olunur, qonşu mərtəbələr ortaq kənarla birləşir. Dam eave, qapı/pəncərə/fənər eyni perspektivdədir; pilləkən/dibçəklər ayrı qatdır. Düzbucaqlı garden maskası çıxarıldı.
- Current state: `fix/mobile-consistency-performance`, HEAD `2be0583`; source/asset/review diff commit edilməyib. Owner diff/release/contact/harness faylları saxlanır. Commit/merge/push/CI yoxdur. Son real Compose host renderləri hazırlanıb və baxılıb; istifadəçi qiymətləndirməsinə təqdim edilir.
- Decisions: mövcud category ID/selected-part/mixed mapping saxlanır, bütöv bina bitmap-i istifadə edilmir. Maksimum6nümayəndə mərtəbə/real metadata saxlanır. Backend/domain/API/authority/aktiv oyun/şəhər dəyişmir, AppBackend dəyişməsi lazım deyil. Yeganə plan `docs/PLAN.md` §7.
- Remaining work: tam piksel pariteti deyil: referansdakı bespoke4mərtəbə əvəzinə müntəzəm6mərtəbə, təkrarlanan balkon/trim və alternativ kataloq artının öz fərqləri qalır. Tam68ID vizual qəbulu yoxdur. Font2 inner/tab scroll əvvəlki kimidir. Yeni böyük işə avtomatik başlanmır.
- Relevant files: `PurpleBuildingArt.kt`; `assets/building-purple/{atlas.png,atlas.json,foundation.png}`; `docs/design/building-purple-v1/modular-v2/`; review `docs/reviews/2026-11-14-purple-modular-v2.md`; `docs/reviews/purple-modular-v2-visual/{reference-comparison.png,inventory-detail-contact.png,module-boundaries-font1.0.png}`; `PurpleInventoryRenderTest.kt`, `PurpleModuleBoundaryRenderTest.kt`.
- Checks performed: son native5/5PASS36s;30production ekran capture(360/390font1,320font2;9detail,category swaps/mixed/40floor/fullbase2fallback)+2renderer-only1/4/40floor sərhəd sheet. İlkin1/4floor inventory fixture testləri min10qaydasına görə uğursuz oldu; fixture düzəldildi, production qaydası dəyişmədi. Final contact/font2/boundary/fallback/reference comparison-a baxıldı. Adi app24unit/APK/lintPASS22s; text contrast4/4PASSmin8.22; diffcheckPASS. Synthetic host; device/server/runtime/GPU/inset/TalkBack/tablet/landscape/alllocale yoxlanmayıb; emulator/CI açılmayıb.
- Exact next step: son renderləri istifadəçiyə göstər. Növbəti chatdə bu handoff +PLAN§7 +review əsasında yalnız istifadəçinin konkret vizual düzəlişini və ya ayrıca commit göstərişini icra et. Əlavə təsdiqsiz merge/push/CI yoxdur.

---
# Son istifadəçi qiymətləndirməsi — 14 noyabr 2026

- İstifadəçi son grounding/lighting renderini rədd etdi: hələ yamaq kimi görünür, təsdiqli referansdakı bütöv/dolğun bina keyfiyyəti yoxdur. Əvvəlki build/render PASS vizual qəbul deyil.
- Yeni əlavə eyni vizual hədəfdir: `/var/folders/02/vb81250n1ld9gwqlq74ybm000000gn/T/codex-clipboard-df65a17c-94d9-43f0-b2b4-7c5c3944feeb.png`. Canonical istinad `docs/design/building-purple-v1/join-reference.png` saxlanır.
- Cari source düzəlişi commit edilməyib və təsdiqlənməyib. Mövcud selected-part ID/mapping və owner diff saxlanmalıdır.
- Müşahidə: təkrarlanan müstəqil gövdə/karnizlər, gövdəyə uyğunlaşmayan giriş/dekal perspektivi və ayrılıqda garden qatının kompozisiyası vahid bina hissini pozur. Əlavə glow bunu bağlamır. Dəqiq asset sərhədləri növbəti scoped yoxlamada ölçülməlidir.
- Exact next step: ui-craft və lazım olarsa imagegen skill ilə eyni təsdiqli dizayn üçün vahid perspektivli, uyğun birləşmə kənarlı modular art nümunəsi hazırla; mövcud hissə seçimlərini saxla. Nümunəni tətbiqdən əvvəl istifadəçiyə göstər və konkret təsdiq al. Bütöv sabit bina bitmap-i ilə hissə dəyişmələrini ləğv etmə; yeni alternativ dizayn uydurma. Sonra real inventory/detail renderləri ilə yoxla. Yeganə plan `docs/PLAN.md` §7. Commit/merge/push/CI yoxdur.

---
# Cari vahid — purple təməl/işıq düzəlişi, 14 noyabr 2026

- Completed: `PurpleBuildingArt.kt` səhnə alt sərhədi torpağın tam görünməsi üçün düzəldildi; ground-a bağlı garden anchor, təmas kölgəsi, kənar foreground bitki örtüşməsi, base_1 fənər/pəncərə üçün atlasdan götürülmüş isti işıq yayılması əlavə edildi. İlk diaqonal grass maskası son renderdən əvvəl düzəldildi.
- Current state: `fix/mobile-consistency-performance`, HEAD `2be0583`, dəyişikliklər commit edilməyib; owner diff/release saxlanır. Merge/push/CI yoxdur.
- Decisions: mövcud hissə ID-ləri/seçim/mixed mapping, real say/geometry/server/aktiv oyun dəyişmir. Yeni asset və alternativ dizayn yoxdur. AppBackend müqaviləsi dəyişmədiyindən backend dəyişməsi tələb olunmur.
- Remaining work: tam referans piksel pariteti deyil; 6 nümayəndə mərtəbə və təkrarlanan karniz/modul gövdə fərqi qalır. Tam kataloq və şəhər bu vahiddən kənardır. İstifadəçinin vizual qiymətləndirməsi gözlənir.
- Relevant files: `PurpleBuildingArt.kt`; `docs/reviews/2026-11-14-purple-grounding.md`; `docs/reviews/purple-grounding-visual/`; təsdiqli `docs/design/building-purple-v1/join-reference.png`; yeganə plan `docs/PLAN.md` §7.
- Checks performed: son native host3/3PASS23s,27capture(360/390font1,320font2;8detail variant); final contact/reference comparison/font2 görüntülərinə baxıldı. Adi app24unit/APK/lintPASS13s; kontrast4/4PASSminimum8.22; diffcheckPASS. Production Compose + synthetic account/HTTP fixture-dir; canlı server/device/emulator/GPU/inset/TalkBack/tablet/landşaft yoxlanmayıb. Font2 inner scroll/tab scroll əvvəlki kimidir.
- Exact next step: real inventar/detail renderlərini və referans müqayisəsini istifadəçiyə göstər; ayrıca göstərişsiz commit/merge/push/CI yoxdur.

---
# Cari davam — təməl/torpaq oturuşu və vizual dolğunluq, 14 noyabr 2026

- Completed: istifadəçinin açıq tapşırığı ilə təsdiqli selected-part template inteqrasiyası lokal commit edildi: `2be0583` (`feat: integrate approved purple modular building previews`). Branch `fix/mobile-consistency-performance`; merge/push/CI yoxdur. Əlaqəsiz owner contact/harness/release və köhnə bitmap faylları commit-dən kənarda saxlanılıb.
- Current state: istifadəçi commit vaxtı yenidən eyni referansı göstərərək binanın torpağa referansdakı kimi oturmadığını və hələ eyni canlı/dolğun görünmədiyini bildirdi. Bu vizual çatışmazlıq açıqdır; son müşahidədən sonra production düzəlişi edilməyib. Əvvəlki “oturuş yoxlandı” qeydi istifadəçinin son qiymətləndirməsini əvəz etmir.
- Decisions: mövcud təsdiqli bina və modul selected-part mexanizmi saxlanır; yeni alternativ dizayn uydurulmur. “Canlı” hazırkı statik referans kontekstində isti işıq, həcm, təmas kölgəsi və bitki/torpaq birləşməsi kimi şərh edilir; ayrıca animasiya tələbi təsdiqlənməyib. Real say/server geometry/authority/aktiv oyun dəyişmir.
- Remaining work: təməlin oturuşu, bina–torpaq təmas kölgəsi və ön/arxa bitki örtüşməsi, pilləkən–torpaq keçidi; fənər/pəncərə işığının fasad və pilləkənə yayılması; mərtəbə birləşmələrinin dolğunluğu. Bunların dəqiq səbəb/realizasiya yoxlaması növbəti vahiddədir; çatışmayan raster qat varsa açıq göstər, sxematik əvəz tətbiq etmə.
- Relevant files: `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/PurpleBuildingArt.kt`, `InventoryScreen.kt`, `GameTokens.kt`; `assets/building-purple/atlas.{png,json}`; `docs/design/building-purple-v1/join-reference.png`; `docs/reviews/purple-inventory-visual/reference-comparison.png`, `inventory-detail-contact.png`; `docs/reviews/mobile-sizing-visual/render/PurpleInventoryRenderTest.kt`; yeganə plan `docs/PLAN.md`§7. Yeni əlavə eyni referansı göstərir: `/var/folders/02/vb81250n1ld9gwqlq74ybm000000gn/T/codex-clipboard-92eed712-ca6c-4cd7-a5c0-2050f3d4540a.png`.
- Checks performed: commit öncəsi native3/3,27capture və adi app24unit/APK/lintPASS, diffcheckPASS. Yeni müşahidə üçün hələ dəyişiklik/yeni yoxlama yoxdur. Emulator/CI açılmır.
- Exact next step: ui-craft və yuxarıdakı scoped source/atlas/reference comparison oxu; template-in ground/garden/entrance anchor-ları və qat sırasını müqayisə et, eyni təsdiqli dizayna uyğun təməl/işıq vahidini hazırla və real inventory/detail before/after renderlərini göstər. Tam vizual uyğunluğu əvvəlcədən PASS sayma.

---
# Cari vahid — purple template selected-part inteqrasiyası, 14 noyabr 2026

- Completed: təsdiqli modul atlas production shared BuildingPreview-a bağlandı; bütöv bitmap istisnası çıxarıldı. Mövcud server parts ID-ləri ilə ayrı window/roof/entrance/light/wall və mixed qatlar inventar/detaildə çəkilir. Son istifadəçi mərtəbə/təməl–torpaq birləşmə referansı saxlanıldı və renderlə tutuşduruldu.
- Current state: `fix/mobile-consistency-performance`, HEAD `4ae6f87`; owner diff/release qorunur. İstifadəçi nəticəni təsdiqlədi və lokal commit tapşırdı; bu vahid commit edilir. Merge/push/CI yoxdur.
- Decisions: `base_1` hissəsi olan mixed seçim purple template saxlayır; tam digər üslublar əvvəlki renderer-dədir. base_1 window→arch, roof→roof, entrance→entrance, lighting→lamp, wall→blank shell; square yeni ID deyil. Maksimum6nümayəndə mərtəbə; real say/accepted geometry/server dəyişmir. Alternativ damın boşluğu öz aspect ratio/eave ilə düzəldildi. Şəhər/aktiv oyun scope deyil.
- Remaining work: tam68IDart pariteti yoxlanmayıb; digər üslubların artı/şəhər retuşu ayrıca. Təkrarlanan karnizlər referansdan daha müntəzəm/qalındır; bespoke ofset masonry ilə piksel eyniliyi iddia edilmir. Font2 inner scroll/tab scroll saxlanır.
- Relevant files: `PurpleBuildingArt.kt`, `InventoryScreen.kt`, `GameTokens.kt`; `assets/building-purple/`; `docs/design/building-purple-v1/modular/README.md`, `join-reference.png`; `docs/reviews/2026-11-14-purple-template-integration.md`; `docs/reviews/purple-inventory-visual/`; `PurpleInventoryRenderTest.kt`. Yeganə plan `docs/PLAN.md`§7.
- Checks performed: final native3/3PASS31s,27production-screen capture(360/390font1,320font2;8detail variants incl40floors); final board/roof/mixed/wall/font2 və reference comparison görüntülərinə baxıldı. Son production editdən sonra adi app24unit/APK/lintPASS18s; diffcheckPASS. Backend müqaviləsi dəyişmədiyindən AppBackend dəyişməsi lazım deyil. Device/emulator/real hesab/GPU/TalkBack/inset/tablet/landşaft/alllocale yoxlanmayıb; CI yoxdur.
- Exact next step: lokal commit nəticəsini bildir; ayrıca göstərişsiz merge/push/CI və yeni feature başlama.

---
Əvvəlki qeydlər tarixçədir; cari status yuxarıdadır.

# Cari vahid — modullu bənövşəyi template sınağı, 14 noyabr 2026

- Completed: istifadəçinin təsdiqli bina üçün modul yanaşma göstərişi ilə9qatlı atlas və Compose/Canvas birləşdirmə nümunəsi hazırlandı. Gövdə/ground/dam/pəncərə/giriş/lampa/balkon/torpaq ayrı çəkilir. İki pəncərə və4/6mərtəbə renderi var; bütöv bina şəkli ilə dəyişmə deyil.
- Current state: fix/mobile-consistency-performance, HEAD4ae6f87; yeni atlas+harness review-only-dir, production-a qoşulmayıb. Əvvəlki production/owner diff və release qorunur. Commit/merge/push/CI yoxdur.
- Decisions: istifadəçi dəqiqləşdirdi və təsdiqlədi: mövcud oyundakı kimi inventardan seçilən hissələr uyğun yeni template-də işləməlidir; ikinci sistem qurulmur. Hazırda yalnız layer swap və birləşmə sınağı var, real catalog ID mapping yoxdur. PLAN mövcud authority olaraq qalır; ayrıca plan yaradılmır.
- Remaining work: mövcud selected-part renderer müqaviləsinə uyğun dəqiq catalog mapping, divar/dam/giriş/işıq və mixed hissələrin dəyişməsi, sonra real inventory/detail inteqrasiyası/renderi. Digər üslublar və şəhər hələ dəyişməyib.
- Relevant files: docs/design/building-purple-v1/modular/{atlas-v1.png,atlas-v1.json,README.md}; docs/reviews/mobile-sizing-visual/render/ModularBuildingStudyTest.kt; docs/reviews/2026-11-14-modular-building-study.md; docs/reviews/modular-building-visual/; production GameFacade.kt,BuildingArt.kt,InventoryScreen.kt; docs/PLAN.md§7.
- Checks performed: native2/2PASS10s,6capture(390font1/320font2;arch/square/6floors). Final arch/squarefont1 və6floorfont2görüntülərinə baxıldı. İlkin dam boşluğu/lampa üst-üstə düşməsi düzəldildi. DiffcheckPASS. Production dəyişmədiyindən adi APK/lint təkrarlanmadı. Backend dəyişməsi lazım deyil; emulator/CI yoxdur.
- Exact next step: modullu nümunəni göstər; yeni chatdə bu atlas+harness və GameFacade seçilmiş hissə müqaviləsini oxuyub tək purple template-i mövcud real ID-lərə bağla, hissə dəyişmələrinin real inventory/detail renderini hazırla. Tam kataloq hazır sayılmır.

---
# Cari vahid — təsdiqli bina artının inventar/detala tətbiqi, 14 noyabr 2026

- Completed: əvvəlki preview vizualı istifadəçi tərəfindən istinada yaxın sayılmadı. Yeni dolğun bənövşəyi imagegen nümunəsi açıq təsdiqləndi; şəffaf derivative production BuildingPreview-a qoşuldu. İnventar/detal və shared parts-edit istifadə edir.
- Current state: `fix/mobile-consistency-performance`, HEAD4ae6f87; bu və əvvəlki owner diff-ləri commit edilməyib. Owner release qorunur; merge/push/CI yoxdur.
- Decisions: yalnız tam base_1 dəsti,≥10real mərtəbə və mövcud geometry/art üçün təsdiqli sabit nümayəndə asseti. Real mərtəbə/ownership/server geometry dəyişmir; başqa/mixed dəstlər modular qalır. Bütün kataloqun çevrilməsi hazır deyil. İstinad docs/design/building-purple-v1/approved-reference.png; PLAN§7 və güzgülər yeniləndi.
- Remaining work: digər dəstlər üçün uyğun art/modular yanaşma, şəhər retuşu və əvvəlki mobil uyğunluq/keçid/floor-foundation işləri. Bu tək nümunə onları bağlamır.
- Relevant files: InventoryScreen.kt; drawable-nodpi/building_purple_approved.png; docs/design/building-purple-v1/; docs/reviews/2026-11-14-approved-building-preview.md; docs/reviews/approved-building-visual/; CompactFlowRenderTest.kt/render.init.gradle.
- Checks performed: native3/3PASS25s(360/390font1,320font2);390inventory/detail və320font2detail görüntülərinə baxıldı. Adi app24unit/APK/lintPASS13s; diffcheckPASS. Backend dəyişikliyi lazım deyil. Emulator/CI yoxdur; real hesab/device yoxlanmayıb.
- Exact next step: inventar və detalın real Compose host renderlərini istifadəçiyə göstər; tək base_1 dəsti məhdudiyyətini açıq de. Ayrıca göstərişsiz şəhərə/kataloqa keçmə, merge/push/CI etmə.

---
# Cari vahid — təbii nisbətli nümayəndə bina, 14 noyabr 2026

- Completed: əvvəlki yastılanan3×üfüqi stretch çıxarıldı. Owned inventory/detail/part-edit preview-da maksimum8nümayəndə mərtəbə,30scene-vahid diapazona yumşaldılmış ofset və uniform sprite scale tətbiq edilir. Torpaq260vahid enlə öz nisbətlərini saxlayır. Real mərtəbə sayı metadata/accessibility və server məlumatında qalır. Aktiv oyun eni/fizikası dəyişmir.
- Current state: `fix/mobile-consistency-performance`, baza `4ae6f87`; bu chat dəyişiklikləri commit edilməyib. Owner release qorunur; merge/push/CI yoxdur.
- Decisions: istifadəçi40mərtəbəni6–8iri vizual mərtəbə ilə göstərməyi açıq təsdiqlədi. Əvvəlki full-floor stretch nəticəsini rədd etdi; aşağıdakı width qeydi tarixidir. PLAN §7 və qərar güzgüləri yenilənib. Əsas referans `docs/design/city-view.png`.
- Remaining work: eyni estetik yanaşmanın şəhər səhnəsinə uyğunlaşdırılması; əvvəlki digər mobil uyğunluq/keçid/floor-foundation işləri. Bu vahid şəhər renderer-ini dəyişmir.
- Relevant files: `BuildingArt.kt`, `GameTokens.kt`, `docs/reviews/2026-11-14-building-preview-width.md`, `docs/reviews/mobile-sizing-visual/render/BuildingWidthRenderTest.kt`, `docs/PLAN.md` §7.
- Checks performed: final native5/5PASS26s;390font1/320font2nümayəndə bina renderlərinə vəproduction360contact-a baxıldı. Adi app unit/APK/lintPASS16s; diffcheckPASS. Backend/domain/API dəyişmir, AppBackend dəyişikliyi lazım deyil. Emulator/CI açılmır.
- Exact next step: yoxlanmış təbii nisbətli preview nəticəsini təhvil ver; şəhər retuşunu ayrıca kiçik vahid kimi davam et. Aktiv oyun/server həndəsəsini dəyişmə. Merge/push/CI avtomatik yoxdur.

---
# Cari vahid — preview eni və torpaq, 14 noyabr 2026

- Completed: `BuildingArt.kt` owned-building preview üçün ayrıca üfüqi miqyas (maksimum3×, mövcud enə sığır) və torpaq eni180→260 scene vahidi. Bütün mərtəbələr görünür; accepted koordinatlar dəyişmir. Aktiv oyun renderer/engine dəyişməyib.
- Current state: `fix/mobile-consistency-performance`, baza `4ae6f87`; dəyişikliklər commit edilməyib. Owner untracked `android-billing/app/release/` qorunur. Merge/push/CI yoxdur.
- Decisions: istifadəçi tam bina+geniş preview/torpağı açıq seçib. Son əlavə qərar: şəhər/inventar/detail görünüşündə 100% həndəsə eyniliyi tələb deyil, `docs/design/city-view.png` kimi kiçik estetik retuş icazəlidir. Yeni attachment mövcud city-view ilə SHA256 eynidir. Bu retuş hələ implementasiya edilməyib; cari width vahidindən sonra gəlir. Server geometry/fizika/aktiv oyun eni dəyişmir. PLAN §7 və qərar güzgüləri yenilənib.
- Remaining work: estetik retuş; əvvəlki digər mobil uyğunluq, keçid və floor/foundation işləri açıqdır. Bu vahid onları bağlamır.
- Relevant files: `BuildingArt.kt`, `GameTokens.kt`; `docs/reviews/2026-11-14-building-preview-width.md`; `docs/reviews/mobile-sizing-visual/render/BuildingWidthRenderTest.kt`; `docs/PLAN.md` §7, `docs/design/city-view.png`.
- Checks performed: Java21/offline native5/5PASS22s (1/16/40mərtəbə, düz/ofsetli,390×844font1 və320×568font2; real screen flow360/390/320font2). Before/after normal və compact renderlərə, production360contact-a baxıldı. Adi app unit/APK/lintPASS19s. Diffreview/diffcheckPASS. Palette dəyişmir. Backend/API dəyişmir, AppBackend dəyişikliyi lazım deyil. Synthetic host; device/emulator/CI/TalkBack/GPU/tablet yoxlanmayıb.
- Exact next step: cari width diff-i təhvil ver; sonra istifadəçinin artıq verdiyi city-view retuş göstərişini ayrıca kiçik vahid kimi hazırla. Qəbul edilmiş server məlumatını dəyişmə, aktiv oyun eninə toxunma. Merge/push/CI avtomatik yoxdur.

---
# Cari vahid — mobil uyğunluq davamı, 14 noyabr 2026

- Completed: performans `e0ad8ae` artıq commit olunub. Bu davamda `PurchaseHeader` uzun balans/font2 zamanı loqonu sıxışdırmır; balans ayrıca sətirdədir, dar başlıq mövcud heading tokenindən istifadə edir. Alış header vahidi ayrıca lokal commit edilir.
- Current state: `fix/mobile-consistency-performance`, baza `e0ad8ae`; owner untracked `android-billing/app/release/` qorunur. Merge/push/CI yoxdur.
- Decisions: mövcud konkret mobil dizayn təsdiqi qüvvədədir, yenidən soruşma; mobil portret, planşet ayrıca. Yeni palette/asset/alternativ dizayn yoxdur. Backend API/domain dəyişmədiyindən AppBackend dəyişməsi lazım deyil.
- Yeni istifadəçi müşahidəsi (14 noyabr, iki telefon screenshot-u): inventar kartları və bina detalları ekranında binalar çox nazik/ensiz görünür, xüsusilə 35–40 mərtəbəli binalar. Alt torpağın eni də bina eni ilə demək olar eynidir və vizual olaraq yetərli deyil. Bu təqdimatlarda bina eni/miqyası və torpaq-bina nisbəti düzəldilməlidir; real qəbul edilmiş mərtəbə sayı/ofset/siluet qorunmalıdır. **Aktiv oyun oynanılan yerdə bina eni istifadəçiyə görə yaxşıdır; həmin eni bu düzəlişə görə dəyişmə.** Bu müşahidə qeydə alınıb, implementasiya/render hələ edilməyib. Mənbə screenshot-lar: `codex-clipboard-4f4e2541-0358-41e4-a9e8-d35aeebb8133.png` (inventar), `codex-clipboard-692046fb-7d0f-4c83-b617-98887dd04686.png` (40 mərtəbəli bina detalı); bu chat əlavələridir. Scoped yoxlama: `BuildingArt.kt`, `InventoryScreen.kt` və bu renderer-in digər preview istifadəçiləri.
- Remaining work: digər ekranların logo/header/düymə/kart uyğunluğu; route keçidləri; bitmap decode/frame polling performansı; mərtəbə birləşmələri və ilk mərtəbənin torpağa oturuşu. Geniş istifadəçi tapşırığı hələ tamamlanmayıb.
- Relevant files: `CityPurchaseChrome.kt`, `MobileUiSizing.kt`, `GameTokens.kt`; `docs/reviews/2026-11-14-mobile-purchase-header.md`; mövcud `docs/reviews/mobile-sizing-visual/render/CompactPurchaseRenderTest.kt`; növbəti `GameScene.kt`, `GameFacade.kt`, `GameAtlas.kt`, `OnlineGame.kt`, `docs/reviews/game-joins-visual/render/`. Yeganə plan `docs/PLAN.md`.
- Checks performed: Java21/offline adi unit/APK/lint PASS23s; final synthetic native purchase host8/8PASS18s; 320/font2/long balance,360,390,440portret və land font2 confirmation görüntülərinə baxıldı. DiffcheckPASS. Device/FPS/runtime/TalkBack yoxlanmayıb, emulator/CI açılmayıb.
- Exact next step: yeni chatdə bu handoff+PLAN oxu; eyni branch-də təsdiqli game-tap/mobile reference və game-joins harness ilə mərtəbə/foundation birləşməsini ayrıca kiçik vahid kimi həll et, review və ayrıca commit et. Sonra digər mobil ekran/keçid işlərini scope ilə davam et. Təsdiqi təkrar istəmə; main merge/push/CI avtomatik yoxdur.

---
Əvvəlki vahidlərin tarixçəsi (aşağıdakı Exact next step-lər cari göstərişi əvəz etmir):

# Cari vahid — şəhər qalan vizual detallar, 14 noyabr 2026
- Completed: istifadəçi city-view istinadını göstərildikdən sonra açıq təsdiqlədi. `fix/city-remaining-visuals` (baza ce12acf) üzərində geniş kafe terrası asseti və ikinci mərtəbə ön balkon inteqrasiyası hazırlandı; corner-window dar balkon və ground giriş/işıq qorunur.
- Current state: istifadəçinin göstərişi ilə bu review edilmiş vahid lokal commit edilir. Mərhələ4 razılaşdırılmış mobil scope üzrə istifadəçi tərəfindən qəbul edilib. Merge/push/CI yoxdur; untracked app/release qorunur.
- Decisions: mövcud təsdiqli sahil/balkon/glow/navbar/sabit kamera və server geometry/ownership dəyişmir. Yeni asset faithful reference reconstruction-dır; cafe iqtisadiyyatı deyil. AppBackend dəyişikliyi tələb olunmadı.
- Remaining work: istifadəçi mövcud kataloqla tamamlamağı seçdi; fontan əlavə edilmir, bu qərar açıq sual deyil. Mərhələ4 mobil scope-də açıq implementasiya işi qalmır; istifadəçinin şərtli təsdiqi qəbul kimi qeydə alınıb. Tam piksel eyniliyi və yoxlanmamış runtime halları PASS sayılmır.
- Relevant files: CityGardenArt.kt,CityScreen.kt; assets/city/terrace-v1.{png,json}; docs/design/city-garden-v1/terrace-v1.md; docs/reviews/2026-11-14-city-remaining-visuals.md; city-scene-visual/render/CityParityRenderTest.kt; yeganə plan docs/PLAN.md.
- Checks performed: native parity3/3PASS17s (3portrait +5focus/return hər ssenari); üç overview+390focus0 baxıldı. Adi236unit/APK/lintPASS20s(core/engine up-to-date); kontrast4/4min8.22. Synthetic host; device/emulator/CI yoxdur.
- Exact next step: lokal commit nəticəsini bildir. Mərhələ5 və planşet ayrıca tapşırıq tələb edir; fontan əlavə etmə. Merge/push/CI avtomatik yoxdur.

# Cari handoff — 14 noyabr 2026

## Qalan phase4 və QA fayllarının lokal commit-i — 14 noyabr 2026
- Completed: detail/parts `5362c44`, tarixçəli merge `7ef170e` ilə main/origin-main-ə push edildi; həmin merge-də owner dəyişiklikləri daxil edilmədi, CI işlədilmədi. Son istifadəçi qalan commit edilməli dəyişiklikləri də saxlamağı tapşırdı.
- Current state: `chore/remaining-phase4-evidence`, baza `7ef170e`. Əvvəlki owner detail responsive fix/test/acceptance/PLAN/handoff/renderləri indi ayrıca phase4 commit-inə, əvvəlki live QA qeyd/patch/XML isə ayrıca docs commit-inə daxildir. Aşağıdakı pending/commit edilməyib statusları tarixidir.
- Decisions: generated `android-billing/app/release/` APK/DM/metadata saxlanır, commit edilmir. QA patch yalnız sənəd kimi saxlanır, production-a tətbiq olunmur. Bu yeni commit-lər üçün merge/push/CI yoxdur.
- Remaining work: tam mərhələ4 qəbulu/şəhər parity və əvvəlki runtime məhdudiyyətləri açıqdır. Yeni böyük vahid bu chatdə başlanmır.
- Relevant files: `docs/reviews/phase4/acceptance.md`, `docs/reviews/2026-11-14-remaining-phase4-review.md`, `InventoryScreen.kt`, iki mövcud render harness; `docs/reviews/2026-11-13-game-release-live-check.md`, game-joins QA patch/XML; yeganə plan `docs/PLAN.md`.
- Checks performed: source dəyişdirilmədən lokal diff review; adi unit/APK/lint eyni production source-da əvvəlki PASS2s/236unit reuse. Tarixi phase4 image SHA256/JSON və QA XML formatları yoxlandı. Cari fokuslu host15/15PASS43s; son üç contact şəklinə baxıldı; diffcheckPASS. Emulator/CI açılmadı.
- Exact next step: lokal commit nəticələrini təhvil ver; yeni iş yeni chatdə bu handoff+PLAN+review-dan davam et. Yeni merge/push/CI ayrıca göstəriş tələb edir.

## Detail/parts vizual tamamlama — cari yekun, 14 noyabr 2026
- Completed: istifadəçinin bu chatdəki açıq tətbiq göstərişi ilə təsdiqli board-ların detail tab/metadata və beş parts kateqoriya ikonları, gradient/cyan çərçivə/aktiv glow tamamlandı. Owner dar DetailRow adaptasiyası saxlanıb; əlaqəsiz tracked owner baseline və iki contact PNG bayt eyniliyi yoxlanıb.
- Current state: `fix/phase4-detail-parts-parity`, HEAD/baza `eaf43e3`; source/review/harness lokal review-dan sonra ayrıca lokal commit-də saxlanır. Owner diff-i index-dən kənardadır. Merge/push/CI yoxdur.
- Decisions: son compact/navbar/modular art və mobil portret qərarları qüvvədədir. Backend/domain/API dəyişmir; AppBackend dəyişikliyi lazım deyil. Təsdiqli görünüş yenidən seçimə çıxarılmır.
- Remaining work: bu scoped ikon/çərçivə vahidi yoxlanıb. Tam raster piksel eyniliyi/mərhələ4 qəbulu və şəhər parity işi bağlanmır.320/font2 secondary metadata scroll tələb edir; cihaz/inset/TalkBack/GPU/API24runtime və planşet/landşaft yoxlanmayıb.
- Relevant files: InventoryScreen.kt,PartsScreen.kt,PartsChrome.kt; drawable/detail_*.xml; `docs/reviews/2026-11-14-phase4-detail-parts-parity.md`; `docs/reviews/mobile-sizing-visual/render/DetailPartsParityRenderTest.kt`; `docs/reviews/detail-parts-visual/`; plan yalnız `docs/PLAN.md`.
- Checks performed: final native8/8PASS23s; normal unit/APK/lintPASS28s (core117+engine95UP-TO-DATE,app24icra); kontrast4/4min8.22,diffcheckPASS; təsdiqli board-larla yanaşı müqayisə və final görüntülərə baxış. Synthetic host; emulator/CI açılmayıb.
- Exact next step: lokal commit nəticəsini istifadəçiyə təhvil ver. Ayrı göstərişsiz merge/push/CI və yeni böyük vahid başlama. Aşağıdakı əvvəlki başlanğıc qeydinin pending konkret UI təsdiqi statusu artıq tarixçədir.

## Cari vahid — mərhələ 4 detail/parts vizual tamamlama (14 noyabr 2026)
- Completed: scheduler review/commit `92c306b`; bütün mövcud branch commit-ləri `eaf43e3` ilə main-ə merge və origin/main-ə push edildi; lokal/remote main eyni idi. Owner19fayl SHA256 ilə dəyişməz qaldı. İstifadəçi mərhələ4davamını istədi; PLAN və acceptance qalan fərqləri oxundu, mövcud iki təsdiqli detail/parts board-una baxıldı.
- Current state: `fix/phase4-detail-parts-parity`, baza `eaf43e3`; UI implementasiyası başlamayıb. Mövcud owner InventoryScreen/phase4/render/release/QA/PLAN/handoff diff-i qorunur. Əvvəlki “merge/push yoxdur” scheduler qeydləri tarixçədir.
- Decisions: ilk kiçik vahid detail tab/metadata ikonları, parts kateqoriya ikonları və təsdiqli çərçivə detallarıdır. Son compact/navbar/modular art qərarları qorunur; board-dakı köhnə navbar qaytarılmır. Placement bağlıdır, bazar mərhələ5/reklam mərhələ6bu vahidə daxil deyil. AGENTS konkret UI təqdimatı+təsdiqi gözlənir; ümumi başlama göstərişi bu təsdiqi əvəz etmir.
- Remaining work: konkret göstərilən dizaynın tətbiq təsdiqi, sonra scoped source/token/asset baxışı və implementasiya, mobil portret host render+görüntüyə baxış, fokuslu yoxlamalar və review. Şəhər tam parity ayrıca növbəti vahiddir; mərhələ qəbulu yoxdur.
- Relevant files: docs/PLAN.md; docs/reviews/phase4/acceptance.md; docs/design/city-completion-v1/{04-building-detail.png,02-parts-flow-v2.png}; app {GameTokens.kt,InventoryScreen.kt,PartsScreen.kt}; mövcud docs/reviews/mobile-sizing-visual/render/.
- Checks performed: bu başlanğıcda yalnız sənəd/status/istinad görüntüləri yoxlanıb; yeni source/build/test/render yoxdur. Əvvəlki scheduler unit/APK/lint və callback host nəticələri onun review-sundadır. Emulator/CI açılmadı.
- Exact next step: yeni chatdə bu bölmə+PLAN+acceptance oxunsun; istifadəçinin detail/parts konkret tətbiq təsdiqi varsa yenidən soruşmadan həmin kiçik vahidi mövcud owner diff-i qoruyaraq tamamla; təsdiq yoxdursa göstərilən iki board üçün onu al. Merge/push/CI üçün yeni ayrıca göstəriş tələb olunur.

## Cari vahid — connectivity retry scheduler (14 noyabr 2026)
- Completed: `fix/network-resilience`, HEAD `83212be` üzərinə process-owned foreground/connectivity backoff scheduler əlavə edildi. Mövcud owner exact transient Error-u retry edir; sole journal/terminal reconciliation dəyişmir. Default network callback foreground-da register, stop-da unregister olur; stale callback-lər rədd edilir. ACCESS_NETWORK_STATE əlavə edildi, Firebase token network xətası retryable IO kimi ötürülür.
- Current state: scheduler source/test/review ayrıca lokal review edildi və bu vahid lokal commit-də saxlanır. Owner InventoryScreen/phase4/render/release/QA və qarışıq PLAN/handoff hissələri qorunur. Merge/push/CI yoxdur.
- Decisions: retry yalnız foreground + validated internet və transient IO/408/429/5xx/definitive RevisionConflict üçündür; 1/2/4/8/16/32/60 saniyə (cap60) backoff, hər anda bir timer və mövcud recovery. Auth/format/unknown/abandoned/receipt-siz reconciliation Error avtomatik dövr yaratmır; həmin pending silinmir/restart edilmir, manual/foreground yoxlaması qalır. Ready və aktiv lokal fizika şəbəkə callback-i ilə invalidate edilmir. Background yeni timer dispatch-ini saxlayır; artıq başlamış recovery ləğv edilmir. OS background worker/ikinci journal yoxdur.
- Checks performed: ayrıca lokal review run-u Java21/offline unit/APK/lint PASS1s (107/108 up-to-date, əvvəlki236 unit reuse); callback host API28/34 2/2PASS5s. Yeni bug tapılmadı. Əvvəlki sübut: Java21/offline core117+engine95+app24=236 unit, failures/errors/skipped0; APK/lint final PASS13s (108task,84up-to-date). API28/34 Robolectric callback lifecycle host2/2PASS4s; invalid validation/handover/stop/stale callback. Yeni8 adi regression: scheduler7 + real terminal owner/sole-journal retry1. Scoped manual diff review/diffcheck PASS; owner17 fayl hash-i eyni. Owner diff-i ilə working-tree yoxlamasıdır. Emulator/device/real-server/API24–25runtime/render/CI yoxdur.
- Remaining work: scheduler lokal review/commit vahidi tamamlandı; ayrıca istifadəçi göstərişi olmadan yeni iş yoxdur. Proses bağlıykən retry zəmanəti yoxdur; durable queue növbəti auth/foreground-da bərpa edilir. API24–25 üçün initial capabilities fallback var, runtime yoxlanmayıb. Qeyri-müəyyən/başqa-device pending-in avtomatik resolution siyasəti və əvvəlki full parity/runtime məhdudiyyətləri qalır.
- Relevant files: core `RecoveryRetryScheduler.kt`, `AuthenticatedRecoveryOwner.kt`, `RecoveryRetrySchedulerTest.kt`; app `RecoveryConnectivity.kt`, `WobbrikApplication.kt`, `AndroidManifest.xml`; engine `PendingGameResultsTest.kt`; review `docs/reviews/2026-11-14-connectivity-retry-scheduler.md`, host `docs/reviews/connectivity-retry/host/`. Yeganə plan `docs/PLAN.md` §§2–4. AppBackend müqaviləsi dəyişmir, backend dəyişikliyi lazım deyil.
- Exact next step: scheduler lokal commit nəticəsini bildir; yeni vahid yalnız ayrıca istifadəçi tapşırığı ilə başlayır. Owner fayllarını və sənədlərin unrelated hissələrini commit-ə qatma; qeyri-müəyyən pending-i silmə/restart etmə. Yeni böyük feature başlama, merge/push/CI işlətmə.


## Cari vahid — account/receipt reconciliation (14 noyabr 2026)
- Completed: `fix/network-resilience` üzərində account-scoped attempt/finish-receipt GET adapterləri, abandoned status-un yalnız historical parser-də oxunması və pending reconciliation qoşuldu. Öz aktiv queued cəhdimiz əvvəl tamamlanır. Uyğun finished attempt + sabit finish receipt + fresh sync-dən sonra yalnız exact intent diskdən bağlanır.
- Current state: HEAD `3ddc047` üzərinə vahid lokal review edildi, Bugbot actionable bug tapmadı; yalnız reconciliation source/test/review və öz PLAN/handoff hissəsi lokal commit edilir. Merge/push/CI yoxdur. Əvvəlki owner phase4/release/QA/source/render/handoff dəyişiklikləri qorunur.
- Decisions: qeyri-müəyyən/404/abandoned/receipt-siz/uyğunsuz pending silinmir və restart edilmir; başqa-device active avtomatik abandon edilmir. Historical snapshot Ready/account cache kimi publish edilmir; son sync equal revision-da da balansı yeniləyir. Sole wire journal retry dəyişməyib. UI/scheduler dəyişməyib.
- Checks performed: Java21/offline core110+engine94+app24=228 unit, failures/errors/skipped0; APK/lint PASS20s, son regression-lardan sonra repeat PASS3s (105up-to-date/108). Son review/commit run-u PASS979ms (107/108up-to-date); 228unit XML nəticəsi reuse. Bugbot və lokal scoped diff review/diffcheck PASS; əlavə kod düzəlişi lazım olmadı. Owner diff-i ilə working-tree yoxlamasıdır. Emulator/device/real-server/render/CI yoxdur.
- Remaining work: connectivity scheduler ayrıca vahiddir. Başqa cihaz hələ aktivdirsə, attempt unknown/abandoned-dırsa və ya öz finish receipt yoxdur/uyğun deyil isə nəticə saxlanır və recovery bloklanır; avtomatik resolution siyasəti uydurulmur. Əvvəlki full parity/runtime məhdudiyyətləri qalır.
- Relevant files: core `AttemptCoordinator.kt`, `GameplayHttpBackend.kt`, `RecoveryDtos.kt`, `GameplayAdaptersTest.kt`; engine `PendingResultReconciler.kt`, `PendingGameResults.kt`, `PendingGameResultsTest.kt`; review `docs/reviews/2026-11-14-account-receipt-reconciliation.md`; yeganə plan `docs/PLAN.md` §§2–4. AppBackend `docs/wobbrik-game-api.md`, `internal/wobbrik/read.go` və `finish.go` read-only yoxlandı; API/schema dəyişikliyi lazım deyil, backend dəyişməyib.
- Exact next step: lokal reconciliation commit nəticəsini bildir. Scheduler və başqa böyük vahid yalnız ayrıca istifadəçi tapşırığı ilə başlayır. Pending-i silmə/restart etmə, owner diff-i qorunsun. Merge/CI/push avtomatik yoxdur.


## Cari davam — network-resilience lokal review/commit (14 noyabr 2026)
- Completed: offline Play/read-cache və pending nəticə diff-i lokal review edildi; yeni konkret bug tapılmadı, production kod dəyişmədi. İstifadəçi həm review/commit, həm ayrıca reconciliation vahidini açıq istədi.
- Current state: `fix/network-resilience`, baza `a2ebcce`; network vahidi `8d7ab4b` lokal commit-ində saxlanıldı. Owner phase4 DetailRow/acceptance/render/handoff və release/oyun QA faylları kənarda qorunur. Merge/push/CI yoxdur.
- Decisions: reconciliation üçün təkrar ümumi icazə istəmə; bu chat review/commit vahidini bitirir. UI dəyişərsə konkret təsdiq/ui-craft sərhədi qalır. Backend dəyişməyib; mövcud account-scoped attempt/receipt GET müqaviləsi var.
- Checks performed: Java21/offline unit/APK/lint PASS1s,107/108up-to-date (əvvəlki218unit reuse); offline host3/3PASS16s; yeni inventory/detailfont2,city390/game360 PNG-lərinə baxıldı; contrast4/4min8.22,diffcheckPASS. Working tree owner diff-i ilə yoxlanıb; isolated commit-only/device/emulator/real-server yoxlaması deyil.
- Remaining work: account/receipt reconciliation və connectivity scheduler implementasiya edilməyib. Başqa cihazın cəhdi avtomatik abandon edilmir; missing-started pending silinmir/restart edilmir. Əvvəlki runtime/full parity məhdudiyyətləri qalır.
- Relevant files: `docs/reviews/2026-11-14-offline-play-read-cache.md` son lokal review; `PendingResultReconciler.kt`, `PendingGameResults.kt`, `PendingGameResultsTest.kt`, `AttemptCoordinator.kt`, `AuthenticatedRecoveryOwner.kt`, `GameplayHttpBackend.kt`, `RecoveryDtos.kt`, `WobbrikApplication.kt`; AppBackend `docs/wobbrik-game-api.md`; yeganə plan `docs/PLAN.md` §§2–4.
- Exact next step: yeni chatdə yalnız bu handoff/review/PLAN və referenced reconciliation fayllərini oxu; backend account-scoped attempt/receipt parser/transport müqaviləsini yoxla, qeyri-müəyyən terminal nəticəni saxlayan reconciliation vahidini implementasiya və fokuslu test et. Başqa-device active attempt üçün avtomatik abandon siyasəti uydurma. Scheduler ayrıca kiçik vahid kimi sərhədlənir. Merge/push/CI yoxdur.


## Cari vahid — offline Play və read-only baxışlar (14 noyabr 2026)
- Completed: `fix/network-resilience` (`a2ebcce` baza) üzərində mövcud PendingGameResults/reconciler yeni sessiyaya bağlı `LocalGameHost` və hazır GameScene/input-a qoşuldu. Yeni Play şəbəkə gözləmir; onlayn halda da start terminal intent-dən sole journal ilə göndərilir. Terminal diskə yazılmayınca retry səhnəsi saxlanır; auth epoch açıq oyunu revoke edir, uğursuz terminal UID üzrə saxlanır. Accepted host-da disk draft re-login itkisi də düzəldi.
- Current state: bu offline Play/cache vahidi branch-də inteqrasiya edilib. Accepted snapshot+kataloq `ReadOnlyAccount` kimi, inventar səhifəsi/detail/şəhər tam raw read paketləri UID+revision üzrə atomik saxlanır. Keş Ready deyil; cached alış/redaktə/silmə bağlıdır. Mövcud server reward/ownership authority, scene artwork və dizayn saxlanır; istifadəçi konkret offline UI qoşulmasını təsdiqləyib.
- Decisions: yeni Play üçün server start nəticə növbəsindən göndərilir; ikinci qeyri-müəyyən start ID yaradılmır. Yarımçıq lokal oyun tərk ediləndə silinir, terminal silinmir. Yalnız əvvəl uğurla oxunmuş uyğun revision-lı baxış açılır. Missing/stale/bounded-limit cache məlumatı uydurmur. AppBackend müqaviləsi yoxlanıldı, API/schema dəyişikliyi lazım deyil; backend dəyişməyib.
- Checks performed: son Java21/offline core109+engine85+app24=218unit, APK/lint PASS (8s, core/engine əvvəlki18s run-dan up-to-date), failures/errors/skipped0. Son3/3production host axını PASS16s:360×640font1/font2 və390×844font1; real Home→Play→Resume, cached view-da0network/POST, mutation disabled. 15render və4reference comparison hazırdır, normal/font2/comparison görüntülərinə baxıldı. Kontrast4/4min8.22 və diffcheckPASS. Emulator/device/real-server E2E/CI/commit/merge/push yoxdur. Owner dirty/release/QA faylları qorunur.
- Remaining work: connectivity scheduler və başqa-device active/missing-started pending üçün account/receipt reconciliation bu vahiddə açılmadı; həmin pending-lər silinmir/restart edilmir. Əvvəl oxunmamış/köhnə revision baxış üçün onlayn refresh lazımdır. Process/hardware itkisi uğursuz disk yazısını xilas etmir. Full design parity/mərhələ qəbulu və device/inset/TalkBack/performance yoxlanmamış qalır.
- Relevant files: `LocalGameHost.kt`, `LocalGameHostTest.kt`, `PendingGameResultsTest.kt`, `AcceptedEngineHost.kt`; core `ReadOnlyGameCache.kt`, `ReadOnlyGameCacheTest.kt`, `AuthenticatedRecoveryOwner.kt`, `RecoveryDtos.kt`, `GameplayHttpBackend.kt`, inventory/city read ports; app `AccountViews.kt`, `AccountViewsTest.kt`, `OnlineGame.kt`, `WobbrikApplication.kt`, `MobileHomeScreen.kt`, `InventoryScreen.kt`, `CityScreen.kt`, `CityChrome.kt`. Review `docs/reviews/2026-11-14-offline-play-read-cache.md`; host/evidence `docs/reviews/offline-views/`. Yeganə plan `docs/PLAN.md` §§2–4.
- Exact next step: bu tamamlanmış vahidin nəticəsini təhvil ver. Yeni chatdə handoff+review oxunur, yalnız istifadəçinin seçdiyi review/commit və ya ayrıca reconciliation vahidi davam edir. Avtomatik merge/CI/push yoxdur; böyük əlaqəsiz feature başlama.


## Cari vahid — durable terminal nəticə recovery (14 noyabr 2026)
- Completed: `fix/network-resilience` branch-də hesab üzrə `PendingGameResults` terminal intent storage və `PendingResultReconciler` əlavə edilib. Mövcud accepted cəhdin terminal draft-ı şəbəkədən əvvəl növbəyə yazılır; restart/retry zamanı checkpoint→finish mövcud sole wire journal-dan keçir. Yeni (hələ UI-a qoşulmamış) nəticələr üçün start→checkpoint→finish dəstəyi var. `onCommitted` receipt-dən sonra, sync/journal remove-dan əvvəl intent-i bağlayır; finish cavab itkisi və crash boşluğu yeni start yaratmır. Köhnə terminal draft yalnız serverin cari active attempt-i üçün import olunur.
- Current state: core çoxsaylı terminal nəticəni saxlayır; **tam tapşırıq bitməyib**. Şəbəkəsiz yeni Play/start, həmin oyunun mövcud scene/input ilə qoşulması, accepted catalog cache və inventory/detail/city read-only cache/routes hələ yoxdur. Recovery yalnız auth/foreground/explicit retry ilə başlayır; connectivity retry scheduler yoxdur. Başqa cihaz active attempt və serverdə artıq olmayan started attempt pending-i saxlayaraq recovery-ni bloklayır; başqa cihazın cəhdi avtomatik abandon edilmir. Bu halların tam account/UI reconciliation axını açıqdır.
- Decisions: yeni wire outbox, wallet, Ready capability, stack və UI/dizayn yoxdur. Növbə terminal intent-dir (checkpoint+parts+stable stage IDs/start marker); server receipt authority qalır. Bounded atomic storage dolarsa/səhv olarsa mövcud nəticələr silinmir və əməliyyat dispatch edilmir. Local storage hardware itkisinə qarşı zəmanət verilmir. AppBackend API/schema dəyişməyib; mövcud receipt və revision müqaviləsi bu vahidə kifayətdir.
- Relevant files: engine `PendingGameResults.kt`, `PendingResultReconciler.kt`, `AcceptedAttemptRestore.kt`, test `PendingGameResultsTest.kt`; core `AttemptCoordinator.kt`, `AuthenticatedRecoveryOwner.kt`; app `AndroidFinishDraftStore.kt`, `WobbrikApplication.kt`; android `AndroidAttemptJournal.kt`. Review: `docs/reviews/2026-11-14-pending-results.md`. Yeganə plan `docs/PLAN.md` §§2–4.
- Checks performed: son Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS11s; 105 core +79 engine +23 app, failures/errors/skipped0. Yeni12 queue regression-u: multiple/reopen, accepted local floors, lost start/finish response, receipt→storage/sync failure, other-device/missing attempt, account switch, revision conflict, corrupt/full storage. Lokal diff review/diffcheck PASS. Emulator/runtime/render/real server E2E/CI/commit/merge/push yoxdur. Əvvəlki owner/release/QA dirty faylları qorunur.
- Remaining work / Exact next step: yeni chatdə yalnız bu handoff və PLAN-ı oxu; queue/reconciler-i təkrar yaratmadan offline Play/start/scene lifecycle-i qoş, account epoch və disk failure zamanı terminal səhnəni retry üçün saxla; sonra accepted catalog+snapshot ilə ReadOnly account model və inventory/detail/city coherent cache/routes əlavə et. Cached model Ready deyil, mutation əməliyyatları bağlı qalmalıdır. Mövcud dizayn dəyişirsə AGENTS konkret UI təsdiqi və ui-craft tətbiq edilir. Başqa-device/missing-started attempt üçün owner-scoped attempts/receipt oxusu lazım olub-olmadığını AppBackend müqaviləsində yoxla; qeyri-müəyyən nəticəni silmə/yeni attempt kimi təkrar mükafatlandırma. Offline yeni oyun və keş baxışları hazır kimi təqdim edilməsin.


## Cari vahid — internet davamlılığı (14 noyabr 2026)
- Completed: handoff, PLAN §§2–3/4/8 və phase4 acceptance oxundu. `fix/network-resilience` branch-i yaradıldı (HEAD baza `a2ebcce`; owner phase4/release/live/QA diff-ləri qorunur). Aktiv Ready cəhdə lifecycle foreground lazımsız sync/Recovering göndərmir; lokal yığılmış mərtəbə və fizika saxlanır. Pause/əl ilə Resume, auth epoch revocation, pending/error recovery və server authority qalır. UI/art/token dəyişməyib.
- Decisions: istifadəçi açıq seçdi: **oflayn nəticələr də sonradan serverə göndərilsin**. Köhnə yalnız mükafatsız məşq qaydası dəyişdi; PLAN §3/4 və context/product-authority güzgüsü yeniləndi. Lokal nəticə server qəbuladək pending-dir; lokal wallet/bina/mükafat təsdiqi yoxdur. İnternet yoxluğunu lokal iş üçün gate etmə; ilk Google identity tələbi qalır.
- Current state: yalnız aktiv accepted cəhdin foreground problemi düzəlib. **Tam offline oyun/nəticə növbəsi və cached inventory/detail/city baxışları HƏLƏ hazır deyil.** Yeni offline cəhd Play-də hələ bloklanır; terminal checkpoint/finish üçün şəbəkə lazımdır. Mövcud tək-command `DurableJournalFile` exact wire outbox olaraq qalır. `FinishDraftStore` terminal draft-dır, çoxsaylı offline nəticə növbəsi deyil. Yeni saxta Ready və ikinci wallet/outbox qurulmayıb.
- Checks performed: `:core:test :engine:test` PASS (105+67, failures/errors/skipped=0); son engine regression-dan sonra repeat PASS8s, core up-to-date. Foreground offline local-floor preservation + explicit resume/frame reset + same-UID re-login stale capability refusal var. Lokal scoped diff review və `git diff --check` PASS. UI/render/runtime/real server E2E yoxlanmayıb; emulator/CI/commit/merge/push yoxdur. Backend dəyişməyib; bu lifecycle fix API/schema tələb etmir.
- Remaining work: hesab üzrə durable pending nəticələr (birdən çox offline cəhd, process death/storage failure); mövcud sole wire journal üzərindən server qəbuluna təhlükəsiz göndərmə (start→checkpoint→finish, lost response, exact retry, revision conflict, başqa cihaz aktiv cəhdi, account/epoch izolyasiyası); server qəbulunda nəticənin bir dəfə bağlanması; accepted catalog/snapshot və inventory/detail/city üçün ayrıca read-only cache və offline route (cache Ready capability deyil). Mövcud dizayn saxlanmalıdır; pending status üçün konkret UI dəyişərsə dizayn qaydasını tətbiq et.
- Relevant files: `docs/PLAN.md`, `docs/reviews/2026-11-14-network-foreground.md`, `docs/reviews/phase4/acceptance.md`; `AuthenticatedRecoveryOwner.kt`, `AttemptCoordinator.kt`, `DiskAttemptJournal.kt`, `SnapshotCache.kt`, `InventoryReads.kt`, `CitySceneReads.kt`; engine `AcceptedEngineHost.kt`, `AcceptedAttemptCommands.kt`, `FinishDraftStore.kt`; app `OnlineGame.kt`, `WobbrikApplication.kt`, `AndroidFinishDraftStore.kt`; AppBackend `docs/wobbrik-game-api.md` və `AGENTS.md`.
- Exact next step: yeni chat-də yalnız bu referenced fayllarla offline nəticənin durable lifecycle/reconciliation vahidini implementasiya et. Mövcud tək journal-a ikinci wire outbox yaratma; draft/command sərhədini aydın saxla. `/sync` yalnız active attempt verir: finish receipt-dən sonra journal silinməsi ilə draft completion arasındakı crash boşluğunu həll etmədən nəticəni yenidən start etmə. Mövcud API ilə atomik/idempotent qəbul mümkün deyilsə AppBackend-in öz handoff/README/AGENTS-ni oxuyub ayrıca branch və müqavilə/test dəyişikliyini et. Backend faylları hələ dəyişməyib. Araşdırma və qərar bu chat-də tamamlandığı üçün AGENTS context qaydasına əsasən yeni böyük offline implementasiya növbəti chat-dədir.

## Əvvəlki vahid — mərhələ 4 yekun regression/qəbul
- Baza: `a2ebcce` (lokal və remote main merge); branch `chore/phase4-acceptance`. İstifadəçi regression+dizayn yoxlaması, problem düzəlişi və qəbul qeydi istəyib; emulator/CI yoxdur. Commit/merge/push bu vahid üçün istənməyib.
- Completed: ilk69host run bir köhnəlmiş Home→City testini tutdu; contentDescription navbar seçimi ilə düzəldi. Silmə dialoqu üçün3ölçüdə native render/ləğv→0submit və detail söz qırılması regression-u əlavə edildi. Dar detail etiket/dəyər paneli token həddi ilə alt-alta keçir; redesign deyil.
- Current state / Checks: final69/69host PASS66s; core104+engine67+app23 unit/APK/lintPASS13s (core cached); son test təmizləməsindən sonra flow3/3PASS18s. Kontrast4/4min8.22,diffcheckPASS. Son detail/dialog və4yanaşı müqayisə şəklinə baxıldı. Qəbul qeydi `docs/reviews/phase4/acceptance.md` və evidence hazırdır. Tam vizual parity açıq fərqlərlədir; tam mərhələ qəbulu iddia edilmir. Owner faylları qorunur; commit/merge/push/CI yoxdur.
- Yeni istifadəçi göstərişi: bundan SONRA zəif/kəsilmiş internet istifadəçini oyun və digər mümkün işlərdən məhrum etməməlidir. Bu, ayrıca növbəti vahiddir; hazır offline implementation sayılmır. Planın server wallet/ownership authority sərhədi saxlanır; offline mükafat/alışı təsdiqlənmiş kimi göstərmə. Aktiv oyun offline pause/abandon qaydası ilə son istək arasında konkret siyasət fərqi implementasiyadan əvvəl dəqiqləşdirilməlidir.
- Relevant files: `InventoryScreen.kt`, `mobile-sizing-visual/render/CompactFlowRenderTest.kt`, `city-scene-visual/render/CityRenderTest.kt`; plan `docs/PLAN.md`.
- Exact next step: acceptance nəticəsini təhvil ver; internet davamlılığını yeni chatdə PLAN §2–3, OnlineGame/AcceptedEngineHost/Room cache/journal əsasında ayrıca kiçik vahid kimi araşdır. Mövcud istifadəçi fayllarını commit-ə qatma; commit/merge/push ayrıca göstərişlə. Zəif internetdə hansı işlərin cached/local davam edə bilməsini müəyyənləşdir, server təsdiqini uydurma.

## Son aktiv vahid — şəhər oxu düzəlişi
- Completed: `ad0a396` bazasından `fix/city-decoration-read`; production transport-un decorations GET yolunu rədd etməsi regression ilə təkrarlandı və düzəldildi. Mobil dizayn dəyişməyib; owner diff/untracked faylları qorunur.
- Current state / Decisions: Lokal review-da əlavə problem tapılmadı; istifadəçi göstərişi ilə iki core faylı və bu vahidin review/handoff qeydi lokal commit-ə hazırlanır. Backend müqaviləsi dəyişmir. Owner faylları commit-dən kənardır; merge/push/CI yoxdur.
- Checks performed: düzəlişdən əvvəl iki regression FAIL; sonra CitySceneReadsTest10/10 və Java21/offline core/app unit+APK+lint PASS13s; diff check PASS. Real hesab E2E/runtime/render yoxdur; emulator açılmadı.
- Remaining work: tarixi screenshot exception logu yoxdur; bu konkret deterministik transport maneəsi aradan qaldırılıb, bütün mümkün server/şəbəkə problemlərinin həlli iddia edilmir. Tam mərhələ4 qəbulu ayrıca açıqdır.
- Relevant files: `GameplayHttpBackend.kt`, `CitySceneReadsTest.kt`, `docs/reviews/2026-11-13-city-decoration-read-fix.md`; plan yalnız `docs/PLAN.md`.
- Exact next step: lokal commit nəticəsini bildir; ayrıca göstərişsiz merge/push/CI və yeni feature başlama. Aşağıdakı qeydlər əvvəlki vahidlərin tarixçəsidir.

## Completed
- Əvvəlki navigation/default kosmetik/mobil portrait vahidi PR #1 ilə remote main-ə merge edilib; lokal/remote main baza `e97e78c` idi.
- Main-dən lokal `feat/phase-4` yaradıldı. İlk mobil oxu vahidi: Home inventar → paginated server inventory → bina detalı → city membership oxusu implementasiya edilib.
- Sakin, base/max dəyər və hissələr serverdən oxunur; preview yalnız accepted geometry və mövcud atlaslarla çəkilir. Legacy/missing art statusu var.
- Session/login epoch və exact Ready izolyasiyası, city mixed-revision rəddi, read DTO regression testləri əlavə edildi.
- İnventar/detail davamında sikkə nişanı, çərçivələr, hissə kartları və v1/server cəmləri ilə tutuşdurulan read-only qiymət bölgüsü əlavə edildi; böyük şrift üçün şaquli detail və scroll tabları var.

- İnventar/detail davamı: binasız şəffaf `inventory_garden.png`, GameScene ilə uyğun preview anchor-ları, üfüqi hissə kartları, dairəvi geri düyməsi və mövcud parlaq HomeButton təqdimatı lokal working diff-dədir. Review: `docs/reviews/2026-11-13-phase4-inventory-garden.md`.

- Hissələr/mağaza və binaya tətbiq working diff-də əlavə edilib: `PartsScreen.kt`, `PartsMutations.kt`, eyni durable journal-a `SHOP_BUY`/`BUILDING_EDIT` route-ları. Review: `docs/reviews/2026-11-13-phase4-parts-flow.md`.

## Current state
- Branch `feat/phase-4`; main merge/push yoxdur. Untracked `android-billing/app/release/` istifadəçi faylları qorunub.
- Review-lar: `docs/reviews/2026-11-13-phase4-inventory-read.md`, `docs/reviews/2026-11-13-phase4-inventory-detail-polish.md`. İlk oxu vahidi implementasiya edilib, Android vizual qəbul/render yoxdur; mərhələ 4 bütöv bitməyib.

## Decisions
- İstifadəçi mərhələ 4 implementasiyasına icazə verdi, AppBackend-ə toxunmağı qadağan etdi. Yalnız server müqavilə/kodu read-only oxundu; server dəyişdirilmədi.
- İstifadəçi inventory-market-v2 inventar və city-completion-v1/04-building-detail görünüşlərini açıq təsdiqlədi. Ehtiyac olsa raster asset hazırlamağa icazə var. `02-parts-flow-v2.png` bu chatdə hissələr/mağaza və binaya tətbiq üçün açıq təsdiqləndi; digər city-completion lövhələri hələ təklifdir.
- Mobil əvvəl, yalnız portret; planşet işi ayrıca göstərişlə. Emulator/canlı yoxlama tələb edilmir, avtomatik açılmır. CI yalnız ayrıca istək ilə main-də.
- Yeganə plan `docs/PLAN.md`; wallet/ownership authority AppBackend qalır. Yeni merge/push göstərişi yoxdur.

## Remaining work
- Bu UI-nin təsdiqli dizayna vizual uyğunluğu və Android renderi yoxlanmayıb. Qiymət breakdown-u əlavə edilib. Ground dressing asset-i və preview anchor düzəlişi əlavə edilib; parlaq çərçivə/ikon eyniliyi və atlas join-lərinin Android render təsdiqi hələ açıqdır; review-da sərhədlər var. Sabit fasadlı game_island accepted geometry-ni pozacağı üçün istifadə edilmədi.
- Hissələr üzrə sikkə alış/edit inteqrasiyası working diff-dədir; tam vizual uyğunluq/render təsdiqlənməyib. Mərhələ 4 qalan vahidlər: delete, placement/dekor/land, real şəhər renderer-i. Başqa lövhələrin konkret təsdiqi uyğun vahiddə lazımdır.
- Mövcud texniki borclar: default kosmetik cihaz üzrədir; nailiyyətlər ümumi hesab şablonudur, online pause restart açıqdır. Bu vahid onları dəyişmir.

## Relevant files
- `docs/PLAN.md` §1 mərhələ 4, §§2/4/6/7; yeni review.
- core `InventoryReads.kt`, `GameplayHttpBackend.kt`, `InventoryReadsTest.kt`, `BuildingValueBreakdown.kt`, `BuildingValueBreakdownTest.kt`.
- app `InventoryScreen.kt`, `OnlineGame.kt`, `HomeScreen.kt`, `WobbrikApplication.kt`, `GameNavigationTest.kt`, `GameTokens.kt`.
- Dizayn: `docs/design/mobile-v1/inventory-market-v2.png`, `docs/design/city-completion-v1/04-building-detail.png`.

## Checks performed
- Hissələr vahidinin son kodunda Java 21 ilə `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (14s); PartsMutationsTest 4/4 PASS. Token kontrastı 4 cüt PASS, minimum 8.22; diff check PASS. Runtime/render/CI/backend testləri edilməyib.
- Java 21 ilə `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS son kodda. InventoryReadsTest 7/7; BuildingValueBreakdownTest 4/4; identity regression mövcuddur.
- Mətn token kontrast cütləri PASS (minimum 8.22 (bu diff-də istifadə edilən ölçülmüş cütlər)), git diff --check PASS.
- CI/backend testləri/runtime/render/emulator/ADB yoxdur. Heç biri PASS kimi təqdim edilmir.

## Exact next step
- Cari working diff-ni `docs/reviews/2026-11-13-phase4-parts-flow.md` ilə review et; birbaşa main merge/push etmə. Əvvəlki inventory garden/polish diff-i və istifadəçi `app/release/` faylları qorunur.
- Hissələrə giriş: İnventar → Hissələr/Mağaza. Tətbiq: bina detalı → Bina görünüşü → kateqoriya → açıq hissə → Tətbiq et. Alış və edit eyni tətbiq-owned durable journal ilə işləyir; ikinci outbox/wallet yoxdur.
- AppBackend dəyişdirilməyib; mövcud `shop/buy` və `buildings/edit` müqaviləsi saxlanılıb. Reklam/real pul axını əlavə edilməyib. Android vizual uyğunluq/runtime/render yoxlanmayıb; emulator avtomatik açılmır, canlı yoxlama gate deyil.
- Əlaqəli yeni fayllar: app `PartsScreen.kt`; core `PartsMutations.kt`, `PartsMutationsTest.kt`; mövcud `AttemptCoordinator.kt`, `GameplayWire.kt`, `GameplayHttpBackend.kt`, `OnlineGame.kt`, `InventoryScreen.kt`.
- Növbəti böyük vahidə keçməzdən əvvəl istifadəçi tapşırığı gözlə; dizaynın renderer olmadan birəbir uyğunluğunu və mərhələ 4 tamamlanmasını iddia etmə. Plan yalnız `docs/PLAN.md`-dir.


## Son review davamı — 13 noyabr 2026
- Current state: `feat/phase-4` / `09185dc`; cari inteqrasiya `PLAN.md` mərhələ 4 ilə tutuşduruldu, implementation dəyişmədi. AppBackend-ə toxunulmadı.
- Completed/Checks: parts wire/receipt/coordinator/domain və UI giriş/çıxış statik review; yeni təsdiqlənmiş bug yoxdur. Java 21 fokuslu core/engine/app unit + assembleDebug/lintDebug BUILD SUCCESSFUL (107/108 UP-TO-DATE); diff check PASS. Runtime/render/CI yoxdur.
- Remaining work: silmə; placement DTO/client və atomik yerləşdirmə/dəyişdirmə/qaytarma; dekor və torpaq alışı; real şəhər renderer-i/əhali/canlılıq. Mövcud parts buy→unlock→apply→detail tam axınının regression yoxlaması və mobil vizual müqayisə açıqdır.
- Decisions: reklam/billing mərhələ 6, bazar/leaderboard mərhələ 5; planşet/landşaft hazırkı iş deyil. Yeni feature, merge/push başlamadı.
- Relevant files/Exact next step: `docs/reviews/2026-11-13-phase4-parts-flow.md` son bölməsi; növbəti review vahidi üçün `PartsMutationsTest.kt` və `PartsScreen.kt` əsasında uğurlu buy/apply və rədd ssenarilərinin regression əhatəsini tamamlamaq; yeni böyük vahid yalnız istifadəçi tapşırığı ilə.


## Son istifadəçi qərarı və silmə vahidi — 13 noyabr 2026
- Decisions: istifadəçi şəhərə yerləşdir/yerini dəyiş/inventara qaytar/atomik dəyiş və əyrilik/sahə yoxlamaları bəndini bağlı saymağı, qalan işləri davam etdirməyi tapşırdı. `PLAN.md` §1-də qeyd edildi. Bu, test/implementasiya PASS iddiası deyil; əvvəlki placement TODO-larını cari tapşırıq kimi götürmə. AppBackend-ə yazı qadağası qalır.
- Completed: bina silmə core adapteri, wire/HTTP route, receipt validation və mövcud durable journal recovery inteqrasiyası working diff-dədir. UI hələ qoşulmayıb. Review: `docs/reviews/2026-11-13-phase4-building-delete.md`.
- Current state: `feat/phase-4` / `09185dc`; əvvəlki diff/release faylları qorunur. `BuildingDeletion.kt`, `BuildingDeletionTest.kt` yenidir; PartsMutationsTest stale Ready testi delete üçün də genişləndi. Backend yalnız read-only müqavilə/kod yoxlaması, dəyişiklik yoxdur.
- Checks performed: Java21 core/engine/app unit + assembleDebug/lintDebug PASS; son test əlavəsindən sonra core:test yenidən PASS; diff check PASS. Kontrast 3 cüt PASS (min8.22). Android runtime/render/CI yoxdur.
- Remaining work: silmə UI təsdiqi və qoşulması; dekor/torpaq; real şəhər renderer-i və V1 canlılıq; mövcud parts axınının tam regression/vizual uyğunluğu. Bütün mərhələ bitmiş sayılmır.
- Relevant files: `BuildingDeletion.kt`, `BuildingDeletionTest.kt`, `GameplayWire.kt`, `GameplayHttpBackend.kt`, `InventoryScreen.kt`, `GameTokens.kt`; təsdiqli `docs/design/city-completion-v1/04-building-detail.png`.
- Exact next step: istifadəçiyə delete confirmation nümunəsini göstər və konkret təsdiqini al (AGENTS UI qapısı). Silmə girişinin görünüşü də təsdiqlənməlidir. Sonra həmin dizayn əsasında InventoryScreen-ə aydın təsdiqli silmə axınını qoş, server uğurundan sonra inventarı yenidən oxu. AppBackend yazısı/CI/merge/push etmə.


## Silmə UI təsdiqi və inteqrasiya — 13 noyabr 2026
- Decisions: istifadəçi “okeydir” ilə `delete-confirmation-proposal.png` dialoqunu və detail sonunda “Binanı sil” girişini açıq təsdiqlədi. Yenidən təsdiq istəmə.
- Completed/Current state: `InventoryScreen.kt`-də silmə girişi və təsdiqli dialoq core `BuildingDeletion`-a qoşuldu. Ləğv et/outside/Back dispatch etmir; yalnız “Binanı sil” təsdiqi exact Ready ilə submit edir. Dispatch zamanı köhnə detail/list seçimindən çıxır, növbəti Ready-də inventory serverdən yenidən oxunur; lokal wallet/refund və optimistik ownership silinməsi yoxdur. Böyük şriftdə dialoq düymələri şaqulidir, izahat scroll edə bilir.
- Relevant files: `InventoryScreen.kt`, `BuildingDeletion.kt`, `BuildingDeletionTest.kt`, `docs/reviews/2026-11-13-phase4-building-delete.md`; `docs/design/city-completion-v1/README.md` təsdiq statusu yeniləndi.
- Remaining work: dekor/torpaq və real şəhər renderer-i; parts tam axın regression/vizual uyğunluğu. Placement bağlıdır. Android silmə UI render/runtime yoxlanmayıb; birəbir vizual PASS yoxdur. Emulator avtomatik açılmır.
- Exact next step: yeni chatdə handoff və `docs/PLAN.md` oxu; dekor vahidi üçün mövcud `docs/design/city-completion-v1/03-decoration-flow.png` dizaynını göstər və konkret təsdiq al; təsdiqədək UI başlama. AppBackend dəyişmə/CI/merge/push yoxdur.

- Son UI kodunda Checks performed: Java21 `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` BUILD SUCCESSFUL (52s; 15 executed,91 up-to-date), `git diff --check` PASS. CI/merge/push yoxdur.

## Dekor/torpaq alış vahidi — 13 noyabr 2026
- Completed: əvvəlki inventory/parts/delete diff artıq `c6c51de` commit-indədir. Dekor/torpaq alış adapteri və UI girişləri əlavə edildi; dekor copy inventory serverdən paginated oxunur. Alış mövcud SHOP_BUY + eyni durable journal/recovery/sync ilədir.
- Current state: `feat/phase-4`; release APK/metadata qorunur və commit-ə daxil edilmir. Placement bağlıdır, AppBackend yazısı yoxdur.
- Decisions: istifadəçi dekor dizaynını və torpaq siyahısı/alış təsdiqini açıq təsdiqlədi. Torpağın map selection hissəsi yoxdur, əməl “Al”dır. Mövcud dəyişiklikləri uyğun commit etmək açıq istənildi.
- Remaining work: Android mobil portret/böyük şrift/inset/a11y renderləri və dizaynla müqayisə açıqdır; art crop/halo/native-pixel hədləri review-da qeyd olunub. Real şəhər renderer-i/V1 canlılıq və əvvəlki parts axınının vizual qəbulu qalır; mərhələ bütöv bitməyib.
- Relevant files: `CityPurchases.kt`, `DecorationReads.kt`, `CityPurchasesTest.kt`, `PartsMutationsTest.kt`; app `DecorationShopScreen.kt`, `LandShopScreen.kt`, `InventoryScreen.kt`, `OnlineGame.kt`, `WobbrikApplication.kt`; `docs/reviews/2026-11-13-phase4-city-purchases.md`; yeganə plan `docs/PLAN.md`.
- Checks performed: core unit testlər (XP/balans/daimi hüquq/təkrar copy, malformed pages/receipt, lost-response disk recovery, stale Ready) PASS; core/app unit + assembleDebug/lintDebug PASS ilk UI kodunda. Son UI əlavələrindən sonra eyni yoxlamalar yenidən başladılıb; son nəticə review qeydinə əlavə edilir. Kontrast 4 cüt PASS min8.22; diff check PASS. Android runtime/render/backend test/CI yoxdur.
- Exact next step: son fokuslu yoxlamanın nəticəsini review et, bu alış vahidini ayrıca commit et; main merge/push/CI yoxdur. Sonra yalnız cari alış vahidinin açıq vizual uyğunluq işini davam etdir; placement-i yenidən açma və AppBackend-ə toxunma. Böyük yeni şəhər renderer vahidi bu chatə yığılmamalıdır.
- Son nəticə: Java21 core/app unit + assembleDebug/lintDebug PASS (11s); CityPurchasesTest4/4, PartsMutationsTest4/4; diff check PASS. Alış vahidi lokal commit üçün review edilib; runtime/render PASS yoxdur.

## Dekor/torpaq vizual davamı — 13 noyabr 2026
- Completed: alış vahidi artıq `faf4af7` commit-indədir. Cari working diff alış təsdiqlərini ortaq `CityPurchasePanel.kt` çərçivəsinə keçirir; torpaq xülasəsini siyahı scroll-undan ayırır, daimi hüquq mətni əlavə edir. Düymələr normal şriftdə yanaşı, böyüdülmüş şriftdə şaqulidir; izahat ayrıca scroll edir.
- Current state: `feat/phase-4` / `faf4af7`; bu davam commit edilməyib. Release istifadəçi faylları qorunur.
- Decisions: əvvəlki dekor və torpaq alış dizayn təsdiqləri qüvvədədir. Placement bağlıdır, AppBackend-ə toxunulmur; emulator açılmır, CI/merge/push yoxdur.
- Remaining work: Android render və dizaynla birəbir uyğunluq açıqdır. Crop/halo/native-pixel keyfiyyəti, kart/ikonlar, kiçik/normal portret font1/2, inset/a11y yoxlanmayıb. Yeni şəhər renderer vahidi başlanmayıb.
- Relevant files: `CityPurchasePanel.kt`, `DecorationShopScreen.kt`, `LandShopScreen.kt`; `docs/reviews/2026-11-13-phase4-city-purchases.md`; `docs/design/city-completion-v1/{03-decoration-flow,05-land-purchase}.png`; yeganə plan `docs/PLAN.md`.
- Checks performed: Java21 `:app:compileDebugKotlin :app:lintDebug` PASS (11s), diff check PASS, 3 kontrast cütü PASS (min8.22). Android runtime/render yoxdur; host screenshot infrastrukturu layihədə yoxdur. Build vizual qəbul deyil.
- Exact next step: cari kiçik panel diff-ni review et; növbəti uyğunluq işində əvvəl host Android render imkanını müəyyənləşdir (emulator avtomatik açma, yeni stack pinləri uydurma). Render mümkün olmazsa məhdudiyyəti saxla; təsdiqli art-ı özbaşına dəyişmə, placement və AppBackend işini açma.

## Kart/ikon/dekor crop davamı — 13 noyabr 2026
- Completed: dekor kartları/qiymət pill-i/cyan kontur, kateqoriya və alış vektor ikonları, alış panelində art+divider+izahat, torpaq kartlarının yığcam/detallı görünüşü və adaptiv başlıq cari working diff-dədir. `CityPurchaseArt.kt` + `purchase-art.json` 12 ölçülmüş crop və native-pixel scale≤1 verir; orijinal dekor atlası dəyişməyib.
- Current state: `feat/phase-4` / `faf4af7`; bu davam hələ commit edilməyib. Əvvəlki panel diff-i daxildir. `app/release/` istifadəçi faylları qorunub.
- Decisions: placement bağlıdır, AppBackend yazısı yoxdur. Əvvəlki dekor/torpaq alış təsdiqləri qüvvədədir. Yeni şəffaf torpaq cutout namizədi göstərildi; orijinaldan xırda detallar fərqləndiyi üçün ayrıca tətbiq təsdiqi async soruşuldu, cavab HƏLƏ YOXDUR. Təsdiqsiz tətbiq etmə.
- Remaining work: aktiv torpaq 153×141px crop-un tünd düzbucaqlı matte-i qalır. `docs/reviews/city-purchases-visual/land-thumbnail-candidate.png` bunun şəffaf yüksək çözünürlüklü namizədidir, app assets-də deyil. Tam piksel eyniliyi/mərhələ qəbulu iddiası yoxdur; server nüsxə/status mətnləri, tall portrait spacing və mövcud inventory return naviqasiyası review-da qeyd olunub. Real inset/TalkBack/cihaz runtime açıqdır.
- Relevant files: `DecorationShopScreen.kt`, `LandShopScreen.kt`, `CityPurchase{Art,Chrome,Panel}.kt`, `assets/city/purchase-art.json`, 6 city vector drawable; `scripts/check-city-purchase-art.py`; review `docs/reviews/2026-11-13-phase4-city-purchases.md` son bölmə; render evidence/harness `docs/reviews/city-purchases-visual/`; yeganə plan `docs/PLAN.md`.
- Checks performed: 6/6 host native Compose render ssenarisi PASS, 12 list/confirmation PNG-yə və design comparison-lara baxıldı: dekor/torpaq 360×640dp font1/2 və390×844dp font1, density3/API34. 12/12 source crop QA PASS, tünd/açıq fonlarda baxıldı. Normal Java21 core/app unit + assembleDebug/lintDebug PASS (15s); kontrast5cüt PASS(min8.22); diff check PASS. Emulator açılmadı; CI/backend test/merge/push yoxdur. Host harness production build pinlərini dəyişmir.
- Exact next step: istifadəçinin torpaq cutout təsdiq cavabını yoxla. Təsdiq varsa namizədi app city assets-ə ayrıca adla köçür, JSON-dakı land bounds-u aktiv metadata-ya əlavə et, `rememberLandArt`-ı ona keçir və host land render/build/lint-ni yenilə. Təsdiq yoxdursa orijinalı saxla, namizədi tətbiq etmə. Bu chatdə başqa böyük vahid açma; yeni chat üçün bu handoff/review və PLAN.md kifayətdir.

## Son qərar — torpaq cutout-u tətbiq edildi
- Completed/Decisions: istifadəçi “Bəli, şəffaf cutout-u tətbiq et” ilə namizədi açıq təsdiqlədi. `city/land-thumbnail-v2.png` app assets-ə dəyişdirilmədən kopyalandı; `purchase-art.json.land` və `rememberLandArt()` ilə siyahı+alış panelinə qoşuldu. Əvvəlki “cavab yoxdur/aktiv matte qalır” qeydləri artıq cari vəziyyət deyil.
- Checks performed: torpaq3/3 host render PASS(15s); altı yeni land list/confirmation PNG-si/contact sheet-ə və tünd/açıq fon cutout QA-sına baxıldı, matte yoxdur. Alpha/bounds QA PASS; Java21 assembleDebug/lintDebug PASS(17s), diff check PASS. Əvvəlki unit/kontrast nəticələri qüvvədədir; domain/rəng tokenləri dəyişməyib.
- Current state: `feat/phase-4` / `faf4af7`, bütün vizual davam working diff-dir, commit/merge/push yoxdur. Placement bağlıdır, AppBackend dəyişmir. CI/emulator yoxdur. Məhdudiyyətlər əvvəlki bölmədə/review-da qalır; tam mərhələ qəbulu deyil.
- Relevant files: CityPurchaseArt.kt; assets/city/{purchase-art.json,land-thumbnail-v2.png}; scripts/check-city-purchase-art.py; docs/reviews/city-purchases-visual/ və 2026-11-13-phase4-city-purchases.md son bölmə.
- Exact next step: bu kiçik alış vizual diff-inin review/qəbulunu yekunlaşdır; yeni feature/şəhər renderer-i başlama. Cutout üçün yenidən təsdiq istəmə. Növbəti böyük vahid yeni chat və ayrıca istifadəçi tapşırığı ilədir.

## Commit təhvil qeydi — 13 noyabr 2026
- İstifadəçi bu vizual alış vahidini commit etməyi tapşırdı. Kart/ikon/panel, 12 dekor crop-u, təsdiqli şəffaf torpaq cutout-u və review/render sübutları eyni lokal commit-ə daxil edilir. Əvvəlki “commit edilməyib” qeydləri bu commit-dən əvvəlki tarixçədir.
- Branch `feat/phase-4`; review bazası `faf4af7`. Son uyğun yoxlamalar yuxarıda qeyd edilib; commit öncəsi diff check PASS. Owner `app/release/` faylları xaricdə saxlanır. Merge/push/CI yoxdur.
- Növbəti addım: yalnız istifadəçinin ayrıca göstərişi ilə review/qəbul və ya növbəti vahid; placement/AppBackend açılmır.

## Şəhər renderer-i başlanğıcı — 13 noyabr 2026 (cari)
- Decisions: istifadəçi göstərilmiş `city-view.png` və V1 canlılığı “basla” ilə təsdiqlədi. Yenidən eyni təsdiqi istəmə. İstifadəçinin öz tikdiyi/yerləşdirdiyi binalar həqiqi mərtəbə sayını, əyriliyini və hissələrini saxlamalıdır. Placement bağlı qalır.
- Decisions (əvvəlki backend qadağasını əvəz edir): AppBackend-də ehtiyac olan dəyişikliklərə icazə var; onun main-indən ayrıca branch aç, worktree yaratma. Bu oxu vahidində backend dəyişikliyi tələb olunmadı və edilmədi.
- Completed: `CitySceneReads.kt` server parcel/position oxusu, paginated city + building detail + placed decoration səhnə yığımı; revision bracket/mixed-page rəddi, legacy null mövqe, canonical tick yoxlamaları. `WobbrikApplication.Configured.cityScene` eyni auth backend-ə qoşulub.
- Current state: `feat/phase-4`, baza artıq `bb02b50` (əvvəlki alış vizualı commit olunub); yeni scene reader/test/bootstrap diff-i commit edilməyib. İstifadəçi release faylları qorunur. Ekran/renderer/animasiya hələ əlavə edilməyib.
- Checks performed: Java21 core:test PASS; CitySceneReadsTest7/7. Son bootstrap compile/lint nəticəsi review-da. UI/render/runtime/CI/backend test yoxdur.
- Relevant files: core `CitySceneReads.kt`, `CitySceneReadsTest.kt`; app `WobbrikApplication.kt`, `InventoryScreen.kt` (`BuildingPreview`), `CosmeticArt.kt`, `GameTokens.kt`; review `docs/reviews/2026-11-13-phase4-city-scene-reads.md`; dizayn `docs/design/city-view.png`; yeganə plan `docs/PLAN.md` §§1/4/7.
- Remaining work/Exact next step: həmin məlumat modelindən real şəhər səhnəsini çək: əvvəl mövcud GameScene/cosmetic atlasları və native crop/anchor hədlərini yoxla, accepted floor geometry + server x/y/quarter-turn projection/depth ordering qur, real parcel union-una ada səthi oturt; sonra təsdiqli şəhər UI/bina seçimi və V1 canlılıq. Şəhəri sabit hazır raster və ya sadə placeholder bina ilə əvəz etmə. Asset/kamera məhdudiyyəti təsdiqli görünüşə mane olarsa açıq göstər, özbaşına alternativ tətbiq etmə. Host Compose render harness-i `docs/reviews/city-purchases-visual/`-dədir; emulator açma. Merge/push/CI göstərişi yoxdur.

- Son yoxlama: Java21 `:core:test :app:compileDebugKotlin :app:lintDebug` PASS (28s); `git diff --check` PASS. UI/render/CI yoxdur.

## Renderer asset sərhədi — 13 noyabr 2026
- Completed: istifadəçinin cari tapşırığı üzrə handoff/PLAN/CitySceneReads və mövcud BuildingPreview/atlas anchor-ları yoxlandı; city-view, wall atlas və land cutout source şəkillərinə baxıldı. Review: `docs/reviews/2026-11-13-city-renderer-art-boundary.md`.
- Current state: renderer/UI kodu əlavə edilməyib; əvvəlki scene-reader/bootstrap owner diff və release faylları qorunur. Placement bağlıdır.
- Açıq texniki sərhəd: atlas hər hissə üçün bir baxış sprite-idir; quarter-turn/kamera dönüşü üçün səth və əlavə baxış məlumatı yoxdur. Alış land cutout-u real parcel union-u deyil və sabit ağaclar daşıyır. Bunları təsdiqli şəhər səhnəsinin tam əvəzi sayma.
- Checks: source inspeksiyası; yeni build/render/CI/backend yoxlaması yoxdur. Android vizual PASS yoxdur.
- Exact next step: mövcud atlaslarla ilkin sabit bucaq təqdimatına istifadəçi razılığını və ya modul asset istiqamətini dəqiqləşdir; bundan əvvəl sadələşdirilmiş şəhər görünüşü tətbiq etmə. Real binaların mərtəbə sayını, qəbul edilmiş ofsetlərini, hissələrini və server mövqeyini saxla; legacy null mövqeni uydurma.

## Cari handoff — sabit şəhər bina qatı və növbəti oyun UI fix-i
- Completed: istifadəçi sabit kameranı “beli” ilə təsdiqlədi. `CityScreen.kt` real scene repository-yə qoşuldu; `CityBuildingProjection.kt` server position/quarter-turn və accepted mərtəbə ofsetlərini qoruyur, depth sort/bounds/focus verir. Server parts mövcud atlasdan, native pixel cap ilə çəkilir. `BuildingArt.kt` inventarla ortaq anchor helper-dir. Home mobil şəhər girişi açılıb. Placement bağlıdır, backend dəyişmədi.
- Current state: `feat/phase-4`, baza `bb02b50`; istifadəçi cari vahidi bitəndə commit etməyi tapşırdı. Scene reader/bootstrap daxil cari vahid commit edilir; release owner faylları xaricdədir. Merge/push/CI yoxdur.
- Decisions: sabit kamera qəbul edilib; yenidən təsdiq istəmə. Tam city-view uyğunluğu iddia edilmir: terrain parcel union/dekor/tam chrome/orbit/V1 canlılıq açıqdır. Əvvəlki art-boundary blok sualı bağlandı.
- Checks performed: 6/6 host native Compose render testi +5/5 projection unit PASS; 9 screenshot/contact/design comparison açılıb baxıldı. 360×640 font1/2,390×844 font1, overview/focus, empty/error və Home entry/click. Normal Java21 core/engine/app unit +assembleDebug/lintDebug PASS(24s); 4 kontrast cütü PASS(min8.22),diff check PASS. Real hesab/device/inset/TalkBack/backend test/CI/emulator yoxdur.
- Relevant files: `CityScreen.kt`, `CityBuildingProjection.kt`, `BuildingArt.kt`, `HomeScreen.kt`, `OnlineGame.kt`, `InventoryScreen.kt`; reader/bootstrap; `docs/reviews/2026-11-13-phase4-city-buildings.md`, `docs/reviews/city-scene-visual/`; yeganə plan `docs/PLAN.md`.
- Remaining work / Exact next step: istifadəçi commit-dən sonra ayrıca aktiv oyun UI problemini düzəltməyi tapşırdı. Screenshot: `/var/folders/02/vb81250n1ld9gwqlq74ybm000000gn/T/codex-clipboard-22dc0624-dab8-41b9-b618-d23b60a3198e.png`. Mərtəbə panel seams/tünd boşluqlar, game island-da hazır ev üzündən ikinci baza/giriş, aktiv oyunda dam, kəndir-floor anchor ayrılması. `GameScene.kt` oxundu, aktiv roof çağırışı təsdiqləndi; dəyişdirilməyib. Yeni chatdə ui-craft/token/game-tap-v2 və screenshot-u oxu, mövcud təsdiqli istinadı təqdim et, konkret fix UI təsdiqini mövcud söhbət göstərişi ilə tutuşdur; fizika/accepted geometry-ni dəyişmədən anchors/crops/layering/roof phase düzəlişini hazırla. Yeni dizayn alternativi uydurma, emulator/CI açma. Başqa şəhər feature-i başlama.

## Active game joins + two atlas proposals — 13 November 2026
- Completed: separate `fix/game-scene-joins` from `e308d62`. GameScene visual pitch/full-width wall drawing, building-free garden in both callers, ground-only entrance, active roof removal and cable anchors/framing corrected. Physics/SceneMotion/accepted geometry/backend unchanged. Review `docs/reviews/2026-11-13-game-scene-joins.md`.
- Current state: working diff, no commit/merge/push/CI. Owner `app/release/` intact. Existing shallow wall art cannot satisfy full game-tap-v2 geometry; do not claim design acceptance.
- Decisions: user explicitly permits old atlas removal if needed and asks TWO complete color sets, same game-tap-v2 style/forms, mainly colors changed. Built-in imagegen produced purple/pink and teal/ivory proposals; no asset approval yet, not installed. Do not request generation scope again.
- Checks performed: 7 native host production-Canvas render fixtures PASS; final Java21 assembleDebug/lintDebug +engine:test PASS(24s), diff check PASS. Final screenshots viewed; no emulator/device/account/HUD/font/inset/a11y/CI/backend checks. PNG alpha exists; generated exterior haze still requires cleanup.
- Relevant files: GameScene.kt, MainActivity.kt, OnlineGame.kt; `docs/reviews/game-joins-visual/render/`; `docs/design/game-tap-v2/atlas-v3-proposal/{purple,teal}.png` and README (prompt set/limitations); plan only `docs/PLAN.md`.
- Remaining work: concrete style approval; clean alpha + matching crop/anchor geometry, independent-part layering compatibility without breaking 68-item ownership catalog; then install approved assets and render seams/ropes/ground/finish. Fallback lacks a dedicated entrance; new ground asset must resolve it. No old atlas deletion before consumer/ID compatibility checked.
- Exact next step: show the two saved proposals and receive visual approval under AGENTS; then continue only this game-scene/atlas integration in new chat. No new city feature, automatic emulator, CI or merge/push.

## Atlas təmizləmə davamı — 13 noyabr 2026
- Decisions: istifadəçinin “təsdiqlədiyim iki atlası təmizləyib GameScene-ə qoş, fizikanı dəyişmə” göstərişi purple/teal dizayn təsdiqidir; yenidən vizual təsdiq istəmə.
- Checks performed: handoff/PLAN/ui-craft/imagegen, orijinal iki atlas, GameScene/CosmeticSprite loader və mövcud host render harness oxundu. Built-in imagegen ilə purple iki dəfə, teal bir dəfə background-extraction sınandı; çıxışlarda xarici halo qalır, app-ə tətbiq edilmədi. Yeni kod/build/render/CI yoxdur; əvvəlki working diff qorunur.
- Current state/Remaining work: imagegen alternativ redaktə üsulu üçün açıq istifadəçi seçimi tələb etdiyindən kontur maskası ilə skript təmizliyinə async icazə sualı göndərildi; cavab gözlənilir. Asset inteqrasiyası başlanmayıb. Fizika/backend dəyişməyib.
- Exact next step: kontur maskasına cavabı yoxla; icazə varsa orijinal iki atlasın daxili piksellərini saxlayaraq xarici halo-nu təmizlə, crop/anchor metadata və GameScene render qoşulmasını hazırla. Tam kompozit sprite-lərin pəncərə/giriş/lampa daşıdığını nəzərə al: duplicate overlay etmə, qarışıq 68 hissə kataloqunu səssiz əvəz etmə; köhnə atlasları compatibility sübutu olmadan silmə. Host production Canvas renderində iki rəng, seams, swing/ropes, ground və finish-i yoxla; emulator/CI açma.

## Cari nəticə — təsdiqli iki atlas GameScene-də
- Decisions: istifadəçi kontur maskası ilə skript təmizliyinə açıq icazə verdi; əvvəlki gözlənilən cavab qeydi bağlanıb. Purple/teal təsdiqlidir.
- Completed: orijinal RGB-ni dəyişmədən xarici alpha halo maskalandı; `assets/game-v3/{purple,teal}.png` + crop/pitch/depth/top-anchor metadata, `GameAtlas.kt` loader və GameScene/both callers inteqrasiyası hazırdır. Giriş yalnız accepted foundation-da, dam yalnız finish-də; composite-da duplicate overlays yoxdur. Renderdə tapılan kəndir ucu/guide plane ölçülmüş anchor-larla düzəldildi.
- Current state: `fix/game-scene-joins`, baza `e308d62`, working diff; commit/merge/push/CI yoxdur, owner app/release qorunub. Purple partless/local və tam base_1; teal tam base_2; qarışıq/digər 68-item seçimləri köhnə independent art-da qalır. Köhnə atlaslar silinməyib. Fizika/SceneMotion/accepted geometry/core/backend diff-i yoxdur.
- Checks performed: 12/12 native host Canvas render PASS, iki rəng dark/light alpha QA və final contact/design comparison görüntülərinə baxıldı. 360×640dp və390×844dp; straight/swing/empty/finish/one floor/40 floors/mixed catalog. Java21 offline engine:test +app:testDebugUnitTest +assembleDebug +lintDebug PASS(12s); diff check PASS. UI rəng/mətn tokenləri dəyişmədiyindən kontrast təkrarlanmadı.
- Remaining work/limits: tam phone.png uyğunluğu iddia edilmir; framing/rope length/background/HUD/garden fərqləri qalır. Picker/inventory/city 68-item art migrasiyası bu vahiddə deyil. Full HUD/font2/inset/a11y/live/device/account yoxlanmayıb, emulator/CI açılmayıb. Bunlar runtime PASS deyil və avtomatik gate yaratmır.
- Relevant files: `GameAtlas.kt`, `GameScene.kt`, `MainActivity.kt`, `OnlineGame.kt`, `assets/game-v3/`, `scripts/clean-game-atlases.py`; review `docs/reviews/2026-11-13-game-scene-joins.md`, `docs/reviews/game-joins-visual/atlas-integration-contact.png`; dizayn `docs/design/game-tap-v2/atlas-v3-proposal/`; plan yalnız `docs/PLAN.md`.
- Exact next step: cari atlas inteqrasiyasını istifadəçiyə təhvil ver; ayrıca göstəriş olmadan yeni feature/art migrasiyası/merge/push/CI başlama. Mövcud source və köhnə catalog atlases-i saxla.

## Atlas review və lokal commit — 13 noyabr 2026
- Completed/Decisions: istifadəçi review və problem yoxdursa lokal commit tapşırdı; merge/push/CI qadağası saxlanır. `e308d62` bazasına qarşı atlas/scene/callers/asset/metadata/script/harness diff-i lokal review edildi; yeni production bug tapılmadı.
- Fixes: yalnız host QA-da hissəsiz lokal default fixture-i düzəldildi, maksimal yellənmə fixture-ləri artıq sıfır bucaq deyil, 90 frame settle edilmiş yük bucağını yoxlayır. Fizika/engine/core/SceneMotion dəyişmir.
- Checks performed: düzəldilmiş son 12 host render 12/12 PASS(15s,0 skips/failures/errors); iki rəngin final rotated swing və local-default PNG-lərinə baxıldı. Production dəyişmədiyi üçün əvvəlki final unit/APK/lint PASS qüvvədədir; staged diff check PASS. Runtime/a11y/CI yoxdur.
- Current state: `fix/game-scene-joins`; bu review və atlas vahidi uyğun lokal commit-ə daxil edilir. Owner app/release xaricdə, PNG render evidence mövcud .gitignore üzrə lokal qalır; source/production atlaslar və harness versionlanır. Merge/push/CI yoxdur.
- Remaining work: əvvəlki tam ekran/runtime/a11y və full design parity məhdudiyyətləri qalır; yeni feature və 68-item migrasiya başlanmadı.
- Relevant files: əvvəlki atlas vahidi + `docs/reviews/2026-11-13-game-scene-joins.md` son review bölməsi, `docs/reviews/game-joins-visual/render/GameJoinsRenderTest.kt`; plan `docs/PLAN.md`.
- Exact next step: lokal commit nəticəsini istifadəçiyə bildir; ayrıca göstəriş olmadan merge/push/CI və yeni böyük vahid başlama.

## Cari vahid — şəhər torpaq/dekor — 13 noyabr 2026
- Decisions: istifadəçi `docs/design/city-view.png` əsasında torpaq/dekor işini açıq təsdiqlədi; kamera sabitdir. Əlavə asset-sərhədi sualına “Bəli, mövcud baxışla davam et” cavabı ağac/skamya/fənər billboard-larını təsdiqləyir; quarter-turn onların görünüşünü dəyişmir. Yollar real müstəvidə dönür. Yenidən təsdiq istəmə.
- Completed: server scene parcels → exact rect/L union → materiallı ot/torpaq ekstrüziyası; serverdə yerləşmiş 12 dekor növü atlasdan; yollarda affine quarter-turn; ortaq bina/dekor painter order, terrain/decor viewport fit, binasız şəhər və semantics sayı. Mövcud exact Ready oxu axını qorunur; placement/mutation yoxdur. Imagegen materialı production assets-ə saxlandı.
- Current state: `feat/city-terrain-decor`, baza `937f5ea`, working diff. Commit/merge/push/CI yoxdur; owner `android-billing/app/release/` qorunur. Backend read-only footprint müqayisəsi edildi, server dəyişikliyi lazım deyil və edilmədi.
- Checks performed: final host 24/24 test PASS (9 terrain native render/union, 6 city regression, 4 terrain geometry, 5 building projection). 10 yeni screenshot və contact/design comparison açılıb baxıldı; 360×640 font1/2,390×844 font1, torpaq-only/decor-only, 6 parcel/rotated L, 12 dekor/4 yol dönüşü, focus/return. Semantics getter exception və material sərhəd piksel xətti düzəldildi. Normal final unit/APK/lint nəticəsi review-da qeyd edilir. Real hesab/device/inset/TalkBack/emulator/CI/backend test yoxdur.
- Remaining work: tam konsept uyğunluğu yoxdur — həqiqi düz parcel kənarları konseptin yumru/dekorativ sahilinə bərabər deyil; mövcud bina artı, tam chrome və V1 canlılıq qalır. Təsdiqli upright single-view məhdudiyyəti saxlanır. Bu, mərhələ 4 qəbulu deyil. Fizika/accepted geometry dəyişməyib.
- Relevant files: `CityScreen.kt`, yeni `CityTerrainGeometry.kt`, `CityTerrainArt.kt`, `CityTerrainGeometryTest.kt`; `assets/city/terrain-material-v1.png`; `docs/reviews/2026-11-13-phase4-city-terrain-decor.md`; `docs/reviews/city-scene-visual/render/CityTerrainRenderTest.kt`; `docs/reviews/city-terrain-visual/`; yeganə plan `docs/PLAN.md`.
- Exact next step: cari torpaq/dekor diff-ni istifadəçiyə təhvil ver və lokal review nəticəsini göstər; ayrıca göstəriş olmadan yeni feature, merge/push/CI başlama. Təsdiqlənmiş dizayn/sabit kamera/billboard qərarlarını yenidən soruşma. Tam şəhər uyğunluğu iddia etmə.
- Son yoxlama: Java21 normal `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (11s); final host 24/24 PASS (15s); lokal diff review və `git diff --check` PASS. Commit yoxdur.

## Lokal review/commit və növbəti iş
- Completed: istifadəçinin review → problem yoxdursa lokal commit göstərişi əsasında `937f5ea` bazalı bütün torpaq/dekor diff-i yenidən review edildi; yeni production bug tapılmadı, kod dəyişmədi. Contact sheet yenidən vizual yoxlandı; diff check PASS. Əvvəlki final test nəticələri qüvvədədir.
- Current state: `feat/city-terrain-decor`; torpaq/dekor vahidi lokal commit edilir. Owner `app/release/` xaricdədir; merge/push/CI qadağası qalır.
- Decisions: istifadəçi sonra digər işə keçməyi də tapşırdı. Mövcud city-view/sabit kamera/billboard təsdiqləri saxlanır; təkrar seçim sualı yoxdur.
- Remaining work / Exact next step: növbəti kiçik vahid şəhərin mövcud təsdiqli dizayna uyğun başlıq və aşağı naviqasiya/“Binaya bax” panelinin tamamlanmasıdır; placement/market endpoint-ləri bu vahidə uydurulmur, kamera sabit qalır. Sonra ayrıca V1 canlılıq. AGENTS context-growth qaydasına görə bu chatdə ikinci böyük implementasiya başlamır; yeni chatdə buradan davam et.
- Relevant files: `docs/PLAN.md` §§1/4/7/9; `docs/design/city-view.png`; `CityScreen.kt`, `GameTokens.kt`, `HomeScreen.kt`, `OnlineGame.kt`, mövcud city host harness və son terrain review. V1 canlılıq hələ implementasiya edilməyib.

## Oyun atlaslarının əlavə emulator şəkilləri — 13 noyabr 2026
- Completed: istifadəçinin digər oyun eskizlərini emulatorda göstər göstərişi üzrə firuzəyi +suvaq/panel/taxta/beton baza dəstlərində sıfırdan bir mərtəbə canlı yerləşdirilib PNG çəkildi; əvvəlki purple ilə6üslub contact sheet və firuzəyi pause var. Bu hesabsız lokal debug gameplay-dir, serverli release flow deyil.
- Current state/Decisions: paralel şəhər işinə toxunmamaq üçün üslub seçicisi yalnız `/private/tmp/wobbrik-variant-qa` surətindədir; repo app sources/physics dəyişməyib, commit/merge/push/CI yoxdur. Cari şəhər handoff-u yuxarıda qüvvədə qalır.
- Checks: temp Java21 offline assembleDebug PASS18s; beş yeni görüntüdə1mərtəbə, betonda əlavə2mərtəbə;6üslub contact və teal pause vizual baxış. Google hesablı real seçilmiş parts yoxlanmayıb. Legacy3–6 hələ panel artıdır, yeni həcmli atlasla eynilik iddiası yoxdur.
- Relevant files: `docs/reviews/2026-11-13-game-release-live-check.md`; `docs/reviews/game-joins-visual/live/variants/all-six-emulator.png`, `teal-pause.png`; QA patch `docs/reviews/game-joins-visual/render/qa-style-selector.patch`; plan `docs/PLAN.md`.
- Remaining work/Exact next step: PNG-ləri istifadəçiyə göstər; yeni implementasiya başlamadan növbəti konkret göstərişi gözlə. Emulator5592 WobbrikLive teal pause-dadır, digər emulator toxunulmayıb.

## Cari vahid — şəhər chrome-u — 13 noyabr 2026
- Completed: `8b4a6a4` üzərindən `feat/city-chrome`; şəhər başlığı/əhali, cyan görünüş zolağı, kamera illüstrasiyalı “Binaya bax”/Bax/Geri paneli və Şəhər/İnventar/Bazar aşağı naviqasiyası qoşuldu. Real Home/inventory və mövcud bina focus/return işləyir. Plan/Bazar deaktivdir, placement/market uydurulmur.
- Current state: istifadəçinin “commit” göstərişi ilə bu vahid lokal commit edilir; merge/push/CI yoxdur; owner release faylları və əvvəlcədən untracked live-check review qorunub.
- Decisions: cari istifadəçi city-view əsasında bu vahidi tapşırıb, kamera sabitdir. Təsdiqli istinaddan kamera illüstrasiyasını şəffaf cutout kimi çıxarmağa ayrıca “Bəli” cavabı verilib; imagegen nəticəsi alpha saxlanılaraq app resource-a qoşulub. Mənbə ilə bit-identik crop deyil.
- Checks performed: final host18/18 PASS(22s), 22 PNG/contact/design comparison-a baxıldı; 320×568/font2,360×640/font1/2,390×844/font1, loading/error/empty, focus/return/nav callbacks. Dar İnventar kəsilməsi düzəldildi, real text line bounds yoxlaması PASS. Son Java21 core/engine/app unit+assembleDebug+lintDebug PASS(14s), kontrast4/4(min8.22),diff check PASS. Emulator/device/real account/inset/TalkBack/CI/backend test yoxdur.
- Remaining work: tam city-view piksel uyğunluğu/qəbulu və V1 canlılıq açıqdır. Mövcud bina artı/terrain sərhədi bu vahiddə dəyişməyib. Yeni feature başlamır; backend dəyişməsi lazım deyil və edilmədi.
- Relevant files: `CityChrome.kt`, `CityScreen.kt`, `res/drawable-nodpi/city_view_camera_v1.png`; review `docs/reviews/2026-11-13-phase4-city-chrome.md`; host `city-scene-visual/render/`, yeni evidence `docs/reviews/city-chrome-visual/`; yeganə plan `docs/PLAN.md`.
- Exact next step: lokal commit nəticəsini bildir; ayrıca göstəriş olmadan merge/push/CI və V1 canlılıq başlama. Sabit kamera/city-view/cutout təsdiqlərini yenidən soruşma.

## Oyun atlası — istifadəçinin rəddi və yeni chat handoff-u (cari oyun tapşırığı)
### Completed / Current state
- Atlas inteqrasiyası `937f5ea` commit-indədir, lakin istifadəçi altı emulator görüntüsünü gördükdən sonra yalnız ilk iki (purple/teal) görünüşün düzgün, digər dördünün səhv olduğunu açıq bildirdi. İstifadəçi: “hamısı birdəfəlik fix olmalı idi”. Buna görə əvvəlki review/commit və build PASS bütün görünüşlərin tamamlanması/qəbulu deyil. Agentin partial fallback həllini tamamlanmış kimi təqdim etməsi səhv idi.
- Kök səbəb: `gameAtlasStyle(parts)` yalnız partless və tam `base_1_*`/`base_2_*` dəstlərini yeni kompozit atlasa yönləndirir. `base_3..6` və qarışıq/digər seçimlər köhnə dayaz panel atlaslarına qayıdır; həcm/perspektiv/anchor/hissə layering uyğunluğu itir. Bu, açıq düzəldilməli problemdir; “köhnə art məhdudiyyətidir” deyib bağlama.
- Repo hazırda başqa şəhər vahidinin `feat/city-chrome` branch-ində, HEAD `8b4a6a4`; şəhər working diff-i və owner `app/release/` mövcuddur. Onlara toxunma, stash/reset/checkout ilə itirmə. Oyun fix-i üçün mövcud git vəziyyətini yoxlayıb ayrıca uyğun fix branch təşkil et; şəhər diff-ini oyun commit-inə qatma. Main merge/push/CI icazəsi yoxdur.
### Decisions
- Purple/teal formaları və təmizlənmiş iki atlas təsdiqlidir; istifadəçi onların emulator görünüşünü düzgün sayıb. Yenidən həmin təsdiqi soruşma, alternativ dizayn uydurma.
- Bütün oyun görünüşləri və mövcud hissə kombinasiyaları birlikdə uyğun həcm/perspektiv və joins ilə işləməlidir. Yalnız iki bütöv dəsti düzəldib qalanları səhv panel fallback-ında saxlamaq qəbul edilmir. 68-item ownership/ID/kataloqu silmə, seçimləri səssiz dəyişmə və bütün hissələri baked-in eyni fasadla əvəz etmə.
- Fizika, SceneMotion, accepted geometry, collision/scoring və backend authority dəyişməsin. Mobil yalnız portret; planşet ayrıca işdir.
- İstifadəçi bu konkret oyun görünüşünü emulatorla yoxlamağı açıq istəyib; əvvəlki avtomatik emulator açmamaq qaydası bu tapşırıq üçün maneə deyil. Hesabsız debug lokal yoxlama qəbul edilib; onu serverli release/hesab PASS kimi təqdim etmə. Merge/push/CI etmə.
### Remaining work
- Bütün dəst və qarışıq hissələr üçün yeni həcmli forma ilə işləyən uyğun modul art/atlas və ortaq crop/anchor/layering həlli qur. Yeni sprite-lər/texniki ayrılma lazım olsa mövcud təsdiqli vizual dili saxla; əlavə vizual qərar həqiqətən tələb olunarsa yalnız o konkret fərqi göstər.
- Pəncərə/giriş/işıq kompozit atlasda baked-in-dir. Universal kompozit üstündən əlavə overlay çəkmək duplicate hissə və səhv ownership görünüşü yaradar; bunu real modul ayrılma ilə həll et. Köhnə atlasları consumer/ID uyğunluğu tamamlanmadan silmə.
- Bütün baza dəstləri + qarışıq və açılan/satın alınan hissələrdən nümayəndə kombinasiyaları: aktiv/yellənmə/drop/joins/foundation/finish və oyun sonu yoxlansın. Bir mərtəbəlik capture seams yoxlaması deyil; çoxmərtəbəli real emulator görüntüsü də tələb olunur. Yalnız host PNG və build PASS ilə bağlama.
### Relevant files
- Plan yalnız `docs/PLAN.md`; ui-craft və `AGENTS.md` tətbiq edilir.
- app `GameAtlas.kt`, `GameScene.kt`, `FinishCosmeticScreen.kt` (`CosmeticSprite`, loader), `MainActivity.kt` (`PracticeGame`), `OnlineGame.kt` (`AcceptedScene`), `BuildingArt.kt`; debug `ScenePreviewActivity.kt`.
- `assets/game-v3/{atlas.json,purple.png,teal.png}`, `assets/finish/catalog-map.json` +mövcud68hissə atlasları; `scripts/clean-game-atlases.py`.
- Təsdiqli istinadlar `docs/design/game-tap-v2/phone.png`, `atlas-v3-proposal/{purple,teal}.png`; review `docs/reviews/2026-11-13-game-scene-joins.md`, `docs/reviews/2026-11-13-game-release-live-check.md`.
- Səhvin emulator sübutu: `docs/reviews/game-joins-visual/live/variants/all-six-emulator.png` (ilk2düzgün,qalan4rədd); orijinal ayrı PNG-lər eyni qovluqdadır. PNG-lər lokal ignored ola bilər; fayllar diskdə yoxdursa bunu açıq qeyd et.
### Checks performed
- `937f5ea` üçün əvvəlki host12/12 +engine/app unit/APK/lint PASS mövcuddur, lakin bütün art uyğunluğu PASS deyil. Yeni test/implementation bu handoff-da edilmədi.
- Hesabsız canlı PracticeEngine: purple və digər5dəstdə sıfırdan1mərtəbə real touch ilə yerləşdirildi. Teal pause çəkildi. Digər4art bu görüntülər əsasında istifadəçi tərəfindən rədd edildi. QA style selector yalnız `/private/tmp/wobbrik-variant-qa` surətindədir; repo app source-a tətbiq edilməyib. Təkrarlamaq üçün `docs/reviews/game-joins-visual/render/qa-style-selector.patch` var. Temp build Java21 assembleDebug PASS18s.
- Disposable WobbrikLive emulator5592 istifadə edildi; emulator5586 başqa iş üçündür, toxunma. Real release APK assets game-v3 ilə byte-identikdir, amma Google girişində qalıb; real hesabın saved parts-i yoxlanmayıb. Source/physics/backend bu emulator yoxlamalarında dəyişməyib.
### Exact next step
- Yeni chatdə bu son oyun handoff-unu və PLAN-ı oxu, yalnız göstərilən art/renderer/metadata fayllərini və altı real emulator görüntüsünü yoxla. Əvvəl bütün seçimlərin niyə səhv fallback-a düşdüyünü və kompozit/bölünən hissə sərhədini müəyyənləşdir; sonra mövcud təsdiqli purple/teal geometry-si üzərində bütün seçimləri bir yerdə düzəldən həlli hazırla. Şəhər diff-inə və fizikaya toxunma. Yeni chat üçün kontekst istənib; bu chatdə implementasiyaya başlama.

## Oyun modul atlası — pəncərə preview-su, 13 noyabr 2026 (cari)
- Completed: `826a441` bazasından `fix/game-modular-atlas`; bütün qeyri-tam purple/teal seçimləri təmiz həcmli shell+material+ayrı hissə qatlarına yönəldilib. Köhnə panel hull fallback silinib. İki generated clean shell, GameFacade.kt və debug selector/read-only telemetry əlavə edilib. Şəhər source-u, fizika, SceneMotion, accepted geometry, 68 ID/ownership/kataloq/backend dəyişməyib.
- Decisions: istifadəçi göstərilən bina gövdələrini OK saydı, damın oturuşunu və pəncərələri rədd etdi. Dam source depth-in iki dəfə sayılması düzəldildi; yeni 14-window proposal göstərildi. İstifadəçi “Əvvəl binanın üzərində göstər” seçdi: **pəncərə tətbiqi hələ təsdiqlənməyib**. Altı bina preview-su hazırdır, yalnız test harness candidate PNG-ni docs-dan oxuyur; production window art dəyişməyib.
- Current state: working diff, commit/merge/push/CI yoxdur. Əvvəlki owner handoff/release/live review/QA patch qorunub. `all-six-emulator.png` HƏLƏ köhnə rədd edilmiş sübutdur, yenilənmiş PASS deyil.
- Checks performed: normal core/engine/app unit+assembleDebug+lintDebug PASS21s (telemetry-dən əvvəl); telemetry assemble PASS5s; son roof correction ilə host22/22 PASS26s, əlavə6window proposal PASS11s. Emulator5592 sıfırdan real touch cəhdləri2–3mərtəbə də göstərdi, amma tam altı-style yekun sübut alınmadı; pəncərə təsdiqi gözlənəndə capture prosesi dayandırıldı. Emulator5586 toxunulmayıb. Son focused check nəticəsi review-da yenilənir. Build vizual qəbul deyil.
- Relevant files: GameAtlas.kt, GameFacade.kt, GameScene.kt, MainActivity.kt; debug ScenePreviewActivity.kt; game-v3/modular{,-teal}.png; docs/design/game-tap-v2/modular-window-proposal/; docs/reviews/2026-11-13-game-modular-atlas.md; game-joins-visual/render/GameJoinsRenderTest.kt; preview `game-joins-visual/proposed-windows-on-buildings.png`. Plan yalnız docs/PLAN.md.
- Remaining work / Exact next step: bina üzərində pəncərə preview-suna cavabı al. Təsdiq olarsa14pəncərəni ID/formaları saxlayaraq clean crop/contour/aspect/corner projection ilə qoş; pure purple/teal təsdiqini səssiz dəyişmə. Dam və bütün68ID üzrə vizual QA, son unit/APK/lint, sonra emulator5592-də altı baza+qarışıq/earned/paid real çoxmərtəbəli və terminal screenshots. Yalnız bundan sonra istənilən all-six-emulator.png-ni yeni emulator sübutu ilə yenilə. Hazırkı nəticəni tamamlanmış atlas fix-i sayma; şəhər/fizika/merge/push/CI yoxdur.
- Son nəticə: cari production/debug source-da Java21 offline core/engine/app unit+assembleDebug+lintDebug PASS13s; diff check PASS. Engine/core/SceneMotion/CityScreen diff yoxdur. Capture və onun QA logcat alt prosesləri dayandırılıb; köhnə pəncərəli emulator cəhdlərini yekun sübut sayma. Bina üzərində yeni pəncərə preview-su istifadəçiyə göstərilir, tətbiq təsdiqi gözlənir.

## Təsdiqli pəncərələr tətbiq edildi — 13 noyabr 2026 (son cari status)
- Decisions: istifadəçi bina üzərindəki yeni pəncərələri tətbiq etməyi və son dam formasını saxlamağı açıq təsdiqlədi. Əvvəlki “pəncərə təsdiqi gözlənir” artıq tarixçədir. 68 ID/seçim, fizika və şəhər sərhədi qüvvədədir.
- Completed: game-v3/windows.png + windows.json bütün14pəncərəni eyni ID-lərlə PracticeGame/AcceptedScene-ə qoşur; bütün dəstlər clean modular hull istifadə edir, baked pəncərə qatı yoxdur. Künc pəncərəsinin iki qanadı ön/yan səthə ayrıca oturur. Dam son təsdiqli rise/eave formasındadır; purple/teal təsdiqli material/trim/base cap qorunur. City/shared catalog/ownership/default seçimlər/backend/engine/SceneMotion dəyişməyib.
- Checks performed: son normal Java21 offline core/engine/app unit+APK+lint PASS29s; host28/28 PASS29s;68unikalID və14window keyset PASS;QA Python syntax/diff check PASS. Emulator5592 hostGPU,540×1200/density210:6baza+3qarışıq realtouch ilə0→3mərtəbə;14kombinasiyada68ID, hərəsində5mərtəbəli active+roof fixture28PNG. Raw/contact şəkillərə baxıldı. Realwooddrop/pause/resume/miss→FINISHED3floor ayrıca yoxlandı. Emulator5586 toxunulmayıb. Fixturelər real oynanmış cəhd kimi təqdim edilmir; bütün Cartesian kombinasiyalar enumerasiya edilməyib.
- Relevant files/evidence: docs/reviews/2026-11-13-game-modular-atlas.md son bölmə; live/variants/all-six-emulator.png artıq YENİ real3floor6style sübutudur; köhnə rədd şəkli ayrıca backup-dadır. live/variants/mixed-emulator.png; live/catalog/{manifest.json,all-68-active-emulator.png,all-68-roof-emulator.png}; render/capture-{live-variants,catalog-emulator}.py və build-emulator-contacts.py.
- Current state: fix/game-modular-atlas working diff; commit/merge/push/CI yoxdur. Owner release/əvvəlki handoff/live-review/QA patch qorunub. Authenticated server/release flow, planşet/landşaft/TalkBack/tam mərhələ qəbulu yoxlanmayıb. Yalnız bu atlas vahidi təhvil verilir.
- Remaining work / Exact next step: yekun review və istifadəçi təhvili; ayrıca göstəriş olmadan merge/push/CI/yeni feature yoxdur. Pəncərə təsdiqini təkrar soruşma. Kiçik portret/font2 son capture nəticəsi review-da əlavə olunur.
- Son yekun: 360×640dp/font2 emulatorda5mərtəbəli panel kadrına baxıldı; HUD/pauza/hint oxunur, mövcud hint dekorativ garden üzərindədir (tam dizayn pariteti iddiası deyil). Emulatorun ilkin1080×2400/density420/font1 parametrləri bərpa edildi; statik5mərtəbəli taxta terminal görünüşündə saxlanır. Son lokal diff review/diff check PASS; scope daxilində açıq bug tapılmadı. Atlas vahidi təhvilə hazırdır, yeni iş/merge/push/CI yalnız ayrıca göstərişlə.

## Atlas vahidinin lokal commit-i
- İstifadəçi bu atlas vahidini lokal commit etməyi açıq tapşırdı. Son review/yoxlamalar qüvvədədir; kod dəyişmədi. Yalnız atlas source/assets, təsdiq sənədi, bu vahidin handoff bölmələri və yeni emulator sübutları commit olunur. Əvvəlcədən mövcud owner release/live-review/QA patch və handoff əlavələri ayrıca saxlanır. Merge/push/CI yoxdur.
- Gələcək hissələr avtomatik uyğun sayılmır: mövcud modul səth/anchor/crop müqaviləsi ilə hazırlanmalı, eyni ID xəritəsinə qoşulmalı və qarışıq+çoxmərtəbəli renderlə yoxlanmalıdır. Köhnə panel hull fallback artıq yoxdur.

## Şəhər V1 canlılığı — 13 noyabr 2026 (son cari status)
- Decisions: istifadəçi V1 canlılığını başlamağı və təsdiqli city-view/sabit kameranı saxlamağı tapşırdı. Hazırlanan sakin/avtomobil nümunəsini “Bəli, bu qrafikanı tətbiq et” ilə açıq təsdiqlədi; yenidən təsdiq istəmə.
- Completed: `CityLifeRoutes.kt` real parcel union/accepted bina footprint/dekor/yol əsasında maksimum6sakin+2avtomobil üçün təhlükəsiz ambient marşrutlar yaradır. Unknown geometry fail-closed; L yolun iki qolu və quarter-turn saxlanır. `CityLifeArt.kt`, təsdiqli directional sprite/alpha metadata və CityScreen painter integration; pəncərə işığı, yüngül ağac hərəkəti. Focus sabitdir; saat background/focus-da dayanır, platform animasiyaları off/on-a tabedir. Server əhali/wallet/ownership və oyun fizikasına toxunulmayıb.
- Current state: `feat/city-liveness`, baza341c2da, working diff; commit/merge/push/CI yoxdur. Əvvəlki owner handoff/release/live-check/QA faylları qorunub. Backend footprint müqayisəsi read-only, dəyişiklik tələb olunmadı.
- Checks performed: final native host43/43 PASS34s (8life render/lifecycle/motion+18city regression+8routes+9geometry/projection); normal Java21 offline core/engine/app unit+assembleDebug+lintDebug PASS29s; kontrast4/4(min8.22),diff check PASS. 320×568/font2,360×640/font1/2,390×844/font1; iki vaxt/focus/return/empty;24native animasiya kadrı. Contact/design comparison/alpha QA və ayrı PNG-lərə baxıldı. Emulator/device/real hesab/API/inset/TalkBack/GPU benchmark/CI yoxdur.
- Remaining work: tam city-view piksel/kompozisiya pariteti, əvvəlki city bina artı/terrain fərqləri və ümumi mərhələ4qəbulu açıqdır. Actorlar directional walking pose+bob, skeletal gait deyil; avtomobillər yol qollarında gediş-gəliş edir, tam traffic network deyil. Planşet/landşaft ayrıca işdir.
- Relevant files: CityLifeRoutes.kt, CityLifeArt.kt, CityScreen.kt, BuildingArt.kt, CityLifeRoutesTest.kt; assets/city/life-actors-v1.{png,json}; docs/design/city-liveness-v1/README.md; docs/reviews/2026-11-13-phase4-city-liveness.md; city-scene-visual/render/CityLifeRenderTest.kt; local city-liveness-visual/contact.png,design-comparison.png,city-life-preview.gif. Yeganə plan docs/PLAN.md.
- Exact next step: hazır V1 canlılıq vahidini istifadəçiyə təhvil ver; sonrakı review/commit/yeni feature üçün ayrıca göstərişi gözlə. Tam şəhər dizaynı və mərhələ4bitmiş sayılmır; merge/push/CI yoxdur.

## V1 canlılıq — lokal review və commit
- İstifadəçi lokal review → problem yoxdursa commit tapşırdı. `341c2da` bazalı vahid yenidən review edildi; yeni bug tapılmadı, source/test dəyişmədi. Son43host və normal unit/APK/lint PASS qüvvədədir; contact sheet yenidən baxıldı, diff check PASS.
- `feat/city-liveness` üzərində yalnız bu vahid commit olunur; əvvəlki owner handoff/release/live-check/QA faylları qorunur və commit-ə daxil edilmir. Merge/push/CI yoxdur. Növbəti addım lokal commit nəticəsini istifadəçiyə bildirməkdir; yeni feature ayrıca göstərişlə.

## Şəhər dizayn tutuşdurması — 13 noyabr 2026 (ən son)
- Completed: `7b7b676` bazasından `fix/city-design-parity`; şəhər köhnə panel fasadı mövcud modular shell/material/14window ilə əvəz edildi, şəhərin seçilmiş kataloq damı saxlandı. Server 100×100 footprint üçün proyeksiya eni100→200/ön dayaq+50 düzəldi; ArtPitch yalnız presentation addımıdır. Loqo/başlıq/tab nisbəti/alt nav çərçivəsi bərpa edildi. Server accepted centers/quarter-turn/ownership, sabit kamera və fizika dəyişmir.
- Current state: working diff, commit/merge/push/CI yoxdur. Əvvəlki owner handoff/release/live-review/XML/QA patch qorunur. **Tam city-view pariteti/mərhələ4qəbulu deyil.**
- Decisions: istifadəçi təsdiqli city-view ilə tutuşdurub fərqləri düzəltməyi tapşırıb; yeni dizayn seçimi yoxdur. Real torpağa statik konsept adası və hesaba uydurma ev/dekor əlavə edilmir. Backend geometry müqaviləsi read-only yoxlandı; dəyişiklik tələb olunmadı.
- Checks performed: final host47/47 PASS50s; normal Java21 offline core/engine/app unit+APK+lint PASS39s; kontrast4/4(min8.22), diff check PASS. 320×568/font2,360×640/font1/2,390×844/font1; representative5bina/3–5floor, mixed coin/reward, bütün dönüşlər və focus/Geri; life/regression. Müqayisə və ayrıca normal/kiçik/font2/mixed-focus PNG-lərə baxıldı. Emulator/device/API/inset/TalkBack/GPU/CI yoxdur.
- Remaining work: dinamik union üçün təsdiqli city-view sahil/çiçək asset-i yoxdur; modular artda balkon/bağça detalları çatmır; uzun telefon pəncərəsində kompozisiya/chrome glow/icon/tab pariteti də açıqdır. Bu asset/kompozisiya fərqləri tamamlanmış sayılmır.
- Relevant files: CityBuildingArt.kt,CityBuildingProjection.kt,CityScreen.kt,CityChrome.kt,GameFacade.kt,GameAtlas.kt; CityBuildingProjectionTest.kt; city-scene-visual/render/CityParityRenderTest.kt; `docs/reviews/2026-11-13-phase4-city-design-parity.md`; lokal `docs/reviews/city-parity-visual/design-comparison.png`. Plan yalnız `docs/PLAN.md`.
- Exact next step: yeni chatdə bu handoff və review-u oxu; təsdiqli city-view əsasında çatışmayan dinamik sahil və modul fasad bağça/balkon artının sərhədini həll et, yeni alternativ dizayn uydurma. Alternativ lazım olsa əvvəl konkret nümunə/təsdiq; sonra inteqrasiya və host portret render müqayisəsi. Cari diff-i qoruyaraq davam et; merge/push/CI avtomatik yoxdur.

## City-view sahil/bağça davamı — 13 noyabr 2026 (ən son)
- Completed: əvvəlki `fix/city-design-parity` diff-i qorunaraq `CityGardenArt.kt` + `city/garden-v1.{png,json}` əlavə edildi. Boolean union xarici konturunda teksturalı sahil, səth daxilində çiçək/ot; modular fasadda ön/yan planter+ivy, corner-window anchor. Ground qapı/işıq örtülmür. Vahid4:5tab rail, laylar/şəhər vektorları, aktiv gradient/çərçivə tokenlərlə uyğunlaşdırılıb. Asset prompt/provenance `docs/design/city-garden-v1/README.md`.
- Current state: eyni branch/baza7b7b676, working diff; commit/merge/push/CI yoxdur. Əvvəlki owner release/live-review/XML/QA patch qorunur. Server geometry/ownership/selected parts/defaults/fizika/API dəyişməyib; backend dəyişikliyi tələb olunmadı.
- Decisions: istifadəçinin bu davam tapşırığı təsdiqli city-view asset/kompozisiyası üçündür; yeni alternativ konsept yaradılmayıb. Səth ornamentləri owned dekor deyil. Statik konsept adası/uydurma bina-fountain/garden mülkiyyəti tətbiq edilmir; entrance/step sprite-ı seçilmiş qapı/footprint-ə müdaxilə etməmək üçün işlədilmir.
- Checks performed: API24-uyğun son source ilə bütün49host test bir run-daPASS58s; son normal Java21/offline core/engine/app unit+APK+lintPASS28s. İlkin say-gözləntisi testi və API26-only Path.approximate lint xətası düzəldilib; minSdk24 qalır, cihazAPI24 runtime yoxlanmayıb. Kontrast4/4min8.22 və diff checkPASS. 360font1,390font1,320font2,mixed-focus,all-parcels və comparison renderlərinə baxıldı. Emulator açılmayıb.
- Remaining work: tam city-view piksel pariteti/mərhələ4qəbulu deyil. Real union düz sərhədlidir; server seçilmiş hissə/bina yerləşmələri konseptdəki statik evlərlə eyniləşdirilmir. Geniş terras/kafe balkonları, tam neon-glow və uzun viewport-un əlavə şaquli havası fərqləri açıqdır. Bunları səssiz redesign, geometry/crop və uydurma dekor ilə gizlətmə. Planşet/landşaft/device/API/inset/TalkBack/GPU/68ID tam şəhər enumerasiyası yoxlanmayıb.
- Relevant files: CityGardenArt.kt,CityScreen.kt,CityChrome.kt; city/garden-v1.{png,json}; drawable/city_{plan_layers,view_skyline}.xml; city-scene-visual/render/CityTerrainRenderTest.kt; `docs/reviews/2026-11-13-phase4-city-design-parity.md` son bölmə; lokal `docs/reviews/city-garden-visual/design-comparison.png`. Yeganə plan docs/PLAN.md.
- Exact next step: cari asset/chrome vahidini review nəticələri və açıq paritet məhdudiyyətləri ilə təhvil ver. Yeni alternativ vizual/terras və ya geometry qərarı üçün əvvəl konkret nümunə və açıq istifadəçi təsdiqi tələb olunur. Ayrıca göstərişsiz commit/merge/push/CI yoxdur.

## City-view chrome glow davamı — 13 noyabr 2026 (ən son)
- Completed: cari sahil/bağça/owner diff qorundu; `CityChrome.kt` aktiv tab və şəhər nav-a token əsaslı cyan glow əlavə edir. Yeni rəng/ölçü/pin yoxdur; layout/geometry/ownership/kamera dəyişmir.
- Current state: eyni `fix/city-design-parity`, baza7b7b676, working diff. Commit/merge/push/CI yoxdur.
- Decisions: əvvəlki təsdiqli city-view və asset sərhədləri qüvvədədir. Yeni terras/alternativ kompozisiya tətbiq edilməyib.
- Checks performed: host49/49PASS58s; 390font1 və320font2 yeni şəkillərinə baxıldı. Kontrast4/4min8.22; diff checkPASS. Adi unit/APK/lint son nəticəsi review-a əlavə edilir; emulator/device yoxdur.
- Remaining work: geniş terras/kafe balkonları, uzun viewport boşluğu və bütün chrome-un piksel pariteti açıqdır. Bu kiçik davam bütün city-view tapşırığını tamamlamır; mərhələ4qəbulu deyil.
- Relevant files: CityChrome.kt; `docs/reviews/2026-11-13-phase4-city-design-parity.md` son bölmə; `docs/reviews/city-garden-visual/representative-{390-font1,320-font2}.png`; plan yalnız docs/PLAN.md.
- Exact next step: son adi yoxlamanın nəticəsini qeyd et; qalan terras üçün konkret modul nümunəsini istifadəçiyə göstərib əvvəlki handoff-un təsdiq sərhədini həll et. Mövcud diff-i qoru; ayrıca göstərişsiz commit/merge/push/CI yoxdur.
- Son adi nəticə: Java21/offline core/engine/app unit+APK+lintPASS16s(108task); son source review və diff checkPASS.
- Yeni konkret balkon nümunəsi: `docs/design/city-garden-v1/balcony-proposal/{preview.png,README.md}`; built-in imagegen, ön/yan ledge+railing/flowers. Tətbiq təsdiqi GÖZLƏNİR; app-ə köçürülməyib. Exact next step: nümunəyə istifadəçi cavabını al; təsdiq olarsa mövcud facade anchor/selected window/occlusion/native-pixel sərhədlərinə uyğun proyeksiya edib portret host müqayisəsi apar. Nümunə bina üzərində yoxlanmayıb; kafe terras/uzun viewport fərqi hələ açıqdır.

## Təsdiqli balkon və kompozisiya — 13 noyabr 2026 (cari yekun)
- Completed: istifadəçi balkon nümunəsini açıq təsdiqlədi; əvvəlki pending artıq tarixçədir. Dəyişdirilməmiş preview `city/balcony-v1.png`+JSON ilə CityGardenArt-a qoşuldu. Ön balkon shell meylinə proyeksiya edilir; yan/corner anchor-ları var, ground qapı/işıq qorunur. Uzun normal portretdə kvadrat scene sahəsi və mərkəzləşmiş board scene→Bax boşluğunu azaldır; kiçik/font2 əvvəlki hündürlük davranışındadır.
- Current state: eyni `fix/city-design-parity`/baza7b7b676 working diff; əvvəlki owner/source/release/live-review/XML/QA qorunur. Commit/merge/push/CI yoxdur.
- Decisions: konkret balkon təsdiqlənib, yenidən soruşma. Mobil əvvəl/portret/sabit kamera/source pixel cap/real union və server parts saxlanır. Backend müqaviləsi dəyişmədiyindən AppBackend dəyişikliyi tələb olunmadı.
- Remaining work: bu balkon/adaptiv kompozisiya vahidi hazırdır. Statik konseptlə tam piksel eyniliyi və mərhələ4qəbulu iddia edilmir; real union/hesab məzmunu konseptin uydurma ev/fontan/kafe dəsti ilə əvəz olunmur. Device/API24runtime/inset/TalkBack/GPU/68IDtam şəhər/planşet/landşaft yoxlanmayıb; canlı yoxlama gate deyil.
- Relevant files: CityGardenArt.kt,CityScreen.kt; assets/city/balcony-v1.{png,json}; təsdiq/provenance `docs/design/city-garden-v1/balcony-proposal/README.md`; `docs/reviews/2026-11-13-phase4-city-design-parity.md` son bölmə; `docs/reviews/city-balcony-visual/balcony-comparison.png`; plan yalnız docs/PLAN.md.
- Checks performed: full49hostPASS47s; output destination/formatdan sonra parity3/3PASS16s; son normal Java21/offline core/engine/app unit+APK+lintPASS19s; kontrast4/4min8.22; diff checkPASS. Final comparison və mixed-focus/raw390/360 şəkillərinə baxıldı; synthetic hesabdır. Emulator açılmadı.
- Exact next step: bu tamamlanmış balkon/kompozisiya vahidini təhvil ver; ayrıca istifadəçi göstərişi olmadan yeni feature, commit, merge, push və CI başlama. Sonrakı review/commit istənərsə cari diff-i və owner fayllarını ayıraraq bax; artıq təsdiqlənmiş balkonu yenidən təsdiqə çıxarma.

## Balkon/kompozisiya lokal review — 13 noyabr 2026
- Completed: istifadəçinin istəyi ilə hazır city diff PLAN §7 və son review/handoff əsasında lokal review edildi; yeni konkret bug tapılmadı, source/test/asset dəyişdirilmədi.
- Current state / Decisions: `fix/city-design-parity`, HEAD `7b7b676`; working diff və owner faylları qorunur. Təsdiqli balkon yenidən təsdiqə çıxarılmır; backend müqaviləsi dəyişmir. Commit/merge/push/CI yoxdur.
- Checks performed: təkrar Java21/offline native49/49PASS58s; normal unit/APK/lintPASS4s(108task,95up-to-date); kontrast4/4min8.22; balkon SHA256 eyniliyi; comparison/raw320font2/mixed-focus və yeni raw390font1 görüntülərinə baxıldı. Synthetic host; device/emulator yoxdur.
- Remaining work: əvvəlki tam paritet/mərhələ4və yoxlanmamış runtime məhdudiyyətləri qalır; yeni düzəliş tapşırığı açılmadı.
- Relevant files: `docs/reviews/2026-11-13-phase4-city-design-parity.md` müstəqil lokal review bölməsi; əvvəlki balkon bölməsindəki source/assets/render yolları; yeganə plan `docs/PLAN.md`.
- Exact next step: lokal review nəticəsini təhvil ver; ayrıca göstərişsiz commit/merge/push/CI və yeni feature başlama.

## City-design-parity — lokal commit
- İstifadəçi review-dan sonra lokal commit tapşırdı. Review edilmiş şəhər source/assets/test/harness, PLAN qeydləri və bu vahidin handoff bölmələri commit edilir; əvvəlki owner oyun handoff əlavələri index-dən kənarda saxlanır.
- Son49host və normal unit/APK/lint/kontrast/diff check PASS qüvvədədir; production source dəyişmədi. Merge/push/CI yoxdur. Növbəti addım lokal commit nəticəsini bildirməkdir.

## Hissələr regression və preview — 13 noyabr 2026 (cari)
- Completed: buy→unlock→apply→fresh detail regression testi; receipt/rejection əhatəsi genişləndi. Parts-dan geri dönüş bina ID-sini saxlayır, serverdən yenilənmiş detal oxunur. İnventar/detail/parts/default-picker köhnə baked fasad əvəzinə oyun modular shell + seçilmiş window art/pitch/roof ilə çəkilir. Server accepted center-ləri dəyişmir. Standart preview-da 0 mərtəbə əvəzinə Standart görünüş; dar böyük şriftdə footer düymələri şaqulidir.
- Current state: `fix/parts-flow-regression`, baza `c40475b`. İstifadəçi bitdikdən sonra lokal commit etməyi tapşırıb. Owner handoff/release/live-review/XML/QA patch qorunur, əlaqəsiz fayllar commit-ə daxil edilmir. Main merge/push/CI yoxdur.
- Decisions: istifadəçi 2-ci şəkildə fasadın da səhv olduğunu dəqiqləşdirdi; düzəliş oyundakı təsdiqli renderer-in təkrar istifadəsidir, redesign/yeni asset deyil. AppBackend yalnız read-only yoxlandı, müqavilə dəyişmir.
- Remaining work: 1-ci şəkildəki şəhər oxu xətasının konkret səbəbi təsdiqlənməyib. `WobbrikCity` cause/stack logu əlavə edildi; empty-city/decor və missing geometry regression-u var. Screenshot-dan hansı HTTP/DTO/race xətası olduğu seçilmir. Tam mərhələ4 qəbulu/runtime yoxlaması iddiası yoxdur.
- Yeni istifadəçi tapşırığı: həddən böyük komponentlər **bir çox səhifədədir**, tək inventory deyil. Cari vahiddən sonra ayrıca mobil responsive ölçü vahidi: düymə/kart/başlıq/ikon/boşluqlar width+height+fontScale pillələrinə görə yığcamlaşmalı, mövcud rəng/üslub və əsas əməliyyatların əlçatanlığı saxlanmalıdır. Ümumi ölçü işi bu commit-ə daxil deyil. Konkret ölçü nümunəsi əvvəl göstərilir; yeni alternativ dizayn tətbiq edilmir. Mobil portret; planşet ayrıca qalır.
- Relevant files: `BuildingArt.kt`, `FinishCosmeticScreen.kt`, `InventoryScreen.kt`, `OnlineGame.kt`, `PartsScreen.kt`, `CityScreen.kt`; core `PartsFlowTest.kt`, `PartsMutationsTest.kt`, `CitySceneReadsTest.kt`; review `docs/reviews/2026-11-13-phase4-parts-regression.md`; host `docs/reviews/parts-flow-visual/render/`. Yeganə plan `docs/PLAN.md`.
- Checks performed: fokuslu core regression-ları, native host preview/detail testləri, normal unit/APK/lint və contrast/diff nəticələrinin son yekunu review-da. Sintetik host-dur; emulator/device/real hesab/API24runtime/inset/TalkBack/GPU yoxlanmayıb. Emulator açılmadı.
- Exact next step: bu vahidin lokal commit nəticəsini bildir. Sonra yeni chatdə handoff+review+GameTokens/HomeButton və ekranların mövcud ölçü istifadəsini scoped yoxla; bir çox mobil səhifənin komponentlərini yığcam/responsive etmək üçün konkret nümunəni göstər. Şəhər root cause-u ayrıca açıq saxla, log/API cavabı olmadan PASS demə. Merge/push/CI avtomatik yoxdur.

## Mobil yığcam ölçü nümunəsi — 13 noyabr 2026 (cari)
- Completed: handoff/PLAN, GameTokens, HomeButton/HomeIcon, PurchaseHeader/Card/Action/Panel və mobil inventory/parts/shop ölçüləri yoxlandı. Production UI dəyişmədən ayrıca Compose host specimen hazırlandı.
- Current state: `feat/mobile-compact-sizing`; konkret ölçü təsdiqi gözlənir. Əvvəlki owner working faylları qorunur; commit/merge/push/CI yoxdur.
- Decisions: mobil portret; üslub/art/radius/rənglər saxlanır. Təklif compact≤360dp width və ya≤640dp height: əsas düymə min48dp/24sp, başlıq18sp, ikon24dp, kart art48dp, section gap8dp; regular min56dp action/24sp heading/72dp art/16dp gap. Toxunma min48dp, font scale saxlanır. Bunlar hələ təsdiqlənməyib/tətbiq edilməyib.
- Relevant files: `docs/reviews/2026-11-13-mobile-compact-sizing.md`; `docs/reviews/mobile-sizing-visual/render/`; `GameTokens.kt`, `HomeScreen.kt`, `CityPurchaseChrome.kt`, `CityPurchasePanel.kt`; plan yalnız `docs/PLAN.md`.
- Checks performed: 3 native host specimen renderi (360×640dp current font1/proposal font1/font2), şəkillərə baxıldı; kontrast4/4PASS min10.48. Tam səhifə responsive/device/inset/TalkBack testi deyil, emulator açılmadı; production build dəyişmədi.
- Remaining work/Exact next step: istifadəçiyə konkret nümunəni göstər, ölçü təsdiqini al; sonra ortaq mobil token profili→komponentlər→ekranlar və mobil host render yoxlaması. AppBackend/API dəyişmə tələb olunmur. Yeni şəhər feature/placement işi açma.

## Yeni tam səhifə UI təklifi — cari qərar
- İstifadəçi əvvəlki ayrı komponent specimen-ini rədd etdi (“yamaq kimi”); sonra yeni UI hazırlamağı istədi. Əvvəlki ölçü təklifi təsdiqlənməyib, onu tətbiq etmə.
- Completed: production UI dəyişmədən Home+Inventory tam səhifə Compose təklifi hazırlandı. Existing GameTokens/home art/logo/icons/BuildingPreview saxlanır. Ana ekran art→stats→Oyna→Şəhərim→secondary; inventory üç iri giriş əvəzinə ortaq toolbar və yığcam grid. Font2 üçün vertical/2row uyğunlaşma var.
- Current state: `feat/mobile-compact-sizing`; yeni kompozisiyanın da konkret tətbiq təsdiqi gözlənir. Owner faylları qorunur; backend/commit/merge/push/CI yoxdur.
- Relevant files: `docs/reviews/mobile-sizing-visual/full-pages-v2.png`; `render/MobileCompositionTest.kt`; `docs/reviews/2026-11-13-mobile-compact-sizing.md` son bölmə; plan yalnız `docs/PLAN.md`.
- Checks:3 native host scenario/6PNG;360×640font1,390×844font1,320×568font2. Primary actions visibility assertions PASS, kontrast4/4PASSmin6.48; görüntülər yoxlanır. Bu review-only sintetik dizayn fixture-dir; production ekran/bütün state/inset/TalkBack/device qəbulu deyil. Emulator açılmadı.
- Remaining/Exact next step: tam səhifə board-u istifadəçiyə göstər; konkret təsdiq gəlmədən app UI-nə qoşma. Təsdiq olarsa ortaq mobile metrics→komponentlər→mövcud ekranlar vahidini davam etdir; ayrı plan/şəhər feature-i açma.

## Ana ekran navbar — son istifadəçi düzəlişi
- İstifadəçi home düymələrini aşağı navbar-a köçürməyi və oyunu başladan Play-i tam ortada istədi. `MobileCompositionTest.kt` yalnız review preview kimi yeniləndi: Şəhər/İnventar/Play/Kolleksiya/Nailiyyətlər. Dar görünüşdə qısa label Uğurlar, a11y adı Nailiyyətlərdir. Inventar təklifi saxlanır.
- Relevant files: `docs/reviews/mobile-sizing-visual/home-navbar-v3-preview.png`, `full-pages-v3.png`, `render/MobileCompositionTest.kt`;v2fixture `.kt.txt` saxlanır; review son navbar bölməsi.
- Checks:3host scenario PASS11s; final360navbar və320font2-yə baxıldı, primary görünür. App/backend UI/domain dəyişməyib; preview click-ləri production start/navigation deyil. CI/emulator/commit/merge/push yoxdur.
- Exact next step: navbar nümunəsini göstər; konkret görünüşün tətbiq təsdiqindən sonra mobil HomeScreen-in real onPlay/onPage inteqrasiyasını hazırla. Ümumi UI işi hələ tətbiq edilməyib; ayrıca plan yaratma.

## Təsdiqli navbar və mobil yığcamlaşdırma — tətbiq/commit yekunu
- Completed: istifadəçi v3navbar-ı açıq təsdiqlədi, tətbiq və yığcamlaşdırmanın davamı+lokal commit istədi. MobileHomeScreen real snapshot/onPlay/onPage ilə işləyir; mərkəzi Play mövcud AcceptedAttemptCommands.start/discard→host.enter→game axınına bağlıdır. Nav4giriş+Hesab real route-dur, recovery/onPlay null-da start bloklanır.
- Completed: ortaq mobil width+height ölçü pillələri, HomeButton/Al, inventory toolbar/normal2sütun/font2horizontal kart, parts grid/preview/kateqoriyalar, default picker başlıq/thumbnail, dekor/land summary və hesab/nailiyyət kompakt portreti. Hesab əsas əməliyyatları scroll-dan çıxarıldı; böyük şrift qorunur. Server snapshot/wallet/API/geometry/journal və gameplay dəyişmir; AppBackend dəyişikliyi lazım deyil.
- Current state: `feat/mobile-compact-sizing`, baza `ec84e7a`; review edilmiş bu UI vahidi source/review/harness və bu handoff ilə lokal commit-də saxlanır. Owner release/live-check/XML/QA patch və əvvəlki oyun handoff əlavələri kənarda qorunur. Merge/push/CI yoxdur.
- Decisions: v3navbar təsdiqlidir, yenidən təsdiq istəmə. Mobil portret; planşet/landşaft ayrıca qalır. Əvvəlki “tətbiq təsdiqi gözlənir/production dəyişməyib” qeydləri tarixçədir.
- Checks performed: native20/20PASS27s; Home6/Flow3/Purchase6/PartsPreview5.360×640font1,390×844font1,320×568font2 + Homefont2reserved24/48dp/offline/longbalance/XP. Real callback/navigation/min48dp; read/detail/picker/68art/6style. Final Java21/offline core/engine/app unit+assembleDebug/lintDebugPASS14s. Kontrast4/4PASSmin6.48,diffcheckPASS; screenshots/contact-lərə baxıldı. Sintetik host, real hesab E2E/device/API24runtime/TalkBack/GPU deyil; emulator açılmadı.
- Relevant files: `MobileHomeScreen.kt`, `MobileUiSizing.kt`, `GameTokens.kt`, `HomeScreen.kt`, `InventoryScreen.kt`, `PartsScreen.kt`, `FinishCosmeticScreen.kt`, `CityPurchase{Chrome,Panel}.kt`, `DecorationShopScreen.kt`, `LandShopScreen.kt`, `OnlineGame.kt`; `docs/reviews/2026-11-13-mobile-compact-sizing.md`, `docs/reviews/mobile-sizing-visual/{README.md,render/,production-contact-*.png}`; plan yalnız `docs/PLAN.md`.
- Remaining work: bu mobil ölçü vahidi fokuslu yoxlamalarla hazırdır; tam mərhələ4/oyun buraxılışı qəbulu deyil. Əvvəlki şəhər oxu root-cause və runtime məhdudiyyətləri ayrıca açıqdır, bu UI commit onları həll edilmiş saymır.
- Exact next step: commit nəticəsini bildir. Sonrakı böyük iş yeni chatdə handoff+PLAN və bu review əsasında; ayrıca göstərişsiz merge/push/CI və yeni feature açma.
