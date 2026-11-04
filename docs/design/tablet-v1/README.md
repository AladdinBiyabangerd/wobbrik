# Planşet dizaynları — 4 noyabr 2026

Aktiv oyun və fasilə üçün son mənbə [game-tap-v2](../game-tap-v2/README.md)-dir. Bu iki vəziyyətdə aşağıdakı köhnə rail, sağ panel, alt rəf və Burax düyməsi tətbiq edilmir: səhnəyə toxunaraq buraxılır, yalnız pauza düyməsi qalır. Digər ekranlar bu dəstdə qalır.

Mobil konseptlərin planşet üçün ayrıca kompozisiyaları. Built-in imagegen istifadə olunur; Android/Compose implementasiyası deyil. Palitra, spacing, radius və mətn ölçüləri üçün vahid mənbə [mobil tokens.css](../mobile-v1/tokens.css)-dir; yeni palitra yaradılmır. Şəhərin bədii mənbəyi təsdiqlənmiş city-plan/view və mobile-primary-v2-dir.

## Cari lövhələr

- [primary-landscape.png](primary-landscape.png): ana ekran, aktiv oyun, şəhər görünüşü və planı. Ana ekranda birbaşa Şəhərim girişi; səhnə və sağ idarəetmə paneli.
- [library-landscape.png](library-landscape.png): inventar, bina detalı, bazar, kolleksiya. Siyahı/grid və seçilmiş obyektin detalı birlikdə görünür.
- [progress-landscape.png](progress-landscape.png): fasilə, nəticə, nailiyyətlər, rekordlar.
- [account-landscape.png](account-landscape.png): hesab, parametrlər, boş inventar, bazarda bağlantı xətası.
- [portrait.png](portrait.png): portret planşetdə ana ekran, oyun, inventar və şəhər planı. Səhnənin altında üfüqi idarəetmə rəfi; inventarda iki sütun və aşağı detal paneli.

Hər PNG dörd konsepti birləşdirən lövhədir; ayrı cihaz screenshot-u deyil. Promptlar eyni qovluqdadır. `account-states-wide.png` ilkin geniş ekran variantıdır, cari əsas mənbə account-landscape-dir.

## Pəncərə ölçüsünə uyğun davranış

Cihaz modeli və orientasiya adı ilə deyil, mövcud pəncərənin eni/hündürlüyü ilə seçilir. Təklif olunan implementasiya sərhədləri: 600dp-dən dar compact, 600–839dp medium, 840dp və geniş expanded. Bunlar konsept qərarlarıdır; real məzmun və böyük şriftlə sonradan təsdiqlənəcək.

- Compact: mövcud mobil bir sütun; detal ayrıca səhifə/sheet, mobil naviqasiya. Split-screen planşet də bu rejimə keçə bilər.
- Medium: səhnə yuxarıda, idarəetmə rəfi aşağıda; inventar iki sütun, seçimin detalı aşağıda. Nəticə/fasilə qısa pəncərədə scroll-a keçir. Portret lövhəsi bu istiqaməti göstərir.
- Expanded: oyun/fasilə xaricində sol naviqasiya rail-i, əsas səhnə/grid və sağ detal/idarəetmə paneli. Təxminən 2:1 məzmun bölgüsü; inventar/bazar grid sütunları mövcud enə görə artır. Aktiv oyun tək bütöv səhnədir, yan panel yoxdur.
- Hündürlük azaldıqda köməkçi məzmun scroll olur; əsas əməl görünən idarəetmə sahəsində qalır. Safe inset, klaviatura, böyük şrift və minimum 48dp hədəflər implementasiyada ölçülməlidir.
- Seçim, scroll, oyun fasiləsi və kamera vəziyyəti pəncərə dəyişəndə saxlanır. Şəhər planı və görünüş eyni server şəhər vəziyyətinin iki təqdimatıdır.

Oyun/fasilə xaricində naviqasiya üçün əsas expanded qayda primary-landscape-dəki sol rail-dir. Digər generativ lövhələrdə logo, ikon dəsti, seçilmiş tab və termin fərqləri var; bunlar yeni naviqasiya müqaviləsi deyil. İnventar/bina detalında İnventar, bazarda Bazar, kolleksiyada Kolleksiya aktiv olur; eyni anda iki bölmə aktiv göstərilmir. Aktiv oyundan çıxış pauzadakı Menyuya qayıt vasitəsilədir; oyun/fasilədə naviqasiya rail-i yoxdur. Parametrlər vahid termindir (bəzi PNG-lərdə Ayarlar yazılıb).

## Qaydalar və yoxlama sərhədi

Aktiv oyun/fasilədə dam yoxdur; dam yalnız uduzandan sonra avtomatik əlavə olunur. Krandakı və qoyulmuş mərtəbələrin eni/dərinliyi/hündürlüyü eynidir. Nəticə nümunəsi 12 mərtəbə üçün 12 sikkə və 12 XP-dir. Mərtəbə sayları və bina rəsmləri dəqiq geometry deyil; thumbnail-də çəkilən modul sayı mətnin sayını sübut etmir. Qiymətlər, kolleksiya və nailiyyətlər illüstrativdir; yeni balans/qayda yaratmır.

Telefonun dörd cari PNG-si və yeni planşet renderlərinə vizual baxış review qeydindədir. Token kontrastı: 8.22–16.57, altı cüt 4.5 həddini keçir. Generativ PNG-nin hər pikselinin tokenlərə uyğunluğu, dp hədəfi, dinamik resize, real cihaz, böyük şrift, ekran oxuyucu və safe inset yoxlanmış sayılmır. Bu dəst işlək responsiv tətbiq deyil. Portret lövhəsi dörd əsas görünüşü əhatə edir; qalan ekranların ayrıca portret renderləri açıqdır. Alış təsdiqi, elan yaratma/geri götürmə, yüklənmə və mobil sənəddə açıq qalan əməliyyat görünüşləri bu işdə tamamlanmır.

Domain/API/DTO/schema, wallet/ledger və server placement icazəsi dəyişmir. AppBackend dəyişikliyi tələb olunmur: iş yalnız mövcud mobil axınların vizual kompozisiyasıdır. Yerləşdir düyməsi server icazəsinin açıldığını göstərmir.
