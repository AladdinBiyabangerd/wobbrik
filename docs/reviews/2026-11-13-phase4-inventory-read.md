# Mərhələ 4 — inventar və bina detalı oxusu

- Branch: `feat/phase-4`; baza `e97e78c`. Review: bu branch-in inventar oxu diff-i (yeni InventoryReads/InventoryScreen/test faylları daxil). Main merge/push yoxdur.
- İstifadəçi inventory-market-v2 inventar və city-completion-v1/04-building-detail görünüşlərini təsdiqlədi; lazım olsa raster asset hazırlamağa icazə verdi. Bu vahid mövcud atlasları təkrar istifadə edir, yeni raster yoxdur.
- Home inventar girişi owner-scoped paginated GET inventory-ni açır; detail ayrıca GET buildings/{id} və revision-consistent GET city ilə yenilənir. Accepted finish-dən home-a qayıtdıqdan sonra inventar fresh read edir; lokal bina/reward yaradılmır.
- Hissələr və sakin/dəyər serverdən gəlir. Preview yalnız accepted_geometry koordinatları ilə qurulur; legacy/missing art üçün açıq status var, straight footprint uydurulmur. Redaktə, bazara çıxarma, delete, placement və mağaza bu oxu vahidində yoxdur.
- AppBackend yalnız read-only müqavilə/kod araşdırması: docs/wobbrik-game-api.md, internal/wobbrik/{market,city}.go, internal/platform/wobbrik_http.go. Mövcud API bu oxular üçün kifayətdir; server/schema/migration dəyişməyib, deploy/test edilməyib.

## Lokal review və düzəlişlər

- Same-UID yeni login-də bərabər Ready snapshot üçün köhnə destination state saxlanmır: identity key və regression test.
- Backend hər token/request cavabından sonra session epoch-u yoxlayır; UI də exact Ready-ni yoxlayır. Account dəyişəndə cavab tətbiq edilmir.
- City səhifələri fərqli revision/population-da birləşdirilmir; retry fresh ilk səhifədən başlayır. Inventory canlı keyset səhifələridir, Yenilə yenidən başlayır.
- İnvеntar cavabı snapshot-larla 64KiB-i keçə bildiyindən yalnız həmin bounded read üçün limit 1MiB-dir. Digər limitlər dəyişməyib.
- Lint unused BoxWithConstraints tapıntısı Box ilə düzəldildi.

## Yoxlamalar və sərhədlər

- Son koddan sonra `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS (Java 21). Yeni core InventoryReadsTest: 7 test; app-də identity regression testi var. Əvvəlki lint uğursuzluğu düzəldilib və yoxlama yenilənib.
- `git diff --check` PASS. Token mətn cütləri kontrast minimum 7.08; PASS. Avtomatik ümumi checker Raised fonunu mətn kimi tanıyır; real istifadə edilən cütlər ayrıca ölçüldü.
- CI işlədilməyib. Emulator/ADB/runtime və Android screenshot/render yoxlanmayıb; font 2.0/inset/a11y və dizayna birəbir vizual uyğunluq PASS deyil. UI implementasiyası vizual qəbul edilmiş sayılmır.
- Detail yalnız serverin base/max qiymətlərini göstərir; konseptdəki qiymət komponentləri/komissiya breakdown-u, tam dekorativ çərçivələr, şəhər/bazar/edit əməliyyatları bu ilk oxu vahidində tamamlanmayıb. Mərhələ 4 bütövlükdə tamamlanmayıb.
