# Wobbrik — vahid aktual plan

Son yenilənmə: **13 noyabr 2026**.

Bu sənəd yeganə plandır: mərhələlər, məhsul qaydaları, iqtisadiyyat, kataloq və texniki sərhədlər burada saxlanılır. `context/current-task.md` yalnız qısa handoff-dur; `reviews/` keçmiş yoxlama sübutları, `design/` vizual istinadlardır. Onlardakı köhnə status və tapşırıqlar paralel plan deyil. Əvvəlki plan faylları silinib; tarix git və ayrıca backup-da qalır.

## 1. Cari vəziyyət və mərhələlər

İstifadəçinin son qərarları: yalnız Android, Kotlin + Compose/Canvas ilə yekun oyun; ilk girişdə Google məcburidir, qonaq/link yoxdur; AppBackend/PostgreSQL vahid authority-dir. Canlı yoxlama heç vaxt tələb edilmir, emulator avtomatik açılmır. Yoxlanmamış runtime/visual nəticə PASS kimi göstərilmir. CI yalnız ayrıca istifadəçi tələbi olduqda, merge-dən sonra main-də `./scripts/local-ci.sh` ilə işləyir; fokuslu build/lint/unit və lokal disposable DB testləri CI deyil.

1. **Texniki təməl — mövcuddur.** `android-billing/` daxilində app/engine/core/android modulları, Firebase kimlik və AppBackend adapter sərhədləri var. Pinlərin authority-si build fayllarıdır: minSdk 24, target/compileSdk 36, JVM 21; yeni stack seçilmir.
2. **Əsas mexanika — inteqrasiya edilib.** Kran/drop/landing, tarazlıq/xilasetmə, checkpoint restore, təsdiqlənmiş oyun qrafikası və pause/frame-clock idarəsi var. Performans və bütün adaptiv hallar üçün yeni ölçmə iddiası yoxdur.
3. **Onlayn oyun — əsas vahidlər main-dədir.** Application-owned host/recovery, exact Ready, davamlı command journal, checkpoint/explicit finish, hesab izolyasiyası, kosmetik draft, Room oxu keşi və restart/abandon mövcuddur. Məcburi Google giriş Android-də, anonymous rəddi backend-də merge edilib. Köhnə guest/link planı ləğvdir. Telefon/canlı yoxlama gate deyil; kosmetik seçim vizualı istifadəçi tərəfindən qəbul edilib. Tam ictimai buraxılış hazır sayılmır.
4. **Kolleksiya/hissələr/şəhər — istifadəçi başlamağa icazə verib, yekun mobil axın açıqdır.** Backend inventory/detail/city/placement və typed geometry mövcuddur. Növbəti kiçik vahid accepted finish → inventory → bina detalı (sakin/dəyər/hissələr) və city membership oxusudur. Konkret inventory/detail/loading UI təsdiqi hələ gözləyir. Mağaza, edit/delete, placement/dekor/land və real şəhər renderer-i ayrıca sonrakı vahidlərdir. Backend API olması mobil tamamlanma deyil.
5. **Bazar/rekordlar — mobil inteqrasiya açıqdır.** Elan yarat/geri götür/al, revision/dedup, vahid wallet və leaderboard UI qoşulmalıdır.
6. **Reklam/alış — məhsul inteqrasiyası açıqdır.** Consent, test ads, reward davam fizikasının razılaşdırılması, reklam hissə irəliləyişi və billing UI. Real SKU, Console, production alış/deploy ayrıca göstəriş tələb edir.
7. **Vizual tamamlama/buraxılış — açıqdır.** Təsdiqlənmiş dizaynın tam tətbiqi, səs/haptic/a11y, adaptiv kompozisiya və performans. Ölçülməyən hallar məhdudiyyət kimi qeyd olunur; canlı yoxlama tələb edilmir. Buraxılış/deploy ayrıca qərardır.

Google vahidinin sübutu: [Android review](reviews/2026-11-13-required-google-sign-in.md). Backend dəyişiklikləri ayrıca AppBackend review və API müqaviləsindədir. Main merge/push backend deployment demək deyil; bu tapşırıqda deploy yoxdur.

## 2. Arxitektura və server müqaviləsi

- Paket `com.aladdin.wobbrik`; `:app` Compose/Canvas və lifecycle, `:engine` Android-dən asılı olmayan fizika, `:core` DTO/domain/coordinator, `:android` Android data/billing adapterləridir. UI, orchestration, domain və persistence ayrılır; boş layer və ikinci framework yaradılmır.
- Firebase Auth kimlik üçündür. Android yalnız Google-backed sessiyanı oyuna buraxır; Credential Manager Google tokenini Firebase credential-a çevirir. Shared backend permanent-account provider siyasətini digər tətbiqlər üçün saxlayır, Wobbrik anonymous istisnası yoxdur. UID/app request body-dən authority kimi götürülmür.
- Hesab açarı `(app_id, uid)`-dir. Wallet/ledger, ownership, progression, cache/journal, revision, receipt və operation ID-ləri tenant/hesab sərhədini qoruyur. Oyunlar arasında ortaq balans və transfer yoxdur.
- AppBackend `/Users/mac/My Workspace/AppBackend` (Go + PostgreSQL) bütün xərclənən sikkələr, qəbul edilmiş oyun vəziyyəti və mülkiyyət üçün vahid authority-dir. Oyun/nailiyyət/alış/bazar eyni wallet/ledger-dən istifadə edir. Client delta və Firestore ikinci wallet deyil. Firebase Spark saxlanılır; Blaze/Cloud Functions və ikinci server infrastrukturu əlavə olunmur.
- Room yalnız hesab üzrə qəbul edilmiş server snapshot oxu keşidir. Yeganə command outbox `DurableJournalFile`-dır (temp + fsync + rename + directory fsync; legacy qalıqları qoruyur). Room-a ikinci outbox köçürülmür. Keş Ready capability və ya alış/mükafat icazəsi deyil; eyni revision-da billing balans yenilənməsi nəzərə alınır.
- Mutasiyalar operation ID + expected revision + rules/engine version ilə işləyir. Şəbəkədən əvvəl durable envelope yazılır; naməlum nəticədə yeni ID/sorğu uydurulmur, eyni envelope retry/reconcile edilir. Auth/login epoch dəyişməsi köhnə cavab/Ready/token-i ləğv edir; journal başqa hesabla göndərilmir və silinmir.
- Checkpoint yalnız yeni mərtəbə üçün sikkə/XP/nailiyyət və accepted progress-i atomik yazır. Finish bir cəhd üçün bir nəticə, minimum 10 mərtəbədə bir bina yaradır; checkpoint mükafatını təkrar vermir. Structural validation fizikanın həqiqiliyinin/replay-in sübutu deyil; saxta uyğun checkpoint riski qalır.
- Mağaza: server qiymət/level şərti + debit + hüquq/nüsxə bir commit. Bazar: alıcı debiti, satıcı xalis krediti, komissiya, ownership, satıcının city yerləşdirməsinin çıxarılması və elan bağlanması bir commit; sabit lock sırası/revision/constraints concurrency-ni qoruyur.
- Google Play alışını backend Developer API ilə yoxlayır; token ilk hesaba bağlanır, eyni token təkrar kredit vermir. PENDING hüquq vermir. Ack/consume yalnız serverdə delivery commit-dən sonra worker ilə; client hüquq vermir və consume/ack etmir. Daimi hüquqlar bərpa olunur, çatdırılmış consumable yenidən verilmir.
- API/DTO-nun dəqiq authority-si AppBackend `docs/wobbrik-game-api.md` və `docs/wobbrik-billing.md`-dir. Burada ikinci API schema saxlanılmır. Domain dəyişikliyi hər iki repo müqaviləsi/migration/test uyğunluğu ilə yoxlanır; backend dəyişikliyi lazım deyilsə review-da açıq yazılır.

### Qəbul vaxtı və leaderboard

- Vahid təqvim saatı PostgreSQL primary-nin saatıdır. `accepted_at` yoxlamalar və lazımi kilidlərdən sonra, nəticə yazılmazdan əvvəl bir dəfə `clock_timestamp()` ilə götürülür və nəticə ilə eyni tranzaksiyada saxlanır. Yalnız commit edilmiş qeyd qəbul edilmiş sayılır. Bu, HTTP giriş və ya dəqiq commit timestamp-i deyil. Rollback olduqda qəbul vaxtı yaranmır; sonrakı uğurlu cəhdin vaxtı əsas olur.
- `now()`/`CURRENT_TIMESTAMP` tranzaksiyanın başlanğıcını, `statement_timestamp()` statement başlanğıcını verir; buna görə onlar burada yoxlamalardan sonrakı qəbul anının əvəzi deyil. DB saatının sinxronluğu və saat sürüşməsinin monitorinqi istismar tələbidir; timestamp mütləq monoton sıra zəmanəti vermir.
- Vaxt anları `timestamptz` ilə saxlanır və API-də UTC verilir. Həftə `Asia/Baku` ilə hesablanır: bazar ertəsi 00:00 daxil, növbəti bazar ertəsi 00:00 xaric. Həftə açarı il + həftə nömrəsi əvəzinə Bakı həftəsinin başlanğıc tarixi ola bilər. Sərhəd hesablaması serverin lokal timezone parametrindən asılı olmur.
- Həftəni yalnız cəhdin yekun nəticəsinin `accepted_at` vaxtı seçir; start/checkpoint vaxtı həftəni rezerv etmir. Məsələn, bazar 23:59-da bitən, bazar ertəsi 00:01-də qəbul edilən nəticə yeni həftəyə düşür. Sorğu sərhəddən əvvəl gəlib kiliddə gözləsə də, yoxlamalardan sonrakı qəbul anı əsasdır. Commit sərhəddən sonra bitsə, saxlanmış qəbul anı dəyişmir.
- Eyni əməliyyatın commit-dən sonra retry-si ilkin nəticə ID-si, `accepted_at` və həftəni qaytarır. Cəhdin bir dəfə yekunlaşdırılması invariantı fərqli operation ID ilə göndərilən təkrarı da ikinci nəticəyə çevirmir.
- Sıralama: uğurlu mərtəbə sayı azalan, `accepted_at` artan, sabit server nəticə ID-si artan. Eyni hesabın eyni mərtəbə rekordunda əvvəl qəbul edilən nəticə saxlanır; daha yüksək nəticə öz qəbul vaxtı ilə onu əvəz edir. Ümumi rekord və həftə rekordu ayrıca seçilir. Nəticə ID-si yalnız tam bərabərlikdə sabitlik üçündür, ayrıca xronologiya vədi deyil.
- İlk uğurlu onlayn giriş AppBackend-in autentifikasiyalı hesab açılışı/sinxronizasiyasını DB-də ilk dəfə uğurla qeyd etməsidir; yalnız Firebase tokeninin alınması kifayət deyil. `first_online_at` app + uid üzrə bir dəfə, paralel cihazlarda da atomik yazılır. Güzəşt həmin anın aid olduğu Bakı günündən sonrakı gecəyarısında bitir. Retry, yenidən giriş və reinstall bu qeydi dəyişmir; yeni UID məsələsi hesab siyasətinin sərhədində qalır.
- 5 dəqiqə aktiv oyun müddəti təqvim vaxtından ayrıdır: cihazda monoton müddət sayğacı ilə ölçülür, pause/arxa plan/menyu/reklam daxil edilmir. Server vaxtından iki timestamp çıxmaq aktiv oyun müddətini sübut etmir. Müştəri bildirişinin etibarlılığı ayrıca məhdudiyyətdir; sayğacın bərpası və serverdə saxlanması reklam inteqrasiyası müqaviləsində dəqiqləşdiriləcək.

## 3. Cəhd lifecycle və bərpa

- `AuthenticatedRecoveryOwner` Firebase listener-dən hər login epoch-u alır; dəyişmə sinxron olaraq hostu və əvvəlki Ready-ni ləğv edir. Token alınması backend sync uğuru deyil. Gecikmiş cavab, eyni UID ilə re-login və logout ayrıca qorunur.
- **13 noyabr 2026 qərarı:** `AcceptedEngineHost` cari exact Ready + foreground ilə lokal fizikanı işlədir; uğurlu enişdə dayanma, server checkpoint-i və reload yoxdur. Yalnız məğlubiyyətdə yığılmış uğurlu mərtəbələr bir terminal checkpoint-də göndərilir; qəbuldan sonra finish mövcud API ilə işləyir; aşağıdakı son kosmetik qərarın avtomatik finish inteqrasiyası hələ implementasiya edilməyib. Sıfır mərtəbədə checkpoint yoxdur. Wallet/XP/bina yalnız server qəbulundan sonra authority-dir; yarımçıq tərk edilən lokal irəliləyiş mükafat qazandırmır. Waiting/recovery/error/pause-də drop bağlıdır. Frame clock arxa plandan qayıdanda reset olur, avtomatik resume yoxdur.
- Şəbəkə fasiləsi eyni aktiv ekran daxilində pause/retry-dir; accepted checkpoint əsasdır. Düşən mərtəbə restore zamanı krana qayıdır. Yeni bina/reward lokal yaradılmır. Uyğunsuz journal/versiya bloklanır və baytları saxlanır.
- **13 noyabr Geri düzəlişi:** aktiv oyunda Android Geri yalnız mövcud pauza panelini açır; təkrar Geri də cəhdi tərk etmir. Menyuya qayıt açıq çıxışdır. Mövcud kosmetik seçimdə Geri home-a qayıdır; aşağıdakı yeni qərar bu finish-picker axınını əvəz edəcək. Bu, 9 noyabrın aktiv oyunda Geri ilə tərk etmə hissəsini əvəz edir.
- **9 noyabr restart/abandon qərarı (Geri üçün yuxarıdakı son qərarla):** Home/oyunun ortasında tətbiqi bağlamaq açıq cəhdi tərk edir. Oyna əvvəl serverdə abandon-u reconcile edir, sonra yeni cəhd açır; abandon nəticə/bina/refund/XP yaratmır. Bu, əvvəlki “hər process ölümündən sonra aktiv cəhdə qayıt” qaydasını əvəz edir. Davam yalnız uduzulmuş cəhdin son accepted progress-i və explicit finish/kosmetik təsdiq niyyəti üçün saxlanılır.
- **13 noyabr son kosmetik qərarı — icra gözləyir:** oyun bitəndə kosmetik seçim açılmır; nəticə mövcud checkpoint → finish müqaviləsi ilə avtomatik tamamlanır və oyunçu ana səhifəyə qayıdır. Kosmetik seçim ayrıca, oyun xaricində istənilən vaxt açılan və sonradan dəyişdirilən standart görünüş seçimidir; növbəti oyun həmin seçimlə açılır. Seçim hesab üzrə saxlanmalı, cari kataloq/unlock-larla yoxlanmalı və başlanmış cəhdin seçimi sonrakı dəyişikliklə dəyişməməlidir. Bu qərar əvvəlki məcburi oyun-sonu seçim/açıq finish təsdiqini əvəz edir. Hazır kod hələ köhnə axındadır; avtomatik finish, daimi standart seçim və oyun qrafikasına tətbiq ayrıca növbəti vahiddir.
- Terminal checkpoint və finish ayrı durable əmrlər olaraq qalır; pending envelope dəyişmir. Miss yeni mərtəbə əlavə etmədən son accepted progress-lə müqayisə olunur. Bu kosmetik qərar ikinci wallet, yeni backend və ya mövcud server müqaviləsinin dəyişməsi üçün göstəriş deyil.
- Reward-la maksimum bir davam məhsul qaydası qalır; onun fizikanı təhlükəsiz stabilləşdirməsi reklam vahidində dəqiqləşdirilməlidir. Hazır olmayan reklam axını implementasiya olunmuş kimi göstərilmir.
- Sessiya/hesab məlumatı silinmir, wallet-lər birləşdirilmir. Tətbiq pre-release-dir, legacy production istifadəçi miqrasiyası bu vahidin gate-i deyil.
- Lokal fake/unit/regression və disposable PostgreSQL testləri real Firebase/device sübutu adlandırılmır. Canlı yoxlama tələb edilmir; emulator avtomatik açılmır.

## 4. Məhsul qaydaları

### Məqsəd və əsas təcrübə

Oyunçu krandan mərtəbələr buraxır, binanın tarazlığını idarə edir, şəxsi rekordunu yeniləyir və leaderboard-da başqalarını keçir. Uyğun binaları şəhərinə yerləşdirir və ya digər oyunçulara satır. Qazandığı sikkə ilə görünüşlər, binalar və şəhər imkanları əldə edir.

Əsas hiss: “Bina əyildi, amma növbəti mərtəbə ilə onu xilas edə bilərəm.”

Oyun nə həddən artıq rahat, nə də bezdirəcək qədər çətin olmalıdır. Məğlubiyyətin səbəbi görünməli, oyunçu növbəti cəhddə nəyi yaxşı edə biləcəyini anlamalıdır. İlk ilham Tower Bloxx sadəliyidir; ad, qrafika, səs və interfeys kopyalanmır.

### Platforma və idarəetmə

- Mobil, portret ekran, bir əllə oyun. Təsdiqlənmiş platforma: yalnız Android; iOS versiyası olmayacaq. Bu qərar konkret mühərrik seçmir.
- Bir toxunuş bir mərtəbə buraxır. Kompüterdə klik və boşluq düyməsi eyni işi görür.
- 4 noyabr 2026 təsdiqi: telefon və planşetdə ayrıca Burax düyməsi yoxdur; oyun sahəsinə toxunulur. Aktiv oyunda yeganə düymə pauzadır; yan/alt menyular göstərilmir. Mərtəbə sayı və rekord interaktiv olmayan göstəricilərdir. Pauzada yalnız Davam et, Yenidən başla, Menyuya qayıt görünür. Pauza/panel toxunuşu mərtəbə buraxmır; Davam et toxunuşu da səhnəyə ötürülmür. Cari vizual mənbə: design/game-tap-v2/README.md.
- Mərtəbə düşərkən əlavə toxunuşlar nəzərə alınmır.
- Sürüşdürmə və telefonu əymə ilə tikinti idarəetməsi yoxdur. Şəhərdə yerləşdirmə idarəetməsi ayrıca prototiplənəcək.
- Tətbiq arxa plana keçəndə oyun fasiləyə düşür.
- Təsdiqlənmiş qayda: oflayn rejim məşq üçündür. Sikkə, satıla bilən bina və leaderboard nəticəsi yalnız onlayn qazanılır. Bazar və ödənişlər də internet tələb edir. Oflayn məşq XP və nailiyyət də qazandırmır (təsdiqlənib). Oflayn qurulan bina şəhərə və inventara əlavə edilmir; məşqin sonunda yalnız mərtəbə sayı göstərilir (təsdiqlənib).

### Tikinti və fizika

Kran mərtəbəni sağa-sola yellədir. Buraxılan mərtəbə düşür, binaya oturur və zərbə yaradır. Qurulmuş mərtəbələr də bir-birinə bağlı bina kimi uyğun yellənir. Kamera binanın yuxarısını və növbəti mərtəbəni görünən saxlayır.

Mərkəzə yaxın eniş daha sabitdir. Sağ və sol yerləşdirmələr tarazlığı həmin istiqamətə dəyişir və bir-birini qismən kompensasiya edə bilir. Fizika tam tikinti simulyasiyası yox, ardıcıl və oxunaqlı modeldir.

- İlk versiyada mərtəbələrin eni, dərinliyi, hündürlüyü və fiziki çəkisi eynidir. Krandakı və artıq yerləşdirilmiş mərtəbələr eyni modul ölçüsünü saxlayır; fərq yerləşdirmə ofsetindən yaranır.
- Aktiv oyunda dam örtüyü verilmir və kran dam daşımır. Oyunçu uduzduqda sistem binaya avtomatik dam örtüyü əlavə edir; bu, ayrıca oynanılan mərtəbə deyil və mərtəbə sayını, sakinləri, XP və mərtəbə mükafatını artırmır.
- Görünüş və alışlar fiziki üstünlük vermir.
- Zərbənin yaratdığı yellənmə zamanla zəifləyir.
- Gözləmək təkbaşına məğlubiyyət yaratmır; təhlükə yeni yerləşdirmənin nəticəsidir.
- Təhlükəli əyilmə ilə geri dönməz aşma fərqləndirilir. Təhlükəli halda növbəti mərtəbə ilə xilasetmə imkanı qalır.
- Tam kənara düşən mərtəbə və geri dönməz aşma cəhdi bitirir.
- Hər mərtəbənin ayrıca sürüşüb dağılması ilk versiyanın tələbi deyil.

Kran sürəti, tarazlıq həddi, zərbə gücü, yellənmə, zəifləmə və çətinlik artımı ayrıca dəyişdirilən parametrlər olmalıdır.

### Çətinlik və reaksiyalar

İlk beş mərtəbə daha bağışlayıcıdır. Sonra kran sürəti və binanın həssaslığı tədricən artır, artıma yuxarı hədd qoyulur. Qəfil və xəbərsiz sürət dəyişməsi yoxdur.

Bir cəhd üçün ilkin hədəf 30–60 saniyədir; vaxt limiti qoyulmur. Bacarıqlı oyunçunun daha uzun oynaması mümkündür.

Dəqiq eniş, adi eniş, təhlükəli əyilmə, uğurlu xilasetmə və aşma fərqli reaksiyalar alır. Aşma animasiyası, zərbə/toz effekti və uyğun vibrasiya olmalıdır. Səs və vibrasiya ayrı söndürülür. Təhlükə yalnız rənglə göstərilmir.

### Bina tamamlamaq və saxlamaq

Minimum 10 uğurla yerləşdirilmiş mərtəbəyə çatan bina saxlanmağa və satışa uyğun olur. Bu hədd istifadəçi tərəfindən təsdiqlənib.

Təsdiqlənmiş qayda: oyunçu 10 mərtəbədən sonra da tikintiyə davam edir. Könüllü “Binanı tamamla” seçimi nəzərdə tutulmur. Məğlubiyyətdə son uğursuz mərtəbə çıxarılır və son uğurlu yerləşdirmədən sonrakı bina vəziyyəti oyunçuda qalır. Vizual aşma uğurlu tikintini silmir. Saxlama və satış üçün 10 mərtəbə həddi qüvvədədir. Bir cəhd yalnız bir yekun bina və bir dəfə nəticə mükafatı yaradır.

Bina qeydi mərtəbə sayını, mərtəbələrin yerləşməsini, əyriliyini, hissələri, sakin tutumunu, yaradıcını və sahibini saxlayır. Saxlama həddi altındakı cəhd bina yaratmasa da qaydalara uyğun oyun irəliləyişi verə bilər.

### Fasad və hissələr

Təsdiqlənmiş başlanğıc kolleksiyası: dəqiq 6 pulsuz üslub var və hamısı ilk gündən seçilə bilir. Oyunçu bərabər dəyərli başlanğıc seçimindən öz üslubunu seçir. Təsadüfi bahalı fasad verilmir. Fərqlilik hissələrin oyunçu tərəfindən birləşdirilməsindən yaranır.

Hissələr: divar rəngi/materialı, pəncərə, balkon/giriş, dam və işıqlandırma. Hər kateqoriyada pulsuz, reklamla, sikkə ilə və birbaşa real pulla əldə edilən seçimlər ola bilər. Oyun və nailiyyətlə açılan hissələr də nəzərdə tutulur.

Təsdiqlənmiş qayda: bəzi xüsusi bacarıq hissələri yalnız nailiyyətlə açılır; onları sikkə, reklam və real pulla açmaq mümkün deyil. Hər hissənin açılma şərti və reklam tələb edirsə irəliləyişi əvvəlcədən görünsün.

Təsdiqlənmiş hazır bina redaktəsi: oyunçu sahib olduğu binanın hissələrini hesabında açıq olan hissələrlə dəyişə bilər. Bu qayda öz qurduğu və aldığı binaya aiddir; alınmış bina yenə təkrar satıla bilməz. Mərtəbə düzülüşü, əyrilik və sakin sayı qorunur, satış dəyəri yenidən hesablanır. Aktiv satış elanı varsa əvvəl geri götürülür.

Hissənin hesabda açılması gələcək tikintilər üçün istifadə hüququ verir. Təsdiqlənmiş qayda: reklamla açılan fasad və hissələr daimidir, təkrar istifadə üçün reklam tələb olunmur. Bir neçə reklam tələb edən hissələrdə tamamlanmış baxışların irəliləyişi saxlanılır. Hazır bina almaq onun hissələrini hesabda açmır. Hissələrin təsdiqlənmiş sayı və üslub adları §6-dədir; dəqiq qrafik icra ayrıca hazırlanacaq.

### Şəhər və sakinlər

Təsdiqlənmiş inventar qaydası: bina sayı üçün oyun limiti yoxdur; texniki saxlama və səhifələmə ayrıca layihələndiriləcək. Lazımsız bina aydın təsdiqdən sonra silinə bilər, sikkə qaytarılmır. Aktiv satış elanındakı binanı silmək üçün əvvəl elan geri götürülür.

Şəhərdə oyunçunun qurduğu və aldığı binalar görünür. Binalar inventardan yerləşdirilə, yerini dəyişə və inventara qaytarıla bilər. Yerləşmiş binanı başqa bina ilə dəyişmək mümkündür; yeni bina sığmırsa dəyişiklik tamamlanmır. Əvvəlki bina itmir.

Yerləşdirmə binanın faktiki əyriliyini və bütün tutduğu sahəni nəzərə alır. Qonşu binalar üst-üstə düşmür. Təsdiqlənmiş qayda: şəhərdə saxlanmış bina forması sabit qalır; tikintidəki təhlükəli yellənmə davam etmir.

Şəhər canlı görünməlidir. Təsdiqlənmiş V1 canlılığı: az sayda hərəkət edən sakin və avtomobil, pəncərə işıqları və yüngül ağac hərəkəti. Hər sakini ayrıca simulyasiya etmək tələb olunmur.

Binanın ölçüsünə uyğun sakinləri olur və sakinlər satış dəyərinə təsir edir. Təsdiqlənmiş formula: sakin sayı = uğurla yerləşdirilmiş mərtəbə sayı × 4. Məsələn, 10 mərtəbəli bina 40 sakinlidir. Fasadın qiyməti sakin sayını dəyişmir. Təsdiqlənmiş qayda: sakinlər passiv sikkə və ya avtomatik vergi gəliri yaratmır. Təsdiqlənmiş V1 sərhədi: köçmə, aclıq və xidmət idarəetməsi yoxdur. Təsdiqlənmiş qayda: şəhərin əhali göstəricisinə yalnız şəhərdə yerləşmiş binaların sakinləri daxildir; inventardakılar sayılmır. Satış elanı olan bina şəhərdə qaldığı müddətdə sakinləri sayılır, satılıb çıxarılanda çıxılır.

Başlanğıc sahə pulsuzdur. Genişlənmə əvvəl tələb olunan leveldə açılır, sonra sikkə ilə alınır. Təsdiqlənmiş qayda: hər uğurla yerləşdirilmiş mərtəbə 1 XP qazandırır; cəhd 10 mərtəbəyə çatmasa da bu XP verilir. Nailiyyət tamamlamaq da XP qazandırır; mövcud üç kateqoriyanın bonusları §4 (sikkə və nailiyyətlər)-də müəyyən edilib. XP toplandıqca level artır. Sikkə almaq və xərcləmək XP vermir. Təsdiqlənmiş qayda: alınmış şəhər genişlənmələri daimidir; təkrar ödəniş tələb edilmir. Təsdiqlənmiş V1 dekorasiyası: sadə ağac, skamya, işıq və yol elementləri sikkə ilə alınır. Bunlar sahə tutur, inventara qaytarıla bilir; gəlir, XP və bina qiyməti bonusu vermir. İnventara qaytarmaq sikkə geri ödənişi deyil.

Təsdiqlənmiş başlanğıc sahə: 8 standart düz binanın təhlükəsiz boşluqlarla yerləşəcəyi ölçü. Bu, sərt 8 bina limiti deyil; əyri binaların faktiki tutduğu sahə və yerləşdirilmiş dekorasiya tutuma təsir edir.

Təsdiqlənmiş qayda: ilk şəhər genişlənməsi level 3-də, yəni ümumilikdə 150 XP toplananda alış üçün açılır və 200 sikkəyə alınır. Açılma ərazini avtomatik vermir. İlk genişlənmə 4 standart düz bina üçün əlavə sahə verir; başlanğıc sahə ilə birlikdə ümumi ölçü 12 standart düz binaya uyğundur. Əyrilik, təhlükəsiz boşluqlar və dekorasiya yerləşdirmədə nəzərə alınır.

Təsdiqlənmiş qayda: ikinci şəhər genişlənməsi level 5-də, yəni ümumilikdə 500 XP toplananda alış üçün açılır və 400 sikkəyə alınır. 5 standart düz bina üçün əlavə sahə verir. Başlanğıc və ilk genişlənmə ilə birlikdə ümumi sahə 17 standart düz bina ölçüsünə çatır; faktiki tutum əyrilik və dekorasiyadan asılıdır.

Təsdiqlənmiş qayda: üçüncü şəhər genişlənməsi level 9-da, yəni ümumilikdə 1.800 XP toplananda alış üçün açılır, 900 sikkəyə alınır və 6 standart düz bina üçün əlavə sahə verir. Başlanğıc və ilk iki genişlənmə ilə birlikdə ümumi sahə 23 standart düz bina ölçüsünə çatır; faktiki tutum əyrilik və dekorasiyadan asılıdır.

Təsdiqlənmiş qayda: dördüncü şəhər genişlənməsi level 13-də, yəni ümumilikdə 3.900 XP toplananda alış üçün açılır, 1.500 sikkəyə alınır və 7 standart düz bina üçün əlavə sahə verir. Başlanğıc və ilk üç genişlənmə ilə birlikdə ümumi sahə 30 standart düz bina ölçüsünə çatır; faktiki tutum əyrilik və dekorasiyadan asılıdır.

Təsdiqlənmiş qayda: beşinci şəhər genişlənməsi level 18-də, yəni ümumilikdə 7.650 XP toplananda alış üçün açılır, 2.500 sikkəyə alınır və 8 standart düz bina üçün əlavə sahə verir. Bütün genişlənmələr alındıqda ümumi sahə 38 standart düz bina ölçüsünə çatır; faktiki tutum əyrilik və dekorasiyadan asılıdır.

Təsdiqlənmiş V1 limiti: başlanğıc sahədən əlavə cəmi 5 şəhər genişlənməsi olacaq. Beşinin də açılma leveli, ölçüsü və qiyməti müəyyən edilib. Bütün genişlənmələrin ümumi qiyməti 5.500 sikkədir.

### Level XP hədləri

Təsdiqlənmiş qayda: oyunçu level 1-də başlayır. Level L-dən L+1-ə keçmək üçün əlavə 50 × L XP tələb olunur. İlk keçidlər müvafiq olaraq 50, 100, 150 və 200 XP-dir. Level L-ə çatmaq üçün ümumi XP həddi 25 × L × (L−1) olur: level 2 üçün 50, level 3 üçün 150, level 4 üçün 300, level 5 üçün 500 XP. Artıq qalan XP növbəti keçidə sayılır; qazanılmış ümumi XP itmir.

### Sikkə və irəliləyiş

Bir əsas valyuta var: sikkə. Oyunçu onu oyun nəticəsindən, nailiyyətdən, bina satışından və real pulla sikkə paketindən əldə edir. Satış yalnız sikkə ilədir; real pula çıxarış nəzərdə tutulmur.

Təsdiqlənmiş əsas oyun mükafatı: hər uğurla yerləşdirilmiş mərtəbə 1 sikkə verir. Cəhd 10 mərtəbəyə çatmasa da mükafat qazanılır. Məsələn, 17 uğurlu mərtəbə 17 sikkə və 17 XP verir; nailiyyət və satış gəliri ayrıca hesablanır. Eyni uğurlu mərtəbənin mükafatı təkrar verilmir.

Təsdiqlənmiş nailiyyət qaydası: hər nailiyyət pilləsi XP və sikkə mükafatını hesab üzrə yalnız bir dəfə verir. Eyni şərtin təkrar yerinə yetirilməsi nailiyyət bonusunu yenidən vermir; adi mərtəbə mükafatları qüvvədə qalır. Hündürlük, dəqiq yerləşdirmə və xilasetmə kateqoriyalarının pillə və bonusları aşağıda müəyyən edilib.

Təsdiqlənmiş hündürlük nailiyyəti: bir cəhddə uğurla yerləşdirilmiş 10, 20, 35, 50, 75 və 100 mərtəbə üçün ayrı pillələr var. Birdəfəlik bonuslar: 10 mərtəbə üçün 10 XP və 10 sikkə; 20 mərtəbə üçün 20 XP və 20 sikkə; 35 mərtəbə üçün 35 XP və 35 sikkə; 50 mərtəbə üçün 50 XP və 50 sikkə; 75 mərtəbə üçün 75 XP və 75 sikkə; 100 mərtəbə üçün 100 XP və 100 sikkə. Bonuslar adi mərtəbə mükafatına əlavə verilir. Bütün hündürlük pillələrinin XP və sikkə bonusları təsdiqlənib. Yüksək hədlərin əlçatanlığı prototipdə yoxlanacaq. Təsdiqlənmiş qayda: reklamla davam edilmiş cəhdlər də hündürlük nailiyyətlərini aça bilir. Hər nailiyyət pilləsinin birdəfəlik mükafat qaydası qüvvədə qalır. Reklamdan sonra çatılan tam uğurlu mərtəbə sayı əsas leaderboard-da da sayılır.

Sikkə xərcləri: bazardan bina, fasad və hissələr, level şərti açılmış şəhər ərazisi. Dekorasiya təsdiqlənmiş əlavə sikkə xərcidir. Bina qurmağa giriş haqqı yoxdur.

Balans məqsədi: oyunçu seçim etməli, reklam və alışın sürətləndirmə dəyərini hiss etməlidir. Pulsuz oyunla da görünən irəliləyiş mümkün qalmalıdır. Büdcə çatışmazlığı əsas oyunu dayandırmamalıdır.

Balansın hazırlanma sırası:

1. Adi oyunçu üçün bir hissəyə və bir əraziyə çatma hədəfini cəhd sayı ilə seçmək.
2. Orta nəticəyə görə sikkə və XP mükafatını hesablamaq.
3. Nailiyyət bonuslarını və uzunmüddətli alış qiymətlərini yerləşdirmək.
4. Reklam bonusunun və sikkə paketinin qazandırdığı vaxtı müqayisə etmək.
5. Sikkə yaradılması, xərclənməsi, bazar dövriyyəsi və alışa çatma müddətinə əsasən düzəliş etmək.

Oyun/nailiyyət və alışlar yeni sikkə yaradır; oyunçulararası satış onu ötürür. Sistem mağazası, ərazi və təklif olunan satış komissiyası sikkəni dövriyyədən çıxarır. İlkin rəqəmlər §5-da seçilib; iqtisadiyyat sınağında yoxlanacaq.

Təsdiqlənmiş dəqiq yerləşdirmə nailiyyəti: bir cəhddə mərkəzə dəqiq yerləşdirilən mərtəbələrin sayı əsas götürülür; ardıcıl olması tələb edilmir. Aradakı qeyri-dəqiq enişlər sayğacı sıfırlamır. Yeni cəhddə sayğac yenidən başlayır. Təsdiqlənmiş pillələr: bir cəhddə 3, 5, 10, 20 və 30 dəqiq yerləşdirmə. Birdəfəlik bonuslar: 3 dəqiq yerləşdirmə üçün 5 XP və 5 sikkə; 5 dəqiq yerləşdirmə üçün 10 XP və 10 sikkə; 10 dəqiq yerləşdirmə üçün 20 XP və 20 sikkə; 20 dəqiq yerləşdirmə üçün 40 XP və 40 sikkə; 30 dəqiq yerləşdirmə üçün 60 XP və 60 sikkə. Bonuslar adi mərtəbə mükafatına əlavə verilir. Bütün dəqiq yerləşdirmə pillələrinin bonusları təsdiqlənib. Təsdiqlənmiş qayda: reklamla davam edəndə dəqiq yerləşdirmə sayğacı qorunur və sonrakı dəqiq enişlər onun üzərinə əlavə olunur. Davam eyni cəhd sayılır; sayğac yalnız yeni cəhd başlayanda sıfırlanır. Hər pillənin birdəfəlik XP və sikkə mükafatı qaydası qüvvədədir.

Təsdiqlənmiş xilasetmə nailiyyəti: oyunçu növbəti mərtəbəni düzgün yerləşdirib binanı təhlükəli əyilmədən sabit vəziyyətə qaytaranda xilasetmə sayılır. Reklamla binanın sabitləşdirilməsi xilasetmə sayılmır. Təsdiqlənmiş qayda: xilasetmələr hesab üzrə bütün oyunlar boyunca toplanır; sayğac yeni cəhd başlayanda sıfırlanmır. Təsdiqlənmiş pillələr: ümumilikdə 1, 5, 15, 30, 60 və 100 xilasetmə. Birdəfəlik bonuslar: ilk xilasetmə üçün 5 XP və 5 sikkə; 5 xilasetmə üçün 10 XP və 10 sikkə; 15 xilasetmə üçün 25 XP və 25 sikkə; 30 xilasetmə üçün 50 XP və 50 sikkə; 60 xilasetmə üçün 90 XP və 90 sikkə; 100 xilasetmə üçün 150 XP və 150 sikkə. Bonuslar adi mərtəbə mükafatına əlavə verilir. Son üç pillənin bonusları istifadəçinin verdiyi səlahiyyətlə ilkin balans kimi seçilib; prototipdə yoxlanacaq. Xilasetmə pillələrinin ümumi bonusu 330 XP və 330 sikkədir. Təsdiqlənmiş sayma qaydası: düzgün mərtəbə yerləşdirməsi binanı təhlükəli tarazlıqdan sabit tarazlığa qaytaranda bir xilasetmə sayılır. Ani yellənmə zamanı düz vəziyyətdən keçmək xilasetmə deyil. Hər təhlükə epizodu yalnız bir xilasetmə verir; növbəti xilasetmə üçün bina yenidən təhlükəli tarazlığa düşməlidir. Təhlükə və sabitliyin ədədi hədləri fizika prototipində tənzimlənəcək. Hər pillənin XP və sikkə bonusu hesab üzrə yalnız bir dəfə verilir.

### Bazar və qiymət limiti

Hər bina və hissə üçün sistem tərəfindən müəyyən edilən dəyər çəkisi saxlanılır. Qiymət hissənin pulsuz və ya pulla əldə olunmasından asılı təsadüfi mükafatla yox, onun təyin edilmiş çəkisi ilə hesablanır.

Baza dəyəri = tikinti dəyəri + sakin tutumu dəyəri + fasad və hissələrin dəyəri.

Maksimum satış qiyməti = baza dəyəri × (1 + icazəli marja).

İlkin sınaq balansı: maksimum marja 20%-dir; yekun maksimum qiymət aşağı tam sikkəyə yuvarlaqlaşdırılır. Mərtəbə və ondan yaranan sakin dəyəri birlikdə balanslanmalıdır ki, eyni böyüklük iki dəfə həddindən artıq qiymətləndirilməsin. Fasadı hesabda açmaq qiyməti ilə binaya əlavə etdiyi dəyər ayrı parametrlərdir.

Satıcı limit daxilində qiyməti seçir. Bazar hündürlük, görünüş və qiymət üzrə süzgəc, bina baxışı və elanın geri götürülməsini təmin edir. Dəyər təkcə elan qiymətlərinə əsaslanaraq özbaşına artmır. Limit satış zəmanəti vermir.

Təklif olunan V1 qaydaları:

- Təsdiqlənmiş V1 qaydası: oyunçu yalnız öz qurduğu və sahibi olduğu binanı sata bilər. Bazardan alınmış bina şəhərdə istifadə oluna bilər, lakin təkrar satıla bilməz.
- Təsdiqlənmiş qayda: satışa çıxarılan bina şəhərdə qalır; elanın dərc edilməsi onu şəhərdən çıxarmır. Bir bina eyni anda yalnız bir elanda ola bilər.
- Elan geri götürüləndə bina əvvəlki yerində qalır. Satış tamamlananda bina satıcının şəhərindən və ya inventarından çıxır, alıcıya keçir; satıcıda nüsxəsi qalmır. Təsdiqlənmiş qayda: elan aktivkən binanın şəhərdə yerini dəyişmək sərbəstdir, yerləşdirmə yoxlamaları qüvvədə qalır. Fasad və hissələri dəyişmək üçün əvvəl elan geri götürülməlidir; dəyişiklikdən sonra bina yenidən qiymətləndirilərək limit daxilində yeni elan verilə bilər.
- İlkin sınaq balansı: uğurlu satışdan 5% komissiya tutulur, yuxarı tam sikkəyə yuvarlaqlaşdırılır (minimum 1 sikkə). Satıcıya qiymət − komissiya çatır; elandan əvvəl xalis gəlir görünür. Satılmayan elan üçün haqq yoxdur.
- Təsdiqlənmiş limit: hesab üzrə eyni anda maksimum 5 aktiv satış elanı.
- Sikkə, bina və mükafat əməliyyatları təkrar icrada çoxalmır; iki alıcı eyni binanı ala bilmir.
- Limitsiz bina nüsxələmə, auksion və oyunçulararası sikkə hədiyyəsi V1-də yoxdur.

### Rekord və leaderboard

Əsas nəticə uğurla yerləşdirilmiş mərtəbələrin sayıdır. Lokal rekord saxlanılır. Alınmış bina şəxsi tikinti rekordunu artırmır.

Təsdiqlənmiş qayda: bütün zamanlar üzrə ümumi və həftəlik leaderboard birlikdə olacaq. Həftəlik sıfırlanma yalnız həftəlik sıralamaya təsir edir; ümumi sıralama, şəxsi rekord, şəhər, sikkə, XP və nailiyyət irəliləyişi qorunur. Onlayn nəticələr yoxlanılsın. Təsdiqlənmiş vaxt qaydası: hamı üçün həftə Asia/Baku vaxt zonasında bazar ertəsi 00:00-da başlayır; cəhdin nəticəsi AppBackend/PostgreSQL tərəfindən qəbul edildiyi həftəyə yazılır. Bərabər mərtəbə sayında nəticəsi server tərəfindən daha əvvəl qəbul edilən oyunçu yuxarıda olur. Bu qayda cihazdakı bitmə anını əvəz edir; həftə sərhədində gecikən nəticə yeni həftəyə düşür. Eyni qəbul vaxtında sabit server nəticə ID-si istifadə olunur (§2). Təsdiqlənmiş qayda: reward reklamla davam edilmiş cəhddə çatılan tam uğurlu mərtəbə sayı əsas leaderboard-da sayılır. Məsələn, 20 mərtəbədə uduzub reklamdan sonra 35 uğurlu mərtəbəyə çatan oyunçunun yarış nəticəsi 35-dir. Hər cəhddə maksimum bir dəfə davam qaydası qüvvədə qalır.

### Reklam və ödəniş

Gəlir modeli: könüllü reward reklam, nəticə fasiləsində məhdud adi reklam, sikkə paketləri və əlavə görünüşlər.

Reward ilə hər cəhddə maksimum bir dəfə davam etmək olar. Uğursuz mərtəbə silinir, bina təhlükəsiz vəziyyətə qaytarılır, oyun fasilədə açılır. Reklam mükafatı yalnız cihazdakı reklam SDK-sının “mükafat qazanıldı” bildirişindən sonra verilir; hər reklam cəhdinin ayrıca qeydi olur və eyni qeyd ikinci mükafat yaratmır. Bu, serverdə baxışın həqiqiliyinin təsdiqi deyil; qalan risk §2–3-də qəbul edilib.

Görünüşləri reklamla açmaq üçün tələb olunan baxış sayı əvvəlcədən göstərilir. Adi reklam tikinti zamanı çıxmır; həm oyun sayı, həm keçən vaxt şərtinə bağlanır. Təsdiqlənmiş qayda: ilk oyun sessiyasında adi avtomatik tam ekran reklam göstərilmir; reward reklamlar yalnız oyunçunun istəyi ilə əlçatandır. Təsdiqlənmiş sərhəd: ilk sessiya güzəşti ilk uğurlu onlayn giriş gününün sonunadək qüvvədədir; tətbiqi bağlayıb açmaq bu güzəşti bitirmir. Təsdiqlənmiş vaxt zonası Asia/Baku-dur: güzəşt ilk uğurlu onlayn giriş günündən sonrakı Bakı gecəyarısında bitir, 24 saatlıq müddət deyil. İlk açılış oflayndırsa, məşq reklamsız qalır və güzəşt günü hələ başlamır. Təsdiqlənmiş qayda: reward reklam tamamlananda adi reklam üçün vaxt və cəhd sayğacları sıfırlanır; növbəti adi reklam yalnız hər iki hədd yenidən dolduqdan sonra uyğun nəticə fasiləsində göstərilə bilər. Reward-dan dərhal sonra adi reklam göstərilmir. Təsdiqlənmiş interval: adi reklam üçün ən azı 4 bitmiş cəhd və 5 dəqiqə aktiv oyun vaxtı birlikdə tələb olunur; göstərilmə yalnız nəticə fasiləsindədir. Fasilə, arxa plan, menyu və reklam vaxtı aktiv oyun vaxtına daxil deyil. Adi reklam tamamlananda da hər iki sayğac sıfırlanır. Tətbiqi yenidən açmaq sayğacları sıfırlamır. Reklam yoxdursa yenidən başlama sərbəstdir.

Real alışın authority-si §2-dəki AppBackend verify/delivery modelidir; cihaz callback-i təkbaşına hüquq vermir. Ödənişli görünüşlər fiziki üstünlük vermir. Paketlər, qiymətlər və platforma inteqrasiyası ayrıca hazırlıq mərhələsində seçilir.

### Tam oyun axını

1. İlk açılış: qısa təqdimat və pulsuz başlanğıc üslubu seçimi. Təsdiqlənmiş qayda: oyunçu qonaq kimi dərhal oynaya və şəhərini qura bilər. Bazarda alış-satışdan və real pul alışından əvvəl hesab bağlama tələb olunur. Qonaq irəliləyişi bağlanan hesaba köçürülür. Təsdiqlənmiş konflikt qaydası: hesabda artıq irəliləyiş varsa, qonaq və hesab irəliləyişləri birləşdirilmir; oyunçu yalnız birini seçir. Seçimdən əvvəl hər ikisinin leveli, sikkəsi və bina sayı göstərilir; seçilməyən irəliləyişin aktiv oyuna köçürülməyəcəyi aydın bildirilir. Seçim yalnız şəhərə yox, bütün oyun irəliləyişinə aiddir. Təsdiqlənmiş bərpa qaydası: hesabına qayıdan oyunçunun daimi pullu görünüş hüquqları bərpa edilir; əvvəl çatdırılmış sikkə paketləri yenidən verilmir. Bu fərq qonaq/hesab irəliləyişi seçimində də qorunur. Texniki təsdiq və hüquqların hesaba bağlanması ayrıca həll edilməlidir.
2. İlk cəhd: “Mərtəbəni buraxmaq üçün toxun” təlimatı və bağışlayıcı ilk mərtəbələr.
3. Tikinti: buraxma, eniş, yellənmə, tarazlığı düzəltmə və artan çətinlik.
4. Minimum hündürlük: 10 uğurlu mərtəbədən sonra bina saxlanmağa uyğun olur; oyunçu tikintiyə davam edir.
5. Məğlubiyyət: aydın aşma/düşmə reaksiyası. Uyğun halda bir dəfə reklamla davam et və ya nəticəyə keç.
6. Nəticə: mərtəbə, rekord, sıralama uyğunluğu, sikkə/XP və nailiyyət irəliləyişi. Mükafat bir dəfə hesablanır.
7. Bina saxlamaq: uyğun bina inventara əlavə edilir. Bina yoxdursa rahat yeni cəhd imkanı qalır.
8. Şəhərə yerləşdirmək: bina önizləməsi, əyriliyə uyğun sahə yoxlaması və yerləşdirmə/dəyişdirmə.
9. İnkişaf: sakin göstəricisi, görünüşlər, dekorasiya və levelə uyğun ərazi alışları.
10. Satmaq: bina detalından baza dəyəri, maksimum limit, qiymət və təklif olunan komissiya görünür; uyğun hesabla elan dərc edilir.
11. Almaq: bazar baxışı, bina və sakin məlumatı, qiymət, təsdiq; bina inventara keçir.
12. Yenidən oynamaq: alınmış/açılmış hissələrdən görünüş seçmək və bir toxunuşla yeni cəhd.
13. Sonrakı giriş: şəhər, satış nəticəsi və nailiyyət irəliləyişi görünür. Cəza və məcburi gündəlik iştirak yoxdur.

İnternet kəsiləndə bazar əməliyyatı uğurlu kimi göstərilmir. Təsdiqlənmiş qayda: oflayn məşq sikkə, satıla bilən bina və leaderboard nəticəsi yaratmır. Təsdiqlənmiş qayda: aktiv oyun ekranında internet kəsiləndə oyun avtomatik fasiləyə düşür və mövcud cəhd qorunur; ekranı/tətbiqi tərk etmək isə §3 abandon siyasətinə tabedir. Bağlantı qayıdanda oyunçu “Davam et” ilə qaldığı yerdən oynayır; oyun özbaşına başlamır. İnternet bərpa olunmadan mükafat və nəticə təsdiqlənmir.

### Ekranlar

- Ana ekran: Oyna, şəhər, rekord, sikkə və level; bazar, kolleksiya və leaderboard-a giriş.
- Oyun: mərtəbə sayı, rekord, fasilə və ilk cəhddə qısa təlimat.
- Fasilə: Davam et, Yenidən başla, Menyuya qayıt; səs/vibrasiya ayrıca seçimlərdədir.
- Nəticə: nəticələr, mükafatlar, uyğun binanı saxlamaq və yenidən oynamaq.
- Şəhər: yerləşdirmə, dəyişdirmə, ərazi və sakin göstəricisi.
- İnventar və bina detalı: forma, hissələr, mərtəbə, sakin, dəyər və satış əməliyyatları.
- Kolleksiya/mağaza: hissələr və əldəetmə şərtləri; sikkə paketləri.
- Bazar: süzgəclər, bina baxışı, alış və şəxsi elanlar.
- Leaderboard, nailiyyətlər, hesab və parametrlər.

Bu siyahı ekranların funksional planıdır. Vizual dizayn başlayanda ui-craft, mərkəzi tokenlər, kontrast və render yoxlaması tətbiq ediləcək.

### İlk ictimai versiya

Təsdiqlənmiş V1 əhatəsi: bir sonsuz rejim, əsas fizika, kamera, səs/vibrasiya/fasilə, lokal rekord, sadə canlı şəhər, inventar, sakin tutumu, bina dəyişdirmə, level və ərazi genişlənməsi, ilk gündən seçilən 6 pulsuz üslub, məhdud əlavə hissələr və sadə dekorasiya/yol elementləri, nailiyyət/sikkə sistemi, sadə bazar, qiymət limiti, leaderboard, reklam və alışlar. İstifadəçi bütün bu sistemlərin ilk ictimai versiyada olmasını təsdiqləyib; onların daxilində təklif kimi göstərilən qaydalar ayrıca dəqiqləşdiriləcək.

Bu, ilkin reklamsız prototipdən daha böyük ictimai buraxılışdır. Bazar, hesab, ödəniş və server funksiyaları əsas mexanika yoxlanandan sonra hazırlanır. Kiçik qapalı sınaq bütün V1 funksiyaları tamamlanmadan keçirilə bilər.

V1 xaricində: canlı çoxoyunçulu oyun, çat, real pul çıxarışı, auksion, avtomatik sakin iqtisadiyyatı, mürəkkəb xidmət/şəhər simulyasiyası, limitsiz bina nüsxələmə və mövsüm sistemləri.

## 5. İqtisadiyyat və ilkin balans

### Balans qərarları

İstifadəçinin göstərişinə əsasən qalan XP və sikkə bonuslarını agent mövcud iqtisadiyyata uyğun ilkin balanslaşdırır; hər məbləğ üçün ayrıca təsdiq istənilmir. Əvvəldən təsdiqlənmiş rəqəmlər saxlanılır. Yeni rəqəmlər sınaq balansı kimi qeyd olunur; əsas oyun qaydaları istifadəçi ilə ayrıca müzakirə edilir.


### İlkin balans

3 noyabr 2026: istifadəçinin verdiyi səlahiyyətlə aşağıdakı rəqəmlər ilkin sınaq balansı kimi seçilib. Bunlar ölçülmüş oyun məlumatı deyil. Əvvəl təsdiqlənmiş mərtəbə mükafatları, nailiyyətlər, level hədləri və beş genişlənmə dəyişmir. Yeni əsas oyun mexanikası və əlavə XP mənbəyi yaradılmır.

### XP və adi irəliləyiş

- Hər uğurlu mərtəbə: 1 XP və 1 sikkə. Alış, satış, reklam baxışı və bina silmək ayrıca XP vermir. Reward ilə davamdan sonra yerləşdirilən yeni mərtəbələr adi mükafatını verir.
- Hündürlük pillələrinin cəmi 290 XP + 290 sikkə, dəqiq eniş pillələrinin cəmi 135 XP + 135 sikkə, xilasetmə pillələrinin cəmi 330 XP + 330 sikkədir. Mövcud bütün nailiyyətlərin birdəfəlik ümumi bonusu 755 XP + 755 sikkədir.
- Hesablama fərziyyəsi: orta cəhd 15 uğurlu mərtəbə. Bonuslar və xərclər nəzərə alınmadan 100 sikkə təxminən 7, 200 sikkə 14, 400 sikkə 27 cəhddir. Bunlar çətinlik və müddət zəmanəti deyil.
- Bütün nailiyyətlər açılıbsa level 18 üçün 7.650 ümumi XP-nin ən azı 6.895-i mərtəbələrdən gəlir. Eyni ümumi 7.650 sikkə gəlirindən bütün ərazilərə 5.500 xərcləndikdə 2.150 qalır; mağaza xərcləri bu ehtiyatla rəqabət aparır. Ərazi açılması avtomatik alış demək deyil.

### Hissə və görünüş qiymətləri

- Təsdiqlənmiş 6 başlanğıc üslubu ilk gündən pulsuz seçilir, bina dəyərinə əlavə çəkisi 0-dır. Konkret üslubların təsdiqlənmiş məzmunu §6-dədir.
- Sikkə ilə daimi açılan hissələr üçün üç qiymət pilləsi: 100, 250 və 600 sikkə. Orta fərziyyə ilə təxminən 7, 17 və 40 cəhd; nailiyyətlər bunu qısaldır. Hər kateqoriyanın təsdiqlənmiş hissələri §6-dədir.
- Reklamla daimi açılan hissələr üçün ilkin tələb: sadə 3, orta 5, xüsusi 8 tamamlanmış reward baxışı. Qismən irəliləyiş qorunur. Birbaşa sikkə verən əlavə reward təklif edilmir; mövcud reward məqsədləri davam və hissə açmaqdır.
- Bina dəyərinə əlavə çəki əldəetmə yolundan asılı deyil: pulsuz baza hissəsi 0, əlavə sadə hissə 4, orta 10, xüsusi 24 sikkə. Beş kateqoriyanın hər birindən yalnız binada faktiki istifadə edilən bir hissə sayılır; eyni hissə hər mərtəbədə təkrar qiymətləndirilmir. Maksimum kosmetik əlavə 120 sikkədir. Nailiyyət/reklam/real pulla açılan hissə də eyni vizual pillənin çəkisini alır.
- Təsdiqlənmiş sadə dekorasiya/yol əhatəsi üçün ilkin qiymət pillələri: kiçik 25, orta 75, böyük 150 sikkə. Konkret elementlərin təsdiqlənmiş pillə bölgüsü §6-dədir. Alış və yerləşdirmə XP, gəlir və bina qiyməti bonusu vermir.

### Bina qiyməti və bazar nümunələri

Uğurlu mərtəbə sayı F, sakin sayı R = 4F, istifadə edilən hissələrin ümumi çəkisi H olsun. Tikinti dəyəri F, sakin dəyəri R / 4 seçilir: baza dəyəri B = F + R / 4 + H = 2F + H. Sakin və hündürlük birlikdə hər mərtəbəyə cəmi 2 sikkə verir; əlavə gizli ölçü əmsalı yoxdur. Dəqiq eniş və əyrilik V1-də əlavə qiymət bonusu yaratmır.

Satıcı tam sikkə ilə 1-dən floor(1,20 × B)-yə qədər qiymət seçir. Satışdan ceil(0,05 × qiymət), minimum 1 sikkə komissiya çıxılır. Bu məbləğlər yalnız uğurlu oyunçulararası satışda ötürülür; sistem binanı avtomatik almır.

- 10 mərtəbə, 40 sakin, pulsuz hissələr: baza 20, maksimum qiymət 24, komissiya 2, satıcıya 22 sikkə.
- 20 mərtəbə, 80 sakin, bir orta hissə: baza 50, maksimum qiymət 60, komissiya 3, satıcıya 57 sikkə.
- 35 mərtəbə, 140 sakin, beş xüsusi hissə: baza 190, maksimum qiymət 228, komissiya 12, satıcıya 216 sikkə.

Hissəni hesabda bir dəfə açmaq gələcək binalarda təkrar istifadəyə imkan verir; satış gəliri alıcının mövcud sikkəsindən gəlir, yeni sikkə yaratmır. Alınmış binanı yenidən satmaq qadağası qüvvədədir.

### Sikkə paketləri və yoxlama

İlkin paket məzmunları: 500, 1.200 və 2.500 sikkə; XP, fiziki üstünlük və level şərtini keçmək hüququ vermir. Orta 15 mərtəbə fərziyyəsi ilə bunlar təxminən 34, 80 və 167 cəhdin əsas sikkə gəliridir. Real pul qiyməti, qiymətə görə paket sərfəliliyi və alışların təsdiqi platforma mərhələsində həll ediləcək; bu plan gəlir proqnozu vermir.

Qapalı sınaqda ilk 100 sikkəlik hissəyə çatma, ilk əraziyə çatma/alış, ümumi sikkə mənbə və xərcləri, bazarda real satış payı, reklamla hissə açma və paketlərin qənaət etdiyi oyun vaxtı ölçüləcək. Əvvəl yeni qiymətlər və çəkilər tənzimlənir; əvvəldən təsdiqlənmiş rəqəmlər səssiz dəyişdirilmir. AppBackend atomikliyi oyun/reward callback-inin həqiqiliyini sübut etmir; real SKU/Console/deploy ayrıca göstərişdə qalır.

## 6. Təsdiqlənmiş V1 kataloqu

### Say və istifadə qaydası

- 6 başlanğıc üslubu × 5 kateqoriya = 30 pulsuz baza hissəsi.
- Əlavə 38 hissə: 15 sikkəlik, 15 reklamlıq, 5 birbaşa real pulla və 3 yalnız nailiyyətlə açılan hissə. Ümumi bina hissəsi sayı 68-dir.
- 12 dekorasiya/yol elementi: ağac, skamya, işıq və yol kateqoriyalarının hərəsində 3 seçim.
- Üslub ayrıca bonuslu əşya deyil, beş baza hissəsinin hazır birləşməsidir. Bütün 30 baza hissəsi ilk gündən açıqdır və kateqoriyalar üzrə qarışdırıla bilər.
- Hər hissə yalnız göstərilən yolla açılır. Sikkə/reklam/real pul seçimləri fərqli hissələrdir; nailiyyət hissələrinin alternativ əldəetmə yolu yoxdur.
- Açılan bina hissəsi hesabda daimi və təkrar istifadəlidir. Dekorasiyada qiymət bir yerləşdirilə bilən nüsxə üçündür; əlavə nüsxə yenidən alınır. İnventara qaytarılan nüsxə yenidən yerləşdirilir, sikkə geri verilmir. Bu nüsxə modeli təsdiqlənib.
- Bütün bina hissələri kosmetikdir: fizika, mərtəbə ölçüsü, tarazlıq, sakin sayı və XP qaydasını dəyişmir. Dəyər çəkisi yalnız mövcud bina qiyməti hesabına daxildir. Dekorasiya bonus vermir.

### Altı pulsuz başlanğıc üslubu

Hər sətirdə sıra belədir: divar/material; pəncərə; balkon/giriş; dam; işıqlandırma. Hər ad bir ayrıca hissədir, qiyməti və bina dəyərinə çəkisi 0-dır.

1. **İçərişəhər:** Əhəngdaşı divar; Tağlı pəncərə; Daş giriş; Düz parapet dam; Giriş fənəri.
2. **Kərpic məhəllə:** Kərpic hörgü; Çərçivəli pəncərə; Dəmir balkon; Maili kirəmit dam; Divar fənəri.
3. **Sahil:** Suvaq fasad; Geniş pəncərə; Açıq eyvan; Yastı terras dam; Eyvan işığı.
4. **Müasir şəhər:** Panel fasad; Lent pəncərə; Şüşəli giriş; Texniki düz dam; Giriş işıq zolağı.
5. **Bağ evi:** Taxta üzlük; Bölməli pəncərə; Taxta veranda; İkiyamaclı dam; Veranda fənəri.
6. **Emalatxana:** Beton fasad; Metal çərçivəli pəncərə; Metal giriş; Metal örtüklü dam; Sənaye divar lampası.

Balkon, veranda, giriş və dam formaları dekorativdir; əlavə oynanılan sahə və sakin yaratmır. İşıqlandırma seçimi yalnız görünüşdür.

### Sikkə ilə açılan 15 əlavə hissə

Hər kateqoriyada 3 hissə var. Sıra və qiymət/çəki hər sətirdə eynidir: **sadə 100 sikkə / +4; orta 250 sikkə / +10; xüsusi 600 sikkə / +24**. Hər hissə ayrıca alınır; level şərti yoxdur.

- Divar/material: İncə suvaq; Naxışlı kərpic; Mozaika üzlük.
- Pəncərə: İkiqanadlı pəncərə; Künc pəncərə; Vitraj pəncərə.
- Balkon/giriş: Sadə məhəccərli balkon; Oyma giriş; Sütunlu portal.
- Dam: Saçaqlı dam; Mansard dam; Pilləli tac dam.
- İşıqlandırma: Qoşa giriş lampası; Karniz işığı; Naxışlı fasad işığı.

Bu bölmədəki bütün hissələrin daimi açılma xərcinin cəmi 4.750 sikkədir. Bu, məcburi inkişaf xərci deyil; hamısının alınması ərazi genişlənmələri ilə eyni sikkə büdcəsinə rəqabət yaradır.

### Reward reklamla açılan 15 əlavə hissə

Hər kateqoriyada 3 hissə var. Sıra və tələb/çəki hər sətirdə eynidir: **sadə 3 baxış / +4; orta 5 baxış / +10; xüsusi 8 baxış / +24**. Baxışlar seçilmiş hissənin irəliləyişinə yazılır; bir baxış bir hissəyə sayılır. Bu bölgü təsdiqlənib. İrəliləyiş qorunur; açıldıqdan sonra təkrar reklam tələb olunmur.

- Divar/material: Zolaqlı suvaq; Çınqıl üzlük; Həndəsi relyef.
- Pəncərə: Jalüzlü pəncərə; Tağlı qoşa pəncərə; Dairəvi panorama.
- Balkon/giriş: Çiçəkli eyvan; Torlu giriş; Qəfəs naxışlı balkon.
- Dam: Kənar haşiyəli dam; Şüşəli dam örtüyü; Dalğalı dam örtüyü.
- İşıqlandırma: Qapıüstü lampa; Şaquli işıq zolağı; Ulduz naxışlı işıq.

Hamısını açmaq üçün cəmi 80 tamamlanmış reward baxışı tələb olunur; davam reklamı bu hissələri açmağa sayılmır. Hissə açma reward-u əlavə sikkə və XP vermir. Reklam təqdim edilməzsə irəliləyiş itmir və açılma təmin edilmiş sayılmır. Reward qeydi və ilk gün güzəşti §2–3-yə tabedir; könüllü reward ilk gün də əlçatandır.

### Birbaşa real pulla açılan 5 daimi hissə

Hər kateqoriyaya bir ayrıca məhsul təsdiqlənib. Hamısı xüsusi pillədədir, bina dəyərinə çəkisi +24-dür. Paket almaq şərti yoxdur; real pul qiymətləri və platforma məhsul identifikatorları hələ seçilmir.

- Divar/material: Mirvari keramika.
- Pəncərə: Kristal çərçivə.
- Balkon/giriş: Filigran portal.
- Dam: Şüşə tac.
- İşıqlandırma: İşıq şəlaləsi.

Bunlar nailiyyət hissələrinin surəti və ya bacarıq nişanı deyil. Fiziki üstünlük və ayrıca XP/sikkə vermir. Hesaba giriş, tamamlanmış alış, token qeydi və daimi hüququn bərpası §2–3-yə tabedir; real məhsul/Console inteqrasiyası ayrıca göstəriş gözləyir.

### Yalnız nailiyyətlə açılan 3 xüsusi hissə

Hər biri xüsusi pillədir və istifadə edildiyi binaya +24 çəki verir. Açılma hesab üzrə birdəfəlik və daimidir; sikkə, reklam və real pulla əldə edilmir.

- **Zirvə tacı — dam:** bir cəhddə 50 uğurlu mərtəbə. Mövcud 50 XP + 50 sikkə pillə bonusuna əlavə olaraq hissə açılır.
- **Dəqiqlik çərçivəsi — pəncərə:** bir cəhddə 20 dəqiq yerləşdirmə; ardıcıllıq tələb olunmur. Mövcud 40 XP + 40 sikkə bonusu saxlanılır.
- **Tarazlıq nişanlı giriş — balkon/giriş:** hesab üzrə 30 xilasetmə. Mövcud 50 XP + 50 sikkə bonusu saxlanılır. Reklamla sabitləşdirmə xilasetmə sayılmır.

Reklamla davam eyni cəhddir: hündürlük və dəqiq eniş sayğacları qorunur. Yeni nailiyyət kateqoriyası, yeni XP mənbəyi və əlavə sikkə bonusu yaradılmır. Bu hissələrin görünüşü yalnız bacarıq açılmasını ifadə edir, tarazlığı yaxşılaşdırmır.

### Sikkə ilə alınan 12 dekorasiya və yol elementi

Hər sətirdə 3 ayrıca seçim var. Sıra və bir nüsxənin qiyməti eynidir: **kiçik 25; orta 75; böyük 150 sikkə**. Bunlar §5-un qiymət pillələridir. Dəqiq footprint ölçüləri §7-dədir.

- Ağac: Tək ağac; Ağac cütü; Kiçik ağaclıq.
- Skamya: Tək skamya; Qarşılıqlı skamyalar; Skamyalı istirahət guşəsi.
- İşıq: Tək küçə fənəri; Qoşa küçə fənəri; Fənər sırası.
- Yol: Qısa düz yol; Dönüş yol hissəsi; Uzun düz yol.

Bir dənə hər seçimdən almaq 1.000 sikkədir. Sahə tuturlar, inventara qaytarılırlar; XP, passiv gəlir və bina dəyəri bonusu vermirlər. Yol görünüş və yerləşdirmə üçündür; yeni nəqliyyat, xidmət və ya yol bağlantısı şərti yaradılmır. Bina işıqlandırma hissəsi ilə şəhər fənəri ayrı məzmundur.

### Kataloqun sərhədi və yoxlama

- Bina kateqoriyaları üzrə say: divar 13, pəncərə 14, balkon/giriş 14, dam 14, işıqlandırma 13; cəmi 68. Nailiyyət hissələri bu saylara daxildir.
- Eyni binada hər kateqoriyadan yalnız istifadə olunan bir hissənin çəkisi hesablanır. Baza hissəsi 0, sadə +4, orta +10, xüsusi +24; bütün bina üçün maksimum +120. Hər mərtəbədə təkrar hesablanmır.
- Altı baza üslubu ayrıca satınalma deyil. Hazır bina almaq onun hissələrini hesabda açmır. Öz və alınmış binada yalnız hesabda açıq hissələrlə redaktə qaydası, aktiv elanın əvvəl geri götürülməsi və alınmış binanın təkrar satılmaması qüvvədədir.
- Mövcud 500, 1.200 və 2.500 sikkə paketləri saxlanılır; 68 hissəyə və 12 dekorasiyaya daxil deyil. Real pul qiymətləri açıqdır. Ərazilər və nailiyyətlərin digər pillələri bu kataloqda yenidən qurulmur.
- Sənəd səviyyəsində saylar, məbləğlər, xüsusi açılma şərtləri və +120 limiti tutuşdurulub. Oyun balansı ölçülməyib; qrafik uyğunluq, kontrast, animasiya və şəhərdə faktiki tutum sınaqdan keçirilməyib.
- İstifadəçi kataloqu bu formada təsdiqləyib; adlar, saylar və əldəetmə bölgüsü qəbul edilib. Bu təsdiq implementasiya və ya ölçülmüş balans təminatı deyil. Kotlin + Compose seçilib; real pul qiymətləri/SKU və tamamlanmamış mobil inteqrasiya ayrıca işdir.

## 7. Şəhər həndəsəsi və yerləşdirmə

1. **Əhatə.** Siyasət yalnız şəhərdə yerləşdirməyə (placement) və şəhər UI-da yerləşdirilmiş binaya
   baxışa aiddir. Canlı oyun (oyun içi) görünüşünə aid deyil: istifadəçinin düzəlişinə görə oyun
   içindəki hazırkı görünüş qaydasındadır və olduğu kimi saxlanılır. Bu siyasətə görə canlı oyun
   vizualları dəyişdirilmir.
2. **Formaya sadiqlik.** Oyunçu oyunda qüllə tikdikdən sonra yerləşdirmə yerindəki (və baxışdakı) bina
   onun real tikdiyi əyriliyə, meylə və mərtəbə ofsetləri yığınına çox böyük ölçüdə bənzəməlidir.
   Formanın mənbəyi oyun həndəsəsidir (mərtəbə ofsetləri, en, istiqamət); bu forma şəhərdə
   yerləşdirməyə və baxışa ötürülür, itirilmir və düz qutuya “düzəldilmir”.
3. **Təqdimat.** Yerləşdirmədə və baxışda bina oyun içindəki sadə bloklu görünüş kimi deyil, düzgün
   dizayn edilmiş UI binası kimi görünməlidir: möhkəm, həcmli, dolğun həcmli. Pəncərələr, dam, divarlar
   və bu kimi bitirmə elementləri bu şəhər UI təqdimatına aiddir.
4. **Geyindirmə.** Hissələr, fasad/dam örtükləri və digər bəzəklər binanın real formasının üzərinə
   oturmalıdır — maketə paltar geyindirmək kimi. Onlar real silueti və səthləri (meyllər və ofsetlər
   daxil) bürüyür; qutunun üstünə atılmış düz düzbucaqlı “dəri” olmamalıdır.
5. **Vizual niyyət və dizayn mənbələri.** İstifadəçi (9 noyabr, 23:04) çatda göstərilən izah
   maketlərinin istədiyi vizual niyyəti tam ifadə etdiyini təsdiqləyib. Həmin maketlər yalnız anlamaq
   üçün illüstrasiyadır: rəsmi təsdiqlənmiş dizayn mənbəyi deyil, `design/`-ə əlavə edilmir və
   `city-plan.png`/`city-view.png`-ni əvəz etmir. Təsdiqlənmiş şəhər vizual referansları
   `design/city-plan.png` və `design/city-view.png` olaraq qalır. İmplementasiya son təsdiqlənmiş dizaynları və uyğun raster keyfiyyətini tələb edir. Eyni dizaynın 3–5 ölçü variantı istənilmir.
6. **Texnologiya.** 3D mühərrik hələ təsdiqlənməyib; kamera döndürmənin texniki realizasiyası hələ
   yoxlanmayıb (mövcud qeydlər qüvvədədir). Burada yalnız vizual tələb qeyd olunur; mühərrik seçilmir.

### Typed geometry və provenance

`city-geometry-v1`: bir engine vahidi 1.000 integer tick-dir; standart mərtəbə eni/dərinliyi 100.000/100.000 tick, obyektlər arasında minimum boşluq 10.000 tick-dir. En mövcud engine 100 vahidinə uyğundur; dərinlik və boşluq icazə verilmiş ilkin oyun parametrləridir, ölçülmüş balans deyil. Koordinatlar ±1.000.000.000 tick daxilindədir; bu texniki sərhəddir, torpaq hüququ deyil.

Saxlanmış mərtəbələr eyni dərinlikdədir, x ofsetləri qorunur; ardıcıl mərtəbələr en boyunca müsbət üst-üstə düşməlidir. Hamısının yerə proyeksiyasının dəqiq union-u bu modeldə düzbucaqlıdır. Origin təməl mərkəzidir, əyri binanı mərkəzləşdirmək onun ofsetini itirməməlidir. Dinamik sway/failed/falling mərtəbə footprint-ə daxil deyil. Dönmə təməl ətrafında saat əqrəbinin əksinə 0/90/180/270°; sonra integer x/y köçürmədir. Half-open sərhədlər toxunanda overlap yoxdur; boşluq bir dəfə pairwise, axis-aligned/Chebyshev məsafəsi ilə tətbiq edilir, məhz həddə bərabər boşluq keçir. Torpağın kənarında əlavə clearance nəzərdə tutulmur; footprint tam sahib olunan union daxilində olmalıdır. Torpaq düzbucaqlıların union-udur; bounding box və yalnız künclərin yoxlanması kifayət deyil.

Checkpoint ayrıca versioned `geometry.centers_x_ticks` göndərir: yalnız sabit uğurlu mərtəbələrin təməl origin-inə görə x ofsetləri. Double `x*1000` məhsulu ən yaxın integer-ə, yarım qiymət sıfırdan uzağa yuvarlaqlaşdırılır; signed zero → 0. Maksimum xəta məhsula görə 0,0005 engine vahididir. Recenter, clamp və canlı fizikanı tick-ə dəyişmə yoxdur. Davamlı və kvantlaşdırılmış qonşu dəstək müsbət olmalıdır; yuvarlaqlaşdırmada dəstək itərsə sorğu rədd edilir. Server snapshot versiya/rules, settled phase, null falling, sayğaclar, baseY=index×30 və x→tick uyğunluğunu, koordinat sərhədini və əvvəl qəbul edilmiş integer prefix-in dəyişməzliyini yoxlayır. Bu struktur qəbuludur, anti-cheat/replay sübutu deyil.

Server qəbul edilmiş geometry-ni attempt/sequence/checkpoint operation/rules/engine provenance-i ilə eyni checkpoint tranzaksiyasında saxlayır; operation ilkin qəbzin accepted_at vaxtına bağlanır, app/UID scope-u containing row-dan gəlir. Finish geometry override qəbul etmir, yalnız saxlanmış məlumatı binaya atomik köçürür. Attempt recovery və inventory/building/listing detail additive provenance qaytarır. Android bərpada provenance və snapshot-dan hesablanan tick-ləri tutuşdurur; köhnə journal envelope-ları dəyişmir. Dəqiq API: AppBackend/docs/wobbrik-game-api.md, “Typed floor geometry and provenance”. Migration 011 yalnız opt-in/provenance saxlanmasını əlavə edir; wallet dəyişmir.

### Torpaq və dekor ölçüləri

1 vahid = 1000 tick; sahə modulu 110×110 vahiddir, məcburi grid/snap deyil. Başlanğıc torpaq 440×220 düzbucaqlıdır. `land_1`: 220×220; `land_2`: 330×220 bounding box-dan yuxarı sağ 110×110 çıxılmış L; `land_3`: 330×220; `land_4`: 440×220 bounding box-dan yuxarı sağ 110×110 çıxılmış L; `land_5`: 440×220. Düzbucaqlı olmayanlar istisna, qalanları düzbucaqlıdır. Sahələr 8/+4/+5/+6/+7/+8 düz bina ekvivalentidir, sərt say limiti deyil. Origin aşağı sol, x sağa/y yuxarı; başlanğıc world origin-dədir. Genişlənmənin yerini oyunçu seçir, quarter-turn dönmə mümkündür. Torpaqlar sahə üzrə üst-üstə düşmür, müsbət uzunluqlu ortaq sərhəd tələb olunur; künc təması kifayət deyil. Əlavə minimum sərhəd uzunluğu yoxdur (tick şkalasında minimum müsbət seqment 1 tick). Müstəqil XP/qiymət hüquqları dəyişmir; alış hüququ və parcel-in yerləşdirilməsi ayrı anlayışlardır.

Dekor ölçüləri (vahid): Tək ağac 30×30; Ağac cütü 70×30; Kiçik ağaclıq 80×80; Tək skamya 40×20; Qarşılıqlı skamyalar 50×60; Skamyalı istirahət guşəsi 90×80; Tək küçə fənəri 10×10; Qoşa küçə fənəri 40×10; Fənər sırası 100×10; Qısa düz yol 100×30; Dönüş yol hissəsi 100×100 bounding box daxilində eni 30 olan L; Uzun düz yol 210×30. L-yol konturu aşağı-sol origin ilə `(0,0),(100,0),(100,30),(30,30),(30,100),(0,100)`; digər dekorlar tam düzbucaqlıdır. Dekor origin-i bounding box mərkəzidir; L-yol üçün bütün koordinatlardan 50 çıxılır. Qruplar bölünməyən tək nüsxədir. Qiymətlər kateqoriya üzrə 25/75/150 qalır.

10 vahid pairwise boşluq bütün dekorlara, o cümlədən yollara tətbiq edilir: yol hissələri bitişmir. Fasiləsiz yol istisnası təsdiqlənməyib. L-yolun boş girintisi tutulmuş sahə deyil; real union istifadə edilir. Hazır artwork uyğunluğu/balans cihazda yoxlanmayıb. Backend placement main-dədir; mobil renderer/UI ayrıca açıq işdir. Bu təsdiq main merge və UI implementasiyası üçün ayrıca tələb olunan OK-ları əvəz etmir.

### Atomik placement

AppBackend main migration 012 və `POST /v1/apps/wobbrik/game/city/placement` əlavə edir. Mövcud `shop/buy` torpaq hüququnu/dekor nüsxəsini ayrıca alır; yerləşdirmə pulsuzdur, wallet/refund yaratmır. Bir əmrdə 1–100 unikal `{kind,id,building_revision?,position}` dəyişiklik, `geometry_version:city-geometry-v1` və mövcud operation/revision/rules envelope istifadə olunur. `kind` land/building/decoration; dekor ID-si alınmış copy ID-dir. `position:{x_ticks:"440000",y_ticks:"0",quarter_turns:0}` place/move, açıq `position:null` return deməkdir; return+place eyni batch-də atomik replacement-dir. Bina üçün cari müsbət content revision tələb olunur.

Koordinatlar canonical decimal string tick, ±1000000000; quarter_turns məcburi 0–3. Snap/rounding yoxdur. Son vəziyyət bütöv yoxlanır: parcel overlap yoxdur, bütün parcel-lər base-ə müsbət sərhədlərlə bağlıdır, obyektlər dəqiq land union daxilindədir və ayrı obyektlər arasında 10000 tick boşluq var. Parcel move/return yalnız qalan obyekt və əlaqə qaydalarını qoruyanda keçir; base (0,0,0) dəyişmir. Hüquq/nüsxə return-dən sonra qalır. Legacy bina placed edilmir; həndəsəsiz köhnə membership qaytarılana qədər layout əmri rədd edilir.

`GET /city` backend-də `placement_enabled:true`, `geometry_version`, base daxil placed `parcels` və bina `position?` verir; `/decorations` nüsxə `position?` əlavə edir. `/sync.land` alınmış hüquqlar olaraq qalır. `/catalog.city_geometry` altı torpaq/on iki dekorun dəqiq local rectangle union-larını decimal string tick-lərlə verir. Köhnə client-lər additive sahələri ignore edə bilər; billing JSON/rules-v1/content revision dəyişmir. Əvvəl backend+migration, sonra ayrıca mobil placement client tələb olunur. Dəqiq schema/error/retry qaydaları AppBackend `docs/wobbrik-game-api.md`-nin son bölməsindədir. Mobil UI təsdiqi və deploy ayrıca göstəriş tələb edir.

## 8. İcra və yoxlama qaydası

- Koddan əvvəl bu plan və qısa handoff oxunur. Hər vahid uyğun feat/fix/chore/docs branch → kiçik commit → lokal diff review → tarixçə saxlanılaraq təsdiqli main merge axını ilə hazırlanır. Remote push/deploy/Console/real SKU ayrıca göstərişlədir. CI yalnız ayrıca tələb ediləndə və yalnız main-də lokaldır; GitHub Actions yaradılmır.
- Domain/API dəyişəndə account/app/epoch izolyasiyası, revision/dedup, exact retry, köhnə client və DB tranzaksiya uyğunluğu fokuslu yoxlanır. Backend üçün disposable real lokal PostgreSQL race/integration, vet və build istifadə olunur; production bazası test edilmir.
- UI-dan əvvəl `ui-craft`, mövcud token və konkret dizayn təsdiqi tətbiq edilir. Əsas mənbələr `design/mobile-v1/`, `tablet-v1/`, `game-tap-v2/`, `google-entry-v1/`, `finish-cosmetics-v1/`, `city-plan.png`, `city-view.png` və ekranın son təsdiqli versiyasıdır. Pending city proposal təsdiqli dizaynı əvəz etmir. Hazır asset sxematik alternativlə əvəz olunmur.
- Ölçü en+hündürlük sinfinə görə tokenlə seçilir; telefon landşaftı avtomatik tablet deyil. Əsas əməliyyat kəsilmir/yalnız scroll-a atılmır, söz qırılmır; zəruri scroll, inset/keyboard/a11y qorunur. Vizual işləyir iddiası üçün renderə baxılmalıdır; render yoxdursa məhdudiyyət açıq yazılır. Canlı/device yoxlama tələb edilmir və emulator avtomatik açılmır.
- Yeni stack, təsdiqlənməmiş dil siyahısı, ikinci plan, ikinci wallet/journal və icazəsiz redesign yaradılmır. Nəticə və yoxlama məhdudiyyətləri `reviews/`-da qeyd olunur; review tarixçədir, aktiv roadmap deyil.

## 9. Qalan qərarlar və dəqiq növbəti addım

- **Mərhələ 4-dən əvvəl dəqiq növbəti vahid:** §3-dəki son kosmetik qərar: oyun sonu avtomatik finish → home; oyun xaricində istənilən vaxt dəyişdirilən hesab üzrə standart görünüş. Mövcud təsdiqli kosmetik ekran əsas götürülür, redesign edilmir. Default seçimin oyun qrafikasına həqiqətən tətbiqi yoxlanmalıdır; yalnız finish metadata-sında saxlamaq bu qərarı tamamlamır.

- **13 noyabr cari icra sırası:** əvvəl Android; AppBackend dəyişiklikləri ayrıca saxlanılır. Mərhələ 4-dən əvvəl hər enişdə save/reload xətası ayrıca fix branch-də düzəldilir. Mövcud vizuallar saxlanılır; əlavə vizual variantlar hələlik iki ilə məhdudlaşdırılır, hansı asset-lərin seçiləcəyi uyğun vahiddə dəqiqləşdirilir; bu, §6 kataloqunun silinməsi deyil.

- Mərhələ 4 ilk vahidi üçün inventory/detail/loading dizaynlarının konkret təsdiqi; real həndəsəni qoruyan preview və raster keyfiyyəti. 68 hissə və mövcud ölçülər istifadə olunur; əlavə land-contour/crop dizayn dəsti tələb olunmur.
- Reward davam fizikasının stabilləşdirilməsi və client callback riskinin texniki sərhədi; SSV hazır deyil və avtomatik yeni tələb seçilmir.
- Refund/xərclənmiş sikkə, deletion/retention və transfer kimi təsdiqlənməmiş siyasətlər; mövcud texniki endpoint hazır siyasət demək deyil. Production legacy miqrasiya sualı yenidən açılmır.
- Real SKU, pul qiymətləri, Console və buraxılış/deploy ayrıca istifadəçi göstərişi gözləyir. Saxta uyğun checkpoint və reward callback riski gizlədilmir, anti-cheat hazır göstərilmir.

Növbəti məhsul vahidi yalnız istifadəçi istəyəndə: §1 mərhələ 4-dəki accepted finish → inventory → bina detalı oxu axını üçün mövcud dizaynın təsdiqlənməmiş hissələrini təqdim et, sonra mobil/backend müqaviləsini yoxlayıb kiçik vahidi implementasiya et. Canlı yoxlama və emulator açılması istəmə.
