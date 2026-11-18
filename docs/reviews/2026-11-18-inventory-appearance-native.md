# İnventar/Görünüş — native review

18 noyabr 2026. Branch `feat/inventory-appearance-redesign`, baza `c309673`. Yoxlanan scope bu review ilə saxlanan feature diff-dir. İstifadəçi `docs/design/inventory-appearance-v1/proposal.png` üçün “birə bir implement et” dedi. Yeganə plan `docs/PLAN.md` §1.1; ui-craft tətbiq edildi.

## Dəyişiklik və lokal review

Mobil `collection` route-u mövcud `CollectionInventoryShell` header/tab/navbarını istifadə edir. Beş native kateqoriya, native vector siluetləri, Açıq/Nailiyyət filtrləri, refresh və sabit key-li lazy kartlar tətbiq edildi. Normal portretdə iki, böyük şriftdə bir sütun; kateqoriyalar üfüqi, məzmun şaquli scroll olunur. Uzun mətn kəsilmir; son status navbarın yuxarısına sürüşdürülə bilir. Rəng/ölçülər GameTokens-dadır; açılma rəngi təsdiqli PNG-dən götürüldü. PNG ekran skin-i kimi işlədilmir.

Kartlar real qəbul edilmiş kataloqu, qiyməti və açılma vəziyyətini göstərir. PNG-dəki nümunə sırası/qiymətlər yeni domain qaydası deyil. İki mərtəbəli fasad material nümunəsi mövcud paylaşılan divar, pəncərə, balkon və garden sprite-larından Canvas-da qurulur; owned bina və ya qəbul edilmiş həndəsə deyil. Digər kateqoriyalar real seçilən hissəni göstərir.

Kart mövcud PartsScreen hissə detalını açır. Coin alışları əvvəlki PartsMutations → sole journal → server yolundadır. Açıq hissə ayrıca “Standart görünüş seç” əmri ilə əvvəlki hesab üzrə preference store-a yazılır; digər kateqoriyaların seçimi qorunur. Read/write IO dispatcher-dədir; exact Ready, UID/login session, katalog/unlock və bütün seçilən asset-lər yoxlanır. Disk xətası retry olunur. Başlanmış cəhdin dondurulmuş seçimi və binanın explicit draft/apply axını dəyişmir. Cached account/kataloq yalnız baxış göstərir; kart əməliyyatları bağlıdır. Planşet əvvəlki FinishCosmeticScreen yolundadır.

Review zamanı başqa materiala sızan purple trim çıxarıldı, material iki mərtəbəyə ayrıca çəkildi, kartdan qayıdanda scroll state saxlandı və əvvəlki bütün-seçim art yoxlaması qorundu. Yeni status formatteri mövcud hissə formatterini paylaşır. Owner iki release `.dm` və `2026-11-18-inventory-city-detail-costs.md` dəyişiklikləri toxunulmadan saxlanır.

## Yoxlamalar

Yekun nəticə: native5/5 və əlavə regular preview1/1; debug41/41, mock44/44 və fokuslu core8/8 unit PASS. assembleDebug, assembleMockDebug və lintDebug PASS. Dəqiq nəticələr `inventory-appearance-native/evidence/validation.txt`-dədir. Native host testləri production Compose komponentlərini sintetik owner/catalog ilə işlədib PNG yaradır: 390×844 və 320×568, fontScale1/2; 360×640 font2, inset və uzun balans. Filtrlər/empty, kateqoriyalar, real detal keçidi, preference disk failure/retry, offline disabled state, refresh və son uzun statusun dock-dan yuxarı sürüşdürülməsi yoxlanır. Renderlər açılıb təsdiqli PNG ilə müqayisə edilir. Kontrast6 cüt 4.5-dən yüksəkdir (`inventory-appearance-native/evidence/contrast.txt`).

İlkin fixture-də bir bootstrap sync və bir catalog çağırışı var. Görünüş kökü, filter/category, detal/back, preference retry və offline replay əlavə inventory/city/building/display sorğusu və payload yaratmır: 0 sorğu/0 əlavə byte. Explicit refresh bir sync + bir catalog çağırışıdır. Bu rəqəmlər sintetik read counter-lardır; bootstrap API byte ölçüsü deyil. Host fixture-də art preparation344–795ms, hazır owner/art-dan sonra ilk content767–1425ms, uğurlu preference yazısından qayıdış35–57ms müşahidə edildi; bunlar ayrıca host xərcləridir və production hədəfi deyil. Test XML stdout-dadır; scheduler-dən asılı millisecond limit assertion yoxdur. Cihaz/ART/frame/GPU və production latency nəticəsi deyil. Böyük hesab ayrıca load benchmark edilməyib; UI yalnız bounded kataloqla işləyir, bütün bina/şəhər kolleksiyasını oxumur.

Yeni bitmap decode yolu və per-card bitmap allocation yoxdur. Əvvəlki process-shared ApprovedArt atlasları və shell-in IO-da yüklənən iki dekor bitmap-i (~2.33MiB RGBA) paylaşılır; əlavə raster asset yaradılmayıb. Optional shell scenery mətn/kataloqu bloklamır.

AppBackend `internal/wobbrik/catalog-v1.json`, mövcud kosmetik contract və mobil PartsMutations uyğunluğu yoxlandı: API/DTO/schema/domain dəyişmir, backend dəyişiklik tələb olunmur. Real alış/backend hesabı bu vahiddə işlədilməyib. SKU/Console/deploy yoxdur.

## Vizual və runtime məhdudiyyətləri

Native kompozisiya təsdiqli PNG-yə əsaslanır. Şrift ölçüsü və faktiki kataloq sırası/mətnlər dinamiktir; mövcud oyun atlaslarından yığılan fasadın material/trim/bitki detalları generativ PNG-nin ayrıca illüstrasiyası ilə piksel-piksel eyni deyil. Tam piksel eyniliyi iddia edilmir. Böyük şriftdə bütün hündür kartın eyni anda görünməsi tələb olunmur; mətn və son status scroll ilə əlçatandır. Bəzi native host PNG-lərində logo və ya Play qatı görünmür, digər ölçü/state PNG-lərində görünür; Play semantics/layout yoxlaması keçir. Səbəbin host readback və ya runtime olması ayrılmayıb. Shared header/navbarın əvvəlki cihaz sübutu bu vahidin yeni runtime yoxlaması sayılmır. Bu qeyri-müəyyənlik və native/PNG art fərqləri səbəbindən tam vizual uyğunluq PASS deyil. Emulator açılmayıb; real hesab, TalkBack, klaviatura, API24 runtime və bütün locale kombinasiyaları yoxlanmayıb. Canlı yoxlama gate deyil.

Main merge, CI, remote push və deploy edilməyib.
