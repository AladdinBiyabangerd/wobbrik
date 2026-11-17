# Cari tapşırıq — native mobil UI pariteti

## Completed

Bina məlumatı paneli `695e45b`-də tamamlanıb. Bu vahid: İnventar/Binalar kartının təsdiqli “✓ Şəhərdə” native nişanı real city membership ID+revision ilə birləşdirildi. Köhnə revision və city read503 üçün nişan yoxdur; city read xətası inventar siyahısını bağlamır. Normal fontda yuxarı sağ slot, font2-də ayrı natural-height sətir istifadə olunur. Tam inventar/app pariteti deyil.

## Current state

Branch `feat/native-ui-parity`; bu vahidin baza commit-i `695e45b`. İstifadəçi hər dar vahid üçün yalnız commit istəyir; merge/push yoxdur. Əvvəlki iki generated `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` dirty qalır və commit edilmir. AppBackend dəyişməyib.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1; authority `docs/design/unified-mobile-v3/README.md` və `01-main.png` İnventar/Binalar. Nişan əməliyyat/placement hüququ deyil. Mövcud InventoryRepository.city pagination/revision və Ready/account cache sərhədi istifadə edilir; server API/domain/schema/wallet dəyişmir. Backend CityPage/CityBuilding müqaviləsi yoxlanıldı, backend dəyişikliyi lazım deyil. Tablet read axını saxlanır. Profil/Ərazi/press animasiyası bu vahiddə dəyişmir. CI/emulator/merge/push/deploy göstərişi yoxdur.

## Remaining work

Tam app/PNG pariteti və mərhələ5 qəbulu açıqdır. Bina detalının qalan status/maksimum satış/silmə, artwork və əməliyyat kompozisiyası tam uyğun kimi təqdim edilmir. Böyük ad üçün mövcud horizontal scroll və kiçik/font2 body/list scroll qalır. Ölçülmüş performans, reklam/alış məhsul inteqrasiyası və buraxılış hazırlığı açıqdır. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Real SKU/Console/deploy ayrıca göstəriş tələb edir. İki .dm artifact ayrıca yoxlama/generasiya qərarı gözləyir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,01-main.png}`
- `docs/reviews/2026-11-17-inventory-city-seal-native.md`
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MobileInventoryPresentation,InventoryScreen,GameTokens}.kt`
- `docs/reviews/mobile-sizing-visual/render/{MobileCommerceProductionRenderTest.kt,render.init.gradle}`
- Contract: `android-billing/core/src/main/kotlin/appbackend/wobbrik/game/InventoryReads.kt`; AppBackend `internal/wobbrik/city.go` (read-only inspected).

## Checks performed

ui-craft Background/Success11.82:1 PASS. İlk renderdə nişan artwork-ü örtdü; üst slot ayrıldı, sonra normal kartların eyni hündürlüyü üçün boş slot hamısında saxlandı. Native hostlar390×844/320×568 font1/font2,360×640 font2 simulated inset + uzun ad/balans; synthetic HTTP, production repository/route/composable. Son mənbədə commerce5/5 PASS (failures/errors/skipped0) + lintDebug PASS1m12s; log `/private/tmp/wobbrik-inventory-city-seal-verified.log`. Son beş inventory renderi və city-unavailable font açılıb baxıldı, authority PNG ilə müqayisə edildi; renderlər `/private/tmp/wobbrik-inventory-city-seal-verified`. Lokal scoped diff review və diffcheck PASS. Mövcud nullable Java String host warning-i qalır. Emulator/CI işə salınmır.

## Exact next step

Bu dar vahidin son review/commit nəticəsini `docs/reviews/2026-11-17-inventory-city-seal-native.md`-dən oxu. Davam üçün eyni branch-də PLAN §1.1 və uyğun təsdiqli PNG əsasında növbəti dar vahidi seç; hazır Bina məlumatı, ownership xülasəsi, şəhər nişanı, Yeni elan/Mənim elanlarım işini təkrarlama. Yalnız fokuslu yoxlama+lokal review-dan sonra commit; merge/push/CI/deploy avtomatik yoxdur.
