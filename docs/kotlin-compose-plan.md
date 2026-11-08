# Wobbrik — Kotlin + Compose hazırlama planı

Şəhərin qüvvədə olan vizual mənbəyi: [təsdiqlənmiş iki görünüş](design/README.md). Plan və həcmli şəhər görünüşü birlikdə tətbiq edilir; köhnə konseptlər istifadə edilmir.

## Cari icra göstərişi — 4 noyabr 2026

İstifadəçi yekun Android oyununun hazırlanmasını başladıb; ayrıca atılacaq prototip tələb olunmur. İlk oyun vahidi yekun layihə strukturu və əsas oynanan tikinti dövrüdür. Mövcud `android-billing` inteqrasiya üçün qorunur. Ölçülər sənədləşdirilmiş, versiyalı oyun dizaynı parametrləri kimi seçilə bilər; geometry həmin modeldən çıxarılmalıdır. Bu göstəriş əvvəlki ölçü mənbəyi gözləmə məhdudiyyətini əvəz edir. Rəqəmlər real bina ölçməsi və ya ölçülmüş balans kimi təqdim edilmir.

Yeni dizayn tokenləri mərkəzləşdiriləcək; ui-craft kontrast və faktiki render/baxış tələb edir. Domain dəyişiklikləri AppBackend ilə birlikdə yoxlanır. Typed geometry, provenance, union/transform/containment/overlap server yoxlamaları hazır olmadan placement bağlı qalır. Real SKU, Console və deploy əhatədən kənardır. Branch → uyğun commit → lokal review → main-ə merge → yalnız main üzərində lokal CI; uğursuzluqda ayrıca fix branch → review → merge → main CI qaydası AGENTS.md-dədir. GitHub CI yaradılmır.

5 noyabr 2026 vəziyyəti: engine main-dədir; Compose hostun lokal implementasiyası mövcuddur, lakin məşq axını, sxematik qrafika və çatışmayan server/bərpa inteqrasiyası səbəbindən yekun oyun tələblərinə uyğun tamamlanmayıb; aşağıdakı tarixi “kod icazəsi yoxdur” qeydləri bu göstərişlə əvəz olunur. Davam üçün `docs/context/current-task.md` əsasdır.

Tarix: 3 noyabr 2026. Status: koddan əvvəl baxış üçün texniki plan. Android-only, Kotlin + Compose, Spark, ayrıca Wobbrik adapteri və game-plan bölmə 23 kataloqu təsdiqlənib. Aşağıdakı yeni modul/data/API bölgüsü implementasiya təklifidir; istifadəçinin əvvəlki məhsul qaydalarını avtomatik dəyişmir. PostgreSQL authority və AppBackend DB qəbul vaxtı 3 noyabr 2026-da təsdiqlənib. Bu tapşırıqda tətbiq/backend kodu yazılmayıb.

## Android versiya qərarı — 4 noyabr 2026

Minimum Android 7.0 (`minSdk 24`), hədəf Android 16 (`targetSdk 36`), `compileSdk 36`. İstifadəçi tərəfindən təsdiqlənib; gələcək application host da bu konfiqurasiyanı istifadə etməlidir.

## 1. Əsas yanaşma

- Compose ekranlar və HUD, Canvas oyun səhnəsi və şəhərin çəkilməsi üçün. Android-dən asılı olmayan Kotlin modulu tarazlıq, eniş, xilasetmə, çətinlik və cəhd vəziyyətini idarə edir.
- Firebase Auth kimlik mənbəyidir. AppBackend Firebase ID tokenindən app + UID-ni müəyyənləşdirir. Ayrı Firebase layihəsi və Android paket allowlist-i saxlanılır.
- Təsdiqlənib: iqtisadiyyat, hesab üzrə oyun irəliləyişi, bina mülkiyyəti, inventar və bazar üçün PostgreSQL əsas mənbədir. Firebase Spark Auth və lazım olarsa pul/mülkiyyət daşımayan profil məlumatlarına xidmət etsin. Şəhərin bina/dekorasiya yerləşdirməsi PostgreSQL-də revision ilə saxlanılsın; offline yalnız keş göstərilsin. Firestore bu modeldə oyun bazası üçün məcburi deyil.
- Room server snapshot-ları, yarımçıq cəhdin lokal köməkçi qeydi və eyni identifikatorla retry edilən gözləyən sorğular üçündür. DataStore səs/vibrasiya/dil kimi cihaz seçimləri üçündür. Lokal pul dəyişməsi server hüququ yaratmır.
- Firestore/PostgreSQL ikili yazısını bir tranzaksiya saymırıq. Gələcək Firestore oxu proyeksiyası lazımdırsa outbox ilə revision-lu surət olacaq; pul və alış qərarlarında həmin surət əsas götürülməyəcək.

### Vahid əsas mənbə təklifinin dəqiq sərhədi

Status: 3 noyabr 2026-da istifadəçi PostgreSQL-in vahid əsas mənbə olmasını və AppBackend-in DB qəbul vaxtını təsdiqləyib. Aşağıdakı detallar bu qərarların texniki müqaviləsidir; implementasiya aparılmayıb.

- AppBackend qaydaları yoxlayır; PostgreSQL qəbul edilmiş oyun vəziyyətini saxlayır. Bütün xərclənən sikkə mənbələri — oyun, nailiyyət, alış və bazar — eyni app + uid wallet/ledger-inə yazılır. Mənbə ledger-də ayrıca səbəb kimi qalır; ikinci xərclənən balans yaranmır.
- Wallet ilə birlikdə dəyişən XP, unlock, bina, inventar, şəhər və elan qeydləri mümkün olan müvafiq əməliyyatda eyni DB tranzaksiyasına daxildir. Ledger auditi və wallet balansı tranzaksiyada uzlaşdırılır; PostgreSQL təkbaşına bu invariantları tətbiq kodu və constraint-lərsiz təmin etmir.
- Firebase Auth kimlik üçün əsas qalır. Room və mümkün Firestore proyeksiyası server vəziyyətini əvəz etmir; köhnə snapshot və ya hesab dəyişməsi balans/mülkiyyət birləşdirmir. Outbox əmrləri aid olduqları app + uid-yə bağlıdır, başqa hesabla göndərilmir.
- Offline məşq və lokal yarımçıq cəhd köməkçi məlumatdır. Server əlçatmazdırsa iqtisadi mutasiya və təsdiqli progression dayandırılır; keş baxışı və mükafatsız məşq mümkündür. Onlayn cəhd üçün bölmə 4-dəki fasilə/bərpa qaydası qalır.
- Bu seçim bazarda atomikliyi və vahid uçotu həll edir; saxta checkpoint/reward məlumatını etibarlı etmir. Refund, hesab konflikti, silmə/retention və transfer siyasəti ayrıca açıqdır.

## 2. Arrowz-dan istifadə xəritəsi

İlkin strategiya: seçilmiş hissələri Wobbrik namespace-i ilə uyğunlaşdırmaq, Arrowz-u dəyişməmək. İki tətbiq üçün ortaq runtime kitabxanası yaratmaq ilk mərhələnin şərti deyil. SDK versiyaları və lisenziya/atribusiya şərtləri köçürmə zamanı yoxlanacaq; bütün repo klonlanmayacaq.

### Kiçik uyğunlaşdırma ilə namizədlər

- build-logic və Gradle convention plugin yanaşması: Android/Compose/JVM modul ayrılığı, debug/release konfiqurasiyası, lint və CI əmrləri. Arrowz-un app ID, signing, Firebase konfiqurasiyası və CI secret-ləri daşınmır.
- core/common/Dispatchers.kt və core/data/NetworkConnectivityMonitor.kt: dispatcher ayırımı və bağlantı müşahidəsi. Yeni app lifecycle ilə yoxlanacaq.
- core/datastore/SettingsPrefs.kt: səs/vibrasiya/dil üçün saxlama yanaşması; yalnız uyğun sahələr.
- core/ads/UmpConsentManager.kt, AdRequestGate.kt, AdMobBackend.kt, RewardedAdProvider.kt: consent və SDK çağırış sərhədləri namizəddir. Reklam siyasəti bu alətlərdən ayrılacaq.
- core/auth/AuthService.kt və CredentialManagerGoogleTokens.kt: anonim hesab, Google giriş/link və xətaların modelləşdirilməsi. Interface-də olmayan backend ID-token refresh inteqrasiyası əlavə layihələndiriləcək. Email-link axınının indiki platforma uyğunluğu ayrıca yoxlanmalıdır; işlək kimi kor-koranə daşınmır.
- core/testing/SnapshotMatrix.kt, fake provider/clock/repository və CI test üsulları: Wobbrik ölçülərinə və mətnlərinə uyğunlaşdırılacaq. Snapshot nəticələri/ölçülmüş sürət Wobbrik üçün hazır təminat deyil.

Bu siyahıda fayl mövcudluğu təsdiqlənib; bütün faylların tam davranış auditi aparılmayıb. Hər köçürmə öz kiçik uyğunluq yoxlamasından keçəcək.

### Yanaşması qalır, domeni yenidən yazılır

- PlayBillingBackend.kt: məhsul sorğusu, checkout, pending və retry yanaşması faydalıdır. Wobbrik-də əvvəl backend billing/identity, sonra setObfuscatedAccountId və Play checkout, sonra server verify tələb olunur. Cihaz hüquq/sikkə verməyəcək və ack/consume etməyəcək; bunlar AppBackend-də çatdırılma commit-indən sonradır.
- BoardCanvas.kt və BoardCamera.kt: koordinat çevirməsi, Canvas qatları və toxunuş ayırımı öyrəniləcək. Arrowz-un tap-away lövhəsi, zoom hədləri və animasiya modeli Wobbrik qülləsinə uyğun hazır mühərrik deyil.
- Room/DAO/repository pattern: keş və outbox üçün istifadə edilə bilər; RoomCoinWallet əsas pul mənbəyi kimi götürülməyəcək.
- AnalyticsTracker və hadisə interfeysləri: razılıq mexanizmi saxlanır, hadisə adları və parametrlər Wobbrik üçün seçilir. Token, email və alış payload-ı analitikaya göndərilmir.
- Dizayn sistemi: tokenlərin ayrılması və uyğunlaşan ekran quruluşu üsulu faydalıdır; Arrowz Neon rəngləri, ikonları və maskotu avtomatik götürülmür. Wobbrik tokenləri ayrıca hazırlanacaq.

### Birbaşa köçürülmür

- RoomCoinWallet: lokal kredit və powerup iqtisadiyyatı Wobbrik-in server bazarı üçün authority deyil.
- SyncPlanner.resolve: themes union, removeAds OR və lokal/cloud tərəf seçimi server alış hüquqlarına tətbiq edilmir. Wobbrik server balansı və mülkiyyətini lokal snapshot əvəz edə bilməz.
- AdPolicy/AdCaps: Arrowz-un level/sessiya/gündəlik limitləri Wobbrik-ə daşınmır. Wobbrik-də 4 bitmiş cəhd + 5 dəqiqə aktiv oyun, nəticə fasiləsi, reward-dan sonra sıfırlama və ilk onlayn günün Bakı gecəyarısınadək güzəşti qalır.
- Puzzle engine, level paketləri, lives/hints/undo/delete, daily challenge, dost dəvətləri və remove-ads məhsulu yeni tələb sayılmır.
- Firebase layihə ID-si, AdMob unit-ləri, SKU-lar, tətbiq məlumatları, hüquqi mətnlər və mağaza resursları yeni app üçün ayrıca olmalıdır.

## 3. Wobbrik modul sərhədləri

İlk prototipdə app + engine kifayətdir. Domen böyüdükcə aşağıdakı məsuliyyətlər ayrılır; əvvəlcədən onlarla boş modul yaradılmır.

- engine: GameState, command/event, sabit zaman addımı, crane/fall/landing, tarazlıq və engine versiyası; Android/Compose/Firebase asılılığı yoxdur.
- app və feature/game: lifecycle, toxunuş, Canvas renderer, kamera, HUD, səs/haptic. feature/city, collection, shop, market, account, leaderboard sonrakı mərhələlərdə.
- core/model və catalog: sabit hissə ID-ləri, bina/geometriya modeli və kataloq versiyası. Müştərinin kataloqu görüntü üçündür; qiymət/çəki və mükafat hesabı serverdə ayrıca əsasdır.
- core/data: AppBackend client, Room keş/outbox, repository və server revision uzlaşdırması.
- core/auth, ads, billing: SDK sərhədləri; feature-lər provider detalları və server sirrlərini bilmir.

## 4. Oyun dövrü və bərpa

- Sabit simulyasiya addımı üçün ilkin namizəd 1/60 saniyədir; çəkilmə ekran frame vaxtına uyğun olur. Uzun frame zamanı catch-up məhdudlaşdırılır, arxa plandan qayıdışda böyük zaman sıçrayışı hesablanmır. Bu parametr prototipdə ölçüləcək.
- Fizika hesabı composable body və draw callback-də aparılmır. Renderer hazır snapshot oxuyur; tez dəyişən koordinatlar bütün ekranın hər frame recomposition-una səbəb olmamalıdır. Şəbəkə/DB draw yolunda işləmir.
- Vəziyyətlər: hazır → yellənən mərtəbə → düşmə → enişin qiymətləndirilməsi → sabit/təhlükəli və ya məğlubiyyət; əlavə fasilə, server təsdiqini gözləmə və reward-dan qayıtma halları.
- Düşmə vaxtı əlavə toxunuş qəbul edilmir. Kosmetik hissə fizikanı dəyişmir. İlk 5 mərtəbə, xilasetmə epizodu və uduzma qərarı game-plan qaydalarına əsaslanır.
- Aktiv oyun HUD-u telefon/planşetdə yalnız pauza düyməsi və interaktiv olmayan say/rekord göstəricisidir; ayrıca Burax, naviqasiya rail-i və yan panel yoxdur. Səhnəyə toxunmaq buraxır; pauza modalı və Davam et toxunuşu scene input-a keçmir. Pauzada Davam et / Yenidən başla / Menyuya qayıt göstərilir; cari konsept docs/design/game-tap-v2/README.md-dir.
- Hər onlayn cəhddə server attempt ID-si və ardıcıl checkpoint revision-u var. Enmiş mərtəbənin server commit-i mükafat, sayğac və checkpoint-i birlikdə yazır. Hər frame şəbəkəyə göndərilmir. Başlanğıc prototipdə təsdiq gözlənərkən növbəti buraxılış bağlıdır; gecikmənin oyun hissinə təsiri ölçüləcək. Başqa batching seçimi məhsul qaydasını dəyişirsə ayrıca razılaşdırılır.
- Bağlantı kəsiləndə fasilə; eyni sorğu retry olunur. Server qəbul edib cavab itibsə təkrar mükafat yoxdur. Bərpada server snapshot-u üstün gəlir. Düşən mərtəbə yenidən kranda göstərilir; bitmiş cəhd açılmır. Lokal təsdiqsiz məlumat son həqiqət deyil.
- Offline məşqdə server cəhdi, XP/sikkə, satılan bina, nailiyyət və leaderboard yazılmır. Gözləyən onlayn cəhd offline məşqə çevrilərək mükafat qazanmır.

## 5. AppBackend-də Wobbrik adapteri

### İcra göstərişi və birgə yenilənmə tələbi — 3 noyabr 2026

İstifadəçi tam server adapterinin hazırlanmasını istəyib: vahid wallet, oyun/nailiyyət mükafatları, bazar, bina mülkiyyəti, şəhər və progression. Oyunun özü hələ qurulmur; real SKU, Console və deploy sonraya saxlanır. Bu göstəriş implementasiya səlahiyyətidir, tamamlanma qeydi deyil.

Oyun tərəfdə bu axınlardan biri qurulanda və ya update ediləndə AppBackend-də uyğun domain qaydaları, API/DTO, schema/migration və testlər də həmin iş çərçivəsində yoxlanmalı və lazım gəldikdə yenilənməlidir. Mobil axın server müqaviləsindən ayrı hazır sayılmır. Müqavilə/qayda versiyası, revision, idempotency, hesab izolyasiyası və köhnə client uyğunluğu birlikdə yoxlanır. Dəqiq şəhər ölçüləri və fizika hədləri bu göstərişlə seçilmiş hesab edilmir; hesab konflikti, refund, retention və transfer üzrə açıq siyasətlər də təsdiqlənmiş sayılmır.

### Mövcud nüvə və yeni iş ayrılığı

### Çoxoyunlu backend sərhədi — 4 noyabr 2026

AppBackend 2–3 oyuna xidmət edir; hər oyun öz adapterini çağırır. Eyni istifadəçi bütün oyunlarda iştirak edə bilər. Hesabın oyun daxilində açarı `(app_id, uid)`-dir; tək `uid` oyun məlumatına giriş üçün kifayət etmir. Wallet/ledger, alış və entitlement, progression/nailiyyət, attempt/checkpoint, inventar/mülkiyyət, bazar/şəhər, nəticələr, revision/dedup və cache açarları oyun sərhədini qorumalıdır. Vahid wallet authority hər oyunun öz hesabına aiddir; oyunlar arasında ortaq balans və ya transfer nəzərdə tutulmur.

Server autentifikasiya və tətbiq konfiqurasiyası ilə icazəli app sərhədini yoxlayıb uyğun adapterə yönləndirir; müştərinin göndərdiyi app/obyekt ID-si başqa oyuna giriş hüququ vermir. Ortaq auth, billing və worker infrastrukturu oyun domain vəziyyətini birləşdirmir. Eyni UID və eyni operation/obyekt identifikatorları ilə müxtəlif oyunlarda paralel əməliyyatların, retry-ların və oxuların bir-birinə təsir etməməsi regression testləri ilə yoxlanmalıdır. Bu tələb qeydə alınıb; bütün backend yollarının buna uyğunluğu ayrıca kod/schema/test yoxlaması tələb edir.

Mövcud internal/platform nüvəsində Firebase auth, konfiqurasiyadan app seçimi, Google Play verify, purchase/ledger atomik krediti, ack/consume job-ları və limitlər var. Hazır route-lar server.go-da billing/identity, billing/catalog, billing/google/verify, entitlements və wallet-dir. Bunların olması tam Wobbrik iqtisadiyyatının hazır olması deyil.

Yeni təklif: internal/wobbrik domain paketi. Ayrı executable/Redis/Kafka tələb edilmir. Ortaq nüvə token/provider/worker işlərini saxlayır, Wobbrik adapteri oyun və bazar qaydalarını idarə edir. Adapter callback-i ayrıca commit açmamalıdır: alış krediti ilə Wobbrik wallet təsiri eyni PostgreSQL tranzaksiyasında olmalıdır. Mövcud wallet/ledger genişləndirilir və ya ona uyğun repository yaradılır; paralel ikinci pul cədvəli əsas mənbə kimi açılmır. Digər tətbiqlərin davranışı regression testləri ilə qorunur.

### Serverin əsas qeydləri

- app + uid hesabı, wallet və səbəb/operation ID-li ledger.
- attempt/checkpoint: engine/rules version, sequence, son qəbul edilmiş mərtəbə və vəziyyət, dəqiq eniş/xilasetmə sayğacları, davam hüququ, active/finished status.
- hesab progression-u, nailiyyətin claimed vəziyyəti, daimi hissə unlock-ları və reklam irəliləyişi.
- bina: server ID-si, creator/owner, həndəsə/hissə snapshot-u, revision, residents və qaydadan hesablanan dəyər. Qiymət/creator/owner müştəridən authority kimi götürülmür.
- şəhər yerləşdirməsi, ərazi hüquqları və dekorasiya nüsxələri; bina məzmunu dəyişmədən yeri dəyişə bilər.
- elan və alış əməliyyatı; həftəlik/ümumi nəticə; reward attempt və request dedup qeydləri.

Bunlar məntiqi qeydlərdir, yekun SQL schema deyil. Böyüyən bütün inventar bir JSON sətrində saxlanmır; səhifələmə və request ölçü hədləri olacaq.

### Atomik əməliyyatlar

- Mərtəbə checkpoint-i: sequence doğrulaması, yalnız yeni mərtəbələr üçün mükafat, XP/level, nailiyyət bonusu/unlock və snapshot bir commit. Cəhdin yekunlaşdırılması minimum 10 mərtəbə olduqda yalnız bir bina və bir nəticə yaradır; əvvəlki mərtəbə sikkəsini yenidən vermir.
- Mağaza/ərazi: server qiyməti və level şərti, pul debiti və unlock/nüsxə eyni commit. Dekorasiya inventara qayıdanda refund yoxdur.
- Elan: creator = owner, təkrar satılmamış bina, bir binaya bir elan, hesab üzrə maksimum 5 aktiv elan. Limit paralel sorğularda hesab/yuva kilidi ilə qorunur.
- Bazar alışı: elan/bina revision-u, kifayət qədər balans, alıcının satıcıdan fərqli olması, server qiyməti/komissiyası; alıcı debiti, satıcı xalis krediti, komissiya ledger-i, mülkiyyət keçidi, satıcının şəhər yerləşdirməsinin silinməsi və elanın bağlanması eyni commit. Alıcıya bina əvvəl inventarda verilir. Özünə satışın rədd edilməsi yeni texniki təklifdir.
- Kilidlər sabit ardıcıllıqla götürülür; unique constraints yarışda ikinci alıcını bloklayır. Redaktə/silmə/elan yarışı da eyni revision və kilidlərdən istifadə edir.
- Alış: mövcud billing commit-i vahid wallet-ə bir kredit verir. PENDING hüquq vermir; durable entitlement serverdən gəlir; cihaz consume/ack etmir.

Baza dəyəri 2F + H, maksimum floor(1,20 × B), komissiya max(1, ceil(0,05 × qiymət)), hər mərtəbəyə 1 XP/1 sikkə və digər təsdiqli balans saxlanılır.

### Sorğu müqaviləsi

Mövcud billing route-ları saxlanır. Namizəd yeni əməliyyatlar: cəhd başlat/checkpoint/finish/read; öz şəhər/inventar/progression snapshot-u; hissə/dekorasiya/ərazi al; bina redaktə/sil; elan yarat/geri götür/list/buy; reward başlat/claim; ümumi/həftəlik leaderboard. Yekun URL və DTO-lar sonrakı müqavilə işidir, bu route-lar hazır deyil.

Hər mutasiyada operation ID, gözlənən revision və qayda versiyası olmalıdır. Dedup app + uid + operation ID ilə scope olunur, amma unikal bina/alış/attempt sequence məhdudiyyətlərini əvəz etmir. Eyni ID və eyni payload əvvəlki nəticəni qaytarır; eyni ID fərqli payload ilə rədd edilir. Commit-dən sonra timeout-da status sorğusu/retry edilir; ikinci kredit və ya alış yaradılmır. Pul müştəridən gələn delta ilə dəyişdirilmir.

Səhvlər UI-da ayrılır: offline/retry, revision conflict/refetch, kifayətsiz balans, artıq satılıb, blocked account, provider pending. Gözləyən əməliyyat uğurlu kimi göstərilmir.

### Hesab və etibarlılıq sərhədi

- Anonymous Firebase UID ilə onlayn oyun mümkündür; real alış üçün daimi hesaba keçid lazımdır. Guest link eyni UID-ni saxlayırsa davam edir. Artıq mövcud hesab konflikti üçün balansları toplamaq yoxdur; istifadəçi seçimi/migrasiya siyasəti ayrıca qərardır. UID/app client body-dən götürülmür.
- Yeni server authority təkrarı və atomikliyi qoruya bilər; cihazın fizikasının həqiqiliyini avtomatik sübut etmir. İlk modeldə struktur/sequence/ədədi limitlər yoxlanır, amma saxta uyğun checkpoint riski bölmə 22-də qəbul edilmiş kimi qalır. Kotlin-Go eyni fizika/replay tələbini hazır saymırıq.
- Reward ilk inteqrasiyada təsdiqlənmiş cihaz SDK callback-i + server reward-attempt dedup modelidir. Bir cəhddə bir davam və 3/5/8 hissə irəliləyişi serverdə qorunur. AppBackend SSV üçün imkan yaradır, lakin SSV hazır deyil və bu planda avtomatik yeni tələb seçilmir.
- Təsdiqlənmiş vaxt mənbəyi: AppBackend-in yoxlayıb PostgreSQL-də qəbul etdiyi əməliyyatın vaxtı əsasdır; aşağıdakı müqavilə tətbiq edilir. Bu, əvvəlki «Firebase qəbul vaxtı» texniki mənbəyini əvəz edir, həftə qaydasını dəyişmir.
- PostgreSQL-də əvvəlki həftələrin nəticələri saxlanır; sıralama üçün fiziki həftəlik silmə tələb olunmur. Hesab üzrə ən yaxşı nəticə və stabil cursor nəzərdə tutulur.

### Vaxt müqaviləsinin dəqiqləşdirilməsi

Status: 3 noyabr 2026-da istifadəçi PostgreSQL-in vahid əsas mənbə olmasını və AppBackend-in DB qəbul vaxtını təsdiqləyib. Aşağıdakı detallar bu qərarların texniki müqaviləsidir; implementasiya aparılmayıb.

- Vahid təqvim saatı PostgreSQL primary-nin saatıdır. `accepted_at` yoxlamalar və lazımi kilidlərdən sonra, nəticə yazılmazdan əvvəl bir dəfə `clock_timestamp()` ilə götürülür və nəticə ilə eyni tranzaksiyada saxlanır. Yalnız commit edilmiş qeyd qəbul edilmiş sayılır. Bu, HTTP giriş və ya dəqiq commit timestamp-i deyil. Rollback olduqda qəbul vaxtı yaranmır; sonrakı uğurlu cəhdin vaxtı əsas olur.
- `now()`/`CURRENT_TIMESTAMP` tranzaksiyanın başlanğıcını, `statement_timestamp()` statement başlanğıcını verir; buna görə onlar burada yoxlamalardan sonrakı qəbul anının əvəzi deyil. DB saatının sinxronluğu və saat sürüşməsinin monitorinqi istismar tələbidir; timestamp mütləq monoton sıra zəmanəti vermir.
- Vaxt anları `timestamptz` ilə saxlanır və API-də UTC verilir. Həftə `Asia/Baku` ilə hesablanır: bazar ertəsi 00:00 daxil, növbəti bazar ertəsi 00:00 xaric. Həftə açarı il + həftə nömrəsi əvəzinə Bakı həftəsinin başlanğıc tarixi ola bilər. Sərhəd hesablaması serverin lokal timezone parametrindən asılı olmur.
- Həftəni yalnız cəhdin yekun nəticəsinin `accepted_at` vaxtı seçir; start/checkpoint vaxtı həftəni rezerv etmir. Məsələn, bazar 23:59-da bitən, bazar ertəsi 00:01-də qəbul edilən nəticə yeni həftəyə düşür. Sorğu sərhəddən əvvəl gəlib kiliddə gözləsə də, yoxlamalardan sonrakı qəbul anı əsasdır. Commit sərhəddən sonra bitsə, saxlanmış qəbul anı dəyişmir.
- Eyni əməliyyatın commit-dən sonra retry-si ilkin nəticə ID-si, `accepted_at` və həftəni qaytarır. Cəhdin bir dəfə yekunlaşdırılması invariantı fərqli operation ID ilə göndərilən təkrarı da ikinci nəticəyə çevirmir.
- Sıralama: uğurlu mərtəbə sayı azalan, `accepted_at` artan, sabit server nəticə ID-si artan. Eyni hesabın eyni mərtəbə rekordunda əvvəl qəbul edilən nəticə saxlanır; daha yüksək nəticə öz qəbul vaxtı ilə onu əvəz edir. Ümumi rekord və həftə rekordu ayrıca seçilir. Nəticə ID-si yalnız tam bərabərlikdə sabitlik üçündür, ayrıca xronologiya vədi deyil.
- İlk uğurlu onlayn giriş AppBackend-in autentifikasiyalı hesab açılışı/sinxronizasiyasını DB-də ilk dəfə uğurla qeyd etməsidir; yalnız Firebase tokeninin alınması kifayət deyil. `first_online_at` app + uid üzrə bir dəfə, paralel cihazlarda da atomik yazılır. Güzəşt həmin anın aid olduğu Bakı günündən sonrakı gecəyarısında bitir. Retry, yenidən giriş və reinstall bu qeydi dəyişmir; yeni UID məsələsi hesab siyasətinin sərhədində qalır.
- 5 dəqiqə aktiv oyun müddəti təqvim vaxtından ayrıdır: cihazda monoton müddət sayğacı ilə ölçülür, pause/arxa plan/menyu/reklam daxil edilmir. Server vaxtından iki timestamp çıxmaq aktiv oyun müddətini sübut etmir. Müştəri bildirişinin etibarlılığı ayrıca məhdudiyyətdir; sayğacın bərpası və serverdə saxlanması reklam inteqrasiyası müqaviləsində dəqiqləşdiriləcək.

### Leaderboard server vahidi — 4 noyabr 2026

AppBackend-də `GET /v1/apps/wobbrik/game/leaderboard` ümumi və cari Bakı həftəsi üçün server snapshot səhifələməsi ilə qurulub. Dəqiq DTO, sıralama, 15 dəqiqəlik hesaba bağlı cursor, retry və köhnə client müqaviləsi `AppBackend/docs/wobbrik-game-api.md`-dədir. Hər online finish nəticə yaradır; bina üçün 10 mərtəbə həddi ayrıca qalır. İlkin finish qəbzinin qəbul vaxtı dəyişmir. Android typed gameplay adapter/host bu vahiddə qurulmayıb; billing və vahid wallet dəyişməyib.

### Şəhərin ölçülərdən asılı olmayan server vahidi — 4 noyabr 2026

İstifadəçi şəhər üçün yuxarıdan ikiölçülü x/y planını və eyni şəhərin həcmli görünüşünü seçib; dəqiq sahə, boşluq və dekorasiya ölçülərinin açıq saxlanmasını, yalnız onlardan asılı olmayan server hissəsinin tamamlanmasını istəyib. AppBackend `GET /game/city` ilə hesab üzrə şəhər üzvlüyünü, revision-u və bütün yerləşmiş binaların sakin cəmini verir; cəm overflow-suz dəqiq onluq string-dir. `POST /game/city/buildings/return` bina mülkiyyətini/hissələrini və aktiv elanı saxlayaraq onu inventara qaytarır; refund/XP vermir. Migration 010 şəhər üzvlüyünün real sahibə aidliyini və bina silinəndə təmizlənməsini qoruyur.

Yerləşdirmə hələ aktiv deyil (`placement_enabled:false`). Əyri binanın x/y sahəsinə çevrilməsi, koordinat vahidi, ərazi formaları, təhlükəsiz boşluq, dönmə və dekorasiya footprint-ləri seçilməyib. Opaque structural-v1 snapshot-dan həndəsə uydurulmur. Dəqiq hazır müqavilə və qalan həndəsə tələbləri `AppBackend/docs/wobbrik-game-api.md`-nin şəhər bölməsindədir; Android host/oyun/SKU/Console/deploy bu vahidə daxil deyil.

## 6. Mərhələlər

Mərhələ siyahısı və vəziyyəti: [PLAN.md](PLAN.md). Əvvəlki mətn: [archive/plan-history.md](archive/plan-history.md).

## 7. Yoxlama və açıq qərarlar

- Pure engine: dəqiq/göndərilmiş eniş, təhlükə epizodu, yeni mərtəbə ilə xilasetmə, gözləmədə məğlub olmama, replayable input, pause/resume, bir davam, yeni cəhd reset-ləri.
- Android: proses ölümü, screen/lifecycle, ikiqat tap, 10-dan aşağı nəticə, uzun qüllədə çəkilən hissələrin məhdudlaşdırılması; bütün qülləni hər frame yenidən qurmaq olmaz. ui-craft ilə token/kontrast və real screenshot baxışı implementasiya mərhələsində.
- Backend: real PostgreSQL concurrency/rollback/dedup və tenant separation; ortaq nüvədə dəyişiklik olsa go vet, race və DB integration testləri. Firebase istifadə edilən client sahələri üçün emulator Rules testləri; server yazıları Rules ilə qorunmur, server/IAM yoxlamaları lazımdır.
- Açıq məhsul qərarları: mövcud hesab konflikti; refund olunmuş xərclənmiş sikkə, hesab silmə/retention/transfer; real pul qiymətləri/SKU. Texniki detallar: dəqiq Android/SDK versiyaları, fizika hədləri, API schema və yük büdcəsi.
- Hazır olmayan SSV/anti-cheat-i hazır göstərmək olmaz. Müştəridən bildirilən oyun gəliri təhlükəsiz alış gəliri ilə eyni wallet-də xərclənəcəksə saxta oyun riski bazara təsir etməyə davam edir.

## 8. Oxunan mənbələr və dəqiq növbəti addım

Yerli: Arrowz settings.gradle.kts; core/auth/AuthService.kt; core/ads/AdPolicy.kt; core/billing/PlayBillingBackend.kt; core/data/RoomCoinWallet.kt; core/sync/SyncPlanner.kt. Əlaqəli fayl adları 2-ci bölmədədir. AppBackend docs/architecture.md, docs/integration.md, internal/platform/server.go və config.go, store.go və migrations/001_init.sql üzrə məhdud axtarış oxunub. Tam kod auditi aparılmayıb.

Rəsmi mənbələr (3 noyabr 2026):
- Compose phases: https://developer.android.com/develop/ui/compose/performance/phases
- Firestore server/Rules sərhədi: https://firebase.google.com/docs/firestore/security/rules-conditions
- PostgreSQL vaxt funksiyalarının semantikası: https://www.postgresql.org/docs/current/functions-datetime.html#FUNCTIONS-DATETIME-CURRENT

Dəqiq növbəti addım: yeni chatdə bu planı və docs/context/current-task.md-ni oxu; authority və DB qəbul vaxtı qərarları təsdiqlənib; ayrıca kod tapşırığı verilərsə yalnız offline mexanika prototipinin kiçik skeletindən başla. Bu plan özü kod icazəsi deyil.
