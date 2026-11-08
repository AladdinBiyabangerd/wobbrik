# Main sync + şəhər həndəsəsi — 8 noyabr 2026

## Completed
- Main `defc9e3` cari `feat/home-design-alignment` branch-inə tarix saxlanılaraq konfliktsiz merge edildi: `36b4aea`. Main branch-in əcdadıdır. Home/online işi qorunur; main-ə əks merge yoxdur.
- Main-in yeni `docs/PLAN.md` sənədi yeganə işlək mərhələ/status planıdır. Texniki parametrlər `kotlin-compose-plan.md`-dədir.
- AppBackend `feat/city-geometry-kernel` / `6e949be`: integer tick bina proyeksiyası, quarter-turn/translation, pairwise clearance və torpaq union containment pure nüvəsi + testlər. API/DB-yə qoşulmayıb.
- Əvvəlki home cutout `44946d8`: original RGB saxlanan 415×451 RGBA maska və painter aspect ratio. Home rekord/XP binding saxlanır.

## Current state
- Wobbrik `feat/home-design-alignment`; backend `feat/city-geometry-kernel`, iki repo ayrıca commit/branch. Main CI yoxdur, push/deploy yoxdur.
- Şəhər tam scope-u təsdiqlənib, ancaq yerləşdirmə hələ `placement_enabled:false`. Kernel feature-in tamamlanması deyil.
- Home tam dizayn qəbulu deyil: generativ logo/ikon, səth/bevel və əlavə XP sətri fərqləri qalır; cutout planşetdə aşağı resolution səbəbindən yumşaqdır. Şəhər/İnventar/Bazar route-ları bağlıdır.

## Decisions
- `city-geometry-v1`: 1.000 tick/engine unit, floor width/depth 100.000 tick, gap 10.000; quarter-turn base origin ətrafında, sonra translation. ±1.000.000.000 texniki koordinat sərhədidir, torpaq hüququ deyil. Dəqiq məna texniki plandadır.
- Torpaq union-unda holes/seams yoxlanır. Bina proyeksiyası eyni depth və ardıcıl x-overlap üçün exact rectangle-dir; transient sway/falling/failed floor daxil deyil. Typed qəbul/quantization/provenance hələ seçilib tətbiq edilməyib.
- Server authority qalır; opaque structural-v1-dən geometry çıxarılmır; legacy inventory-only. Yeni scope/placement icazəsi istəmə.
- Hazır dizaynlar məcburidir; sistem fontu təsdiqlənmiş istisnadır. Real SKU/Console/push/deploy ayrıca göstərişlədir.
- PLAN.md-dəki cari qayda main merge üçün Aladdin OK tələb edir. Bütün online tarix review edilmədən main-ə dolayı daxil edilməsin.

## Remaining work
- Typed floor geometry + engine→tick dəqiqlik/normalization və accepted checkpoint provenance, backend finish persistence; Android uyğunluğu.
- Torpaq və 12 dekor footprint-ləri, copy ownership, atomik place/move/replace/return, mobil renderer/naviqasiya və real DB/device testləri. Read-only şəhər tam scope əvəzi deyil.
- Home qalan uyğunluq və canlı sync/start/finish e2e; inventory pagination, Parametrlər, Room/outbox/fault proof ayrıca açıqdır.

## Relevant files
- docs/PLAN.md; docs/kotlin-compose-plan.md (Cari icra göstərişi, §5 şəhər həndəsəsi); docs/game-plan.md §7/§23.
- docs/reviews/2026-11-08-main-sync-city-geometry.md; docs/reviews/2026-11-08-home-design-alignment.md (son bölmələr).
- /Users/mac/My Workspace/AppBackend/internal/wobbrik/{city_geometry,city_geometry_test,attempts,finish,city}.go
- /Users/mac/My Workspace/AppBackend/docs/{wobbrik-game-api.md,reviews/2026-11-08-city-geometry-kernel.md}; backend AGENTS.md/README.md.
- android-billing/engine/src/main/kotlin/com/aladdin/wobbrik/engine/{EngineCheckpoint,PracticeEngine,AcceptedAttemptCommands}.kt
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{GameplayHttpBackend,RecoveryDtos}.kt
- HomeScreen.kt, OnlineGame.kt, GameTokens.kt; docs/design/account-assets-v1/; docs/design/mobile-v1/tokens.css.

## Checks performed
- Merge ancestor check PASS; iki repo diff --check PASS. Wobbrik bu vahiddə docs-only; yeni Android build/render yoxdur.
- Backend Go `go test -race -count=1 ./internal/wobbrik` PASS, `go vet ./...` PASS, `go build ./cmd/server` PASS. Binary `/private/tmp/wobbrik-contract-tools/go/bin/go`.
- Projection/bad support, int64 bounds, 4 rotation/inverse, translation, exact clearance/one tick, seams/holes/L-shape, 3×3 grid-in 511 subset coverage oracle testləri keçib. Pure kernel olduğundan DB suite işlədilməyib; DB PASS iddiası yoxdur.
- Əvvəlki home installDebug/render/contrast/cutout yoxlamaları home review-da saxlanır; fixture render canlı axın sübutu deyil. Tam online diff review və main CI açıqdır.

## Exact next step
- Yeni chatdə bu qeyd, PLAN.md və texniki planın şəhər bölməsini oxu; iki repo branch-ini qoru. Backend AGENTS.md-ni oxu.
- Yalnız EngineCheckpoint/attempts/finish müqavilələrini araşdırıb integer tick typed geometry/provenance qəbulunu və precision qaydasını bağla; legacy opaque-ı trust etmə. Eyni vahiddə backend və mobil DTO uyğunluğunu testlə; sonra atomik placement vahidinə keç. Yeni paralel master plan yaratma.
- Main worktree /Users/mac/My Workspace/Wobbrik-rail-icons. Feature review + Aladdin OK olmadan main merge etmə; CI yalnız main-də ./scripts/local-ci.sh.
