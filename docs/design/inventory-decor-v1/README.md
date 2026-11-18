# İnventar — Dekor dizayn nümunəsi

18 noyabr 2026. İstifadəçi Görünüşün vizual düzəlişindən sonra İnventar/Dekor üçün yeni dizayn istədi. `proposal.png` built-in ImageGen dizayn təklifidir; təsdiqlənmiş authority və native screenshot deyil. Tam prompt və targeted Yol ikon düzəlişi `prompt.txt`-dədir. Yeganə plan `../../PLAN.md` §1.1-dir.

Kolleksiya header/navbarı saxlanır; Dekor tabı aktivdir. Hamısı/Ağac/Skamya/İşıq/Yol filtrləri, iki sütunlu art-led sahiblik kartları təklif olunur. Kart bir server nüsxəsini göstərir; ad kataloqdan, Şəhərdə/Saxlanılır statusu `DecorationCopy.position`-dan gəlməlidir. Şəkildəki nüsxələr və balans nümayiş məlumatıdır. Mövcud “Dekor üçün Bazara keç” girişi saxlanır. İnventarda alış, yeni yerləşdirmə əməliyyatı və ərazi girişi yoxdur.

Nümunə açılıb baxılıb: ilkin səhv dam siluetli Yol ikonu yol işarəsi ilə düzəldildi; yazılar, aktiv Dekor tabı və İnventar navbarı yoxlandı. Native implementasiya edilməyib. Başlanğıc fayllar `OwnedCityItemsScreen.kt` və core `DecorationReads.kt`-dir. UI native Compose/vector olmalıdır; bütöv PNG skin deyil. Mürəkkəb illüstrasiyalar üçün mövcud dekor asset-lərinin uyğunluğu ayrıca yoxlanmalıdır; nümunə hazır asset dəsti deyil.

Kiçik portret, font2, inset/loading/error/empty/cache state-ləri native renderlə yoxlanmış sayılmır; implementasiya zamanı mövcud tokenlər, minimum48dp target və uyğun scroll saxlanmalıdır. Mövcud token kontrastı: Text/PanelTop12.51:1, Metadata/PanelTop7.41:1, Unlocked/PanelTop9.10:1; generativ PNG native kontrast/a11y sübutu deyil. Backend/API/schema/domain dəyişmir. Növbəti addım istifadəçinin dizayn qiymətləndirməsidir.
