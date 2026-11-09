# Hesab səhifəsi — telefon landşaftında görünən əməl (təklif, 9 noyabr 2026)

Status: **Variant C, düzəliş c2 təsdiqləndi** (istifadəçi, 9 noyabr 2026: "C düzəlişi təsdiq, implementasiyaya başla"). İmplementasiya mənbəyi: `c2-live-phone-landscape.png` və `c2-offline-phone-landscape.png`. A, B və ilk C (yan-yana Kolleksiya/Nailiyyətlər) təsdiqlənməyib.

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

## Canlı Ready səhifəsi (istifadəçi sorğusu, 9 noyabr)

Mənbə: `source/live-phone-landscape-top.png` və `-scrolled.png` (canlı Ready, emulator-5586). Yenidən yaratmaq: `uv run --with pillow --with numpy python compose_live.py`.

- `live-current-phone-landscape.png`: cari vəziyyət — Oyna yarımçıq, Kolleksiya/Nailiyyətlər və Ana ekran qatın altında.
- `live-option-a-phone-landscape.png`: A — Oyna | Ana ekran altda sabit sırada; başlıq, kart və Kolleksiya/Nailiyyətlər yuxarıda scroll olur.
- `live-option-b-phone-landscape.png`: B — Oyna və Ana ekran sütunun başında; başlıq, kart və Kolleksiya/Nailiyyətlər altda (scroll).
- `live-option-c-phone-landscape.png`: C — üç panel: kiçildilmiş "Hesab" illüstrasiyası | kart | Oyna, Kolleksiya/Nailiyyətlər, Ana ekran; scroll yoxdur.

Tapıntı (mövcud defekt, keşdən asılı deyil): telefon landşaftında "Nailiyyətlər" düyməsinin yazısı iki sətrə bölünür ("Nailiyyətl / ər"). Mockup-lar real kəsiklərdən qurulduğu üçün bunu olduğu kimi göstərir; C-də görünür, A/B-də həmin sıra scroll zonasındadır. Düzəliş ayrıca qərar tələb edir.

## Seçim: Variant C, düzəliş c2 (istifadəçi, 9 noyabr)

İstifadəçi C-ni seçdi, şərt: heç bir düymə yazısı sətrə bölünməməli ("Nailiyyətlər" qırılmamalıdır). Düzəliş (`compose_c2.py`):

- `c2-live-phone-landscape.png`: sağ paneldə Oyna, Kolleksiya, Nailiyyətlər, Ana ekran — hamısı panelin tam enində alt-alta; Kolleksiya/Nailiyyətlər yan-yana deyil. Onların ikon+yazı pikselləri eyni 420dpi / font 1.0 canlı portret renderindən (`source/live-phone-portrait.png`, yazılar bir sətirdə) götürülüb; yazı ölçüsü və düymə üslubu dəyişməyib. Aralıq: Oyna→16dp→Kolleksiya→8dp→Nailiyyətlər→16dp→Ana ekran; 4 düymə 411dp hündürlüyə scroll-suz sığır.
- `c2-offline-phone-landscape.png`: eyni üç panel həndəsəsi; status, Yenilə, Ana ekran (onlar əvvəl də bir sətirdə idi).
- İllüstrasiya və kart panelləri C ilə eynidir. Bütün yazılara tam ölçüdə baxılıb: heç biri bölünmür/kəsilmir.

## Açıq suallar / yoxlanmayanlar

- A seçilərsə: canlı vəziyyətdə sabit sıra "Oyna | Ana ekran" olsunmu, yoxsa yalnız offline/bərpa vəziyyətinə tətbiq edilsin?
- Kiçik telefon landşaftı, font 2.0, safe inset/kəsik və real toxunma hədəfləri yalnız implementasiyadan sonra render ilə yoxlanacaq. Mockup Compose screenshot-u deyil.
- Yeni asset/raster tələb olunmur.
