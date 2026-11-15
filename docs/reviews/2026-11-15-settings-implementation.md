# Təsdiqli Parametrlər — real inteqrasiya, 15 noyabr 2026

Branch: `feat/settings-flow`, baza `a192f4d`. Review scope: bu branch-in settings source/assets/build dependency və host harness working diff-i. Lokal commit/merge/push/CI yoxdur. Əvvəldən dirty release baseline profilləri və compact proposal faylları qorunur.

## Tətbiq

Hesab → Parametrlər → Hesab real route-u əlavə edildi; Android Geri də eyni geri çağırışını istifadə edir. Profilin pending yeni ölçü dizaynı tətbiq edilmədi. SettingsScreen təsdiqli `account-compact-visual/settings-390.png` kompozisiyasını, eyni GameTokens və xətti ikon yollarını saxlayır. Bütün sətir switch target-dir (min64dp), role/state semantikası var, header Geri48dp-dir. Body böyük şriftdə scroll edir.

DeviceSettings cihaz üzrə DataStore-dur, account/wallet deyil. Arrowz-un mövcud DataStore1.1.1 pini götürüldü; başqa stack yenilənmədi. İlkin dəyərlər təsdiqli nümunə ilə eynidir: musiqi/səs açıq, titrəmə bağlı. Yazı atomikdir; UI yazı gedərkən switch-ləri bloklayır, oxu/yazı uğursuzluğu açıq göstərilir, xəta saxta uğura çevrilmir.

GameFeedback activity lifecycle üzrə foreground musiqisi, audio focus, SoundPool klik və sistem haptic-i bağlayır. Ümumi HomeButton və hər iki real oyun host-un istifadə etdiyi GameScene drop toxunuşları seçimlərə tabedir. Bu, bütün ayrı custom kart/nav kontrol və landing/finish hadisələri üçün geniş səs dizaynı deyil. SFX SoundPool hazır olduqdan sonra işləyir. Musiqi arxa planda/focus itkisində pauza olunur; başqa aktiv musiqinin üstünə avtomatik başlanmır. Cihazda eşidilmə/haptic/focus interruption yoxlanmayıb.

Audio: Arrowz-dakı CC0 `Space City` (MintoDog) və Kenney Interface Sounds `select_002` eyni mövcud OGG kodlaması ilə kopyalandı. Mənbələr 15 noyabrda yoxlandı: https://opengameart.org/content/space-city və https://kenney.nl/assets/interface-sounds . App `assets/licenses/` daxilində mənbə/lisenziya qeydi var. Təsdiqli ikon yolları üçün upstream Lucide ISC/Feather MIT notice da daxil edildi.

## Dəstək və hüquqi məzmun — cari istifadəçi qərarı

İstifadəçi Wobbrik üçün AppBackend-də DailyPlate ilə eyni public legal mexanizmini istədi və dəstək e-poçtunu `aladdinbiyabangerd@gmail.com` kimi təsdiqlədi. Əvvəlki “ünvan/mətn əlavə edilməyib” vəziyyəti aradan qaldırıldı. Dəstək ACTION_SENDTO mailto açır, avtomatik mesaj göndərmir; email app tapılmadıqda ünvan seçilə bilən mətndə verilir.

AppBackend ayrıca `feat/wobbrik-legal-pages` branch-ində `/Users/mac/My Workspace/AppBackend-wobbrik-legal-pages` daimi worktree-dədir, baza `b2cd617`. `internal/legalpages/content/wobbrik.json` ingilis+azərbaycan dilində privacy/terms/delete-account mətnlərini saxlayır. Android asset-i həmin faylla bayt-bayt eynidir. Bu sənəd dilləri yeni Android UI locale siyahısı deyil; hazırda UI Azərbaycan dilindədir.

Məxfilik sətiri real offline hüquqi mətn pəncərəsini açır; “Sənədi seç” ilə Xidmət şərtləri və Wobbrik hesabını sil sənədləri də oxunur. Başlıq, tarix, əlaqə və bölmələr canonical JSON-dan alınır. Mətn scroll olunur, düymələr scroll-dan kənardadır. Əsas təsdiqli settings kompozisiyası dəyişməyib. Mətnlər Google/Firebase giriş+diagnostika, oyun/public profil/bazar məlumatları, şərti Play alışları və mövcud silinmə/backup məhdudiyyətlərini açıqlayır; DailyPlate sağlamlıq/18+/trial/subscription qaydaları köçürülməyib.

Deploy yoxdur; tətbiq hələ dərc edilməmiş URL-ə göndərilmir. Public səhifələr yalnız ayrıca backend deployment-dən sonra xaricdən əlçatan olacaq. Bu vahid yeni hesab-silmə API-si və ya iqtisadiyyat qaydası yaratmır. Backend test/review: həmin worktree-də `docs/reviews/2026-11-15-wobbrik-legal-pages.md`.

## Lokal review və yoxlama

İlk davranış testi standalone ekran üçün çatışmayan CommunityBoard provider-i tutdu; provider SettingsScreen daxilinə köçürüldü. Render harness-də xarici provider çıxarıldı və testlər təkrarlandı. İlk offline compile DataStore AAR keşdə olmadığı üçün uğursuz idi; eyni pin online resolve-dan sonra compile keçdi. İlk müqayisə skripti Pillow olmadığına görə işləmədi; Java ImageIO ilə müqayisə edildi.

Native host render: 390×844font1,360×640font1,320×568font2. Hər üçünə baxıldı; font2 dəstək bölməsi scroll altındadır. Təsdiqli fixture ilə hər üç ölçüdə dəyişən piksel sayı **0**. Yan-yana board-lar `settings-implementation-visual/*-comparison.png` altındadır (sol təsdiqli, sağ production composable). Bu nəticə eyni sintetik host şərtlərinə aiddir, cihaz ekranına zəmanət deyil.

İlkin native testlər4/4PASS21s:3render + switch/back/persistence. Normal app unit/assembleDebug/lintDebug son run15sPASS; lint warnings mövcuddur, o cümlədən dependency yeniləmə təklifləri. Kontrast3/3PASS:min8.22. Final native5/5PASS22s: üç render, switch/back/persistence və real SettingsRoute DataStore+dialog/back testi. Son source üzrə diff whitespace PASS. Son SettingsRoute dəyişikliklərindən sonra assembleDebug/lintDebug13sPASS.

Settings persistence üçün backend API/schema/economy dəyişmir. Son istifadəçi göstərişi ilə ayrıca public legal content və regression testləri backend-ə əlavə edildi; yeni auth/game/billing API və migration yoxdur. Real hesab E2E, cihaz audio/haptic, API24 runtime, TalkBack, real inset/IME yoxlanmayıb; emulator açılmayıb. Mobil əvvəl qərarına əsasən planşet/landşaft işi aparılmayıb. CI işlədilməyib.

## Hüquqi inteqrasiyanın son yoxlaması

- Backend daimi worktree-də fokuslu public route/race testləri PASS (legalpages1.533s/platform1.700s); go vet və server/preview build PASS. Read-only content/routes dəyişir, DB/payment/state testləri bu işdə tələb olunmur və təkrarlanmayıb.
- Android final normal app unit/assembleDebug/lintDebug38sPASS; lint warnings qalır, failure yoxdur. Legal dialog üçün ayrıca native regular/font2 testləri13sPASS; final bütöv native host7/7PASS44s:3settings render, switch/persistence/back, real route+mailto+3legal document selection,2legal dialog render.
- Android dialog real `ShadowDialog` window-dan çəkildi; əvvəlki activity-only capture dialogu göstərmirdi və sübut kimi istifadə edilmir. Font2 testində resource configuration fontScale da2.0 edildi; yalnız xarici CompositionLocal sıfırlanan dialog window üçün kifayət etmirdi. Düzgün normal/böyük şrift şəkillərinə baxıldı.
- Web template/CSS dəyişməyib. Browser CDP390px AZ privacy+terms və1280px EN deletion full-page renderlərinə baxıldı; document scrollWidth viewport-la eynidir. İlk Chrome CLI390 screenshot əslində daha geniş viewport-u kəsirdi, həmin nəticə atıldı. IAB bağlantısı timeout idi. Yenidən alınan screenshot-lar daimi backend review qovluğundadır.
- Kontrast: Android text/action3/3PASSmin8.22; web3/3PASSmin4.88. JSON nüsxələrinin byte parity-si və hər iki diff whitespace yoxlaması PASS.
- Müvəqqəti backend worktree fasilədə silindi; Android asset-dən JSON və regression-lar daimi worktree-də bərpa edildi, backend yoxlamaları təkrarlandı. Mövcud owner profil/recipe işi dəyişdirilməyib.
- Real device/TalkBack/audio/haptic/API24/IME/real account E2E yoxlanmayıb. Emulator/CI/commit/merge/push/deploy yoxdur. Tam mərhələ5 və ya hüquqi/Play qəbulu iddiası deyil.

## Müstəqil diff review və lokal commit — 15 noyabr 2026

İstifadəçi Android settings dəyişikliklərinin review və lokal commit-ini açıq tapşırdı. Branch `feat/settings-flow`, review bazası `a192f4d`; scope: settings source/dependency, navigation, device persistence, lifecycle/audio/haptic, offline legal content, licenses və settings host harness. Əlaqəsiz release baseline `.dm` faylları və profil/settings proposal arxivi commit-dən kənarda qorunur. Renders repo ignore qaydasına əsasən lokal qalır; harness commit edilir.

Tapıntı: SoundPool.load çağırışı OnLoadCompleteListener qeydiyyatından əvvəl idi. Sürətli completion itə və `loaded=false` qalaraq klik səsini səssiz saxlaya bilərdi. Listener load-dan əvvələ keçirildi. GameFeedbackLoadTest dərhal tamamlanan load-u simulyasiya edir və sonra real click çağırışında playback-i, sound-off/background zamanı əlavə playback olmamasını yoxlayır. Qalan review-da konkret açıq defect tapılmadı. Persistence error/cancellation, min48dp/role/state, route/back, support intent, legal selector və account/server sərhədləri yoxlandı.

Son yoxlamalar:
- Java21/offline `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug` PASS29s; əvvəlki lint/deprecation xəbərdarlıqları qalır. İlkin sandbox Gradle cache lock-u blokladı; eyni yoxlamalar icazəli execution ilə uğurla təkrarlandı.
- Review-only native host: SettingsRegular/Small/Font, SettingsBehavior/Route, LegalRegular/Font və GameFeedbackLoadTest — **8/8 PASS42s**, failure/error0. Parametrlər360×640 və390×844 font1,320×568 font2; legal390font1/320font2 yenidən render edildi və beş görüntüyə baxıldı. Üç settings PNG təsdiqli fixture ilə bayt-bayt eynidir. Font2 body/legal scroll saxlanır, footer əməliyyatları görünür.
- Kontrast Text/Muted/Accent on Surface3/3PASS, min8.22. Canonical backend/Android legal JSON byte parity PASS; diff whitespace PASS.
- Yeni backend dəyişməsi tələb olunmur: public profil/legal işi artıq AppBackend main/origin main cbd0eeb-dədir; bu düzəliş yalnız Android SoundPool initialization sırasıdır.

Real audio eşidilmə/haptic/focus interruption, API24/device/TalkBack/inset/IME və tam mərhələ qəbulu yoxlanmayıb. Emulator/CI/merge/push/deploy yoxdur. Lokal commit source/assets və bu review/handoff-u saxlayır.

Commit öncəsi staged diff check upstream Kenney notice CRLF/trailing whitespace-ni aşkarladı; yalnız sətir sonları/boşluqlar normallaşdırıldı, lisenziya mətni dəyişmədi. Son staged diff check PASS; bu mətn-only düzəlişdən sonra build təkrarlanmadı.
