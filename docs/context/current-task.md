# Cari tapşırıq — native mobil UI pariteti

## Completed

Bina məlumatı `695e45b`, İnventar şəhər nişanı `bdbb360`, Dekor sahiblik nişanı `c5168c8` tamamlanıb. Şəhər statusu `cad42a2`-də tamamlanıb. Bu vahid: server maksimum satış qiyməti native Tag/coin kartında, silmə girişi ayrıca destructive CommerceAction ilə göstərilir; böyük şriftdə qiymət alt sətrə keçir. Mövcud confirmation/canMutate/journal saxlanır. Tam detal/app pariteti deyil.

## Current state

Branch `feat/native-ui-parity`; bu vahidin bazası `cad42a2`. İstifadəçi yalnız commit istəyir; merge/push/CI yoxdur. İki generated `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` əvvəlki dirty vəziyyətdədir və commit edilmir. AppBackend dəyişməyib.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1; authority `docs/design/unified-mobile-v3/README.md` və `02-market.png`. Qiymət server maxPrice-dır; traded bina təkrar satılmır, silmə confirmation tələb edir və sikkə qaytarmır. GameTokens/MenuRelief/Tag/coin və optional destructive CommerceAction istifadə edilir. Android InventoryReads/BuildingDeletion və AppBackend market.go müqaviləsi yoxlandı; API/domain/schema/wallet/backend dəyişikliyi lazım deyil. Ərazi/press animasiyası/tablet dəyişmir. CI/emulator/merge/push/deploy yoxdur.

## Remaining work

Tam app/PNG pariteti və mərhələ5 qəbulu açıqdır. Bina detalının artwork və ümumi əməliyyat kompozisiyası tam uyğun kimi təqdim edilmir. Maksimum satış/silmə dar vahidi hazırdır; PNG-də göstərilməyən mövcud funksiyalar saxlanıb. Long qiymətin bütün rəqəmləri üçün horizontal scroll lazımdır; bu scroll üçün yeni touch assertion yoxdur. Disabled silmə üzrə ayrıca native ssenari yoxdur. Böyük ad horizontal scroll-u və kiçik/font2 body/list scroll qalır. Ölçülmüş performans, reklam/alış məhsul inteqrasiyası və buraxılış hazırlığı açıqdır. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Null city detail üçün ayrıca native render yoxdur; source şərti saxlanıb. Real SKU/Console/deploy ayrıca göstəriş tələb edir. İki .dm artifact ayrıca yoxlama/generasiya qərarı gözləyir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,02-market.png}`
- `docs/reviews/2026-11-17-building-sale-delete-native.md`
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MobileInventoryPresentation,MobileCommerceUi,GameTokens}.kt`
- `docs/reviews/mobile-sizing-visual/render/{MobileCommerceProductionRenderTest.kt,render.init.gradle}`
- Contract: Android `core/.../{InventoryReads,BuildingDeletion}.kt`; AppBackend `internal/wobbrik/market.go` (read-only).

## Checks performed

Kontrast4/4 PASS minimum6.48. İlk commerce2/5 PASS: qiymət mətninin horizontal parent-i outer detail scroll-a çatmırdı; test əvvəl coin sətrini outer scroll ilə görünən etmək üçün düzəldildi. Son source commerce5/5 + BuildingDeletionTest3/3 PASS, failures/errors/skipped0; lintDebug PASS1m54s (`/private/tmp/wobbrik-building-sale-delete-final.log`). 390×844/320×568 font1/font2,360×640 font2 simulated inset+uzun ad/balans üçün beş qiymət/silmə/confirmation renderi açıldı, PNG ilə müqayisə edildi. Long price/font2 və traded/inset renderi də açıldı. Read-only qiymət,48dp silmə, confirmation ləğvi+submit0, traded satış disabled yoxlanır. Lokal diff review+diffcheck PASS. Nullable Java String host warning-i qalır. Emulator/CI yoxdur.

## Exact next step

Bu vahidin review/commit nəticəsini `docs/reviews/2026-11-17-building-sale-delete-native.md`-dən oxu. Context böyüyür: növbəti dar vahidi yeni chatdə PLAN §1.1, 02-market.png və bu handoff əsasında seç. Bina detalının qalan artwork/əməliyyat kompozisiyası üzrə konkret fərqi yoxla; yeni placement hüququ və ya alternativ dizayn uydurma. Hazır məlumat/ownership/şəhər statusu/nişanı, Dekor sahiblik nişanı, maksimum satış/silmə və Yeni elan/Mənim elanlarım işini təkrarlama. Yalnız fokuslu yoxlama+lokal review-dan sonra commit; merge/push/CI/deploy avtomatik yoxdur.
