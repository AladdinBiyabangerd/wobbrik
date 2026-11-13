# Şəhər dizayn tutuşdurması — 13 noyabr 2026

## Scope və nəticə
- Branch `fix/city-design-parity`, baza `7b7b676`. Lokal working diff; commit/merge/push/CI yoxdur.
- İstifadəçi təsdiqli `docs/design/city-view.png` ilə tutuşdurub fərqləri düzəltməyi tapşırdı. Eyni istinad göstərildi; yeni dizayn təklif edilmədi. Token mənbəyi `GameTokens.kt` / `docs/design/mobile-v1/tokens.css`; yeni UI rəng/ölçü tokeni yoxdur.
- **Qismən uyğunlaşdırmadır; bütün dizayn fərqləri bağlanmayıb.** Aşağıdakı bina miqyası/fasad və chrome düzəlişləri hazırdır. Sahil, bağça/balkon qrafikası və tam kompozisiya uyğunluğu açıqdır. Mərhələ 4 qəbulu deyil.

## Düzəldilənlər
- Şəhərin köhnə düz panel fasadı `CityBuildingArt.kt` vasitəsilə mövcud modular shell/material/14 pəncərə ilə əvəz edildi. Eyni server hissə ID-ləri işləyir; yeni mülkiyyət/default/kataloq yaradılmır. Giriş/işıq yalnız ground mərtəbədə, seçilmiş pəncərə ön/yan səthdədir; corner kit iki qanada bölünür. Şəhər seçilmiş kataloq damını saxlayır; oyuna məxsus flat-cap əvəzetməsi şəhərə daşınmır. Inventory presentation dəyişməyib.
- Server 100×100 footprint `x-y` proyeksiyasında 200 vahid en tutur; əvvəl fasad 100 vahid çəkilirdi. En 200 və ön ayaq +50 ilə həmin footprint-ə uyğunlaşdırıldı. `ArtPitch=200×176/464` yalnız modul artının şaquli çəkiliş addımıdır; serverdə floor height/checkpoint dəyişmir. Accepted signed centers və quarter-turn əvvəlki kimi proyeksiya olunur, bütün mərtəbələr qalır. Bounds dam/fasad/ayaq üçün genişləndi; hit-test eyni bounds/view-dən istifadə edir. Fokusda clean shell native piksel limitini keçmir.
- Loqo header-ə qaytarıldı; Ana ekran əməliyyatı minimum 48dp loqo hədəfində saxlanır. Başlıq mərkəzləşdi, Plan/görünüş nisbəti və alt naviqasiyanın ümumi çərçivəsi bərpa edildi; gradient mövcud tokenlərdən gəlir. Böyük şriftdə başlıq üçün mövcud Body pilləsi, loqo üçün Target pilləsi istifadə olunur.
- Mövcud pəncərə canlılığı modular window decal alpha-sına qoşuldu. `drawModularFloor` default alpha=1 saxlayır; oyun çağırışlarının görünüş/davranış parametrləri dəyişmir. Physics/SceneMotion dəyişmir.

## Backend uyğunluğu
- `/Users/mac/My Workspace/AppBackend/internal/wobbrik/city_geometry.go` read-only tutuşduruldu: `cityFloorWidth=100_000`, `cityFloorDepth=100_000`, base-center origin və accepted centers-dən footprint union. 1000 ticks/world-unit müqaviləsi saxlanır.
- Yalnız presentation dəyişir; API/DTO/schema/wallet/ownership/revision/dedup/mutation dəyişmir. Backend dəyişikliyi tələb olunmadı və edilmədi; backend testləri bu vahiddə yoxdur.

## Yoxlamalar
- Son native host render: Java21/offline, mövcud `city-scene-visual/render/render.init.gradle`; **47/47 PASS, 50s**: projection 6, terrain geometry 4, routes 8, CityRender 9, CityTerrainRender 9, CityLifeRender 8, CityParityRender 3. Yeni footprint testi fasadın sol/sağ və ön dayağını real proyeksiya edilmiş 100×100 server footprint küncləri ilə tutuşdurur.
- Son normal `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` **PASS, 39s**, 108 task (core/engine unit nəticələri UP-TO-DATE; app unit icra edildi).
- Kontrast 4/4 PASS: Text/Surface 13.00; Muted/Surface 8.22; Background/Accent 11.45; Text/Raised 10.24. Diff check PASS.
- Host hallar: 320×568/font2, 360×640/font1/2, 390×844/font1; 10 mərtəbəli/əyrilikli şəhər, representative 3–5 mərtəbəli 5 bina, bütün quarter-turn-lər və qarışıq coin/reward hissələri; hər binaya fokus/Geri; boş/loading/error, terrain/decor, lifecycle/motion regression.
- Lokal sübut `docs/reviews/city-parity-visual/`: `design-comparison.png` solda təsdiqli referens, sağda production Canvas/synthetic hesab; `representative-*.png`, onların `focus-*.png`-ləri, həyat və regression kadrları. Bu, real hesab/device sübutu deyil. PNG-lər mövcud ignore qaydasına görə lokal ola bilər. Əvvəlki liveness contact/comparison faylları yeni nəticə kimi təqdim edilmir.
- Müqayisə şəkli, normal/kiçik/font2 və qarışıq focus PNG-ləri açılıb baxıldı. Düymələr/naviqasiya görünür; font2-də scene kiçilir, primary controls saxlanır. Konseptlə fərqlər aşağıda açıqdır.
- Emulator açılmadı. Real hesab/API, cihaz inset/klaviatura, TalkBack, GPU/FPS, planşet/landşaft və bütün 68 ID-nin şəhərdə tam visual enumerasiyası yoxlanmayıb. Canlı yoxlama gate deyil; yoxlanmamış hallar PASS deyil.

## Qalan dizayn fərqləri və növbəti vahid
- Real parcel union düz kənarlı materialdır. Konseptdəki yumru sahil/çiçək/ot perimetri üçün dinamik kontura uyğun sprite/mesh asset-i yoxdur. Statik adanı səhnənin altına qoymaq real torpaq sərhədini yanlış göstərəcəyindən tətbiq edilmədi; sxematik scallop/placeholder alternativi də tətbiq edilmədi.
- Hazır modular artda konseptin balkonları, pilləkən/bağça əlavələri, fasad bitkiləri yoxdur. Bunlar server tərəfindən seçilən mövcud hissələri və accepted həndəsəni örtən hazır bina şəkilləri ilə əvəz edilmir.
- Referens daha enli/qısa lövhədir; real telefonun uzun pəncərəsində əlavə boş hava qalır. Ada/binaları viewport-a sığdırmaq və tam torpağı göstərmək məhdudiyyəti saxlanır. Tam chrome glow/icon rəngi, birləşmiş tab kompozisiyası və bütün scene nisbətləri hələ piksel pariteti deyil. Məhsulda “Konsept”, “Ölçülər şərtidir” yazıları əlavə edilmir; sabit kamera son qərardır.
- Növbəti ayrıca vahid: təsdiqli referensdən dinamik parcel sahil artını və modul fasad bağça/balkon detallarını hazırlamaq üçün çatışmayan asset sərhədini müəyyənləşdir; yalnız eyni təsdiqli dizaynı bərpa et. Alternativ vizual lazım olsa AGENTS.md üzrə tətbiqdən əvvəl göstərib açıq təsdiq al. Sonra həmin assetləri real union/accepted binalara qoşub yenidən portret render müqayisəsi apar. Bütün fərqlər bağlanmadan şəhər və mərhələ 4 tamamlandı demə.

## Lokal review
- Baza qarşı yalnız bu vahidin source/test/harness/docs diff-i oxundu. Footprint miqyas xətası düzəldildi; modular source pixel cap əlavə edildi; şəhər kataloq damının game-only cap ilə dəyişdirilməməsi yoxlandı. Son diff-də ayrıca təsdiqlənmiş funksional bug tapılmadı; yuxarıdakı vizual boşluqlar açıqdır.
- Əvvəlcədən dirty `docs/context/current-task.md` əlavələri, `app/release/`, game release/live-review/XML/QA patch faylları qorunur; bu vahidin review-u onların məzmununu qəbul etmir.

## Sahil/bağça davamı — 13 noyabr 2026

### Dəyişiklik və sərhəd
- Eyni `fix/city-design-parity` branch-i və `7b7b676` bazası; əvvəlki working diff və owner faylları qorunub. Commit/merge/push/CI yoxdur.
- Təsdiqli `city-view.png` əsasında built-in imagegen ilə şəffaf `city/garden-v1.png` komponent atlası və crop JSON hazırlandı. Prompt/provenance: `docs/design/city-garden-v1/README.md`. İlk halo-lu çıxış production-a qoşulmadı; alpha cleanup nəticəsi istifadə olunur. Native host renderdə qara fon/halo görünmür.
- `CityGardenArt.kt`: sahil hazır Boolean union konturundan ölçülür; parcel birləşmələrinin və L-in daxili tikişlərində sahil yoxdur. Qaya/ot materialı mövcud cliff konturuna, içəri çəkilmiş çiçək/ot tuft-ları səthə clip edilir. Server torpağının forması/miqyası dəyişmir. Ayrı adalar arasında art körpüsü yaradılmır. Bu xırda səth ornamentləri owned dekor/kataloq/mutation deyil.
- Eyni modul fasadlara ön/yan çiçək planter-ləri və sarmaşıq qoşuldu; `coins_window_2` ön künc anchor-unu izləyir. Ground qatında yeni planter çəkilmir; seçilmiş giriş/işıq və accepted footprint qorunur. Atlasdakı giriş pilləsi/topiary sprite-ı istifadə edilmir: qapını örtmək və footprint-dən kənara əlavə çıxmaq təsdiqlənməyib.
- Plan/görünüş vahid rail, referensə yaxın 4:5 nisbət, laylar ikonu, pəncərə dəlikləri olan cyan şəhər vektoru, aktiv pill/nav gradient və daha güclü aktiv çərçivə əlavə edildi. Bütün UI rəng/ölçüləri `GameTokens`-dən gəlir; yeni palitra yoxdur. Böyük şriftdə ikonlar yığcamlaşır, sözlər ortadan qırılmır.
- Backend/API/DTO/schema/ownership/wallet/geometry/revision dəyişmir. Yalnız presentation və host QA dəyişdiyindən AppBackend dəyişikliyi tələb olunmadı; backend source/test işlədilmədi.

### Vizual tutuşdurma və qalan fərqlər
- `docs/reviews/city-garden-visual/` yeni synthetic-account host sübutudur; əvvəlki `city-parity-visual/` şəkilləri dəyişdirilmir. `design-comparison.png` referens/390-font1/320-font2 müqayisəsidir, app screenshot-u kimi təqdim edilmir.
- 360/font1, 390/font1, 320/font2 və qarışıq hissəli focus ayrıca açılıb baxıldı. Bitkilər və sahil inteqrasiya edilib, künc planter-i düzəldilib; əsas Bax/Geri və naviqasiya görünür. Hər bina üçün focus/return fixture-i qalır.
- **Tam piksel pariteti və mərhələ 4 qəbulu deyil.** Statik referensin yumru adası real düzbucaqlı/L union-a çevrilmir; üzərindəki konkret beş ev, fountain, pilləkən, ağac və bağlar hesaba uydurulmur. Bina rəngi/damı/pəncərəsi real seçilmiş hissələrdən gəlir, buna görə konseptin bütöv evlərinin siluetləri ilə eyniləşdirilmir. Yeni balaca planter-lər geniş terras/kafe balkonunu əvəz edən iddia deyil.
- Uzun telefon pəncərəsində scene tam torpaq/bina bounds-u və source pixel cap ilə sığdırılır; əlavə şaquli hava qalır. Onu yox etmək üçün geometry-ni dəyişmək, scene-ni kəsmək və ya binanı qeyri-bərabər uzatmaq edilmədi. Referensin neon glow-u və bütün chrome/scene nisbətləri hələ eyni piksel nəticəsi deyil. Bu fərqlər PASS kimi bağlanmır.
- Emulator/device açılmadı; real hesab/API, inset/klaviatura, TalkBack, GPU/performance, bütün68ID şəhər enumerasiyası və planşet/landşaft yoxlanmayıb. Mobil əvvəl/portret qərarı saxlanır; canlı yoxlama gate deyil.

### Yoxlama qeydi
- İlk fokuslu parity+terrain host yoxlaması 12/12 PASS,36s (iki yeni shore testindən əvvəl).
- Son source ilə tam49test dəstində48PASS/1FAIL: yeni disconnected test native segment/tuft sayını ikiqat gözləyirdi. Bu say rendering müqaviləsi deyil; yoxlama həqiqi perimetr uzunluğu, ayrı torpaqların körpüsüz qalması və bitkilərin hər adada olması ilə əvəz edildi. Uğursuz run PASS sayılmır; son fokuslu nəticə aşağıda qeyd edilir.
- Kontrast4/4 PASS: min8.22; Text/Surface13.00, Text/Raised10.24, Background/Accent11.45. Lokal diff review-da yeni domain/mutation/default dəyişməsi yoxdur.
- Düzəlişdən sonra son fokuslu terrain/shore host dəsti **11/11 PASS,17s**. Eyni production source ilə əvvəlki tam run-dakı digər38test PASS qüvvədədir; ümumi49test əhatəsi iki run əsasında yoxlanıb, tək tam49PASS run iddiası yoxdur. Son dəyişiklik yalnız test gözləntisindədir.
- Yeni comparison şəklinə də baxıldı: normal/320font2 chrome və torpaq bütöv görünür; yuxarıdakı paritet məhdudiyyətləri açıq saxlanır. PNG-lər mövcud ignore qaydasına görə lokal sübutdur.
- Adi unit/APK keçsə də ilk normal lint run **FAIL,38s** oldu: `Path.approximate` minSdk24 əvəzinə API26 tələb edirdi. Production oxunuşu `PathMeasure.getPosTan` və yarım-world-unit sampling/collinear reduction ilə dəyişdirildi; final clip dəqiq səth/cliff yolunda qalır. MinSdk/stack pin dəyişmir. Son production dəyişiklikdən sonra bütün49host və normal yoxlamalar təkrarlanır; nəticələri aşağıdadır.

### Son yekun yoxlama
- API24-uyğun son production source ilə **bütün49host test bir run-da PASS,58s**. Bu nəticə yuxarıdakı əvvəlki iki-run48+11 statusunu əvəz edir. Source dəyişmədən son normal Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` **PASS,28s**,108task. İlkin lint problemi düzəlib; minSdk24 qalır. API24 cihaz runtime testi edilməyib.
- Son terrain/L/all-parcels renderinə, 360font1-ə və yenilənmiş comparison-a baxıldı. Əvvəl baxılmış320font2/mixed-focus hallarını dəyişən chrome/fasad source-u sonra dəyişməyib; son host run onların PNG-lərini də yenilədi. Kontrast4/4 və son diff check PASS.
- Lokal diff review: yeni garden/shore sampling/clip, corner anchor, vector tint və çağırışlar oxundu. Degenerate strip-lər atılır, sampling scene dəyişəndə hesablanır, source art pixel cap daxilindədir. Yeni domain bug tapılmadı; real vaxt performansı ölçülməyib. Əvvəlki owner diff/release/live-check/XML/QA patch saxlanır. Tam piksel pariteti üçün yuxarıdakı açıq fərqlər qüvvədədir.

## Chrome işıqlanması davamı — 13 noyabr 2026
- Cari owner diff və əvvəlki sahil/bağça implementasiyası qorundu. Yeni dəyişiklik yalnız `CityChrome.kt`: aktiv city-view tab və şəhər naviqasiyası üçün `GameTokens` rəng/ölçülərindən pilləli cyan glow. Surface arxasında çəkilir; layout, hit target, server geometry, kamera və source pixel cap dəyişmir. API blur/shadow asılılığı yoxdur.
- Son host dəst **49/49 PASS,58s**: projection6/terrain geometry4/routes8/CityRender9/terrain11/life8/parity3. 390×844/font1 və320×568/font2 yeni production renderləri açılıb baxıldı: glow görünür, Bax və nav görünür. Synthetic host hesabıdır; device/emulator/API24 runtime yoxlanmayıb.
- Kontrast4/4 PASS(min8.22); palette dəyişməyib. Diff check PASS. Adi unit/APK/lint nəticəsi aşağıda əlavə olunur.
- **Tapşırığın tam paritet hissəsi hələ açıqdır.** Sahil/bağça əvvəlki diff-də mövcuddur; bu davam onu yenidən yaradılmış iş kimi təqdim etmir. Geniş terras/kafe balkonları və uzun viewport-un boşluğu dəyişməyib. Əvvəlki neon-glow boşluğunun aktiv tab/nav hissəsi düzəldi; bütün chrome üçün piksel eyniliyi iddiası yoxdur. Yeni terras/alternativ kompozisiya nümunəsinin ayrıca təsdiqi əvvəlki handoff sərhədidir. Statik adanı/uydurma ownership-i tətbiq etmək və ya şəhəri kəsmək həll kimi seçilmədi.
- Backend presentation müqaviləsi dəyişmədiyindən AppBackend dəyişməsi lazım deyil. CI/commit/merge/push yoxdur.
- Son adi Java21/offline core/engine/app unit + assembleDebug + lintDebug **PASS,16s**,108task. Son source dəyişməyib; yalnız nəticə/handoff qeydləri əlavə olunur. Lokal source review-da glow-un aktiv elementlə məhdud olduğu və layout/hit target dəyişmədiyi yoxlandı.
- Qalan balkon üçün built-in imagegen ilə konkret ön/yan ledge+railing/flowers nümunəsi hazırlandı və baxıldı: `docs/design/city-garden-v1/balcony-proposal/preview.png`; dəqiq prompt/provenance sibling README-dədir. Bu yalnız təklifdir, app asset-i deyil. Front perspektivi mövcud fasada proyeksiya tələb edir; bina üzərində anchor/occlusion/native-pixel/render yoxlamaları edilməyib. Tətbiq təsdiqi gözlənir; geniş kafe terrası bu nümunə ilə həll olunmuş sayılmır.

## Təsdiqli balkon və portret kompozisiyası — 13 noyabr 2026

### Nəticə və qərar
- İstifadəçi “Bəli, balkon nümunəsini təsdiqləyirəm. Tətbiq et və qalan kompozisiya fərqlərini tamamla, cari diff-i qoru” ilə konkret nümunəni açıq təsdiqlədi. Əvvəlki pending qeydləri tarixçədir; təkrar təsdiq lazım deyil.
- `city/balcony-v1.png` təsdiqli preview-nun dəyişdirilməmiş nüsxəsidir (SHA256 hər ikisi `9dd16141a0dd030cc7fc6b073b3ea7735982a5f9bd897d456eaabb751a630220`). Sibling JSON görünən ön/yan obyekt crop-larını göstərir. Prompt/provenance `docs/design/city-garden-v1/balcony-proposal/README.md`-də qalır; yeni imagegen çağırışı edilmədi.
- `CityGardenArt.kt` iki atlası yükləyir; əvvəlki kiçik fasad planter-ləri təsdiqli geniş railing/ledge/çiçək/ivy balkonları ilə əvəz olundu. Ön asset shell-in ölçülmüş16/326meylinə affine proyeksiya edilir; corner-window dar ön anchor-u saxlayır, yan asset sağ səthə oturur. Ground mərtəbəyə balkon əlavə edilmir; qapı/işıq və seçilmiş pəncərə formaları görünür. Mərtəbə centers/quarter-turn/parts/footprint və depth sırası dəyişmir.
- `CityScreen.kt`: yalnız normal şriftli uzun portretdə (inset-dən sonrakı height>2×width) səhnə maksimum kvadrat sahə tutur; header/səhnə/Bax/nav vahid board kimi şaquli mərkəzləşir. Beləliklə scene→Bax arası lazımsız Canvas boşluğu azalır. 360×640 və320×568/font2 əvvəlki bütün mövcud hündürlük davranışını saxlayır. Səhnə/torpaq kəsilmir, source pixel cap və sabit kamera qalır; artıq hündürlük board-un ətrafında fon olaraq qalır.
- Təsdiqli balkon inteqrasiyası və bu adaptiv kompozisiya vahidi tamamlandı. Bu, statik konsept adasının/eyni evlərin hesaba köçürülməsi və ya tam city-view piksel eyniliyi iddiası deyil: server union forması, real bina/owned dekor sayı və seçilmiş hissələrə görə səhnə fərqlənir. Kafe mebeli, fontan və əlavə mülkiyyət yaradılmadı. Mərhələ4qəbulu ayrıca qalır.

### Yoxlama və review
- İlkin parity+chrome host12/12PASS48s. Son production anchor/layout ilə tam şəhər host dəsti49/49PASS47s. Yalnız evidence destination və whitespace formatı yenilənəndən sonra parity3/3yenidənPASS16s. Son production davranışı dəyişməyib.
- Son normal Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS19s,108task. Kontrast4/4PASS(min8.22); palette dəyişməyib. Diff checkPASS.
- `docs/reviews/city-balcony-visual/`: son parity birbaşa həmin qovluğa render edilib; full49run-dakı terrain/life/regression PNG-ləri eyni production source-un əvvəlki output destination-dan kopyalanıb. `balcony-comparison.png` QA kompozitidir, app ekranı deyil: təsdiqli referens/390font1/360font1/320font2/mixed-focus. Comparison və final mixed focus, əvvəlki raw390/360focus açılıb baxıldı; controls görünür, ground girişləri və pəncərələr saxlanır, halo/matte görünmür. Synthetic hesabdır, real hesab sübutu deyil.
- Lokal source review: asset source/crop, front shear, corner/ground istisnası, scale/viewport və pointer/semantics eyni view istifadəsi oxundu. Yeni domain/mutation dəyişməsi yoxdur. API/DTO/schema/ownership/wallet dəyişmədiyi üçün AppBackend dəyişikliyi tələb olunmadı.
- Emulator/device/real hesab/API24runtime/inset/TalkBack/GPU/performance/68IDtam şəhər enumerasiyası/planşet/landşaft yoxlanmayıb. Canlı yoxlama gate deyil. CI/commit/merge/push yoxdur; owner release/live-review/XML/QA və əvvəlki source diff qorunub.
## Müstəqil lokal diff review — 13 noyabr 2026

- Branch `fix/city-design-parity`, HEAD/baza `7b7b676e2d8ab3e2386e735355e4ce6ca44a10ae`; hazır tracked və untracked city source/assets/test/harness diff-i review edildi. Handoff-un son balkon bölməsi və PLAN §7/cari icra qeydi əsas götürüldü. Bu review əvvəlki working diff-in üzərindədir; ayrıca balkon-only commit mövcud deyil.
- **Yeni konkret bug tapılmadı.** Balkon crop/source, ön affine meyl, corner anchor, ground istisnası, shared facade default alpha=1, bounds/source-pixel limit, eyni viewport-la hit-test, uzun portret weight/height və compact/font2 davranışı oxundu. Accepted geometry, seçilmiş hissə ID-ləri, ownership və sabit kamera saxlanır. API/domain dəyişmədiyindən AppBackend dəyişikliyi tələb olunmur; backend yoxlaması bu review-da işlədilmədi.
- Təsdiqli preview və app balkon PNG-si SHA256 üzrə eynidir: `9dd16141a0dd030cc7fc6b073b3ea7735982a5f9bd897d456eaabb751a630220`. Token mənbəyi `GameTokens.kt`; kontrast yenidən 4/4 PASS, minimum8.22.
- Java21/offline native host dəsti yenidən **49/49 PASS,58s**: projection6, terrain geometry4, routes8, render9, terrain11, life8, parity3; XML-də failure/error0. Adi `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` **PASS,4s**,108task (95UP-TO-DATE; core/engine testləri cache-dən, app unit icra edildi). İlk sandbox cəhdi Gradle cache lock icazəsinə görə başlamadı; eyni host əmri icazəli təkrar icrada keçdi. Bu CI deyil.
- Mövcud `balcony-comparison.png`, raw320/font2 və mixed-focus, yenidən render edilmiş raw390/font1 açılıb baxıldı. Balcony/ground/pəncərə və əsas Bax/Geri/nav görünür. Bunlar synthetic native host sübutudur; konseptlə tam piksel eyniliyi və mərhələ4qəbulu təsdiqlənmir.
- Device/API24runtime/inset/TalkBack/GPU/68ID tam enumerasiya/planşet/landşaft yoxlanmayıb; canlı yoxlama gate deyil. Əvvəlki paritet məhdudiyyətləri qüvvədədir.
- Source/test/asset düzəlişi edilmədi. Owner release/live-review/XML/QA patch və əvvəlki handoff məzmunu qorundu; yalnız bu review qeydi və handoff əlavəsi yazıldı, mövcud ignored host PNG-ləri testlər yenilədi. Commit/merge/push/CI yoxdur.


## Lokal commit
- İstifadəçinin ayrıca commit göstərişi ilə review edilmiş vahid lokal commit edilir. Əvvəlki yoxlamalardan sonra production dəyişməyib. Owner release/live-review/XML/QA və əlaqəsiz handoff əlavələri commit-ə daxil edilmir. Merge/push/CI yoxdur.
