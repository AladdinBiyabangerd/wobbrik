# Cari tapşırıq — native mobil UI pariteti

## Completed

Bina məlumatı `695e45b`, İnventar şəhər nişanı `bdbb360`, Dekor sahiblik nişanı `c5168c8`, şəhər statusu `cad42a2`, maksimum satış/silmə `c272351` hazırdır. Bu vahid: yalnız bina detalı artwork arxasına mövcud Raised tokeni ilə native radial işıq əlavə edildi; normal/böyük şriftdə real BuildingPreview saxlanır. Tam detal/app pariteti deyil.

## Current state

Branch `feat/native-ui-parity`; baza `c272351`. İstifadəçi yalnız commit istəyir; merge/push/CI yoxdur. İki generated `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` əvvəlki dirty vəziyyətdə qalır və commit edilmir. AppBackend dəyişməyib.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1; authority `docs/design/unified-mobile-v3/README.md` və `02-market.png`. Artwork işığı native gradient-dir; yeni raster/token yoxdur. Real geometry/selected parts saxlanır; PNG nümunəsi kataloq binası yaratmır. Rim variantı fallback mətn kontrastı3.18 olduğundan saxlanmadı; Raised minimum6.48-dir. Satış artıq Tag ikonlu secondary action-dır; Bina görünüşü real parts route-udur. Yeni placement hüququ/alternativ dizayn əlavə edilmir. Dekorativ fon üçün API/domain/schema/wallet/backend dəyişikliyi lazım deyil; əvvəlki müqavilə review-u qüvvədədir. Ərazi/press animasiyası/tablet dəyişmir.

## Remaining work

Tam app/PNG pariteti və mərhələ5 qəbulu açıqdır. Detal artwork-ün şüaları/silueti, ümumi əməliyyat kompozisiyası tam uyğun deyil; real geometry/parts saxlanmalıdır. Mövcud əlavə funksiyalar saxlanır. Böyük ad/uzun qiymət horizontal scroll-u, kiçik/font2 body scroll qalır. Uzun qiymət scroll touch assertion, disabled silmə və null city üçün ayrıca render yoxdur. Ölçülmüş performans, reklam/alış məhsul inteqrasiyası və buraxılış hazırlığı açıqdır. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Real SKU/Console/deploy ayrıca göstəriş tələb edir. İki .dm artifact ayrıca qərar gözləyir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,02-market.png}`
- `docs/reviews/2026-11-17-building-detail-artwork-light.md`
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MobileInventoryPresentation,InventoryScreen,BuildingArt,PurpleBuildingArt,GameTokens}.kt`
- `docs/reviews/mobile-sizing-visual/render/{MobileCommerceProductionRenderTest.kt,render.init.gradle}`

## Checks performed

Son source commerce5/5 PASS, failures/errors/skipped0; lintDebug PASS2m1s. Kontrast Muted/Raised6.48,Text/Raised10.24 PASS. Beş native detail renderi açıldı:390×844/320×568 font1/font2,360×640 font2 simulated inset+uzun mətn/balans;02-market.png ilə müqayisə edildi. İşıq yumşaqdır; tam şüa/artwork pariteti deyil. Lokal diff review+diffcheck PASS. Log `/private/tmp/wobbrik-building-detail-light-final.log`, renderlər `/private/tmp/wobbrik-building-detail-light-final/`. Emulator/CI yoxdur.

## Exact next step

Bu vahidin review/commit nəticəsini `docs/reviews/2026-11-17-building-detail-artwork-light.md`-dən oxu. PLAN §1.1 və02-market.png əsasında qalan artwork/əməliyyat fərqlərindən növbəti dar vahidi seç; hazır məlumat/ownership/şəhər statusu/nişanı, Dekor sahiblik nişanı, maksimum satış/silmə və Yeni elan/Mənim elanlarım işini təkrarlama. Yeni placement hüququ və alternativ dizayn uydurma. Yalnız fokuslu yoxlama+lokal review-dan sonra commit; merge/push/CI/deploy avtomatik yoxdur.
