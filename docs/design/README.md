# Təsdiqlənmiş şəhər dizaynı

4 noyabr 2026: istifadəçinin seçdiyi 2-ci və 3-cü şəkillər şəhər implementasiyasının yeganə vizual referanslarıdır. Bunlar eyni şəhərin iki rejimidir.

- [Plan](city-plan.png): yuxarıdan x/y yerləşdirmə, seçilmiş binanın konturu, döndər/ləğv et/yerləşdir və sahə əlavə et axını.
- [Şəhər görünüşü](city-view.png): eyni şəhərin həcmli təqdimatı, binaya baxış və ətrafında kamera döndürmə istiqaməti.

Yaşıl, üzvi formalı ada; bağlar, ağaclar, cığırlar, fəvvarə; rəngli və əyri binalar əsas bədii istiqamətdir. Tünd fon və firuzəyi aktiv idarəetmələr bu iki referansdan götürülür. Şəhər bölməsinin lokal naviqasiyası Şəhər / İnventar / Bazar-dır; bu, digər oyun funksiyalarını ləğv etmir.

Əvvəlki asfalt yol şəbəkəli neon şəhər/bazar maketi, alternativ üslub seçimləri, Unity/Compose müqayisə maketləri və köhnə arcade ekran dəsti silinib. Onların prompt və token faylları da implementasiya mənbəyi deyil. Digər ekranlar hazırlanarkən bu iki referansla uyğun dizayn qurulmalıdır; silinmiş ekranların təsdiqləndiyi fərz edilmir.

Şəkillər konseptdir: bina sayı, mərtəbə sayı, ölçü və qiymət üzrə məhsul qaydası yaratmır. Plan koordinatları ilə həcmli təqdimat eyni server şəhər vəziyyətindən alınmalıdır. Kamera döndürmənin texniki realizasiyası və performansı hələ yoxlanmayıb; ayrıca 3D mühərrik seçimi təsdiqlənmir.

Hazır tətbiq token faylı yoxdur. Implementasiyada tokenlər mərkəzləşdirilməli, kontrast ölçülməli və telefon/planşet, portret/landşaft pəncərələrində render edilərək baxılmalıdır. Bu təmizlikdə ekran kodu, yeni dizayn və cihaz renderi yaradılmayıb.
