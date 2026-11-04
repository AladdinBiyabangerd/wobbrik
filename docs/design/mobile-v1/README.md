# Mobil dizayn — ilk dəst, 4 noyabr 2026

İstifadəçi şəhər referanslarından yeni mərkəzi token dəstinin yaradılmasına icazə verib. `tokens.css` konsept üçün mənbədir; Android theme implementasiyası deyil. Mövcud şəhər referansları əvəz edilmir.

`mobile-primary-v2.png` cari düzəlişdir; `mobile-primary.png` əvvəlki versiya kimi saxlanır. Hər ikisində soldan ana ekran, oyun, şəhər görünüşü, şəhər planı. Built-in imagegen ilə yaradılıb; ilkin prompt `prompt.txt`-dədir. Son redaktə promptu: “Preserve all artwork, layouts, dimensions, colors and other labels exactly. Correct ONLY the second screen's large bottom cyan button label to exact Azerbaijani word Burax.”

## Yoxlama və məhdudiyyətlər

- PNG 1774×887: dörd mobil portret konsepti. İlkin və düzəldilmiş görüntülərə baxılıb; Burax yazısı düzəldilib. Bu, dörd ayrı cihaz screenshot-u deyil.
- Token kontrastları: text/background 16.57, muted/surface 8.22, text/surface-raised 10.24, on-accent/accent 11.45, success/surface 9.28, danger/surface 8.37. Hamısı 4.5 həddini keçir. Generativ PNG-də hər pikselin tokenlə eyniliyi təmin edilmir.
- Spacing 4/8/12/16/24/32, minimum hədəf 48, kənar boşluq 16 layihələndirmə tələbləridir; bitmap işlək layout və touch target testi deyil.
- Ana ekranda şəhərə birbaşa giriş sonrakı iterasiyada əlavə edilməlidir; hazırda şəhər ayrıca konsept kimi göstərilir. Dörd görünüş arasında həndəsi eynilik generativ təsvirlə təmin edilmir.
- Kiçik/geniş telefon, landşaft, böyük şrift, safe inset və dəyişən pəncərələr işlək UI-də hələ yoxlanmayıb. Planşet kompozisiyası və renderi yoxdur; responsivlik tamamlanmış sayılmır.
- Qalan ekranlar: fasilə, nəticə, inventar/bina detalı, bazar, kolleksiya, nailiyyətlər, rekordlar, hesab/parametrlər və boş/yüklənmə/xəta vəziyyətləri.
- Balans və göstəricilər nümunədir. Placement gələcək axının konseptidir; server icazəsi açılmayıb. Domain/API/schema dəyişmədiyinə görə AppBackend dəyişikliyi tələb olunmur.

## Adaptiv istiqamət

Telefon: əsas səhnə + aşağıda bir sütunlu idarəetmə; qısa pəncərədə köməkçi məlumat yığılıb sheet/scroll-a keçir, səhnə ilə düymələr üst-üstə düşmür. Landşaftda səhnə və idarəetmə yan-yana planlaşdırılır. Planşetdə ayrıca iki panel: səhnə və seçilmiş bina/inventar paneli; mobil ekranı böyütmək kifayət deyil. Bunlar növbəti renderlərdə yoxlanmalı qərarlardır.

## Dam və modul düzəlişi

4 noyabr 2026 istifadəçi təsdiqi: aktiv oyunda dam yoxdur; uduzduqda avtomatik əlavə edilir. Krandakı və yerləşdirilmiş mərtəbələrin eni/dərinliyi/hündürlüyü eynidir; ofset ölçünü dəyişmir. Şəhərdə bitmiş binaların damı qalır. Son edit promptu `floor-roof-edit-prompt.txt`-dədir, built-in imagegen istifadə olunur. Bu düzəliş backend və ya Android runtime implementasiyası deyil.
