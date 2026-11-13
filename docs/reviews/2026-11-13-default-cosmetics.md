# Daimi kosmetik seçim və avtomatik finish — 13 noyabr 2026

- Branch: `fix/navigation-consistency`; baza `8a718da`; review bütün bu vahidin working diff-i üzrədir.
- İstifadəçi mövcud `finish-cosmetics-v1/phone-tablet.png` dizaynını və Kolleksiya girişini bu chatdə açıq təsdiqləyib. Yeni dizayn/token/palette yoxdur.
- Kolleksiya mövcud picker-i açır. Təsdiqlə account-scoped SharedPreferences-a sync commit edir; Geri saxlamadan home-a qayıdır. Bu cihazda davamlı seçimdir, cihazlararası sync deyil.
- Hər cəhd ilk accepted Ready-də account/attempt açarı ilə ayrıca capture edilir. Sonrakı default dəyişməsi bu görünüşü dəyişmir. Kataloq/unlock yoxlaması və pulsuz fallback `FinishCosmetics`-də qalır.
- Host terminal intent-i seçilmiş hissələrlə saxlayır. Əlavə mərtəbələr varsa əvvəl checkpoint, yeni exact Ready ilə avtomatik finish; sıfır mərtəbədə checkpoint yoxdur. Hər Ready capability üzrə bir avtomatik dispatch, retry isə mövcud durable journal ilə eyni envelope-dur. Qəbuldan sonra mövcud canonical home keçidi işləyir.
- Aktiv Canvas divar/pəncərə/giriş/işıq/dam atlaslarını cəhdin seçimindən çəkir; fizika/geometry dəyişmir. Offline practice köhnə qrafikasını saxlayır.

## Lokal diff review

- Storage capture uğursuzluğu gameplay-i bloklayır; retry stale Ready-ni host-a yenidən qura bilmir.
- Terminal draft yazısı uğursuz olanda checkpoint göndərilmir; retry əvvəl yazını və lazım olan checkpoint-i tamamlayır.
- Köhnə account/epoch capability-si avtomatik finish və preference confirm üçün qəbul edilmir.
- Lost response testində iki transport çağırışı mümkündür; eyni operation/envelope replay-i bir məntiqi finish-dir. Test buna uyğun yoxlanır.
- UI/state/data sərhədi: preferences adapter app-də, store kontraktı engine-də; ownership/currency authority dəyişmir.

## Yoxlamalar və məhdudiyyətlər

- `:app:assembleDebug :engine:test :app:testDebugUnitTest :app:lintDebug` PASS (son exact-Ready düzəlişindən sonra: engine 67, app 3 test; lint 0 error).
- Engine: 67 test; yeni coverage: default capture/freeze/next attempt/account isolation/locked fallback, storage capture failure, terminal automatic finish/recovery/exact envelope replay; sıfır mərtəbə və terminal checkpoint bərpası avtomatik axına uyğunlaşdırılıb.
- `git diff --check` PASS.
- Naviqasiya PNG/script-ləri yeniləndi; bunlar kod xəritəsidir, Android render sübutu deyil.
- Android runtime/render YOXLANMAYIB: kiçik/normal telefon, planşet, hər iki orientasiya, font 1.0/2.0, inset/klaviatura və accessibility vizual QA aparılmayıb. Mövcud dizayn uyğunluğu və atlas anchor-ları renderlə təsdiqlənməyib. Emulator/ADB açılmayıb; bu yoxlamalar tamamlanma/merge gate deyil.
- AppBackend dəyişməyib: mövcud checkpoint/finish DTO, revision, dedup və wallet qaydaları olduğu kimi istifadə edilir. Yeni backend müqaviləsinə ehtiyac yoxdur; backend test/deploy yoxdur.
- CI, main merge, remote push yoxdur. İstifadəçinin `app/release/` fayllarına toxunulmayıb. Mərhələ 4 başlanmayıb.
