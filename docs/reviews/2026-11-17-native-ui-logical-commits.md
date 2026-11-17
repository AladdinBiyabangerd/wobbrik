# Native UI — məntiqi commit və asılılıq yoxlaması

17 noyabr 2026. Branch `feat/community-tabs-native-consistency`; başlanğıc `7f540c7` (main bazası `6c96e9c`). İstifadəçi mövcud kodu asılılıqları ilə yoxlayıb məntiqi commit-lərə ayırmağa açıq icazə verdi. Bu qeyd yeni icra planı deyil; authority `docs/PLAN.md` §1.1 qalır.

## Qoruma və sərhəd

Bərpa snapshot-u `/private/tmp/wobbrik-pre-logical-commits-20261117.tar.gz` (101 dəyişmiş/untracked fayl) yaradıldı. Working tree stash/reset edilmədi. Android-in snapshot-dakı46 faylının baytları bölmədən sonra eynidir. Yeni UI/Profil dizaynı və Ərazi girişi əlavə edilmədi. Mövcud Profil işi yalnız öz commit-inə daxil edildi. AppBackend-də bu tur dəyişiklik edilmədi; mövcud additive achievement_progress və server position müqavilələri yoxlandı. Backend deploy/test nəticəsi bu tur iddia edilmir.

CODEOWNERS/nested ownership faylı tapılmadı (root AGENTS.md qüvvədədir). Asılılıqlar core DTO → native token/surface/icon/chrome → feature ekranları → route inteqrasiyası ilə ayrıldı. HomeStats/AchievementLevelBadge qarşılıqlı çağırışına görə Home və Nailiyyətlər bir vahiddədir. PurchaseCard shape və commerce çağırışları commerce vahidində birlikdə saxlanır. OnlineGame-in yalnız achievements hunk-u əvvəl, qalan commerce route-ları sonra stage edildi.

## Commit-lər və izolyasiya olunmuş yoxlama

- `666cec2` — feat(core): parse optional server achievement progress. Staged tree `08dff5859c`; PASS: `:core:test`.
- `be07104` — feat(core): expose server decoration placement in ownership reads. Staged tree `22744500df`; PASS: `:core:test`.
- `609fac5` — feat(ui): add native menu surfaces icons and shared chrome. Staged tree `a316a25d7f`; PASS: `:app:compileDebugKotlin`.
- `85eb988` — feat(ui): render native home and accepted achievement progress. Staged tree `8a990e2843`; PASS: `:app:compileDebugKotlin`.
- `31cf567` — feat(ui): render native leaderboard with shared portrait artwork. Staged tree `75e542a9c4`; PASS: `:app:compileDebugKotlin`.
- `1a6a263` — feat(ui): integrate native profile overview editor and avatar picker. Staged tree `893ff28523`; PASS: `:app:compileDebugKotlin`.
- `61ee928` — feat(ui): render native accessible settings controls. Staged tree `89f5578058`; PASS: `:app:compileDebugKotlin`.
- `391f373` — feat(ui): separate inventory ownership and native market flows. Staged tree `c670307ce3`; PASS: `:app:compileDebugKotlin`.
- `e54ca0b` — fix(ui): keep city loading and view actions usable in compact portraits. Staged tree `49626920c9`; PASS: `:app:compileDebugKotlin`.
- `79d2a5a` — feat(debug): isolate persistent local gameplay in mock build. Staged tree `21de5596e4`; PASS: `:app:testDebugUnitTest`, `:app:testMockDebugUnitTest`, `:app:assembleDebug`, `:app:assembleMockDebug`, `:app:compileReleaseKotlin`, `:app:lintDebug`, `:app:lintMockDebug`.

Hər namizəd `git write-tree` → `git archive` ilə `/private/tmp/wobbrik-commit-candidate` altına çıxarıldı və stage olunmamış production faylları olmadan yoxlandı. Eyni disposable qovluqda build cache saxlandı; deleted source yoxdur. Hər stage-də diff --check keçdi. Lokal mərhələ logları `/private/tmp/wobbrik-slice-*.log`; bütün code compile/test namizədləri keçdi. Sonradan `03ea7ba` authority/rule mirror və `fae26b3` tarixi review/QA mənbələri ayrıca commit edildi. Köhnə proposal/review mətnləri cari dizayn qərarını əvəz etmir.

## Nəticələr

- Core138/138, debug24/24, mock27/27 unit test; failure/error/skipped0. Core ilk iki candidate-də həqiqətən işləyib; UP-TO-DATE nəticəsi ilə əvəz edilməyib.
- Debug/mockDebug APK, compileReleaseKotlin, lintDebug/lintMockDebug PASS (tam mock candidate2m56s). Release/debug source set-ləri real null BuildVariantBackend istifadə edir. Mock manifest paketi com.aladdin.wobbrik.mock, INTERNET yoxdur, FirebaseInitProvider yoxdur.
- ui-craft kontrast5/5 PASS: Muted/PanelTop8.59; Background/Accent11.45; Background/Success11.82; Text/PanelTop13.58; Text/PanelBottom16.16.
- Commit79d2a5a mənbəsindən yeni native production host run: Home/Nailiyyətlər/Rekordlar/shell/commerce/Profil/editor/Parametrlər. 52/52 PASS3m12s,28 suite, failure/error/skipped0. Native test-only init `/private/tmp/wobbrik-settings-render.init.gradle`: tracked render.init.gradle əsası + forkEvery1/maxParallelForks2. Çıxış `/private/tmp/wobbrik-committed-native-renders`, log `/private/tmp/wobbrik-committed-native-tests.log`.

## Qalan məhdudiyyətlər

İki əvvəlki generated `.dm` baseline artifact-ı (`app/release/baselineProfiles/{0,1}/app-release.dm`) generasiya provenance-i yoxlanmadığından commit edilmədi, dəyişmədən working tree-də saxlanır. PNG render artifact-ları repo ignore qaydasına əsasən lokal qalır. Tam PNG pariteti/mərhələ qəbulu, real device/account/TalkBack/IME/inset/API24/GPU/performance/locale/tablet yoxlanmayıb. Deprecated Compose/native ripple warning-ləri əvvəlki source/host davranışıdır. Emulator, CI, main merge, push və deploy edilmədi.

Son native run-dan `own-listings-regular.png` və `detail-ownership-font.png` açılıb baxıldı; əvvəlki təsdiqli kompozisiya və böyük şriftdə scroll məhdudiyyəti saxlanır. Bu commit bölməsi yeni vizual paritet iddiası yaratmır. Compiled class yoxlaması: debug/release-də MockGameplayTransport yoxdur; mockDebug-də var. Lokal diff review-da yeni funksional blocker tapılmadı; əvvəlki sənədləşdirilmiş vizual/runtime məhdudiyyətləri qalır.

Tarixi review commit-indəki əlavə HomeComponentProposalTest daxil olmaqla son QA mənbələri ayrıca candidate-də compileDebugUnitTestKotlin PASS3s. Son testdən sonra production source dəyişməyib; diffcheck PASS. Qısa current-task handoff yeniləndi.
