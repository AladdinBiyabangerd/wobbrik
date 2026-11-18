# İnventar — bounded membership və page cache / 18 noyabr 2026

Android `fix/inventory-bounded-membership-cache`, baza `6bb1457`; AppBackend `feat/wobbrik`, baza `32ad8f7`. Əvvəlki 3001-membership regression-u bu işdə yeni uğurlu cache gözləntisinə uyğun saxlanır; əvvəlki review/handoff əlavələri tarixi qeydlərdir. Owner iki release `.dm` binarisi qorunur, scope-a daxil deyil. Main merge/CI/push/deploy yoxdur.

## Dəyişiklik

- İnventar route-u artıq `inventory-city` oxumur. List/detail/pagination/remount/refresh yalnız öz bounded bina oxusunu edir. City repository-si şəhərin öz ekranı və legacy cache regression-ları üçün saxlanır; bütün şəhəri oxumaq İnventar asılılığı deyil.
- Backend `view=display` inventory və owner building detail-ə optional `in_city:boolean` əlavə edir. Eyni SQL statement snapshot-ında `(app,owner_uid,id)` üzrə mövcud city index-i ilə scalar `LIMIT 1` probe; population aggregate, city paging və əlavə SQL roundtrip yoxdur. Legacy representation sahəni omit edir. Membership owner/non-deleted building query-sindən gəlir; authority/mutasiya deyil. Wallet/domain/schema/migration/rules/revision/dedup dəyişmir.
- Mobil strict boolean parse edir; sahə yoxdursa naməlum status saxlanır. Köhnə server/cache üçün tam şəhər fallback yoxdur; yeni backend deploy edilənədək status naməlum qala bilər. Yeni field əvvəlki reader-lər üçün additive-dir. Mövcud server revision bracket/fresh mutation gate/UID-login izolyasiyası saxlanır.
- İnventar artıq əvvəlki kimi hər page/detail-i ayrıca cache key-də saxlayır. Tam şəhər paketini bu key-lərə yığmır. Əlavə chunk/manifest formatı yaradılmadı: mövcud atomik `.new`/rename/fsync və format v1 saxlanır. Cache üçün ayrıca 8 MiB bounded file büdcəsi ayrıldı; bu, 1 MiB wire body-nin wrapper/JSON escaping xərcini də qarşılayır. `DurableJournalFile` default 140000-byte həddi, sole command journal, pending result və digər intent store-lar dəyişmir. Android cache factory-yə böyük büdcə açıq ötürülür. Yeni fayl adı/schema və ikinci wallet/journal yoxdur.
- Owner building detail transport-u inventory kimi 1 MiB qəbul edir; əvvəlki 65536-byte həddi çıxarılır. Sonsuz payload dəstəyi yoxdur: 1 MiB wire / 8 MiB complete-cache sərhədləri qalır; arbitrary scene və digər çoxcavablı view-lar ayrıca scope-dur.

## Sübut və yoxlamalar

Real disposable PostgreSQL18 + authenticated HTTP middleware/synthetic identity, 1001 bina, race-enabled lokal nümunə:

- Köhnə City100 membership oxusu + sync: 12 HTTP / 209 SQL / 94387 byte / 1.701s.
- Yeni inventory20 display page + sync: 2 HTTP / 44 SQL / 9028 byte / 23.05ms; 20 bina üçün inline status baseline ilə bərabərdir. Bunlar eyni məhsul nəticəsinin fərqli scope-larıdır: əvvəlki rəqəmlərə ayrıca inventory page daxil deyildi. Tam Android production end-to-end sürətlənmə nisbəti deyil.
- Legacy City20 müqayisə: 52 HTTP / 849 SQL / 107864 byte / 7.566s. Inline detail və page-də əlavə SQL fanout yoxdur. Foreign/deleted owner 404; membership silinəndə false; legacy sahə absent.
- Android core165 unit, normal app unit, assembleDebug/lintDebug PASS (son normal run11s); native16/16 PASS16s. Strict true/false/absent/invalid membership, şəhər sorğusuz route/remount, cached display/fresh delete gate, 3001 üzvlük cache/replay, near1MiB escaped body-nin production DurableJournalFile/IoJournalFileSystem roundtrip-i və default journal/oversize rejection-da əvvəlki faylın saxlanması yoxlanır.
- Backend full `go test -race ./... -count=1` real disposable DB ilə PASS, platform34.356s; son focused probe PASS16.743s; vet/build PASS. İlk izolasiya fixture-i FK-yə uyğun olmayan başqa owner membership yaratmağa çalışırdı; çıxarıldı, supported foreign-owner denial və membership return/delete halları ilə final suite təkrar keçirildi. İlk Android yeni payload fixture-i və compile cəhdi uğursuz idi; düzəlişdən sonra normal və native final yoxlamaları keçdi. Bunlar CI deyil.
- `ui-craft`: GameTokens oxundu; yeni rəng/ölçü yoxdur. Text/Surface13.00, Muted8.22, Success9.28 kontrast PASS. Native320/360/390 portret font1/2, inset və uzun balans renderləri alındı. 360detail, 390font2, 320font2 scrolled city status, 390font2 inset/uzun balans və 320list açılıb approved01-main ilə müqayisə edildi. Layout/material əvvəlki kompozisiyadır, status yaşıl görünür, font2 scroll və sabit primary action saxlanır. Əvvəlki iki-sətir dock, artwork/PNG parity və uzun balansın horizontal scroll borcu bağlanmır.

## Lokal diff review və sərhəd

Owner/non-deleted query, legacy NULL scan, page cursor, strict parser, membership precedence, no city fallback, cache factory/default limit separation, atomic replace və account/revision checks yoxlandı. Əlavə correctness blocker tapılmadı. Diffcheck PASS. Production source/backend API dəyişməsi bu paired vahiddə birlikdədir; backend deploy ayrıca göstəriş tələb edir. Cihaz/emulator açılmadı, Firebase Android→Go E2E, production RTT/SQL contention/frame/GPU/ART/memory/battery ölçülməyib. Bu yalnız istifadəçinin istədiyi performans1–2 vahididir; bütün app performansı və redesign qəbulu deyil.

Evidence: `inventory-bounded-membership/evidence/`; yerli screenshot-lar `rendered/` altındadır (PNG ignore qaydası saxlanır). Native rerun: əvvəlki `inventory-first-open/render/render.init.gradle` və `inventory-detail-latency/render/render.init.gradle` ilə InventoryFirstOpenTest/InventoryDetailLatencyTest. Normal checks init-sizdir.
