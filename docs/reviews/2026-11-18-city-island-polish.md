# Şəhər adası — təsdiqli şəkillə ikinci müqayisə

18 noyabr 2026. `feat/city-collection-design`, baza `a98272c`, working diff. Authority: `docs/PLAN.md` §1.1, `docs/design/city-collection-v1/proposal.png` və istifadəçinin cari göstərişi. Əvvəlki dirty iş saxlanıb; commit/merge/CI/push/deploy yoxdur.

## Düzəliş

Əvvəlki island-ground comparison-da xırda ağaclar və yalnız sahil boyunca planting genişlənən səthdə boş park hissələri yaradırdı. Mövcud `GameTokens` və artwork ölçüləri yoxlanıldı. Yeni UI palitrası/layout ölçüsü yoxdur; dünya artwork ölçüləri `CityIslandStyle` daxilindədir: tree width100→160, mövcud spatial bucket pilləsindən128 planting spacing, paving scale .12/.06→.24/.12. Mövcud atlasdan daha iri daş döşəmə oxunur; yeni asset/decode yoxdur.

Interior planting accepted union daxilində, water/park/root margin və real bina/dekor exclusion ilə hazırlanır. Ağaclar bir-birinə də occupancy daxilində mane olur. Interior sample maksimum64×64, tree cap128; bucket yoxlamaları scene boyunca bina×namizəd skanı yaratmır. Əvvəlki shoreline namizədləri saxlanır. Park/grid preparation əvvəlki Default job/cancellation sərhədindədir. Tree fit bounds atlasın real aspect ratio-su və yeni ölçü ilə eynidir. Depth sorting və native input/layout saxlanır.

Sahə artımında land/surface dəyişməsi planting-i yenidən qurur. Əlavə regression boş sahildən yaranan daxili planting-i,800→1600 extent artımında yeni hissəyə yayılmanı, su dəliyinin və promenade-in boş saxlanmasını yoxlayır. Əvvəlki real1→6parcel/20shape-rotation yoxlamaları da saxlanır.

## Lokal review

Scope: `CityIslandArt.kt`, `CityIllustrationArt.kt`, `CityScreen.kt` tree bounds və host `CityIllustrationTest.kt`; token reuse, land/park clipping, bounded sampling, account/read scope-un dəyişməməsi və legacy illustrated=false sərhədi. API/DTO/schema/domain/ownership dəyişmir; AppBackend dəyişikliyi tələb olunmur. Yeni HTTP oxusu yoxdur. Əvvəlki44fokuslu read/geometry/render testinə yeni1planting testi əlavə olunur. GPU/ART/heap/frame/production vaxtı ölçülməyib, sürət iddiası yoxdur.

## Yoxlamalar

Son production dəyişikliklərindən sonra `assembleDebug`, `lintDebug` və8sinifdə45/45fokuslu test PASS (0failure/error/skipped),1m37s; `/private/tmp/city-island-polish-final.log`, XML `city-island-polish/test-results/`. Əvvəlki ara run45/45PASS olsa da son bounds/root/paving dəyişikliklərinə yekun sübut sayılmadı. Son `git diff --check` PASS.

38native screenshot matrix-i açılıb baxıldı:320/390 fontScale1/2,360font2 inset/uzun ad/Long.MAX_VALUE, focus/fit/custom actions/refresh/retry/cache/empty/loading/error/scroll və eyni ekranda2→6parcel growth. Growth PNG ilk açılışda bəzi UI qatları boş göründüyündən ayrıca1/1growth run təkrarlandı (19s); tam native header/frame/summary/navbar olan son çəkiliş açılıb yoxlandı və yekun screens/matrix/comparison-a qoyuldu. Production kodu bunun üçün dəyişmədi. Native host capture təkrarlanma məhdudiyyəti qeyd olunur. Font2-də scene azalması və əvvəlki320navbar wrap qalır; bu vahidin dəyişdirdiyi artwork-də clipping/parkdan çıxan root görülmədi. Proposal / əvvəl / indi /6parcel müqayisəsi `city-island-polish/comparison.jpg`-dədir. Kontrast6/6 PASS, minimum7.41:1 (`city-island-polish/contrast.txt`). Emulator/real cihaz açılmayıb. Host native render real cihaz/ART/GPU sübutu deyil.

## Qalan fərqlər

Real parcel küncləri və concave əlavələr PNG-nin yumru maket konturu ilə eyni deyil; server geometry-ni saxtalaşdırmaq üçün yumrulaşdırılmır. Bina sıxlığı/silueti/say/mərtəbə/roof real hesaba tabedir, proposal-dakı beş bina kompozisiyasına çevrilmir. Dar yerlərdə park darala bilər. Böyük sparse extents-də əvvəlki entrance grid limiti qalır; interior sampling də cap səbəbilə seyrəkləşir. Ağaclar/daşlar atlas təkrarlarıdır; tam pixel parity/mərhələ qəbulu iddia edilmir. Runtime inset/IME/API24/TalkBack/bütün locales yoxlanmayıb.
