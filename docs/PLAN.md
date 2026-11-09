# Wobbrik — vahid plan (PLAN.md)

Son yenilənmə: **8 noyabr 2026**.

**Bu, layihənin yeganə işlək planıdır.** Mərhələ sırası və vəziyyəti yalnız burada saxlanılır.
- Oyun qaydaları, kataloq və balans: [game-plan.md](game-plan.md) (mərhələ siyahısı yoxdur).
- Texniki arxitektura, modullar, API/DB müqaviləsi: [kotlin-compose-plan.md](kotlin-compose-plan.md).
- Köhnə mərhələ bölgüləri və Unity/texnologiya seçimi tarixi: [archive/plan-history.md](archive/plan-history.md).
- Növbəti konkret addım: [context/current-task.md](context/current-task.md).

**Stack:** Android-only, Kotlin + Compose, minSdk 24 / targetSdk 36, Firebase Spark (yalnız Auth, Crashlytics, Remote Config; Blaze/Cloud Functions yoxdur), AppBackend (Go + PostgreSQL) pul və irəliləyişin əsas mənbəyi. Paket: `com.aladdin.wobbrik`.

**Dizayn mənbələri (məcburi):** `docs/design/` — `mobile-v1`, `tablet-v1`, `tablet-rail-icons-v1`, `game-tap-v2`, `game-assets-v1`, `account-assets-v1`, `city-plan.png`, `city-view.png`. Dizayn uydurulmur; çatışmayan dizayn gözlənilir. Bütün ekranlar en və hündürlük üzrə responsivdir: mətn, komponent, boşluq və illüstrasiya ölçüləri ekran ölçüsünə görə pillə ilə dəyişə bilər, əsas əməliyyatlar kəsilmir, yazılarda söz qırılmır (qayda: `AGENTS.md` → Visual work).

**İş qaydası:** hər hissə ayrıca branch → lokal review → Aladdin OK verəndən sonra main-ə merge → lokal CI yalnız main-də. `google-services.json`, `local.properties`, `output/`, log və açarlar commit olunmur. Push/deploy/Console ayrıca göstərişlə.

**8 noyabr merge yeniləməsi:** istifadəçinin ayrıca OK-u ilə `feat/city-footprint-placement` və onun əvvəlki branch tarixçəsi main-ə birləşdirildi (Wobbrik `cad922b`, AppBackend `1eec2ef`). Aşağıdakı “main-də deyil” qeydləri merge-dən əvvəlki tarixi statusdur. Wobbrik lokal CI və backend lokal yoxlamaları keçdi; mərhələ 3/4 qəbulu və mobil placement inteqrasiyası hələ açıqdır. Push/deploy edilməyib.

**Merge-dən əvvəlki yer (8 noyabr):** mərhələ 3 (onlayn) davam edir; paralel olaraq home ekranı dizayna uyğunlaşdırılır (`feat/home-design-alignment`, main-də deyil). Onlayn ekranlar `feat/stage-three-online-progression` / `feat/stage-three-online-screens` branch-lərindədir, main-ə merge olunmayıb.

Bu sənəd layihənin ümumi mərhələlərini və hazırkı irəliləyişi göstərir.
`[x]` — göstərilən konkret iş tamamlanıb; `[ ]` — hələ tamamlanmayıb.
Bir alt işin tamamlanması bütün mərhələnin və ya yekun oyunun hazır olması demək deyil.

**6 noyabr qeydi: mərhələ 3, addım 3.4 — real oyunun onlayn axına qoşulması.**
Ekranda görünən oyun hələ ayrıca offline məşqdir. Onlayn hostun input-u kilidlidir.

## 1. Texniki təməl və müqavilə — əsas hissəsi hazırdır

- [x] Android üçün Kotlin + Compose istiqaməti və modul quruluşu yaradılıb.
- [x] Minimum Android 7.0, hədəf Android 16 müəyyənləşdirilib.
- [x] Firebase Auth kimlik, AppBackend/PostgreSQL isə pul və oyun irəliləyişinin əsas mənbəyi kimi müəyyənləşdirilib.
- [x] Başlatma, checkpoint və finish üçün mövcud server müqaviləsi mobil inteqrasiya ilə tutuşdurulub.
- [x] Branch → lokal review → main-ə merge → lokal CI iş qaydası tətbiq olunub.
- [ ] Faktiki backend ünvanı ilə canlı mobil bağlantı yoxlanılsın.
- [ ] Buraxılış build-i və cihazdakı işləmə ayrıca təsdiqlənsin.

## 2. Əsas tikinti mexanikası — qismən hazırdır

- [x] Lokal mühərrikdə kran, buraxma, düşmə və eniş mexanikası var.
- [x] Tarazlıq, təhlükə və xilasetmə hesablamaları var.
- [x] Fizikanın checkpoint formatında saxlanması və geri bərpası var.
- [x] Lokal məşq səhnəsi və pauza mexanizmi var.
- [ ] Bu mexanika ayrıca məşq axınından yekun onlayn oyuna inteqrasiya edilsin.
- [x] Oyun səhnəsi təsdiqlənmiş qrafika və dizayna yaxınlaşdırılıb (framing, HUD,
  fasilə paneli, guide/cable). Sprite-lar əvvəlki təsdiqdən; yeni garden/sky art
  yoxdur. Capture və fərqlər:
  [compose review](reviews/2026-11-06-construction-cycle-compose.md).
- [ ] Uzun cəhdlər və müxtəlif frame sürətləri üzrə yekun oyun davranışı yoxlanılsın.
- [ ] Zəif və orta cihazlarda FPS, jank, yaddaş və başlanğıc vaxtı ölçülsün.
  Emulator soft-GPU-da FrameTiming harness işləyir; fiziki 60 Hz sübutu açıqdır.

## 3. Onlayn oyun və serverdə irəliləyiş — hazırda üzərində işlənən mərhələ

### 3.1. Hesab və cəhdin bərpası

- [x] Tətbiq səviyyəsində Firebase auth/recovery owner qurulub.
- [x] Hesab və login dövrü dəyişəndə əvvəlki hesabın nəticəsinin tətbiqi bloklanır.
- [x] Serverin qəbul etdiyi cəhd `AcceptedAttemptRestore` ilə yoxlanıb pauzada bərpa olunur.
- [x] Boş, bitmiş, terminal, uyğunsuz və davam edilmiş cəhdlər fərqləndirilir.
- [ ] Real giriş ekranı və hesab naviqasiyası yekun axına qoşulsun.
- [ ] Canlı hesabla giriş/çıxış və yenidən giriş yoxlanılsın.

### 3.2. Mühərrikin tətbiqə bağlanması

- [x] Application-owned `AcceptedEngineHost` qurulub.
- [x] Auth/recovery dəyişəndə əvvəlki fizika sinxron təmizlənir.
- [x] Activity/process pauzası və frame-clock reset qurulub.
- [x] Foreground avtomatik resume etmir; onlayn input kilidli saxlanır.

### 3.3. Etibarlı start/checkpoint/finish əmrləri

- [x] `AcceptedAttemptCommands` eyni recovery coordinator və journal-a qoşulub.
- [x] Əmr şəbəkəyə göndərilməzdən əvvəl davamlı journal-a yazılır.
- [x] Cavab itəndə ilkin operation ID, revision və envelope ilə retry edilir.
- [x] Köhnə Ready/hesab/login, təkrar dispatch və uyğunsuz checkpoint bloklanır.
- [x] Finish yalnız terminal vəziyyət və qəbul edilmiş mərtəbə/sayğaclarla göndərilir.
- [x] Lokal pul/mükafat verilmir; qəbuldan sonra server snapshot-u yenilənir.
- [x] Bu inteqrasiya üçün 11 yeni test əlavə edilib və lokal CI keçib.
- [ ] Eyni davranış real server, şəbəkə kəsilməsi və cihazda prosesin bağlanması ilə yoxlanılsın.

### 3.4. Real oyunun onlayn axına qoşulması — DAVAM EDİR

- [x] Host settled landing-də checkpoint/finish göndərir; UI OnlineGame ilə bağlıdır
  (`feat/stage-three-online-progression`, hələ main-də deyil).
- [x] Təsdiq gözlənərkən (`waiting`), xətada və bərpada input bağlanır.
- [x] Retry və reconciliation-dan sonra yalnız düzgün qəbul edilmiş vəziyyətdən davam — fake testlə yoxlanıb; canlı device/DB yoxlaması istifadəçi qərarı ilə (9 noyabr) tələb olunmur.
- [x] Son eniş → checkpoint → yeni Ready → finish axını: canlı oyun axını yoxlaması istifadəçi qərarı ilə (9 noyabr) tələb olunmur və açıq iş sayılmır.
- [x] Finish kosmetik seçimi versiyalı server kataloqu və cari hesabın unlock-larına bağlandı (`feat/finish-cosmetics`); durable draft və explicit təsdiq var. Vizual qəbul istifadəçidədir (canlı yoxlama tələb olunmur), main-ə merge edilməyib.
- [x] Explicit resume/drop ilə onlayn input açılır; restart/abandon siyasəti `feat/restart-abandon`-da əmələ bağlanıb (merge edilməyib).
- [ ] Restart/abandon siyasəti (istifadəçi qərarı, 9 noyabr) implementasiya olunsun: oyunçu açıq cəhdi oyunun
  ortasında tərk edərsə (Home, Geri və ya tətbiqi bağlamaq) cəhd silinir və saxlanmır — nəticə və bina
  yaranmır, sonradan “Cəhdə qayıt” olmur. Saxlama yalnız iki halda olur: (1) oyunçu uduzur — uduzmadan
  əvvəlki son qəbul edilmiş uğurlu vəziyyət saxlanır; (2) oyunçu oyunu özü bitirir — mövcud axın:
  mərtəbələr tamamlandıqdan sonra kosmetik seçim, açıq təsdiq, sonra finish. Abandon API, refund və
  ya finish-as-abandon icad edilmir; offline məşq restart-ı lokal qalır. Serverdə açıq cəhdi
  bağlayan/silən endpoint yox idi; ona görə server dəyişikliyi lazım oldu.
  Tətbiq: `feat/restart-abandon` (bu repo) + AppBackend `feat/wobbrik-attempt-abandon` (yeni
  `POST /game/attempts/abandon`, migration 017, `status='abandoned'`: nəticə/bina/pul/XP yazılmır,
  refund/clawback yoxdur). Merge, push və deploy təsdiq gözləyir. Ekranlara qayıtmaq (Geri, “Menyuya
  qayıt”) açıq cəhdi silir; uduzulmuş cəhd (kosmetik seçim) saxlanır və yalnız o “Cəhdə qayıt”
  olaraq qalır. Tətbiq bağlanıbsa, köhnə açıq cəhd açılışda bərpa edilmir; “Oyna” onu əvvəlcə silir,
  sonra yeni cəhd başladır.
- [ ] Google guest link UI yazılıb; live Console web client + provider yoxlaması açıqdır.
- [ ] Room yalnız hesab üzrə server snapshot keşi kimi qoşulsun (plan vahid 3). 9 noyabr qərarı (B):
  gözləyən əmrlər üçün yeganə outbox davamlı fayl journal-ı (`DurableJournalFile`) qalır; Room
  ikinci journal/outbox və wallet authority deyil, journal Room-a köçürülmür.
  Status (9 noyabr): `feat/room-cache`-də implementasiya edildi və lokal yoxlandı; merge gözləyir.

**Məhdudiyyət:** checkpoint və finish ayrıca journal əmrləridir. Terminal server
checkpoint-i və hesab/cəhd üzrə lokal kosmetik draft seçim niyyətini bərpa edir;
mərtəbə əlavə etməyən miss də qəbul edilmiş progress ilə tutuşdurulur. Finish
avtomatik göndərilmir: açıq təsdiq tələb edir. Pending envelope əvvəlki ID/revision
ilə dəyişmədən drain olunur. Yeni draft server snapshot-u və ya ikinci outbox deyil.
Fake regressiyalar və izolə renderlər canlı server/cihaz sübutu sayılmır.
9 noyabr AtomicFile crash matrisi (`test/atomicfile-crash-matrix`, main-də deyil): API 24–29 və
30+ AtomicFile modelləri üzərində 258 JVM ssenarisi köhnə/yeni vəziyyəti dəqiq bərpa etdi;
AtomicFile-ın yalnız log etdiyi commit xətası üçün read-back düzəlişi edildi
([review](reviews/2026-11-09-atomicfile-crash-matrix.md)). API 24–29 ilk yazı boşluğu
`fix/legacy-atomicfile-first-write`-də (main-də deyil) bütün API-lərdə temp+fsync+rename+qovluq
fsync ilə bağlandı; köhnə `.bak`/`.new` qalıqları oxunur. JVM-də 250 durable + 258 upgrade ssenarisi,
0 “göndərilməyib, amma bloklanıb” halı. Cihaz durability sübutu deyil
([review](reviews/2026-11-09-legacy-atomicfile-first-write.md)).

8 noyabr disposable APK yoxlaması: `e0af061` START journal-ı APK yenilənməsində
(versionCode 1→2) eyni envelope ilə dedup bərpa edildi; uyğunsuz storage/rules/engine
startup və retry-də baytları qorudu, sorğu göndərmədi. Ümumi “Bağlantı yoxdur”
mesajı əvəzinə təsdiqlənmiş “Cəhd bu versiyada bərpa edilmir” xəta/bərpa ekranı
(`fix/incompatible-journal-ux`, main-də deyil) debug fixture renderləri ilə yoxlanıb;
canlı Android journal bərpası ilə bu UI yoxlanmayıb ([review](reviews/2026-11-08-incompatible-journal-ux.md)).
Bu, bütün §3.4-ün və köhnə checkpoint/finish-in qəbulu deyil.
Sübut və sərhədlər: [APK journal review](reviews/2026-11-08-apk-journal-upgrade.md).

## 4. Kolleksiya, hissələr və şəhər — yekun Android axını tamamlanmayıb

- [ ] 10 və daha çox mərtəbədən yaranan binanın kolleksiyada göstərilməsi qoşulsun.
- [ ] 68 hissə və 12 dekorasiya kataloqunun mobil axını tamamlanılsın.
- [ ] İnventar, hissə seçimi və bina redaktəsi serverlə inteqrasiya edilsin.
- [ ] Ərazi alışları, şəhər/inventar keçidləri və sakin göstəriciləri qoşulsun.
- [ ] Yerləşdirmə üçün həndəsə, ölçü və server yoxlama müqaviləsi tamamlansın.
- [x] İlk pure həndəsə nüvəsi: integer tick, bina proyeksiyası, quarter-turn, pairwise boşluq və torpaq union containment (`AppBackend` branch `feat/city-geometry-kernel`, `6e949be`; API-yə qoşulmayıb və main-də deyil).
- [x] Typed geometry/provenance müqaviləsi: yeni cəhd opt-in, engine→tick precision, server checkpoint qəbulu/finish persistence və Android wire/recovery uyğunluğu (`feat/typed-geometry-provenance`, hər iki repo; main-də deyil). Placement bağlı qalır.
- [x] Torpaq və 12 dekorun ilkin footprint parametrləri istifadəçi tərəfindən təsdiqləndi (8 noyabr; dəqiq konturlar `reviews/2026-11-08-city-footprint-proposal.md`, texniki müqavilə `kotlin-compose-plan.md`). Server/mobile implementasiyası və placement aktivləşməsi deyil.
- [x] Backend footprint/copy placement vahidi: ayrıca alınmış torpaq hüququndan pulsuz atomik parcel və bina/dekor place/move/replace/return; migration 012, server union/clearance yoxlaması və read/HTTP müqaviləsi (`feat/city-footprint-placement`, main-də deyil). Bu qeyd mobil şəhər axınının və mərhələ 3/4-ün qəbulu deyil.
- [ ] Şəhərin təsdiqlənmiş plan və həcmli görünüşləri həyata keçirilsin.

### Şəhərdə binanın görünüşü — vizual siyasət (istifadəçi qərarı, 9 noyabr 2026)

Bu, qərarlaşdırılmış siyasətdir; kodda hələ tətbiq edilməyib. İmplementasiyası 4-cü mərhələnin (şəhər
planı/həcmli görünüşlər və yerləşdirmə UI) bir hissəsidir; yuxarıdakı 4-cü mərhələ bəndləri bu qeydlə
tamamlanmış sayılmır.

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
   üçün illüstrasiyadır: rəsmi təsdiqlənmiş dizayn mənbəyi deyil, `docs/design/`-ə əlavə edilmir və
   `city-plan.png`/`city-view.png`-ni əvəz etmir. Təsdiqlənmiş şəhər vizual referansları
   `docs/design/city-plan.png` və `docs/design/city-view.png` olaraq qalır. İmplementasiya istifadəçinin
   4-cü mərhələ üçün çatışmayan dizaynları və ölçüləndikdə dağılmayan yüksək keyfiyyətli rasterləri
   hazırlamasını gözləyir. Eyni dizaynın 3–5 ölçü variantı istənilmir.
6. **Texnologiya.** 3D mühərrik hələ təsdiqlənməyib; kamera döndürmənin texniki realizasiyası hələ
   yoxlanmayıb (mövcud qeydlər qüvvədədir). Burada yalnız vizual tələb qeyd olunur; mühərrik seçilmir.

Serverdə mövcud API-lər bu mobil mərhələnin tamamlanması demək deyil.
Backend main-də yerləşdirmə typed geometry ilə aktivdir; mobil placement axını hələ inteqrasiya edilməyib. Opaque checkpoint-dən etibarlı həndəsə uydurulmur.

## 5. Bazar və rekordlar — yekun Android axını tamamlanmayıb

- [ ] Elan yaratmaq, geri götürmək, baxmaq və almaq mobil axını qoşulsun.
- [ ] Serverin revision/dedup və vahid wallet qaydaları mobil axında qorunsun.
- [ ] Ümumi və həftəlik leaderboard mobil interfeysə qoşulsun.
- [ ] Paralel alış, köhnə elan və retry davranışı uçdan-uca yoxlanılsın.

## 6. Reklam və real alış — yekun məhsul axını tamamlanmayıb

- [ ] Consent və test reklamları yekun oyuna qoşulsun.
- [ ] Reklamla davam etmənin fizikanı necə bərpa edəcəyi dəqiqləşdirilib inteqrasiya edilsin.
- [ ] Reklamla hissə açılması və reklam göstərmə qaydaları tamamlanılsın.
- [ ] Mövcud billing təməli yekun mağaza/hesab axınına bağlansın.
- [ ] Pending, restore, interruption və hesab dəyişməsi sandbox cihazda yoxlanılsın.
- [ ] Real SKU, Console və production alışları üçün ayrıca istifadəçi göstərişi alınsın.

## 7. Vizual tamamlama və buraxılış — tamamlanmayıb

- [ ] Bütün ekranlar təsdiqlənmiş dizaynlara uyğun həyata keçirilsin.
- [ ] Real menyu, naviqasiya, nəticə və bərpa ekranları tamamlansın.
- [ ] Telefon və planşet renderləri dizaynla yanaşı müqayisə edilsin.
- [ ] Portret, landşaft, dəyişən pəncərə və şrift miqyası yoxlanılsın.
- [ ] Səs, haptic və əlçatanlıq tamamlanılsın.
- [ ] Performans və buraxılış yoxlamaları keçirilsin (canlı oyun axını yoxlaması istifadəçi qərarı ilə, 9 noyabr, tələb olunmur).
- [ ] Deploy və mağaza buraxılışı üçün ayrıca göstəriş alınsın.

## Açıq qərarlar

- [x] Restart/abandon siyasəti (9 noyabr, istifadəçi qərarı): oyunun ortasında tərk edilən açıq cəhd
  (Home, Geri, tətbiqi bağlamaq) silinir — nəticə/bina yox, sonradan “Cəhdə qayıt” yox. Saxlama yalnız
  uduzanda (uduzmadan əvvəlki son qəbul edilmiş uğurlu vəziyyət) və oyunçu oyunu özü bitirəndə
  (kosmetik seçim + açıq təsdiq + finish) olur. Abandon API, refund, finish-as-abandon icad edilmir;
  offline məşq restart-ı lokaldır. İmplementasiya §3.4-dədir (`feat/restart-abandon`, merge gözləyir).
- [x] Şəhərdə binanın görünüşü (9 noyabr, istifadəçi qərarı): yalnız şəhərdə yerləşdirmə və baxış üçün
  bina oyunda real tikilən əyriliyə/meylə/ofsetlərə çox böyük ölçüdə bənzəyir (forma oyun həndəsəsindən
  gəlir, düz qutuya çevrilmir), dolğun həcmli dizayn edilmiş UI binası kimi görünür (pəncərə, dam,
  divar) və hissələr/örtüklər real formanın üzərinə maketə paltar kimi geyindirilir. Canlı oyun
  görünüşü dəyişmir. Təsdiqlənmiş referanslar `city-plan.png`/`city-view.png` qalır; çat maketləri
  dizayn mənbəyi deyil; 3D mühərrik seçilmir. Ətraflı: §4 “Şəhərdə binanın görünüşü”. İmplementasiya
  4-cü mərhələdədir, dizayn/rasterləri gözləyir.
- [ ] Continued cəhdin fizikasının stabilləşdirilməsi necə işləyəcək?
- [ ] Hesab konflikti, refund və hesab silmə/retention siyasətləri dəqiqləşdirilsin.

Bu qərarlar özbaşına seçilmir. Restart/abandon siyasəti qərarlaşdırılıb və `feat/restart-abandon`-da əmələ bağlanıb (main-ə merge edilməyib).

## Mənbələr və davam

- Ümumi texniki plan: [kotlin-compose-plan.md](kotlin-compose-plan.md), xüsusən 5–7-ci bölmələr.
- Növbəti konkret iş və yoxlamalar: [context/current-task.md](context/current-task.md).
- Auth/lifecycle və əmrlərin müqaviləsi: [host-lifecycle-contract.md](host-lifecycle-contract.md).
- Son inteqrasiya review-u: [durable-attempt-commands](reviews/2026-11-05-durable-attempt-commands.md).

Yeni chat üçün prompt:

> `docs/context/current-task.md` və `docs/PLAN.md` oxu. 3.4-cü addımı —
> AcceptedEngineHost və AcceptedAttemptCommands əsasında onlayn simulyasiya və eniş
> təsdiqi dövrünü qur. Təhlükəsiz axın yoxlanana qədər input kilidli; restart/abandon yalnız
> 9 noyabr siyasətinə uyğun (tərk = cəhd silinir; saxlama yalnız uduzanda və öz finish-də). Tamamlanan işlərə uyğun checklist-i yenilə.
