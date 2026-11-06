# Wobbrik — ümumi plan və tamamlanma siyahısı

Son yenilənmə: **6 noyabr 2026**.

Bu sənəd layihənin ümumi mərhələlərini və hazırkı irəliləyişi göstərir.
`[x]` — göstərilən konkret iş tamamlanıb; `[ ]` — hələ tamamlanmayıb.
Bir alt işin tamamlanması bütün mərhələnin və ya yekun oyunun hazır olması demək deyil.

**Hazırkı yerimiz: mərhələ 3, addım 3.4 — real oyunun onlayn axına qoşulması.**
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

### 3.4. Real oyunun onlayn axına qoşulması — NÖVBƏTİ ADDIM

- [ ] Onlayn simulyasiya və hər enişdən sonra checkpoint göndərmə dövrü qurulsun.
- [ ] Təsdiq gözlənərkən, xətada və bərpada simulyasiya/input təhlükəsiz dayansın.
- [ ] Retry və reconciliation-dan sonra yalnız düzgün qəbul edilmiş vəziyyətdən davam edilsin.
- [ ] Son eniş → checkpoint → yeni Ready → finish ardıcıllığı yekun oyun axınına qoşulsun.
- [ ] Finish üçün kosmetik hissə seçimi kataloq və hesabın açdığı hissələrlə əlaqələndirilsin.
- [ ] Təhlükəsiz axın yoxlanıldıqdan sonra onlayn input açılsın.

**Məhdudiyyət:** hər əmr ayrıca davamlıdır. Checkpoint ilə finish arasındakı niyyət
avtomatik saxlanmır; həmin arada proses bağlanarsa, bərpadan sonra explicit finish
yenidən tələb olunur. Testlərdə server müqaviləsini təqlid edən fake istifadə olunub;
cihazda və canlı serverdə yoxlama aparılmış sayılmır.

## 4. Kolleksiya, hissələr və şəhər — yekun Android axını tamamlanmayıb

- [ ] 10 və daha çox mərtəbədən yaranan binanın kolleksiyada göstərilməsi qoşulsun.
- [ ] 68 hissə və 12 dekorasiya kataloqunun mobil axını tamamlanılsın.
- [ ] İnventar, hissə seçimi və bina redaktəsi serverlə inteqrasiya edilsin.
- [ ] Ərazi alışları, şəhər/inventar keçidləri və sakin göstəriciləri qoşulsun.
- [ ] Yerləşdirmə üçün həndəsə, ölçü və server yoxlama müqaviləsi tamamlansın.
- [ ] Şəhərin təsdiqlənmiş plan və həcmli görünüşləri həyata keçirilsin.

Serverdə mövcud API-lər bu mobil mərhələnin tamamlanması demək deyil.
Yerləşdirmə hazırda bağlıdır; opaque checkpoint-dən etibarlı həndəsə uydurulmur.

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
- [ ] Canlı oyun, performans və buraxılış yoxlamaları keçirilsin.
- [ ] Deploy və mağaza buraxılışı üçün ayrıca göstəriş alınsın.

## Açıq qərarlar

- [ ] Restart/abandon siyasəti: server cəhdini necə bağlayacaq və hansı nəticəni yaradacaq?
- [ ] Continued cəhdin fizikasının stabilləşdirilməsi necə işləyəcək?
- [ ] Hesab konflikti, refund və hesab silmə/retention siyasətləri dəqiqləşdirilsin.

Bu qərarlar özbaşına seçilmir. Onlayn restart/abandon hazırda heç bir əmələ bağlanmayıb.

## Mənbələr və davam

- Ümumi texniki plan: [kotlin-compose-plan.md](kotlin-compose-plan.md), xüsusən 5–7-ci bölmələr.
- Növbəti konkret iş və yoxlamalar: [context/current-task.md](context/current-task.md).
- Auth/lifecycle və əmrlərin müqaviləsi: [host-lifecycle-contract.md](host-lifecycle-contract.md).
- Son inteqrasiya review-u: [durable-attempt-commands](reviews/2026-11-05-durable-attempt-commands.md).

Yeni chat üçün prompt:

> `docs/context/current-task.md` və `docs/project-progress.md` oxu. 3.4-cü addımı —
> AcceptedEngineHost və AcceptedAttemptCommands əsasında onlayn simulyasiya və eniş
> təsdiqi dövrünü qur. Təhlükəsiz axın yoxlanana qədər input kilidli, restart/abandon
> açıq qalsın. Tamamlanan işlərə uyğun checklist-i yenilə.
