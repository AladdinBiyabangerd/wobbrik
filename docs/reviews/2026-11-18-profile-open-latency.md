# Profil açılışı — 18 noyabr 2026

Branch `fix/profile-open-latency`, baza `54621a7`; bu vahidin production diff-i `PublicAccountScreen.kt`, `OnlineGame.kt`, `NativeProfileOverview.kt`, `NativeProfileEditor.kt`-dir. Əvvəlki İnventar/chrome/core cache working diff-i saxlanır. İki release baseline-profile binary-si iş ərzində ayrıca dəyişmiş göründü; bu vahid onları yazmayıb və geri qaytarmayıb. Review yoxlanmış pre-commit diff-i əhatə edir; Profil vahidi ayrıca commit-lə saxlanır. Əvvəlki owner diff-i bu commit-ə daxil deyil. Merge/push/deploy/CI yoxdur.

## Səbəb və düzəliş

Production Home → `MobileMenuShell` → `PublicAccountScreen` yolu ölçüldü. Hər mount yeni `ProfileEditor` yaradır, əvvəlki profil göstərilmədən GET `/profile` gözlənilirdi. Mobil overview artıq native-dir: `ProfileFrame` mobil qolunda `ProfileArtProvider` və onun dörd legacy board/skin decode-u çağırılmır. Bu köhnə xərc cari səbəb kimi təqdim edilmir. Portrait helper əvvəl də IO-da işləyirdi, lakin yalnız profil cavabından sonra overview mount olanda başlayırdı. İlk kompozisiyada ölçülən böyük screen intervalı əvvəl34,48ms, sonra34,78ms-dir; böyük CPU sürətlənməsi iddia edilmir. İç-içə trace intervalları toplanmır.

- `ProfileDisplayMemory` authenticated root-da navigation-dan yuxarı yalnız son server `PublicProfile`-ını yaddaşda saxlayır. Configured/session parametrlərinə açıq bağlıdır; eyni UID/new epoch və başqa hesab yeni boş memory alır. Disk/process cache, Google identity, wallet və yeni authority yoxdur. Process/root yenidən yaradılarsa ilkin GET yenə lazımdır.
- Təkrar giriş cached ad/avatarı dərhal göstərir, hər mount-da əvvəlki kimi bir fon GET-i işləyir. Yeni route editoru yenə fresh GET-dən öz revision-unu alır. Cached profile, loading və refresh failure redaktə/save capability-si deyil: ayrıca `fresh` gate-i var. Save və conflict-dən qəbul edilən server profili də növbəti display üçün saxlanır; uncertain replacement/revision retry core qaydaları dəyişmir.
- Yalnız mobil profile route-da portrait IO GET-lə paralel başlayır; overview/editor/avatar picker bir route-owned resource state/bitmap-i paylaşır. Route bağlananda loaderin mövcud cancellation sərhədi qalır. Home-da yeni eager decode, process bitmap cache və tablet dəyişikliyi yoxdur. Standalone native composable-lar mövcud fallback loader-i saxlayır.

## Müqayisəli ölçmə

Native Robolectric API34,360×640,xxhdpi, real Home və profile composable-ları; synthetic account/transport. HTTP cavabı IO worker-də real400ms sleep ilə gecikdirilib; `delay` virtual Compose saatına qarışdığı üçün ilkin virtual-delay nümunələri yekun baseline kimi götürülmür. Hər müqayisə yalnız `ProfileOpenLatencyTest.firstAndReturningProfile` seçilərək ayrıca test run-da aparılıb. Class/JIT/host scheduling dəyişkənliyi var; percentile/device/frame-present benchmarkı deyil. Artwork əvvəlcədən profil üçün prewarm edilməyib.

- İlk profil mətni: əvvəl529,26ms, son581,59ms. İlk avatar: əvvəl571,76ms, son582,17ms. **Cold giriş sürətlənməsi sübut olunmayıb**; bu iki tək host nümunəsinin fərqi ayrıca causal bölgü/production regression sübutu deyil. Serial mətn→avatar mərhələsi42,50ms-dən0,58ms-ə düşüb; ilk giriş yenə profili şəbəkədən alır.
- Təkrar girişlərdə mətn: əvvəl455,03/451,23/475,14ms; sonra36,29/38,08/24,19ms.
- Təkrar girişlərdə avatar: əvvəl510,28/492,54/516,46ms; sonra78,78/82,63/66,44ms. Route remount hələ IO portrait hazırlığı edir; bu şəkil xərci tam aradan qaldırılmayıb.
- Hər giriş əvvəl və sonra1GET/61response UTF-8 bayt; dörd giriş4GET/244bayt. Headers/request bytes daxil deyil. Traffic/backend sürətlənməsi iddia edilmir. Ölçmə timestamp-i mətn/avatar semantics görünən anda götürülür; növbəti round-dan əvvəl fresh edit gate-i gözlənilir.

Raw `profile-open-latency/evidence/{before,after}-{rounds.csv,spans.tsv}`; cache-only aralıq nümunə ayrıca `cache-only-rounds.csv`-dədir, final deyil. Tracing yalnız opt-in QA testindədir, app source/dependency-sinə əlavə edilməyib. Main composition ilk~35ms xərci qalır; ART/class-load/GPU hissələri bu vahiddə ayrılmayıb.

## Yoxlamalar və lokal review

- Son normal core154/app37unit, assembleDebug və lintDebug PASS51s. Bu CI deyil. Log `/private/tmp/wobbrik-profile-checks.log`.
- Native axın2 + overview5 + editor/avatar5 =12/12PASS20s (son müstəqil fixture ilə təkrar), failure/error/skipped0. Ayrıca paired-source isolated ölçmə1/1PASS10s; müstəqil fixture-in son isolated yoxlaması1/1PASS9s. Flow testləri cached display/fresh mutation gate, bloklu GET, refresh failure/retry, eyni UID/new epoch, başqa hesab, save və son server adının növbəti girişdə göstərilməsini yoxlayır. Cached profil əsasında write yoxdur; save gözlənilən fresh revision1ilə gedir.
- Test ilkin mərhələdə cached refresh failure-dan sonra `busy=false`-ın edit-i açdığını tutdu; ayrıca `fresh` gate ilə düzəldildi. Epoch ayrıca composable parametrinə çıxarıldı ki, mutable session getter-i Compose skipping arxasında gizlənməsin. Test fixture-in root epoch invalidation-u production lifetime-a uyğunlaşdırıldı. Pinned düyməyə yanlış `performScrollTo`, effect başlamadan yalnız atomic counter gözləməsi və standalone renderer-in async portrait-i gözləməməsi də QA-da düzəldildi. Aralıq FAIL-lər PASS sayılmır.
- Mövcud `NativeProfileEditorRenderTest` yalnız avatar IO completion-u gözləmək üçün2wait aldı; istehsal UI davranışını dəyişmir.
- `GameTokens`/mövcud ölçülər saxlanır. Kontrast: Text/Surface13,00; Muted8,22; Accent8,99; Danger8,37PASS. `evidence/contrast.txt`.
- Native şəkillər `profile-open-latency/rendered/` altındadır (repo qaydasına görə PNG-lər ignored-dur). Açılıb baxılanlar: actual fresh və cached-error360/font1; overview320/font1,320/font2,390/font1,390/font2,360/font2+simulated inset/uzun ad/Long.MAX balans; editor390/font1 və avatar picker320/font2. `03-account.png` ilə müqayisədə artwork, native ribbon/panel/action və chrome saxlanır. Cached-error-də status/retry scroll olunan body-də, disabled Dəyiş sabit hissədədir. Kiçik mobil iki sətir dock, böyük şrift qısayollarında horizontal scroll, tam PNG parity və real inset/IME/locale/TalkBack borcları əvvəlki kimi qalır. Actual flow fixture-də account XP interval/best metadata yoxdur; həmin PNG full-account parity sübutu deyil.
- Production source SHA256-ları `evidence/source-sha256.txt`-dədir. Diff review: root lifetime və epoch key, session/Ready checks, fresh edit/save gate, successful load/save/conflict publish, failed refresh, IO resource sharing, standalone fallback və tablet qolu yoxlandı. Əlavə problem tapılmadı. `git diff --check` PASS.

AppBackend `docs/wobbrik-game-api.md` public profiles bölməsi və `internal/wobbrik/profile.go`, mobil `PublicProfiles`/`ProfileEditor`/HTTP müqaviləsi yoxlandı: profil revision-u iqtisadi revision-dan müstəqildir, fresh load və existing conflict/exact retry saxlanır. API/DTO/schema/domain/server dəyişikliyi tələb olunmur; backend dəyişdirilmədi, backend test/E2E işlədilmədi.

## Təkrar yoxlama və sərhəd

Repo kökündə JDK21 ilə:

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' android-billing/gradlew -p android-billing -I ../docs/reviews/profile-open-latency/render/render.init.gradle :app:testDebugUnitTest --tests '*ProfileOpenLatencyTest.firstAndReturningProfile' --offline --console=plain
```

Müqayisəli before/after cütü eyni inventory host fixture-indən istifadə edib; commit üçün profile-only fixture eyni snapshot/catalog/owner/HTTP/UI ilə həmin uncommitted test mənbəyindən ayrılıb. Ayrı fixture-in əlavə son ölçməsi `standalone-fixture-rounds.csv`-də saxlanır və müqayisəli cütə qarışdırılmır. Bu init yalnız öz profile fixture-ini və mövcud mobile-sizing render harness-ini istifadə edir; normal build/CI-yə qoşulmur. Raw yeni ölçülər `/private/tmp/wobbrik-profile-open/{rounds.csv,spans.tsv}`-yə yazılır. Regression üçün `--tests '*ProfileOpenLatencyTest.cachedDisplay*' --tests '*ProfileOpenLatencyTest.successfulSave*'`, vizual matrix üçün review-da adı göstərilən MobileProfile/NativeEditor testləri seçilir. Baseline profile screen mənbələri `54621a7`-dədir; before harness həmin mənbələrdə yeni displayMemory arqumenti/helper-i olmadan eyni Home/HTTP/timing yolunu çağırırdı. Normal checks init-sizdir.

Production2+s, fiziki frame/ART/GPU/HTTP/yaddaş/battery və authenticated Android→GoE2E ölçülməyib. Emulator/cihaz açılmayıb. Bu vahid təkrar Profil girişində sübut edilmiş network presentation gate-ni çıxarır; ilk giriş, bütün menyular və app-wide performans bitmiş sayılmır.
