# İnventar — lokal məzmunun art/network-dən ayrılması / 17 noyabr 2026

Branch `fix/inventory-detail-latency`, baza `253f569`. Production diff: ApprovedArtLoading.kt, InitialAccountSync.kt, InventoryScreen.kt, OnlineGame.kt. CI/merge/push/deploy yoxdur.

## Səbəb və dəyişiklik

Root `needsBundledArt("inventory")` gate-i bütün İnventarı ApprovedArt hazırlığı bitənədək əvəz edirdi. Buna görə AccountViews-in əvvəlcədən qəbul edilmiş lokal page-i təqdim etməsi ekranı aça bilmirdi. İlkin hesab gate-i də read-only qəbul edilmiş hesab olsa belə əlavə “Menyuya keç” kliki istəyirdi.

İnventar artıq ümumi art gate-inə daxil deyil. Root mövcud hazırlıq status/retry-ni yalnız İnventarın illustration scope-una ötürür; kart məlumatı, naviqasiya, səhifə və detail oxuları davam edir. Art Loading/Failed ikən həmin illustration sahəsi mövcud ApprovedArtStatus göstərir; art düzələndə eyni BuildingPreview/renderer çəkilir. Native metadata raster-ə çevrilmir. Legacy detail-in kosmetik map çağırışı da art Ready olmadıqda sinxron loader-ə girmir. Qəbul edilmiş read-only hesab olduqda ilkin menyu avtomatik açılır; yeni login epoch qəbul edilmiş məlumatı yoxdursa əvvəlki giriş latchesini təkrar istifadə etmir.

Ayrıca qalan `ImageBitmap.imageResource(inventory_garden)` çağırışı main-thread-də67ms/6 291 456bayt ölçüldü. Purple renderer həmin bitmap-dən istifadə etmədiyindən çağırış çıxarıldı. Digər template-lər eyni resource-u mövcud rememberResourceArt ilə IO-da hazırlayır; yalnız illustration gözləyir. Palitra/layout/crop/parts/geometry dəyişmir. Art/root IO hazırlanması yenə~84MiB process atlas büdcəsinə malikdir; onun cold startup/device xərcinin həll edildiyi iddia edilmir.

## Əvvəl/sonra ölçü və məhdudiyyət

Production InventoryScreen/AccountViews/ReadOnlyGameCache/owner, SDK34 native Robolectric xxhdpi. Əvvəl boş Home məzmunu qurulub, sonra route dəyişilir: əvvəlki556ms ölçüsündən fərqli olaraq ilkin setContent ölçüyə daxil deyil. Transport synthetic, qəbul edilmiş inventory bundle revision1 üzrə əvvəl yazılır. Bütün fresh network cavabları gate ilə bağlanır. ApprovedArt bitmap-ləri testdən əvvəl hazırlanır, onun root statusu isə ayrıca idarə olunur: bu **real decode müddəti deyil**, art gate asılılığını izolə edən fixture-dir.

- Baza: injected art status1200ms sonra Ready olur; lokal kart **1384ms**, həmin anda şəbəkə hələ bağlıdır. Bu1384ms production/device gecikməsi və ya real atlas decode vaxtı deyil.
- Son source: art Loading və network bağlı qaldığı halda ilk host nümunəsi **375ms**; eyni suite-in sonrakı matrix halları **36–57ms**. İlk host grid/class/JIT qurulması ölçüdən çıxarılmayıb və375ms qənaətbəxş device nəticəsi sayılmır.
- Settle-dən sonra hər hal üçün iki remount: **13–70ms**, **0əlavəHTTP**. Bunlar fərqli ölçü/font/üslub nümunələridir; percentile və eyni device benchmark deyil.
- İlk lokal təqdimatda2əlavə request başlanır (inventory/city), cavablar blokludur. Məlumat həmin sorğuların bitməsini gözləmir. Fresh body-lər açıldıqda mövcud sync/revision bracket tamamlanır; traffic qənaəti iddiası yoxdur.
- Garden: ayrıca köhnə imageResource çağırışı67ms main-thread/6MiB; purple artıq ona müraciət etmir, digər template IO istifadə edir. Bu rəqəm ümumi app10+s gecikməsini izah etmir.

Raw evidence: inventory-local-first/evidence/{before,after-initial,matrix,final,garden}.xml. Son fixture `render/InventoryLocalFirstTest.kt`; ilkin before testi eyni root needsBundledArt şərtini istifadə edir, art1200ms gecikməsi var; son test art-ı lokal görünənədək heç açmır. Reproduce: `cd android-billing` sonra `./gradlew -I ../docs/reviews/inventory-local-first/render/render.init.gradle :app:testDebugUnitTest --tests '*InventoryLocalFirstTest' --offline --console=plain`. Garden ölçmə testi ayrıca köhnə çağırışı izolə edir; production call path-in qaldığı mənasına gəlmir.

## Lokal/server uyğunluğu

Yeni lokal oyun modeli, saxta bina və ikinci wallet yoxdur. Mövcud qəbul edilmiş bundle həmin repository/DTO parser və həmin renderer ilə replay edilir. UID/login epoch/revision/rules/Ready yoxlamaları, accepted geometry, sole journal və server authority dəyişmir. Stale revision bundle lokal göstərilmir. Keşdə olmayan və ya başqa revision-a aid məlumatı instant göstərmək üçün uydurma vəziyyət yaradılmır.

Native test şəbəkə və art Loading ikən cached detail-in “Binanı sil” və “Yalnız baxış” əməliyyatlarının disabled olduğunu, yalnız fresh building cavabından sonra silmə icazəsinin açıldığını yoxlayır.0command göndərilib. Art failure kart metadata-sını gizlətmir, retry var. Local→fresh eyni floor/name/parts/geometry input yolu saxlanır; city badge yalnız membership gəldikdə görünür (bu cached mülkiyyət dəyişməsi deyil). Hesab gate-inin yeni epoch-da köhnə giriş icazəsini daşımadığı yoxlanır.

Backend docs/wobbrik-game-api.md inventory owner scope, accepted snapshot və revision/epoch sərhədi ilə tutuşduruldu. API/DTO/domain/schema/authority dəyişmədiyindən backend dəyişikliyi tələb olunmur; AppBackend dəyişdirilməyib, backend testi işlədilməyib. Real HTTPS server latency ayrıca ölçülməyib; ayrı web service-in gecikmənin səbəbi olduğu barədə nəticə yoxdur.

## Yoxlama və vizual review

Native suite7/7PASS13s: cached route art/network bloklu, Loading→Failed→Ready, local→fresh,0HTTP remount, cached detail capability, yeni login epoch;320×568font1/2,360×640font1,390×844font2synthetic padding, ayrıca tam teal template. Normal core152/app37unit+assembleDebug+lintDebugPASS40s. Son source ilə əvvəlki InventoryDetailLatencyTest7/7PASS16s (evidence/detail-regression.xml). CI deyil.

Yeddi PNG açılıb baxıldı (320font2 loading-in kiçildilmiş surəti də açıldı); `01-main.png` İnventar authority-si ilə müqayisə edildi. Hazır vəziyyətdə bina/crop və native metadata eynidir; loading yalnız illustration içindədir, navbar qalır. Keş/şəbəkə statusları fresh haldan fərqlənir; accepted binanın başqa lokal dizaynı yoxdur. Tam PNG parity iddia edilmir: kiçik portretdə iki sətir dock, font2 horizontal tabs, uzun mətn/kiçik art miqyası borcu saxlanır. Renders rendered/ altındadır. Kontrast Text/Surface13.00, Muted8.22, Accent8.99PASS. Real OS inset/IME/locale/TalkBack/GPU/Long.MAX və device cold-start yoxlanmayıb; synthetic padding OS inset deyil. Emulator açılmayıb.

Diff review: Inventory scope digər route-ların art gate-ini zəiflətmir; preparation status root-dan gəlir, Loading zamanı atlas getter-lərinə girilmir. Garden worker config-scoped mövcud resource helper-dən istifadə edir; cancellation köhnə state-ə publish etmir. Cached data əməliyyat capability-si yaratmır. DiffcheckPASS; əlavə correctness tapıntısı yoxdur. İlk host375ms və real cihaz10+s/menyu2+s açıq ölçmə borcudur; app-wide performans tamamlanmış sayılmır.
