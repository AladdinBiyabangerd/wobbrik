# Mərhələ 5 — Android bazar inteqrasiyası, 14 noyabr 2026

Branch `feat/market-leaderboard`, baza `9653b98`; yoxlanmış vahid working diff-dir, commit/merge/push/deploy yoxdur. İrsi `docs/PLAN.md`, handoff, release və untracked purple PNG dəyişiklikləri qorunub. Bu qeyd mərhələ 5 qəbulu deyil: rekordlar/profil editoru hələ hazırlanmayıb.

## Müqavilə və inteqrasiya

Oxunub: cari handoff, `docs/PLAN.md` §§1–2 və §4 bazar/rekord qaydaları; AppBackend `docs/wobbrik-game-api.md` bazar/profil/leaderboard bölmələri, `internal/wobbrik/market.go`, `docs/reviews/2026-11-14-wobbrik-public-profiles.md`. Bazar üçün yeni backend müqaviləsi/schema tələb olunmur. Backend profil branch-i hələ main/deploy deyil; həmin review-un PostgreSQL PASS nəticələri bu Android vahidinin canlı inteqrasiya sübutu deyil.

- `MarketRepository` aktiv və öz elan tarixçəsini, cursor-u, mərtəbə/qiymət/part süzgəclərini və aktiv elan detalını mövcud API-dən oxuyur. Bağlı öz elanlarında serverin verdiyi satış/geri-götürmə statusu göstərilir; satılmış bina üçün saxta preview yaranmır.
- `MarketMutations` və üç yeni route sole `DiskAttemptJournal` / `AuthenticatedRecoveryOwner` / `AttemptCoordinator` vasitəsilə işləyir. Body/operation ID/account revision/listing və building revision exact retry-da dəyişmir. Şəbəkə/naməlum server xətası journal-ı saxlayır; sənədləşdirilmiş rədd/revision conflict yeni qərar üçün sync tələb edir. Client debit, grant, avtomatik rebase, ikinci wallet/outbox yoxdur.
- Receipt yoxlaması committed account revision, listing/building ID və revision, qiymət və 5% yuxarı yuvarlaq komissiyanı yoxlayır. Elan dərcində əvvəlcədən maksimum qiymət və satıcıya xalis gəlir göstərilir. Alış və withdrawal ayrıca təsdiqlənir; satın alınmış bina satış seçimindən çıxarılır. Beş elan/creator/ownership/race qaydalarında server authority qalır.
- Bazar route-u, alış/elanlarım tab-ları, canlı detail, filtr forması, öz inventarından satış seçimi, inventar detalından birbaşa satış forması qoşulub. Destination və nəticələr exact Ready/login capability-yə bağlıdır; hesab dəyişəndə köhnə coroutine nəticəsi qəbul edilmir. Müvəffəqiyyət sonrası bərpa yeni server snapshot-u və yenidən oxunuş verir.
- Aktiv kart preview-ləri detail API ilə alınır, maksimum 4 paralel read. Bir səhifə əlavə 20 detail sorğusu yarada bilər; bu latency/performance ölçülməyib. Elan oxular arasında bağlananda kart fresh detail/retry təklif edir, köhnə preview-dan alış əmri göndərilmir.

## Vizual əsas və sərhəd

İstifadəçinin artıq təsdiqlədiyi `docs/design/mobile-v1/inventory-market-v2.png` üçüncü ekran əsas götürülüb; yenidən təsdiq istənilməyib. Mövcud `GameTokens` və `MobileUiSizing` istifadə olunub, yeni palette/spacing/radius pinləri yoxdur. Qaranlıq fon, cyan seçilmiş tab/action, sağ metadata və sol real bina preview-li kartlar, Şəhər/İnventar/Bazar alt naviqasiyası qorunur. Binalar son təsdiqli production `BuildingPreview` renderer-i ilə real selected parts və accepted geometry-dən çəkilir; konseptdəki illüstrativ bina və qiymətlər saxta domain məlumatına çevrilmir.

Renderlər `market-visual/` altındadır: hər ölçüdə `market`, `detail`, `sale`, `inventory-sale-entry`. 360×640 font1, 390×844 font1, 320×568 font2. Kiçik/font2-də naviqasiya iki sıraya keçir, dekorativ ikonlar yığcamlaşdırılır, mətn kəsilmir; əsas geri/naviqasiya/form təsdiqi scroll-dan kənarda qalır. Bazar kartları və ikinci dərəcəli form məzmunu lazım olanda scroll olunur. Qiymət/min/max/görünüş filtrləri konseptin göstərmədiyi real funksional məzmunla eyni token chrome-unda verilib.

Native Compose host production composable/art/repository-ni synthetic HTTP fixture ilə işlədir. Bu, cihaz və ya real hesab E2E deyil. PNG və renderlərə baxılıb, lakin tam piksel eyniliyi iddia edilmir: token chrome-u və production modular bina şəkilləri konsept bitmap-in ayrıca raster kəsiyi deyil. Konseptdəki cartoon avatar/podium ayrıca production asset kimi yoxdur; sadələşdirilmiş fiqur/placeholder ilə əvəz edilməyib. Rekordlar ekranı bu vahiddə qoşulmayıb.

## Lokal review və düzəlişlər

Wire whitelist, typed envelope/journal round-trip, mutation rejection boundary, receipt korrelyasiyası, account epoch/current Ready, filter+cursor state, live listing closure, initial sale intent-in bir dəfə istifadəsi, satın alınmış bina eligibility-si, callback/back və min48dp target-lər yoxlanıb. Tapılanlar branch-də düzəldildi:

- Yeni enum route-ları historical və engine attempt-only fixture-lərində açıq rədd edilir; əvvəlki versiya dəstəyi uydurulmur.
- Dialogun wrap/weight ölçüləndirməsi native host-da layout dövrü yaratdı; bounded portrait dialog + ayrıca scroll body/fixed footer tətbiq olundu. Əvvəlki iki ilişmiş run PASS sayılmır; yalnız təsdiqlənmiş Gradle test worker-ləri dayandırılıb.
- Font2 alt `İnventar` clipping-i iki sıraya adaptasiya ilə düzəldildi. Native no-wrap paragraph-ın tam container width flag-i glyph overflow ilə eyni deyil; yoxlama real line glyph span/ölçünü ölçür və görüntüyə baxılır.
- İstifadəçi böyük boş sahəli sale screenshot-unu dəyişməyi istədi. Sale dialog hündürlüyü mövcud token pillələrindən ayrıca `GameTokens.Market.SaleDialogHeight` ilə yığcamlaşdırıldı; fontScale artanda available portrait height limitinə qədər böyüyür. Filtr dialogunun ayrıca böyük məzmunu öz bounded ölçüsündə qalır. Dialogun parent fontScale-i ayrıca ötürülür: synthetic font2 fixture-i artıq dialog daxilində də real böyük şriftlə çəkilir; uzun body və komissiya mətni lazım olanda scroll ilə açılır, təsdiq/ləğv footer-i sabitdir.
- İnventardan gələn initial building sorğusu fresh read-dən sonra consume olunur; uğurlu mutation sonrası form təkrar açılmır.
- Bütün cari səhifə binaları traded olduqda boş satış seçimi üçün izah göstərilir. `listing_conflict` / `invalid_request` ümumi recovery mətni market əməliyyatlarına da uyğundur.

## Yoxlamalar

- Adi Java21/offline `:core:test :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`: PASS; core123 (bazar6), engine95, app24; final source üzrə PASS26s.
- Native render `MarketRenderTest`: 3 ssenari; kart/detail, alış confirm cancel, öz elan oxusu, sale input/fee, invalid boş qiymət disabled, inventar→seçilmiş binanın sale formu və intent consumption, naviqasiya callback/glyph ölçüsü. Final3/3PASS26s, istifadəçinin son sale ölçü düzəlişi və dialog daxilində parent fontScale preservation daxildir; `market-visual/checks.txt`.
- Core bazar6: hər üç route üçün durable restart/lost response exact retry; foreign identity/fee/revision/envelope rejection; definitive vs ambiguous failure; filter/cursor/path injection; duplicate/closed public reads; same-UID login epoch-da stale command rəddi.
- Token kontrast4/4 PASS: Text/Surface13.00, Muted/Surface8.22, Background/Accent11.45, Danger/Surface8.37. Palette dəyişməyib. `git diff --check` PASS.
- CI istənilməyib/işlədilməyib. Emulator açılmayıb. Runtime/device, real iki hesab market E2E, API24/GPU/inset/klaviatura/TalkBack, bütün locale/state/filter/paging race kombinasiyaları yoxlanmayıb. Empty/error/closed-history UI-nin bütün renderləri yoxdur. Mobil portret qərarına görə planşet/landşaft işi açılmayıb.

Növbəti vahid: təsdiqli `play-progress-v2.png` əsasında leaderboard all/weekly cursor traversal və account epoch, profil GET/PUT/revision conflict, bundled builder/pilot/architect və podium asset-ləri. Yeni dizayn təsdiqi istəmə; saxta rəqəm/avatar/podium ilə production ekran göstərmə. Yeganə plan `docs/PLAN.md` olaraq qalır.
