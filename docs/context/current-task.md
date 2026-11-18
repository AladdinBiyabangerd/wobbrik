# Cari tapşırıq — İnventar payload/cache gecikməsi / 18 noyabr 2026

## Completed

- İnventar böyük opaque checkpoint-ləri istifadə etmədən download/cache edirdi. Qəbul edilən böyük səhifə140000byte cache limitini keçir, optional persistence alınmır, təkrar giriş yenidən şəbəkəyə gedirdi. Baseline regression gözlənildiyi kimi FAIL; son source PASS.
- AppBackend additive `view=display` SQL-də checkpoint-i götürmür; metadata/accepted geometry/cursor dəyişmir. Köhnə inventory/detail qalır. Typed fixture335225→10905byte,1HTTP/11SQL sabit; race host48–52→11–12ms. Production/device ölçüsü deyil.
- Kotlin yeni view istəyir; köhnə serverin full cavabından yalnız inventory cache üçün snapshot çıxarılır. Köhnə query cache replay-i exact cursor/account/revision ilə saxlanır. Legacy ilk download sürətlənmir; təkrar replay0HTTP olur. UID/epoch/revision/fresh mutation gate və final sync saxlanır.
- Əvvəlki Profil `db6e70d` və İnventar `8cd7bae` işləri tarixdə saxlanır; əvvəlki disk-lock/chrome/row düzəlişləri bu işdə dəyişmir.

## Current state

Android `fix/inventory-payload-latency`, baza8cd7bae; AppBackend persistent `feat/wobbrik`, baza838c0c7. Paired scoped diff-lər lokal review və yoxlamalardan keçib, ayrıca repository commit-ləri ilə saxlanır. Main merge/push/deploy/CI yoxdur. İki release baseline-profile owner binarisi dirty saxlanır, dəyişdirilmir və commit-ə daxil deyil.

## Decisions

- Yeganə plan docs/PLAN.md§1.1/§2–3. Yeni infrastruktur/dizayn/prewarm/cache limiti yoxdur.
- Cached baxış Ready/mutasiya hüququ yaratmır; server authority, sole journal, hesab izolyasiyası saxlanır.
- Backend deploy ayrıca tapşırıqdır; deploy olmadan yeni wire payload qənaəti production-da yoxdur. Köhnə serverdə mobil cache normalizasiyası işləyir.
- CI yalnız ayrıca istifadəçi tələbi ilə main-də; emulator avtomatik açılmır; merge ayrıca təsdiqə bağlıdır.

## Remaining work

- İstifadəçinin cihazındakı10+s-in dəqiq tam səbəbi hələ təsdiqlənməyib: bağlı cihaz yoxdur, emulator açılmayıb. Production HTTP/RTT/SQL contention və real payload paylanması, ART/frame/GPU/yaddaş/battery, authenticated Android→Go E2E yoxlanmayıb. Lokal fixture səbəb və düzəlişi sübut edir,10+s-in tam həlli deyil.
- Çox böyük accepted geometry mövcud transport/cache limitini hələ keçə bilər; final sync/persistence fresh təqdimat yolunda qalır.
- Əvvəlki Profil ilk GET/composition/portrait IO; digər menyu call path-ləri və2+s ölçüləri açıqdır.
- Əvvəlki font2kiçik illustration/horizontal tabs, iki sətir dock, tam PNG parity və inset/IME/locale/TalkBack borcları qalır. İki baseline-profile owner dəyişməsi ayrıca qalır.

## Relevant files

- docs/reviews/2026-11-18-inventory-payload-latency.md; inventory-payload/evidence/.
- android-billing/core/src/main/kotlin/appbackend/wobbrik/game/{InventoryReads,ReadOnlyGameCache,GameplayHttpBackend}.kt.
- core InventoryPayloadTest/InventoryReadsTest; core/src/test/resources/inventory-display-backend.json.
- AppBackend internal/wobbrik/market.go; internal/platform/wobbrik_{http,inventory_payload_test}.go; docs/wobbrik-game-api.md və paired review.
- Əvvəlki review-lar: 2026-11-18-inventory-{first-open,composition-profile,final-review}.md; 2026-11-18-profile-open-latency.md.

## Checks performed

- Android core158/app37unit, assembleDebug/lintDebug PASS58s; final source XML və SHA evidence-dədir. CI deyil.
- Backend full real disposable PostgreSQL18 race suite PASS(platform30.889s), vet/build PASS; son test-only output dəyişikliyindən sonra focused regression PASS2.453s. Disposable DB yoxlamalardan sonra dayandırılıb.
- Lokal diff review/diffcheck PASS. UI/art/layout dəyişməyib; yeni visual render və cihaz nəticəsi iddia edilmir. `adb devices -l` boşdur.

## Exact next step

Bu payload/cache vahidini istifadəçiyə review üçün təqdim et. Main merge/deploy/CI avtomatik yoxdur. Sonrakı ölçmədə yalnız İnventarın real hesab/cihaz axınını hədəflə: request mərhələləri və byte ölçüsü, final sync, cache hit/miss və first-frame vaxtını ayır; mövcud sübutu10+s-in tam həlli kimi təqdim etmə. Yeni böyük işi yeni chatdə bu handoff və docs/PLAN.md§1.1 ilə davam et. Owner baseline-profile binarilərinə toxunma.
