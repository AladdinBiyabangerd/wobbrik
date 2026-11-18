# Cari tapşırıq — Inventory city paging/detail costs / 18 noyabr 2026

## Completed

- `docs/PLAN.md` §1.1 üzrə cihazsız böyük-hesab oxusu: existing plain City100 istifadə edilir;1001bina üçün51→11city səhifəsi. Optional membership hələ bütün şəhəri oxuyur.
- Hər səhifədə accumulated map köçürülməsi aradan qaldırılıb; refresh-local linear accumulator, final copy, revision/population/duplicate/cursor checks saxlanır.
- AppBackend additive building detail `?view=display`: istifadə olunmayan opaque snapshot SQL-dən alınmır. Mobil display istəyir, old-server snapshot-u cache üçün silir, legacy detail və20-page city bundle-larını tam cursor chain ilə replay edir.

## Current state

Android `fix/inventory-city-detail-costs`, baza `efbd9e9`; AppBackend `feat/wobbrik`, baza `1ef8b32`. Paired scoped diff lokal review edilib. Owner iki release baseline-profile binarisi dirty saxlanır və işə daxil deyil. Main merge/CI/push/deploy yoxdur.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1/§2–3. Server wallet/ownership authority, sole journal, UID/login/capability/revision və final-sync/fresh-mutation gate saxlanır. Yeni endpoint/schema/domain/wallet qaydası yoxdur; detail-in additive display read müqaviləsi sənədləşdirilib. Cihaz/emulator açılmır. City100 mövcud serverdə işləyir; detail wire qənaəti ayrıca backend deploy tələb edir.

## Remaining work

Production10+s, RTT/SQL contention/paylanma, fiziki frame/ART/GPU/battery və Firebase-authenticated Android→Go E2E yoxlanmayıb. City hələ O(N) bütün membership-i yükləyir və hər page population hesablayır; huge city/geometry transport/cache limitini keçə bilər. Bir final sync qalır. Əvvəlki PNG parity/inset/IME/locale/TalkBack borcları bağlanmır.

## Relevant files

Core `InventoryReads.kt`, `GameplayHttpBackend.kt`, `ReadOnlyGameCache.kt`; tests `InventoryReadsTest`, `InventoryPayloadTest`, scene/parts/menu fixtures. AppBackend `internal/wobbrik/market.go`, `internal/platform/wobbrik_http.go`, inventory payload/city probe tests; `docs/wobbrik-game-api.md`. Review: `docs/reviews/2026-11-18-inventory-city-detail-costs.md`; evidence `docs/reviews/inventory-city-detail/`.

## Checks performed

- Real disposable PostgreSQL18 + authenticated HTTP middleware/synthetic identity,1001bina: final sync daxil52→12HTTP,849→209SQL,107864→94388byte; lokal race sample7.160→1.561s. Production ölçməsi deyil.
- Detail typed10floor valid opaque fixture16758→542byte; geometry/metadata bərabərdir, display11SQL. Legacy query/snapshot, invalid view, foreign owner yoxlanıb.
- Android core162/app41unit, assembleDebug/lintDebug PASS35s; backend focused racePASS16.639s, full race suitePASS(platform42.509s), vet/buildPASS. İlkin port/fixture uğursuzluqları review-da qeyd edilib və final suite-lər təkrar keçirilib. Diffcheck/lokal reviewPASS. Disposable DB dayandırılıb.

## Exact next step

Bu dar paired vahidin istifadəçi review-u; avtomatik main merge/CI/push/deploy yoxdur. Növbəti ayrıca performans işi yalnız yeni göstərişlə; bu nəticələri bütün10+s gecikmənin həlli kimi təqdim etmə. Əvvəlki owner binarilərinə toxunma.
