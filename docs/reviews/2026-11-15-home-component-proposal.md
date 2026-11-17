# Home — çərçivəsiz komponent təklifi

İstifadəçi əvvəlki native Home-un cihaz şəklini rədd etdi: böyük xarici çərçivə/daxili panellər/dock “oyun içində 2-ci oyun” təsiri yaradır. Compose ilə yazılması kompozisiyanı düzgün etmir. Əvvəlki nümunə qəbul edilməyib; bu yeni variant da təsdiq gözləyən proposal-dır.

`feat/home-approved-parity` üzərində yalnız review harness və sənədlər əlavə/yeniləndi. Bu turda production source dəyişmədi. Hazırkı APK-də əvvəlki Home qalır. Owner CityScreen/harness/baseline/account-compact və əvvəlki Home dəyişiklikləri qorunur. Commit/merge/push/CI/emulator yoxdur.

## Arrowz-da baxılan istinad

Read-only: `/Users/mac/My Workspace/arrowz/feature/home/src/main/kotlin/com/aladdin/arrowz/feature/home/HomeScreen.kt` — HomePhone, CoinPill; `core/designsystem/.../component/NeonKit.kt` — NeonButton və NeonProgress. Prinsip: vahid ekran fonu, müstəqil header/düymə/progress/card komponentləri; bütün ekranı ikinci böyük kartla çərçivələməmək. Arrowz-un stack/pin/məhsul qaydaları və dizaynı köçürülməyib.

## Nümunə

Source: `mobile-sizing-visual/render/HomeComponentProposalTest.kt`. Native Scaffold/Row/Column/Text/Surface/LinearProgressIndicator/IconButton/FilledIconButton; mövcud Wobbrik tokenləri, Nunito, logo/ikon/illüstrasiya. Böyük xarici çərçivə, stat kartının konturu və şüşə dock silinib. 7 girişin sırası və mərkəzi Oyna saxlanır.

Bu review-only statik vizual fixturedir. Play callback-i input testində yoxlanır; digər route ikonları noop-dur, real server/production naviqasiyası deyil. Təsdiqlənərsə mövcud production state/callback/recovery sərhədləri ilə inteqrasiya olunacaq. AppBackend/API/domain dəyişmir.

## Yoxlamalar

Son native2/2 PASS10s: 390×844 və360×640 portret/font1, Play displayed+click once, PixelCopy SUCCESS. Hər iki final PNG açılıb baxıldı. İlk nested NavigationBarItem/IconButton semantics/display cəhdi FAIL idi; proposal navbar ayrı native Row+IconButton-larla dəyişdirildi, son tur PASS. Kontrast4/4 PASS min8.22, diffcheck PASS. Lokal log `/private/tmp/home-components-proposal.log`.

`home-component-proposal/preview-390.png` paylaşım ölçülü native renderdir. `home-390.png`, `home-360.png` tam host renderləridir. Bunlar emulator/device deyil. Font2/inset/TalkBack/locale/performance/planşet yoxlanmayıb; bu scope üçün production build/lint təkrarlanmayıb. Təsdiq və tam paritet/PASS iddiası yoxdur.
