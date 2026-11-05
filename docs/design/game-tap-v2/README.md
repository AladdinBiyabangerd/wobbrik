# Oyun ekranı — toxunaraq buraxma, 4 noyabr 2026

İstifadəçinin son düzəlişi həm telefon, həm də planşet üçün əsasdır. Bu qovluq əvvəlki mobile-v1 və tablet-v1 lövhələrindəki yalnız aktiv oyun və fasilə görünüşlərini əvəz edir. Digər ekranlar əvvəlki mənbələrdə qalır.

## Cari renderlər

- [phone.png](phone.png): telefon portret — aktiv oyun və pauza.
- [tablet-landscape.png](tablet-landscape.png): planşet landşaft — aktiv oyun və pauza.
- [tablet-portrait.png](tablet-portrait.png): planşet portret — aktiv oyun və pauza.

Built-in imagegen ilə yaradılan konsept lövhələridir; hər PNG iki vəziyyət göstərir. Promptlar eyni qovluqdadır. Mövcud [tokens.css](../mobile-v1/tokens.css) istifadə olunur; yeni palitra yoxdur.

## Davranış

5 noyabr düzəlişi: kranın gövdəsi və yuxarı asılma nöqtəsi sabit qalır.
Sağa-sola ip/asqı və asılmış mərtəbə hərəkət edir; səhnə kranla birlikdə pan etmir.

- Oyun bütün pəncərənin səhnəsidir. Aktiv oyunda yalnız bir düymə var: sağ yuxarı təhlükəsiz sahədə pauza. Sol/sağ menyu, geri/ana ekran, wallet, logo, alt idarəetmə rəfi və ayrıca Burax düyməsi yoxdur.
- Oyun sahəsinə toxunmaq bir mərtəbəni buraxır. Mərtəbə düşərkən, eniş qiymətləndirilərkən, server təsdiqi gözlənərkən və fasilədə buraxma bağlıdır. Pauza toxunuşu oyun sahəsinə ötürülmür.
- Mərtəbə sayı və rekord kiçik, interaktiv olmayan göstəricilərdir. İlk cəhddə “Buraxmaq üçün toxun” qısa mətn təlimatıdır; düymə deyil və öyrənildikdən sonra itir.
- Pauza fizikanı və oyun input-unu dayandırır. Eyni səhnənin üzərində tündləşdirici qat və mərkəzdə Fasilə paneli açılır. Paneldə yalnız Davam et, Yenidən başla, Menyuya qayıt var; ayrıca səs/bağla düyməsi yoxdur. Panel xaricinə toxunmaq oyunu davam etdirmir və mərtəbə buraxmır.
- Davam et oyunu qaldığı yerdən açır; həmin toxunuş mərtəbə buraxmır. Yenidən başla cari cəhddən çıxıb yeni cəhdə keçir; Menyuya qayıt ana menyuya aparır. Bu seçimlər məğlubiyyət/finish və ya yeni mükafat mənbəyi yaratmır; mövcud server cəhd/progression qaydaları əsasdır. Serverdə cəhdin bağlanması/yenidən başlanması implementasiya zamanı mövcud müqaviləyə uyğunlaşdırılmalıdır.
- Sistem geri əməliyyatı aktiv oyunda pauzanı açmalıdır; açıq pauzada təsadüfi buraxma yaratmamalıdır. Arxa plana keçmə və şəbəkə fasiləsi üzrə mövcud bərpa qaydaları qalır.
- Aktiv/fasilədə bina damsızdır; krandakı və qoyulmuş mərtəbələr eyni ölçüdədir. Dam yalnız məğlubiyyətdən sonra əlavə edilir. Rəsmlər dəqiq geometry modeli deyil.

## Adaptiv qayda və yoxlama sərhədi

Pəncərə ölçüsü dəyişəndə səhnə mövcud en/hündürlüyə uyğun çərçivələnir: bina üstü və krandakı modul görünən qalır. Landşaft planşetdə də yan panel/rail açılmır; əlavə en səhnəyə verilir. Pauza paneli oxunaqlı məhdud endə mərkəzdə qalır, qısa pəncərə/böyük şriftdə panel məzmunu scroll olur. Minimum hədəf tokens.css-dəki 48dp-dir. Safe inset və pauza sahəsi oyun input-undan ayrılır.

Bu iş PNG dizaynıdır: klik handler, fizika, runtime responsivlik, TalkBack, böyük şrift, real dp hədəfi və dinamik pəncərə yoxlaması aparılmayıb. Telefon landşaftı ayrıca render edilməyib. Konsept renderlərinin dəqiq ölçüləri və vizual tapıntılar review-dədir. AppBackend domain/API/DTO/schema, wallet və mükafat dəyişmədiyindən backend dəyişikliyi tələb olunmur; tam oyun axınının server inteqrasiyası hazır sayılmır.
