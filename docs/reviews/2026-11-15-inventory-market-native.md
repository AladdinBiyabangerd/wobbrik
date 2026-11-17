# PLAN §1.1 — native mobil İnventar / Bazar

Branch `feat/community-tabs-native-consistency`, baza HEAD `6c96e9c`; vahid working diff-dir. Başlanğıcdakı owner Home/City/Nailiyyətlər/Rekordlar/Profil/Parametrlər/token/mock/baseline/sənəd işi saxlanır. İndex boşdur; stage/commit/merge/push/deploy/CI yoxdur. Vizual authority `docs/design/unified-mobile-v3/{README.md,01-main.png,02-market.png,05-owned-and-listings.png}` və istifadəçinin bu chatdəki son screenshot düzəlişləridir.

## Dəyişiklik və son istifadəçi qərarı

İnventarın dörd native tabı Binalar/Görünüş/Dekor/Ərazi-dir. Dekor/Ərazi tabları alış deyil, server nüsxələri və snapshot-dakı daimi ərazi hüquqlarıdır. Başlanğıc sahə pulsuz hüquq kimi ayrıca göstərilir; offer siyahısı sahiblik siyahısı sayılmır. Sahiblik ekranındakı keçid uyğun Bazar alış tabına aparır. Dekor copy ID üzrə saxlanır; eyni item-in iki nüsxəsi birləşdirilmir. Səhifələmə, error/retry və read-only/cache göstəricisi var.

**Son screenshot göstərişi:** İnventar və Bazarın tablı kök ekranlarında geri oxu və “İnventar”/“Bazar” başlıq sətri çıxarıldı; boş hündürlük saxlanmır. Ümumi balans və 7-slot dock qalır. Detal/yeni elan öz contextual geri idarəsini saxlayır; Android BackHandler qalır. Bu qərar PNG-lərdəki əvvəlki kök başlıq sətrini əvəz edir.

Binalarda native relyefli iki sütunlu kartlar və real accepted geometry preview-u; font2-də bir sütun, uzun ad/rəqəm üçün horizontal scroll var. Bina detalında native hero, sahiblik/şəhərə aidlik/maksimum satış məlumatı, real görünüş və satış əməliyyatları var. Silmə explicit təsdiqi saxlayır; ikinci dərəcəli silmə idarəsi detal body scroll-undadır. Əsas əməliyyatlar body-dən kənardadır.

Bazarın Binalar/Dekor/Ərazi girişləri və Alış/Mənim elanlarım bölgüsü qoşulub. Alışda native bina grid-i, öz elanlarında native list/status/geri götürmə, yuxarıda Yeni elan var. Preview/detail/filter/cursor və exact Ready/UID yoxlamaları saxlanır. Elanı geri götür düyməsi təzə detail oxusundan sonra təsdiq açır; birbaşa mutation etmir. Back/refresh pending təsdiq niyyətini təmizləyir. Native elan forması, qiymət sahəsi, komissiya, alıcı və geri götürmə təsdiqləri mövcud `MarketMutations` qaydaları/callback-ləri ilə işləyir.

Dekor və ərazi alışları mövcud `CityPurchases.eligible/buy` ilə işləyir. Normal fontda seçim xülasəsi aşağı native paneldədir; böyük şriftdə təsdiq modalı body scroll + pinned Al/Ləğv et istifadə edir. Beləliklə kiçik ekranın chrome-u təsdiq düymələrini sıxışdırmır. Toolbar məzmunla scroll olur; zəruri vertical/horizontal scroll saxlanır.

Səth, kontur, gradient, tab, düymə və idarəetmə native Compose-dur. Palette/tree/layers/tag/cart/refresh/back/play siluetləri Canvas ilə çəkilir. Yeni rəng/ölçü tokeni, dependency/pin, dil və raster asset əlavə olunmayıb. Raster yalnız əvvəlki kompleks bina/dekor/ərazi artwork-üdür; PNG-dəki mətn və UI crop edilmir. Görünüş tabının mövcud native picker-i də böyük şriftdə bir sütuna keçir, item adlarını göstərir və eyni native action-lardan istifadə edir. Basılma animasiyası əvvəlki qərar üzrə ayrıca qalır.

## Backend və məhsul sərhədi

Backend-in `internal/wobbrik/shop.go` mövcud dekor response-u `position,omitempty` göndərir. Android `DecorationCopy` bu optional sahəni mövcud `CitySceneReads.position` ilə oxuyur: position var → Şəhərdə, yoxdur/null → Saxlanılır. Yeni request/API/rules/schema/migration/wallet yaranmır. Repository `GameplayReads` qəbul edir ki, hazır `AccountViews`/cache yolu təkrar istifadə olunsun. Oxular login/capability epoch-u və UID ilə yoxlanır, account dəyişəndə UI state sıfırlanır.

AppBackend dəyişiklik və backend test rerun tələb olunmur: server müqaviləsi dəyişmir; yoxlama read-only source inspection-dur. Alış/elan/withdraw əmrlərinin revision/dedup/wallet qaydaları əvvəlki domain/journal qatında qalır. PNG-dəki fontan, qiymətlər, ərazi hüceyrə sayları və gəlir yeni kataloq qaydası sayılmadı. Placement əvvəlki istifadəçi qərarı üzrə bağlıdır: saxlanmış dekorun Yerləşdir idarəsi disabled və səbəbi görünür; saxta placement callback-i yoxdur. İnventar detalının city oxusundan asılılığı mövcud davranışdır: city read uğursuzdursa xəta və retry görünür, sahiblik badge-i uydurulmur.

Legacy/tablet shop/picker yolları native mobile dispatch-dən kənardadır; tablet inventar qısayolları əvvəlki purchase route-larına gedir. Planşet renderi/implementasiyası bu vahidə daxil deyil.

## Yoxlamalar

Aralıq uğursuz turlar PASS sayılmır. Final yoxlamalar:

- Son istifadəçi başlıq/ox düzəlişlərindən sonra dedicated commerce5 + shared shell4 = **9/9 PASS1m29s**, failure/error/skipped0.390×844 və320×568,font1/font2;360×640,font2+simulated24/48dp inset+Long.MAX_VALUE balans+uzun bina adı. Root ekranlarda geri düyməsinin olmaması, detal/görünüş route-u, owned status+disabled placement, dekor error/retry, alış seçimi/cancel, elan qiyməti/enabled submit, withdraw təsdiqi/cancel və buy təsdiqi/cancel yoxlandı. Testlər iqtisadi submission sayının0 olduğunu yoxlayır; real alış/withdraw/deploy etmir. Native layout dəyişmədən daha sonra yalnız legacy tablet route/label guard-ı bərpa olundu; final APK/lint həmin son source ilə alındı.
- Core testlərinin bu vahiddə real rerun-u **138/138 PASS**, failure/error/skipped0. Yeni `DecorationOwnershipReadTest` mövcud optional position/null/absent və malformed coordinate/turn hallarını yoxlayır. Sonrakı task UP-TO-DATE olduqda rerun sayılmır.
- Kontrast Text/Surface13.00, Muted/Surface8.22, Background/Accent11.45, Text/Raised10.24 — **4/4 PASS**, minimum8.22. Yeni palitra yoxdur.
- Render init-i olmadan final normal debug + mockDebug APK, lintDebug və core task **PASS51s**; core bu son invocation-da UP-TO-DATE-dir. CI deyil. `git diff --check` PASS; index boşdur.
- Final65 ekran/dialog PNG-si və5 contact sheet `inventory-market-native/` altındadır. Beş ölçü/font/inset contact sheet-i və ayrıca normal İnventar/Bazar, font2 withdraw dialogu açılıb baxıldı;01-main/02-market/05-owned-and-listings ilə müqayisə edildi. Başlıq/ox üçün son screenshot qərarı tətbiq olunur. Bina preview-u cari accepted geometry/material ilə çəkilir, PNG nümunəsindəki mərtəbə sayını və saxta qiyməti təkrarlamır.

## Lokal review və məhdudiyyətlər

Scoped review production route/dispatch, cache/session guard, copy/page identity, native/legacy sərhədi, enabled state, confirmation cancel, body/action ölçüsü, import və feature yerləşməsini yoxlayır. Domain qaydaları UI-də təkrarlanmayıb; sintetik fixture production adapter kimi qoşulmayıb.

İlk native turda unbounded nested horizontal scroll exception oldu; label eninə mövcud token limiti verildi. Sonrakı font2 turunda aşağı inline alış paneli düymələri sıxışdırdı; böyük şrift təsdiqi ayrıca native modal-a keçirildi. Long balance+inset halında toolbar pinned action-ı48dp-dən kiçildirdi; toolbar body scroll-a köçdü. Testlərdə lazy-list offscreen node-u gözləmək və qəsdən city503 qaytaran shared fixture-də detal açmaq fərziyyələri düzəldi. Shared fixture-in şəhər error/retry/read-budget regression-u saxlanır; sağlam city ilə real detal/görünüş callback-i dedicated commerce host-da yoxlanır.

Host render sintetik HTTP/snapshot-lardır, real hesab/device E2E deyil. Emulator açılmır. Real IME/inset/TalkBack/API24/GPU/performance və əlavə locale/runtime matrisi yoxlanmayıb. Dialog PNG-ləri ayrıca pəncərə capture-ıdır; şəffaf xarici sahə viewer-də qara görünə bilər. Font2-də aşağı faktlar, kartlar və xülasə scroll-dadır; uzun etiket/rəqəmlər horizontal scroll olur. Mövcud real oyun artwork-ü PNG-dəki nümunə binaların material/pozalarını və ərazi illustration-unu piksel eyni təkrarlamır. Native relyef/işıqlandırma və bütün state-lər üçün tam piksel pariteti iddia edilmir. Bütün tətbiqin dizaynı və PLAN§1.1/mərhələ qəbulu deyil; əvvəlki Profil artwork məhdudiyyəti də açıq qalır.
