# Cari tapşırıq — native mobil UI pariteti

## Completed

Bina məlumatı `695e45b`, İnventar şəhər nişanı `bdbb360`, Dekor sahiblik nişanı `c5168c8`, şəhər statusu `cad42a2`, maksimum satış/silmə `c272351` hazırdır. Artwork işığı `09c7b7a`-da hazırdır. Bu vahid: mobile detail şəhər503 zamanı bina məlumatını gizlətmir; optional membership unknown kimi göstərilir.403/cancellation/account guard və tablet davranışı saxlanır. Tam detal/app pariteti deyil.

## Current state

Branch `feat/native-ui-parity`; bu vahidin bazası `09c7b7a`. İstifadəçi yalnız commit istəyir; merge/push/CI yoxdur. İki generated `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` əvvəlki dirty vəziyyətdə qalır və commit edilmir. AppBackend dəyişməyib.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1; authority `docs/design/unified-mobile-v3/README.md` və `02-market.png`. Artwork işığı native gradient-dir; yeni raster/token yoxdur. Real geometry/selected parts saxlanır; PNG nümunəsi kataloq binası yaratmır. Rim variantı fallback mətn kontrastı3.18 olduğundan saxlanmadı; Raised minimum6.48-dir. Satış artıq Tag ikonlu secondary action-dır; Bina görünüşü real parts route-udur. Yeni placement hüququ/alternativ dizayn əlavə edilmir. Şəhər membership mutasiya authority-si deyil. Building oxusu məcburidir; optional city503 unknown verir,403/cancellation/AccountChanged atılır. AccountViews cache/session/revision və canMutate dəyişmir; API/domain/schema/wallet/backend dəyişikliyi lazım deyil (market.go read-only yoxlandı). Ərazi/press animasiyası/tablet dəyişmir.

## Remaining work

Tam app/PNG pariteti və mərhələ5 qəbulu açıqdır. Detal artwork-ün şüaları/silueti, ümumi əməliyyat kompozisiyası tam uyğun deyil; real geometry/parts saxlanmalıdır. Mövcud əlavə funksiyalar saxlanır. Böyük ad/uzun qiymət horizontal scroll-u, kiçik/font2 body scroll qalır. Uzun qiymət scroll touch assertion, disabled silmə üçün ayrıca render yoxdur. Partial detail cache/replay və AccountChanged üçün ayrıca yeni host ssenarisi yoxdur. Ölçülmüş performans, reklam/alış məhsul inteqrasiyası və buraxılış hazırlığı açıqdır. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Real SKU/Console/deploy ayrıca göstəriş tələb edir. İki .dm artifact ayrıca qərar gözləyir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,02-market.png}`
- `docs/reviews/2026-11-17-building-detail-city-unavailable.md`
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MobileInventoryPresentation,InventoryScreen,BuildingArt,PurpleBuildingArt,GameTokens}.kt`
- `docs/reviews/mobile-sizing-visual/render/{MobileCommerceProductionRenderTest.kt,render.init.gradle}`

## Checks performed

Son source commerce5/5 + ReadOnlyGameCacheTest4/4 + BuildingDeletionTest3/3 PASS, failures/errors/skipped0; lintDebug PASS2m14s. Kontrast6.48/10.24 PASS. Beş unknown-city native detail renderi açıldı:390×844/320×568 font1/font2,360×640 font2 simulated inset+uzun mətn/balans;02-market.png ilə müqayisə edildi.503 zamanı detail/status/actions/back,403 zamanı error və mutation girişlərinin olmaması yoxlandı. İlk toplu run5/5 AppNotIdleException verdi (səbəb sübut olunmayıb), sonra regular və son toplu run PASS oldu. Lokal diff review+diffcheck PASS. Log `/private/tmp/wobbrik-detail-city-verified.log`, renderlər `/private/tmp/wobbrik-detail-city-verified/`. Emulator/CI yoxdur.

## Exact next step

Bu vahidin review/commit nəticəsini `docs/reviews/2026-11-17-building-detail-city-unavailable.md`-dən oxu. Context böyüyür: növbəti dar vahidi yeni chatdə davam et. PLAN §1.1 və02-market.png əsasında qalan artwork/əməliyyat fərqlərindən növbəti dar vahidi seç; hazır məlumat/ownership/şəhər statusu/nişanı və şəhər503 fallback, Dekor sahiblik nişanı, maksimum satış/silmə və Yeni elan/Mənim elanlarım işini təkrarlama. Yeni placement hüququ və alternativ dizayn uydurma. Yalnız fokuslu yoxlama+lokal review-dan sonra commit; merge/push/CI/deploy avtomatik yoxdur.
