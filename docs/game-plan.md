# Wobbrik oyun planı

Şəhərin qüvvədə olan vizual mənbəyi: [təsdiqlənmiş iki görünüş](design/README.md). Plan və həcmli şəhər görünüşü birlikdə tətbiq edilir; köhnə konseptlər istifadə edilmir.

Tarix: 3 noyabr 2026. Status: əsas qərarları təsdiqlənmiş, ilkin iqtisadi balansı tamamlanmış plan. Kod yazılmasına başlanmayıb.

Bu plan bir toxunuşla bina qurma oyununu şəxsi şəhər, görünüş kolleksiyası və oyunçulararası sikkə bazarı ilə birləşdirir. İstifadəçinin müəyyən etdiyi tələblər əsas götürülür. “Təklif” kimi göstərilən qaydalar və bütün sınaq rəqəmləri təsdiqə açıqdır.

## 1. Məqsəd və əsas təcrübə

Oyunçu krandan mərtəbələr buraxır, binanın tarazlığını idarə edir, şəxsi rekordunu yeniləyir və leaderboard-da başqalarını keçir. Uyğun binaları şəhərinə yerləşdirir və ya digər oyunçulara satır. Qazandığı sikkə ilə görünüşlər, binalar və şəhər imkanları əldə edir.

Əsas hiss: “Bina əyildi, amma növbəti mərtəbə ilə onu xilas edə bilərəm.”

Oyun nə həddən artıq rahat, nə də bezdirəcək qədər çətin olmalıdır. Məğlubiyyətin səbəbi görünməli, oyunçu növbəti cəhddə nəyi yaxşı edə biləcəyini anlamalıdır. İlk ilham Tower Bloxx sadəliyidir; ad, qrafika, səs və interfeys kopyalanmır.

## 2. Platforma və idarəetmə

- Mobil, portret ekran, bir əllə oyun. Təsdiqlənmiş platforma: yalnız Android; iOS versiyası olmayacaq. Bu qərar konkret mühərrik seçmir.
- Bir toxunuş bir mərtəbə buraxır. Kompüterdə klik və boşluq düyməsi eyni işi görür.
- 4 noyabr 2026 təsdiqi: telefon və planşetdə ayrıca Burax düyməsi yoxdur; oyun sahəsinə toxunulur. Aktiv oyunda yeganə düymə pauzadır; yan/alt menyular göstərilmir. Mərtəbə sayı və rekord interaktiv olmayan göstəricilərdir. Pauzada yalnız Davam et, Yenidən başla, Menyuya qayıt görünür. Pauza/panel toxunuşu mərtəbə buraxmır; Davam et toxunuşu da səhnəyə ötürülmür. Cari vizual mənbə: docs/design/game-tap-v2/README.md.
- Mərtəbə düşərkən əlavə toxunuşlar nəzərə alınmır.
- Sürüşdürmə və telefonu əymə ilə tikinti idarəetməsi yoxdur. Şəhərdə yerləşdirmə idarəetməsi ayrıca prototiplənəcək.
- Tətbiq arxa plana keçəndə oyun fasiləyə düşür.
- Təsdiqlənmiş qayda: oflayn rejim məşq üçündür. Sikkə, satıla bilən bina və leaderboard nəticəsi yalnız onlayn qazanılır. Bazar və ödənişlər də internet tələb edir. Oflayn məşq XP və nailiyyət də qazandırmır (təsdiqlənib). Oflayn qurulan bina şəhərə və inventara əlavə edilmir; məşqin sonunda yalnız mərtəbə sayı göstərilir (təsdiqlənib).

## 3. Tikinti və fizika

Kran mərtəbəni sağa-sola yellədir. Buraxılan mərtəbə düşür, binaya oturur və zərbə yaradır. Qurulmuş mərtəbələr də bir-birinə bağlı bina kimi uyğun yellənir. Kamera binanın yuxarısını və növbəti mərtəbəni görünən saxlayır.

Mərkəzə yaxın eniş daha sabitdir. Sağ və sol yerləşdirmələr tarazlığı həmin istiqamətə dəyişir və bir-birini qismən kompensasiya edə bilir. Fizika tam tikinti simulyasiyası yox, ardıcıl və oxunaqlı modeldir.

- İlk versiyada mərtəbələrin eni, dərinliyi, hündürlüyü və fiziki çəkisi eynidir. Krandakı və artıq yerləşdirilmiş mərtəbələr eyni modul ölçüsünü saxlayır; fərq yerləşdirmə ofsetindən yaranır.
- Aktiv oyunda dam örtüyü verilmir və kran dam daşımır. Oyunçu uduzduqda sistem binaya avtomatik dam örtüyü əlavə edir; bu, ayrıca oynanılan mərtəbə deyil və mərtəbə sayını, sakinləri, XP və mərtəbə mükafatını artırmır.
- Görünüş və alışlar fiziki üstünlük vermir.
- Zərbənin yaratdığı yellənmə zamanla zəifləyir.
- Gözləmək təkbaşına məğlubiyyət yaratmır; təhlükə yeni yerləşdirmənin nəticəsidir.
- Təhlükəli əyilmə ilə geri dönməz aşma fərqləndirilir. Təhlükəli halda növbəti mərtəbə ilə xilasetmə imkanı qalır.
- Tam kənara düşən mərtəbə və geri dönməz aşma cəhdi bitirir.
- Hər mərtəbənin ayrıca sürüşüb dağılması ilk versiyanın tələbi deyil.

Kran sürəti, tarazlıq həddi, zərbə gücü, yellənmə, zəifləmə və çətinlik artımı ayrıca dəyişdirilən parametrlər olmalıdır.

## 4. Çətinlik və reaksiyalar

İlk beş mərtəbə daha bağışlayıcıdır. Sonra kran sürəti və binanın həssaslığı tədricən artır, artıma yuxarı hədd qoyulur. Qəfil və xəbərsiz sürət dəyişməsi yoxdur.

Bir cəhd üçün ilkin hədəf 30–60 saniyədir; vaxt limiti qoyulmur. Bacarıqlı oyunçunun daha uzun oynaması mümkündür.

Dəqiq eniş, adi eniş, təhlükəli əyilmə, uğurlu xilasetmə və aşma fərqli reaksiyalar alır. Aşma animasiyası, zərbə/toz effekti və uyğun vibrasiya olmalıdır. Səs və vibrasiya ayrı söndürülür. Təhlükə yalnız rənglə göstərilmir.

## 5. Bina tamamlamaq və saxlamaq

Minimum 10 uğurla yerləşdirilmiş mərtəbəyə çatan bina saxlanmağa və satışa uyğun olur. Bu hədd istifadəçi tərəfindən təsdiqlənib.

Təsdiqlənmiş qayda: oyunçu 10 mərtəbədən sonra da tikintiyə davam edir. Könüllü “Binanı tamamla” seçimi nəzərdə tutulmur. Məğlubiyyətdə son uğursuz mərtəbə çıxarılır və son uğurlu yerləşdirmədən sonrakı bina vəziyyəti oyunçuda qalır. Vizual aşma uğurlu tikintini silmir. Saxlama və satış üçün 10 mərtəbə həddi qüvvədədir. Bir cəhd yalnız bir yekun bina və bir dəfə nəticə mükafatı yaradır.

Bina qeydi mərtəbə sayını, mərtəbələrin yerləşməsini, əyriliyini, hissələri, sakin tutumunu, yaradıcını və sahibini saxlayır. Saxlama həddi altındakı cəhd bina yaratmasa da qaydalara uyğun oyun irəliləyişi verə bilər.

## 6. Fasad və hissələr

Təsdiqlənmiş başlanğıc kolleksiyası: dəqiq 6 pulsuz üslub var və hamısı ilk gündən seçilə bilir. Oyunçu bərabər dəyərli başlanğıc seçimindən öz üslubunu seçir. Təsadüfi bahalı fasad verilmir. Fərqlilik hissələrin oyunçu tərəfindən birləşdirilməsindən yaranır.

Hissələr: divar rəngi/materialı, pəncərə, balkon/giriş, dam və işıqlandırma. Hər kateqoriyada pulsuz, reklamla, sikkə ilə və birbaşa real pulla əldə edilən seçimlər ola bilər. Oyun və nailiyyətlə açılan hissələr də nəzərdə tutulur.

Təsdiqlənmiş qayda: bəzi xüsusi bacarıq hissələri yalnız nailiyyətlə açılır; onları sikkə, reklam və real pulla açmaq mümkün deyil. Hər hissənin açılma şərti və reklam tələb edirsə irəliləyişi əvvəlcədən görünsün.

Təsdiqlənmiş hazır bina redaktəsi: oyunçu sahib olduğu binanın hissələrini hesabında açıq olan hissələrlə dəyişə bilər. Bu qayda öz qurduğu və aldığı binaya aiddir; alınmış bina yenə təkrar satıla bilməz. Mərtəbə düzülüşü, əyrilik və sakin sayı qorunur, satış dəyəri yenidən hesablanır. Aktiv satış elanı varsa əvvəl geri götürülür.

Hissənin hesabda açılması gələcək tikintilər üçün istifadə hüququ verir. Təsdiqlənmiş qayda: reklamla açılan fasad və hissələr daimidir, təkrar istifadə üçün reklam tələb olunmur. Bir neçə reklam tələb edən hissələrdə tamamlanmış baxışların irəliləyişi saxlanılır. Hazır bina almaq onun hissələrini hesabda açmır. Hissələrin təsdiqlənmiş sayı və üslub adları bölmə 23-dədir; dəqiq qrafik icra ayrıca hazırlanacaq.

## 7. Şəhər və sakinlər

Təsdiqlənmiş inventar qaydası: bina sayı üçün oyun limiti yoxdur; texniki saxlama və səhifələmə ayrıca layihələndiriləcək. Lazımsız bina aydın təsdiqdən sonra silinə bilər, sikkə qaytarılmır. Aktiv satış elanındakı binanı silmək üçün əvvəl elan geri götürülür.

Şəhərdə oyunçunun qurduğu və aldığı binalar görünür. Binalar inventardan yerləşdirilə, yerini dəyişə və inventara qaytarıla bilər. Yerləşmiş binanı başqa bina ilə dəyişmək mümkündür; yeni bina sığmırsa dəyişiklik tamamlanmır. Əvvəlki bina itmir.

Yerləşdirmə binanın faktiki əyriliyini və bütün tutduğu sahəni nəzərə alır. Qonşu binalar üst-üstə düşmür. Təsdiqlənmiş qayda: şəhərdə saxlanmış bina forması sabit qalır; tikintidəki təhlükəli yellənmə davam etmir.

Şəhər canlı görünməlidir. Təsdiqlənmiş V1 canlılığı: az sayda hərəkət edən sakin və avtomobil, pəncərə işıqları və yüngül ağac hərəkəti. Hər sakini ayrıca simulyasiya etmək tələb olunmur.

Binanın ölçüsünə uyğun sakinləri olur və sakinlər satış dəyərinə təsir edir. Təsdiqlənmiş formula: sakin sayı = uğurla yerləşdirilmiş mərtəbə sayı × 4. Məsələn, 10 mərtəbəli bina 40 sakinlidir. Fasadın qiyməti sakin sayını dəyişmir. Təsdiqlənmiş qayda: sakinlər passiv sikkə və ya avtomatik vergi gəliri yaratmır. Təsdiqlənmiş V1 sərhədi: köçmə, aclıq və xidmət idarəetməsi yoxdur. Təsdiqlənmiş qayda: şəhərin əhali göstəricisinə yalnız şəhərdə yerləşmiş binaların sakinləri daxildir; inventardakılar sayılmır. Satış elanı olan bina şəhərdə qaldığı müddətdə sakinləri sayılır, satılıb çıxarılanda çıxılır.

Başlanğıc sahə pulsuzdur. Genişlənmə əvvəl tələb olunan leveldə açılır, sonra sikkə ilə alınır. Təsdiqlənmiş qayda: hər uğurla yerləşdirilmiş mərtəbə 1 XP qazandırır; cəhd 10 mərtəbəyə çatmasa da bu XP verilir. Nailiyyət tamamlamaq da XP qazandırır; mövcud üç kateqoriyanın bonusları bölmə 8-də müəyyən edilib. XP toplandıqca level artır. Sikkə almaq və xərcləmək XP vermir. Təsdiqlənmiş qayda: alınmış şəhər genişlənmələri daimidir; təkrar ödəniş tələb edilmir. Təsdiqlənmiş V1 dekorasiyası: sadə ağac, skamya, işıq və yol elementləri sikkə ilə alınır. Bunlar sahə tutur, inventara qaytarıla bilir; gəlir, XP və bina qiyməti bonusu vermir. İnventara qaytarmaq sikkə geri ödənişi deyil.

Təsdiqlənmiş başlanğıc sahə: 8 standart düz binanın təhlükəsiz boşluqlarla yerləşəcəyi ölçü. Bu, sərt 8 bina limiti deyil; əyri binaların faktiki tutduğu sahə və yerləşdirilmiş dekorasiya tutuma təsir edir.

Təsdiqlənmiş qayda: ilk şəhər genişlənməsi level 3-də, yəni ümumilikdə 150 XP toplananda alış üçün açılır və 200 sikkəyə alınır. Açılma ərazini avtomatik vermir. İlk genişlənmə 4 standart düz bina üçün əlavə sahə verir; başlanğıc sahə ilə birlikdə ümumi ölçü 12 standart düz binaya uyğundur. Əyrilik, təhlükəsiz boşluqlar və dekorasiya yerləşdirmədə nəzərə alınır.

Təsdiqlənmiş qayda: ikinci şəhər genişlənməsi level 5-də, yəni ümumilikdə 500 XP toplananda alış üçün açılır və 400 sikkəyə alınır. 5 standart düz bina üçün əlavə sahə verir. Başlanğıc və ilk genişlənmə ilə birlikdə ümumi sahə 17 standart düz bina ölçüsünə çatır; faktiki tutum əyrilik və dekorasiyadan asılıdır.

Təsdiqlənmiş qayda: üçüncü şəhər genişlənməsi level 9-da, yəni ümumilikdə 1.800 XP toplananda alış üçün açılır, 900 sikkəyə alınır və 6 standart düz bina üçün əlavə sahə verir. Başlanğıc və ilk iki genişlənmə ilə birlikdə ümumi sahə 23 standart düz bina ölçüsünə çatır; faktiki tutum əyrilik və dekorasiyadan asılıdır.

Təsdiqlənmiş qayda: dördüncü şəhər genişlənməsi level 13-də, yəni ümumilikdə 3.900 XP toplananda alış üçün açılır, 1.500 sikkəyə alınır və 7 standart düz bina üçün əlavə sahə verir. Başlanğıc və ilk üç genişlənmə ilə birlikdə ümumi sahə 30 standart düz bina ölçüsünə çatır; faktiki tutum əyrilik və dekorasiyadan asılıdır.

Təsdiqlənmiş qayda: beşinci şəhər genişlənməsi level 18-də, yəni ümumilikdə 7.650 XP toplananda alış üçün açılır, 2.500 sikkəyə alınır və 8 standart düz bina üçün əlavə sahə verir. Bütün genişlənmələr alındıqda ümumi sahə 38 standart düz bina ölçüsünə çatır; faktiki tutum əyrilik və dekorasiyadan asılıdır.

Təsdiqlənmiş V1 limiti: başlanğıc sahədən əlavə cəmi 5 şəhər genişlənməsi olacaq. Beşinin də açılma leveli, ölçüsü və qiyməti müəyyən edilib. Bütün genişlənmələrin ümumi qiyməti 5.500 sikkədir.

### Level XP hədləri

Təsdiqlənmiş qayda: oyunçu level 1-də başlayır. Level L-dən L+1-ə keçmək üçün əlavə 50 × L XP tələb olunur. İlk keçidlər müvafiq olaraq 50, 100, 150 və 200 XP-dir. Level L-ə çatmaq üçün ümumi XP həddi 25 × L × (L−1) olur: level 2 üçün 50, level 3 üçün 150, level 4 üçün 300, level 5 üçün 500 XP. Artıq qalan XP növbəti keçidə sayılır; qazanılmış ümumi XP itmir.

## 8. Sikkə və irəliləyiş

Bir əsas valyuta var: sikkə. Oyunçu onu oyun nəticəsindən, nailiyyətdən, bina satışından və real pulla sikkə paketindən əldə edir. Satış yalnız sikkə ilədir; real pula çıxarış nəzərdə tutulmur.

Təsdiqlənmiş əsas oyun mükafatı: hər uğurla yerləşdirilmiş mərtəbə 1 sikkə verir. Cəhd 10 mərtəbəyə çatmasa da mükafat qazanılır. Məsələn, 17 uğurlu mərtəbə 17 sikkə və 17 XP verir; nailiyyət və satış gəliri ayrıca hesablanır. Eyni uğurlu mərtəbənin mükafatı təkrar verilmir.

Təsdiqlənmiş nailiyyət qaydası: hər nailiyyət pilləsi XP və sikkə mükafatını hesab üzrə yalnız bir dəfə verir. Eyni şərtin təkrar yerinə yetirilməsi nailiyyət bonusunu yenidən vermir; adi mərtəbə mükafatları qüvvədə qalır. Hündürlük, dəqiq yerləşdirmə və xilasetmə kateqoriyalarının pillə və bonusları aşağıda müəyyən edilib.

Təsdiqlənmiş hündürlük nailiyyəti: bir cəhddə uğurla yerləşdirilmiş 10, 20, 35, 50, 75 və 100 mərtəbə üçün ayrı pillələr var. Birdəfəlik bonuslar: 10 mərtəbə üçün 10 XP və 10 sikkə; 20 mərtəbə üçün 20 XP və 20 sikkə; 35 mərtəbə üçün 35 XP və 35 sikkə; 50 mərtəbə üçün 50 XP və 50 sikkə; 75 mərtəbə üçün 75 XP və 75 sikkə; 100 mərtəbə üçün 100 XP və 100 sikkə. Bonuslar adi mərtəbə mükafatına əlavə verilir. Bütün hündürlük pillələrinin XP və sikkə bonusları təsdiqlənib. Yüksək hədlərin əlçatanlığı prototipdə yoxlanacaq. Təsdiqlənmiş qayda: reklamla davam edilmiş cəhdlər də hündürlük nailiyyətlərini aça bilir. Hər nailiyyət pilləsinin birdəfəlik mükafat qaydası qüvvədə qalır. Reklamdan sonra çatılan tam uğurlu mərtəbə sayı əsas leaderboard-da da sayılır.

Sikkə xərcləri: bazardan bina, fasad və hissələr, level şərti açılmış şəhər ərazisi. Dekorasiya təsdiqlənmiş əlavə sikkə xərcidir. Bina qurmağa giriş haqqı yoxdur.

Balans məqsədi: oyunçu seçim etməli, reklam və alışın sürətləndirmə dəyərini hiss etməlidir. Pulsuz oyunla da görünən irəliləyiş mümkün qalmalıdır. Büdcə çatışmazlığı əsas oyunu dayandırmamalıdır.

Balansın hazırlanma sırası:

1. Adi oyunçu üçün bir hissəyə və bir əraziyə çatma hədəfini cəhd sayı ilə seçmək.
2. Orta nəticəyə görə sikkə və XP mükafatını hesablamaq.
3. Nailiyyət bonuslarını və uzunmüddətli alış qiymətlərini yerləşdirmək.
4. Reklam bonusunun və sikkə paketinin qazandırdığı vaxtı müqayisə etmək.
5. Sikkə yaradılması, xərclənməsi, bazar dövriyyəsi və alışa çatma müddətinə əsasən düzəliş etmək.

Oyun/nailiyyət və alışlar yeni sikkə yaradır; oyunçulararası satış onu ötürür. Sistem mağazası, ərazi və təklif olunan satış komissiyası sikkəni dövriyyədən çıxarır. İlkin rəqəmlər bölmə 19-da seçilib; iqtisadiyyat sınağında yoxlanacaq.

Təsdiqlənmiş dəqiq yerləşdirmə nailiyyəti: bir cəhddə mərkəzə dəqiq yerləşdirilən mərtəbələrin sayı əsas götürülür; ardıcıl olması tələb edilmir. Aradakı qeyri-dəqiq enişlər sayğacı sıfırlamır. Yeni cəhddə sayğac yenidən başlayır. Təsdiqlənmiş pillələr: bir cəhddə 3, 5, 10, 20 və 30 dəqiq yerləşdirmə. Birdəfəlik bonuslar: 3 dəqiq yerləşdirmə üçün 5 XP və 5 sikkə; 5 dəqiq yerləşdirmə üçün 10 XP və 10 sikkə; 10 dəqiq yerləşdirmə üçün 20 XP və 20 sikkə; 20 dəqiq yerləşdirmə üçün 40 XP və 40 sikkə; 30 dəqiq yerləşdirmə üçün 60 XP və 60 sikkə. Bonuslar adi mərtəbə mükafatına əlavə verilir. Bütün dəqiq yerləşdirmə pillələrinin bonusları təsdiqlənib. Təsdiqlənmiş qayda: reklamla davam edəndə dəqiq yerləşdirmə sayğacı qorunur və sonrakı dəqiq enişlər onun üzərinə əlavə olunur. Davam eyni cəhd sayılır; sayğac yalnız yeni cəhd başlayanda sıfırlanır. Hər pillənin birdəfəlik XP və sikkə mükafatı qaydası qüvvədədir.

Təsdiqlənmiş xilasetmə nailiyyəti: oyunçu növbəti mərtəbəni düzgün yerləşdirib binanı təhlükəli əyilmədən sabit vəziyyətə qaytaranda xilasetmə sayılır. Reklamla binanın sabitləşdirilməsi xilasetmə sayılmır. Təsdiqlənmiş qayda: xilasetmələr hesab üzrə bütün oyunlar boyunca toplanır; sayğac yeni cəhd başlayanda sıfırlanmır. Təsdiqlənmiş pillələr: ümumilikdə 1, 5, 15, 30, 60 və 100 xilasetmə. Birdəfəlik bonuslar: ilk xilasetmə üçün 5 XP və 5 sikkə; 5 xilasetmə üçün 10 XP və 10 sikkə; 15 xilasetmə üçün 25 XP və 25 sikkə; 30 xilasetmə üçün 50 XP və 50 sikkə; 60 xilasetmə üçün 90 XP və 90 sikkə; 100 xilasetmə üçün 150 XP və 150 sikkə. Bonuslar adi mərtəbə mükafatına əlavə verilir. Son üç pillənin bonusları istifadəçinin verdiyi səlahiyyətlə ilkin balans kimi seçilib; prototipdə yoxlanacaq. Xilasetmə pillələrinin ümumi bonusu 330 XP və 330 sikkədir. Təsdiqlənmiş sayma qaydası: düzgün mərtəbə yerləşdirməsi binanı təhlükəli tarazlıqdan sabit tarazlığa qaytaranda bir xilasetmə sayılır. Ani yellənmə zamanı düz vəziyyətdən keçmək xilasetmə deyil. Hər təhlükə epizodu yalnız bir xilasetmə verir; növbəti xilasetmə üçün bina yenidən təhlükəli tarazlığa düşməlidir. Təhlükə və sabitliyin ədədi hədləri fizika prototipində tənzimlənəcək. Hər pillənin XP və sikkə bonusu hesab üzrə yalnız bir dəfə verilir.

## 9. Bazar və qiymət limiti

Hər bina və hissə üçün sistem tərəfindən müəyyən edilən dəyər çəkisi saxlanılır. Qiymət hissənin pulsuz və ya pulla əldə olunmasından asılı təsadüfi mükafatla yox, onun təyin edilmiş çəkisi ilə hesablanır.

Baza dəyəri = tikinti dəyəri + sakin tutumu dəyəri + fasad və hissələrin dəyəri.

Maksimum satış qiyməti = baza dəyəri × (1 + icazəli marja).

İlkin sınaq balansı: maksimum marja 20%-dir; yekun maksimum qiymət aşağı tam sikkəyə yuvarlaqlaşdırılır. Mərtəbə və ondan yaranan sakin dəyəri birlikdə balanslanmalıdır ki, eyni böyüklük iki dəfə həddindən artıq qiymətləndirilməsin. Fasadı hesabda açmaq qiyməti ilə binaya əlavə etdiyi dəyər ayrı parametrlərdir.

Satıcı limit daxilində qiyməti seçir. Bazar hündürlük, görünüş və qiymət üzrə süzgəc, bina baxışı və elanın geri götürülməsini təmin edir. Dəyər təkcə elan qiymətlərinə əsaslanaraq özbaşına artmır. Limit satış zəmanəti vermir.

Təklif olunan V1 qaydaları:

- Təsdiqlənmiş V1 qaydası: oyunçu yalnız öz qurduğu və sahibi olduğu binanı sata bilər. Bazardan alınmış bina şəhərdə istifadə oluna bilər, lakin təkrar satıla bilməz.
- Təsdiqlənmiş qayda: satışa çıxarılan bina şəhərdə qalır; elanın dərc edilməsi onu şəhərdən çıxarmır. Bir bina eyni anda yalnız bir elanda ola bilər.
- Elan geri götürüləndə bina əvvəlki yerində qalır. Satış tamamlananda bina satıcının şəhərindən və ya inventarından çıxır, alıcıya keçir; satıcıda nüsxəsi qalmır. Təsdiqlənmiş qayda: elan aktivkən binanın şəhərdə yerini dəyişmək sərbəstdir, yerləşdirmə yoxlamaları qüvvədə qalır. Fasad və hissələri dəyişmək üçün əvvəl elan geri götürülməlidir; dəyişiklikdən sonra bina yenidən qiymətləndirilərək limit daxilində yeni elan verilə bilər.
- İlkin sınaq balansı: uğurlu satışdan 5% komissiya tutulur, yuxarı tam sikkəyə yuvarlaqlaşdırılır (minimum 1 sikkə). Satıcıya qiymət − komissiya çatır; elandan əvvəl xalis gəlir görünür. Satılmayan elan üçün haqq yoxdur.
- Təsdiqlənmiş limit: hesab üzrə eyni anda maksimum 5 aktiv satış elanı.
- Sikkə, bina və mükafat əməliyyatları təkrar icrada çoxalmır; iki alıcı eyni binanı ala bilmir.
- Limitsiz bina nüsxələmə, auksion və oyunçulararası sikkə hədiyyəsi V1-də yoxdur.

## 10. Rekord və leaderboard

Əsas nəticə uğurla yerləşdirilmiş mərtəbələrin sayıdır. Lokal rekord saxlanılır. Alınmış bina şəxsi tikinti rekordunu artırmır.

Təsdiqlənmiş qayda: bütün zamanlar üzrə ümumi və həftəlik leaderboard birlikdə olacaq. Həftəlik sıfırlanma yalnız həftəlik sıralamaya təsir edir; ümumi sıralama, şəxsi rekord, şəhər, sikkə, XP və nailiyyət irəliləyişi qorunur. Onlayn nəticələr yoxlanılsın. Təsdiqlənmiş vaxt qaydası: hamı üçün həftə Asia/Baku vaxt zonasında bazar ertəsi 00:00-da başlayır; cəhdin nəticəsi Firebase tərəfindən qəbul edildiyi həftəyə yazılır. Bərabər mərtəbə sayında nəticəsi server tərəfindən daha əvvəl qəbul edilən oyunçu yuxarıda olur. Bu qayda cihazdakı bitmə anını əvəz edir; həftə sərhədində gecikən nəticə yeni həftəyə düşür. Eyni qəbul vaxtı üçün sabit əlavə sıralama açarı texniki layihələndirmədə seçiləcək. Təsdiqlənmiş qayda: reward reklamla davam edilmiş cəhddə çatılan tam uğurlu mərtəbə sayı əsas leaderboard-da sayılır. Məsələn, 20 mərtəbədə uduzub reklamdan sonra 35 uğurlu mərtəbəyə çatan oyunçunun yarış nəticəsi 35-dir. Hər cəhddə maksimum bir dəfə davam qaydası qüvvədə qalır.

## 11. Reklam və ödəniş

Gəlir modeli: könüllü reward reklam, nəticə fasiləsində məhdud adi reklam, sikkə paketləri və əlavə görünüşlər.

Reward ilə hər cəhddə maksimum bir dəfə davam etmək olar. Uğursuz mərtəbə silinir, bina təhlükəsiz vəziyyətə qaytarılır, oyun fasilədə açılır. Reklam mükafatı yalnız cihazdakı reklam SDK-sının “mükafat qazanıldı” bildirişindən sonra verilir; hər reklam cəhdinin ayrıca qeydi olur və eyni qeyd ikinci mükafat yaratmır. Bu, serverdə baxışın həqiqiliyinin təsdiqi deyil; qalan risk bölmə 22-də qəbul edilib.

Görünüşləri reklamla açmaq üçün tələb olunan baxış sayı əvvəlcədən göstərilir. Adi reklam tikinti zamanı çıxmır; həm oyun sayı, həm keçən vaxt şərtinə bağlanır. Təsdiqlənmiş qayda: ilk oyun sessiyasında adi avtomatik tam ekran reklam göstərilmir; reward reklamlar yalnız oyunçunun istəyi ilə əlçatandır. Təsdiqlənmiş sərhəd: ilk sessiya güzəşti ilk uğurlu onlayn giriş gününün sonunadək qüvvədədir; tətbiqi bağlayıb açmaq bu güzəşti bitirmir. Təsdiqlənmiş vaxt zonası Asia/Baku-dur: güzəşt ilk uğurlu onlayn giriş günündən sonrakı Bakı gecəyarısında bitir, 24 saatlıq müddət deyil. İlk açılış oflayndırsa, məşq reklamsız qalır və güzəşt günü hələ başlamır. Təsdiqlənmiş qayda: reward reklam tamamlananda adi reklam üçün vaxt və cəhd sayğacları sıfırlanır; növbəti adi reklam yalnız hər iki hədd yenidən dolduqdan sonra uyğun nəticə fasiləsində göstərilə bilər. Reward-dan dərhal sonra adi reklam göstərilmir. Təsdiqlənmiş interval: adi reklam üçün ən azı 4 bitmiş cəhd və 5 dəqiqə aktiv oyun vaxtı birlikdə tələb olunur; göstərilmə yalnız nəticə fasiləsindədir. Fasilə, arxa plan, menyu və reklam vaxtı aktiv oyun vaxtına daxil deyil. Adi reklam tamamlananda da hər iki sayğac sıfırlanır. Tətbiqi yenidən açmaq sayğacları sıfırlamır. Reklam yoxdursa yenidən başlama sərbəstdir.

Real alışlarda bölmə 22-də qəbul edilmiş məhdud qorunan cihaz yoxlaması və token qeydi modeli əsas götürülür; müstəqil server təsdiqi yoxdur və təkrar kreditləşməyə qarşı model hələ sınaqdan keçirilməyib. Ödənişli görünüşlər fiziki üstünlük vermir. Paketlər, qiymətlər və platforma inteqrasiyası ayrıca hazırlıq mərhələsində seçilir.

## 12. Tam oyun axını

1. İlk açılış: qısa təqdimat və pulsuz başlanğıc üslubu seçimi. Təsdiqlənmiş qayda: oyunçu qonaq kimi dərhal oynaya və şəhərini qura bilər. Bazarda alış-satışdan və real pul alışından əvvəl hesab bağlama tələb olunur. Qonaq irəliləyişi bağlanan hesaba köçürülür. Təsdiqlənmiş konflikt qaydası: hesabda artıq irəliləyiş varsa, qonaq və hesab irəliləyişləri birləşdirilmir; oyunçu yalnız birini seçir. Seçimdən əvvəl hər ikisinin leveli, sikkəsi və bina sayı göstərilir; seçilməyən irəliləyişin aktiv oyuna köçürülməyəcəyi aydın bildirilir. Seçim yalnız şəhərə yox, bütün oyun irəliləyişinə aiddir. Təsdiqlənmiş bərpa qaydası: hesabına qayıdan oyunçunun daimi pullu görünüş hüquqları bərpa edilir; əvvəl çatdırılmış sikkə paketləri yenidən verilmir. Bu fərq qonaq/hesab irəliləyişi seçimində də qorunur. Texniki təsdiq və hüquqların hesaba bağlanması ayrıca həll edilməlidir.
2. İlk cəhd: “Mərtəbəni buraxmaq üçün toxun” təlimatı və bağışlayıcı ilk mərtəbələr.
3. Tikinti: buraxma, eniş, yellənmə, tarazlığı düzəltmə və artan çətinlik.
4. Minimum hündürlük: 10 uğurlu mərtəbədən sonra bina saxlanmağa uyğun olur; oyunçu tikintiyə davam edir.
5. Məğlubiyyət: aydın aşma/düşmə reaksiyası. Uyğun halda bir dəfə reklamla davam et və ya nəticəyə keç.
6. Nəticə: mərtəbə, rekord, sıralama uyğunluğu, sikkə/XP və nailiyyət irəliləyişi. Mükafat bir dəfə hesablanır.
7. Bina saxlamaq: uyğun bina inventara əlavə edilir. Bina yoxdursa rahat yeni cəhd imkanı qalır.
8. Şəhərə yerləşdirmək: bina önizləməsi, əyriliyə uyğun sahə yoxlaması və yerləşdirmə/dəyişdirmə.
9. İnkişaf: sakin göstəricisi, görünüşlər, dekorasiya və levelə uyğun ərazi alışları.
10. Satmaq: bina detalından baza dəyəri, maksimum limit, qiymət və təklif olunan komissiya görünür; uyğun hesabla elan dərc edilir.
11. Almaq: bazar baxışı, bina və sakin məlumatı, qiymət, təsdiq; bina inventara keçir.
12. Yenidən oynamaq: alınmış/açılmış hissələrdən görünüş seçmək və bir toxunuşla yeni cəhd.
13. Sonrakı giriş: şəhər, satış nəticəsi və nailiyyət irəliləyişi görünür. Cəza və məcburi gündəlik iştirak yoxdur.

İnternet kəsiləndə bazar əməliyyatı uğurlu kimi göstərilmir. Təsdiqlənmiş qayda: oflayn məşq sikkə, satıla bilən bina və leaderboard nəticəsi yaratmır. Təsdiqlənmiş qayda: onlayn cəhddə internet kəsiləndə oyun avtomatik fasiləyə düşür və mövcud cəhd qorunur. Bağlantı qayıdanda oyunçu “Davam et” ilə qaldığı yerdən oynayır; oyun özbaşına başlamır. İnternet bərpa olunmadan mükafat və nəticə təsdiqlənmir.

## 13. Ekranlar

- Ana ekran: Oyna, şəhər, rekord, sikkə və level; bazar, kolleksiya və leaderboard-a giriş.
- Oyun: mərtəbə sayı, rekord, fasilə və ilk cəhddə qısa təlimat.
- Fasilə: davam et, yenidən başla, səs, vibrasiya.
- Nəticə: nəticələr, mükafatlar, uyğun binanı saxlamaq və yenidən oynamaq.
- Şəhər: yerləşdirmə, dəyişdirmə, ərazi və sakin göstəricisi.
- İnventar və bina detalı: forma, hissələr, mərtəbə, sakin, dəyər və satış əməliyyatları.
- Kolleksiya/mağaza: hissələr və əldəetmə şərtləri; sikkə paketləri.
- Bazar: süzgəclər, bina baxışı, alış və şəxsi elanlar.
- Leaderboard, nailiyyətlər, hesab və parametrlər.

Bu siyahı ekranların funksional planıdır. Vizual dizayn başlayanda ui-craft, mərkəzi tokenlər, kontrast və render yoxlaması tətbiq ediləcək.

## 14. İlk ictimai versiya

Təsdiqlənmiş V1 əhatəsi: bir sonsuz rejim, əsas fizika, kamera, səs/vibrasiya/fasilə, lokal rekord, sadə canlı şəhər, inventar, sakin tutumu, bina dəyişdirmə, level və ərazi genişlənməsi, ilk gündən seçilən 6 pulsuz üslub, məhdud əlavə hissələr və sadə dekorasiya/yol elementləri, nailiyyət/sikkə sistemi, sadə bazar, qiymət limiti, leaderboard, reklam və alışlar. İstifadəçi bütün bu sistemlərin ilk ictimai versiyada olmasını təsdiqləyib; onların daxilində təklif kimi göstərilən qaydalar ayrıca dəqiqləşdiriləcək.

Bu, ilkin reklamsız prototipdən daha böyük ictimai buraxılışdır. Bazar, hesab, ödəniş və server funksiyaları əsas mexanika yoxlanandan sonra hazırlanır. Kiçik qapalı sınaq bütün V1 funksiyaları tamamlanmadan keçirilə bilər.

V1 xaricində: canlı çoxoyunçulu oyun, çat, real pul çıxarışı, auksion, avtomatik sakin iqtisadiyyatı, mürəkkəb xidmət/şəhər simulyasiyası, limitsiz bina nüsxələmə və mövsüm sistemləri.

## 15. Hazırlama mərhələləri

Köhnə mərhələ siyahısı arxivə köçürülüb. İşlək plan: [PLAN.md](PLAN.md).

## 16. Açıq qərarlar

1. Bağlanıb: yalnız Android; iOS versiyası olmayacaq. Geniş Android V1 əhatəsi təsdiqlənib.
2. Bağlanıb: minimum saxlanma və satış həddi 10 mərtəbədir; tikinti davam edir, könüllü tamamlama seçimi yoxdur.
3. Bağlanıb: məğlubiyyətdə son uğurlu bina saxlanılır.
4. Bağlanıb: hər mərtəbəyə 4 sakin; sakinlər passiv sikkə gəliri yaratmır.
5. Xilasetmənin sayma qaydası və mövcud üç nailiyyət kateqoriyasının bütün XP/sikkə bonusları müəyyən edilib. Təhlükə/sabitliyin ədədi hədləri prototipdə tənzimlənəcək. Beş şəhər genişlənməsinin bütün parametrləri təsdiqlənib (bölmə 7). Level L-dən L+1-ə keçid üçün əlavə 50 × L XP, hər uğurlu mərtəbəyə 1 XP (10-dan aşağı cəhdlər daxil) və sikkə alışı/xərclənməsinin XP verməməsi təsdiqlənib.
6. Başlanğıcda 6 pulsuz üslub, yalnız nailiyyətlə açılan xüsusi hissələr və hazır binanın açıq hissələrlə redaktəsi təsdiqlənib (bölmə 6). Konkret hissə kataloqu və hər hissənin əldəetmə yolu bölmə 23-də təsdiqlənib.
7. İlkin qiymət çəkiləri, 20% maksimum marja və 5% komissiya bölmə 19-da tamamlanıb; sınaqda yoxlanacaq. Eyni anda 5 aktiv elan limiti təsdiqlənib.
8. Bağlanıb: satışdakı bina şəhərdə qalır və yeri dəyişdirilə bilər; fasad/hissə dəyişikliyi üçün elan əvvəl geri götürülür. V1-də yalnız öz qurduğu bina satılır; alınmış bina təkrar satılmır.
9. Bağlanıb: reward reklamdan sonra çatılan tam uğurlu mərtəbə sayı əsas leaderboard-da sayılır.
10. Oflayn rejim məşqdir; sikkə, XP, nailiyyət, satıla bilən bina və leaderboard yalnız onlayn qazanılır (təsdiqlənib). Oflayn bina şəhər və inventara saxlanmır (təsdiqlənib). Bağlantı kəsiləndə avtomatik fasilə, cəhdin qorunması və bağlantıdan sonra istifadəçinin “Davam et” seçimi təsdiqlənib. Təsdiqlənmiş qayda: tətbiq tam bağlananda yarımçıq cəhd son tamamlanmış mərtəbədən fasilədə bərpa olunur. Düşməkdə olan mərtəbə varsa yenidən kranda təqdim edilir. Tamamlanmış mərtəbələrin mövqeyi, tarazlıq vəziyyəti, sayğaclar və istifadə olunmuş reklamla davam hüququ qorunur; əvvəlki mükafatlar təkrar verilmir. Onlayn cəhdi davam etdirmək üçün internet tələb olunur. Artıq məğlubiyyətlə bitmiş cəhd yarımçıq cəhd kimi geri açılmır.
11. Adi reklam intervalı və ilk günün vaxt zonası bağlanıb (bölmə 11). Görünüş reklamları və sikkə paketləri üçün ilkin balans bölmə 19-dadır; real pul qiymətləri və platforma inteqrasiyası açıqdır.

12. Bağlanıb: şəhərdə bina forması sabitdir; sadə sakin/avtomobil, işıq və ağac animasiyaları var; köçmə, aclıq və xidmət idarəetməsi yoxdur. Ağac, skamya, işıq və yol dekorasiyası sikkə ilə alınır, sahə tutur, inventara qaytarılır və iqtisadi/XP bonusu vermir (bölmə 7).

Məzmun kataloqu bölmə 23-də təsdiqlənib. Mühərrik seçimi, real pul qiymətləri və Spark texniki layihələndirməsi açıq qalır. İlkin iqtisadi balans bölmə 19-da tamamlanıb. Kod və vizual hazırlıq yalnız ayrıca tapşırıqla başlayır.

## 17. Firebase məhdudiyyəti

Təsdiqlənmiş texniki qərar: layihə Firebase Spark planında qalacaq. Ayrıca server və Blaze tələb edən Cloud Functions istifadə olunmayacaq. Bu qərar əvvəlki serverdə nəticə/alış yoxlaması ifadələrini hazır texniki həll kimi qəbul etməyə imkan vermir.

Bazarın mülkiyyət və sikkə əməliyyatları üçün Firebase Security Rules və tranzaksiyalarla mümkün olan təminatlar ayrıca layihələndirilib yoxlanmalıdır. Oflayn nəticələrin həqiqiliyinin yoxlanması və real pul alışlarının etibarlı təsdiqi üçün Spark daxilində həll hələ müəyyən edilməyib. Bu imkanlar həll edilmiş sayılmır; lazım gələrsə funksiyaların əhatəsi istifadəçi ilə yenidən dəqiqləşdiriləcək. Tam V1 məhsul məqsədi saxlanılır, texniki mümkünlük yoxlaması açıqdır.

## 18. Balans qərarlarının idarə edilməsi

İstifadəçinin göstərişinə əsasən qalan XP və sikkə bonuslarını agent mövcud iqtisadiyyata uyğun ilkin balanslaşdırır; hər məbləğ üçün ayrıca təsdiq istənilmir. Əvvəldən təsdiqlənmiş rəqəmlər saxlanılır. Yeni rəqəmlər sınaq balansı kimi qeyd olunur; əsas oyun qaydaları istifadəçi ilə ayrıca müzakirə edilir.


## 19. Tamamlanmış ilkin XP və sikkə balansı

3 noyabr 2026: istifadəçinin verdiyi səlahiyyətlə aşağıdakı rəqəmlər ilkin sınaq balansı kimi seçilib. Bunlar ölçülmüş oyun məlumatı deyil. Əvvəl təsdiqlənmiş mərtəbə mükafatları, nailiyyətlər, level hədləri və beş genişlənmə dəyişmir. Yeni əsas oyun mexanikası və əlavə XP mənbəyi yaradılmır.

### XP və adi irəliləyiş

- Hər uğurlu mərtəbə: 1 XP və 1 sikkə. Alış, satış, reklam baxışı və bina silmək ayrıca XP vermir. Reward ilə davamdan sonra yerləşdirilən yeni mərtəbələr adi mükafatını verir.
- Hündürlük pillələrinin cəmi 290 XP + 290 sikkə, dəqiq eniş pillələrinin cəmi 135 XP + 135 sikkə, xilasetmə pillələrinin cəmi 330 XP + 330 sikkədir. Mövcud bütün nailiyyətlərin birdəfəlik ümumi bonusu 755 XP + 755 sikkədir.
- Hesablama fərziyyəsi: orta cəhd 15 uğurlu mərtəbə. Bonuslar və xərclər nəzərə alınmadan 100 sikkə təxminən 7, 200 sikkə 14, 400 sikkə 27 cəhddir. Bunlar çətinlik və müddət zəmanəti deyil.
- Bütün nailiyyətlər açılıbsa level 18 üçün 7.650 ümumi XP-nin ən azı 6.895-i mərtəbələrdən gəlir. Eyni ümumi 7.650 sikkə gəlirindən bütün ərazilərə 5.500 xərcləndikdə 2.150 qalır; mağaza xərcləri bu ehtiyatla rəqabət aparır. Ərazi açılması avtomatik alış demək deyil.

### Hissə və görünüş qiymətləri

- Təsdiqlənmiş 6 başlanğıc üslubu ilk gündən pulsuz seçilir, bina dəyərinə əlavə çəkisi 0-dır. Konkret üslubların təsdiqlənmiş məzmunu bölmə 23-dədir.
- Sikkə ilə daimi açılan hissələr üçün üç qiymət pilləsi: 100, 250 və 600 sikkə. Orta fərziyyə ilə təxminən 7, 17 və 40 cəhd; nailiyyətlər bunu qısaldır. Hər kateqoriyanın təsdiqlənmiş hissələri bölmə 23-dədir.
- Reklamla daimi açılan hissələr üçün ilkin tələb: sadə 3, orta 5, xüsusi 8 tamamlanmış reward baxışı. Qismən irəliləyiş qorunur. Birbaşa sikkə verən əlavə reward təklif edilmir; mövcud reward məqsədləri davam və hissə açmaqdır.
- Bina dəyərinə əlavə çəki əldəetmə yolundan asılı deyil: pulsuz baza hissəsi 0, əlavə sadə hissə 4, orta 10, xüsusi 24 sikkə. Beş kateqoriyanın hər birindən yalnız binada faktiki istifadə edilən bir hissə sayılır; eyni hissə hər mərtəbədə təkrar qiymətləndirilmir. Maksimum kosmetik əlavə 120 sikkədir. Nailiyyət/reklam/real pulla açılan hissə də eyni vizual pillənin çəkisini alır.
- Təsdiqlənmiş sadə dekorasiya/yol əhatəsi üçün ilkin qiymət pillələri: kiçik 25, orta 75, böyük 150 sikkə. Konkret elementlərin təsdiqlənmiş pillə bölgüsü bölmə 23-dədir. Alış və yerləşdirmə XP, gəlir və bina qiyməti bonusu vermir.

### Bina qiyməti və bazar nümunələri

Uğurlu mərtəbə sayı F, sakin sayı R = 4F, istifadə edilən hissələrin ümumi çəkisi H olsun. Tikinti dəyəri F, sakin dəyəri R / 4 seçilir: baza dəyəri B = F + R / 4 + H = 2F + H. Sakin və hündürlük birlikdə hər mərtəbəyə cəmi 2 sikkə verir; əlavə gizli ölçü əmsalı yoxdur. Dəqiq eniş və əyrilik V1-də əlavə qiymət bonusu yaratmır.

Satıcı tam sikkə ilə 1-dən floor(1,20 × B)-yə qədər qiymət seçir. Satışdan ceil(0,05 × qiymət), minimum 1 sikkə komissiya çıxılır. Bu məbləğlər yalnız uğurlu oyunçulararası satışda ötürülür; sistem binanı avtomatik almır.

- 10 mərtəbə, 40 sakin, pulsuz hissələr: baza 20, maksimum qiymət 24, komissiya 2, satıcıya 22 sikkə.
- 20 mərtəbə, 80 sakin, bir orta hissə: baza 50, maksimum qiymət 60, komissiya 3, satıcıya 57 sikkə.
- 35 mərtəbə, 140 sakin, beş xüsusi hissə: baza 190, maksimum qiymət 228, komissiya 12, satıcıya 216 sikkə.

Hissəni hesabda bir dəfə açmaq gələcək binalarda təkrar istifadəyə imkan verir; satış gəliri alıcının mövcud sikkəsindən gəlir, yeni sikkə yaratmır. Alınmış binanı yenidən satmaq qadağası qüvvədədir.

### Sikkə paketləri və yoxlama

İlkin paket məzmunları: 500, 1.200 və 2.500 sikkə; XP, fiziki üstünlük və level şərtini keçmək hüququ vermir. Orta 15 mərtəbə fərziyyəsi ilə bunlar təxminən 34, 80 və 167 cəhdin əsas sikkə gəliridir. Real pul qiyməti, qiymətə görə paket sərfəliliyi və alışların təsdiqi platforma mərhələsində həll ediləcək; bu plan gəlir proqnozu vermir.

Qapalı sınaqda ilk 100 sikkəlik hissəyə çatma, ilk əraziyə çatma/alış, ümumi sikkə mənbə və xərcləri, bazarda real satış payı, reklamla hissə açma və paketlərin qənaət etdiyi oyun vaxtı ölçüləcək. Əvvəl yeni qiymətlər və çəkilər tənzimlənir; əvvəldən təsdiqlənmiş rəqəmlər səssiz dəyişdirilmir. Firebase Spark ilə etibarlı nəticə və alış yoxlaması həll olunana qədər bu iqtisadiyyat texniki olaraq hazır sayılmır.


## 20. Son təsdiqlənmiş qərar paketi

- Məzmun kataloqu agent tərəfindən hazırlanıb və istifadəçi tərəfindən 3 noyabr 2026-da təqdim edilən formada təsdiqlənib. Altı üslubun, əlavə hissələrin və dekorasiyaların konkret adları, sayları və əldəetmə yolları bölmə 23-dədir.
- Üç xüsusi nailiyyət hissəsi olacaq: hündürlük kateqoriyası üçün bir cəhddə 50 mərtəbə; dəqiq eniş üçün bir cəhddə 20 dəqiq yerləşdirmə; xilasetmə üçün hesab üzrə 30 xilasetmə. Hər kateqoriya bir xüsusi hissə açır. Hissələr başqa yolla açılmır; mövcud XP/sikkə bonusları saxlanılır. Təsdiqlənmiş hissə adları və kateqoriyaları bölmə 23-dədir.
- Daimi pullu görünüşlər hesaba qayıdışda bərpa olunur; əvvəldən çatdırılmış sikkə paketi təkrar verilmir. Qonaq/hesab seçimi bu fərqi dəyişmir.
- Koddan əvvəl Spark daxilində bazar, nəticə/vaxt və real alış təsdiqi araşdırılır. Etibarlı həlli tapılmayan funksiyanın ictimai buraxılışa daxil edilməsindən əvvəl alternativ istifadəçi ilə ayrıca razılaşdırılacaq. Mövcud V1 məqsədi avtomatik kiçildilmir və Blaze/ayrıca serverə avtomatik keçilmir.

## 21. Firebase Spark texniki mümkünlük araşdırması

Yoxlama tarixi: 3 noyabr 2026. Rəsmi sənədlər əsasında layihəyə tətbiq olunan nəticələrdir; Firebase layihəsinə giriş, qayda yazılması, emulator və real cihaz sınağı aparılmayıb. Aşağıdakı dizayn təklifləri işləyən təminat kimi qəbul edilməməlidir.

### Plan sərhədi və saxlama

Spark Cloud Functions deploy etməyə imkan vermir; Cloud Run/Pub/Sub kimi Google Cloud xidmətləri billing tələb edərək layihəni Blaze-a keçirir. Buna görə bunlar cari məhdudiyyətə uyğun həll deyil. [Firebase planları](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans).

Cloud Firestore Standard üçün pulsuz kvota: 1 GiB məlumat, gündə 50.000 sənəd oxuma, 20.000 yazma, 20.000 silmə, ayda 10 GiB çıxış trafiki. Bir layihədə yalnız bir pulsuz baza var; gündəlik kvota Pacific gecəyarısı ətrafında yenilənir, oyunun Bakı vaxtı qaydası ilə eyni deyil. TTL silmə billing tələb edir. Bir sənədin həddi 1 MiB-dir. Deməli, inventarı bir böyüyən sənəddə saxlamaq olmaz; bina qeydləri ayrıca sənədlərə və lazım gəlsə hissələrə bölünməlidir. Oyun limiti qoyulmaması fiziki limitsiz saxlama deyil. [Firestore limitləri](https://firebase.google.com/docs/firestore/quotas).

Cloud Storage for Firebase üçün 3 fevral 2026-dan Blaze tələb olunur. Spark dizaynında bazar şəkillərinin Storage-a yüklənməsinə arxalanmaq olmaz. Təklif: görünüş resursları tətbiqdə olsun, bina həndəsə/hissə identifikatorları ilə Firestore-da saxlanılsın, önizləmə cihazda qurulsun. Bu, hazır implementasiya deyil. [Storage billing tələbi](https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024).

### Hesab və şəhər: kvota daxilində mümkün

Firebase anonim hesabı sonradan giriş məlumatına bağlamağı dəstəkləyir. Bu, qonaqdan hesaba keçid üçün uyğundur; artıq mövcud hesabla konflikt və irəliləyiş seçimi tətbiq səviyyəsində ayrıca layihələndirilməlidir. Şəhər, inventar və hesabın qorunan saxlanması prinsipcə Spark-da mümkündür. [Anonim Android hesabları](https://firebase.google.com/docs/auth/android/anonymous-auth).

### Bazar: atomik mübadilə prinsipcə mümkün, iqtisadi etibarlılıq ayrıca problemdir

Firestore tranzaksiyaları və Rules getAfter() əlaqəli sənədlərin eyni atomik əməliyyatda dəyişməsini yoxlamağa imkan verir. Buna əsasən bina mülkiyyəti, alıcının balansının azalması, satıcının xalis gəliri və elanın bağlanması birlikdə qoruna bilər. Bütün əməliyyat üçün 20, hər ayrıca sənəd əməliyyatı üçün 10 sənəd giriş çağırışı limiti var. [Tranzaksiyalar və getAfter](https://firebase.google.com/docs/firestore/manage-data/transactions).

Dizayn namizədi: satış tranzaksiyası elan, bina, iki pul kisəsi və təkrar istifadə olunmayan əməliyyat qeydini birlikdə dəyişir. Rules mövcud sahib/yaradıcı, alıcının kimliyi, kifayət qədər balans, qiymət limiti, komissiya, yalnız icazəli sahə dəyişiklikləri və satışın tam bağlanmasını qarşılıqlı yoxlamalıdır. Satıcının pul kisəsinə alıcının sərbəst yazması açılmamalıdır; yalnız uyğun satışa bağlı dəqiq artım qəbul olunmalıdır. Maksimum beş elan üçün hesabda sabit beş elan yuvası namizəd modeldir; təkrar bina elanı da ayrıca bloklanmalıdır. Bu model test olunmayıb.

İki alıcının yarışı, təkrar göndəriş, saxta qiymət/mülkiyyət, natamam ödəniş və silmə/satış yarışı sonradan emulator sınağı tələb edir. Bağlantı kəsildikdə yerli keşdə görünən dəyişiklik satış uğuru sayılmır; server təsdiqi tələb olunur. Atomik mübadilə saxta oyunla yaradılmış ilkin sikkə və binanı etibarlı etmir.

### Nəticə, XP və sikkə: cari modeldə etibarlı oyun sübutu yoxdur

Rules verilənlərin tipini, icazəli dəyişiklikləri və saxlanmış sənədləri yoxlaya bilir; xarici xidmət çağırışı və dövrlər yerinə yetirmir. App Check tətbiq/cihaz attestasiya qatıdır və bütün sui-istifadəni aradan qaldırmağa zəmanət vermir. [Rules məhdudiyyətləri](https://firebase.google.com/docs/firestore/security/rules-conditions), [App Check](https://firebase.google.com/docs/app-check).

Layihə üzrə nəticə: müştərinin göndərdiyi mərtəbə sayı, dəqiq eniş, xilasetmə və fiziki düzülüşü sadəcə onlayn olmaqla həqiqi oyun kimi təsdiqləmək olmaz. Məbləğ artımı və təkrar mükafat Rules ilə məhdudlaşdırıla bilər, amma saxta uyğun cəhdin yaradılması qalır. Etibarlı fizika/replay yoxlayıcısı cari Spark modelində müəyyən edilməyib. Hər mərtəbəni şəbəkədə yoxlamaq ayrıca model ola bilər, lakin gecikmə, kvota və saxta hərəkət problemləri həll olunmadan alternativ sayılmır. Buna görə ictimai yarış və bazar iqtisadiyyatının oyun mənşəyi hələ buraxılış maneəsidir.

### Vaxt: serverin qəbul vaxtı mümkündür, cihazdakı bitmə anı sübut olunmur

Rules request.time xidmətin sorğunu qəbul etdiyi vaxtdır və server timestamp ilə bərabər yoxlanıla bilər. Bu, cihaz saatının saxtalaşdırılmasına qarşı etibarlı qeyd vaxtı verir. [Request.time](https://firebase.google.com/docs/reference/rules/rules.firestore.Request).

Bakı həftə sərhədləri əvvəlcədən verilən təqvim intervalları ilə təsvir edilə bilər; planlaşdırılmış həftəlik silmə tələb olunmadan nəticələr həftə identifikatoru ilə saxlanıla bilər. Lakin gecikmiş nəticənin serverə gəlməsi cihazda cəhdin bitdiyi vaxtla eyni deyil. Təklif olunan alternativ: yarış üçün bitmə vaxtı serverin nəticəni qəbul etdiyi vaxt sayılsın; eyni timestamp olarsa sabit ikinci sıralama açarı istifadə edilsin. Bu dəyişiklik hələ istifadəçi tərəfindən təsdiqlənməyib. İlk reklamsız günün başlanğıcını da ilk təsdiqlənmiş giriş vaxtına bağlamaq təklifdir; ilk açılış oflayn olarsa davranış ayrıca seçilməlidir.

### Real pul alışları və bərpa: təhlükəsiz server təsdiqi həll olunmayıb

Google Play alış tokeninin etibarlı backend-ə göndərilməsini, təkrar tokenin yoxlanmasını və Developer API ilə alışın təsdiqini tövsiyə edir. PENDING vəziyyəti hüquq verməməlidir. Rules xarici API çağıra bilmədiyinə görə tək Spark Rules bu axını yerinə yetirmir; xidmət hesabı sirrini tətbiqə yerləşdirmək uyğun alternativ deyil. [Google Play alış təhlükəsizliyi](https://developer.android.com/google/play/billing/security).

Billing kitabxanası cihazda alışları sorğulamağa və təsdiq/istehlak etməyə imkan verir, lakin müştəri yoxlaması qlobal, etibarlı sikkə kreditləşdirməsini əvəz etmir. Daimi görünüş hüququ ilə istehlak edilən sikkə paketi ayrı saxlanmalıdır; artıq verilmiş paket bərpada yenidən kreditləşməməlidir. Çatdırılma/acknowledgement/consume ardıcıllığı, tokenin bir hesaba bağlanması, refund və cihaz dəyişməsi üçün etibarlı qeyd axını hələ yoxdur. Məhsul qaydası təsdiqlənib, texniki həll tamamlanmayıb. [Play Billing inteqrasiyası](https://developer.android.com/google/play/billing/integrate).

### Reward reklam: cihaz callback-i ilə server təsdiqi fərqlidir

AdMob SSV imzalı callback-i xarici sistemə göndərir və cihaz callback-inin saxtalaşdırılmasına qarşı əlavə müdafiə verir. Cari Spark/serversiz modeldə onu qəbul edib yoxlayan endpoint yoxdur; Rules bu endpoint-i əvəz etmir. SDK ilə cihazda reward vermək mümkündür, amma bunu serverdə təsdiqlənmiş baxış adlandırmaq olmaz. Reklamla davam leaderboard-a təsir etdiyindən bu məsələ də etibarlılıq qərarına daxildir. [AdMob SSV](https://developers.google.com/admob/android/ssv).

### Yekun və növbəti qərar

Spark qonaq/hesab, inventar/şəhər və məhdud kvotalı atomik bazar mexanizmi üçün namizəddir. Tam ictimai V1-in etibarlı oyun mükafatları, yarış nəticələri, real pul kreditləşməsi və serverdə reward təsdiqi üçün cari məhdudiyyətlər daxilində tamamlanmış həll tapılmadı. Bu, bütün serversiz modellərin riyazi qeyri-mümkünlük sübutu deyil; mövcud oyun modelinin buraxılışa hazır olmadığının nəticəsidir.

İstifadəçiyə təqdim ediləcək seçimlər: (a) Spark məhdudiyyətini saxlayaraq ilk buraxılışın əhatə/etibarlılıq səviyyəsini yenidən razılaşdırmaq; (b) tam V1 üçün etibarlı backend məhdudiyyətini yenidən müzakirə etmək. Heç biri avtomatik seçilməyib. Mühərrik seçimi bu maneələri özü həll etmir. Növbəti addım həmin məhsul/texniki seçimi etməkdir; sonra kataloq və mühərrik müqayisəsinə keçmək olar. Hələ kod yazılmır.

### İstifadəçinin seçimi — 3 noyabr 2026

- Firebase Spark məhdudiyyəti və planlaşdırılan bütün V1 funksiyaları saxlanılır; Blaze və ayrıca backend seçilməyib.
- İstifadəçi bütün funksiyaları əvvəl yalnız qapalı sınaqda hazırlayıb ictimai buraxılışı bütün etibarlılıq problemləri həll edilənədək gözlətmək təklifinə «yox» cavabı verib. Bu, sınaqların tam ləğvi və ya dərhal buraxılış göstərişi deyil.
- Spark daxilində etibarlılığı artırmaq üçün funksiyaların işləmə qaydalarını dəyişmək istiqamətinə «hə» deyilib. Konkret dəyişikliklər sonrakı beş cavabla təsdiqlənib və bölmə 22-də qeyd olunub.
- Araşdırmadan sonra bəzi problemlər açıq qalsa, məhdud qoruma ilə ictimai buraxılışa «hə» deyilib. Saxta sikkə və nəticələrin bazara və yarışa təsir edə bilməsi müzakirədə açıq göstərilib və qəbul edilib.
- Bu seçim texniki boşluqları həll olunmuş etmir: oyun nəticələrinin, alışların və reward reklamların etibarlı təsdiqi hələ açıqdır. Platforma tələblərinə uyğunluq ayrıca yoxlanmalıdır.
- Konkret işləmə qaydaları və qəbul edilmiş risklər bölmə 22-dədir. Funksiyalar çıxarılmır; hələ kod yazılmır.

## 22. Spark üçün təsdiqlənmiş işləmə qaydaları və qalan risklər

3 noyabr 2026: istifadəçi aşağıdakı beş təklifin hamısına «hə» cavabı verib. Bunlar məhsul qərarlarıdır; implementasiya, Rules/emulator və real cihaz sınaqları aparılmayıb. Spark və bütün V1 funksiyaları saxlanılır. Riskin qəbul edilməsi texniki təminat və ya platforma uyğunluğunun təsdiqi deyil. Əvvəlki bölmələrdə ziddiyyətli vaxt/reklam/alış ifadələri olduqda bu bölmədəki son qərarlar əsasdır.

### 1. Oyun nəticəsi, XP, sikkə, nailiyyət və bina
- Hər onlayn cəhdin ayrıca identifikatoru olur; mərtəbə irəliləyişi həmin cəhddə saxlanılır. Eyni mərtəbə üçün mükafat ikinci dəfə verilmir.
- Bağlantı kəsiləndə oyun fasiləyə düşür; təsdiqlənməmiş irəliləyiş gözləyir. Bərpada son bulud qeydi əsasdır; əvvəlki mükafatlar təkrar verilmir. Oflayn məşq qaydaları saxlanılır.
- Qəbul edilmiş risk: dəyişdirilmiş tətbiq oynanmamış mərtəbəni oynanmış kimi göndərə bilər. Təkrar mükafatın məhdudlaşdırılması oyunun həqiqiliyini sübut etmir. Son qeyd göndərilmədən tətbiq bağlanarsa, bir qədər irəliləyiş itə bilər.

### 2. Leaderboard və vaxt
- Nəticə yalnız Firebase qəbul etdikdən sonra sıralamaya düşür. Həftə və bərabərlikdə üstünlük serverin qəbul vaxtı ilə müəyyənləşir; həftə Bakı vaxtı ilə bazar ertəsi 00:00-da başlayır.
- Qəbul edilmiş risk: bazar günü bitib bazar ertəsi göndərilən nəticə yeni həftəyə düşür. Saxta oyun nəticəsi problemi qalır. Eyni timestamp üçün sabit əlavə sıralama açarı hələ seçilməyib.

### 3. Bazar və sikkə mübadiləsi
- Pulun alıcıdan çıxılması, satıcının xalis payı, binanın sahibinin dəyişməsi və elanın bağlanması bir atomik əməliyyatda aparılır. Bir hissə uğursuz olsa, heç biri tətbiq olunmur. Uğur yalnız server təsdiqindən sonra göstərilir.
- Oyundan qazanılan sikkə və binalar qalan risklə bazarda istifadə olunur. Mövcud qiymət, komissiya, beş elan və alınmış binanın təkrar satılmaması qaydaları saxlanılır.
- Qəbul edilmiş risk: düzgün mübadilə saxta oyunla yaradılmış sikkə və binanı etibarlı etmir. Spark kvotası dolarsa alışlar müvəqqəti işləməyə bilər. Rules modeli hələ yoxlanmayıb.

### 4. Reward reklam və ilk gün güzəşti
- Reward yalnız cihazdakı reklam SDK-sının “mükafat qazanıldı” bildirişindən sonra verilir. Hər reklam cəhdi ayrıca qeyd olunur; eyni qeyd ikinci mükafat yaratmır.
- Hər oyun cəhdində maksimum bir reklamla davam qalır; onun tam nəticəsi əsas leaderboard-da sayılır. Adi reklam üçün mövcud 4 cəhd + 5 dəqiqə aktiv oyun, nəticə fasiləsi və sayğac sıfırlama qaydaları saxlanılır.
- İlk reklamsız gün ilk uğurlu onlayn girişdən həmin günün Bakı gecəyarısınadəkdir. İlk açılış oflayndırsa, məşq reklamsızdır və güzəşt günü başlamır.
- Qəbul edilmiş risk: dəyişdirilmiş tətbiq cihazdakı reklam bildirişini saxtalaşdıra bilər. Cari Spark modelində SSV-ni qəbul edib yoxlayan server yoxdur; qeyd üzrə təkrarı bloklamaq baxışın həqiqiliyini təsdiqləmir.

### 5. Real pul alışları və bərpa
- Alış üçün oyun hesabına giriş tələb olunur. Tətbiq Google Play-dən tamamlanmış alış gördükdən sonra alış tokenini həmin oyun hesabına bağlayıb hüququ verir. Gözləyən (PENDING) alış hüquq yaratmır.
- Eyni tokenlə təkrar sikkə verilməməlidir. Daimi görünüşlər bərpa olunur; artıq çatdırılmış sikkə paketi yenidən verilmir.
- Qəbul edilmiş risk: Spark-da token saxlamaq onun həqiqətən Google tərəfindən verildiyini müstəqil təsdiqləmir. Saxta alış məlumatı, refund və yarımçıq çatdırılma problemləri qalır. Bu model təhlükəsiz backend təsdiqini əvəz etmir.
- Tokenin hesaba bağlanması, çatdırılma/acknowledgement/consume ardıcıllığı və nasazlıqdan bərpa texniki layihələndirmə və cihaz sınağı tələb edir; işləyən təminat sayılmır.

Məzmun kataloqu hazırlanıb və bölmə 23-də təsdiqlənib. Mühərrik, real pul qiymətləri və yuxarıdakı texniki sınaqlar açıq qalır. Kod yalnız ayrıca tapşırıqla yazılacaq.


## 23. Təsdiqlənmiş vahid V1 məzmun kataloqu

3 noyabr 2026. Bölmə 20-də verilmiş səlahiyyətlə hazırlanıb və istifadəçi kataloqu təqdim edilən formada təsdiqləyib. Aşağıdakı konkret adlar, saylar, əldəetmə yolları, qiymət/çəki bölgüsü, reklam irəliləyişi və dekorasiya nüsxə modeli **təsdiqlənib**. Bölmə 19-un qiymət pillələri və çəkiləri, bölmə 20-nin nailiyyət şərtləri və bölmə 22-nin işləmə qaydaları dəyişmir. Bu, məzmun siyahısıdır; kod, hazır qrafik resurs, ekran maketi və ya işləyən alış sistemi deyil. Görünüş adları bədii istiqamətdir; rəng kodu, ölçü, şrift və digər dizayn tokenləri seçilmir. Render və vizual yoxlama aparılmayıb.

### Say və istifadə qaydası

- 6 başlanğıc üslubu × 5 kateqoriya = 30 pulsuz baza hissəsi.
- Əlavə 38 hissə: 15 sikkəlik, 15 reklamlıq, 5 birbaşa real pulla və 3 yalnız nailiyyətlə açılan hissə. Ümumi bina hissəsi sayı 68-dir.
- 12 dekorasiya/yol elementi: ağac, skamya, işıq və yol kateqoriyalarının hərəsində 3 seçim.
- Üslub ayrıca bonuslu əşya deyil, beş baza hissəsinin hazır birləşməsidir. Bütün 30 baza hissəsi ilk gündən açıqdır və kateqoriyalar üzrə qarışdırıla bilər.
- Hər hissə yalnız göstərilən yolla açılır. Sikkə/reklam/real pul seçimləri fərqli hissələrdir; nailiyyət hissələrinin alternativ əldəetmə yolu yoxdur.
- Açılan bina hissəsi hesabda daimi və təkrar istifadəlidir. Dekorasiyada qiymət bir yerləşdirilə bilən nüsxə üçündür; əlavə nüsxə yenidən alınır. İnventara qaytarılan nüsxə yenidən yerləşdirilir, sikkə geri verilmir. Bu nüsxə modeli təsdiqlənib.
- Bütün bina hissələri kosmetikdir: fizika, mərtəbə ölçüsü, tarazlıq, sakin sayı və XP qaydasını dəyişmir. Dəyər çəkisi yalnız mövcud bina qiyməti hesabına daxildir. Dekorasiya bonus vermir.

### Altı pulsuz başlanğıc üslubu

Hər sətirdə sıra belədir: divar/material; pəncərə; balkon/giriş; dam; işıqlandırma. Hər ad bir ayrıca hissədir, qiyməti və bina dəyərinə çəkisi 0-dır.

1. **İçərişəhər:** Əhəngdaşı divar; Tağlı pəncərə; Daş giriş; Düz parapet dam; Giriş fənəri.
2. **Kərpic məhəllə:** Kərpic hörgü; Çərçivəli pəncərə; Dəmir balkon; Maili kirəmit dam; Divar fənəri.
3. **Sahil:** Suvaq fasad; Geniş pəncərə; Açıq eyvan; Yastı terras dam; Eyvan işığı.
4. **Müasir şəhər:** Panel fasad; Lent pəncərə; Şüşəli giriş; Texniki düz dam; Giriş işıq zolağı.
5. **Bağ evi:** Taxta üzlük; Bölməli pəncərə; Taxta veranda; İkiyamaclı dam; Veranda fənəri.
6. **Emalatxana:** Beton fasad; Metal çərçivəli pəncərə; Metal giriş; Metal örtüklü dam; Sənaye divar lampası.

Balkon, veranda, giriş və dam formaları dekorativdir; əlavə oynanılan sahə və sakin yaratmır. İşıqlandırma seçimi yalnız görünüşdür.

### Sikkə ilə açılan 15 əlavə hissə

Hər kateqoriyada 3 hissə var. Sıra və qiymət/çəki hər sətirdə eynidir: **sadə 100 sikkə / +4; orta 250 sikkə / +10; xüsusi 600 sikkə / +24**. Hər hissə ayrıca alınır; level şərti yoxdur.

- Divar/material: İncə suvaq; Naxışlı kərpic; Mozaika üzlük.
- Pəncərə: İkiqanadlı pəncərə; Künc pəncərə; Vitraj pəncərə.
- Balkon/giriş: Sadə məhəccərli balkon; Oyma giriş; Sütunlu portal.
- Dam: Saçaqlı dam; Mansard dam; Pilləli tac dam.
- İşıqlandırma: Qoşa giriş lampası; Karniz işığı; Naxışlı fasad işığı.

Bu bölmədəki bütün hissələrin daimi açılma xərcinin cəmi 4.750 sikkədir. Bu, məcburi inkişaf xərci deyil; hamısının alınması ərazi genişlənmələri ilə eyni sikkə büdcəsinə rəqabət yaradır.

### Reward reklamla açılan 15 əlavə hissə

Hər kateqoriyada 3 hissə var. Sıra və tələb/çəki hər sətirdə eynidir: **sadə 3 baxış / +4; orta 5 baxış / +10; xüsusi 8 baxış / +24**. Baxışlar seçilmiş hissənin irəliləyişinə yazılır; bir baxış bir hissəyə sayılır. Bu bölgü təsdiqlənib. İrəliləyiş qorunur; açıldıqdan sonra təkrar reklam tələb olunmur.

- Divar/material: Zolaqlı suvaq; Çınqıl üzlük; Həndəsi relyef.
- Pəncərə: Jalüzlü pəncərə; Tağlı qoşa pəncərə; Dairəvi panorama.
- Balkon/giriş: Çiçəkli eyvan; Torlu giriş; Qəfəs naxışlı balkon.
- Dam: Kənar haşiyəli dam; Şüşəli dam örtüyü; Dalğalı dam örtüyü.
- İşıqlandırma: Qapıüstü lampa; Şaquli işıq zolağı; Ulduz naxışlı işıq.

Hamısını açmaq üçün cəmi 80 tamamlanmış reward baxışı tələb olunur; davam reklamı bu hissələri açmağa sayılmır. Hissə açma reward-u əlavə sikkə və XP vermir. Reklam təqdim edilməzsə irəliləyiş itmir və açılma təmin edilmiş sayılmır. Reward qeydi və ilk gün güzəşti bölmə 22-yə tabedir; könüllü reward ilk gün də əlçatandır.

### Birbaşa real pulla açılan 5 daimi hissə

Hər kateqoriyaya bir ayrıca məhsul təsdiqlənib. Hamısı xüsusi pillədədir, bina dəyərinə çəkisi +24-dür. Paket almaq şərti yoxdur; real pul qiymətləri və platforma məhsul identifikatorları hələ seçilmir.

- Divar/material: Mirvari keramika.
- Pəncərə: Kristal çərçivə.
- Balkon/giriş: Filigran portal.
- Dam: Şüşə tac.
- İşıqlandırma: İşıq şəlaləsi.

Bunlar nailiyyət hissələrinin surəti və ya bacarıq nişanı deyil. Fiziki üstünlük və ayrıca XP/sikkə vermir. Hesaba giriş, tamamlanmış alış, token qeydi və daimi hüququn bərpası bölmə 22-yə tabedir; təhlükəsiz server təsdiqi hazır sayılmır.

### Yalnız nailiyyətlə açılan 3 xüsusi hissə

Hər biri xüsusi pillədir və istifadə edildiyi binaya +24 çəki verir. Açılma hesab üzrə birdəfəlik və daimidir; sikkə, reklam və real pulla əldə edilmir.

- **Zirvə tacı — dam:** bir cəhddə 50 uğurlu mərtəbə. Mövcud 50 XP + 50 sikkə pillə bonusuna əlavə olaraq hissə açılır.
- **Dəqiqlik çərçivəsi — pəncərə:** bir cəhddə 20 dəqiq yerləşdirmə; ardıcıllıq tələb olunmur. Mövcud 40 XP + 40 sikkə bonusu saxlanılır.
- **Tarazlıq nişanlı giriş — balkon/giriş:** hesab üzrə 30 xilasetmə. Mövcud 50 XP + 50 sikkə bonusu saxlanılır. Reklamla sabitləşdirmə xilasetmə sayılmır.

Reklamla davam eyni cəhddir: hündürlük və dəqiq eniş sayğacları qorunur. Yeni nailiyyət kateqoriyası, yeni XP mənbəyi və əlavə sikkə bonusu yaradılmır. Bu hissələrin görünüşü yalnız bacarıq açılmasını ifadə edir, tarazlığı yaxşılaşdırmır.

### Sikkə ilə alınan 12 dekorasiya və yol elementi

Hər sətirdə 3 ayrıca seçim var. Sıra və bir nüsxənin qiyməti eynidir: **kiçik 25; orta 75; böyük 150 sikkə**. Bunlar bölmə 19-un qiymət pillələridir. Dəqiq sahə ölçüləri şəhər yerləşdirmə prototipində seçiləcək; burada yeni ölçü vahidi təyin edilmir.

- Ağac: Tək ağac; Ağac cütü; Kiçik ağaclıq.
- Skamya: Tək skamya; Qarşılıqlı skamyalar; Skamyalı istirahət guşəsi.
- İşıq: Tək küçə fənəri; Qoşa küçə fənəri; Fənər sırası.
- Yol: Qısa düz yol; Dönüş yol hissəsi; Uzun düz yol.

Bir dənə hər seçimdən almaq 1.000 sikkədir. Sahə tuturlar, inventara qaytarılırlar; XP, passiv gəlir və bina dəyəri bonusu vermirlər. Yol görünüş və yerləşdirmə üçündür; yeni nəqliyyat, xidmət və ya yol bağlantısı şərti yaradılmır. Bina işıqlandırma hissəsi ilə şəhər fənəri ayrı məzmundur.

### Kataloqun sərhədi və yoxlama

- Bina kateqoriyaları üzrə say: divar 13, pəncərə 14, balkon/giriş 14, dam 14, işıqlandırma 13; cəmi 68. Nailiyyət hissələri bu saylara daxildir.
- Eyni binada hər kateqoriyadan yalnız istifadə olunan bir hissənin çəkisi hesablanır. Baza hissəsi 0, sadə +4, orta +10, xüsusi +24; bütün bina üçün maksimum +120. Hər mərtəbədə təkrar hesablanmır.
- Altı baza üslubu ayrıca satınalma deyil. Hazır bina almaq onun hissələrini hesabda açmır. Öz və alınmış binada yalnız hesabda açıq hissələrlə redaktə qaydası, aktiv elanın əvvəl geri götürülməsi və alınmış binanın təkrar satılmaması qüvvədədir.
- Mövcud 500, 1.200 və 2.500 sikkə paketləri saxlanılır; 68 hissəyə və 12 dekorasiyaya daxil deyil. Real pul qiymətləri açıqdır. Ərazilər və nailiyyətlərin digər pillələri bu kataloqda yenidən qurulmur.
- Sənəd səviyyəsində saylar, məbləğlər, xüsusi açılma şərtləri və +120 limiti tutuşdurulub. Oyun balansı ölçülməyib; qrafik uyğunluq, kontrast, animasiya və şəhərdə faktiki tutum sınaqdan keçirilməyib.
- İstifadəçi kataloqu bu formada təsdiqləyib; adlar, saylar və əldəetmə bölgüsü qəbul edilib. Bu təsdiq implementasiya və ya ölçülmüş balans təminatı deyil. Mühərrik seçimi, real pul qiymətləri və texniki layihələndirmə növbəti ayrıca işlərdir; bu tapşırıqda kod yazılmır.


## 24. AppBackend və texnologiya seçimi

Son qərar: Kotlin + Compose (Unity yox). Seçim tarixi: [archive/plan-history.md](archive/plan-history.md).

## 25. Kotlin + Compose hazırlama planı

Android-only Kotlin + Compose seçimi üçün mərhələlər, Arrowz təkrar istifadə xəritəsi və AppBackend Wobbrik adapteri [kotlin-compose-plan.md](kotlin-compose-plan.md) sənədində hazırlanıb. Bu, koddan əvvəl baxış üçün texniki plandır. Yeni təkliflər: PostgreSQL-də vahid wallet/mülkiyyət/progression authority-si, AppBackend qəbul vaxtı və ayrıca Wobbrik domain paketi. Bunlar əvvəlki texniki mənbə seçimini avtomatik təsdiqlənmiş kimi əvəz etmir. Əvvəlki məhsul balansı, kataloq və V1 əhatəsi saxlanılır. Arrowz-un lokal wallet-i, entitlement union-u, reklam siyasəti və client ack/consume axını olduğu kimi köçürülmür. Planın hazırlanması kod yazılmasına icazə deyil.

### Vahid əsas mənbə və vaxt qərarının təsdiqi

3 noyabr 2026: istifadəçi PostgreSQL-in oyun iqtisadiyyatı, progression, mülkiyyət, inventar, şəhər və bazar üçün vahid əsas mənbə olmasını təsdiqləyib. Firebase Auth kimlik üçün qalır, Room keş/outbox-dur. Həftə, bərabərlikdə sıralama və ilk reklamsız gün üçün AppBackend-in PostgreSQL-də qəbul vaxtı əsas götürülür; bu, əvvəlki Firebase qəbul vaxtı mənbəyini əvəz edir. Bakı həftəsi və məhsul balansı dəyişmir. Dəqiq qəbul/retry/həftə sərhədi müqaviləsi docs/kotlin-compose-plan.md bölmə 5-dədir. Hesab konflikti, refund, retention və transfer siyasətləri açıqdır. Bu təsdiq kod yazmaq tapşırığı deyil; implementasiya aparılmayıb.

4 noyabr 2026: istifadəçi şəhərin yuxarıdan ikiölçülü x/y planını və eyni şəhərin həcmli görünüşünü seçib. Dəqiq koordinat/sahə, təhlükəsiz boşluq və 12 dekorasiyanın ölçüləri açıq saxlanılır; bu mərhələdə yalnız həmin rəqəmlərdən asılı olmayan server hissəsi hazırlanır. Başlanğıc 8 və genişlənmələrin +4/+5/+6/+7/+8 standart düz bina ekvivalenti dəyişmir; bunlar sərt bina sayı limiti deyil. Əyri tikinti formasının şəhər footprint-inə çevrilməsi ayrıca müqavilə tələb edir.


### Torpaq/dekor ölçülərinin təsdiqi — 8 noyabr 2026

İstifadəçi torpaq formalarını və 12 dekorun ilkin ölçülərini təqdim olunan formada təsdiqlədi. Konkret ölçülər və dəqiq konturlar: [footprint qərarı](reviews/2026-11-08-city-footprint-proposal.md); texniki müqavilə: [city geometry](kotlin-compose-plan.md). Bu qərar əvvəlki “dəqiq sahə ölçüləri açıqdır” qeydlərini əvəz edir. Başlanğıc 8 və +4/+5/+6/+7/+8 ekvivalent, qiymətlər, level hüquqları və nüsxə modeli dəyişmir. 10 vahid boşluq yol hissələrinə də tətbiq edilir, bitişik yol istisnası yoxdur. UI/gameplay implementasiyası və mərhələ qəbulu deyil.
