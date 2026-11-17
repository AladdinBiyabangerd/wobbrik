# Cari tapşırıq — native mobil UI pariteti

## Completed

Bina məlumatı `695e45b`, İnventar şəhər nişanı `bdbb360`-də tamamlanıb. Bu vahid: Bazar/Dekor kartında real item ID üzrə sahib olunan nüsxə varsa “✓ Sahibsən” native nişanı; real nüsxə sayı və təkrar `Al` girişi saxlanır. Tam dekor/app pariteti deyil.

## Current state

Branch `feat/native-ui-parity`; bu vahidin baza commit-i `bdbb360`. İstifadəçi yalnız commit istəyir; merge/push yoxdur. İki generated `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` əvvəlki dirty vəziyyətdədir və commit edilmir. AppBackend dəyişməyib.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1; authority `docs/design/unified-mobile-v3/README.md` və `02-market.png` Bazar/Dekor. Sahibsən nişanı read-only-dır, copy təkrar alınır; sahiblik alışı bloklamır. Mövcud DecorationShopScreen account/session/Ready ilə oxunmuş bütün nüsxələri ötürür. Loading/error/null və sıfır nüsxədə nişan yoxdur. Android CityPurchases və AppBackend shop.go DecorationCopy/Page müqaviləsi yoxlanıldı; API/domain/schema/wallet/backend dəyişikliyi lazım deyil. Profil/Ərazi/press animasiyası/tablet bu vahiddə dəyişmir. CI/emulator/merge/push/deploy yoxdur.

## Remaining work

Tam app/PNG pariteti və mərhələ5 qəbulu açıqdır. Bina detalının qalan status/maksimum satış/silmə, artwork və əməliyyat kompozisiyası tam uyğun kimi təqdim edilmir. Böyük ad üçün mövcud horizontal scroll və kiçik/font2 body/list scroll qalır. Ölçülmüş performans, reklam/alış məhsul inteqrasiyası və buraxılış hazırlığı açıqdır. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Real SKU/Console/deploy ayrıca göstəriş tələb edir. İki .dm artifact ayrıca yoxlama/generasiya qərarı gözləyir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,02-market.png}`
- `docs/reviews/2026-11-17-decoration-ownership-native.md`
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MobileShopPresentation,DecorationShopScreen,GameTokens}.kt`
- `docs/reviews/mobile-sizing-visual/render/{MobileCommerceProductionRenderTest.kt,render.init.gradle}`
- Contract: Android `core/.../CityPurchases.kt`; AppBackend `internal/wobbrik/shop.go` (read-only inspected).

## Checks performed

ui-craft kontrast3/3 PASS (minimum8.59). Production repository/route/composable native host ölçüləri390×844/320×568 font1/font2,360×640 font2 simulated inset + uzun ad/balans; synthetic HTTP. İlk host run-da yeni503 yoxlaması lazy grid-in aşağıdakı xəta elementini scroll etmədən gözlədiyi üçün timeout verdi; test scrollToNode ilə düzəldildi. Nişan+sayı birlikdə göstərən capture üçün count sətrinə scroll əlavə edildi. Son production mənbədə commerce5/5 PASS+lintDebug PASS1m8s (`/private/tmp/wobbrik-decor-ownership-final.log`); son test-only96dp capture scroll əlavəsi ilə font2 variantları3/3 PASS39s (`/private/tmp/wobbrik-decor-ownership-capture.log`). Bütün beş ölçü ownership renderi və son üç capture açıldı, PNG ilə müqayisə edildi; nişan/count tam oxunur, scroll qalır. Scoped lokal diff review və diffcheck PASS; mövcud nullable Java String host warning-i qalır. Emulator/CI yoxdur.

## Exact next step

Bu vahidin son review/commit nəticəsini `docs/reviews/2026-11-17-decoration-ownership-native.md`-dən oxu. Context böyüdüyünə görə növbəti dar vahidi yeni chatdə davam et. Eyni branch-də PLAN §1.1 və uyğun təsdiqli PNG əsasında seçim et; hazır Bina məlumatı, ownership xülasəsi, İnventar şəhər nişanı, Dekor sahiblik nişanı və Yeni elan/Mənim elanlarım işini təkrarlama. Yalnız fokuslu yoxlama+lokal review-dan sonra commit; merge/push/CI/deploy avtomatik yoxdur.
