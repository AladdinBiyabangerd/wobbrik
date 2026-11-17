# Cari tapşırıq — native mobil UI pariteti

## Completed

Bina məlumatı `695e45b`, İnventar şəhər nişanı `bdbb360`, Dekor sahiblik nişanı `c5168c8` tamamlanıb. Bu vahid: bina detalında mövcud şəhərə aidlik statusu native read-only panel/City ikonu ilə göstərilir; exact revision, mismatch, inventarda və null mətnləri saxlanır. Tam detal/app pariteti deyil.

## Current state

Branch `feat/native-ui-parity`; bu vahidin bazası `c5168c8`. İstifadəçi yalnız commit istəyir; merge/push/CI yoxdur. İki generated `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` əvvəlki dirty vəziyyətdədir və commit edilmir. AppBackend dəyişməyib.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1; authority `docs/design/unified-mobile-v3/README.md` və `02-market.png`. Şəhər statusu read-only-dır, yeni action/placement hüququ yaratmır. Mövcud GameTokens/MenuRelief/City vector istifadə edilir. Android InventoryReads və AppBackend city.go owner-scoped building_revision müqaviləsi yoxlandı; API/domain/schema/wallet/backend dəyişikliyi lazım deyil. Ərazi/press animasiyası/tablet dəyişmir. CI/emulator/merge/push/deploy yoxdur.

## Remaining work

Tam app/PNG pariteti və mərhələ5 qəbulu açıqdır. Bina detalının maksimum satış/silmə, artwork və əməliyyat kompozisiyası tam uyğun kimi təqdim edilmir. Böyük ad horizontal scroll-u və kiçik/font2 body/list scroll qalır. Ölçülmüş performans, reklam/alış məhsul inteqrasiyası və buraxılış hazırlığı açıqdır. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Null city detail üçün ayrıca native render yoxdur; source şərti saxlanıb. Real SKU/Console/deploy ayrıca göstəriş tələb edir. İki .dm artifact ayrıca yoxlama/generasiya qərarı gözləyir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,02-market.png}`
- `docs/reviews/2026-11-17-building-city-status-native.md`
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MobileInventoryPresentation,GameTokens}.kt`
- `docs/reviews/mobile-sizing-visual/render/{MobileCommerceProductionRenderTest.kt,render.init.gradle}`
- Contract: Android `core/.../InventoryReads.kt`; AppBackend `internal/wobbrik/city.go` (read-only).

## Checks performed

Kontrast6/6 PASS minimum8.59. İlk host4/5 PASS: inset Parts route keçidində idle timeout; səbəb sübut olunmadı, sonda təkrarlanmadı. İkinci3/5 PASS: əlavə test lazy list-in görünməyən kartını indekslə seçirdi; konkret bina ID-si ilə düzəldildi. Son source commerce5/5 PASS+lintDebug PASS1m30s, failures/errors/skipped0 (`/private/tmp/wobbrik-detail-city-status-verified.log`). 390×844/320×568 font1/font2,360×640 font2 simulated inset+uzun ad/balans beş status renderi açıldı, PNG ilə müqayisə edildi; status oxunur, scroll qalır. Exact/mismatch/inventarda status və read-only semantics yoxlanır. Lokal diff review+diffcheck PASS. Mövcud nullable Java String host warning-i qalır. Emulator/CI yoxdur.

## Exact next step

Bu vahidin review/commit nəticəsini `docs/reviews/2026-11-17-building-city-status-native.md`-dən oxu. Növbəti dar vahid üçün bina detalının maksimum satış/silmə kompozisiyasını PLAN §1.1 və 02-market.png ilə yoxla. Hazır Bina məlumatı, ownership xülasəsi, şəhər statusu/nişanı, Dekor sahiblik nişanı və Yeni elan/Mənim elanlarım işini təkrarlama. Yalnız fokuslu yoxlama+lokal review-dan sonra commit; merge/push/CI/deploy avtomatik yoxdur.
