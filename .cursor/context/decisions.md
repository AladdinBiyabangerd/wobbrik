# Decisions

- Əsas workspace: `/Users/mac/My Workspace/Wobbrik`; digər worktree-lər üçün `git worktree list` yoxla. Remote: `https://github.com/AladdinBiyabangerd/wobbrik.git`; remote mövcudluğu push icazəsi deyil.
- Yeganə aktual plan `docs/PLAN.md`-dir; handoff yalnız `docs/context/current-task.md`, review-lar tarixi sübutdur.
- 4 noyabr: Kotlin + Compose ilə yekun Android oyununun implementasiyası, minSdk 24 / target+compileSdk 36 təsdiqlənib; köhnə “kod icazəsi yoxdur” qeydləri qüvvədən düşüb.
- PostgreSQL vahid authority; Firebase Auth kimlik; AppBackend DB qəbul vaxtı (3 noyabr qərarı). Server sərhədi vahid plan §2-də, dəqiq API backend müqaviləsindədir.
- Hər məntiqi vahid `feat/`, `fix/`, `chore/`, `docs/` branch → kiçik commit → lokal diff review → tarix saxlanılmaqla main merge → yalnız istifadəçi ayrıca CI tələb edəndə main-də `./scripts/local-ci.sh`. Uğursuzluq ayrıca fix branch/review/merge/main CI tələb edir.
- Remote push/deploy/Console/real SKU avtomatik deyil. Növbəti mərhələ yalnız ayrıca istifadəçi tapşırığı ilə başlayır.
- 8 noyabr: şablon mövcud qaydalara uyğunlaşdırılır; `docs/context/current-task.md` canonical qalır. Cursor giriş faylı onu göstərir; paralel handoff və paralel plan yaradılmır.
- Təsdiqlənmiş dizaynlar saxlanılır; template C.2 yenidən dizayn icazəsi vermir. Nümunə OK-u, mərhələ qəbulu və növbəti mərhələ startı ayrıdır.
- Aktiv branch statusu və tamamlanmamış işlər yalnız handoff-da saxlanır; bu fayl mərhələ qəbulu iddia etmir.

- 13 noyabr cari vəziyyəti: məcburi Google entry və guest/link çıxarılması hər iki main-dədir. Anonymous fallback, wallet merge və data deletion yoxdur. Canlı yoxlama tələb etmə, emulatoru avtomatik açma.

- 14 noyabr: oyun xarici bina təqdimatına `docs/design/city-view.png` kimi kiçik estetik retuş icazəlidir; 100% həndəsə eyniliyi tələb deyil (PLAN §7 son qərarı). Server accepted geometry və aktiv oyun eni dəyişmir.

- 14 noyabr son dəqiqləşdirmə: istifadəçi 40 real mərtəbəni vizualda6–8 təbii nisbətli nümayəndə mərtəbə ilə göstərməyi açıq təsdiqlədi; real mərtəbə sayı metadata-da qalır. Üfüqi dartılma/yastılanma rədd edildi. Owned inventory/detail preview maksimum8mərtəbə və yumşaldılmış ofsetlə çəkilir; server məlumatı və aktiv oyun dəyişmir. Şəhər renderer-inə həmin retuşun tətbiqi ayrıca qalan işdir.

- 14 noyabr — modul template inteqrasiyası: təsdiqli `building-purple-v1/modular` atlası inventar/detail və shared hissə redaktəsinin mövcud `parts` ID xəritəsinə bağlanır. Bütöv bina bitmap istisnası çıxarılır. `base_1` hissəsi olan seçimlər bu template-də, hər kateqoriya öz seçilmiş ID-si ilə çəkilir; tam digər üslublar mövcud renderer-də qalır. `base_1_window/roof/entrance/lighting` atlasın arch/roof/entrance/lamp qatlarını seçir; başqa ID-lər mövcud kataloq artıdır, square yeni ID deyil. Bu template maksimum6nümayəndə mərtəbə göstərir; real say/accepted geometry dəyişmir. İstifadəçinin son şəkli mərtəbələrin vahid bina kimi oturuşu və təməl–torpaq/pilləkən birləşməsi üçün əlavə istinaddır (`design/building-purple-v1/join-reference.png`). Şəhər/aktiv oyun/server dəyişmir; bütün kataloqun yeni art-a çevrilməsi iddia edilmir.

- 15 noyabr lokal test istisnası: ayrıca `mockDebug` (`com.aladdin.wobbrik.mock`, Wobbrik Mock) Google-sız sintetik profil/lokal adapter istifadə edir. Real backend və wallet deyil; adi debug/release Google tələbini saxlayır. Mock source release-ə daxil edilmir.

- 15 noyabr cari UI qərarı: son təsdiqli PNG-lər (`unified-mobile-v3/README.md`) əsasında native Compose implementasiyası təsdiqlənib. Layout/səth/düymə/navbar və navbar ikonları native/vector; raster yalnız mürəkkəb illüstrasiya/oyun art-ı. Təkrar təsdiq və köhnə proposal göstərişləri yoxdur. Mobil portret, planşet ayrıca tapşırıqdır.
