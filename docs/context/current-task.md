# Cari tapşırıq — native mobil UI pariteti

## Completed

17 noyabr əvvəlki native UI işi tarix saxlanılmaqla Wobbrik main `0315674`-ə merge/push edilib; AppBackend inteqrasiya/policy main `cb684a7`-dədir. Əvvəlki sübutlar: `docs/reviews/2026-11-17-native-ui-logical-commits.md` və `2026-11-17-native-ui-remote-merge.md`.

Bu vahid: İnventar “Bina məlumatı” panelində native ikon/etiket/dəyər sətirləri, məlumat başlığı, mövcud tokenlərlə relyef, böyük şriftdə şaquli dəyər kompozisiyası hazırlandı. Hündürlük/Bina növü/Sakinlər real OwnedBuilding+kataloq məlumatıdır. Tam bina detalı və bütün app pariteti deyil.

## Current state

Yeni iş branch-i `feat/native-ui-parity`, baza main `0315674`. İstifadəçi bütün növbəti işi bu branch-də və hər tamamlanan vahiddən sonra yalnız commit istəyir. Bu vahidin commit-i həmin mənbə/review/handoff dəyişikliklərini əhatə edir. Əvvəlki iki generated `android-billing/app/release/baselineProfiles/{0,1}/app-release.dm` dirty qalır; provenance yoxlanmayıb, commit edilmir. Bu tur AppBackend dəyişməyib.

## Decisions

Yeganə plan `docs/PLAN.md` §1.1, authority `docs/design/unified-mobile-v3/README.md`. Commit icazəlidir; yeni merge/push/deploy/CI göstərişi yoxdur. Davam eyni branch-dədir. Profilə toxunulmur, Ərazi qaytarılmır, yeni placement hüququ/səviyyə uydurulmur. Android OwnedBuilding və backend Building müqaviləsində bina səviyyəsi yoxdur; PNG-dəki “Səviyyə 3” əvəzinə real sahələr göstərilir. Server API/domain/schema/wallet dəyişmədiyi üçün backend dəyişikliyi lazım deyil. Press animasiyası ayrıca tapşırıqdır.

## Remaining work

Tam app/PNG pariteti və mərhələ5 qəbulu açıqdır. Bina detalında bütün kompozisiyanın pariteti iddia edilmir: status/maksimum satış/silmə və əvvəlki artwork/sabit əməliyyatlar saxlanır. Kiçik/font2 body scroll tələb edir. Ölçülmüş performans işi, reklam/alış məhsul inteqrasiyası, buraxılış hazırlığı açıqdır. Real device/account/TalkBack/IME/real inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Deploy/real SKU/Console ayrıca göstəriş tələb edir. İki .dm artifact ayrıca yoxlama/generasiya qərarı gözləyir.

## Relevant files

- `docs/PLAN.md` §1.1; `docs/design/unified-mobile-v3/{README.md,02-market.png}`
- `docs/reviews/2026-11-17-building-information-native.md`
- `android-billing/app/src/main/kotlin/com/aladdin/wobbrik/{MobileInventoryPresentation,InventoryScreen,GameTokens}.kt`
- `docs/reviews/mobile-sizing-visual/render/{MobileCommerceProductionRenderTest.kt,render.init.gradle}`
- Contract: `android-billing/core/src/main/kotlin/appbackend/wobbrik/game/InventoryReads.kt`; AppBackend `internal/wobbrik/market.go` Building (read-only inspected).

## Checks performed

ui-craft kontrast5/5 PASS, minimum8.59. İlk host5/5 failed: test sabit görünüş düyməsinə scroll etməyə çalışırdı; test düzəldildi, təkrar5/5 PASS + lintDebug PASS2m5s. İlk render baxışı font2 məlumat ikonunda mətn kəsilməsi tapdı, ikon dairəsi Inset×fontScale edildi. Son production mənbə5/5 native commerce PASS (failure/error/skipped0) + lintDebug PASS1m34s. Renderlər `/private/tmp/wobbrik-building-information-final` altında açılıb baxıldı. Son test-only başlıq capture əlavəsi ilə kiçik/font2 commerce1/1 PASS; ikon mətni açılıb yoxlandı. Diffcheck PASS. Native hostlar390×844/320×568 font1/font2,360×640 font2 simulated inset + uzun ad/balans; synthetic HTTP, production repository/route/composable. Emulator/CI işə salınmır. Diff review yeni domain/dependency/token olmadığını təsdiqləyir.

## Exact next step

Bu vahid tamamlanıb; davam üçün istifadəçinin növbəti dar ekran vahidini götür və eyni `feat/native-ui-parity` branch-də saxla. PLAN §1.1 və həmin ekranın təsdiqli PNG-sini oxu; hazır panel/üst xülasə/Yeni elan/Mənim elanlarım işini təkrarlama. Növbəti məntiqi vahid yalnız yoxlama+lokal review-dan sonra commit edilir; merge/push/CI/deploy avtomatik edilmir. Context böyüdüyünə görə növbəti böyük işi yeni chatdə davam et.
