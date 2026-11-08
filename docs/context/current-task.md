# Footprint implementation continuation — 8 November 2026

- Completed: server-owned six land and twelve decoration rectangle unions, quarter-turn union transform, positive-edge/no-overlap parcel predicate. No runtime caller/schema/API change yet.
- Current state: backend `feat/city-footprint-placement`; Wobbrik `docs/city-placement-continuation`. Prior branch refs preserved. Main merge requires separate user OK; no merge/push/deploy/CI.
- Decision pending: existing handoff explicitly leaves purchase/parcel transaction coupling undecided. Asked user whether purchased rights remain separate from later free placement (existing API compatible), or purchase and initial placement must be one transaction. Do not infer answer from footprint approval.
- Remaining: copy ownership persistence, atomic place/move/replace/return, API/read contract and real PostgreSQL concurrency/rollback tests. Placement remains false. UI unchanged.
- Relevant files: AppBackend internal/wobbrik/city_footprints{,_test}.go, city.go, shop.go, city_geometry.go; docs/wobbrik-game-api.md. Approved sizes: both footprint reviews; Wobbrik docs/kotlin-compose-plan.md.
- Checks: `go test -race ./internal/wobbrik`, `go vet ./internal/wobbrik`, diff review and diff --check PASS. Pure geometry only; no PostgreSQL/state changes or DB tests. Toolchain restored from existing archive into /private/tmp/wobbrik-footprint-tools/go.
- Exact next step: obtain coupling answer, then complete the backend transaction/schema/API unit on the existing feature branch; preserve main approval gate. Full task is not complete.

# Typed geometry və footprint təklifi — 8 noyabr 2026

## Son davam — footprint parametrləri təsdiqlənib
- İstifadəçi torpaq formasını və 12 dekor ölçüsünü ilkin oyun parametrləri kimi seçib təsdiqə təqdim etməyə icazə verdi; konkret parametrləri sonradan “okeydir” cavabı ilə təsdiqlədi.
- Hər iki main `git pull --ff-only origin main` ilə aktual yoxlanıldı. Android feature branch main ilə yeniləndi (`15f2551`); yalnız UI approval review konflikti main-in daha yeni qeydi ilə həll edildi. Backend feature artıq main-i ehtiva edirdi. Əvvəlki branch-lər qorundu; feature→main merge/push/deploy yoxdur.
- Cari Android branch: `docs/city-footprint-proposal`, base `15f2551`. Backend kodu dəyişməyib; sənəd davamı `docs/city-footprint-parameters` branch-indədir.
- Konkret təklif və dəqiq konturlar: `docs/reviews/2026-11-08-city-footprint-proposal.md`. Başlanğıc 440×220; genişlənmələr 110×110 modullardan 4/5/6/7/8 ekvivalent; 5 və 7 modul üçün L-forma. 12 dekorun ölçüləri həmin sənəddədir. Məcburi grid yoxdur. Yol üçün 10 vahid boşluq istisnası yoxdur, buna görə yol hissələri bitişmir. Torpaq/dekor müqaviləsi təsdiqlidir; alış/parcel transaction coupling hələ seçilməyib.
- Yoxlamalar: bütün 6 parcel üçün nümunə bina sayı və pairwise 10 boşluq, ümumi 459800 vahid², dekor cəmi 1000 sikkə və L-yolun 5100 vahid² sahəsi PASS. Mövcud mobile-v1 tokenləri ilə statik ölçü sxemi render edildi və görüntüyə baxıldı; text/background, muted/surface, accent/surface-raised kontrastı PASS. İnteraktiv təqdimat üçün canvas hazırlandı; TypeScript yoxlaması PASS; runtime render ayrıca yoxlanmayıb. App UI/server kodu dəyişməyib; cihaz/live HTTP və hazır artwork uyğunluğu yoxlanmayıb. CI işlədilməyib.
- **Exact next step:** təsdiqlənmiş parametrlər canonical `docs/kotlin-compose-plan.md` və AppBackend `docs/wobbrik-game-api.md`-yə yazılıb; uyğun backend domain/data footprint və atomik placement vahidini ayrıca feature branch-də davam et. Backend sənəd branch-i `docs/city-footprint-parameters`-dir. UI üçün ayrıca konkret dizayn approval-u və main üçün ayrıca OK tələbi saxlanır. Aşağıdakı typed geometry qeydləri əvvəlki vahidin vəziyyətidir.

## Completed
- Backend və Android typed geometry müqaviləsi `feat/typed-geometry-provenance` branch-lərində tamamlandı; əvvəlki bütün branch ref-ləri saxlanıldı. Android base `e470a47`, backend base `6e949be`.
- Yeni start opt-in; checkpoint tick shape/bounds/support/snapshot consistency və immutable integer prefix yoxlaması; server provenance atomik saxlanır və finish-də binaya köçürülür. Attempt/inventory/building/listing read provenance verir.
- Android tick conversion, durable wire, typed recovery/provenance verification qoşuldu. Legacy active attempts/journal replay dəyişməyib; legacy bina inventory-only qalır.

## Current state
- Hər iki repo feature branch-dədir, main merge/CI/push/deploy yoxdur. PLAN.md üzrə main merge Aladdin OK tələb edir; bütün online tarix dolayı main-ə daxil edilməməlidir.
- Placement hələ `false`. UI/renderer dəyişməyib. Home/online mövcud işi və dizayn borcları saxlanır; typed müqavilə bütün şəhərin və ya mərhələ 3-ün qəbulu deyil.

## Decisions
- `city-geometry-v1`: 1000 tick/unit, width/depth 100000, gap 10000; foundation origin saxlanır. Double×1000 ən yaxın integer, half-away-from-zero; signed zero→0. Recenter/clamp/canlı physics quantization yoxdur. Quantization dəstəyi itirəndə reject.
- Start `geometry_version` ilə lifetime opt-in. Structural-v1/rules-v1 dəyişmir. Yeni Android üçün əvvəl backend+migration 011 lazımdır; köhnə backend yeni sahəni rədd edir. Legacy upgrade/backfill yoxdur.
- Provenance attempt/sequence/operation/rules/engine-dir; app/UID containing DB row-dan, accepted_at ilkin checkpoint qəbzindəndir. Struktur qəbuludur, physics replay/anti-cheat deyil.
- Main üçün ayrıca OK, real SKU/Console/push/deploy üçün ayrıca göstəriş qüvvədədir.

## Remaining work
- Review nəticələrini istifadəçiyə təqdim et; OK olmadan main merge etmə. Backend və Android müstəqil review/merge tələb edir.
- Torpaq/12 dekor footprint-ləri və copy ownership; atomik place/move/replace/return; mobil şəhər renderer/naviqasiya və device/live HTTP e2e ayrıca vahidlərdir.
- Home qalan dizayn uyğunluğu, canlı online sync/start/checkpoint/finish, inventory pagination, Parametrlər, Room/outbox/fault proof açıqdır. Yenidən dizayn icazəsi yoxdur.

## Relevant files
- docs/PLAN.md; docs/kotlin-compose-plan.md §5; docs/reviews/2026-11-08-typed-geometry-provenance.md.
- android-billing/core/.../game/{CityFloorGeometry,GameplayWire,RecoveryDtos,AttemptCoordinator}.kt.
- android-billing/engine/.../{CityGeometry,AcceptedAttemptCommands,AcceptedAttemptRestore}.kt və müvafiq testlər.
- /Users/mac/My Workspace/AppBackend/internal/wobbrik/{accepted_geometry,attempts,finish,read,market}.go; migrations/011_wobbrik_typed_geometry.sql; docs/wobbrik-game-api.md və eyniadlı review.

## Checks performed
- Android `:core:test :engine:test :app:compileDebugKotlin` PASS: core 47, engine 50, sıfır skip/failure. UI yoxdur, render/device yoxlanmayıb.
- Backend disposable PostgreSQL 18, loopback 55439 / geometry_contract_test: `go test -race -count=1 ./...` PASS. Son numeric parsing/read test düzəlişlərindən sonra `-run 'Geometry|TypedSnapshot'` PASS. `go vet ./...`, server build PASS.
- DB testləri: concurrent checkpoint retry, tenant isolation, typed recovery, missing geometry rollback, finish final-write rollback/replay/persistence, legacy no-upgrade/NULL provenance. Pure tests: prefix rewrite, malformed/null/string/fraction ticks/floors, Go/Kotlin precision vektorları.
- Hər iki repo lokal diff review və diff --check PASS; CI feature branch-də işlədilməyib. Main worktree /Users/mac/My Workspace/Wobbrik-rail-icons.

## Exact next step
- Bu handoff, PLAN.md və hər iki repo-nun typed geometry review-unu oxu. İstifadəçi OK verərsə müvafiq feature tarixlərini ayrıca review edib merge et; Wobbrik CI yalnız main-də ./scripts/local-ci.sh.
- Növbəti implementasiya tapşırığı ayrıca verilərsə torpaq/dekor footprint/copy ownership və atomik placement vahidinə keç; typed/provenance-ni təkrar qurma, legacy geometry uydurma. UI işinə başlamazdan əvvəl mövcud təsdiqli nümunəni təqdim edib konkret approval al.
