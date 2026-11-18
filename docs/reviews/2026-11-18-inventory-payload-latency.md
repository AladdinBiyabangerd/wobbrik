# İnventar — artıq checkpoint payload və cache itkisi / 18 noyabr 2026

Android branch `fix/inventory-payload-latency`, baza `8cd7bae`; AppBackend persistent `feat/wobbrik`, baza `838c0c7`. Lokal review bu bazalar üzərində production/test/contract diff-ini əhatə edir. İki release baseline-profile owner binarisinə toxunulmur. Main merge, push, deploy və CI yoxdur.

## Sübut edilmiş problem və düzəliş

İnventar siyahısı hər bina üçün opaque engine `snapshot` alırdı. `InventoryReads.building` bu sahəni istifadə etmir; renderer-in istifadə etdiyi accepted geometry ayrıca sahədir. `ReadOnlyGameCache.capture` isə xam cavabı saxlayırdı. Böyük, lakin serverin qəbul etdiyi səhifə 140000-byte cache limitini keçəndə persistence exception-u optional cache kimi udulurdu: canlı oxu uğurlu, disk bundle yox idi. `AccountViews.completed` bu halda növbəti girişdə replay edə bilmir və yenidən canlı capture edir. Bu, təkrarlanan şəbəkə asılılığının sübut edilmiş səbəbidir; istifadəçinin konkret cihazındakı 10+s-in tam səbəbi kimi təsdiqlənməyib.

Backend additive `inventory?limit=20[&cursor=ID]&view=display` əlavə edir. Eyni owner/non-deleted/keyset SQL-dən bütün əvvəlki metadata, parts və accepted geometry qalır; checkpoint SQL-də `NULL::jsonb` proyeksiya olunur, JSON-da `snapshot:null` gəlir. Köhnə inventory və building detail müqavilələri dəyişmir. Unknown/empty/repeated view rədd edilir. Schema, rules, wallet, mutation və dedup dəyişikliyi yoxdur. Müqavilə AppBackend `docs/wobbrik-game-api.md`-dədir.

Android yeni view-ni istəyir. Köhnə server query-ni ignore edəndə capture yalnız inventory body-dən istifadə olunmayan snapshot-u silir; digər metadata/geometry/cursor serverdən olduğu kimi qalır. Köhnə tam cache yalnız eyni query-nin view-siz formasına alias edilir; fərqli cursor/account/revision alias edilmir. Final sync/revision bracket, login epoch yoxlamaları və cached/fresh mutation gate saxlanır. Yeni cache limiti, ikinci wallet, prewarm, dizayn və UI dəyişikliyi yoxdur.

## Ölçmələr

Disposable PostgreSQL18, loopback55475/wobbrik_inventory_test, real AppBackend auth/middleware/HTTP handler-ləri, fake identity. 21 bina domain Start→Checkpoint→Finish API-si ilə yaradılır; 20-lik ilk səhifə və 1-lik ikinci səhifə. Checkpoint-lər typed10floor geometry və 16KiB-dan kiçik synthetic opaque padding daşıyır; production hesabının məlumat/paylanması deyil. Hər sample-dan əvvəl yalnız fixture rate history-si sıfırlanır; bütün ölçülmüş sorğular normal middleware-dən keçir.

- Eyni geometry/metadata: **335225→10905 JSON byte**, təxminən96.7% azalma. Hər mode1HTTP/11SQL; fanout/SQL sayı azalması iddia edilmir.
- Son race-enabled focused üç sample: legacy **52.25/49.11/48.36ms**, display **11.60/10.87/10.85ms**. Timer HTTP handler+JSON decode-u əhatə edir. Bunlar host/race nümunələridir; TLS/Firebase/production RTT, percentile və device latency deyil.
- İlkin geometry-siz legacy probe326265byte və2.7–4.2ms idi; onu typed/race nəticəsinə birbaşa latency müqayisəsi kimi işlətmirik. Lokal DB-də10s sorğu müşahidə edilmədi.

Real Kotlin cache/repository, synthetic20building transport, idarə olunan256byte/ms transfer (production sürəti deyil):

- Baza regression:324299successful response byte,2request, capture1318ms; **cache0byte/0file**, test gözlənildiyi kimi FAIL.
- Son full-response/old-server yolu: eyni324299byte/2request; ilk capture1274ms, cache4409byte. **İlk legacy download sürətlənmir**; təkrar replay0ms-rounded/0HTTP olur.
- Son compact synthetic yolu:4119response byte/2request, capture20ms, cache4769byte, replay0ms-rounded/0HTTP. Bu fixture backend-in10905byte geometry-li fixture-i ilə eyni deyil; rəqəmlər birləşdirilmir.
- Backend-in həqiqi synthetic display HTTP JSON-u Kotlin test resource kimi parserdən keçirilir:20building, cursor və hər binanın10accepted center-i saxlanır. Bu wire-contract testi Android→işlək Go authenticated E2E deyil.

## Yoxlamalar və lokal review

Son Android source: core158/158, app37/37 unit PASS; assembleDebug/lintDebug PASS58s. Əlavə testlər böyük legacy cavabın cache-də saxlanması, compact model ekvivalentliyi, exact legacy query replay, yanlış cursor/UID/revision və yeni path allowlist sərhədlərini yoxlayır. Əvvəlki cancellation/auth rejection/local-first mutation gate testləri də tam suite-də keçdi. İlk baseline regression-un FAIL nəticəsi düzəliş sübutudur, PASS kimi göstərilmir.

Backend production source-da tam `go test -race ./... -count=1` real disposable PostgreSQL ilə PASS, platform30.889s; go vet və server build PASS. Son test-only fixture-output dəyişməsindən sonra focused payload regression yenidən PASS2.453s. Legacy/display model ekvivalentliyi accepted geometry daxil olmaqla, pagination21→20+1, owner isolation, invalid view və legacy detail snapshot qorunması yoxlanır. Rate/auth təhlükəsizlik işi çıxarılmayıb. Diffcheck PASS.

Lokal review: SQL proyeksiyası yalnız server boolean-ından seçilir; istifadəçi dəyəri SQL-ə daxil edilmir. Limit+1/cursor və owner şərtləri ortaq qalır. Cache normalizasiyası yalnız inventory-də opaque snapshot-u silir; geometry və authority sahələri dəyişmir. Capture-in sync/session/revision yoxlamaları persistence-dən əvvəl qalır, replay capability yaratmır. Başqa production problem tapılmadı.

Evidence: `inventory-payload/evidence/{mobile-before,mobile-checks,backend-focused,backend-race}.txt`, `mobile-payload.xml`, `source-sha256.txt`. Backend counterpart `docs/reviews/2026-11-18-inventory-payload-latency.md`; fixture `core/src/test/resources/inventory-display-backend.json`. Reproduce: Android normal fokuslu Gradle tasks; backend TEST_DATABASE_URL ilə `go test -race ./internal/platform -run '^TestIntegrationInventoryPayloadProbe$' -count=1 -v`. `WOBBRIK_INVENTORY_FIXTURE=/absolute/path.json` yalnız synthetic HTTP fixture export-u açır.

## Məhdudiyyətlər

Qoşulmuş cihaz yoxdur (`adb devices -l` boş); emulator açılmayıb. Production10+s, fiziki frame/ART/GPU/HTTP RTT/battery və real hesabın payload paylanması ölçülməyib. Dəyişiklik UI/art/layout-a toxunmur; yeni vizual render/parity iddiası yoxdur. Əvvəlki vizual borclar qalır. Çox böyük geometry hələ transport/cache limitini keçə bilər; bunu unlimited-inventory həlli kimi təqdim etmirik. Final sync/disk completion ilk fresh nəticənin hissəsi olaraq qalır. Backend deploy edilmədən wire-byte azalması production-da işləməyəcək; köhnə serverdə yalnız mobil cache normalizasiyası fayda verir. Bütün10+s problemin həll edildiyi iddia edilmir.
