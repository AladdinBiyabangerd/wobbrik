# Torpaq və dekor footprint təklifi — 8 noyabr 2026

Status: **8 noyabr 2026-da istifadəçi tərəfindən təsdiqlənmiş ilkin oyun parametrləri**. Domain/data implementasiyası deyil. Branch: `docs/city-footprint-proposal`, base `15f2551`. İstifadəçi təqdim edilmiş konkret təklifə “okeydir” cavabı ilə təsdiq verib.

## Dəyişməyən əsas

`docs/kotlin-compose-plan.md`-dəki cari `city-geometry-v1` əsasdır: 1 oyun vahidi = 1000 tick, düz bina 100×100 vahid, obyektlər arasında 10 vahid Chebyshev boşluq, quarter-turn dönmə. Köhnə backend sənədindəki sərbəst dönmə/boşluqsuz yerləşdirmə tarixi bu yeni müqaviləni əvəz etmir. Tikinti əyriliyi saxlanır, sahə real proyeksiyadan hesablanır. Burada metr və ekran pikseli nəzərdə tutulmur.

## Torpaq forması və ölçüləri

Hesab modulu 110×110 vahiddir (110000×110000 tick). Modul yalnız sahə balansı və nümunə yerləşmə üçün istifadə olunur; obyektlərə məcburi grid/snap qaydası təklif edilmir. Hər modulun ortasında 100×100 düz bina qonşusundan 10 vahid aralı yerləşir. Xarici kənardakı 5 vahid nümunə payıdır, məcburi setback deyil.

- Pulsuz başlanğıc: düzbucaqlı 440×220; yerli kontur `(0,0),(440,0),(440,220),(0,220)`. 4×2 modul, sahə 96800 vahid², 8 düz bina üçün nümunə.
- `land_1`: düzbucaqlı 220×220. 2×2 modul, 48400 vahid², +4 ekvivalent; 150 XP / 200 sikkə.
- `land_2`: L-forma, 330×220 bounding box; kontur `(0,0),(330,0),(330,110),(220,110),(220,220),(0,220)`. 5 modul, 60500 vahid², +5 ekvivalent; 500 XP / 400 sikkə.
- `land_3`: düzbucaqlı 330×220. 3×2 modul, 72600 vahid², +6 ekvivalent; 1800 XP / 900 sikkə.
- `land_4`: L-forma, 440×220 bounding box; kontur `(0,0),(440,0),(440,110),(330,110),(330,220),(0,220)`. 7 modul, 84700 vahid², +7 ekvivalent; 3900 XP / 1500 sikkə.
- `land_5`: düzbucaqlı 440×220. 4×2 modul, 96800 vahid², +8 ekvivalent; 7650 XP / 2500 sikkə.

Düzbucaqlının konturu `(0,0),(W,0),(W,H),(0,H)`-dir; bütün konturlar sonda ilkin nöqtəyə bağlanır. Lokal origin torpaq konturunun aşağı sol küncüdür, x sağa/y yuxarı artır. Başlanğıc torpaq world origin-dədir. L-formalar uzun ensiz 5×1 və 7×1 zolaqlar əvəzinə daha yığcam sahə verir; girinti bütün sahib olunan torpağın union yoxlamasını tələb edir. Ümumi sahə 459800 vahid² = 38×12100; cəmlənən ekvivalentlər 8/12/17/23/30/38-dir. Bunlar sərt bina sayı limitləri deyil.

Hər genişlənmə sabit ölçülü bir parcel-dir, oyunçu onu mövcud torpağın istədiyi tərəfində/yerində yerləşdirir, 0/90/180/270° döndərə bilir. Bu, əvvəlcədən təyin edilmiş xəritə deyil. Əvvəlki torpaq alışını məcburi etməyən XP/qiymət qaydası saxlanır. Yer seçimi alma hüququndan ayrıdır; alışın yerləşdirməyə atomik bağlanması bu təklifdə həll olunmur.

Yeni parcel mövcud union ilə müsbət uzunluqlu ortaq sərhəd paylaşır; yalnız künc təması keçmir. Yeni təklif: torpaq parcel-ləri sahə üzrə üst-üstə düşməsin; əlavə clearance yoxdur. Tam tick koordinatları istifadə edildiyinə görə ən kiçik müsbət ortaq seqment 1 tick-dir; ayrıca oyun minimumu əlavə edilmir. Torpağın və obyektlərin daxili parcel sərhədini keçməsi qəbul edilmiş union qaydasına tabedir.

## 12 dekorun tutulmuş sahəsi

Aşağıdakılar görünüşün rəsmi və ya hündürlüyü deyil, serverin yoxladığı yer proyeksiyasıdır. Ağacın tacı, skamya qrupu və fənər qrupunun yerdəki ümumi eni bu sərhədə uyğun hazırlanmalıdır; hazır assetlərin ölçülərə uyğunluğu yoxlanmayıb. Dairə/ellips collision əvəzinə aşağıdakı dəqiq düz kənarlı konturlar təklif olunur. Dönmə 0/90/180/270°; dekor origin-i göstərilən bounding box-un mərkəzidir. Ölçülər vahiddir, tick üçün 1000-ə vurulur.

- Tək ağac: 30×30 düzbucaqlı — 25 sikkə.
- Ağac cütü: 70×30 düzbucaqlı — 75 sikkə.
- Kiçik ağaclıq: 80×80 düzbucaqlı — 150 sikkə.
- Tək skamya: 40×20 düzbucaqlı — 25 sikkə.
- Qarşılıqlı skamyalar: 50×60 düzbucaqlı, ortadakı aralıq da qrupun sahəsidir — 75 sikkə.
- Skamyalı istirahət guşəsi: 90×80 düzbucaqlı — 150 sikkə.
- Tək küçə fənəri: 10×10 düzbucaqlı — 25 sikkə.
- Qoşa küçə fənəri: 40×10 düzbucaqlı — 75 sikkə.
- Fənər sırası: 100×10 düzbucaqlı — 150 sikkə.
- Qısa düz yol: 100×30 düzbucaqlı — 25 sikkə.
- Dönüş yol hissəsi: 100×100 bounding box daxilində eni 30 olan L. Aşağı-sol origin ilə kontur `(0,0),(100,0),(100,30),(30,30),(30,100),(0,100)`; dekor origin-i üçün hər koordinatdan 50 çıxılır. Sahəsi 5100 vahid²; boş 70×70 girinti tutulmuş sahə sayılmır — 75 sikkə.
- Uzun düz yol: 210×30 düzbucaqlı — 150 sikkə.

Düzbucaqlı dekor konturu mərkəz origin ilə `(-W/2,-H/2),(W/2,-H/2),(W/2,H/2),(-W/2,H/2)`-dir. Hər seçim bir ayrıca alınan/yerləşdirilən nüsxədir; qrup daxili elementləri bölünmür. Qrupun düzbucaqlısı daxilində başqa obyekt yerləşdirilmir. Dönüş yolunun boş girintisi isə real union-un xaricidir və 10 vahid boşluq saxlanmaqla istifadə oluna bilər.

## Seçimin nəticələri və təsdiq sərhədi

- 10 vahid boşluq dekor–dekor və dekor–bina cütlərinə də aiddir. Yol hissələri bitişmir və fasiləsiz yol şəbəkəsi yaranmır. Yol üçün istisna bu təklifə daxil deyil; bitişik yol istənilərsə ayrıca qayda qərarı lazımdır.
- 10 vahid bina aralığına minimum 10 vahid enli dekor və iki tərəfdə 10 boşluq sığmır. Dekor üçün oyunçu ən azı 30 vahid bina aralığı saxlamalıdır; daha böyük dekorlar əlavə sahə sərf edir. 8 binanın nümunə doluluğu dekor sahəsi zəmanəti deyil.
- Ölçülər ölçülmüş balans deyil; 100×100 bina ilə müqayisədən seçilib. L-formalı torpaq/künc yol union-u bounding box kimi yoxlanmamalıdır.
- Təsdiq yalnız bu footprint/torpaq parametrlərinə aid olacaq. Oyun ekranının yeni dizaynı, UI implementasiyası, placement aktivləşdirilməsi, main merge, push və deploy təsdiqi deyil.

## Yoxlamalar və növbəti addım

Mənbələr: `docs/game-plan.md` §7, §23; `docs/kotlin-compose-plan.md` şəhər həndəsəsi bölməsi; AppBackend `docs/wobbrik-game-api.md` torpaq hüquqları/union qaydaları. Hər iki typed geometry review oxunub. Server/mobile kod dəyişmir, CI tətbiq olunmur. Rəqəmlərin və nümunə yerləşmələrin fokuslu hesab yoxlaması PASS (6 parcel tutumu, pairwise boşluq, cəm sahə/qiymət və L-yol sahəsi). Statik sxem render edilib və görüntüyə baxılıb; 3 mövcud token kontrast cütü PASS. İnteraktiv canvas runtime-ı yoxlanmayıb; cihaz/live HTTP/balans və hazır artwork uyğunluğu sübutu yoxdur.

Növbəti addım: təsdiqlənmiş parametrlərlə ayrıca backend domain/data placement vahidinə başla. Typed/provenance-ni yenidən qurma. Main üçün ayrıca OK tələbi saxlanır.

Lokal sənəd review-u: Android/backend konturları, ölçülər, origin və clearance eyniliyi yoxlanıldı. Sxemdə L-parcel-in y istiqaməti müqaviləyə uyğunlaşdırıldı. Canvas TypeScript check PASS; runtime yoxlanmayıb. `git diff --check` PASS. Sənəd-only vahid olduğuna görə build/test və main CI işlədilməyib. Main merge ayrıca OK gözləyir.
