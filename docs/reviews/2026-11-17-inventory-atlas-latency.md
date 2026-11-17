# İnventar — paylaşılan purple atlas / 17 noyabr 2026

Branch: `fix/inventory-detail-latency`; bu vahidin bazası `4fbd615`. Production diff: yalnız `ApprovedArt.kt` və `PurpleBuildingArt.kt`. CI/merge/push/deploy yoxdur.

## Ölçülən səbəb və düzəliş

`BuildingPreview` hər kart üçün `rememberPurpleBuildingArt` çağırırdı. Onun composition-local `remember(context)` bloku hər mount-da dörd PNG-ni main thread-də decode edirdi. Digər game/window/cosmetic atlasları artıq process üzrə paylaşılırdı; purple qrupu həmin hazırlıqdan kənarda idi. Dörd purple PNG-nin fayl ölçüləri cəmi 4 840 679 bayt, decoded RGBA allocation cəmi 25 161 744 baytdır. Fayl ölçüsü stream-I/O telemetry deyil.

Purple loader mövcud `ApprovedArt` immutable APK-art keşinə qoşuldu. Mövcud authenticated root IO hazırlığı və art gate-i artıq bu qrupu da hazırlayır. Kartlar və list/detail remount-ları eyni map/bitmap-ləri alır. Asset, crop, renderer, layout, geometry, account/cache/Ready və HTTP yolları dəyişmir. Eager hazırlıq indi bir dəfə əlavə purple decode edir və process ömrü boyu ~24 MiB saxlayır: bütün qruplar üçün əvvəlki ~60 MiB əvəzinə ~84 MiB. Dörd paralel kartda purple retention ~96→24 MiB olur. Cold startup müddəti ayrıca ölçülməyib; bu xərc yox edilməyib, IO-da bir dəfə hazırlanmağa keçirilib. Preparesiz birbaşa composable çağırışı digər mövcud art helper-ləri kimi sinxron fallback saxlayır; production root əvvəl hazırlayır.

## Müqayisəli host ölçüsü

Robolectric SDK34 native graphics, xxhdpi. Production loader və InventoryScreen/AccountViews/cache/owner; synthetic transport: inventory40ms, city bloklu. Eyni iki test əvvəl baza source ilə, sonra son source ilə ayrıca işə salınıb. Hər test dəsti əvvəl `ApprovedArt.prepare` işlədir; bu route-after-preparation ölçüsüdür, cold startup/device deyil. Host thread CPU JDK ThreadMXBean ilə ölçülür.

- Əvvəl purple helper hər kartda **84–90ms wall / 84–88ms CPU**, 4 yeni bitmap / **25 161 744 bayt**. Dörd kart + bir remount: 32 fərqli bitmap / **201 293 952 bayt** müşahidə edilmiş allocation; bu retained heap/GC peak ölçüsü deyil.
- Sonra helper hər kartda **<1ms wall/CPU** (tam millisekund çıxışında0). Bütün səkkiz mount eyni **4 bitmap / 25 161 744 bayt** paylaşır. İlk kartın `newBitmaps=4` çıxışı əvvəlcədən IO-da hazırlanmış bitmap-lərin ilk identity müşahidəsidir; həmin anda dörd yeni decode iddiası deyil. Sonrakı kart/remount-lar0 əlavə bitmap müşahidə edir.
- Root ilk kart **905→556ms**, həmin anda hər ikisində **3HTTP / 2170response UTF8 bayt / 1city request**. Tək müqayisəli nümunədir, percentile deyil; JIT/host scheduling təsiri var. Bildirilən device10+s və ümumi menyu2+s həll edilmiş sayılmır.
- Səkkiz paralel IO hazırlığı eyni map və dörd bitmap qaytarır: fokuslu test PASS. Keşə hesab və Activity daxil edilmir; yalnız bundled art saxlanır. Yalnız tam uğurlu loader nəticəsi keşə yazılır; failure/retry mövcud root mexanizmi ilə qalır.

Raw sübut: `inventory-atlas-latency/evidence/{baseline,after,matrix,concurrency}.xml`. Reproduce: `cd android-billing` sonra `./gradlew -I ../docs/reviews/inventory-atlas-latency/render/render.init.gradle :app:testDebugUnitTest --tests '*InventoryAtlasLatencyTest' --offline --console=plain`. Timing müqayisəsi üçün yalnız `purpleDecodeMounts` və `rootTiming` seçilir. Fixture ilk kartı semantics ilə müəyyən edir, display/GPU təqdim olunma timestamp-i deyil.

## Vizual baxış və yoxlamalar

Dörd native root PNG açılıb baxıldı:320×568font1/2,360×640font1,390×844font2 və synthetic inset padding. `01-main.png` İnventar istinadı ilə müqayisə edildi. 360×640 əvvəl/sonra PNG SHA256 eynidir: `f67886cf97dd960fa4a751c2ba65d4fb61634cb99d82f6fecb10773b9d73cc89`. Çıxışlar `inventory-atlas-latency/rendered/` altındadır. Raster/art/crop/layout dəyişməyib; yeni dizayn tətbiq edilməyib. Mövcud kiçik-portret iki sətir dock, böyük şrift üçün horizontal tab scroll və kartlarda illustration miqyası/paritet borcu qalır. Tam PNG parity iddia edilmir.

Kontrast: Text/Surface13.00, Muted/Surface8.22, Accent/Surface8.99 PASS. Palitra/token dəyişməyib. Long.MAX/inset/locale/IME/TalkBack/GPU bu root fixture-də yoxlanmayıb; synthetic padding real OS inset deyil. Emulator açılmayıb.

Normal core/app unit + assembleDebug + lintDebug PASS40s (`evidence/checks.log`); bu CI deyil. Native atlas suite5/5, concurrency1/1 PASS. Mövcud InventoryDetailLatencyTest7/7PASS19s: cached display-only/fresh mutation gate, city-dən asılı olmayan detail, list↔detail sorğu dedup və beş detail matrix halı (`evidence/detail-regression.xml`).

## Lokal diff review və sərhəd

Diff üzrə loader yalnız AssetManager qəbul edir; Activity/reference və account məlumatını process keşinə daşımır. `@Synchronized` eyni qrupun paralel duplicate decode-unu qarşılayır; prepared flag purple uğurundan sonra publish edilir. Renderer və metadata parser məzmunu saxlanır. Source SHA256: ApprovedArt `2a83cceddd4c7f7f030c197b8ed937485a335c5e27f6be2ecc544d8b294ed83c`, PurpleBuildingArt `a3e9397614e493a3bd975a970c7264631c77c13d83a63486d0745b43fceafd2b`. Diffcheck PASS; əlavə correctness tapıntısı yoxdur.

Yalnız bundled PNG decode/retention dəyişir; Inventory/City API/DTO, wallet/ownership/domain/schema dəyişmir. Backend dəyişikliyi tələb olunmur, AppBackend dəyişdirilməyib və backend testləri bu vahiddə işlədilməyib. Qalan performans işi: real cihaz/production route gecikməsi və payload ölçüsü, digər BuildingPreview resource işi, Profile CPU və cold startup/memory büdcəsi. Bu vahid bütün app performansının və redesign-ın qəbulu deyil.
