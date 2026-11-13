# Şəhər — real torpaq səthi və dekor qatı

- Branch `feat/city-terrain-decor`, baza `937f5ea`; lokal working diff. Commit/merge/push/CI yoxdur. Owner `android-billing/app/release/` qorunub.
- İstifadəçi bu chatdə `docs/design/city-view.png` görünüşünü torpaq/dekor implementasiyası üçün açıq təsdiqlədi. Kamera sabitdir. Son əlavə təsdiq: ağac/skamya/fənər qrupları mövcud bir baxışla çəkilə bilər; quarter-turn onların baxışını dəyişmir. Yollar real yer müstəvisində dönür.

## Dəyişiklik və review

`CityTerrainGeometry` PLAN §7 və AppBackend `internal/wobbrik/city_footprints.go` ilə tutuşdurulmuş read-side ölçüləri proyeksiya edir. Base və beş genişlənmənin origin/quarter-turn və L girintiləri saxlanır. Bu, placement validator deyil. `CityTerrainArt` bütün proyeksiya olunmuş düzbucaqlıların Boolean union-unu və aşağı ekstrüziyasını hazırlayır: daxili parcel divarı yoxdur. Paths scene dəyişəndə hesablanır. Tekstura dünya koordinatlarına bağlanır; alış illüstrasiyası real kontur əvəzi deyil.

`CityScreen` mövcud exact Ready scene repository-dən gələn parcel/decor nüsxələrini çəkir. Alınıb yerləşdirilməmiş dekor əlavə edilmir; yeni mutation/placement/journal/wallet yoxdur. Yollar atlas crop-unun affine proyeksiyası ilə yer müstəvisinə oturur; L yolun alpha boşluğu qorunur. Upright dekorlar təsdiqli billboard baxışıdır, bina və dekor eyni depth/x/ID painter sırasındadır. Bütün torpaq/dekor/bina bounds viewport-a daxil edilir; binasız şəhərdə də real torpaq görünür. Fokus mövcud bina görünüşünü saxlayır, qayıdış səhnəni bərpa edir. Dekor artı çatışmazlığı səssiz saxta sprite ilə əvəz edilmir. Semantics-də yerləşmiş sahə/dekor sayı var.

Review/render zamanı aşkarlanıb düzəldilənlər: semantics property-yə `+=` oxumaq runtime exception yaradırdı; indi tək assignment-dir. Material sheet iki materialın sərhədində blend pikselləri daşıyırdı; runtime crop-un 4px gutter-i bu sətri istisna edir. Fokus fixture-i screenshot-dan əvvəl Compose idle gözləyir. Bina/dekor tie order bina touch sırası ilə uyğunlaşdırıldı. Native crop cap billboard və affine yol üçün saxlanır; fokus görünməyən dekorlarla məhdudlaşdırılmır.

Backend read-only yoxlandı: mövcud city/parcels və decorations position müqaviləsi kifayətdir; server dəyişməsi lazım deyil. Hesab/revision/retry müqaviləsi dəyişmir. Fizika/engine/accepted geometry dəyişmir.

## Asset və prompt

Built-in `image_gen` istifadə edildi; CLI/API fallback yoxdur. Yeni production asset: `android-billing/app/src/main/assets/city/terrain-material-v1.png`. Köhnə assetlər saxlanır. Runtime material crop-u atlasın iki yarısını və sərhəd gutter-ini ayırır; ayrıca image-edit skripti yoxdur.

Final prompt:

> Use case: stylized-concept. Production bitmap material sheet for Wobbrik fixed isometric city terrain, in polished colorful hand-painted mobile game style. Create a square opaque image split EXACTLY into two horizontal equal halves. TOP HALF: seamless lush lime/olive green short grass material viewed straight down, subtle tiny grass blade detail, low contrast, evenly lit, NO objects NO flowers NO trees NO rocks NO paths NO buildings NO island outline NO border. BOTTOM HALF: warm brown and muted mauve earthen rock cliff material, vertical softly faceted earth strata, evenly lit, horizontally repeatable, NO grass NO objects. Both materials fill their half edge to edge. No labels no text no watermark. The grass and rock should match a whimsical floating grassy city island, bright warm grass and terracotta/mauve rocky side walls. This is a UV material sheet, NOT a finished island or landscape.

## Vizual sərhəd

Host native Compose/Canvas fixture-lər real production kod/asset-lərdən istifadə edir; hesab və yerləşdirmələr açıq synthetic-dir. 360×640 font1/2, 390×844 font1, binasız/yalnız torpaq, bütün dekorlar və 4 yol istiqaməti, 6 parcel/rotated L, bina focus/return yoxlanır. `city-terrain-visual/` görüntüləri açılıb baxılır və konseptlə müqayisə edilir. Mövcud bina layer regression harness-i də işlədilir.

Tam `city-view.png` birəbir uyğunluğu PASS deyil: həqiqi parcel sərhədləri düz həndəsi xətlərdir, konseptdəki yumru/dekorativ perimetr yoxdur; ot/torpaq yeni materialdır. Mövcud bina artı və təsdiqli bir-baxış dekor qrupları saxlanır. Konseptdəki fountain, sərbəst davamlı dotted yollar, tam chrome və V1 canlılıq bu vahiddə əlavə edilməyib. Fountain təsdiqli 12 dekor kataloqunda yoxdur. Terrain konturu yumrulaşdırılaraq server sahəsi dəyişdirilmir. Bu nəticə mərhələ 4 qəbulu deyil.

Telefon portret işi; tablet/landscape ayrıca göstərişə qalır. Real hesab/device/ADB/emulator/TalkBack və real system inset yoxlaması yoxdur. CI/backend testləri yoxdur; bunlar PASS sayılmır. Mövcud UI mətn/rəng tokenləri dəyişmədiyindən kontrast yenidən ölçülməyib; yeni material qeyri-interaktiv oyun qrafikasıdır.

## Son yoxlamalar

Son nəticələr aşağıda qeyd edilir.

- Final host run: Java21/offline existing render init ilə 24/24 test PASS, 0 skipped/failures/errors (15s). `CityTerrainRenderTest` 9, `CityRenderTest` 6, `CityTerrainGeometryTest` 4, `CityBuildingProjectionTest` 5. 10 yeni screenshot + contact/design comparison açılıb baxıldı. Köhnə city-scene review-un tracked PNG evidence-i əvvəlki versiyada saxlandı; yeni evidence `city-terrain-visual/`-dədir.
- Son production diff-də Java21 `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` BUILD SUCCESSFUL (11s, 23 executed/85 up-to-date). Bu, `local-ci.sh` deyil. `git diff --check` PASS.
- Diff üzrə lokal review tamamlandı; yuxarıdakı semantics/material/tie-order düzəlişlərindən sonra yeni production bug təsdiqlənmədi. PNG evidence lokal ignore qaydasına tabedir; production material və harness/source ayrıca working diff-dədir.

## İstifadəçi tapşırığı ilə commit öncəsi review

13 noyabr: `937f5ea` bazasına qarşı bütün cari production/source/asset/geometry-test/host-harness/documentation diff-i yenidən lokal oxundu. Parcel/L transformları, Boolean union/extrusion, road affine origin/quarter-turn, native sampling cap, scene/focus/touch/semantics və painter sırası yoxlandı; final contact sheet yenidən açılıb baxıldı. Yeni təsdiqlənmiş production bug tapılmadı. Production dəyişmədiyindən yuxarıdakı final 24/24 host və unit/APK/lint PASS qüvvədədir; diff check təkrar PASS. Bu review live/device/a11y və tam dizayn qəbulu deyil. İstifadəçi lokal commit istədi; merge/push/CI edilmir.
