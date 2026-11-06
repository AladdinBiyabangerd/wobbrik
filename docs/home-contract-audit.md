# Home çatışmazlıqları — müqavilə auditi, 7 noyabr 2026

## Status və scope

Bu vahid kod/müqavilə yoxlamasıdır; beş çatışmazlığın implementasiyası deyil.
Wobbrik online işi `feat/stage-three-online-progression` branch-ində qorunur.
AppBackend `main` oxunub, dəyişdirilməyib. Backend dəyişikliyi tələb olunur:
şəxsi rekord və XP intervalı hazırda sync müqaviləsində yoxdur.

## 1. Rekord

- Mənbə AppBackend `internal/wobbrik/finish.go`: hər qəbul edilmiş finish üçün
  `wobbrik_results` yaranır, bina isə yalnız >=10 mərtəbədə yaranır.
- `internal/wobbrik/leaderboard.go` və `docs/wobbrik-game-api.md` bütün vaxtlar
  üzrə ən yüksək tamamlanmış nəticəni seçir; 0–9 mərtəbə və reklamla davam
  edilmiş cəhdlər daxildir. Aktiv checkpoint və offline məşq rekord deyil.
- Şəxsi rekord UID/app-scoped nəticələrdən hesablanmalıdır. Satılmış və ya
  silinmiş bina nəticəni əvəz etmir; inventardan rekord hesablamaq olmaz.
- `internal/wobbrik/read.go` Snapshot-da rekord yoxdur. Leaderboard səhifəsində
  özünü axtarmaq home üçün etibarlı həll deyil: istifadəçi ilk səhifədə olmaya
  bilər, səhifə materialized və müddətlidir.
- Növbəti implementasiya: eyni account lock altında `best_floors` additive
  non-negative integer, nəticəsiz hesabda 0. Client-dən rekord qəbul edilmir.

## 2. XP həddi və bar

- Təsdiqlənmiş qayda: L-dən L+1-ə əlavə 50×L XP.
  L başlanğıcı 25×L×(L−1), növbəti hədd 25×L×(L+1) ümumi XP-dir.
- AppBackend `attempts.go:Level` integer binary search ilə səviyyə hesablayır;
  `read.go` yalnız `xp` və `level` verir. Mobil `RecoveryDtos.kt` də yalnız
  bunları oxuyur; barın həddi hazır müqavilədə yoxdur.
- Növbəti implementasiya: sync və attempt-read snapshot-larına
  `level_start_xp` və `next_level_xp` decimal string sahələri əlavə et.
  Növbəti hədd MaxInt64-dən böyük ola bilər: Go-da təhlükəsiz integer/big.Int,
  Kotlin-də BigInteger ilə oxu; XP mükafatı və Level qaydası dəyişməsin.
- Bar payı `(xp−level_start_xp)/(next_level_xp−level_start_xp)` olmalıdır;
  əvvəlcə tam ədədlərlə fərqi hesabla, yalnız çəkilmə payını float-a çevir.
  Dizayndakı 580/800 kimi rəqəmlər server qaydasının əvəzi deyil.
- Köhnə serverdə əlavə sahələr olmadıqda yeni mobil DTO null/unavailable
  saxlasın, saxta 0 rekord və ya lokal hədd göstərməsin. Yanlış tip, mənfi
  rekord və uyğunsuz interval fail closed olsun. Köhnə client əlavə JSON
  sahələrini ignor edə bilir; wobbrik-v1 iqtisadi qaydası dəyişmir.

## Müqavilə vahidinin tələb olunan yoxlamaları

- Yeni hesab, sıfır və sub-ten finish, aşağı/yüksək növbəti nəticə, aktiv
  checkpoint-in rekord olmaması, satış/silmədən sonra rekordun qalması.
- İki UID/app izolyasiyası, blocked/foreign attempt, exact finish retry,
  itmiş cavabdan operation recovery + sync; rekord yeni mükafat verməməlidir.
- XP 0/49/50/149/150 və MaxInt64; interval daxilində xp, level uyğunluğu,
  böyük decimal string; köhnə/yeni server-client DTO uyğunluğu və malformed JSON.
- Snapshot oxusu əlavə revision, dedup receipt və wallet/ledger mutasiyası
  yaratmamalıdır; mövcud sync-in account-open davranışı ayrıca saxlanır.
- Yeni schema sütunu lazım deyil: rekord mövcud nəticələrdən, hədlər mövcud
  qaydadan çıxır. Query planı və lazım gələrsə indeks ölçülüb yoxlanmalıdır.
- Backend öz branch/commit/review-u, go vet, race və disposable PostgreSQL
  testləri ilə yoxlanmalıdır; Wobbrik CI bunu əvəz etmir.

## Sonrakı kiçik mərhələlər

1. Yuxarıdakı backend read müqaviləsi + mobil DTO/test uyğunluğu. UI dəyişmə.
2. Real rekord və XP barını təsdiqlənmiş home kompozisiyasına bağla.
3. Bina sayı: mövcud owner-scoped, non-deleted inventory API-sini yoxla.
   İlk səhifənin uzunluğu total deyil; satış/silmə və səhifələrarası dəyişmə
   nəzərə alınmalıdır. Dəqiq total lazım olduqda backend müqaviləsini tamamla.
4. Parametrlər: təsdiqlənmiş account dizaynındakı idarəetmələri mövcud real
   preference/navigation imkanları ilə bağla; işləməyən toggle yaratma.
5. Tablet home “Şəhərim”: mövcud city read/return sərhədində real naviqasiya.
   Placement disabled qalır; geometry və ölçü qərarları açıqdır. Saxta şəhər
   və ya hazır olmayan place/move axını ilə tamamlanmış sayma.

## Vizual mənbələr və qəbul

- Telefon: `docs/design/mobile-v1/mobile-primary-v2.png` və account-states.
- Tablet: `docs/design/tablet-v1/primary-landscape.png`, account-landscape
  və portrait; tablet README-də aktiv oyun istisnaları qorunur.
- Tokenlər: `docs/design/mobile-v1/tokens.css` və Android `GameTokens.kt`.
- İkonlar: `docs/design/tablet-rail-icons-v1/`; resursların olması runtime
  rail inteqrasiyasının tamamlanması deyil.
- Hər vizual vahiddə telefon və tablet renderinə bax, təsdiqlənmiş müvafiq
  lövhə ilə yanaşı müqayisə et; ölçü/orientasiya/font scale və fərqləri review-da
  qeyd et. Asset çatışmazlığında alternativ üçün əvvəl istifadəçi razılığı al.
- Bu auditdə vizual dəyişiklik və device render yoxdur.
