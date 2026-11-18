# Project agent instructions

Koddan əvvəl bu faylı və `docs/context/current-task.md`-ni oxu. Paralel əsas plan və yeni stack pinləri uydurma.

## Canonical sources

- Yeganə plan: `docs/PLAN.md` — mərhələlər §1, arxitektura/server §2, lifecycle §3, məhsul/iqtisadiyyat §4–5, kataloq §6, şəhər §7. Başqa plan yaratma.
- Aktiv handoff: `docs/context/current-task.md`. `.cursor/context/current-task.md` yalnız bu fayla girişdir, ikinci status mənbəyi deyil.
- Qısa kontekst: `.cursor/context/{architecture,business-rules,decisions}.md`.
- Cursor üçün always-on güzgülər: `.cursor/rules/*.mdc`; qayda dəyişəndə uyğun güzgünü də yenilə.
- Ziddiyyətdə istifadəçinin son açıq qərarı → planın cari icra göstərişi və təsdiqlənmiş qərarları → uyğun plan bölməsi/əlavəsi → köhnə tarixi qeydlər. Həll olunmamış ziddiyyəti soruş; köhnə statusu yeni göstərişin əvəzi sayma. Traceability ID-ləri ayrıca plan deyil.

## Stack və sərhədlər

- Android-only, Kotlin + Compose/Canvas; paket `com.aladdin.wobbrik`. Modul və mövcud pinlər `.cursor/context/architecture.md`-dədir; build faylları versiyalar üçün əsasdır.
- AppBackend/PostgreSQL iqtisadiyyat və qəbul edilmiş oyun vəziyyəti üçün authority-dir; Firebase Auth kimlik üçündür. Room/cache/outbox hədəfi serveri əvəz etmir və hazır implementasiya kimi təqdim edilmir.
- Real SKU, Console, remote push və deploy ayrıca göstəriş tələb edir.

## Mərhələ qapısı

1. Yalnız real açıq texniki/funksional sualları koddan əvvəl dəqiqləşdir və qərarı qeyd et; təsdiqlənmiş dizaynı yenidən seçim sualına çevirmə.
2. Mövcud skelet üzərində həmin vahidin domain/data/UI inteqrasiyasını hazırla. Müvəqqəti saxta UI və demo adapter yekun oyun deyil.
3. Uyğun fokuslu yoxlamaları və axının son yoxlamasını apar; CI yalnız aşağıdakı branch/main qaydasına tabedir.
4. Mövcud təsdiqlənmiş dizaynı birəbir tətbiq et. Şablondakı “yeni nümunə → yeni dizayn dəsti” axını təsdiqlənmiş dizaynları əvəz etmir; yeni/alternativ dizayn əvvəlcədən razılıq tələb edir.
5. Mobil portret ölçüləri, böyük şrift, inset/klaviatura, əlçatanlıq və tələb olunan lokalları yoxla; zəruri scroll-u saxla. Planşet ayrıca tapşırıqdır. Təsdiqlənməmiş dil siyahısını uydurma.
6. Vizual vahiddə real build renderlərini dizaynla yanaşı müqayisə et; nəticə və məhdudiyyətləri `docs/reviews/` altında yaz. Tam mərhələ qəbulu üçün ayrıca `docs/reviews/<phase>/acceptance.md` istifadə oluna bilər; mövcud review-ları köçürmə.
7. Dizayn nümunəsinin OK-u mərhələ qəbulu deyil; mərhələ qəbulu növbəti mərhələyə başlama icazəsi deyil. Növbəti mərhələ ayrıca istifadəçi tapşırığı ilə başlayır.

## Code organization

- Yeni koddan əvvəl mövcud paket/qovluq konvensiyalarını yoxla və uyğun olanı təkrar istifadə et.
- Feature və məsuliyyət üzrə qruplaşdır; hər fayl/sinfin aydın işi olsun.
- UI, state/orchestration, domain qaydaları və data access ayrılır. Domain qaydalarını UI/persistence-də təkrarlama.
- Feature helper-ləri feature daxilində saxla; yalnız aydın ortaq məqsədlə paylaş. Modul sərhədində uyğun interfeys seç; dairəvi asılılıq yaratma.
- Boş layer/framework və vaxtından əvvəl abstraksiya yaratma. Bitirməzdən əvvəl yerləşmə, dublikat və importları yoxla; uyğun build/test nəticələrini dürüst qeyd et.
- Dirty owner işini səssiz stash/reset/force ilə məhv etmə. Diagnostic ekran real domain/repository çağırmalıdır; demo/synthetic adapter açıq işarələnməlidir.

## Performans — yeni və dəyişən səhifələr

- Hər yeni ekran, tab, detail və naviqasiya axını ilk implementasiyadan performans nəzərə alınaraq hazırlanır; optimizasiya ayrıca son mərhələyə saxlanmır. Bu qayda həmin axına xidmət edən mobil və AppBackend oxularına da aiddir.
- Koddan əvvəl mövcud repository/cache/art helper-lərini tap; ilk açılış, təkrar giriş və refresh üçün hansı məlumatın göstəriləcəyini, hansı sorğuların lazım olduğunu və işin nə ilə böyüdüyünü müəyyən et. Yeni paralel plan/framework yaratma.
- Uyğun qəbul edilmiş lokal məlumat varsa ekran onu şəbəkəni gözləmədən göstərir; zəruri sinxronizasiya arxa planda gedir. İlk girişdə cache yoxdursa shell və əlçatan loading/error/retry görünür; əlaqəsiz və optional məlumat əsas məzmunu bloklamır.
- Display cache mutasiya icazəsi deyil. UID/login epoch, account revision, fresh mutation və final-sync sərhədlərini saxla; köhnə/başqa hesab məlumatını və qəbul edilməmiş nəticəni fresh kimi göstərmə. Sorğu azaltmaq üçün authority yoxlamasını çıxarma.
- Oxu göstərilən səhifə/detail ilə məhdud olmalıdır. Bir status üçün bütün kolleksiyanı/şəhəri gəzmə, kart başına N+1 sorğu yaratma, eyni aggregate-i hər page üçün təkrarlama. Lazım olan read metadata-sını serverdə owner-scoped projection/bounded batch və uyğun indekslə al; backend uyğunluğunu eyni vahiddə yoxla. Məsələn, İnventar şəhər statusunu display bina cavabındakı `in_city`-dən alır, bütün City səhifələrindən deyil.
- Pagination, cursor və parallelism məhdud olur; təkrar mount/recomposition/back naviqasiyası eyni qəbul edilmiş oxunu səbəbsiz təkrarlamır. Refresh və revision dəyişikliyi düzgün invalidation edir; route/account dəyişəndə iş ləğv olunur, köhnə cavab yeni state-ə yazılmır. Polling yalnız konkret lifecycle ehtiyacı ilə və görünməyən ekranda dayandırılaraq istifadə olunur.
- Main/UI thread-də disk/network I/O, bitmap decode və ağır parse/hesablama etmə. Art-ı uyğun lifecycle/config/density sərhədində paylaş; kart/recomposition başına decode/bitmap allocation və səbəbsiz frame-state yeniləməsi yaratma. Böyük siyahıda sabit key-li lazy komponentlər və linear accumulation istifadə et; gizli eager prewarm ilə xərci başqa ekrana köçürmə.
- DTO yalnız axının istifadə etdiyi məlumatı daşıyır; opaque snapshot kimi ağır unused sahələri display cavabına qatma. Wire, parse, bitmap və tam serializə edilmiş cache paketinin ölçüsünü birlikdə nəzərə al; JSON escaping/wrapper xərcini say. Page/detail cache-ni hesab/revision üzrə atomik saxla; oxu cache həddini command journal həddi ilə qarışdırma. Cache write uğursuzluğu uğurlu live oxunu pozmur; sonsuz payload/cache artımı həll deyil.
- Yeni/dəyişən axın üçün cold/warm/cache-hit/cache-miss/offline/refresh və böyük-hesab hallarından uyğun olanları yoxla. Fokuslu testlər request/byte sayını, lazımsız oxunun olmadığını, cache replay/invalidation və hesab/revision/cancellation sərhədlərini yoxlamalıdır; scheduler-dən asılı sərt millisecond assertion-ları unit testə yazma.
- Optimallaşdırma iddiasına eyni scope/fixture-də əvvəl–sonra ölçmə əlavə et. Yeni axında ilkin request/byte və ilk məzmun/təkrar giriş ölçüsünü qeyd et; backend dəyişibsə SQL sayı ilə yanaşı sorğunun planını/scan və aggregate xərcini uyğun böyük-hesab fixture-ində yoxla. Host/JVM/synthetic nəticəsini production/ART/frame/GPU nəticəsi kimi təqdim etmə; ölçülməyən halları açıq yaz. Məhsul üçün təsdiqlənməmiş latency büdcəsi uydurma.
- Lokal review-da bu qaydaları yoxla, sübut və məhdudiyyətləri `docs/reviews/` altında yaz. Vizual dəyişiklik varsa ayrıca `ui-craft`/native render qaydaları qüvvədədir. Emulator/canlı yoxlama avtomatik tələb edilmir; CI yalnız istifadəçinin ayrıca göstərişi ilə main-də lokaldır.

# Working notes

## Branch, commit və lokal CI

- Hər məntiqi vahid uyğun `feat/`, `fix/`, `chore/` və ya `docs/` branch-ində hazırlanır; `main` üzərində implementasiya edilmir. Commit-lər kiçik, məzmununa uyğun və review edilə bilən olur.
- Hər branch bitəndə diff üzrə lokal review edilir. Tapılan problemlər branch-də düzəldilir; CI branch-də işə salınmır. Uğursuz və ya icra edilməmiş yoxlama keçmiş sayılmır.
- CI yalnız branch `main`-ə merge edildikdən sonra `main` üzərində və yalnız lokaldır: `./scripts/local-ci.sh`. GitHub Actions/remote CI yaradılmır. Arrowz-dakı vahid Gradle `ci` task yanaşması nümunədir, onun oyun qaydaları köçürülmür.
- Review qeydi `docs/reviews/` altında branch, yoxlanmış diff/commit, tapıntılar, düzəlişlər, CI nəticəsi və qalan məhdudiyyətləri göstərir. Son dəyişiklikdən sonra uyğun yoxlamalar yenilənir.
- Lokal review və istifadəçi təsdiqindən sonra branch tarixi saxlanılmaqla `main`-ə merge edilir; yalnız istifadəçi CI-ni ayrıca tələb edibsə `main` üzərində `./scripts/local-ci.sh` işlədilir. CI uğursuz olarsa iş tamamlanmış sayılmır: düzəliş ayrıca branch-də hazırlanır, review və merge edilir, CI yenə yalnız `main` üzərində işlədilir. Remote push/deploy ayrıca göstəriş tələb edir.
- AppBackend dəyişərsə onun ayrıca repository/branch/commit və öz AGENTS.md yoxlamaları da tələb olunur; Wobbrik CI nəticəsi backend yoxlamalarını əvəz etmir.

## Məhsulun icra məqsədi

- Wobbrik yekun, real Android oyunu kimi hazırlanır; ayrıca atılacaq prototip, demo və ya yalnız məşq tətbiqi məqsəd deyil. İş mərhələlərlə görülür, lakin hər vahid yekun oyuna inteqrasiya üçün hazırlanmalıdır.
- Lokal engine və offline iş rejimi server authority-sini əvəz etmir. Məşq rejimi, placeholder qrafika, saxta menyu və ya session-only nəticə real oyun axınının əvəzi və tamamlanmış implementasiya sayıla bilməz. Mövcud belə hissələr açıq texniki borc kimi qeyd olunmalı və yekun axına uyğunlaşdırılmalıdır.
- Tamamlanma dizayna uyğun qrafika, real menyu/naviqasiya, cəhdin bərpası və tələb olunan AppBackend hesab/progression inteqrasiyası ilə yoxlanır; bunlar olmadan yalnız işlək mexanika yekun oyun kimi təqdim edilmir. Real SKU, Console və deploy üçün ayrıca göstəriş tələbi qüvvədədir.

## AppBackend ilə uyğunluq

- Wobbrik-in serveri `/Users/mac/My Workspace/AppBackend` layihəsidir. 4 noyabr 2026 göstərişi ilə yekun Android oyununun Kotlin + Compose implementasiyasına icazə verilib; real SKU, Console və deploy ayrıca göstərişədək sonraya saxlanır.
- Wallet, oyun/nailiyyət mükafatları, progression/checkpoint, bina mülkiyyəti, bazar və şəhər axını qurulanda və ya yenilənəndə AppBackend-də uyğun domain qaydaları, API/DTO, schema/migration və test dəyişikliklərini də eyni işin hissəsi kimi yoxla və lazım olanları et. Yalnız mobil axını dəyişib server uyğunluğunu tamamlanmış sayma.
- Xərclənən bütün sikkələr və mülkiyyət üçün vahid authority AppBackend/PostgreSQL-dir. Client delta, Room və ya Firestore ikinci wallet yaratmır. Alış krediti, oyun mükafatı, xərcləmə və bazar transferi eyni wallet/ledger ilə işləməlidir.
- Müqavilə dəyişəndə backend və mobil uyğunluğunu, qayda versiyasını, revision/dedup, hesab izolyasiyası, retry və köhnə client davranışını yoxla; dəyişmiş API müqaviləsini sənədləşdir. Backend dəyişməsinə ehtiyac yoxdursa bunu yoxlama nəticəsində açıq qeyd et.
- Əsas müqavilə: `docs/PLAN.md`, xüsusən §2 və §7. Backend alış sərhədi: `/Users/mac/My Workspace/AppBackend/docs/wobbrik-billing.md`. Tam domain adapterinin hazır olduğunu yalnız kod və yoxlamalar bunu təsdiqləyəndə bildir.

## Visual work

- Bütün oyun və menyu düymələrində vahid native basılma animasiyası olmalıdır: basılı saxlananda yüngül sıxılma, buraxıldıqda və ya toxunma ləğv edildikdə yumşaq şəkildə ilkin vəziyyətə qayıtma. Bu, navbar/Play, geri, ikon və dialoq əməliyyat düymələrinə də aiddir; disabled düymə basılma reaksiyası vermir. Klik semantikası, əlçatanlıq və toxunma sahəsi qorunur. Bu tələb qaydaya yazılıb, hazırda implementasiya edilməyib; tətbiqi ayrıca tapşırıqla edilir.

- Cari tapşırıq: app-in ekranlarını son təsdiqli PNG-lərə uyğun Kotlin + Jetpack Compose ilə yenidən qur. Bu implementasiya təsdiqlənib; eyni dizaynı yenidən təsdiqə çıxarma. Yeni alternativ dizayn istifadəçinin ayrıca razılığını tələb edir.
- Vizual authority: `docs/design/unified-mobile-v3/README.md`; Home üçün `home-approved-frameless.png`, digər ekranlar üçün həmin qovluqdakı `01-main.png`–`05-owned-and-listings.png`. Home-un 7-slot header/navbar qərarı bütün menyu ekranlarına tətbiq olunur. Köhnə nümunə/proposal/review cari icra göstərişi deyil.
- PNG vizual istinaddır. Ekranın layout-u, panel, kart, düymə, tab, navbar, mətn, balans, XP və state-ləri adi native Compose komponentləri ilə qurulur. Gradient, kontur və kölgə kodla çəkilir; ekran və sadə UI səthləri raster skin ilə yığılmır.
- Navbar düymələri və onların içindəki ikonlar da native komponent/vector (`ImageVector`, vector drawable və ya uyğun Canvas) ilə hazırlanır; PNG-dən kəsilmiş raster ikon istifadə edilmir. İkonun təsdiqli silueti, rəngi və nisbəti saxlanır; təsadüfi standart ikonla əvəz edilmir.
- Raster asset yalnız mürəkkəb illüstrasiya və oyun qrafikası kimi hissələr üçündür. Lazım olan qrafikanı ayrıca asset kimi istifadə et; mətn və idarəetməni asset-ə bişirmə. Sadə UI-ni bütöv şəkillə əvəz etmə.
- Təsdiqli kompozisiya, rəng, tipoqrafiya, boşluq, forma və iyerarxiya saxlanır. Native implementasiya dizaynı sadələşdirmək və ya yenisini uydurmaq icazəsi deyil. Çatışmayan asset/texniki maneəni bildir; razılaşdırılmamış alternativ tətbiq etmə.
- Mobil əvvəl və yalnız portret. Planşet işi ayrıca istifadəçi tapşırığıdır; mövcud təsdiqli planşet görünüşünü pozma. `MainActivity` portret kilidini saxla. Cari mobil işdə landşaft/planşet implementasiyası və renderi tələb edilmir.
- `ui-craft` istifadə et. Rəng/ölçü yazmazdan əvvəl `GameTokens` və mövcud ölçü pillələrini tap. Layout eni, hündürlüyü və fontScale-a uyğunlaşır; kiçik portretdə komponent/boşluq pillələri yığcamlaşır. Əsas əməliyyat görünən və əlçatan qalır; sözlər ortadan qırılmır, idarəetmələr üst-üstə düşmür; zəruri scroll saxlanır.
- Hər vizual vahiddə kiçik və normal mobil portret, fontScale 1.0/2.0 və uyğun inset/uzun mətn halları üçün mümkün native renderləri al, açıb bax və təsdiqli PNG ilə yanaşı müqayisə et. Kontrastı ölç. Build/test PASS vizual uyğunluq sübutu deyil. Yoxlanmamış hallar açıq yazılır; emulator avtomatik açılmır və canlı yoxlama gate deyil.
- Nəticə və fərqləri `docs/reviews/` altında qeyd et. Yalnız tamamlanan vahidi tamamlanmış kimi bildir; bütün ekranlar bitmədən app redesign-ını bitmiş sayma.

# Context growth

Optimize for minimum necessary context. One chat = one logical task.

## Warn and hand off when ANY is true

1. Chat completed 2+ distinct subtasks/features
2. Substantial implementation across 3+ files more than once
3. Long multi-step plan continuing in the same chat
4. History full of large diffs/tool output and next step is another big change
5. Topic switch (auth → payments → export → refactor) in the same chat

## Required actions (do not skip)

1. Finish only the current small logical unit if almost done
2. Update `docs/context/current-task.md` with: Completed / Current state / Decisions / Remaining work / Relevant files / Checks performed / Exact next step
3. Tell the user explicitly:
   `Context böyüyür — bu tapşırığı yeni chatdə davam et.`
   Then give a short continuation prompt (goal + key files + plan path if any)
4. Do not stack another large unrelated step in this chat after the warning

## Do not

- Rescan the whole repo when scoped files are enough
- Dump full conversations into context files — keep them short and reusable
- Start implementing a large planned feature in a chat that already did research + planning

## Continuity

- At the start of a continuation chat, read `docs/context/current-task.md` if present, then inspect only the referenced files needed for the task.
- Keep the handoff factual: include checks performed, unresolved issues, and the exact next step; do not invent completed work or context-usage percentages.
- Recommend a new chat without creating one automatically. If the user chooses to continue here, respect that choice and keep the handoff current.

## Cari istifadəçi qərarı — 13 noyabr 2026

- Yeganə plan `docs/PLAN.md`-dir. Köhnə planlar silinib; tarix git/backup və review-lardadır. Handoff ikinci plan deyil.
- Canlı yoxlama heç vaxt tələb edilmir, emulator avtomatik açılmır. Yoxlanmamış runtime/render PASS sayılmır və canlı yoxlama merge/tamamlanma gate-i deyil.
- CI yalnız istifadəçi ayrıca istəyəndə, yalnız main-də lokaldır. Fokuslu build/lint/unit və disposable lokal PostgreSQL testləri icazəli adi yoxlamalardır. Avtomatik CI işə salınmır.


15 noyabr 2026 — son istifadəçi qərarı: mobil Bazar daxilində Ərazi tabı və ərazi alış girişi çıxarılır. Son əlavə göstərişlə mobil İnventarın Ərazi tabı da çıxarılır. Saxlanmış server hüquqları dəyişmir; ərazi üçün menyu girişi və yeni alternativ yer yoxdur. Ərazi alışının yeni yeri istifadəçi tərəfindən sonra göstəriləcək; yeni yer uydurulmur. Bu qərar əvvəlki Bazar/Ərazi PNG girişini əvəz edir.
