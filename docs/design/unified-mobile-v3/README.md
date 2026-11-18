# Vahid mobil UI — təsdiqli istinadlar

15 noyabr 2026: istifadəçi bütün ekranlar üçün vahid dizayn, naviqasiya düzəlişləri və son ana ekran variantının implementasiyasını təsdiqlədi. İcra planı yalnız `../../PLAN.md` §1.1-dədir.

- `home-approved-frameless.png`: istifadəçinin son təsdiqi — yalnız böyük xarici çərçivə çıxarılıb, qalan dizayn saxlanır. Native navbar PNG-dəki şaquli boşluğa deyil, Android safe bottom-a bağlanır; bütün komponentlər mobil portret ölçülərinə/fontScale-a uyğunlaşır. ImageGen edit konseptidir, işlək render deyil.
- `home-approved-final.png`: Home üçün əvvəlki baza; cari Home authority-si `home-approved-frameless.png`-dir. Navbar **Ana / Şəhər / İnventar / Oyna / Bazar / Uğurlar / Profil**; Oyna navbarın tam ortasında. Son istifadəçi düzəlişi: mərkəzi düymədə “Oyna” yazısı yoxdur, native play işarəsi daha böyükdür; əlçatanlıq adı saxlanır. Üst profil yoxdur, balans sağdadır. Bu chrome/header qərarı digər bütün menyu ekranlarına da tətbiq olunur.
- `01-main.png`: əsas ekranlar, Şəhər/Plan, İnventar/Binalar, vahid Görünüş, Uğurlar/Nailiyyətlər.
- `02-market.png`: bazar alışları, sahib olunan bina detalı, dekor/ərazi alışları, elan forması və alış təsdiqi.
- `03-account.png`: profil, redaktə, avatar, parametrlər, Uğurlar/Rekordlar, Google giriş.
- `04-game-states.png`: aktiv oyun/pauza, şəhər xətası, loading, boş inventar və yığcam portret nümunəsi.
- `05-owned-and-listings.png`: İnventar/Dekor və Ərazi (sahiblik), Bazar/Mənim elanlarım və Yeni elan girişi.
- `prompts.json`: lövhələrin built-in ImageGen prompt-ları. Son home dəyişiklikləri: 7 slotda Oyna mərkəzə keçirildi; üst profil silindi; coin pill sağ kənara çəkildi. Mənbə generativ PNG-dir, işlək layout testi deyil.

Lövhələrdəki əvvəlki 6-slot dock və üst avatar son home qərarı ilə əvəz edilir; əvvəlki lövhələrin başqa kompozisiyaları saxlanır. Profilin tac lenti, aktiv oyunun yalnız pauza idarəsi və toxunaraq buraxma prinsipi saxlanır. Nümunə rəqəmlər, personajlar, assetlər və fontan yeni kataloq/domain qaydası yaratmır. Native real state, server snapshot və cari kataloq authority olaraq qalır. Bu PNG-lər mətnsiz production assetlər və real native render deyil; yalnız mürəkkəb qrafika üçün ayrıca raster asset hazırlanması icra işinə daxildir.

## Cari implementasiya qaydası

Son təsdiqli PNG-lər native Compose implementasiyası üçün təsdiqlənib; təkrar dizayn təsdiqi tələb edilmir. Ekran layout-u, səthlər, düymələr, navbar və mətn kodla qurulur. Navbar düymələrinin içindəki ikonlar native/vector/Canvas ilə çəkilir və təsdiqli görünüşü saxlayır. Raster yalnız mürəkkəb illüstrasiya/oyun qrafikası üçün ayrıca asset-dir; ekran, sadə UI skin-i və navbar ikon crop-u kimi istifadə edilmir. İcra yalnız mobil portretdir; planşet ayrıca tapşırıqdır.

15 noyabr — son screenshot düzəlişi: İnventar və Bazarın tablı kök ekranlarında geri oxu və “İnventar”/“Bazar” başlıq sətri yoxdur; tablar ortaq balans header-inin altında boş slot saxlanmadan yerləşir. Detal/yeni elan contextual geri idarəsi və Android Back qalır. Bu göstəriş həmin PNG-lərdəki köhnə kök başlıq sətrini əvəz edir.


15 noyabr 2026 — son istifadəçi qərarı: mobil Bazar daxilində Ərazi tabı və ərazi alış girişi çıxarılır. Son əlavə göstərişlə mobil İnventarın Ərazi tabı da çıxarılır. Saxlanmış server hüquqları dəyişmir; ərazi üçün menyu girişi və yeni alternativ yer yoxdur. Ərazi alışının yeni yeri istifadəçi tərəfindən sonra göstəriləcək; yeni yer uydurulmur. Bu qərar əvvəlki Bazar/Ərazi PNG girişini əvəz edir.

18 noyabr 2026 — yalnız İnventar/Binalar kök ekranı üçün istifadəçi yeni `../inventory-art-led-v1/reference.png` nümunəsinin native implementasiyasını ayrıca branch-də istəyib. Bu ekranın kart/tab/navbar üslubu üçün yeni istinad üstündür. Digər səhifələr dəyişmir və yeni üsluba keçirilməsi ayrıca qəbul gözləyir. İcra scope-u `../../PLAN.md` §1.1-dədir.

18 noyabr — son navbar qərarı: istifadəçi emulatorun yeni İnventar ekranında gördüyü `CollectionNavbar` üslubunu bütün app-in vahid menu navbarı kimi təsdiqlədi. Köhnə `MobileMenuNavbar` üslubu yeni icrada əvəz edilməlidir; bu qərar həmin PNG-lərin legacy dock üslubundan üstündür.7slot/sıra/Play/aktiv səhifə semantikası qalır. Qlobal native miqrasiya hələ icra edilməyib. Hazırda yalnız bina detalının yeni dizayn nümunəsi hazırlanır; digər ekranların redesign-ına blanket icra icazəsi kimi təqdim edilmir.

18 noyabr 2026 — son icra qeydi: istifadəçinin “okey implement” təsdiqi ilə `docs/design/building-detail-v1/proposal.png` bina detalı üçün native tətbiq edildi. Təsdiqli `CollectionNavbar` mobil menyularda vahidləşdirildi; legacy navbar renderer çıxarıldı. Digər səhifələrin məzmun redesign-ı bu vahidə daxil deyil. Sübut: `docs/reviews/2026-11-18-building-detail-native.md`.
