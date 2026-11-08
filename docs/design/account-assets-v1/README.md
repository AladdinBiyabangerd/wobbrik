# Hesab və ana ekran asset hazırlığı — 5 noyabr 2026

İstifadəçi çatışmayan asset-lərin təsdiqlənmiş dizayna uyğun ayrıca hazırlanmasını
seçdi. Built-in `image_gen` istifadə edilib; CLI/API fallback yoxdur.

- `home-city.png`: mobile-primary-v2.png-in soldakı ana ekran şəhəri.
- `wobbrik-logo.png`: həmin ana ekranın logo referansı.
- `account-hero.png`: account-states.png-in soldakı hesab illüstrasiyası.

Hər üç PNG alpha kanallıdır. Hesab hero-sunun ilkin önizləməsində rəngli halo
görüldüyü üçün yalnız fon təmizləmə redaktəsi istəndi. Sonra AppKit ilə hər üçü
GameTokens.Background (#061831) üzərində render edildi və görüntüyə baxıldı:
fon şəffafdır, halo görünmür. `alpha-preview.png` bu QA renderidir, ekran deyil.
Təkrarlama: `swift docs/design/account-assets-v1/render-preview.swift
docs/design/account-assets-v1/home-city.png docs/design/account-assets-v1/wobbrik-logo.png
docs/design/account-assets-v1/account-hero.png` — nəticə `/tmp/wobbrik-asset-qa.png`.
Promptlar `prompts.md`-dədir. Orijinal konsept PNG-ləri dəyişdirilmir.

Bu asset-lər hələ Android resurslarına və ekranlarına bağlanmayıb. Generativ
extraction orijinal piksellərin kəsilməsi deyil: şəhərin detallarında və logo
hərflərində fərqlər ola bilər. Təsdiqlənmiş mobile-v1/tablet-v1 dizaynları əsas
qalır; birəbir uyğun ekran tamamlandığı iddia edilmir. UI ikonları və dekorativ
elementlər növbəti ekran vahidində həmin dizaynlarla yoxlanmalıdır.

## Home üçün piksel qoruyan cutout — 8 noyabr 2026

İstifadəçi lövhədən ayırmanı, sonra isə imagegen əvəzinə lokal Python
maskalamasını açıq təsdiqlədi. `home-city-cutout-v2.png` mobile-primary-v2-nin
soldakı home illüstrasiyasının `(20,154)–(435,605)` crop-udur: **415×451 RGBA**.
RGB pikselləri mənbə ilə eynidir; yalnız alpha maskası dəyişir. Upscale,
generativ tamamlama, rəng və forma dəyişikliyi edilmir. Mənbənin öz aşağı
resolution-u böyük ekranlarda görünən yumşaqlığı məhdudlaşdırır.

`home-city-cutout-v2-mask.png` lokal GrabCut-un nəticəsinin tünd/açıq fonda
baxılmış alpha maskasıdır. `extract-home-city.py` mənbənin SHA256-sını yoxlayır,
maskanı tətbiq edir, RGB fərqinin sıfır olduğunu təsdiqləyir və həm dizayn,
həm də Android `home_city_cutout_v2.png` çıxışlarını yazır. Təkrar yaratmaq:
`uv run --with pillow python docs/design/account-assets-v1/extract-home-city.py`.
Əvvəlki generativ `home-city.png` və Android `home_city.png` saxlanır;
HomeScreen yeni cutout-a bağlanır. Bu dekorativ home asset-idir, real hesab
şəhərinin renderer-i deyil. Tam ekran vizual qəbulu home review-da izlənir.

İlk built-in imagegen cəhdi generativ rekonstruksiya verdiyinə görə runtime-a
qəbul edilmədi; QA namizədi gitignored home review qovluğundadır. Prompt:
“background-extraction; mobile-primary-v2-dən yalnız soldakı home şəhərini
şəffaf PNG kimi ayır; bütün bina, mərtəbə, mövqe və detalları saxla; UI/fon/mətn
olmasın; yenidən şərh etmə.” Son qəbul edilən asset imagegen çıxışı deyil.
