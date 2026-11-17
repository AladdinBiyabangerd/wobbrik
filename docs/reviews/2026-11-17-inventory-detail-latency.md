# İnventar detalı — gecikmə və lokal review / 17 noyabr 2026

Branch: `fix/inventory-detail-latency`, baza `02fbf5a` (HTTPS fix daxil). Yoxlanmış production diff yalnız `InventoryScreen.kt`; son source SHA256 `6343b6f474725839e50ad0a7f1909f33be6c91669e6482054e25a95567309cbf`. Main merge/push/deploy/CI yoxdur.

## Səbəb və düzəliş

Əvvəl detail bina oxusundan sonra şəhərin bütün membership səhifələrini gözləyirdi. Siyahıdan detala keçid optional city effektini ləğv edib detail daxilində yenidən city oxuyurdu. İndi membership ayrıca, yalnız Ready/retry ilə bağlı effekt və mövcud `inventory-city` açarı ilə işləyir; seçim və pagination onu yenidən başlatmır. Bina öz oxusu tamamlananda göstərilir. Membership heç vaxt əməliyyat səlahiyyəti vermir.

Detail mövcud `detail-<id>` bundle açarını saxlayır: köhnə building+city bundle-dan eyni building path replay edilir. Yeni capture yalnız building path və əvvəlki accepted revision sync bracket-i saxlayır. `onLocal` əvvəlcə display-only məlumatı göstərir; yalnız fresh nəticə cached bayrağını götürür. Exact Ready/current-session yoxlamaları və sole journal/server authority dəyişməyib.

## Müqayisəli ölçü

Production InventoryScreen/AccountViews/ReadOnlyGameCache/owner; synthetic transport: building/inventory 40ms, city gate detail seçildikdən 1200ms sonra açılır. Eyni host test `routeTiming`, baza source müvəqqəti bərpa edilərək ayrıca ölçülüb, sonra son source qaytarılıb. Bu bir əvvəl/sonra nümunəsidir, percentile və device ölçüsü deyil.

- Əvvəl detail ilk məzmun: **1408ms**, həmin anda 6HTTP/1623response UTF8 byte/2city request.
- Son diff: **197ms**, həmin anda 5HTTP/1504byte/1city request.
- Son diff tam settled: **6HTTP/1898byte/1city request**. Total HTTP azalmayıb, byte artıb: əvvəldə list city capture ləğv olunduğu üçün onun son sync bracket-i tamamlanmırdı; indi optional city capture da tamamlanır. İddia daha tez detail görünməsi və təkrar membership oxusunun aradan qalxmasıdır, ümumi traffic qənaəti deyil.
- Dörd ilkin render testində və son beş matrix halında city cavabı bloklu ikən həm siyahı, həm detail görünür. Detail→siyahı→detail eyni epoch daxilində əlavə HTTP yaratmır. Köhnə bundle əvvəl göstərilir, şəbəkə bloklu ikən silmə disabled-dir, fresh bina cavabından sonra aktivləşir. Heç bir command göndərilmir.

Raw sübut: `inventory-detail-latency/evidence/{baseline,after}.xml`; müqayisə fixture-i `inventory-detail-latency/render/InventoryDetailLatencyTest.kt`. Normal yoxlama log-u `evidence/checks.log`.

## Yoxlamalar və vizual baxış

- Son production source ilə normal core152/app37unit + assembleDebug + lintDebug PASS29s. CI deyil.
- Son native opt-in host suite7/7PASS18s: timing, legacy cache authority, 320×568font1/2, 360×640font1, 390×844font2insets və Long.MAX balans. Hər matrix halında silmə düyməsinə scroll və geri/təkrar detail keçidi yoxlanıb.
- Beş PNG açılıb baxıldı; `02-market.png` bina detalı istinadı ilə müqayisə edildi. Layout/token/art dəyişmir. Mövcud native kartlar/illustration/başlıq iyerarxiyası saxlanır; tam dizayn pariteti iddia edilmir. Font2-də uzun scroll, kiçik portretdə iki sətir dock və Long.MAX header clipping borcu qalır. Insets fixture token padding-dir, real OS inset/IME sübutu deyil. Tablet/landşaft/emulator yoxlanmayıb.
- Kontrast: Text/Surface13.00, Muted/Surface8.22, Accent/Surface8.99 PASS. Palitra dəyişməyib.
- Lokal diff review: köhnə bundle üçün yeni açar yaratmaq ilkin yanaşması düzəldildi; mövcud açar və exact path saxlanaraq offline uyğunluq qorundu. İstifadəsiz mobileShell dəyişəni silindi. Son diffcheck PASS. Əlavə konkret correctness bug tapılmadı.

## Backend uyğunluğu və qalan iş

AppBackend `wobInventory`→`Store.Inventory` və `Store.City` oxu axını yoxlanıb: owner/deleted filtrləri, limit/cursor və read revision müqaviləsi dəyişmir. Bu client scheduling vahidi üçün API/DTO/schema/domain/backend dəyişməsi lazım deyil; backend testləri bu vahiddə işlədilməyib. Yeni server performansı iddiası yoxdur.

Root İnventarın bildirilmiş10+s və ümumi menyuların2+s device gecikməsi ölçülməyib və həll edilmiş sayılmır. Inventory bina kartlarının sinxron BuildingPreview art/atlas decode işi, atlas retention, Profile foreground/skin CPU işi və real payload ölçüləri növbəti ölçmə vahididir. Art/atlas dəyişməsi burada edilmir. Production latency, OS inset/IME/TalkBack/GPU/locale matrix açıqdır.

Təkrar native yoxlama: `cd android-billing` sonra `./gradlew -I ../docs/reviews/inventory-detail-latency/render/render.init.gradle :app:testDebugUnitTest --tests '*InventoryDetailLatencyTest' --offline --console=plain`. PNG-lər `/private/tmp/wobbrik-inventory-detail/` altında çıxır.
