# İnventar/Dekor — native implementasiya

18 noyabr 2026. Branch `feat/inventory-decor-native`, baza `31d4bfe`. Review scope: `InventoryDecorScreen.kt`, `OwnedCityItemsScreen.kt`, `CityPurchaseArt.kt`, `MobileMenuShell.kt`, üç `inventory_decor_*` vectoru, fokuslu test və dizayn/handoff sənədləri. Owner-in iki release `.dm` və `2026-11-18-inventory-city-detail-costs.md` dəyişiklikləri scope xaricində saxlanıb.

İstifadəçi `docs/design/inventory-decor-v1/proposal.png`-ni implementasiya üçün təsdiqlədi. Təkrar dizayn təsdiqi istənilmədi. Dekor mövcud kolleksiya header/navbarına qoşuldu. Hamısı/Ağac/Skamya/İşıq/Yol yerli filtrləri, normal şriftdə iki sütunlu stable-copy-ID lazy kartlar, böyük şriftdə bir sütun, yeniləmə, səhifələmə və daimi Bazar keçidi native Compose-dur. İllüstrasiya mövcud atlasdan gəlir; səth/mətn/idarəetmə PNG skin deyil. Hər kart bir real server nüsxəsidir; ad kataloqdan, Şəhərdə/Saxlanılır statusu yalnız `DecorationCopy.position`-dan gəlir. Eyni növdən ayrı ID-li nüsxələr birləşdirilmir. Filtr serverə yeni sorğu göndərmir, yalnız yüklənmiş səhifələrə tətbiq edilir; Daha çox əlaqəsiz filtrdə də əlçatandır, filtr boş mətni bunu açıq deyir. Yeni placement, ərazi girişi, alış əmri və domain qaydası yoxdur.

Keşin `onLocal` callback-i həmin account/login/revision sərhədində ilk accepted məzmunu şəbəkəni gözləmədən göstərir. `onFresh` keçmiş cache nişanını silir. Refresh ilk səhifəyə qayıdır və köhnə pagination quyruğunu əvəz edir; cursor işləri Compose effect cancellation-ına tabedir. Atlasın yeni Dekor yolu IO dispatcher-də bir dəfə mount üzrə decode edir, kart/recomposition üzrə bitmap yaratmır. Digər mövcud atlas istifadəçilərinin sinxron davranışı dəyişdirilməyib. Yeni proses cache/framework yaradılmayıb.

## Yoxlamalar və review düzəlişləri

- `:app:assembleDebug`, `:app:lintDebug` PASS.
- `DecorationInventoryReadsTest`1, `AccountViewsTest`10, `AccountViewsReadOnlyTest`8 və `DecorationOwnershipReadTest`1:20 fokuslu test PASS. İlk20 copy fixture:2 sorğu (bounded `decorations?limit=20` + authority final-sync),1149 cavab baytı. Eyni capability-də üç təkrar giriş:0 əlavə sorğu/byte; offline:0; refresh:2. Accepted cache şəbəkə gate-i açılmamış publish edilir, başqa UID-yə replay rədd edilir. Bu JVM fixture ölçüsüdür; production latency, böyük hesab/ART/frame/GPU ölçüsü deyil. City və kart başına oxu testdə qadağandır.
- Emulatorsuz native Robolectric/Compose render matrisi:390×844/320×568 fontScale1/2,360×640 font2 inset/Long.MAX_VALUE balans;5/5 PASS. Filtr, Daha çox, Bazar, refresh/retry, cache/error/empty/loading yoxlanıb. Əlavə uzun dekor adı/font2/inset fixture-i ayrıca1/1 PASS, render açılıb baxıldı; sözlər bütöv sətirə keçir.
- `ui-craft` kontrastı6/6 PASS; minimum7.41:1. Sübut `inventory-decor-native/contrast.txt`.
- Renderlər açılıb `proposal.png` ilə müqayisə edildi; `inventory-decor-native/screens/`. İlk mərhələdə böyük şriftlə CTA mətni kəsilirdi; native pill və bütöv sözlərlə wrap ilə düzəldildi. Host testin lazy ekrandan kənar state axtarışları explicit grid scroll ilə düzəldildi; əvvəlki uğursuz sınaqlar PASS kimi təqdim edilmir. Final5/5 matrisi bu düzəlişlərdən sonradır.
- Lokal diff review və `git diff --check` aparıldı. UI/data ayrımı, kataloq adı lookup üçün bir indeks, copy ID key-ləri, filtrdə əlavə oxunun olmaması yoxlandı.

## Backend uyğunluğu

AppBackend `internal/wobbrik/shop.go` içində `DecorationCopy{ID,ItemID,Position}` və `Store.Decorations` yoxlandı. Owner-scoped keyset query eyni bounded səhifədə placement-i LEFT JOIN edir. Mobil repository20 limit saxlayır; bütün City kolleksiyası və N+1 tələb olunmur. API/DTO/domain/schema/migration dəyişməsi lazım deyil; backend repository dəyişdirilmədi, backend test/SQL-plan ölçüsü bu UI vahidində yenidən işlədilmədi.

## Açıq vizual məhdudiyyət — birəbir uyğunluq PASS deyil

Mövcud `city/decor-isometric.png` proposal illüstrasiyalarının asset dəsti deyil: xüsusən skamya/fənər forması, ağac altlığı/çiçəklər/yol kompozisiyası fərqlidir. Kiçik crop rezolyusiyası və mövcud `PurchaseArt`-ın upsample etməmə sərhədi art-ı kartda referansdan kiçik göstərir. Bu fərq renderdə açıq görünür; uyğun yüksək keyfiyyətli ayrıca dekor asset-ləri olmadan birəbir nəticə iddia edilmir. Mövcud kolleksiya chrome-u təkrar istifadə olunub; normal kart/filtr və header nisbətləri də PNG ilə piksel-paritet deyil. Kiçik font2 viewport-da header/dock/CTA çox yer tutur; məzmun scroll-la açılır, bütün kartlar ilk görünüşə sığmır. Üfüqi kateqoriya/tab scroll-u və native navbarın mövcud yığcam/wrap davranışı qalır.

Bu, funksional native Dekor UI/data vahididir, tam vizual qəbul və ya bütün app redesign tamamlanması deyil. Real hesab, cihaz/ART/GPU/production şəbəkə, TalkBack və canlı axın yoxlanmayıb. Emulator və CI işə salınmayıb; merge/push/deploy yoxdur. Sonrakı iş yalnız Dekor asset və vizual paritet fərqinin bağlanmasıdır; başqa ekran başlanmır.
