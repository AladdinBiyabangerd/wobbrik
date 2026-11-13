# Şəhər V1 canlılığı — 13 noyabr 2026

## Scope və review
- Branch: `feat/city-liveness`; baza `341c2da`. Lokal working diff review edilib, commit/merge/push yoxdur. Əvvəlcədən owner handoff əlavələri, release faylları və game live-check/QA faylları qorunub və bu vahidə aid deyil.
- İstifadəçi V1 canlılığına başlamağı, təsdiqli `docs/design/city-view.png` və sabit kameranı saxlamağı tapşırdı. Sakin/avtomobil asset-i çatışmadığından built-in imagegen nümunəsi göstərildi; istifadəçi “Bəli, bu qrafikanı tətbiq et” ilə konkret təsdiq verdi. Promptlar və mənbə `docs/design/city-liveness-v1/README.md`-dədir.
- `CityLifeRoutes.kt`: accepted scene əsasında maksimum 6 sakin və 2 avtomobil üçün presentation marşrutları. Say server əhalisinin əvəzi deyil; sakin göstəricisi əvvəlki kimi membership-dəndir. Population 0 və ya bina olmadıqda sakin yoxdur.
- Sakin marşrutu bütün eni ilə real parcel union daxilində olmalı, bina footprint-i və yerləşmiş qeyri-yol dekorundan/yoldan kənarda qalmalıdır. Əyri bina bütün accepted centers min/max və quarter-turn ilə maneədir. Union örtülməsi rectangle subtraction ilə tam yoxlanır; aralıqdakı boşluq və L notch sampling ilə ötürülmür. Unknown bina geometry/position və unknown dekor olduqda actor marşrutları fail-closed olur. Bu, server placement qaydasını əvəz edən validator deyil.
- Avtomobil yalnız yerləşmiş yol union-unda olur. `decor_road_2` tam kvadrat deyil, iki real L qoludur. Qollar ayrı qısa gediş-gəliş marşrutlarıdır; tam yol şəbəkəsi, yolayrıcı keçidi və traffic simulation yoxdur. Yeni yol/torpaq/mülkiyyət uydurulmur.
- `CityLifeArt.kt`: təsdiqli 8 directional sprite, sakin hərəkətində yüngül addım ritmi, mövcud window sprite alpha-sının yavaş 0.64–1 dəyişməsi, mövcud ağac billboard-unun kök ətrafında ±0.45° hərəkəti. Sakinlər ayrıca skeletal/multi-frame gait istifadə etmir. Ağac üçün sabit horizontal sweep envelope var; kamera bucağı və per-frame framing sabitdir. Yeni UI palitrası/spacing tokeni yoxdur; bu rəqəmlər səhnənin world/motion parametrləridir.
- Actor/bina/upright dekor eyni painter depth sırasındadır; yollar altdadır. Binaya fokusda actor/terrain yoxdur və bina işığı sabitdir. Art source-ları inventar/default/catalog və oyun atlası ilə əvəz edilmir.
- Saat yalnız Canvas redraw-a abunə olur; navigation/server oxusunu hər frame recompose/reload etmir. Maksimum 30 ambient redraw/s, RESUMED-dən kənarda və focus zamanı dayanır; pause vaxtı catch-up edilmir. Platform motion scale 0-da statikdir, yenidən açıldıqda hərəkət bərpa olunur. Aktyorlar decorative-dir, yeni focus/click/announcement yükü yaratmır.

## Backend uyğunluğu
- Mövcud CityScene DTO/reader, exact Ready lifecycle və server membership/geometry istifadə olunur. AppBackend `city_footprints.go` və `city_geometry.go` read-only tutuşduruldu: 100×100 bina footprint, accepted offsets, quarter-turn, dekor ölçüləri və L yol qolları.
- API/DTO/schema/revision/dedup/wallet/mutation dəyişmir; backend dəyişikliyi tələb olunmadı və edilmədi. Server runtime/testi bu vahiddə icra edilmədi. Placement bağlı qalır; bazar/billing əlavə edilmir.

## Checks performed
- Son native host: Java21, offline, mövcud `city-scene-visual/render/render.init.gradle`; 43/43 PASS (34s): CityLifeRenderTest 8, CityRenderTest 9, CityTerrainRenderTest 9, CityLifeRoutesTest 8, CityTerrainGeometryTest 4, CityBuildingProjectionTest 5.
- Life render: 320×568dp/font2; 360×640dp/font1 və font2; 390×844dp/font1. Hər ölçüdə iki vaxt, focus steady və return. Empty şəhər, production clock background/resume və platform animations off/on testləri PASS. Animasiya 24 native frame, 250ms ara ilə; GIF yalnız həmin host kadrlarından yığılmış sübutdur.
- Marşrut testləri: deterministic bounded actors, real road tələbi, parcel edge/gap və dekor maneəsi, L road bütün 4 dönüşdə, rotated L parcel notch, legacy geometry, zero population, leaning building footprint bütün 4 dönüşdə və davamlı turnaround.
- Son normal yoxlama: Java21 offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (29s).
- Token kontrastı: Text/Surface 13.00; Muted/Surface 8.22; Background/Accent 11.45; Text/Raised 10.24 — 4/4 PASS. `git diff --check` PASS.
- İlk host run-da test səhvən detail-dəki “40 sakin” yazısını header-də axtarırdı; real header “Əhali: 40” gözləntisi ilə düzəldildi. Motion toggle testinə snapshot apply notification əlavə edildi; son tam run-da hər ikisi PASS. Production problemi kimi gizlədilmir, uğursuz run PASS sayılmır.

## Vizual sübut və sərhədlər
- `docs/reviews/city-liveness-visual/`: `contact.png`, `design-comparison.png`, `actors-alpha-qa.png`, `city-life-preview.gif`; ayrı ölçü/kadr PNG-ləri və `sequence/frame-*.png`. Mövcud `.gitignore` qaydasına görə render PNG/GIF-ləri lokal ola bilər; production art və harness mənbə faylları ayrıca saxlanır.
- Contact/comparison/alpha QA və ayrı kiçik/normal/font2/focus/return/empty kadrlar açılıb baxıldı. Sakin/avtomobil kiçik miqyasda oxunur; ağ fon daşımır; mövcud əsas əməliyyatlar əlçatandır. Alpha source həqiqətən 0–255-dir; 8 metadata crop-u sheet sərhədlərindədir.
- `city-view.png` ilə yanaşı müqayisədə əvvəlki açıq fərqlər qalır: parcel kənarları dekorativ sahil deyil; şəhər bina artı konseptdəki həcmlə eyni deyil; chrome/kompozisiya tam piksel pariteti deyil. Synthetic scene konseptdəki 5 hazır evi uydurmur. Bu vahid həmin fərqləri bağlamır və tam mərhələ 4 qəbulu deyil.
- Emulator/device/real account/API flow/inset/TalkBack və böyük şəhərdə GPU/FPS ölçümü yoxdur. Bütün building/decor/frame kombinasiyaları enumerasiya edilməyib. Planşet/landşaft ayrıca işdir. CI işə salınmayıb; merge/push yoxdur.

## Exact next step
- Bu V1 canlılıq vahidini istifadəçiyə təhvil ver. Sonrakı review/commit və ya başqa böyük iş ayrıca göstərişlə; təsdiqli actor qrafikası və sabit kamera üçün təkrar icazə istəmə. Yeganə plan `docs/PLAN.md` qalır.

## Lokal review və commit — 13 noyabr 2026
- İstifadəçi review-dən sonra problem yoxdursa lokal commit etməyi tapşırdı; merge/push/CI qadağası saxlanır.
- `341c2da` bazasına qarşı routing/coverage/obstacles/quarter-turn, lifecycle/motion clock, painter/depth/focus, bundled sprite metadata və regression harness diff-i yenidən review edildi. Yeni təsdiqlənmiş bug tapılmadı; production/test kodu dəyişmədi.
- Son 43/43 host PASS34s və normal core/engine/app unit/APK/lint PASS29s qüvvədədir; hazırkı contact sheet yenidən açılıb baxıldı, diff check PASS. Eyni kod üçün testlər səbəbsiz təkrarlanmadı.
- Yalnız bu vahidin kod/asset/harness/dizayn/review/plan və handoff bölməsi commit olunur. Əvvəlcədən owner release/live-check/QA patch və başqa handoff əlavələri index-ə qatılmır, working tree-də qorunur. CI/merge/push yoxdur; əvvəlki cihaz/a11y/performance və tam dizayn pariteti məhdudiyyətləri qalır.
