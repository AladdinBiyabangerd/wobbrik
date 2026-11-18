# İnventar — oxu keşinin disk kilidi / 18 noyabr 2026

Branch `fix/inventory-detail-latency`; yoxlanmış baza `54621a7` üzərində working diff. Production: yalnız `ReadOnlyGameCache.kt`; regression: `ReadOnlyGameCacheTest.kt`. UI dizaynı, DTO/API, server və journal dəyişmir. CI/merge/push/deploy yoxdur.

## Səbəb və düzəliş

`ReadOnlyGameCache.accepted/account/capture/replay` bütün fayllar üçün eyni instance monitorunu saxlayırdı. Hesabın qəbul edilmiş snapshot+kataloq yazısı və ya başqa baxışın `replace/fsync` işi hazır İnventar faylının replay-ni də bloklayırdı. `AuthenticatedRecoveryOwner` qəbul edilmiş hesabı nəşr etdikdən sonra IO-da `readCache.accepted` çağırır; buna görə Ready və lokal bundle olması bu disk kilidini aradan qaldırmırdı. Bu, kodda sübut edilmiş head-of-line blocking-dir; istifadəçinin cihazındakı10+s müddətin tam izahı deyil.

Kilid artıq mövcud SHA-256 UID/view fayl açarı üzrədir. Eyni fayla oxu/yazı serial qalır: `DurableJournalFile.read` yarımçıq `.new` faylını təmizləyə bildiyinə görə sadəcə bütün kilidləri silmək təhlükəli olardı. Kilid qeydinin istifadəsi gözləyənlər daxil olmaqla sayılır; son istifadəçi çıxanda qeyd silinir, səhifə/account sayı qədər daimi lock cədvəli böyümür. `accepted`-də əvvəlki snapshot+kataloq validasiyası və monoton revision compare+write eyni account faylının reentrant kilidi altında saxlanır. Lokal review-da bu validasiyanın əvvəlki davranışı dəqiq qorundu.

## Ölçmə

Core fixture real `ReadOnlyGameCache`, synthetic JournalFile, blocked unrelated account write. Əvvəl **621ms**, replay yalnız600ms test gözləmə pəncərəsindən sonra yazı buraxıldıqda tamamlandı; regression gözlənildiyi kimi FAIL oldu. Son source **1ms**, replay yazı hələ bloklu ikən tamamlandı. Bunlar ölçülmüş fiziki fsync müddətləri deyil, idarə olunan stall ilə kilid asılılığının ölçüsüdür. Əlavə regression eyni faylın oxusunun writer tamamlanana qədər başlamadığını və təkrar kilid əldə edilməsini yoxlayır.

Native SDK34/Robolectric xxhdpi: production `InventoryScreen`, `AccountViews`, repository/cache/owner və `MobileMenuShell`; transport və disk synthetic-dir. Qəbul edilmiş revision1 inventory bundle əvvəl yazılır. ApprovedArt əvvəl hazırlanır, route-də onun statusu Loading saxlanır, bütün fresh transport cavabları bloklanır. Home-dan İnventara keçiddən başlayaraq, metadata semantikası görünənədək ölçülür; fiziki GPU frame/cihaz cold startup deyil.

- Eyni unrelated view persistence1200ms bloklu baza: **1258ms**; lokal kart çıxanda disk artıq buraxılmışdı.
- Son source ilk host nümunəsi: **361ms**; kart disk, art və network hələ bloklu ikən görünür. Eyni suite-də sonrakı ölçü/font/üslub nümunələri **34–49ms**. Əvvəlki son-source təkrarlarında ilk333/332ms alınıb; percentile/cihaz benchmarkı deyil.
- İlk lokal təqdimata qədər **2request başlanır,0response byte** alınır (inventory/city). Server cavabları açıldıqdan sonra əvvəlki read+sync revision bracket tamamlanır. Sorğu qənaəti iddia edilmir.
- Hər halın2remount-u: **12–113ms**, **0əlavəHTTP**, buna görə əlavə response byte da0. Hər hansı wallet/ownership command göndərilmir.

Evidence: `inventory-first-open/evidence/{lock-before,lock-after,native-before,native-after}.xml`; build/unit/lint `checks.log`, native `native-checks.log`. Native reproduce: `cd android-billing` və `./gradlew -I ../docs/reviews/inventory-first-open/render/render.init.gradle :app:testDebugUnitTest --tests '*InventoryFirstOpenTest' --offline --console=plain`. Baza üçün production cache faylının54621a7versiyası işlədilib; final fixture əlavə olaraq kartın diskdən əvvəl görünməsini və0response byte-ı assert edir.

## Lokal/server uyğunluğu

Lokal məlumat yalnız əvvəl serverdən qəbul edilmiş eyni UID/revision bundle-dan, eyni parser və renderer-dən gəlir. Fərqli revision və ya çatışmayan bundle instant görünüş üçün qəbul edilmir. Keş Ready/mutasiya capability-si vermir; fresh detail gələnədək dəyişdirici əməliyyatlar disabled qalır. Fresh read-in server sync bracket-i, login epoch/auth rejection/cancellation, account izolyasiyası və sole command journal saxlanır. İkinci wallet və fərqli lokal bina modeli yoxdur.

AppBackend `docs/wobbrik-game-api.md` inventory/building owner scope və canlı cursor semantics ilə tutuşduruldu. Bu dəyişiklik yalnız lokal faylların concurrency-sidir; API/DTO/domain/schema/rule versiyası dəyişmir, backend dəyişikliyi tələb olunmur. AppBackend testləri və authenticated E2E bu vahiddə işlədilməyib.

## Yoxlama və məhdudiyyət

Son source: core154/app37unit, assembleDebug, lintDebug PASS26s; native7/7PASS14s. CI deyil. UID/revision miss, auth rejection, cancellation və cached detail mutation gate mövcud regression-larda yoxlanır; yeni testlər unrelated yazıdan azad replay və same-file serialization-u yoxlayır. Diff review: eyni fayl üçün compare/write və `.new/.bak` toxunmaları hələ serialdır; lock users xəritəsi qısa ayrıca monitor altındadır, disk işini saxlamır; giriş/çıxış exception-da da balanslıdır. Diffcheck PASS.

`ui-craft`: GameTokens əvvəl oxundu, yeni vizual dəyər yazılmadı.320/360/390portret font1/2, synthetic padding, purple/teal və art Loading/Failed/Ready native suite-dədir. Altı saxlanmış PNG açılıb `01-main.png` ilə müqayisə edildi; UI source dəyişmir. Kontrast Text/Surface13.00, Muted8.22, Accent8.99PASS. Əvvəlki kiçik portret iki sətir dock, font2horizontal tabs/kiçik illustration və tam PNG parity borcları qalır. Sintetik inset real OS inset deyil; uzun bütün mətn/balans, IME, locale, TalkBack və GPU yoxlanmayıb.

`adb devices -l` heç bir qoşulmuş cihaz göstərmədi; emulator açılmadı. İlk host361ms daxilində class/JIT/Compose-grid CPU payları ayrıca ayrılmayıb. Real production10+s, fiziki frame, cold84MiB atlas decode/yaddaş və digər menyuların2+s şikayəti açıq qalır. Bu vahid yalnız ölçülmüş əlavə disk-kilid gözləməsini aradan qaldırır; bütün app performansı bitmiş sayılmır.
