# Rekordlar — native metal podium və çələng düzəlişi

- Branch: `feat/community-tabs-native-consistency`; baza HEAD `6c96e9c`, uncommitted working diff. İstifadəçinin göstərişi ilə commit yoxdur. Əvvəlki owner dəyişiklikləri saxlanır; stage/reset/stash/merge/push/deploy/CI edilmədi.
- Scope: PLAN §1.1, `2026-11-15-records-native.md`-də açıq qalan podium/laurel material və kontur vahidi. Authority: `design/unified-mobile-v3/03-account.png` aşağı orta, son Home header/7-slot qərarı ilə. Profil/Parametrlər bu vahidə daxil deyil.

## Dəyişiklik və source review

- Yalnız `MobileRecordsPresentation.kt` production mənbəyi dəyişdi. Düz yumru kartlar native silindrik pillə ilə əvəz edildi: elliptik üst səth, metal yan gradient, kontur, işıq xətti və mərkəzi medal. Qızıl/gümüş/bürünc üçün mövcud `GameTokens` palitrası və onun qarışıqları işlənir; yeni rəng və ölçü tokeni yoxdur. Üçüncü yerin əvvəlki qızıldan navy-yə qaralan səthi götürüldü.
- Siyahının ilk üç rank nişanı eyni metal ailəsindədir. Digər rank-lar navy nişanını saxlayır. Nömrə, ad və nəticə native Text-dir; raster səth/ikon əlavə olunmadı. Mövcud personaj sheet-i dəyişmədi, paylaşılmış bitmap provider saxlanır.
- Birinci yerin yaşıl oval yarpaqları daha incə qızılı native çələngə dəyişdi; ikinci/üçüncü yerlərə aşağı yaşıl-qızılı çələng əlavə edildi. İlk renderdə iri yarpaqlar aşkarlandı, ikinci turda proporsiya və hər yarpağın lokal gradient istiqaməti düzəldi. Qalibin dairəvi çərçivəsi saxlanır.
- Lokal incremental source review girişdəki fayl snapshot-u ilə aparıldı: yalnız dekorativ drawing və rank ink dəyişir; ölçü kontraktı, read budget, scope/cursor/session/refresh, scroll və shared chrome dəyişmir. AppBackend API/domain/schema dəyişikliyi lazım deyil; bu tur backend kodu dəyişdirilmədi və backend testləri təkrarlanmadı.

## Yoxlamalar

- Son mənbə: native host `MobileRecordsProductionRenderTest`7 + shell4 + achievements9 = **20/20 PASS**, failure/error/skipped0; Gradle43s. İlk iterasiya da20/20 PASS47s idi. API34 Robolectric synthetic transport/account fixture-dir, real hesab testi deyil.
- Son PNG-lər açılıb təsdiqli `03-account.png` ilə müqayisə edildi:390/font1,320/font1,320/font2,360/font2 simulated24/48dp inset+uzun balans,320/font2 uzun ad/rank/xal. Sübutlar `records-metal-native/production-records-*.png`-dədir.390-da native podium və üç list kartı,320-də mövcud fitted rows görünür. Font2-də selected tab horizontal scroll mövqeyindədir; body və uzun ad scroll-u qalır, dock sabitdir. Screenshot bütün scroll məzmununu göstərmir.
- Token checker5/5 PASS: Text/Background16.57, Muted/Surface8.22, Background/Accent11.45, Background/Gold10.24, Text/Raised10.24. Əlavə gradient hesabı: rank mətninin aşağı mərkəzi70% nöqtəsində Background ink qızıl8.23, gümüş6.75, bürünc7.59:1. Dekorativ kənar parıltısı mətn səthi kimi qiymətləndirilmədi.
- Son normal `:app:assembleDebug :app:assembleMockDebug :app:lintDebug` **PASS23s**; render init script bu tura tətbiq olunmadı. `git diff --check` PASS. Emulator və avtomatik CI açılmadı.

## Qalan məhdudiyyətlər

- Native metal/yarpaq konturları reference-ə yaxınlaşdırılıb; generativ portretlərin silueti və bütün ekran proporsiyası piksel surəti deyil. Tam app redesign və mərhələ qəbulu deyil. Profil/Parametrlər və digər ekranlar açıqdır.
- Real device/account E2E, TalkBack, IME, API24 runtime, GPU/performance və locale matrisi yoxlanmayıb. Mövcud böyük-font scroll/fitted-window davranışını bu vahid yenidən layihələndirmir. Press animasiyası ayrıca tapşırıqdır.
