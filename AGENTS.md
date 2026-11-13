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
5. Telefon/planşet, pəncərə/orientasiya, böyük şrift, inset/klaviatura, əlçatanlıq və tələb olunan lokalları yoxla; zəruri scroll-u saxla. Təsdiqlənməmiş dil siyahısını uydurma.
6. Vizual vahiddə real build renderlərini dizaynla yanaşı müqayisə et; nəticə və məhdudiyyətləri `docs/reviews/` altında yaz. Tam mərhələ qəbulu üçün ayrıca `docs/reviews/<phase>/acceptance.md` istifadə oluna bilər; mövcud review-ları köçürmə.
7. Dizayn nümunəsinin OK-u mərhələ qəbulu deyil; mərhələ qəbulu növbəti mərhələyə başlama icazəsi deyil. Növbəti mərhələ ayrıca istifadəçi tapşırığı ilə başlayır.

## Code organization

- Yeni koddan əvvəl mövcud paket/qovluq konvensiyalarını yoxla və uyğun olanı təkrar istifadə et.
- Feature və məsuliyyət üzrə qruplaşdır; hər fayl/sinfin aydın işi olsun.
- UI, state/orchestration, domain qaydaları və data access ayrılır. Domain qaydalarını UI/persistence-də təkrarlama.
- Feature helper-ləri feature daxilində saxla; yalnız aydın ortaq məqsədlə paylaş. Modul sərhədində uyğun interfeys seç; dairəvi asılılıq yaratma.
- Boş layer/framework və vaxtından əvvəl abstraksiya yaratma. Bitirməzdən əvvəl yerləşmə, dublikat və importları yoxla; uyğun build/test nəticələrini dürüst qeyd et.
- Dirty owner işini səssiz stash/reset/force ilə məhv etmə. Diagnostic ekran real domain/repository çağırmalıdır; demo/synthetic adapter açıq işarələnməlidir.

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

- **13 noyabr 2026 — mobil əvvəl:** mobil oyun və ekranlar tam bitmədən planşet dizaynına keçilmir. Hər mobil vahid üçün ayrıca planşet dizaynı/nümunəsi hazırlanmaz; planşet dizaynı və ona aid ayrıca implementasiya/render işi mobil tamamlandıqdan sonra istifadəçinin ayrıca tapşırığı ilə başlayır. Mövcud təsdiqlənmiş planşet görünüşləri qorunur. Bu ardıcıllıq əvvəlki hər vizual vahiddə telefon+planşet işi tələbindən üstündür; mobil yalnız portret rejimindədir. Mobil üçün landşaft dizaynı/implementasiyası tələb edilmir; əvvəlki telefon landşaftı yoxlama tələblərini bu qərar əvəz edir. Müxtəlif portret ölçüləri, böyük şrift, inset və əlçatanlıq uyğunlaşdırması qüvvədə qalır. Mobil portret kilidi app manifestində `MainActivity` üçün `screenOrientation="portrait"` ilə tətbiq edilir.

- UI hazırlayanda implementasiyaya başlamazdan əvvəl tətbiq ediləcək dizaynı/vizual nümunəni istifadəçiyə göstər və açıq təsdiqini al; təsdiq gələnədək UI implementasiyasına başlama. Mövcud təsdiqlənmiş dizayn varsa, həmin dizaynı təqdim et, yeni alternativ uydurma. Ümumi işə başlama icazəsini bu konkret UI təsdiqinin əvəzi sayma.
- Təsdiqlənmiş hazır dizaynlar implementasiya üçün məcburi vizual əsasdır və birəbir həyata keçirilməlidir: kompozisiya, rənglər, tipoqrafiya, boşluqlar, ölçü nisbətləri, formalar, ikonlar, illüstrasiyalar və oyun qrafikası qorunmalıdır. Əsas istinadlar `docs/design/mobile-v1/` və `docs/design/game-tap-v2/` altındakı cari təsdiqlənmiş dizaynlardır; eyni ekran üçün son təsdiqlənmiş versiya əsas götürülür.
- Hazır qrafikanı özbaşına sxematik fiqurlar, placeholder və ya sadələşdirilmiş alternativlərlə əvəz etmək, ekranı yenidən dizayn etmək olmaz. Lazımi asset çatışmırsa və ya texniki məhdudiyyət birəbir uyğunluğa mane olursa, bunu açıq bildir və alternativi tətbiq etməzdən əvvəl istifadəçinin razılığını al; belə nəticəni tamamlanmış dizayn kimi təqdim etmə.
- Yalnız pəncərə ölçüsü, orientasiya və şrift miqyası üçün zəruri responsiv/adaptiv uyğunlaşdırmalara icazə verilir; dizaynın vizual dili və elementlərin iyerarxiyası saxlanmalıdır. Bu zərurətdən kənar hər vizual dəyişiklik istifadəçi ilə əvvəlcədən razılaşdırılmalıdır.
- Vizual yoxlamada telefon və planşet renderlərini müvafiq təsdiqlənmiş dizaynlarla yanaşı müqayisə et; fərqləri və razılaşdırılmış istisnaları review qeydində göstər. Sadəcə build-in keçməsi və ya screenshot-un mövcudluğu dizayna uyğunluğun sübutu deyil.
- Bütün ekran və oyun interfeysləri responsiv/adaptiv olmalıdır: həm telefonda, həm də planşetdə rahat və düzgün işləməlidir.
- Layout cihaz adına deyil, mövcud pəncərə ölçüsünə uyğunlaşmalıdır; portret, landşaft və ölçüsü dəyişən pəncərələr nəzərə alınmalıdır. Məzmun kəsilməməli, idarəetmələr üst-üstə düşməməli və əlçatmaz qalmamalıdır.
- **Responsivlik qaydası (Aladdin, 9 noyabr 2026):** layout pəncərənin həm eni, həm də hündürlüyü üzrə ölçü sinfinə uyğunlaşır (`GameTokens`: `MediumWidth`, `CompactHeight` və s.); telefon landşaftı qısa hündürlük kimi ayrıca nəzərə alınır və eni geniş olduğu üçün avtomatik planşet layoutuna düşmür.
  - Mətn, komponent (düymə, kart, ikon, rail elementi), boşluq və illüstrasiya ölçüləri bütün ekranlarda məcburi eyni olmamalıdır: kiçik/qısa ekranda kiçilə, böyük ekranda böyüyə bilər. Ölçülər token pillələri ilə (məs. compact/regular/expanded) seçilir, təsadüfi rəqəm yazılmır; vizual dil, rənglər, iyerarxiya və təsdiqlənmiş kompozisiyanın nisbətləri qorunur.
  - Əsas əməliyyatlar (Oyna, Ana ekran, naviqasiya elementləri və s.) heç bir ölçüdə kəsilməməli, ekrandan kənara düşməməli və yalnız scroll ilə əlçatan qalmamalıdır; yer çatmayanda əvvəlcə ölçü/boşluq kiçildilir, sonra ikinci dərəcəli məzmun yığcamlaşdırılır, scroll ən son ehtiyatdır.
  - Düymə, tab və menyu yazılarında söz qırılmamalıdır (sözün ortasından sətirə keçid olmaz); lazım gələrsə komponent genişləndirilir, alt-alta düzülür və ya mətn ölçüsü pillə ilə kiçildilir.
  - İstifadəçinin artıq təsdiqlədiyi ölçü/orientasiya üçün layout başqa ölçünü düzəldərkən dəyişdirilmir; düzəliş yalnız problemli ölçü sinfinə tətbiq olunur və təsdiqlənmiş halların dəyişmədiyi renderlə yoxlanır.
  - Vizual iş tamamlanmış sayılmazdan əvvəl ən azı bu hallarda render edilib baxılır: kiçik telefon (≈640×360dp landşaft / 360×640dp portret), normal telefon portret və landşaft, planşet portret və landşaft, şrift miqyası 1.0 və 2.0 (ən azı telefon landşaftında). Yoxlanmamış hal review qeydində açıq yazılır.
- Telefon dizaynını sadəcə böyütmək planşet üçün kifayət etmirsə, planşetə uyğun ayrıca kompozisiya/dizayn hazırla (məsələn, iki panel və ya uyğun naviqasiya); mövcud dizayn tokenlərini saxla.
- Vizual işi tamamlanmış saymazdan əvvəl telefon və planşet ölçülərində render edib görüntülərə bax; yoxlanmış ölçüləri və yoxlanmamış halları qeyd et.

When a task touches how something looks, use the `ui-craft` skill — it carries
the token, contrast and screenshot workflow. Two things hold regardless of it:

- **Find the token file before writing a value.** Inventing a colour, spacing or
  radius next to a palette that already names one is the most common way an
  interface drifts, and it never looks broken enough to get caught in review.
- **Don't report a visual change as working without having rendered it and
  looked at the image.** If rendering wasn't possible, say that plainly instead
  of implying the result was checked.

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
