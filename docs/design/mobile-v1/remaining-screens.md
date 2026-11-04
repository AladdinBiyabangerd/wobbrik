# Qalan mobil ekranlar — 4 noyabr 2026

Built-in imagegen ilə yaradılıb. Üslub mənbəyi `mobile-primary-v2.png`, palitra mənbəyi `tokens.css`-dir. Hər PNG dörd portret ekranı yan-yana göstərən konsept lövhəsidir; ayrıca cihaz screenshot-u deyil.

- `inventory-market-v2.png`: inventar, bina detalları, bazar, kolleksiya. İlkin `inventory-market.png` arxivdir. Mərtəbə mətnləri minimum 10 saxlama/satış həddinə düzəldilib.
- `play-progress-v2.png`: fasilə, nəticə, nailiyyətlər, rekordlar. İlkin `play-progress.png` arxivdir. Enerji göstəricisi çıxarılıb; 12 mərtəbənin adi mükafatı 12 sikkə və 12 XP-dir.
- `account-states.png`: hesab, parametrlər, boş inventar, bazarda bağlantı xətası.

İlkin tam promptlar `inventory-market-prompt.txt`, `play-progress-prompt.txt`, `account-states-prompt.txt`; son redaktə promptları `remaining-edit-prompts.txt` faylındadır.

## Yoxlama sərhədi

Son PNG-lərə vizual baxılıb. Mətnlər, əsas düymələr, alt naviqasiya və təhlükəsiz kənarlar portret konseptində yoxlanıb. Palitranın altı semantik kontrast cütü 4.5 həddini keçir; generativ bitmap-in hər pikselinə aid zəmanət deyil.

Şəkildəki binalar illüstrativ thumbnail-dir: mərtəbə mətnləri ilə çəkilmiş modul sayı dəqiq geometry sübutu deyil. Sikkə qiymətləri, XP səviyyə barı, nailiyyət adları və bonusları dizayn nümunələridir; təsdiqlənmiş game-plan kataloqunu/balansını dəyişmir. Kolleksiyanın altı pulsuz başlanğıc üslubunun hamısı bu tək görünüşdə göstərilmir. Bina detalında sakin, dəyər və hissə redaktəsi üzrə əlavə görünüşlər qalır.

Bu dəst bütün funksional axınları bitirmir: alış təsdiqi, elan yaratma/geri götürmə, reklam/ödəniş əldəetmə şərtləri, sikkə paketləri, yüklənmə və digər xəta vəziyyətləri ayrıca qalır. Ana ekrana birbaşa şəhər girişinin düzəlişi də əvvəlki dəstdə açıqdır.

Telefon portret konseptidir; kiçik/geniş telefon, landşaft, planşet, böyük şrift və faktiki 48dp touch target/runtime yoxlanmayıb. Responsiv implementasiya tamamlanmış sayılmır. Domain/API/DTO/schema və iqtisadi qaydalar dəyişmədiyindən AppBackend dəyişikliyi tələb olunmur. Şəhərə əlavə etmə düyməsi server placement icazəsinin açıldığı mənasına gəlmir.
