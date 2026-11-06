# Mərhələ 3 / vahid 2 — ekran bağlantısı, açıq review

Branch: əvvəl `feat/stage-three-online-screens`; 6 noyabr 2026-da main +
construction-cycle üzərinə `feat/stage-three-online-progression` kimi gətirilib.
Bu branch yekun deyil, main-ə merge edilməyib; `./scripts/local-ci.sh`
işlədilməyib. Dizayn uyğunluğu və live Google/Console yoxlaması bağlanmadan
vahid 2 tamamlanmış sayılmır.

## Yoxlanmış diff

- Artwork commit: `118e0ee`; hazır şəhər/logo/hesab hero resurslara köçürülüb,
  istifadəçinin əlavə təsdiqi ilə yeni UI ikonları və recovery hero hazırlanıb.
- Ekran/host commit-i: `b8c9d31`; yoxlanmış diff: MainActivity, OnlineGame, GameTokens,
  AcceptedEngineHost, AcceptedAttemptCommandsTest, AccountPreviewActivity,
  debug manifest və capture-online-screens.py.
- Google guest link (7 noyabr 2026): `GoogleGuestLink.kt`, OnlineGame hesab
  ekranı binding, `linkGuest` collision/epoch qorunması, `wobbrik.googleWebClientId`
  BuildConfig, credentials/googleid asılılıqları, README.
- Əlaqəsiz untracked responsive-rule və compose-game-host faylları qorunub.

## İmplementasiya və düzəldilmiş tapıntılar

- MainActivity artıq PracticeSession/PracticeEngine yaratmır; OnlineGame
  application-owned AcceptedEngineHost-a bağlıdır. Offline PracticeGame yalnız
  köhnə debug fixture üçün qalıb; onun checkpoint-i online command-a ötürülmür.
- Frame/drop/resume canonical exact Ready instansı ilə çağırılır. Hər frame
  recovery.state.value oxunur, referentialEqualityPolicy ilə saxlanır:
  qısa Recovering keçidi conflation-da itəndə bərabər snapshot-lı yeni auth
  epoch köhnə UI Ready capability-sini saxlamamalıdır. Host stale input-u
  sinxron şəkildə rədd edir; UI heç vaxt öz Ready nüsxəsini yaratmır.
- İstifadəçi fasiləsi foreground-u saxtalaşdırmadan host.pause ilə işləyir;
  frame saatı sıfırlanır, drop bağlanır. Kamera host-un real engine.cameraX
  dəyərindən götürülür (köhnə rules üçün sabit origin qorunur).
- Start və aktiv cəhdə qayıtma açıq əməliyyatlardır; checkpoint-dən sonrakı
  Ready explicit resume tələb edir. Restart/abandon binding yoxdur.
- Auth Loading/Session/Error backend Ready deyil. Bərpa və xəta ekranı,
  auth retry/recovery retry, konfiqurasiya çatışmazlığı görünür. Balans/XP/level
  və nailiyyət/unlock sayı yalnız cari server snapshot-dandır; lokal grant,
  ikinci wallet və session-only rekord yoxdur. Backend ID-ləri UI-yə çıxmır.
- Qısa landşaft + 1.5 şriftdə başlıq kəsilməsi görüldü; qısa landşaft da
  iki panelə keçir, controls scroll olur. Geniş pəncərədə 2:1 nisbət qalır.
- Error vəziyyətində artıq oyun/kolleksiya düymələri və boş progression
  kartı çıxarıldı; recovery əsas əməl oldu. Konfiqurasiya xətası sonsuz
  loading spinner kimi göstərilmir.
- Main palette dəyişmir. ExpandedWidth=840dp tablet-v1 sənədindən portdur;
  başqa rəng/radius/spacing palitrası yaradılmayıb.

## Yoxlamalar

- Son source diff-dən sonra `:app:assembleDebug :engine:test --offline`
  BUILD SUCCESSFUL, 6s; engine 45 test, 0 failure/error.
- Host-un mövcud online flow testinə user pause, foreground-un saxlanması,
  clock reset, pause-dan sonra drop-un rəddi və explicit resume yoxlaması əlavədir.
- `git diff --check` keçdi; staged diff-check commit-dən əvvəl yenilənir.
- ui-craft kontrastı: Text/Background 16.57, Muted/Surface 8.22,
  Background/Accent 11.45. Hamısı 4.5 həddindən yuxarıdır.
- Debug fixture server formasında sabit 1250 coins / 580 XP / level 7 verir;
  bu real hesab və ya Firebase/backend inteqrasiyasının sübutu deyil.
- Render aləti: `python3 scripts/capture-online-screens.py`; orijinal wm size,
  density və font_scale bərpa edilir (null font setting delete ilə bərpa olunur).
- İlk emulator seriyasında uiautomator exit 137, sonra hardware ColorBuffer
  xətaları/boş starting window görüldü; bunlar keçmiş yoxlama sayılmır.
  Son render vəziyyəti aşağıda ayrıca qeyd edilir.

## Açıq tapıntılar / merge sərhədi

- **Vahid 2 TAMAM DEYİL.** Google guest link UI yazılıb: Credential Manager +
  `linkWithCredential` (eyni UID); collision/cancel-da sign-in yoxdur; linkdən
  sonra recovery epoch yenilənir. Live sübut yoxdur: `google-services.json`-də
  oauth_client/default_web_client_id yoxdur; Console web client + Google
  provider ayrıca icazə tələb edir; bu işdə Console dəyişdirilməyib.
- Təsdiqlənmiş mobile-primary-v2, account-states və tablet primary/account
  lövhələri ilə müqayisədə tipoqrafiya, dekor, düymə forması və kompozisiya
  fərqləri qalır. Tablet naviqasiya rail-i və hesabın tam actions quruluşu
  yoxdur. Bu fərqlər təsdiqlənmiş istisna deyil; ekran birəbir hazır deyil.
- Kolleksiya/nailiyyət səhifələri yalnız server unlock/achievement sayının
  xülasəsidir, tam təsdiqlənmiş kolleksiya/nailiyyət ekranları deyil.
  Rekord/XP növbəti həddi server snapshot-da olmadığından uydurulmayıb.
- Generativ qrafika referans piksellərinin extraction-u deyil. Yeni promptlar
  `docs/design/account-assets-v1/ui-icons/README.md`-dədir. Crown istifadə
  olunmur və Android resurslarından kənarda saxlanır.
- Real Firebase Google link, canlı gameplay/PostgreSQL, iki cihaz,
  proses ölümü və accessibility interaction sınaqları aparılmayıb.
  Screenshot mövcudluğu online axının işlədiyinin sübutu deyil.
- AppBackend `docs/wobbrik-game-api.md` yenidən oxundu: anonymous gameplay,
  start/checkpoint/finish/sync, revision/dedup və wallet authority dəyişmir.
  Bu UI bağlantısına API/DTO/schema/migration dəyişikliyi lazım deyil.
  Server structural-v1 hələ fizika replay verifier deyil.
- Room/outbox və real fault proof planın ayrıca vahid 3/4 işidir.

## Son render / handoff

- Source commit `b8c9d31`-in APK-sı software SwiftShader emulatorunda yenidən
  render edildi; aşağıdakı 9 fixture şəklinin hər birinə baxıldı:
  phone-home/account/error/game (1080×2400, 420dpi), tablet-home/account/pause
  (1280×800, 160dpi), tablet-portrait (800×1280, 160dpi), large-font
  (900×450, 240dpi, font_scale=1.5). Fayllar `docs/reviews/online-screens/`-dədir.
- Yeni large-font renderində Hesab başlığı artıq kəsilmir; hero və controls
  iki paneldədir. Controls scroll viewport-unda aşağı məzmun ilk kadrda görünmür;
  screenshot-un özü bütün düymələrin toxunma əlçatanlığını sübut etmir.
- MainActivity özü də açılıb, `main-configuration.png` çəkilib və görüntüyə
  baxılıb: offline məşq əvəzinə server ünvanının konfiqurasiya edilmədiyi
  ekran görünür. Bu APK-da backend origin yoxdur; endpoint uydurulmayıb.
  Konfiqurasiya çatışmazlığında Yenilə konfiqurasiya yarada bilmir; ayrıca
  configuration-vs-retry təqdimatı da açıq UI tapıntısıdır.
- Əlavə `large-font-scrolled.png`: uiautomator-da 1250 fixture marker-i
  yoxlandıqdan sonra sağ panel swipe edildi, aşağı Kolleksiya/Nailiyyətlər
  və Ana ekran düymələrinin göründüyünə baxıldı. Uzun etiketlər 1.5 şriftdə
  sözün içindən qırıla bilir; typography polish açıqdır. Bu fixture callback-ləri
  command göndərmir, canlı toxunma/online sınağı deyil.
- wm size/density original physical 1080×2400 / 420dpi-yə qaytarılıb;
  font_scale default 1.0-dır. Software emulator test üçün açıq saxlanılıb.
- Son renderlərdə hardware starting-window/ColorBuffer nasazlığı təkrarlanmadı.
  Error hero-nun böyük halosunun təmizləndiyi token fonunda görünür.
- mobile-primary-v2/account-states və tablet primary/account referansları ilə
  birbaşa vizual müqayisə aparıldı: yuxarıda sadalanan tipoqrafiya, rail,
  actions və kompozisiya fərqləri qalır; vizual uyğunluq keçmiş sayılmır.
- Source build/45 engine testi keçib; main merge və main lokal CI yoxdur.
  Branch tamamlanmadığı üçün bu nəticə final oyun və ya vahid 2 tamamlanması deyil.
