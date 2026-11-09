# Mərhələ 3 — onlayn cəhd və progression

5 noyabr 2026: istifadəçi mərhələ 3-ə başlamağa icazə verdi. Bu icra planıdır;
mərhələnin tamamlandığını bildirmir.

## Yoxlanmış vəziyyət

- AttemptCoordinator: account/login epoch, serial mutasiya, şəbəkədən əvvəl
  davamlı command, exact retry və receipt-dən sonra sync var.
- AuthenticatedRecoveryOwner: auth/foreground recovery və exact Ready instance
  ilə dispatch var. RecoveryDtos server XP, sikkə və nailiyyətləri oxuyur.
- AndroidAttemptJournal no-backup qovluqda `DurableJournalFile` (temp + fsync + rename) istifadə edir; 9 noyabrdan bu yeganə outbox-dur, Room deyil.
- AcceptedAttemptCommands start/checkpoint/terminal finish göndərə bilir.
- AcceptedEngineHost server checkpoint-i paused bərpa edir, frame/drop/resume bağlıdır.
- MainActivity ayrıca offline PracticeEngine göstərir. WobbrikApplication Firebase
  listener-i mövcud hesabı izləyir; hesab yaratma/giriş axını yoxdur.

## İmplementasiya sırası

1. **Online engine idarəsi, vizual dəyişikliksiz.** Exact Ready və foreground ilə
   explicit resume/frame/drop bağla. Settled landing-də simulyasiyanı dayandır,
   mövcud command vasitəsilə checkpoint göndər, yeni server Ready-dən bərpa et.
   Recovery/error/auth dəyişməsində input dərhal bağlansın. Offline engine-dən
   online proposal götürmə. Terminal əlavə mərtəbə checkpoint-indən sonra finish
   niyyətinin proses ölümündə bərpasını həll et; iki command atomik deyil.
2. **Hesab və ekran bağlantısı.** ui-craft və təsdiqlənmiş dizaynları oxu.
   Firebase anonymous giriş və mövcud hesab sessiyasını bootstrap-a bağla.
   Token alınmasını backend sync uğuru sayma. Ready/Recovering/Error, explicit
   start/resume və server progression göstər. Aktiv cəhdi restart ilə silmə:
   abandon müqaviləsi yoxdur. Guest link UID-ni saxlasın; hesab balansları toplanmasın.
3. **Room keşi.** 9 noyabr qərarı (B): exact command outbox davamlı fayl journal-ı
   (`DurableJournalFile`) olaraq qalır və Room-a köçürülmür; migration tələb olunmur.
   Room yalnız hesab (app + uid) üzrə qəbul edilmiş server snapshot-larının oxu keşidir:
   ikinci journal/outbox yaratma, keşdən əmr/pul/mülkiyyət qərarı vermə. Room wallet
   authority deyil; keş yalnız server cavabı ilə yazılır və hesab/login dəyişəndə
   həmin hesabdan kənarda göstərilmir. Eyni revision-da yeni server balansını da qəbul
   et: billing domain revision-u artırmır.
   Təsdiqlənmiş pinlər (9 noyabr): Room 2.7.2, KSP 2.1.10-1.0.31. DAO testləri izolə
   emulyatorda instrumented işləyir (5554/5582 yox). Keş dəyərləri canlı dəyər kimi
   göstərilir; yeni UI elementi/mətni yoxdur. Oyun/əmr yalnız canlı server Ready ilə açılır.
   Status (9 noyabr): `feat/room-cache` — review `docs/reviews/2026-11-09-room-snapshot-cache.md`; merge gözləyir.
4. **İnteqrasiya sübutu.** Cavab itkisi, commit sonrası timeout, proses ölümü,
   şəbəkə fasiləsi, login epoch və iki cihaz revision yarışı yoxlanılsın.
   XP/sikkə/nailiyyət təkrarlanmasın; restore son server checkpoint-i olsun.
   Real PostgreSQL/device sınağını fake testlərdən ayır.

Hər vahid ayrıca branch/review/merge ilə bitir. CI yalnız main-də merge-dən sonra
`./scripts/local-ci.sh` ilə işləyir. Backend dəyişərsə ayrıca repository qaydaları
və DB yoxlamaları tətbiq edilir. Push/deploy/Console/real SKU daxil deyil.

## Server sərhədi və tamamlanma

AppBackend/docs/wobbrik-game-api.md start/checkpoint/finish, sync və receipt
müqavilələrini artıq verir. Bu plan üçün API/schema dəyişikliyi tələb olunmadı;
kod vahidində uyğunluq yenidən yoxlanmalıdır. Structural-v1 fizika/terminal failure
sübutu deyil; saxta uyğun checkpoint riski qalır.

Auth, real online host, server progression, davamlı bərpa və nasazlıq sübutu
birlikdə işləmədən mərhələ 3 tamam deyil. Offline menyu/session-only rekord və
dizayn uyğunluğu borcu açıqdır. UI telefon/planşetdə təsdiqlənmiş dizaynlarla
yanaşı müqayisə edilməlidir.

## 5–6 noyabr — vahid 2 ekran branch-i (açıq)

`feat/stage-three-online-progression` (əvvəlki `feat/stage-three-online-screens`
main + construction-cycle üzərinə gətirilib): MainActivity online host-a
bağlanıb; hazır art Android resurslarına keçirilib. Branch hələ main-də deyil
və vahid 2 bağlanmayıb. Review: `docs/reviews/2026-11-05-online-screens.md`.
Google hesabı ilə qonağı eyni UID-də link etmək seçilib; credential UI/binding
və təsdiqlənmiş dizaynla qalan uyğunluq işləri növbəti addımdır. Konfiqurasiya,
fixture renderi və fake engine testləri real auth/backend sübutu sayılmır.
Room keşi və fault proof ayrıca vahid 3/4-dür (outbox fayl journal-ında qalır, 9 noyabr qərarı B).
