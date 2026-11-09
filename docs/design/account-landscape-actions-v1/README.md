# Hesab səhifəsi — telefon landşaftında görünən əməl (təklif, 9 noyabr 2026)

Status: **təklif, təsdiqlənməyib.** UI kodu yazılmayıb; implementasiya istifadəçinin seçdiyi variantın açıq təsdiqindən sonra ayrıca branch-də başlayır.

## Problem

Room keşi (main `f53eab3`) hesab/nailiyyətlər/kolleksiya səhifələrində canlı olmayan vəziyyətdə də keş kartını göstərir. Telefon landşaftında (2400×1080, 420dpi ≈ 914×411dp) sağ idarəetmə sütunu hündürlüyə sığmır: "Bağlantı yoxdur" görünür, **Yenilə** və **Ana ekran** qatın altında qalır (scroll ilə əlçatandır). Eyni sütun canlı Ready vəziyyətində də sığmır: **Oyna** yarımçıq görünür (`source/phone-landscape-live.png`) — bu, keşdən əvvəl də mövcud idi.

Telefon portret, planşet portret və planşet landşaftda hamısı görünür (`consistency-unchanged-portrait-tablet.png`); heç bir variant onları dəyişmir.

## Metod

Mockup-lar yalnız real `AccountPreviewActivity` renderlərindən (emulator-5586, `source/`) kəsilib yerləri dəyişdirilmiş elementlərdir: header, illüstrasiya, kart, status mətni və düymələr eyni piksellərdir; düymə/kart eni boş orta hissə çıxarılaraq qısaldılıb, C-də illüstrasiya kiçildilib. Yeni rəng, ikon, mətn və ya asset yoxdur. Alt zolaqdakı izah mətni yalnız lövhə qeydidir. Yenidən yaratmaq: `uv run --with pillow --with numpy python compose.py`.

## Variantlar (yalnız idarəetmə sütunu sığmayanda — telefon landşaftı)

- **A — sabit əməl sırası** (`option-a-sticky-actions-phone-landscape.png`): status mətni və əməllər sütunun altında sabitdir; Yenilə | Ana ekran yan-yana bir sırada (mövcud Kolleksiya | Nailiyyətlər sırası kimi). "Qonaq inşaatçı" + keş kartı yuxarıda scroll olur, kəsilən kənar fon rənginə solur. İllüstrasiya dəyişmir.
- **B — status əvvəl** (`option-b-status-first-phone-landscape.png`): sütunun sırası təsdiqlənmiş offline kompozisiyası kimidir: "Bağlantı yoxdur" → Yenilə → Ana ekran, keş başlığı və kartı altda (scroll). Düymələr tam endə qalır.
- **C — üç panel** (`option-c-three-panes-phone-landscape.png`): kiçildilmiş illüstrasiya | keş kartı | status + Yenilə + Ana ekran. Scroll olmadan hər şey görünür.

## Müqayisə

| | A | B | C |
|---|---|---|---|
| Yenilə scroll-suz görünür | bəli | bəli | bəli |
| Keş dəyərləri ilk baxışda | qismən (səviyyə, XP) | demək olar yox (yalnız sikkə) | tam |
| Təsdiqlənmiş dizayna yaxınlıq | hesab iyerarxiyası (məlumat yuxarı, əməl aşağı) saxlanır; düymələr yan-yana | offline ardıcıllığı birəbir; hesab kartı ikinci plana keçir | yeni üç panel kompozisiyası; illüstrasiya kiçilir |
| Canlı Ready üçün də işləyir | bəli (Oyna | Ana ekran sabit; Kolleksiya/Nailiyyətlər scroll) | Oyna yuxarıda olmalıdır — hesab səhifəsinin iyerarxiyasını dəyişir | ayrıca dizayn tələb edir |
| Böyük şrift (2.0) riski | sabit zona böyüyür; sərhəddən keçəndə hər şey scroll olur, status əvvəl | aşağı | yüksək (dar panellər) |

Tövsiyə: **A**. `tablet-v1/README.md` adaptiv qaydasına birbaşa uyğundur ("hündürlük azaldıqda köməkçi məzmun scroll olur; əsas əməl görünən idarəetmə sahəsində qalır"), illüstrasiya ölçüsünü və hesab iyerarxiyasını saxlayır, eyni qayda canlı Oyna problemini də həll edir. B ən mühafizəkar alternativdir.

## Açıq suallar / yoxlanmayanlar

- A seçilərsə: canlı vəziyyətdə sabit sıra "Oyna | Ana ekran" olsunmu, yoxsa yalnız offline/bərpa vəziyyətinə tətbiq edilsin?
- Kiçik telefon landşaftı, font 2.0, safe inset/kəsik və real toxunma hədəfləri yalnız implementasiyadan sonra render ilə yoxlanacaq. Mockup Compose screenshot-u deyil.
- Yeni asset/raster tələb olunmur.
